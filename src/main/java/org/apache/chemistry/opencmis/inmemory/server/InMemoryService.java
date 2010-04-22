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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.api.Acl;
import org.apache.chemistry.opencmis.commons.api.AllowableActions;
import org.apache.chemistry.opencmis.commons.api.BindingsObjectFactory;
import org.apache.chemistry.opencmis.commons.api.ContentStream;
import org.apache.chemistry.opencmis.commons.api.ExtensionsData;
import org.apache.chemistry.opencmis.commons.api.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.api.Holder;
import org.apache.chemistry.opencmis.commons.api.NavigationService;
import org.apache.chemistry.opencmis.commons.api.ObjectData;
import org.apache.chemistry.opencmis.commons.api.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.api.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.api.ObjectList;
import org.apache.chemistry.opencmis.commons.api.ObjectParentData;
import org.apache.chemistry.opencmis.commons.api.ObjectService;
import org.apache.chemistry.opencmis.commons.api.Properties;
import org.apache.chemistry.opencmis.commons.api.RenditionData;
import org.apache.chemistry.opencmis.commons.api.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.api.RepositoryService;
import org.apache.chemistry.opencmis.commons.api.TypeDefinition;
import org.apache.chemistry.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.api.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.api.server.CallContext;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;
import org.apache.chemistry.opencmis.inmemory.ConfigConstants;
import org.apache.chemistry.opencmis.inmemory.clientprovider.NavigationServiceImpl;
import org.apache.chemistry.opencmis.inmemory.clientprovider.ObjectServiceImpl;
import org.apache.chemistry.opencmis.inmemory.clientprovider.RepositoryServiceImpl;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.StoreManagerFactory;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.StoreManagerImpl;
import org.apache.chemistry.opencmis.server.support.DiscoveryServiceWrapper;
import org.apache.chemistry.opencmis.server.support.NavigationServiceWrapper;
import org.apache.chemistry.opencmis.server.support.ObjectServiceWrapper;
import org.apache.chemistry.opencmis.server.support.RepositoryServiceWrapper;
import org.apache.chemistry.opencmis.server.support.VersioningServiceWrapper;
import org.apache.chemistry.opencmis.util.repository.ObjectGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InMemoryService extends AbstractCmisService {

    private static final Log LOG = LogFactory.getLog(InMemoryService.class.getName());

    private StoreManager storeManager; // singleton root of everything
    private CallContext context;

    private InMemoryRepositoryServiceImpl fRepSvc;
    private InMemoryObjectServiceImpl fObjSvc;
    private InMemoryNavigationServiceImpl fNavSvc;
    private InMemoryVersioningServiceImpl fVerSvc;
    private InMemoryDiscoveryServiceImpl fDisSvc;
    private InMemoryMultiFilingServiceImpl fMultiSvc;

    public StoreManager getStoreManager() {
        return storeManager;
    }

    public InMemoryService(Map<String, String> parameters) {

        // initialize in-memory management
        String repositoryClassName = (String) parameters.get(ConfigConstants.REPOSITORY_CLASS);
        if (null == repositoryClassName)
            repositoryClassName = StoreManagerImpl.class.getName();

        if (null == storeManager)
            storeManager = StoreManagerFactory.createInstance(repositoryClassName);

        String repositoryId = parameters.get(ConfigConstants.REPOSITORY_ID);

        List<String> allAvailableRepositories = storeManager.getAllRepositoryIds();

        // init existing repositories
        for (String existingRepId : allAvailableRepositories)
            storeManager.initRepository(existingRepId);

        // create repository if configured as a startup parameter
        if (null != repositoryId) {
            if (allAvailableRepositories.contains(repositoryId))
                LOG.warn("Repostory " + repositoryId + " already exists and will not be created.");
            else {
                String typeCreatorClassName = parameters.get(ConfigConstants.TYPE_CREATOR_CLASS);
                storeManager.createAndInitRepository(repositoryId, typeCreatorClassName);
            }
        }

        // With some special configuration settings fill the repository with
        // some documents and folders
        // if is empty
        if (!allAvailableRepositories.contains(repositoryId))
            fillRepositoryIfConfigured(parameters, repositoryId);

        fRepSvc = new InMemoryRepositoryServiceImpl(storeManager);
        fNavSvc = new InMemoryNavigationServiceImpl(storeManager);
        fObjSvc = new InMemoryObjectServiceImpl(storeManager);
        fVerSvc = new InMemoryVersioningServiceImpl(storeManager, fObjSvc);
        fDisSvc = new InMemoryDiscoveryServiceImpl(storeManager, fRepSvc, fNavSvc);
        fMultiSvc = new InMemoryMultiFilingServiceImpl(storeManager);

    }

    public CallContext getCallContext() {
        return context;
    }

    public void setCallContext(CallContext context) {
        this.context = context;
    }

    // --- repository service ---

    @Override
    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
        return fRepSvc.getRepositoryInfos(getCallContext(), extension);
    }

    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
        return fRepSvc.getRepositoryInfo(getCallContext(), repositoryId, extension);
    }

    public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return fRepSvc.getTypeChildren(getCallContext(), repositoryId, typeId, includePropertyDefinitions, maxItems,
                skipCount, extension);
    }

    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
        return fRepSvc.getTypeDefinition(getCallContext(), repositoryId, typeId, extension);
    }

    public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension) {
        return fRepSvc.getTypeDescendants(getCallContext(), repositoryId, typeId, depth, includePropertyDefinitions,
                extension);
    }

    // --- navigation service ---

    public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return fNavSvc.getCheckedOutDocs(getCallContext(), repositoryId, folderId, filter, orderBy,
                includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount, extension, null);
    }

    public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return fNavSvc.getChildren(getCallContext(), repositoryId, folderId, filter, orderBy, includeAllowableActions,
                includeRelationships, renditionFilter, includePathSegment, maxItems, skipCount, extension, null);
    }

    public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        return fNavSvc.getDescendants(getCallContext(), repositoryId, folderId, depth, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePathSegment, extension, null);
    }

    public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
        return fNavSvc.getFolderParent(getCallContext(), repositoryId, folderId, filter, extension, null);
    }

    public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        return fNavSvc.getFolderTree(getCallContext(), repositoryId, folderId, depth, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePathSegment, extension, null);
    }

    public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, ExtensionsData extension) {
        return fNavSvc.getObjectParents(getCallContext(), repositoryId, objectId, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includeRelativePathSegment, extension, null);
    }

    // --- object service ---

    public String create(String repositoryId, Properties properties, String folderId, ContentStream contentStream,
            VersioningState versioningState, List<String> policies, ExtensionsData extension) {
        ObjectData od = fObjSvc.create(getCallContext(), repositoryId, properties, folderId, contentStream,
                versioningState, policies, extension, null);
        return od.getId();

    }

    public String createDocument(String repositoryId, Properties properties, String folderId,
            ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        return fObjSvc.createDocument(getCallContext(), repositoryId, properties, folderId, contentStream,
                versioningState, policies, addAces, removeAces, extension);
    }

    public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties,
            String folderId, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        return fObjSvc.createDocumentFromSource(getCallContext(), repositoryId, sourceId, properties, folderId,
                versioningState, policies, addAces, removeAces, extension);
    }

    public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        return fObjSvc.createFolder(getCallContext(), repositoryId, properties, folderId, policies, addAces,
                removeAces, extension);
    }

    public String createPolicy(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        return fObjSvc.createPolicy(getCallContext(), repositoryId, properties, folderId, policies, addAces,
                removeAces, extension);
    }

    public String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        return fObjSvc.createRelationship(getCallContext(), repositoryId, properties, policies, addAces, removeAces,
                extension);
    }

    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ExtensionsData extension) {
        fObjSvc.deleteContentStream(getCallContext(), repositoryId, objectId, changeToken, extension);
    }

    public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {
        fObjSvc.deleteObjectOrCancelCheckOut(getCallContext(), repositoryId, objectId, allVersions, extension);
    }

    public void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions,
            ExtensionsData extension) {
        fObjSvc.deleteObjectOrCancelCheckOut(getCallContext(), repositoryId, objectId, allVersions, extension);
    }

    public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
            UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
        return fObjSvc.deleteTree(getCallContext(), repositoryId, folderId, allVersions, unfileObjects,
                continueOnFailure, extension);
    }

    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
        return fObjSvc.getAllowableActions(getCallContext(), repositoryId, objectId, extension);
    }

    public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
            BigInteger length, ExtensionsData extension) {
        return fObjSvc.getContentStream(getCallContext(), repositoryId, objectId, streamId, offset, length, extension);
    }

    public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        return fObjSvc.getObject(getCallContext(), repositoryId, objectId, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension, null);
    }

    public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        return fObjSvc.getObjectByPath(getCallContext(), repositoryId, path, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension, null);
    }

    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
        return fObjSvc.getProperties(getCallContext(), repositoryId, objectId, filter, extension);
    }

    public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return fObjSvc.getRenditions(getCallContext(), repositoryId, objectId, renditionFilter, maxItems, skipCount,
                extension);
    }

    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
            ExtensionsData extension) {
        fObjSvc.moveObject(getCallContext(), repositoryId, objectId, targetFolderId, sourceFolderId, extension, null);
    }

    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
            Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
        fObjSvc.setContentStream(getCallContext(), repositoryId, objectId, overwriteFlag, changeToken, contentStream,
                extension);
    }

    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            Properties properties, ExtensionsData extension) {
        fObjSvc.updateProperties(getCallContext(), repositoryId, objectId, changeToken, properties, null, extension,
                null);
    }

    // --- versioning service ---

    public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
        fVerSvc.cancelCheckOut(getCallContext(), repositoryId, objectId, extension);
    }

    public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties,
            ContentStream contentStream, String checkinComment, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        fVerSvc.checkIn(getCallContext(), repositoryId, objectId, major, properties, contentStream, checkinComment,
                policies, addAces, removeAces, extension, null);
    }

    public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension,
            Holder<Boolean> contentCopied) {
        fVerSvc.checkOut(getCallContext(), repositoryId, objectId, extension, contentCopied, null);
    }

    public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
        return fVerSvc.getObjectOfLatestVersion(getCallContext(), repositoryId, versionSeriesId, major, filter,
                includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeAcl,
                extension, null);
    }

    public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, ExtensionsData extension) {
        return fVerSvc.getPropertiesOfLatestVersion(getCallContext(), repositoryId, versionSeriesId, major, filter,
                extension);
    }

    public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
            Boolean includeAllowableActions, ExtensionsData extension) {
        return fVerSvc.getAllVersions(getCallContext(), repositoryId, versionSeriesId, filter, includeAllowableActions,
                extension, null);
    }

    // --- discovery service ---

    public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties,
            String filter, Boolean includePolicyIds, Boolean includeAcl, BigInteger maxItems, ExtensionsData extension) {
        return super.getContentChanges(repositoryId, changeLogToken, includeProperties, filter, includePolicyIds,
                includeAcl, maxItems, extension);
    }

    public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return super.query(repositoryId, statement, searchAllVersions, includeAllowableActions, includeRelationships,
                renditionFilter, maxItems, skipCount, extension);
    }

    // --- multi filing service ---

    public void addObjectToFolder(String repositoryId, String objectId, String folderId, Boolean allVersions,
            ExtensionsData extension) {
        fMultiSvc.addObjectToFolder(getCallContext(), repositoryId, objectId, folderId, allVersions, extension, null);
    }

    public void removeObjectFromFolder(String repositoryId, String objectId, String folderId, ExtensionsData extension) {
        fMultiSvc.removeObjectFromFolder(getCallContext(), repositoryId, objectId, folderId, extension, null);
    }

    // --- relationship service ---

    public ObjectList getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes,
            RelationshipDirection relationshipDirection, String typeId, String filter, Boolean includeAllowableActions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return super.getObjectRelationships(repositoryId, objectId, includeSubRelationshipTypes, relationshipDirection,
                typeId, filter, includeAllowableActions, maxItems, skipCount, extension);
    }

    // --- ACL service ---

    public Acl applyAcl(String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation) {
        return super.applyAcl(repositoryId, objectId, aces, aclPropagation);
    }

    public Acl applyAcl(String repositoryId, String objectId, Acl addAces, Acl removeAces,
            AclPropagation aclPropagation, ExtensionsData extension) {
        return super.applyAcl(repositoryId, objectId, addAces, removeAces, aclPropagation, extension);
    }

    public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
        return super.getAcl(repositoryId, objectId, onlyBasicPermissions, extension);
    }

    // --- policy service ---

    public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        super.applyPolicy(repositoryId, policyId, objectId, extension);
    }

    public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter,
            ExtensionsData extension) {
        return super.getAppliedPolicies(repositoryId, objectId, filter, extension);
    }

    public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        super.removePolicy(repositoryId, policyId, objectId, extension);
    }

    // //////////////
    //	
    private void fillRepositoryIfConfigured(Map<String, String> parameters, String repositoryId) {
        class DummyCallContext implements CallContext {

            public String get(String key) {
                return null;
            }

            public String getBinding() {
                return null;
            }

            public boolean isObjectInfoRequired() {
                return false;
            }

            public String getRepositoryId() {
                return null;
            }

            public String getLocale() {
                return null;
            }

            public String getPassword() {
                return null;
            }

            public String getUsername() {
                return null;
            }
        }

        String doFillRepositoryStr = parameters.get(ConfigConstants.USE_REPOSITORY_FILER);
        boolean doFillRepository = doFillRepositoryStr == null ? false : Boolean.parseBoolean(doFillRepositoryStr);

        if (!doFillRepository)
            return;

        BindingsObjectFactory objectFactory = new BindingsObjectFactoryImpl();
        NavigationService navSvc = new NavigationServiceImpl(fNavSvc);
        ObjectService objSvc = new ObjectServiceImpl(fObjSvc);
        RepositoryService repSvc = new RepositoryServiceImpl(fRepSvc);

        String levelsStr = parameters.get(ConfigConstants.FILLER_DEPTH);
        int levels = 1;
        if (null != levelsStr)
            levels = Integer.parseInt(levelsStr);

        String docsPerLevelStr = parameters.get(ConfigConstants.FILLER_DOCS_PER_FOLDER);
        int docsPerLevel = 1;
        if (null != docsPerLevelStr)
            docsPerLevel = Integer.parseInt(docsPerLevelStr);

        String childrenPerLevelStr = parameters.get(ConfigConstants.FILLER_FOLDERS_PER_FOLDER);
        int childrenPerLevel = 2;
        if (null != childrenPerLevelStr)
            childrenPerLevel = Integer.parseInt(childrenPerLevelStr);

        String documentTypeId = parameters.get(ConfigConstants.FILLER_DOCUMENT_TYPE_ID);
        if (null == documentTypeId)
            documentTypeId = BaseTypeId.CMIS_DOCUMENT.value();

        String folderTypeId = parameters.get(ConfigConstants.FILLER_FOLDER_TYPE_ID);
        if (null == folderTypeId)
            folderTypeId = BaseTypeId.CMIS_FOLDER.value();

        int contentSizeKB = 0;
        String contentSizeKBStr = parameters.get(ConfigConstants.FILLER_CONTENT_SIZE);
        if (null != contentSizeKBStr)
            contentSizeKB = Integer.parseInt(contentSizeKBStr);

        // Create a hierarchy of folders and fill it with some documents
        ObjectGenerator gen = new ObjectGenerator(objectFactory, navSvc, objSvc, repositoryId);

        gen.setNumberOfDocumentsToCreatePerFolder(docsPerLevel);

        // Set the type id for all created documents:
        gen.setDocumentTypeId(documentTypeId);

        // Set the type id for all created folders:
        gen.setFolderTypeId(folderTypeId);

        // Set contentSize
        gen.setContentSizeInKB(contentSizeKB);

        // set properties that need to be filled
        // set the properties the generator should fill with values for
        // documents:
        // Note: must be valid properties in configured document and folder type

        List<String> propsToSet = readPropertiesToSetFromConfig(parameters, ConfigConstants.FILLER_DOCUMENT_PROPERTY);
        if (null != propsToSet)
            gen.setDocumentPropertiesToGenerate(propsToSet);

        propsToSet = readPropertiesToSetFromConfig(parameters, ConfigConstants.FILLER_FOLDER_PROPERTY);
        if (null != propsToSet)
            gen.setFolderPropertiesToGenerate(propsToSet);

        // Simulate a runtime context with configuration parameters
        // Attach the CallContext to a thread local context that can be accessed
        // from everywhere
        // RuntimeContext.attachCfg(new DummyCallContext());

        // Build the tree
        RepositoryInfo rep = repSvc.getRepositoryInfo(repositoryId, null);
        String rootFolderId = rep.getRootFolderId();

        try {
            gen.createFolderHierachy(levels, childrenPerLevel, rootFolderId);
            // Dump the tree
            gen.dumpFolder(rootFolderId, "*");
        } catch (Exception e) {
            LOG.error("Could not create folder hierarchy with documents. " + e);
            e.printStackTrace();
        }

    }

    private List<String> readPropertiesToSetFromConfig(Map<String, String> parameters, String keyPrefix) {
        List<String> propsToSet = new ArrayList<String>();
        for (int i = 0;; ++i) {
            String propertyKey = keyPrefix + Integer.toString(i);
            String propertyToAdd = parameters.get(propertyKey);
            if (null == propertyToAdd)
                break;
            else
                propsToSet.add(propertyToAdd);
        }
        return propsToSet;
    }

}
