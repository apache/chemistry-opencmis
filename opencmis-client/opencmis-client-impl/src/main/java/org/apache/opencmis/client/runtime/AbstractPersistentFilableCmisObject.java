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

import java.util.ArrayList;
import java.util.List;

import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.FileableCmisObject;
import org.apache.opencmis.client.api.Folder;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectParentData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.PropertyIdData;
import org.apache.opencmis.commons.provider.PropertyStringData;

public abstract class AbstractPersistentFilableCmisObject extends AbstractPersistentCmisObject
    implements FileableCmisObject {

  private List<Folder> parents;
  private List<String> paths;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.FileableCmisObject#getParents()
   */
  public List<Folder> getParents() {
    if (this.parents != null) {
      return this.parents;
    }

    // get object ids and paths of the parent folders
    String objectId = getObjectId();
    List<ObjectParentData> providerParents = getProvider().getNavigationService().getObjectParents(
        getRepositoryId(), objectId, PropertyIds.CMIS_OBJECT_ID + "," + PropertyIds.CMIS_PATH,
        false, IncludeRelationships.NONE, null, true, null);

    this.parents = new ArrayList<Folder>();
    this.paths = new ArrayList<String>();

    for (ObjectParentData p : providerParents) {
      if ((p == null) || (p.getObject() == null) || (p.getObject().getProperties() == null)) {
        // should not happen...
        throw new CmisRuntimeException("Repository sent invalid data!");
      }

      // get id property
      PropertyData<?> idProperty = p.getObject().getProperties().getProperties().get(
          PropertyIds.CMIS_OBJECT_ID);
      if (!(idProperty instanceof PropertyIdData)) {
        // the repository sent an object without a valid object id...
        throw new CmisRuntimeException("Repository sent invalid data! No object id!");
      }

      // get path property
      PropertyData<?> pathProperty = p.getObject().getProperties().getProperties().get(
          PropertyIds.CMIS_PATH);
      if (!(pathProperty instanceof PropertyStringData)) {
        // the repository sent a folder without a valid path...
        throw new CmisRuntimeException("Repository sent invalid data! No path property!");
      }

      if (p.getRelativePathSegment() == null) {
        // the repository didn't send a relative path segment
        throw new CmisRuntimeException("Repository sent invalid data! No relative path segement!");
      }

      // fetch the object and make sure it is a folder
      CmisObject parentFolder = getSession().getObject((String) idProperty.getFirstValue());
      if (!(parentFolder instanceof Folder)) {
        // the repository sent an object that is not a folder...
        throw new CmisRuntimeException("Repository sent invalid data! Object is not a folder!");
      }

      this.parents.add((Folder) parentFolder);

      String folderPath = ((String) pathProperty.getFirstValue());
      this.paths.add(folderPath + (folderPath.endsWith("/") ? "" : "/")
          + p.getRelativePathSegment());
    }

    return this.parents;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.FileableCmisObject#getPaths()
   */
  public List<String> getPaths() {
    if (this.paths != null) {
      return this.paths;
    }

    getParents(); // fills the paths list too

    return this.paths;
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.api.FileableCmisObject#addToFolder(org.apache.opencmis.client.api
   * .Folder, boolean)
   */
  public void addToFolder(Folder folder, boolean allVersions) {
    String objectId = getObjectId();

    if (folder == null) {
      throw new IllegalArgumentException("Folder must be set!");
    }

    if (folder.getId() == null) {
      throw new IllegalArgumentException("Folder must contain an object id!");
    }

    getProvider().getMultiFilingService().addObjectToFolder(getRepositoryId(), objectId,
        folder.getId(), allVersions, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.api.FileableCmisObject#removeFromFolder(org.apache.opencmis.client
   * .api.Folder)
   */
  public void removeFromFolder(Folder folder) {
    String objectId = getObjectId();

    if (folder == null) {
      throw new IllegalArgumentException("Folder must be set!");
    }

    if (folder.getId() == null) {
      throw new IllegalArgumentException("Folder must contain an object id!");
    }

    getProvider().getMultiFilingService().removeObjectFromFolder(getRepositoryId(), objectId,
        folder.getId(), null);
  }
}
