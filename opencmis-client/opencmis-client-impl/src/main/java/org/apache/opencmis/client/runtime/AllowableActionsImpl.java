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

import java.util.Map;

import org.apache.opencmis.client.api.AllowableActions;
import org.apache.opencmis.commons.provider.AllowableActionsData;

/**
 * Allowable actions implementation.
 */
public class AllowableActionsImpl implements AllowableActions {

  private Map<String, Boolean> actions;

  public AllowableActionsImpl(Map<String, Boolean> actions) {
    if (actions == null) {
      throw new IllegalArgumentException("Allowable actions map must be set!");
    }

    this.actions = actions;
  }

  public Boolean canAddObjectToFolder() {
    return actions.get(AllowableActionsData.ACTION_CAN_ADD_OBJECT_TO_FOLDER);
  }

  public Boolean canApplyAcl() {
    return actions.get(AllowableActionsData.ACTION_CAN_APPLY_ACL);
  }

  public Boolean canApplyPolicy() {
    return actions.get(AllowableActionsData.ACTION_CAN_APPLY_POLICY);
  }

  public Boolean canCancelCheckOut() {
    return actions.get(AllowableActionsData.ACTION_CAN_CANCEL_CHECK_OUT);
  }

  public Boolean canCheckIn() {
    return actions.get(AllowableActionsData.ACTION_CAN_CHECK_IN);
  }

  public Boolean canCheckOut() {
    return actions.get(AllowableActionsData.ACTION_CAN_CHECK_OUT);
  }

  public Boolean canCreateDocument() {
    return actions.get(AllowableActionsData.ACTION_CAN_CREATE_DOCUMENT);
  }

  public Boolean canCreateFolder() {
    return actions.get(AllowableActionsData.ACTION_CAN_CREATE_FOLDER);
  }

  public Boolean canCreateRelationship() {
    return actions.get(AllowableActionsData.ACTION_CAN_CREATE_RELATIONSHIP);
  }

  public Boolean canDeleteContentStream() {
    return actions.get(AllowableActionsData.ACTION_CAN_DELETE_CONTENT_STREAM);
  }

  public Boolean canDeleteObject() {
    return actions.get(AllowableActionsData.ACTION_CAN_DELETE_OBJECT);
  }

  public Boolean canDeleteTree() {
    return actions.get(AllowableActionsData.ACTION_CAN_DELETE_TREE);
  }

  public Boolean canGetAcl() {
    return actions.get(AllowableActionsData.ACTION_CAN_GET_ACL);
  }

  public Boolean canGetAllVersions() {
    return actions.get(AllowableActionsData.ACTION_CAN_GET_ALL_VERSIONS);
  }

  public Boolean canGetAppliedPolicies() {
    return actions.get(AllowableActionsData.ACTION_CAN_GET_APPLIED_POLICIES);
  }

  public Boolean canGetChildren() {
    return actions.get(AllowableActionsData.ACTION_CAN_GET_CHILDREN);
  }

  public Boolean canGetContentStream() {
    return actions.get(AllowableActionsData.ACTION_CAN_GET_CONTENT_STREAM);
  }

  public Boolean canGetDescendants() {
    return actions.get(AllowableActionsData.ACTION_CAN_GET_CONTENT_STREAM);
  }

  public Boolean canGetFolderParent() {
    return actions.get(AllowableActionsData.ACTION_CAN_GET_FOLDER_PARENT);
  }

  public Boolean canGetObjectParents() {
    return actions.get(AllowableActionsData.ACTION_CAN_GET_OBJECT_PARENTS);
  }

  public Boolean canGetObjectRelationships() {
    return actions.get(AllowableActionsData.ACTION_CAN_GET_OBJECT_RELATIONSHIPS);
  }

  public Boolean canGetProperties() {
    return actions.get(AllowableActionsData.ACTION_CAN_GET_PROPERTIES);
  }

  public Boolean canGetRenditions() {
    return actions.get(AllowableActionsData.ACTION_CAN_GET_RENDITIONS);
  }

  public Boolean canMoveObject() {
    return actions.get(AllowableActionsData.ACTION_CAN_MOVE_OBJECT);
  }

  public Boolean canRemoveObjectFromFolder() {
    return actions.get(AllowableActionsData.ACTION_CAN_REMOVE_OBJECT_FROM_FOLDER);
  }

  public Boolean canRemovePolicy() {
    return actions.get(AllowableActionsData.ACTION_CAN_REMOVE_POLICY);
  }

  public Boolean canSetContentStream() {
    return actions.get(AllowableActionsData.ACTION_CAN_SET_CONTENT_STREAM);
  }

  public Boolean canUpdateProperties() {
    return actions.get(AllowableActionsData.ACTION_CAN_UPDATE_PROPERTIES);
  }
}
