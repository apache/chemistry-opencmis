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
package org.apache.opencmis.commons.provider;

import java.io.Serializable;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.CapabilityAcl;
import org.apache.opencmis.commons.enums.CapabilityChanges;
import org.apache.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.opencmis.commons.enums.CapabilityJoin;
import org.apache.opencmis.commons.enums.CapabilityQuery;
import org.apache.opencmis.commons.enums.CapabilityRendition;

public interface RepositoryCapabilitiesData extends Serializable, ExtensionsData {

  CapabilityAcl getCapabilityAcl();

  Boolean allVersionsSearchable();

  CapabilityChanges getCapabilityChanges();

  CapabilityContentStreamUpdates getCapabilityContentStreamUpdatability();

  Boolean supportsGetDescendants();

  Boolean supportsGetFolderTree();

  Boolean supportsMultifiling();

  Boolean isPwcSearchable();

  Boolean isPwcUpdatable();

  CapabilityQuery getCapabilityQuery();

  CapabilityRendition getCapabilityRenditions();

  Boolean supportsUnfiling();

  Boolean supportsVersionSpecificFiling();

  CapabilityJoin getCapabilityJoin();
}
