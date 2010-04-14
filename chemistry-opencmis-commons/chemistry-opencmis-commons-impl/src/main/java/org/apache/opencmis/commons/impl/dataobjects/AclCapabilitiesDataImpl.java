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

import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.enums.SupportedPermissions;
import org.apache.opencmis.commons.provider.AclCapabilitiesData;
import org.apache.opencmis.commons.provider.PermissionDefinitionData;
import org.apache.opencmis.commons.provider.PermissionMappingData;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AclCapabilitiesDataImpl extends AbstractExtensionData implements AclCapabilitiesData {

  private static final long serialVersionUID = 1L;

  private SupportedPermissions fSupportedPermissions;
  private AclPropagation fACLPropagation;
  private List<PermissionMappingData> fPermissionMappingList;
  private List<PermissionDefinitionData> fPermissionDefinitionList;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ACLCapabilitiesData#getACLPropagation()
   */
  public SupportedPermissions getSupportedPermissions() {
    return fSupportedPermissions;
  }

  public void setSupportedPermissions(SupportedPermissions supportedPermissions) {
    fSupportedPermissions = supportedPermissions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ACLCapabilitiesData#getACLPropagation()
   */
  public AclPropagation getAclPropagation() {
    return fACLPropagation;
  }

  public void setAclPropagation(AclPropagation aclPropagation) {
    fACLPropagation = aclPropagation;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ACLCapabilitiesData#getPermissionMappingData()
   */
  public List<PermissionMappingData> getPermissionMappingData() {
    return fPermissionMappingList;
  }

  public void setPermissionMappingData(List<PermissionMappingData> permissionMappingList) {
    fPermissionMappingList = permissionMappingList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ACLCapabilitiesData#getPermissionDefinitionData()
   */
  public List<PermissionDefinitionData> getPermissionDefinitionData() {
    return fPermissionDefinitionList;
  }

  public void setPermissionDefinitionData(List<PermissionDefinitionData> permissionDefinitionList) {
    fPermissionDefinitionList = permissionDefinitionList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ACL Capabilities [ACL propagation=" + fACLPropagation + ", permission definition list="
        + fPermissionDefinitionList + ", permission mappings=" + fPermissionMappingList + "]"
        + super.toString();
  }
}
