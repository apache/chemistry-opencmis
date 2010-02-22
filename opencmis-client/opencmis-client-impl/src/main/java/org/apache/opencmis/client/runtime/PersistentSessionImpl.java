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
package org.apache.opencmis.client.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.opencmis.client.api.ChangeEvent;
import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.Document;
import org.apache.opencmis.client.api.ExtensionHandler;
import org.apache.opencmis.client.api.Folder;
import org.apache.opencmis.client.api.PersistentSession;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.SessionContext;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.repository.ObjectFactory;
import org.apache.opencmis.client.api.repository.PropertyFactory;
import org.apache.opencmis.client.api.repository.RepositoryInfo;
import org.apache.opencmis.client.api.util.PagingList;
import org.apache.opencmis.client.api.util.Testable;
import org.apache.opencmis.client.provider.factory.CmisProviderFactory;
import org.apache.opencmis.client.runtime.cache.Cache;
import org.apache.opencmis.client.runtime.cache.CacheImpl;
import org.apache.opencmis.client.runtime.repository.PropertyFactoryImpl;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.enums.CmisProperties;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.CmisProvider;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.util.repository.ObjectGenerator;

public class PersistentSessionImpl implements PersistentSession, Testable,
		Serializable {

	/*
	 * root folder containing generated test data
	 * (not serializable)
	 */
	private transient Folder testRootFolder = null;

	/*
	 * session parameter
	 * (serializable)
	 */
	private Map<String, String> parameters = null;

	/*
	 * CMIS provider
	 * (serializable)
	 */
	private CmisProvider provider = null;

	/*
	 * Session Locale, determined from session parameter
	 * (serializable)
	 */
	private Locale locale = null;

	/*
	 * If not set explicitly then the repository id is returned by the repository
	 * (serializable)
	 */
	private String repositoryId;

	/*
	 * helper factory
	 * (non serializable)
	 */
	private transient PropertyFactory propertyFactory = new PropertyFactoryImpl();

	/*
	 * Object cache
	 * (serializable)
	 */
	private Cache cache = new CacheImpl();
	
	/**
	 * required for serialization
	 */
	private static final long serialVersionUID = -4287481628831198383L;

	public PersistentSessionImpl(Map<String, String> parameters) {
		this.parameters = parameters;
		this.locale = this.determineLocale(parameters);
		this.repositoryId = this.determineRepositoryId(parameters);
	}

	private String determineRepositoryId(Map<String, String> parameters) {
		String repositoryId = parameters.get(SessionParameter.REPOSITORY_ID);
		// if null then the provider will return a repository id (lazy)
		return repositoryId;
	}
	
	private Locale determineLocale(Map<String, String> parameters) {
		Locale locale = null;
		
		String language = parameters
				.get(SessionParameter.LOCALE_ISO639_LANGUAGE);
		String country = parameters
				.get(SessionParameter.LOCALE_ISO3166_COUNTRY);
		String variant = parameters.get(SessionParameter.LOCALE_VARIANT);

		if (variant != null) {
			// all 3 parameter must not be null and valid 
			locale = new Locale(language, country, variant);
		}else {
			if (country != null) {
				// 2 parameter must not be null and valid 
				locale = new Locale(language, country);
			}else {
				if (language != null) {
					// 1 parameter must not be null and valid 
					locale = new Locale(language);
				} else {
					locale = Locale.getDefault();
				}
			}
		}
		
		return locale;
	}

	public void clear() {
		throw new CmisRuntimeException("not implemented");
	}

	public PagingList<Document> getCheckedOutDocs(Folder folder,
			String orderby, int itemsPerPage) {
		throw new CmisRuntimeException("not implemented");
	}

	public PagingList<ChangeEvent> getContentChanges(String changeLogToken,
			int itemsPerPage) {
		throw new CmisRuntimeException("not implemented");
	}

	public SessionContext getContext() {
		throw new CmisRuntimeException("not implemented");
	}

	public String getExtensionContext() {
		throw new CmisRuntimeException("not implemented");
	}

	public ExtensionHandler getExtensionHandler(String context) {
		throw new CmisRuntimeException("not implemented");
	}

	public Locale getLocale() {
		return this.locale;
	}

	public CmisObject getObject(String objectid) {
		throw new CmisRuntimeException("not implemented");
	}

	public CmisObject getObjectByPath(String path) {
		throw new CmisRuntimeException("not implemented");
	}

	public ObjectFactory getObjectFactory() {
		throw new CmisRuntimeException("not implemented");
	}

	public PropertyFactory getPropertyFactory() {
		return this.propertyFactory;
	}

	public RepositoryInfo getRepositoryInfo() {
		throw new CmisRuntimeException("not implemented");
	}

	public Folder getRootFolder() {
		Folder rootFolder = null;
		
		if (this.cache.containsPath("/")) {
			rootFolder = (Folder) this.cache.getByPath("/");
		} else {
			String rootFolderId = this.getRepositoryInfo().getRootFolderId();
			ObjectData od = this.provider.getObjectService().getObject(this.repositoryId, rootFolderId, "", false, IncludeRelationships.NONE, "", false, false, null);
			rootFolder = new PersistentFolderImpl(this, od);
			this.cache.put(rootFolder);
		}
		
		return rootFolder;
	}

	public PagingList<ObjectType> getTypeChildren(ObjectType t,
			boolean includePropertyDefinitions, int itemsPerPage) {
		throw new CmisRuntimeException("not implemented");
	}

	public ObjectType getTypeDefinition(String typeId) {
		throw new CmisRuntimeException("not implemented");
	}

	public PagingList<ObjectType> getTypeDescendants(ObjectType t, int depth,
			boolean includePropertyDefinitions, int itemsPerPage) {
		throw new CmisRuntimeException("not implemented");
	}

	public PagingList<CmisObject> query(String statement,
			boolean searchAllVersions, int itemsPerPage) {
		throw new CmisRuntimeException("not implemented");
	}

	public SessionContext setContext(SessionContext context) {
		throw new CmisRuntimeException("not implemented");
	}

	public String setExtensionContext(String context) {
		throw new CmisRuntimeException("not implemented");
	}

	public ExtensionHandler setExtensionHandler(String context,
			ExtensionHandler extensionHandler) {
		throw new CmisRuntimeException("not implemented");
	}

	public void generateTestData(Map<String, String> parameter) {
		ObjectGenerator og = new ObjectGenerator(this.provider.getObjectFactory(), this.provider.getNavigationService(), this.provider.getObjectService(), this.repositoryId);
		Folder rootFolder = null;
		String documentTypeId = null;
		String folderTypeId = null;

		// check preconditions (mandatory parameter)
		if (!parameter.containsKey(Testable.DOCUMENT_TYPE_ID_PARAMETER)) {
			throw new CmisRuntimeException(
					"Can't genereate test data! Paramter missing: "
							+ Testable.DOCUMENT_TYPE_ID_PARAMETER);
		} else {
			documentTypeId = parameter.get(Testable.DOCUMENT_TYPE_ID_PARAMETER);
		}
		if (!parameter.containsKey(Testable.FOLDER_TYPE_ID_PARAMETER)) {
			throw new CmisRuntimeException(
					"Can't genereate test data! Paramter missing: "
							+ Testable.FOLDER_TYPE_ID_PARAMETER);
		} else {
			folderTypeId = parameter.get(Testable.FOLDER_TYPE_ID_PARAMETER);
		}

		// optional test root folder:
		if (parameter.containsKey(Testable.ROOT_FOLDER_ID_PARAMETER)) {
			// test root folder
			String testRootId = parameter
					.get(Testable.ROOT_FOLDER_ID_PARAMETER);
			rootFolder = (Folder) this.getObject(testRootId);
		} else {
			// repository root
			rootFolder = this.getRootFolder();
		}

		// create test root folder
		List<Property<?>> properties = new ArrayList<Property<?>>();
		Property<String> nameProperty = this.getPropertyFactory()
				.createCmisProperty(CmisProperties.NAME,
						UUID.randomUUID().toString());
		properties.add(nameProperty);
		this.testRootFolder = rootFolder.createFolder(properties, null, null,
				null);

		og.setContentSizeInKB(10);
		og.setDocumentTypeId(documentTypeId);
		og.setFolderTypeId(folderTypeId);
		og.setNumberOfDocumentsToCreatePerFolder(2);
		og.setDocumentPropertiesToGenerate(null);
		og.setFolderPropertiesToGenerate(null);

		og.createFolderHierachy(2, 2, this.testRootFolder.getId());
	}

	public void cleanUpTestData() {
		if (this.testRootFolder != null) {
			this.testRootFolder.deleteTree(true, UnfileObjects.DELETE, true);
			this.testRootFolder = null;
		}
	}

	/**
	 * Connect session object to the provider. This is the very first call after
	 * a session is created.
	 * <p>
	 * In dependency of the parameter set an {@code AtomPub}, a {@code
	 * WebService} or an {@code InMemory} provider is selected.
	 */
	public void connect() {
		if (this.parameters == null || this.parameters.isEmpty()) {
			throw new CmisRuntimeException("Session parameter not set!");
		}
		// Is the AtomPub URL set?
		boolean isAtomPub = this.parameters
				.containsKey(SessionParameter.ATOMPUB_URL) ? true : false;
		// Are the WebService Prefix or all service URLS are set?
		boolean isWebService = this.parameters
				.containsKey(SessionParameter.WEBSERVICE_URL_PREFIX)
				|| (this.parameters
						.containsKey(SessionParameter.WEBSERVICES_ACL_SERVICE)
						&& this.parameters
								.containsKey(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE)
						&& this.parameters
								.containsKey(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE)
						&& this.parameters
								.containsKey(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE)
						&& this.parameters
								.containsKey(SessionParameter.WEBSERVICES_OBJECT_SERVICE)
						&& this.parameters
								.containsKey(SessionParameter.WEBSERVICES_POLICY_SERVICE)
						&& this.parameters
								.containsKey(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE)
						&& this.parameters
								.containsKey(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE) && this.parameters
						.containsKey(SessionParameter.WEBSERVICES_VERSIONING_SERVICE)) ? true
				: false;

		if (!(isAtomPub ^ isWebService)) {
			// Illegal parameter combination
			throw new CmisRuntimeException("Ambiguous session parameter: "
					+ this.parameters);
		}

		if (isAtomPub) {
			this.provider = this.creaetAtomPubProvider(this.parameters);
		} else if (isWebService) {
			this.provider = this.creaetWebServiceProvider(this.parameters);
		} else {
			// Illegal parameter combination
			throw new CmisRuntimeException("Ambiguous session parameter: "
					+ this.parameters);
		}

	}

	private CmisProvider creaetWebServiceProvider(Map<String, String> parameters) {
		CmisProviderFactory factory = CmisProviderFactory.newInstance();
		CmisProvider provider = factory
				.createCmisWebServicesProvider(parameters);

		return provider;
	}

	private CmisProvider creaetAtomPubProvider(Map<String, String> parameters) {
		CmisProviderFactory factory = CmisProviderFactory.newInstance();
		CmisProvider provider = factory.createCmisAtomPubProvider(parameters);

		return provider;
	}

}
