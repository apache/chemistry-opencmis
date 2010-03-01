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
package org.apache.opencmis.client.runtime;

import java.util.List;

import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.FileableCmisObject;
import org.apache.opencmis.client.api.Folder;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.Holder;

public abstract class AbstractPersistentFilableCmisObject extends AbstractPersistentCmisObject
    implements FileableCmisObject {

  public List<Folder> getParents() {
    // TODO Auto-generated method stub
    throw new CmisRuntimeException("not implemented");
  }

  public List<String> getPaths() {
    // TODO Auto-generated method stub
    throw new CmisRuntimeException("not implemented");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.api.FileableCmisObject#move(org.apache.opencmis.client.api.Folder,
   * org.apache.opencmis.client.api.Folder)
   */
  public FileableCmisObject move(Folder sourceFolder, Folder targetFolder) {
    if (sourceFolder == null) {
      throw new IllegalArgumentException("Source folder must be set!");
    }

    if (targetFolder == null) {
      throw new IllegalArgumentException("Target folder must be set!");
    }

    return move(sourceFolder.getId(), targetFolder.getId());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.FileableCmisObject#move(java.lang.String, java.lang.String)
   */
  public FileableCmisObject move(String sourceFolderId, String targetFolderId) {
    String objectId = getObjectId();
    Holder<String> objectIdHolder = new Holder<String>(objectId);

    if (sourceFolderId == null) {
      throw new IllegalArgumentException("Source folder id must be set!");
    }

    if (targetFolderId == null) {
      throw new IllegalArgumentException("Target folder id must be set!");
    }

    getProvider().getObjectService().moveObject(getRepositoryId(), objectIdHolder, targetFolderId,
        sourceFolderId, null);

    if (objectIdHolder.getValue() == null) {
      return null;
    }

    CmisObject movedObject = getSession().getObject(objectIdHolder.getValue());
    if (movedObject instanceof FileableCmisObject) {
      return (FileableCmisObject) movedObject;
    }

    return null;
  }

  public void addToFolder(Folder folder, boolean allVersions) {
    // TODO Auto-generated method stub
    throw new CmisRuntimeException("not implemented");
  }

  public void removeFromFolder(Folder folder) {
    // TODO Auto-generated method stub
    throw new CmisRuntimeException("not implemented");
  }
}
