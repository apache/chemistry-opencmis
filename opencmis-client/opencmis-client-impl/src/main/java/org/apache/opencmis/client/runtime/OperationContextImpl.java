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
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.apache.opencmis.client.api.OperationContext;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.enums.IncludeRelationships;

/**
 * {@link OperationContext} implementation.
 */
public class OperationContextImpl implements OperationContext, Serializable {

  private static final long serialVersionUID = 1L;

  private TreeSet<String> filter;
  private boolean includeAcls;
  private boolean includeAllowableActions;
  private boolean includePolicies;
  private IncludeRelationships includeRelationships;
  private TreeSet<String> renditionFilter;
  private boolean includePathSegments;
  private String orderBy;
  private boolean cacheEnabled;

  public OperationContextImpl() {
    setFilter(null);
    setIncludeAcls(false);
    setIncludeAllowableActions(true);
    setIncludePolicies(false);
    setIncludeRelationships(IncludeRelationships.NONE);
    setRenditionFilter(null);
    setIncludePathSegments(true);
    setOrderBy(null);
    setCacheEnabled(false);
  }

  public OperationContextImpl(OperationContext source) {
    setFilter(source.getFilter());
    setIncludeAcls(source.isIncludeAcls());
    setIncludeAllowableActions(source.isIncludeAllowableActions());
    setIncludePolicies(source.isIncludePolicies());
    setIncludeRelationships(source.getIncludeRelationships());
    setRenditionFilter(source.getRenditionFilter());
    setIncludePathSegments(source.isIncludePathSegments());
    setOrderBy(source.getOrderBy());
    setCacheEnabled(source.isCacheEnabled());
  }

  public OperationContextImpl(Set<String> propertyFilter, boolean includeAcls,
      boolean includeAllowableActions, boolean includePolicies,
      IncludeRelationships includeRelationships, Set<String> renditionFilter,
      boolean includePathSegments, String orderBy, boolean cacheEnabled) {
    setFilter(filter);
    setIncludeAcls(includeAcls);
    setIncludeAllowableActions(includeAllowableActions);
    setIncludePolicies(includePolicies);
    setIncludeRelationships(includeRelationships);
    setRenditionFilter(renditionFilter);
    setIncludePathSegments(includePathSegments);
    setOrderBy(orderBy);
    setCacheEnabled(cacheEnabled);
  }

  public Set<String> getFilter() {
    return Collections.unmodifiableSet(this.filter);
  }

  public void setFilter(Set<String> propertyFilter) {
    if (propertyFilter != null) {
      TreeSet<String> tempSet = new TreeSet<String>();

      for (String oid : propertyFilter) {
        if (oid == null) {
          continue;
        }

        String toid = oid.trim();
        if (toid.length() == 0) {
          continue;
        }
        if (toid.equals("*")) {
          tempSet = new TreeSet<String>();
          tempSet.add("*");
          break;
        }

        tempSet.add(toid);
      }

      if (tempSet.size() == 0) {
        this.filter = null;
      }
      else {
        this.filter = tempSet;
      }
    }
    else {
      this.filter = null;
    }
  }

  public String getFilterString() {
    if (this.filter == null) {
      return null;
    }

    if (this.filter.contains("*")) {
      return "*";
    }

    this.filter.add(PropertyIds.CMIS_OBJECT_ID);
    this.filter.add(PropertyIds.CMIS_BASE_TYPE_ID);
    this.filter.add(PropertyIds.CMIS_OBJECT_TYPE_ID);

    StringBuilder sb = new StringBuilder();

    for (String oid : this.filter) {
      if (sb.length() > 0) {
        sb.append(",");
      }

      sb.append(oid);
    }

    return sb.toString();
  }

  public boolean isIncludeAcls() {
    return includeAcls;
  }

  public void setIncludeAcls(boolean include) {
    this.includeAcls = include;
  }

  public boolean isIncludeAllowableActions() {
    return this.includeAllowableActions;
  }

  public void setIncludeAllowableActions(boolean include) {
    this.includeAllowableActions = include;
  }

  public boolean isIncludePolicies() {
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

  public Set<String> getRenditionFilter() {
    return Collections.unmodifiableSet(this.renditionFilter);
  }

  public void setRenditionFilter(Set<String> renditionFilter) {
    TreeSet<String> tempSet = new TreeSet<String>();

    if (renditionFilter != null) {
      for (String rf : renditionFilter) {
        if (rf == null) {
          continue;
        }

        String trf = rf.trim();
        if (trf.length() == 0) {
          continue;
        }

        tempSet.add(trf);
      }

      if (tempSet.size() == 0) {
        tempSet.add("cmis:none");
      }
    }
    else {
      tempSet.add("cmis:none");
    }

    this.renditionFilter = tempSet;
  }

  public String getRenditionFilterString() {
    if (this.renditionFilter == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder();

    for (String rf : this.renditionFilter) {
      if (sb.length() > 0) {
        sb.append(",");
      }

      sb.append(rf);
    }

    return sb.toString();
  }

  public boolean isIncludePathSegments() {
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

  public boolean isCacheEnabled() {
    return cacheEnabled;
  }

  public void setCacheEnabled(boolean cacheEnabled) {
    this.cacheEnabled = cacheEnabled;
  }

  public String getCacheKey() {
    // TODO Auto-generated method stub
    return null;
  }
}
