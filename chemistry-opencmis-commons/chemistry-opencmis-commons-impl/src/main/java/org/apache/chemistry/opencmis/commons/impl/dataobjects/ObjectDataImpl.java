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
package org.apache.chemistry.opencmis.commons.impl.dataobjects;

import java.util.List;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.api.Acl;
import org.apache.chemistry.opencmis.commons.api.AllowableActions;
import org.apache.chemistry.opencmis.commons.api.ChangeEventInfoData;
import org.apache.chemistry.opencmis.commons.api.ObjectData;
import org.apache.chemistry.opencmis.commons.api.PolicyIdListData;
import org.apache.chemistry.opencmis.commons.api.PropertiesData;
import org.apache.chemistry.opencmis.commons.api.PropertyData;
import org.apache.chemistry.opencmis.commons.api.RenditionData;
import org.apache.chemistry.opencmis.commons.enums.BaseObjectTypeIds;

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
  private AllowableActions fAllowableActions;
  private Acl fAcl;
  private Boolean fIsExactAcl;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectData#getId()
   */
  public String getId() {
    Object value = getFirstValue(PropertyIds.OBJECT_ID);
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
    Object value = getFirstValue(PropertyIds.BASE_TYPE_ID);
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
  public AllowableActions getAllowableActions() {
    return fAllowableActions;
  }

  public void setAllowableActions(AllowableActions allowableActions) {
    fAllowableActions = allowableActions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectData#getACL()
   */
  public Acl getAcl() {
    return fAcl;
  }

  public void setAcl(Acl acl) {
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
