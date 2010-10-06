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
package scripts

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.chemistry.opencmis.commons.*
import org.apache.chemistry.opencmis.commons.data.*
import org.apache.chemistry.opencmis.commons.enums.*
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.client.api.*

class CMIS {
    
    Session session
    
    CMIS(Session session) {
        this.session = session
    }
    
    CmisObject getObject(id) {
        CmisObject result = null
        
        if(id instanceof CmisObject) {
            result = id
        } else if(id instanceof ObjectId) {
            result = session.getObject(id)
        } else if(id instanceof String) {
            if(id.startsWith("/")) {
                result = session.getObjectByPath(id)
            } else {
                result = session.getObject(session.createObjectId(id))
            }
        }
        
        if(result == null) {
            throw new Exception("Object not found!")
        }
        
        return result
    }
    
    Folder getFolder(id) {
        CmisObject folder = getObject(id)
        if(!(folder instanceof Folder)) {
            throw new Exception("Object is not a folder!")
        }
        
        return folder
    }
    
    Document getDocument(id) {
        CmisObject doc = getObject(id)
        if(!(doc instanceof Document)) {
            throw new Exception("Object is not a document!")
        }
        
        return doc
    }
    
    void printProperties(id) {
        CmisObject object = getObject(id)
        
        for(Property prop: object.getProperties()) {
            printProperty(prop)
        }
    }
    
    void printProperty(Property prop) {
        println prop.getId() + ": " + prop.getValuesAsString()
    }
    
    void download(id, destination) {
        Document doc = getDocument(id)
        
        def file = new FileOutputStream(destination)
        def out = new BufferedOutputStream(file)
        out << doc.contentStream.stream
        out.close()
    }
    
    Folder createFolder(parent, String name, String type = "cmis:folder") {        
        CmisObject parentFolder = getFolder(parent)
        
        def properties = [
                    (PropertyIds.OBJECT_TYPE_ID): type, 
                    (PropertyIds.NAME): name
                ]
        
        return parentFolder.createFolder(properties, session.getDefaultContext())
    }
    
    Document createTextDocument(parent, String name, String content, String type = "cmis:document", 
    VersioningState versioningState = VersioningState.MAJOR) {        
        CmisObject parentFolder = getFolder(parent)
        
        def properties = [
                    (PropertyIds.OBJECT_TYPE_ID): type,
                    (PropertyIds.NAME): name
                ]
        
        def stream = new ByteArrayInputStream(content.bytes)
        def contentStream = new ContentStreamImpl(name, content.bytes.length, "text/plain", stream)
        
        return parentFolder.createDocument(properties, contentStream, 
        versioningState, session.getDefaultContext())
    }
    
    Document createDocumentFromFile(parent, File file, String type = "cmis:document", 
    VersioningState versioningState = VersioningState.MAJOR) {
        CmisObject parentFolder = getFolder(parent)
        
        def name = file.getName()
        def mimetype = org.apache.chemistry.opencmis.workbench.model.MIMETypes.getMIMEType(file)
        
        def properties = [
                    (PropertyIds.OBJECT_TYPE_ID): type,
                    (PropertyIds.NAME): name
                ]
        
        def contentStream = new ContentStreamImpl(name, file.size(), mimetype, new FileInputStream(file))
        
        return parentFolder.createDocument(properties, contentStream,
        versioningState, session.getDefaultContext())
    }
    
    void delete(id) {
        getObject(id).delete(true)
    }
}