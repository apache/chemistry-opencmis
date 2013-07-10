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

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyMapperTika extends AbstractPropertyMapper {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyMapperTika.class.getName());
    
    private Map<String, String> propMapTags = new HashMap<String, String> (); // tag to property id
    private Map<String, String> tokenizerMap = new HashMap<String, String> (); // tag to tokenizer regexp

    public PropertyMapperTika() {
        reset();
    }
    
    public boolean initialize(String cfgPrefix, String typeKey, Properties properties) {
        super.initialize(cfgPrefix, typeKey, properties);
        buildIdMap(typeKey, properties);
        
        return true;
    }
    
    public void reset() {
    }
    
    public String getMappedPropertyId(String key) {
        String propId = propMapTags.get(key);
        return propId;
    }

    public Object convertValue(String key, PropertyDefinition<?> propDef, String strValue) {
        Object value = null;
        PropertyType pt = propDef.getPropertyType();
        
        if (null == pt)
            value = null;
        else if (null != strValue && strValue.length() > 0) {
            // Tika has a bug and sometimes fails to parse MP3 tags, then generates '\0' in String
            // see https://issues.apache.org/jira/browse/TIKA-887
            int lastIllegalPos = -1;
            for (int i=0; i<strValue.length(); i++) {
              int c = strValue.codePointAt(i);
              if (Character.isISOControl(c))
                  lastIllegalPos = i;                  
            }
            if (lastIllegalPos >= 0)
                strValue = strValue.substring(lastIllegalPos+1); // use remaining part after illegal char

            switch (pt) {
            case STRING:
            case HTML:
            case URI:
            case ID:
                
                if (propDef.getCardinality() == Cardinality.SINGLE)
                    value = strValue;
                else {
                    String tokenizer = tokenizerMap.containsKey(key) ? tokenizerMap.get(key) : "\\W";
                    String[] result = strValue.split(tokenizer);
                    List<String> valList = new ArrayList<String>();
                    for (String s : result)
                        valList.add(s.trim());
                    value = valList;
                }
                break;
            case INTEGER:
                if (propDef.getCardinality() == Cardinality.SINGLE)
                    value = Integer.valueOf(strValue);
                else {
                        String tokenizer = tokenizerMap.containsKey(key) ? tokenizerMap.get(key) : "\\W";
                        String[] result = strValue.split(tokenizer);
                        List<Integer> valList = new ArrayList<Integer>();
                        for (String s : result)
                            valList.add(Integer.valueOf(s.trim()));
                        value = valList;
                    }
                break;
            case DECIMAL:
                if (propDef.getCardinality() == Cardinality.SINGLE)
                    value = Double.valueOf(strValue);                
                else {
                    String tokenizer = tokenizerMap.containsKey(key) ? tokenizerMap.get(key) : "[\\s;:]";                        
                    String[] result = strValue.split(tokenizer);
                        List<Double> valList = new ArrayList<Double>();
                        for (String s : result)
                            valList.add(Double.valueOf(s.trim()));
                        value = valList;
                    }
                break;
            case DATETIME:
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, new DateFormatSymbols(Locale.US));
                    if (propDef.getCardinality() == Cardinality.SINGLE) {
                        Date date = sdf.parse(strValue);
                        GregorianCalendar cal = new GregorianCalendar();
                        cal.setTime(date);
                        value = date;
                    } else {
                        String tokenizer = tokenizerMap.containsKey(key) ? tokenizerMap.get(key) : "[;,:]";                        
                        String[] result = strValue.split(tokenizer);
                        List<GregorianCalendar> valList = new ArrayList<GregorianCalendar>();
                        for (String s : result) {
                            Date date = sdf.parse(s.trim());
                            GregorianCalendar cal = new GregorianCalendar();
                            cal.setTime(date);
                            valList.add(cal);
                        }
                        value = valList;
                    }
                } catch (ParseException e) {
                    LOG.error("Could not parse date: " + strValue + " (check date format");
                    LOG.error(e.toString(), e);
                    value = null;
                    e.printStackTrace();
                }
                break;
            default:
                throw new MapperException("unknown property type " + pt);
            }            
        }
        return value;
    }
    
    void buildIdMap(String typeKey, Properties properties) {
        Set<String> keys = properties.stringPropertyNames(); 
        String prefix = propPrefix + ".id.";
        String tokenizerPrefix = propPrefix + ".tokenizer.";

        for (String key : keys) {
            if (key.startsWith(prefix)) {
                String id = key.substring(prefix.length());
                String cmisPropId = properties.getProperty(key).trim();
                if (null == cmisPropId)
                    throw new MapperException("Configuration key " + key + " must have a value assigned");
                LOG.debug("Found mapping for type " + typeKey + " with " + id + " to " + cmisPropId);
                propMapTags.put(id,  cmisPropId);
            }
            if (key.startsWith(tokenizerPrefix)) {
                String id = key.substring(tokenizerPrefix.length());
                String regex = properties.getProperty(key).trim();
                if (null == regex)
                    throw new MapperException("Configuration key " + key + " must have a value assigned");
                LOG.debug("Found tokenizer mapping for property " + id + " to " + regex);
                tokenizerMap.put(id, regex);
            }
        }
    }
    
    int getSize() {
        return propMapTags.size();
    }
    
 }
