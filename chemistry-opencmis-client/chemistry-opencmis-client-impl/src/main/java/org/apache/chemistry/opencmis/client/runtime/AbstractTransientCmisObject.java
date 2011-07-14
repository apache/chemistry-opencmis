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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectFactory;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.TransientCmisObject;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ExtensionLevel;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.apache.chemistry.opencmis.commons.spi.Holder;

public abstract class AbstractTransientCmisObject implements TransientCmisObject {

    protected Session session;
    protected CmisObject object;

    protected Map<String, Property<?>> properties;
    protected AllowableActions allowableActions;
    protected List<Rendition> renditions;
    protected Acl acl;
    protected Map<AclPropagation, List<AceChangeHolder>> addAces;
    protected Map<AclPropagation, List<AceChangeHolder>> removeAces;
    protected List<Policy> policies;
    protected Set<String> addPolicies;
    protected Set<String> removePolicies;
    protected List<Relationship> relationships;
    protected Map<ExtensionLevel, List<CmisExtensionElement>> inputExtensions;
    protected Map<ExtensionLevel, List<CmisExtensionElement>> ouputExtensions;

    protected boolean isModified;
    protected boolean isPropertyUpdateRequired;
    protected boolean isMarkedForDelete;
    protected boolean deleteAllVersions;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void initialize(Session session, CmisObject object) {
        this.session = session;
        this.object = object;

        ObjectFactory of = getObjectFactory();

        // --- create snapshot ---

        // properties (modifiable)
        properties = new LinkedHashMap<String, Property<?>>();
        for (Property<?> property : object.getProperties()) {
            properties.put(property.getId(),
                    of.createProperty(property.getDefinition(), new ArrayList(property.getValues())));
        }
        isPropertyUpdateRequired = false;

        // allowable actions (unmodifiable)
        allowableActions = object.getAllowableActions();

        // policies (modifiable)
        policies = new ArrayList<Policy>();
        if (object.getPolicies() != null) {
            policies.addAll(object.getPolicies());
        }
        addPolicies = new HashSet<String>();
        removePolicies = new HashSet<String>();

        // ACL (unmodifiable)
        acl = object.getAcl();
        addAces = new HashMap<AclPropagation, List<AceChangeHolder>>();
        removeAces = new HashMap<AclPropagation, List<AceChangeHolder>>();

        // relationships (unmodifiable)
        relationships = object.getRelationships();

        // renditions (unmodifiable)
        renditions = object.getRenditions();

        // input extensions (unmodifiable)
        inputExtensions = new HashMap<ExtensionLevel, List<CmisExtensionElement>>();
        for (ExtensionLevel level : ExtensionLevel.values()) {
            List<CmisExtensionElement> extension = object.getExtensions(level);
            if (extension != null) {
                inputExtensions.put(level, extension);
            }
        }

        // output extensions (modifiable)
        ouputExtensions = new HashMap<ExtensionLevel, List<CmisExtensionElement>>();

        isModified = false;
        deleteAllVersions = true;
        isMarkedForDelete = false;
    }

    public CmisObject getCmisObject() {
        return object;
    }

    /**
     * Returns the session object.
     */
    protected Session getSession() {
        return this.session;
    }

    /**
     * Returns the repository id.
     */
    protected String getRepositoryId() {
        return getSession().getRepositoryInfo().getId();
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

    protected ObjectId getObjectId() {
        return getSession().createObjectId(getId());
    }

    public ObjectType getBaseType() {
        return object.getBaseType();
    }

    public BaseTypeId getBaseTypeId() {
        return object.getBaseTypeId();
    }

    public ObjectType getType() {
        return object.getType();
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

    public void setName(String name) {
        setPropertyValue(PropertyIds.NAME, name);
    }

    public List<Property<?>> getProperties() {
        return Collections.unmodifiableList(new ArrayList<Property<?>>(this.properties.values()));
    }

    @SuppressWarnings("unchecked")
    public <T> Property<T> getProperty(String id) {
        return (Property<T>) this.properties.get(id);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPropertyValue(String id) {
        Property<T> property = getProperty(id);
        if (property == null) {
            return null;
        }

        return (T) property.getValue();
    }

    @SuppressWarnings("unchecked")
    public <T> void setPropertyValue(String id, Object value) {
        PropertyDefinition<T> propertyDefinition = (PropertyDefinition<T>) getType().getPropertyDefinitions().get(id);
        if (propertyDefinition == null) {
            throw new IllegalArgumentException("Unknown property '" + id + "'!");
        }
        // check updatability
        if (propertyDefinition.getUpdatability() == Updatability.READONLY) {
            throw new IllegalArgumentException("Property is read-only!");
        }

        List<T> values = checkProperty(propertyDefinition, value);

        // create and set property
        Property<T> newProperty = getObjectFactory().createProperty(propertyDefinition, values);
        properties.put(id, newProperty);

        isPropertyUpdateRequired = true;
        isModified = true;
    }

    public AllowableActions getAllowableActions() {
        return allowableActions;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public List<Rendition> getRenditions() {
        return renditions;
    }

    public void delete(boolean allVersions) {
        deleteAllVersions = allVersions;
        isMarkedForDelete = true;
        isModified = true;
    }

    public void applyPolicy(Policy... policyIds) {
        for (Policy policy : policyIds) {
            if ((policy != null) && (policy.getId() != null)) {
                addPolicies.add(policy.getId());
                addPolicyToPolicyList(policy);
            }
        }
    }

    public void removePolicy(Policy... policyIds) {
        for (Policy policy : policyIds) {
            if ((policy != null) && (policy.getId() != null)) {
                removePolicies.add(policy.getId());
                removePolicyFromPolicyList(policy);
            }
        }
    }

    public List<Policy> getPolicies() {
        return policies;
    }

    private void addPolicyToPolicyList(Policy policy) {
        for (Policy p : policies) {
            if (policy.getId().equals(p.getId())) {
                return;
            }
        }

        policies.add(policy);
    }

    private void removePolicyFromPolicyList(Policy policy) {
        Iterator<Policy> iter = policies.iterator();
        while (iter.hasNext()) {
            Policy p = iter.next();
            if (policy.getId().equals(p.getId())) {
                iter.remove();
            }
        }
    }

    public Acl getOriginalAcl() {
        return acl;
    }

    public void addAce(String principalId, List<String> permissions, AclPropagation aclPropagation) {
        AceChangeHolder ach = new AceChangeHolder(principalId, permissions, aclPropagation);

        List<AceChangeHolder> list = addAces.get(aclPropagation);
        if (list == null) {
            list = new ArrayList<AbstractTransientCmisObject.AceChangeHolder>();
            addAces.put(aclPropagation, list);
        }

        list.add(ach);
    }

    public void removeAce(String principalId, List<String> permissions, AclPropagation aclPropagation) {
        AceChangeHolder ach = new AceChangeHolder(principalId, permissions, aclPropagation);

        List<AceChangeHolder> list = removeAces.get(aclPropagation);
        if (list == null) {
            list = new ArrayList<AbstractTransientCmisObject.AceChangeHolder>();
            removeAces.put(aclPropagation, list);
        }

        list.add(ach);
    }

    public List<CmisExtensionElement> getInputExtensions(ExtensionLevel level) {
        return inputExtensions.get(level);
    }

    public List<CmisExtensionElement> getOutputExtensions(ExtensionLevel level) {
        return ouputExtensions.get(level);
    }

    public void setOutputExtensions(ExtensionLevel level, List<CmisExtensionElement> extensions) {
        ouputExtensions.put(level, extensions);
    }

    public boolean isMarkedForDelete() {
        return isMarkedForDelete;
    }

    public boolean isModified() {
        return isModified;
    }

    public void reset() {
        initialize(session, object);
    }

    public void refreshAndReset() {
        object.refresh();
        reset();
    }

    public ObjectId save() {
        if (!isModified()) {
            // nothing has change, so there is nothing to do
            return getObjectId();
        }

        String objectId = getId();

        if (saveDelete(objectId)) {
            // object has been deleted, there is nothing else to do
            // ... and there is no object id anymore
            return null;
        }
        String newObjectId = saveProperties(getId(), getChangeToken());
        saveACL(newObjectId);
        savePolicies(newObjectId);

        return getSession().createObjectId(newObjectId);
    }

    /**
     * Fetches the latest change token of this object from the repository.
     */
    protected String getLatestChangeToken(String objectId) {
        // determine the object id query name
        PropertyDefinition<?> objectIdPropDef = getCmisObject().getType().getPropertyDefinitions()
                .get(PropertyIds.OBJECT_ID);
        if (objectIdPropDef == null) {
            return null;
        }

        String objectIdQueryName = objectIdPropDef.getQueryName();
        if (objectIdQueryName == null) {
            return null;
        }

        // determine the change token query name
        PropertyDefinition<?> changeTokenPropDef = getCmisObject().getType().getPropertyDefinitions()
                .get(PropertyIds.CHANGE_TOKEN);
        if (changeTokenPropDef == null) {
            return null;
        }

        String changeTokenQueryName = changeTokenPropDef.getQueryName();
        if (changeTokenQueryName == null) {
            return null;
        }

        // get the change token property
        Properties properties = getBinding().getObjectService().getProperties(getRepositoryId(), objectId,
                objectIdQueryName + "," + changeTokenQueryName, null);

        // if a change token is set, return it
        PropertyData<?> changeToken = properties.getProperties().get(PropertyIds.CHANGE_TOKEN);

        if ((changeToken == null) || (changeToken.getFirstValue() == null)) {
            return null;
        }

        return changeToken.getFirstValue().toString();
    }

    protected boolean saveDelete(String objectId) {
        if (isMarkedForDelete) {
            getBinding().getObjectService().deleteObject(getRepositoryId(), objectId, deleteAllVersions, null);
            return true;
        }

        return false;
    }

    protected Properties prepareProperties() {
        Set<Updatability> updatebility = new HashSet<Updatability>();
        updatebility.add(Updatability.READWRITE);

        // check if checked out
        Boolean isCheckedOut = getPropertyValue(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT);
        if ((isCheckedOut != null) && isCheckedOut.booleanValue()) {
            updatebility.add(Updatability.WHENCHECKEDOUT);
        }

        // convert properties
        Properties result = getObjectFactory().convertProperties(properties, getType(), updatebility);

        // extensions
        List<CmisExtensionElement> extensions = ouputExtensions.get(ExtensionLevel.PROPERTIES);
        if (extensions != null) {
            result.setExtensions(extensions);
        }

        return result;
    }

    protected String saveProperties(String objectId, String changeToken) {
        if (isPropertyUpdateRequired) {
            Holder<String> objectIdHolder = new Holder<String>(objectId);
            Holder<String> changeTokenHolder = new Holder<String>(changeToken);

            // convert properties
            Properties props = prepareProperties();

            // it's time to update
            getBinding().getObjectService().updateProperties(getRepositoryId(), objectIdHolder, changeTokenHolder,
                    props, null);

            if (objectIdHolder.getValue() != null) {
                return objectIdHolder.getValue();
            }
        }

        return objectId;
    }

    protected void savePolicies(String objectId) {
        // add policies
        for (String policyId : addPolicies) {
            getBinding().getPolicyService().applyPolicy(getRepositoryId(), policyId, objectId, null);
        }

        // remove policies
        for (String policyId : removePolicies) {
            getBinding().getPolicyService().removePolicy(getRepositoryId(), policyId, objectId, null);
        }
    }

    protected Acl prepareAcl(List<AceChangeHolder> achList) {
        if ((achList == null) || (achList.isEmpty())) {
            return null;
        }

        ObjectFactory of = getObjectFactory();

        List<Ace> aces = new ArrayList<Ace>();
        for (AceChangeHolder ach : achList) {
            aces.add(of.createAce(ach.getPrincipalId(), ach.getPermissions()));
        }

        return of.createAcl(aces);
    }

    protected void saveACL(String objectId) {
        for (AclPropagation ap : AclPropagation.values()) {
            if (!addAces.containsKey(ap) && !removeAces.containsKey(ap)) {
                continue;
            }

            getBinding().getAclService().applyAcl(getRepositoryId(), objectId, prepareAcl(addAces.get(ap)),
                    prepareAcl(removeAces.get(ap)), ap, null);
        }

        if (addAces.containsKey(null) || removeAces.containsKey(null)) {
            getBinding().getAclService().applyAcl(getRepositoryId(), objectId, prepareAcl(addAces.get(null)),
                    prepareAcl(removeAces.get(null)), null, null);
        }
    }

    // --- internal ---

    /**
     * Checks if a value matches a property definition.
     * <p>
     * Returns a list of values.
     */
    @SuppressWarnings("unchecked")
    private static <T> List<T> checkProperty(PropertyDefinition<T> propertyDefinition, Object value) {

        // null values are ok for updates
        if (value == null) {
            return null;
        }

        // single and multi value check
        List<T> values = null;
        if (value instanceof List<?>) {
            if (propertyDefinition.getCardinality() != Cardinality.MULTI) {
                throw new IllegalArgumentException("Property '" + propertyDefinition.getId()
                        + "' is not a multi value property!");
            }

            values = (List<T>) value;
            if (values.isEmpty()) {
                return values;
            }
        } else {
            if (propertyDefinition.getCardinality() != Cardinality.SINGLE) {
                throw new IllegalArgumentException("Property '" + propertyDefinition.getId()
                        + "' is not a single value property!");
            }

            values = Collections.singletonList((T) value);
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
            typeMatch = (firstValue instanceof BigInteger) || (firstValue instanceof Byte)
                    || (firstValue instanceof Short) || (firstValue instanceof Integer) || (firstValue instanceof Long);
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

        return values;
    }

    // --- ACE helper class ---

    public static class AceChangeHolder implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String principalId;
        private final List<String> permissions;
        private final AclPropagation aclPropagation;

        public AceChangeHolder(String principalId, List<String> permissions, AclPropagation aclPropagation) {
            if ((principalId == null) || (principalId.length() == 0)) {
                throw new IllegalArgumentException("Principal id must be set!");
            }

            if ((permissions == null) || (permissions.size() == 0)) {
                throw new IllegalArgumentException("Permissions id must be set!");
            }

            this.principalId = principalId;
            this.permissions = permissions;
            this.aclPropagation = aclPropagation;
        }

        public String getPrincipalId() {
            return principalId;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public AclPropagation getAclPropagation() {
            return aclPropagation;
        }
    }
}
