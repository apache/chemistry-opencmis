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
import org.apache.opencmis.client.api.ObjectId;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectParentData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.PropertyIdData;
import org.apache.opencmis.commons.provider.PropertyStringData;

/**
 * Base class for all filable persistent session object impl classes.
 */
public abstract class AbstractPersistentFilableCmisObject extends AbstractPersistentCmisObject
    implements FileableCmisObject {

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.FileableCmisObject#getParents()
   */
  public List<Folder> getParents() {
    String objectId = getObjectId();

    // get object ids of the parent folders
    List<ObjectParentData> providerParents = getProvider().getNavigationService().getObjectParents(
        getRepositoryId(), objectId, PropertyIds.CMIS_OBJECT_ID, false, IncludeRelationships.NONE,
        null, false, null);

    List<Folder> parents = new ArrayList<Folder>();

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

      // fetch the object and make sure it is a folder
      ObjectId parentId = getSession().createObjectId((String) idProperty.getFirstValue());
      CmisObject parentFolder = getSession().getObject(parentId);
      if (!(parentFolder instanceof Folder)) {
        // the repository sent an object that is not a folder...
        throw new CmisRuntimeException("Repository sent invalid data! Object is not a folder!");
      }

      parents.add((Folder) parentFolder);
    }

    return parents;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.FileableCmisObject#getPaths()
   */
  public List<String> getPaths() {
    String objectId = getObjectId();

    // get object paths of the parent folders
    List<ObjectParentData> providerParents = getProvider().getNavigationService().getObjectParents(
        getRepositoryId(), objectId, PropertyIds.CMIS_PATH, false, IncludeRelationships.NONE, null,
        true, null);

    List<String> paths = new ArrayList<String>();

    for (ObjectParentData p : providerParents) {
      if ((p == null) || (p.getObject() == null) || (p.getObject().getProperties() == null)) {
        // should not happen...
        throw new CmisRuntimeException("Repository sent invalid data!");
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

      String folderPath = ((String) pathProperty.getFirstValue());
      paths.add(folderPath + (folderPath.endsWith("/") ? "" : "/") + p.getRelativePathSegment());
    }

    return paths;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.api.FileableCmisObject#move(org.apache.opencmis.client.api.ObjectId,
   * org.apache.opencmis.client.api.ObjectId)
   */
  public FileableCmisObject move(ObjectId sourceFolderId, ObjectId targetFolderId) {
    String objectId = getObjectId();
    Holder<String> objectIdHolder = new Holder<String>(objectId);

    if ((sourceFolderId == null) || (sourceFolderId.getId() == null)) {
      throw new IllegalArgumentException("Source folder id must be set!");
    }

    if ((targetFolderId == null) || (targetFolderId.getId() == null)) {
      throw new IllegalArgumentException("Target folder id must be set!");
    }

    getProvider().getObjectService().moveObject(getRepositoryId(), objectIdHolder,
        targetFolderId.getId(), sourceFolderId.getId(), null);

    if (objectIdHolder.getValue() == null) {
      return null;
    }

    CmisObject movedObject = getSession().getObject(
        getSession().createObjectId(objectIdHolder.getValue()));
    if (!(movedObject instanceof FileableCmisObject)) {
      throw new CmisRuntimeException("Moved object is invalid!");
    }

    return (FileableCmisObject) movedObject;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.api.FileableCmisObject#addToFolder(org.apache.opencmis.client.api
   * .ObjectId, boolean)
   */
  public void addToFolder(ObjectId folderId, boolean allVersions) {
    String objectId = getObjectId();

    if ((folderId == null) || (folderId.getId() == null)) {
      throw new IllegalArgumentException("Folder Id must be set!");
    }

    getProvider().getMultiFilingService().addObjectToFolder(getRepositoryId(), objectId,
        folderId.getId(), allVersions, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.api.FileableCmisObject#removeFromFolder(org.apache.opencmis.client
   * .api.ObjectId)
   */
  public void removeFromFolder(ObjectId folderId) {
    String objectId = getObjectId();

    if ((folderId == null) || (folderId.getId() == null)) {
      throw new IllegalArgumentException("Folder Id must be set!");
    }

    getProvider().getMultiFilingService().removeObjectFromFolder(getRepositoryId(), objectId,
        folderId.getId(), null);
  }
}
