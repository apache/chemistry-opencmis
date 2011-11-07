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

import static org.apache.chemistry.opencmis.server.impl.browser.json.JSONConstants.*;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AclCapabilities;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ChangeEventInfo;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.PropertyBoolean;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyDateTime;
import org.apache.chemistry.opencmis.commons.data.PropertyDecimal;
import org.apache.chemistry.opencmis.commons.data.PropertyHtml;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyInteger;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.data.PropertyUri;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.RelationshipTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils;
import org.apache.chemistry.opencmis.server.impl.browser.TypeCache;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * OpenCMIS objects to JSON converter.
 */
public class JSONConverter {

    /**
     * Private constructor.
     */
    private JSONConverter() {
    }

    /**
     * Converts a repository info object.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(RepositoryInfo repositoryInfo, HttpServletRequest request) {
        if (repositoryInfo == null) {
            return null;
        }

        JSONObject result = new JSONObject();

        result.put(REPINFO_ID, repositoryInfo.getId());
        result.put(REPINFO_NAME, repositoryInfo.getName());
        result.put(REPINFO_DESCRIPTION, repositoryInfo.getDescription());
        result.put(REPINFO_VENDOR, repositoryInfo.getVendorName());
        result.put(REPINFO_PRODUCT, repositoryInfo.getProductName());
        result.put(REPINFO_PRODUCT_VERSION, repositoryInfo.getProductVersion());
        result.put(REPINFO_ROOT_FOLDER_ID, repositoryInfo.getRootFolderId());
        result.put(REPINFO_CAPABILITIES, convert(repositoryInfo.getCapabilities()));
        result.put(REPINFO_ACL_CAPABILITIES, convert(repositoryInfo.getAclCapabilities()));
        result.put(REPINFO_CHANGE_LOCK_TOKEN, repositoryInfo.getLatestChangeLogToken());
        result.put(REPINFO_CMIS_VERSION_SUPPORTED, repositoryInfo.getCmisVersionSupported());
        result.put(REPINFO_THIN_CLIENT_URI, repositoryInfo.getThinClientUri());
        result.put(REPINFO_CHANGES_INCOMPLETE, repositoryInfo.getChangesIncomplete());

        if (repositoryInfo.getChangesOnType() != null) {
            JSONArray changesOnType = new JSONArray();

            for (BaseTypeId type : repositoryInfo.getChangesOnType()) {
                changesOnType.add(getJSONStringValue(type));
            }

            result.put(REPINFO_CHANGES_ON_TYPE, changesOnType);
        }

        result.put(REPINFO_PRINCIPAL_ID_ANONYMOUS, repositoryInfo.getPrincipalIdAnonymous());
        result.put(REPINFO_PRINCIPAL_ID_ANYONE, repositoryInfo.getPrincipalIdAnyone());

        result.put(REPINFO_REPOSITORY_URL, BrowserBindingUtils.compileRepositoryUrl(request, repositoryInfo.getId())
                .toString());
        result.put(REPINFO_ROOT_FOLDER_URL, BrowserBindingUtils.compileRootUrl(request, repositoryInfo.getId())
                .toString());

        return result;
    }

    /**
     * Converts a capabilities object.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(RepositoryCapabilities capabilities) {
        if (capabilities == null) {
            return null;
        }

        JSONObject result = new JSONObject();

        result.put(JSON_CAP_CONTENT_STREAM_UPDATES, getJSONStringValue(capabilities.getContentStreamUpdatesCapability()
                .value()));
        result.put(JSON_CAP_CHANGES, getJSONStringValue(capabilities.getChangesCapability().value()));
        result.put(JSON_CAP_RENDITIONS, getJSONStringValue(capabilities.getRenditionsCapability().value()));
        result.put(JSON_CAP_GET_DESCENDANTS, capabilities.isGetDescendantsSupported());
        result.put(JSON_CAP_GET_FOLDER_TREE, capabilities.isGetFolderTreeSupported());
        result.put(JSON_CAP_MULTIFILING, capabilities.isMultifilingSupported());
        result.put(JSON_CAP_UNFILING, capabilities.isUnfilingSupported());
        result.put(JSON_CAP_VERSION_SPECIFIC_FILING, capabilities.isVersionSpecificFilingSupported());
        result.put(JSON_CAP_PWC_SEARCHABLE, capabilities.isPwcSearchableSupported());
        result.put(JSON_CAP_PWC_UPDATABLE, capabilities.isPwcUpdatableSupported());
        result.put(JSON_CAP_ALL_VERSIONS_SEARCHABLE, capabilities.isAllVersionsSearchableSupported());
        result.put(JSON_CAP_QUERY, getJSONStringValue(capabilities.getQueryCapability().value()));
        result.put(JSON_CAP_JOIN, getJSONStringValue(capabilities.getJoinCapability().value()));
        result.put(JSON_CAP_ACL, getJSONStringValue(capabilities.getAclCapability().value()));

        return result;
    }

    /**
     * Converts an ACL capabilities object.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(AclCapabilities capabilities) {
        if (capabilities == null) {
            return null;
        }

        JSONObject result = new JSONObject();

        result.put(JSON_ACLCAP_SUPPORTED_PERMISSIONS,
                getJSONStringValue(capabilities.getSupportedPermissions().value()));
        result.put(JSON_ACLCAP_ACL_PROPAGATION, getJSONStringValue(capabilities.getAclPropagation().value()));

        // permissions
        if (capabilities.getPermissions() != null) {
            JSONArray permissions = new JSONArray();

            for (PermissionDefinition permDef : capabilities.getPermissions()) {
                JSONObject permission = new JSONObject();
                permission.put(JSON_ACLCAP_PERMISSION_PERMISSION, permDef.getId());
                permission.put(JSON_ACLCAP_PERMISSION_DESCRIPTION, permDef.getDescription());

                permissions.add(permission);
            }

            result.put(JSON_ACLCAP_PERMISSIONS, permissions);
        }

        // permission mapping

        if (capabilities.getPermissionMapping() != null) {
            JSONArray permissionMapping = new JSONArray();

            for (PermissionMapping permMap : capabilities.getPermissionMapping().values()) {
                JSONArray mappingPermissions = new JSONArray();
                if (permMap.getPermissions() != null) {
                    for (String p : permMap.getPermissions()) {
                        mappingPermissions.add(p);
                    }
                }

                JSONObject mapping = new JSONObject();
                mapping.put(JSON_ACLCAP_MAPPING_KEY, permMap.getKey());
                mapping.put(JSON_ACLCAP_MAPPING_PERMISSION, mappingPermissions);

                permissionMapping.add(mapping);
            }

            result.put(JSON_ACLCAP_PERMISSION_MAPPING, permissionMapping);
        }

        return result;
    }

    /**
     * Converts an object.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(ObjectData object, TypeCache typeCache) {
        if (object == null) {
            return null;
        }

        JSONObject result = new JSONObject();

        // properties
        if (object.getProperties() != null) {
            JSONObject properties = new JSONObject();

            for (PropertyData<?> property : object.getProperties().getPropertyList()) {
                TypeDefinition type = null;
                if (typeCache != null) {
                    type = typeCache.getTypeDefinitionForObject(object.getId());
                }

                PropertyDefinition<?> propDef = null;
                if (type != null) {
                    propDef = type.getPropertyDefinitions().get(property.getId());
                }

                properties.put(property.getId(), convert(property, propDef));
            }

            result.put(JSON_OBJECT_PROPERTIES, properties);
        }

        // allowable actions
        if (object.getAllowableActions() != null) {
            result.put(JSON_OBJECT_ALLOWABLE_ACTIONS, convert(object.getAllowableActions()));
        }

        // relationships
        if (object.getRelationships() != null) {
            JSONArray relationships = new JSONArray();

            for (ObjectData relationship : object.getRelationships()) {
                relationships.add(convert(relationship, typeCache));
            }

            result.put(JSON_OBJECT_RELATIONSHIPS, relationships);
        }

        // change event info
        if (object.getChangeEventInfo() != null) {
            JSONObject changeEventInfo = new JSONObject();

            ChangeEventInfo cei = object.getChangeEventInfo();
            changeEventInfo.put(JSON_CHANGE_EVENT_TYPE, getJSONStringValue(cei.getChangeType().value()));
            changeEventInfo.put(JSON_CHANGE_EVENT_TIME, getJSONValue(cei.getChangeTime()));

            result.put(JSON_OBJECT_CHANGE_EVENT_INFO, changeEventInfo);
        }

        // ACL
        if ((object.getAcl() != null) && (object.getAcl().getAces() != null)) {
            result.put(JSON_OBJECT_ACL, convert(object.getAcl()));
            result.put(JSON_OBJECT_EXACT_ACL, object.isExactAcl());
        }

        // policy ids
        if ((object.getPolicyIds() != null) && (object.getPolicyIds().getPolicyIds() != null)) {
            JSONArray policyIds = new JSONArray();

            for (String pi : object.getPolicyIds().getPolicyIds()) {
                policyIds.add(pi);
            }

            result.put(JSON_OBJECT_POLICY_IDS, policyIds);
        }

        // renditions
        if (object.getRenditions() != null) {
            JSONArray renditions = new JSONArray();

            for (RenditionData rendition : object.getRenditions()) {
                renditions.add(convert(rendition));
            }

            result.put(JSON_OBJECT_RENDITIONS, renditions);
        }

        return result;
    }

    /**
     * Converts a property.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(PropertyData<?> property, PropertyDefinition<?> propDef) {
        if (property == null) {
            return null;
        }

        JSONObject result = new JSONObject();

        result.put(JSON_PROPERTY_ID, property.getId());
        result.put(JSON_PROPERTY_LOCALNAME, property.getLocalName());
        result.put(JSON_PROPERTY_DISPLAYNAME, property.getDisplayName());
        result.put(JSON_PROPERTY_QUERYNAME, property.getQueryName());

        if (propDef != null) {
            result.put(JSON_PROPERTY_DATATYPE, propDef.getPropertyType().value());
            result.put(JSON_PROPERTY_CARDINALITY, propDef.getCardinality().value());

            if ((property.getValues() == null) || (property.getValues().size() == 0)) {
                result.put(JSON_PROPERTY_VALUE, null);
            } else if (propDef.getCardinality() == Cardinality.SINGLE) {
                result.put(JSON_PROPERTY_VALUE, getJSONValue(property.getValues().get(0)));
            } else {
                JSONArray values = new JSONArray();

                for (Object value : property.getValues()) {
                    values.add(getJSONValue(value));
                }

                result.put(JSON_PROPERTY_VALUE, values);
            }
        } else {
            result.put(JSON_PROPERTY_DATATYPE, getJSONPropertyDataType(property));

            if ((property.getValues() == null) || (property.getValues().size() == 0)) {
                result.put(JSON_PROPERTY_VALUE, null);
            } else if (property.getValues().size() > 0) {
                JSONArray values = new JSONArray();

                for (Object value : property.getValues()) {
                    values.add(getJSONValue(value));
                }

                result.put(JSON_PROPERTY_VALUE, values);
            }
        }

        return result;
    }

    /**
     * Converts allowable actions.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(AllowableActions allowableActions) {
        if (allowableActions == null) {
            return null;
        }

        JSONObject result = new JSONObject();

        Set<Action> actionSet = allowableActions.getAllowableActions();
        for (Action action : Action.values()) {
            result.put(action.value(), actionSet.contains(action));
        }

        return result;
    }

    /**
     * Converts an ACL.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(Acl acl) {
        if ((acl == null) || (acl.getAces() == null)) {
            return null;
        }

        JSONArray aceObjects = new JSONArray();

        for (Ace ace : acl.getAces()) {
            JSONArray permissions = new JSONArray();
            if (ace.getPermissions() != null) {
                for (String p : ace.getPermissions()) {
                    permissions.add(p);
                }
            }

            JSONObject aceObject = new JSONObject();
            JSONObject principalObjecy = new JSONObject();
            principalObjecy.put(JSON_ACE_PRINCIPAL_ID, ace.getPrincipalId());
            aceObject.put(JSON_ACE_PRINCIPAL, principalObjecy);
            aceObject.put(JSON_ACE_PERMISSIONS, permissions);
            aceObject.put(JSON_ACE_IS_DIRECT, ace.isDirect());

            aceObjects.add(aceObject);
        }

        JSONObject result = new JSONObject();
        result.put(JSON_ACL_ACES, aceObjects);
        result.put(JSON_ACL_IS_EXACT, acl.isExact());

        return result;
    }

    /**
     * Converts a rendition.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(RenditionData rendition) {
        if (rendition == null) {
            return null;
        }

        JSONObject result = new JSONObject();

        result.put(JSON_RENDITION_STREAM_ID, rendition.getStreamId());
        result.put(JSON_RENDITION_MIMETYPE, rendition.getMimeType());
        result.put(JSON_RENDITION_LENGTH, rendition.getBigLength());
        result.put(JSON_RENDITION_KIND, rendition.getKind());
        result.put(JSON_RENDITION_TITLE, rendition.getTitle());
        result.put(JSON_RENDITION_HEIGHT, rendition.getBigHeight());
        result.put(JSON_RENDITION_WIDTH, rendition.getBigWidth());
        result.put(JSON_RENDITION_DOCUMENT_ID, rendition.getRenditionDocumentId());

        return result;
    }

    /**
     * Converts a query object list.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(ObjectList list) {
        JSONObject result = new JSONObject();

        if (list != null) {
            JSONArray objects = new JSONArray();
            if (list.getObjects() != null) {
                for (ObjectData object : list.getObjects()) {
                    objects.add(convert(object, null));
                }
            }

            result.put(JSON_OBJECTLIST_OBJECTS, objects);

            if (list.hasMoreItems() != null) {
                result.put(JSON_OBJECTLIST_HAS_MORE_ITEMS, list.hasMoreItems());
            }
            if (list.getNumItems() != null) {
                result.put(JSON_OBJECTLIST_NUM_ITEMS, list.getNumItems());
            }
        }

        return result;
    }

    /**
     * Converts an object in a folder list.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(ObjectInFolderData objectInFolder, TypeCache typeCache) {
        if ((objectInFolder == null) || (objectInFolder.getObject() == null)) {
            return null;
        }

        JSONObject result = new JSONObject();
        result.put(JSON_OBJECTINFOLDER_OBJECT, convert(objectInFolder.getObject(), typeCache));
        if (objectInFolder.getPathSegment() != null) {
            result.put(JSON_OBJECTINFOLDER_PATH_SEGMENT, objectInFolder.getPathSegment());
        }

        return result;
    }

    /**
     * Converts a folder list.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(ObjectInFolderList objectInFolderList, TypeCache typeCache) {
        if (objectInFolderList == null) {
            return null;
        }

        JSONObject result = new JSONObject();

        if (objectInFolderList.getObjects() != null) {
            JSONArray objects = new JSONArray();

            for (ObjectInFolderData object : objectInFolderList.getObjects()) {
                objects.add(convert(object, typeCache));
            }

            result.put(JSON_OBJECTINFOLDERLIST_OBJECTS, objects);
        }

        if (objectInFolderList.hasMoreItems() != null) {
            result.put(JSON_OBJECTINFOLDERLIST_HAS_MORE_ITEMS, objectInFolderList.hasMoreItems());
        }
        if (objectInFolderList.getNumItems() != null) {
            result.put(JSON_OBJECTINFOLDERLIST_NUM_ITEMS, objectInFolderList.getNumItems());
        }

        return result;
    }

    /**
     * Converts a folder container.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(ObjectInFolderContainer container, TypeCache typeCache) {
        if (container == null) {
            return null;
        }

        JSONObject result = new JSONObject();
        result.put(JSON_OBJECTINFOLDERCONTAINER_OBJECT, convert(container.getObject(), typeCache));

        if ((container.getChildren() != null) && (container.getChildren().size() > 0)) {
            JSONArray children = new JSONArray();
            for (ObjectInFolderContainer descendant : container.getChildren()) {
                children.add(JSONConverter.convert(descendant, typeCache));
            }

            result.put(JSON_OBJECTINFOLDERCONTAINER_CHILDREN, children);
        }

        return result;
    }

    /**
     * Converts an object parent.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(ObjectParentData parent, TypeCache typeCache) {
        if ((parent == null) || (parent.getObject() == null)) {
            return null;
        }

        JSONObject result = new JSONObject();
        result.put(JSON_OBJECTPARENTS_OBJECT, convert(parent.getObject(), typeCache));
        if (parent.getRelativePathSegment() != null) {
            result.put(JSON_OBJECTPARENTS_RELATIVE_PATH_SEGMENT, parent.getRelativePathSegment());
        }

        return result;
    }

    /**
     * Converts a type definition.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(TypeDefinition type) {
        if (type == null) {
            return null;
        }

        JSONObject result = new JSONObject();
        result.put(JSON_TYPE_ID, type.getId());
        result.put(JSON_TYPE_LOCALNAME, type.getLocalName());
        result.put(JSON_TYPE_LOCALNAMESPACE, type.getLocalNamespace());
        result.put(JSON_TYPE_DISPLAYNAME, type.getDisplayName());
        result.put(JSON_TYPE_QUERYNAME, type.getQueryName());
        result.put(JSON_TYPE_DESCRIPTION, type.getDescription());
        result.put(JSON_TYPE_BASE_ID, type.getBaseTypeId().value());
        result.put(JSON_TYPE_PARENT_ID, type.getParentTypeId());
        result.put(JSON_TYPE_CREATABLE, type.isCreatable());
        result.put(JSON_TYPE_FILEABLE, type.isFileable());
        result.put(JSON_TYPE_QUERYABLE, type.isQueryable());
        result.put(JSON_TYPE_FULLTEXT_INDEXED, type.isFulltextIndexed());
        result.put(JSON_TYPE_INCLUDE_IN_SUPERTYPE_QUERY, type.isIncludedInSupertypeQuery());
        result.put(JSON_TYPE_CONTROLABLE_POLICY, type.isControllablePolicy());
        result.put(JSON_TYPE_CONTROLABLE_ACL, type.isControllableAcl());

        if (type instanceof DocumentTypeDefinition) {
            result.put(JSON_TYPE_VERSIONABLE, ((DocumentTypeDefinition) type).isVersionable());
            result.put(JSON_TYPE_CONTENTSTREAM_ALLOWED, ((DocumentTypeDefinition) type).getContentStreamAllowed()
                    .value());
        }

        if (type instanceof RelationshipTypeDefinition) {
            result.put(JSON_TYPE_ALLOWED_SOURCE_TYPES,
                    getJSONArrayFromList(((RelationshipTypeDefinition) type).getAllowedSourceTypeIds()));
            result.put(JSON_TYPE_ALLOWED_TARGET_TYPES,
                    getJSONArrayFromList(((RelationshipTypeDefinition) type).getAllowedTargetTypeIds()));
        }

        if ((type.getPropertyDefinitions() != null) && (!type.getPropertyDefinitions().isEmpty())) {
            JSONObject propertyDefs = new JSONObject();

            for (PropertyDefinition<?> pd : type.getPropertyDefinitions().values()) {
                propertyDefs.put(pd.getId(), convert(pd));
            }

            result.put(JSON_TYPE_PROPERTY_DEFINITIONS, propertyDefs);
        }

        return result;
    }

    /**
     * Converts a property type definition.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(PropertyDefinition<?> propertyDef) {
        if (propertyDef == null) {
            return null;
        }

        JSONObject result = new JSONObject();
        result.put(JSON_PROPERTYTYPE_ID, propertyDef.getId());
        result.put(JSON_PROPERTYTYPE_LOCALNAME, propertyDef.getLocalName());
        result.put(JSON_PROPERTYTYPE_LOCALNAMESPACE, propertyDef.getLocalName());
        result.put(JSON_PROPERTYTYPE_DISPLAYNAME, propertyDef.getDisplayName());
        result.put(JSON_PROPERTYTYPE_QUERYNAME, propertyDef.getQueryName());
        result.put(JSON_PROPERTYTYPE_DESCRIPTION, propertyDef.getDescription());
        result.put(JSON_PROPERTYTYPE_PROPERTY_TYPE, propertyDef.getPropertyType().value());
        result.put(JSON_PROPERTYTYPE_CARDINALITY, propertyDef.getCardinality().value());
        result.put(JSON_PROPERTYTYPE_UPDATABILITY, propertyDef.getUpdatability().value());
        result.put(JSON_PROPERTYTYPE_INHERITED, propertyDef.isInherited());
        result.put(JSON_PROPERTYTYPE_REQUIRED, propertyDef.isRequired());
        result.put(JSON_PROPERTYTYPE_QUERYABLE, propertyDef.isQueryable());
        result.put(JSON_PROPERTYTYPE_OPENCHOICE, propertyDef.isOpenChoice());

        // TODO: add type specific details
        // TODO: add choice

        return result;
    }

    /**
     * Converts a type definition list.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(TypeDefinitionList list) {
        if (list == null) {
            return null;
        }

        JSONObject result = new JSONObject();

        if (list.getList() != null) {
            JSONArray objects = new JSONArray();

            for (TypeDefinition type : list.getList()) {
                objects.add(convert(type));
            }

            result.put(JSON_TYPESLIST_TYPES, objects);
        }

        result.put(JSON_TYPESLIST_HAS_MORE_ITEMS, list.hasMoreItems());
        result.put(JSON_TYPESLIST_NUM_ITEMS, list.getNumItems());

        return result;
    }

    /**
     * Converts a type definition container.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(TypeDefinitionContainer container) {
        if (container == null) {
            return null;
        }

        JSONObject result = new JSONObject();
        result.put(JSON_TYPESCONTAINER_TYPE, convert(container.getTypeDefinition()));

        if ((container.getChildren() != null) && (container.getChildren().size() > 0)) {
            JSONArray children = new JSONArray();
            for (TypeDefinitionContainer child : container.getChildren()) {
                children.add(JSONConverter.convert(child));
            }

            result.put(JSON_TYPESCONTAINER_CHILDREN, children);
        }

        return result;
    }

    // -----------------------------------------------------------------

    public static String getJSONStringValue(Object obj) {
        if (obj == null) {
            return null;
        }

        return obj.toString();
    }

    public static Object getJSONValue(Object value) {
        if (value instanceof GregorianCalendar) {
            return ((GregorianCalendar) value).getTimeInMillis();
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    public static JSONArray getJSONArrayFromList(List<?> list) {
        if (list == null) {
            return null;
        }

        JSONArray result = new JSONArray();
        result.addAll(list);

        return result;
    }

    public static String getJSONPropertyDataType(PropertyData<?> property) {
        if (property instanceof PropertyBoolean) {
            return PropertyType.BOOLEAN.value();
        } else if (property instanceof PropertyId) {
            return PropertyType.ID.value();
        } else if (property instanceof PropertyInteger) {
            return PropertyType.INTEGER.value();
        } else if (property instanceof PropertyDateTime) {
            return PropertyType.DATETIME.value();
        } else if (property instanceof PropertyDecimal) {
            return PropertyType.DECIMAL.value();
        } else if (property instanceof PropertyHtml) {
            return PropertyType.HTML.value();
        } else if (property instanceof PropertyString) {
            return PropertyType.STRING.value();
        } else if (property instanceof PropertyUri) {
            return PropertyType.URI.value();
        }

        return null;
    }
}
