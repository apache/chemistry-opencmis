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
import org.apache.chemistry.opencmis.client.api.ObjectFactory;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.TransientCmisObject;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.ExtensionLevel;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.apache.chemistry.opencmis.commons.spi.Holder;

/**
 * Base class for all persistent session object impl classes.
 */
public abstract class AbstractCmisObject implements CmisObject, Serializable {

    private static final long serialVersionUID = 1L;

    private SessionImpl session;
    private ObjectType objectType;
    private Map<String, Property<?>> properties;
    private AllowableActions allowableActions;
    private List<Rendition> renditions;
    private Acl acl;
    private List<Policy> policies;
    private List<Relationship> relationships;
    private Map<ExtensionLevel, List<CmisExtensionElement>> extensions;
    private OperationContext creationContext;
    private long refreshTimestamp;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Initializes the object.
     */
    protected void initialize(SessionImpl session, ObjectType objectType, ObjectData objectData,
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
            throw new IllegalArgumentException("Object type must have property definitions!");
        }

        this.session = session;
        this.objectType = objectType;
        this.extensions = new HashMap<ExtensionLevel, List<CmisExtensionElement>>();
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
    }

    /**
     * Acquires a write lock.
     */
    protected void writeLock() {
        lock.writeLock().lock();
    }

    /**
     * Releases a write lock.
     */
    protected void writeUnlock() {
        lock.writeLock().unlock();
    }

    /**
     * Acquires a read lock.
     */
    protected void readLock() {
        lock.readLock().lock();
    }

    /**
     * Releases a read lock.
     */
    protected void readUnlock() {
        lock.readLock().unlock();
    }

    /**
     * Returns the session object.
     */
    protected SessionImpl getSession() {
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
     * Returns the {@link OperationContext} that was used to create this object.
     */
    protected OperationContext getCreationContext() {
        return creationContext;
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

    // --- delete ---

    public void delete() {
        delete(true);
    }

    public void delete(boolean allVersions) {
        readLock();
        try {
            getSession().delete(this, allVersions);
        } finally {
            readUnlock();
        }
    }

    // --- update properties ---

    public CmisObject updateProperties(Map<String, ?> properties) {
        ObjectId objectId = updateProperties(properties, true);
        if (objectId == null) {
            return null;
        }

        if (!getObjectId().equals(objectId.getId())) {
            return getSession().getObject(objectId, getCreationContext());
        }

        return this;
    }

    public ObjectId updateProperties(Map<String, ?> properties, boolean refresh) {
        if (properties == null || properties.isEmpty()) {
            throw new IllegalArgumentException("Properties must not be empty!");
        }

        readLock();
        String newObjectId = null;
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

            newObjectId = objectIdHolder.getValue();

            // remove the object from the cache, it has been changed
            getSession().removeObjectFromCache(objectId);
        } finally {
            readUnlock();
        }

        if (refresh) {
            refresh();
        }

        if (newObjectId == null) {
            return null;
        }

        return getSession().createObjectId(newObjectId);
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
            return Collections.unmodifiableList(new ArrayList<Property<?>>(this.properties.values()));
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

    @SuppressWarnings("unchecked")
    public <T> T getPropertyValue(String id) {
        Property<T> property = getProperty(id);
        if (property == null) {
            return null;
        }
        // explicit cast needed by the Sun compiler
        return (T) property.getValue();
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
        Acl result = getSession().applyAcl(this, addAces, removeAces, aclPropagation);

        refresh();

        return result;
    }

    public Acl addAcl(List<Ace> addAces, AclPropagation aclPropagation) {
        return applyAcl(addAces, null, aclPropagation);
    }

    public Acl removeAcl(List<Ace> removeAces, AclPropagation aclPropagation) {
        return applyAcl(null, removeAces, aclPropagation);
    }

    public Acl setAcl(List<Ace> aces) {
        Acl result = getSession().setAcl(this, aces);

        refresh();

        return result;
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

    public void applyPolicy(ObjectId... policyIds) {
        readLock();
        try {
            getSession().applyPolicy(this, policyIds);
        } finally {
            readUnlock();
        }

        refresh();
    }

    public void removePolicy(ObjectId... policyIds) {
        readLock();
        try {
            getSession().removePolicy(this, policyIds);
        } finally {
            readUnlock();
        }

        refresh();
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

    // --- extensions ---

    public List<CmisExtensionElement> getExtensions(ExtensionLevel level) {
        List<CmisExtensionElement> ext = extensions.get(level);
        if (ext == null) {
            return null;
        }

        return Collections.unmodifiableList(ext);
    }

    // --- adapters ---

    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Class<T> adapterInterface) {
        if (adapterInterface == null) {
            return null;
        }
        if (adapterInterface.equals(TransientCmisObject.class)) {
            return (T) createTransientCmisObject();
        }
        return null;
    }

    public TransientCmisObject getTransientObject() {
        return getAdapter(TransientCmisObject.class);
    }

    protected TransientCmisObject createTransientCmisObject() {
        return null;
    }

    // --- other ---

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

            OperationContext oc = getCreationContext();

            // get the latest data from the repository
            ObjectData objectData = getSession()
                    .getBinding()
                    .getObjectService()
                    .getObject(getRepositoryId(), objectId, oc.getFilterString(), oc.isIncludeAllowableActions(),
                            oc.getIncludeRelationships(), oc.getRenditionFilterString(), oc.isIncludePolicies(),
                            oc.isIncludeAcls(), null);

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
}
