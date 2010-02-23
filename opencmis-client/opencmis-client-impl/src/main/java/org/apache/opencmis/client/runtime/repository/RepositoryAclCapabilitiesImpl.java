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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.opencmis.client.api.AclPermission;
import org.apache.opencmis.client.api.repository.AclPermissionMapping;
import org.apache.opencmis.client.api.repository.RepositoryAclCapabilities;
import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.AclCapabilitiesData;
import org.apache.opencmis.commons.provider.PermissionMappingData;

public class RepositoryAclCapabilitiesImpl implements
		RepositoryAclCapabilities, Serializable {

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
	List<AclPermission> aclPermissions = null;

	public RepositoryAclCapabilitiesImpl(AclCapabilitiesData aclCapabilities) {
		this.aclCapabilities = aclCapabilities;
	}

	public AclPropagation getAclPropagation() {
		return this.aclCapabilities.getAclPropagation();
	}

	public AclPermissionMapping getAddPolicyObjectPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getAddPolicyPolicyPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getAddToFolderFolderPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getAddToFolderObjectPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getApplyAclObjectPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getCancelCheckoutDocumentPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getCheckinDocumentPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getCheckoutDocumentPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getCreateDocumentFolderPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getCreateDocumentTypePermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getCreateFolderFolderPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getCreateFolderTypePermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getCreatePolicyTypePermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getCreateRelationshipSourcePermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getCreateRelationshipTargetPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getCreateRelationshipTypePermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getDeleteContentDocumentPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getDeleteObjectPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getDeleteTreeFolderPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getGetAclObjectPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getGetAllVersionsVersionSeriesPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getGetAppliedPoliciesObjectPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getGetChildrenFolderPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getGetDescendentsFolderPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getGetFolderParentObjectPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getGetObjectRelationshipsObjectPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getGetParentsFolderPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getGetPropertiesObjectPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getMoveObjectPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getMoveSourcePermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getMoveTargetPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public Map<String, AclPermissionMapping> getPermissionMapping() {
		if (this.aclPermissionMapping == null) {
			this.aclPermissionMapping = new Hashtable<String, AclPermissionMapping>();
			AclPermissionMapping apm = null;
			for (PermissionMappingData pmd : this.aclCapabilities
					.getPermissionMappingData()) {
				apm = new AclPermissionMappingImpl(pmd);
				this.aclPermissionMapping.put(pmd.getKey(), apm);
			}
		}
		return this.aclPermissionMapping;
	}

	public List<AclPermission> getPermissions() {
		if (this.aclPermissions == null) {
			this.aclPermissions = new ArrayList<AclPermission>();
		}
		return this.aclPermissions;
	}

	public AclPermissionMapping getRemoveFromFolderFolderPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getRemoveFromFolderObjectPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getRemovePolicyObjectPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getRemovePolicyPolicyPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getSetContentDocumentPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getUpdatePropertiesObjectPermissions() {
		throw new CmisRuntimeException("not implemented");
	}

	public AclPermissionMapping getViewContentObjectPermissions() {
		throw new CmisRuntimeException("not implemented");
	}
}
