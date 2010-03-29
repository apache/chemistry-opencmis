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
package org.apache.opencmis.client.runtime.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.opencmis.client.api.AclPermission;
import org.apache.opencmis.client.api.repository.AclPermissionMapping;
import org.apache.opencmis.client.api.repository.RepositoryAclCapabilities;
import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.enums.SupportedPermissions;
import org.apache.opencmis.commons.provider.AclCapabilitiesData;
import org.apache.opencmis.commons.provider.PermissionDefinitionData;
import org.apache.opencmis.commons.provider.PermissionMappingData;

public class RepositoryAclCapabilitiesImpl implements RepositoryAclCapabilities, Serializable {

  /*
   * serialization
   */
  private static final long serialVersionUID = 2824818352611088504L;

  /*
   * provider data (serializable)
   */
  private AclCapabilitiesData aclCapabilities;

  /*
   * permission mapping (serializable)
   */
  private Map<String, AclPermissionMapping> aclPermissionMapping = null;

  /*
   * permissions (serializable)
   */
  private List<AclPermission> aclPermissions = null;

  /**
   * Constructor.
   */
  public RepositoryAclCapabilitiesImpl(AclCapabilitiesData aclCapabilities) {
    this.aclCapabilities = aclCapabilities;
    this.aclPermissions = new ArrayList<AclPermission>();
    this.aclPermissionMapping = new HashMap<String, AclPermissionMapping>();

    if (this.aclCapabilities != null) {
      // copy permissions
      Map<String, AclPermission> permissionMap = new HashMap<String, AclPermission>();
      for (PermissionDefinitionData permission : this.aclCapabilities.getPermissionDefinitionData()) {
        AclPermission ap = new AclPermissionImpl(permission.getPermission(), permission
            .getDescription());
        aclPermissions.add(ap);
        permissionMap.put(ap.getName(), ap);
      }

      // copy mappings
      for (PermissionMappingData pmd : this.aclCapabilities.getPermissionMappingData()) {
        AclPermissionMapping apm = new AclPermissionMappingImpl(pmd, permissionMap);
        this.aclPermissionMapping.put(pmd.getKey(), apm);
      }
    }
  }

  public SupportedPermissions getSupportedPermissions() {
    return this.aclCapabilities.getSupportedPermissions();
  }

  public AclPropagation getAclPropagation() {
    return this.aclCapabilities.getAclPropagation();
  }

  public Map<String, AclPermissionMapping> getPermissionMapping() {
    return Collections.unmodifiableMap(this.aclPermissionMapping);
  }

  public List<AclPermission> getPermissions() {
    return Collections.unmodifiableList(this.aclPermissions);
  }

  public AclPermissionMapping getAddPolicyObjectPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_ADD_POLICY_OBJECT);
  }

  public AclPermissionMapping getAddPolicyPolicyPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_ADD_POLICY_POLICY);
  }

  public AclPermissionMapping getAddToFolderFolderPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_ADD_TO_FOLDER_FOLDER);
  }

  public AclPermissionMapping getAddToFolderObjectPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_ADD_TO_FOLDER_OBJECT);
  }

  public AclPermissionMapping getApplyAclObjectPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_APPLY_ACL_OBJECT);
  }

  public AclPermissionMapping getCancelCheckoutDocumentPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_CANCEL_CHECKOUT_DOCUMENT);
  }

  public AclPermissionMapping getCheckinDocumentPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_CHECKIN_DOCUMENT);
  }

  public AclPermissionMapping getCheckoutDocumentPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_CHECKOUT_DOCUMENT);
  }

  public AclPermissionMapping getCreateDocumentFolderPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_CREATE_DOCUMENT_FOLDER);
  }

  public AclPermissionMapping getCreateDocumentTypePermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_CREATE_DOCUMENT_TYPE);
  }

  public AclPermissionMapping getCreateFolderFolderPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_CREATE_FOLDER_FOLDER);
  }

  public AclPermissionMapping getCreateFolderTypePermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_CREATE_FOLDER_TYPE);
  }

  public AclPermissionMapping getCreatePolicyTypePermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_CREATE_POLICY_TYPE);
  }

  public AclPermissionMapping getCreateRelationshipSourcePermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_CREATE_RELATIONSHIP_SOURCE);
  }

  public AclPermissionMapping getCreateRelationshipTargetPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_CREATE_RELATIONSHIP_TARGET);
  }

  public AclPermissionMapping getCreateRelationshipTypePermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_CREATE_RELATIONSHIP_TYPE);
  }

  public AclPermissionMapping getDeleteContentDocumentPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_DELETE_CONTENT_DOCUMENT);
  }

  public AclPermissionMapping getDeleteObjectPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_DELETE_OBJECT);
  }

  public AclPermissionMapping getDeleteTreeFolderPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_DELETE_TREE_FOLDER);
  }

  public AclPermissionMapping getGetAclObjectPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_GET_ACL_OBJECT);
  }

  public AclPermissionMapping getGetAllVersionsVersionSeriesPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_GET_ALL_VERSIONS_VERSION_SERIES);
  }

  public AclPermissionMapping getGetAppliedPoliciesObjectPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_GET_APPLIED_POLICIES_OBJECT);
  }

  public AclPermissionMapping getGetChildrenFolderPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_GET_CHILDREN_FOLDER);
  }

  public AclPermissionMapping getGetDescendentsFolderPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_GET_DESCENDENTS_FOLDER);
  }

  public AclPermissionMapping getGetFolderParentObjectPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_GET_FOLDER_PARENT_OBJECT);
  }

  public AclPermissionMapping getGetObjectRelationshipsObjectPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_GET_OBJECT_RELATIONSHIPS_OBJECT);
  }

  public AclPermissionMapping getGetParentsFolderPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_GET_PARENTS_FOLDER);
  }

  public AclPermissionMapping getGetPropertiesObjectPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_GET_PROPERTIES_OBJECT);
  }

  public AclPermissionMapping getMoveObjectPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_MOVE_OBJECT);
  }

  public AclPermissionMapping getMoveSourcePermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_MOVE_SOURCE);
  }

  public AclPermissionMapping getMoveTargetPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_MOVE_TARGET);
  }

  public AclPermissionMapping getRemoveFromFolderFolderPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_REMOVE_FROM_FOLDER_FOLDER);
  }

  public AclPermissionMapping getRemoveFromFolderObjectPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_REMOVE_FROM_FOLDER_OBJECT);
  }

  public AclPermissionMapping getRemovePolicyObjectPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_REMOVE_POLICY_OBJECT);
  }

  public AclPermissionMapping getRemovePolicyPolicyPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_REMOVE_POLICY_POLICY);
  }

  public AclPermissionMapping getSetContentDocumentPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_SET_CONTENT_DOCUMENT);
  }

  public AclPermissionMapping getUpdatePropertiesObjectPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT);
  }

  public AclPermissionMapping getViewContentObjectPermissions() {
    return this.aclPermissionMapping.get(AclPermissionMapping.CAN_VIEW_CONTENT_OBJECT);
  }
}
