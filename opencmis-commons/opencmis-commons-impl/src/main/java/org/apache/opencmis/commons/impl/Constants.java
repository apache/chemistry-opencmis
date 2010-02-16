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
package org.apache.opencmis.commons.impl;

/**
 * Constants for CMIS server and client.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public final class Constants {

  /**
   * Private constructor.
   */
  private Constants() {
  }

  // namespaces
  public static final String NAMESPACE_CMIS = "http://docs.oasis-open.org/ns/cmis/core/200908/";
  public static final String NAMESPACE_ATOM = "http://www.w3.org/2005/Atom";
  public static final String NAMESPACE_APP = "http://www.w3.org/2007/app";
  public static final String NAMESPACE_RESTATOM = "http://docs.oasis-open.org/ns/cmis/restatom/200908/";
  public static final String NAMESPACE_XSI = "http://www.w3.org/2001/XMLSchema-instance";

  // media types
  public static final String MEDIATYPE_SERVICE = "application/atomsvc+xml";
  public static final String MEDIATYPE_FEED = "application/atom+xml;type=feed";
  public static final String MEDIATYPE_ENTRY = "application/atom+xml;type=entry";
  public static final String MEDIATYPE_CHILDREN = MEDIATYPE_FEED;
  public static final String MEDIATYPE_DESCENDANTS = "application/cmistree+xml";
  public static final String MEDIATYPE_QUERY = "application/cmisquery+xml";
  public static final String MEDIATYPE_ALLOWABLEACTION = "application/cmisallowableactions+xml";
  public static final String MEDIATYPE_ACL = "application/cmisacl+xml";
  public static final String MEDIATYPE_CMISATOM = "application/cmisatom+xml";
  public static final String MEDIATYPE_OCTETSTREAM = "application/octet-stream";

  // collections
  public static final String COLLECTION_ROOT = "root";
  public static final String COLLECTION_TYPES = "types";
  public static final String COLLECTION_QUERY = "query";
  public static final String COLLECTION_CHECKEDOUT = "checkedout";
  public static final String COLLECTION_UNFILED = "unfiled";

  // URI templates
  public static final String TEMPLATE_OBJECT_BY_ID = "objectbyid";
  public static final String TEMPLATE_OBJECT_BY_PATH = "objectbypath";
  public static final String TEMPLATE_TYPE_BY_ID = "typebyid";
  public static final String TEMPLATE_QUERY = "query";

  // Link rel
  public static final String REL_SELF = "self";
  public static final String REL_ENCLOSURE = "enclosure";
  public static final String REL_SERVICE = "service";
  public static final String REL_DESCRIBEDBY = "describedby";
  public static final String REL_ALTERNATE = "alternate";
  public static final String REL_DOWN = "down";
  public static final String REL_UP = "up";
  public static final String REL_FIRST = "first";
  public static final String REL_LAST = "last";
  public static final String REL_PREV = "previous";
  public static final String REL_NEXT = "next";
  public static final String REL_VIA = "via";
  public static final String REL_EDIT = "edit";
  public static final String REL_EDITMEDIA = "edit-media";
  public static final String REL_VERSIONHISTORY = "version-history";
  public static final String REL_CURRENTVERSION = "current-version";
  public static final String REL_WORKINGCOPY = "working-copy";
  public static final String REL_FOLDERTREE = "http://docs.oasis-open.org/ns/cmis/link/200908/foldertree";
  public static final String REL_ALLOWABLEACTIONS = "http://docs.oasis-open.org/ns/cmis/link/200908/allowableactions";
  public static final String REL_ACL = "http://docs.oasis-open.org/ns/cmis/link/200908/acl";
  public static final String REL_SOURCE = "http://docs.oasis-open.org/ns/cmis/link/200908/source";
  public static final String REL_TARGET = "http://docs.oasis-open.org/ns/cmis/link/200908/target";

  public static final String REL_RELATIONSHIPS = "http://docs.oasis-open.org/ns/cmis/link/200908/relationships";
  public static final String REL_POLICIES = "http://docs.oasis-open.org/ns/cmis/link/200908/policies";

  public static final String REP_REL_TYPEDESC = "http://docs.oasis-open.org/ns/cmis/link/200908/typedescendants";
  public static final String REP_REL_FOLDERTREE = "http://docs.oasis-open.org/ns/cmis/link/200908/foldertree";
  public static final String REP_REL_ROOTDESC = "http://docs.oasis-open.org/ns/cmis/link/200908/rootdescendants";
  public static final String REP_REL_CHANGES = "http://docs.oasis-open.org/ns/cmis/link/200908/changes";

  // parameter
  public static final String PARAM_ACL = "includeACL";
  public static final String PARAM_ALLOWABLE_ACTIONS = "includeAllowableActions";
  public static final String PARAM_ALL_VERSIONS = "allVersions";
  public static final String PARAM_CHANGE_LOG_TOKEN = "changeLogToken";
  public static final String PARAM_CHANGE_TOKEN = "changeToken";
  public static final String PARAM_CHECKIN_COMMENT = "checkinComment";
  public static final String PARAM_CHECK_IN = "checkIn";
  public static final String PARAM_CHILD_TYPES = "childTypes";
  public static final String PARAM_CONTINUE_ON_FAILURE = "continueOnFailure";
  public static final String PARAM_DEPTH = "depth";
  public static final String PARAM_FILTER = "filter";
  public static final String PARAM_FOLDER_ID = "folderId";
  public static final String PARAM_ID = "id";
  public static final String PARAM_MAJOR = "major";
  public static final String PARAM_MAX_ITEMS = "maxItems";
  public static final String PARAM_ONLY_BASIC_PERMISSIONS = "onlyBasicPermissions";
  public static final String PARAM_ORDER_BY = "orderBy";
  public static final String PARAM_OVERWRITE_FLAG = "overwriteFlag";
  public static final String PARAM_PATH = "path";
  public static final String PARAM_PATH_SEGMENT = "includePathSegment";
  public static final String PARAM_POLICY_ID = "policyId";
  public static final String PARAM_POLICY_IDS = "includePolicyIds";
  public static final String PARAM_PROPERTIES = "includeProperties";
  public static final String PARAM_PROPERTY_DEFINITIONS = "includePropertyDefinitions";
  public static final String PARAM_RELATIONSHIPS = "includeRelationships";
  public static final String PARAM_RELATIONSHIP_DIRECTION = "relationshipDirection";
  public static final String PARAM_RELATIVE_PATH_SEGMENT = "includeRelativePathSegment";
  public static final String PARAM_REMOVE_FROM = "removeFrom";
  public static final String PARAM_RENDITION_FILTER = "renditionFilter";
  public static final String PARAM_REPOSITORY_ID = "repositoryId";
  public static final String PARAM_RETURN_VERSION = "returnVersion";
  public static final String PARAM_ROPERTY_DEFINITIONS = "includePropertyDefinitions";
  public static final String PARAM_SKIP_COUNT = "skipCount";
  public static final String PARAM_SOURCE_FOLDER_ID = "sourceFolderId";
  public static final String PARAM_STREAM_ID = "streamId";
  public static final String PARAM_SUB_RELATIONSHIP_TYPES = "includeSubRelationshipTypes";
  public static final String PARAM_TYPE_ID = "typeId";
  public static final String PARAM_UNFILE_OBJECTS = "unfileObjects";
  public static final String PARAM_VERSIONIG_STATE = "versioningState";
  public static final String PARAM_Q = "q";
  public static final String PARAM_SEARCH_ALL_VERSIONS = "searchAllVersions";
  public static final String PARAM_ACL_PROPAGATION = "ACLPropagation";

  // rendition filter
  public static final String RENDITION_NONE = "cmis:none";
}
