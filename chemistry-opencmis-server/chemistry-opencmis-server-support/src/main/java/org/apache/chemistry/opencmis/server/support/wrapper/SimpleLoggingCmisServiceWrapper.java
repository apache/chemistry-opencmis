/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License",repositoryId); you may not use this file except in compliance
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
package org.apache.chemistry.opencmis.server.support.wrapper;

import java.math.BigInteger;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
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
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract CMIS service wrapper. All service wrappers managed by
 * {@link CmisServiceWrapperManager} must be derived from this class.
 */
public class SimpleLoggingCmisServiceWrapper extends AbstractCmisServiceWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleLoggingCmisServiceWrapper.class);

    public SimpleLoggingCmisServiceWrapper(CmisService service) {
        super(service);
    }

    /**
     * Logs a call.
     */
    protected void log(String operation, String repositoryId) {
        if (repositoryId == null) {
            repositoryId = "<none>";
        }

        HttpServletRequest request = (HttpServletRequest) getCallContext().get(CallContext.HTTP_SERVLET_REQUEST);
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "<unknown>";
        }

        String binding = getCallContext().getBinding();

        LOG.info("Operation: {}, Repository ID: {}, Binding: {}, User Agent: {}", operation, repositoryId, binding,
                userAgent);
    }

    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
        log("getRepositoryInfos", null);
        return getWrappedService().getRepositoryInfos(extension);
    }

    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
        log("getRepositoryInfo", repositoryId);
        return getWrappedService().getRepositoryInfo(repositoryId, extension);
    }

    public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        log("getTypeChildren", repositoryId);
        return getWrappedService().getTypeChildren(repositoryId, typeId, includePropertyDefinitions, maxItems,
                skipCount, extension);
    }

    public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension) {
        log("getTypeDescendants", repositoryId);
        return getWrappedService().getTypeDescendants(repositoryId, typeId, depth, includePropertyDefinitions,
                extension);
    }

    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
        log("getTypeDefinition", repositoryId);
        return getWrappedService().getTypeDefinition(repositoryId, typeId, extension);
    }

    public TypeDefinition createType(String repositoryId, TypeDefinition type, ExtensionsData extension) {
        log("createType", repositoryId);
        return getWrappedService().createType(repositoryId, type, extension);
    }

    public TypeDefinition updateType(String repositoryId, TypeDefinition type, ExtensionsData extension) {
        log("updateType", repositoryId);
        return getWrappedService().updateType(repositoryId, type, extension);
    }

    public void deleteType(String repositoryId, String typeId, ExtensionsData extension) {
        log("deleteType", repositoryId);
        getWrappedService().deleteType(repositoryId, typeId, extension);
    }

    public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        log("getChildren", repositoryId);
        return getWrappedService().getChildren(repositoryId, folderId, filter, orderBy, includeAllowableActions,
                includeRelationships, renditionFilter, includePathSegment, maxItems, skipCount, extension);
    }

    public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        log("getDescendants", repositoryId);
        return getWrappedService().getDescendants(repositoryId, folderId, depth, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePathSegment, extension);
    }

    public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        log("getFolderTree", repositoryId);
        return getWrappedService().getFolderTree(repositoryId, folderId, depth, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePathSegment, extension);
    }

    public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, ExtensionsData extension) {
        log("getObjectParents", repositoryId);
        return getWrappedService().getObjectParents(repositoryId, objectId, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includeRelativePathSegment, extension);
    }

    public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
        log("getFolderParent", repositoryId);
        return getWrappedService().getFolderParent(repositoryId, folderId, filter, extension);
    }

    public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        log("getCheckedOutDocs", repositoryId);
        return getWrappedService().getCheckedOutDocs(repositoryId, folderId, filter, orderBy, includeAllowableActions,
                includeRelationships, renditionFilter, maxItems, skipCount, extension);
    }

    public String createDocument(String repositoryId, Properties properties, String folderId,
            ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        log("createDocument", repositoryId);
        return getWrappedService().createDocument(repositoryId, properties, folderId, contentStream, versioningState,
                policies, addAces, removeAces, extension);
    }

    public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties,
            String folderId, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        log("createDocumentFromSource", repositoryId);
        return getWrappedService().createDocumentFromSource(repositoryId, sourceId, properties, folderId,
                versioningState, policies, addAces, removeAces, extension);
    }

    public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        log("createFolder", repositoryId);
        return getWrappedService().createFolder(repositoryId, properties, folderId, policies, addAces, removeAces,
                extension);
    }

    public String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        log("createRelationship", repositoryId);
        return getWrappedService().createRelationship(repositoryId, properties, policies, addAces, removeAces,
                extension);
    }

    public String createPolicy(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        log("createPolicy", repositoryId);
        return getWrappedService().createPolicy(repositoryId, properties, folderId, policies, addAces, removeAces,
                extension);
    }

    public String createItem(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        log("createItem", repositoryId);
        return getWrappedService().createItem(repositoryId, properties, folderId, policies, addAces, removeAces,
                extension);
    }

    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
        log("getAllowableActions", repositoryId);
        return getWrappedService().getAllowableActions(repositoryId, objectId, extension);
    }

    public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        log("getObject", repositoryId);
        return getWrappedService().getObject(repositoryId, objectId, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension);
    }

    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
        log("getProperties", repositoryId);
        return getWrappedService().getProperties(repositoryId, objectId, filter, extension);
    }

    public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        log("getRenditions", repositoryId);
        return getWrappedService().getRenditions(repositoryId, objectId, renditionFilter, maxItems, skipCount,
                extension);
    }

    public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        log("getObjectByPath", repositoryId);
        return getWrappedService().getObjectByPath(repositoryId, path, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension);
    }

    public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
            BigInteger length, ExtensionsData extension) {
        log("getContentStream", repositoryId);
        return getWrappedService().getContentStream(repositoryId, objectId, streamId, offset, length, extension);
    }

    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            Properties properties, ExtensionsData extension) {
        log("updateProperties", repositoryId);
        getWrappedService().updateProperties(repositoryId, objectId, changeToken, properties, extension);
    }

    public List<BulkUpdateObjectIdAndChangeToken> bulkUpdateProperties(String repositoryId,
            List<BulkUpdateObjectIdAndChangeToken> objectIdsAndChangeTokens, Properties properties,
            List<String> addSecondaryTypeIds, List<String> removeSecondaryTypeIds, ExtensionsData extension) {
        log("bulkUpdateProperties", repositoryId);
        return getWrappedService().bulkUpdateProperties(repositoryId, objectIdsAndChangeTokens, properties,
                addSecondaryTypeIds, removeSecondaryTypeIds, extension);
    }

    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
            ExtensionsData extension) {
        log("moveObject", repositoryId);
        getWrappedService().moveObject(repositoryId, objectId, targetFolderId, sourceFolderId, extension);
    }

    public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {
        log("deleteObject", repositoryId);
        getWrappedService().deleteObject(repositoryId, objectId, allVersions, extension);
    }

    public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
            UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
        log("deleteTree", repositoryId);
        return getWrappedService().deleteTree(repositoryId, folderId, allVersions, unfileObjects, continueOnFailure,
                extension);
    }

    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
            Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
        log("setContentStream", repositoryId);
        getWrappedService().setContentStream(repositoryId, objectId, overwriteFlag, changeToken, contentStream,
                extension);
    }

    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ExtensionsData extension) {
        log("deleteContentStream", repositoryId);
        getWrappedService().deleteContentStream(repositoryId, objectId, changeToken, extension);
    }

    public void appendContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ContentStream contentStream, boolean isLastChunk, ExtensionsData extension) {
        log("appendContentStream", repositoryId);
        getWrappedService().appendContentStream(repositoryId, objectId, changeToken, contentStream, isLastChunk,
                extension);
    }

    public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension,
            Holder<Boolean> contentCopied) {
        log("checkOut", repositoryId);
        getWrappedService().checkOut(repositoryId, objectId, extension, contentCopied);
    }

    public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
        log("cancelCheckOut", repositoryId);
        getWrappedService().cancelCheckOut(repositoryId, objectId, extension);
    }

    public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties,
            ContentStream contentStream, String checkinComment, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        log("checkIn", repositoryId);
        getWrappedService().checkIn(repositoryId, objectId, major, properties, contentStream, checkinComment, policies,
                addAces, removeAces, extension);
    }

    public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
        log("getObjectOfLatestVersion", repositoryId);
        return getWrappedService()
                .getObjectOfLatestVersion(repositoryId, objectId, versionSeriesId, major, filter,
                        includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeAcl,
                        extension);
    }

    public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, ExtensionsData extension) {
        log("getPropertiesOfLatestVersion", repositoryId);
        return getWrappedService().getPropertiesOfLatestVersion(repositoryId, objectId, versionSeriesId, major, filter,
                extension);
    }

    public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
            Boolean includeAllowableActions, ExtensionsData extension) {
        log("getAllVersions", repositoryId);
        return getWrappedService().getAllVersions(repositoryId, objectId, versionSeriesId, filter,
                includeAllowableActions, extension);
    }

    public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        log("query", repositoryId);
        return getWrappedService().query(repositoryId, statement, searchAllVersions, includeAllowableActions,
                includeRelationships, renditionFilter, maxItems, skipCount, extension);
    }

    public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties,
            String filter, Boolean includePolicyIds, Boolean includeAcl, BigInteger maxItems, ExtensionsData extension) {
        log("getContentChanges", repositoryId);
        return getWrappedService().getContentChanges(repositoryId, changeLogToken, includeProperties, filter,
                includePolicyIds, includeAcl, maxItems, extension);
    }

    public void addObjectToFolder(String repositoryId, String objectId, String folderId, Boolean allVersions,
            ExtensionsData extension) {
        log("addObjectToFolder", repositoryId);
        getWrappedService().addObjectToFolder(repositoryId, objectId, folderId, allVersions, extension);
    }

    public void removeObjectFromFolder(String repositoryId, String objectId, String folderId, ExtensionsData extension) {
        log("removeObjectFromFolder", repositoryId);
        getWrappedService().removeObjectFromFolder(repositoryId, objectId, folderId, extension);
    }

    public ObjectList getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes,
            RelationshipDirection relationshipDirection, String typeId, String filter, Boolean includeAllowableActions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        log("getObjectRelationships", repositoryId);
        return getWrappedService().getObjectRelationships(repositoryId, objectId, includeSubRelationshipTypes,
                relationshipDirection, typeId, filter, includeAllowableActions, maxItems, skipCount, extension);
    }

    public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
        log("getAcl", repositoryId);
        return getWrappedService().getAcl(repositoryId, objectId, onlyBasicPermissions, extension);
    }

    public Acl applyAcl(String repositoryId, String objectId, Acl addAces, Acl removeAces,
            AclPropagation aclPropagation, ExtensionsData extension) {
        log("applyAcl", repositoryId);
        return getWrappedService().applyAcl(repositoryId, objectId, addAces, removeAces, aclPropagation, extension);
    }

    public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        log("applyPolicy", repositoryId);
        getWrappedService().applyPolicy(repositoryId, policyId, objectId, extension);
    }

    public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        log("removePolicy", repositoryId);
        getWrappedService().removePolicy(repositoryId, policyId, objectId, extension);
    }

    public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter,
            ExtensionsData extension) {
        log("getRepositoryInfos", repositoryId);
        return getWrappedService().getAppliedPolicies(repositoryId, objectId, filter, extension);
    }

    public String create(String repositoryId, Properties properties, String folderId, ContentStream contentStream,
            VersioningState versioningState, List<String> policies, ExtensionsData extension) {
        log("create", repositoryId);
        return getWrappedService().create(repositoryId, properties, folderId, contentStream, versioningState, policies,
                extension);
    }

    public void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions,
            ExtensionsData extension) {
        log("deleteObjectOrCancelCheckOut", repositoryId);
        getWrappedService().deleteObjectOrCancelCheckOut(repositoryId, objectId, allVersions, extension);
    }

    public Acl applyAcl(String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation) {
        log("applyAcl", repositoryId);
        return getWrappedService().applyAcl(repositoryId, objectId, aces, aclPropagation);
    }
}
