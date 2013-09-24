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

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
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
 * {@link CmisBaseException} which is a runtime exception.
 * </p>
 * 
 * <p>
 * (Please refer to the <a
 * href="http://docs.oasis-open.org/cmis/CMIS/v1.0/os/">CMIS specification</a>
 * for details about the domain model, terms, concepts, base types, properties,
 * IDs and query names, query language, etc.)
 * </p>
 */
public interface Session extends Serializable {

    /**
     * Clears all cached data. This implies that all data will be reloaded from
     * the repository (depending on the implementation, reloading might be done
     * immediately or be deferred).
     */
    void clear();

    // session context

    /**
     * Returns the underlying binding object.
     * 
     * @return the binding object, not {@code null}
     */
    CmisBinding getBinding();

    /**
     * Returns the current default operation parameters for filtering, paging
     * and caching.
     * 
     * <p>
     * <em>Please note:</em> The returned object is not thread-safe and should
     * only be modified right after the session has been created and before the
     * session object has been used. In order to change the default context in
     * thread-safe manner, create a new {@link OperationContext} object and use
     * {@link #setDefaultContext(OperationContext)} to apply it.
     * </p>
     * 
     * @return the default operation context, not {@code null}
     */
    OperationContext getDefaultContext();

    /**
     * Sets the current session parameters for filtering, paging and caching.
     * 
     * @param context
     *            the {@code OperationContext} to be used for the session; if
     *            {@code null}, a default context is used
     */
    void setDefaultContext(OperationContext context);

    /**
     * Creates a new operation context object.
     * 
     * @return the newly created operation context object
     */
    OperationContext createOperationContext();

    /**
     * Creates a new operation context object with the given properties.
     * 
     * @return the newly created operation context object
     * 
     * @see OperationContext
     */
    OperationContext createOperationContext(Set<String> filter, boolean includeAcls, boolean includeAllowableActions,
            boolean includePolicies, IncludeRelationships includeRelationships, Set<String> renditionFilter,
            boolean includePathSegments, String orderBy, boolean cacheEnabled, int maxItemsPerPage);

    /**
     * Creates an object ID from a String.
     * 
     * @return the object ID object
     */
    ObjectId createObjectId(String id);

    // localization

    /**
     * Get the current locale to be used for this session.
     * 
     * @return the current locale, may be {@code null}
     */
    Locale getLocale();

    // services

    /**
     * Returns the repository info of the repository associated with this
     * session.
     * 
     * @return the repository info, not {@code null}
     * 
     * @cmis 1.0
     */
    RepositoryInfo getRepositoryInfo();

    /**
     * Gets a factory object that provides methods to create the objects used by
     * this API.
     * 
     * @return the repository info, not {@code null}
     */
    ObjectFactory getObjectFactory();

    // types

    /**
     * Returns the type definition of the given type ID.
     * 
     * @param typeId
     *            the type ID
     * @return the type definition
     * 
     * @throws CmisObjectNotFoundException
     *             if a type with the given type ID doesn't exist
     * 
     * @cmis 1.0
     */
    ObjectType getTypeDefinition(String typeId);

    /**
     * Returns the type children of the given type ID.
     * 
     * @param typeId
     *            the type ID or {@code null} to request the base types
     * @param includePropertyDefinitions
     *            indicates if the property definitions should be included
     * @return the type iterator, not {@code null}
     * 
     * @throws CmisObjectNotFoundException
     *             if a type with the given type ID doesn't exist
     * 
     * @cmis 1.0
     */
    ItemIterable<ObjectType> getTypeChildren(String typeId, boolean includePropertyDefinitions);

    /**
     * Returns the type descendants of the given type ID.
     * 
     * @param typeId
     *            the type ID or {@code null} to request the base types
     * @param includePropertyDefinitions
     *            indicates if the property definitions should be included
     * @param depth
     *            the tree depth, must be >0 or -1 for infinite depth
     * 
     * @throws CmisObjectNotFoundException
     *             if a type with the given type ID doesn't exist
     * 
     * @cmis 1.0
     */
    List<Tree<ObjectType>> getTypeDescendants(String typeId, int depth, boolean includePropertyDefinitions);

    /**
     * Creates a new type.
     * 
     * @param type
     *            the type definition
     * @return the new type definition
     * 
     * @cmis 1.1
     */
    ObjectType createType(TypeDefinition type);

    /**
     * Updates an existing type.
     * 
     * @param type
     *            the type definition updates
     * @return the updated type definition
     * 
     * @cmis 1.1
     */
    ObjectType updateType(TypeDefinition type);

    /**
     * Deletes a type.
     * 
     * @param typeId
     *            the ID of the type to delete
     * 
     * @cmis 1.1
     */
    void deleteType(String typeId);

    // navigation

    /**
     * Gets the root folder of the repository.
     * 
     * @return the root folder object, not {@code null}
     * 
     * @cmis 1.0
     */
    Folder getRootFolder();

    /**
     * Gets the root folder of the repository with the given
     * {@link OperationContext}.
     * 
     * @return the root folder object, not {@code null}
     * 
     * @cmis 1.0
     */
    Folder getRootFolder(OperationContext context);

    /**
     * Returns all checked out documents.
     * 
     * @see Folder#getCheckedOutDocs()
     * 
     * @cmis 1.0
     */
    ItemIterable<Document> getCheckedOutDocs();

    /**
     * Returns all checked out documents with the given {@link OperationContext}
     * .
     * 
     * @see Folder#getCheckedOutDocs(OperationContext)
     * 
     * @cmis 1.0
     */
    ItemIterable<Document> getCheckedOutDocs(OperationContext context);

    /**
     * Returns a CMIS object from the session cache. If the object is not in the
     * cache or the cache is turned off per default {@link OperationContext}, it
     * will load the object from the repository and puts it into the cache.
     * 
     * @param objectId
     *            the object ID
     * 
     * @see #getObject(String)
     * 
     * @cmis 1.0
     */
    CmisObject getObject(ObjectId objectId);

    /**
     * Returns a CMIS object from the session cache. If the object is not in the
     * cache or the given {@link OperationContext} has caching turned off, it
     * will load the object from the repository and puts it into the cache.
     * 
     * @param objectId
     *            the object ID
     * @param context
     *            the {@link OperationContext} to use
     * 
     * @see #getObject(String, OperationContext)
     * 
     * @cmis 1.0
     */
    CmisObject getObject(ObjectId objectId, OperationContext context);

    /**
     * Returns a CMIS object from the session cache. If the object is not in the
     * cache or the cache is turned off per default {@link OperationContext}, it
     * will load the object from the repository and puts it into the cache.
     * 
     * @param objectId
     *            the object ID
     * 
     * @see #getObject(ObjectId)
     * 
     * @cmis 1.0
     */
    CmisObject getObject(String objectId);

    /**
     * Returns a CMIS object from the session cache. If the object is not in the
     * cache or the given {@link OperationContext} has caching turned off, it
     * will load the object from the repository and puts it into the cache.
     * 
     * @param objectId
     *            the object ID
     * @param context
     *            the {@link OperationContext} to use
     * 
     * @see #getObject(ObjectId, OperationContext)
     * 
     * @cmis 1.0
     */
    CmisObject getObject(String objectId, OperationContext context);

    /**
     * Returns a CMIS object from the session cache. If the object is not in the
     * cache or the cache is turned off per default {@link OperationContext}, it
     * will load the object from the repository and puts it into the cache.
     * 
     * @param path
     *            the object path
     * 
     * @cmis 1.0
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
     * 
     * @cmis 1.0
     */
    CmisObject getObjectByPath(String path, OperationContext context);

    /**
     * Removes the given object from the cache.
     * 
     * @param objectId
     *            object ID
     */
    void removeObjectFromCache(ObjectId objectId);

    /**
     * Removes the given object from the cache.
     * 
     * @param objectId
     *            object ID
     */
    void removeObjectFromCache(String objectId);

    // discovery

    /**
     * Sends a query to the repository. (See CMIS spec "2.1.10 Query".)
     * 
     * @param statement
     *            the query statement (CMIS query language)
     * @param searchAllVersions
     *            specifies if the latest and non-latest versions of document
     *            objects should be included
     * 
     * @cmis 1.0
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
     * 
     * @cmis 1.0
     */
    ItemIterable<QueryResult> query(String statement, boolean searchAllVersions, OperationContext context);

    /**
     * 
     * @param type
     *            the ID of the object type
     * @param where
     *            the WHERE part of the query
     * @param searchAllVersions
     *            specifies if the latest and non-latest versions of document
     *            objects should be included
     * @param context
     *            the OperationContext
     * 
     * @cmis 1.0
     */
    ItemIterable<CmisObject> queryObjects(String typeId, String where, boolean searchAllVersions,
            OperationContext context);

    /**
     * Creates a query statement.
     * 
     * @param statement
     *            the query statement with placeholders ('?').
     * 
     * @see QueryStatement
     * 
     * @cmis 1.0
     */
    QueryStatement createQueryStatement(String statement);

    /**
     * Returns the content changes.
     * 
     * @param changeLogToken
     *            the change log token to start from or {@code null}
     * @param includeProperties
     *            indicates if changed properties should be included in the
     *            result
     * @param maxNumItems
     *            maximum numbers of events
     * @return the change events
     * 
     * @cmis 1.0
     */
    ChangeEvents getContentChanges(String changeLogToken, boolean includeProperties, long maxNumItems);

    /**
     * Returns the content changes.
     * 
     * @param changeLogToken
     *            the change log token to start from or {@code null}
     * @param includeProperties
     *            indicates if changed properties should be included in the
     *            result
     * @param maxNumItems
     *            maximum numbers of events
     * @param context
     *            the OperationContext
     * @return the change events
     * 
     * @cmis 1.0
     */
    ChangeEvents getContentChanges(String changeLogToken, boolean includeProperties, long maxNumItems,
            OperationContext context);

    /**
     * Returns an iterator of content changes, starting from the given change
     * log token to the latest entry in the change log.
     * <p>
     * Note: Paging and skipping are not supported.
     * 
     * @param changeLogToken
     *            the change log token to start from or {@code null}
     * @param includeProperties
     *            indicates if changed properties should be included in the
     *            result
     * 
     * @cmis 1.0
     */
    ItemIterable<ChangeEvent> getContentChanges(String changeLogToken, boolean includeProperties);

    /**
     * Returns an iterator of content changes, starting from the given change
     * log token to the latest entry in the change log.
     * <p>
     * Note: Paging and skipping are not supported.
     * 
     * @param changeLogToken
     *            the change log token to start from or {@code null}
     * @param includeProperties
     *            indicates if changed properties should be included in the
     *            result
     * @param context
     *            the OperationContext
     * 
     * @cmis 1.0
     */
    ItemIterable<ChangeEvent> getContentChanges(final String changeLogToken, final boolean includeProperties,
            OperationContext context);

    /**
     * Returns the latest change log token.
     * <p>
     * In contrast to the repository info, this change log token is not cached.
     * This method requests the token from the repository every single time it
     * is called.
     * 
     * @return the latest change log token or {@code null} if the repository
     *         doesn't provide one
     * 
     * @cmis 1.0
     */
    String getLatestChangeLogToken();

    // create

    /**
     * Creates a new document.
     * 
     * The stream in {@code contentStream} is consumed but not closed by this
     * method.
     * 
     * @return the object ID of the new document
     * 
     * @see Folder#createDocument(Map, ContentStream, VersioningState, List,
     *      List, List, OperationContext)
     * 
     * @cmis 1.0
     */
    ObjectId createDocument(Map<String, ?> properties, ObjectId folderId, ContentStream contentStream,
            VersioningState versioningState, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces);

    /**
     * Creates a new document.
     * 
     * The stream in {@code contentStream} is consumed but not closed by this
     * method.
     * 
     * @return the object ID of the new document
     * 
     * @see Folder#createDocument(Map, ContentStream, VersioningState, List,
     *      List, List, OperationContext)
     * 
     * @cmis 1.0
     */
    ObjectId createDocument(Map<String, ?> properties, ObjectId folderId, ContentStream contentStream,
            VersioningState versioningState);

    /**
     * Creates a new document from a source document.
     * 
     * @return the object ID of the new document
     * 
     * @see Folder#createDocumentFromSource(ObjectId, Map, VersioningState,
     *      List, List, List, OperationContext)
     * 
     * @cmis 1.0
     */
    ObjectId createDocumentFromSource(ObjectId source, Map<String, ?> properties, ObjectId folderId,
            VersioningState versioningState, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces);

    /**
     * Creates a new document from a source document.
     * 
     * @return the object ID of the new document
     * 
     * @see Folder#createDocumentFromSource(ObjectId, Map, VersioningState,
     *      List, List, List, OperationContext)
     * 
     * @cmis 1.0
     */
    ObjectId createDocumentFromSource(ObjectId source, Map<String, ?> properties, ObjectId folderId,
            VersioningState versioningState);

    /**
     * Creates a new folder.
     * 
     * @return the object ID of the new folder
     * 
     * @see Folder#createFolder(Map, List, List, List, OperationContext)
     * 
     * @cmis 1.0
     */
    ObjectId createFolder(Map<String, ?> properties, ObjectId folderId, List<Policy> policies, List<Ace> addAces,
            List<Ace> removeAces);

    /**
     * Creates a new folder.
     * 
     * @return the object ID of the new folder
     * 
     * @see Folder#createFolder(Map, List, List, List, OperationContext)
     * 
     * @cmis 1.0
     */
    ObjectId createFolder(Map<String, ?> properties, ObjectId folderId);

    /**
     * Creates a new policy.
     * 
     * @return the object ID of the new policy
     * 
     * @see Folder#createPolicy(Map, List, List, List, OperationContext)
     * 
     * @cmis 1.0
     */
    ObjectId createPolicy(Map<String, ?> properties, ObjectId folderId, List<Policy> policies, List<Ace> addAces,
            List<Ace> removeAces);

    /**
     * Creates a new policy.
     * 
     * @return the object ID of the new policy
     * 
     * @see Folder#createPolicy(Map, List, List, List, OperationContext)
     * 
     * @cmis 1.0
     */
    ObjectId createPolicy(Map<String, ?> properties, ObjectId folderId);

    /**
     * Creates a new item.
     * 
     * @return the object ID of the new policy
     * 
     * @see Folder#createItem(Map, List, List, List, OperationContext)
     * 
     * @cmis 1.1
     */
    ObjectId createItem(Map<String, ?> properties, ObjectId folderId, List<Policy> policies, List<Ace> addAces,
            List<Ace> removeAces);

    /**
     * Creates a new item.
     * 
     * @return the object ID of the new item
     * 
     * @see Folder#createItem(Map, List, List, List, OperationContext)
     * 
     * @cmis 1.1
     */
    ObjectId createItem(Map<String, ?> properties, ObjectId folderId);

    /**
     * Creates a new relationship.
     * 
     * @return the object ID of the new relationship
     * 
     * @cmis 1.0
     */
    ObjectId createRelationship(Map<String, ?> properties, List<Policy> policies, List<Ace> addAces,
            List<Ace> removeAces);

    /**
     * Creates a new relationship.
     * 
     * @return the object ID of the new relationship
     * 
     * @cmis 1.0
     */
    ObjectId createRelationship(Map<String, ?> properties);

    /**
     * Fetches the relationships from or to an object from the repository.
     * 
     * @cmis 1.0
     */
    ItemIterable<Relationship> getRelationships(ObjectId objectId, boolean includeSubRelationshipTypes,
            RelationshipDirection relationshipDirection, ObjectType type, OperationContext context);

    /**
     * Updates multiple objects in one request.
     * 
     * @cmis 1.0
     */
    List<BulkUpdateObjectIdAndChangeToken> bulkUpdateProperties(List<CmisObject> objects, Map<String, ?> properties,
            List<String> addSecondaryTypeIds, List<String> removeSecondaryTypeIds);

    /**
     * Deletes an object and, if it is a document, all versions in the version
     * series.
     * 
     * @param objectId
     *            the ID of the object
     * 
     * @cmis 1.0
     */
    void delete(ObjectId objectId);

    /**
     * Deletes an object.
     * 
     * @param objectId
     *            the ID of the object
     * @param allVersions
     *            if this object is a document this parameter defines if only
     *            this version or all versions should be deleted
     * 
     * @cmis 1.0
     */
    void delete(ObjectId objectId, boolean allVersions);

    /**
     * Retrieves the main content stream of a document
     * 
     * @param docId
     *            the ID of the document
     * @return the content stream or {@code null} if the document has no content
     *         stream
     * 
     * @cmis 1.0
     */
    ContentStream getContentStream(ObjectId docId);

    /**
     * Retrieves the content stream of a document
     * 
     * @param docId
     *            the ID of the document
     * @param streamId
     *            the stream ID
     * @param offset
     *            the offset of the stream or {@code null} to read the stream
     *            from the beginning
     * @param length
     *            the maximum length of the stream or {@code null} to read to
     *            the end of the stream
     * 
     * @return the content stream or {@code null} if the document has no content
     *         stream
     * 
     * @cmis 1.0
     */
    ContentStream getContentStream(ObjectId docId, String streamId, BigInteger offset, BigInteger length);

    /**
     * Fetches the ACL of an object from the repository.
     * 
     * @param objectId
     *            the ID the object
     * @param onlyBasicPermissions
     *            if {@code true} the repository should express the ACL only
     *            with the basic permissions defined in the CMIS specification;
     *            if {@code false} the repository can express the ACL with basic
     *            and repository specific permissions
     * 
     * @return the ACL of the object
     * 
     * @cmis 1.0
     */
    Acl getAcl(ObjectId objectId, boolean onlyBasicPermissions);

    /**
     * Applies ACL changes to an object and potentially dependent objects.
     * 
     * Only direct ACEs can be added and removed.
     * 
     * @param objectId
     *            the ID the object
     * @param addAces
     *            list of ACEs to be added or {@code null} if no ACEs should be
     *            added
     * @param removeAces
     *            list of ACEs to be removed or {@code null} if no ACEs should
     *            be removed
     * @param aclPropagation
     *            value that defines the propagation of the ACE changes;
     *            {@code null} is equal to
     *            {@link AclPropagation#REPOSITORYDETERMINED}
     * 
     * @return the new ACL of the object
     * 
     * @cmis 1.0
     */
    Acl applyAcl(ObjectId objectId, List<Ace> addAces, List<Ace> removeAces, AclPropagation aclPropagation);

    /**
     * Removes the direct ACEs of an object and sets the provided ACEs.
     * 
     * The changes are local to the given object and are not propagated to
     * dependent objects.
     * 
     * @param objectId
     *            the ID the object
     * @param aces
     *            list of ACEs to be set
     * 
     * @return the new ACL of the object
     * 
     * @cmis 1.0
     */
    Acl setAcl(ObjectId objectId, List<Ace> aces);

    /**
     * Applies a set of policies to an object.
     * 
     * This operation is not atomic. If it fails some policies might already be
     * applied.
     * 
     * @param objectId
     *            the ID the object
     * @param policyIds
     *            the IDs of the policies to be applied
     * 
     * @cmis 1.0
     */
    void applyPolicy(ObjectId objectId, ObjectId... policyIds);

    /**
     * Removes a set of policies from an object.
     * 
     * This operation is not atomic. If it fails some policies might already be
     * removed.
     * 
     * @param objectId
     *            the ID the object
     * @param policyIds
     *            the IDs of the policies to be removed
     * 
     * @cmis 1.0
     */
    void removePolicy(ObjectId objectId, ObjectId... policyIds);
}
