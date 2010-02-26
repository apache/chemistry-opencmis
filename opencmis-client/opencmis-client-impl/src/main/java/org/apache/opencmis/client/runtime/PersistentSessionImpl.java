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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.apache.opencmis.client.runtime.repository.PersistentObjectFactoryImpl;
import org.apache.opencmis.client.runtime.repository.PropertyFactoryImpl;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.BindingType;
import org.apache.opencmis.commons.enums.CmisProperties;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.CmisProvider;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.util.repository.ObjectGenerator;

public class PersistentSessionImpl implements PersistentSession, Testable,
		Serializable {

	private static Log log = LogFactory.getLog(PersistentSessionImpl.class);

	/*
	 * default session context (serializable)
	 */
	private SessionContextImpl context = new SessionContextImpl();

	/*
	 * root folder containing generated test data (not serializable)
	 */
	private transient Folder testRootFolder = null;

	/*
	 * session parameter (serializable)
	 */
	private Map<String, String> parameters = null;

	/*
	 * CMIS provider (serializable)
	 */
	private CmisProvider provider = null;

	/*
	 * Session Locale, determined from session parameter (serializable)
	 */
	private Locale locale = null;

	/*
	 * helper factory (non serializable)
	 */
	private transient PropertyFactory propertyFactory = PropertyFactoryImpl
			.newInstance(this);

	/*
	 * Object cache (serializable)
	 */
	private Cache cache = null;

	/*
	 * Lazy loaded repository info. Will be invalid after clear(). Access by
	 * getter always. (serializable)
	 */
	private RepositoryInfoImpl repositoryInfo;

	/*
	 * helper factory (non serializable)
	 */
	private transient ObjectFactory objectFactory = PersistentObjectFactoryImpl
			.newInstance(this);

	/**
	 * required for serialization
	 */
	private static final long serialVersionUID = -4287481628831198383L;

	public PersistentSessionImpl(Map<String, String> parameters) {
		this.parameters = parameters;
		PersistentSessionImpl.log.info("Session Parameters: " + parameters);

		this.locale = this.determineLocale(parameters);
		PersistentSessionImpl.log.info("Session Locale: "
				+ this.locale.toString());

		int cacheSize = this.determineCacheSize(parameters);

		if (cacheSize == -1) {
			this.cache = CacheImpl.newInstance();
		} else {
			this.cache = CacheImpl.newInstance(cacheSize);
		}
		PersistentSessionImpl.log.info("Session Cache Size: "
				+ this.cache.size());
	}

	private int determineCacheSize(Map<String, String> parameters) {
		int size = -1;

		return size;
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
		} else {
			if (country != null) {
				// 2 parameter must not be null and valid
				locale = new Locale(language, country);
			} else {
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
		/*
		 * clear cache
		 */

		int cacheSize = this.determineCacheSize(this.parameters);
		if (cacheSize == -1) {
			this.cache = CacheImpl.newInstance();
		} else {
			this.cache = CacheImpl.newInstance(cacheSize);
		}
		PersistentSessionImpl.log.info("Session Cache Size: "
				+ this.cache.size());

		/*
		 * clear repository info
		 */

		this.repositoryInfo.clear();
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
		return this.context;
	}

	public Locale getLocale() {
		return this.locale;
	}

	public CmisObject getObject(String objectId) {
		CmisObject obj = null;
		if (this.cache.containsId(objectId)) {
			obj = this.cache.get(objectId);
		} else {
			/* query context */
			String filter = this.context.getIncludeProperties();
			boolean includeAllowableActions = this.context
					.getIncludeAllowableActions();
			IncludeRelationships includeRelationships = this.context
					.getIncludeRelationships();
			String renditionFilter = this.context.getIncludeRenditions();
			boolean includePolicyIds = this.context.getIncludePolicies();
			boolean includeAcl = this.context.getIncludeAcls();
			ExtensionsData extension = null;

			/* ask backend */
			String repositoryId = this.getRepositoryId();
			ObjectData od = this.provider.getObjectService().getObject(
					repositoryId, objectId, filter, includeAllowableActions,
					includeRelationships, renditionFilter, includePolicyIds,
					includeAcl, extension);

			/* determine type */
			switch (od.getBaseTypeId()) {
			case CMIS_DOCUMENT:
				obj = new PersistentDocumentImpl(this, od);
				break;
			case CMIS_FOLDER:
				obj = new PersistentFolderImpl(this, od);
				break;
			case CMIS_POLICY:
				obj = new PersistentPolicyImpl(this, od);
			case CMIS_RELATIONSHIP:
				obj = new PersistentRelationshipImpl(this, od);
			default:
				throw new CmisRuntimeException("unsupported type: "
						+ od.getBaseTypeId());
			}

			this.cache.put(obj);
		}
		return obj;
	}

	public CmisObject getObjectByPath(String path) {
		throw new CmisRuntimeException("not implemented");
	}

	public ObjectFactory getObjectFactory() {
		return this.objectFactory;
	}

	public PropertyFactory getPropertyFactory() {
		return this.propertyFactory;
	}

	public RepositoryInfo getRepositoryInfo() {
		if (this.repositoryInfo == null) {
			/* get initial repository id from session parameter */
			String repositoryId = this.determineRepositoryId(this.parameters);
			RepositoryInfoImpl rii = new RepositoryInfoImpl(this, repositoryId);
			this.repositoryInfo = rii;
		}
		return this.repositoryInfo;
	}

	public Folder getRootFolder() {
		Folder rootFolder = null;

		if (this.cache.containsPath("/")) {
			rootFolder = (Folder) this.cache.getByPath("/");
		} else {
			String rootFolderId = this.getRepositoryInfo().getRootFolderId();
			String repositoryId = this.getRepositoryId();
			ObjectData od = this.provider.getObjectService().getObject(
					repositoryId, rootFolderId, null, false,
					IncludeRelationships.NONE, null, false, false, null);
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

	public void setContext(SessionContext context) {
		this.context = (SessionContextImpl) context;
	}

	public String setExtensionContext(String context) {
		throw new CmisRuntimeException("not implemented");
	}

	public ExtensionHandler setExtensionHandler(String context,
			ExtensionHandler extensionHandler) {
		throw new CmisRuntimeException("not implemented");
	}

	public void generateTestData(Map<String, String> parameter) {
		String repositoryId = this.getRepositoryId();
		ObjectGenerator og = new ObjectGenerator(this.provider
				.getObjectFactory(), this.provider.getNavigationService(),
				this.provider.getObjectService(), repositoryId);
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
		Property<String> typeProperty = this
				.getPropertyFactory()
				.createCmisProperty(CmisProperties.OBJECT_TYPE_ID, folderTypeId);
		properties.add(typeProperty);

		this.testRootFolder = rootFolder.createFolder(properties, null, null,
				null);

		og.setContentSizeInKB(10);
		og.setDocumentTypeId(documentTypeId);
		og.setFolderTypeId(folderTypeId);
		og.setNumberOfDocumentsToCreatePerFolder(2);
		og.setDocumentPropertiesToGenerate(new ArrayList<String>());
		og.setFolderPropertiesToGenerate(new ArrayList<String>());

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

		BindingType bt = BindingType.fromValue(this.parameters
				.get(SessionParameter.BINDING_TYPE));

		switch (bt) {
		case ATOMPUB:
			this.provider = this.createAtomPubProvider(this.parameters);
			break;
		case WEBSERVICES:
			this.provider = this.createWebServiceProvider(this.parameters);
			break;
		case UNSPECIFIC:
			this.provider = this.createUnspecificProvider(this.parameters);
			break;
		default:
			throw new CmisRuntimeException("Ambiguous session parameter: "
					+ this.parameters);
		}
	}

	private CmisProvider createUnspecificProvider(Map<String, String> parameters) {
		CmisProviderFactory factory = CmisProviderFactory.newInstance();
		CmisProvider provider = factory.createCmisProvider(parameters);
		return provider;
	}

	private CmisProvider createWebServiceProvider(Map<String, String> parameters) {
		CmisProviderFactory factory = CmisProviderFactory.newInstance();
		CmisProvider provider = factory
				.createCmisWebServicesProvider(parameters);

		return provider;
	}

	private CmisProvider createAtomPubProvider(Map<String, String> parameters) {
		CmisProviderFactory factory = CmisProviderFactory.newInstance();
		CmisProvider provider = factory.createCmisAtomPubProvider(parameters);

		return provider;
	}

	public CmisProvider getProvider() {
		return this.provider;
	}

	public Cache getCache() {
		return this.cache;
	}

	public String getRepositoryId() {
		return this.getRepositoryInfo().getId();
	}

}
