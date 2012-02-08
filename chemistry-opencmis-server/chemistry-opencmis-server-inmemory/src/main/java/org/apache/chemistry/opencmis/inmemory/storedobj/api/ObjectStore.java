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
package org.apache.chemistry.opencmis.inmemory.storedobj.api;

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

/**
 * @author Jens
 * 
 *         This is the interface an implementation must provide to store any
 *         kind of CMIS objects. The ObjectStore is the topmost container of all
 *         CMIS object that get persisted. It is comparable to a file system,
 *         one object store exists per repository id. The object store allows
 *         access objects by an id. In addition a object can be retrieved by
 *         path. Typically the object store owns the list of object ids and
 *         maintains the path hierarchy.
 */
public interface ObjectStore {

    /**
     * Get the root folder of this object store
     * 
     * @return the root folder of this store
     */
    Folder getRootFolder();

    /**
     * return an object by path.
     * 
     * @param path
     *            the path to the object
     * @return the stored object with this path
     */
    StoredObject getObjectByPath(String path, String user);

    /**
     * get an object by its id
     * 
     * @param folderId
     *            the id of the object
     * @return the object identified by this id
     */
    StoredObject getObjectById(String folderId);

    /**
     * Deletes an object from the store. For a folders the folder must be empty.
     * 
     * @param objectId
     * @param user 
     * @param allVersions is TRUE all version of the document are deleted, otherwise just this one
     */
    void deleteObject(String objectId, Boolean allVersions, String user);
       
    /**
     * Create a document as initial step. The document is created but still
     * temporary It is not yet persisted and does not have an id yet. After this
     * call additional actions can take place (like assigning properties and a
     * type) before it is persisted.
     * 
     * @param name
     *            name of the document
     * @param propMap
     * 			  map of properties   
     * @param user
     * 			  the user who creates the document
     * @param folder
     * 			  the parent folder 
     * @param addACEs
     * 			  aces that are added 
     * @param removeACEs 
     *            aces that are removed
     * @return document object
     */
     Document createDocument(String name, Map<String, PropertyData<?>> propMap, String user, Folder folder,
 			Acl addACEs, Acl removeACEs);


    /**
     * Create a folder as initial step. The folder is created but still
     * temporary It is not yet persisted and does not have an id yet. After this
     * call additional actions can take place (like assigning properties and a
     * type) before it is persisted.
     * 
     * @param name
     *            name of the folder
     * @param propMap
     * 			  map of properties   
     * @param user
     * 			  the user who creates the document
     * @param folder
     * 			  the parent folder 
     * @param addACEs
     * 			  aces that are added 
     * @param removeACEs 
     *            aces that are removed
     * @return folder object
     */
    Folder createFolder(String name, Map<String, PropertyData<?>> propMap, String user, Folder folder,
			Acl addACEs, Acl removeACEs);

    /**
     * Create a document that supports versions as initial step. The document is
     * created but still temporary. It is not yet persisted and does not have an
     * id yet. After this call additional actions can take place (like assigning
     * properties and a type) before it is persisted.
     * 
     * @param name
     *            name of the document
     *             * @param propMap
     * 			  map of properities   
     * @param user
     * 			  the user who creates the document
     * @param folder
     * 			  the parent folder 
     * @param addACEs
     * 			  aces that are added 
     * @param removeACEs 
     *            aces that are removed
     * @return versioned document object
     */
    DocumentVersion createVersionedDocument(String name,
			Map<String, PropertyData<?>> propMap, String user, Folder folder,
			Acl addACEs, Acl removeACEs, ContentStream contentStream, VersioningState versioningState);

    /**
     * Clear repository and remove all data.
     */
    void clear();
    
    /**
     * For statistics: return the number of objects contained in the system
     * @return
     *      number of stored objects
     */
    long getObjectCount();
    
    /**
     * Create a relationship. The relationship is
     * created but still temporary. It is not yet persisted and does not have an
     * id yet. After this call additional actions can take place (like assigning
     * properties and a type) before it is persisted.
     * 
     * @param sourceObject
     *            source of the relationship
     * @param targetObject
     *            target of the relationship
     * @param propMap
     * 			  map of properities   
     * @param user
     * 			  the user who creates the document
     * @param folder
     * 			  the parent folder 
     * @param addACEs
     * 			  aces that are added 
     * @param removeACEs 
     *            aces that are removed
     * @return versioned document object
     */
    StoredObject createRelationship(StoredObject sourceObject, StoredObject targetObject, 
    		Map<String, PropertyData<?>> propMap,
			String user, Acl addACEs, Acl removeACEs);
    
    /**
     * Return a list of all documents that are checked out in the repository.
     * 
     * @param orderBy
     *            orderBy specification according to CMIS spec.
     * @param user
     * 			user id of user calling
     * @param includeRelationships
     * 			if true include all relationships in the response
     * @return list of checked out documents in the repository
     */
    List<StoredObject> getCheckedOutDocuments(String orderBy, String user, IncludeRelationships includeRelationships);
    
    /**
     * Apply a ACLs by relative adding and removing a list of ACEs to/from an object
     * 
     * @param so
     *      object where ACLs are applied
     * @param addAces
     *      list of ACEs to be added
     * @param removeAces
     *      list of ACEs to be removed
     * @param aclPropagation
     *      enum value how to propagate ACLs to child objects
     * @return
     *      new ACL of object
     */
    Acl applyAcl(StoredObject so, Acl addAces, Acl removeAces, AclPropagation aclPropagation, String principalId);
    
    /**
     * Apply a ACLs by setting a new list of ACEs to an object
     * 
     * @param so
     *      object where ACLs are applied
     * @param aces
     *      list of ACEs to be applied
     * @param aclPropagation
     *      enum value how to propagate ACLs to child objects
     * @return
     *      new ACL of object
     */
    Acl applyAcl(StoredObject so, Acl aces, AclPropagation aclPropagation, String principalId);
    
    /**
     * Check if this store contains any object with the given type id
     * 
     * @param typeId
     *      id of type definition to check
     * @return
     *      true if at least one object in the store has the given type, false
     *      if no objects exist having this type
     */
    boolean isTypeInUse(String typeId);
}