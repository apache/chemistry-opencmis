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
package org.apache.chemistry.opencmis.server.impl.browser.json;

/**
 * JSON object constants.
 */
public class JSONConstants {

    public final static String ERROR_EXCEPTION = "exception";
    public final static String ERROR_MESSAGE = "message";
    public final static String ERROR_STACKTRACE = "stacktrace";

    public final static String REPINFO_ID = "repositoryId";
    public final static String REPINFO_NAME = "repositoryName";
    public final static String REPINFO_DESCRIPTION = "repositoryDescription";
    public final static String REPINFO_VENDOR = "vendorName";
    public final static String REPINFO_PRODUCT = "productName";
    public final static String REPINFO_PRODUCT_VERSION = "productVersion";
    public final static String REPINFO_ROOT_FOLDER_ID = "rootFolderId";
    public final static String REPINFO_REPOSITORY_URL = "repositoryUrl";
    public final static String REPINFO_ROOT_FOLDER_URL = "rootFolderUrl";
    public final static String REPINFO_CAPABILITIES = "capabilities";
    public final static String REPINFO_ACL_CAPABILITIES = "aclCapabilities";
    public final static String REPINFO_CHANGE_LOCK_TOKEN = "latestChangeLogToken";
    public final static String REPINFO_CMIS_VERSION_SUPPORTED = "cmisVersionSupported";
    public final static String REPINFO_THIN_CLIENT_URI = "thinClientURI";
    public final static String REPINFO_CHANGES_INCOMPLETE = "changesIncomplete";
    public final static String REPINFO_CHANGES_ON_TYPE = "changesOnType";
    public final static String REPINFO_PRINCIPAL_ID_ANONYMOUS = "principalIdAnonymous";
    public final static String REPINFO_PRINCIPAL_ID_ANYONE = "principalIdAnyone";

    public final static String JSON_CAP_CONTENT_STREAM_UPDATES = "capabilityContentStreamUpdatability";
    public final static String JSON_CAP_CHANGES = "capabilityChanges";
    public final static String JSON_CAP_RENDITIONS = "capabilityRenditions";
    public final static String JSON_CAP_GET_DESCENDANTS = "capabilityGetDescendants";
    public final static String JSON_CAP_GET_FOLDER_TREE = "capabilityGetFolderTree";
    public final static String JSON_CAP_MULTIFILING = "capabilityMultifiling";
    public final static String JSON_CAP_UNFILING = "capabilityUnfiling";
    public final static String JSON_CAP_VERSION_SPECIFIC_FILING = "capabilityVersionSpecificFiling";
    public final static String JSON_CAP_PWC_SEARCHABLE = "capabilityPWCSearchable";
    public final static String JSON_CAP_PWC_UPDATABLE = "capabilityPWCUpdatable";
    public final static String JSON_CAP_ALL_VERSIONS_SEARCHABLE = "capabilityAllVersionsSearchable";
    public final static String JSON_CAP_QUERY = "capabilityQuery";
    public final static String JSON_CAP_JOIN = "capabilityJoin";
    public final static String JSON_CAP_ACL = "capabilityACL";

    public final static String JSON_ACLCAP_SUPPORTED_PERMISSIONS = "supportedPermissions";
    public final static String JSON_ACLCAP_ACL_PROPAGATION = "propagation";
    public final static String JSON_ACLCAP_PERMISSIONS = "permissions";
    public final static String JSON_ACLCAP_PERMISSION_MAPPING = "permissionMapping";

    public final static String JSON_ACLCAP_PERMISSION_PERMISSION = "permission";
    public final static String JSON_ACLCAP_PERMISSION_DESCRIPTION = "description";

    public final static String JSON_ACLCAP_MAPPING_KEY = "key";
    public final static String JSON_ACLCAP_MAPPING_PERMISSION = "permission";

    public final static String JSON_OBJECT_PROPERTIES = "properties";
    public final static String JSON_OBJECT_ALLOWABLE_ACTIONS = "allowableActions";
    public final static String JSON_OBJECT_RELATIONSHIPS = "relationships";
    public final static String JSON_OBJECT_CHANGE_EVENT_INFO = "changeEventInfo";
    public final static String JSON_OBJECT_ACL = "acl";
    public final static String JSON_OBJECT_EXACT_ACL = "exactACL";
    public final static String JSON_OBJECT_POLICY_IDS = "policyIds";
    public final static String JSON_OBJECT_RENDITIONS = "renditions";

    public final static String JSON_OBJECTINFOLDER_OBJECT = "object";
    public final static String JSON_OBJECTINFOLDER_PATH_SEGMENT = "pathSegment";
    public final static String JSON_OBJECTPARENTS_OBJECT = "object";
    public final static String JSON_OBJECTPARENTS_RELATIVE_PATH_SEGMENT = "relativePathSegment";

    public final static String JSON_PROPERTY_ID = "id";
    public final static String JSON_PROPERTY_LOCALNAME = "localName";
    public final static String JSON_PROPERTY_DISPLAYNAME = "displayName";
    public final static String JSON_PROPERTY_QUERYNAME = "queryName";
    public final static String JSON_PROPERTY_VALUE = "value";
    public final static String JSON_PROPERTY_DATATYPE = "type";
    public final static String JSON_PROPERTY_CARDINALITY = "cardinality";

    public final static String JSON_CHANGE_EVENT_TYPE = "changeType";
    public final static String JSON_CHANGE_EVENT_TIME = "changeTime";

    public final static String JSON_ACL_ACES = "aces";
    public final static String JSON_ACL_IS_EXACT = "isExact";

    public final static String JSON_ACE_PRINCIPAL = "princial";
    public final static String JSON_ACE_PRINCIPAL_ID = "princialId";
    public final static String JSON_ACE_PERMISSIONS = "permissions";
    public final static String JSON_ACE_IS_DIRECT = "isDirect";

    public final static String JSON_RENDITION_STREAM_ID = "streamId";
    public final static String JSON_RENDITION_MIMETYPE = "mimeType";
    public final static String JSON_RENDITION_LENGTH = "length";
    public final static String JSON_RENDITION_KIND = "kind";
    public final static String JSON_RENDITION_TITLE = "title";
    public final static String JSON_RENDITION_HEIGHT = "height";
    public final static String JSON_RENDITION_WIDTH = "width";
    public final static String JSON_RENDITION_DOCUMENT_ID = "renditionDocumentId";

    public final static String JSON_OBJECTLIST_OBJECTS = "objects";
    public final static String JSON_OBJECTLIST_HAS_MORE_ITEMS = "hasMoreItems";
    public final static String JSON_OBJECTLIST_NUM_ITEMS = "numItems";

    public final static String JSON_OBJECTINFOLDERLIST_OBJECTS = "objects";
    public final static String JSON_OBJECTINFOLDERLIST_HAS_MORE_ITEMS = "hasMoreItems";
    public final static String JSON_OBJECTINFOLDERLIST_NUM_ITEMS = "numItems";

    public final static String JSON_OBJECTINFOLDERCONTAINER_OBJECT = "object";
    public final static String JSON_OBJECTINFOLDERCONTAINER_CHILDREN = "children";

    public final static String JSON_TYPE_ID = "id";
    public final static String JSON_TYPE_LOCALNAME = "localName";
    public final static String JSON_TYPE_LOCALNAMESPACE = "localNamespace";
    public final static String JSON_TYPE_DISPLAYNAME = "displayName";
    public final static String JSON_TYPE_QUERYNAME = "queryName";
    public final static String JSON_TYPE_DESCRIPTION = "description";
    public final static String JSON_TYPE_BASE_ID = "baseId";
    public final static String JSON_TYPE_PARENT_ID = "parentId";
    public final static String JSON_TYPE_CREATABLE = "creatable";
    public final static String JSON_TYPE_FILEABLE = "fileable";
    public final static String JSON_TYPE_QUERYABLE = "queryable";
    public final static String JSON_TYPE_FULLTEXT_INDEXED = "fulltextIndexed";
    public final static String JSON_TYPE_INCLUDE_IN_SUPERTYPE_QUERY = "includedInSupertypeQuery";
    public final static String JSON_TYPE_CONTROLABLE_POLICY = "controllablePolicy";
    public final static String JSON_TYPE_CONTROLABLE_ACL = "controllableACL";
    public final static String JSON_TYPE_PROPERTY_DEFINITIONS = "propertyDefinitions";

    public final static String JSON_TYPE_VERSIONABLE = "versionable"; // document
    public final static String JSON_TYPE_CONTENTSTREAM_ALLOWED = "contentStreamAllowed"; // document

    public final static String JSON_TYPE_ALLOWED_SOURCE_TYPES = "allowedSourceTypes"; // relationship
    public final static String JSON_TYPE_ALLOWED_TARGET_TYPES = "allowedTargetTypes"; // relationship

    public final static String JSON_PROPERTYTYPE_ID = "id";
    public final static String JSON_PROPERTYTYPE_LOCALNAME = "localName";
    public final static String JSON_PROPERTYTYPE_LOCALNAMESPACE = "localNamespace";
    public final static String JSON_PROPERTYTYPE_DISPLAYNAME = "displayName";
    public final static String JSON_PROPERTYTYPE_QUERYNAME = "queryName";
    public final static String JSON_PROPERTYTYPE_DESCRIPTION = "description";
    public final static String JSON_PROPERTYTYPE_PROPERTY_TYPE = "propertyType";
    public final static String JSON_PROPERTYTYPE_CARDINALITY = "cardinality";
    public final static String JSON_PROPERTYTYPE_UPDATABILITY = "updatability";
    public final static String JSON_PROPERTYTYPE_INHERITED = "inherited";
    public final static String JSON_PROPERTYTYPE_REQUIRED = "required";
    public final static String JSON_PROPERTYTYPE_QUERYABLE = "queryable";
    public final static String JSON_PROPERTYTYPE_OPENCHOICE = "openChoice";

    public final static String JSON_PROPERTYTYPE_DEAULT_VALUE = "defaultValue";

    public final static String JSON_PROPERTYTYPE_MAX_LENGTH = "maxLength";
    public final static String JSON_PROPERTYTYPE_MIN_VALUE = "minValue";
    public final static String JSON_PROPERTYTYPE_MAX_VALUE = "maxValue";
    public final static String JSON_PROPERTYTYPE_MAX_PRECISION = "precision";
    public final static String JSON_PROPERTYTYPE_MAX_RESOLUTION = "resolution";

    public final static String JSON_PROPERTYTYPE_CHOICE_DISPLAYNAME = "displayName";
    public final static String JSON_PROPERTYTYPE_CHOICE_VALUE = "value";
    public final static String JSON_PROPERTYTYPE_CHOICE_CHOICE = "choice";

    public final static String JSON_TYPESLIST_TYPES = "types";
    public final static String JSON_TYPESLIST_HAS_MORE_ITEMS = "hasMoreItems";
    public final static String JSON_TYPESLIST_NUM_ITEMS = "numItems";

    public final static String JSON_TYPESCONTAINER_TYPE = "type";
    public final static String JSON_TYPESCONTAINER_CHILDREN = "children";
}
