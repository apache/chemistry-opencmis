package org.apache.chemistry.opencmis.server.support;

/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

import java.math.BigInteger;
import java.util.List;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service wrapper.
 */
public class CmisServiceWrapper<T extends CmisService> implements CmisService {

    public static final BigInteger MINUS_ONE = BigInteger.valueOf(-1);

    private static final Logger LOG = LoggerFactory.getLogger(CmisServiceWrapper.class);

    private BigInteger defaultTypesMaxItems = null;
    private BigInteger defaultTypesDepth = MINUS_ONE;

    private BigInteger defaultMaxItems = null;
    private BigInteger defaultDepth = MINUS_ONE;

    private final T service;

    /**
     * Constructor.
     */
    public CmisServiceWrapper(T service, BigInteger defaultTypesMaxItems, BigInteger defaultTypesDepth,
            BigInteger defaultMaxItems, BigInteger defaultDepth) {
        if (service == null) {
            throw new IllegalArgumentException("Service must be set!");
        }

        this.service = service;

        setDefaultTypesMaxItems(defaultTypesMaxItems);
        setDefaultTypesDepth(defaultTypesDepth);
        setDefaultMaxItems(defaultMaxItems);
        setDefaultDepth(defaultDepth);
    }

    // --- wrapper operations ---

    /**
     * Set the default maxItems.
     */
    protected void setDefaultTypesMaxItems(BigInteger defaultTypesMaxItems) {
        this.defaultTypesMaxItems = defaultTypesMaxItems;
    }

    /**
     * Set the default depth.
     */
    protected void setDefaultTypesDepth(BigInteger defaultTypesDepth) {
        this.defaultTypesDepth = defaultTypesDepth;
    }

    /**
     * Set the default maxItems.
     */
    protected void setDefaultMaxItems(BigInteger defaultMaxItems) {
        this.defaultMaxItems = defaultMaxItems;
    }

    /**
     * Set the default depth.
     */
    protected void setDefaultDepth(BigInteger defaultDepth) {
        this.defaultDepth = defaultDepth;
    }

    /**
     * Converts the given exception into a CMIS exception.
     */
    protected CmisBaseException createCmisException(Exception e) {
        if (e == null) {
            // should never happen
            // if it happens its the fault of the framework...

            return new CmisRuntimeException("Unknown exception!");
        } else if (e instanceof CmisBaseException) {
            return (CmisBaseException) e;
        } else {
            // should not happen if the connector works correctly
            // it's alarming enough to log the exception
            LOG.warn(e.toString(), e);

            return new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Throws an exception if the given id is <code>null</code> or empty.
     */
    protected void checkId(String name, String id) {
        if (id == null) {
            throw new CmisInvalidArgumentException(name + " must be set!");
        }

        if (id.length() == 0) {
            throw new CmisInvalidArgumentException(name + " must not be empty!");
        }
    }

    /**
     * Throws an exception if the given ids are all <code>null</code> or empty.
     */
    protected void checkIds(String name, String... ids) {
        for (String id : ids) {
            if (id != null && id.length() > 0) {
                return;
            }
        }

        throw new CmisInvalidArgumentException(name + " must be set!");
    }

    /**
     * Throws an exception if the given holder or id is <code>null</code> or
     * empty.
     */
    protected void checkHolderId(String name, Holder<String> holder) {
        if (holder == null) {
            throw new CmisInvalidArgumentException(name + " must be set!");
        }

        checkId(name, holder.getValue());
    }

    /**
     * Throws an exception if the repository id is <code>null</code> or empty.
     */
    protected void checkRepositoryId(String repositoryId) {
        checkId("Repository Id", repositoryId);
    }

    /**
     * Throws an exception if the given path is <code>null</code> or invalid.
     */
    protected void checkPath(String name, String path) {
        if (path == null) {
            throw new CmisInvalidArgumentException(name + " must be set!");
        }

        if (path.length() == 0) {
            throw new CmisInvalidArgumentException(name + " must not be empty!");
        }

        if (path.charAt(0) != '/') {
            throw new CmisInvalidArgumentException(name + " must start with '/'!");
        }
    }

    /**
     * Throws an exception if the given properties set is <code>null</code>.
     */
    protected void checkProperties(Properties properties) {
        if (properties == null) {
            throw new CmisInvalidArgumentException("Properties must be set!");
        }
    }

    /**
     * Throws an exception if the given property isn't set or of the wrong type.
     */
    protected void checkProperty(Properties properties, String propertyId, Class<?> clazz) {
        if (properties.getProperties() == null) {
            throw new CmisInvalidArgumentException("Property " + propertyId + " must be set!");
        }

        PropertyData<?> property = properties.getProperties().get(propertyId);
        if (property == null) {
            throw new CmisInvalidArgumentException("Property " + propertyId + " must be set!");
        }

        Object value = property.getFirstValue();
        if (value == null) {
            throw new CmisInvalidArgumentException("Property " + propertyId + " must have a value!");
        }

        if (!clazz.isAssignableFrom(value.getClass())) {
            throw new CmisInvalidArgumentException("Property " + propertyId + " has the wrong type!");
        }
    }

    /**
     * Throws an exception if the given content object is <code>null</code>.
     */
    protected void checkContentStream(ContentStream content) {
        if (content == null) {
            throw new CmisInvalidArgumentException("Content must be set!");
        }
    }

    /**
     * Throws an exception if the given query statement is <code>null</code> or
     * empty.
     */
    protected void checkQueryStatement(String statement) {
        if (statement == null) {
            throw new CmisInvalidArgumentException("Statement must be set!");
        }

        if (statement.length() == 0) {
            throw new CmisInvalidArgumentException("Statement must not be empty!");
        }
    }

    /**
     * Returns <code>true</code> if <code>value</code> is <code>null</code>.
     */
    protected Boolean getDefaultTrue(Boolean value) {
        if (value == null) {
            return Boolean.TRUE;
        }

        return value;
    }

    /**
     * Returns <code>false</code> if <code>value</code> is <code>null</code>.
     */
    protected Boolean getDefaultFalse(Boolean value) {
        if (value == null) {
            return Boolean.FALSE;
        }

        return value;
    }

    /**
     * Returns the <code>IncludeRelationships.NONE</code> if <code>value</code>
     * is <code>null</code>.
     */
    protected IncludeRelationships getDefault(IncludeRelationships value) {
        if (value == null) {
            return IncludeRelationships.NONE;
        }

        return value;
    }

    /**
     * Returns the <code>VersioningState.MAJOR</code> if <code>value</code> is
     * <code>null</code>.
     */
    protected VersioningState getDefault(VersioningState value) {
        if (value == null) {
            return VersioningState.MAJOR;
        }

        return value;
    }

    /**
     * Returns the <code>UnfileObjects.DELETE</code> if <code>value</code> is
     * <code>null</code>.
     */
    protected UnfileObject getDefault(UnfileObject value) {
        if (value == null) {
            return UnfileObject.DELETE;
        }

        return value;
    }

    /**
     * Returns the <code>AclPropagation.REPOSITORYDETERMINED</code> if
     * <code>value</code> is <code>null</code>.
     */
    protected AclPropagation getDefault(AclPropagation value) {
        if (value == null) {
            return AclPropagation.REPOSITORYDETERMINED;
        }

        return value;
    }

    /**
     * Returns the <code>RelationshipDirection.SOURCE</code> if
     * <code>value</code> is <code>null</code> .
     */
    protected RelationshipDirection getDefault(RelationshipDirection value) {
        if (value == null) {
            return RelationshipDirection.SOURCE;
        }

        return value;
    }

    /**
     * Returns the <code>"cmis:none"</code> if <code>value</code> is
     * <code>null</code>.
     */
    protected String getDefaultRenditionFilter(String value) {
        if ((value == null) || (value.length() == 0)) {
            return "cmis:none";
        }

        return value;
    }

    /**
     * Returns the default maxItems if <code>maxItems</code> ==
     * <code>null</code>, throws an exception if <code>maxItems</code> &lt; 0,
     * returns <code>maxItems</code> otherwise.
     */
    protected BigInteger getTypesMaxItems(BigInteger maxItems) {
        if (maxItems == null) {
            return defaultTypesMaxItems;
        }

        if (maxItems.compareTo(BigInteger.ZERO) == -1) {
            throw new CmisInvalidArgumentException("maxItems must not be negative!");
        }

        return maxItems;
    }

    /**
     * Checks the depth parameter if it complies with CMIS specification and
     * returns the default value if <code>depth</code> is <code>null</code>.
     */
    protected BigInteger getTypesDepth(BigInteger depth) {
        if (depth == null) {
            return defaultTypesDepth;
        }

        if (depth.compareTo(BigInteger.ZERO) == 0) {
            throw new CmisInvalidArgumentException("depth must not be 0!");
        }

        if (depth.compareTo(MINUS_ONE) == -1) {
            throw new CmisInvalidArgumentException("depth must not be <-1!");
        }

        return depth;
    }

    /**
     * Returns the default maxItems if <code>maxItems</code> ==
     * <code>null</code>, throws an exception if <code>maxItems</code> &lt; 0,
     * returns <code>maxItems</code> otherwise.
     */
    protected BigInteger getMaxItems(BigInteger maxItems) {
        if (maxItems == null) {
            return defaultMaxItems;
        }

        if (maxItems.compareTo(BigInteger.ZERO) == -1) {
            throw new CmisInvalidArgumentException("maxItems must not be negative!");
        }

        return maxItems;
    }

    /**
     * Returns 0 if <code>skipCount</code> == <code>null</code>, throws an
     * exception if <code>skipCount</code> &lt; 0, returns
     * <code>skipCount</code> otherwise.
     */
    protected BigInteger getSkipCount(BigInteger skipCount) {
        if (skipCount == null) {
            return BigInteger.ZERO;
        }

        if (skipCount.compareTo(BigInteger.ZERO) == -1) {
            throw new CmisInvalidArgumentException("skipCount must not be negative!");
        }

        return skipCount;
    }

    /**
     * Checks the depth parameter if it complies with CMIS specification and
     * returns the default value if <code>depth</code> is <code>null</code>.
     */
    protected BigInteger getDepth(BigInteger depth) {
        if (depth == null) {
            return defaultDepth;
        }

        if (depth.compareTo(BigInteger.ZERO) == 0) {
            throw new CmisInvalidArgumentException("depth must not be 0!");
        }

        if (depth.compareTo(MINUS_ONE) == -1) {
            throw new CmisInvalidArgumentException("depth must not be <-1!");
        }

        return depth;
    }

    /**
     * Throws an exception if the given value is negative.
     */
    protected void checkNullOrPositive(String name, BigInteger value) {
        if (value == null) {
            return;
        }

        if (value.compareTo(BigInteger.ZERO) == -1) {
            throw new CmisInvalidArgumentException(name + " must not be negative!");
        }
    }

    // --- service operations ---

    public T getWrappedService() {
        return service;
    }

    public ObjectInfo getObjectInfo(String repositoryId, String objectId) {
        return service.getObjectInfo(repositoryId, objectId);
    }

    public void close() {
        service.close();
    }

    // --- repository service ---

    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
        checkRepositoryId(repositoryId);

        try {
            return service.getRepositoryInfo(repositoryId, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
        try {
            return service.getRepositoryInfos(extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        includePropertyDefinitions = getDefaultFalse(includePropertyDefinitions);
        maxItems = getTypesMaxItems(maxItems);
        skipCount = getSkipCount(skipCount);

        try {
            return service.getTypeChildren(repositoryId, typeId, includePropertyDefinitions, maxItems, skipCount,
                    extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Type Id", typeId);

        try {
            return service.getTypeDefinition(repositoryId, typeId, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        includePropertyDefinitions = getDefaultFalse(includePropertyDefinitions);
        depth = getTypesDepth(depth);

        try {
            return service.getTypeDescendants(repositoryId, typeId, depth, includePropertyDefinitions, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    // --- navigation service ---

    public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        includeAllowableActions = getDefaultFalse(includeAllowableActions);
        includeRelationships = getDefault(includeRelationships);
        renditionFilter = getDefaultRenditionFilter(renditionFilter);
        maxItems = getMaxItems(maxItems);
        skipCount = getSkipCount(skipCount);

        try {
            return service.getCheckedOutDocs(repositoryId, folderId, filter, orderBy, includeAllowableActions,
                    includeRelationships, renditionFilter, maxItems, skipCount, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Folder Id", folderId);
        includeAllowableActions = getDefaultFalse(includeAllowableActions);
        includeRelationships = getDefault(includeRelationships);
        renditionFilter = getDefaultRenditionFilter(renditionFilter);
        includePathSegment = getDefaultFalse(includePathSegment);
        maxItems = getMaxItems(maxItems);
        skipCount = getSkipCount(skipCount);

        try {
            return service.getChildren(repositoryId, folderId, filter, orderBy, includeAllowableActions,
                    includeRelationships, renditionFilter, includePathSegment, maxItems, skipCount, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Folder Id", folderId);
        depth = getDepth(depth);
        includeAllowableActions = getDefaultFalse(includeAllowableActions);
        includeRelationships = getDefault(includeRelationships);
        renditionFilter = getDefaultRenditionFilter(renditionFilter);
        includePathSegment = getDefaultFalse(includePathSegment);

        try {
            return service.getDescendants(repositoryId, folderId, depth, filter, includeAllowableActions,
                    includeRelationships, renditionFilter, includePathSegment, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Folder Id", folderId);

        try {
            return service.getFolderParent(repositoryId, folderId, filter, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Folder Id", folderId);
        depth = getDepth(depth);
        includeAllowableActions = getDefaultFalse(includeAllowableActions);
        includeRelationships = getDefault(includeRelationships);
        renditionFilter = getDefaultRenditionFilter(renditionFilter);
        includePathSegment = getDefaultFalse(includePathSegment);

        try {
            return service.getFolderTree(repositoryId, folderId, depth, filter, includeAllowableActions,
                    includeRelationships, renditionFilter, includePathSegment, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);
        includeAllowableActions = getDefaultFalse(includeAllowableActions);
        includeRelationships = getDefault(includeRelationships);
        renditionFilter = getDefaultRenditionFilter(renditionFilter);
        includeRelativePathSegment = getDefaultFalse(includeRelativePathSegment);

        try {
            return service.getObjectParents(repositoryId, objectId, filter, includeAllowableActions,
                    includeRelationships, renditionFilter, includeRelativePathSegment, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    // --- object service ---

    public String create(String repositoryId, Properties properties, String folderId, ContentStream contentStream,
            VersioningState versioningState, List<String> policies, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkProperties(properties);
        checkProperty(properties, PropertyIds.OBJECT_TYPE_ID, String.class);
        versioningState = getDefault(versioningState);

        try {
            return service.create(repositoryId, properties, folderId, contentStream, versioningState, policies,
                    extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public String createDocument(String repositoryId, Properties properties, String folderId,
            ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkProperties(properties);
        checkProperty(properties, PropertyIds.OBJECT_TYPE_ID, String.class);
        versioningState = getDefault(versioningState);

        try {
            return service.createDocument(repositoryId, properties, folderId, contentStream, versioningState, policies,
                    addAces, removeAces, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties,
            String folderId, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Source Id", sourceId);
        versioningState = getDefault(versioningState);

        try {
            return service.createDocumentFromSource(repositoryId, sourceId, properties, folderId, versioningState,
                    policies, addAces, removeAces, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkProperties(properties);
        checkProperty(properties, PropertyIds.OBJECT_TYPE_ID, String.class);
        checkId("Folder Id", folderId);

        try {
            return service.createFolder(repositoryId, properties, folderId, policies, addAces, removeAces, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public String createPolicy(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkProperties(properties);
        checkProperty(properties, PropertyIds.OBJECT_TYPE_ID, String.class);

        try {
            return service.createPolicy(repositoryId, properties, folderId, policies, addAces, removeAces, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkProperties(properties);
        checkProperty(properties, PropertyIds.OBJECT_TYPE_ID, String.class);
        // checkProperty(properties, PropertyIds.SOURCE_ID, String.class);
        // checkProperty(properties, PropertyIds.TARGET_ID, String.class);

        try {
            return service.createRelationship(repositoryId, properties, policies, addAces, removeAces, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkHolderId("Object Id", objectId);

        try {
            service.deleteContentStream(repositoryId, objectId, changeToken, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);
        allVersions = getDefaultTrue(allVersions);

        try {
            service.deleteObjectOrCancelCheckOut(repositoryId, objectId, allVersions, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions,
            ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);
        allVersions = getDefaultTrue(allVersions);

        try {
            service.deleteObjectOrCancelCheckOut(repositoryId, objectId, allVersions, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
            UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Folder Id", folderId);
        allVersions = getDefaultTrue(allVersions);
        unfileObjects = getDefault(unfileObjects);
        continueOnFailure = getDefaultFalse(continueOnFailure);

        try {
            return service.deleteTree(repositoryId, folderId, allVersions, unfileObjects, continueOnFailure, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);

        try {
            return service.getAllowableActions(repositoryId, objectId, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
            BigInteger length, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);
        checkNullOrPositive("Offset", offset);
        checkNullOrPositive("Length", length);

        try {
            return service.getContentStream(repositoryId, objectId, streamId, offset, length, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);
        includeAllowableActions = getDefaultFalse(includeAllowableActions);
        includeRelationships = getDefault(includeRelationships);
        renditionFilter = getDefaultRenditionFilter(renditionFilter);
        includePolicyIds = getDefaultFalse(includePolicyIds);
        includeAcl = getDefaultFalse(includeAcl);

        try {
            return service.getObject(repositoryId, objectId, filter, includeAllowableActions, includeRelationships,
                    renditionFilter, includePolicyIds, includeAcl, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkPath("Path", path);
        includeAllowableActions = getDefaultFalse(includeAllowableActions);
        includeRelationships = getDefault(includeRelationships);
        renditionFilter = getDefaultRenditionFilter(renditionFilter);
        includePolicyIds = getDefaultFalse(includePolicyIds);
        includeAcl = getDefaultFalse(includeAcl);

        try {
            return service.getObjectByPath(repositoryId, path, filter, includeAllowableActions, includeRelationships,
                    renditionFilter, includePolicyIds, includeAcl, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);

        try {
            return service.getProperties(repositoryId, objectId, filter, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);
        renditionFilter = getDefaultRenditionFilter(renditionFilter);
        maxItems = getMaxItems(maxItems);
        skipCount = getSkipCount(skipCount);

        try {
            return service.getRenditions(repositoryId, objectId, renditionFilter, maxItems, skipCount, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
            ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkHolderId("Object Id", objectId);
        checkId("Target Folder Id", targetFolderId);

        try {
            service.moveObject(repositoryId, objectId, targetFolderId, sourceFolderId, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
            Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkHolderId("Object Id", objectId);
        overwriteFlag = getDefaultTrue(overwriteFlag);
        checkContentStream(contentStream);

        try {
            service.setContentStream(repositoryId, objectId, overwriteFlag, changeToken, contentStream, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            Properties properties, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkHolderId("Object Id", objectId);
        checkProperties(properties);

        try {
            service.updateProperties(repositoryId, objectId, changeToken, properties, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    // --- versioning service ---

    public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);

        try {
            service.cancelCheckOut(repositoryId, objectId, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties,
            ContentStream contentStream, String checkinComment, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkHolderId("Object Id", objectId);
        major = getDefaultTrue(major);

        try {
            service.checkIn(repositoryId, objectId, major, properties, contentStream, checkinComment, policies,
                    addAces, removeAces, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension,
            Holder<Boolean> contentCopied) {
        checkRepositoryId(repositoryId);
        checkHolderId("Object Id", objectId);

        try {
            service.checkOut(repositoryId, objectId, extension, contentCopied);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkIds("Version Series Id", objectId, versionSeriesId);
        major = getDefaultFalse(major);
        includeAllowableActions = getDefaultFalse(includeAllowableActions);
        includeRelationships = getDefault(includeRelationships);
        renditionFilter = getDefaultRenditionFilter(renditionFilter);
        includePolicyIds = getDefaultFalse(includePolicyIds);
        includeAcl = getDefaultFalse(includeAcl);

        try {
            return service.getObjectOfLatestVersion(repositoryId, objectId, versionSeriesId, major, filter,
                    includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeAcl,
                    extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkIds("Version Series Id", objectId, versionSeriesId);
        major = getDefaultFalse(major);

        try {
            return service.getPropertiesOfLatestVersion(repositoryId, objectId, versionSeriesId, major, filter,
                    extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
            Boolean includeAllowableActions, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkIds("Version Series Id", objectId, versionSeriesId);
        includeAllowableActions = getDefaultFalse(includeAllowableActions);

        try {
            return service.getAllVersions(repositoryId, objectId, versionSeriesId, filter, includeAllowableActions,
                    extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    // --- discovery service ---

    public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties,
            String filter, Boolean includePolicyIds, Boolean includeAcl, BigInteger maxItems, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        includeProperties = getDefaultFalse(includeProperties);
        includePolicyIds = getDefaultFalse(includePolicyIds);
        includeAcl = getDefaultFalse(includeAcl);
        maxItems = getMaxItems(maxItems);

        try {
            return service.getContentChanges(repositoryId, changeLogToken, includeProperties, filter, includePolicyIds,
                    includeAcl, maxItems, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkQueryStatement(statement);
        searchAllVersions = getDefaultFalse(searchAllVersions);
        includeAllowableActions = getDefaultFalse(includeAllowableActions);
        includeRelationships = getDefault(includeRelationships);
        renditionFilter = getDefaultRenditionFilter(renditionFilter);
        maxItems = getMaxItems(maxItems);
        skipCount = getSkipCount(skipCount);

        try {
            return service.query(repositoryId, statement, searchAllVersions, includeAllowableActions,
                    includeRelationships, renditionFilter, maxItems, skipCount, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    // --- multi filing service ---

    public void addObjectToFolder(String repositoryId, String objectId, String folderId, Boolean allVersions,
            ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);
        checkId("Folder Id", folderId);
        allVersions = getDefaultTrue(allVersions);

        try {
            service.addObjectToFolder(repositoryId, objectId, folderId, allVersions, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public void removeObjectFromFolder(String repositoryId, String objectId, String folderId, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);

        try {
            service.removeObjectFromFolder(repositoryId, objectId, folderId, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    // --- relationship service ---

    public ObjectList getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes,
            RelationshipDirection relationshipDirection, String typeId, String filter, Boolean includeAllowableActions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);
        includeSubRelationshipTypes = getDefaultFalse(includeSubRelationshipTypes);
        relationshipDirection = getDefault(relationshipDirection);
        includeAllowableActions = getDefaultFalse(includeAllowableActions);
        maxItems = getMaxItems(maxItems);
        skipCount = getSkipCount(skipCount);

        try {
            return service.getObjectRelationships(repositoryId, objectId, includeSubRelationshipTypes,
                    relationshipDirection, typeId, filter, includeAllowableActions, maxItems, skipCount, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    // --- ACL service ---

    public Acl applyAcl(String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);
        aclPropagation = getDefault(aclPropagation);

        try {
            return service.applyAcl(repositoryId, objectId, aces, aclPropagation);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public Acl applyAcl(String repositoryId, String objectId, Acl addAces, Acl removeAces,
            AclPropagation aclPropagation, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);
        aclPropagation = getDefault(aclPropagation);

        try {
            return service.applyAcl(repositoryId, objectId, addAces, removeAces, aclPropagation, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);
        onlyBasicPermissions = getDefaultTrue(onlyBasicPermissions);

        try {
            return service.getAcl(repositoryId, objectId, onlyBasicPermissions, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    // --- policy service ---

    public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Policy Id", policyId);
        checkId("Object Id", objectId);

        try {
            service.applyPolicy(repositoryId, policyId, objectId, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter,
            ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Object Id", objectId);

        try {
            return service.getAppliedPolicies(repositoryId, objectId, filter, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }

    public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        checkRepositoryId(repositoryId);
        checkId("Policy Id", policyId);
        checkId("Object Id", objectId);

        try {
            service.removePolicy(repositoryId, policyId, objectId, extension);
        } catch (Exception e) {
            throw createCmisException(e);
        }
    }
}
