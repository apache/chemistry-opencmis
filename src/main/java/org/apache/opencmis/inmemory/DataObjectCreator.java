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
package org.apache.opencmis.inmemory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.opencmis.commons.impl.dataobjects.AllowableActionsDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.ChangeEventInfoDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PolicyIdListDataImpl;
import org.apache.opencmis.commons.provider.AccessControlEntry;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.AllowableActionsData;
import org.apache.opencmis.commons.provider.ChangeEventInfoData;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PolicyIdListData;
import org.apache.opencmis.commons.provider.RenditionData;
import org.apache.opencmis.inmemory.storedobj.api.StoredObject;

/**
 * @author Jens A collection of utility functions to fill the data objects used as return values for
 *         the service object calls
 */
public class DataObjectCreator {

  public static AllowableActionsData fillAllowableActions(StoredObject so) {

    AllowableActionsDataImpl allowableActions = new AllowableActionsDataImpl();
    Map<String, Boolean> actions = new HashMap<String, Boolean>();
    actions.put(AllowableActionsData.ACTION_CAN_DELETE_OBJECT, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_UPDATE_PROPERTIES, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_GET_PROPERTIES, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_GET_OBJECT_RELATIONSHIPS, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_GET_OBJECT_PARENTS, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_GET_FOLDER_PARENT, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_GET_FOLDER_TREE, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_GET_DESCENDANTS, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_MOVE_OBJECT, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_DELETE_CONTENT_STREAM, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_CHECK_OUT, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_CANCEL_CHECK_OUT, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_CHECK_IN, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_SET_CONTENT_STREAM, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_GET_ALL_VERSIONS, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_ADD_OBJECT_TO_FOLDER, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_REMOVE_OBJECT_FROM_FOLDER, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_GET_CONTENT_STREAM, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_APPLY_POLICY, Boolean.FALSE);
    actions.put(AllowableActionsData.ACTION_CAN_GET_APPLIED_POLICIES, Boolean.FALSE);
    actions.put(AllowableActionsData.ACTION_CAN_REMOVE_POLICY, Boolean.FALSE);
    actions.put(AllowableActionsData.ACTION_CAN_GET_CHILDREN, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_CREATE_DOCUMENT, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_CREATE_FOLDER, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_CREATE_RELATIONSHIP, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_CREATE_POLICY, Boolean.FALSE);
    actions.put(AllowableActionsData.ACTION_CAN_DELETE_TREE, Boolean.TRUE);
    actions.put(AllowableActionsData.ACTION_CAN_GET_RENDITIONS, Boolean.FALSE);
    actions.put(AllowableActionsData.ACTION_CAN_GET_ACL, Boolean.FALSE);
    actions.put(AllowableActionsData.ACTION_CAN_APPLY_ACL, Boolean.FALSE);
    allowableActions.setAllowableActions(actions);
    return allowableActions;
  }

  public static AccessControlList fillACL(StoredObject so) {
    AccessControlListImpl acl = new AccessControlListImpl();
    List<AccessControlEntry> aces = new ArrayList<AccessControlEntry>();
    // TODO to be completed if ACLs are implemented
    acl.setAces(aces);
    return acl;
  }

  public static PolicyIdListData fillPolicyIds(StoredObject so) {
    // TODO: to be completed if policies are implemented
    PolicyIdListDataImpl polIds = new PolicyIdListDataImpl();
    // polIds.setPolicyIds(...);
    return polIds;
  }

  public static List<ObjectData> fillRelationships(IncludeRelationships includeRelationships,
      StoredObject so) {
    // TODO: to be completed if relationships are implemented
    List<ObjectData> relationships = new ArrayList<ObjectData>();
    return relationships;
  }

  public static List<RenditionData> fillRenditions(StoredObject so) {
    // TODO: to be completed if renditions are implemented
    List<RenditionData> renditions = new ArrayList<RenditionData>();
    return renditions;
  }

  public static ChangeEventInfoData fillChangeEventInfo(StoredObject so) {
    // TODO: to be completed if change information is implemented
    ChangeEventInfoData changeEventInfo = new ChangeEventInfoDataImpl();
    return changeEventInfo;
  }
}
