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
package org.apache.chemistry.opencmis.tools.parser;

import java.io.File;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.tools.mapper.MapperException;
import org.apache.chemistry.opencmis.tools.mapper.PropertyMapper;

/**
 * This interface is used to extract metadata from content. An instance is 
 * responsible to parse the content and extract the metadata. The metadata
 * must be stored in a CMIS property bag. Implementations of this class are
 * created by the Configurator depending on the Content-Type to parse and
 * the corresponding parser configuration
 * 
 * @author Jens
 *
 */
public interface MetadataParser {
    
    /**
     * Initialize a parser with a given property mapper and a content type
     * @param mapper
     *      PropertyMapper used to map tags to properties and convert values
     * @param contentType
     *      content type of file to extract
     */
    void initialize(PropertyMapper mapper, String contentType);
    
    /**
     * get ready for parsing a new file
     */
    void reset();
    
    /**
     * Parse a file and extract all metadata and store them in a CMIS property bag
     * @param f
     *      file to parse
     * @throws MapperException
     */
    void extractMetadata(File f, TypeDefinition td, Session session) throws MapperException;
    
    /**
     * Return all found metadata, called after parsing is completed.
     * @return
     *      extracted CMIS properties
     */
    Map<String, Object> getCmisProperties();
    
    /**
     * get all content types handled by this parser
     * @return
     *      array with content types
     */
    String[] getContentTypes();
    
    /**
     * get the CMIS type id used by this type
     * @return
     *      CMIS type id
     */
    String getMappedTypeId();
    
    /**
     * get the associated property mapper for this CMIS type
     * 
     * @return
     *      property mapper
     */
    PropertyMapper getMapper();
}
