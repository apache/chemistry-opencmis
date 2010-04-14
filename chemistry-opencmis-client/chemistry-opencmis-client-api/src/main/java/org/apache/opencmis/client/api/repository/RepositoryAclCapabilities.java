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
package org.apache.opencmis.client.api.repository;

import java.util.List;
import java.util.Map;

import org.apache.opencmis.client.api.AclPermission;
import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.enums.SupportedPermissions;

/**
 * Information about a repositories ACL capabilities, as provided by RepositoryInfo.
 * 
 * @see RepositoryInfo#getAclCapabilities()
 * 
 *      See CMIS Domain Model - section 2.2.2.2.
 */
public interface RepositoryAclCapabilities {

  SupportedPermissions getSupportedPermissions();
  
  AclPropagation getAclPropagation();

  List<AclPermission> getPermissions();

  // generic mapping

  /**
   * The methods {@code get}&lt;<i>AllowableAction</i>&gt;&lt;<i>Operand</i>&gt;{@code Permissions}
   * should be used instead...
   */
  Map<String, AclPermissionMapping> getPermissionMapping();

  // specific mappings

  AclPermissionMapping getGetDescendentsFolderPermissions();

  AclPermissionMapping getGetChildrenFolderPermissions();

  AclPermissionMapping getGetParentsFolderPermissions();

  AclPermissionMapping getGetFolderParentObjectPermissions();

  AclPermissionMapping getCreateDocumentTypePermissions();

  AclPermissionMapping getCreateDocumentFolderPermissions();

  AclPermissionMapping getCreateFolderTypePermissions();

  AclPermissionMapping getCreateFolderFolderPermissions();

  AclPermissionMapping getCreateRelationshipTypePermissions();

  AclPermissionMapping getCreateRelationshipSourcePermissions();

  AclPermissionMapping getCreateRelationshipTargetPermissions();

  AclPermissionMapping getCreatePolicyTypePermissions();

  AclPermissionMapping getGetPropertiesObjectPermissions();

  AclPermissionMapping getViewContentObjectPermissions();

  AclPermissionMapping getUpdatePropertiesObjectPermissions();

  AclPermissionMapping getMoveObjectPermissions();

  AclPermissionMapping getMoveTargetPermissions();

  AclPermissionMapping getMoveSourcePermissions();

  AclPermissionMapping getDeleteObjectPermissions();

  AclPermissionMapping getDeleteTreeFolderPermissions();

  AclPermissionMapping getSetContentDocumentPermissions();

  AclPermissionMapping getDeleteContentDocumentPermissions();

  AclPermissionMapping getAddToFolderObjectPermissions();

  AclPermissionMapping getAddToFolderFolderPermissions();

  AclPermissionMapping getRemoveFromFolderObjectPermissions();

  AclPermissionMapping getRemoveFromFolderFolderPermissions();

  AclPermissionMapping getCheckoutDocumentPermissions();

  AclPermissionMapping getCancelCheckoutDocumentPermissions();

  AclPermissionMapping getCheckinDocumentPermissions();

  AclPermissionMapping getGetAllVersionsVersionSeriesPermissions();

  AclPermissionMapping getGetObjectRelationshipsObjectPermissions();

  AclPermissionMapping getAddPolicyObjectPermissions();

  AclPermissionMapping getAddPolicyPolicyPermissions();

  AclPermissionMapping getRemovePolicyObjectPermissions();

  AclPermissionMapping getRemovePolicyPolicyPermissions();

  AclPermissionMapping getGetAppliedPoliciesObjectPermissions();

  AclPermissionMapping getGetAclObjectPermissions();

  AclPermissionMapping getApplyAclObjectPermissions();

}
