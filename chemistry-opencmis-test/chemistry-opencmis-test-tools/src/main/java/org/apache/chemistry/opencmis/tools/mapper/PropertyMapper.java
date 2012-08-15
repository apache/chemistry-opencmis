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

import java.util.Properties;

import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;

/**
 * A property mapper is a class being responsible to map a Content-Type (e.g. image/jpeg)
 * to a CMIS type id and to map and convert properties. Extracted tags are mapped to 
 * CMIS property ids and sometimes type conversion is required (e.g. a string to a date).
 * Classes implementing this interface are not responsible for extracting the metadata 
 * (see MetadataParser). It only reads a configuration and maps properties. The 
 * Configurator will read the configuration properties and instantiate implementations 
 * of this interface (one instance per CMIS type)
 * 
 * @author Jens
 *
 */
public interface PropertyMapper {
    
    /**
     * initialize a property mapper
     * @param cfgPrefix
     *      prefix for all configuration entries in properties file
     * @param typeKey
     *      type key in configuration used to identify this type
     * @param properties
     *      all properties read from resource mapping.properties
     * @return
     *      true, if processing should continue, false if not 
     */
    boolean initialize(String cfgPrefix, String typeKey, Properties properties);
    
    /**
     * Reset all internal data to get ready for a new parsing process 
     */
    void reset();
    
    /**
     * return the CMIS type id used for this mapper
     * @return
     *      CMIS type id to map this content type to or null if not mappable
     */
    String getMappedTypeId();
    
    /**
     * return the CMIS property id for a found tag in a file
     * @param key
     *      tag (usually parsed from Tika) found in file
     * @return
     *      CMIS property this tag gets mapped to, null if not mapped
     */
    String getMappedPropertyId(String key);
    
    /**
     * Convert a value parsed from the file to the type the corresponding property expects
     * @param id
     *      CMIS property id
     * @param propertyType
     *      property type from type definition
     * @param strValue
     *      value read from file (Tika always gives a string)
     * @return
     *      converted value conforming to the mapped property
     */
    Object convertValue(String id, PropertyDefinition<?> propDef, String strValue);

    /**
     * get all content types handled by this parser
     * @return
     *      array with content types
     */
    public String[] getContentTypes();

}
