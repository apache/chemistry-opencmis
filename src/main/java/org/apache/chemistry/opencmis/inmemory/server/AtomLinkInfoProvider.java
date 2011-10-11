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

import java.util.List;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Content;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Filing;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.VersionedDocument;
import org.apache.chemistry.opencmis.inmemory.types.PropertyCreationHelper;

/**
 * For the Atom binding more information might be required than the result of a
 * service call provides (mainly to fill all the links). This class fills the
 * objectInfoHolder that was introduced for this purpose
 *
 * @author Jens
 *
 */
public class AtomLinkInfoProvider {

    private final StoreManager fStoreManager;

    public AtomLinkInfoProvider(StoreManager storeManager) {
        fStoreManager = storeManager;
    }

    /**
     * FillObjectInfoHolder object with required information needed for Atom
     * binding for a single object
     *
     * @param repositoryId
     *            id of repository
     * @param objectId
     *            object to retrieve information for
     * @param objectInfos
     *            Holder to fill with information
     */
    public void fillInformationForAtomLinks(String repositoryId, StoredObject so, ObjectData od, ObjectInfoImpl objInfo) {
        if (null == objInfo || null == so) {
            return;
        }
        TypeDefinition typeDef = fStoreManager.getTypeById(repositoryId, so.getTypeId()).getTypeDefinition();

        // Fill all setters:
        objInfo.setId(so.getId());
        objInfo.setName(so.getName());
        objInfo.setCreatedBy(so.getCreatedBy()); // !
        objInfo.setCreationDate(so.getCreatedAt()); // !
        objInfo.setLastModificationDate(so.getModifiedAt());
        objInfo.setTypeId(so.getTypeId());
        objInfo.setBaseType(typeDef.getBaseTypeId());
        objInfo.setObject(od);

        // versioning information:
        if (so instanceof DocumentVersion) {
            DocumentVersion ver = (DocumentVersion) so;
            DocumentVersion pwc = ver.getParentDocument().getPwc();
            objInfo.setIsCurrentVersion(ver == ver.getParentDocument().getLatestVersion(false));
            objInfo.setVersionSeriesId(ver.getParentDocument().getId());
            objInfo.setWorkingCopyId(pwc == null ? null : pwc.getId());
            objInfo.setWorkingCopyOriginalId(pwc == ver ? ver.getParentDocument().getLatestVersion(false).getId()
                    : null);
        } else if (so instanceof VersionedDocument) {
            VersionedDocument doc = (VersionedDocument) so;
            DocumentVersion pwc = doc.getPwc();
            objInfo.setIsCurrentVersion(false);
            objInfo.setVersionSeriesId(doc.getId());
            objInfo.setWorkingCopyId(pwc == null ? null : pwc.getId());
            objInfo.setWorkingCopyOriginalId(null);
        } else { // unversioned document
            objInfo.setIsCurrentVersion(true);
            objInfo.setVersionSeriesId(null);
            objInfo.setWorkingCopyId(null);
            objInfo.setWorkingCopyOriginalId(null);
        }

        if (so instanceof Content) {
            Content cont = ((Content) so);
            objInfo.setHasContent(cont.getContent(0, -1) != null);
            objInfo.setContentType(cont.getContent(0, -1) != null ? cont.getContent(0, -1).getMimeType() : null);
            objInfo.setFileName(cont.getContent(0, -1) != null ? cont.getContent(0, -1).getFileName() : null);
        } else {
            objInfo.setHasContent(false);
            objInfo.setContentType(null);
            objInfo.setFileName(null);
        }

        // Filing
        if (so instanceof Filing) {
            Filing sop = ((Filing) so);
            objInfo.setHasParent(sop.hasParent());
        } else {
            objInfo.setHasParent(false);
        }

        // Renditions, currently not supported by in-memory provider
        objInfo.setRenditionInfos(null);

        // Relationships, currently not supported by in-memory provider
        objInfo.setSupportsRelationships(false);
        objInfo.setRelationshipSourceIds(null);
        objInfo.setRelationshipTargetIds(null);

        // Policies, currently not supported by in-memory provider
        objInfo.setSupportsPolicies(false);

        // ACLs, currently not supported by in-memory provider
        objInfo.setHasAcl(true);

        objInfo.setSupportsDescendants(true);
        objInfo.setSupportsFolderTree(true);

    }

    public void fillInformationForAtomLinks(String repositoryId, StoredObject so, ObjectInfoImpl objectInfo) {
        TypeDefinition td = fStoreManager.getTypeById(repositoryId, so.getTypeId()).getTypeDefinition();
        ObjectData od = PropertyCreationHelper.getObjectData(td, so, null, null, false,
                IncludeRelationships.NONE, null, false, false, null);
        fillInformationForAtomLinks(repositoryId, so, od, objectInfo);
    }

    /**
     * FillObjectInfoHolder object with required information needed for Atom
     * binding for a single object
     *
     * @param repositoryId
     *            id of repository
     * @param objectId
     *            object to retrieve information for
     * @param objectInfos
     *            Holder to fill with information
     */
   public void fillInformationForAtomLinks(String repositoryId, String objectId, ObjectInfoImpl objectInfo) {
        if (null == objectInfo || null == objectId) {
            return;
        }

        ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);
        StoredObject so = objectStore.getObjectById(objectId);
        fillInformationForAtomLinks(repositoryId, so, objectInfo);
    }


    /**
     * FillObjectInfoHolder object with required information needed for Atom
     * binding after a getChildren() call in navigation service
     *
     * @param repositoryId
     *            id of repository
     * @param objectId
     *            object to retrieve information for
     * @param objectInfos
     *            Holder to fill with information
     * @param objList
     *            result of getChildren call
     */
    public void fillInformationForAtomLinks(String repositoryId, String objectId, ObjectInfoHandler objectInfos,
            ObjectInFolderList objList) {

        if (null == objectInfos || null == objList || null == objectId) {
            return;
        }


        // Fill object information for requested object
        ObjectInfoImpl objectInfo = new ObjectInfoImpl();
        fillInformationForAtomLinks(repositoryId, objectId, objectInfo);
        objectInfos.addObjectInfo(objectInfo);

        // Fill object information for all children in result list
        for (ObjectInFolderData object : objList.getObjects()) {
            objectInfo = new ObjectInfoImpl();
            fillInformationForAtomLinks(repositoryId, object.getObject().getId(), objectInfo);
            objectInfos.addObjectInfo(objectInfo);
        }
    }

    /**
     * FillObjectInfoHolder object with required information needed for Atom
     * binding for an object list
     *
     * @param repositoryId
     *            id of repository
     * @param objectId
     *            object to retrieve information for
     * @param objectInfos
     *            Holder to fill with information
     * @param objList
     *            result of getChildren call
     */
    public void fillInformationForAtomLinks(String repositoryId, String objectId, ObjectInfoHandler objectInfos,
            ObjectList objList) {

        ObjectInfoImpl objectInfo = new ObjectInfoImpl();
        if (null != objectId) {
            // Fill object information for requested object
            fillInformationForAtomLinks(repositoryId, objectId, objectInfo);
            objectInfos.addObjectInfo(objectInfo);
        }

        if (null != objList && null != objList.getObjects()) {
            // Fill object information for all children in result list
            List<ObjectData> listObjects = objList.getObjects();
            if (null != listObjects) {
                for (ObjectData object : listObjects) {
                    objectInfo = new ObjectInfoImpl();
                    fillInformationForAtomLinks(repositoryId, object.getId(), objectInfo);
                    objectInfos.addObjectInfo(objectInfo);
                }
            }
        }

    }

    /**
     * FillObjectInfoHolder object with required information needed for Atom
     * binding for an ObjectInFolderContainer
     *
     * @param repositoryId
     *            id of repository
     * @param objectId
     *            object to retrieve information for
     * @param objectInfos
     *            Holder to fill with information
     * @param objList
     *            result of getChildren call
     */
    private void fillInformationForAtomLinks(String repositoryId, ObjectInfoHandler objectInfos,
            ObjectInFolderContainer oifc) {

        if (null == objectInfos || null == oifc) {
            return;
        }

        // Fill object information for all elements in result list
        fillInformationForAtomLinks(repositoryId, objectInfos, oifc.getObject());

        if (null != oifc.getChildren()) {
            for (ObjectInFolderContainer object : oifc.getChildren()) {
                // call recursively
                ObjectInfoImpl objectInfo = new ObjectInfoImpl();
                fillInformationForAtomLinks(repositoryId, objectInfos, object);
                objectInfos.addObjectInfo(objectInfo);
            }
        }
    }

    /**
     * FillObjectInfoHolder object with required information needed for Atom
     * binding for a list with ObjectInFolderContainers
     *
     * @param repositoryId
     *            id of repository
     * @param objectId
     *            object to retrieve information for
     * @param objectInfos
     *            Holder to fill with information
     * @param oifcList
     *            result of getDescendants call
     */
    public void fillInformationForAtomLinks(String repositoryId, String objectId, ObjectInfoHandler objectInfos,
            List<ObjectInFolderContainer> oifcList) {

        if (null == objectInfos || null == oifcList || null == objectId) {
            return;
        }

        ObjectInfoImpl objectInfo = new ObjectInfoImpl();
        // Fill object information for requested object
        fillInformationForAtomLinks(repositoryId, objectId, objectInfo);
        objectInfos.addObjectInfo(objectInfo);

        for (ObjectInFolderContainer object : oifcList) {
            fillInformationForAtomLinks(repositoryId, objectInfos, object);
        }
    }

    private void fillInformationForAtomLinks(String repositoryId, ObjectInfoHandler objectInfos,
            ObjectInFolderData object) {

        ObjectInfoImpl objectInfo = new ObjectInfoImpl();
        fillInformationForAtomLinks(repositoryId, object.getObject().getId(), objectInfo);
        objectInfos.addObjectInfo(objectInfo);
    }

    /**
     * FillObjectInfoHolder object with required information needed for Atom
     * binding for a list with ObjectParentData objects
     *
     * @param repositoryId
     *            id of repository
     * @param objectId
     *            object to retrieve information for
     * @param objectInfos
     *            Holder to fill with information
     * @param objParents
     *            result of getObjectParents call
     */
    public void fillInformationForAtomLinksGetParents(String repositoryId, String objectId,
            ObjectInfoHandler objectInfos, List<ObjectParentData> objParents) {

        if (null == objectInfos || null == objParents || null == objectId) {
            return;
        }

        // Fill object information for requested object
        ObjectInfoImpl objectInfo = new ObjectInfoImpl();
        fillInformationForAtomLinks(repositoryId, objectId, objectInfo);

        for (ObjectParentData object : objParents) {
            objectInfo = new ObjectInfoImpl();
            fillInformationForAtomLinks(repositoryId, object.getObject().getId(), objectInfo);
            objectInfos.addObjectInfo(objectInfo);
        }
    }

}
