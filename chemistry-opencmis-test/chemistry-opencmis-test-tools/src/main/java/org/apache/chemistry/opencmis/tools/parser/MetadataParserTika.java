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
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.tools.mapper.MapperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A metadata parser using the Apache Tika library
 * @author Jens
 *
 */
public class MetadataParserTika extends AbstractMetadataParser {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataParserTika.class.getName());
    private Session session;

    public MetadataParserTika() {
    }
    
    public void extractMetadata(File f, TypeDefinition td, Session session) throws MapperException {
        try {
            InputStream stream = new FileInputStream(f);
            Metadata metadata = new Metadata();
            ContentHandler handler = new DefaultHandler();
            Parser parser = new AutoDetectParser(); 
            ParseContext context = new ParseContext();
            parser.parse(stream, handler, metadata, context);

            reset();
            
            for (String key : metadata.names()) {
                String val = metadata.get(key);
                LOG.debug("Found metadata \'" + key + "\': " + val);      
                if (null != cmisProperties) {
                    String propertyId = mapper.getMappedPropertyId(key);
                    if (null != propertyId && null != val) {
                        if (td != null) {
                            PropertyDefinition<?> propDef = getMappedPropertyDefinition(td.getId(), propertyId, session);                            
                            Object convVal = mapper.convertValue(propertyId, propDef, val);
                            if (null != convVal)
                                cmisProperties.put(propertyId, convVal);
                        } else
                            cmisProperties.put(propertyId, val); // omit conversion if no type definition is available
                    }
                }
            }

        } catch (Exception e) {
            LOG.error(e.toString(), e);
            throw new MapperException("Extracting metadata failed for file " + f.getAbsolutePath(), e);
        }
    }

    private PropertyDefinition<?> getMappedPropertyDefinition(String typeId, String propertyId, Session session) {
        String typeIdOrg = typeId;
        while (null != typeId) {
            TypeDefinition td = session.getTypeDefinition(typeId);
            PropertyDefinition<?> propDef = td.getPropertyDefinitions().get(propertyId);
            if (null != propDef)
                return propDef;
            typeId = td.getParentTypeId();
        }
        throw new MapperException("Mapping error: unknown property "+ propertyId + " in type definition " + typeIdOrg);
    }    
    
    public void listMetadata(File f) throws MapperException {
        try {
            InputStream stream = new FileInputStream(f);
            Metadata metadata = new Metadata();
            ContentHandler handler = new DefaultHandler();
            Parser parser = new AutoDetectParser(); 
            ParseContext context = new ParseContext();
            parser.parse(stream, handler, metadata, context);

            for (String key : metadata.names()) {
                String val = metadata.get(key);
                LOG.info("Found metadata \'" + key + "\': " + val);      
            }

        } catch (Exception e) {
            LOG.error(e.toString(), e);
            throw new MapperException("Extracting metadata failed, file not found: " + f.getAbsolutePath(), e);
        }
    } 
}
