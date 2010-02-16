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
package org.apache.opencmis.inmemory.storedobj.api;

import java.util.List;

/**
 * @author Jens
 * 
 * This is the interface an implementation must provide to store any kind of CMIS
 * objects. The ObjectStore is the topmost container of all CMIS object that get 
 * persisted. It is comparable to a file system, one object store exists per 
 * repository id. The object store allows access objects by an id. In addition a
 * object can be retrieved by path. Typically the object store owns the list of 
 * object ids and maintains the path hierarchy.
 */
public interface ObjectStore {

  /**
   * Get the root folder of this object store
   * @return
   *    the root folder of this store
   */
  Folder getRootFolder();

  /**
   * return an object by path. 
   * @param path
   *    the path to the object
   * @return
   *    the stored object with this path
   */
  StoredObject getObjectByPath(String path);

  /**
   * get an object by its id
   * @param folderId
   *      the id of the object
   * @return
   *      the object identified by this id
   */
  StoredObject getObjectById(String folderId);
  
  /**
   * Deletes an object from the store. For a folders the folder must be empty.
   * @param objectId
   */
  void deleteObject(String objectId);

  /**
   * Create a document as initial step. The document is created but still temporary
   * It is not yet persisted and does not have an id yet. After this call additional
   * actions can take place (like assigning properties and a type) before it is 
   * persisted.
   * 
   * @param name
   *    name of the document
   * @return
   *    document object
   */
  Document createDocument(String name);
  
  /**
   * Create a folder as initial step. The folder is created but still temporary
   * It is not yet persisted and does not have an id yet. After this call additional
   * actions can take place (like assigning properties and a type) before it is 
   * persisted.
   * 
   * @param name
   *    name of the folder
   * @return
   *    folder object
   */
  Folder createFolder(String name);

  /**
   * Create a document that supports versions as initial step. The document is created 
   * but still temporary. It is not yet persisted and does not have an id yet. After 
   * this call additional actions can take place (like assigning properties and a type) 
   * before it is persisted.
   * 
   * @param name
   *    name of the document
   * @return
   *    versioned document object
   */
  VersionedDocument createVersionedDocument(String name);

  /**
   * Return a list of all documents that are checked out in the repository.
   * 
   * @param orderBy
   *    orderBy specification according to CMIS spec.
   * @return
   *    list of checked out documents in the repository
   */
  List<VersionedDocument> getCheckedOutDocuments(String orderBy);
}