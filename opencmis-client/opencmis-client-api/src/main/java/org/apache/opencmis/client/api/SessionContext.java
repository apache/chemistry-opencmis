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

import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.exceptions.CmisFilterNotValidException;

/**
 * A container for some session parameters, like paging cursors, etc.
 * 
 * @see Session.getContext
 * @see Session.setContext
 */
public interface SessionContext {

  // filtering and additional data ((pre-)populating objects)

  void setIncludeProperties(String filter) throws CmisFilterNotValidException;

  void setIncludeRelationships(IncludeRelationships filter) throws CmisFilterNotValidException;

  void setIncludePolicies(boolean include);

  void setIncludeRenditions(String filter) throws CmisFilterNotValidException;

  void setIncludeAcls(boolean include);

  void setIncludeAllowableActions(boolean include);

  void setIncludePathSegments(boolean include);

  String getIncludeProperties();

  IncludeRelationships getIncludeRelationships();

  boolean getIncludePolicies();

  String getIncludeRenditions();

  boolean getIncludeAcls();

  boolean getIncludeAllowableActions();

  boolean getIncludePathSegments();

}
