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
import java.util.List;

import org.apache.opencmis.inmemory.storedobj.api.Document;
import org.apache.opencmis.inmemory.storedobj.api.Folder;
import org.apache.opencmis.inmemory.storedobj.api.MultiFiling;
import org.apache.opencmis.inmemory.storedobj.api.VersionedDocument;

/**
 * @author Jens
 * 
 * AbstractMultiPathImpl is the common superclass of all objects hold in the repository that 
 * have multiple parent folders, these are: Folders
 */
public abstract class AbstractMultiFilingImpl extends StoredObjectImpl implements MultiFiling {

  protected List<Folder> fParents = new ArrayList<Folder>(1);

  AbstractMultiFilingImpl(ObjectStoreImpl objStore) {
    super(objStore);
  }

  /* (non-Javadoc)
   * @see org.apache.opencmis.inmemory.storedobj.api.MultiParentPath#addParent(org.apache.opencmis.inmemory.storedobj.api.Folder)
   */
  public void addParent(Folder parent) {
    
    if (parent.hasChild(getName()))
      throw new IllegalArgumentException(
          "Cannot assign new parent folder, this name already exists in target folder.");

    if (null == fParents)
      fParents = new ArrayList<Folder>();
    
    fParents.add(parent);
  }

  
  /* (non-Javadoc)
   * @see org.apache.opencmis.inmemory.storedobj.api.MultiParentPath#removeParent(org.apache.opencmis.inmemory.storedobj.api.Folder)
   */
  public void removeParent(Folder parent){
    fParents.remove(parent);
    if (fParents.isEmpty())
      fParents = null;
  }
  
  
  /* (non-Javadoc)
   * @see org.apache.opencmis.inmemory.storedobj.api.MultiParentPath#getParents()
   */
  public List<Folder> getParents() {
    return fParents;
  }

  
  /* (non-Javadoc)
   * @see org.apache.opencmis.inmemory.storedobj.api.MultiParentPath#getPathSegment()
   */
  public String getPathSegment() {
    return getName();
  }

  
  /* (non-Javadoc)
   * @see org.apache.opencmis.inmemory.storedobj.api.Path#move(org.apache.opencmis.inmemory.storedobj.api.Folder, org.apache.opencmis.inmemory.storedobj.api.Folder)
   */
  public void move(Folder oldParent, Folder newParent) {
    addParent(newParent);
    removeParent(oldParent);
  }

}
