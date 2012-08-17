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

import org.apache.chemistry.opencmis.commons.data.AclCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

/**
 * Repository info data implementation.
 */
public class RepositoryInfoImpl extends AbstractExtensionData implements RepositoryInfo {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String description;
    private String versionSupported;
    private RepositoryCapabilities capabilities;
    private String rootFolderId;
    private AclCapabilities aclCapabilities;
    private String principalAnonymous;
    private String principalAnyone;
    private String thinClientUri;
    private Boolean changesIncomplete;
    private List<BaseTypeId> changesOnType;
    private String latestChangeLogToken;
    private String vendorName;
    private String productName;
    private String productVersion;

    /**
     * Constructor.
     */
    public RepositoryInfoImpl() {
    }

    public RepositoryInfoImpl(RepositoryInfo data) {
        id = data.getId();
        name = data.getName();
        description = data.getDescription();
        versionSupported = data.getCmisVersionSupported();
        capabilities = data.getCapabilities();
        rootFolderId = data.getRootFolderId();
        aclCapabilities = data.getAclCapabilities();
        principalAnonymous = data.getPrincipalIdAnonymous();
        principalAnyone = data.getPrincipalIdAnyone();
        thinClientUri = data.getThinClientUri();
        changesIncomplete = data.getChangesIncomplete();
        changesOnType = data.getChangesOnType();
        latestChangeLogToken = data.getLatestChangeLogToken();
        vendorName = data.getVendorName();
        productName = data.getProductName();
        productVersion = data.getProductVersion();
        setExtensions(data.getExtensions());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCmisVersionSupported() {
        return versionSupported;
    }

    public void setCmisVersionSupported(String versionSupported) {
        this.versionSupported = versionSupported;
    }

    public RepositoryCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(RepositoryCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public String getRootFolderId() {
        return rootFolderId;
    }

    public void setRootFolder(String rootFolderId) {
        this.rootFolderId = rootFolderId;
    }

    public AclCapabilities getAclCapabilities() {
        return aclCapabilities;
    }

    public void setAclCapabilities(AclCapabilities aclCapabilities) {
        this.aclCapabilities = aclCapabilities;
    }

    public String getPrincipalIdAnonymous() {
        return principalAnonymous;
    }

    public void setPrincipalAnonymous(String principalAnonymous) {
        this.principalAnonymous = principalAnonymous;
    }

    public String getPrincipalIdAnyone() {
        return principalAnyone;
    }

    public void setPrincipalAnyone(String principalAnyone) {
        this.principalAnyone = principalAnyone;
    }

    public String getThinClientUri() {
        return thinClientUri;
    }

    public void setThinClientUri(String thinClientUri) {
        this.thinClientUri = thinClientUri;
    }

    public Boolean getChangesIncomplete() {
        return changesIncomplete;
    }

    public void setChangesIncomplete(Boolean changesIncomplete) {
        this.changesIncomplete = changesIncomplete;
    }

    public List<BaseTypeId> getChangesOnType() {
        return changesOnType;
    }

    public void setChangesOnType(List<BaseTypeId> changesOnType) {
        this.changesOnType = changesOnType;
    }

    public String getLatestChangeLogToken() {
        return latestChangeLogToken;
    }

    public void setLatestChangeLogToken(String latestChangeLogToken) {
        this.latestChangeLogToken = latestChangeLogToken;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    @Override
    public String toString() {
        return "Repository Info [id=" + id + ", name=" + name + ", description=" + description + ", capabilities="
                + capabilities + ", ACL capabilities=" + aclCapabilities + ", changes incomplete=" + changesIncomplete
                + ", changes on type=" + changesOnType + ", latest change log token=" + latestChangeLogToken
                + ", principal anonymous=" + principalAnonymous + ", principal anyone=" + principalAnyone
                + ", vendor name=" + vendorName + ", product name=" + productName + ", product version="
                + productVersion + ", root folder id=" + rootFolderId + ", thin client URI=" + thinClientUri
                + ", version supported=" + versionSupported + "]" + super.toString();
    }

}
