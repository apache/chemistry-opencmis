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

import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_CARDINALITY;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_DESCRIPTION;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_DISPLAYNAME;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_ID;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_INHERITED;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_LOCALNAME;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_LOCALNAMESPACE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_MAX_LENGTH;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_MAX_VALUE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_MIN_VALUE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_OPENCHOICE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_ORDERABLE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_PRECISION;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_QUERYABLE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_QUERYNAME;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_REQUIRED;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_RESOLUTION;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_TYPE_UPDATABILITY;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ACLCAP_ACL_PROPAGATION;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ACLCAP_SUPPORTED_PERMISSIONS;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CAP_ACL;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CAP_ALL_VERSIONS_SEARCHABLE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CAP_CHANGES;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CAP_CONTENT_STREAM_UPDATES;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CAP_GET_DESCENDANTS;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CAP_GET_FOLDER_TREE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CAP_JOIN;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CAP_MULTIFILING;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CAP_PWC_SEARCHABLE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CAP_PWC_UPDATABLE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CAP_QUERY;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CAP_RENDITIONS;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CAP_UNFILING;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CAP_VERSION_SPECIFIC_FILING;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_RENDITION_DOCUMENT_ID;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_RENDITION_HEIGHT;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_RENDITION_KIND;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_RENDITION_LENGTH;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_RENDITION_MIMETYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_RENDITION_STREAM_ID;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_RENDITION_TITLE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_RENDITION_WIDTH;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_CHANGES_INCOMPLETE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_CHANGE_LOCK_TOKEN;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_CMIS_VERSION_SUPPORTED;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_DESCRIPTION;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_ID;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_NAME;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_PRODUCT;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_PRODUCT_VERSION;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_REPOSITORY_URL;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_ROOT_FOLDER_ID;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_ROOT_FOLDER_URL;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_THIN_CLIENT_URI;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_VENDOR;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_BASE_ID;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_CONTENTSTREAM_ALLOWED;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_CONTROLABLE_ACL;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_CONTROLABLE_POLICY;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_CREATABLE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_DESCRIPTION;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_DISPLAYNAME;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_FILEABLE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_FULLTEXT_INDEXED;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_ID;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_INCLUDE_IN_SUPERTYPE_QUERY;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_LOCALNAME;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_LOCALNAMESPACE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_PARENT_ID;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_QUERYABLE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_QUERYNAME;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TYPE_VERSIONABLE;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AclCapabilities;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.AtomPropertyType;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.DateTimeResolution;
import org.apache.chemistry.opencmis.commons.enums.DecimalPrecision;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.SupportedPermissions;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AclCapabilitiesDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PolicyTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RelationshipTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RenditionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoBrowserBindingImpl;

public class AtomPubConverter {

	public static RepositoryInfo convertRepositoryInfo(Map<String, String> repositoryInfoRawValues, Map<String, String> repositoryCapabilitiesRawValues, AclCapabilities aclCapabilities,
			List<String> changesOnType, List<CmisExtensionElement> extensions) {
		if (repositoryInfoRawValues == null || repositoryCapabilitiesRawValues == null) {
			return null;
		}

		RepositoryInfoBrowserBindingImpl result = new RepositoryInfoBrowserBindingImpl();

		result.setId(getString(repositoryInfoRawValues, TAG_REPINFO_ID));
		result.setName(getString(repositoryInfoRawValues, TAG_REPINFO_NAME));
		result.setDescription(getString(repositoryInfoRawValues, TAG_REPINFO_DESCRIPTION));
		result.setVendorName(getString(repositoryInfoRawValues, TAG_REPINFO_VENDOR));
		result.setProductName(getString(repositoryInfoRawValues, TAG_REPINFO_PRODUCT));
		result.setProductVersion(getString(repositoryInfoRawValues, TAG_REPINFO_PRODUCT_VERSION));
		result.setRootFolder(getString(repositoryInfoRawValues, TAG_REPINFO_ROOT_FOLDER_ID));
		result.setRepositoryUrl(getString(repositoryInfoRawValues, TAG_REPINFO_REPOSITORY_URL));
		result.setRootUrl(getString(repositoryInfoRawValues, TAG_REPINFO_ROOT_FOLDER_URL));
		result.setCapabilities(convertRepositoryCapabilities(repositoryCapabilitiesRawValues));
		result.setAclCapabilities(aclCapabilities);
		result.setLatestChangeLogToken(getString(repositoryInfoRawValues, TAG_REPINFO_CHANGE_LOCK_TOKEN));
		result.setCmisVersionSupported(getString(repositoryInfoRawValues, TAG_REPINFO_CMIS_VERSION_SUPPORTED));
		result.setThinClientUri(getString(repositoryInfoRawValues, TAG_REPINFO_THIN_CLIENT_URI));
		result.setChangesIncomplete(getBoolean(repositoryInfoRawValues, TAG_REPINFO_CHANGES_INCOMPLETE));

		List<BaseTypeId> types = new ArrayList<BaseTypeId>();
		if (changesOnType != null) {
			for (Object type : changesOnType) {
				if (type != null) {
					types.add(BaseTypeId.fromValue(type.toString()));
				}
			}
		}
		result.setChangesOnType(types);

		result.setPrincipalAnonymous(getString(repositoryInfoRawValues, "principalAnonymous"));
		result.setPrincipalAnyone(getString(repositoryInfoRawValues, "principalAnyone"));

		// handle extensions
		result.setExtensions(extensions);

		return result;
	}

	public static RepositoryCapabilities convertRepositoryCapabilities(Map<String, String> values) {
		if (values == null) {
			return null;
		}

		RepositoryCapabilitiesImpl result = new RepositoryCapabilitiesImpl();

		result.setCapabilityContentStreamUpdates(getEnum(values, TAG_CAP_CONTENT_STREAM_UPDATES, CapabilityContentStreamUpdates.class));
		result.setCapabilityChanges(getEnum(values, TAG_CAP_CHANGES, CapabilityChanges.class));
		result.setCapabilityRendition(getEnum(values, TAG_CAP_RENDITIONS, CapabilityRenditions.class));
		result.setSupportsGetDescendants(getBoolean(values, TAG_CAP_GET_DESCENDANTS));
		result.setSupportsGetFolderTree(getBoolean(values, TAG_CAP_GET_FOLDER_TREE));
		result.setSupportsMultifiling(getBoolean(values, TAG_CAP_MULTIFILING));
		result.setSupportsUnfiling(getBoolean(values, TAG_CAP_UNFILING));
		result.setSupportsVersionSpecificFiling(getBoolean(values, TAG_CAP_VERSION_SPECIFIC_FILING));
		result.setIsPwcSearchable(getBoolean(values, TAG_CAP_PWC_SEARCHABLE));
		result.setIsPwcUpdatable(getBoolean(values, TAG_CAP_PWC_UPDATABLE));
		result.setAllVersionsSearchable(getBoolean(values, TAG_CAP_ALL_VERSIONS_SEARCHABLE));
		result.setCapabilityQuery(getEnum(values, TAG_CAP_QUERY, CapabilityQuery.class));
		result.setCapabilityJoin(getEnum(values, TAG_CAP_JOIN, CapabilityJoin.class));
		result.setCapabilityAcl(getEnum(values, TAG_CAP_ACL, CapabilityAcl.class));

		// handle extensions
		// convertExtension(values, result, CAP_KEYS);

		return result;
	}

	public static AclCapabilities convertAclCapabilities(Map<String, String> AclCapabilitiesRawValue, List<PermissionDefinition> permissionDefinitionList,
			Map<String, PermissionMapping> permissionMapping) {
		if (AclCapabilitiesRawValue == null) {
			return null;
		}

		AclCapabilitiesDataImpl result = new AclCapabilitiesDataImpl();

		result.setSupportedPermissions(getEnum(AclCapabilitiesRawValue, TAG_ACLCAP_SUPPORTED_PERMISSIONS, SupportedPermissions.class));
		result.setAclPropagation(getEnum(AclCapabilitiesRawValue, TAG_ACLCAP_ACL_PROPAGATION, AclPropagation.class));

		if (permissionDefinitionList != null) {
			result.setPermissionDefinitionData(permissionDefinitionList);
		}

		if (permissionMapping != null) {
			result.setPermissionMappingData(permissionMapping);
		}

		// handle extensions
		// convertExtension(json, result, ACLCAP_KEYS);

		return result;
	}

	public static final String PROPERTY = "property";

	/**
	 * Converts an object.
	 */
	public static ObjectData convertObject(Properties properties, AllowableActions allowableActions, Acl acl, List<RenditionData> renditions, List<CmisExtensionElement> extensions) {
		if (properties == null) {
			return null;
		}

		ObjectDataImpl result = new ObjectDataImpl();

		// TODO ACL Is exact ?
		result.setAcl(acl);
		result.setAllowableActions(allowableActions);
		/*
		 * Map<String, Object> jsonChangeEventInfo = getMap(json.get(JSON_OBJECT_CHANGE_EVENT_INFO)); if (jsonChangeEventInfo != null) { ChangeEventInfoDataImpl changeEventInfo = new
		 * ChangeEventInfoDataImpl();
		 * 
		 * changeEventInfo.setChangeTime(getDateTime(jsonChangeEventInfo, JSON_CHANGE_EVENT_TIME)); changeEventInfo.setChangeType(getEnum(jsonChangeEventInfo, JSON_CHANGE_EVENT_TYPE,
		 * ChangeType.class));
		 * 
		 * convertExtension(json, result, CHANGE_EVENT_KEYS);
		 * 
		 * result.setChangeEventInfo(changeEventInfo); } result.setIsExactAcl(getBoolean(json, JSON_OBJECT_EXACT_ACL)); result. setPolicyIds(convertPolicyIds(getList(json.get(JSON_OBJECT_POLICY_IDS
		 * ))));
		 */
		result.setProperties(properties);
		/*
		 * List<Object> jsonRelationships = getList(json.get(JSON_OBJECT_RELATIONSHIPS)); if (jsonRelationships != null) { result.setRelationships(convertObjects(jsonRelationships)); }
		 */
		if (renditions != null) {
			result.setRenditions(renditions);
		}

		result.setExtensions(extensions);

		return result;
	}

	public static final String PROPERTY_DATATYPE = "type";
	public static final String PROPERTY_DEFINITION_ID = "propertyDefinitionId";
	public static final String PROPERTY_LOCALNAME = "localName";
	public static final String PROPERTY_DISPLAYNAME = "displayName";
	public static final String PROPERTY_QUERYNAME = "queryName";
	public static final String PROPERTY_VALUE = "value";

	/**
	 * Converts properties.
	 */
	public static PropertyData<?> convertProperty(String propertyTypeValue, String id, String displayName, String queryName, String localName, List<String> values) {

		AbstractPropertyData<?> property = null;

		AtomPropertyType propertyType = null;
		try {
			propertyType = AtomPropertyType.fromValue(propertyTypeValue);
		} catch (Exception e) {
			throw new CmisRuntimeException("Invalid property: " + id);
		}

		switch (propertyType) {
		case STRING:
			property = new PropertyStringImpl();
			{
				List<String> propertyValues = null;
				if (values != null) {
					propertyValues = new ArrayList<String>();
					for (String obj : values) {
						propertyValues.add(obj);
					}
				}
				((PropertyStringImpl) property).setValues(propertyValues);
			}
			break;
		case ID:
			property = new PropertyIdImpl();
			{
				List<String> propertyValues = null;
				if (values != null) {
					propertyValues = new ArrayList<String>();
					for (String obj : values) {
						propertyValues.add(obj);
					}
				}
				((PropertyIdImpl) property).setValues(propertyValues);
			}
			break;
		case BOOLEAN:
			property = new PropertyBooleanImpl();
			{
				List<Boolean> propertyValues = null;
				if (values != null) {
					propertyValues = new ArrayList<Boolean>();
					for (String obj : values) {
						propertyValues.add(Boolean.parseBoolean(obj));
					}
				}
				((PropertyBooleanImpl) property).setValues(propertyValues);
			}
			break;
		case INTEGER:
			property = new PropertyIntegerImpl();
			{
				List<BigInteger> propertyValues = null;
				if (values != null) {
					propertyValues = new ArrayList<BigInteger>();
					for (String obj : values) {
						propertyValues.add(BigInteger.valueOf(Long.parseLong(obj)));
					}
				}
				((PropertyIntegerImpl) property).setValues(propertyValues);
			}
			break;
		case DECIMAL:
			property = new PropertyDecimalImpl();
			{
				List<BigDecimal> propertyValues = null;
				if (values != null) {
					propertyValues = new ArrayList<BigDecimal>();
					for (String obj : values) { 
						propertyValues.add(new BigDecimal(obj));
					}
				}
				((PropertyDecimalImpl) property).setValues(propertyValues);
			}
			break;
		case DATETIME:
			property = new PropertyDateTimeImpl();
			{
				List<GregorianCalendar> propertyValues = null;
				if (values != null) {
					propertyValues = new ArrayList<GregorianCalendar>();
					for (String obj : values) {
						GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
						cal.setTime(parseAtomPubDate(obj));
						propertyValues.add(cal);
					}
				}
				((PropertyDateTimeImpl) property).setValues(propertyValues);
			}
			break;
		case HTML:
			property = new PropertyHtmlImpl();
			{
				List<String> propertyValues = null;
				if (values != null) {
					propertyValues = new ArrayList<String>();
					for (String obj : values) {
						propertyValues.add(obj.toString());
					}
				}
				((PropertyHtmlImpl) property).setValues(propertyValues);
			}
			break;
		case URI:
			property = new PropertyUriImpl();
			{
				List<String> propertyValues = null;
				if (values != null) {
					propertyValues = new ArrayList<String>();
					for (Object obj : values) {
						propertyValues.add(obj.toString());
					}
				}
				((PropertyUriImpl) property).setValues(propertyValues);
			}
			break;
		}

		property.setId(id);
		property.setDisplayName(displayName);
		property.setQueryName(queryName);
		property.setLocalName(localName);

		return property;
	}

	public static Properties convertProperties(List<PropertyData<?>> props, List<CmisExtensionElement> extensions) {
		if (props == null) {
			return null;
		}

		PropertiesImpl result = new PropertiesImpl();

		for (PropertyData<?> propertyData : props) {
			result.addProperty(propertyData);
		}

		result.setExtensions(extensions);

		return result;
	}

	/**
	 * Converts properties.
	 * 
	 * public static Properties convertProperties(XmlPullParser parser) { if (parser == null) { return null; }
	 * 
	 * PropertiesImpl result = new PropertiesImpl();
	 * 
	 * try { int eventType = parser.getEventType(); String name = parser.getName(); String nameSpace = parser.getNamespace(); int countParam = 0; String id = null, displayName = null, queryName =
	 * null, localName = null, paramName = null, tag = null; List<String> values = null;
	 * 
	 * while (!(eventType == XmlPullParser.END_TAG && Constants.SELECTOR_PROPERTIES.equals(name))) { switch (eventType) { case XmlPullParser.START_TAG:
	 * 
	 * if (!Constants.NAMESPACE_CMIS.equals(nameSpace)){ skip(parser); eventType = parser.getEventType(); name = parser.getName(); nameSpace = parser.getNamespace(); continue; }
	 * 
	 * AbstractPropertyData<?> property = null;
	 * 
	 * AtomPropertyType propertyType = null; try { propertyType = AtomPropertyType.fromValue(name); tag = name; } catch (Exception e) { throw new CmisRuntimeException("Invalid property: " + id); }
	 * 
	 * countParam = parser.getAttributeCount(); displayName = null; queryName = null; localName = null; paramName = null; for (int i = 0; i < countParam; i++) { paramName = parser.getAttributeName(i);
	 * if (JSON_PROPERTY_DISPLAYNAME.equals(paramName)) { displayName = parser.getAttributeValue(i); } else if (JSON_PROPERTY_QUERYNAME.equals(paramName)) { queryName = parser.getAttributeValue(i); }
	 * else if (JSON_PROPERTY_LOCALNAME.equals(paramName)) { localName = parser.getAttributeValue(i); } else if (PROPERTY_DEFINITION_ID.equals(paramName)) { id = parser.getAttributeValue(i); } }
	 * 
	 * values = new ArrayList<String>(2); while (!(eventType == XmlPullParser.END_TAG && tag.equals(name))) { switch (eventType) { case XmlPullParser.START_TAG: if (JSON_PROPERTY_VALUE.equals(name)) {
	 * parser.next(); values.add(parser.getText()); } break; } eventType = parser.next(); name = parser.getName(); }
	 * 
	 * switch (propertyType) { case STRING: property = new PropertyStringImpl(); { List<String> propertyValues = null; if (values != null) { propertyValues = new ArrayList<String>(); for (String obj :
	 * values) { propertyValues.add(obj); } } ((PropertyStringImpl) property).setValues(propertyValues); } break; case ID: property = new PropertyIdImpl(); { List<String> propertyValues = null; if
	 * (values != null) { propertyValues = new ArrayList<String>(); for (String obj : values) { propertyValues.add(obj); } } ((PropertyIdImpl) property).setValues(propertyValues); } break; case
	 * BOOLEAN: property = new PropertyBooleanImpl(); { List<Boolean> propertyValues = null; if (values != null) { propertyValues = new ArrayList<Boolean>(); for (String obj : values) {
	 * propertyValues.add(Boolean.parseBoolean(obj)); } } ((PropertyBooleanImpl) property).setValues(propertyValues); } break; case INTEGER: property = new PropertyIntegerImpl(); { List<BigInteger>
	 * propertyValues = null; if (values != null) { propertyValues = new ArrayList<BigInteger>(); for (String obj : values) { propertyValues.add(BigInteger.valueOf(Long.parseLong(obj))); } }
	 * ((PropertyIntegerImpl) property).setValues(propertyValues); } break; case DECIMAL: property = new PropertyDecimalImpl(); { List<BigDecimal> propertyValues = null; if (values != null) {
	 * propertyValues = new ArrayList<BigDecimal>(); for (String obj : values) { propertyValues.add(BigDecimal.valueOf(Long.parseLong(obj))); } } ((PropertyDecimalImpl)
	 * property).setValues(propertyValues); } break; case DATETIME: property = new PropertyDateTimeImpl(); { List<GregorianCalendar> propertyValues = null; if (values != null) { propertyValues = new
	 * ArrayList<GregorianCalendar>(); for (String obj : values) { GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC")); cal.setTime(parseAtomPubDate(obj));
	 * propertyValues.add(cal); } } ((PropertyDateTimeImpl) property).setValues(propertyValues); } break; case HTML: property = new PropertyHtmlImpl(); { List<String> propertyValues = null; if (values
	 * != null) { propertyValues = new ArrayList<String>(); for (String obj : values) { propertyValues.add(obj.toString()); } } ((PropertyHtmlImpl) property).setValues(propertyValues); } break; case
	 * URI: property = new PropertyUriImpl(); { List<String> propertyValues = null; if (values != null) { propertyValues = new ArrayList<String>(); for (Object obj : values) {
	 * propertyValues.add(obj.toString()); } } ((PropertyUriImpl) property).setValues(propertyValues); } break; }
	 * 
	 * property.setId(id); property.setDisplayName(displayName); property.setQueryName(queryName); property.setLocalName(localName); result.addProperty(property); //System.out.println("SIZE PROPS" +
	 * result.getPropertyList().size());
	 * 
	 * break;
	 * 
	 * } eventType = parser.next(); name = parser.getName(); nameSpace = parser.getNamespace(); } } catch (Exception e) { e.printStackTrace(); }
	 * 
	 * return result; }
	 */

	/**
	 * Converts allowable actions.
	 */
	public static AllowableActions convertAllowableActions(Map<String, String> rawValues, List<CmisExtensionElement> extensions) {
		if (rawValues == null) {
			return null;
		}

		AllowableActionsImpl result = new AllowableActionsImpl();
		Set<Action> allowableActions = new HashSet<Action>();

		for (Action action : Action.values()) {
			Boolean value = getBoolean(rawValues, action.value());
			if (value != null && value.booleanValue()) {
				allowableActions.add(action);
			}
		}

		result.setAllowableActions(allowableActions);

		result.setExtensions(extensions);

		return result;
	}

	/**
	 * Converts a rendition.
	 */
	public static RenditionData convertRendition(Map<String, String> data, List<CmisExtensionElement> extensions) {
		if (data == null) {
			return null;
		}

		RenditionDataImpl result = new RenditionDataImpl();

		result.setBigHeight(getInteger(data, TAG_RENDITION_HEIGHT));
		result.setKind(getString(data, TAG_RENDITION_KIND));
		result.setBigLength(getInteger(data, TAG_RENDITION_LENGTH));
		result.setMimeType(getString(data, TAG_RENDITION_MIMETYPE));
		result.setRenditionDocumentId(getString(data, TAG_RENDITION_DOCUMENT_ID));
		result.setStreamId(getString(data, TAG_RENDITION_STREAM_ID));
		result.setTitle(getString(data, TAG_RENDITION_TITLE));
		result.setBigWidth(getInteger(data, TAG_RENDITION_WIDTH));

		result.setExtensions(extensions);

		return result;
	}

	public static TypeDefinition convertTypeDefinition(BaseTypeId baseType, Map<String, String> definitionRawValues, List<PropertyDefinition<?>> propertyDefinitionList,
			List<CmisExtensionElement> extensions) {
		if (definitionRawValues == null || propertyDefinitionList == null) {
			return null;
		}

		AbstractTypeDefinition result = null;

		switch (baseType) {
		case CMIS_FOLDER:
			result = new FolderTypeDefinitionImpl();
			break;
		case CMIS_DOCUMENT:
			result = new DocumentTypeDefinitionImpl();
			((DocumentTypeDefinitionImpl) result).setContentStreamAllowed(getEnum(definitionRawValues, TYPE_CONTENTSTREAM_ALLOWED, ContentStreamAllowed.class));
			((DocumentTypeDefinitionImpl) result).setIsVersionable(getBoolean(definitionRawValues, TYPE_VERSIONABLE));
			break;
		case CMIS_RELATIONSHIP:
			result = new RelationshipTypeDefinitionImpl();

			/*
			 * Object allowedSourceTypes = json.get(JSON_TYPE_ALLOWED_SOURCE_TYPES); if (allowedSourceTypes instanceof List) { List<String> types = new ArrayList<String>(); for (Object type :
			 * ((List<Object>) allowedSourceTypes)) { if (type != null) { types.add(type.toString()); } }
			 * 
			 * ((RelationshipTypeDefinitionImpl) result).setAllowedSourceTypes(types); }
			 * 
			 * Object allowedTargetTypes = json.get(JSON_TYPE_ALLOWED_TARGET_TYPES); if (allowedTargetTypes instanceof List) { List<String> types = new ArrayList<String>(); for (Object type :
			 * ((List<Object>) allowedTargetTypes)) { if (type != null) { types.add(type.toString()); } }
			 * 
			 * ((RelationshipTypeDefinitionImpl) result).setAllowedTargetTypes(types); }
			 */

			break;
		case CMIS_POLICY:
			result = new PolicyTypeDefinitionImpl();
			break;
		default:
			throw new CmisRuntimeException("Type '" + baseType + "' does not match a base type!");
		}

		result.setId(getString(definitionRawValues, TYPE_ID));
		result.setLocalName(getString(definitionRawValues, TYPE_LOCALNAME));
		result.setLocalNamespace(getString(definitionRawValues, TYPE_LOCALNAMESPACE));
		result.setDisplayName(getString(definitionRawValues, TYPE_DISPLAYNAME));
		result.setQueryName(getString(definitionRawValues, TYPE_QUERYNAME));
		result.setDescription(getString(definitionRawValues, TYPE_DESCRIPTION));
		result.setBaseTypeId(getEnum(definitionRawValues, TYPE_BASE_ID, BaseTypeId.class));
		result.setIsCreatable(getBoolean(definitionRawValues, TYPE_CREATABLE));
		result.setIsFileable(getBoolean(definitionRawValues, TYPE_FILEABLE));
		result.setIsQueryable(getBoolean(definitionRawValues, TYPE_QUERYABLE));
		result.setIsFulltextIndexed(getBoolean(definitionRawValues, TYPE_FULLTEXT_INDEXED));
		result.setIsIncludedInSupertypeQuery(getBoolean(definitionRawValues, TYPE_INCLUDE_IN_SUPERTYPE_QUERY));
		result.setIsControllablePolicy(getBoolean(definitionRawValues, TYPE_CONTROLABLE_POLICY));
		result.setIsControllableAcl(getBoolean(definitionRawValues, TYPE_CONTROLABLE_ACL));
		result.setParentTypeId(getString(definitionRawValues, TYPE_PARENT_ID));

		if (propertyDefinitionList != null) {
			for (PropertyDefinition<?> propertyDefinition : propertyDefinitionList) {
				result.addPropertyDefinition(propertyDefinition);
			}
		}

		result.setExtensions(extensions);

		return result;
	}

	public static PropertyDefinition<?> convertPropertyDefinition(String id, Map<String, String> propertyTypeRawValues, List<CmisExtensionElement> extensions) {
		if (propertyTypeRawValues == null) {
			return null;
		}

		AbstractPropertyDefinition<?> result = null;

		// find property type
		//Locale.ENGLISH is used because in turkish the "I".toLowerCase is not equal to i.
		PropertyType propertyType = PropertyType.fromValue(id.replace(PROPERTY, "").replace("Definition", "").toLowerCase(Locale.ENGLISH));
		if (propertyType == null) {
			throw new CmisRuntimeException("Invalid property type '" + id + "'! Data type not set!");
		}

		// find
		Cardinality cardinality = getEnum(propertyTypeRawValues, ATTR_PROPERTY_TYPE_CARDINALITY, Cardinality.class);
		if (cardinality == null) {
			throw new CmisRuntimeException("Invalid property type '" + id + "'! Cardinality not set!");
		}

		switch (propertyType) {
		case STRING:
			result = new PropertyStringDefinitionImpl();
			((PropertyStringDefinitionImpl) result).setMaxLength(getInteger(propertyTypeRawValues, ATTR_PROPERTY_TYPE_MAX_LENGTH));
			// ((PropertyStringDefinitionImpl)
			// result).setChoices(convertChoicesString(json.get(JSON_PROPERTY_TYPE_CHOICE)));
			break;
		case ID:
			result = new PropertyIdDefinitionImpl();
			// ((PropertyIdDefinitionImpl)
			// result).setChoices(convertChoicesString(json.get(JSON_PROPERTY_TYPE_CHOICE)));
			break;
		case BOOLEAN:
			result = new PropertyBooleanDefinitionImpl();
			// ((PropertyBooleanDefinitionImpl)
			// result).setChoices(convertChoicesBoolean(json.get(JSON_PROPERTY_TYPE_CHOICE)));
			break;
		case INTEGER:
			result = new PropertyIntegerDefinitionImpl();
			((PropertyIntegerDefinitionImpl) result).setMinValue(getInteger(propertyTypeRawValues, ATTR_PROPERTY_TYPE_MIN_VALUE));
			((PropertyIntegerDefinitionImpl) result).setMaxValue(getInteger(propertyTypeRawValues, ATTR_PROPERTY_TYPE_MAX_VALUE));
			// ((PropertyIntegerDefinitionImpl)
			// result).setChoices(convertChoicesInteger(json.get(JSON_PROPERTY_TYPE_CHOICE)));
			break;
		case DATETIME:
			result = new PropertyDateTimeDefinitionImpl();
			((PropertyDateTimeDefinitionImpl) result).setDateTimeResolution(getEnum(propertyTypeRawValues, ATTR_PROPERTY_TYPE_RESOLUTION, DateTimeResolution.class));
			// ((PropertyDateTimeDefinitionImpl)
			// result).setChoices(convertChoicesDateTime(json.get(JSON_PROPERTY_TYPE_CHOICE)));
			break;
		case DECIMAL:
			result = new PropertyDecimalDefinitionImpl();
			((PropertyDecimalDefinitionImpl) result).setMinValue(getDecimal(propertyTypeRawValues, ATTR_PROPERTY_TYPE_MIN_VALUE));
			((PropertyDecimalDefinitionImpl) result).setMaxValue(getDecimal(propertyTypeRawValues, ATTR_PROPERTY_TYPE_MAX_VALUE));
			((PropertyDecimalDefinitionImpl) result).setPrecision(getEnum(propertyTypeRawValues, ATTR_PROPERTY_TYPE_PRECISION, DecimalPrecision.class));
			// ((PropertyDecimalDefinitionImpl)
			// result).setChoices(convertChoicesDecimal(json.get(JSON_PROPERTY_TYPE_CHOICE)));
			break;
		case HTML:
			result = new PropertyHtmlDefinitionImpl();
			// ((PropertyHtmlDefinitionImpl)
			// result).setChoices(convertChoicesString(json.get(JSON_PROPERTY_TYPE_CHOICE)));
			break;
		case URI:
			result = new PropertyUriDefinitionImpl();
			// ((PropertyUriDefinitionImpl)
			// result).setChoices(convertChoicesString(json.get(JSON_PROPERTY_TYPE_CHOICE)));
			break;
		default:
			throw new CmisRuntimeException("Property type '" + id + "' does not match a data type!");
		}

		/*
		 * default value Object defaultValue = json.get(JSON_PROPERTY_TYPE_DEAULT_VALUE); if (defaultValue != null) { if (defaultValue instanceof List) { List values = new ArrayList(); for (Object
		 * value : (List) defaultValue) { values.add(getCMISValue(value, propertyType)); } result.setDefaultValue(values); } else { result.setDefaultValue((List)
		 * Collections.singletonList(getCMISValue(defaultValue, propertyType))); } }
		 */

		// generic
		result.setId(getString(propertyTypeRawValues, ATTR_PROPERTY_TYPE_ID));
		result.setPropertyType(propertyType);
		result.setCardinality(cardinality);
		result.setLocalName(getString(propertyTypeRawValues, ATTR_PROPERTY_TYPE_LOCALNAME));
		result.setLocalNamespace(getString(propertyTypeRawValues, ATTR_PROPERTY_TYPE_LOCALNAMESPACE));
		result.setQueryName(getString(propertyTypeRawValues, ATTR_PROPERTY_TYPE_QUERYNAME));
		result.setDescription(getString(propertyTypeRawValues, ATTR_PROPERTY_TYPE_DESCRIPTION));
		result.setDisplayName(getString(propertyTypeRawValues, ATTR_PROPERTY_TYPE_DISPLAYNAME));
		result.setIsInherited(getBoolean(propertyTypeRawValues, ATTR_PROPERTY_TYPE_INHERITED));
		result.setIsOpenChoice(getBoolean(propertyTypeRawValues, ATTR_PROPERTY_TYPE_OPENCHOICE));
		result.setIsOrderable(getBoolean(propertyTypeRawValues, ATTR_PROPERTY_TYPE_ORDERABLE));
		result.setIsQueryable(getBoolean(propertyTypeRawValues, ATTR_PROPERTY_TYPE_QUERYABLE));
		result.setIsRequired(getBoolean(propertyTypeRawValues, ATTR_PROPERTY_TYPE_REQUIRED));
		result.setUpdatability(getEnum(propertyTypeRawValues, ATTR_PROPERTY_TYPE_UPDATABILITY, Updatability.class));

		result.setExtensions(extensions);

		return result;
	}

	// -----------------------------------------------------------------

	private static BigDecimal getDecimal(Map<String, String> values, String key) {
		return (values.get(key) == null) ? null : BigDecimal.valueOf(Long.parseLong(values.get(key)));
	}

	private static Boolean getBoolean(Map<String, String> values, String key) {
		return (values.get(key) == null) ? null : Boolean.parseBoolean(values.get(key));
	}

	private static String getString(Map<String, String> values, String key) {
		return (values.get(key) == null) ? null : values.get(key);
	}

	private static BigInteger getInteger(Map<String, String> values, String key) {
		return (values.get(key) == null) ? null : BigInteger.valueOf(Long.parseLong(values.get(key)));
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T getEnum(Map<String, String> repositoryCapabilitiesRawValues, String key, Class<T> clazz) {
		String value = getString(repositoryCapabilitiesRawValues, key);
		if (value == null) {
			return null;
		}

		try {
			Method m = clazz.getMethod("fromValue", String.class);
			return (T) m.invoke(null, value);
		} catch (Exception e) {
			if (e instanceof IllegalArgumentException) {
				return null;
			} else {
				throw new CmisRuntimeException("Could not parse enum value!", e);
			}
		}
	}

	// -----------------------------------------------------------------

	/*
	 * @SuppressWarnings("unchecked") public static void convertExtension(Map<String, String> source, ExtensionsData target, Set<String> cmisKeys) { if (source == null) { return; }
	 * 
	 * List<CmisExtensionElement> extensions = null;
	 * 
	 * for (Map.Entry<String, String> element : source.entrySet()) { if (cmisKeys.contains(element.getKey())) { continue; }
	 * 
	 * if (extensions == null) { extensions = new ArrayList<CmisExtensionElement>(); }
	 * 
	 * if (element.getValue() instanceof Map) { extensions.add(new CmisExtensionElementImpl(null, element.getKey(), null, convertExtension((Map<String, Object>) element.getValue()))); } else if
	 * (element.getValue() instanceof List) { extensions.add(new CmisExtensionElementImpl(null, element.getKey(), null, convertExtension((List<Object>) element.getValue()))); } else { String value =
	 * (element.getValue() == null ? null : element.getValue().toString()); extensions.add(new CmisExtensionElementImpl(null, element.getKey(), null, value)); } }
	 * 
	 * target.setExtensions(extensions); }
	 */

	// -----------------------------------------------------------------

	 private static final String FORMAT_1 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	 private static final String FORMAT_2 = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	 private static final String FORMAT_3 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	 private static final String FORMAT_4 = "MMM dd yyyy HH:mm:ss zzzz";

	 private static final String FORMAT_5 = "dd MMM yyyy HH:mm:ss zzzz";

	 private static final String[] DATE_FORMATS = { FORMAT_1, FORMAT_2, FORMAT_3, FORMAT_4, FORMAT_5 };

	 private static Date parseAtomPubDate(String atomPubDate)
	    {
	        Date d = null;
	        SimpleDateFormat sdf;
	        for (int i = 0; i < DATE_FORMATS.length; i++)
	        {
	            sdf = new SimpleDateFormat(DATE_FORMATS[i], Locale.UK);
	            sdf.setLenient(true);
	            try
	            {
	                d = sdf.parse(atomPubDate);
	                break;
	            }
	            catch (ParseException e)
	            {
	                continue;
	            }
	        }

	        return d;
	    }
}
