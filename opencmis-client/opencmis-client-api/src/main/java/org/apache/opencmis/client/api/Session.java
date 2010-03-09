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
package org.apache.opencmis.client.api;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.repository.ObjectFactory;
import org.apache.opencmis.client.api.repository.PropertyFactory;
import org.apache.opencmis.client.api.repository.RepositoryInfo;
import org.apache.opencmis.client.api.util.Container;
import org.apache.opencmis.client.api.util.PagingList;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.provider.CmisProvider;

/**
 * A session is associated with a specific connection to a CMIS repository. A session belongs to one
 * authenticated user and
 */
public interface Session {

  /**
   * Clear all cached data. This implies that all data will be reloaded from the repository
   * (depending on the implementation, reloading might be done immediately or be deferred).
   */
  void clear();

  // session context

  /**
   * Gets the underlying provider object.
   */
  CmisProvider getProvider();

  /**
   * Get the current default operation parameters for filtering and paging.
   */
  OperationContext getDefaultContext();

  /**
   * Set the current session parameters for filtering and paging.
   * 
   * @param context
   *          the <code>OperationContext</code> to be used for the session; if <code>null</code>, a
   *          default context is used
   */
  void setDefaultContext(OperationContext context);

  /**
   * Creates an operation context object.
   */
  OperationContext createOperationContext(Set<String> filter, boolean includeAcls,
      boolean includeAllowableActions, boolean includePolicies,
      IncludeRelationships includeRelationships, Set<String> renditionFilter,
      boolean includePathSegments, String orderBy, boolean cacheEnabled);

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
   * Access to the object services <code>create</code><i>...</i>, plus factory methods to create
   * <code>Acl</code>s, <code>Ace</code>s, and <code>ContentStream</code>.
   */
  ObjectFactory getObjectFactory();

  /**
   * Gets the factory for <code>Property</code> objects.
   */
  PropertyFactory getPropertyFactory();

  // types

  /**
   * Returns the type definition of the given type id.
   */
  ObjectType getTypeDefinition(String typeId);

  /**
   * Returns the type children of the given type id.
   */
  PagingList<ObjectType> getTypeChildren(String typeId, boolean includePropertyDefinitions,
      int itemsPerPage);

  /**
   * Returns the type descendants of the given type id.
   */
  List<Container<ObjectType>> getTypeDescendants(String typeId, int depth,
      boolean includePropertyDefinitions);

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
  PagingList<Document> getCheckedOutDocs(int itemsPerPage);

  PagingList<Document> getCheckedOutDocs(OperationContext context, int itemsPerPage);

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
  PagingList<QueryResult> query(String statement, boolean searchAllVersions, int itemsPerPage);

  PagingList<QueryResult> query(String statement, boolean searchAllVersions,
      OperationContext context, int itemsPerPage);

  /**
   * Discovery service <code>getContentChanges</code>.
   */
  PagingList<ChangeEvent> getContentChanges(String changeLogToken, int itemsPerPage);

  // create

  ObjectId createDocument(List<Property<?>> properties, ObjectId folderId,
      ContentStream contentStream, VersioningState versioningState, List<Policy> policies,
      List<Ace> addAces, List<Ace> removeAces);

  ObjectId createDocumentFromSource(ObjectId source, List<Property<?>> properties,
      ObjectId folderId, VersioningState versioningState, List<Policy> policies, List<Ace> addAces,
      List<Ace> removeAces);

  ObjectId createFolder(List<Property<?>> properties, ObjectId folderId, List<Policy> policies,
      List<Ace> addAces, List<Ace> removeAces);

  ObjectId createPolicy(List<Property<?>> properties, ObjectId folderId, List<Policy> policies,
      List<Ace> addAces, List<Ace> removeAces);

  ObjectId createRelationship(List<Property<?>> properties, List<Policy> policies,
      List<Ace> addAces, List<Ace> removeAces);
}
