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
package org.apache.chemistry.opencmis.client.runtime;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.chemistry.opencmis.client.api.ChangeEvent;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ExtensionHandler;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectFactory;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.PagingIterable;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.cache.Cache;
import org.apache.chemistry.opencmis.client.runtime.cache.CacheImpl;
import org.apache.chemistry.opencmis.client.runtime.repository.PersistentObjectFactoryImpl;
import org.apache.chemistry.opencmis.client.runtime.util.AbstractPageFetch;
import org.apache.chemistry.opencmis.client.runtime.util.ContainerImpl;
import org.apache.chemistry.opencmis.client.runtime.util.DefaultPagingIterable;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.api.Ace;
import org.apache.chemistry.opencmis.commons.api.CmisBinding;
import org.apache.chemistry.opencmis.commons.api.ContentStream;
import org.apache.chemistry.opencmis.commons.api.DiscoveryService;
import org.apache.chemistry.opencmis.commons.api.NavigationService;
import org.apache.chemistry.opencmis.commons.api.ObjectData;
import org.apache.chemistry.opencmis.commons.api.ObjectList;
import org.apache.chemistry.opencmis.commons.api.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.api.RepositoryService;
import org.apache.chemistry.opencmis.commons.api.TypeDefinition;
import org.apache.chemistry.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.api.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Persistent model session.
 */
public class PersistentSessionImpl implements Session, Serializable {

    private static final OperationContext DEFAULT_CONTEXT = new OperationContextImpl(null, false, true, false,
            IncludeRelationships.NONE, null, true, null, true);

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
    private CmisBinding binding = null;

    /*
     * Session Locale, determined from session parameter (serializable)
     */
    private Locale locale = null;

    /*
     * Object cache (serializable)
     */
    private Cache cache = null;

    /*
     * Lazy loaded repository info. Will be invalid after clear(). Access by
     * getter always. (serializable)
     */
    private RepositoryInfo repositoryInfo;

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
        } else {
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
            } else {
                this.cache = CacheImpl.newInstance(cacheSize);
            }
            PersistentSessionImpl.log.info("Session Cache Size: " + this.cache.getCacheSize());

            /*
             * clear provider cache
             */
            getBinding().clearAllCaches();
        } finally {
            fLock.writeLock().unlock();
        }
    }

    public void save() {
        // nop
    }

    public void cancel() {
        throw new UnsupportedOperationException("cancel");
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
    public PagingIterable<Document> getCheckedOutDocs(int itemsPerPage) {
        return getCheckedOutDocs(getDefaultContext(), itemsPerPage);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.apache.opencmis.client.api.Session#getCheckedOutDocs(org.apache.
     * opencmis.client.api. OperationContext, int)
     */
    public PagingIterable<Document> getCheckedOutDocs(OperationContext context, final int itemsPerPage) {
        if (itemsPerPage < 1) {
            throw new IllegalArgumentException("itemsPerPage must be > 0!");
        }

        final NavigationService navigationService = getBinding().getNavigationService();
        final ObjectFactory objectFactory = getObjectFactory();
        final OperationContext ctxt = new OperationContextImpl(context);

        return new DefaultPagingIterable<Document>(new AbstractPageFetch<Document>() {

            @Override
            protected AbstractPageFetch.PageFetchResult<Document> fetchPage(long skipCount) {

                // get all checked out documents
                ObjectList checkedOutDocs = navigationService.getCheckedOutDocs(getRepositoryId(), null, ctxt
                        .getFilterString(), ctxt.getOrderBy(), ctxt.isIncludeAllowableActions(), ctxt
                        .getIncludeRelationships(), ctxt.getRenditionFilterString(), BigInteger.valueOf(itemsPerPage),
                        BigInteger.valueOf(skipCount), null);

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

                return new AbstractPageFetch.PageFetchResult<Document>(page, checkedOutDocs.getNumItems(),
                        checkedOutDocs.hasMoreItems()) {
                };
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.opencmis.client.api.Session#getContentChanges(java.lang.String
     * , int)
     */
    public PagingIterable<ChangeEvent> getContentChanges(String changeLogToken, int itemsPerPage) {
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
        } finally {
            fLock.readLock().unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.apache.opencmis.client.api.Session#setDefaultContext(org.apache.
     * opencmis.client.api. OperationContext)
     */
    public void setDefaultContext(OperationContext context) {
        fLock.writeLock().lock();
        try {
            this.context = (context == null ? DEFAULT_CONTEXT : context);
        } finally {
            fLock.writeLock().unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.opencmis.client.api.Session#createOperationContext(java.util
     * .Set, boolean, boolean, boolean,
     * org.apache.opencmis.commons.enums.IncludeRelationships, java.util.Set,
     * boolean, java.lang.String, boolean)
     */
    public OperationContext createOperationContext(Set<String> filter, boolean includeAcls,
            boolean includeAllowableActions, boolean includePolicies, IncludeRelationships includeRelationships,
            Set<String> renditionFilter, boolean includePathSegments, String orderBy, boolean cacheEnabled) {
        return new OperationContextImpl(filter, includeAcls, includeAllowableActions, includePolicies,
                includeRelationships, renditionFilter, includePathSegments, orderBy, cacheEnabled);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.opencmis.client.api.Session#createObjectId(java.lang.String)
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
        ObjectData objectData = this.binding.getObjectService().getObject(getRepositoryId(), objectId.getId(),
                context.getFilterString(), context.isIncludeAllowableActions(), context.getIncludeRelationships(),
                context.getRenditionFilterString(), context.isIncludePolicies(), context.isIncludeAcls(), null);

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
     * @see
     * org.apache.opencmis.client.api.Session#getObjectByPath(java.lang.String)
     */
    public CmisObject getObjectByPath(String path) {
        return getObjectByPath(path, getDefaultContext());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.opencmis.client.api.Session#getObjectByPath(java.lang.String,
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
        ObjectData objectData = this.binding.getObjectService().getObjectByPath(getRepositoryId(), path,
                context.getFilterString(), context.isIncludeAllowableActions(), context.getIncludeRelationships(),
                context.getRenditionFilterString(), context.isIncludePolicies(), context.isIncludeAcls(), null);

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
            return this.repositoryInfo;
        } finally {
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
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * @seeorg.apache.opencmis.client.api.Session#getRootFolder(org.apache.opencmis
     * .client.api. OperationContext)
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
     * @see
     * org.apache.opencmis.client.api.Session#getTypeChildren(java.lang.String,
     * boolean, int)
     */
    public PagingIterable<ObjectType> getTypeChildren(final String typeId, final boolean includePropertyDefinitions,
            final int itemsPerPage) {
        if (itemsPerPage < 1) {
            throw new IllegalArgumentException("itemsPerPage must be > 0!");
        }

        final RepositoryService repositoryService = getBinding().getRepositoryService();
        final ObjectFactory objectFactory = this.getObjectFactory();

        return new DefaultPagingIterable<ObjectType>(new AbstractPageFetch<ObjectType>() {

            @Override
            protected AbstractPageFetch.PageFetchResult<ObjectType> fetchPage(long skipCount) {

                // fetch the data
                TypeDefinitionList tdl = repositoryService.getTypeChildren(
                        PersistentSessionImpl.this.getRepositoryId(), typeId, includePropertyDefinitions, BigInteger
                                .valueOf(itemsPerPage), BigInteger.valueOf(skipCount), null);

                // convert type definitions
                List<ObjectType> page = new ArrayList<ObjectType>(tdl.getList().size());
                for (TypeDefinition typeDefinition : tdl.getList()) {
                    page.add(objectFactory.convertTypeDefinition(typeDefinition));
                }

                return new AbstractPageFetch.PageFetchResult<ObjectType>(page, tdl.getNumItems(), tdl.hasMoreItems()) {
                };
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.opencmis.client.api.Session#getTypeDefinition(java.lang.String
     * )
     */
    public ObjectType getTypeDefinition(String typeId) {
        TypeDefinition typeDefinition = getBinding().getRepositoryService().getTypeDefinition(getRepositoryId(),
                typeId, null);
        return objectFactory.convertTypeDefinition(typeDefinition);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.opencmis.client.api.Session#getTypeDescendants(java.lang.String
     * , int, boolean)
     */
    public List<Tree<ObjectType>> getTypeDescendants(String typeId, int depth, boolean includePropertyDefinitions) {
        List<TypeDefinitionContainer> descendants = getBinding().getRepositoryService().getTypeDescendants(
                getRepositoryId(), typeId, BigInteger.valueOf(depth), includePropertyDefinitions, null);

        return convertTypeDescendants(descendants);
    }

    /**
     * Converts provider <code>TypeDefinitionContainer</code> to API
     * <code>Container</code>.
     */
    private List<Tree<ObjectType>> convertTypeDescendants(List<TypeDefinitionContainer> descendantsList) {
        List<Tree<ObjectType>> result = new ArrayList<Tree<ObjectType>>();

        for (TypeDefinitionContainer container : descendantsList) {
            ObjectType objectType = objectFactory.convertTypeDefinition(container.getTypeDefinition());
            List<Tree<ObjectType>> children = convertTypeDescendants(container.getChildren());

            result.add(new ContainerImpl<ObjectType>(objectType, children));
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.opencmis.client.api.Session#query(java.lang.String,
     * boolean, int)
     */
    public PagingIterable<QueryResult> query(final String statement, final boolean searchAllVersions,
            final int itemsPerPage) {
        return query(statement, searchAllVersions, getDefaultContext(), itemsPerPage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.opencmis.client.api.Session#query(java.lang.String,
     * boolean, org.apache.opencmis.client.api.OperationContext, int)
     */
    public PagingIterable<QueryResult> query(final String statement, final boolean searchAllVersions,
            OperationContext context, final int itemsPerPage) {

        if (itemsPerPage < 1) {
            throw new IllegalArgumentException("itemsPerPage must be > 0!");
        }

        final DiscoveryService discoveryService = getBinding().getDiscoveryService();
        final ObjectFactory objectFactory = this.getObjectFactory();
        final OperationContext ctxt = new OperationContextImpl(context);

        return new DefaultPagingIterable<QueryResult>(new AbstractPageFetch<QueryResult>() {

            @Override
            protected AbstractPageFetch.PageFetchResult<QueryResult> fetchPage(long skipCount) {

                // fetch the data
                ObjectList resultList = discoveryService.query(getRepositoryId(), statement, searchAllVersions, ctxt
                        .isIncludeAllowableActions(), ctxt.getIncludeRelationships(), ctxt.getRenditionFilterString(),
                        BigInteger.valueOf(itemsPerPage), BigInteger.valueOf(skipCount), null);

                // convert type definitions
                List<QueryResult> page = new ArrayList<QueryResult>();
                if (resultList.getObjects() != null) {
                    for (ObjectData objectData : resultList.getObjects()) {
                        if (objectData == null) {
                            continue;
                        }

                        page.add(objectFactory.convertQueryResult(objectData));
                    }
                }

                return new AbstractPageFetch.PageFetchResult<QueryResult>(page, resultList.getNumItems(), resultList
                        .hasMoreItems()) {
                };
            }
        });

    }

    public String setExtensionContext(String context) {
        throw new CmisRuntimeException("not implemented");
    }

    public ExtensionHandler setExtensionHandler(String context, ExtensionHandler extensionHandler) {
        throw new CmisRuntimeException("not implemented");
    }

    /**
     * Connect session object to the provider. This is the very first call after
     * a session is created.
     * <p>
     * In dependency of the parameter set an {@code AtomPub}, a {@code
     * WebService} or an {@code InMemory} provider is selected.
     */
    public void connect() {
        fLock.writeLock().lock();
        try {
            this.binding = CmisBindingHelper.createProvider(this.parameters);

            /* get initial repository id from session parameter */
            String repositoryId = this.determineRepositoryId(this.parameters);
            if (repositoryId == null) {
                throw new IllegalStateException("Repository Id is not set!");
            }

            repositoryInfo = getBinding().getRepositoryService().getRepositoryInfo(repositoryId, null);
        } finally {
            fLock.writeLock().unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.opencmis.client.api.Session#getBinding()
     */
    public CmisBinding getBinding() {
        fLock.readLock().lock();
        try {
            return this.binding;
        } finally {
            fLock.readLock().unlock();
        }
    }

    public Cache getCache() {
        fLock.readLock().lock();
        try {
            return this.cache;
        } finally {
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
     * @see org.apache.opencmis.client.api.Session#createDocument(java.util.Map,
     * org.apache.opencmis.client.api.ObjectId,
     * org.apache.opencmis.client.api.ContentStream,
     * org.apache.opencmis.commons.enums.VersioningState, java.util.List,
     * java.util.List, java.util.List)
     */
    public ObjectId createDocument(Map<String, ?> properties, ObjectId folderId, ContentStream contentStream,
            VersioningState versioningState, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces) {
        if ((folderId != null) && (folderId.getId() == null)) {
            throw new IllegalArgumentException("Folder Id must be set!");
        }

        String newId = getBinding().getObjectService().createDocument(getRepositoryId(),
                objectFactory.convertProperties(properties, null, CREATE_UPDATABILITY),
                (folderId == null ? null : folderId.getId()), objectFactory.convertContentStream(contentStream),
                versioningState, objectFactory.convertPolicies(policies), objectFactory.convertAces(addAces),
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
     * org.apache.opencmis.client.api.Session#createDocumentFromSource(org.apache
     * .opencmis.client. api.ObjectId, java.util.Map,
     * org.apache.opencmis.client.api.ObjectId,
     * org.apache.opencmis.commons.enums.VersioningState, java.util.List,
     * java.util.List, java.util.List)
     */
    public ObjectId createDocumentFromSource(ObjectId source, Map<String, ?> properties, ObjectId folderId,
            VersioningState versioningState, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces) {
        if ((folderId != null) && (folderId.getId() == null)) {
            throw new IllegalArgumentException("Folder Id must be set!");
        }

        // get the type of the source document
        ObjectType type = null;
        if (source instanceof CmisObject) {
            type = ((CmisObject) source).getBaseType();
        } else {
            CmisObject sourceObj = getObject(source);
            type = sourceObj.getType();
        }

        if (type.getBaseTypeId() != BaseTypeId.CMIS_DOCUMENT) {
            throw new IllegalArgumentException("Source object must be a document!");
        }

        String newId = getBinding().getObjectService().createDocumentFromSource(getRepositoryId(), source.getId(),
                objectFactory.convertProperties(properties, type, CREATE_UPDATABILITY),
                (folderId == null ? null : folderId.getId()), versioningState, objectFactory.convertPolicies(policies),
                objectFactory.convertAces(addAces), objectFactory.convertAces(removeAces), null);

        if (newId == null) {
            return null;
        }

        return createObjectId(newId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.opencmis.client.api.Session#createFolder(java.util.Map,
     * org.apache.opencmis.client.api.ObjectId, java.util.List, java.util.List,
     * java.util.List)
     */
    public ObjectId createFolder(Map<String, ?> properties, ObjectId folderId, List<Policy> policies,
            List<Ace> addAces, List<Ace> removeAces) {
        if ((folderId != null) && (folderId.getId() == null)) {
            throw new IllegalArgumentException("Folder Id must be set!");
        }

        String newId = getBinding().getObjectService().createFolder(getRepositoryId(),
                objectFactory.convertProperties(properties, null, CREATE_UPDATABILITY),
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
     * @see org.apache.opencmis.client.api.Session#createPolicy(java.util.Map,
     * org.apache.opencmis.client.api.ObjectId, java.util.List, java.util.List,
     * java.util.List)
     */
    public ObjectId createPolicy(Map<String, ?> properties, ObjectId folderId, List<Policy> policies,
            List<Ace> addAces, List<Ace> removeAces) {
        if ((folderId != null) && (folderId.getId() == null)) {
            throw new IllegalArgumentException("Folder Id must be set!");
        }

        String newId = getBinding().getObjectService().createPolicy(getRepositoryId(),
                objectFactory.convertProperties(properties, null, CREATE_UPDATABILITY),
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
     * @see
     * org.apache.opencmis.client.api.Session#createRelationship(java.util.Map,
     * java.util.List, java.util.List, java.util.List)
     */
    public ObjectId createRelationship(Map<String, ?> properties, List<Policy> policies, List<Ace> addAces,
            List<Ace> removeAces) {
        String newId = getBinding().getObjectService().createRelationship(getRepositoryId(),
                objectFactory.convertProperties(properties, null, CREATE_UPDATABILITY),
                objectFactory.convertPolicies(policies), objectFactory.convertAces(addAces),
                objectFactory.convertAces(removeAces), null);

        if (newId == null) {
            return null;
        }

        return createObjectId(newId);
    }
}
