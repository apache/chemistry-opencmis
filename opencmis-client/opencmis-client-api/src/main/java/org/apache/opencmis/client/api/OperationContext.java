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
package org.apache.opencmis.client.api;

import java.util.Set;

import org.apache.opencmis.commons.enums.IncludeRelationships;

public interface OperationContext {

  Set<String> getFilter();

  void setFilter(Set<String> propertyFilter);

  /**
   * Returns the filter extended by cmis:objectId, cmis:objectTypeId and cmis:baseTypeId.
   */
  String getFilterString();

  boolean isIncludeAllowableActions();

  void setIncludeAllowableActions(boolean include);

  boolean isIncludeAcls();

  void setIncludeAcls(boolean include);

  IncludeRelationships getIncludeRelationships();

  void setIncludeRelationships(IncludeRelationships include);

  boolean isIncludePolicies();

  void setIncludePolicies(boolean include);

  Set<String> getRenditionFilter();

  void setRenditionFilter(Set<String> renditionFilter);

  String getRenditionFilterString();

  boolean isIncludePathSegments();

  void setIncludePathSegments(boolean include);

  String getOrderBy();

  void setOrderBy(String orderBy);

  boolean isCacheEnabled();

  void setCacheEnabled(boolean cacheEnabled);

  String getCacheKey();
}
