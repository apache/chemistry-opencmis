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
package org.apache.chemistry.opencmis.commons.impl;

/**
 * JSON object constants.
 */
public class JSONConstants {

    public static final String ERROR_EXCEPTION = "exception";
    public static final String ERROR_MESSAGE = "message";
    public static final String ERROR_STACKTRACE = "stacktrace";

    public static final String REPINFO_ID = "repositoryId";
    public static final String REPINFO_NAME = "repositoryName";
    public static final String REPINFO_DESCRIPTION = "repositoryDescription";
    public static final String REPINFO_VENDOR = "vendorName";
    public static final String REPINFO_PRODUCT = "productName";
    public static final String REPINFO_PRODUCT_VERSION = "productVersion";
    public static final String REPINFO_ROOT_FOLDER_ID = "rootFolderId";
    public static final String REPINFO_REPOSITORY_URL = "repositoryUrl";
    public static final String REPINFO_ROOT_FOLDER_URL = "rootFolderUrl";
    public static final String REPINFO_CAPABILITIES = "capabilities";
    public static final String REPINFO_ACL_CAPABILITIES = "aclCapabilities";
    public static final String REPINFO_CHANGE_LOCK_TOKEN = "latestChangeLogToken";
    public static final String REPINFO_CMIS_VERSION_SUPPORTED = "cmisVersionSupported";
    public static final String REPINFO_THIN_CLIENT_URI = "thinClientURI";
    public static final String REPINFO_CHANGES_INCOMPLETE = "changesIncomplete";
    public static final String REPINFO_CHANGES_ON_TYPE = "changesOnType";
    public static final String REPINFO_PRINCIPAL_ID_ANONYMOUS = "principalIdAnonymous";
    public static final String REPINFO_PRINCIPAL_ID_ANYONE = "principalIdAnyone";

    public static final String JSON_CAP_CONTENT_STREAM_UPDATES = "capabilityContentStreamUpdatability";
    public static final String JSON_CAP_CHANGES = "capabilityChanges";
    public static final String JSON_CAP_RENDITIONS = "capabilityRenditions";
    public static final String JSON_CAP_GET_DESCENDANTS = "capabilityGetDescendants";
    public static final String JSON_CAP_GET_FOLDER_TREE = "capabilityGetFolderTree";
    public static final String JSON_CAP_MULTIFILING = "capabilityMultifiling";
    public static final String JSON_CAP_UNFILING = "capabilityUnfiling";
    public static final String JSON_CAP_VERSION_SPECIFIC_FILING = "capabilityVersionSpecificFiling";
    public static final String JSON_CAP_PWC_SEARCHABLE = "capabilityPWCSearchable";
    public static final String JSON_CAP_PWC_UPDATABLE = "capabilityPWCUpdatable";
    public static final String JSON_CAP_ALL_VERSIONS_SEARCHABLE = "capabilityAllVersionsSearchable";
    public static final String JSON_CAP_QUERY = "capabilityQuery";
    public static final String JSON_CAP_JOIN = "capabilityJoin";
    public static final String JSON_CAP_ACL = "capabilityACL";

    public static final String JSON_ACLCAP_SUPPORTED_PERMISSIONS = "supportedPermissions";
    public static final String JSON_ACLCAP_ACL_PROPAGATION = "propagation";
    public static final String JSON_ACLCAP_PERMISSIONS = "permissions";
    public static final String JSON_ACLCAP_PERMISSION_MAPPING = "permissionMapping";

    public static final String JSON_ACLCAP_PERMISSION_PERMISSION = "permission";
    public static final String JSON_ACLCAP_PERMISSION_DESCRIPTION = "description";

    public static final String JSON_ACLCAP_MAPPING_KEY = "key";
    public static final String JSON_ACLCAP_MAPPING_PERMISSION = "permission";

    public static final String JSON_OBJECT_PROPERTIES = "properties";
    public static final String JSON_OBJECT_ALLOWABLE_ACTIONS = "allowableActions";
    public static final String JSON_OBJECT_RELATIONSHIPS = "relationships";
    public static final String JSON_OBJECT_CHANGE_EVENT_INFO = "changeEventInfo";
    public static final String JSON_OBJECT_ACL = "acl";
    public static final String JSON_OBJECT_EXACT_ACL = "exactACL";
    public static final String JSON_OBJECT_POLICY_IDS = "policyIds";
    public static final String JSON_OBJECT_RENDITIONS = "renditions";

    public static final String JSON_OBJECTINFOLDER_OBJECT = "object";
    public static final String JSON_OBJECTINFOLDER_PATH_SEGMENT = "pathSegment";
    public static final String JSON_OBJECTPARENTS_OBJECT = "object";
    public static final String JSON_OBJECTPARENTS_RELATIVE_PATH_SEGMENT = "relativePathSegment";

    public static final String JSON_PROPERTY_ID = "id";
    public static final String JSON_PROPERTY_LOCALNAME = "localName";
    public static final String JSON_PROPERTY_DISPLAYNAME = "displayName";
    public static final String JSON_PROPERTY_QUERYNAME = "queryName";
    public static final String JSON_PROPERTY_VALUE = "value";
    public static final String JSON_PROPERTY_DATATYPE = "type";
    public static final String JSON_PROPERTY_CARDINALITY = "cardinality";

    public static final String JSON_CHANGE_EVENT_TYPE = "changeType";
    public static final String JSON_CHANGE_EVENT_TIME = "changeTime";

    public static final String JSON_ACL_ACES = "aces";
    public static final String JSON_ACL_IS_EXACT = "isExact";

    public static final String JSON_ACE_PRINCIPAL = "princial";
    public static final String JSON_ACE_PRINCIPAL_ID = "princialId";
    public static final String JSON_ACE_PERMISSIONS = "permissions";
    public static final String JSON_ACE_IS_DIRECT = "isDirect";

    public static final String JSON_RENDITION_STREAM_ID = "streamId";
    public static final String JSON_RENDITION_MIMETYPE = "mimeType";
    public static final String JSON_RENDITION_LENGTH = "length";
    public static final String JSON_RENDITION_KIND = "kind";
    public static final String JSON_RENDITION_TITLE = "title";
    public static final String JSON_RENDITION_HEIGHT = "height";
    public static final String JSON_RENDITION_WIDTH = "width";
    public static final String JSON_RENDITION_DOCUMENT_ID = "renditionDocumentId";

    public static final String JSON_OBJECTLIST_OBJECTS = "objects";
    public static final String JSON_OBJECTLIST_HAS_MORE_ITEMS = "hasMoreItems";
    public static final String JSON_OBJECTLIST_NUM_ITEMS = "numItems";

    public static final String JSON_OBJECTINFOLDERLIST_OBJECTS = "objects";
    public static final String JSON_OBJECTINFOLDERLIST_HAS_MORE_ITEMS = "hasMoreItems";
    public static final String JSON_OBJECTINFOLDERLIST_NUM_ITEMS = "numItems";

    public static final String JSON_OBJECTINFOLDERCONTAINER_OBJECT = "object";
    public static final String JSON_OBJECTINFOLDERCONTAINER_CHILDREN = "children";

    public static final String JSON_TYPE_ID = "id";
    public static final String JSON_TYPE_LOCALNAME = "localName";
    public static final String JSON_TYPE_LOCALNAMESPACE = "localNamespace";
    public static final String JSON_TYPE_DISPLAYNAME = "displayName";
    public static final String JSON_TYPE_QUERYNAME = "queryName";
    public static final String JSON_TYPE_DESCRIPTION = "description";
    public static final String JSON_TYPE_BASE_ID = "baseId";
    public static final String JSON_TYPE_PARENT_ID = "parentId";
    public static final String JSON_TYPE_CREATABLE = "creatable";
    public static final String JSON_TYPE_FILEABLE = "fileable";
    public static final String JSON_TYPE_QUERYABLE = "queryable";
    public static final String JSON_TYPE_FULLTEXT_INDEXED = "fulltextIndexed";
    public static final String JSON_TYPE_INCLUDE_IN_SUPERTYPE_QUERY = "includedInSupertypeQuery";
    public static final String JSON_TYPE_CONTROLABLE_POLICY = "controllablePolicy";
    public static final String JSON_TYPE_CONTROLABLE_ACL = "controllableACL";
    public static final String JSON_TYPE_PROPERTY_DEFINITIONS = "propertyDefinitions";

    public static final String JSON_TYPE_VERSIONABLE = "versionable"; // document
    public static final String JSON_TYPE_CONTENTSTREAM_ALLOWED = "contentStreamAllowed"; // document

    public static final String JSON_TYPE_ALLOWED_SOURCE_TYPES = "allowedSourceTypes"; // relationship
    public static final String JSON_TYPE_ALLOWED_TARGET_TYPES = "allowedTargetTypes"; // relationship

    public static final String JSON_PROPERTYTYPE_ID = "id";
    public static final String JSON_PROPERTYTYPE_LOCALNAME = "localName";
    public static final String JSON_PROPERTYTYPE_LOCALNAMESPACE = "localNamespace";
    public static final String JSON_PROPERTYTYPE_DISPLAYNAME = "displayName";
    public static final String JSON_PROPERTYTYPE_QUERYNAME = "queryName";
    public static final String JSON_PROPERTYTYPE_DESCRIPTION = "description";
    public static final String JSON_PROPERTYTYPE_PROPERTY_TYPE = "propertyType";
    public static final String JSON_PROPERTYTYPE_CARDINALITY = "cardinality";
    public static final String JSON_PROPERTYTYPE_UPDATABILITY = "updatability";
    public static final String JSON_PROPERTYTYPE_INHERITED = "inherited";
    public static final String JSON_PROPERTYTYPE_REQUIRED = "required";
    public static final String JSON_PROPERTYTYPE_QUERYABLE = "queryable";
    public static final String JSON_PROPERTYTYPE_OPENCHOICE = "openChoice";

    public static final String JSON_PROPERTYTYPE_DEAULT_VALUE = "defaultValue";

    public static final String JSON_PROPERTYTYPE_MAX_LENGTH = "maxLength";
    public static final String JSON_PROPERTYTYPE_MIN_VALUE = "minValue";
    public static final String JSON_PROPERTYTYPE_MAX_VALUE = "maxValue";
    public static final String JSON_PROPERTYTYPE_MAX_PRECISION = "precision";
    public static final String JSON_PROPERTYTYPE_MAX_RESOLUTION = "resolution";

    public static final String JSON_PROPERTYTYPE_CHOICE_DISPLAYNAME = "displayName";
    public static final String JSON_PROPERTYTYPE_CHOICE_VALUE = "value";
    public static final String JSON_PROPERTYTYPE_CHOICE_CHOICE = "choice";

    public static final String JSON_TYPESLIST_TYPES = "types";
    public static final String JSON_TYPESLIST_HAS_MORE_ITEMS = "hasMoreItems";
    public static final String JSON_TYPESLIST_NUM_ITEMS = "numItems";

    public static final String JSON_TYPESCONTAINER_TYPE = "type";
    public static final String JSON_TYPESCONTAINER_CHILDREN = "children";

    // Constant utility class.
    private JSONConstants() {
    }

}
