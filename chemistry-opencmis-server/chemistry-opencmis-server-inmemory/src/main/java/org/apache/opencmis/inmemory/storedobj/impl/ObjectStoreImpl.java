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
package org.apache.opencmis.inmemory.storedobj.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.opencmis.inmemory.storedobj.api.Document;
import org.apache.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.opencmis.inmemory.storedobj.api.Folder;
import org.apache.opencmis.inmemory.storedobj.api.MultiFiling;
import org.apache.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.opencmis.inmemory.storedobj.api.Filing;
import org.apache.opencmis.inmemory.storedobj.api.SingleFiling;
import org.apache.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.opencmis.inmemory.storedobj.api.VersionedDocument;

/**
 * InMemory folder implementation
 * 
 * @author Jens
 * 
 */

public class ObjectStoreImpl implements ObjectStore {

  /**
   * Simple id generator that uses just an integer 
   */
  private static int NEXT_UNUSED_ID = 100;
  
  /**
   * Maps the absolute folder path to the corresponding folder object
   */
  private Map<String, StoredObject> fStoredObjectMap = new HashMap<String, StoredObject>();
  final String fRepositoryId; 
  FolderImpl fRootFolder = null;

  public ObjectStoreImpl(String repositoryId) {
    fRepositoryId = repositoryId;
    createRootFolder();
  }

  private static synchronized Integer getNextId() {
    return NEXT_UNUSED_ID++;
  }
  

  /*
   * (non-Javadoc)
   * 
   * @see org.opencmis.client.provider.spi.inmemory.storedobj.impl.ObjectStore#getRootFolder()
   */
  public Folder getRootFolder() {
    return fRootFolder;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.spi.inmemory.storedobj.impl.ObjectStore#getFolderByPath(java.lang
   * .String)
   */
  public StoredObject getObjectByPath(String path) {
    
    for (StoredObject so : fStoredObjectMap.values()) {
      if (so instanceof SingleFiling) {
        String soPath = ((SingleFiling) so).getPath();
        if (soPath.equals(path))
          return so;
      }
      else if (so instanceof MultiFiling) {
        MultiFiling mfo = (MultiFiling)so;
        List<Folder> parents = mfo.getParents();
        for (Folder parent : parents) {
          String parentPath = parent.getPath();
          String mfPath = parentPath.equals(Folder.PATH_SEPARATOR) ? parentPath
              + mfo.getPathSegment() : parentPath + Folder.PATH_SEPARATOR + mfo.getPathSegment();
          if (mfPath.equals(path))
            return so;
        }
      }
      else
        return null;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.spi.inmemory.storedobj.impl.ObjectStore#getObjectById(java.lang
   * .String)
   */
  public StoredObject getObjectById(String objectId) {
    // we use path as id so we just can look it up in the map
    StoredObject so = fStoredObjectMap.get(objectId);
    return so;
  }

 /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.spi.inmemory.storedobj.impl.ObjectStore#deleteObject(java.lang
   * .String)
   */
  public void deleteObject(String objectId) {
    String path = objectId; // currently the same
    StoredObject obj = fStoredObjectMap.get(path);

    if (null == obj)
      throw new RuntimeException("Cannot delete object with id  " + objectId
          + ". Object does not exist.");

    if (obj instanceof FolderImpl) {
      deleteFolder(objectId);
    }
    else if (obj instanceof DocumentVersion ){
      DocumentVersion vers = (DocumentVersion) obj;
      VersionedDocument parentDoc = vers.getParentDocument();
      fStoredObjectMap.remove(path);
      boolean otherVersionsExist =  vers.getParentDocument().deleteVersion(vers);
      if (!otherVersionsExist)
        fStoredObjectMap.remove(parentDoc.getId());
    }
    else {
      fStoredObjectMap.remove(path);
    }
  }
  public void removeVersion(DocumentVersion vers) {
    StoredObject found = fStoredObjectMap.remove(vers.getId());

    if (null == found)
      throw new RuntimeException("Cannot delete object with id  " + vers.getId()
          + ". Object does not exist.");    
  }
  
//  public void changePath(StoredObject obj, String oldPath, String newPath) {
//    fStoredObjectMap.remove(oldPath);
//    fStoredObjectMap.put(newPath, obj);
//  }

  // /////////////////////////////////////////
  // methods used by folders and documents, but not for public use
  
//  void storeObject(String id, StoredObject sop) {
//    fStoredObjectMap.put(id, sop);
//  }

  public String storeObject(StoredObject so) {    
    String id = so.getId();
    // check if update or create
    if (null == id)
      id = getNextId().toString();
    fStoredObjectMap.put(id, so);
    return id;
  }
  
  StoredObject getObject(String id) {
    return fStoredObjectMap.get(id);
  }

  void removeObject(String id) {
    fStoredObjectMap.remove(id);
  }
  
  Set<String> getIds() {
   Set<String> entries = fStoredObjectMap.keySet();
   return entries;
  }
  
//  void renameAllIdsWithPrefix(String oldPath, String newPath) {
//    Iterator<Entry<String, StoredObject>> it = fStoredObjectMap.entrySet().iterator();
//    Map<String, StoredObject> newMap = new HashMap<String, StoredObject>();
//    while (it.hasNext()) {
//      Map.Entry<String, StoredObject> entry = (Map.Entry<String, StoredObject>) it
//          .next();
//
//      if (entry.getKey().startsWith(oldPath)) {
//        if (entry.getValue() instanceof Path) {
//          newPath = ((Path)entry.getValue()).getPath();
//          it.remove(); // the only safe way to modify while iteration
//          newMap.put(newPath, entry.getValue()); // we can't add to the current collection while
//                                                 // iterating          
//        }
//      }
//    }
//    fStoredObjectMap.putAll(newMap); // add all at once when iteration is complete  
//  }
  
  // /////////////////////////////////////////
  // private helper methods

  private void createRootFolder() {
    FolderImpl rootFolder = new FolderImpl(this);
    rootFolder.setName("RootFolder");
    rootFolder.setParent(null);
    rootFolder.setTypeId(BaseObjectTypeIds.CMIS_FOLDER.value());
    rootFolder.setCreatedBy("Admin");
    rootFolder.setModifiedBy("Admin");
    rootFolder.setModifiedAtNow();
    rootFolder.setRepositoryId(fRepositoryId);
    rootFolder.persist();
    fRootFolder =  rootFolder;
  }

  public Document createDocument(String name) {
    Document doc = new DocumentImpl(this);
    doc.setRepositoryId(fRepositoryId);
    doc.setName(name);
    return doc;
  }

  public VersionedDocument createVersionedDocument(String name) {
    VersionedDocument doc = new VersionedDocumentImpl(this);
    doc.setRepositoryId(fRepositoryId);
    doc.setName(name);
    return doc;
  }

  public Folder createFolder(String name) {
    Folder folder = new FolderImpl(this, name, null);;
    folder.setRepositoryId(fRepositoryId);
    return folder;
  }

  public List<VersionedDocument> getCheckedOutDocuments(String orderBy) {
    List<VersionedDocument> res = new ArrayList<VersionedDocument>();
    
    for (StoredObject so : fStoredObjectMap.values() ) {
      if (so instanceof VersionedDocument) {
        VersionedDocument verDoc = (VersionedDocument) so;
        if (verDoc.isCheckedOut()) {
          res.add(verDoc);
        }
      }
    }
    
    return res;
  }
  
  private void deleteFolder(String folderId) {
    StoredObject folder = fStoredObjectMap.get(folderId);
    if (folder == null)
      throw new RuntimeException("Unknown object with id:  " + folderId);

    if (!(folder instanceof FolderImpl)) {
        throw new RuntimeException("Cannot delete folder with id:  " + folderId
            + ". Object exists but is not a folder.");
    }

    // check if children exist
    List<StoredObject> children = ((Folder)folder).getChildren(-1, -1);
    if (children!=null && !children.isEmpty())
      throw new CmisConstraintException("Cannot delete folder with id:  " + folderId
          + ". Folder is not empty.");
      
    fStoredObjectMap.remove(folderId);    
  }

}
