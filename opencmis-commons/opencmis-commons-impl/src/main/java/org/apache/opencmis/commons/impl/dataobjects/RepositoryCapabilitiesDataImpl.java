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

import org.apache.opencmis.commons.enums.CapabilityAcl;
import org.apache.opencmis.commons.enums.CapabilityChanges;
import org.apache.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.opencmis.commons.enums.CapabilityJoin;
import org.apache.opencmis.commons.enums.CapabilityQuery;
import org.apache.opencmis.commons.enums.CapabilityRendition;
import org.apache.opencmis.commons.provider.RepositoryCapabilitiesData;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RepositoryCapabilitiesDataImpl extends AbstractExtensionData implements
    RepositoryCapabilitiesData {

  private static final long serialVersionUID = 1L;

  private Boolean fAllVersionsSearchable;
  private CapabilityAcl fCapabilityAcl;
  private CapabilityChanges fCapabilityChanges;
  private CapabilityContentStreamUpdates fCapabilityContentStreamUpdates;
  private CapabilityJoin fCapabilityJoin;
  private CapabilityQuery fCapabilityQuery;
  private CapabilityRendition fCapabilityRendition;
  private Boolean fIsPwcSearchable;
  private Boolean fIsPwcUpdatable;
  private Boolean fSupportsGetDescendants;
  private Boolean fSupportsGetFolderTree;
  private Boolean fSupportsMultifiling;
  private Boolean fSupportsUnfiling;
  private Boolean fSupportsVersionSpecificFiling;

  /**
   * Constructor.
   */
  public RepositoryCapabilitiesDataImpl() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryCapabilitiesData#allVersionsSearchable()
   */
  public Boolean allVersionsSearchable() {
    return fAllVersionsSearchable;
  }

  public void setAllVersionsSearchable(Boolean allVersionsSearchable) {
    fAllVersionsSearchable = allVersionsSearchable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryCapabilitiesData#getCapabilityACL()
   */
  public CapabilityAcl getCapabilityAcl() {
    return fCapabilityAcl;
  }

  public void setCapabilityAcl(CapabilityAcl capabilityAcl) {
    fCapabilityAcl = capabilityAcl;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryCapabilitiesData#getCapabilityChanges()
   */
  public CapabilityChanges getCapabilityChanges() {
    return fCapabilityChanges;
  }

  public void setCapabilityChanges(CapabilityChanges capabilityChanges) {
    fCapabilityChanges = capabilityChanges;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.RepositoryCapabilitiesData#getCapabilityContentStreamUpdatability
   * ()
   */
  public CapabilityContentStreamUpdates getCapabilityContentStreamUpdatability() {
    return fCapabilityContentStreamUpdates;
  }

  public void setCapabilityContentStreamUpdates(
      CapabilityContentStreamUpdates capabilityContentStreamUpdates) {
    fCapabilityContentStreamUpdates = capabilityContentStreamUpdates;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryCapabilitiesData#getCapabilityJoin()
   */
  public CapabilityJoin getCapabilityJoin() {
    return fCapabilityJoin;
  }

  public void setCapabilityJoin(CapabilityJoin capabilityJoin) {
    fCapabilityJoin = capabilityJoin;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryCapabilitiesData#getCapabilityQuery()
   */
  public CapabilityQuery getCapabilityQuery() {
    return fCapabilityQuery;
  }

  public void setCapabilityQuery(CapabilityQuery capabilityQuery) {
    fCapabilityQuery = capabilityQuery;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryCapabilitiesData#getCapabilityRenditions()
   */
  public CapabilityRendition getCapabilityRenditions() {
    return fCapabilityRendition;
  }

  public void setCapabilityRendition(CapabilityRendition capabilityRendition) {
    fCapabilityRendition = capabilityRendition;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryCapabilitiesData#isPwcSearchable()
   */
  public Boolean isPwcSearchable() {
    return fIsPwcSearchable;
  }

  public void setIsPwcSearchable(Boolean isPwcSearchable) {
    fIsPwcSearchable = isPwcSearchable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryCapabilitiesData#isPwcUpdatable()
   */
  public Boolean isPwcUpdatable() {
    return fIsPwcUpdatable;
  }

  public void setIsPwcUpdatable(Boolean isPwcUpdatable) {
    fIsPwcUpdatable = isPwcUpdatable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryCapabilitiesData#supportsGetDescendants()
   */
  public Boolean supportsGetDescendants() {
    return fSupportsGetDescendants;
  }

  public void setSupportsGetDescendants(Boolean supportsGetDescendants) {
    fSupportsGetDescendants = supportsGetDescendants;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryCapabilitiesData#supportsGetFolderTree()
   */
  public Boolean supportsGetFolderTree() {
    return fSupportsGetFolderTree;
  }

  public void setSupportsGetFolderTree(Boolean supportsGetFolderTree) {
    fSupportsGetFolderTree = supportsGetFolderTree;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryCapabilitiesData#supportsMultifiling()
   */
  public Boolean supportsMultifiling() {
    return fSupportsMultifiling;
  }

  public void setSupportsMultifiling(Boolean supportsMultifiling) {
    fSupportsMultifiling = supportsMultifiling;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryCapabilitiesData#supportsUnfiling()
   */
  public Boolean supportsUnfiling() {
    return fSupportsUnfiling;
  }

  public void setSupportsUnfiling(Boolean supportsUnfiling) {
    fSupportsUnfiling = supportsUnfiling;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryCapabilitiesData#supportsVersionSpecificFiling()
   */
  public Boolean supportsVersionSpecificFiling() {
    return fSupportsVersionSpecificFiling;
  }

  public void setSupportsVersionSpecificFiling(Boolean supportsVersionSpecificFiling) {
    fSupportsVersionSpecificFiling = supportsVersionSpecificFiling;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Repository Capabilities [all versions searchable=" + fAllVersionsSearchable
        + ", capability ACL=" + fCapabilityAcl + ", capability changes=" + fCapabilityChanges
        + ", capability content stream updates=" + fCapabilityContentStreamUpdates
        + ", capability join=" + fCapabilityJoin + ", capability query=" + fCapabilityQuery
        + ", capability rendition=" + fCapabilityRendition + ", is PWC searchable="
        + fIsPwcSearchable + ", is PWC updatable=" + fIsPwcUpdatable + ", supports GetDescendants="
        + fSupportsGetDescendants + ", supports GetFolderTree=" + fSupportsGetFolderTree
        + ", supports multifiling=" + fSupportsMultifiling + ", supports unfiling="
        + fSupportsUnfiling + ", supports version specific filing="
        + fSupportsVersionSpecificFiling + "]" + super.toString();
  }
}
