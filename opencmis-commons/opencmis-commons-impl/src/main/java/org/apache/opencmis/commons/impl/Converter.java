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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.opencmis.commons.api.Choice;
import org.apache.opencmis.commons.api.DocumentTypeDefinition;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.FolderTypeDefinition;
import org.apache.opencmis.commons.api.PolicyTypeDefinition;
import org.apache.opencmis.commons.api.PropertyBooleanDefinition;
import org.apache.opencmis.commons.api.PropertyDateTimeDefinition;
import org.apache.opencmis.commons.api.PropertyDecimalDefinition;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.api.PropertyHtmlDefinition;
import org.apache.opencmis.commons.api.PropertyIdDefinition;
import org.apache.opencmis.commons.api.PropertyIntegerDefinition;
import org.apache.opencmis.commons.api.PropertyStringDefinition;
import org.apache.opencmis.commons.api.PropertyUriDefinition;
import org.apache.opencmis.commons.api.RelationshipTypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.api.TypeDefinitionList;
import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.enums.CapabilityAcl;
import org.apache.opencmis.commons.enums.CapabilityChanges;
import org.apache.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.opencmis.commons.enums.CapabilityJoin;
import org.apache.opencmis.commons.enums.CapabilityQuery;
import org.apache.opencmis.commons.enums.CapabilityRendition;
import org.apache.opencmis.commons.enums.Cardinality;
import org.apache.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.opencmis.commons.enums.DateTimeResolution;
import org.apache.opencmis.commons.enums.PropertyType;
import org.apache.opencmis.commons.enums.SupportedPermissions;
import org.apache.opencmis.commons.enums.TypeOfChanges;
import org.apache.opencmis.commons.enums.Updatability;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.dataobjects.AbstractPropertyData;
import org.apache.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.apache.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.AclCapabilitiesDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.AllowableActionsDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.ChangeEventInfoDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.ChoiceImpl;
import org.apache.opencmis.commons.impl.dataobjects.ContentStreamDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.ExtensionDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectInFolderContainerImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PolicyIdListDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PolicyTypeDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertiesDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyBooleanDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyDateTimeDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyDecimalDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyHtmlDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyHtmlDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyIdDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyIntegerDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyStringDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyUriDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyUriDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.RelationshipTypeDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.RenditionDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.RepositoryInfoDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.TypeDefinitionContainerImpl;
import org.apache.opencmis.commons.impl.dataobjects.TypeDefinitionListImpl;
import org.apache.opencmis.commons.impl.jaxb.CmisACLCapabilityType;
import org.apache.opencmis.commons.impl.jaxb.CmisACLType;
import org.apache.opencmis.commons.impl.jaxb.CmisAccessControlEntryType;
import org.apache.opencmis.commons.impl.jaxb.CmisAccessControlListType;
import org.apache.opencmis.commons.impl.jaxb.CmisAccessControlPrincipalType;
import org.apache.opencmis.commons.impl.jaxb.CmisAllowableActionsType;
import org.apache.opencmis.commons.impl.jaxb.CmisChangeEventType;
import org.apache.opencmis.commons.impl.jaxb.CmisChoiceBoolean;
import org.apache.opencmis.commons.impl.jaxb.CmisChoiceDateTime;
import org.apache.opencmis.commons.impl.jaxb.CmisChoiceDecimal;
import org.apache.opencmis.commons.impl.jaxb.CmisChoiceHtml;
import org.apache.opencmis.commons.impl.jaxb.CmisChoiceId;
import org.apache.opencmis.commons.impl.jaxb.CmisChoiceInteger;
import org.apache.opencmis.commons.impl.jaxb.CmisChoiceString;
import org.apache.opencmis.commons.impl.jaxb.CmisChoiceUri;
import org.apache.opencmis.commons.impl.jaxb.CmisContentStreamType;
import org.apache.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.opencmis.commons.impl.jaxb.CmisListOfIdsType;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectInFolderContainerType;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectInFolderListType;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectInFolderType;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectListType;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectParentsType;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.impl.jaxb.CmisPermissionDefinition;
import org.apache.opencmis.commons.impl.jaxb.CmisPermissionMapping;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertiesType;
import org.apache.opencmis.commons.impl.jaxb.CmisProperty;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyBoolean;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyBooleanDefinitionType;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyDateTime;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyDateTimeDefinitionType;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyDecimal;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyDecimalDefinitionType;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyDefinitionType;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyHtml;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyHtmlDefinitionType;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyId;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyIdDefinitionType;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyInteger;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyIntegerDefinitionType;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyString;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyStringDefinitionType;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyUri;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyUriDefinitionType;
import org.apache.opencmis.commons.impl.jaxb.CmisRenditionType;
import org.apache.opencmis.commons.impl.jaxb.CmisRepositoryCapabilitiesType;
import org.apache.opencmis.commons.impl.jaxb.CmisRepositoryInfoType;
import org.apache.opencmis.commons.impl.jaxb.CmisTypeContainer;
import org.apache.opencmis.commons.impl.jaxb.CmisTypeDefinitionListType;
import org.apache.opencmis.commons.impl.jaxb.CmisTypeDefinitionType;
import org.apache.opencmis.commons.impl.jaxb.CmisTypeDocumentDefinitionType;
import org.apache.opencmis.commons.impl.jaxb.CmisTypeFolderDefinitionType;
import org.apache.opencmis.commons.impl.jaxb.CmisTypePolicyDefinitionType;
import org.apache.opencmis.commons.impl.jaxb.CmisTypeRelationshipDefinitionType;
import org.apache.opencmis.commons.impl.jaxb.DeleteTreeResponse;
import org.apache.opencmis.commons.impl.jaxb.EnumACLPropagation;
import org.apache.opencmis.commons.impl.jaxb.EnumAllowableActionsKey;
import org.apache.opencmis.commons.impl.jaxb.EnumBaseObjectTypeIds;
import org.apache.opencmis.commons.impl.jaxb.EnumCapabilityACL;
import org.apache.opencmis.commons.impl.jaxb.EnumCapabilityChanges;
import org.apache.opencmis.commons.impl.jaxb.EnumCapabilityContentStreamUpdates;
import org.apache.opencmis.commons.impl.jaxb.EnumCapabilityJoin;
import org.apache.opencmis.commons.impl.jaxb.EnumCapabilityQuery;
import org.apache.opencmis.commons.impl.jaxb.EnumCapabilityRendition;
import org.apache.opencmis.commons.impl.jaxb.EnumCardinality;
import org.apache.opencmis.commons.impl.jaxb.EnumContentStreamAllowed;
import org.apache.opencmis.commons.impl.jaxb.EnumDateTimeResolution;
import org.apache.opencmis.commons.impl.jaxb.EnumPropertyType;
import org.apache.opencmis.commons.impl.jaxb.EnumSupportedPermissions;
import org.apache.opencmis.commons.impl.jaxb.EnumTypeOfChanges;
import org.apache.opencmis.commons.impl.jaxb.EnumUpdatability;
import org.apache.opencmis.commons.provider.AccessControlEntry;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.AclCapabilitiesData;
import org.apache.opencmis.commons.provider.AllowableActionsData;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.FailedToDeleteData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectInFolderContainer;
import org.apache.opencmis.commons.provider.ObjectInFolderData;
import org.apache.opencmis.commons.provider.ObjectInFolderList;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.commons.provider.ObjectParentData;
import org.apache.opencmis.commons.provider.PermissionDefinitionData;
import org.apache.opencmis.commons.provider.PermissionMappingData;
import org.apache.opencmis.commons.provider.PolicyIdListData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyBooleanData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.PropertyDateTimeData;
import org.apache.opencmis.commons.provider.PropertyDecimalData;
import org.apache.opencmis.commons.provider.PropertyHtmlData;
import org.apache.opencmis.commons.provider.PropertyIdData;
import org.apache.opencmis.commons.provider.PropertyIntegerData;
import org.apache.opencmis.commons.provider.PropertyStringData;
import org.apache.opencmis.commons.provider.PropertyUriData;
import org.apache.opencmis.commons.provider.RenditionData;
import org.apache.opencmis.commons.provider.RepositoryCapabilitiesData;
import org.apache.opencmis.commons.provider.RepositoryInfoData;

import com.sun.xml.ws.developer.StreamingDataHandler;

/**
 * Contains converter methods.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public final class Converter {

  /**
   * Private constructor.
   */
  private Converter() {
  }

  // -------------------------------------------------------------------------
  // --- Repository Info ---
  // -------------------------------------------------------------------------

  /**
   * Converts a repository info object.
   */
  public static RepositoryInfoData convert(CmisRepositoryInfoType repositoryInfo) {
    if (repositoryInfo == null) {
      return null;
    }

    RepositoryInfoDataImpl result = new RepositoryInfoDataImpl();

    result.setAclCapabilities(convert(repositoryInfo.getAclCapability()));
    result.setChangesIncomplete(repositoryInfo.isChangesIncomplete());
    List<BaseObjectTypeIds> baseObjectTypeIds = new ArrayList<BaseObjectTypeIds>();
    for (EnumBaseObjectTypeIds bot : repositoryInfo.getChangesOnType()) {
      baseObjectTypeIds.add(convert(BaseObjectTypeIds.class, bot));
    }
    result.setChangesOnType(baseObjectTypeIds);
    result.setCmisVersionSupported(repositoryInfo.getCmisVersionSupported());
    result.setLatestChangeLogToken(repositoryInfo.getLatestChangeLogToken());
    result.setPrincipalAnonymous(repositoryInfo.getPrincipalAnonymous());
    result.setPrincipalAnyone(repositoryInfo.getPrincipalAnyone());
    result.setProductName(repositoryInfo.getProductName());
    result.setProductVersion(repositoryInfo.getProductVersion());
    result.setRepositoryCapabilities(convert(repositoryInfo.getCapabilities()));
    result.setRepositoryDescription(repositoryInfo.getRepositoryDescription());
    result.setRepositoryId(repositoryInfo.getRepositoryId());
    result.setRepositoryName(repositoryInfo.getRepositoryName());
    result.setRootFolder(repositoryInfo.getRootFolderId());
    result.setThinClientUri(repositoryInfo.getThinClientURI());
    result.setVendorName(repositoryInfo.getVendorName());

    // handle extensions
    convertExtension(repositoryInfo, result);

    return result;
  }

  /**
   * Converts a repository capability object.
   */
  public static RepositoryCapabilitiesData convert(CmisRepositoryCapabilitiesType capabilities) {
    if (capabilities == null) {
      return null;
    }

    RepositoryCapabilitiesDataImpl result = new RepositoryCapabilitiesDataImpl();

    result.setAllVersionsSearchable(capabilities.isCapabilityAllVersionsSearchable());
    result.setCapabilityAcl(convert(CapabilityAcl.class, capabilities.getCapabilityACL()));
    result.setCapabilityChanges(convert(CapabilityChanges.class, capabilities
        .getCapabilityChanges()));
    result.setCapabilityContentStreamUpdates(convert(CapabilityContentStreamUpdates.class,
        capabilities.getCapabilityContentStreamUpdatability()));
    result.setCapabilityJoin(convert(CapabilityJoin.class, capabilities.getCapabilityJoin()));
    result.setCapabilityQuery(convert(CapabilityQuery.class, capabilities.getCapabilityQuery()));
    result.setCapabilityRendition(convert(CapabilityRendition.class, capabilities
        .getCapabilityRenditions()));
    result.setIsPwcSearchable(capabilities.isCapabilityPWCSearchable());
    result.setIsPwcUpdatable(capabilities.isCapabilityPWCUpdatable());
    result.setSupportsGetDescendants(capabilities.isCapabilityGetDescendants());
    result.setSupportsGetFolderTree(capabilities.isCapabilityGetFolderTree());
    result.setSupportsMultifiling(capabilities.isCapabilityMultifiling());
    result.setSupportsUnfiling(capabilities.isCapabilityUnfiling());
    result.setSupportsVersionSpecificFiling(capabilities.isCapabilityVersionSpecificFiling());

    // handle extensions
    convertExtension(capabilities, result);

    return result;
  }

  /**
   * Converts a ACL capability object.
   */
  public static AclCapabilitiesData convert(CmisACLCapabilityType aclCapabilities) {
    if (aclCapabilities == null) {
      return null;
    }

    AclCapabilitiesDataImpl result = new AclCapabilitiesDataImpl();

    result.setSupportedPermissions(convert(SupportedPermissions.class, aclCapabilities
        .getSupportedPermissions()));

    result.setAclPropagation(convert(AclPropagation.class, aclCapabilities.getPropagation()));

    List<PermissionDefinitionData> permissionDefinitionList = new ArrayList<PermissionDefinitionData>();
    for (CmisPermissionDefinition permDef : aclCapabilities.getPermissions()) {
      PermissionDefinitionDataImpl permDefData = new PermissionDefinitionDataImpl();
      permDefData.setPermission(permDef.getPermission());
      permDefData.setDescription(permDef.getDescription());
      convertExtension(permDef, permDefData);

      permissionDefinitionList.add(permDefData);
    }
    result.setPermissionDefinitionData(permissionDefinitionList);

    List<PermissionMappingData> permissionMapping = new ArrayList<PermissionMappingData>();
    for (CmisPermissionMapping permMapping : aclCapabilities.getMapping()) {
      if (permMapping.getKey() != null) {
        PermissionMappingDataImpl permMappingData = new PermissionMappingDataImpl();
        permMappingData.setKey(permMapping.getKey().value());
        permMappingData.setPermissions(permMapping.getPermission());
        convertExtension(permMapping, permMappingData);

        permissionMapping.add(permMappingData);
      }
    }
    result.setPermissionMappingData(permissionMapping);

    // handle extensions
    convertExtension(aclCapabilities, result);

    return result;
  }

  /**
   * Converts a repository info object.
   */
  public static CmisRepositoryInfoType convert(RepositoryInfoData repositoryInfo) {
    if (repositoryInfo == null) {
      return null;
    }

    CmisRepositoryInfoType result = new CmisRepositoryInfoType();

    result.setAclCapability(convert(repositoryInfo.getAclCapabilities()));
    result.setCapabilities(convert(repositoryInfo.getRepositoryCapabilities()));
    result.setChangesIncomplete(repositoryInfo.changesIncomplete());
    result.setCmisVersionSupported(repositoryInfo.getCmisVersionSupported());
    result.setLatestChangeLogToken(repositoryInfo.getLatestChangeLogToken());
    result.setPrincipalAnonymous(repositoryInfo.getPrincipalAnonymous());
    result.setPrincipalAnyone(repositoryInfo.getPrincipalAnyone());
    result.setProductName(repositoryInfo.getProductName());
    result.setProductVersion(repositoryInfo.getProductVersion());
    result.setRepositoryDescription(repositoryInfo.getRepositoryDescription());
    result.setRepositoryId(repositoryInfo.getRepositoryId());
    result.setRepositoryName(repositoryInfo.getRepositoryName());
    result.setRootFolderId(repositoryInfo.getRootFolderId());
    result.setThinClientURI(repositoryInfo.getThinClientUri());
    result.setVendorName(repositoryInfo.getVendorName());

    if (repositoryInfo.getChangesOnType() != null) {
      for (BaseObjectTypeIds boti : repositoryInfo.getChangesOnType()) {
        result.getChangesOnType().add(convert(EnumBaseObjectTypeIds.class, boti));
      }
    }

    // handle extensions
    convertExtension(repositoryInfo, result);

    return result;
  }

  /**
   * Converts a repository capability object.
   */
  public static CmisRepositoryCapabilitiesType convert(RepositoryCapabilitiesData capabilities) {
    if (capabilities == null) {
      return null;
    }

    CmisRepositoryCapabilitiesType result = new CmisRepositoryCapabilitiesType();

    result.setCapabilityACL(convert(EnumCapabilityACL.class, capabilities.getCapabilityAcl()));
    result.setCapabilityAllVersionsSearchable(capabilities.allVersionsSearchable());
    result.setCapabilityChanges(convert(EnumCapabilityChanges.class, capabilities
        .getCapabilityChanges()));
    result.setCapabilityContentStreamUpdatability(convert(EnumCapabilityContentStreamUpdates.class,
        capabilities.getCapabilityContentStreamUpdatability()));
    result.setCapabilityGetDescendants(capabilities.supportsGetDescendants());
    result.setCapabilityGetFolderTree(capabilities.supportsGetFolderTree());
    result.setCapabilityJoin(convert(EnumCapabilityJoin.class, capabilities.getCapabilityJoin()));
    result.setCapabilityMultifiling(capabilities.supportsMultifiling());
    result.setCapabilityPWCSearchable(capabilities.isPwcSearchable());
    result.setCapabilityPWCUpdatable(capabilities.isPwcUpdatable());
    result
        .setCapabilityQuery(convert(EnumCapabilityQuery.class, capabilities.getCapabilityQuery()));
    result.setCapabilityRenditions(convert(EnumCapabilityRendition.class, capabilities
        .getCapabilityRenditions()));
    result.setCapabilityUnfiling(capabilities.supportsUnfiling());
    result.setCapabilityVersionSpecificFiling(capabilities.supportsVersionSpecificFiling());

    // handle extensions
    convertExtension(capabilities, result);

    return result;
  }

  /**
   * Converts a ACL capability object.
   */
  public static CmisACLCapabilityType convert(AclCapabilitiesData aclCapabilities) {
    if (aclCapabilities == null) {
      return null;
    }

    CmisACLCapabilityType result = new CmisACLCapabilityType();

    result.setSupportedPermissions(convert(EnumSupportedPermissions.class, aclCapabilities
        .getSupportedPermissions()));

    result.setPropagation(convert(EnumACLPropagation.class, aclCapabilities.getAclPropagation()));

    if (aclCapabilities.getPermissionDefinitionData() != null) {
      for (PermissionDefinitionData pdd : aclCapabilities.getPermissionDefinitionData()) {
        CmisPermissionDefinition permDef = new CmisPermissionDefinition();
        permDef.setDescription(pdd.getDescription());
        permDef.setPermission(pdd.getPermission());
        convertExtension(pdd, permDef);

        result.getPermissions().add(permDef);
      }
    }

    if (aclCapabilities.getPermissionMappingData() != null) {
      for (PermissionMappingData pmd : aclCapabilities.getPermissionMappingData()) {
        CmisPermissionMapping permMap = new CmisPermissionMapping();
        permMap.setKey(EnumAllowableActionsKey.fromValue(pmd.getKey()));

        if (pmd.getPermissions() != null) {
          for (String permission : pmd.getPermissions()) {
            permMap.getPermission().add(permission);
          }
        }

        convertExtension(pmd, permMap);

        result.getMapping().add(permMap);
      }
    }

    // handle extensions
    convertExtension(aclCapabilities, result);

    return result;
  }

  // -------------------------------------------------------------------------
  // --- Types ---
  // -------------------------------------------------------------------------

  /**
   * Converts a type definition object.
   */
  public static TypeDefinition convert(CmisTypeDefinitionType typeDefinition) {
    if (typeDefinition == null) {
      return null;
    }

    AbstractTypeDefinition result = null;

    if (typeDefinition instanceof CmisTypeFolderDefinitionType) {
      result = new FolderTypeDefinitionImpl();
    }
    else if (typeDefinition instanceof CmisTypeDocumentDefinitionType) {
      result = new DocumentTypeDefinitionImpl();

      ((DocumentTypeDefinitionImpl) result).setContentStreamAllowed(convert(
          ContentStreamAllowed.class, ((CmisTypeDocumentDefinitionType) typeDefinition)
              .getContentStreamAllowed()));
      ((DocumentTypeDefinitionImpl) result)
          .setIsVersionable(((CmisTypeDocumentDefinitionType) typeDefinition).isVersionable());
    }
    else if (typeDefinition instanceof CmisTypeRelationshipDefinitionType) {
      result = new RelationshipTypeDefinitionImpl();

      ((RelationshipTypeDefinitionImpl) result)
          .setAllowedSourceTypes(((CmisTypeRelationshipDefinitionType) typeDefinition)
              .getAllowedSourceTypes());
      ((RelationshipTypeDefinitionImpl) result)
          .setAllowedTargetTypes(((CmisTypeRelationshipDefinitionType) typeDefinition)
              .getAllowedTargetTypes());
    }
    else if (typeDefinition instanceof CmisTypePolicyDefinitionType) {
      result = new PolicyTypeDefinitionImpl();
    }
    else {
      throw new CmisRuntimeException("Type '" + typeDefinition.getId()
          + "' does not match a base type!");
    }

    result.setBaseId(convert(BaseObjectTypeIds.class, typeDefinition.getBaseId()));
    result.setDescription(typeDefinition.getDescription());
    result.setDisplayName(typeDefinition.getDisplayName());
    result.setId(typeDefinition.getId());
    result.setIsControllableAcl(typeDefinition.isControllableACL());
    result.setIsControllablePolicy(typeDefinition.isControllablePolicy());
    result.setIsCreatable(typeDefinition.isCreatable());
    result.setIsFileable(typeDefinition.isFileable());
    result.setIsFulltextIndexed(typeDefinition.isFulltextIndexed());
    result.setIsIncludedInSupertypeQuery(typeDefinition.isIncludedInSupertypeQuery());
    result.setIsQueryable(typeDefinition.isQueryable());
    result.setLocalName(typeDefinition.getLocalName());
    result.setLocalNamespace(typeDefinition.getLocalNamespace());
    result.setParentId(typeDefinition.getParentId());
    result.setQueryName(typeDefinition.getQueryName());

    for (CmisPropertyDefinitionType propertyDefinition : typeDefinition.getPropertyDefinition()) {
      result.addPropertyDefinition(convert(propertyDefinition));
    }

    // handle extensions
    convertExtension(typeDefinition, result);

    return result;
  }

  /**
   * Converts a property definition object.
   */
  public static PropertyDefinition<?> convert(CmisPropertyDefinitionType propertyDefinition) {
    if (propertyDefinition == null) {
      return null;
    }

    AbstractPropertyDefinition<?> result = null;

    if (propertyDefinition instanceof CmisPropertyStringDefinitionType) {
      result = new PropertyStringDefinitionImpl();

      ((PropertyStringDefinitionImpl) result)
          .setChoices(convertChoiceStringList(((CmisPropertyStringDefinitionType) propertyDefinition)
              .getChoice()));

      CmisPropertyString prop = ((CmisPropertyStringDefinitionType) propertyDefinition)
          .getDefaultValue();
      if (prop != null) {
        ((PropertyStringDefinitionImpl) result).setDefaultValue(prop.getValue());
      }

      // specific
      ((PropertyStringDefinitionImpl) result)
          .setMaxLength(((CmisPropertyStringDefinitionType) propertyDefinition).getMaxLength());
    }
    else if (propertyDefinition instanceof CmisPropertyIdDefinitionType) {
      result = new PropertyIdDefinitionImpl();

      ((PropertyIdDefinitionImpl) result)
          .setChoices(convertChoiceIdList(((CmisPropertyIdDefinitionType) propertyDefinition)
              .getChoice()));

      CmisPropertyId prop = ((CmisPropertyIdDefinitionType) propertyDefinition).getDefaultValue();
      if (prop != null) {
        ((PropertyIdDefinitionImpl) result).setDefaultValue(prop.getValue());
      }
    }
    else if (propertyDefinition instanceof CmisPropertyIntegerDefinitionType) {
      result = new PropertyIntegerDefinitionImpl();

      ((PropertyIntegerDefinitionImpl) result)
          .setChoices(convertChoiceIntegerList(((CmisPropertyIntegerDefinitionType) propertyDefinition)
              .getChoice()));

      CmisPropertyInteger prop = ((CmisPropertyIntegerDefinitionType) propertyDefinition)
          .getDefaultValue();
      if (prop != null) {
        ((PropertyIntegerDefinitionImpl) result).setDefaultValue(prop.getValue());
      }

      // specific
      ((PropertyIntegerDefinitionImpl) result)
          .setMinValue(((CmisPropertyIntegerDefinitionType) propertyDefinition).getMinValue());
      ((PropertyIntegerDefinitionImpl) result)
          .setMaxValue(((CmisPropertyIntegerDefinitionType) propertyDefinition).getMaxValue());
    }
    else if (propertyDefinition instanceof CmisPropertyDecimalDefinitionType) {
      result = new PropertyDecimalDefinitionImpl();

      ((PropertyDecimalDefinitionImpl) result)
          .setChoices(convertChoiceDecimalList(((CmisPropertyDecimalDefinitionType) propertyDefinition)
              .getChoice()));

      CmisPropertyDecimal prop = ((CmisPropertyDecimalDefinitionType) propertyDefinition)
          .getDefaultValue();
      if (prop != null) {
        ((PropertyDecimalDefinitionImpl) result).setDefaultValue(prop.getValue());
      }

      // specific
      ((PropertyDecimalDefinitionImpl) result)
          .setMinValue(((CmisPropertyDecimalDefinitionType) propertyDefinition).getMinValue());
      ((PropertyDecimalDefinitionImpl) result)
          .setMaxValue(((CmisPropertyDecimalDefinitionType) propertyDefinition).getMaxValue());

    }
    else if (propertyDefinition instanceof CmisPropertyBooleanDefinitionType) {
      result = new PropertyBooleanDefinitionImpl();

      ((PropertyBooleanDefinitionImpl) result)
          .setChoices(convertChoiceBooleanList(((CmisPropertyBooleanDefinitionType) propertyDefinition)
              .getChoice()));

      CmisPropertyBoolean prop = ((CmisPropertyBooleanDefinitionType) propertyDefinition)
          .getDefaultValue();
      if (prop != null) {
        ((PropertyBooleanDefinitionImpl) result).setDefaultValue(prop.getValue());
      }
    }
    else if (propertyDefinition instanceof CmisPropertyDateTimeDefinitionType) {
      result = new PropertyDateTimeDefinitionImpl();

      ((PropertyDateTimeDefinitionImpl) result)
          .setChoices(convertChoiceDateTimeList(((CmisPropertyDateTimeDefinitionType) propertyDefinition)
              .getChoice()));

      CmisPropertyDateTime prop = ((CmisPropertyDateTimeDefinitionType) propertyDefinition)
          .getDefaultValue();
      if (prop != null) {
        ((PropertyDateTimeDefinitionImpl) result).setDefaultValue(convertXMLCalendar(prop
            .getValue()));
      }

      // specific
      ((PropertyDateTimeDefinitionImpl) result).setDateTimeResolution(convert(
          DateTimeResolution.class, ((CmisPropertyDateTimeDefinitionType) propertyDefinition)
              .getResolution()));
    }
    else if (propertyDefinition instanceof CmisPropertyHtmlDefinitionType) {
      result = new PropertyHtmlDefinitionImpl();

      ((PropertyHtmlDefinitionImpl) result)
          .setChoices(convertChoiceHtmlList(((CmisPropertyHtmlDefinitionType) propertyDefinition)
              .getChoice()));

      CmisPropertyHtml prop = ((CmisPropertyHtmlDefinitionType) propertyDefinition)
          .getDefaultValue();
      if (prop != null) {
        ((PropertyHtmlDefinitionImpl) result).setDefaultValue(prop.getValue());
      }
    }
    else if (propertyDefinition instanceof CmisPropertyUriDefinitionType) {
      result = new PropertyUriDefinitionImpl();

      ((PropertyUriDefinitionImpl) result)
          .setChoices(convertChoiceUriList(((CmisPropertyUriDefinitionType) propertyDefinition)
              .getChoice()));

      CmisPropertyUri prop = ((CmisPropertyUriDefinitionType) propertyDefinition).getDefaultValue();
      if (prop != null) {
        ((PropertyUriDefinitionImpl) result).setDefaultValue(prop.getValue());
      }
    }
    else {
      return null;
    }

    result.setCardinality(convert(Cardinality.class, propertyDefinition.getCardinality()));
    result.setDescription(propertyDefinition.getDescription());
    result.setDisplayName(propertyDefinition.getDisplayName());
    result.setId(propertyDefinition.getId());
    result.setIsInherited(propertyDefinition.isInherited());
    result.setIsOpenChoice(propertyDefinition.isOpenChoice());
    result.setIsQueryable(propertyDefinition.isQueryable());
    result.setIsOrderable(propertyDefinition.isOrderable());
    result.setIsRequired(propertyDefinition.isRequired());
    result.setLocalName(propertyDefinition.getLocalName());
    result.setLocalNamespace(propertyDefinition.getLocalNamespace());
    result.setPropertyType(convert(PropertyType.class, propertyDefinition.getPropertyType()));
    result.setQueryName(propertyDefinition.getQueryName());
    result.setUpdatability(convert(Updatability.class, propertyDefinition.getUpdatability()));

    // handle extensions
    convertExtension(propertyDefinition, result);

    return result;
  }

  /**
   * Converts a type definition object.
   */
  public static CmisTypeDefinitionType convert(TypeDefinition typeDefinition) {
    if (typeDefinition == null) {
      return null;
    }

    CmisTypeDefinitionType result = null;

    if (typeDefinition instanceof DocumentTypeDefinition) {
      result = new CmisTypeDocumentDefinitionType();

      DocumentTypeDefinition docTypeDefintion = (DocumentTypeDefinition) typeDefinition;
      ((CmisTypeDocumentDefinitionType) result).setVersionable(convertBoolean(docTypeDefintion
          .isVersionable(), false));
      ((CmisTypeDocumentDefinitionType) result).setContentStreamAllowed(convert(
          EnumContentStreamAllowed.class, docTypeDefintion.getContentStreamAllowed()));
    }
    else if (typeDefinition instanceof FolderTypeDefinition) {
      result = new CmisTypeFolderDefinitionType();
    }
    else if (typeDefinition instanceof RelationshipTypeDefinition) {
      result = new CmisTypeRelationshipDefinitionType();

      RelationshipTypeDefinition relationshipTypeDefinition = (RelationshipTypeDefinition) typeDefinition;

      if (relationshipTypeDefinition.getAllowedSourceTypes() != null) {
        for (String type : relationshipTypeDefinition.getAllowedSourceTypes()) {
          ((CmisTypeRelationshipDefinitionType) result).getAllowedSourceTypes().add(type);
        }
      }

      if (relationshipTypeDefinition.getAllowedTargetTypes() != null) {
        for (String type : relationshipTypeDefinition.getAllowedTargetTypes()) {
          ((CmisTypeRelationshipDefinitionType) result).getAllowedTargetTypes().add(type);
        }
      }
    }
    else if (typeDefinition instanceof PolicyTypeDefinition) {
      result = new CmisTypePolicyDefinitionType();
    }
    else {
      return null;
    }

    result.setBaseId(convert(EnumBaseObjectTypeIds.class, typeDefinition.getBaseId()));
    result.setControllableACL(convertBoolean(typeDefinition.isControllableAcl(), false));
    result.setControllablePolicy(convertBoolean(typeDefinition.isControllablePolicy(), false));
    result.setCreatable(convertBoolean(typeDefinition.isCreatable(), false));
    result.setDescription(typeDefinition.getDescription());
    result.setDisplayName(typeDefinition.getDisplayName());
    result.setFileable(convertBoolean(typeDefinition.isFileable(), false));
    result.setFulltextIndexed(convertBoolean(typeDefinition.isFulltextIndexed(), false));
    result.setId(typeDefinition.getId());
    result.setIncludedInSupertypeQuery(convertBoolean(typeDefinition.isIncludedInSupertypeQuery(),
        false));
    result.setLocalName(typeDefinition.getLocalName());
    result.setLocalNamespace(typeDefinition.getLocalNamespace());
    result.setParentId(typeDefinition.getParentId());
    result.setQueryable(convertBoolean(typeDefinition.isQueryable(), false));
    result.setQueryName(typeDefinition.getQueryName());

    if (typeDefinition.getPropertyDefinitions() != null) {
      for (PropertyDefinition<?> propDef : typeDefinition.getPropertyDefinitions().values()) {
        result.getPropertyDefinition().add(convert(propDef));
      }
    }

    // handle extensions
    convertExtension(typeDefinition, result);

    return result;
  }

  /**
   * Converts a property definition object.
   */
  public static CmisPropertyDefinitionType convert(PropertyDefinition<?> propertyDefinition) {
    if (propertyDefinition == null) {
      return null;
    }

    CmisPropertyDefinitionType result = null;

    if (propertyDefinition instanceof PropertyStringDefinition) {
      result = new CmisPropertyStringDefinitionType();

      PropertyStringDefinition source = (PropertyStringDefinition) propertyDefinition;
      CmisPropertyStringDefinitionType target = (CmisPropertyStringDefinitionType) result;

      convertChoiceStringList(source.getChoices(), target.getChoice());

      if (source.getDefaultValue() != null) {
        CmisPropertyString defaultValue = new CmisPropertyString();
        defaultValue.setPropertyDefinitionId(propertyDefinition.getId());
        for (String value : source.getDefaultValue()) {
          defaultValue.getValue().add(value);
        }
        target.setDefaultValue(defaultValue);
      }

      // specific
      target.setMaxLength(source.getMaxLength());
    }
    else if (propertyDefinition instanceof PropertyIdDefinition) {
      result = new CmisPropertyIdDefinitionType();

      PropertyIdDefinition source = (PropertyIdDefinition) propertyDefinition;
      CmisPropertyIdDefinitionType target = (CmisPropertyIdDefinitionType) result;

      convertChoiceIdList(source.getChoices(), target.getChoice());

      if (source.getDefaultValue() != null) {
        CmisPropertyId defaultValue = new CmisPropertyId();
        defaultValue.setPropertyDefinitionId(propertyDefinition.getId());
        for (String value : source.getDefaultValue()) {
          defaultValue.getValue().add(value);
        }
        target.setDefaultValue(defaultValue);
      }
    }
    else if (propertyDefinition instanceof PropertyIntegerDefinition) {
      result = new CmisPropertyIntegerDefinitionType();

      PropertyIntegerDefinition source = (PropertyIntegerDefinition) propertyDefinition;
      CmisPropertyIntegerDefinitionType target = (CmisPropertyIntegerDefinitionType) result;

      convertChoiceIntegerList(source.getChoices(), target.getChoice());

      if (source.getDefaultValue() != null) {
        CmisPropertyInteger defaultValue = new CmisPropertyInteger();
        defaultValue.setPropertyDefinitionId(propertyDefinition.getId());
        for (BigInteger value : source.getDefaultValue()) {
          defaultValue.getValue().add(value);
        }
        target.setDefaultValue(defaultValue);
      }

      // specific
      target.setMinValue(source.getMinValue());
      target.setMaxValue(source.getMaxValue());
    }
    else if (propertyDefinition instanceof PropertyDecimalDefinition) {
      result = new CmisPropertyDecimalDefinitionType();

      PropertyDecimalDefinition source = (PropertyDecimalDefinition) propertyDefinition;
      CmisPropertyDecimalDefinitionType target = (CmisPropertyDecimalDefinitionType) result;

      convertChoiceDecimalList(source.getChoices(), target.getChoice());

      if (source.getDefaultValue() != null) {
        CmisPropertyDecimal defaultValue = new CmisPropertyDecimal();
        defaultValue.setPropertyDefinitionId(propertyDefinition.getId());
        for (BigDecimal value : source.getDefaultValue()) {
          defaultValue.getValue().add(value);
        }
        target.setDefaultValue(defaultValue);
      }

      // specific
      target.setMinValue(source.getMinValue());
      target.setMaxValue(source.getMaxValue());
      if (source.getPrecision() != null) {
        target.setPrecision(source.getPrecision().value());
      }
    }
    else if (propertyDefinition instanceof PropertyBooleanDefinition) {
      result = new CmisPropertyBooleanDefinitionType();

      PropertyBooleanDefinition source = (PropertyBooleanDefinition) propertyDefinition;
      CmisPropertyBooleanDefinitionType target = (CmisPropertyBooleanDefinitionType) result;

      convertChoiceBooleanList(source.getChoices(), target.getChoice());

      if (source.getDefaultValue() != null) {
        CmisPropertyBoolean defaultValue = new CmisPropertyBoolean();
        defaultValue.setPropertyDefinitionId(propertyDefinition.getId());
        for (Boolean value : source.getDefaultValue()) {
          defaultValue.getValue().add(value);
        }
        target.setDefaultValue(defaultValue);
      }
    }
    else if (propertyDefinition instanceof PropertyDateTimeDefinition) {
      result = new CmisPropertyDateTimeDefinitionType();

      PropertyDateTimeDefinition source = (PropertyDateTimeDefinition) propertyDefinition;
      CmisPropertyDateTimeDefinitionType target = (CmisPropertyDateTimeDefinitionType) result;

      convertChoiceDateTimeList(source.getChoices(), target.getChoice());

      if (source.getDefaultValue() != null) {
        CmisPropertyDateTime defaultValue = new CmisPropertyDateTime();
        defaultValue.setPropertyDefinitionId(propertyDefinition.getId());
        for (XMLGregorianCalendar value : convertCalendar(source.getDefaultValue())) {
          defaultValue.getValue().add(value);
        }
        target.setDefaultValue(defaultValue);
      }

      // specific
      target.setResolution(convert(EnumDateTimeResolution.class, source.getDateTimeResolution()));
    }
    else if (propertyDefinition instanceof PropertyHtmlDefinition) {
      result = new CmisPropertyHtmlDefinitionType();

      PropertyHtmlDefinition source = (PropertyHtmlDefinition) propertyDefinition;
      CmisPropertyHtmlDefinitionType target = (CmisPropertyHtmlDefinitionType) result;

      convertChoiceHtmlList(source.getChoices(), target.getChoice());

      if (source.getDefaultValue() != null) {
        CmisPropertyHtml defaultValue = new CmisPropertyHtml();
        defaultValue.setPropertyDefinitionId(propertyDefinition.getId());
        for (String value : source.getDefaultValue()) {
          defaultValue.getValue().add(value);
        }
        target.setDefaultValue(defaultValue);
      }
    }
    else if (propertyDefinition instanceof PropertyUriDefinition) {
      result = new CmisPropertyUriDefinitionType();

      PropertyUriDefinition source = (PropertyUriDefinition) propertyDefinition;
      CmisPropertyUriDefinitionType target = (CmisPropertyUriDefinitionType) result;

      convertChoiceUriList(source.getChoices(), target.getChoice());

      if (source.getDefaultValue() != null) {
        CmisPropertyUri defaultValue = new CmisPropertyUri();
        defaultValue.setPropertyDefinitionId(propertyDefinition.getId());
        for (String value : source.getDefaultValue()) {
          defaultValue.getValue().add(value);
        }
        target.setDefaultValue(defaultValue);
      }
    }
    else {
      return null;
    }

    result.setCardinality(convert(EnumCardinality.class, propertyDefinition.getCardinality()));
    result.setDescription(propertyDefinition.getDescription());
    result.setDisplayName(propertyDefinition.getDisplayName());
    result.setId(propertyDefinition.getId());
    result.setInherited(propertyDefinition.isInherited());
    result.setLocalName(propertyDefinition.getLocalName());
    result.setLocalNamespace(propertyDefinition.getLocalNamespace());
    result.setOpenChoice(propertyDefinition.isOpenChoice());
    result.setOrderable(convertBoolean(propertyDefinition.isOrderable(), false));
    result.setPropertyType(convert(EnumPropertyType.class, propertyDefinition.getPropertyType()));
    result.setQueryable(convertBoolean(propertyDefinition.isQueryable(), false));
    result.setQueryName(propertyDefinition.getQueryName());
    result.setRequired(convertBoolean(propertyDefinition.isRequired(), false));
    result.setUpdatability(convert(EnumUpdatability.class, propertyDefinition.getUpdatability()));

    // handle extensions
    convertExtension(propertyDefinition, result);

    return result;
  }

  // -------------------------------------------------------------------------
  // --- Choices ---
  // -------------------------------------------------------------------------

  /**
   * Converts a choices list.
   */
  private static List<Choice<String>> convertChoiceStringList(List<CmisChoiceString> choices) {
    if (choices == null) {
      return null;
    }

    List<Choice<String>> result = new ArrayList<Choice<String>>();

    for (CmisChoiceString choice : choices) {
      ChoiceImpl<String> newChoice = new ChoiceImpl<String>();

      newChoice.setChoice(convertChoiceStringList(choice.getChoice()));
      newChoice.setDisplayName(choice.getDisplayName());
      newChoice.setValue(choice.getValue());

      result.add(newChoice);
    }

    return result;
  }

  /**
   * Converts a choices list.
   */
  private static void convertChoiceStringList(List<Choice<String>> choices,
      List<CmisChoiceString> target) {
    if (choices == null) {
      return;
    }

    for (Choice<String> choice : choices) {
      CmisChoiceString newChoice = new CmisChoiceString();

      convertChoiceStringList(choice.getChoice(), newChoice.getChoice());
      newChoice.setDisplayName(choice.getDisplayName());

      if (choice.getValue() != null) {
        for (String value : choice.getValue()) {
          newChoice.getValue().add(value);
        }
      }

      target.add(newChoice);
    }
  }

  /**
   * Converts a choices list.
   */
  private static List<Choice<String>> convertChoiceIdList(List<CmisChoiceId> choices) {
    if (choices == null) {
      return null;
    }

    List<Choice<String>> result = new ArrayList<Choice<String>>();

    for (CmisChoiceId choice : choices) {
      ChoiceImpl<String> newChoice = new ChoiceImpl<String>();

      newChoice.setChoice(convertChoiceIdList(choice.getChoice()));
      newChoice.setDisplayName(choice.getDisplayName());
      newChoice.setValue(choice.getValue());

      result.add(newChoice);
    }

    return result;
  }

  /**
   * Converts a choices list.
   */
  private static void convertChoiceIdList(List<Choice<String>> choices, List<CmisChoiceId> target) {
    if (choices == null) {
      return;
    }

    for (Choice<String> choice : choices) {
      CmisChoiceId newChoice = new CmisChoiceId();

      convertChoiceIdList(choice.getChoice(), newChoice.getChoice());
      newChoice.setDisplayName(choice.getDisplayName());

      if (choice.getValue() != null) {
        for (String value : choice.getValue()) {
          newChoice.getValue().add(value);
        }
      }

      target.add(newChoice);
    }
  }

  /**
   * Converts a choices list.
   */
  private static List<Choice<BigInteger>> convertChoiceIntegerList(List<CmisChoiceInteger> choices) {
    if (choices == null) {
      return null;
    }

    List<Choice<BigInteger>> result = new ArrayList<Choice<BigInteger>>();

    for (CmisChoiceInteger choice : choices) {
      ChoiceImpl<BigInteger> newChoice = new ChoiceImpl<BigInteger>();

      newChoice.setChoice(convertChoiceIntegerList(choice.getChoice()));
      newChoice.setDisplayName(choice.getDisplayName());
      newChoice.setValue(choice.getValue());

      result.add(newChoice);
    }

    return result;
  }

  /**
   * Converts a choices list.
   */
  private static void convertChoiceIntegerList(List<Choice<BigInteger>> choices,
      List<CmisChoiceInteger> target) {
    if (choices == null) {
      return;
    }

    for (Choice<BigInteger> choice : choices) {
      CmisChoiceInteger newChoice = new CmisChoiceInteger();

      convertChoiceIntegerList(choice.getChoice(), newChoice.getChoice());
      newChoice.setDisplayName(choice.getDisplayName());

      if (choice.getValue() != null) {
        for (BigInteger value : choice.getValue()) {
          newChoice.getValue().add(value);
        }
      }

      target.add(newChoice);
    }
  }

  /**
   * Converts a choices list.
   */
  private static List<Choice<BigDecimal>> convertChoiceDecimalList(List<CmisChoiceDecimal> choices) {
    if (choices == null) {
      return null;
    }

    List<Choice<BigDecimal>> result = new ArrayList<Choice<BigDecimal>>();

    for (CmisChoiceDecimal choice : choices) {
      ChoiceImpl<BigDecimal> newChoice = new ChoiceImpl<BigDecimal>();

      newChoice.setChoice(convertChoiceDecimalList(choice.getChoice()));
      newChoice.setDisplayName(choice.getDisplayName());
      newChoice.setValue(choice.getValue());

      result.add(newChoice);
    }

    return result;
  }

  /**
   * Converts a choices list.
   */
  private static void convertChoiceDecimalList(List<Choice<BigDecimal>> choices,
      List<CmisChoiceDecimal> target) {
    if (choices == null) {
      return;
    }

    for (Choice<BigDecimal> choice : choices) {
      CmisChoiceDecimal newChoice = new CmisChoiceDecimal();

      convertChoiceDecimalList(choice.getChoice(), newChoice.getChoice());
      newChoice.setDisplayName(choice.getDisplayName());

      if (choice.getValue() != null) {
        for (BigDecimal value : choice.getValue()) {
          newChoice.getValue().add(value);
        }
      }

      target.add(newChoice);
    }
  }

  /**
   * Converts a choices list.
   */
  private static List<Choice<Boolean>> convertChoiceBooleanList(List<CmisChoiceBoolean> choices) {
    if (choices == null) {
      return null;
    }

    List<Choice<Boolean>> result = new ArrayList<Choice<Boolean>>();

    for (CmisChoiceBoolean choice : choices) {
      ChoiceImpl<Boolean> newChoice = new ChoiceImpl<Boolean>();

      newChoice.setChoice(convertChoiceBooleanList(choice.getChoice()));
      newChoice.setDisplayName(choice.getDisplayName());
      newChoice.setValue(choice.getValue());

      result.add(newChoice);
    }

    return result;
  }

  /**
   * Converts a choices list.
   */
  private static void convertChoiceBooleanList(List<Choice<Boolean>> choices,
      List<CmisChoiceBoolean> target) {
    if (choices == null) {
      return;
    }

    for (Choice<Boolean> choice : choices) {
      CmisChoiceBoolean newChoice = new CmisChoiceBoolean();

      convertChoiceBooleanList(choice.getChoice(), newChoice.getChoice());
      newChoice.setDisplayName(choice.getDisplayName());

      if (choice.getValue() != null) {
        for (Boolean value : choice.getValue()) {
          newChoice.getValue().add(value);
        }
      }

      target.add(newChoice);
    }
  }

  /**
   * Converts a choices list.
   */
  private static List<Choice<GregorianCalendar>> convertChoiceDateTimeList(
      List<CmisChoiceDateTime> choices) {
    if (choices == null) {
      return null;
    }

    List<Choice<GregorianCalendar>> result = new ArrayList<Choice<GregorianCalendar>>();

    for (CmisChoiceDateTime choice : choices) {
      ChoiceImpl<GregorianCalendar> newChoice = new ChoiceImpl<GregorianCalendar>();

      newChoice.setChoice(convertChoiceDateTimeList(choice.getChoice()));
      newChoice.setDisplayName(choice.getDisplayName());
      newChoice.setValue(convertXMLCalendar(choice.getValue()));

      result.add(newChoice);
    }

    return result;
  }

  /**
   * Converts a choices list.
   */
  private static void convertChoiceDateTimeList(List<Choice<GregorianCalendar>> choices,
      List<CmisChoiceDateTime> target) {
    if (choices == null) {
      return;
    }

    for (Choice<GregorianCalendar> choice : choices) {
      CmisChoiceDateTime newChoice = new CmisChoiceDateTime();

      convertChoiceDateTimeList(choice.getChoice(), newChoice.getChoice());
      newChoice.setDisplayName(choice.getDisplayName());

      if (choice.getValue() != null) {
        for (XMLGregorianCalendar value : convertCalendar(choice.getValue())) {
          newChoice.getValue().add(value);
        }
      }

      target.add(newChoice);
    }
  }

  /**
   * Converts a choices list.
   */
  private static List<Choice<String>> convertChoiceHtmlList(List<CmisChoiceHtml> choices) {
    if (choices == null) {
      return null;
    }

    List<Choice<String>> result = new ArrayList<Choice<String>>();

    for (CmisChoiceHtml choice : choices) {
      ChoiceImpl<String> newChoice = new ChoiceImpl<String>();

      newChoice.setChoice(convertChoiceHtmlList(choice.getChoice()));
      newChoice.setDisplayName(choice.getDisplayName());
      newChoice.setValue(choice.getValue());

      result.add(newChoice);
    }

    return result;
  }

  /**
   * Converts a choices list.
   */
  private static void convertChoiceHtmlList(List<Choice<String>> choices,
      List<CmisChoiceHtml> target) {
    if (choices == null) {
      return;
    }

    for (Choice<String> choice : choices) {
      CmisChoiceHtml newChoice = new CmisChoiceHtml();

      convertChoiceHtmlList(choice.getChoice(), newChoice.getChoice());
      newChoice.setDisplayName(choice.getDisplayName());

      if (choice.getValue() != null) {
        for (String value : choice.getValue()) {
          newChoice.getValue().add(value);
        }
      }

      target.add(newChoice);
    }
  }

  /**
   * Converts a choices list.
   */
  private static List<Choice<String>> convertChoiceUriList(List<CmisChoiceUri> choices) {
    if (choices == null) {
      return null;
    }

    List<Choice<String>> result = new ArrayList<Choice<String>>();

    for (CmisChoiceUri choice : choices) {
      ChoiceImpl<String> newChoice = new ChoiceImpl<String>();

      newChoice.setChoice(convertChoiceUriList(choice.getChoice()));
      newChoice.setDisplayName(choice.getDisplayName());
      newChoice.setValue(choice.getValue());

      result.add(newChoice);
    }

    return result;
  }

  /**
   * Converts a choices list.
   */
  private static void convertChoiceUriList(List<Choice<String>> choices, List<CmisChoiceUri> target) {
    if (choices == null) {
      return;
    }

    for (Choice<String> choice : choices) {
      CmisChoiceUri newChoice = new CmisChoiceUri();

      convertChoiceUriList(choice.getChoice(), newChoice.getChoice());
      newChoice.setDisplayName(choice.getDisplayName());

      if (choice.getValue() != null) {
        for (String value : choice.getValue()) {
          newChoice.getValue().add(value);
        }
      }

      target.add(newChoice);
    }
  }

  // -------------------------------------------------------------------------
  // --- Objects ---
  // -------------------------------------------------------------------------

  /**
   * Converts a CMIS object.
   */
  public static ObjectData convert(CmisObjectType object) {
    if (object == null) {
      return null;
    }

    ObjectDataImpl result = new ObjectDataImpl();

    result.setAcl(convert(object.getAcl(), object.isExactACL()));
    result.setAllowableActions(convert(object.getAllowableActions()));
    if (object.getChangeEventInfo() != null) {
      ChangeEventInfoDataImpl changeEventInfo = new ChangeEventInfoDataImpl();
      if (object.getChangeEventInfo().getChangeTime() != null) {
        changeEventInfo.setChangeTime(object.getChangeEventInfo().getChangeTime()
            .toGregorianCalendar());
      }
      changeEventInfo.setTypeOfChanges(convert(TypeOfChanges.class, object.getChangeEventInfo()
          .getChangeType()));
      convertExtension(object.getChangeEventInfo(), changeEventInfo);

      result.setChangeEventInfo(changeEventInfo);
    }
    result.setIsExactAcl(object.isExactACL());
    result.setPolicyIds(convert(object.getPolicyIds()));
    result.setProperties(convert(object.getProperties()));
    List<ObjectData> relationships = new ArrayList<ObjectData>();
    for (CmisObjectType cmisObject : object.getRelationship()) {
      relationships.add(convert(cmisObject));
    }
    result.setRelationships(relationships);
    List<RenditionData> renditions = new ArrayList<RenditionData>();
    for (CmisRenditionType rendition : object.getRendition()) {
      renditions.add(convert(rendition));
    }
    result.setRenditions(renditions);

    // handle extensions
    convertExtension(object, result);

    return result;
  }

  /**
   * Converts a properties object.
   */
  public static PropertiesData convert(CmisPropertiesType properties) {
    if (properties == null) {
      return null;
    }

    PropertiesDataImpl result = new PropertiesDataImpl();

    for (CmisProperty property : properties.getProperty()) {
      result.addProperty(convert(property));
    }

    // handle extensions
    convertExtension(properties, result);

    return result;
  }

  /**
   * Converts a property object.
   */
  public static PropertyData<?> convert(CmisProperty property) {
    if (property == null) {
      return null;
    }

    PropertyData<?> result = null;

    if (property instanceof CmisPropertyString) {
      result = new PropertyStringDataImpl(property.getPropertyDefinitionId(),
          ((CmisPropertyString) property).getValue());
    }
    else if (property instanceof CmisPropertyId) {
      result = new PropertyIdDataImpl(property.getPropertyDefinitionId(),
          ((CmisPropertyId) property).getValue());
    }
    else if (property instanceof CmisPropertyInteger) {
      result = new PropertyIntegerDataImpl(property.getPropertyDefinitionId(),
          ((CmisPropertyInteger) property).getValue());
    }
    else if (property instanceof CmisPropertyDecimal) {
      result = new PropertyDecimalDataImpl(property.getPropertyDefinitionId(),
          ((CmisPropertyDecimal) property).getValue());
    }
    else if (property instanceof CmisPropertyBoolean) {
      result = new PropertyBooleanDataImpl(property.getPropertyDefinitionId(),
          ((CmisPropertyBoolean) property).getValue());
    }
    else if (property instanceof CmisPropertyDateTime) {
      result = new PropertyDateTimeDataImpl(property.getPropertyDefinitionId(),
          convertXMLCalendar(((CmisPropertyDateTime) property).getValue()));
    }
    else if (property instanceof CmisPropertyHtml) {
      result = new PropertyHtmlDataImpl(property.getPropertyDefinitionId(),
          ((CmisPropertyHtml) property).getValue());
    }
    else if (property instanceof CmisPropertyUri) {
      result = new PropertyUriDataImpl(property.getPropertyDefinitionId(),
          ((CmisPropertyUri) property).getValue());
    }
    else {
      return null;
    }

    ((AbstractPropertyData<?>) result).setLocalName(property.getLocalName());
    ((AbstractPropertyData<?>) result).setQueryName(property.getQueryName());
    ((AbstractPropertyData<?>) result).setDisplayName(property.getDisplayName());

    // handle extensions
    convertExtension(property, result);

    return result;
  }

  /**
   * Converts a properties object.
   */
  public static CmisPropertiesType convert(PropertiesData properties) {
    if (properties == null) {
      return null;
    }

    CmisPropertiesType result = new CmisPropertiesType();

    if (properties.getProperties() != null) {
      for (PropertyData<?> property : properties.getProperties().values()) {
        result.getProperty().add(convert(property));
      }
    }

    // handle extensions
    convertExtension(properties, result);

    return result;
  }

  /**
   * Converts a property object.
   */
  public static CmisProperty convert(PropertyData<?> property) {
    if (property == null) {
      return null;
    }

    CmisProperty result = null;

    if (property instanceof PropertyStringData) {
      result = new CmisPropertyString();
      ((CmisPropertyString) result).getValue().addAll(((PropertyStringData) property).getValues());
    }
    else if (property instanceof PropertyIdData) {
      result = new CmisPropertyId();
      ((CmisPropertyId) result).getValue().addAll(((PropertyIdData) property).getValues());
    }
    else if (property instanceof PropertyIntegerData) {
      result = new CmisPropertyInteger();
      ((CmisPropertyInteger) result).getValue()
          .addAll(((PropertyIntegerData) property).getValues());
    }
    else if (property instanceof PropertyDecimalData) {
      result = new CmisPropertyDecimal();
      ((CmisPropertyDecimal) result).getValue()
          .addAll(((PropertyDecimalData) property).getValues());
    }
    else if (property instanceof PropertyBooleanData) {
      result = new CmisPropertyBoolean();
      ((CmisPropertyBoolean) result).getValue()
          .addAll(((PropertyBooleanData) property).getValues());
    }
    else if (property instanceof PropertyDateTimeData) {
      result = new CmisPropertyDateTime();
      ((CmisPropertyDateTime) result).getValue().addAll(
          convertCalendar(((PropertyDateTimeData) property).getValues()));
    }
    else if (property instanceof PropertyHtmlData) {
      result = new CmisPropertyHtml();
      ((CmisPropertyHtml) result).getValue().addAll(((PropertyHtmlData) property).getValues());
    }
    else if (property instanceof PropertyUriData) {
      result = new CmisPropertyUri();
      ((CmisPropertyUri) result).getValue().addAll(((PropertyUriData) property).getValues());
    }
    else {
      return null;
    }

    result.setPropertyDefinitionId(property.getId());
    result.setLocalName(property.getLocalName());
    result.setQueryName(property.getQueryName());
    result.setDisplayName(property.getDisplayName());

    return result;
  }

  /**
   * Converts a rendition object.
   */
  public static RenditionData convert(CmisRenditionType rendition) {
    if (rendition == null) {
      return null;
    }

    RenditionDataImpl result = new RenditionDataImpl();

    result.setHeight(rendition.getHeight());
    result.setKind(rendition.getKind());
    result.setLength(rendition.getLength());
    result.setMimeType(rendition.getMimetype());
    result.setRenditionDocumentId(rendition.getRenditionDocumentId());
    result.setStreamId(rendition.getStreamId());
    result.setTitle(rendition.getTitle());
    result.setWidth(rendition.getWidth());

    // handle extensions
    convertExtension(rendition, result);

    return result;
  }

  /**
   * Converts a rendition object.
   */
  public static CmisRenditionType convert(RenditionData rendition) {
    if (rendition == null) {
      return null;
    }

    CmisRenditionType result = new CmisRenditionType();

    result.setHeight(rendition.getHeight());
    result.setKind(rendition.getKind());
    result.setLength(rendition.getLength());
    result.setMimetype(rendition.getMimeType());
    result.setRenditionDocumentId(rendition.getRenditionDocumentId());
    result.setStreamId(rendition.getStreamId());
    result.setTitle(rendition.getTitle());
    result.setWidth(rendition.getWidth());

    // handle extensions
    convertExtension(rendition, result);

    return result;
  }

  /**
   * Converts a CMIS object.
   */
  public static CmisObjectType convert(ObjectData object) {
    if (object == null) {
      return null;
    }

    CmisObjectType result = new CmisObjectType();

    result.setAcl(convert(object.getAcl()));
    result.setAllowableActions(convert(object.getAllowableActions()));
    if (object.getChangeEventInfo() != null) {
      CmisChangeEventType changeEventInfo = new CmisChangeEventType();

      changeEventInfo.setChangeType(convert(EnumTypeOfChanges.class, object.getChangeEventInfo()
          .getChangeType()));
      changeEventInfo.setChangeTime(convertCalendar(object.getChangeEventInfo().getChangeTime()));

      convertExtension(object.getChangeEventInfo(), changeEventInfo);

      result.setChangeEventInfo(changeEventInfo);
    }
    result.setExactACL(object.getAcl() == null ? null : object.getAcl().isExact());
    result.setPolicyIds(convert(object.getPolicyIds()));
    result.setProperties(convert(object.getProperties()));
    if (object.getRelationships() != null) {
      for (ObjectData relationship : object.getRelationships()) {
        result.getRelationship().add(convert(relationship));
      }
    }
    if (object.getRenditions() != null) {
      for (RenditionData rendition : object.getRenditions()) {
        result.getRendition().add(convert(rendition));
      }
    }

    // handle extensions
    convertExtension(object, result);

    return result;
  }

  // -------------------------------------------------------------------------
  // --- ACLs and Policies ---
  // -------------------------------------------------------------------------

  /**
   * Converts an ACL object with its ACEs.
   */
  public static AccessControlList convert(CmisAccessControlListType acl, Boolean isExact) {
    if (acl == null) {
      return null;
    }

    AccessControlListImpl result = new AccessControlListImpl();

    List<AccessControlEntry> aces = new ArrayList<AccessControlEntry>();
    for (CmisAccessControlEntryType entry : acl.getPermission()) {
      if (entry == null) {
        continue;
      }

      AccessControlEntryImpl ace = new AccessControlEntryImpl();
      ace.setDirect(entry.isDirect());
      ace.setPermissions(entry.getPermission());
      AccessControlPrincipalDataImpl principal = new AccessControlPrincipalDataImpl(entry
          .getPrincipal() == null ? null : entry.getPrincipal().getPrincipalId());
      convertExtension(entry.getPrincipal(), principal);
      ace.setPrincipal(principal);

      // handle extensions
      convertExtension(entry, ace);

      aces.add(ace);
    }

    result.setAces(aces);

    result.setExact(isExact);

    // handle extensions
    convertExtension(acl, result);

    return result;
  }

  /**
   * Converts an ACL object with its ACEs.
   */
  public static CmisAccessControlListType convert(AccessControlList acl) {
    if (acl == null) {
      return null;
    }

    CmisAccessControlListType result = new CmisAccessControlListType();

    if (acl.getAces() != null) {
      for (AccessControlEntry ace : acl.getAces()) {
        if (ace == null) {
          continue;
        }

        CmisAccessControlEntryType entry = new CmisAccessControlEntryType();

        if (ace.getPrincipal() != null) {
          CmisAccessControlPrincipalType pincipal = new CmisAccessControlPrincipalType();

          pincipal.setPrincipalId(ace.getPrincipal().getPrincipalId());
          convertExtension(pincipal, ace.getPrincipal());

          entry.setPrincipal(pincipal);
        }

        entry.setDirect(ace.isDirect());
        entry.getPermission().addAll(ace.getPermissions());

        convertExtension(ace, entry);

        result.getPermission().add(entry);
      }
    }

    // handle extensions
    convertExtension(acl, result);

    return result;
  }

  /**
   * Converts an AllowableActions object.
   */
  public static AllowableActionsData convert(CmisAllowableActionsType allowableActions) {
    if (allowableActions == null) {
      return null;
    }

    AllowableActionsDataImpl result = new AllowableActionsDataImpl();

    Map<String, Boolean> actionsMap = new HashMap<String, Boolean>();
    actionsMap.put(AllowableActionsData.ACTION_CAN_ADD_OBJECT_TO_FOLDER, allowableActions
        .isCanAddObjectToFolder());
    actionsMap.put(AllowableActionsData.ACTION_CAN_APPLY_ACL, allowableActions.isCanApplyACL());
    actionsMap.put(AllowableActionsData.ACTION_CAN_APPLY_POLICY, allowableActions
        .isCanApplyPolicy());
    actionsMap.put(AllowableActionsData.ACTION_CAN_CANCEL_CHECK_OUT, allowableActions
        .isCanCancelCheckOut());
    actionsMap.put(AllowableActionsData.ACTION_CAN_CHECK_IN, allowableActions.isCanCheckIn());
    actionsMap.put(AllowableActionsData.ACTION_CAN_CHECK_OUT, allowableActions.isCanCheckOut());
    actionsMap.put(AllowableActionsData.ACTION_CAN_CREATE_DOCUMENT, allowableActions
        .isCanCreateDocument());
    actionsMap.put(AllowableActionsData.ACTION_CAN_CREATE_FOLDER, allowableActions
        .isCanCreateFolder());
    actionsMap.put(AllowableActionsData.ACTION_CAN_CREATE_POLICY, allowableActions
        .isCanCreatePolicy());
    actionsMap.put(AllowableActionsData.ACTION_CAN_CREATE_RELATIONSHIP, allowableActions
        .isCanCreateRelationship());
    actionsMap.put(AllowableActionsData.ACTION_CAN_DELETE_CONTENT_STREAM, allowableActions
        .isCanDeleteContentStream());
    actionsMap.put(AllowableActionsData.ACTION_CAN_DELETE_OBJECT, allowableActions
        .isCanDeleteObject());
    actionsMap.put(AllowableActionsData.ACTION_CAN_DELETE_TREE, allowableActions.isCanDeleteTree());
    actionsMap.put(AllowableActionsData.ACTION_CAN_GET_ACL, allowableActions.isCanGetACL());
    actionsMap.put(AllowableActionsData.ACTION_CAN_GET_ALL_VERSIONS, allowableActions
        .isCanGetAllVersions());
    actionsMap.put(AllowableActionsData.ACTION_CAN_GET_APPLIED_POLICIES, allowableActions
        .isCanGetAppliedPolicies());
    actionsMap.put(AllowableActionsData.ACTION_CAN_GET_CHILDREN, allowableActions
        .isCanGetChildren());
    actionsMap.put(AllowableActionsData.ACTION_CAN_GET_CONTENT_STREAM, allowableActions
        .isCanGetContentStream());
    actionsMap.put(AllowableActionsData.ACTION_CAN_GET_DESCENDANTS, allowableActions
        .isCanGetDescendants());
    actionsMap.put(AllowableActionsData.ACTION_CAN_GET_FOLDER_PARENT, allowableActions
        .isCanGetFolderParent());
    actionsMap.put(AllowableActionsData.ACTION_CAN_GET_FOLDER_TREE, allowableActions
        .isCanGetFolderTree());
    actionsMap.put(AllowableActionsData.ACTION_CAN_GET_OBJECT_PARENTS, allowableActions
        .isCanGetObjectParents());
    actionsMap.put(AllowableActionsData.ACTION_CAN_GET_OBJECT_RELATIONSHIPS, allowableActions
        .isCanGetObjectRelationships());
    actionsMap.put(AllowableActionsData.ACTION_CAN_GET_PROPERTIES, allowableActions
        .isCanGetProperties());
    actionsMap.put(AllowableActionsData.ACTION_CAN_GET_RENDITIONS, allowableActions
        .isCanGetRenditions());
    actionsMap.put(AllowableActionsData.ACTION_CAN_MOVE_OBJECT, allowableActions.isCanMoveObject());
    actionsMap.put(AllowableActionsData.ACTION_CAN_REMOVE_OBJECT_FROM_FOLDER, allowableActions
        .isCanRemoveObjectFromFolder());
    actionsMap.put(AllowableActionsData.ACTION_CAN_REMOVE_POLICY, allowableActions
        .isCanRemovePolicy());
    actionsMap.put(AllowableActionsData.ACTION_CAN_SET_CONTENT_STREAM, allowableActions
        .isCanSetContentStream());
    actionsMap.put(AllowableActionsData.ACTION_CAN_UPDATE_PROPERTIES, allowableActions
        .isCanUpdateProperties());

    result.setAllowableActions(actionsMap);

    // handle extensions
    convertExtension(allowableActions, result);

    return result;
  }

  /**
   * Converts an AllowableActions object.
   */
  public static CmisAllowableActionsType convert(AllowableActionsData allowableActions) {
    if (allowableActions == null) {
      return null;
    }

    CmisAllowableActionsType result = new CmisAllowableActionsType();

    if (allowableActions.getAllowableActions() != null) {
      Map<String, Boolean> actionsMap = allowableActions.getAllowableActions();

      result.setCanAddObjectToFolder(actionsMap
          .get(AllowableActionsData.ACTION_CAN_ADD_OBJECT_TO_FOLDER));
      result.setCanApplyACL(actionsMap.get(AllowableActionsData.ACTION_CAN_APPLY_ACL));
      result.setCanApplyPolicy(actionsMap.get(AllowableActionsData.ACTION_CAN_APPLY_POLICY));
      result.setCanCancelCheckOut(actionsMap.get(AllowableActionsData.ACTION_CAN_CANCEL_CHECK_OUT));
      result.setCanCheckIn(actionsMap.get(AllowableActionsData.ACTION_CAN_CHECK_IN));
      result.setCanCheckOut(actionsMap.get(AllowableActionsData.ACTION_CAN_CHECK_OUT));
      result.setCanCreateDocument(actionsMap.get(AllowableActionsData.ACTION_CAN_CREATE_DOCUMENT));
      result.setCanCreateFolder(actionsMap.get(AllowableActionsData.ACTION_CAN_CREATE_FOLDER));
      result.setCanCreatePolicy(actionsMap.get(AllowableActionsData.ACTION_CAN_CREATE_POLICY));
      result.setCanCreateRelationship(actionsMap
          .get(AllowableActionsData.ACTION_CAN_CREATE_RELATIONSHIP));
      result.setCanDeleteContentStream(actionsMap
          .get(AllowableActionsData.ACTION_CAN_DELETE_CONTENT_STREAM));
      result.setCanDeleteObject(actionsMap.get(AllowableActionsData.ACTION_CAN_DELETE_OBJECT));
      result.setCanDeleteTree(actionsMap.get(AllowableActionsData.ACTION_CAN_DELETE_TREE));
      result.setCanGetACL(actionsMap.get(AllowableActionsData.ACTION_CAN_GET_ACL));
      result.setCanGetAllVersions(actionsMap.get(AllowableActionsData.ACTION_CAN_GET_ALL_VERSIONS));
      result.setCanGetAppliedPolicies(actionsMap
          .get(AllowableActionsData.ACTION_CAN_GET_APPLIED_POLICIES));
      result.setCanGetChildren(actionsMap.get(AllowableActionsData.ACTION_CAN_GET_CHILDREN));
      result.setCanGetContentStream(actionsMap
          .get(AllowableActionsData.ACTION_CAN_GET_CONTENT_STREAM));
      result.setCanGetDescendants(actionsMap.get(AllowableActionsData.ACTION_CAN_GET_DESCENDANTS));
      result.setCanGetFolderParent(actionsMap
          .get(AllowableActionsData.ACTION_CAN_GET_FOLDER_PARENT));
      result.setCanGetFolderTree(actionsMap.get(AllowableActionsData.ACTION_CAN_GET_FOLDER_TREE));
      result.setCanGetObjectParents(actionsMap
          .get(AllowableActionsData.ACTION_CAN_GET_OBJECT_PARENTS));
      result.setCanGetObjectRelationships(actionsMap
          .get(AllowableActionsData.ACTION_CAN_GET_OBJECT_RELATIONSHIPS));
      result.setCanGetProperties(actionsMap.get(AllowableActionsData.ACTION_CAN_GET_PROPERTIES));
      result.setCanGetRenditions(actionsMap.get(AllowableActionsData.ACTION_CAN_GET_RENDITIONS));
      result.setCanMoveObject(actionsMap.get(AllowableActionsData.ACTION_CAN_MOVE_OBJECT));
      result.setCanRemoveObjectFromFolder(actionsMap
          .get(AllowableActionsData.ACTION_CAN_REMOVE_OBJECT_FROM_FOLDER));
      result.setCanRemovePolicy(actionsMap.get(AllowableActionsData.ACTION_CAN_REMOVE_POLICY));
      result.setCanSetContentStream(actionsMap
          .get(AllowableActionsData.ACTION_CAN_SET_CONTENT_STREAM));
      result.setCanUpdateProperties(actionsMap
          .get(AllowableActionsData.ACTION_CAN_UPDATE_PROPERTIES));
    }

    // handle extensions
    convertExtension(allowableActions, result);

    return result;
  }

  /**
   * Converts a list of policy ids.
   */
  public static PolicyIdListData convert(CmisListOfIdsType policyIds) {
    if (policyIds == null) {
      return null;
    }

    PolicyIdListDataImpl result = new PolicyIdListDataImpl();
    result.setPolicyIds(policyIds.getId());

    // handle extensions
    convertExtension(policyIds, result);

    return result;
  }

  /**
   * Converts a list of policy ids.
   */
  public static CmisListOfIdsType convert(PolicyIdListData policyIds) {
    if (policyIds == null) {
      return null;
    }

    CmisListOfIdsType result = new CmisListOfIdsType();
    if (policyIds.getPolicyIds() != null) {
      for (String id : policyIds.getPolicyIds()) {
        result.getId().add(id);
      }
    }

    // handle extensions
    convertExtension(policyIds, result);

    return result;
  }

  /**
   * Converts a list of policy ids.
   */
  public static CmisListOfIdsType convertPolicyIds(List<String> policyIds) {
    if (policyIds == null) {
      return null;
    }

    CmisListOfIdsType result = new CmisListOfIdsType();
    result.getId().addAll(policyIds);

    return result;
  }

  // -------------------------------------------------------------------------
  // --- Lists, containers and similar ---
  // -------------------------------------------------------------------------

  /**
   * Converts a list of calendar objects.
   */
  public static List<GregorianCalendar> convertXMLCalendar(List<XMLGregorianCalendar> calendar) {
    if (calendar == null) {
      return null;
    }

    List<GregorianCalendar> result = new ArrayList<GregorianCalendar>();
    for (XMLGregorianCalendar cal : calendar) {
      result.add(cal.toGregorianCalendar());
    }

    return result;
  }

  /**
   * Converts a list of calendar objects.
   */
  public static List<XMLGregorianCalendar> convertCalendar(List<GregorianCalendar> calendar) {
    if (calendar == null) {
      return null;
    }

    DatatypeFactory df;
    try {
      df = DatatypeFactory.newInstance();
    }
    catch (DatatypeConfigurationException e) {
      throw new CmisRuntimeException("Convert exception: " + e.getMessage(), e);
    }

    List<XMLGregorianCalendar> result = new ArrayList<XMLGregorianCalendar>();
    for (GregorianCalendar cal : calendar) {
      result.add(df.newXMLGregorianCalendar(cal));
    }

    return result;
  }

  /**
   * Converts a calendar object.
   */
  public static XMLGregorianCalendar convertCalendar(GregorianCalendar calendar) {
    if (calendar == null) {
      return null;
    }

    DatatypeFactory df;
    try {
      df = DatatypeFactory.newInstance();
    }
    catch (DatatypeConfigurationException e) {
      throw new CmisRuntimeException("Convert exception: " + e.getMessage(), e);
    }

    return df.newXMLGregorianCalendar(calendar);
  }

  /**
   * Converts a type list.
   */
  public static TypeDefinitionList convert(CmisTypeDefinitionListType typeList) {
    if (typeList == null) {
      return null;
    }

    TypeDefinitionListImpl result = new TypeDefinitionListImpl();
    List<TypeDefinition> types = new ArrayList<TypeDefinition>();
    for (CmisTypeDefinitionType typeDefinition : typeList.getTypes()) {
      types.add(convert(typeDefinition));
    }

    result.setList(types);
    result.setHasMoreItems(typeList.isHasMoreItems());
    result.setNumItems(typeList.getNumItems());

    // handle extensions
    convertExtension(typeList, result);

    return result;
  }

  /**
   * Converts a type list.
   */
  public static CmisTypeDefinitionListType convert(TypeDefinitionList typeList) {
    if (typeList == null) {
      return null;
    }

    CmisTypeDefinitionListType result = new CmisTypeDefinitionListType();

    if (typeList.getList() != null) {
      for (TypeDefinition tdd : typeList.getList()) {
        result.getTypes().add(convert(tdd));
      }
    }

    result.setHasMoreItems(convertBoolean(typeList.hasMoreItems(), false));
    result.setNumItems(typeList.getNumItems());

    // handle extensions
    convertExtension(typeList, result);

    return result;
  }

  /**
   * Converts a type container list.
   */
  public static List<TypeDefinitionContainer> convertTypeContainerList(
      List<CmisTypeContainer> typeContainers) {
    if (typeContainers == null) {
      return null;
    }

    List<TypeDefinitionContainer> result = new ArrayList<TypeDefinitionContainer>();
    for (CmisTypeContainer container : typeContainers) {
      TypeDefinitionContainerImpl newConatiner = new TypeDefinitionContainerImpl();
      newConatiner.setTypeDefinition(convert(container.getType()));
      newConatiner.setChildren(convertTypeContainerList(container.getChildren()));
      convertExtension(container, newConatiner);

      result.add(newConatiner);
    }

    return result;
  }

  /**
   * Converts a type container list.
   */
  public static void convertTypeContainerList(List<TypeDefinitionContainer> typeContainers,
      List<CmisTypeContainer> target) {
    if (typeContainers == null) {
      return;
    }

    for (TypeDefinitionContainer container : typeContainers) {
      CmisTypeContainer newConatiner = new CmisTypeContainer();
      newConatiner.setType(convert(container.getTypeDefinition()));
      convertTypeContainerList(container.getChildren(), newConatiner.getChildren());
      convertExtension(container, newConatiner);

      target.add(newConatiner);
    }
  }

  /**
   * Converts an ObjectInFolder object.
   */
  public static ObjectInFolderData convert(CmisObjectInFolderType objectInFolder) {
    if (objectInFolder == null) {
      return null;
    }

    ObjectInFolderDataImpl result = new ObjectInFolderDataImpl();

    result.setObject(convert(objectInFolder.getObject()));
    result.setPathSegment(objectInFolder.getPathSegment());

    // handle extensions
    convertExtension(objectInFolder, result);

    return result;
  }

  /**
   * Converts an ObjectInFolder object.
   */
  public static CmisObjectInFolderType convert(ObjectInFolderData objectInFolder) {
    if (objectInFolder == null) {
      return null;
    }

    CmisObjectInFolderType result = new CmisObjectInFolderType();

    result.setObject(convert(objectInFolder.getObject()));
    result.setPathSegment(objectInFolder.getPathSegment());

    // handle extensions
    convertExtension(objectInFolder, result);

    return result;
  }

  /**
   * Converts an ObjectParent object.
   */
  public static ObjectParentData convert(CmisObjectParentsType objectParent) {
    if (objectParent == null) {
      return null;
    }

    ObjectParentDataImpl result = new ObjectParentDataImpl();

    result.setObject(convert(objectParent.getObject()));
    result.setRelativePathSegment(objectParent.getRelativePathSegment());

    // handle extensions
    convertExtension(objectParent, result);

    return result;
  }

  /**
   * Converts an ObjectParent object.
   */
  public static CmisObjectParentsType convert(ObjectParentData objectParent) {
    if (objectParent == null) {
      return null;
    }

    CmisObjectParentsType result = new CmisObjectParentsType();

    result.setObject(convert(objectParent.getObject()));
    result.setRelativePathSegment(objectParent.getRelativePathSegment());

    // handle extensions
    convertExtension(objectParent, result);

    return result;
  }

  /**
   * Converts an ObjectInFolder list object.
   */
  public static ObjectInFolderList convert(CmisObjectInFolderListType objectInFolderList) {
    if (objectInFolderList == null) {
      return null;
    }

    ObjectInFolderListImpl result = new ObjectInFolderListImpl();
    List<ObjectInFolderData> objects = new ArrayList<ObjectInFolderData>();
    for (CmisObjectInFolderType object : objectInFolderList.getObjects()) {
      objects.add(convert(object));
    }

    result.setObjects(objects);

    result.setHasMoreItems(objectInFolderList.isHasMoreItems());
    result.setNumItems(objectInFolderList.getNumItems());

    // handle extensions
    convertExtension(objectInFolderList, result);

    return result;
  }

  /**
   * Converts an ObjectInFolder list object.
   */
  public static CmisObjectInFolderListType convert(ObjectInFolderList objectInFolderList) {
    if (objectInFolderList == null) {
      return null;
    }

    CmisObjectInFolderListType result = new CmisObjectInFolderListType();

    if (objectInFolderList.getObjects() != null) {
      for (ObjectInFolderData object : objectInFolderList.getObjects()) {
        result.getObjects().add(convert(object));
      }
    }

    result.setHasMoreItems(objectInFolderList.hasMoreItems());
    result.setNumItems(objectInFolderList.getNumItems());

    // handle extensions
    convertExtension(objectInFolderList, result);

    return result;
  }

  /**
   * Converts an Object list object.
   */
  public static ObjectList convert(CmisObjectListType objectList) {
    if (objectList == null) {
      return null;
    }

    ObjectListImpl result = new ObjectListImpl();

    List<ObjectData> objects = new ArrayList<ObjectData>();
    for (CmisObjectType object : objectList.getObjects()) {
      objects.add(convert(object));
    }

    result.setObjects(objects);
    result.setHasMoreItems(objectList.isHasMoreItems());
    result.setNumItems(objectList.getNumItems());

    // handle extensions
    convertExtension(objectList, result);

    return result;
  }

  /**
   * Converts an Object list object.
   */
  public static CmisObjectListType convert(ObjectList objectList) {
    if (objectList == null) {
      return null;
    }

    CmisObjectListType result = new CmisObjectListType();

    if (objectList.getObjects() != null) {
      for (ObjectData object : objectList.getObjects()) {
        result.getObjects().add(convert(object));
      }
    }

    result.setHasMoreItems(objectList.hasMoreItems());
    result.setNumItems(objectList.getNumItems());

    // handle extensions
    convertExtension(objectList, result);

    return result;
  }

  /**
   * Converts an ObjectInFolder container object.
   */
  public static ObjectInFolderContainer convert(CmisObjectInFolderContainerType container) {
    if (container == null) {
      return null;
    }

    ObjectInFolderContainerImpl result = new ObjectInFolderContainerImpl();

    result.setObject(convert(container.getObjectInFolder()));

    List<ObjectInFolderContainer> containerList = new ArrayList<ObjectInFolderContainer>();
    if (!container.getChildren().isEmpty()) {
      for (CmisObjectInFolderContainerType containerChild : container.getChildren()) {
        containerList.add(convert(containerChild));
      }
    }
    result.setChildren(containerList);

    // handle extensions
    convertExtension(container, result);

    return result;
  }

  /**
   * Converts an ObjectInFolder container object.
   */
  public static CmisObjectInFolderContainerType convert(ObjectInFolderContainer container) {
    if (container == null) {
      return null;
    }

    CmisObjectInFolderContainerType result = new CmisObjectInFolderContainerType();

    result.setObjectInFolder(convert(container.getObject()));

    if (container.getChildren() != null) {
      for (ObjectInFolderContainer child : container.getChildren()) {
        result.getChildren().add(convert(child));
      }
    }

    // handle extensions
    convertExtension(container, result);

    return result;
  }

  /**
   * Converts an access control list object.
   */
  public static AccessControlList convert(CmisACLType acl) {
    AccessControlList result = convert(acl.getACL(), acl.isExact());

    // handle extensions
    convertExtension(acl, result);

    return result;
  }

  /**
   * Converts a FailedToDelete object.
   */
  public static FailedToDeleteData convert(DeleteTreeResponse.FailedToDelete failedToDelete) {
    if (failedToDelete == null) {
      return null;
    }

    FailedToDeleteDataImpl result = new FailedToDeleteDataImpl();

    result.setIds(failedToDelete.getObjectIds());

    // handle extensions
    convertExtension(failedToDelete, result);

    return result;
  }

  /**
   * Converts a FailedToDelete object.
   */
  public static DeleteTreeResponse.FailedToDelete convert(FailedToDeleteData failedToDelete) {
    if (failedToDelete == null) {
      return null;
    }

    DeleteTreeResponse.FailedToDelete result = new DeleteTreeResponse.FailedToDelete();

    if (failedToDelete.getIds() != null) {
      for (String id : failedToDelete.getIds()) {
        result.getObjectIds().add(id);
      }
    }

    // handle extensions
    convertExtension(failedToDelete, result);

    return result;
  }

  // -------------------------------------------------------------------------
  // --- Stream ---
  // -------------------------------------------------------------------------

  /**
   * Converts a content stream object.
   */
  public static ContentStreamData convert(CmisContentStreamType contentStream) {
    if (contentStream == null) {
      return null;
    }

    ContentStreamDataImpl result = new ContentStreamDataImpl();

    result.setFilename(contentStream.getFilename());
    result.setLength(contentStream.getLength());
    result.setMimeType(contentStream.getMimeType());
    if (contentStream.getStream() != null) {
      try {
        if (contentStream.getStream() instanceof StreamingDataHandler) {
          result.setStream(((StreamingDataHandler) contentStream.getStream()).readOnce());
        }
        else {
          result.setStream(contentStream.getStream().getInputStream());
        }
      }
      catch (IOException e) {
        throw new CmisRuntimeException("Could not get the stream: " + e.getMessage(), e);
      }
    }

    // handle extensions
    convertExtension(contentStream, result);

    return result;
  }

  /**
   * Converts a content stream object.
   */
  public static CmisContentStreamType convert(final ContentStreamData contentStream) {
    if (contentStream == null) {
      return null;
    }

    CmisContentStreamType result = new CmisContentStreamType();

    result.setFilename(contentStream.getFilename());
    result.setLength(contentStream.getLength());
    result.setMimeType(contentStream.getMimeType());

    result.setStream(new DataHandler(new DataSource() {

      public OutputStream getOutputStream() throws IOException {
        return null;
      }

      public String getName() {
        return contentStream.getFilename();
      }

      public InputStream getInputStream() throws IOException {
        return contentStream.getStream();
      }

      public String getContentType() {
        return contentStream.getMimeType();
      }
    }));

    return result;
  }

  // -------------------------------------------------------------------------
  // --- Extensions and holders ---
  // -------------------------------------------------------------------------

  /**
   * Converts a provider extension into a Web Services extension.
   */
  public static CmisExtensionType convert(ExtensionsData extension) {
    if (extension == null) {
      return null;
    }

    CmisExtensionType result = new CmisExtensionType();

    if (extension.getExtensions() != null) {
      for (Object obj : extension.getExtensions()) {
        result.getAny().add(obj);
      }
    }

    return result;
  }

  /**
   * Converts a provider extension into a Web Services extension holder.
   */
  public static javax.xml.ws.Holder<CmisExtensionType> convertExtensionHolder(
      ExtensionsData extension) {
    if (extension == null) {
      return null;
    }

    javax.xml.ws.Holder<CmisExtensionType> result = new javax.xml.ws.Holder<CmisExtensionType>();
    result.value = convert(extension);

    return result;
  }

  /**
   * Copies a holder value.
   */
  public static void setExtensionValues(javax.xml.ws.Holder<CmisExtensionType> source,
      ExtensionsData target) {
    if (target == null) {
      return;
    }
    target.setExtensions(null);

    if ((source == null) || (source.value == null)) {
      return;
    }

    List<Object> list = new ArrayList<Object>();
    target.setExtensions(list);

    if (!source.value.getAny().isEmpty()) {
      for (Object obj : source.value.getAny()) {
        list.add(obj);
      }
    }
  }

  /**
   * Converts a Web Services extension extension into a provider holder.
   */
  public static ExtensionsData convertExtensionHolder(
      javax.xml.ws.Holder<CmisExtensionType> extension) {
    if (extension == null) {
      return null;
    }

    return convert(extension.value);
  }

  /**
   * Copies a holder value.
   */
  public static void setExtensionValues(ExtensionsData source,
      javax.xml.ws.Holder<CmisExtensionType> target) {
    if ((target == null) || (target.value == null)) {
      return;
    }
    target.value.getAny().clear();

    if ((source == null) || (source.getExtensions() == null)) {
      return;
    }

    if (source.getExtensions() != null) {
      for (Object ext : source.getExtensions()) {
        target.value.getAny().add(ext);
      }
    }
  }

  /**
   * Converts a holder into a WS holder.
   */
  public static <T> javax.xml.ws.Holder<T> convertHolder(Holder<T> orgHolder) {
    if (orgHolder == null) {
      return null;
    }

    javax.xml.ws.Holder<T> result = new javax.xml.ws.Holder<T>();
    result.value = orgHolder.getValue();

    return result;
  }

  /**
   * Converts a WS holder into a holder.
   */
  public static <T> Holder<T> convertHolder(javax.xml.ws.Holder<T> orgHolder) {
    if (orgHolder == null) {
      return null;
    }

    Holder<T> result = new Holder<T>();
    result.setValue(orgHolder.value);

    return result;
  }

  /**
   * Copies a holder value for a WS holder to a holder.
   */
  public static <T> void setHolderValue(javax.xml.ws.Holder<T> source, Holder<T> target) {
    if ((source == null) || (target == null)) {
      return;
    }

    target.setValue(source.value);
  }

  /**
   * Copies a holder value for a holder to a WS holder.
   */
  public static <T> void setHolderValue(Holder<T> source, javax.xml.ws.Holder<T> target) {
    if ((source == null) || (target == null)) {
      return;
    }

    target.value = source.getValue();
  }

  @SuppressWarnings("unchecked")
  public static void convertExtension(Object source, ExtensionsData target) {
    if (source == null) {
      return;
    }

    try {
      Method m = source.getClass().getMethod("getAny", new Class<?>[0]);
      List<Object> list = (List<Object>) m.invoke(source, new Object[0]);

      if (!list.isEmpty()) {
        target.setExtensions(list);
      }
      else {
        target.setExtensions(null);
      }
    }
    catch (NoSuchMethodException e) {
    }
    catch (Exception e) {
      throw new CmisRuntimeException("Exception: " + e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  public static void convertExtension(ExtensionsData source, Object target) {
    if (source == null) {
      return;
    }

    try {
      Method m = source.getClass().getMethod("getAny", new Class<?>[0]);
      List<Object> list = (List<Object>) m.invoke(source, new Object[0]);

      list.clear();
      if (source.getExtensions() != null) {
        list.addAll(source.getExtensions());
      }
    }
    catch (NoSuchMethodException e) {
    }
    catch (Exception e) {
      throw new CmisRuntimeException("Exception: " + e.getMessage(), e);
    }
  }

  /**
   * Converts an extension object.
   */
  public static ExtensionsData convert(CmisExtensionType extension) {
    if (extension == null) {
      return null;
    }

    ExtensionDataImpl result = new ExtensionDataImpl();
    result.setExtensions(extension.getAny());

    return result;
  }

  private static boolean convertBoolean(Boolean value, boolean def) {
    return (value == null ? def : value.booleanValue());
  }

  // -------------------------------------------------------------------------
  // --- Enums ---
  // -------------------------------------------------------------------------

  /**
   * Converts an Enum.
   */
  public static <T extends Enum<T>> T convert(Class<T> destClass, Enum<?> source) {
    if (source == null) {
      return null;
    }

    return Enum.valueOf(destClass, source.name());
  }
}
