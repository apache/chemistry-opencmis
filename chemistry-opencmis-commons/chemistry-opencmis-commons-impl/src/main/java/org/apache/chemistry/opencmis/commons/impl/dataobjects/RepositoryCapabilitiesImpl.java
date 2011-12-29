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

import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;

/**
 * RepositoryCapabilities Implementation.
 */
public class RepositoryCapabilitiesImpl extends AbstractExtensionData implements RepositoryCapabilities {

    private static final long serialVersionUID = 1L;

    private Boolean allVersionsSearchable;
    private CapabilityAcl capabilityAcl;
    private CapabilityChanges capabilityChanges;
    private CapabilityContentStreamUpdates capabilityContentStreamUpdates;
    private CapabilityJoin capabilityJoin;
    private CapabilityQuery capabilityQuery;
    private CapabilityRenditions capabilityRendition;
    private Boolean isPwcSearchable;
    private Boolean isPwcUpdatable;
    private Boolean supportsGetDescendants;
    private Boolean supportsGetFolderTree;
    private Boolean supportsMultifiling;
    private Boolean supportsUnfiling;
    private Boolean supportsVersionSpecificFiling;

    /**
     * Constructor.
     */
    public RepositoryCapabilitiesImpl() {
    }

    public Boolean isAllVersionsSearchableSupported() {
        return allVersionsSearchable;
    }

    public void setAllVersionsSearchable(Boolean allVersionsSearchable) {
        this.allVersionsSearchable = allVersionsSearchable;
    }

    public CapabilityAcl getAclCapability() {
        return capabilityAcl;
    }

    public void setCapabilityAcl(CapabilityAcl capabilityAcl) {
        this.capabilityAcl = capabilityAcl;
    }

    public CapabilityChanges getChangesCapability() {
        return capabilityChanges;
    }

    public void setCapabilityChanges(CapabilityChanges capabilityChanges) {
        this.capabilityChanges = capabilityChanges;
    }

    public CapabilityContentStreamUpdates getContentStreamUpdatesCapability() {
        return capabilityContentStreamUpdates;
    }

    public void setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates capabilityContentStreamUpdates) {
        this.capabilityContentStreamUpdates = capabilityContentStreamUpdates;
    }

    public CapabilityJoin getJoinCapability() {
        return capabilityJoin;
    }

    public void setCapabilityJoin(CapabilityJoin capabilityJoin) {
        this.capabilityJoin = capabilityJoin;
    }

    public CapabilityQuery getQueryCapability() {
        return capabilityQuery;
    }

    public void setCapabilityQuery(CapabilityQuery capabilityQuery) {
        this.capabilityQuery = capabilityQuery;
    }

    public CapabilityRenditions getRenditionsCapability() {
        return capabilityRendition;
    }

    public void setCapabilityRendition(CapabilityRenditions capabilityRendition) {
        this.capabilityRendition = capabilityRendition;
    }

    public Boolean isPwcSearchableSupported() {
        return isPwcSearchable;
    }

    public void setIsPwcSearchable(Boolean isPwcSearchable) {
        this.isPwcSearchable = isPwcSearchable;
    }

    public Boolean isPwcUpdatableSupported() {
        return isPwcUpdatable;
    }

    public void setIsPwcUpdatable(Boolean isPwcUpdatable) {
        this.isPwcUpdatable = isPwcUpdatable;
    }

    public Boolean isGetDescendantsSupported() {
        return supportsGetDescendants;
    }

    public void setSupportsGetDescendants(Boolean supportsGetDescendants) {
        this.supportsGetDescendants = supportsGetDescendants;
    }

    public Boolean isGetFolderTreeSupported() {
        return supportsGetFolderTree;
    }

    public void setSupportsGetFolderTree(Boolean supportsGetFolderTree) {
        this.supportsGetFolderTree = supportsGetFolderTree;
    }

    public Boolean isMultifilingSupported() {
        return supportsMultifiling;
    }

    public void setSupportsMultifiling(Boolean supportsMultifiling) {
        this.supportsMultifiling = supportsMultifiling;
    }

    public Boolean isUnfilingSupported() {
        return supportsUnfiling;
    }

    public void setSupportsUnfiling(Boolean supportsUnfiling) {
        this.supportsUnfiling = supportsUnfiling;
    }

    public Boolean isVersionSpecificFilingSupported() {
        return supportsVersionSpecificFiling;
    }

    public void setSupportsVersionSpecificFiling(Boolean supportsVersionSpecificFiling) {
        this.supportsVersionSpecificFiling = supportsVersionSpecificFiling;
    }

    @Override
    public String toString() {
        return "Repository Capabilities [all versions searchable=" + allVersionsSearchable + ", capability ACL="
                + capabilityAcl + ", capability changes=" + capabilityChanges + ", capability content stream updates="
                + capabilityContentStreamUpdates + ", capability join=" + capabilityJoin + ", capability query="
                + capabilityQuery + ", capability rendition=" + capabilityRendition + ", is PWC searchable="
                + isPwcSearchable + ", is PWC updatable=" + isPwcUpdatable + ", supports GetDescendants="
                + supportsGetDescendants + ", supports GetFolderTree=" + supportsGetFolderTree
                + ", supports multifiling=" + supportsMultifiling + ", supports unfiling=" + supportsUnfiling
                + ", supports version specific filing=" + supportsVersionSpecificFiling + "]" + super.toString();
    }
}
