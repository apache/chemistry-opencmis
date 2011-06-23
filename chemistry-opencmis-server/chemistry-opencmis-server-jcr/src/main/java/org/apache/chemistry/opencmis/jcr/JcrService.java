/*
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
package org.apache.chemistry.opencmis.jcr;

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
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;

import javax.jcr.Credentials;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JCR service implementation.
 */
public class JcrService extends AbstractCmisService {
    private final JcrRepository jcrRepository;
    private final Map<String, Session> sessions = new HashMap<String, Session>();

    private CallContext context;

    public JcrService(JcrRepository jcrRepository) {
        this.jcrRepository = jcrRepository;
    }

    @Override
    public void close() {
        for (Session session : sessions.values()) {
            session.logout();
        }
        
        super.close();
    }

    public void setCallContext(CallContext context) {
        this.context = context;
    }

    public CallContext getCallContext() {
        return context;
    }

    //------------------------------------------< repository service >---

    @Override
    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
        return jcrRepository.getRepositoryInfo(login(repositoryId));
    }

    @Override
    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
        return jcrRepository.getRepositoryInfos(login(null));
    }

    @Override
    public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        
        return jcrRepository.getTypeChildren(login(repositoryId), typeId, includePropertyDefinitions, maxItems, skipCount);
    }

    @Override
    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
        return jcrRepository.getTypeDefinition(login(repositoryId), typeId);
    }

    @Override
    public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension) {
        
        return jcrRepository.getTypesDescendants(login(repositoryId), typeId, depth, includePropertyDefinitions);
    }

    //------------------------------------------< navigation service >---

    @Override
    public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {

        return jcrRepository.getChildren(login(repositoryId), folderId, filter, includeAllowableActions,
                includePathSegment, maxItems, skipCount, this, context.isObjectInfoRequired());
    }

    @Override
    public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {

        return jcrRepository.getDescendants(login(repositoryId), folderId, depth, filter, includeAllowableActions,
                includePathSegment, this, context.isObjectInfoRequired(), false);
    }

    @Override
    public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
        return jcrRepository.getFolderParent(login(repositoryId), folderId, filter, this, context.isObjectInfoRequired());
    }

    @Override
    public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {

        return jcrRepository.getDescendants(login(repositoryId), folderId, depth, filter, includeAllowableActions,
                includePathSegment, this, context.isObjectInfoRequired(), true);
    }

    @Override
    public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, ExtensionsData extension) {

        return jcrRepository.getObjectParents(login(repositoryId), objectId, filter, includeAllowableActions,
                includeRelativePathSegment, this, context.isObjectInfoRequired());
    }

    @Override
    public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {

        return jcrRepository.getCheckedOutDocs(login(repositoryId), folderId, filter, orderBy, includeAllowableActions,
                maxItems, skipCount);
    }
    
    //------------------------------------------< object service >---

    @Override
    public String createDocument(String repositoryId, Properties properties, String folderId,
            ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        
        return jcrRepository.createDocument(login(repositoryId), properties, folderId, contentStream, versioningState);
    }

    @Override
    public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties,
            String folderId, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {

        return jcrRepository.createDocumentFromSource(login(repositoryId), sourceId, properties, folderId, versioningState);
    }

    @Override
    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
            Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {

        jcrRepository.setContentStream(login(repositoryId), objectId, overwriteFlag, contentStream);
    }

    @Override
    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ExtensionsData extension) {
        
        jcrRepository.setContentStream(login(repositoryId), objectId, true, null);
    }

    @Override
    public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {

        return jcrRepository.createFolder(login(repositoryId), properties, folderId);
    }

    @Override
    public void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions,
            ExtensionsData extension) {
        
        jcrRepository.deleteObject(login(repositoryId), objectId, allVersions);
    }

    @Override
    public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
            UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {

        return jcrRepository.deleteTree(login(repositoryId), folderId);
    }

    @Override
    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
        return jcrRepository.getAllowableActions(login(repositoryId), objectId);
    }

    @Override
    public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
            BigInteger length, ExtensionsData extension) {
        
        return jcrRepository.getContentStream(login(repositoryId), objectId, offset, length);
    }

    @Override
    public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {

        return jcrRepository.getObject(login(repositoryId), objectId, filter, includeAllowableActions, this,
                context.isObjectInfoRequired());
    }

    @Override
    public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        
        return jcrRepository.getObjectByPath(login(repositoryId), path, filter, includeAllowableActions, includeAcl,
                this, context.isObjectInfoRequired());
    }

    @Override
    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
        return jcrRepository.getProperties(login(repositoryId), objectId, filter, false, this,
                context.isObjectInfoRequired());
    }

    @Override
    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
            ExtensionsData extension) {

        jcrRepository.moveObject(login(repositoryId), objectId, targetFolderId, this, context.isObjectInfoRequired());
    }

    @Override
    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            Properties properties, ExtensionsData extension) {

        jcrRepository.updateProperties(login(repositoryId), objectId, properties, this, context.isObjectInfoRequired());
    }

    //------------------------------------------< versioning service >---

    @Override
    public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension,
            Holder<Boolean> contentCopied) {

        jcrRepository.checkOut(login(repositoryId), objectId, contentCopied);
    }

    @Override
    public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
        jcrRepository.cancelCheckout(login(repositoryId), objectId);
    }

    @Override
    public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties,
            ContentStream contentStream, String checkinComment, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        
        jcrRepository.checkIn(login(repositoryId), objectId, major, properties, contentStream, checkinComment);
    }

    @Override
    public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
            Boolean includeAllowableActions, ExtensionsData extension) {

        return jcrRepository.getAllVersions(login(repositoryId), versionSeriesId == null ? objectId : versionSeriesId, 
                filter, includeAllowableActions, this, context.isObjectInfoRequired());
    }

    @Override
    public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {

        return jcrRepository.getObject(login(repositoryId), versionSeriesId == null ? objectId : versionSeriesId,
                filter, includeAllowableActions, this, context.isObjectInfoRequired());
    }

    @Override
    public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, ExtensionsData extension) {

        ObjectData object = getObjectOfLatestVersion(repositoryId, objectId, versionSeriesId, major, filter, false,
                null, null, false, false, extension);
        
        return object.getProperties();
    }

    // --- discovery service ---

    @Override
    public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {

        return jcrRepository.query(login(repositoryId), statement, searchAllVersions, includeAllowableActions,
                maxItems, skipCount);
    }
    
    //------------------------------------------< protected >---

    protected Session login(String workspaceName) {
        if (context == null) {
            throw new CmisRuntimeException("No user context!");
        }

        Session session = sessions.get(workspaceName);
        if (session == null) {
            String userName = context.getUsername();
            String password = context.getPassword();
            Credentials credentials = userName == null
                ? null
                : new SimpleCredentials(userName, password == null ? "".toCharArray() : password.toCharArray());

            session = jcrRepository.login(credentials, workspaceName);
            sessions.put(workspaceName, session);
        }
        return session;
    }

}
