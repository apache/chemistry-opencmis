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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.api.DocumentTypeDefinition;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.Updatability;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertiesDataImpl;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.AllowableActionsData;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.FailedToDeleteData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectService;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.RenditionData;
import org.apache.opencmis.inmemory.server.RuntimeContext;
import org.apache.opencmis.inmemory.storedobj.api.Content;
import org.apache.opencmis.inmemory.storedobj.api.Document;
import org.apache.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.opencmis.inmemory.storedobj.api.Folder;
import org.apache.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.opencmis.inmemory.storedobj.api.Filing;
import org.apache.opencmis.inmemory.storedobj.api.SingleFiling;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.opencmis.inmemory.storedobj.api.VersionedDocument;
import org.apache.opencmis.inmemory.types.InMemoryDocumentTypeDefinition;
import org.apache.opencmis.inmemory.types.InMemoryFolderTypeDefinition;
import org.apache.opencmis.inmemory.types.PropertyCreationHelper;
import org.apache.opencmis.server.spi.CallContext;

public class ObjectServiceImpl extends AbstractServiceImpl implements ObjectService {
  private static Log log = LogFactory.getLog(ObjectService.class);

  public ObjectServiceImpl(StoreManager storeManager) {
    super(storeManager);
    fStoreManager = storeManager;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.ObjectService#createDocument(java.lang.String,
   * org.opencmis.client.provider.PropertiesData, java.lang.String,
   * org.opencmis.client.provider.ContentStreamData,
   * org.opencmis.commons.enums.VersioningState, java.util.List,
   * org.opencmis.client.provider.AccessControlList,
   * org.opencmis.client.provider.AccessControlList,
   * org.opencmis.client.provider.ExtensionsData)
   */
  public String createDocument(String repositoryId, PropertiesData properties, String folderId,
      ContentStreamData contentStream, VersioningState versioningState, List<String> policies,
      AccessControlList addACEs, AccessControlList removeACEs, ExtensionsData extension) {

    log.debug("start createDocument()");
    checkRepositoryId(repositoryId);

    ObjectStore folderStore = fStoreManager.getObjectStore(repositoryId);

    // get name from properties
    PropertyData<?> pd = properties.getProperties().get(PropertyIds.CMIS_NAME);
    String name = (String) pd.getFirstValue();

    // Validation stuff
    TypeValidator.validateRequiredSystemProperties(properties);
    TypeDefinition typeDef = getTypeDefinition(repositoryId, properties);

    Folder folder = null;
    if (null != folderId) {
      StoredObject so = folderStore.getObjectById(folderId);
  
      if (null == so)
        throw new CmisInvalidArgumentException(" Cannot create document, folderId: " + folderId
            + " is invalid");
  
      if (so instanceof Folder)
        folder = (Folder) so;
      else
        throw new CmisInvalidArgumentException(
            "Can't creat document, folderId does not refer to a folder: " + folderId);
      
      TypeValidator.validateAllowedChildObjectTypes(typeDef, folder.getAllowedChildObjectTypeIds());
    }
    

    // check if the given type is a document type
    if (!typeDef.getBaseId().equals(
        InMemoryDocumentTypeDefinition.getRootDocumentType().getBaseId()))
      throw new RuntimeException("Cannot create a document, with a non-document type: " + typeDef.getId());

    TypeValidator.validateVersionStateForCreate((DocumentTypeDefinition) typeDef, versioningState);
    TypeValidator.validateProperties(typeDef, properties, true);

    // set user, creation date, etc.
    String user = RuntimeContext.getRuntimeConfigValue(CallContext.USERNAME);
    if (user == null)
      user = "unknown";
    
    String resId = null;
    
    // Now we are sure to have document type definition:
    if (((DocumentTypeDefinition)typeDef).isVersionable()) {
      VersionedDocument verDoc = fStoreManager.getObjectStore(repositoryId).createVersionedDocument(name);
      verDoc.createSystemBasePropertiesWhenCreated(properties.getProperties(), user);
      verDoc.setCustomProperties(properties.getProperties());
      DocumentVersion version = verDoc.addVersion(contentStream, versioningState, user);
      if (null != folder)
        folder.addChildDocument(verDoc); // add document to folder and set parent in doc
      else
        verDoc.persist();      
      version.createSystemBasePropertiesWhenCreated(properties.getProperties(), user);
      version.setCustomProperties(properties.getProperties());
      version.persist();
      resId = version.getId(); // return the version and not the version series to caller
    } else {
      Document doc = fStoreManager.getObjectStore(repositoryId).createDocument(name);
      doc.setContent(contentStream, false);
      // add document to folder
      doc.createSystemBasePropertiesWhenCreated(properties.getProperties(), user);
      doc.setCustomProperties(properties.getProperties());
      if (null != folder)
        folder.addChildDocument(doc); // add document to folder and set parent in doc
      else
        doc.persist();
      resId = doc.getId();
    }
        
    // versioningState, policies, addACEs, removeACEs, extension are ignored for
    // now.
    log.debug("stop createDocument()");
    return resId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.ObjectService#createDocumentFromSource(java
   * .lang.String, java.lang.String,
   * org.opencmis.client.provider.PropertiesData, java.lang.String,
   * org.opencmis.commons.enums.VersioningState, java.util.List,
   * org.opencmis.client.provider.AccessControlList,
   * org.opencmis.client.provider.AccessControlList,
   * org.opencmis.client.provider.ExtensionsData)
   */
  public String createDocumentFromSource(String repositoryId, String sourceId,
      PropertiesData properties, String folderId, VersioningState versioningState,
      List<String> policies, AccessControlList addACEs, AccessControlList removeACEs,
      ExtensionsData extension) {

    log.debug("start createDocumentFromSource()");

    checkStandardParameters(repositoryId, sourceId);
    ObjectStore folderStore = fStoreManager.getObjectStore(repositoryId);
    checkExistingObjectId(folderStore, sourceId);

    ContentStreamData content = getContentStream(repositoryId, sourceId, null, BigInteger
        .valueOf(-1), BigInteger.valueOf(-1), null);

    // get all properties of existing document
    StoredObject so = folderStore.getObjectById(sourceId);

    if (so == null)
      throw new CmisObjectNotFoundException("Unknown object id: " + sourceId);

    // build properties collection
    List<String> requestedIds = FilterParser.getRequestedIdsFromFilter("*");

    PropertiesData existingProps = PropertyCreationHelper.getPropertiesFromObject(repositoryId, so,
        fStoreManager, requestedIds);

    PropertiesDataImpl newPD = new PropertiesDataImpl();
    // copy all existing properties
    for (PropertyData<?> prop : existingProps.getProperties().values()) {
      newPD.addProperty(prop);
    }
    // overwrite all new properties
    for (PropertyData<?> prop : properties.getProperties().values()) {
      newPD.addProperty(prop);
    }

    String res = createDocument(repositoryId, newPD, folderId, content, versioningState, policies,
        addACEs, removeACEs, null);
    log.debug("stop createDocumentFromSource()");
    return res;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.ObjectService#createFolder(java.lang.String,
   * org.opencmis.client.provider.PropertiesData, java.lang.String,
   * java.util.List, org.opencmis.client.provider.AccessControlList,
   * org.opencmis.client.provider.AccessControlList,
   * org.opencmis.client.provider.ExtensionsData)
   */
  public String createFolder(String repositoryId, PropertiesData properties, String folderId,
      List<String> policies, AccessControlList addACEs, AccessControlList removeACEs,
      ExtensionsData extension) {
    
    log.debug("start createFolder()");
    
    checkStandardParameters(repositoryId, folderId);

    ObjectStore fs = fStoreManager.getObjectStore(repositoryId);
    StoredObject so = null;
    Folder parent = null;

    // get required properties
    PropertyData<?> pd = properties.getProperties().get(PropertyIds.CMIS_NAME);
    String folderName = (String) pd.getFirstValue();
    if (null == folderName || folderName.length() == 0)
      throw new CmisInvalidArgumentException("Cannot create a folder without a name.");

    TypeValidator.validateRequiredSystemProperties(properties);

    TypeDefinition typeDef = getTypeDefinition(repositoryId, properties);

        // check if the given type is a folder type
    if (!typeDef.getBaseId().equals(
        InMemoryFolderTypeDefinition.getRootFolderType().getBaseId()))
      throw new RuntimeException("Cannot create a folder, with a non-folder type: " + typeDef.getId());

    TypeValidator.validateProperties(typeDef, properties, true);

    // create folder
    try {
      log.info("get folder for id: " + folderId);
      so = fs.getObjectById(folderId);
    } catch (Exception e) {
      throw new CmisObjectNotFoundException("Failed to retrieve folder.", e);
    }

    if (so instanceof Folder)
      parent = (Folder) so;
    else
      throw new CmisInvalidArgumentException(
          "Can't create folder, folderId does not refer to a folder: " + folderId);
    try {
      ObjectStore objStore = fStoreManager.getObjectStore(repositoryId);
      Folder newFolder = objStore.createFolder(folderName);
      // set default system attributes
      String user = RuntimeContext.getRuntimeConfigValue(CallContext.USERNAME);
      if (user == null)
        user = "unknown";
      newFolder.createSystemBasePropertiesWhenCreated(properties.getProperties(), user);
      newFolder.setCustomProperties(properties.getProperties());
      parent.addChildFolder(newFolder);
      log.debug("stop createFolder()");
      return newFolder.getId();
    } catch (Exception e) {
      throw new CmisInvalidArgumentException("Failed to create child folder.", e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.ObjectService#createPolicy(java.lang.String,
   * org.opencmis.client.provider.PropertiesData, java.lang.String,
   * java.util.List, org.opencmis.client.provider.AccessControlList,
   * org.opencmis.client.provider.AccessControlList,
   * org.opencmis.client.provider.ExtensionsData)
   */
  public String createPolicy(String repositoryId, PropertiesData properties, String folderId,
      List<String> policies, AccessControlList addACEs, AccessControlList removeACEs,
      ExtensionsData extension) {
    // TODO to be completed if ACLs are implemented
    log.debug("start createPolicy()");
    checkStandardParameters(repositoryId, folderId);
    log.debug("stop createPolicy()");
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.ObjectService#createRelationship(java.lang
   * .String, org.opencmis.client.provider.PropertiesData, java.util.List,
   * org.opencmis.client.provider.AccessControlList,
   * org.opencmis.client.provider.AccessControlList,
   * org.opencmis.client.provider.ExtensionsData)
   */
  public String createRelationship(String repositoryId, PropertiesData properties,
      List<String> policies, AccessControlList addACEs, AccessControlList removeACEs,
      ExtensionsData extension) {
    // TODO to be completed if relationships are implemented
    log.debug("start createRelationship()");
    checkRepositoryId(repositoryId);
    log.debug("stop createRelationship()");
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.ObjectService#deleteContentStream(java.lang
   * .String, org.opencmis.client.provider.Holder,
   * org.opencmis.client.provider.Holder,
   * org.opencmis.client.provider.ExtensionsData)
   */
  public void deleteContentStream(String repositoryId, Holder<String> objectId,
      Holder<String> changeToken, ExtensionsData extension) {
    log.debug("start deleteContentStream()");
    checkStandardParameters(repositoryId, objectId.getValue());
    
    ObjectStore folderStore = fStoreManager.getObjectStore(repositoryId);
    StoredObject so = folderStore.getObjectById(objectId.getValue());

    if (so == null)
      throw new CmisObjectNotFoundException("Unknown object id: " + objectId);

    if (!(so instanceof Content))
      throw new CmisObjectNotFoundException("Id" + objectId
          + " does not refer to a document, but only documents can have content");

    ((Content) so).setContent(null, true);
    log.debug("stop deleteContentStream()");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.ObjectService#deleteObject(java.lang.String,
   * java.lang.String, java.lang.Boolean,
   * org.opencmis.client.provider.ExtensionsData)
   */
  public void deleteObject(String repositoryId, String objectId, Boolean allVersions,
      ExtensionsData extension) {

    log.debug("start deleteObject()");
    checkStandardParameters(repositoryId, objectId);
    ObjectStore fs = fStoreManager.getObjectStore(repositoryId);
    log.info("delete object for id: " + objectId);

    // check if it is the root folder
    ObjectStore folderStore = fStoreManager.getObjectStore(repositoryId);
    if (objectId.equals(folderStore.getRootFolder().getId()))
      throw new CmisNotSupportedException("You can't delete a root folder");

    fs.deleteObject(objectId);
    log.debug("stop deleteObject()");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.ObjectService#deleteTree(java.lang.String,
   * java.lang.String, java.lang.Boolean,
   * org.opencmis.commons.enums.UnfileObject, java.lang.Boolean,
   * org.opencmis.client.provider.ExtensionsData)
   */
  public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
      UnfileObjects unfileObject, Boolean continueOnFailure, ExtensionsData extension) {
    log.debug("start deleteTree()");
    checkStandardParameters(repositoryId, folderId);
    List<String> failedToDeleteIds = new ArrayList<String>();
    FailedToDeleteDataImpl result = new FailedToDeleteDataImpl();

    if (null == allVersions)
      allVersions = true;
    if (null == unfileObject)
      unfileObject = UnfileObjects.DELETE;
    if (null == continueOnFailure)
      continueOnFailure = false;
    
    
    ObjectStore folderStore = fStoreManager.getObjectStore(repositoryId);
    StoredObject so = folderStore.getObjectById(folderId);

    if (null == so)
      throw new RuntimeException("Cannot delete object with id  " + folderId
          + ". Object does not exist.");

    if (!(so instanceof Folder))
      throw new RuntimeException("deleteTree can only be invoked on a folder, but id " + folderId
          + " does not refer to a folder");

    if (unfileObject == UnfileObjects.UNFILE)
      throw new CmisNotSupportedException("This repository does not support unfile operations.");

    // check if it is the root folder
    if (folderId.equals(folderStore.getRootFolder().getId()))
      throw new CmisNotSupportedException("You can't delete a root folder");

    // recursively delete folder
    deleteRecursive(folderStore, (Folder) so, continueOnFailure, allVersions, failedToDeleteIds);

    result.setIds(failedToDeleteIds);
    log.debug("stop deleteTree()");
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.ObjectService#getAllowableActions(java.lang
   * .String, java.lang.String, org.opencmis.client.provider.ExtensionsData)
   */
  public AllowableActionsData getAllowableActions(String repositoryId, String objectId,
      ExtensionsData extension) {
    log.debug("start getAllowableActions()");
    checkStandardParameters(repositoryId, objectId);
    ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);
    StoredObject so = objectStore.getObjectById(objectId);

    if (so == null)
      throw new CmisObjectNotFoundException("Unknown object id: " + objectId);

    AllowableActionsData allowableActions = DataObjectCreator.fillAllowableActions(objectStore, so);
    log.debug("stop getAllowableActions()");
    return allowableActions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.ObjectService#getContentStream(java.lang.String
   * , java.lang.String, java.lang.String, java.math.BigInteger,
   * java.math.BigInteger, org.opencmis.client.provider.ExtensionsData)
   */
  public ContentStreamData getContentStream(String repositoryId, String objectId, String streamId,
      BigInteger offset, BigInteger length, ExtensionsData extension) {

    log.debug("start getContentStream()");
    checkStandardParameters(repositoryId, objectId);
    ObjectStore folderStore = fStoreManager.getObjectStore(repositoryId);
    StoredObject so = folderStore.getObjectById(objectId);

    if (so == null)
      throw new CmisObjectNotFoundException("Unknown object id: " + objectId);

    if (!(so instanceof Content))
      throw new CmisObjectNotFoundException("Id" + objectId
          + " does not refer to a document or version, but only those can have content");

    ContentStreamData csd = getContentStream(so, streamId, offset, length);
    log.debug("stop getContentStream()");
    return csd;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.opencmis.client.provider.ObjectService#getObject(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.Boolean,
   * org.opencmis.commons.enums.IncludeRelationships, java.lang.String,
   * java.lang.Boolean, java.lang.Boolean,
   * org.opencmis.client.provider.ExtensionsData)
   */
  public ObjectData getObject(String repositoryId, String objectId, String filter,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeACL, ExtensionsData extension) {

    log.debug("start getObject()");
    checkStandardParameters(repositoryId, objectId);
    ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);
    StoredObject so = objectStore.getObjectById(objectId);

    if (so == null)
      throw new CmisObjectNotFoundException("Unknown object id: " + objectId);

    ObjectData od = PropertyCreationHelper.getObjectData(fStoreManager, so, filter,
        includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds,
        includeACL, extension);
    
    log.debug("stop getObject()");
    return od;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.ObjectService#getObjectByPath(java.lang.String
   * , java.lang.String, java.lang.String, java.lang.Boolean,
   * org.opencmis.commons.enums.IncludeRelationships, java.lang.String,
   * java.lang.Boolean, java.lang.Boolean,
   * org.opencmis.client.provider.ExtensionsData)
   */
  public ObjectData getObjectByPath(String repositoryId, String path, String filter,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeACL, ExtensionsData extension) {
    log.debug("start getObjectByPath()");
    checkRepositoryId(repositoryId);
    ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);
    StoredObject so = objectStore.getObjectByPath(path);

    if (so == null)
      throw new CmisObjectNotFoundException("Unknown path: " + path);

    ObjectData od = PropertyCreationHelper.getObjectData(fStoreManager, so, filter,
        includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds,
        includeACL, extension);
    
    log.debug("stop getObjectByPath()");
    return od;
  }

  public PropertiesData getProperties(String repositoryId, String objectId, String filter,
      ExtensionsData extension) {

    log.debug("start getProperties()");
    checkStandardParameters(repositoryId, objectId);

    ObjectStore folderStore = fStoreManager.getObjectStore(repositoryId);
    StoredObject so = folderStore.getObjectById(objectId);

    if (so == null)
      throw new CmisObjectNotFoundException("Unknown object id: " + objectId);

    // build properties collection
    List<String> requestedIds = FilterParser.getRequestedIdsFromFilter(filter);
    PropertiesData props = PropertyCreationHelper.getPropertiesFromObject(repositoryId, so,
        fStoreManager, requestedIds);
    log.debug("stop getProperties()");
    return props;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.ObjectService#getRenditions(java.lang.String,
   * java.lang.String, java.lang.String, java.math.BigInteger,
   * java.math.BigInteger, org.opencmis.client.provider.ExtensionsData)
   */
  public List<RenditionData> getRenditions(String repositoryId, String objectId,
      String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
    // TODO to be completed if renditions are implemented
    log.debug("start getRenditions()");
    checkStandardParameters(repositoryId, objectId);
    log.debug("stop getRenditions()");
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.ObjectService#moveObject(java.lang.String,
   * org.opencmis.client.provider.Holder, java.lang.String, java.lang.String,
   * org.opencmis.client.provider.ExtensionsData)
   */
  public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId,
      String sourceFolderId, ExtensionsData extension) {
    log.debug("start moveObject()");
    checkStandardParameters(repositoryId, objectId.getValue());
    Folder targetFolder = null;
    Folder sourceFolder = null;
    ObjectStore folderStore = fStoreManager.getObjectStore(repositoryId);
    StoredObject so = folderStore.getObjectById(objectId.getValue());
    Filing spo = null;

    if (null == so)
      throw new CmisObjectNotFoundException("Unknown object: " + objectId.getValue());
    else if (so instanceof Filing)
      spo = (Filing) so;
    else
      throw new CmisInvalidArgumentException("Object must be folder or document: "
          + objectId.getValue());

    StoredObject soTarget = folderStore.getObjectById(targetFolderId);
    if (null == soTarget)
      throw new CmisObjectNotFoundException("Unknown target folder: " + targetFolderId);
    else if (soTarget instanceof Folder)
      targetFolder = (Folder) soTarget;
    else
      throw new CmisNotSupportedException("Destination " + targetFolderId + " of a move operation must be a folder");

    StoredObject soSource = folderStore.getObjectById(sourceFolderId);
    if (null == soSource)
      throw new CmisObjectNotFoundException("Unknown source folder: " + sourceFolderId);
    else if (soSource instanceof Folder)
      sourceFolder = (Folder) soSource;
    else
      throw new CmisNotSupportedException("Source " + sourceFolderId + " of a move operation must be a folder");

    boolean foundOldParent = false;
    for (Folder parent: spo.getParents()) {
      if (parent.getId().equals(soSource.getId())) {
        foundOldParent = true;
        break;
      }
    }
    if (!foundOldParent)
      throw new CmisNotSupportedException("Cannot move object, source folder " + sourceFolderId + "is not a parent of object " + objectId.getValue());
    
    if (so instanceof Folder && hasDescendant((Folder) so, targetFolder)) {
      throw new CmisNotSupportedException(
          "Destination of a move cannot be a subfolder of the source");
    }
    
    spo.move(sourceFolder, targetFolder);
    objectId.setValue(so.getId());
    log.debug("stop moveObject()");
  }

  private boolean hasDescendant(Folder sourceFolder, Folder targetFolder) {
    String sourceId = sourceFolder.getId();
    String targetId = targetFolder.getId();
    while (targetId != null) {
      // log.info("comparing source id " + sourceId + " with predecessor " +
      // targetId);
      if (targetId.equals(sourceId))
        return true;
      targetFolder = targetFolder.getParent();
      if (null != targetFolder)
        targetId = targetFolder.getId();
      else
        targetId = null;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.ObjectService#setContentStream(java.lang.String
   * , org.opencmis.client.provider.Holder, java.lang.Boolean,
   * org.opencmis.client.provider.Holder,
   * org.opencmis.client.provider.ContentStreamData,
   * org.opencmis.client.provider.ExtensionsData)
   */
  public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
      Holder<String> changeToken, ContentStreamData contentStream, ExtensionsData extension) {
    
    log.debug("start setContentStream()");
    checkStandardParameters(repositoryId, objectId.getValue());

    ObjectStore folderStore = fStoreManager.getObjectStore(repositoryId);
    StoredObject so = folderStore.getObjectById(objectId.getValue());
    Content content;

    if (!(so instanceof Document || so instanceof VersionedDocument || so instanceof DocumentVersion))
      throw new CmisObjectNotFoundException("Id" + objectId
          + " does not refer to a document, but only documents can have content");
    
    if (so instanceof Document) 
      content = ((Document) so);
    else if (so instanceof DocumentVersion) {
      // something that is versionable check the proper status of the object
      String user = RuntimeContext.getRuntimeConfigValue(CallContext.USERNAME);
      testHasProperCheckedOutStatus(so, user);
      content = (DocumentVersion) so;
    } else
      throw new IllegalArgumentException("Content cannot be set on this object (must be document or version)");

    if (!overwriteFlag && content.getContent(0, -1) != null)
      throw new CmisConstraintException(
          "cannot overwrite existing content if overwrite flag is not set");

    content.setContent(contentStream, true);
    log.debug("stop setContentStream()");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.ObjectService#updateProperties(java.lang.String
   * , org.opencmis.client.provider.Holder, org.opencmis.client.provider.Holder,
   * org.opencmis.client.provider.PropertiesData,
   * org.opencmis.client.provider.ExtensionsData)
   */
  public void updateProperties(String repositoryId, Holder<String> objectId,
      Holder<String> changeToken, PropertiesData properties, ExtensionsData extension) {

    log.debug("start updateProperties()");
    checkStandardParameters(repositoryId, objectId.getValue());
    
    ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);

    // Validation stuff
    StoredObject so = objectStore.getObjectById(objectId.getValue());

    TypeDefinition typeDef = getTypeDefinition(repositoryId, so);
    boolean isCheckedOut = false;
    
    // if the object is a versionable object it must be checked-out
    if (so instanceof VersionedDocument || so instanceof DocumentVersion) {
      String user = RuntimeContext.getRuntimeConfigValue(CallContext.USERNAME);
      //VersionedDocument verDoc = testIsNotCheckedOutBySomeoneElse(so, user); 
      testHasProperCheckedOutStatus(so, user);
      isCheckedOut = true;
    }

    Map<String, PropertyData<?>> oldProperties = so.getProperties();

    // check properties for validity
    TypeValidator.validateProperties(typeDef, properties, false);

    if (changeToken != null && changeToken.getValue() != null
        && Long.valueOf(so.getChangeToken()) > Long.valueOf(changeToken.getValue()))
      throw new CmisUpdateConflictException(" updateProperties failed: outdated changeToken");

    // update properties
    boolean hasUpdatedName = false;
    boolean hasUpdatedOtherProps = false;

    for (String key : properties.getProperties().keySet()) {
      if (key.equals(PropertyIds.CMIS_NAME))
        continue; // ignore here

      PropertyData<?> value = properties.getProperties().get(key);
      PropertyDefinition<?> propDef = typeDef.getPropertyDefinitions().get(key);
      if (value.getValues() == null || value.getFirstValue() == null) {
        // delete property
        // check if a required a property
        if (propDef.isRequired())
          throw new CmisConstraintException(
              "updateProperties failed, following property can't be deleted, because it is required: "
                  + key);
        oldProperties.remove(key);
        hasUpdatedOtherProps = true;
      } else {
        if (propDef.getUpdatability().equals(Updatability.WHENCHECKEDOUT) && !isCheckedOut)
          throw new CmisConstraintException(
              "updateProperties failed, following property can't be updated, because it is not checked-out: "
                  + key);
        else if (!propDef.getUpdatability().equals(Updatability.READWRITE) )
          throw new CmisConstraintException(
              "updateProperties failed, following property can't be updated, because it is not writable: "
                  + key);
        oldProperties.put(key, value);
        hasUpdatedOtherProps = true;
      }
    }

    // get name from properties and perform special rename to check if path
    // already exists
    PropertyData<?> pd = properties.getProperties().get(PropertyIds.CMIS_NAME);
    if (pd != null && so instanceof Filing) {
      String newName = (String) pd.getFirstValue();
      List<Folder> parents = ((Filing) so).getParents();
      if (so instanceof Folder && parents.isEmpty())
        throw new CmisConstraintException(
            "updateProperties failed, you cannot rename the root folder");
      for (Folder parent : parents) {
        if (parent.hasChild(newName))
          throw new CmisConstraintException(
              "updateProperties failed, cannot rename because path already exists.");
      }
      so.rename((String) pd.getFirstValue()); // note: this does persist
      hasUpdatedName = true;
    }

    if (hasUpdatedOtherProps) {
      // set user, creation date, etc.
      String user = RuntimeContext.getRuntimeConfigValue(CallContext.USERNAME);

      if (user == null)
        user = "unknown";
      so.updateSystemBasePropertiesWhenModified(properties.getProperties(), user);
      // set changeToken
      so.persist();
    }

    if (hasUpdatedName || hasUpdatedOtherProps) {
      objectId.setValue(so.getId()); // might have a new id
      if (null != changeToken) {
        String changeTokenVal = so.getChangeToken();
        log.info("updateProperties(), new change token is: " + changeTokenVal);
        changeToken.setValue(changeTokenVal);
      }
    }
    log.debug("stop updateProperties()");
  }

  // ///////////////////////////////////////////////////////
  // private helper methods

  /**
   * Recursively delete a tree by traversing it and first deleting all children
   * and then the object itself
   * 
   * @param folderStore
   * @param parentFolder
   * @param continueOnFailure
   * @param allVersions
   * @param failedToDeleteIds
   * @return returns true if operation should continue, false if it should stop
   */
  private boolean deleteRecursive(ObjectStore folderStore, Folder parentFolder,
      boolean continueOnFailure, boolean allVersions, List<String> failedToDeleteIds) {
    List<StoredObject> children = parentFolder.getChildren(-1, -1);

    if (null == children)
      return true;

    for (StoredObject child : children) {
      if (child instanceof Folder) {
        boolean mustContinue = deleteRecursive(folderStore, (Folder) child, continueOnFailure,
            allVersions, failedToDeleteIds);
        if (!mustContinue && !continueOnFailure)
          return false; // stop further deletions
      } else {
        try {
          folderStore.deleteObject(child.getId());
        } catch (Exception e) {
          failedToDeleteIds.add(child.getId());
        }
      }
    }
    folderStore.deleteObject(parentFolder.getId());
    return true;
  }

  private ContentStreamData getContentStream(StoredObject so, String streamId, BigInteger offset,
      BigInteger length) {

    long lOffset = offset == null ? 0 : offset.longValue();
    long lLength = length == null ? -1 : length.longValue();
    ContentStreamData csd = ((Content) so).getContent(lOffset, lLength);
    return csd;
  }

}
