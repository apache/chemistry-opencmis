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
import java.util.List;
import java.util.Map;

import org.apache.opencmis.client.api.AclPermission;
import org.apache.opencmis.client.api.repository.AclPermissionMapping;
import org.apache.opencmis.commons.provider.PermissionMappingData;

public class AclPermissionMappingImpl implements AclPermissionMapping, Serializable {

  /**
   * serialization
   */
  private static final long serialVersionUID = -8682418497088386853L;

  /*
   * permission mapping key (serializable)
   */
  private String key;

  /*
   * permission list (serializable)
   */
  private List<AclPermission> permissionList;

  /**
   * Constructor.
   */
  public AclPermissionMappingImpl(PermissionMappingData pmd, Map<String, AclPermission> permissions) {
    this.key = pmd.getKey();
    this.permissionList = new ArrayList<AclPermission>();

    for (String permission : pmd.getPermissions()) {
      AclPermission aclPermission = permissions.get(permission);
      if (aclPermission == null) {
        aclPermission = new AclPermissionImpl(permission, permission);
      }

      this.permissionList.add(aclPermission);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.repository.AclPermissionMapping#getKey()
   */
  public String getKey() {
    return this.key;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.repository.AclPermissionMapping#getPermissions()
   */
  public List<AclPermission> getPermissions() {
    return permissionList;
  }
}
