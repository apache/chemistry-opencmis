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
import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;

public interface PermissionMappingData extends Serializable, ExtensionsData {
  String KEY_CAN_GET_DESCENDENTS_FOLDER = "canGetDescendents.Folder";
  String KEY_CAN_GET_CHILDREN_FOLDER = "canGetChildren.Folder";
  String KEY_CAN_GET_PARENTS_FOLDER = "canGetParents.Folder";
  String KEY_CAN_GET_FOLDER_PARENT_OBJECT = "canGetFolderParent.Object";
  String KEY_CAN_CREATE_DOCUMENT_FOLDER = "canCreateDocument.Folder";
  String KEY_CAN_CREATE_FOLDER_FOLDER = "canCreateFolder.Folder";
  String KEY_CAN_CREATE_RELATIONSHIP_SOURCE = "canCreateRelationship.Source";
  String KEY_CAN_CREATE_RELATIONSHIP_TARGET = "canCreateRelationship.Target";
  String KEY_CAN_GET_PROPERTIES_OBJECT = "canGetProperties.Object";
  String KEY_CAN_VIEW_CONTENT_OBJECT = "canViewContent.Object";
  String KEY_CAN_UPDATE_PROPERTIES_OBJECT = "canUpdateProperties.Object";
  String KEY_CAN_MOVE_OBJECT = "canMove.Object";
  String KEY_CAN_MOVE_TARGET = "canMove.Target";
  String KEY_CAN_MOVE_SOURCE = "canMove.Source";
  String KEY_CAN_DELETE_OBJECT = "canDelete.Object";
  String KEY_CAN_DELETE_TREE_FOLDER = "canDeleteTree.Folder";
  String KEY_CAN_SET_CONTENT_DOCUMENT = "canSetContent.Document";
  String KEY_CAN_DELETE_CONTENT_DOCUMENT = "canDeleteContent.Document";
  String KEY_CAN_ADD_TO_FOLDER_OBJECT = "canAddToFolder.Object";
  String KEY_CAN_ADD_TO_FOLDER_FOLDER = "canAddToFolder.Folder";
  String KEY_CAN_REMOVE_FROM_FOLDER_OBJECT = "canRemoveFromFolder.Object";
  String KEY_CAN_REMOVE_FROM_FOLDER_FOLDER = "canRemoveFromFolder.Folder";
  String KEY_CAN_CHECKOUT_DOCUMENT = "canCheckout.Document";
  String KEY_CAN_CANCEL_CHECKOUT_DOCUMENT = "canCancelCheckout.Document";
  String KEY_CAN_CHECKIN_DOCUMENT = "canCheckin.Document";
  String KEY_CAN_GET_ALL_VERSIONS_VERSION_SERIES = "canGetAllVersions.VersionSeries";
  String KEY_CAN_GET_OBJECT_RELATIONSHIPS_OBJECT = "canGetObjectRelationships.Object";
  String KEY_CAN_ADD_POLICY_OBJECT = "canAddPolicy.Object";
  String KEY_CAN_ADD_POLICY_POLICY = "canAddPolicy.Policy";
  String KEY_CAN_REMOVE_POLICY_OBJECT = "canRemovePolicy.Object";
  String KEY_CAN_REMOVE_POLICY_POLICY = "canRemovePolicy.Policy";
  String KEY_CAN_GET_APPLIED_POLICIES_OBJECT = "canGetAppliedPolicies.Object";
  String KEY_CAN_GET_ACL_OBJECT = "canGetACL.Object";
  String KEY_CAN_APPLY_ACL_OBJECT = "canApplyACL.Object";

  String getKey();

  List<String> getPermissions();

}
