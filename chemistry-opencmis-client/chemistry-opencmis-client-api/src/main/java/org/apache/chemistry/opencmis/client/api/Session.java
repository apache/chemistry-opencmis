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

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;

/**
 * A session is a connection to a CMIS repository with a specific user.
 * 
 * <p>
 * Not all operations might be supported the connected repository. Either
 * OpenCMIS or the repository will throw an exception if an unsupported
 * operation is called. The capabilities of the repository can be discovered by
 * evaluating the repository info (see {@link #getRepositoryInfo()}).
 * </p>
 * 
 * <p>
 * Almost all methods might throw exceptions derived from
 * {@link CmisBaseException} which is a runtime exception!
 * </p>
 * 
 * <p>
 * (Please refer to the <a
 * href="http://docs.oasis-open.org/cmis/CMIS/v1.0/os/">CMIS specification</a>
 * for details about the domain model, terms, concepts, base types, properties,
 * ids and query names, query language, etc.)
 * </p>
 */
public interface Session {

    /**
     * Clears all cached data. This implies that all data will be reloaded from
     * the repository (depending on the implementation, reloading might be done
     * immediately or be deferred).
     */
    void clear();

    // session context

    /**
     * Returns the underlying binding object.
     */
    CmisBinding getBinding();

    /**
     * Returns the current default operation parameters for filtering, paging
     * and caching.
     */
    OperationContext getDefaultContext();

    /**
     * Sets the current session parameters for filtering, paging and caching.
     * 
     * @param context
     *            the <code>OperationContext</code> to be used for the session;
     *            if <code>null</code>, a default context is used
     */
    void setDefaultContext(OperationContext context);

    /**
     * Creates a new operation context object.
     */
    OperationContext createOperationContext();

    /**
     * Creates a new operation context object with the given properties.
     * 
     * @see OperationContext
     */
    OperationContext createOperationContext(Set<String> filter, boolean includeAcls, boolean includeAllowableActions,
            boolean includePolicies, IncludeRelationships includeRelationships, Set<String> renditionFilter,
            boolean includePathSegments, String orderBy, boolean cacheEnabled, int maxItemsPerPage);

    /**
     * Creates an object id from a String.
     */
    ObjectId createObjectId(String id);

    // localization

    /**
     * Get the current locale to be used for this session.
     */
    Locale getLocale();

    // services

    /**
     * Returns the repository info of the repository associated with this
     * session.
     */
    RepositoryInfo getRepositoryInfo();

    /**
     * Gets a factory object that provides methods to create the objects used by
     * this API.
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
     * Gets the root folder of the repository.
     */
    Folder getRootFolder();

    /**
     * Gets the root folder of the repository with the given
     * {@link OperationContext}.
     */
    Folder getRootFolder(OperationContext context);

    /**
     * Returns all checked out documents.
     * 
     * @see Folder#getCheckedOutDocs()
     */
    ItemIterable<Document> getCheckedOutDocs();

    /**
     * Returns all checked out documents with the given {@link OperationContext}
     * .
     * 
     * @see Folder#getCheckedOutDocs(OperationContext)
     */
    ItemIterable<Document> getCheckedOutDocs(OperationContext context);

    /**
     * Returns a CMIS object from the session cache. If the object is not in the
     * cache or the cache is turned off per default {@link OperationContext}, it
     * will load the object from the repository and puts it into the cache.
     * 
     * @param objectId
     *            the object id
     * 
     * @see #getObject(String)
     */
    CmisObject getObject(ObjectId objectId);

    /**
     * Returns a CMIS object from the session cache. If the object is not in the
     * cache or the given {@link OperationContext} has caching turned off, it
     * will load the object from the repository and puts it into the cache.
     * 
     * @param objectId
     *            the object id
     * @param context
     *            the {@link OperationContext} to use
     * 
     * @see #getObject(String, OperationContext)
     */
    CmisObject getObject(ObjectId objectId, OperationContext context);

    /**
     * Returns a CMIS object from the session cache. If the object is not in the
     * cache or the cache is turned off per default {@link OperationContext}, it
     * will load the object from the repository and puts it into the cache.
     * 
     * @param objectId
     *            the object id
     * 
     * @see #getObject(ObjectId)
     */
    CmisObject getObject(String objectId);

    /**
     * Returns a CMIS object from the session cache. If the object is not in the
     * cache or the given {@link OperationContext} has caching turned off, it
     * will load the object from the repository and puts it into the cache.
     * 
     * @param objectId
     *            the object id
     * @param context
     *            the {@link OperationContext} to use
     * 
     * @see #getObject(ObjectId, OperationContext)
     */
    CmisObject getObject(String objectId, OperationContext context);

    /**
     * Returns a CMIS object from the session cache. If the object is not in the
     * cache or the cache is turned off per default {@link OperationContext}, it
     * will load the object from the repository and puts it into the cache.
     * 
     * @param path
     *            the object path
     */
    CmisObject getObjectByPath(String path);

    /**
     * Returns a CMIS object from the session cache. If the object is not in the
     * cache or the given {@link OperationContext} has caching turned off, it
     * will load the object from the repository and puts it into the cache.
     * 
     * @param path
     *            the object path
     * @param context
     *            the {@link OperationContext} to use
     */
    CmisObject getObjectByPath(String path, OperationContext context);

    // discovery

    /**
     * Sends a query to the repository. (See CMIS spec "2.1.10 Query".)
     * 
     * @param statement
     *            the query statement (CMIS query language)
     * @param searchAllVersions
     *            specifies if the latest and non-latest versions of document
     *            objects should be included
     */
    ItemIterable<QueryResult> query(String statement, boolean searchAllVersions);

    /**
     * Sends a query to the repository using the given {@link OperationContext}.
     * (See CMIS spec "2.1.10 Query".)
     * 
     * @param statement
     *            the query statement (CMIS query language)
     * @param searchAllVersions
     *            specifies if the latest and non-latest versions of document
     *            objects should be included
     * @param context
     *            the OperationContext
     */
    ItemIterable<QueryResult> query(String statement, boolean searchAllVersions, OperationContext context);

    /**
     * Returns the content changes.
     * 
     * @param changeLogToken
     *            the change log token to start from or <code>null</code>
     * @param includeProperties
     *            indicates if changed properties should be included in the
     *            result
     * @param maxNumItems
     *            maximum numbers of events
     */
    ChangeEvents getContentChanges(String changeLogToken, boolean includeProperties, long maxNumItems);

    /**
     * Returns the content changes.
     * 
     * @param changeLogToken
     *            the change log token to start from or <code>null</code>
     * @param includeProperties
     *            indicates if changed properties should be included in the
     *            result
     * @param maxNumItems
     *            maximum numbers of events
     * @param context
     *            the OperationContext
     */
    ChangeEvents getContentChanges(String changeLogToken, boolean includeProperties, long maxNumItems,
            OperationContext context);

    // create

    /**
     * Creates a new document.
     * 
     * @return the object id of the new document
     * 
     * @see Folder#createDocument(Map, ContentStream, VersioningState, List,
     *      List, List, OperationContext)
     */
    ObjectId createDocument(Map<String, ?> properties, ObjectId folderId, ContentStream contentStream,
            VersioningState versioningState, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces);

    /**
     * Creates a new document.
     * 
     * @return the object id of the new document
     * 
     * @see Folder#createDocument(Map, ContentStream, VersioningState, List,
     *      List, List, OperationContext)
     */
    ObjectId createDocument(Map<String, ?> properties, ObjectId folderId, ContentStream contentStream,
            VersioningState versioningState);

    /**
     * Creates a new document from a source document.
     * 
     * @return the object id of the new document
     * 
     * @see Folder#createDocumentFromSource(ObjectId, Map, VersioningState,
     *      List, List, List, OperationContext)
     */
    ObjectId createDocumentFromSource(ObjectId source, Map<String, ?> properties, ObjectId folderId,
            VersioningState versioningState, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces);

    /**
     * Creates a new document from a source document.
     * 
     * @return the object id of the new document
     * 
     * @see Folder#createDocumentFromSource(ObjectId, Map, VersioningState,
     *      List, List, List, OperationContext)
     */
    ObjectId createDocumentFromSource(ObjectId source, Map<String, ?> properties, ObjectId folderId,
            VersioningState versioningState);

    /**
     * Creates a new folder.
     * 
     * @return the object id of the new folder
     * 
     * @see Folder#createFolder(Map, List, List, List, OperationContext)
     */
    ObjectId createFolder(Map<String, ?> properties, ObjectId folderId, List<Policy> policies, List<Ace> addAces,
            List<Ace> removeAces);

    /**
     * Creates a new folder.
     * 
     * @return the object id of the new folder
     * 
     * @see Folder#createFolder(Map, List, List, List, OperationContext)
     */
    ObjectId createFolder(Map<String, ?> properties, ObjectId folderId);

    /**
     * Creates a new policy.
     * 
     * @return the object id of the new policy
     * 
     * @see Folder#createPolicy(Map, List, List, List, OperationContext)
     */
    ObjectId createPolicy(Map<String, ?> properties, ObjectId folderId, List<Policy> policies, List<Ace> addAces,
            List<Ace> removeAces);

    /**
     * Creates a new policy.
     * 
     * @return the object id of the new policy
     * 
     * @see Folder#createPolicy(Map, List, List, List, OperationContext)
     */
    ObjectId createPolicy(Map<String, ?> properties, ObjectId folderId);

    /**
     * Creates a new relationship.
     * 
     * @return the object id of the new relationship
     */
    ObjectId createRelationship(Map<String, ?> properties, List<Policy> policies, List<Ace> addAces,
            List<Ace> removeAces);

    /**
     * Creates a new relationship.
     * 
     * @return the object id of the new relationship
     */
    ObjectId createRelationship(Map<String, ?> properties);

    /**
     * Fetches the relationships from or to an object from the repository.
     */
    ItemIterable<Relationship> getRelationships(ObjectId objectId, boolean includeSubRelationshipTypes,
            RelationshipDirection relationshipDirection, ObjectType type, OperationContext context);

    /**
     * Fetches the ACL of an object from the repository.
     */
    Acl getAcl(ObjectId objectId, boolean onlyBasicPermissions);

    /**
     * Applies an ACL to an object.
     */
    Acl applyAcl(ObjectId objectId, List<Ace> addAces, List<Ace> removeAces, AclPropagation aclPropagation);

    /**
     * Applies a set of policies to an object.
     */
    void applyPolicy(ObjectId objectId, ObjectId... policyIds);

    /**
     * Removes a set of policies from an object.
     */
    void removePolicy(ObjectId objectId, ObjectId... policyIds);
}
