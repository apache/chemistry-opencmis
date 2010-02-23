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

import org.apache.opencmis.client.api.AclPermission;
import org.apache.opencmis.client.api.repository.AclPermissionMapping;
import org.apache.opencmis.client.runtime.AclPermissionImpl;
import org.apache.opencmis.commons.provider.PermissionMappingData;

public class AclPermissionMappingImpl implements AclPermissionMapping,
		Serializable {

	/**
	 * serialization
	 */
	private static final long serialVersionUID = -8682418497088386853L;

	/*
	 * permission mapping data (serializable)
	 */
	private PermissionMappingData pmd = null;

	/*
	 * permission list (serializable)
	 */
	private List<AclPermission> permissionList = null;

	public AclPermissionMappingImpl(PermissionMappingData pmd) {
		this.pmd = pmd;
	}

	public String getKey() {
		return this.pmd.getKey();
	}

	public List<AclPermission> getPermissions() {
		if (this.permissionList == null) {
			this.permissionList = new ArrayList<AclPermission>();

			for (String descr : this.pmd.getPermissions()) {
				AclPermission acl = new AclPermissionImpl(descr);
				this.permissionList.add(acl);
			}
		}
		return permissionList;
	}

}
