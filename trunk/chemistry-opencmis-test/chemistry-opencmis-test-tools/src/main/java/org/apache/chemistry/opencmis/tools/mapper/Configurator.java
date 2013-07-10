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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.chemistry.opencmis.tools.parser.MetadataParser;
import org.apache.chemistry.opencmis.tools.parser.MetadataParserTika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configurator {

    private static final Logger LOG = LoggerFactory.getLogger(Configurator.class.getName());
    
    private static Configurator INSTANCE;
    static final String PREFIX = "mapping.contentType";
    
    public static Configurator getInstance() {
        if (null == INSTANCE)
            INSTANCE = new Configurator();
        return INSTANCE;
    }
    
    private Properties properties;
    private Map<String, PropertyMapper> contentTypeMapperMap  = new HashMap<String, PropertyMapper>();
    private Map<String, MetadataParser> parserMap = new HashMap<String, MetadataParser>();
    private String defaultDocumentType;
    private String defaultFolderType;
    
    private Configurator() {     
        loadProperties();
        loadDefaults();
        buildMapperMap();
        createParsers();
    }
    
    // just for unit tests
    Configurator(Properties props) {
//        contentTypeMapperMap = new HashMap<String, PropertyMapper>();
        this.properties = props;
    }
    
    
    public PropertyMapper getPropertyMapper(String contentType) {
        MetadataParser parser = getParser(contentType);
        return parser.getMapper();
    }
    
    public MetadataParser getParser(String contentType) {
        MetadataParser parser = parserMap.get(contentType);
        if (null == parser) {
            // if not found try a more generic one
            String genericContentType = contentType.substring(0, contentType.indexOf('/')) + "/*";
            for (String key: parserMap.keySet()) {
                if (key.equals(genericContentType))
                    return parserMap.get(key);
            }
        }
        return parser;
        
//        for (String contentType : contentTypes) {
//            if (contentType.equals(mimeType))
//                return cmisTypeId;
//            boolean isStar = contentType.endsWith("/*");
//            if (isStar) {
//                String generalPartParam = mimeType.substring(0, mimeType.indexOf('/'));
//                String generalPartCfg = contentType.substring(0, mimeType.indexOf('/'));
//                if (generalPartParam.equals(generalPartCfg))
//                    return cmisTypeId;
//            }
//        }
//        return null;        
    }

    private void loadProperties() {
        // Returns null on lookup failures:
        InputStream in = Configurator.class.getResourceAsStream ("/mapping.properties");
        if (in != null)
        {
            properties = new Properties();
            try {
                properties.load (in);
            } catch (IOException e) {
                LOG.error(e.toString(), e);
                e.printStackTrace();
                throw new MapperException("Could not load file mapping.properties as resource", e);
            } 
        }
    }
    
    private void loadDefaults() {
        defaultDocumentType = properties.getProperty(PREFIX + ".default.document");
        if (null == defaultDocumentType)
            defaultDocumentType = "cmis:document";
        
        defaultFolderType = properties.getProperty(PREFIX + ".default.folder");
        if (null == defaultFolderType)
            defaultFolderType = "cmis:folder";                
    }
    
    public String getDefaultDocumentType() {
        return defaultDocumentType;
    }
    
    public String getDefaultFolderType() {
        return defaultFolderType;
    }

    public final Properties getProperties() {
        return properties;
    }
    
    /**
     * return an overridden MIME type from a file extension
     * 
     * @param fileExtension
     *      enforced or content-type or null if none is set
     */
    public String getContentType(String fileExtension) {
        return properties.getProperty(PREFIX+ ".forceContentType." + fileExtension, null);
    }
    
    String[] getTypeKeys() {
        String s = properties.getProperty(PREFIX + "s");
        
        if (null == s)
            return null;
        
        String[] keys = s.split(",");
        
        for (int i=0; i<keys.length; i++)
            keys[i] = keys[i].trim();
        
        return keys;
    }
    
    void  buildMapperMap() {
        
        String[] typeKeys = getTypeKeys();
        for (String typeKey : typeKeys) {
            PropertyMapper mapper = loadMapperClass(typeKey);
            String contentType = properties.getProperty(PREFIX + "." + typeKey);
            if (null == contentType) 
                throw new MapperException("Missingt content type in properties: " + PREFIX + "." + contentType);
            boolean ok = mapper.initialize(PREFIX, typeKey, properties);

            if (ok)
                contentTypeMapperMap.put(typeKey, mapper);
        }        
    }
    
    void createParsers() {
        String[] typeKeys = getTypeKeys();
        for (String typeKey : typeKeys) {
            MetadataParser parser = loadParserClass(typeKey);
            String contentType = properties.getProperty(PREFIX + "." + typeKey);
            if (null == contentType) 
                throw new MapperException("Missing content type in properties: " + PREFIX + "." + contentType);
            
            PropertyMapper mapper = contentTypeMapperMap.get(typeKey);

            parser.initialize(mapper, contentType);
            String[] contentTypes = parser.getContentTypes();
            for (String ct : contentTypes)
                parserMap.put(ct, parser);
        }                
    }
    
    MetadataParser loadParserClass(String typeKey) {
        String className = properties.getProperty(PREFIX + "." + typeKey + ".parserClass");
        if (null == className) // use Tika as default parser if none is configured
            className = MetadataParserTika.class.getName();
//            throw new MapperException("Missing parser class in properties: " + PREFIX + "." + typeKey + ".parserClass");

        Object obj = null;

        try {
            obj = Class.forName(className).newInstance();
        } catch (InstantiationException e) {
            LOG.error(e.toString(), e);
            throw new MapperException(
                    "Illegal class to load metadata parser, cannot instantiate " + className, e);
        } catch (IllegalAccessException e) {
            LOG.error(e.toString(), e);
            throw new MapperException(
                    "Illegal class to load metadata parser, cannot access " + className, e);
        } catch (ClassNotFoundException e) {
            LOG.error(e.toString(), e);
            throw new MapperException(
                    "Illegal class to load metadata parser, class not found: " + className, e);
        }

        if (obj instanceof MetadataParser) {
            return (MetadataParser) obj;
        } else {
            throw new MapperException("Illegal class to create metadata parser: " + className + ", must implement MetadataParser interface.");
        }
    }

    PropertyMapper loadMapperClass(String typeKey) {
        String className = properties.getProperty(PREFIX + "." + typeKey + ".mapperClass");
        if (null == className) 
            className = PropertyMapperTika.class.getName();
//            throw new MapperException("Missing property mapper in properties: " + PREFIX + "." + typeKey + ".mapperClass");

        Object obj = null;

        try {
            obj = Class.forName(className).newInstance();
        } catch (InstantiationException e) {
            LOG.error(e.toString(), e);
            throw new MapperException(
                    "Illegal class to load mapping configuration, cannot instantiate " + className, e);
        } catch (IllegalAccessException e) {
            LOG.error(e.toString(), e);
            throw new MapperException(
                    "Illegal class to load mapping configuration, cannot access " + className, e);
        } catch (ClassNotFoundException e) {
            LOG.error(e.toString(), e);
            throw new MapperException(
                    "Illegal class to load mapping configuration, class not found: " + className, e);
        }

        if (obj instanceof PropertyMapper) {
            return (PropertyMapper) obj;
        } else {
            throw new MapperException("Illegal class to create property mapper: " + className + ", must implement PropertyMapper interface.");
        }
    }
}
