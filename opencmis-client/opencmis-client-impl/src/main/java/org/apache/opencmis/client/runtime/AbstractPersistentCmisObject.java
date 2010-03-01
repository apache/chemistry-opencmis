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

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.opencmis.client.api.Ace;
import org.apache.opencmis.client.api.Acl;
import org.apache.opencmis.client.api.AllowableActions;
import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.Policy;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.Relationship;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.enums.RelationshipDirection;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.CmisProvider;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;

/**
 * Base class for all persistent session object impl classes.
 */
public abstract class AbstractPersistentCmisObject implements CmisObject {

  private PersistentSessionImpl session;
  private ObjectType objectType;
  private Map<String, Property<?>> properties;
  private AllowableActions allowableActions;
  private Acl acl;
  private List<Policy> policies;
  private boolean isChanged = false;

  /**
   * Initializes the object.
   */
  protected void initialize(PersistentSessionImpl session, ObjectType objectType,
      ObjectData objectData) {
    if (session == null) {
      throw new IllegalArgumentException("Session must be set!");
    }

    if (objectType == null) {
      throw new IllegalArgumentException("Object type must be set!");
    }

    this.session = session;
    this.objectType = objectType;

    if (objectData != null) {
      // handle properties
      if (objectData.getProperties() != null) {
        this.properties = SessionUtil.convertProperties(session, objectType, objectData
            .getProperties());
      }

      // handle allowable actions
      if (objectData.getAllowableActions() != null) {
        this.allowableActions = SessionUtil.convertAllowableActions(getSession(), objectData
            .getAllowableActions());
      }

      // handle ACL
      if (objectData.getAcl() != null) {
        acl = SessionUtil.convertAcl(getSession(), objectData.getAcl());
      }

      // handle policies
      if ((objectData.getPolicyIds() != null) && (objectData.getPolicyIds().getPolicyIds() != null)) {
        policies = new ArrayList<Policy>();
        for (String pid : objectData.getPolicyIds().getPolicyIds()) {
          CmisObject policy = session.getObject(pid);
          if (policy instanceof Policy) {
            policies.add((Policy) policy);
          }
        }
      }
    }
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
    return this.objectType;
  }

  /**
   * Returns the provider object.
   */
  protected CmisProvider getProvider() {
    return getSession().getProvider();
  }

  /**
   * Returns the id of this object or throws an exception if the id is unknown.
   */
  protected String getObjectId() {
    String objectId = getId();
    if (objectId == null) {
      throw new IllegalStateException("Object Id is unknown");
    }

    return objectId;
  }

  // --- operations ---

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#delete(boolean)
   */
  public void delete(boolean allVersions) {
    String objectId = getObjectId();
    getProvider().getObjectService().deleteObject(getRepositoryId(), objectId, allVersions, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#updateProperties()
   */
  public void updateProperties() {
    String objectId = getObjectId();
    Holder<String> objectIdHolder = new Holder<String>(objectId);

    String changeToken = getChangeToken();
    Holder<String> changeTokenHolder = new Holder<String>(changeToken);

    getProvider().getObjectService().updateProperties(getRepositoryId(), objectIdHolder,
        changeTokenHolder, SessionUtil.convertProperties(getSession(), properties.values()), null);
  }

  // --- properties ---

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getBaseType()
   */
  public ObjectType getBaseType() {
    BaseObjectTypeIds baseTypeId = getBaseTypeId();
    if (baseTypeId == null) {
      return null;
    }

    return getSession().getTypeDefinition(baseTypeId.value());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getBaseTypeId()
   */
  public BaseObjectTypeIds getBaseTypeId() {
    return getPropertyValue(PropertyIds.CMIS_BASE_TYPE_ID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getChangeToken()
   */
  public String getChangeToken() {
    return getPropertyValue(PropertyIds.CMIS_CHANGE_TOKEN);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getCreatedBy()
   */
  public String getCreatedBy() {
    return getPropertyValue(PropertyIds.CMIS_CREATED_BY);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getCreationDate()
   */
  public GregorianCalendar getCreationDate() {
    return getPropertyValue(PropertyIds.CMIS_CREATION_DATE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getId()
   */
  public String getId() {
    return getPropertyValue(PropertyIds.CMIS_OBJECT_ID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getLastModificationDate()
   */
  public GregorianCalendar getLastModificationDate() {
    return getPropertyValue(PropertyIds.CMIS_LAST_MODIFICATION_DATE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getLastModifiedBy()
   */
  public String getLastModifiedBy() {
    return getPropertyValue(PropertyIds.CMIS_LAST_MODIFIED_BY);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getName()
   */
  public String getName() {
    return getPropertyValue(PropertyIds.CMIS_NAME);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getProperties()
   */
  public List<Property<?>> getProperties() {
    return new ArrayList<Property<?>>(properties.values());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getProperty(java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public <T> Property<T> getProperty(String id) {
    return (Property<T>) properties.get(id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getPropertyMultivalue(java.lang.String)
   */
  public <T> List<T> getPropertyMultivalue(String id) {
    Property<T> property = getProperty(id);
    if (property == null) {
      return null;
    }

    return property.getValues();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getPropertyValue(java.lang.String)
   */
  public <T> T getPropertyValue(String id) {
    Property<T> property = getProperty(id);
    if (property == null) {
      return null;
    }

    return property.getValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#setName(java.lang.String)
   */
  public void setName(String name) {
    setProperty(PropertyIds.CMIS_NAME, name);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#setProperty(java.lang.String, java.lang.Object)
   */
  public <T> void setProperty(String id, T value) {
    // TODO Auto-generated method stub
    throw new CmisRuntimeException("not implemented");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#setPropertyMultivalue(java.lang.String,
   * java.util.List)
   */
  public <T> void setPropertyMultivalue(String id, List<T> value) {
    // TODO Auto-generated method stub
    throw new CmisRuntimeException("not implemented");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.api.CmisObject#setType(org.apache.opencmis.client.api.objecttype
   * .ObjectType)
   */
  public void setType(ObjectType type) {
    // TODO Auto-generated method stub
    throw new CmisRuntimeException("not implemented");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#setTypeId(java.lang.String)
   */
  public void setTypeId(String typeId) {
    // TODO Auto-generated method stub
    throw new CmisRuntimeException("not implemented");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getType()
   */
  public ObjectType getType() {
    return this.objectType;
  }

  // --- allowable actions ---

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getAllowableActions()
   */
  public AllowableActions getAllowableActions() {
    return this.allowableActions;
  }

  // --- ACL ---

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getAcl(boolean)
   */
  public Acl getAcl(boolean onlyBasicPermissions) {
    String objectId = getObjectId();
    return SessionUtil.convertAcl(getSession(), getProvider().getAclService().getAcl(
        getRepositoryId(), objectId, onlyBasicPermissions, null));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#applyAcl(java.util.List, java.util.List,
   * org.apache.opencmis.commons.enums.AclPropagation)
   */
  public Acl applyAcl(List<Ace> addAces, List<Ace> removeAces, AclPropagation aclPropagation) {
    String objectId = getObjectId();
    return SessionUtil.convertAcl(getSession(), getProvider().getAclService().applyAcl(
        getRepositoryId(), objectId, SessionUtil.convertAces(getSession(), addAces),
        SessionUtil.convertAces(getSession(), removeAces), aclPropagation, null));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#addAcl(java.util.List,
   * org.apache.opencmis.commons.enums.AclPropagation)
   */
  public void addAcl(List<Ace> addAces, AclPropagation aclPropagation) {
    applyAcl(addAces, null, aclPropagation);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#removeAcl(java.util.List,
   * org.apache.opencmis.commons.enums.AclPropagation)
   */
  public void removeAcl(List<Ace> removeAces, AclPropagation aclPropagation) {
    applyAcl(null, removeAces, aclPropagation);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getAcl()
   */
  public Acl getAcl() {
    return this.acl;
  }

  // --- policies ---

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.api.CmisObject#applyPolicy(org.apache.opencmis.client.api.Policy)
   */
  public void applyPolicy(Policy policy) {
    if (policy == null) {
      throw new IllegalArgumentException("Policy is not set!");
    }

    applyPolicy(policy.getId());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#applyPolicy(java.lang.String)
   */
  public void applyPolicy(String policyId) {
    if (policyId == null) {
      throw new IllegalArgumentException("Policy id is not set!");
    }

    String objectId = getObjectId();
    getProvider().getPolicyService().applyPolicy(getRepositoryId(), policyId, objectId, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.api.CmisObject#removePolicy(org.apache.opencmis.client.api.Policy)
   */
  public void removePolicy(Policy policy) {
    if (policy == null) {
      throw new IllegalArgumentException("Policy is not set!");
    }

    removePolicy(policy.getId());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#removePolicy(java.lang.String)
   */
  public void removePolicy(String policyId) {
    if (policyId == null) {
      throw new IllegalArgumentException("Policy id is not set!");
    }

    String objectId = getObjectId();
    getProvider().getPolicyService().removePolicy(getRepositoryId(), policyId, objectId, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#getPolicies()
   */
  public List<Policy> getPolicies() {
    return policies;
  }

  // --- relationships ---

  public List<Relationship> getRelationships() {
    // TODO Auto-generated method stub
    throw new CmisRuntimeException("not implemented");
  }

  public List<Relationship> getRelationships(boolean includeSubRelationshipTypes,
      RelationshipDirection relationshipDirection, ObjectType type) {
    // TODO Auto-generated method stub
    throw new CmisRuntimeException("not implemented");
  }

  // --- other ---

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.CmisObject#isChanged()
   */
  public boolean isChanged() {
    return isChanged;
  }

  /**
   * Sets the isChanged flag to <code>true</code>
   */
  protected void setChanged() {
    isChanged = true;
  }
}
