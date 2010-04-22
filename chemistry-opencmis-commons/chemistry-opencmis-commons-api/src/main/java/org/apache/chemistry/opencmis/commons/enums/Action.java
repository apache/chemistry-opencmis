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
package org.apache.chemistry.opencmis.commons.enums;

public enum Action {

    CAN_DELETE_OBJECT("canDeleteObject"), //
    CAN_UPDATE_PROPERTIES("canUpdateProperties"), //
    CAN_GET_PROPERTIES("canGetProperties"), //
    CAN_GET_OBJECT_RELATIONSHIPS("canGetObjectRelationships"), //
    CAN_GET_OBJECT_PARENTS("canGetObjectParents"), //
    CAN_GET_FOLDER_PARENT("canGetFolderParent"), //
    CAN_GET_FOLDER_TREE("canGetFolderTree"), //
    CAN_GET_DESCENDANTS("canGetDescendants"), //
    CAN_MOVE_OBJECT("canMoveObject"), //
    CAN_DELETE_CONTENT_STREAM("canDeleteContentStream"), //
    CAN_CHECK_OUT("canCheckOut"), //
    CAN_CANCEL_CHECK_OUT("canCancelCheckOut"), //
    CAN_CHECK_IN("canCheckIn"), //
    CAN_SET_CONTENT_STREAM("canSetContentStream"), //
    CAN_GET_ALL_VERSIONS("canGetAllVersions"), //
    CAN_ADD_OBJECT_TO_FOLDER("canAddObjectToFolder"), //
    CAN_REMOVE_OBJECT_FROM_FOLDER("canRemoveObjectFromFolder"), //
    CAN_GET_CONTENT_STREAM("canGetContentStream"), //
    CAN_APPLY_POLICY("canApplyPolicy"), //
    CAN_GET_APPLIED_POLICIES("canGetAppliedPolicies"), //
    CAN_REMOVE_POLICY("canRemovePolicy"), //
    CAN_GET_CHILDREN("canGetChildren"), //
    CAN_CREATE_DOCUMENT("canCreateDocument"), //
    CAN_CREATE_FOLDER("canCreateFolder"), //
    CAN_CREATE_RELATIONSHIP("canCreateRelationship"), //
    CAN_DELETE_TREE("canDeleteTree"), //
    CAN_GET_RENDITIONS("canGetRenditions"), //
    CAN_GET_ACL("canGetACL"), //
    CAN_APPLY_ACL("canApplyACL");

    private final String value;

    Action(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Action fromValue(String v) {
        for (Action c : Action.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
