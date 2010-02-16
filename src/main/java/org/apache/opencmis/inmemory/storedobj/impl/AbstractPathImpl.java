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

import org.apache.opencmis.inmemory.NameValidator;
import org.apache.opencmis.inmemory.storedobj.api.Document;
import org.apache.opencmis.inmemory.storedobj.api.Folder;
import org.apache.opencmis.inmemory.storedobj.api.Path;
import org.apache.opencmis.inmemory.storedobj.api.VersionedDocument;

/**
 * InMemory Stored Object
 * 
 * @author Jens
 * 
 */

/**
 * StoredPathObject is the common superclass of all objects hold in the repository that are
 * identified by a path, these are: Documents and Folders
 */

public abstract class AbstractPathImpl extends StoredObjectImpl implements
    Path {

  protected FolderImpl fParent;
  protected ObjectStoreImpl fObjStore;

  protected AbstractPathImpl(ObjectStoreImpl objStore) {
    fObjStore = objStore;
  }
  
  public String getId() {
    return getPath();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.inmemory.StoredObjectWithPath#getPath()
   */
  public String getPath() {
    StringBuffer path= new StringBuffer(getName());
    if (null == getParent())
      path.replace(0, path.length(), PATH_SEPARATOR); // root folder--> set /
    else {
      Folder f = getParent();
      while (f.getParent() != null) {
        path.insert(0,  PATH_SEPARATOR);
        path.insert(0,  f.getName());
        f = f.getParent();
      }
      path.insert(0,  PATH_SEPARATOR);
    }
//    if (LOG.isDebugEnabled())
//      LOG.debug("getPath() returns: " + path.toString());
    return path.toString();    
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.inmemory.StoredObjectWithPath#getParent()
   */
  public Folder getParent() {
    return fParent;
  }
  
  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.storedobj.api.StoredObjectWithPath#setParent(org.apache.opencmis.client.provider.spi.inmemory.storedobj.api.Folder)
   */
  public void setParent(Folder parent) {
    fParent = (FolderImpl) parent;
  }
  
  public void rename(String newName) {
    if (!NameValidator.isValidId(newName))
      throw new IllegalArgumentException(NameValidator.ERROR_ILLEGAL_NAME);
    if (getParent() == null)
      throw new IllegalArgumentException("Root folder cannot be renamed.");
    if (getParent().hasChild(newName))
      throw new IllegalArgumentException("Cannot rename object to " + newName
          + ". This path already exists.");

    String oldPath = getPath(); 
    setName(newName);
    String newPath = getPath();
    fObjStore.changePath(this, oldPath, newPath);    
  }
  
  public void move(Folder newParent) {
    // we delegate this to the folder class because we need access to the global map to move
    if (this instanceof Document || this instanceof VersionedDocument)
      fParent.moveChildDocument((Document) this, newParent);    
    else {// it must be a folder
      if (getParent() == null)
        throw new IllegalArgumentException("Root folder cannot be moved.");
      if (newParent == null)
        throw new IllegalArgumentException("null is not a valid move target.");
      if (newParent.hasChild(getName()))
        throw new IllegalArgumentException(
            "Cannot move folder, this name already exists in target.");

      String oldPath = getPath(); // old path
      setParent(newParent);
      String newPath = getPath(); // new path

      fObjStore.renameAllIdsWithPrefix(oldPath, newPath);
//      fId = newPath;
    }
//    fId = getPath(); // as we use path the id will change
  }


}
