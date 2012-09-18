/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.chemistry.opencmis.tools.mapper;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.PhotographicConversions;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.jpeg.JpegDirectory;

public class PropertyMapperExif extends AbstractPropertyMapper {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyMapperExif.class.getName());
    private static String EXIF_DATE_FORMAT = "yyyy:MM:dd HH:mm:ss";

    private Map<String, String> propMapExif = new HashMap<String, String> (); // tag to property id
    private Map<String, String> propMapGps = new HashMap<String, String> ();  // tag to property id
    private Map<String, String> propMapJpeg = new HashMap<String, String> (); // tag to property id
    
    protected Map<String, Object> propMap;

    public boolean initialize(String cfgPrefix, String typeKey, Properties properties) {
        super.initialize(cfgPrefix, typeKey, properties);
        reset();
        dateFormat = EXIF_DATE_FORMAT;
        buildIdMap("exif", propMapExif, properties);
        buildIdMap("gps", propMapGps, properties);
        buildIdMap("jpeg", propMapJpeg, properties);
        
        return true;
    }

    public void reset() {
        propMap = new HashMap<String, Object> ();
    }
    

    public Map<String, Object> getMappedProperties() {
        return propMap;
    }
    
    private void buildIdMap(String dirKey, Map<String, String> propMap, Properties properties) {
        Set<String> keys = properties.stringPropertyNames(); 
        String prefix = propPrefix + "." + dirKey + ".id.";
        for (String key : keys) {
            if (key.startsWith(prefix)) {
                String id = key.substring(prefix.length());
                String cmisPropId = properties.getProperty(key).trim();
                if (null == cmisPropId)
                    throw new MapperException("Configuration key " + key + " must have a value assigned");
                LOG.debug("Found mapping for type " + dirKey + " with " + id + " to " + cmisPropId);
                propMap.put(id,  cmisPropId);
            }
        }
    }

    public String getMappedTypeId(String mimeType) {
        return cmisTypeId;
    }

    public String getMappedPropertyId(String key) {
        return null;
    }

    public Object convertValue(String id, PropertyDefinition<?> propDef, String strValue) {
        return null;
    }

    private String getHexString(int tagType) {
        StringBuffer hexStr = new StringBuffer();
        hexStr.append(Integer.toHexString(tagType));
        while (hexStr.length() < 4)
            hexStr.insert(0, "0");
        hexStr.insert(0, "0x");
        return hexStr.toString();
    }
    /**
     * store the property id mapped to this tag in a JPEG file in a local map
     * @param dir
     *      directory of tag
     * @param tag
     *      tag
     */
    
    public void mapTagAndConvert(Directory dir, Tag tag, TypeDefinition td) {
        String propId = null;
        String hexStr = getHexString(tag.getTagType());
        
        if (GpsDirectory.class.equals(dir.getClass())) {
            propId = propMapGps.get(hexStr);
        } else if (ExifDirectory.class.equals(dir.getClass())) {
            propId = propMapExif.get(hexStr);
        } else if (JpegDirectory.class.equals(dir.getClass())) {
            propId = propMapJpeg.get(hexStr);
        } else
            propId = null;
        
        if (null != propId) {
            if (null != td) {
                PropertyDefinition<?> pd = td.getPropertyDefinitions().get(propId);
                if (null == pd)
                    throw new MapperException("Unknown property id " + propId + " in type definition " + td.getId());
                PropertyType pt = pd.getPropertyType();
                Object convValue = convertValue(dir, tag, pt);
                propMap.put(propId, convValue);
            } else
                propMap.put(propId, dir.getObject(tag.getTagType())); // omit conversion if no type definition is available
        }
    }
    
    public Object convertValue(Directory dir, Tag tag, PropertyType propType) {
        
        Object res = null;
        String hexStr = getHexString(tag.getTagType());

        // Handle all tags corresponding to their directory specifics
        if (GpsDirectory.class.equals(dir.getClass())) {
            // first check for those tags that need special consideration:
            if ( GpsDirectory.TAG_GPS_LONGITUDE == tag.getTagType()) {
                Object ref = dir.getObject(GpsDirectory.TAG_GPS_LONGITUDE_REF);
                boolean mustInv = ref != null && ref.equals("W");
                return convertGps(tag, dir, mustInv);
            } else if ( GpsDirectory.TAG_GPS_LATITUDE == tag.getTagType()) {
                Object ref = dir.getObject(GpsDirectory.TAG_GPS_LONGITUDE_REF);
                boolean mustInv = ref != null && ref.equals("S");
                return convertGps(tag, dir, mustInv);
            } else {
                String propId = propMapGps.get(hexStr);
                LOG.debug("Found GPS tag '" + tag + "\', property mapped is: " + propId);
                if (null == propId) {
                    LOG.info("Ignoring EXIF tag '" + tag + "\' no property mapped to this tag.");
                } else if (propType == null) {
                    // should not happen and is a configuration error: we have a property id but no type
                    LOG.error("Ignoring EXIF tag '" + tag + "\' no property type mapped to this tag.");
                }
                Object src = dir.getObject(tag.getTagType());        
                Class<?> clazz = src.getClass();
                if (clazz.equals(Rational.class)) {
                    // expect a CMIS decimal property
                    if (propType != PropertyType.DECIMAL)
                        throw new MapperException("Tag value has type Rational and expected CMIS Decimal, but found: " + propType + " for tag: " + tag);
                    double d = ((Rational) src).doubleValue();
                    res = d;
                } else if (clazz.equals(String.class)) {
                    if (propType != PropertyType.STRING && propType != PropertyType.ID && propType != PropertyType.URI &&
                            propType != PropertyType.HTML && propType != PropertyType.DATETIME)
                        throw new MapperException("Tag value has type String and expected CMIS String, but found: " + propType + " for tag: " + tag);
                    String s = ((String) src);
                    res = s;
                } else
                res = null;
            }
        } else if (ExifDirectory.class.equals(dir.getClass())) {
            // is there a property mapped to this tag?
            String propId = propMapExif.get(hexStr);
            LOG.debug("Found EXIF tag '" + tag + "\', property mapped is: " + propId);

            if (null == propId) {
                LOG.debug("Ignoring EXIF tag '" + tag + "\' no property mapped to this tag.");
            } else if (propType == null) {
                // should not happen and is a configuration error: we have a property id but no type
                LOG.error("Ignoring EXIF tag '" + tag + "\' no property type mapped to this tag.");
            } else {
                Object src = dir.getObject(tag.getTagType());        
                Class<?> clazz = src.getClass();
                // handle arrays and map them to multi-value properties
                if (clazz.isArray()) {
                    LOG.error("Found a multi-value tag " + tag + ": multi value not implemented");
                    return null;
                }
                if (clazz.equals(Rational.class)) {
                    // expect a CMIS decimal property
                    if (propType != PropertyType.DECIMAL)
                        throw new MapperException("Tag value has type Rational and expected CMIS Decimal, but found: " + propType + " for tag: " + tag);
                    
                    if (tag.getTagType() == ExifDirectory.TAG_SHUTTER_SPEED) {
                        // requires special handling, see Tika impl.
                        double apexValue = ((Rational) src).doubleValue();
                        res = PhotographicConversions.shutterSpeedToExposureTime(apexValue);
                    } else if (tag.getTagType() == (ExifDirectory.TAG_APERTURE)) {
                        double aperture =((Rational) src).doubleValue();
                        double fStop = PhotographicConversions.apertureToFStop(aperture);
                        res = fStop;
                    } else {
                        // convert to a double
                        double d = ((Rational) src).doubleValue();
                        res = d;
                    }
                } else if (clazz.equals(Integer.class)) {
                    if (propType != PropertyType.INTEGER)
                        throw new MapperException("Tag value has type Integer and expected CMIS Integer, but found: " + propType + " for tag: " + tag);
                    // convert to a long
                    long l = ((Integer) src).longValue();
                    res = l;                                        
                } else if (clazz.equals(String.class)) {
                    if (propType != PropertyType.STRING && propType != PropertyType.ID && propType != PropertyType.URI &&
                            propType != PropertyType.HTML && propType != PropertyType.DATETIME)
                        throw new MapperException("Tag value has type String and expected CMIS String, but found: " + propType + " for tag: " + tag);
                    // convert to a String
                    if (propType == PropertyType.DATETIME) {
                        // parse format: 2012:02:25 16:23:16
                        DateFormat formatter = new SimpleDateFormat(dateFormat);
                        GregorianCalendar cal;
                        try {
                            Date date = (Date)formatter.parse((String) src);
                            // convert date to GreogorianCalendar as CMIS expects
                            cal = new GregorianCalendar();
                            cal.setTime(date);
                        } catch (ParseException e) {
                            LOG.error(e.toString(), e);
                            throw new MapperException("Unrecognized date format in EXIF date tag: " + src + " for tag: " + tag + " expected: yyyy:MM:dd HH:mm:ss");
                        }
                        res = cal;                     
                    } else {
                        String s = ((String) src);
                        res = s;
                    }
                } else if (clazz.equals(Date.class)) {
                    if (propType != PropertyType.DATETIME)
                        throw new MapperException("Tag value has type Date and expected CMIS DateTime, but found: " + propType + " for tag: " + tag);
                    // convert to a String
                    Date date = ((Date) src);
                    // convert date to GreogorianCalendar as CMIS expects
                    GregorianCalendar  cal= new GregorianCalendar();
                    cal.setTime(date);
                    res = cal;                                        
                } else if (clazz.equals(Boolean.class)) {
                    if (propType != PropertyType.BOOLEAN)
                        throw new MapperException("Tag value has type Boolean and expected CMIS Boolean, but found: " + propType + " for tag: " + tag);
                    // convert to a String
                    Boolean b = ((Boolean) src);
                    res = b;                                        
                } else {
                    LOG.debug("Tag value has unsupported type: " + clazz.getName() + " for EXIF tag: " + tag);
                    // throw new MapperException("Tag value has unsupported type: " + clazz.getName() + " for tag: " + tag);
                }                
            }            
        } else if (JpegDirectory.class.equals(dir.getClass())) {
            // is there a property mapped to this tag?
            String propId = propMapJpeg.get(hexStr);
            LOG.debug("Found JPEG tag '" + tag + "\', property mapped is: " + propId);

            if (null == propId) {
                LOG.info("Ignoring JPEG tag '" + tag + "\' no property mapped to this tag.");
            } else if (propType == null) {
                // should not happen and is a configuration error: we have a property id but no type
                LOG.error("Ignoring JPEG tag '" + tag + "\' no property type mapped to this tag.");
            } else {
                Object src = dir.getObject(tag.getTagType());        
                Class<?> clazz = src.getClass();

                if (clazz.equals(Integer.class)) {
                    if (propType != PropertyType.INTEGER)
                        throw new MapperException("Tag value has type Integer and expected CMIS Integer, but found: " + propType + " for tag: " + tag);
                    // convert to a long
                    long l = ((Integer) src).longValue();
                    res = l;                                        
                } else {
                    LOG.debug("Tag value has unsupported type: " + clazz.getName() + " for JPEG tag: " + tag);
                }
            }            
        }
            
        return res;        
    }
    
    private static Object convertGps(Tag tag, Directory dir, boolean mustInvert) {
        Double res = null; 
        Object src = dir.getObject(tag.getTagType());
        
        Class<?> stringArrayClass = src.getClass();
        Class<?> stringArrayComponentType = stringArrayClass.getComponentType();
        if (!stringArrayClass.isArray() || null == stringArrayComponentType || Array.getLength(src) != 3) 
            throw new MapperException("GPS coordinate \"" + tag + "\" has unknown type.");
        if (!stringArrayComponentType.equals(Rational.class))
            throw new MapperException("GPS coordinate \"" + tag + "\" has unknown component type (expected Rational, found: " + 
                    stringArrayComponentType.getName() + ")");
        // do conversion
        Rational[] components;
        components = (Rational[]) src;
        int deg = components[0].intValue();
        double min = components[1].doubleValue();
        double sec = components[2].doubleValue();
        Double d = (deg + min / 60 + sec / 3600);
        if (d > 0.0 && mustInvert)
            d = -d;
        res = d;
        return res;
    }
}
