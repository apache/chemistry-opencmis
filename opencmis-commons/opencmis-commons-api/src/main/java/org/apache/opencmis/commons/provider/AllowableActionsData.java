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

import java.util.Map;

import org.apache.opencmis.commons.api.ExtensionsData;

public interface AllowableActionsData extends ExtensionsData {
  String ACTION_CAN_DELETE_OBJECT = "canDeleteObject";
  String ACTION_CAN_UPDATE_PROPERTIES = "canUpdateProperties";
  String ACTION_CAN_GET_PROPERTIES = "canGetProperties";
  String ACTION_CAN_GET_OBJECT_RELATIONSHIPS = "canGetObjectRelationships";
  String ACTION_CAN_GET_OBJECT_PARENTS = "canGetObjectParents";
  String ACTION_CAN_GET_FOLDER_PARENT = "canGetFolderParent";
  String ACTION_CAN_GET_FOLDER_TREE = "canGetFolderTree";
  String ACTION_CAN_GET_DESCENDANTS = "canGetDescendants";
  String ACTION_CAN_MOVE_OBJECT = "canMoveObject";
  String ACTION_CAN_DELETE_CONTENT_STREAM = "canDeleteContentStream";
  String ACTION_CAN_CHECK_OUT = "canCheckOut";
  String ACTION_CAN_CANCEL_CHECK_OUT = "canCancelCheckOut";
  String ACTION_CAN_CHECK_IN = "canCheckIn";
  String ACTION_CAN_SET_CONTENT_STREAM = "canSetContentStream";
  String ACTION_CAN_GET_ALL_VERSIONS = "canGetAllVersions";
  String ACTION_CAN_ADD_OBJECT_TO_FOLDER = "canAddObjectToFolder";
  String ACTION_CAN_REMOVE_OBJECT_FROM_FOLDER = "canRemoveObjectFromFolder";
  String ACTION_CAN_GET_CONTENT_STREAM = "canGetContentStream";
  String ACTION_CAN_APPLY_POLICY = "canApplyPolicy";
  String ACTION_CAN_GET_APPLIED_POLICIES = "canGetAppliedPolicies";
  String ACTION_CAN_REMOVE_POLICY = "canRemovePolicy";
  String ACTION_CAN_GET_CHILDREN = "canGetChildren";
  String ACTION_CAN_CREATE_DOCUMENT = "canCreateDocument";
  String ACTION_CAN_CREATE_FOLDER = "canCreateFolder";
  String ACTION_CAN_CREATE_RELATIONSHIP = "canCreateRelationship";
  String ACTION_CAN_CREATE_POLICY = "canCreatePolicy";
  String ACTION_CAN_DELETE_TREE = "canDeleteTree";
  String ACTION_CAN_GET_RENDITIONS = "canGetRenditions";
  String ACTION_CAN_GET_ACL = "canGetACL";
  String ACTION_CAN_APPLY_ACL = "canApplyACL";

  Map<String, Boolean> getAllowableActions();
}
