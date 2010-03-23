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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.client.api.Ace;
import org.apache.opencmis.client.api.ChangeEvent;
import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.ContentStream;
import org.apache.opencmis.client.api.Document;
import org.apache.opencmis.client.api.ExtensionHandler;
import org.apache.opencmis.client.api.Folder;
import org.apache.opencmis.client.api.ObjectId;
import org.apache.opencmis.client.api.OperationContext;
import org.apache.opencmis.client.api.PersistentSession;
import org.apache.opencmis.client.api.Policy;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.QueryResult;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.repository.ObjectFactory;
import org.apache.opencmis.client.api.repository.RepositoryInfo;
import org.apache.opencmis.client.api.util.Container;
import org.apache.opencmis.client.api.util.PagingList;
import org.apache.opencmis.client.runtime.cache.Cache;
import org.apache.opencmis.client.runtime.cache.CacheImpl;
import org.apache.opencmis.client.runtime.repository.PersistentObjectFactoryImpl;
import org.apache.opencmis.client.runtime.repository.RepositoryInfoImpl;
import org.apache.opencmis.client.runtime.util.AbstractPagingList;
import org.apache.opencmis.client.runtime.util.ContainerImpl;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.api.TypeDefinitionList;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.Updatability;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.CmisProvider;
import org.apache.opencmis.commons.provider.DiscoveryService;
import org.apache.opencmis.commons.provider.NavigationService;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.commons.provider.RepositoryService;

/**
 * Persistent model session.
 */
public class PersistentSessionImpl implements PersistentSession, Serializable {

  private static final OperationContext DEFAULT_CONTEXT = new OperationContextImpl(null, false,
      true, false, IncludeRelationships.NONE, null, true, null, true);

  private static final Set<Updatability> CREATE_UPDATABILITY = new HashSet<Updatability>();
  static {
    CREATE_UPDATABILITY.add(Updatability.ONCREATE);
    CREATE_UPDATABILITY.add(Updatability.READWRITE);
  }

  private static Log log = LogFactory.getLog(PersistentSessionImpl.class);

  private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock();

  /*
   * default session context (serializable)
   */
  private OperationContext context = DEFAULT_CONTEXT;

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
   * Object cache (serializable)
   */
  private Cache cache = null;

  /*
   * Lazy loaded repository info. Will be invalid after clear(). Access by getter always.
   * (serializable)
   */
  private RepositoryInfoImpl repositoryInfo;

  /*
   * helper factory (non serializable)
   */
  private final ObjectFactory objectFactory = PersistentObjectFactoryImpl.newInstance(this);

  /**
   * required for serialization
   */
  private static final long serialVersionUID = -4287481628831198383L;

  /**
   * Constructor.
   */
  public PersistentSessionImpl(Map<String, String> parameters) {
    this.parameters = parameters;
    PersistentSessionImpl.log.info("Session Parameters: " + parameters);

    this.locale = this.determineLocale(parameters);
    PersistentSessionImpl.log.info("Session Locale: " + this.locale.toString());

    int cacheSize = this.determineCacheSize(parameters);

    if (cacheSize == -1) {
      this.cache = CacheImpl.newInstance();
    }
    else {
      this.cache = CacheImpl.newInstance(cacheSize);
    }
    PersistentSessionImpl.log.info("Session Cache Size: " + this.cache.getCacheSize());
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

    String language = parameters.get(SessionParameter.LOCALE_ISO639_LANGUAGE);
    String country = parameters.get(SessionParameter.LOCALE_ISO3166_COUNTRY);
    String variant = parameters.get(SessionParameter.LOCALE_VARIANT);

    if (variant != null) {
      // all 3 parameter must not be null and valid
      locale = new Locale(language, country, variant);
    }
    else {
      if (country != null) {
        // 2 parameter must not be null and valid
        locale = new Locale(language, country);
      }
      else {
        if (language != null) {
          // 1 parameter must not be null and valid
          locale = new Locale(language);
        }
        else {
          locale = Locale.getDefault();
        }
      }
    }

    return locale;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#clear()
   */
  public void clear() {
    fLock.writeLock().lock();
    try {
      int cacheSize = this.determineCacheSize(this.parameters);
      if (cacheSize == -1) {
        this.cache = CacheImpl.newInstance();
      }
      else {
        this.cache = CacheImpl.newInstance(cacheSize);
      }
      PersistentSessionImpl.log.info("Session Cache Size: " + this.cache.getCacheSize());

      /*
       * clear provider cache
       */
      getProvider().clearAllCaches();
    }
    finally {
      fLock.writeLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#getObjectFactory()
   */
  public ObjectFactory getObjectFactory() {
    return this.objectFactory;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#getCheckedOutDocs(int)
   */
  public PagingList<Document> getCheckedOutDocs(int itemsPerPage) {
    return getCheckedOutDocs(getDefaultContext(), itemsPerPage);
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.apache.opencmis.client.api.Session#getCheckedOutDocs(org.apache.opencmis.client.api.
   * OperationContext, int)
   */
  public PagingList<Document> getCheckedOutDocs(OperationContext context, final int itemsPerPage) {
    if (itemsPerPage < 1) {
      throw new IllegalArgumentException("itemsPerPage must be > 0!");
    }

    final NavigationService nagivationService = getProvider().getNavigationService();
    final ObjectFactory objectFactory = getObjectFactory();
    final OperationContext ctxt = new OperationContextImpl(context);

    return new AbstractPagingList<Document>() {

      @Override
      protected FetchResult fetchPage(int pageNumber) {
        int skipCount = pageNumber * getMaxItemsPerPage();

        // get all checked out documents
        ObjectList checkedOutDocs = nagivationService.getCheckedOutDocs(getRepositoryId(), null,
            ctxt.getFilterString(), ctxt.getOrderBy(), ctxt.isIncludeAllowableActions(), ctxt
                .getIncludeRelationships(), ctxt.getRenditionFilterString(), BigInteger
                .valueOf(getMaxItemsPerPage()), BigInteger.valueOf(skipCount), null);

        // convert objects
        List<Document> page = new ArrayList<Document>();
        if (checkedOutDocs.getObjects() != null) {
          for (ObjectData objectData : checkedOutDocs.getObjects()) {
            CmisObject doc = objectFactory.convertObject(objectData, ctxt);
            if (!(doc instanceof Document)) {
              // should not happen...
              continue;
            }

            page.add((Document) doc);
          }
        }

        return new FetchResult(page, checkedOutDocs.getNumItems(), checkedOutDocs.hasMoreItems());
      }

      @Override
      public int getMaxItemsPerPage() {
        return itemsPerPage;
      }
    };
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#getContentChanges(java.lang.String, int)
   */
  public PagingList<ChangeEvent> getContentChanges(String changeLogToken, int itemsPerPage) {
    throw new CmisRuntimeException("not implemented");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#getDefaultContext()
   */
  public OperationContext getDefaultContext() {
    fLock.readLock().lock();
    try {
      return this.context;
    }
    finally {
      fLock.readLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.apache.opencmis.client.api.Session#setDefaultContext(org.apache.opencmis.client.api.
   * OperationContext)
   */
  public void setDefaultContext(OperationContext context) {
    fLock.writeLock().lock();
    try {
      this.context = (context == null ? DEFAULT_CONTEXT : context);
    }
    finally {
      fLock.writeLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#createOperationContext(java.util.Set, boolean,
   * boolean, boolean, org.apache.opencmis.commons.enums.IncludeRelationships, java.util.Set,
   * boolean, java.lang.String, boolean)
   */
  public OperationContext createOperationContext(Set<String> filter, boolean includeAcls,
      boolean includeAllowableActions, boolean includePolicies,
      IncludeRelationships includeRelationships, Set<String> renditionFilter,
      boolean includePathSegments, String orderBy, boolean cacheEnabled) {
    return new OperationContextImpl(filter, includeAcls, includeAllowableActions, includePolicies,
        includeRelationships, renditionFilter, includePathSegments, orderBy, cacheEnabled);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#createObjectId(java.lang.String)
   */
  public ObjectId createObjectId(String id) {
    return new ObjectIdImpl(id);
  }

  public Locale getLocale() {
    return this.locale;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#getObject(java.lang.String)
   */
  public CmisObject getObject(ObjectId objectId) {
    return getObject(objectId, getDefaultContext());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#getObject(java.lang.String,
   * org.apache.opencmis.client.api.OperationContext)
   */
  public CmisObject getObject(ObjectId objectId, OperationContext context) {
    if ((objectId == null) || (objectId.getId() == null)) {
      throw new IllegalArgumentException("Object Id must be set!");
    }
    if (context == null) {
      throw new IllegalArgumentException("Operation context must be set!");
    }

    CmisObject result = null;

    // ask the cache first
    if (context.isCacheEnabled()) {
      result = this.cache.getById(objectId.getId(), context.getCacheKey());
      if (result != null) {
        return result;
      }
    }

    // get the object
    ObjectData objectData = this.provider.getObjectService().getObject(getRepositoryId(),
        objectId.getId(), context.getFilterString(), context.isIncludeAllowableActions(),
        context.getIncludeRelationships(), context.getRenditionFilterString(),
        context.isIncludePolicies(), context.isIncludeAcls(), null);

    result = getObjectFactory().convertObject(objectData, context);

    // put into cache
    if (context.isCacheEnabled()) {
      this.cache.put(result, context.getCacheKey());
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#getObjectByPath(java.lang.String)
   */
  public CmisObject getObjectByPath(String path) {
    return getObjectByPath(path, getDefaultContext());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#getObjectByPath(java.lang.String,
   * org.apache.opencmis.client.api.OperationContext)
   */
  public CmisObject getObjectByPath(String path, OperationContext context) {
    if (path == null) {
      throw new IllegalArgumentException("Path must be set!");
    }
    if (context == null) {
      throw new IllegalArgumentException("Operation context must be set!");
    }

    CmisObject result = null;

    // ask the cache first
    if (context.isCacheEnabled()) {
      result = this.cache.getByPath(path, context.getCacheKey());
      if (result != null) {
        return result;
      }
    }

    // get the object
    ObjectData objectData = this.provider.getObjectService().getObjectByPath(getRepositoryId(),
        path, context.getFilterString(), context.isIncludeAllowableActions(),
        context.getIncludeRelationships(), context.getRenditionFilterString(),
        context.isIncludePolicies(), context.isIncludeAcls(), null);

    result = getObjectFactory().convertObject(objectData, context);

    // put into cache
    if (context.isCacheEnabled()) {
      this.cache.putPath(path, result, context.getCacheKey());
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#getRepositoryInfo()
   */
  public RepositoryInfo getRepositoryInfo() {
    fLock.readLock().lock();
    try {
      if (this.repositoryInfo == null) {
        fLock.readLock().unlock();
        fLock.writeLock().lock();
        try {
          // try again
          if (this.repositoryInfo != null) {
            return this.repositoryInfo;
          }

          /* get initial repository id from session parameter */
          String repositoryId = this.determineRepositoryId(this.parameters);
          if (repositoryId == null) {
            throw new IllegalStateException("Repository Id is not set!");
          }

          RepositoryInfoData data = getProvider().getRepositoryService().getRepositoryInfo(
              repositoryId, null);

          this.repositoryInfo = new RepositoryInfoImpl(data);
        }
        finally {
          fLock.writeLock().unlock();
          fLock.readLock().lock();
        }
      }

      return this.repositoryInfo;
    }
    finally {
      fLock.readLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#getRootFolder()
   */
  public Folder getRootFolder() {
    return getRootFolder(getDefaultContext());
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.apache.opencmis.client.api.Session#getRootFolder(org.apache.opencmis.client.api.
   * OperationContext)
   */
  public Folder getRootFolder(OperationContext context) {
    String rootFolderId = getRepositoryInfo().getRootFolderId();

    CmisObject rootFolder = getObject(createObjectId(rootFolderId), context);
    if (!(rootFolder instanceof Folder)) {
      throw new CmisRuntimeException("Root folder object is not a folder!");
    }

    return (Folder) rootFolder;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#getTypeChildren(java.lang.String, boolean, int)
   */
  public PagingList<ObjectType> getTypeChildren(final String typeId,
      final boolean includePropertyDefinitions, final int itemsPerPage) {
    if (itemsPerPage < 1) {
      throw new IllegalArgumentException("itemsPerPage must be > 0!");
    }

    final String repositoryId = getRepositoryId();
    final RepositoryService repositoryService = getProvider().getRepositoryService();

    // set up PagingList object
    return new AbstractPagingList<ObjectType>() {

      @Override
      protected FetchResult fetchPage(int pageNumber) {
        int skipCount = pageNumber * getMaxItemsPerPage();

        // fetch the data
        TypeDefinitionList tdl = repositoryService.getTypeChildren(repositoryId, typeId,
            includePropertyDefinitions, BigInteger.valueOf(getMaxItemsPerPage()), BigInteger
                .valueOf(skipCount), null);

        // convert type definitions
        List<ObjectType> page = new ArrayList<ObjectType>(tdl.getList().size());
        for (TypeDefinition typeDefinition : tdl.getList()) {
          page.add(objectFactory.convertTypeDefinition(typeDefinition));
        }

        return new FetchResult(page, tdl.getNumItems(), tdl.hasMoreItems());
      }

      @Override
      public int getMaxItemsPerPage() {
        return itemsPerPage;
      }
    };
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#getTypeDefinition(java.lang.String)
   */
  public ObjectType getTypeDefinition(String typeId) {
    TypeDefinition typeDefinition = getProvider().getRepositoryService().getTypeDefinition(
        getRepositoryId(), typeId, null);
    return objectFactory.convertTypeDefinition(typeDefinition);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#getTypeDescendants(java.lang.String, int, boolean)
   */
  public List<Container<ObjectType>> getTypeDescendants(String typeId, int depth,
      boolean includePropertyDefinitions) {
    List<TypeDefinitionContainer> descendants = getProvider().getRepositoryService()
        .getTypeDescendants(getRepositoryId(), typeId, BigInteger.valueOf(depth),
            includePropertyDefinitions, null);

    return convertTypeDescendants(descendants);
  }

  /**
   * Converts provider <code>TypeDefinitionContainer</code> to API <code>Container</code>.
   */
  private List<Container<ObjectType>> convertTypeDescendants(
      List<TypeDefinitionContainer> descendantsList) {
    List<Container<ObjectType>> result = new ArrayList<Container<ObjectType>>();

    for (TypeDefinitionContainer container : descendantsList) {
      ObjectType objectType = objectFactory.convertTypeDefinition(container.getTypeDefinition());
      List<Container<ObjectType>> children = convertTypeDescendants(container.getChildren());

      result.add(new ContainerImpl<ObjectType>(objectType, children));
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#query(java.lang.String, boolean, int)
   */
  public PagingList<QueryResult> query(final String statement, final boolean searchAllVersions,
      final int itemsPerPage) {
    return query(statement, searchAllVersions, getDefaultContext(), itemsPerPage);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#query(java.lang.String, boolean,
   * org.apache.opencmis.client.api.OperationContext, int)
   */
  public PagingList<QueryResult> query(final String statement, final boolean searchAllVersions,
      OperationContext context, final int itemsPerPage) {

    final DiscoveryService discoveryService = getProvider().getDiscoveryService();
    final ObjectFactory of = getObjectFactory();
    final OperationContext ctxt = new OperationContextImpl(context);

    // set up PagingList object
    return new AbstractPagingList<QueryResult>() {

      @Override
      protected FetchResult fetchPage(int pageNumber) {
        int skipCount = pageNumber * getMaxItemsPerPage();

        // fetch the data
        ObjectList resultList = discoveryService.query(getRepositoryId(), statement,
            searchAllVersions, ctxt.isIncludeAllowableActions(), ctxt.getIncludeRelationships(),
            ctxt.getRenditionFilterString(), BigInteger.valueOf(getMaxItemsPerPage()), BigInteger
                .valueOf(skipCount), null);

        // convert type definitions
        List<QueryResult> page = new ArrayList<QueryResult>();
        if (resultList.getObjects() != null) {
          for (ObjectData objectData : resultList.getObjects()) {
            if (objectData == null) {
              continue;
            }

            page.add(of.convertQueryResult(objectData));
          }
        }

        return new FetchResult(page, resultList.getNumItems(), resultList.hasMoreItems());
      }

      @Override
      public int getMaxItemsPerPage() {
        return itemsPerPage;
      }
    };
  }

  public String setExtensionContext(String context) {
    throw new CmisRuntimeException("not implemented");
  }

  public ExtensionHandler setExtensionHandler(String context, ExtensionHandler extensionHandler) {
    throw new CmisRuntimeException("not implemented");
  }

  /**
   * Connect session object to the provider. This is the very first call after a session is created.
   * <p>
   * In dependency of the parameter set an {@code AtomPub}, a {@code WebService} or an {@code
   * InMemory} provider is selected.
   */
  public void connect() {
    fLock.writeLock().lock();
    try {
      this.provider = CmisProviderHelper.createProvider(this.parameters);
    }
    finally {
      fLock.writeLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#getProvider()
   */
  public CmisProvider getProvider() {
    fLock.readLock().lock();
    try {
      return this.provider;
    }
    finally {
      fLock.readLock().unlock();
    }
  }

  public Cache getCache() {
    fLock.readLock().lock();
    try {
      return this.cache;
    }
    finally {
      fLock.readLock().unlock();
    }
  }

  /**
   * Returns the repository id.
   */
  public String getRepositoryId() {
    return getRepositoryInfo().getId();
  }

  // creates

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#createDocument(java.util.List,
   * org.apache.opencmis.client.api.ObjectId, org.apache.opencmis.client.api.ContentStream,
   * org.apache.opencmis.commons.enums.VersioningState, java.util.List, java.util.List,
   * java.util.List)
   */
  public ObjectId createDocument(List<Property<?>> properties, ObjectId folderId,
      ContentStream contentStream, VersioningState versioningState, List<Policy> policies,
      List<Ace> addAces, List<Ace> removeAces) {
    if ((folderId != null) && (folderId.getId() == null)) {
      throw new IllegalArgumentException("Folder Id must be set!");
    }

    String newId = getProvider().getObjectService().createDocument(getRepositoryId(),
        objectFactory.convertProperties(properties, CREATE_UPDATABILITY),
        (folderId == null ? null : folderId.getId()),
        objectFactory.convertContentStream(contentStream), versioningState,
        objectFactory.convertPolicies(policies), objectFactory.convertAces(addAces),
        objectFactory.convertAces(removeAces), null);

    if (newId == null) {
      return null;
    }

    return createObjectId(newId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.api.Session#createDocumentFromSource(org.apache.opencmis.client.
   * api.Document, java.util.List, java.lang.String,
   * org.apache.opencmis.commons.enums.VersioningState, java.util.List, java.util.List,
   * java.util.List)
   */
  public ObjectId createDocumentFromSource(ObjectId source, List<Property<?>> properties,
      ObjectId folderId, VersioningState versioningState, List<Policy> policies, List<Ace> addAces,
      List<Ace> removeAces) {
    if ((folderId != null) && (folderId.getId() == null)) {
      throw new IllegalArgumentException("Folder Id must be set!");
    }

    String newId = getProvider().getObjectService().createDocumentFromSource(getRepositoryId(),
        source.getId(), objectFactory.convertProperties(properties, CREATE_UPDATABILITY),
        (folderId == null ? null : folderId.getId()), versioningState,
        objectFactory.convertPolicies(policies), objectFactory.convertAces(addAces),
        objectFactory.convertAces(removeAces), null);

    if (newId == null) {
      return null;
    }

    return createObjectId(newId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#createFolder(java.util.List, java.lang.String,
   * java.util.List, java.util.List, java.util.List)
   */
  public ObjectId createFolder(List<Property<?>> properties, ObjectId folderId,
      List<Policy> policies, List<Ace> addAces, List<Ace> removeAces) {
    if ((folderId != null) && (folderId.getId() == null)) {
      throw new IllegalArgumentException("Folder Id must be set!");
    }

    String newId = getProvider().getObjectService().createFolder(getRepositoryId(),
        objectFactory.convertProperties(properties, CREATE_UPDATABILITY),
        (folderId == null ? null : folderId.getId()), objectFactory.convertPolicies(policies),
        objectFactory.convertAces(addAces), objectFactory.convertAces(removeAces), null);

    if (newId == null) {
      return null;
    }

    return createObjectId(newId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#createPolicy(java.util.List, java.lang.String,
   * java.util.List, java.util.List, java.util.List)
   */
  public ObjectId createPolicy(List<Property<?>> properties, ObjectId folderId,
      List<Policy> policies, List<Ace> addAces, List<Ace> removeAces) {
    if ((folderId != null) && (folderId.getId() == null)) {
      throw new IllegalArgumentException("Folder Id must be set!");
    }

    String newId = getProvider().getObjectService().createPolicy(getRepositoryId(),
        objectFactory.convertProperties(properties, CREATE_UPDATABILITY),
        (folderId == null ? null : folderId.getId()), objectFactory.convertPolicies(policies),
        objectFactory.convertAces(addAces), objectFactory.convertAces(removeAces), null);

    if (newId == null) {
      return null;
    }

    return createObjectId(newId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Session#createRelationship(java.util.List, java.util.List,
   * java.util.List, java.util.List)
   */
  public ObjectId createRelationship(List<Property<?>> properties, List<Policy> policies,
      List<Ace> addAces, List<Ace> removeAces) {
    String newId = getProvider().getObjectService().createRelationship(getRepositoryId(),
        objectFactory.convertProperties(properties, CREATE_UPDATABILITY),
        objectFactory.convertPolicies(policies), objectFactory.convertAces(addAces),
        objectFactory.convertAces(removeAces), null);

    if (newId == null) {
      return null;
    }

    return createObjectId(newId);
  }
}
