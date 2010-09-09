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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectFactory;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.runtime.util.AbstractPageFetch;
import org.apache.chemistry.opencmis.client.runtime.util.CollectionIterable;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ExtensionLevel;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.commons.spi.RelationshipService;

/**
 * Base class for all persistent session object impl classes.
 */
public abstract class AbstractPersistentCmisObject implements CmisObject {

    private PersistentSessionImpl session;
    private ObjectType objectType;
    private Map<String, Property<?>> properties;
    private AllowableActions allowableActions;
    private List<Rendition> renditions;
    private Acl acl;
    private List<Policy> policies;
    private List<Relationship> relationships;
    private Map<ExtensionLevel, List<Object>> extensions;
    private OperationContext creationContext;
    private boolean isChanged = false;
    private long refreshTimestamp;

    private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock();

    /**
     * Initializes the object.
     */
    protected void initialize(PersistentSessionImpl session, ObjectType objectType, ObjectData objectData,
            OperationContext context) {
        if (session == null) {
            throw new IllegalArgumentException("Session must be set!");
        }

        if (objectType == null) {
            throw new IllegalArgumentException("Object type must be set!");
        }

        if ((objectType.getPropertyDefinitions() == null) || objectType.getPropertyDefinitions().size() < 9) {
            // there must be at least the 9 standard properties that all objects
            // have
            throw new IllegalArgumentException("Object type must have property defintions!");
        }

        this.session = session;
        this.objectType = objectType;
        this.extensions = new HashMap<ExtensionLevel, List<Object>>();
        this.creationContext = new OperationContextImpl(context);
        this.refreshTimestamp = System.currentTimeMillis();

        ObjectFactory of = getObjectFactory();

        if (objectData != null) {
            // handle properties
            if (objectData.getProperties() != null) {
                this.properties = of.convertProperties(objectType, objectData.getProperties());
                extensions.put(ExtensionLevel.PROPERTIES, objectData.getProperties().getExtensions());
            }

            // handle allowable actions
            if (objectData.getAllowableActions() != null) {
                this.allowableActions = objectData.getAllowableActions();
                extensions.put(ExtensionLevel.ALLOWABLE_ACTIONS, objectData.getAllowableActions().getExtensions());
            }

            // handle renditions
            if (objectData.getRenditions() != null) {
                this.renditions = new ArrayList<Rendition>();
                for (RenditionData rd : objectData.getRenditions()) {
                    this.renditions.add(of.convertRendition(getId(), rd));
                }
            }

            // handle ACL
            if (objectData.getAcl() != null) {
                acl = objectData.getAcl();
                extensions.put(ExtensionLevel.ACL, objectData.getAcl().getExtensions());
            }

            // handle policies
            if ((objectData.getPolicyIds() != null) && (objectData.getPolicyIds().getPolicyIds() != null)) {
                policies = new ArrayList<Policy>();
                for (String pid : objectData.getPolicyIds().getPolicyIds()) {
                    CmisObject policy = session.getObject(getSession().createObjectId(pid));
                    if (policy instanceof Policy) {
                        policies.add((Policy) policy);
                    }
                }
                extensions.put(ExtensionLevel.POLICIES, objectData.getPolicyIds().getExtensions());
            }

            // handle relationships
            if (objectData.getRelationships() != null) {
                relationships = new ArrayList<Relationship>();
                for (ObjectData rod : objectData.getRelationships()) {
                    CmisObject relationship = of.convertObject(rod, this.creationContext);
                    if (relationship instanceof Relationship) {
                        relationships.add((Relationship) relationship);
                    }
                }
            }

            extensions.put(ExtensionLevel.OBJECT, objectData.getExtensions());
        }

        isChanged = false;
    }

    /**
     * Acquires a write lock.
     */
    protected void writeLock() {
        fLock.writeLock().lock();
    }

    /**
     * Releases a write lock.
     */
    protected void writeUnlock() {
        fLock.writeLock().unlock();
    }

    /**
     * Acquires a read lock.
     */
    protected void readLock() {
        fLock.readLock().lock();
    }

    /**
     * Releases a read lock.
     */
    protected void readUnlock() {
        fLock.readLock().unlock();
    }

    /**
     * Returns the session object.
     */
    protected PersistentSessionImpl getSession() {
        return this.session;
    }

    /**
     * Returns the repository id.
     */
    protected String getRepositoryId() {
        return getSession().getRepositoryId();
    }

    /**
     * Returns the object type.
     */
    protected ObjectType getObjectType() {
        readLock();
        try {
            return this.objectType;
        } finally {
            readUnlock();
        }
    }

    /**
     * Returns the binding object.
     */
    protected CmisBinding getBinding() {
        return getSession().getBinding();
    }

    /**
     * Returns the object factory.
     */
    protected ObjectFactory getObjectFactory() {
        return getSession().getObjectFactory();
    }

    /**
     * Returns the id of this object or throws an exception if the id is
     * unknown.
     */
    protected String getObjectId() {
        String objectId = getId();
        if (objectId == null) {
            throw new IllegalStateException("Object Id is unknown!");
        }

        return objectId;
    }

    /**
     * Returns the query name of a property.
     */
    protected String getPropertyQueryName(String propertyId) {
        readLock();
        try {
            PropertyDefinition<?> propDef = objectType.getPropertyDefinitions().get(propertyId);
            if (propDef == null) {
                return null;
            }

            return propDef.getQueryName();
        } finally {
            readUnlock();
        }
    }

    // --- operations ---

    public void delete(boolean allVersions) {
        String objectId = getObjectId();
        getBinding().getObjectService().deleteObject(getRepositoryId(), objectId, allVersions, null);
    }

    public ObjectId updateProperties() {
        readLock();
        try {
            String objectId = getObjectId();
            Holder<String> objectIdHolder = new Holder<String>(objectId);

            String changeToken = getChangeToken();
            Holder<String> changeTokenHolder = new Holder<String>(changeToken);

            Set<Updatability> updatebility = new HashSet<Updatability>();
            updatebility.add(Updatability.READWRITE);

            // check if checked out
            Boolean isCheckedOut = getPropertyValue(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT);
            if ((isCheckedOut != null) && isCheckedOut.booleanValue()) {
                updatebility.add(Updatability.WHENCHECKEDOUT);
            }

            // it's time to update
            getBinding().getObjectService().updateProperties(getRepositoryId(), objectIdHolder, changeTokenHolder,
                    getObjectFactory().convertProperties(this.properties, this.objectType, updatebility), null);

            if (objectIdHolder.getValue() == null) {
                return null;
            }

            return getSession().createObjectId(objectIdHolder.getValue());
        } finally {
            readUnlock();
        }
    }

    public ObjectId updateProperties(Map<String, ?> properties) {
        if ((properties == null) || (properties.isEmpty())) {
            throw new IllegalArgumentException("Properties must not be empty!");
        }

        readLock();
        try {
            String objectId = getObjectId();
            Holder<String> objectIdHolder = new Holder<String>(objectId);

            String changeToken = getChangeToken();
            Holder<String> changeTokenHolder = new Holder<String>(changeToken);

            Set<Updatability> updatebility = new HashSet<Updatability>();
            updatebility.add(Updatability.READWRITE);

            // check if checked out
            Boolean isCheckedOut = getPropertyValue(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT);
            if ((isCheckedOut != null) && isCheckedOut.booleanValue()) {
                updatebility.add(Updatability.WHENCHECKEDOUT);
            }

            // it's time to update
            getBinding().getObjectService().updateProperties(getRepositoryId(), objectIdHolder, changeTokenHolder,
                    getObjectFactory().convertProperties(properties, this.objectType, updatebility), null);

            if (objectIdHolder.getValue() == null) {
                return null;
            }

            return getSession().createObjectId(objectIdHolder.getValue());
        } finally {
            readUnlock();
        }
    }

    // --- properties ---

    public ObjectType getBaseType() {
        BaseTypeId baseTypeId = getBaseTypeId();
        if (baseTypeId == null) {
            return null;
        }

        return getSession().getTypeDefinition(baseTypeId.value());
    }

    public BaseTypeId getBaseTypeId() {
        String baseType = getPropertyValue(PropertyIds.BASE_TYPE_ID);
        if (baseType == null) {
            return null;
        }

        return BaseTypeId.fromValue(baseType);
    }

    public String getChangeToken() {
        return getPropertyValue(PropertyIds.CHANGE_TOKEN);
    }

    public String getCreatedBy() {
        return getPropertyValue(PropertyIds.CREATED_BY);
    }

    public GregorianCalendar getCreationDate() {
        return getPropertyValue(PropertyIds.CREATION_DATE);
    }

    public String getId() {
        return getPropertyValue(PropertyIds.OBJECT_ID);
    }

    public GregorianCalendar getLastModificationDate() {
        return getPropertyValue(PropertyIds.LAST_MODIFICATION_DATE);
    }

    public String getLastModifiedBy() {
        return getPropertyValue(PropertyIds.LAST_MODIFIED_BY);
    }

    public String getName() {
        return getPropertyValue(PropertyIds.NAME);
    }

    public List<Property<?>> getProperties() {
        readLock();
        try {
            return new ArrayList<Property<?>>(this.properties.values());
        } finally {
            readUnlock();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Property<T> getProperty(String id) {
        readLock();
        try {
            return (Property<T>) this.properties.get(id);
        } finally {
            readUnlock();
        }
    }

    public <T> List<T> getPropertyMultivalue(String id) {
        Property<T> property = getProperty(id);
        if (property == null) {
            return null;
        }

        return property.getValues();
    }

    public <T> T getPropertyValue(String id) {
        Property<T> property = getProperty(id);
        if (property == null) {
            return null;
        }

        return property.getFirstValue();
    }

    public void setName(String name) {
        setProperty(PropertyIds.NAME, name);
    }

    @SuppressWarnings("unchecked")
    public <T> void setProperty(String id, T value) {
        PropertyDefinition<?> propertyDefinition = checkProperty(id, value);

        // check updatability
        if (propertyDefinition.getUpdatability() == Updatability.READONLY) {
            throw new IllegalArgumentException("Property is read-only!");
        }

        // create property
        Property<T> newProperty = (Property<T>) getObjectFactory().createProperty(
                (PropertyDefinition<T>) propertyDefinition, value);

        writeLock();
        try {
            setChanged();
            this.properties.put(id, newProperty);
        } finally {
            writeUnlock();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void setPropertyMultivalue(String id, List<T> value) {
        PropertyDefinition<?> propertyDefinition = checkProperty(id, value);

        // check updatability
        if (propertyDefinition.getUpdatability() == Updatability.READONLY) {
            throw new IllegalArgumentException("Property is read-only!");
        }

        // create property
        Property<T> newProperty = (Property<T>) getObjectFactory().createPropertyMultivalue(
                (PropertyDefinition<T>) propertyDefinition, value);

        writeLock();
        try {
            setChanged();
            this.properties.put(id, newProperty);
        } finally {
            writeUnlock();
        }
    }

    public ObjectType getType() {
        readLock();
        try {
            return this.objectType;
        } finally {
            readUnlock();
        }
    }

    // --- allowable actions ---

    public AllowableActions getAllowableActions() {
        readLock();
        try {
            return this.allowableActions;
        } finally {
            readUnlock();
        }
    }

    // --- renditions ---

    public List<Rendition> getRenditions() {
        readLock();
        try {
            return this.renditions;
        } finally {
            readUnlock();
        }
    }

    // --- ACL ---

    public Acl getAcl(boolean onlyBasicPermissions) {
        String objectId = getObjectId();
        return getBinding().getAclService().getAcl(getRepositoryId(), objectId, onlyBasicPermissions, null);
    }

    public Acl applyAcl(List<Ace> addAces, List<Ace> removeAces, AclPropagation aclPropagation) {
        String objectId = getObjectId();

        ObjectFactory of = getObjectFactory();

        return getBinding().getAclService().applyAcl(getRepositoryId(), objectId, of.convertAces(addAces),
                of.convertAces(removeAces), aclPropagation, null);
    }

    public void addAcl(List<Ace> addAces, AclPropagation aclPropagation) {
        applyAcl(addAces, null, aclPropagation);
    }

    public void removeAcl(List<Ace> removeAces, AclPropagation aclPropagation) {
        applyAcl(null, removeAces, aclPropagation);
    }

    public Acl getAcl() {
        readLock();
        try {
            return this.acl;
        } finally {
            readUnlock();
        }
    }

    // --- policies ---

    public void applyPolicy(ObjectId policyId) {
        if ((policyId == null) || (policyId.getId() == null)) {
            throw new IllegalArgumentException("Policy Id is not set!");
        }

        String objectId = getObjectId();
        getBinding().getPolicyService().applyPolicy(getRepositoryId(), policyId.getId(), objectId, null);
    }

    public void removePolicy(ObjectId policyId) {
        if ((policyId == null) || (policyId.getId() == null)) {
            throw new IllegalArgumentException("Policy Id is not set!");
        }

        String objectId = getObjectId();
        getBinding().getPolicyService().removePolicy(getRepositoryId(), policyId.getId(), objectId, null);
    }

    public List<Policy> getPolicies() {
        readLock();
        try {
            return this.policies;
        } finally {
            readUnlock();
        }
    }

    // --- relationships ---

    public List<Relationship> getRelationships() {
        readLock();
        try {
            return this.relationships;
        } finally {
            readUnlock();
        }
    }

    public ItemIterable<Relationship> getRelationships(final boolean includeSubRelationshipTypes,
            final RelationshipDirection relationshipDirection, ObjectType type, OperationContext context) {

        final String objectId = getObjectId();
        final String typeId = (type == null ? null : type.getId());
        final RelationshipService relationshipService = getBinding().getRelationshipService();
        final OperationContext ctxt = new OperationContextImpl(context);

        return new CollectionIterable<Relationship>(new AbstractPageFetch<Relationship>(ctxt.getMaxItemsPerPage()) {

            @Override
            protected AbstractPageFetch.PageFetchResult<Relationship> fetchPage(long skipCount) {

                // fetch the relationships
                ObjectList relList = relationshipService.getObjectRelationships(getRepositoryId(), objectId,
                        includeSubRelationshipTypes, relationshipDirection, typeId, ctxt.getFilterString(), ctxt
                                .isIncludeAllowableActions(), BigInteger.valueOf(this.maxNumItems), BigInteger
                                .valueOf(skipCount), null);

                // convert relationship objects
                List<Relationship> page = new ArrayList<Relationship>();
                if (relList.getObjects() != null) {
                    for (ObjectData rod : relList.getObjects()) {
                        Relationship relationship = new PersistentRelationshipImpl(getSession(), getObjectFactory()
                                .getTypeFromObjectData(rod), rod, ctxt);

                        page.add(relationship);
                    }
                }

                return new AbstractPageFetch.PageFetchResult<Relationship>(page, relList.getNumItems(), relList
                        .hasMoreItems()) {
                };
            }
        });
    }

    // --- extensions ---

    public List<Object> getExtensions(ExtensionLevel level) {
        List<Object> ext = extensions.get(level);
        if (ext == null) {
            return null;
        }

        return Collections.unmodifiableList(ext);
    }

    // --- other ---

    public boolean isChanged() {
        readLock();
        try {
            return isChanged;
        } finally {
            readUnlock();
        }
    }

    /**
     * Sets the isChanged flag to <code>true</code>
     */
    protected void setChanged() {
        writeLock();
        try {
            isChanged = true;
        } finally {
            writeUnlock();
        }
    }

    public long getRefreshTimestamp() {
        readLock();
        try {
            return this.refreshTimestamp;
        } finally {
            readUnlock();
        }
    }

    public void refresh() {
        writeLock();
        try {
            String objectId = getObjectId();

            // get the latest data from the repository
            ObjectData objectData = getSession().getBinding().getObjectService().getObject(getRepositoryId(), objectId,
                    creationContext.getFilterString(), creationContext.isIncludeAllowableActions(),
                    creationContext.getIncludeRelationships(), creationContext.getRenditionFilterString(),
                    creationContext.isIncludePolicies(), creationContext.isIncludeAcls(), null);

            // reset this object
            initialize(getSession(), getObjectType(), objectData, this.creationContext);
        } finally {
            writeUnlock();
        }
    }

    public void refreshIfOld(long durationInMillis) {
        writeLock();
        try {
            if (this.refreshTimestamp < System.currentTimeMillis() - durationInMillis) {
                refresh();
            }
        } finally {
            writeUnlock();
        }
    }

    // --- internal ---

    /**
     * Checks if a value matches a property definition.
     */
    private PropertyDefinition<?> checkProperty(String id, Object value) {
        PropertyDefinition<?> propertyDefinition = getObjectType().getPropertyDefinitions().get(id);
        if (propertyDefinition == null) {
            throw new IllegalArgumentException("Unknown property '" + id + "'!");
        }

        // null values are ok for updates
        if (value == null) {
            return propertyDefinition;
        }

        // single and multi value check
        List<?> values = null;
        if (value instanceof List<?>) {
            if (propertyDefinition.getCardinality() != Cardinality.MULTI) {
                throw new IllegalArgumentException("Property '" + propertyDefinition.getId()
                        + "' is not a multi value property!");
            }

            values = (List<?>) value;
            if (values.isEmpty()) {
                return propertyDefinition;
            }
        } else {
            if (propertyDefinition.getCardinality() != Cardinality.SINGLE) {
                throw new IllegalArgumentException("Property '" + propertyDefinition.getId()
                        + "' is not a single value property!");
            }

            values = Collections.singletonList(value);
        }

        // check if list contains null values
        for (Object o : values) {
            if (o == null) {
                throw new IllegalArgumentException("Property '" + propertyDefinition.getId()
                        + "' contains null values!");
            }
        }

        // take a sample and test the data type
        boolean typeMatch = false;
        Object firstValue = values.get(0);

        switch (propertyDefinition.getPropertyType()) {
        case STRING:
        case ID:
        case URI:
        case HTML:
            typeMatch = (firstValue instanceof String);
            break;
        case INTEGER:
            typeMatch = (firstValue instanceof BigInteger);
            break;
        case DECIMAL:
            typeMatch = (firstValue instanceof BigDecimal);
            break;
        case BOOLEAN:
            typeMatch = (firstValue instanceof Boolean);
            break;
        case DATETIME:
            typeMatch = (firstValue instanceof GregorianCalendar);
            break;
        }

        if (!typeMatch) {
            throw new IllegalArgumentException("Value of property '" + propertyDefinition.getId()
                    + "' does not match property type!");
        }

        return propertyDefinition;
    }
}
