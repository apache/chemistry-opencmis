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
package org.apache.chemistry.opencmis.tools.filecopy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.tools.mapper.Configurator;
import org.apache.chemistry.opencmis.tools.mapper.MapperException;
import org.apache.chemistry.opencmis.tools.mapper.PropertyMapper;
import org.apache.chemistry.opencmis.tools.parser.MetadataParser;
import org.apache.chemistry.opencmis.tools.parser.MetadataParserTika;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
    
public class FileCopier {
    
    private static final Logger LOG = LoggerFactory.getLogger(FileCopier.class.getName());
    // initialize configurator to get parsers and property mappings
    private static final Configurator CFG = Configurator.getInstance(); 
    private static int TOTAL_NUM = 0;
    
    private Session session;
    
    public FileCopier() {
    }
    
    public void connect(Map<String, String> parameters) {
        System.out.println("Connecting to a repository ...");

        // Create a SessionFactory and set up the SessionParameter map
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();

        session = sessionFactory.createSession(parameters);

        LOG.debug("Got a connection to repository.");
    }
        
    public void copyRecursive(String folderName, String folderId) {

        try {
            File fileOrDir = new File(folderName);
            if (fileOrDir.isDirectory()) {
                String newFolderId = createFolderInRepository(fileOrDir.getAbsolutePath(), folderId);
                File[] children = fileOrDir.listFiles();
                for (File file: children) {
                    if (!file.getName().equals(".") && !file.getName().equals("..")) {
                        copyRecursive(file.getAbsolutePath(), newFolderId);
                    }
                }
            } else {
                copyFileToRepository(fileOrDir.getAbsolutePath(), folderId);
            }            
        } catch (Exception e) {
            LOG.error(e.toString(), e);
        } finally {  
        }
    }
    
    private String copyFileToRepository(String fileName, String folderId) {
        LOG.debug("uploading file " + fileName);
        FileInputStream is = null;
        Map<String, Object> properties = new HashMap<String, Object>();
        Folder parentFolder;
        String id = null;
        
        if (null == folderId)
            parentFolder = session.getRootFolder();
        else
            parentFolder = (Folder) session.getObject(folderId);
        
        try {
            File f = new File(fileName);
            Tika tika = new Tika();     
            String mimeType = tika.detect(f);
            LOG.info("Detected MIME type: "+ mimeType);
            
            // extract metadata: first get a parser
            MetadataParser parser = CFG.getParser(mimeType);
            if (null == parser) {
                properties.put(PropertyIds.NAME, f.getName().replaceAll(" ", "_"));
                properties.put(PropertyIds.OBJECT_TYPE_ID, CFG.getDefaultDocumentType());
            } else {
                parser.reset();
                PropertyMapper mapper = CFG.getPropertyMapper(mimeType);
                if (null == mapper)
                    throw new MapperException("Unknown mime type (no configuration): " + mimeType);
                String typeId = mapper.getMappedTypeId();
                if (null == typeId)
                    throw new MapperException("No CMIS type configured for mime type" + mimeType);
                TypeDefinition td = session.getTypeDefinition(typeId);
                if (null == td)
                    throw new MapperException("CMIS type " + typeId + " does not exist on server.");

                LOG.info("Detected MIME type: "+ mimeType + " is mapped to CMIS type id: " + td.getId());
                parser.extractMetadata(f, td, session);
                properties = parser.getCmisProperties();
            }
                        
            // check if there is an overridden content type configured
            int posLastDot = f.getName().indexOf('.');
            String ext = posLastDot < 0 ? null : f.getName().substring(posLastDot+1, f.getName().length());
            String overridden = null;
            if (null != ext && (overridden = CFG.getContentType(ext)) != null)
                mimeType = overridden;
            long length = f.length();
            
            is = new FileInputStream(fileName);

            ContentStream contentStream = session.getObjectFactory().createContentStream(fileName,
                    length, mimeType, is);
            if (!properties.containsKey(PropertyIds.NAME))
                properties.put(PropertyIds.NAME, f.getName().replaceAll(" ", "_"));
            LOG.debug("uploading document with content lenth: " + contentStream.getLength());
            Document doc = parentFolder.createDocument(properties, contentStream, VersioningState.NONE);
            is.close();
            
            id = doc.getId();
            LOG.info("New document created with id: " + id + ", name: " +  properties.get(PropertyIds.NAME) + " in folder: " + parentFolder.getId());
            LOG.debug("total number of creations : " + ++TOTAL_NUM);
        } catch (Exception e) {
            LOG.error("Failed to create CMIS document.", e);
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOG.error(e.toString(), e);
                }
            }
            LOG.debug("Conversion and transfer done.");    
        }
        return id;
    }
    
    private  String createFolderInRepository(String fileName, String parentFolderId) {
        Folder parentFolder;
        String id = null;
        if (null == parentFolderId)
            parentFolder = session.getRootFolder();
        else
            parentFolder = (Folder) session.getObject(parentFolderId);
        Map<String, Object> properties = new HashMap<String, Object>();
        File f = new File(fileName);
        properties.put(PropertyIds.NAME, f.getName().replaceAll(" ", "_"));
        properties.put(PropertyIds.OBJECT_TYPE_ID, CFG.getDefaultFolderType());
        try {
            Folder folder = parentFolder.createFolder(properties);
            id = folder.getId();
            LOG.debug("New folder created with id: " + folder.getId() + ", path: " + folder.getPaths().get(0));
        } catch (Exception e) {
            LOG.error("Failed to create CMIS document.", e);
        } finally {
        }
        LOG.info("New folder created with id: " + id + ", name: " +  properties.get(PropertyIds.NAME) + " in parent folder: " + parentFolder.getId());
        return id;
    }
    
    public void listMetadata(String fileName) {
        try {
            File f = new File(fileName);
            Tika tika = new Tika();     
            String mimeType = tika.detect(f);
            LOG.info("Detected MIME type: "+ mimeType);
            
            // extract metadata: first get a parser
            MetadataParser parser = CFG.getParser(mimeType);
            if (null == parser) {
                LOG.warn("Unknown content type " + mimeType + " no metadata found, listing all tags found in file.");
                MetadataParserTika mpt = new MetadataParserTika();
                mpt.listMetadata(f);
            } else {
                PropertyMapper mapper = CFG.getPropertyMapper(mimeType);
                if (null == mapper)
                    throw new MapperException("Unknown mime type (no configuration): " + mimeType);
                String typeId = mapper.getMappedTypeId();
                if (null == typeId)
                    throw new MapperException("No CMIS type configured for mime type" + mimeType);
                
                // Session available? if yes do conversion
                TypeDefinition td = null;
                if (null!= session) {
                    td = session.getTypeDefinition(typeId);
                    if (null == td)
                        throw new MapperException("CMIS type " + typeId + " does not exist on server.");
                    else
                    	LOG.info("Detected MIME type: "+ mimeType + " is mapped to CMIS type id: " + td.getId());
                }
                
                parser.extractMetadata(f, td, session);
                Map<String, Object> properties = parser.getCmisProperties();
                for (String key : properties.keySet()) {
                    LOG.info("Found metadata tag " + key + "mapped to " + properties.get(key));
                }
            }                        
        } catch (Exception e) {
            LOG.error("Failed to list metadata", e);
        } finally {
        }
        LOG.debug("Conversion and transfer done.");            
    }
    
    static public void main(String[] args) {
        String fileName = args[0];
        LOG.debug("extracting CMIS properties for file " + fileName);
        try {
            new FileCopier().listMetadata(fileName);            
        } catch (Exception e) {
            LOG.error(e.toString(), e);
        } finally {
        }
        LOG.debug("Extraction done.");    
    }
}
