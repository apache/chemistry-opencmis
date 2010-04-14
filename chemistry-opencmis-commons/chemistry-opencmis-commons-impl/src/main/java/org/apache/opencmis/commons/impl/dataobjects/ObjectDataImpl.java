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
package org.apache.opencmis.commons.impl.dataobjects;

import java.util.List;

import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.AllowableActionsData;
import org.apache.opencmis.commons.provider.ChangeEventInfoData;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PolicyIdListData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.RenditionData;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class ObjectDataImpl extends AbstractExtensionData implements ObjectData {

  private PropertiesData fProperties;
  private ChangeEventInfoData fChangeEventInfo;
  private List<ObjectData> fRelationships;
  private List<RenditionData> fRenditions;
  private PolicyIdListData fPolicyIds;
  private AllowableActionsData fAllowableActions;
  private AccessControlList fAcl;
  private Boolean fIsExactAcl;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectData#getId()
   */
  public String getId() {
    Object value = getFirstValue(PropertyIds.CMIS_OBJECT_ID);
    if (value instanceof String) {
      return (String) value;
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectData#getBaseTypeId()
   */
  public BaseObjectTypeIds getBaseTypeId() {
    Object value = getFirstValue(PropertyIds.CMIS_BASE_TYPE_ID);
    if (value instanceof String) {
      try {
        return BaseObjectTypeIds.fromValue((String) value);
      }
      catch (Exception e) {
      }
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectData#getProperties()
   */
  public PropertiesData getProperties() {
    return fProperties;
  }

  public void setProperties(PropertiesData properties) {
    fProperties = properties;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectData#getChangeEventInfo()
   */
  public ChangeEventInfoData getChangeEventInfo() {
    return fChangeEventInfo;
  }

  public void setChangeEventInfo(ChangeEventInfoData changeEventInfo) {
    fChangeEventInfo = changeEventInfo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectData#getRelationships()
   */
  public List<ObjectData> getRelationships() {
    return fRelationships;
  }

  public void setRelationships(List<ObjectData> relationships) {
    fRelationships = relationships;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectData#getRenditions()
   */
  public List<RenditionData> getRenditions() {
    return fRenditions;
  }

  public void setRenditions(List<RenditionData> renditions) {
    fRenditions = renditions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectData#getPolicyIds()
   */
  public PolicyIdListData getPolicyIds() {
    return fPolicyIds;
  }

  public void setPolicyIds(PolicyIdListData policyIds) {
    fPolicyIds = policyIds;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectData#getAllowableActions()
   */
  public AllowableActionsData getAllowableActions() {
    return fAllowableActions;
  }

  public void setAllowableActions(AllowableActionsData allowableActions) {
    fAllowableActions = allowableActions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectData#getACL()
   */
  public AccessControlList getAcl() {
    return fAcl;
  }

  public void setAcl(AccessControlList acl) {
    fAcl = acl;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectData#isExactACL()
   */
  public Boolean isExactAcl() {
    return fIsExactAcl;
  }

  public void setIsExactAcl(Boolean isExactACL) {
    fIsExactAcl = isExactACL;
  }

  // ---- internal ----

  /**
   * Returns the first value of a property or <code>null</code> if the property is not set.
   */
  private Object getFirstValue(String id) {
    if ((fProperties == null) || (fProperties.getProperties() == null)) {
      return null;
    }

    PropertyData<?> property = fProperties.getProperties().get(id);
    if (property == null) {
      return null;
    }

    return property.getFirstValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Object Data [properties=" + fProperties + ", allowable actions=" + fAllowableActions
        + ", change event info=" + fChangeEventInfo + ", ACL=" + fAcl + ", is exact ACL="
        + fIsExactAcl + ", policy ids=" + fPolicyIds + ", relationships=" + fRelationships
        + ", renditions=" + fRenditions + "]" + super.toString();
  }
}
