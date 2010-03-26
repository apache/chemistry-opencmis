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

import java.util.Collections;
import java.util.List;

import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.opencmis.inmemory.NameValidator;
import org.apache.opencmis.inmemory.storedobj.api.Document;
import org.apache.opencmis.inmemory.storedobj.api.Folder;
import org.apache.opencmis.inmemory.storedobj.api.SingleFiling;
import org.apache.opencmis.inmemory.storedobj.api.VersionedDocument;

/**
 * 
 * @author Jens
 * 
 * AbstractPathImpl is the common superclass of all objects hold in the repository that
 * have a single parent, these are: Folders
 * 
 */

public abstract class AbstractSingleFilingImpl extends StoredObjectImpl implements
    SingleFiling {

  protected FolderImpl fParent;

  protected AbstractSingleFilingImpl(ObjectStoreImpl objStore) {
    super(objStore);
  }
    
  /*
   * (non-Javadoc)
   * 
   * @see org.opencmis.client.provider.spi.inmemory.StoredObjectWithPath#getPath()
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
   * @see org.opencmis.client.provider.spi.inmemory.StoredObjectWithPath#getParent()
   */
  public Folder getParent() {
    return fParent;
  }
  
  public List<Folder> getParents() {
    if (null==fParent)
      return Collections.emptyList();
    else
      return Collections.singletonList((Folder)fParent);
  }

  /* (non-Javadoc)
   * @see org.opencmis.client.provider.spi.inmemory.storedobj.api.StoredObjectWithPath#setParent(org.opencmis.client.provider.spi.inmemory.storedobj.api.Folder)
   */
  public void setParent(Folder parent) {
    fParent = (FolderImpl) parent;
  }
  
  public void rename(String newName) {
    if (!NameValidator.isValidId(newName))
      throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_NAME);
    if (getParent() == null)
      throw new CmisInvalidArgumentException("Root folder cannot be renamed.");
    if (getParent().hasChild(newName))
      throw new CmisNameConstraintViolationException("Cannot rename object to " + newName
          + ". This path already exists.");

    setName(newName);
  }
  
  public void move(Folder oldParent, Folder newParent) {
    
    if (this instanceof Document || this instanceof VersionedDocument)
      fParent.moveChildDocument(this, oldParent, newParent);    
    else {// it must be a folder
      if (getParent() == null)
        throw new IllegalArgumentException("Root folder cannot be moved.");
      if (newParent == null)
        throw new IllegalArgumentException("null is not a valid move target.");
      if (newParent.hasChild(getName()))
        throw new IllegalArgumentException(
            "Cannot move folder, this name already exists in target.");

      setParent(newParent);
    }
  }


}
