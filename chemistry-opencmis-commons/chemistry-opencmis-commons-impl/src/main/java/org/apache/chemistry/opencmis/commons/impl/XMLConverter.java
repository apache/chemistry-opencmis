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

import static org.apache.chemistry.opencmis.commons.impl.XMLConstants.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.PropertyBoolean;
import org.apache.chemistry.opencmis.commons.data.PropertyDateTime;
import org.apache.chemistry.opencmis.commons.data.PropertyDecimal;
import org.apache.chemistry.opencmis.commons.data.PropertyHtml;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyInteger;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.data.PropertyUri;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityOrderBy;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.DateTimeResolution;
import org.apache.chemistry.opencmis.commons.enums.DecimalPrecision;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.SupportedPermissions;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AclCapabilitiesDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChoiceImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CreatablePropertyTypesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ExtensionFeatureImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ItemTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.NewTypeSettableAttributesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PolicyTypeDefinitionImpl;
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
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.SecondaryTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeMutabilityImpl;

public class XMLConverter {

    private XMLConverter() {
    }

    public static RepositoryInfo convertRepositoryInfo(XMLStreamReader parser) throws XMLStreamException {
        return REPOSITORY_INFO_PARSER.walk(parser);
    }

    public static TypeDefinition convertTypeDefinition(XMLStreamReader parser) throws XMLStreamException {
        return TYPE_DEF_PARSER.walk(parser);
    }

    // ------------------------------
    // --- repository info parser ---
    // ------------------------------

    private static final XMLWalker<RepositoryInfoImpl> REPOSITORY_INFO_PARSER = new XMLWalker<RepositoryInfoImpl>() {
        @Override
        protected RepositoryInfoImpl prepareTarget(XMLStreamReader parser, QName name) throws XMLStreamException {
            return new RepositoryInfoImpl();
        }

        @Override
        protected boolean read(XMLStreamReader parser, QName name, RepositoryInfoImpl target) throws XMLStreamException {

            if (isCmisNamespace(name)) {
                if (isTag(name, TAG_REPINFO_ID)) {
                    target.setId(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_NAME)) {
                    target.setName(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_DESCRIPTION)) {
                    target.setDescription(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_VENDOR)) {
                    target.setVendorName(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_PRODUCT)) {
                    target.setProductName(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_PRODUCT_VERSION)) {
                    target.setProductVersion(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_ROOT_FOLDER_ID)) {
                    target.setRootFolder(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_CHANGE_LOG_TOKEN)) {
                    target.setRootFolder(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_CAPABILITIES)) {
                    target.setCapabilities(CAPABILITIES_PARSER.walk(parser));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_ACL_CAPABILITIES)) {
                    target.setAclCapabilities(ACL_CAPABILITIES_PARSER.walk(parser));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_CMIS_VERSION_SUPPORTED)) {
                    target.setCmisVersionSupported(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_THIN_CLIENT_URI)) {
                    target.setThinClientUri(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_CHANGES_INCOMPLETE)) {
                    target.setChangesIncomplete(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_CHANGES_ON_TYPE)) {
                    target.setChangesOnType(addToList(target.getChangesOnType(), readEnum(parser, BaseTypeId.class)));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_PRINCIPAL_ID_ANONYMOUS)) {
                    target.setPrincipalAnonymous(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_PRINCIPAL_ID_ANYONE)) {
                    target.setPrincipalAnyone(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_REPINFO_EXTENDED_FEATURES)) {
                    target.setExtensionFeature(addToList(target.getExtensionFeatures(),
                            EXTENDED_FEATURES_PARSER.walk(parser)));
                    return true;
                }
            }

            return false;
        }
    };

    private static final XMLWalker<RepositoryCapabilitiesImpl> CAPABILITIES_PARSER = new XMLWalker<RepositoryCapabilitiesImpl>() {
        @Override
        protected RepositoryCapabilitiesImpl prepareTarget(XMLStreamReader parser, QName name)
                throws XMLStreamException {
            return new RepositoryCapabilitiesImpl();
        }

        @Override
        protected boolean read(XMLStreamReader parser, QName name, RepositoryCapabilitiesImpl target)
                throws XMLStreamException {
            if (isCmisNamespace(name)) {
                if (isTag(name, TAG_CAP_ACL)) {
                    target.setCapabilityAcl(readEnum(parser, CapabilityAcl.class));
                    return true;
                }

                if (isTag(name, TAG_CAP_ALL_VERSIONS_SEARCHABLE)) {
                    target.setAllVersionsSearchable(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_CHANGES)) {
                    target.setCapabilityChanges(readEnum(parser, CapabilityChanges.class));
                    return true;
                }

                if (isTag(name, TAG_CAP_CONTENT_STREAM_UPDATABILITY)) {
                    target.setCapabilityContentStreamUpdates(readEnum(parser, CapabilityContentStreamUpdates.class));
                    return true;
                }

                if (isTag(name, TAG_CAP_GET_DESCENDANTS)) {
                    target.setSupportsGetDescendants(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_GET_FOLDER_TREE)) {
                    target.setSupportsGetFolderTree(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_ORDER_BY)) {
                    target.setOrderByCapability(readEnum(parser, CapabilityOrderBy.class));
                    return true;
                }

                if (isTag(name, TAG_CAP_MULTIFILING)) {
                    target.setSupportsMultifiling(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_PWC_SEARCHABLE)) {
                    target.setIsPwcSearchable(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_PWC_UPDATABLE)) {
                    target.setIsPwcUpdatable(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_QUERY)) {
                    target.setCapabilityQuery(readEnum(parser, CapabilityQuery.class));
                    return true;
                }

                if (isTag(name, TAG_CAP_RENDITIONS)) {
                    target.setCapabilityRendition(readEnum(parser, CapabilityRenditions.class));
                    return true;
                }

                if (isTag(name, TAG_CAP_UNFILING)) {
                    target.setSupportsUnfiling(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_VERSION_SPECIFIC_FILING)) {
                    target.setSupportsVersionSpecificFiling(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_JOIN)) {
                    target.setCapabilityJoin(readEnum(parser, CapabilityJoin.class));
                    return true;
                }

                if (isTag(name, TAG_CAP_CREATABLE_PROPERTY_TYPES)) {
                    target.setCreatablePropertyTypes(CREATABLE_PROPERTY_TYPES_PARSER.walk(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_NEW_TYPE_SETTABLE_ATTRIBUTES)) {
                    target.setNewTypeSettableAttributes(NEW_TYPES_SETTABLE_ATTRIBUTES_PARSER.walk(parser));
                    return true;
                }
            }

            return false;
        }
    };

    private static final XMLWalker<CreatablePropertyTypesImpl> CREATABLE_PROPERTY_TYPES_PARSER = new XMLWalker<CreatablePropertyTypesImpl>() {
        @Override
        protected CreatablePropertyTypesImpl prepareTarget(XMLStreamReader parser, QName name)
                throws XMLStreamException {
            return new CreatablePropertyTypesImpl();
        }

        @Override
        protected boolean read(XMLStreamReader parser, QName name, CreatablePropertyTypesImpl target)
                throws XMLStreamException {
            if (isCmisNamespace(name)) {
                if (isTag(name, TAG_CAP_CREATABLE_PROPERTY_TYPES_CANCREATE)) {
                    Set<PropertyType> ptSet = target.canCreate();
                    if (ptSet == null) {
                        ptSet = new HashSet<PropertyType>();
                        target.setCanCreate(ptSet);
                    }

                    ptSet.add(readEnum(parser, PropertyType.class));
                    return true;
                }
            }
            return false;
        }
    };

    private static final XMLWalker<NewTypeSettableAttributesImpl> NEW_TYPES_SETTABLE_ATTRIBUTES_PARSER = new XMLWalker<NewTypeSettableAttributesImpl>() {
        @Override
        protected NewTypeSettableAttributesImpl prepareTarget(XMLStreamReader parser, QName name)
                throws XMLStreamException {
            return new NewTypeSettableAttributesImpl();
        }

        @Override
        protected boolean read(XMLStreamReader parser, QName name, NewTypeSettableAttributesImpl target)
                throws XMLStreamException {
            if (isCmisNamespace(name)) {
                if (isTag(name, TAG_CAP_NEW_TYPE_SETTABLE_ATTRIBUTES_ID)) {
                    target.setCanSetId(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_NEW_TYPE_SETTABLE_ATTRIBUTES_LOCALNAME)) {
                    target.setCanSetLocalName(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_NEW_TYPE_SETTABLE_ATTRIBUTES_LOCALNAMESPACE)) {
                    target.setCanSetLocalNamespace(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_NEW_TYPE_SETTABLE_ATTRIBUTES_DISPLAYNAME)) {
                    target.setCanSetDisplayName(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_NEW_TYPE_SETTABLE_ATTRIBUTES_QUERYNAME)) {
                    target.setCanSetQueryName(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_NEW_TYPE_SETTABLE_ATTRIBUTES_DESCRIPTION)) {
                    target.setCanSetDescription(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_NEW_TYPE_SETTABLE_ATTRIBUTES_CREATEABLE)) {
                    target.setCanSetCreatable(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_NEW_TYPE_SETTABLE_ATTRIBUTES_FILEABLE)) {
                    target.setCanSetFileable(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_NEW_TYPE_SETTABLE_ATTRIBUTES_QUERYABLE)) {
                    target.setCanSetQueryable(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_NEW_TYPE_SETTABLE_ATTRIBUTES_FULLTEXTINDEXED)) {
                    target.setCanSetFulltextIndexed(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_NEW_TYPE_SETTABLE_ATTRIBUTES_INCLUDEDINSUPERTYTPEQUERY)) {
                    target.setCanSetIncludedInSupertypeQuery(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_NEW_TYPE_SETTABLE_ATTRIBUTES_CONTROLABLEPOLICY)) {
                    target.setCanSetControllablePolicy(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_CAP_NEW_TYPE_SETTABLE_ATTRIBUTES_CONTROLABLEACL)) {
                    target.setCanSetControllableAcl(readBoolean(parser));
                    return true;
                }
            }

            return false;
        }
    };

    private static final XMLWalker<AclCapabilitiesDataImpl> ACL_CAPABILITIES_PARSER = new XMLWalker<AclCapabilitiesDataImpl>() {
        @Override
        protected AclCapabilitiesDataImpl prepareTarget(XMLStreamReader parser, QName name) throws XMLStreamException {

            return new AclCapabilitiesDataImpl();
        }

        @Override
        protected boolean read(XMLStreamReader parser, QName name, AclCapabilitiesDataImpl target)
                throws XMLStreamException {
            if (isCmisNamespace(name)) {
                if (isTag(name, TAG_ACLCAP_SUPPORTED_PERMISSIONS)) {
                    target.setSupportedPermissions(readEnum(parser, SupportedPermissions.class));
                    return true;
                }

                if (isTag(name, TAGACLCAP_ACL_PROPAGATION)) {
                    target.setAclPropagation(readEnum(parser, AclPropagation.class));
                    return true;
                }

                if (isTag(name, TAG_ACLCAP_PERMISSIONS)) {
                    target.setPermissionDefinitionData(addToList(target.getPermissions(),
                            PERMISSION_DEFINITION_PARSER.walk(parser)));
                    return true;
                }

                if (isTag(name, TAG_ACLCAP_PERMISSION_MAPPING)) {
                    Map<String, PermissionMapping> mapping = target.getPermissionMapping();
                    if (mapping == null) {
                        mapping = new HashMap<String, PermissionMapping>();
                        target.setPermissionMappingData(mapping);
                    }

                    PermissionMapping pm = PERMISSION_MAPPING_PARSER.walk(parser);
                    mapping.put(pm.getKey(), pm);

                    return true;
                }
            }

            return false;
        }
    };

    private static final XMLWalker<PermissionDefinitionDataImpl> PERMISSION_DEFINITION_PARSER = new XMLWalker<PermissionDefinitionDataImpl>() {
        @Override
        protected PermissionDefinitionDataImpl prepareTarget(XMLStreamReader parser, QName name)
                throws XMLStreamException {
            return new PermissionDefinitionDataImpl();
        }

        @Override
        protected boolean read(XMLStreamReader parser, QName name, PermissionDefinitionDataImpl target)
                throws XMLStreamException {
            if (isCmisNamespace(name)) {
                if (isTag(name, TAG_ACLCAP_PERMISSION_PERMISSION)) {
                    target.setPermission(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_ACLCAP_PERMISSION_DESCRIPTION)) {
                    target.setDescription(readText(parser));
                    return true;
                }
            }

            return false;
        }
    };

    private static final XMLWalker<PermissionMappingDataImpl> PERMISSION_MAPPING_PARSER = new XMLWalker<PermissionMappingDataImpl>() {
        @Override
        protected PermissionMappingDataImpl prepareTarget(XMLStreamReader parser, QName name) throws XMLStreamException {
            return new PermissionMappingDataImpl();
        }

        @Override
        protected boolean read(XMLStreamReader parser, QName name, PermissionMappingDataImpl target)
                throws XMLStreamException {
            if (isCmisNamespace(name)) {
                if (isTag(name, TAG_ACLCAP_MAPPING_KEY)) {
                    target.setKey(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_ACLCAP_MAPPING_PERMISSION)) {
                    target.setPermissions(addToList(target.getPermissions(), readText(parser)));
                    return true;
                }
            }

            return false;
        }
    };

    private static final XMLWalker<ExtensionFeatureImpl> EXTENDED_FEATURES_PARSER = new XMLWalker<ExtensionFeatureImpl>() {
        @Override
        protected ExtensionFeatureImpl prepareTarget(XMLStreamReader parser, QName name) throws XMLStreamException {
            return new ExtensionFeatureImpl();
        }

        @Override
        protected boolean read(XMLStreamReader parser, QName name, ExtensionFeatureImpl target)
                throws XMLStreamException {
            if (isCmisNamespace(name)) {
                if (isTag(name, TAG_FEATURE_ID)) {
                    target.setId(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_FEATURE_URL)) {
                    target.setUrl(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_FEATURE_COMMON_NAME)) {
                    target.setCommonName(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_FEATURE_VERSION_LABEL)) {
                    target.setVersionLabel(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_FEATURE_DESCRIPTION)) {
                    target.setDescription(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_FEATURE_DATA)) {
                    Map<String, String> featureData = target.getFeatureData();
                    if (featureData == null) {
                        featureData = new HashMap<String, String>();
                        target.setFeatureData(featureData);
                    }

                    String[] data = FEATURE_DATA_PARSER.walk(parser);
                    featureData.put(data[0], data[1]);

                    return true;
                }
            }

            return false;
        }
    };

    private static final XMLWalker<String[]> FEATURE_DATA_PARSER = new XMLWalker<String[]>() {
        @Override
        protected String[] prepareTarget(XMLStreamReader parser, QName name) throws XMLStreamException {
            return new String[2];
        }

        @Override
        protected boolean read(XMLStreamReader parser, QName name, String[] target) throws XMLStreamException {
            if (isCmisNamespace(name)) {
                if (isTag(name, TAG_FEATURE_DATA_KEY)) {
                    target[0] = readText(parser);
                    return true;
                }

                if (isTag(name, TAG_FEATURE_DATA_VALUE)) {
                    target[1] = readText(parser);
                    return true;
                }
            }

            return false;
        }
    };

    // --------------------------
    // --- definition parsers ---
    // --------------------------

    private static final XMLWalker<AbstractTypeDefinition> TYPE_DEF_PARSER = new XMLWalker<AbstractTypeDefinition>() {
        @Override
        protected AbstractTypeDefinition prepareTarget(XMLStreamReader parser, QName name) throws XMLStreamException {

            AbstractTypeDefinition result = null;

            String typeAttr = parser.getAttributeValue(Constants.NAMESPACE_XSI, "type");
            if (typeAttr != null) {
                if (typeAttr.endsWith(XMLConstants.ATTR_DOCUMENT_TYPE)) {
                    result = new DocumentTypeDefinitionImpl();
                } else if (typeAttr.endsWith(XMLConstants.ATTR_FOLDER_TYPE)) {
                    result = new FolderTypeDefinitionImpl();
                } else if (typeAttr.endsWith(XMLConstants.ATTR_RELATIONSHIP_TYPE)) {
                    result = new RelationshipTypeDefinitionImpl();
                } else if (typeAttr.endsWith(XMLConstants.ATTR_POLICY_TYPE)) {
                    result = new PolicyTypeDefinitionImpl();
                } else if (typeAttr.endsWith(XMLConstants.ATTR_ITEM_TYPE)) {
                    result = new ItemTypeDefinitionImpl();
                } else if (typeAttr.endsWith(XMLConstants.ATTR_SECONDARY_TYPE)) {
                    result = new SecondaryTypeDefinitionImpl();
                }
            }

            if (result == null) {
                throw new CmisInvalidArgumentException("Cannot read type definition!");
            }

            return result;
        }

        @Override
        protected boolean read(XMLStreamReader parser, QName name, AbstractTypeDefinition target)
                throws XMLStreamException {
            if (isCmisNamespace(name)) {
                if (isTag(name, TAG_TYPE_ID)) {
                    target.setId(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_LOCALNAME)) {
                    target.setLocalName(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_LOCALNAMESPACE)) {
                    target.setLocalNamespace(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_DISPLAYNAME)) {
                    target.setDisplayName(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_QUERYNAME)) {
                    target.setQueryName(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_DESCRIPTION)) {
                    target.setDescription(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_BASE_ID)) {
                    BaseTypeId baseType = readEnum(parser, BaseTypeId.class);
                    if (baseType == null) {
                        throw new CmisInvalidArgumentException("Invalid base type!");
                    }

                    target.setBaseTypeId(baseType);
                    return true;
                }

                if (isTag(name, TAG_TYPE_PARENT_ID)) {
                    target.setParentTypeId(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_CREATABLE)) {
                    target.setIsCreatable(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_FILEABLE)) {
                    target.setIsFileable(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_QUERYABLE)) {
                    target.setIsQueryable(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_FULLTEXT_INDEXED)) {
                    target.setIsFulltextIndexed(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_INCLUDE_IN_SUPERTYPE_QUERY)) {
                    target.setIsIncludedInSupertypeQuery(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_CONTROLABLE_POLICY)) {
                    target.setIsControllablePolicy(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_CONTROLABLE_ACL)) {
                    target.setIsControllableAcl(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_TYPE_MUTABILITY)) {
                    target.setTypeMutability(TYPE_MUTABILITY_PARSER.walk(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_PROP_DEF_STRING) || isTag(name, TAG_TYPE_PROP_DEF_ID)
                        || isTag(name, TAG_TYPE_PROP_DEF_BOOLEAN) || isTag(name, TAG_TYPE_PROP_DEF_INTEGER)
                        || isTag(name, TAG_TYPE_PROP_DEF_DATETIME) || isTag(name, TAG_TYPE_PROP_DEF_DECIMAL)
                        || isTag(name, TAG_TYPE_PROP_DEF_HTML) || isTag(name, TAG_TYPE_PROP_DEF_URI)) {
                    target.addPropertyDefinition(PROPERTY_TYPE_PARSER.walk(parser));
                    return true;
                }

                if (target instanceof DocumentTypeDefinitionImpl) {
                    if (isTag(name, TAG_TYPE_VERSIONABLE)) {
                        ((DocumentTypeDefinitionImpl) target).setIsVersionable(readBoolean(parser));
                        return true;
                    }

                    if (isTag(name, TAG_TYPE_CONTENTSTREAM_ALLOWED)) {
                        ((DocumentTypeDefinitionImpl) target).setContentStreamAllowed(readEnum(parser,
                                ContentStreamAllowed.class));
                        return true;
                    }
                }

                if (target instanceof RelationshipTypeDefinitionImpl) {
                    if (isTag(name, TAG_TYPE_ALLOWED_SOURCE_TYPES)) {
                        RelationshipTypeDefinitionImpl relTarget = (RelationshipTypeDefinitionImpl) target;
                        relTarget
                                .setAllowedSourceTypes(addToList(relTarget.getAllowedSourceTypeIds(), readText(parser)));
                        return true;
                    }

                    if (isTag(name, TAG_TYPE_ALLOWED_TARGET_TYPES)) {
                        RelationshipTypeDefinitionImpl relTarget = (RelationshipTypeDefinitionImpl) target;
                        relTarget
                                .setAllowedTargetTypes(addToList(relTarget.getAllowedTargetTypeIds(), readText(parser)));
                        return true;
                    }
                }
            }

            return false;
        }
    };

    private static final XMLWalker<TypeMutabilityImpl> TYPE_MUTABILITY_PARSER = new XMLWalker<TypeMutabilityImpl>() {
        @Override
        protected TypeMutabilityImpl prepareTarget(XMLStreamReader parser, QName name) throws XMLStreamException {
            return new TypeMutabilityImpl();
        }

        @Override
        protected boolean read(XMLStreamReader parser, QName name, TypeMutabilityImpl target) throws XMLStreamException {
            if (isCmisNamespace(name)) {
                if (isTag(name, TAG_TYPE_TYPE_MUTABILITY_CREATE)) {
                    target.setCanCreate(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_TYPE_MUTABILITY_UPDATE)) {
                    target.setCanUpdate(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_TYPE_TYPE_MUTABILITY_DELETE)) {
                    target.setCanDelete(readBoolean(parser));
                    return true;
                }
            }

            return false;
        }
    };

    private static final XMLWalker<AbstractPropertyDefinition<?>> PROPERTY_TYPE_PARSER = new XMLWalker<AbstractPropertyDefinition<?>>() {
        @Override
        protected AbstractPropertyDefinition<?> prepareTarget(XMLStreamReader parser, QName name)
                throws XMLStreamException {
            AbstractPropertyDefinition<?> result = null;

            if (isTag(name, TAG_TYPE_PROP_DEF_STRING)) {
                result = new PropertyStringDefinitionImpl();
            } else if (isTag(name, TAG_TYPE_PROP_DEF_ID)) {
                result = new PropertyIdDefinitionImpl();
            } else if (isTag(name, TAG_TYPE_PROP_DEF_BOOLEAN)) {
                result = new PropertyBooleanDefinitionImpl();
            } else if (isTag(name, TAG_TYPE_PROP_DEF_INTEGER)) {
                result = new PropertyIntegerDefinitionImpl();
            } else if (isTag(name, TAG_TYPE_PROP_DEF_DATETIME)) {
                result = new PropertyDateTimeDefinitionImpl();
            } else if (isTag(name, TAG_TYPE_PROP_DEF_DECIMAL)) {
                result = new PropertyDecimalDefinitionImpl();
            } else if (isTag(name, TAG_TYPE_PROP_DEF_HTML)) {
                result = new PropertyHtmlDefinitionImpl();
            } else if (isTag(name, TAG_TYPE_PROP_DEF_URI)) {
                result = new PropertyUriDefinitionImpl();
            }

            if (result == null) {
                throw new CmisInvalidArgumentException("Cannot read property type definition!");
            }

            return result;
        }

        @Override
        protected boolean read(XMLStreamReader parser, QName name, AbstractPropertyDefinition<?> target)
                throws XMLStreamException {
            if (isCmisNamespace(name)) {

                if (isTag(name, TAG_PROPERTY_TYPE_ID)) {
                    target.setId(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_PROPERTY_TYPE_LOCALNAME)) {
                    target.setLocalName(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_PROPERTY_TYPE_LOCALNAMESPACE)) {
                    target.setLocalNamespace(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_PROPERTY_TYPE_DISPLAYNAME)) {
                    target.setDisplayName(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_PROPERTY_TYPE_QUERYNAME)) {
                    target.setQueryName(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_PROPERTY_TYPE_DESCRIPTION)) {
                    target.setDescription(readText(parser));
                    return true;
                }

                if (isTag(name, TAG_PROPERTY_TYPE_PROPERTY_TYPE)) {
                    PropertyType propType = readEnum(parser, PropertyType.class);
                    if (propType == null) {
                        throw new CmisInvalidArgumentException("Invalid property type!");
                    }

                    target.setPropertyType(propType);
                    return true;
                }

                if (isTag(name, TAG_PROPERTY_TYPE_CARDINALITY)) {
                    Cardinality cardinality = readEnum(parser, Cardinality.class);
                    if (cardinality == null) {
                        throw new CmisInvalidArgumentException("Invalid cardinality!");
                    }

                    target.setCardinality(cardinality);
                    return true;
                }

                if (isTag(name, TAG_PROPERTY_TYPE_UPDATABILITY)) {
                    Updatability updatability = readEnum(parser, Updatability.class);
                    if (updatability == null) {
                        throw new CmisInvalidArgumentException("Invalid updatability!");
                    }

                    target.setUpdatability(updatability);
                    return true;
                }

                if (isTag(name, TAG_PROPERTY_TYPE_INHERITED)) {
                    target.setIsInherited(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_PROPERTY_TYPE_REQUIRED)) {
                    target.setIsRequired(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_PROPERTY_TYPE_QUERYABLE)) {
                    target.setIsQueryable(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_PROPERTY_TYPE_ORDERABLE)) {
                    target.setIsOrderable(readBoolean(parser));
                    return true;
                }

                if (isTag(name, TAG_PROPERTY_TYPE_OPENCHOICE)) {
                    target.setIsOpenChoice(readBoolean(parser));
                    return true;
                }

                if (target instanceof PropertyStringDefinitionImpl) {
                    if (isTag(name, TAG_PROPERTY_TYPE_DEAULT_VALUE)) {
                        PropertyString prop = PROPERTY_STRING_PARSER.walk(parser);
                        ((PropertyStringDefinitionImpl) target).setDefaultValue(prop.getValues());
                        return true;
                    }

                    if (isTag(name, TAG_PROPERTY_TYPE_CHOICE)) {
                        CHOICE_STRING_PARSER.addToChoiceList(parser, (PropertyStringDefinitionImpl) target);
                        return true;
                    }

                    if (isTag(name, TAG_PROPERTY_TYPE_MAX_LENGTH)) {
                        ((PropertyStringDefinitionImpl) target).setMaxLength(readInteger(parser));
                        return true;
                    }
                } else if (target instanceof PropertyIdDefinitionImpl) {
                    if (isTag(name, TAG_PROPERTY_TYPE_DEAULT_VALUE)) {
                        PropertyId prop = PROPERTY_ID_PARSER.walk(parser);
                        ((PropertyIdDefinitionImpl) target).setDefaultValue(prop.getValues());
                        return true;
                    }

                    if (isTag(name, TAG_PROPERTY_TYPE_CHOICE)) {
                        CHOICE_STRING_PARSER.addToChoiceList(parser, (PropertyStringDefinitionImpl) target);
                        return true;
                    }
                } else if (target instanceof PropertyBooleanDefinitionImpl) {
                    if (isTag(name, TAG_PROPERTY_TYPE_DEAULT_VALUE)) {
                        PropertyBoolean prop = PROPERTY_BOOLEAN_PARSER.walk(parser);
                        ((PropertyBooleanDefinitionImpl) target).setDefaultValue(prop.getValues());
                        return true;
                    }

                    if (isTag(name, TAG_PROPERTY_TYPE_CHOICE)) {
                        CHOICE_BOOLEAN_PARSER.addToChoiceList(parser, (PropertyBooleanDefinitionImpl) target);
                        return true;
                    }
                } else if (target instanceof PropertyIntegerDefinitionImpl) {
                    if (isTag(name, TAG_PROPERTY_TYPE_DEAULT_VALUE)) {
                        PropertyInteger prop = PROPERTY_INTEGER_PARSER.walk(parser);
                        ((PropertyIntegerDefinitionImpl) target).setDefaultValue(prop.getValues());
                        return true;
                    }

                    if (isTag(name, TAG_PROPERTY_TYPE_CHOICE)) {
                        CHOICE_INTEGER_PARSER.addToChoiceList(parser, (PropertyIntegerDefinitionImpl) target);
                        return true;
                    }

                    if (isTag(name, TAG_PROPERTY_TYPE_MAX_VALUE)) {
                        ((PropertyIntegerDefinitionImpl) target).setMaxValue(readInteger(parser));
                        return true;
                    }

                    if (isTag(name, TAG_PROPERTY_TYPE_MIN_VALUE)) {
                        ((PropertyIntegerDefinitionImpl) target).setMinValue(readInteger(parser));
                        return true;
                    }
                } else if (target instanceof PropertyDateTimeDefinitionImpl) {
                    if (isTag(name, TAG_PROPERTY_TYPE_DEAULT_VALUE)) {
                        PropertyDateTime prop = PROPERTY_DATETIME_PARSER.walk(parser);
                        ((PropertyDateTimeDefinitionImpl) target).setDefaultValue(prop.getValues());
                        return true;
                    }

                    if (isTag(name, TAG_PROPERTY_TYPE_CHOICE)) {
                        CHOICE_DATETIME_PARSER.addToChoiceList(parser, (PropertyDateTimeDefinitionImpl) target);
                        return true;
                    }

                    if (isTag(name, TAG_PROPERTY_TYPE_RESOLUTION)) {
                        ((PropertyDateTimeDefinitionImpl) target).setDateTimeResolution(readEnum(parser,
                                DateTimeResolution.class));
                        return true;
                    }
                } else if (target instanceof PropertyDecimalDefinitionImpl) {
                    if (isTag(name, TAG_PROPERTY_TYPE_DEAULT_VALUE)) {
                        PropertyDecimal prop = PROPERTY_DECIMAL_PARSER.walk(parser);
                        ((PropertyDecimalDefinitionImpl) target).setDefaultValue(prop.getValues());
                        return true;
                    }

                    if (isTag(name, TAG_PROPERTY_TYPE_CHOICE)) {
                        CHOICE_DECIMAL_PARSER.addToChoiceList(parser, (PropertyDecimalDefinitionImpl) target);
                        return true;
                    }

                    if (isTag(name, TAG_PROPERTY_TYPE_MAX_VALUE)) {
                        ((PropertyDecimalDefinitionImpl) target).setMaxValue(readDecimal(parser));
                        return true;
                    }

                    if (isTag(name, TAG_PROPERTY_TYPE_MIN_VALUE)) {
                        ((PropertyDecimalDefinitionImpl) target).setMinValue(readDecimal(parser));
                        return true;
                    }

                    if (isTag(name, TAG_PROPERTY_TYPE_PRECISION)) {
                        ((PropertyDecimalDefinitionImpl) target).setPrecision(readEnum(parser, DecimalPrecision.class));
                        return true;
                    }
                } else if (target instanceof PropertyHtmlDefinitionImpl) {
                    if (isTag(name, TAG_PROPERTY_TYPE_DEAULT_VALUE)) {
                        PropertyHtml prop = PROPERTY_HTML_PARSER.walk(parser);
                        ((PropertyHtmlDefinitionImpl) target).setDefaultValue(prop.getValues());
                        return true;
                    }

                    if (isTag(name, TAG_PROPERTY_TYPE_CHOICE)) {
                        CHOICE_STRING_PARSER.addToChoiceList(parser, (PropertyStringDefinitionImpl) target);
                        return true;
                    }
                } else if (target instanceof PropertyUriDefinitionImpl) {
                    if (isTag(name, TAG_PROPERTY_TYPE_DEAULT_VALUE)) {
                        PropertyUri prop = PROPERTY_URI_PARSER.walk(parser);
                        ((PropertyUriDefinitionImpl) target).setDefaultValue(prop.getValues());
                        return true;
                    }

                    if (isTag(name, TAG_PROPERTY_TYPE_CHOICE)) {
                        CHOICE_STRING_PARSER.addToChoiceList(parser, (PropertyStringDefinitionImpl) target);
                        return true;
                    }
                }
            }

            return false;
        }
    };

    private static final ChoiceXMLWalker<String> CHOICE_STRING_PARSER = new ChoiceXMLWalker<String>() {
        @Override
        protected ChoiceImpl<String> createTarget(XMLStreamReader parser, QName name) {
            return new ChoiceImpl<String>();
        }

        @Override
        protected void addValue(XMLStreamReader parser, ChoiceImpl<String> target) throws XMLStreamException {
            target.setValue(addToList(target.getValue(), readText(parser)));
        }

        protected void addChoice(XMLStreamReader parser, ChoiceImpl<String> target) throws XMLStreamException {
            target.setChoice(addToList(target.getChoice(), CHOICE_STRING_PARSER.walk(parser)));
        }
    };

    private static final ChoiceXMLWalker<Boolean> CHOICE_BOOLEAN_PARSER = new ChoiceXMLWalker<Boolean>() {
        @Override
        protected ChoiceImpl<Boolean> createTarget(XMLStreamReader parser, QName name) {
            return new ChoiceImpl<Boolean>();
        }

        @Override
        protected void addValue(XMLStreamReader parser, ChoiceImpl<Boolean> target) throws XMLStreamException {
            target.setValue(addToList(target.getValue(), readBoolean(parser)));
        }

        protected void addChoice(XMLStreamReader parser, ChoiceImpl<Boolean> target) throws XMLStreamException {
            target.setChoice(addToList(target.getChoice(), CHOICE_BOOLEAN_PARSER.walk(parser)));
        }
    };

    private static final ChoiceXMLWalker<BigInteger> CHOICE_INTEGER_PARSER = new ChoiceXMLWalker<BigInteger>() {
        @Override
        protected ChoiceImpl<BigInteger> createTarget(XMLStreamReader parser, QName name) {
            return new ChoiceImpl<BigInteger>();
        }

        @Override
        protected void addValue(XMLStreamReader parser, ChoiceImpl<BigInteger> target) throws XMLStreamException {
            target.setValue(addToList(target.getValue(), readInteger(parser)));
        }

        protected void addChoice(XMLStreamReader parser, ChoiceImpl<BigInteger> target) throws XMLStreamException {
            target.setChoice(addToList(target.getChoice(), CHOICE_INTEGER_PARSER.walk(parser)));
        }
    };

    private static final ChoiceXMLWalker<GregorianCalendar> CHOICE_DATETIME_PARSER = new ChoiceXMLWalker<GregorianCalendar>() {
        @Override
        protected ChoiceImpl<GregorianCalendar> createTarget(XMLStreamReader parser, QName name) {
            return new ChoiceImpl<GregorianCalendar>();
        }

        @Override
        protected void addValue(XMLStreamReader parser, ChoiceImpl<GregorianCalendar> target) throws XMLStreamException {
            target.setValue(addToList(target.getValue(), readDateTime(parser)));
        }

        protected void addChoice(XMLStreamReader parser, ChoiceImpl<GregorianCalendar> target)
                throws XMLStreamException {
            target.setChoice(addToList(target.getChoice(), CHOICE_DATETIME_PARSER.walk(parser)));
        }
    };

    private static final ChoiceXMLWalker<BigDecimal> CHOICE_DECIMAL_PARSER = new ChoiceXMLWalker<BigDecimal>() {
        @Override
        protected ChoiceImpl<BigDecimal> createTarget(XMLStreamReader parser, QName name) {
            return new ChoiceImpl<BigDecimal>();
        }

        @Override
        protected void addValue(XMLStreamReader parser, ChoiceImpl<BigDecimal> target) throws XMLStreamException {
            target.setValue(addToList(target.getValue(), readDecimal(parser)));
        }

        protected void addChoice(XMLStreamReader parser, ChoiceImpl<BigDecimal> target) throws XMLStreamException {
            target.setChoice(addToList(target.getChoice(), CHOICE_DECIMAL_PARSER.walk(parser)));
        }
    };

    private static abstract class ChoiceXMLWalker<T> extends XMLWalker<ChoiceImpl<T>> {

        public void addToChoiceList(XMLStreamReader parser, AbstractPropertyDefinition<T> propDef)
                throws XMLStreamException {
            propDef.setChoices(addToList(propDef.getChoices(), walk(parser)));
        }

        protected abstract ChoiceImpl<T> createTarget(XMLStreamReader parser, QName name);

        @Override
        protected ChoiceImpl<T> prepareTarget(XMLStreamReader parser, QName name) throws XMLStreamException {
            ChoiceImpl<T> result = createTarget(parser, name);

            if (parser.getAttributeCount() > 0) {
                for (int i = 0; i < parser.getAttributeCount(); i++) {
                    String attr = parser.getAttributeLocalName(i);
                    if (ATTR_PROPERTY_TYPE_CHOICE_DISPLAYNAME.equals(attr)) {
                        result.setDisplayName(parser.getAttributeValue(i));
                    }
                }
            }

            return result;
        }

        @Override
        protected boolean read(XMLStreamReader parser, QName name, ChoiceImpl<T> target) throws XMLStreamException {
            if (isCmisNamespace(name)) {
                if (isTag(name, TAG_PROPERTY_TYPE_CHOICE_VALUE)) {
                    addValue(parser, target);
                    return true;
                }

                if (isTag(name, TAG_PROPERTY_TYPE_CHOICE_CHOICE)) {
                    addChoice(parser, target);
                    return true;
                }
            }

            return false;
        }

        protected abstract void addValue(XMLStreamReader parser, ChoiceImpl<T> target) throws XMLStreamException;

        protected abstract void addChoice(XMLStreamReader parser, ChoiceImpl<T> target) throws XMLStreamException;
    };

    // --------------------------------
    // --- objects and lists parsers ---
    // --------------------------------

    // ------------------------
    // --- property parsers ---
    // ------------------------

    private static final PropertyXMLWalker<PropertyStringImpl> PROPERTY_STRING_PARSER = new PropertyStringXMLWalker<PropertyStringImpl>() {
        @Override
        protected PropertyStringImpl createTarget(XMLStreamReader parser, QName name) {
            return new PropertyStringImpl();
        }
    };

    private static final PropertyXMLWalker<PropertyIdImpl> PROPERTY_ID_PARSER = new PropertyStringXMLWalker<PropertyIdImpl>() {
        @Override
        protected PropertyIdImpl createTarget(XMLStreamReader parser, QName name) {
            return new PropertyIdImpl();
        }
    };

    private static final PropertyXMLWalker<PropertyHtmlImpl> PROPERTY_HTML_PARSER = new PropertyStringXMLWalker<PropertyHtmlImpl>() {
        @Override
        protected PropertyHtmlImpl createTarget(XMLStreamReader parser, QName name) {
            return new PropertyHtmlImpl();
        }
    };

    private static final PropertyXMLWalker<PropertyUriImpl> PROPERTY_URI_PARSER = new PropertyStringXMLWalker<PropertyUriImpl>() {
        @Override
        protected PropertyUriImpl createTarget(XMLStreamReader parser, QName name) {
            return new PropertyUriImpl();
        }
    };

    private static final PropertyXMLWalker<PropertyBooleanImpl> PROPERTY_BOOLEAN_PARSER = new PropertyXMLWalker<PropertyBooleanImpl>() {
        @Override
        protected PropertyBooleanImpl createTarget(XMLStreamReader parser, QName name) {
            return new PropertyBooleanImpl();
        }

        @Override
        protected void addValue(XMLStreamReader parser, PropertyBooleanImpl target) throws XMLStreamException {
            target.setValues(addToList(target.getValues(), readBoolean(parser)));
        }
    };

    private static final PropertyXMLWalker<PropertyIntegerImpl> PROPERTY_INTEGER_PARSER = new PropertyXMLWalker<PropertyIntegerImpl>() {
        @Override
        protected PropertyIntegerImpl createTarget(XMLStreamReader parser, QName name) {
            return new PropertyIntegerImpl();
        }

        @Override
        protected void addValue(XMLStreamReader parser, PropertyIntegerImpl target) throws XMLStreamException {
            target.setValues(addToList(target.getValues(), readInteger(parser)));
        }
    };

    private static final PropertyXMLWalker<PropertyDecimalImpl> PROPERTY_DECIMAL_PARSER = new PropertyXMLWalker<PropertyDecimalImpl>() {
        @Override
        protected PropertyDecimalImpl createTarget(XMLStreamReader parser, QName name) {
            return new PropertyDecimalImpl();
        }

        @Override
        protected void addValue(XMLStreamReader parser, PropertyDecimalImpl target) throws XMLStreamException {
            target.setValues(addToList(target.getValues(), readDecimal(parser)));
        }
    };

    private static final PropertyXMLWalker<PropertyDateTimeImpl> PROPERTY_DATETIME_PARSER = new PropertyXMLWalker<PropertyDateTimeImpl>() {
        @Override
        protected PropertyDateTimeImpl createTarget(XMLStreamReader parser, QName name) {
            return new PropertyDateTimeImpl();
        }

        @Override
        protected void addValue(XMLStreamReader parser, PropertyDateTimeImpl target) throws XMLStreamException {
            target.setValues(addToList(target.getValues(), readDateTime(parser)));
        }
    };

    private static abstract class PropertyXMLWalker<T extends AbstractPropertyData<?>> extends XMLWalker<T> {

        protected abstract T createTarget(XMLStreamReader parser, QName name);

        @Override
        protected T prepareTarget(XMLStreamReader parser, QName name) throws XMLStreamException {
            T result = createTarget(parser, name);

            if (parser.getAttributeCount() > 0) {
                for (int i = 0; i < parser.getAttributeCount(); i++) {
                    String attr = parser.getAttributeLocalName(i);
                    if (ATTR_PROPERTY_ID.equals(attr)) {
                        result.setId(parser.getAttributeValue(i));
                    } else if (ATTR_PROPERTY_LOCALNAME.equals(attr)) {
                        result.setLocalName(parser.getAttributeValue(i));
                    } else if (ATTR_PROPERTY_DISPLAYNAME.equals(attr)) {
                        result.setDisplayName(parser.getAttributeValue(i));
                    } else if (ATTR_PROPERTY_QUERYNAME.equals(attr)) {
                        result.setQueryName(parser.getAttributeValue(i));
                    }
                }
            }

            return result;
        }

        protected abstract void addValue(XMLStreamReader parser, T target) throws XMLStreamException;

        @Override
        protected boolean read(XMLStreamReader parser, QName name, T target) throws XMLStreamException {
            if (isCmisNamespace(name)) {
                if (isTag(name, TAG_PROPERTY_VALUE)) {
                    addValue(parser, target);
                    return true;
                }
            }

            return false;
        }

    };

    private static abstract class PropertyStringXMLWalker<T extends AbstractPropertyData<String>> extends
            PropertyXMLWalker<T> {
        @Override
        protected void addValue(XMLStreamReader parser, T target) throws XMLStreamException {
            target.setValues(addToList(target.getValues(), readText(parser)));
        }
    }
}
