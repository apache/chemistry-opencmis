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
package org.apache.chemistry.opencmis.client.parser;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.apache.chemistry.opencmis.client.mapper.MapperException;
import org.apache.chemistry.opencmis.client.mapper.PropertyMapperExif;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;

/**
 * A parser implementation using a lower level interface to get more control about the 
 * EXIF tags than Tika provides
 * 
 * @author Jens
 *
 */
public class MetadataParserExif extends AbstractMetadataParser  {

    private static final Log LOG = LogFactory.getLog(MetadataParserExif.class.getName());
    
    public void extractMetadata(File f, TypeDefinition td) throws MapperException {
        
        reset();
        
        // see  http://code.google.com/p/metadata-extractor/wiki/GettingStarted
        try {
            com.drew.metadata.Metadata metadata = ImageMetadataReader.readMetadata(f);
            Iterator<?> it = metadata.getDirectoryIterator();
            while (it.hasNext()) {
                Directory directory = (com.drew.metadata.Directory) it.next();
                Iterator<?> tagIt = directory.getTagIterator();
                while (tagIt.hasNext()) {
                    Tag tag = (Tag) tagIt.next();
                    Object o = directory.getObject(tag.getTagType());
                    LOG.debug("Tag: " + tag + ", value: " + o + ", class: " + o.getClass() + 
                            ", tag type: " + tag.getTagType() + ", hex-value: " + tag.getTagTypeHex());
                    if (null != cmisProperties) {
                        ((PropertyMapperExif)mapper).mapTagAndConvert(directory, tag, td);
                    }
                }
            }
            Map<String, Object> props = ((PropertyMapperExif)mapper).getMappedProperties();
            cmisProperties.putAll(props);
        } catch (ImageProcessingException e) {            
            LOG.error(e);
        }

    }

}
