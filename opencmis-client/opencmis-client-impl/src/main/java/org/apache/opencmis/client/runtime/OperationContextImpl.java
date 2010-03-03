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
package org.apache.opencmis.client.runtime;

import java.io.Serializable;

import org.apache.opencmis.client.api.OperationContext;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.enums.IncludeRelationships;

/**
 * {@link OperationContext} implementation.
 */
public class OperationContextImpl implements OperationContext, Serializable {

  private static final long serialVersionUID = 1L;

  private String filter;
  private boolean includeAcls;
  private boolean includeAllowableActions;
  private boolean includePolicies;
  private IncludeRelationships includeRelationships;
  private String renditionFilter;
  private boolean includePathSegments;
  private String orderBy;

  public OperationContextImpl() {
    this.filter = null;
    this.includeAcls = false;
    this.includeAllowableActions = true;
    this.includePolicies = true;
    this.includeRelationships = IncludeRelationships.NONE;
    this.renditionFilter = null;
    this.includePathSegments = true;
    this.orderBy = null;
  }

  public OperationContextImpl(OperationContext source) {
    this.filter = source.getFilter();
    this.includeAcls = source.getIncludeAcls();
    this.includeAllowableActions = source.getIncludeAllowableActions();
    this.includePolicies = source.getIncludePolicies();
    this.includeRelationships = source.getIncludeRelationships();
    this.renditionFilter = source.getRenditionFilter();
    this.includePathSegments = source.getIncludePathSegments();
    this.orderBy = source.getOrderBy();
  }

  public OperationContextImpl(String filter, boolean includeAcls, boolean includeAllowableActions,
      boolean includePolicies, IncludeRelationships includeRelationships, String renditionFilter,
      boolean includePathSegments, String orderBy) {
    this.filter = filter;
    this.includeAcls = includeAcls;
    this.includeAllowableActions = includeAllowableActions;
    this.includePolicies = includePolicies;
    this.includeRelationships = includeRelationships;
    this.renditionFilter = renditionFilter;
    this.includePathSegments = includePathSegments;
    this.orderBy = orderBy;
  }

  public String getFilter() {
    return this.filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public String getFullFilter() {
    String fullFilter = filter;

    if ((fullFilter != null) && (fullFilter.indexOf('*') == -1)) {
      if (fullFilter.indexOf(PropertyIds.CMIS_OBJECT_ID) == -1) {
        fullFilter = PropertyIds.CMIS_OBJECT_ID + "," + fullFilter;
      }
      if (fullFilter.indexOf(PropertyIds.CMIS_BASE_TYPE_ID) == -1) {
        fullFilter = PropertyIds.CMIS_BASE_TYPE_ID + "," + fullFilter;
      }
      if (fullFilter.indexOf(PropertyIds.CMIS_OBJECT_TYPE_ID) == -1) {
        fullFilter = PropertyIds.CMIS_OBJECT_TYPE_ID + "," + fullFilter;
      }
    }

    return fullFilter;
  }

  public boolean getIncludeAcls() {
    return includeAcls;
  }

  public void setIncludeAcls(boolean include) {
    this.includeAcls = include;
  }

  public boolean getIncludeAllowableActions() {
    return this.includeAllowableActions;
  }

  public void setIncludeAllowableActions(boolean include) {
    this.includeAllowableActions = include;
  }

  public boolean getIncludePolicies() {
    return this.includePolicies;
  }

  public void setIncludePolicies(boolean include) {
    this.includePolicies = include;
  }

  public IncludeRelationships getIncludeRelationships() {
    return this.includeRelationships;
  }

  public void setIncludeRelationships(IncludeRelationships include) {
    this.includeRelationships = include;
  }

  public String getRenditionFilter() {
    return this.renditionFilter;
  }

  public void setRenditionFilter(String filter) {
    this.renditionFilter = filter;
  }

  public boolean getIncludePathSegments() {
    return includePathSegments;
  }

  public void setIncludePathSegments(boolean include) {
    this.includePathSegments = include;
  }

  public String getOrderBy() {
    return this.orderBy;
  }

  public void setOrderBy(String orderBy) {
    this.orderBy = orderBy;
  }

}
