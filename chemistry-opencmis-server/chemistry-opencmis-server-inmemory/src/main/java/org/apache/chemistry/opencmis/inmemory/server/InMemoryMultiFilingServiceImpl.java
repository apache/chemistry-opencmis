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
package org.apache.chemistry.opencmis.inmemory.server;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Folder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.MultiFiling;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.chemistry.opencmis.inmemory.types.PropertyCreationHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InMemoryMultiFilingServiceImpl extends InMemoryAbstractServiceImpl {

    private static final Log LOG = LogFactory.getLog(InMemoryMultiFilingServiceImpl.class.getName());

    AtomLinkInfoProvider fAtomLinkProvider;

    public InMemoryMultiFilingServiceImpl(StoreManager storeMgr) {
        super(storeMgr);
        fAtomLinkProvider = new AtomLinkInfoProvider(storeMgr);
    }

    public void addObjectToFolder(CallContext context, String repositoryId, String objectId, String folderId,
            Boolean allVersions, ExtensionsData extension, ObjectInfoHandler objectInfos) {

        LOG.debug("Begin addObjectToFolder()");

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

        if (context.isObjectInfoRequired()) {
            ObjectInfoImpl objectInfo = new ObjectInfoImpl();
            fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfo);
            fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, folder, objectInfo);
            objectInfos.addObjectInfo(objectInfo);
        }

        LOG.debug("End addObjectToFolder()");
    }

    public void removeObjectFromFolder(CallContext context, String repositoryId, String objectId,
            String folderId, ExtensionsData extension, ObjectInfoHandler objectInfos) {

        LOG.debug("Begin removeObjectFromFolder()");

        StoredObject so = checkStandardParameters(repositoryId, objectId);
        ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);
        StoredObject folder = checkExistingObjectId(objectStore, folderId);

        checkObjects(so, folder);
        Folder parent = (Folder) folder;
        MultiFiling obj = (MultiFiling) so;
        obj.removeParent(parent);

        // To be able to provide all Atom links in the response we need
        // additional information:
        if (context.isObjectInfoRequired()) {
            ObjectInfoImpl objectInfo = new ObjectInfoImpl();
            fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfo);
            fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, folder, objectInfo);
            objectInfos.addObjectInfo(objectInfo);
        }

        LOG.debug("End removeObjectFromFolder()");
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
