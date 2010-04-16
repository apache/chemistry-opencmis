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

import org.apache.chemistry.opencmis.commons.api.AclCapabilities;
import org.apache.chemistry.opencmis.commons.api.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.api.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

/**
 * Repository info data implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RepositoryInfoImpl extends AbstractExtensionData implements RepositoryInfo {

	private static final long serialVersionUID = 1L;

	private String fId;
	private String fName;
	private String fDescription;
	private String fVersionSupported;
	private RepositoryCapabilities fCapabilities;
	private String fRootFolderId;
	private AclCapabilities fAclCapabilities;
	private String fPrincipalAnonymous;
	private String fPrincipalAnyone;
	private String fThinClientUri;
	private Boolean fChangesIncomplete;
	private List<BaseTypeId> fChangesOnType;
	private String fLatestChangeLogToken;
	private String fVendorName;
	private String fProductName;
	private String fProductVersion;

	/**
	 * Constructor.
	 */
	public RepositoryInfoImpl() {
	}

	public RepositoryInfoImpl(RepositoryInfo data) {
		fId = data.getId();
		fName = data.getName();
		fDescription = data.getDescription();
		fVersionSupported = data.getCmisVersionSupported();
		fCapabilities = data.getCapabilities();
		fRootFolderId = data.getRootFolderId();
		fAclCapabilities = data.getAclCapabilities();
		fPrincipalAnonymous = data.getPrincipalIdAnonymous();
		fPrincipalAnyone = data.getPrincipalIdAnyone();
		fThinClientUri = data.getThinClientUri();
		fChangesIncomplete = data.getChangesIncomplete();
		fChangesOnType = data.getChangesOnType();
		fLatestChangeLogToken = data.getLatestChangeLogToken();
		fVendorName = data.getVendorName();
		fProductName = data.getProductName();
		fProductVersion = data.getProductVersion();
	}

	public String getId() {
		return fId;
	}

	public void setRepositoryId(String id) {
		fId = id;
	}

	public String getName() {
		return fName;
	}

	public void setRepositoryName(String name) {
		fName = name;
	}

	public String getDescription() {
		return fDescription;
	}

	public void setRepositoryDescription(String description) {
		fDescription = description;
	}

	public String getCmisVersionSupported() {
		return fVersionSupported;
	}

	public void setCmisVersionSupported(String versionSupported) {
		fVersionSupported = versionSupported;
	}

	public RepositoryCapabilities getCapabilities() {
		return fCapabilities;
	}

	public void setRepositoryCapabilities(RepositoryCapabilities capabilities) {
		fCapabilities = capabilities;
	}

	public String getRootFolderId() {
		return fRootFolderId;
	}

	public void setRootFolder(String rootFolderId) {
		fRootFolderId = rootFolderId;
	}

	public AclCapabilities getAclCapabilities() {
		return fAclCapabilities;
	}

	public void setAclCapabilities(AclCapabilities aclCapabilities) {
		fAclCapabilities = aclCapabilities;
	}

	public String getPrincipalIdAnonymous() {
		return fPrincipalAnonymous;
	}

	public void setPrincipalAnonymous(String principalAnonymous) {
		fPrincipalAnonymous = principalAnonymous;
	}

	public String getPrincipalIdAnyone() {
		return fPrincipalAnyone;
	}

	public void setPrincipalAnyone(String principalAnyone) {
		fPrincipalAnyone = principalAnyone;
	}

	public String getThinClientUri() {
		return fThinClientUri;
	}

	public void setThinClientUri(String thinClientUri) {
		fThinClientUri = thinClientUri;
	}

	public Boolean getChangesIncomplete() {
		return fChangesIncomplete;
	}

	public void setChangesIncomplete(Boolean changesIncomplete) {
		fChangesIncomplete = changesIncomplete;
	}

	public List<BaseTypeId> getChangesOnType() {
		return fChangesOnType;
	}

	public void setChangesOnType(List<BaseTypeId> changesOnType) {
		fChangesOnType = changesOnType;
	}

	public String getLatestChangeLogToken() {
		return fLatestChangeLogToken;
	}

	public void setLatestChangeLogToken(String latestChangeLogToken) {
		fLatestChangeLogToken = latestChangeLogToken;
	}

	public String getVendorName() {
		return fVendorName;
	}

	public void setVendorName(String vendorName) {
		fVendorName = vendorName;
	}

	public String getProductName() {
		return fProductName;
	}

	public void setProductName(String productName) {
		fProductName = productName;
	}

	public String getProductVersion() {
		return fProductVersion;
	}

	public void setProductVersion(String productVersion) {
		fProductVersion = productVersion;
	}

	@Override
	public String toString() {
		return "Repository Info [id=" + fId + ", name=" + fName + ", description=" + fDescription + ", capabilities="
				+ fCapabilities + ", ACL capabilities=" + fAclCapabilities + ", changes incomplete="
				+ fChangesIncomplete + ", changes on type=" + fChangesOnType + ", latest change log token="
				+ fLatestChangeLogToken + ", principal anonymous=" + fPrincipalAnonymous + ", principal anyone="
				+ fPrincipalAnyone + ", vendor name=" + fVendorName + ", product name=" + fProductName
				+ ", product version=" + fProductVersion + ", root folder id=" + fRootFolderId + ", thin client URI="
				+ fThinClientUri + ", version supported=" + fVersionSupported + "]" + super.toString();
	}

}
