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
package org.apache.chemistry.opencmis.inmemory.storedobj.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityOrderBy;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.SupportedPermissions;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AclCapabilitiesDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CreatablePropertyTypesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.NewTypeSettableAttributesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionContainerImpl;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.inmemory.RepositoryInfoCreator;
import org.apache.chemistry.opencmis.inmemory.TypeCreator;
import org.apache.chemistry.opencmis.inmemory.TypeManagerImpl;
import org.apache.chemistry.opencmis.inmemory.query.InMemoryQueryProcessor;
import org.apache.chemistry.opencmis.inmemory.server.InMemoryServiceContext;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.CmisServiceValidator;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.TypeManagerCreatable;
import org.apache.chemistry.opencmis.server.support.TypeManager;

/**
 * factory to create objects that are stored in the InMemory store
 * 
 * @author Jens
 */
public class StoreManagerImpl implements StoreManager {

    private static final String CMIS_READ = "cmis:read";
    private static final String CMIS_WRITE = "cmis:write";
    private static final String CMIS_ALL = "cmis:all";

    protected final BindingsObjectFactory fObjectFactory;
    protected RepositoryInfo fRepositoryInfo;
    protected CmisServiceValidator validator;
    
    private static final String OPENCMIS_VERSION;
    private static final String OPENCMIS_SERVER;

    static {
        Package p = Package.getPackage("org.apache.chemistry.opencmis.inmemory");
        if (p == null) {
            OPENCMIS_VERSION = "?";
            OPENCMIS_SERVER = "Apache-Chemistry-OpenCMIS-InMemory";
        } else {
            String ver = p.getImplementationVersion();
            OPENCMIS_VERSION = (null == ver ? "?" : ver);
            OPENCMIS_SERVER = "Apache-Chemistry-OpenCMIS-InMemory/" + OPENCMIS_VERSION;
        }
    }

    /**
     * map from repository id to a type manager
     */
    private final Map<String, TypeManagerImpl> fMapRepositoryToTypeManager = new HashMap<String, TypeManagerImpl>();

    /**
     * map from repository id to a object store
     */
    private final Map<String, ObjectStore> fMapRepositoryToObjectStore = new HashMap<String, ObjectStore>();

    public ObjectStoreImpl getStore(String repositoryId) {
        return (ObjectStoreImpl) fMapRepositoryToObjectStore.get(repositoryId);
    }

    public StoreManagerImpl() {
        fObjectFactory = new BindingsObjectFactoryImpl();
    }

    public List<String> getAllRepositoryIds() {
        Set<String> repIds = fMapRepositoryToObjectStore.keySet();
        List<String> result = new ArrayList<String>();
        result.addAll(repIds);
        return result;
    }

    public void initRepository(String repositoryId) {
        fMapRepositoryToObjectStore.put(repositoryId, new ObjectStoreImpl(repositoryId));
        fMapRepositoryToTypeManager.put(repositoryId, new TypeManagerImpl());
    }

    public void createAndInitRepository(String repositoryId, String typeCreatorClassName) {
        if (fMapRepositoryToObjectStore.containsKey(repositoryId)
                || fMapRepositoryToTypeManager.containsKey(repositoryId)) {
            throw new RuntimeException("Cannot add repository, repository " + repositoryId + " already exists.");
        }

        fMapRepositoryToObjectStore.put(repositoryId, new ObjectStoreImpl(repositoryId));
        fMapRepositoryToTypeManager.put(repositoryId, new TypeManagerImpl());

        // initialize the type system:
        initTypeSystem(repositoryId, typeCreatorClassName);
    }

    public ObjectStore getObjectStore(String repositoryId) {
        return fMapRepositoryToObjectStore.get(repositoryId);
    }

    public CmisServiceValidator getServiceValidator() {
        return new InMemoryServiceValidatorImpl(this);
    }

    public BindingsObjectFactory getObjectFactory() {
        return fObjectFactory;
    }

    public TypeDefinitionContainer getTypeById(String repositoryId, String typeId) {
        TypeManager typeManager = fMapRepositoryToTypeManager.get(repositoryId);
        if (null == typeManager) {
            throw new RuntimeException("Unknown repository " + repositoryId);
        }

        boolean cmis11 = InMemoryServiceContext.getCallContext().getCmisVersion() != CmisVersion.CMIS_1_0;
        TypeDefinitionContainer tdc = typeManager.getTypeById(typeId);
        if (null != tdc && !cmis11) {
            TypeDefinition td = tdc.getTypeDefinition();
            if (td.getBaseTypeId() == BaseTypeId.CMIS_ITEM || td.getBaseTypeId() == BaseTypeId.CMIS_SECONDARY
                    || td.getId().equals(BaseTypeId.CMIS_ITEM.value())
                    || td.getId().equals(BaseTypeId.CMIS_SECONDARY.value())) {
                tdc = null; // filter new types for CMIS 1.0
            }
        }
        return tdc;
    }

    public TypeDefinitionContainer getTypeById(String repositoryId, String typeId, boolean includePropertyDefinitions,
            int depth) {
        TypeManager typeManager = fMapRepositoryToTypeManager.get(repositoryId);
        if (null == typeManager) {
            throw new CmisInvalidArgumentException("Unknown repository " + repositoryId);
        }

        TypeDefinitionContainer tc = typeManager.getTypeById(typeId);

        if (tc != null) {
            if (depth == -1) {
                if (includePropertyDefinitions)
                    return tc;
                else
                    depth = Integer.MAX_VALUE;
            } else if (depth == 0 || depth < -1)
                throw new CmisInvalidArgumentException("illegal depth value: " + depth);

            return cloneTypeList(depth, includePropertyDefinitions, tc, null);
        } else
            return null;
    }

    public Collection<TypeDefinitionContainer> getTypeDefinitionList(String repositoryId,
            boolean includePropertyDefinitions) {
//        Collection<TypeDefinitionContainer> result;
        TypeManager typeManager = fMapRepositoryToTypeManager.get(repositoryId);
        if (null == typeManager) {
            throw new CmisInvalidArgumentException("Unknown repository " + repositoryId);
        }
        Collection<TypeDefinitionContainer> typeColl = getRootTypes(repositoryId, includePropertyDefinitions);
//        if (includePropertyDefinitions) {
//            result = typeColl;
//        } else {
//            result = new ArrayList<TypeDefinitionContainer>(typeColl.size());
//            // copy list and omit properties
//            for (TypeDefinitionContainer c : typeColl) {
//                AbstractTypeDefinition td = ((AbstractTypeDefinition) c.getTypeDefinition()).clone();
//                TypeDefinitionContainerImpl tdc = new TypeDefinitionContainerImpl(td);
//                tdc.setChildren(c.getChildren());
//                td.setPropertyDefinitions(null);
//                result.add(tdc);
//            }
//        }
//        return result;
        return typeColl;
    }

    public List<TypeDefinitionContainer> getRootTypes(String repositoryId, boolean includePropertyDefinitions) {
        List<TypeDefinitionContainer> result;
        TypeManager typeManager = fMapRepositoryToTypeManager.get(repositoryId);
        if (null == typeManager) {
            throw new CmisInvalidArgumentException("Unknown repository " + repositoryId);
        }
        List<TypeDefinitionContainer> rootTypes = typeManager.getRootTypes();
        
        // remove cmis:item and cmis:secondary for CMIS 1.0
        boolean cmis11 = InMemoryServiceContext.getCallContext().getCmisVersion() != CmisVersion.CMIS_1_0;
        if (!cmis11) {
            rootTypes = new ArrayList<TypeDefinitionContainer>(rootTypes);
            TypeDefinitionContainer tcItem = null, tcSecondary = null;
            for(TypeDefinitionContainer tc : rootTypes) {
                if (tc.getTypeDefinition().getId().equals(BaseTypeId.CMIS_ITEM.value()))
                    tcItem = tc;
                if (tc.getTypeDefinition().getId().equals(BaseTypeId.CMIS_SECONDARY.value()))
                    tcSecondary = tc;
            }
            if (tcItem != null)
                rootTypes.remove(tcItem);
            if (tcSecondary != null)
                rootTypes.remove(tcSecondary);
        }
        
        if (includePropertyDefinitions) {
            result = rootTypes;
        } else {
            result = new ArrayList<TypeDefinitionContainer>(rootTypes.size());
            // copy list and omit properties
            for (TypeDefinitionContainer c : rootTypes) {
                AbstractTypeDefinition td = ((AbstractTypeDefinition) c.getTypeDefinition()).clone();
                TypeDefinitionContainerImpl tdc = new TypeDefinitionContainerImpl(td);
                tdc.setChildren(c.getChildren());
                td.setPropertyDefinitions(null);
                result.add(tdc);
            }
        }
        return result;
    }

    public RepositoryInfo getRepositoryInfo(String repositoryId) {
        ObjectStore sm = fMapRepositoryToObjectStore.get(repositoryId);
        if (null == sm) {
            return null;
        }

        RepositoryInfo repoInfo = createRepositoryInfo(repositoryId);

        return repoInfo;
    }

    public void clearTypeSystem(String repositoryId) {
        TypeManagerImpl typeManager = fMapRepositoryToTypeManager.get(repositoryId);
        if (null == typeManager) {
            throw new CmisInvalidArgumentException("Unknown repository " + repositoryId);
        }

        typeManager.clearTypeSystem();
    }

    public void initRepositoryInfo(String repositoryId, String repoInfoCreatorClassName) {
        RepositoryInfoCreator repoCreator = null;

        if (repoInfoCreatorClassName != null) {
            Object obj = null;
            try {
                obj = Class.forName(repoInfoCreatorClassName).newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(
                        "Illegal class to create type system, must implement RepositoryInfoCreator interface.", e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        "Illegal class to create type system, must implement RepositoryInfoCreator interface.", e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(
                        "Illegal class to create type system, must implement RepositoryInfoCreator interface.", e);
            }

            if (obj instanceof RepositoryInfoCreator) {
                repoCreator = (RepositoryInfoCreator) obj;
                fRepositoryInfo = repoCreator.createRepositoryInfo();
            } else {
                throw new RuntimeException(
                        "Illegal class to create repository info, must implement RepositoryInfoCreator interface.");
            }
        } else {
            // create a default repository info
            createRepositoryInfo(repositoryId);
        }
    }

    public List<TypeDefinition> initTypeSystem(String typeCreatorClassName) {

        List<TypeDefinition> typesList = null;

        if (typeCreatorClassName != null) {
            Object obj = null;
            TypeCreator typeCreator = null;

            try {
                obj = Class.forName(typeCreatorClassName).newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(
                        "Illegal class to create type system, must implement TypeCreator interface.", e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        "Illegal class to create type system, must implement TypeCreator interface.", e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(
                        "Illegal class to create type system, must implement TypeCreator interface.", e);
            }

            if (obj instanceof TypeCreator) {
                typeCreator = (TypeCreator) obj;
            } else {
                throw new RuntimeException("Illegal class to create type system, must implement TypeCreator interface.");
            }

            // retrieve the list of available types from the configured class.
            // test
            typesList = typeCreator.createTypesList();
        }

        return typesList;
    }

    private void initTypeSystem(String repositoryId, String typeCreatorClassName) {

        List<TypeDefinition> typeDefs = null;
        TypeManagerImpl typeManager = fMapRepositoryToTypeManager.get(repositoryId);
        if (null == typeManager) {
            throw new RuntimeException("Unknown repository " + repositoryId);
        }

        if (null != typeCreatorClassName) {
            typeDefs = initTypeSystem(typeCreatorClassName);
        }

        typeManager.initTypeSystem(typeDefs);
    }

    @SuppressWarnings("serial")
    private RepositoryInfo createRepositoryInfo(String repositoryId) {
        boolean cmis11 = InMemoryServiceContext.getCallContext().getCmisVersion() != CmisVersion.CMIS_1_0;
        ObjectStore objStore = getObjectStore(repositoryId);
        String rootFolderId = objStore.getRootFolder().getId();
        // repository info
        RepositoryInfoImpl repoInfo;
        repoInfo = new RepositoryInfoImpl();
        repoInfo.setId(repositoryId == null ? "inMem" : repositoryId);
        repoInfo.setName("Apache Chemistry OpenCMIS InMemory Repository");
        repoInfo.setDescription("Apache Chemistry OpenCMIS InMemory Repository (Version: " + OPENCMIS_VERSION + ")");
        repoInfo.setRootFolder(rootFolderId);
        repoInfo.setPrincipalAnonymous(InMemoryAce.getAnonymousUser());
        repoInfo.setPrincipalAnyone(InMemoryAce.getAnyoneUser());
        repoInfo.setThinClientUri("");
        repoInfo.setChangesIncomplete(Boolean.TRUE);
        repoInfo.setLatestChangeLogToken(Long.valueOf(new Date(0).getTime()).toString());
        repoInfo.setVendorName("Apache Chemistry");
        repoInfo.setProductName(OPENCMIS_SERVER);
        repoInfo.setProductVersion(OPENCMIS_VERSION);

        // set capabilities
        RepositoryCapabilitiesImpl caps = new RepositoryCapabilitiesImpl();
        caps.setAllVersionsSearchable(false);
        caps.setCapabilityAcl(CapabilityAcl.MANAGE);
        caps.setCapabilityChanges(CapabilityChanges.OBJECTIDSONLY);
        caps.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.ANYTIME);
        caps.setCapabilityJoin(CapabilityJoin.NONE);
        caps.setCapabilityQuery(CapabilityQuery.BOTHCOMBINED);
        caps.setCapabilityRendition(CapabilityRenditions.READ);
        caps.setIsPwcSearchable(false);
        caps.setIsPwcUpdatable(true);
        caps.setSupportsGetDescendants(true);
        caps.setSupportsGetFolderTree(true);
        caps.setSupportsMultifiling(true);
        caps.setSupportsUnfiling(true);
        caps.setSupportsVersionSpecificFiling(false);
        caps.setCapabilityAcl(CapabilityAcl.MANAGE);

        AclCapabilitiesDataImpl aclCaps = new AclCapabilitiesDataImpl();
        aclCaps.setAclPropagation(AclPropagation.OBJECTONLY);
        aclCaps.setSupportedPermissions(SupportedPermissions.BASIC);

        // permissions
        List<PermissionDefinition> permissions = new ArrayList<PermissionDefinition>();
        permissions.add(createPermission(CMIS_READ, "Read"));
        permissions.add(createPermission(CMIS_WRITE, "Write"));
        permissions.add(createPermission(CMIS_ALL, "All"));
        if (cmis11) {
            NewTypeSettableAttributesImpl typeAttrs = new NewTypeSettableAttributesImpl();
            typeAttrs.setCanSetControllableAcl(false);
            typeAttrs.setCanSetControllablePolicy(false);
            typeAttrs.setCanSetCreatable(true);
            typeAttrs.setCanSetDescription(true);
            typeAttrs.setCanSetDisplayName(true);
            typeAttrs.setCanSetFileable(false);
            typeAttrs.setCanSetFulltextIndexed(false);
            typeAttrs.setCanSetId(true);
            typeAttrs.setCanSetIncludedInSupertypeQuery(false);
            typeAttrs.setCanSetLocalName(true);
            typeAttrs.setCanSetLocalNamespace(true);
            typeAttrs.setCanSetQueryable(false);
            typeAttrs.setCanSetQueryName(true);
            caps.setNewTypeSettableAttributes(typeAttrs);
        }
        aclCaps.setPermissionDefinitionData(permissions);

        // mapping
        List<PermissionMapping> list = new ArrayList<PermissionMapping>();
        list.add(createMapping(PermissionMapping.CAN_GET_DESCENDENTS_FOLDER, CMIS_READ));
        list.add(createMapping(PermissionMapping.CAN_GET_CHILDREN_FOLDER, CMIS_READ));
        list.add(createMapping(PermissionMapping.CAN_GET_PARENTS_FOLDER, CMIS_READ));
        list.add(createMapping(PermissionMapping.CAN_GET_FOLDER_PARENT_OBJECT, CMIS_READ));
        list.add(createMapping(PermissionMapping.CAN_CREATE_DOCUMENT_FOLDER, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_CREATE_FOLDER_FOLDER, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_CREATE_RELATIONSHIP_SOURCE, CMIS_READ));
        list.add(createMapping(PermissionMapping.CAN_CREATE_RELATIONSHIP_TARGET, CMIS_READ));
        list.add(createMapping(PermissionMapping.CAN_GET_PROPERTIES_OBJECT, CMIS_READ));
        list.add(createMapping(PermissionMapping.CAN_VIEW_CONTENT_OBJECT, CMIS_READ));
        list.add(createMapping(PermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_MOVE_OBJECT, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_MOVE_TARGET, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_MOVE_SOURCE, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_DELETE_OBJECT, CMIS_WRITE));
        ;
        list.add(createMapping(PermissionMapping.CAN_DELETE_TREE_FOLDER, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_SET_CONTENT_DOCUMENT, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_DELETE_CONTENT_DOCUMENT, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_ADD_TO_FOLDER_OBJECT, CMIS_WRITE));
        ;
        list.add(createMapping(PermissionMapping.CAN_ADD_TO_FOLDER_FOLDER, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_REMOVE_FROM_FOLDER_OBJECT, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_REMOVE_FROM_FOLDER_FOLDER, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_CHECKOUT_DOCUMENT, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_CANCEL_CHECKOUT_DOCUMENT, CMIS_WRITE));
        ;
        list.add(createMapping(PermissionMapping.CAN_CHECKIN_DOCUMENT, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_GET_ALL_VERSIONS_VERSION_SERIES, CMIS_READ));
        list.add(createMapping(PermissionMapping.CAN_GET_OBJECT_RELATIONSHIPS_OBJECT, CMIS_READ));
        list.add(createMapping(PermissionMapping.CAN_ADD_POLICY_OBJECT, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_ADD_POLICY_POLICY, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_REMOVE_POLICY_OBJECT, CMIS_WRITE));
        list.add(createMapping(PermissionMapping.CAN_REMOVE_POLICY_POLICY, CMIS_WRITE));
        ;
        list.add(createMapping(PermissionMapping.CAN_GET_APPLIED_POLICIES_OBJECT, CMIS_READ));
        list.add(createMapping(PermissionMapping.CAN_GET_ACL_OBJECT, CMIS_READ));
        list.add(createMapping(PermissionMapping.CAN_APPLY_ACL_OBJECT, CMIS_ALL));

        Map<String, PermissionMapping> map = new LinkedHashMap<String, PermissionMapping>();
        for (PermissionMapping pm : list) {
            map.put(pm.getKey(), pm);
        }
        
        List<BaseTypeId> changesOnType;
        // CMIS 1.1 extensions
        if (cmis11) {
            repoInfo.setCmisVersionSupported(CmisVersion.CMIS_1_1.value());
            repoInfo.setCmisVersion(CmisVersion.CMIS_1_1);
            changesOnType = new ArrayList<BaseTypeId>() {{
                add(BaseTypeId.CMIS_DOCUMENT);
                add(BaseTypeId.CMIS_FOLDER);
                add(BaseTypeId.CMIS_ITEM);
            }};
            
            Set<PropertyType> propertyTypeSet = new HashSet<PropertyType>() {{
                add(PropertyType.BOOLEAN);
                add(PropertyType.DATETIME);
                add(PropertyType.DECIMAL);
                add(PropertyType.HTML);
                add(PropertyType.ID);
                add(PropertyType.INTEGER);
                add(PropertyType.STRING);
                add(PropertyType.URI);
            }};
            CreatablePropertyTypesImpl creatablePropertyTypes = new CreatablePropertyTypesImpl();
            creatablePropertyTypes.setCanCreate(propertyTypeSet);
            caps.setCreatablePropertyTypes(creatablePropertyTypes);
            caps.setOrderByCapability(CapabilityOrderBy.COMMON);
        } else {
            repoInfo.setCmisVersionSupported(CmisVersion.CMIS_1_0.value());
            repoInfo.setCmisVersion(CmisVersion.CMIS_1_0);
            changesOnType = new ArrayList<BaseTypeId>() {{
                add(BaseTypeId.CMIS_DOCUMENT);
                add(BaseTypeId.CMIS_FOLDER);
            }};
        }
        repoInfo.setChangesOnType(changesOnType);

        aclCaps.setPermissionMappingData(map);

        repoInfo.setAclCapabilities(aclCaps);

        repoInfo.setCapabilities(caps);

        fRepositoryInfo = repoInfo;
        return repoInfo;
    }

    private static PermissionDefinition createPermission(String permission, String description) {
        PermissionDefinitionDataImpl pd = new PermissionDefinitionDataImpl();
        pd.setId(permission);
        pd.setDescription(description);

        return pd;
    }

    private static PermissionMapping createMapping(String key, String permission) {
        PermissionMappingDataImpl pm = new PermissionMappingDataImpl();
        pm.setKey(key);
        pm.setPermissions(Collections.singletonList(permission));

        return pm;
    }

    /**
     * traverse tree and replace each need node with a clone. remove properties
     * on clone if requested, cut children of clone if depth is exceeded.
     * 
     * @param depth
     *            levels of children to copy
     * @param includePropertyDefinitions
     *            indicates with or without property definitions
     * @param tdc
     *            type definition to clone
     * @param parent
     *            parent container where to add clone as child
     * @return cloned type definition
     */
    private static TypeDefinitionContainer cloneTypeList(int depth, boolean includePropertyDefinitions,
            TypeDefinitionContainer tdc, TypeDefinitionContainer parent) {

        AbstractTypeDefinition tdClone = ((AbstractTypeDefinition) tdc.getTypeDefinition()).clone();
        if (!includePropertyDefinitions) {
            tdClone.setPropertyDefinitions(null);
        }

        TypeDefinitionContainerImpl tdcClone = new TypeDefinitionContainerImpl(tdClone);
        if (null != parent)
            parent.getChildren().add(tdcClone);

        if (depth > 0) {
            List<TypeDefinitionContainer> children = tdc.getChildren();
            for (TypeDefinitionContainer child : children) {
                cloneTypeList(depth - 1, includePropertyDefinitions, child, tdcClone);
            }
        }
        return tdcClone;
    }

    public TypeManagerCreatable getTypeManager(String repositoryId) {
        TypeManagerCreatable typeManager = fMapRepositoryToTypeManager.get(repositoryId);
        return typeManager;
    }

    public ObjectList query(String user, String repositoryId, String statement, Boolean searchAllVersions,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount) {
        TypeManager tm = getTypeManager(repositoryId);
        ObjectStore objectStore = getObjectStore(repositoryId);

        InMemoryQueryProcessor queryProcessor = new InMemoryQueryProcessor(getStore(repositoryId));
        ObjectList objList = queryProcessor.query(tm, objectStore, user, repositoryId, statement, searchAllVersions,
                includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount);

        // LOG.debug("Query result, number of matching objects: " +
        // objList.getNumItems());
        return objList;
    }

}
