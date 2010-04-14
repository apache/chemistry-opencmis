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

/**
 * Information about the actions for a CMIS object.
 * 
 * @see org.apache.opencmis.client.api.CmisObject#getAllowableActions()
 * 
 *      See CMIS Domain Model - section 2.2.4.6.
 */
public interface AllowableActions {

  Boolean canDeleteObject();

  Boolean canUpdateProperties();

  Boolean canGetProperties();

  Boolean canGetObjectRelationships();

  Boolean canGetObjectParents();

  Boolean canGetFolderParent();

  Boolean canGetDescendants();

  Boolean canMoveObject();

  Boolean canDeleteContentStream();

  Boolean canCheckOut();

  Boolean canCancelCheckOut();

  Boolean canCheckIn();

  Boolean canSetContentStream();

  Boolean canGetAllVersions();

  Boolean canAddObjectToFolder();

  Boolean canRemoveObjectFromFolder();

  Boolean canGetContentStream();

  Boolean canApplyPolicy();

  Boolean canGetAppliedPolicies();

  Boolean canRemovePolicy();

  Boolean canGetChildren();

  Boolean canCreateDocument();

  Boolean canCreateFolder();

  Boolean canCreateRelationship();

  Boolean canDeleteTree();

  Boolean canGetRenditions();

  Boolean canGetAcl();

  Boolean canApplyAcl();
}
