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

import java.util.Locale;

import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.repository.ObjectFactory;
import org.apache.opencmis.client.api.repository.PropertyFactory;
import org.apache.opencmis.client.api.repository.RepositoryInfo;
import org.apache.opencmis.client.api.util.PagingList;

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

	// session context

	/**
	 * Get the current session parameters for filtering and paging.
	 */
	SessionContext getContext();

	/**
	 * Set the current session parameters for filtering and paging.
	 * 
	 * @param context
	 *            the <code>SessionContext</code> to be used for the session; if
	 *            <code>null</code>, a default context is used
	 */
	void setContext(SessionContext context);

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

	/**
	 * Get the factory for <code>Property</code> objects.
	 */
	PropertyFactory getPropertyFactory();

	ObjectType getTypeDefinition(String typeId);

	PagingList<ObjectType> getTypeChildren(ObjectType t,
			boolean includePropertyDefinitions, int itemsPerPage);

	PagingList<ObjectType> getTypeDescendants(ObjectType t, int depth,
			boolean includePropertyDefinitions, int itemsPerPage);

	// navigation

	/**
	 * Get the root folder for the repository.
	 */
	Folder getRootFolder();

	/**
	 * Navigation service <code>getCheckedOutDocs</code>.
	 * 
	 * @param folder
	 * @param orderby
	 * @return @
	 */
	PagingList<Document> getCheckedOutDocs(Folder folder, String orderby,
			int itemsPerPage);

	/**
	 * Object service <code>getObject</code>.
	 * 
	 * @param objectid
	 * @return @
	 */
	CmisObject getObject(String objectid);

	/**
	 * Object service <code>getObjectByPath</code>.
	 * 
	 * @param path
	 * @return @
	 */
	CmisObject getObjectByPath(String path);

	// discovery

	/**
	 * Discovery service <code>query</code>.
	 */
	PagingList<CmisObject> query(String statement, boolean searchAllVersions,
			int itemsPerPage);

	/**
	 * Discovery service <code>getContentChanges</code>.
	 * 
	 * @return
	 */
	PagingList<ChangeEvent> getContentChanges(String changeLogToken,
			int itemsPerPage);

}
