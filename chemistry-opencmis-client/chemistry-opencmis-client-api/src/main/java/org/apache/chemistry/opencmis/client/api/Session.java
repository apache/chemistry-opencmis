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
package org.apache.chemistry.opencmis.client.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.api.Ace;
import org.apache.chemistry.opencmis.commons.api.CmisBinding;
import org.apache.chemistry.opencmis.commons.api.ContentStream;
import org.apache.chemistry.opencmis.commons.api.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

/**
 * A session is associated with a specific connection to a CMIS repository. A
 * session belongs to one authenticated user and
 */
public interface Session {

    /**
     * Clear all cached data. This implies that all data will be reloaded from
     * the repository (depending on the implementation, reloading might be done
     * immediately or be deferred).
     */
    void clear();

    /**
     * Save all pending actions for this session. Corresponds to a
     * <code>commit</code> if the CMIS provider supports transactions. If
     * transactions are not supported by the CMIS provider, changes might be
     * applied only partially.
     */
    void save();

    /**
     * Cancel all pending actions for this session. Corresponds to a
     * <code>rollback</code> if the CMIS provider supports transactions. If
     * transactions are not supported by the CMIS provider, some changes might
     * already be applied and therefore not rolled back.
     */
    void cancel();

    // session context

    /**
     * Gets the underlying binding object.
     */
    CmisBinding getBinding();

    /**
     * Get the current default operation parameters for filtering and paging.
     */
    OperationContext getDefaultContext();

    /**
     * Set the current session parameters for filtering and paging.
     * 
     * @param context
     *            the <code>OperationContext</code> to be used for the session;
     *            if <code>null</code>, a default context is used
     */
    void setDefaultContext(OperationContext context);

    /**
     * Creates a default operation context object.
     */
    OperationContext createOperationContext();
    
    /**
     * Creates an operation context object.
     */
    OperationContext createOperationContext(Set<String> filter, boolean includeAcls, boolean includeAllowableActions,
            boolean includePolicies, IncludeRelationships includeRelationships, Set<String> renditionFilter,
            boolean includePathSegments, String orderBy, boolean cacheEnabled, int maxItemsPerPage);

    /**
     * Creates an object id.
     */
    ObjectId createObjectId(String id);

    // localization

    /**
     * Get the current locale to be used for this session.
     */
    Locale getLocale();

    // services

    /**
     * Repository service <code>getRepositoryInfo()</code>.
     */
    RepositoryInfo getRepositoryInfo();

    /**
     * Access to the object services <code>create</code><i>...</i>, plus factory
     * methods to create <code>Acl</code>s, <code>Ace</code>s, and
     * <code>ContentStream</code>.
     */
    ObjectFactory getObjectFactory();

    // types

    /**
     * Returns the type definition of the given type id.
     */
    ObjectType getTypeDefinition(String typeId);

    /**
     * Returns the type children of the given type id.
     */
    ItemIterable<ObjectType> getTypeChildren(String typeId, boolean includePropertyDefinitions);

    /**
     * Returns the type descendants of the given type id.
     */
    List<Tree<ObjectType>> getTypeDescendants(String typeId, int depth, boolean includePropertyDefinitions);

    // navigation

    /**
     * Gets the root folder for the repository.
     */
    Folder getRootFolder();

    Folder getRootFolder(OperationContext context);

    /**
     * Returns all checked out documents.
     * 
     * @see Folder#getCheckedOutDocs(int)
     */
    ItemIterable<Document> getCheckedOutDocs();

    ItemIterable<Document> getCheckedOutDocs(OperationContext context);

    /**
     * Object service <code>getObject</code>.
     */
    CmisObject getObject(ObjectId objectId);

    CmisObject getObject(ObjectId objectId, OperationContext context);

    /**
     * Object service <code>getObjectByPath</code>.
     */
    CmisObject getObjectByPath(String path);

    CmisObject getObjectByPath(String path, OperationContext context);

    // discovery

    /**
     * Discovery service <code>query</code>.
     */
    ItemIterable<QueryResult> query(String statement, boolean searchAllVersions);

    ItemIterable<QueryResult> query(String statement, boolean searchAllVersions, OperationContext context);

    /**
     * Discovery service <code>getContentChanges</code>.
     */
    ItemIterable<ChangeEvent> getContentChanges(String changeLogToken);

    // create

    ObjectId createDocument(Map<String, ?> properties, ObjectId folderId, ContentStream contentStream,
            VersioningState versioningState, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces);

    ObjectId createDocumentFromSource(ObjectId source, Map<String, ?> properties, ObjectId folderId,
            VersioningState versioningState, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces);

    ObjectId createFolder(Map<String, ?> properties, ObjectId folderId, List<Policy> policies, List<Ace> addAces,
            List<Ace> removeAces);

    ObjectId createPolicy(Map<String, ?> properties, ObjectId folderId, List<Policy> policies, List<Ace> addAces,
            List<Ace> removeAces);

    ObjectId createRelationship(Map<String, ?> properties, List<Policy> policies, List<Ace> addAces,
            List<Ace> removeAces);
}
