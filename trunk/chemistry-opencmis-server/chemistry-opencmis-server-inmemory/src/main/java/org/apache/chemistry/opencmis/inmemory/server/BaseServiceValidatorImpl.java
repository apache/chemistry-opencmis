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

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.CmisServiceValidator;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Policy;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;

public class BaseServiceValidatorImpl implements CmisServiceValidator {

    protected final StoreManager fStoreManager;

    public BaseServiceValidatorImpl(StoreManager sm) {
        fStoreManager = sm;
    }

    /**
     * Check if repository is known and that object exists. To avoid later calls
     * to again retrieve the object from the id return the retrieved object for
     * later use.
     * 
     * @param repositoryId
     *            repository id
     * @param objectId
     *            object id
     * @return object for objectId
     */
    protected StoredObject checkStandardParameters(String repositoryId, String objectId) {
        if (null == repositoryId) {
            throw new CmisInvalidArgumentException("Repository Id cannot be null.");
        }

        if (null == objectId) {
            throw new CmisInvalidArgumentException("Object Id cannot be null.");
        }

        ObjectStore objStore = fStoreManager.getObjectStore(repositoryId);

        if (objStore == null) {
            throw new CmisObjectNotFoundException("Unknown repository id: " + repositoryId);
        }

        StoredObject so = objStore.getObjectById(objectId);

        if (so == null) {
            throw new CmisObjectNotFoundException("Unknown object id: " + objectId);
        }

        return so;
    }

    protected StoredObject checkStandardParametersByPath(String repositoryId, String path, String user) {
        if (null == repositoryId) {
            throw new CmisInvalidArgumentException("Repository Id cannot be null.");
        }

        if (null == path) {
            throw new CmisInvalidArgumentException("Path parameter cannot be null.");
        }

        ObjectStore objStore = fStoreManager.getObjectStore(repositoryId);

        if (objStore == null) {
            throw new CmisObjectNotFoundException("Unknown repository id: " + repositoryId);
        }

        StoredObject so = objStore.getObjectByPath(path, user);

        if (so == null) {
            throw new CmisObjectNotFoundException("Unknown path: " + path);
        }

        return so;
    }

    protected StoredObject checkStandardParametersAllowNull(String repositoryId, String objectId) {

        StoredObject so = null;

        if (null == repositoryId) {
            throw new CmisInvalidArgumentException("Repository Id cannot be null.");
        }

        if (null != objectId) {

            ObjectStore objStore = fStoreManager.getObjectStore(repositoryId);

            if (objStore == null) {
                throw new CmisObjectNotFoundException("Unknown repository id: " + repositoryId);
            }

            so = objStore.getObjectById(objectId);

            if (so == null) {
                throw new CmisObjectNotFoundException("Unknown object id: " + objectId);
            }
        }

        return so;
    }

    protected StoredObject checkExistingObjectId(ObjectStore objStore, String objectId) {

        if (null == objectId) {
            throw new CmisInvalidArgumentException("Object Id cannot be null.");
        }

        StoredObject so = objStore.getObjectById(objectId);

        if (so == null) {
            throw new CmisObjectNotFoundException("Unknown object id: " + objectId);
        }

        return so;
    }

    protected void checkRepositoryId(String repositoryId) {
        if (null == repositoryId) {
            throw new CmisInvalidArgumentException("Repository Id cannot be null.");
        }

        ObjectStore objStore = fStoreManager.getObjectStore(repositoryId);

        if (objStore == null) {
            throw new CmisInvalidArgumentException("Unknown repository id: " + repositoryId);
        }
    }

    protected StoredObject[] checkParams(String repositoryId, String objectId1, String objectId2) {
        StoredObject[] so = new StoredObject[2];
        ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);
        so[0] = checkExistingObjectId(objectStore, objectId1);
        so[1] = checkExistingObjectId(objectStore, objectId2);
        return so;
    }
    
    protected void checkPolicies(String repositoryId, List<String> policyIds) {
        if (policyIds != null && policyIds.size() > 0) {
            for (String policyId : policyIds) {
                TypeDefinitionContainer tdc = fStoreManager.getTypeById(repositoryId, policyId);
                if (tdc == null)
                    throw new CmisInvalidArgumentException("Unknown policy type: " + policyId);
                if (tdc.getTypeDefinition().getBaseTypeId() != BaseTypeId.CMIS_POLICY)
                    throw new CmisInvalidArgumentException( policyId + " is not a policy type");
            }
        }
    }

    @Override
	public void getRepositoryInfos(CallContext context, ExtensionsData extension) {
    }

    @Override
	public void getRepositoryInfo(CallContext context, String repositoryId, ExtensionsData extension) {

        checkRepositoryId(repositoryId);
    }

    @Override
	public void getTypeChildren(CallContext context, String repositoryId, String typeId, ExtensionsData extension) {

        checkRepositoryId(repositoryId);
    }

    @Override
	public void getTypeDescendants(CallContext context, String repositoryId, String typeId, ExtensionsData extension) {

        checkRepositoryId(repositoryId);
    }

    @Override
	public void getTypeDefinition(CallContext context, String repositoryId, String typeId, ExtensionsData extension) {

        checkRepositoryId(repositoryId);
    }

    @Override
	public StoredObject getChildren(CallContext context, String repositoryId, String folderId, ExtensionsData extension) {

        return checkStandardParameters(repositoryId, folderId);
    }

    @Override
	public StoredObject getDescendants(CallContext context, String repositoryId, String folderId,
            ExtensionsData extension) {

        return checkStandardParameters(repositoryId, folderId);
    }

    @Override
	public StoredObject getFolderTree(CallContext context, String repositoryId, String folderId,
            ExtensionsData extension) {

        return checkStandardParameters(repositoryId, folderId);
    }

    @Override
	public StoredObject getObjectParents(CallContext context, String repositoryId, String objectId,
            ExtensionsData extension) {

        return checkStandardParameters(repositoryId, objectId);
    }

    @Override
	public StoredObject getFolderParent(CallContext context, String repositoryId, String folderId,
            ExtensionsData extension) {

        return checkStandardParameters(repositoryId, folderId);
    }

    @Override
	public StoredObject getCheckedOutDocs(CallContext context, String repositoryId, String folderId,
            ExtensionsData extension) {

        if (null != folderId) {
            return checkStandardParameters(repositoryId, folderId);
        } else {
            checkRepositoryId(repositoryId);
            return null;
        }

    }

    @Override
	public StoredObject createDocument(CallContext context, String repositoryId, String folderId,
            List<String> policyIds, ExtensionsData extension) {
        return checkStandardParametersAllowNull(repositoryId, folderId);
    }

    @Override
	public StoredObject createDocumentFromSource(CallContext context, String repositoryId, String sourceId,
            String folderId, List<String> policyIds, ExtensionsData extension) {

        return checkStandardParametersAllowNull(repositoryId, sourceId);
    }

    @Override
	public StoredObject createFolder(CallContext context, String repositoryId, String folderId, List<String> policyIds,
            ExtensionsData extension) {
        return checkStandardParameters(repositoryId, folderId);
    }

    @Override
	public StoredObject[] createRelationship(CallContext context, String repositoryId, String sourceId,
            String targetId, List<String> policyIds, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkStandardParametersAllowNull(repositoryId, null);
        return checkParams(repositoryId, sourceId, targetId);
    }

    @Override
	public StoredObject createPolicy(CallContext context, String repositoryId, String folderId, Acl addAces,
            Acl removeAces, List<String> policyIds, ExtensionsData extension) {

        return checkStandardParametersAllowNull(repositoryId, null);
    }

    // CMIS 1.1
    @Override
	public StoredObject createItem(CallContext context, String repositoryId, Properties properties, String folderId,
            List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
        return checkStandardParametersAllowNull(repositoryId, folderId);
    }

    @Override
	public StoredObject getAllowableActions(CallContext context, String repositoryId, String objectId,
            ExtensionsData extension) {
        //
        return checkStandardParameters(repositoryId, objectId);
    }

    @Override
	public StoredObject getObject(CallContext context, String repositoryId, String objectId, ExtensionsData extension) {

        StoredObject so = checkStandardParameters(repositoryId, objectId);
        return so;
    }

    @Override
	public StoredObject getProperties(CallContext context, String repositoryId, String objectId,
            ExtensionsData extension) {

        return checkStandardParameters(repositoryId, objectId);
    }

    @Override
	public StoredObject getRenditions(CallContext context, String repositoryId, String objectId,
            ExtensionsData extension) {

        return checkStandardParameters(repositoryId, objectId);
    }

    @Override
	public StoredObject getObjectByPath(CallContext context, String repositoryId, String path, ExtensionsData extension) {

        return checkStandardParametersByPath(repositoryId, path, context.getUsername());
    }

    @Override
	public StoredObject getContentStream(CallContext context, String repositoryId, String objectId, String streamId,
            ExtensionsData extension) {

        return checkStandardParameters(repositoryId, objectId);
    }

    @Override
	public StoredObject updateProperties(CallContext context, String repositoryId, Holder<String> objectId,
            ExtensionsData extension) {

        return checkStandardParameters(repositoryId, objectId.getValue());
    }

    @Override
	public StoredObject[] moveObject(CallContext context, String repositoryId, Holder<String> objectId,
            String targetFolderId, String sourceFolderId, ExtensionsData extension) {

        StoredObject[] res = new StoredObject[3];
        res[0] = checkStandardParameters(repositoryId, objectId.getValue());
        res[1] = checkExistingObjectId(fStoreManager.getObjectStore(repositoryId), sourceFolderId);
        res[2] = checkExistingObjectId(fStoreManager.getObjectStore(repositoryId), targetFolderId);
        return res;
    }

    @Override
	public StoredObject deleteObject(CallContext context, String repositoryId, String objectId, Boolean allVersions,
            ExtensionsData extension) {

        return checkStandardParameters(repositoryId, objectId);
    }

    @Override
	public StoredObject deleteTree(CallContext context, String repositoryId, String folderId, Boolean allVersions,
            UnfileObject unfileObjects, ExtensionsData extension) {
        return checkStandardParameters(repositoryId, folderId);
    }

    @Override
	public StoredObject setContentStream(CallContext context, String repositoryId, Holder<String> objectId,
            Boolean overwriteFlag, ExtensionsData extension) {

        return checkStandardParameters(repositoryId, objectId.getValue());
    }

    @Override
	public StoredObject appendContentStream(CallContext context, String repositoryId, Holder<String> objectId,
            ExtensionsData extension) {
        return checkStandardParameters(repositoryId, objectId.getValue());
    }

    @Override
	public StoredObject deleteContentStream(CallContext context, String repositoryId, Holder<String> objectId,
            ExtensionsData extension) {
        return checkStandardParameters(repositoryId, objectId.getValue());
    }

    @Override
	public StoredObject checkOut(CallContext context, String repositoryId, Holder<String> objectId,
            ExtensionsData extension, Holder<Boolean> contentCopied) {

        return checkStandardParameters(repositoryId, objectId.getValue());
    }

    @Override
	public StoredObject cancelCheckOut(CallContext context, String repositoryId, String objectId,
            ExtensionsData extension) {

        return checkStandardParameters(repositoryId, objectId);
    }

    @Override
	public StoredObject checkIn(CallContext context, String repositoryId, Holder<String> objectId, Acl addAces,
            Acl removeAces, List<String> policyIds, ExtensionsData extension) {
        return checkStandardParameters(repositoryId, objectId.getValue());
    }

    @Override
	public StoredObject getObjectOfLatestVersion(CallContext context, String repositoryId, String objectId,
            String versionSeriesId, ExtensionsData extension) {

        return checkStandardParameters(repositoryId, versionSeriesId == null ? objectId : versionSeriesId);
    }

    @Override
	public StoredObject getPropertiesOfLatestVersion(CallContext context, String repositoryId, String objectId,
            String versionSeriesId, ExtensionsData extension) {

        return checkStandardParameters(repositoryId, versionSeriesId == null ? objectId : versionSeriesId);
    }

    @Override
	public StoredObject getAllVersions(CallContext context, String repositoryId, String objectId,
            String versionSeriesId, ExtensionsData extension) {

        return checkStandardParameters(repositoryId, versionSeriesId == null ? objectId : versionSeriesId);
    }

    @Override
	public void query(CallContext context, String repositoryId, ExtensionsData extension) {

        checkRepositoryId(repositoryId);
    }

    @Override
	public void getContentChanges(CallContext context, String repositoryId, ExtensionsData extension) {

        checkRepositoryId(repositoryId);
    }

    @Override
	public StoredObject[] addObjectToFolder(CallContext context, String repositoryId, String objectId, String folderId,
            Boolean allVersions, ExtensionsData extension) {

        return checkParams(repositoryId, objectId, folderId);
    }

    @Override
	public StoredObject[] removeObjectFromFolder(CallContext context, String repositoryId, String objectId,
            String folderId, ExtensionsData extension) {

        return checkParams(repositoryId, objectId, folderId);
    }

    @Override
	public StoredObject getObjectRelationships(CallContext context, String repositoryId, String objectId,
            RelationshipDirection relationshipDirection, String typeId, ExtensionsData extension) {

        StoredObject so = checkStandardParameters(repositoryId, objectId);

        if (relationshipDirection == null) {
            throw new CmisInvalidArgumentException("Relationship direction cannot be null.");
        }

        if (typeId != null) {
            TypeDefinition typeDef = fStoreManager.getTypeById(repositoryId, typeId).getTypeDefinition();
            if (typeDef == null) {
                throw new CmisInvalidArgumentException("Type Id " + typeId + " is not known in repository "
                        + repositoryId);
            }

            if (!typeDef.getBaseTypeId().equals(BaseTypeId.CMIS_RELATIONSHIP)) {
                throw new CmisInvalidArgumentException("Type Id " + typeId + " is not a relationship type.");
            }
        }
        return so;
    }

    @Override
	public StoredObject getAcl(CallContext context, String repositoryId, String objectId, ExtensionsData extension) {

        return checkStandardParameters(repositoryId, objectId);
    }

    @Override
	public StoredObject applyAcl(CallContext context, String repositoryId, String objectId,
            AclPropagation aclPropagation, ExtensionsData extension) {

        return checkStandardParameters(repositoryId, objectId);
    }

    @Override
	public StoredObject[] applyPolicy(CallContext context, String repositoryId, String policyId, String objectId,
            ExtensionsData extension) {

        return checkParams(repositoryId, policyId, objectId);
    }

    @Override
	public StoredObject[] removePolicy(CallContext context, String repositoryId, String policyId, String objectId,
            ExtensionsData extension) {

        StoredObject[] sos = checkParams(repositoryId, policyId, objectId);
        StoredObject pol = sos[0];
        if (!(pol instanceof Policy))
            throw new CmisInvalidArgumentException("Id " + policyId + " is not a policy object.");
        return sos;
    }

    @Override
	public StoredObject getAppliedPolicies(CallContext context, String repositoryId, String objectId,
            ExtensionsData extension) {

        return checkStandardParameters(repositoryId, objectId);
    }

    @Override
	public StoredObject create(CallContext context, String repositoryId, String folderId, ExtensionsData extension) {

        return checkStandardParameters(repositoryId, folderId);
    }

    public StoredObject deleteObjectOrCancelCheckOut(CallContext context, String repositoryId, String objectId,
            ExtensionsData extension) {

        return checkStandardParameters(repositoryId, objectId);
    }

    @Override
	public StoredObject applyAcl(CallContext context, String repositoryId, String objectId) {

        return checkStandardParameters(repositoryId, objectId);
    }
}
