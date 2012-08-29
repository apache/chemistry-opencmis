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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.chemistry.opencmis.client.api.ChangeEvents;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ExtensionHandler;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectFactory;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.QueryStatement;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.cache.Cache;
import org.apache.chemistry.opencmis.client.runtime.cache.CacheImpl;
import org.apache.chemistry.opencmis.client.runtime.repository.ObjectFactoryImpl;
import org.apache.chemistry.opencmis.client.runtime.util.AbstractPageFetcher;
import org.apache.chemistry.opencmis.client.runtime.util.CollectionIterable;
import org.apache.chemistry.opencmis.client.runtime.util.TreeImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.spi.AclService;
import org.apache.chemistry.opencmis.commons.spi.AuthenticationProvider;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.apache.chemistry.opencmis.commons.spi.DiscoveryService;
import org.apache.chemistry.opencmis.commons.spi.ExtendedAclService;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.commons.spi.NavigationService;
import org.apache.chemistry.opencmis.commons.spi.RelationshipService;
import org.apache.chemistry.opencmis.commons.spi.RepositoryService;

/**
 * Persistent model session.
 */
public class SessionImpl implements Session {

    private static final OperationContext DEFAULT_CONTEXT = new OperationContextImpl(null, false, true, false,
            IncludeRelationships.NONE, null, true, null, true, 100);

    private static final Set<Updatability> CREATE_UPDATABILITY = new HashSet<Updatability>();
    private static final Set<Updatability> CREATE_AND_CHECKOUT_UPDATABILITY = new HashSet<Updatability>();

    static {
        CREATE_UPDATABILITY.add(Updatability.ONCREATE);
        CREATE_UPDATABILITY.add(Updatability.READWRITE);
        CREATE_AND_CHECKOUT_UPDATABILITY.add(Updatability.ONCREATE);
        CREATE_AND_CHECKOUT_UPDATABILITY.add(Updatability.READWRITE);
        CREATE_AND_CHECKOUT_UPDATABILITY.add(Updatability.WHENCHECKEDOUT);
    }

    // private static Logger log = LoggerFactory.getLogger(SessionImpl.class);

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /*
     * default session context (serializable)
     */
    private OperationContext context = DEFAULT_CONTEXT;

    /*
     * session parameter (serializable)
     */
    private Map<String, String> parameters;

    /*
     * CMIS binding (serializable)
     */
    private CmisBinding binding;

    /*
     * Session Locale, determined from session parameter (serializable)
     */
    private Locale locale;

    /*
     * Object factory (serializable)
     */
    private final ObjectFactory objectFactory;

    /*
     * Authentication provider (serializable)
     */
    private final AuthenticationProvider authenticationProvider;

    /*
     * Object cache (serializable)
     */
    private Cache cache;
    private final boolean cachePathOmit;

    /*
     * Repository info (serializable)
     */
    private RepositoryInfo repositoryInfo;

    /**
     * required for serialization
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public SessionImpl(Map<String, String> parameters, ObjectFactory objectFactory,
            AuthenticationProvider authenticationProvider, Cache cache) {
        if (parameters == null) {
            throw new IllegalArgumentException("No parameters provided!");
        }

        this.parameters = parameters;
        this.locale = determineLocale(parameters);

        this.objectFactory = (objectFactory == null ? createObjectFactory() : objectFactory);
        this.authenticationProvider = authenticationProvider;
        this.cache = (cache == null ? createCache() : cache);

        cachePathOmit = Boolean.parseBoolean(parameters.get(SessionParameter.CACHE_PATH_OMIT));
    }

    private static Locale determineLocale(Map<String, String> parameters) {
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

    private ObjectFactory createObjectFactory() {
        try {
            String classname = parameters.get(SessionParameter.OBJECT_FACTORY_CLASS);

            Class<?> objectFactoryClass;
            if (classname == null) {
                objectFactoryClass = ObjectFactoryImpl.class;
            } else {
                objectFactoryClass = Class.forName(classname);
            }

            Object of = objectFactoryClass.newInstance();
            if (!(of instanceof ObjectFactory)) {
                throw new Exception("Class does not implement ObjectFactory!");
            }

            ((ObjectFactory) of).initialize(this, parameters);

            return (ObjectFactory) of;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to create object factory: " + e, e);
        }
    }

    private Cache createCache() {
        try {
            String classname = parameters.get(SessionParameter.CACHE_CLASS);

            Class<?> cacheClass;
            if (classname == null) {
                cacheClass = CacheImpl.class;
            } else {
                cacheClass = Class.forName(classname);
            }

            Object of = cacheClass.newInstance();
            if (!(of instanceof Cache)) {
                throw new Exception("Class does not implement Cache!");
            }

            ((Cache) of).initialize(this, parameters);

            return (Cache) of;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to create cache: " + e, e);
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            // create new object cache
            this.cache = createCache();

            // clear provider cache
            getBinding().clearAllCaches();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ObjectFactory getObjectFactory() {
        return this.objectFactory;
    }

    public ItemIterable<Document> getCheckedOutDocs() {
        return getCheckedOutDocs(getDefaultContext());
    }

    public ItemIterable<Document> getCheckedOutDocs(OperationContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Operation context must be set!");
        }

        final NavigationService navigationService = getBinding().getNavigationService();
        final ObjectFactory objectFactory = getObjectFactory();
        final OperationContext ctxt = new OperationContextImpl(context);

        return new CollectionIterable<Document>(new AbstractPageFetcher<Document>(ctxt.getMaxItemsPerPage()) {

            @Override
            protected AbstractPageFetcher.Page<Document> fetchPage(long skipCount) {

                // get all checked out documents
                ObjectList checkedOutDocs = navigationService.getCheckedOutDocs(getRepositoryId(), null,
                        ctxt.getFilterString(), ctxt.getOrderBy(), ctxt.isIncludeAllowableActions(),
                        ctxt.getIncludeRelationships(), ctxt.getRenditionFilterString(),
                        BigInteger.valueOf(this.maxNumItems), BigInteger.valueOf(skipCount), null);

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

                return new AbstractPageFetcher.Page<Document>(page, checkedOutDocs.getNumItems(),
                        checkedOutDocs.hasMoreItems());
            }
        });
    }

    public ChangeEvents getContentChanges(String changeLogToken, boolean includeProperties, long maxNumItems) {
        return getContentChanges(changeLogToken, includeProperties, maxNumItems, getDefaultContext());
    }

    public ChangeEvents getContentChanges(String changeLogToken, boolean includeProperties, long maxNumItems,
            OperationContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Operation context must be set!");
        }

        lock.readLock().lock();
        try {
            Holder<String> changeLogTokenHolder = new Holder<String>(changeLogToken);

            ObjectList objectList = getBinding().getDiscoveryService().getContentChanges(getRepositoryInfo().getId(),
                    changeLogTokenHolder, includeProperties, context.getFilterString(), context.isIncludePolicies(),
                    context.isIncludeAcls(), BigInteger.valueOf(maxNumItems), null);

            return objectFactory.convertChangeEvents(changeLogTokenHolder.getValue(), objectList);
        } finally {
            lock.readLock().unlock();
        }
    }

    public OperationContext getDefaultContext() {
        lock.readLock().lock();
        try {
            return this.context;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setDefaultContext(OperationContext context) {
        lock.writeLock().lock();
        try {
            this.context = (context == null ? DEFAULT_CONTEXT : context);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public OperationContext createOperationContext(Set<String> filter, boolean includeAcls,
            boolean includeAllowableActions, boolean includePolicies, IncludeRelationships includeRelationships,
            Set<String> renditionFilter, boolean includePathSegments, String orderBy, boolean cacheEnabled,
            int maxItemsPerPage) {
        return new OperationContextImpl(filter, includeAcls, includeAllowableActions, includePolicies,
                includeRelationships, renditionFilter, includePathSegments, orderBy, cacheEnabled, maxItemsPerPage);
    }

    public OperationContext createOperationContext() {
        return new OperationContextImpl();
    }

    public ObjectId createObjectId(String id) {
        return new ObjectIdImpl(id);
    }

    public Locale getLocale() {
        return this.locale;
    }

    public CmisObject getObject(ObjectId objectId) {
        return getObject(objectId, getDefaultContext());
    }

    public CmisObject getObject(ObjectId objectId, OperationContext context) {
        if ((objectId == null) || (objectId.getId() == null)) {
            throw new IllegalArgumentException("Object Id must be set!");
        }

        return getObject(objectId.getId(), context);
    }

    public CmisObject getObject(String objectId) {
        return getObject(objectId, getDefaultContext());
    }

    public CmisObject getObject(String objectId, OperationContext context) {
        if (objectId == null) {
            throw new IllegalArgumentException("Object Id must be set!");
        }
        if (context == null) {
            throw new IllegalArgumentException("Operation context must be set!");
        }

        CmisObject result = null;

        // ask the cache first
        if (context.isCacheEnabled()) {
            result = this.cache.getById(objectId, context.getCacheKey());
            if (result != null) {
                return result;
            }
        }

        // get the object
        ObjectData objectData = this.binding.getObjectService().getObject(getRepositoryId(), objectId,
                context.getFilterString(), context.isIncludeAllowableActions(), context.getIncludeRelationships(),
                context.getRenditionFilterString(), context.isIncludePolicies(), context.isIncludeAcls(), null);

        result = getObjectFactory().convertObject(objectData, context);

        // put into cache
        if (context.isCacheEnabled()) {
            this.cache.put(result, context.getCacheKey());
        }

        return result;
    }

    public CmisObject getObjectByPath(String path) {
        return getObjectByPath(path, getDefaultContext());
    }

    public CmisObject getObjectByPath(String path, OperationContext context) {
        if (path == null) {
            throw new IllegalArgumentException("Path must be set!");
        }
        if (context == null) {
            throw new IllegalArgumentException("Operation context must be set!");
        }

        CmisObject result = null;

        // ask the cache first
        if (context.isCacheEnabled() && !cachePathOmit) {
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

    public void removeObjectFromCache(ObjectId objectId) {
        if ((objectId == null) || (objectId.getId() == null)) {
            return;
        }

        removeObjectFromCache(objectId.getId());
    }

    public void removeObjectFromCache(String objectId) {
        cache.remove(objectId);
    }

    public RepositoryInfo getRepositoryInfo() {
        lock.readLock().lock();
        try {
            return this.repositoryInfo;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Folder getRootFolder() {
        return getRootFolder(getDefaultContext());
    }

    public Folder getRootFolder(OperationContext context) {
        String rootFolderId = getRepositoryInfo().getRootFolderId();

        CmisObject rootFolder = getObject(createObjectId(rootFolderId), context);
        if (!(rootFolder instanceof Folder)) {
            throw new CmisRuntimeException("Root folder object is not a folder!");
        }

        return (Folder) rootFolder;
    }

    public ItemIterable<ObjectType> getTypeChildren(final String typeId, final boolean includePropertyDefinitions) {
        final RepositoryService repositoryService = getBinding().getRepositoryService();
        final ObjectFactory objectFactory = this.getObjectFactory();

        return new CollectionIterable<ObjectType>(new AbstractPageFetcher<ObjectType>(this.getDefaultContext()
                .getMaxItemsPerPage()) {

            @Override
            protected AbstractPageFetcher.Page<ObjectType> fetchPage(long skipCount) {

                // fetch the data
                TypeDefinitionList tdl = repositoryService.getTypeChildren(SessionImpl.this.getRepositoryId(), typeId,
                        includePropertyDefinitions, BigInteger.valueOf(this.maxNumItems),
                        BigInteger.valueOf(skipCount), null);

                // convert type definitions
                List<ObjectType> page = new ArrayList<ObjectType>(tdl.getList().size());
                for (TypeDefinition typeDefinition : tdl.getList()) {
                    page.add(objectFactory.convertTypeDefinition(typeDefinition));
                }

                return new AbstractPageFetcher.Page<ObjectType>(page, tdl.getNumItems(), tdl.hasMoreItems()) {
                };
            }
        });
    }

    public ObjectType getTypeDefinition(String typeId) {
        TypeDefinition typeDefinition = getBinding().getRepositoryService().getTypeDefinition(getRepositoryId(),
                typeId, null);
        return objectFactory.convertTypeDefinition(typeDefinition);
    }

    public List<Tree<ObjectType>> getTypeDescendants(String typeId, int depth, boolean includePropertyDefinitions) {
        List<TypeDefinitionContainer> descendants = getBinding().getRepositoryService().getTypeDescendants(
                getRepositoryId(), typeId, BigInteger.valueOf(depth), includePropertyDefinitions, null);

        return convertTypeDescendants(descendants);
    }

    /**
     * Converts binding <code>TypeDefinitionContainer</code> to API
     * <code>Container</code>.
     */
    private List<Tree<ObjectType>> convertTypeDescendants(List<TypeDefinitionContainer> descendantsList) {
        List<Tree<ObjectType>> result = new ArrayList<Tree<ObjectType>>();

        for (TypeDefinitionContainer container : descendantsList) {
            ObjectType objectType = objectFactory.convertTypeDefinition(container.getTypeDefinition());
            List<Tree<ObjectType>> children = convertTypeDescendants(container.getChildren());

            result.add(new TreeImpl<ObjectType>(objectType, children));
        }

        return result;
    }

    public ItemIterable<QueryResult> query(final String statement, final boolean searchAllVersions) {
        return query(statement, searchAllVersions, getDefaultContext());
    }

    public ItemIterable<QueryResult> query(final String statement, final boolean searchAllVersions,
            OperationContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Operation context must be set!");
        }

        final DiscoveryService discoveryService = getBinding().getDiscoveryService();
        final ObjectFactory objectFactory = this.getObjectFactory();
        final OperationContext ctxt = new OperationContextImpl(context);

        return new CollectionIterable<QueryResult>(new AbstractPageFetcher<QueryResult>(ctxt.getMaxItemsPerPage()) {

            @Override
            protected AbstractPageFetcher.Page<QueryResult> fetchPage(long skipCount) {

                // fetch the data
                ObjectList resultList = discoveryService.query(getRepositoryId(), statement, searchAllVersions,
                        ctxt.isIncludeAllowableActions(), ctxt.getIncludeRelationships(),
                        ctxt.getRenditionFilterString(), BigInteger.valueOf(this.maxNumItems),
                        BigInteger.valueOf(skipCount), null);

                // convert query results
                List<QueryResult> page = new ArrayList<QueryResult>();
                if (resultList.getObjects() != null) {
                    for (ObjectData objectData : resultList.getObjects()) {
                        if (objectData == null) {
                            continue;
                        }

                        page.add(objectFactory.convertQueryResult(objectData));
                    }
                }

                return new AbstractPageFetcher.Page<QueryResult>(page, resultList.getNumItems(),
                        resultList.hasMoreItems());
            }
        });
    }

    public ItemIterable<CmisObject> queryObjects(String typeId, String where, final boolean searchAllVersions,
            OperationContext context) {
        if (typeId == null || typeId.trim().length() == 0) {
            throw new IllegalArgumentException("Type id must be set!");
        }

        if (context == null) {
            throw new IllegalArgumentException("Operation context must be set!");
        }

        final DiscoveryService discoveryService = getBinding().getDiscoveryService();
        final ObjectFactory objectFactory = this.getObjectFactory();
        final OperationContext ctxt = new OperationContextImpl(context);
        final StringBuilder statement = new StringBuilder("SELECT ");

        String select = ctxt.getFilterString();
        if (select == null) {
            statement.append("*");
        } else {
            statement.append(select);
        }

        final ObjectType type = getTypeDefinition(typeId);
        statement.append(" FROM ");
        statement.append(type.getQueryName());

        if (where != null && where.trim().length() > 0) {
            statement.append(" WHERE ");
            statement.append(where);
        }

        String orderBy = ctxt.getOrderBy();
        if (orderBy != null && orderBy.trim().length() > 0) {
            statement.append(" ORDER BY ");
            statement.append(orderBy);
        }

        return new CollectionIterable<CmisObject>(new AbstractPageFetcher<CmisObject>(ctxt.getMaxItemsPerPage()) {

            @Override
            protected AbstractPageFetcher.Page<CmisObject> fetchPage(long skipCount) {

                // fetch the data
                ObjectList resultList = discoveryService.query(getRepositoryId(), statement.toString(),
                        searchAllVersions, ctxt.isIncludeAllowableActions(), ctxt.getIncludeRelationships(),
                        ctxt.getRenditionFilterString(), BigInteger.valueOf(this.maxNumItems),
                        BigInteger.valueOf(skipCount), null);

                // convert query results
                List<CmisObject> page = new ArrayList<CmisObject>();
                if (resultList.getObjects() != null) {
                    for (ObjectData objectData : resultList.getObjects()) {
                        if (objectData == null) {
                            continue;
                        }

                        page.add(objectFactory.convertObject(objectData, ctxt));
                    }
                }

                return new AbstractPageFetcher.Page<CmisObject>(page, resultList.getNumItems(),
                        resultList.hasMoreItems());
            }
        });
    }

    public QueryStatement createQueryStatement(final String statement) {
        return new QueryStatementImpl(this, statement);
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
     * In dependency of the parameter set an {@code AtomPub}, a
     * {@code WebService} or an {@code InMemory} provider is selected.
     */
    public void connect() {
        lock.writeLock().lock();
        try {
            this.binding = CmisBindingHelper.createBinding(parameters, authenticationProvider);

            /* get initial repository id from session parameter */
            String repositoryId = parameters.get(SessionParameter.REPOSITORY_ID);
            if (repositoryId == null) {
                throw new IllegalStateException("Repository Id is not set!");
            }

            repositoryInfo = objectFactory.convertRepositoryInfo(getBinding().getRepositoryService().getRepositoryInfo(
                    repositoryId, null));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public CmisBinding getBinding() {
        lock.readLock().lock();
        try {
            return this.binding;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Cache getCache() {
        lock.readLock().lock();
        try {
            return this.cache;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the repository id.
     */
    public String getRepositoryId() {
        return getRepositoryInfo().getId();
    }

    // --- creates ---

    public ObjectId createDocument(Map<String, ?> properties, ObjectId folderId, ContentStream contentStream,
            VersioningState versioningState, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces) {
        if ((properties == null) || (properties.isEmpty())) {
            throw new IllegalArgumentException("Properties must not be empty!");
        }

        String newId = getBinding().getObjectService().createDocument(
                getRepositoryId(),
                objectFactory.convertProperties(properties, null,
                        (versioningState == VersioningState.CHECKEDOUT ? CREATE_AND_CHECKOUT_UPDATABILITY
                                : CREATE_UPDATABILITY)), (folderId == null ? null : folderId.getId()),
                objectFactory.convertContentStream(contentStream), versioningState,
                objectFactory.convertPolicies(policies), objectFactory.convertAces(addAces),
                objectFactory.convertAces(removeAces), null);

        if (newId == null) {
            return null;
        }

        return createObjectId(newId);
    }

    public ObjectId createDocumentFromSource(ObjectId source, Map<String, ?> properties, ObjectId folderId,
            VersioningState versioningState, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces) {
        if ((source == null) || (source.getId() == null)) {
            throw new IllegalArgumentException("Source must be set!");
        }

        // get the type of the source document
        ObjectType type = null;
        if (source instanceof CmisObject) {
            type = ((CmisObject) source).getType();
        } else {
            CmisObject sourceObj = getObject(source);
            type = sourceObj.getType();
        }

        if (type.getBaseTypeId() != BaseTypeId.CMIS_DOCUMENT) {
            throw new IllegalArgumentException("Source object must be a document!");
        }

        String newId = getBinding().getObjectService().createDocumentFromSource(
                getRepositoryId(),
                source.getId(),
                objectFactory.convertProperties(properties, type,
                        (versioningState == VersioningState.CHECKEDOUT ? CREATE_AND_CHECKOUT_UPDATABILITY
                                : CREATE_UPDATABILITY)), (folderId == null ? null : folderId.getId()), versioningState,
                objectFactory.convertPolicies(policies), objectFactory.convertAces(addAces),
                objectFactory.convertAces(removeAces), null);

        if (newId == null) {
            return null;
        }

        return createObjectId(newId);
    }

    public ObjectId createFolder(Map<String, ?> properties, ObjectId folderId, List<Policy> policies,
            List<Ace> addAces, List<Ace> removeAces) {
        if ((folderId == null) || (folderId.getId() == null)) {
            throw new IllegalArgumentException("Folder Id must be set!");
        }
        if ((properties == null) || (properties.isEmpty())) {
            throw new IllegalArgumentException("Properties must not be empty!");
        }

        String newId = getBinding().getObjectService().createFolder(getRepositoryId(),
                objectFactory.convertProperties(properties, null, CREATE_UPDATABILITY), folderId.getId(),
                objectFactory.convertPolicies(policies), objectFactory.convertAces(addAces),
                objectFactory.convertAces(removeAces), null);

        if (newId == null) {
            return null;
        }

        return createObjectId(newId);
    }

    public ObjectId createPolicy(Map<String, ?> properties, ObjectId folderId, List<Policy> policies,
            List<Ace> addAces, List<Ace> removeAces) {
        if ((properties == null) || (properties.isEmpty())) {
            throw new IllegalArgumentException("Properties must not be empty!");
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

    public ObjectId createRelationship(Map<String, ?> properties, List<Policy> policies, List<Ace> addAces,
            List<Ace> removeAces) {
        if ((properties == null) || (properties.isEmpty())) {
            throw new IllegalArgumentException("Properties must not be empty!");
        }

        String newId = getBinding().getObjectService().createRelationship(getRepositoryId(),
                objectFactory.convertProperties(properties, null, CREATE_UPDATABILITY),
                objectFactory.convertPolicies(policies), objectFactory.convertAces(addAces),
                objectFactory.convertAces(removeAces), null);

        if (newId == null) {
            return null;
        }

        return createObjectId(newId);
    }

    public ObjectId createDocument(Map<String, ?> properties, ObjectId folderId, ContentStream contentStream,
            VersioningState versioningState) {
        return this.createDocument(properties, folderId, contentStream, versioningState, null, null, null);
    }

    public ObjectId createDocumentFromSource(ObjectId source, Map<String, ?> properties, ObjectId folderId,
            VersioningState versioningState) {
        return this.createDocumentFromSource(source, properties, folderId, versioningState, null, null, null);
    }

    public ObjectId createFolder(Map<String, ?> properties, ObjectId folderId) {
        return this.createFolder(properties, folderId, null, null, null);
    }

    public ObjectId createPolicy(Map<String, ?> properties, ObjectId folderId) {
        return this.createPolicy(properties, folderId, null, null, null);
    }

    // --- relationships ---

    public ObjectId createRelationship(Map<String, ?> properties) {
        return this.createRelationship(properties, null, null, null);
    }

    public ItemIterable<Relationship> getRelationships(ObjectId objectId, final boolean includeSubRelationshipTypes,
            final RelationshipDirection relationshipDirection, ObjectType type, OperationContext context) {
        if ((objectId == null) || (objectId.getId() == null)) {
            throw new IllegalArgumentException("Invalid object id!");
        }
        if (context == null) {
            throw new IllegalArgumentException("Operation context must be set!");
        }

        final String id = objectId.getId();
        final String typeId = (type == null ? null : type.getId());
        final RelationshipService relationshipService = getBinding().getRelationshipService();
        final OperationContext ctxt = new OperationContextImpl(context);

        return new CollectionIterable<Relationship>(new AbstractPageFetcher<Relationship>(ctxt.getMaxItemsPerPage()) {

            @Override
            protected AbstractPageFetcher.Page<Relationship> fetchPage(long skipCount) {

                // fetch the relationships
                ObjectList relList = relationshipService.getObjectRelationships(getRepositoryId(), id,
                        includeSubRelationshipTypes, relationshipDirection, typeId, ctxt.getFilterString(),
                        ctxt.isIncludeAllowableActions(), BigInteger.valueOf(this.maxNumItems),
                        BigInteger.valueOf(skipCount), null);

                // convert relationship objects
                List<Relationship> page = new ArrayList<Relationship>();
                if (relList.getObjects() != null) {
                    for (ObjectData rod : relList.getObjects()) {
                        CmisObject relationship = getObject(createObjectId(rod.getId()), ctxt);
                        if (!(relationship instanceof Relationship)) {
                            throw new CmisRuntimeException("Repository returned an object that is not a relationship!");
                        }

                        page.add((Relationship) relationship);
                    }
                }

                return new AbstractPageFetcher.Page<Relationship>(page, relList.getNumItems(), relList.hasMoreItems());
            }
        });
    }

    // --- delete ---

    public void delete(ObjectId objectId) {
        delete(objectId, true);
    }

    public void delete(ObjectId objectId, boolean allVersions) {
        if ((objectId == null) || (objectId.getId() == null)) {
            throw new IllegalArgumentException("Invalid object id!");
        }

        getBinding().getObjectService().deleteObject(getRepositoryId(), objectId.getId(), allVersions, null);
        removeObjectFromCache(objectId);
    }

    // --- content stream ---

    public ContentStream getContentStream(ObjectId docId) {
        return getContentStream(docId, null, null, null);
    }

    public ContentStream getContentStream(ObjectId docId, String streamId, BigInteger offset, BigInteger length) {
        if ((docId == null) || (docId.getId() == null)) {
            throw new IllegalArgumentException("Invalid document id!");
        }

        // get the stream
        ContentStream contentStream = null;
        try {
            contentStream = getBinding().getObjectService().getContentStream(getRepositoryId(), docId.getId(),
                    streamId, offset, length, null);
        } catch (CmisConstraintException e) {
            // no content stream
            return null;
        }

        return contentStream;
    }

    // --- ACL ---

    public Acl getAcl(ObjectId objectId, boolean onlyBasicPermissions) {
        if ((objectId == null) || (objectId.getId() == null)) {
            throw new IllegalArgumentException("Invalid object id!");
        }

        String id = objectId.getId();

        return getBinding().getAclService().getAcl(getRepositoryId(), id, onlyBasicPermissions, null);
    }

    public Acl applyAcl(ObjectId objectId, List<Ace> addAces, List<Ace> removeAces, AclPropagation aclPropagation) {
        if ((objectId == null) || (objectId.getId() == null)) {
            throw new IllegalArgumentException("Invalid object id!");
        }

        ObjectFactory of = getObjectFactory();

        return getBinding().getAclService().applyAcl(getRepositoryId(), objectId.getId(), of.convertAces(addAces),
                of.convertAces(removeAces), aclPropagation, null);
    }

    public Acl setAcl(ObjectId objectId, List<Ace> aces) {
        if ((objectId == null) || (objectId.getId() == null)) {
            throw new IllegalArgumentException("Invalid object id!");
        }
        if (aces == null) {
            aces = Collections.emptyList();
        }

        AclService aclService = getBinding().getAclService();
        if (!(aclService instanceof ExtendedAclService)) {
            throw new CmisNotSupportedException("setAcl() is not supported by the binding implementation.");
        }

        ObjectFactory of = getObjectFactory();

        return ((ExtendedAclService) aclService).setAcl(getRepositoryId(), objectId.getId(), of.convertAces(aces));
    }

    // --- Policies ---

    public void applyPolicy(ObjectId objectId, ObjectId... policyIds) {
        if ((objectId == null) || (objectId.getId() == null)) {
            throw new IllegalArgumentException("Invalid object id!");
        }

        if ((policyIds == null) || (policyIds.length == 0)) {
            throw new IllegalArgumentException("No Policies provided!");
        }

        String[] ids = new String[policyIds.length];
        for (int i = 0; i < policyIds.length; i++) {
            if ((policyIds[i] == null) || (policyIds[i].getId() == null)) {
                throw new IllegalArgumentException("A Policy Id is not set!");
            }

            ids[i] = policyIds[i].getId();
        }

        for (String id : ids) {
            getBinding().getPolicyService().applyPolicy(getRepositoryId(), id, objectId.getId(), null);
        }
    }

    public void removePolicy(ObjectId objectId, ObjectId... policyIds) {
        if ((objectId == null) || (objectId.getId() == null)) {
            throw new IllegalArgumentException("Invalid object id!");
        }

        if ((policyIds == null) || (policyIds.length == 0)) {
            throw new IllegalArgumentException("No Policies provided!");
        }

        String[] ids = new String[policyIds.length];
        for (int i = 0; i < policyIds.length; i++) {
            if ((policyIds[i] == null) || (policyIds[i].getId() == null)) {
                throw new IllegalArgumentException("A Policy Id is not set!");
            }

            ids[i] = policyIds[i].getId();
        }

        for (String id : ids) {
            getBinding().getPolicyService().removePolicy(getRepositoryId(), id, objectId.getId(), null);
        }
    }
}
