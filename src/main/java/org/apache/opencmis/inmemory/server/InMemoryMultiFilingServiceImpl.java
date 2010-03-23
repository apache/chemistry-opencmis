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
package org.apache.opencmis.inmemory.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.inmemory.storedobj.api.Folder;
import org.apache.opencmis.inmemory.storedobj.api.MultiFiling;
import org.apache.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.opencmis.inmemory.types.PropertyCreationHelper;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisMultiFilingService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

public class InMemoryMultiFilingServiceImpl extends AbstractServiceImpl implements
    CmisMultiFilingService {

  private static final Log LOG = LogFactory.getLog(InMemoryMultiFilingServiceImpl.class.getName());

  AtomLinkInfoProvider fAtomLinkProvider;

  public InMemoryMultiFilingServiceImpl(StoreManager storeMgr) {
    super(storeMgr);
    fAtomLinkProvider = new AtomLinkInfoProvider(storeMgr);
  }

  public ObjectData addObjectToFolder(CallContext context, String repositoryId, String objectId,
      String folderId, Boolean allVersions, ExtensionsData extension, ObjectInfoHolder objectInfos) {

    try {
      LOG.debug("Begin addObjectToFolder()");

      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      StoredObject[] so2 = checkParams(repositoryId, objectId, folderId);
      if (allVersions != null && allVersions.booleanValue() == false)
        throw new CmisNotSupportedException(
            "Cannot add object to folder, version specific filing is not supported.");
      StoredObject so = so2[0];
      StoredObject folder = so2[1];
      checkObjects(so, folder);

      Folder newParent = (Folder) folder;
      MultiFiling obj = (MultiFiling) so;
      obj.addParent(newParent);

      fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfos);
      fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, folder, objectInfos);

      ObjectData od = PropertyCreationHelper.getObjectData(fStoreManager, so, null, false,
          IncludeRelationships.NONE, null, false, false, extension);

      LOG.debug("End addObjectToFolder()");
      return od;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public ObjectData removeObjectFromFolder(CallContext context, String repositoryId,
      String objectId, String folderId, ExtensionsData extension, ObjectInfoHolder objectInfos) {

    try {
      LOG.debug("Begin removeObjectFromFolder()");
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      StoredObject so = checkStandardParameters(repositoryId, objectId);
      ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);
      StoredObject folder = checkExistingObjectId(objectStore, folderId);

      checkObjects(so, folder);
      Folder parent = (Folder) folder;
      MultiFiling obj = (MultiFiling) so;
      obj.removeParent(parent);

      // To be able to provide all Atom links in the response we need additional information:
      fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfos);
      fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, folder, objectInfos);

      ObjectData od = PropertyCreationHelper.getObjectData(fStoreManager, so, null, false,
          IncludeRelationships.NONE, null, false, false, extension);

      LOG.debug("End removeObjectFromFolder()");

      return od;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  private StoredObject[] checkParams(String repositoryId, String objectId, String folderId) {
    StoredObject[] so = new StoredObject[2];
    so[0] = checkStandardParameters(repositoryId, objectId);
    ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);
    so[1] = checkExistingObjectId(objectStore, folderId);
    return so;
  }

  private void checkObjects(StoredObject so, StoredObject folder) {
    if (!(so instanceof MultiFiling))
      throw new CmisConstraintException("Cannot add object to folder, object id " + so.getId()
          + " is not a multi-filed object.");

    if ((so instanceof Folder))
      throw new CmisConstraintException("Cannot add object to folder, object id " + folder.getId()
          + " is a folder and folders are not multi-filed.");

    if (!(folder instanceof Folder))
      throw new CmisConstraintException("Cannot add object to folder, folder id " + folder.getId()
          + " does not refer to a folder.");
  }

}
