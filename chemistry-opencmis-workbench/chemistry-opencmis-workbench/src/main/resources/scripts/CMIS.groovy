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
        
        if(object.getProperties() == null || object.getProperties().isEmpty()) {
            println "- no properties (???) -"
            return
        }
        
        for(Property prop: object.getProperties()) {
            printProperty(prop)
        }
    }
    
    void printProperty(Property prop) {
        println prop.getId() + ": " + prop.getValuesAsString()
    }
    
    void printAllowableActions(id) {
        CmisObject object = getObject(id)
        
        if(object.getAllowableActions() == null || 
        object.getAllowableActions().getAllowableActions() == null || 
        object.getAllowableActions().getAllowableActions().isEmpty()) {
            println "- no allowable actions -"
            return
        }
        
        for(Action action: object.getAllowableActions().getAllowableActions()) {
            println action.value()
        }
    }
    
    void printVersions(id) {
        Document doc = getDocument(id)
        
        List<Document> versions = doc.getAllVersions()
        
        if(versions == null || versions.isEmpty()) {
            println "- no versions -"
            return
        }
        
        for(Document version: doc.getAllVersions()) {
            println(version.getVersionLabel() + " (" + version.getId() +") [" + version.getType().getId() + "]")
        }
    }
    
    void printChildren(id) {
        Folder folder = getFolder(id)
        
        boolean hasChildren = false
        for(CmisObject child: folder.getChildren()) {
            println(child.getName() + " (" + child.getId() +") [" + child.getType().getId() + "]")
            hasChildren = true;
        }
        
        if(!hasChildren) {
            println "- no children -"
        }
    }
    
    void printRelationships(id) {
        CmisObject object = getObject(id)
        
        boolean hasRelationships = false
        for(CmisObject rel: object.getRelationships()) {
            println(rel.getName() + " (" + rel.getId() +") [" + rel.getType().getId() + "]")
            hasRelationships = true
        }
        
        if(!hasRelationships) {
            println "- no relationships -"
        }
    }
    
    void printRenditions(id) {
        Document doc = getDocument(id)
        
        List<Rendition> renditons = doc.getRenditions()
        
        if(renditons == null || renditons.isEmpty()) {
            println "- no renditions -"
            return
        }
        
        for(Rendition rendition: renditons) {
            println(rendition.getTitle() + " (MIME type: " + rendition.getMimeType() + ", length: "  + rendition.getLength() + " bytes)")
        }
    }
    
    void printObjectSummary(id) {
        CmisObject object = getObject(id)
        
        println "Name:        " + object.getName()
        println "Object Id:   " + object.getId()
        println "Object Type: " + object.getType().getId()
        println ""
        println "--------------------------------------------------"
        println "Properties:"
        println "--------------------------------------------------"
        printProperties(object)
        println ""
        println "--------------------------------------------------"
        println "Allowable Actions:"
        println "--------------------------------------------------"
        printAllowableActions(object)
        println ""
        println "--------------------------------------------------"
        println "Relationships:"
        println "--------------------------------------------------"
        printRelationships(object)
        
        if(object instanceof Document) {
            println ""
            println "--------------------------------------------------"
            println "Versions:"
            println "--------------------------------------------------"
            printVersions(object)
            println ""
            println "--------------------------------------------------"
            println "Renditions:"
            println "--------------------------------------------------"
            printRenditions(object)
        }
        
        if(object instanceof Folder) {
            println ""
            println "--------------------------------------------------"
            println "Children:"
            println "--------------------------------------------------"
            printChildren(id)
        }
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
        
        return parentFolder.createFolder(properties)
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
        
        return parentFolder.createDocument(properties, contentStream, versioningState)
    }
    
    Document createDocumentFromFile(parent, File file, String type = "cmis:document", 
    VersioningState versioningState = VersioningState.MAJOR) {
        CmisObject parentFolder = getFolder(parent)
        
        def name = file.getName()
        def mimetype = org.apache.chemistry.opencmis.commons.impl.MimeTypes.getMIMEType(file)
        
        def properties = [
                    (PropertyIds.OBJECT_TYPE_ID): type,
                    (PropertyIds.NAME): name
                ]
        
        def contentStream = new ContentStreamImpl(name, file.size(), mimetype, new FileInputStream(file))
        
        return parentFolder.createDocument(properties, contentStream, versioningState)
    }
    
    Relationship createRelationship(source, target, name, type) {
        CmisObject sourceObject = getObject(source)
        CmisObject targetObject = getObject(target)
        
        def properties = [
                    (PropertyIds.OBJECT_TYPE_ID): type,
                    (PropertyIds.NAME): name,
                    (PropertyIds.SOURCE_ID): sourceObject.id,
                    (PropertyIds.TARGET_ID): targetObject.id
                ]
        
        return getObject(session.createRelationship(properties));
    }
    
    void delete(id) {
        getObject(id).delete(true)
    }
}