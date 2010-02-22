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
package org.apache.opencmis.fileshare;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.api.PropertyDefinition;
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
import org.apache.opencmis.commons.enums.SupportedPermissions;
import org.apache.opencmis.commons.enums.Updatability;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisBaseException;
import org.apache.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.exceptions.CmisStorageException;
import org.apache.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.opencmis.commons.impl.Converter;
import org.apache.opencmis.commons.impl.JaxBHelper;
import org.apache.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.AclCapabilitiesDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.AllowableActionsDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.ContentStreamDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectInFolderContainerImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertiesDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyBooleanDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyDateTimeDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyDecimalDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyHtmlDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyIdDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyIntegerDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyStringDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyUriDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.RepositoryInfoDataImpl;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.impl.jaxb.CmisProperty;
import org.apache.opencmis.commons.provider.AccessControlEntry;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.AllowableActionsData;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.FailedToDeleteData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectInFolderContainer;
import org.apache.opencmis.commons.provider.ObjectInFolderData;
import org.apache.opencmis.commons.provider.ObjectInFolderList;
import org.apache.opencmis.commons.provider.ObjectParentData;
import org.apache.opencmis.commons.provider.PermissionDefinitionData;
import org.apache.opencmis.commons.provider.PermissionMappingData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.PropertyDateTimeData;
import org.apache.opencmis.commons.provider.PropertyIdData;
import org.apache.opencmis.commons.provider.PropertyStringData;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.ObjectInfoHolder;
import org.apache.opencmis.server.spi.ObjectInfoImpl;

/**
 * File system back-end for CMIS server.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class FileShareRepository {

  private static final String ROOT_ID = "@root@";
  private static final String SHADOW_EXT = ".cmis.xml";
  private static final String SHADOW_FOLDER = "cmis.xml";

  private static final String USER_UNKNOWN = "<unknown>";

  private static final String CMIS_READ = "cmis:read";
  private static final String CMIS_WRITE = "cmis:write";
  private static final String CMIS_ALL = "cmis:all";

  private static final int BUFFER_SIZE = 64 * 1024;

  private static final Log log = LogFactory.getLog(FileShareRepository.class);

  /** Repository id */
  private String fRepositoryId;
  /** Root directory */
  private File fRoot;
  /** Types */
  private TypeManager fTypes;
  /** User table */
  private Map<String, Boolean> fUserMap;
  /** Repository info */
  private RepositoryInfoDataImpl fRepositoryInfo;

  /**
   * Constructor.
   * 
   * @param repId
   *          CMIS repository id
   * @param root
   *          root folder
   * @param types
   *          type manager object
   */
  public FileShareRepository(String repId, String root, TypeManager types) {
    // check repository id
    if ((repId == null) || (repId.trim().length() == 0)) {
      throw new IllegalArgumentException("Invalid repository id!");
    }

    fRepositoryId = repId;

    // check root folder
    if ((root == null) || (root.trim().length() == 0)) {
      throw new IllegalArgumentException("Invalid root folder!");
    }

    fRoot = new File(root);
    if (!fRoot.isDirectory()) {
      throw new IllegalArgumentException("Root is not a directory!");
    }

    // set types
    fTypes = types;

    // set up user table
    fUserMap = new HashMap<String, Boolean>();

    // compile repository info
    fRepositoryInfo = new RepositoryInfoDataImpl();

    fRepositoryInfo.setRepositoryId(fRepositoryId);
    fRepositoryInfo.setRepositoryName(fRepositoryId);
    fRepositoryInfo.setRepositoryDescription(fRepositoryId);

    fRepositoryInfo.setCmisVersionSupported("1.0");

    fRepositoryInfo.setProductName("OpenCMIS FileShare");
    fRepositoryInfo.setProductVersion("0.1");
    fRepositoryInfo.setVendorName("OpenCMIS");

    fRepositoryInfo.setRootFolder(ROOT_ID);

    fRepositoryInfo.setThinClientUri("");

    RepositoryCapabilitiesDataImpl capabilities = new RepositoryCapabilitiesDataImpl();
    capabilities.setCapabilityAcl(CapabilityAcl.DISCOVER);
    capabilities.setAllVersionsSearchable(false);
    capabilities.setCapabilityJoin(CapabilityJoin.NONE);
    capabilities.setSupportsMultifiling(false);
    capabilities.setSupportsUnfiling(false);
    capabilities.setSupportsVersionSpecificFiling(false);
    capabilities.setIsPwcSearchable(false);
    capabilities.setIsPwcUpdatable(false);
    capabilities.setCapabilityQuery(CapabilityQuery.NONE);
    capabilities.setCapabilityChanges(CapabilityChanges.NONE);
    capabilities.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.ANYTIME);
    capabilities.setSupportsGetDescendants(true);
    capabilities.setSupportsGetFolderTree(true);
    capabilities.setCapabilityRendition(CapabilityRendition.NONE);

    fRepositoryInfo.setRepositoryCapabilities(capabilities);

    AclCapabilitiesDataImpl aclCapability = new AclCapabilitiesDataImpl();
    aclCapability.setSupportedPermissions(SupportedPermissions.BASIC);
    aclCapability.setAclPropagation(AclPropagation.OBJECTONLY);

    // permissions
    List<PermissionDefinitionData> permissions = new ArrayList<PermissionDefinitionData>();
    permissions.add(createPermission(CMIS_READ, "Read"));
    permissions.add(createPermission(CMIS_WRITE, "Write"));
    permissions.add(createPermission(CMIS_ALL, "All"));
    aclCapability.setPermissionDefinitionData(permissions);

    // mapping
    List<PermissionMappingData> mappings = new ArrayList<PermissionMappingData>();
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_CREATE_DOCUMENT_FOLDER, CMIS_READ));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_CREATE_FOLDER_FOLDER, CMIS_READ));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_DELETE_CONTENT_DOCUMENT, CMIS_WRITE));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_DELETE_OBJECT, CMIS_ALL));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_DELETE_TREE_FOLDER, CMIS_ALL));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_GET_ACL_OBJECT, CMIS_READ));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_GET_ALL_VERSIONS_VERSION_SERIES,
        CMIS_READ));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_GET_CHILDREN_FOLDER, CMIS_READ));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_GET_DESCENDENTS_FOLDER, CMIS_READ));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_GET_FOLDER_PARENT_OBJECT, CMIS_READ));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_GET_PARENTS_FOLDER, CMIS_READ));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_GET_PROPERTIES_OBJECT, CMIS_READ));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_MOVE_OBJECT, CMIS_WRITE));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_MOVE_SOURCE, CMIS_READ));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_MOVE_TARGET, CMIS_WRITE));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_SET_CONTENT_DOCUMENT, CMIS_WRITE));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_UPDATE_PROPERTIES_OBJECT, CMIS_WRITE));
    mappings.add(createMapping(PermissionMappingData.KEY_CAN_VIEW_CONTENT_OBJECT, CMIS_READ));
    aclCapability.setPermissionMappingData(mappings);

    fRepositoryInfo.setAclCapabilities(aclCapability);
  }

  private PermissionDefinitionData createPermission(String permission, String description) {
    PermissionDefinitionDataImpl pd = new PermissionDefinitionDataImpl();
    pd.setPermission(permission);
    pd.setDescription(description);

    return pd;
  }

  private PermissionMappingData createMapping(String key, String permission) {
    PermissionMappingDataImpl pm = new PermissionMappingDataImpl();
    pm.setKey(key);
    pm.setPermissions(Collections.singletonList(permission));

    return pm;
  }

  /**
   * Adds a user to the repository.
   */
  public void addUser(String user, boolean readOnly) {
    if ((user == null) || (user.length() == 0)) {
      return;
    }

    fUserMap.put(user, readOnly);
  }

  // --- the public stuff ---

  /**
   * Returns the repository id.
   */
  public String getRepositoryId() {
    return fRepositoryId;
  }

  /**
   * CMIS getRepositoryInfo.
   */
  public RepositoryInfoData getRepositoryInfo(CallContext context) {
    debug("getRepositoryInfo");
    checkUser(context, false);

    return fRepositoryInfo;
  }

  /**
   * CMIS getTypesChildren.
   */
  public TypeDefinitionList getTypesChildren(CallContext context, String typeId,
      boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount) {
    debug("getTypesChildren");
    checkUser(context, false);

    return fTypes
        .getTypesChildren(context, typeId, includePropertyDefinitions, maxItems, skipCount);
  }

  /**
   * CMIS getTypeDefinition.
   */
  public TypeDefinition getTypeDefinition(CallContext context, String typeId) {
    debug("getTypeDefinition");
    checkUser(context, false);

    return fTypes.getTypeDefinition(context, typeId);
  }

  /**
   * CMIS getTypesDescendants.
   */
  public List<TypeDefinitionContainer> getTypesDescendants(CallContext context, String typeId,
      BigInteger depth, Boolean includePropertyDefinitions) {
    debug("getTypesDescendants");
    checkUser(context, false);

    return fTypes.getTypesDescendants(context, typeId, depth, includePropertyDefinitions);
  }

  /**
   * Create* dispatch for AtomPub.
   */
  public ObjectData create(CallContext context, PropertiesData properties, String folderId,
      ContentStreamData contentStream, VersioningState versioningState, ObjectInfoHolder objectInfos) {
    debug("create");
    boolean userReadOnly = checkUser(context, true);

    String typeId = getTypeId(properties);
    TypeDefinition type = fTypes.getType(typeId);
    if (type == null) {
      throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
    }

    String objectId = null;
    if (type.getBaseId() == BaseObjectTypeIds.CMIS_DOCUMENT) {
      objectId = createDocument(context, properties, folderId, contentStream, versioningState);
    }
    else if (type.getBaseId() == BaseObjectTypeIds.CMIS_FOLDER) {
      objectId = createFolder(context, properties, folderId);
    }
    else {
      throw new CmisObjectNotFoundException("Cannot create object of type '" + typeId + "'!");
    }

    return compileObjectType(getFile(objectId), null, false, false, userReadOnly, objectInfos);
  }

  /**
   * CMIS createDocument.
   */
  public String createDocument(CallContext context, PropertiesData properties, String folderId,
      ContentStreamData contentStream, VersioningState versioningState) {
    debug("createDocument");
    checkUser(context, true);

    // check properties
    if ((properties == null) || (properties.getProperties() == null)) {
      throw new CmisInvalidArgumentException("Properties must be set!");
    }

    // check versioning state
    if (VersioningState.NONE != versioningState) {
      throw new CmisConstraintException("Versioning not supported!");
    }

    // check type
    String typeId = getTypeId(properties);
    TypeDefinition type = fTypes.getType(typeId);
    if (type == null) {
      throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
    }

    // compile the properties
    PropertiesData props = compileProperties(typeId, context.getUsername(), millisToCalendar(System
        .currentTimeMillis()), context.getUsername(), properties);

    // check the name
    String name = getStringProperty(properties, PropertyIds.CMIS_NAME);
    if (!isValidName(name)) {
      throw new CmisNameConstraintViolationException("Name is not valid!");
    }

    // get parent File
    File parent = getFile(folderId);
    if (!parent.isDirectory()) {
      throw new CmisObjectNotFoundException("Parent is not a folder!");
    }

    // check the file
    File newFile = new File(parent, name);
    if (newFile.exists()) {
      throw new CmisNameConstraintViolationException("Document already exists!");
    }

    // create the file
    try {
      newFile.createNewFile();
    }
    catch (IOException e) {
      throw new CmisStorageException("Could not create file: " + e.getMessage());
    }

    // write content, if available
    if ((contentStream != null) && (contentStream.getStream() != null)) {
      try {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(newFile), BUFFER_SIZE);
        InputStream in = new BufferedInputStream(contentStream.getStream(), BUFFER_SIZE);

        byte[] buffer = new byte[BUFFER_SIZE];
        int b;
        while ((b = in.read(buffer)) > -1) {
          out.write(buffer, 0, b);
        }

        out.flush();
        out.close();
        in.close();
      }
      catch (Exception e) {
        throw new CmisStorageException("Could not write content: " + e.getMessage(), e);
      }
    }

    // write properties
    writePropertiesFile(newFile, props);

    return getId(newFile);
  }

  /**
   * CMIS createDocumentFromSource.
   */
  public String createDocumentFromSource(CallContext context, String sourceId,
      PropertiesData properties, String folderId, VersioningState versioningState) {

    // check versioning state
    if (VersioningState.NONE != versioningState) {
      throw new CmisConstraintException("Versioning not supported!");
    }

    // get parent File
    File parent = getFile(folderId);
    if (!parent.isDirectory()) {
      throw new CmisObjectNotFoundException("Parent is not a folder!");
    }

    // get source File
    File source = getFile(sourceId);
    if (!source.isFile()) {
      throw new CmisObjectNotFoundException("Source is not a document!");
    }

    // file name
    String name = source.getName();

    // get properties
    PropertiesDataImpl sourceProperties = new PropertiesDataImpl();
    readCustomProperties(source, sourceProperties, null, new ObjectInfoImpl());

    // get the type id
    String typeId = getIdProperty(sourceProperties, PropertyIds.CMIS_OBJECT_TYPE_ID);
    if (typeId == null) {
      typeId = TypeManager.DOCUMENT_TYPE_ID;
    }

    // copy properties
    PropertiesDataImpl newProperties = new PropertiesDataImpl();
    for (PropertyData<?> prop : sourceProperties.getProperties().values()) {
      if ((prop.getId().equals(PropertyIds.CMIS_OBJECT_TYPE_ID))
          || (prop.getId().equals(PropertyIds.CMIS_CREATED_BY))
          || (prop.getId().equals(PropertyIds.CMIS_CREATION_DATE))
          || (prop.getId().equals(PropertyIds.CMIS_LAST_MODIFIED_BY))) {
        continue;
      }

      newProperties.addProperty(prop);
    }

    // replace properties
    if (properties != null) {
      // find new name
      String newName = getStringProperty(properties, PropertyIds.CMIS_NAME);
      if (newName != null) {
        if (!isValidName(newName)) {
          throw new CmisNameConstraintViolationException("Name is not valid!");
        }
        name = newName;
      }

      // get the property definitions
      TypeDefinition type = fTypes.getType(typeId);
      if (type == null) {
        throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
      }

      // replace with new values
      for (PropertyData<?> prop : properties.getProperties().values()) {
        PropertyDefinition<?> propType = type.getPropertyDefinitions().get(prop.getId());

        // do we know that property?
        if (propType == null) {
          throw new CmisConstraintException("Property '" + prop.getId() + "' is unknown!");
        }

        // can it be set?
        if ((propType.getUpdatability() != Updatability.READWRITE)) {
          throw new CmisConstraintException("Property '" + prop.getId() + "' cannot be updated!");
        }

        // empty properties are invalid
        if (isEmptyProperty(prop)) {
          throw new CmisConstraintException("Property '" + prop.getId() + "' must not be empty!");
        }

        newProperties.addProperty(prop);
      }
    }

    addPropertyId(newProperties, typeId, null, PropertyIds.CMIS_OBJECT_TYPE_ID, typeId);
    addPropertyString(newProperties, typeId, null, PropertyIds.CMIS_CREATED_BY, context
        .getUsername());
    addPropertyDateTime(newProperties, typeId, null, PropertyIds.CMIS_CREATION_DATE,
        millisToCalendar(System.currentTimeMillis()));
    addPropertyString(newProperties, typeId, null, PropertyIds.CMIS_LAST_MODIFIED_BY, context
        .getUsername());

    // check the file
    File newFile = new File(parent, name);
    if (newFile.exists()) {
      throw new CmisNameConstraintViolationException("Document already exists.");
    }

    // create the file
    try {
      newFile.createNewFile();
    }
    catch (IOException e) {
      throw new CmisStorageException("Could not create file: " + e.getMessage(), e);
    }

    // copy content
    try {
      OutputStream out = new BufferedOutputStream(new FileOutputStream(newFile));
      InputStream in = new BufferedInputStream(new FileInputStream(source));

      byte[] buffer = new byte[BUFFER_SIZE];
      int b;
      while ((b = in.read(buffer)) > -1) {
        out.write(buffer, 0, b);
      }

      out.flush();
      out.close();
      in.close();
    }
    catch (Exception e) {
      throw new CmisStorageException("Could not roead or write content: " + e.getMessage(), e);
    }

    // write properties
    writePropertiesFile(newFile, newProperties);

    return getId(newFile);
  }

  /**
   * CMIS createFolder.
   */
  public String createFolder(CallContext context, PropertiesData properties, String folderId) {
    debug("createFolder");
    checkUser(context, true);

    // check properties
    if ((properties == null) || (properties.getProperties() == null)) {
      throw new CmisInvalidArgumentException("Properties must be set!");
    }

    // check type
    String typeId = getTypeId(properties);
    TypeDefinition type = fTypes.getType(typeId);
    if (type == null) {
      throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
    }

    // compile the properties
    PropertiesData props = compileProperties(typeId, context.getUsername(), millisToCalendar(System
        .currentTimeMillis()), context.getUsername(), properties);

    // check the name
    String name = getStringProperty(properties, PropertyIds.CMIS_NAME);
    if (!isValidName(name)) {
      throw new CmisNameConstraintViolationException("Name is not valid.");
    }

    // get parent File
    File parent = getFile(folderId);
    if (!parent.isDirectory()) {
      throw new CmisObjectNotFoundException("Parent is not a folder!");
    }

    // create the folder
    File newFolder = new File(parent, name);
    if (!newFolder.mkdir()) {
      throw new CmisStorageException("Could not create folder!");
    }

    // write properties
    writePropertiesFile(newFolder, props);

    return getId(newFolder);
  }

  /**
   * CMIS moveObject.
   */
  public ObjectData moveObject(CallContext context, Holder<String> objectId, String targetFolderId,
      ObjectInfoHolder objectInfos) {
    debug("moveObject");
    boolean userReadOnly = checkUser(context, true);

    if (objectId == null) {
      throw new CmisInvalidArgumentException("Id is not valid!");
    }

    // get the file and parent
    File file = getFile(objectId.getValue());
    File parent = getFile(targetFolderId);

    // build new path
    File newFile = new File(parent, file.getName());
    if (newFile.exists()) {
      throw new CmisStorageException("Object already exists!");
    }

    // move it
    if (!file.renameTo(newFile)) {
      throw new CmisStorageException("Move failed!");
    }
    else {
      // set new id
      objectId.setValue(getId(newFile));

      // if it is a file, move properties file too
      if (newFile.isFile()) {
        File propFile = getPropertiesFile(file);
        if (propFile.exists()) {
          File newPropFile = new File(parent, propFile.getName());
          propFile.renameTo(newPropFile);
        }
      }
    }

    return compileObjectType(newFile, null, false, false, userReadOnly, objectInfos);
  }

  /**
   * CMIS setContentStream and deleteContentStream.
   */
  public void setContentStream(CallContext context, Holder<String> objectId, Boolean overwriteFlag,
      ContentStreamData contentStream) {
    debug("setContentStream or deleteContentStream");
    checkUser(context, true);

    if (objectId == null) {
      throw new CmisInvalidArgumentException("Id is not valid!");
    }

    // get the file
    File file = getFile(objectId.getValue());
    if (!file.isFile()) {
      throw new CmisStreamNotSupportedException("Not a file!");
    }

    // check overwrite
    boolean owf = (overwriteFlag == null ? true : overwriteFlag.booleanValue());
    if (!owf && file.length() > 0) {
      throw new CmisContentAlreadyExistsException("Content already exists!");
    }

    try {
      OutputStream out = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);

      if ((contentStream == null) || (contentStream.getStream() == null)) {
        // delete content
        out.write(new byte[0]);
      }
      else {
        // set content
        InputStream in = new BufferedInputStream(contentStream.getStream(), BUFFER_SIZE);

        byte[] buffer = new byte[BUFFER_SIZE];
        int b;
        while ((b = in.read(buffer)) > -1) {
          out.write(buffer, 0, b);
        }

        in.close();
      }

      out.close();
    }
    catch (Exception e) {
      throw new CmisStorageException("Could not write content: " + e.getMessage(), e);
    }
  }

  /**
   * CMIS deleteObject.
   */
  public void deleteObject(CallContext context, String objectId) {
    debug("deleteObject");
    checkUser(context, true);

    // get the file or folder
    File file = getFile(objectId);
    if (!file.exists()) {
      throw new CmisObjectNotFoundException("Object not found!");
    }

    // check if it is a folder and if it is empty
    if (!isFolderEmpty(file)) {
      throw new CmisConstraintException("Folder is not empty!");
    }

    // delete properties and actual file
    getPropertiesFile(file).delete();
    if (!file.delete()) {
      throw new CmisStorageException("Deletion failed!");
    }
  }

  /**
   * CMIS deleteTree.
   */
  public FailedToDeleteData deleteTree(CallContext context, String folderId,
      Boolean continueOnFailure) {
    debug("deleteTree");
    checkUser(context, true);

    boolean cof = (continueOnFailure == null ? false : continueOnFailure.booleanValue());

    // get the file or folder
    File file = getFile(folderId);

    FailedToDeleteDataImpl result = new FailedToDeleteDataImpl();
    result.setIds(new ArrayList<String>());

    // if it is a folder, remove it recursively
    if (file.isDirectory()) {
      deleteFolder(file, cof, result);
    }
    else {
      getPropertiesFile(file).delete();
      if (!file.delete()) {
        result.getIds().add(getId(file));
      }
    }

    return result;
  }

  /**
   * CMIS updateProperties.
   */
  public ObjectData updateProperties(CallContext context, Holder<String> objectId,
      PropertiesData properties, ObjectInfoHolder objectInfos) {
    debug("updateProperties");
    boolean userReadOnly = checkUser(context, true);

    if (objectId == null) {
      throw new CmisInvalidArgumentException("Id is not valid!");
    }

    // get the file or folder
    File file = getFile(objectId.getValue());

    // get and check the new name
    String newName = getStringProperty(properties, PropertyIds.CMIS_NAME);
    boolean isRename = (newName != null) && (!file.getName().equals(newName));
    if (isRename && !isValidName(newName)) {
      throw new CmisNameConstraintViolationException("Name is not valid!");
    }

    // get old properties
    PropertiesDataImpl oldProperties = new PropertiesDataImpl();
    readCustomProperties(file, oldProperties, null, new ObjectInfoImpl());

    // get the type id
    String typeId = getIdProperty(oldProperties, PropertyIds.CMIS_OBJECT_TYPE_ID);
    if (typeId == null) {
      typeId = (file.isDirectory() ? TypeManager.FOLDER_TYPE_ID : TypeManager.DOCUMENT_TYPE_ID);
    }

    // get the creator
    String creator = getStringProperty(oldProperties, PropertyIds.CMIS_CREATED_BY);
    if (creator == null) {
      creator = context.getUsername();
    }

    // get creation date
    GregorianCalendar creationDate = getDateTimeProperty(oldProperties,
        PropertyIds.CMIS_CREATION_DATE);
    if (creationDate == null) {
      creationDate = millisToCalendar(file.lastModified());
    }

    // compile the properties
    PropertiesData props = updateProperties(typeId, creator, creationDate, context.getUsername(),
        oldProperties, properties);

    // write properties
    writePropertiesFile(file, props);

    // rename file or folder if necessary
    File newFile = file;
    if (isRename) {
      File parent = file.getParentFile();
      File propFile = getPropertiesFile(file);
      newFile = new File(parent, newName);
      if (!file.renameTo(newFile)) {
        // if something went wrong, throw an exception
        throw new CmisUpdateConflictException("Could not rename object!");
      }
      else {
        // set new id
        objectId.setValue(getId(newFile));

        // if it is a file, rename properties file too
        if (newFile.isFile()) {
          if (propFile.exists()) {
            File newPropFile = new File(parent, newName + SHADOW_EXT);
            propFile.renameTo(newPropFile);
          }
        }
      }
    }

    return compileObjectType(newFile, null, false, false, userReadOnly, objectInfos);
  }

  /**
   * CMIS getObject.
   */
  public ObjectData getObject(CallContext context, String objectId, String filter,
      Boolean includeAllowableActions, Boolean includeAcl, ObjectInfoHolder objectInfos) {
    debug("getObject");
    boolean userReadOnly = checkUser(context, false);

    // check id
    if (objectId == null) {
      throw new CmisInvalidArgumentException("Object Id must be set.");
    }

    // get the file or folder
    File file = getFile(objectId);

    // set defaults if values not set
    boolean iaa = (includeAllowableActions == null ? false : includeAllowableActions.booleanValue());
    boolean iacl = (includeAcl == null ? false : includeAcl.booleanValue());

    // split filter
    Set<String> filterCollection = splitFilter(filter);

    // gather properties
    return compileObjectType(file, filterCollection, iaa, iacl, userReadOnly, objectInfos);
  }

  /**
   * CMIS getAllowableActions.
   */
  public AllowableActionsData getAllowableActions(CallContext context, String objectId) {
    debug("getAllowableActions");
    boolean userReadOnly = checkUser(context, false);

    File file = getFile(objectId);
    if (!file.exists()) {
      throw new CmisObjectNotFoundException("Object not found!");
    }

    return compileAllowableActions(file, userReadOnly);
  }

  /**
   * CMIS getACL.
   */
  public AccessControlList getAcl(CallContext context, String repositoryId, String objectId) {
    debug("getAcl");
    checkUser(context, false);

    // get the file or folder
    File file = getFile(objectId);
    if (!file.exists()) {
      throw new CmisObjectNotFoundException("Object not found!");
    }

    return compileAcl(file);
  }

  /**
   * CMIS getContentStream.
   */
  public ContentStreamData getContentStream(CallContext context, String objectId,
      BigInteger offset, BigInteger length) {
    debug("getContentStream");
    checkUser(context, false);

    if ((offset != null) || (length != null)) {
      throw new CmisInvalidArgumentException("Offset and Length are not supported!");
    }

    // get the file
    final File file = getFile(objectId);
    if (!file.isFile()) {
      throw new CmisStreamNotSupportedException("Not a file!");
    }

    InputStream stream = null;
    try {
      stream = new BufferedInputStream(new FileInputStream(file), 4 * 1024);
    }
    catch (FileNotFoundException e) {
      throw new CmisObjectNotFoundException(e.getMessage(), e);
    }

    // compile data
    ContentStreamDataImpl result = new ContentStreamDataImpl();
    result.setFilename(file.getName());
    result.setLength(BigInteger.valueOf(file.length()));
    result.setMimeType(MIMETypes.getMIMEType(file));
    result.setStream(stream);

    return result;
  }

  /**
   * CMIS getChildren.
   */
  public ObjectInFolderList getChildren(CallContext context, String folderId, String filter,
      Boolean includeAllowableActions, Boolean includePathSegment, BigInteger maxItems,
      BigInteger skipCount, ObjectInfoHolder objectInfos) {
    debug("getChildren");
    boolean userReadOnly = checkUser(context, false);

    // split filter
    Set<String> filterCollection = splitFilter(filter);

    // set defaults if values not set
    boolean iaa = (includeAllowableActions == null ? false : includeAllowableActions.booleanValue());
    boolean ips = (includePathSegment == null ? false : includePathSegment.booleanValue());

    // skip and max
    int skip = (skipCount == null ? 0 : skipCount.intValue());
    if (skip < 0) {
      skip = 0;
    }

    int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
    if (max < 0) {
      max = Integer.MAX_VALUE;
    }

    // get the folder
    File folder = getFile(folderId);
    if (!folder.isDirectory()) {
      throw new CmisObjectNotFoundException("Not a folder!");
    }

    // set object info of the the folder
    if (objectInfos != null) {
      compileObjectType(folder, null, false, false, userReadOnly, objectInfos);
    }

    // prepare result
    ObjectInFolderListImpl result = new ObjectInFolderListImpl();
    result.setObjects(new ArrayList<ObjectInFolderData>());
    result.setHasMoreItems(false);
    int count = 0;

    // iterate through children
    for (File child : folder.listFiles()) {
      // skip hidden and shadow files
      if (child.isHidden() || child.getName().equals(SHADOW_FOLDER)
          || child.getPath().endsWith(SHADOW_EXT)) {
        continue;
      }

      count++;

      if (skip > 0) {
        skip--;
        continue;
      }

      if (result.getObjects().size() >= max) {
        result.setHasMoreItems(true);
        continue;
      }

      // build and add child object
      ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
      objectInFolder.setObject(compileObjectType(child, filterCollection, iaa, false, userReadOnly,
          objectInfos));
      if (ips) {
        objectInFolder.setPathSegment(child.getName());
      }

      result.getObjects().add(objectInFolder);
    }

    result.setNumItems(BigInteger.valueOf(count));

    return result;
  }

  /**
   * CMIS getDescendants.
   */
  public List<ObjectInFolderContainer> getDescendants(CallContext context, String folderId,
      BigInteger depth, String filter, Boolean includeAllowableActions, Boolean includePathSegment,
      ObjectInfoHolder objectInfos, boolean foldersOnly) {
    debug("getDescendants or getFolderTree");
    boolean userReadOnly = checkUser(context, false);

    // check depth
    int d = (depth == null ? 2 : depth.intValue());
    if (d == 0) {
      throw new CmisInvalidArgumentException("Depth must not be 0!");
    }
    if (d < -1) {
      d = -1;
    }

    // split filter
    Set<String> filterCollection = splitFilter(filter);

    // set defaults if values not set
    boolean iaa = (includeAllowableActions == null ? false : includeAllowableActions.booleanValue());
    boolean ips = (includePathSegment == null ? false : includePathSegment.booleanValue());

    // get the folder
    File folder = getFile(folderId);
    if (!folder.isDirectory()) {
      throw new CmisObjectNotFoundException("Not a folder!");
    }

    // set object info of the the folder
    if (objectInfos != null) {
      compileObjectType(folder, null, false, false, userReadOnly, objectInfos);
    }

    // get the tree
    List<ObjectInFolderContainer> result = new ArrayList<ObjectInFolderContainer>();
    gatherDescendants(folder, result, foldersOnly, d, filterCollection, iaa, ips, userReadOnly,
        objectInfos);

    return result;
  }

  /**
   * CMIS getFolderParent.
   */
  public ObjectData getFolderParent(CallContext context, String folderId, String filter,
      ObjectInfoHolder objectInfos) {
    List<ObjectParentData> parents = getObjectParents(context, folderId, filter, false, false,
        objectInfos);

    if (parents.size() == 0) {
      throw new CmisInvalidArgumentException("The root folder has no parent!");
    }

    return parents.get(0).getObject();
  }

  /**
   * CMIS getObjectParents.
   */
  public List<ObjectParentData> getObjectParents(CallContext context, String objectId,
      String filter, Boolean includeAllowableActions, Boolean includeRelativePathSegment,
      ObjectInfoHolder objectInfos) {
    debug("getObjectParents");
    boolean userReadOnly = checkUser(context, false);

    // split filter
    Set<String> filterCollection = splitFilter(filter);

    // set defaults if values not set
    boolean iaa = (includeAllowableActions == null ? false : includeAllowableActions.booleanValue());
    boolean irps = (includeRelativePathSegment == null ? false : includeRelativePathSegment
        .booleanValue());

    // get the file or folder
    File file = getFile(objectId);

    // don't climb above the root folder
    if (fRoot.equals(file)) {
      return Collections.emptyList();
    }

    // set object info of the the object
    if (objectInfos != null) {
      compileObjectType(file, null, false, false, userReadOnly, objectInfos);
    }

    // get parent folder
    File parent = file.getParentFile();
    ObjectData object = compileObjectType(parent, filterCollection, iaa, false, userReadOnly,
        objectInfos);

    ObjectParentDataImpl result = new ObjectParentDataImpl();
    result.setObject(object);
    if (irps) {
      result.setRelativePathSegment(file.getName());
    }

    return Collections.singletonList((ObjectParentData) result);
  }

  /**
   * CMIS getObjectByPath.
   */
  public ObjectData getObjectByPath(CallContext context, String folderPath, String filter,
      boolean includeAllowableActions, boolean includeACL, ObjectInfoHolder objectInfos) {
    debug("getObjectByPath");
    boolean userReadOnly = checkUser(context, false);

    // split filter
    Set<String> filterCollection = splitFilter(filter);

    // check path
    if ((folderPath == null) || (!folderPath.startsWith("/"))) {
      throw new CmisInvalidArgumentException("Invalid folder path!");
    }

    // get the file or folder
    File file = null;
    if (folderPath.length() == 1) {
      file = fRoot;
    }
    else {
      String path = folderPath.replace('/', File.separatorChar).substring(1);
      file = new File(fRoot, path);
    }

    if (!file.exists()) {
      throw new CmisObjectNotFoundException("Path doesn't exist.");
    }

    return compileObjectType(file, filterCollection, includeAllowableActions, includeACL,
        userReadOnly, objectInfos);
  }

  // --- helper methods ---

  /**
   * Gather the children of a folder.
   */
  private void gatherDescendants(File folder, List<ObjectInFolderContainer> list,
      boolean foldersOnly, int depth, Set<String> filter, boolean includeAllowableActions,
      boolean includePathSegments, boolean userReadOnly, ObjectInfoHolder objectInfos) {
    // iterate through children
    for (File child : folder.listFiles()) {
      // skip hidden and shadow files
      if (child.isHidden() || child.getName().equals(SHADOW_FOLDER)
          || child.getPath().endsWith(SHADOW_EXT)) {
        continue;
      }

      // folders only?
      if (foldersOnly && !child.isDirectory()) {
        continue;
      }

      // add to list
      ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
      objectInFolder.setObject(compileObjectType(child, filter, includeAllowableActions, false,
          userReadOnly, objectInfos));
      if (includePathSegments) {
        objectInFolder.setPathSegment(child.getName());
      }

      ObjectInFolderContainerImpl container = new ObjectInFolderContainerImpl();
      container.setObject(objectInFolder);

      list.add(container);

      // move to next level
      if ((depth != 1) && child.isDirectory()) {
        container.setChildren(new ArrayList<ObjectInFolderContainer>());
        gatherDescendants(child, container.getChildren(), foldersOnly, depth - 1, filter,
            includeAllowableActions, includePathSegments, userReadOnly, objectInfos);
      }
    }
  }

  /**
   * Removes a folder and its content.
   * 
   * @throws
   */
  private boolean deleteFolder(File folder, boolean continueOnFailure, FailedToDeleteDataImpl ftd) {
    boolean success = true;

    for (File file : folder.listFiles()) {
      if (file.isDirectory()) {
        if (!deleteFolder(file, continueOnFailure, ftd)) {
          if (!continueOnFailure) {
            return false;
          }
          success = false;
        }
      }
      else {
        if (!file.delete()) {
          ftd.getIds().add(getId(file));
          if (!continueOnFailure) {
            return false;
          }
          success = false;
        }
      }
    }

    if (!folder.delete()) {
      ftd.getIds().add(getId(folder));
      success = false;
    }

    return success;
  }

  /**
   * Checks if the given name is valid for a file system.
   * 
   * @param name
   *          the name to check
   * 
   * @return <code>true</code> if the name is valid, <code>false</code> otherwise
   */
  private boolean isValidName(String name) {
    if ((name == null) || (name.length() == 0) || (name.indexOf(File.separatorChar) != -1)
        || (name.indexOf(File.pathSeparatorChar) != -1)) {
      return false;
    }

    return true;
  }

  /**
   * Checks if a folder is empty. A folder is considered as empty if no files or only the shadow
   * file reside in the folder.
   * 
   * @param folder
   *          the folder
   * 
   * @return <code>true</code> if the folder is empty.
   */
  private boolean isFolderEmpty(File folder) {
    if (!folder.isDirectory()) {
      return true;
    }

    String[] fileNames = folder.list();

    if ((fileNames == null) || (fileNames.length == 0)) {
      return true;
    }

    if ((fileNames.length == 1) && (fileNames[0].equals(SHADOW_FOLDER))) {
      return true;
    }

    return false;
  }

  /**
   * Compiles an object type object from a file or folder.�
   */
  private ObjectData compileObjectType(File file, Set<String> filter,
      boolean includeAllowableActions, boolean includeAcl, boolean userReadOnly,
      ObjectInfoHolder objectInfos) {
    ObjectDataImpl result = new ObjectDataImpl();

    result.setProperties(compileProperties(file, filter, objectInfos));

    if (includeAllowableActions) {
      result.setAllowableActions(compileAllowableActions(file, userReadOnly));
    }

    if (includeAcl) {
      result.setAcl(compileAcl(file));
      result.setIsExactAcl(true);
    }

    return result;
  }

  /**
   * Gathers all base properties of a file or folder.
   */
  private PropertiesData compileProperties(File file, Set<String> orgfilter,
      ObjectInfoHolder objectInfos) {
    if (file == null) {
      throw new IllegalArgumentException("File must not be null!");
    }

    // we can gather properties if the file or folder doesn't exist
    if (!file.exists()) {
      throw new CmisObjectNotFoundException("Object not found!");
    }

    // copy filter
    Set<String> filter = (orgfilter == null ? null : new HashSet<String>(orgfilter));

    // find base type
    String typeId = null;
    ObjectInfoImpl objectInfo = null;

    if (file.isDirectory()) {
      typeId = TypeManager.FOLDER_TYPE_ID;
      objectInfo = new FolderInfo();
      objectInfo.setTypeId(typeId);
    }
    else {
      typeId = TypeManager.DOCUMENT_TYPE_ID;
      objectInfo = new DocumentInfo();
      objectInfo.setTypeId(typeId);
    }

    // let's do it
    try {
      PropertiesDataImpl result = new PropertiesDataImpl();

      // id
      String id = fileToId(file);
      addPropertyId(result, typeId, filter, PropertyIds.CMIS_OBJECT_ID, id);
      objectInfo.setId(id);

      // name
      String name = file.getName();
      addPropertyString(result, typeId, filter, PropertyIds.CMIS_NAME, name);
      objectInfo.setName(name);

      // created and modified by
      addPropertyString(result, typeId, filter, PropertyIds.CMIS_CREATED_BY, USER_UNKNOWN);
      addPropertyString(result, typeId, filter, PropertyIds.CMIS_LAST_MODIFIED_BY, USER_UNKNOWN);
      objectInfo.setCreatedBy(USER_UNKNOWN);

      // creation and modification date
      GregorianCalendar lastModified = millisToCalendar(file.lastModified());
      addPropertyDateTime(result, typeId, filter, PropertyIds.CMIS_CREATION_DATE, lastModified);
      addPropertyDateTime(result, typeId, filter, PropertyIds.CMIS_LAST_MODIFICATION_DATE,
          lastModified);
      objectInfo.setCreationDate(lastModified);
      objectInfo.setLastModificationDate(lastModified);

      // directory or file
      if (file.isDirectory()) {
        // base type and type name
        addPropertyId(result, typeId, filter, PropertyIds.CMIS_BASE_TYPE_ID,
            BaseObjectTypeIds.CMIS_FOLDER.value());
        addPropertyId(result, typeId, filter, PropertyIds.CMIS_OBJECT_TYPE_ID,
            TypeManager.FOLDER_TYPE_ID);
        String path = getRepositoryPath(file);
        addPropertyString(result, typeId, filter, PropertyIds.CMIS_PATH, (path.length() == 0 ? "/"
            : path));

        // folder properties
        if (!fRoot.equals(file)) {
          addPropertyId(result, typeId, filter, PropertyIds.CMIS_PARENT_ID, (fRoot.equals(file
              .getParentFile()) ? ROOT_ID : fileToId(file.getParentFile())));
          objectInfo.setHasParent(true);
        }
        else {
          objectInfo.setHasParent(false);
        }
      }
      else {
        // base type and type name
        addPropertyId(result, typeId, filter, PropertyIds.CMIS_BASE_TYPE_ID,
            BaseObjectTypeIds.CMIS_DOCUMENT.value());
        addPropertyId(result, typeId, filter, PropertyIds.CMIS_OBJECT_TYPE_ID,
            TypeManager.DOCUMENT_TYPE_ID);

        // file properties
        addPropertyBoolean(result, typeId, filter, PropertyIds.CMIS_IS_IMMUTABLE, false);
        addPropertyBoolean(result, typeId, filter, PropertyIds.CMIS_IS_LATEST_VERSION, true);
        addPropertyBoolean(result, typeId, filter, PropertyIds.CMIS_IS_MAJOR_VERSION, true);
        addPropertyBoolean(result, typeId, filter, PropertyIds.CMIS_IS_LATEST_MAJOR_VERSION, true);
        addPropertyString(result, typeId, filter, PropertyIds.CMIS_VERSION_LABEL, file.getName());
        addPropertyId(result, typeId, filter, PropertyIds.CMIS_VERSION_SERIES_ID, fileToId(file));
        addPropertyString(result, typeId, filter, PropertyIds.CMIS_CHECKIN_COMMENT, "");
        addPropertyInteger(result, typeId, filter, PropertyIds.CMIS_CONTENT_STREAM_LENGTH, file
            .length());
        addPropertyString(result, typeId, filter, PropertyIds.CMIS_CONTENT_STREAM_MIME_TYPE,
            MIMETypes.getMIMEType(file));
        addPropertyString(result, typeId, filter, PropertyIds.CMIS_CONTENT_STREAM_FILE_NAME, file
            .getName());

        objectInfo.setContentType(MIMETypes.getMIMEType(file));
        objectInfo.setFileName(file.getName());
      }

      // read custom properties
      readCustomProperties(file, result, filter, objectInfo);

      if (filter != null) {
        if (!filter.isEmpty()) {
          debug("Unknown filter properties: " + filter.toString(), null);
        }
      }

      if (objectInfos != null) {
        objectInfos.addObjectInfo(objectInfo);
      }

      return result;
    }
    catch (Exception e) {
      if (e instanceof CmisBaseException) {
        throw (CmisBaseException) e;
      }
      throw new CmisRuntimeException(e.getMessage());
    }
  }

  /**
   * Reads and adds properties.
   */
  @SuppressWarnings("unchecked")
  private void readCustomProperties(File file, PropertiesDataImpl properties, Set<String> filter,
      ObjectInfoImpl objectInfo) {
    File propFile = getPropertiesFile(file);

    // if it doesn't exists, ignore it
    if (!propFile.exists()) {
      return;
    }

    // parse it
    JAXBElement<CmisObjectType> obj = null;
    try {
      Unmarshaller u = JaxBHelper.createUnmarshaller();
      obj = (JAXBElement<CmisObjectType>) u.unmarshal(propFile);
    }
    catch (Exception e) {
      warn("Unvalid CMIS properties: " + propFile.getAbsolutePath(), e);
    }

    if ((obj == null) || (obj.getValue() == null) || (obj.getValue().getProperties() == null)) {
      return;
    }

    // add it to properties
    for (CmisProperty cmisProp : obj.getValue().getProperties().getProperty()) {
      PropertyData<?> prop = Converter.convert(cmisProp);

      // overwrite object info
      if (prop instanceof PropertyStringData) {
        String firstValueStr = ((PropertyStringData) prop).getFirstValue();
        if (PropertyIds.CMIS_NAME.equals(prop.getId())) {
          objectInfo.setName(firstValueStr);
        }
        else if (PropertyIds.CMIS_OBJECT_TYPE_ID.equals(prop.getId())) {
          objectInfo.setTypeId(firstValueStr);
        }
        else if (PropertyIds.CMIS_CREATED_BY.equals(prop.getId())) {
          objectInfo.setCreatedBy(firstValueStr);
        }
        else if (PropertyIds.CMIS_CONTENT_STREAM_MIME_TYPE.equals(prop.getId())) {
          objectInfo.setContentType(firstValueStr);
        }
        else if (PropertyIds.CMIS_CONTENT_STREAM_FILE_NAME.equals(prop.getId())) {
          objectInfo.setFileName(firstValueStr);
        }
      }

      if (prop instanceof PropertyDateTimeData) {
        GregorianCalendar firstValueCal = ((PropertyDateTimeData) prop).getFirstValue();
        if (PropertyIds.CMIS_CREATION_DATE.equals(prop.getId())) {
          objectInfo.setCreationDate(firstValueCal);
        }
        else if (PropertyIds.CMIS_LAST_MODIFICATION_DATE.equals(prop.getId())) {
          objectInfo.setLastModificationDate(firstValueCal);
        }
      }

      // check filter
      if (filter != null) {
        if (!filter.contains(prop.getId())) {
          continue;
        }
        else {
          filter.remove(prop.getId());
        }
      }

      // don't overwrite id
      if (PropertyIds.CMIS_OBJECT_ID.equals(prop.getId())) {
        continue;
      }

      // don't overwrite base type
      if (PropertyIds.CMIS_BASE_TYPE_ID.equals(prop.getId())) {
        continue;
      }

      // add it
      properties.addProperty(prop);
    }
  }

  /**
   * Checks and compiles a property set that can be written to disc.
   */
  private PropertiesData compileProperties(String typeId, String creator,
      GregorianCalendar creationDate, String modifier, PropertiesData properties) {
    PropertiesDataImpl result = new PropertiesDataImpl();
    Set<String> addedProps = new HashSet<String>();

    if ((properties == null) || (properties.getProperties() == null)) {
      throw new CmisConstraintException("No properties!");
    }

    // get the property definitions
    TypeDefinition type = fTypes.getType(typeId);
    if (type == null) {
      throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
    }

    // check if all required properties are there
    for (PropertyData<?> prop : properties.getProperties().values()) {
      PropertyDefinition<?> propType = type.getPropertyDefinitions().get(prop.getId());

      // do we know that property?
      if (propType == null) {
        throw new CmisConstraintException("Property '" + prop.getId() + "' is unknown!");
      }

      // skip type id
      if (propType.getId().equals(PropertyIds.CMIS_OBJECT_TYPE_ID)) {
        continue;
      }

      // can it be set?
      if ((propType.getUpdatability() == Updatability.READONLY)) {
        throw new CmisConstraintException("Property '" + prop.getId() + "' is readonly!");
      }

      // empty properties are invalid
      if (isEmptyProperty(prop)) {
        throw new CmisConstraintException("Property '" + prop.getId() + "' must not be empty!");
      }

      // add it
      result.addProperty(prop);
      addedProps.add(prop.getId());
    }

    // check if required properties are missing
    for (PropertyDefinition<?> propDef : type.getPropertyDefinitions().values()) {
      if (!addedProps.contains(propDef.getId())
          && (propDef.getUpdatability() != Updatability.READONLY)) {
        if (!addPropertyDefault(result, propDef) && propDef.isRequired()) {
          throw new CmisConstraintException("Property '" + propDef.getId() + "' is required!");
        }
      }
    }

    addPropertyId(result, typeId, null, PropertyIds.CMIS_OBJECT_TYPE_ID, typeId);
    addPropertyString(result, typeId, null, PropertyIds.CMIS_CREATED_BY, creator);
    addPropertyDateTime(result, typeId, null, PropertyIds.CMIS_CREATION_DATE, creationDate);
    addPropertyString(result, typeId, null, PropertyIds.CMIS_LAST_MODIFIED_BY, modifier);

    return result;
  }

  /**
   * Checks and updates a property set that can be written to disc.
   */
  private PropertiesData updateProperties(String typeId, String creator,
      GregorianCalendar creationDate, String modifier, PropertiesData oldProperties,
      PropertiesData properties) {
    PropertiesDataImpl result = new PropertiesDataImpl();

    if (properties == null) {
      throw new CmisConstraintException("No properties!");
    }

    // get the property definitions
    TypeDefinition type = fTypes.getType(typeId);
    if (type == null) {
      throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
    }

    // copy old properties
    for (PropertyData<?> prop : oldProperties.getProperties().values()) {
      PropertyDefinition<?> propType = type.getPropertyDefinitions().get(prop.getId());

      // do we know that property?
      if (propType == null) {
        throw new CmisConstraintException("Property '" + prop.getId() + "' is unknown!");
      }

      // only add read/write properties
      if ((propType.getUpdatability() != Updatability.READWRITE)) {
        continue;
      }

      result.addProperty(prop);
    }

    // update properties
    for (PropertyData<?> prop : properties.getProperties().values()) {
      PropertyDefinition<?> propType = type.getPropertyDefinitions().get(prop.getId());

      // do we know that property?
      if (propType == null) {
        throw new CmisConstraintException("Property '" + prop.getId() + "' is unknown!");
      }

      // can it be set?
      if ((propType.getUpdatability() == Updatability.READONLY)) {
        throw new CmisConstraintException("Property '" + prop.getId() + "' is readonly!");
      }

      if ((propType.getUpdatability() == Updatability.ONCREATE)) {
        throw new CmisConstraintException("Property '" + prop.getId()
            + "' can only be set on create!");
      }

      // default or value
      if (isEmptyProperty(prop)) {
        addPropertyDefault(result, propType);
      }
      else {
        result.addProperty(prop);
      }
    }

    addPropertyId(result, typeId, null, PropertyIds.CMIS_OBJECT_TYPE_ID, typeId);
    addPropertyString(result, typeId, null, PropertyIds.CMIS_CREATED_BY, creator);
    addPropertyDateTime(result, typeId, null, PropertyIds.CMIS_CREATION_DATE, creationDate);
    addPropertyString(result, typeId, null, PropertyIds.CMIS_LAST_MODIFIED_BY, modifier);

    return result;
  }

  private boolean isEmptyProperty(PropertyData<?> prop) {
    if ((prop == null) || (prop.getValues() == null)) {
      return true;
    }

    return prop.getValues().isEmpty();
  }

  private void addPropertyId(PropertiesDataImpl props, String typeId, Set<String> filter,
      String id, String value) {
    if (!checkAddProperty(props, typeId, filter, id)) {
      return;
    }

    if (value == null) {
      throw new IllegalArgumentException("Value must not be null!");
    }

    props.addProperty(new PropertyIdDataImpl(id, value));
  }

  private void addPropertyString(PropertiesDataImpl props, String typeId, Set<String> filter,
      String id, String value) {
    if (!checkAddProperty(props, typeId, filter, id)) {
      return;
    }

    props.addProperty(new PropertyStringDataImpl(id, value));
  }

  private void addPropertyInteger(PropertiesDataImpl props, String typeId, Set<String> filter,
      String id, long value) {
    if (!checkAddProperty(props, typeId, filter, id)) {
      return;
    }

    props.addProperty(new PropertyIntegerDataImpl(id, BigInteger.valueOf(value)));
  }

  private void addPropertyBoolean(PropertiesDataImpl props, String typeId, Set<String> filter,
      String id, boolean value) {
    if (!checkAddProperty(props, typeId, filter, id)) {
      return;
    }

    props.addProperty(new PropertyBooleanDataImpl(id, value));
  }

  private void addPropertyDateTime(PropertiesDataImpl props, String typeId, Set<String> filter,
      String id, GregorianCalendar value) {
    if (!checkAddProperty(props, typeId, filter, id)) {
      return;
    }

    props.addProperty(new PropertyDateTimeDataImpl(id, value));
  }

  private boolean checkAddProperty(PropertiesData properties, String typeId, Set<String> filter,
      String id) {
    if ((properties == null) || (properties.getProperties() == null)) {
      throw new IllegalArgumentException("Properties must not be null!");
    }

    if (id == null) {
      throw new IllegalArgumentException("Id must not be null!");
    }

    TypeDefinition type = fTypes.getType(typeId);
    if (type == null) {
      throw new IllegalArgumentException("Unknown type: " + typeId);
    }
    if (!type.getPropertyDefinitions().containsKey(id)) {
      throw new IllegalArgumentException("Unknown property: " + id);
    }

    String queryName = type.getPropertyDefinitions().get(id).getQueryName();

    if ((queryName != null) && (filter != null)) {
      if (!filter.contains(queryName)) {
        return false;
      }
      else {
        filter.remove(queryName);
      }
    }

    return true;
  }

  /**
   * Adds the default value of property if defined.
   */
  @SuppressWarnings("unchecked")
  private boolean addPropertyDefault(PropertiesDataImpl props, PropertyDefinition<?> propDef) {
    if ((props == null) || (props.getProperties() == null)) {
      throw new IllegalArgumentException("Props must not be null!");
    }

    if (propDef == null) {
      return false;
    }

    List<?> defaultValue = propDef.getDefaultValue();
    if ((defaultValue != null) && (!defaultValue.isEmpty())) {
      switch (propDef.getPropertyType()) {
      case BOOLEAN:
        props
            .addProperty(new PropertyBooleanDataImpl(propDef.getId(), (List<Boolean>) defaultValue));
        break;
      case DATETIME:
        props.addProperty(new PropertyDateTimeDataImpl(propDef.getId(),
            (List<GregorianCalendar>) defaultValue));
        break;
      case DECIMAL:
        props.addProperty(new PropertyDecimalDataImpl(propDef.getId(),
            (List<BigDecimal>) defaultValue));
        break;
      case HTML:
        props.addProperty(new PropertyHtmlDataImpl(propDef.getId(), (List<String>) defaultValue));
        break;
      case ID:
        props.addProperty(new PropertyIdDataImpl(propDef.getId(), (List<String>) defaultValue));
        break;
      case INTEGER:
        props.addProperty(new PropertyIntegerDataImpl(propDef.getId(),
            (List<BigInteger>) defaultValue));
        break;
      case STRING:
        props.addProperty(new PropertyStringDataImpl(propDef.getId(), (List<String>) defaultValue));
        break;
      case URI:
        props.addProperty(new PropertyUriDataImpl(propDef.getId(), (List<String>) defaultValue));
        break;
      default:
        throw new RuntimeException("Unknown datatype! Spec change?");
      }

      return true;
    }

    return false;
  }

  /**
   * Compiles the allowable actions for a file or folder.
   */
  private AllowableActionsData compileAllowableActions(File file, boolean userReadOnly) {
    if (file == null) {
      throw new IllegalArgumentException("File must not be null!");
    }

    // we can gather properties if the file or folder doesn't exist
    if (!file.exists()) {
      throw new CmisObjectNotFoundException("Object not found!");
    }

    boolean isReadOnly = !file.canWrite();
    boolean isFolder = file.isDirectory();
    boolean isRoot = fRoot.equals(file);

    Map<String, Boolean> aam = new HashMap<String, Boolean>();

    aam.put(AllowableActionsData.ACTION_CAN_GET_OBJECT_PARENTS, !isRoot);
    aam.put(AllowableActionsData.ACTION_CAN_CREATE_RELATIONSHIP, false);
    aam.put(AllowableActionsData.ACTION_CAN_GET_PROPERTIES, true);
    aam.put(AllowableActionsData.ACTION_CAN_GET_RENDITIONS, false);
    aam.put(AllowableActionsData.ACTION_CAN_UPDATE_PROPERTIES, !userReadOnly && !isReadOnly);
    aam.put(AllowableActionsData.ACTION_CAN_MOVE_OBJECT, !userReadOnly);
    aam.put(AllowableActionsData.ACTION_CAN_DELETE_OBJECT, !userReadOnly && !isReadOnly);
    aam.put(AllowableActionsData.ACTION_CAN_GET_OBJECT_RELATIONSHIPS, false);
    aam.put(AllowableActionsData.ACTION_CAN_APPLY_POLICY, false);
    aam.put(AllowableActionsData.ACTION_CAN_REMOVE_POLICY, false);
    aam.put(AllowableActionsData.ACTION_CAN_GET_ACL, true);
    aam.put(AllowableActionsData.ACTION_CAN_APPLY_ACL, false);

    if (isFolder) {
      aam.put(AllowableActionsData.ACTION_CAN_GET_DESCENDANTS, true);
      aam.put(AllowableActionsData.ACTION_CAN_GET_CHILDREN, true);
      aam.put(AllowableActionsData.ACTION_CAN_GET_FOLDER_PARENT, !isRoot);
      aam.put(AllowableActionsData.ACTION_CAN_GET_FOLDER_TREE, true);
      aam.put(AllowableActionsData.ACTION_CAN_CREATE_DOCUMENT, !userReadOnly);
      aam.put(AllowableActionsData.ACTION_CAN_CREATE_FOLDER, !userReadOnly);
      aam.put(AllowableActionsData.ACTION_CAN_CREATE_POLICY, false);
      aam.put(AllowableActionsData.ACTION_CAN_DELETE_TREE, !userReadOnly && !isReadOnly);
    }
    else {
      aam.put(AllowableActionsData.ACTION_CAN_GET_CONTENT_STREAM, true);
      aam.put(AllowableActionsData.ACTION_CAN_SET_CONTENT_STREAM, !userReadOnly && !isReadOnly);
      aam.put(AllowableActionsData.ACTION_CAN_DELETE_CONTENT_STREAM, !userReadOnly && !isReadOnly);
      aam.put(AllowableActionsData.ACTION_CAN_ADD_OBJECT_TO_FOLDER, false);
      aam.put(AllowableActionsData.ACTION_CAN_REMOVE_OBJECT_FROM_FOLDER, false);
      aam.put(AllowableActionsData.ACTION_CAN_CHECK_OUT, false);
      aam.put(AllowableActionsData.ACTION_CAN_CANCEL_CHECK_OUT, false);
      aam.put(AllowableActionsData.ACTION_CAN_CHECK_IN, false);
      aam.put(AllowableActionsData.ACTION_CAN_GET_ALL_VERSIONS, true);
    }

    AllowableActionsDataImpl result = new AllowableActionsDataImpl();
    result.setAllowableActions(aam);

    return result;
  }

  /**
   * Compiles the ACL for a file or folder.
   */
  private AccessControlList compileAcl(File file) {
    AccessControlListImpl result = new AccessControlListImpl();
    result.setAces(new ArrayList<AccessControlEntry>());

    for (Map.Entry<String, Boolean> ue : fUserMap.entrySet()) {
      // create principal
      AccessControlPrincipalDataImpl principal = new AccessControlPrincipalDataImpl();
      principal.setPrincipalId(ue.getKey());

      // create ACE
      AccessControlEntryImpl entry = new AccessControlEntryImpl();
      entry.setPrincipal(principal);
      entry.setPermissions(new ArrayList<String>());
      entry.getPermissions().add(CMIS_READ);
      if (!ue.getValue().booleanValue() && file.canWrite()) {
        entry.getPermissions().add(CMIS_WRITE);
        entry.getPermissions().add(CMIS_ALL);
      }

      entry.setDirect(true);

      // add ACE
      result.getAces().add(entry);
    }

    return result;
  }

  /**
   * Writes the properties for a document or folder.
   */
  private void writePropertiesFile(File file, PropertiesData properties) {
    File propFile = getPropertiesFile(file);

    // if no properties set delete the properties file
    if ((properties == null) || (properties.getProperties() == null)
        || (properties.getProperties().size() == 0)) {
      propFile.delete();
      return;
    }

    // create object
    CmisObjectType object = new CmisObjectType();
    object.setProperties(Converter.convert(properties));

    // write it
    try {
      JaxBHelper.CMIS_EXTRA_OBJECT_FACTORY.createObject(object);
      JAXBElement<CmisObjectType> objElement = JaxBHelper.CMIS_EXTRA_OBJECT_FACTORY
          .createObject(object);

      Marshaller m = JaxBHelper.createMarshaller();
      m.setProperty("jaxb.formatted.output", true);
      m.marshal(objElement, propFile);
    }
    catch (Exception e) {
      throw new CmisStorageException("Couldn't store properties!", e);
    }
  }

  // --- internal stuff ---

  /**
   * Converts milliseconds into a calendar object.
   */
  private GregorianCalendar millisToCalendar(long millis) {
    GregorianCalendar result = new GregorianCalendar();
    result.setTimeZone(TimeZone.getTimeZone("GMT"));
    result.setTimeInMillis(millis);

    return result;
  }

  /**
   * Splits a filter statement into a collection of properties. If <code>filter</code> is
   * <code>null</code>, empty or one of the properties is '*' , an empty collection will be
   * returned.
   */
  private Set<String> splitFilter(String filter) {
    if (filter == null) {
      return null;
    }

    if (filter.trim().length() == 0) {
      return null;
    }

    Set<String> result = new HashSet<String>();
    for (String s : filter.split(",")) {
      s = s.trim();
      if (s.equals("*")) {
        return null;
      }
      else if (s.length() > 0) {
        result.add(s);
      }
    }

    // set a few base properties
    // query name == id (for base type properties)
    result.add(PropertyIds.CMIS_OBJECT_ID);
    result.add(PropertyIds.CMIS_OBJECT_TYPE_ID);
    result.add(PropertyIds.CMIS_BASE_TYPE_ID);

    return result;
  }

  /**
   * Gets the type id from a set of properties.
   */
  private String getTypeId(PropertiesData properties) {
    PropertyData<?> typeProperty = properties.getProperties().get(PropertyIds.CMIS_OBJECT_TYPE_ID);
    if (!(typeProperty instanceof PropertyIdData)) {
      throw new CmisInvalidArgumentException("Type id must be set!");
    }

    String typeId = ((PropertyIdData) typeProperty).getFirstValue();
    if (typeId == null) {
      throw new CmisInvalidArgumentException("Type id must be set!");
    }

    return typeId;
  }

  /**
   * Returns the first value of an id property.
   */
  private String getIdProperty(PropertiesData properties, String name) {
    PropertyData<?> property = properties.getProperties().get(name);
    if (!(property instanceof PropertyIdData)) {
      return null;
    }

    return ((PropertyIdData) property).getFirstValue();
  }

  /**
   * Returns the first value of an string property.
   */
  private String getStringProperty(PropertiesData properties, String name) {
    PropertyData<?> property = properties.getProperties().get(name);
    if (!(property instanceof PropertyStringData)) {
      return null;
    }

    return ((PropertyStringData) property).getFirstValue();
  }

  /**
   * Returns the first value of an datetime property.
   */
  private GregorianCalendar getDateTimeProperty(PropertiesData properties, String name) {
    PropertyData<?> property = properties.getProperties().get(name);
    if (!(property instanceof PropertyDateTimeData)) {
      return null;
    }

    return ((PropertyDateTimeData) property).getFirstValue();
  }

  /**
   * Checks if the user in the given context is valid for this repository and if the user has the
   * required permissions.
   */
  private boolean checkUser(CallContext context, boolean writeRequired) {
    if (context == null) {
      throw new CmisPermissionDeniedException("No user context!");
    }

    Boolean readOnly = fUserMap.get(context.getUsername());
    if (readOnly == null) {
      throw new CmisPermissionDeniedException("Unknown user!");
    }

    if (readOnly.booleanValue() && writeRequired) {
      throw new CmisPermissionDeniedException("No write permission!");
    }

    return readOnly.booleanValue();
  }

  /**
   * Returns the properties file of the given file.
   */
  private File getPropertiesFile(File file) {
    if (file.isDirectory()) {
      return new File(file, SHADOW_FOLDER);
    }

    return new File(file.getAbsolutePath() + SHADOW_EXT);
  }

  /**
   * Returns the File object by id or throws an appropriate exception.
   */
  private File getFile(String id) {
    try {
      return idToFile(id);
    }
    catch (Exception e) {
      throw new CmisObjectNotFoundException(e.getMessage(), e);
    }
  }

  /**
   * Converts an id to a File object. A simple and insecure implementation, but good enough for now.
   */
  private File idToFile(String id) throws Exception {
    if ((id == null) || (id.length() == 0)) {
      throw new CmisInvalidArgumentException("Id is not valid!");
    }

    if (id.equals(ROOT_ID)) {
      return fRoot;
    }

    return new File(fRoot, (new String(Base64.decodeBase64(id.getBytes("ISO-8859-1")), "UTF-8"))
        .replace('/', File.separatorChar));
  }

  /**
   * Returns the id of a File object or throws an appropriate exception.
   */
  private String getId(File file) {
    try {
      return fileToId(file);
    }
    catch (Exception e) {
      throw new CmisRuntimeException(e.getMessage());
    }
  }

  /**
   * Creates a File object from an id. A simple and insecure implementation, but good enough for
   * now.
   */
  private String fileToId(File file) throws Exception {
    if (file == null) {
      throw new IllegalArgumentException("File is not valid!");
    }

    if (fRoot.equals(file)) {
      return ROOT_ID;
    }

    String path = getRepositoryPath(file);

    return new String(Base64.encodeBase64(path.getBytes("UTF-8")), "ISO-8859-1");
  }

  private String getRepositoryPath(File file) {
    return file.getAbsolutePath().substring(fRoot.getAbsolutePath().length()).replace(
        File.separatorChar, '/');
  }

  private void warn(String msg, Throwable t) {
    log.warn("<" + fRepositoryId + "> " + msg, t);
  }

  private void debug(String msg) {
    debug(msg, null);
  }

  private void debug(String msg, Throwable t) {
    log.debug("<" + fRepositoryId + "> " + msg, t);
  }
}
