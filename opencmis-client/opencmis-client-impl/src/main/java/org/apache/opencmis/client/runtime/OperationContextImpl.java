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

  public static final String PROPERTIES_STAR = "*";
  public static final String RENDITION_NONE = "cmis:none";

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
  private String cacheKey;

  /**
   * Default constructor.
   */
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
    generateCacheKey();
  }

  /**
   * Copy constructor.
   */
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
    generateCacheKey();
  }

  /**
   * Constructor with parameters.
   */
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
    generateCacheKey();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#getFilter()
   */
  public Set<String> getFilter() {
    if (this.filter == null) {
      return null;
    }

    return Collections.unmodifiableSet(this.filter);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#setFilter(java.util.Set)
   */
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
        if (toid.equals(PROPERTIES_STAR)) {
          tempSet = new TreeSet<String>();
          tempSet.add(PROPERTIES_STAR);
          break;
        }
        if (toid.indexOf(',') > -1) {
          throw new IllegalArgumentException("Property id must not contain a comma!");
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

    generateCacheKey();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#setFilter(java.lang.String)
   */
  public void setFilterString(String propertyFilter) {
    if (propertyFilter == null) {
      setFilter(null);
      return;
    }

    String[] propertyIds = propertyFilter.split(",");
    TreeSet<String> tempSet = new TreeSet<String>();
    for (String pid : propertyIds) {
      tempSet.add(pid);
    }

    setFilter(tempSet);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#getFilterString()
   */
  public String getFilterString() {
    if (this.filter == null) {
      return null;
    }

    if (this.filter.contains(PROPERTIES_STAR)) {
      return PROPERTIES_STAR;
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

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#isIncludeAcls()
   */
  public boolean isIncludeAcls() {
    return includeAcls;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#setIncludeAcls(boolean)
   */
  public void setIncludeAcls(boolean include) {
    this.includeAcls = include;
    generateCacheKey();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#isIncludeAllowableActions()
   */
  public boolean isIncludeAllowableActions() {
    return this.includeAllowableActions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#setIncludeAllowableActions(boolean)
   */
  public void setIncludeAllowableActions(boolean include) {
    this.includeAllowableActions = include;
    generateCacheKey();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#isIncludePolicies()
   */
  public boolean isIncludePolicies() {
    return this.includePolicies;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#setIncludePolicies(boolean)
   */
  public void setIncludePolicies(boolean include) {
    this.includePolicies = include;
    generateCacheKey();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#getIncludeRelationships()
   */
  public IncludeRelationships getIncludeRelationships() {
    return this.includeRelationships;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.api.OperationContext#setIncludeRelationships(org.apache.opencmis
   * .commons.enums.IncludeRelationships)
   */
  public void setIncludeRelationships(IncludeRelationships include) {
    this.includeRelationships = include;
    generateCacheKey();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#getRenditionFilter()
   */
  public Set<String> getRenditionFilter() {
    if (this.renditionFilter == null) {
      return null;
    }

    return Collections.unmodifiableSet(this.renditionFilter);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#setRenditionFilter(java.util.Set)
   */
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
        if (trf.indexOf(',') > -1) {
          throw new IllegalArgumentException("Rendition must not contain a comma!");
        }

        tempSet.add(trf);
      }

      if (tempSet.size() == 0) {
        tempSet.add(RENDITION_NONE);
      }
    }
    else {
      tempSet.add(RENDITION_NONE);
    }

    this.renditionFilter = tempSet;
    generateCacheKey();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#setRenditionFilterString(java.lang.String)
   */
  public void setRenditionFilterString(String renditionFilter) {
    if (renditionFilter == null) {
      setRenditionFilter(null);
      return;
    }

    String[] renditions = renditionFilter.split(",");
    TreeSet<String> tempSet = new TreeSet<String>();
    for (String rend : renditions) {
      tempSet.add(rend);
    }

    setRenditionFilter(tempSet);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#getRenditionFilterString()
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#isIncludePathSegments()
   */
  public boolean isIncludePathSegments() {
    return includePathSegments;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#setIncludePathSegments(boolean)
   */
  public void setIncludePathSegments(boolean include) {
    this.includePathSegments = include;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#getOrderBy()
   */
  public String getOrderBy() {
    return this.orderBy;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#setOrderBy(java.lang.String)
   */
  public void setOrderBy(String orderBy) {
    this.orderBy = orderBy;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#isCacheEnabled()
   */
  public boolean isCacheEnabled() {
    return cacheEnabled;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#setCacheEnabled(boolean)
   */
  public void setCacheEnabled(boolean cacheEnabled) {
    this.cacheEnabled = cacheEnabled;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.OperationContext#getCacheKey()
   */
  public String getCacheKey() {
    return cacheKey;
  }

  /**
   * Generates a new cache key from all parameters that are relevant for caching.
   */
  protected void generateCacheKey() {
    if (!cacheEnabled) {
      cacheKey = null;
    }

    StringBuilder sb = new StringBuilder();

    sb.append(includeAcls ? "1" : "0");
    sb.append(includeAllowableActions ? "1" : "0");
    sb.append(includePolicies ? "1" : "0");
    sb.append("|");
    sb.append(filter == null ? "" : getFilterString());
    sb.append("|");
    sb.append(includeRelationships == null ? "" : includeRelationships.value());

    sb.append("|");
    sb.append(renditionFilter == null ? "" : getRenditionFilterString());

    cacheKey = sb.toString();
  }
}
