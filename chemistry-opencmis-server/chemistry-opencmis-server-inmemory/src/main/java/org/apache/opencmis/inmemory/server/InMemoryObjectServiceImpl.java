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
package org.apache.opencmis.inmemory.server;

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
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
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
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.RenditionData;
import org.apache.opencmis.inmemory.DataObjectCreator;
import org.apache.opencmis.inmemory.FilterParser;
import org.apache.opencmis.inmemory.NameValidator;
import org.apache.opencmis.inmemory.TypeValidator;
import org.apache.opencmis.inmemory.storedobj.api.Content;
import org.apache.opencmis.inmemory.storedobj.api.Document;
import org.apache.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.opencmis.inmemory.storedobj.api.Filing;
import org.apache.opencmis.inmemory.storedobj.api.Folder;
import org.apache.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.opencmis.inmemory.storedobj.api.VersionedDocument;
import org.apache.opencmis.inmemory.types.InMemoryDocumentTypeDefinition;
import org.apache.opencmis.inmemory.types.InMemoryFolderTypeDefinition;
import org.apache.opencmis.inmemory.types.InMemoryPolicyTypeDefinition;
import org.apache.opencmis.inmemory.types.InMemoryRelationshipTypeDefinition;
import org.apache.opencmis.inmemory.types.PropertyCreationHelper;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisObjectService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

public class InMemoryObjectServiceImpl extends AbstractServiceImpl implements CmisObjectService {
  private static final Log LOG = LogFactory.getLog(ServiceFactory.class.getName());

  AtomLinkInfoProvider fAtomLinkProvider;

  public InMemoryObjectServiceImpl(StoreManager storeManager) {
    super(storeManager);
    fAtomLinkProvider = new AtomLinkInfoProvider(fStoreManager);
  }

  public String createDocument(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, ContentStreamData contentStream, VersioningState versioningState,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {

    try {
      LOG.debug("start createDocument()");
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      StoredObject so = createDocumentIntern(repositoryId, properties, folderId, contentStream,
          versioningState, policies, addAces, removeAces, extension);
      LOG.debug("stop createDocument()");
      return so.getId();
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public String createDocumentFromSource(CallContext context, String repositoryId, String sourceId,
      PropertiesData properties, String folderId, VersioningState versioningState,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      LOG.debug("start createDocumentFromSource()");

      StoredObject so = checkStandardParameters(repositoryId, sourceId);

      ContentStreamData content = getContentStream(context, repositoryId, sourceId, null,
          BigInteger.valueOf(-1), BigInteger.valueOf(-1), null);

      if (so == null)
        throw new CmisObjectNotFoundException("Unknown object id: " + sourceId);

      // build properties collection
      List<String> requestedIds = FilterParser.getRequestedIdsFromFilter("*");

      PropertiesData existingProps = PropertyCreationHelper.getPropertiesFromObject(repositoryId,
          so, fStoreManager, requestedIds);

      PropertiesDataImpl newPD = new PropertiesDataImpl();
      // copy all existing properties
      for (PropertyData<?> prop : existingProps.getProperties().values()) {
        newPD.addProperty(prop);
      }
      // overwrite all new properties
      for (PropertyData<?> prop : properties.getProperties().values()) {
        newPD.addProperty(prop);
      }

      String res = createDocument(context, repositoryId, newPD, folderId, content, versioningState,
          policies, addAces, removeAces, null);
      LOG.debug("stop createDocumentFromSource()");
      return res;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public String createFolder(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension) {
    try {
      LOG.debug("start createFolder()");
      RuntimeContext.attachCfg(context);

      Folder folder = createFolderIntern(repositoryId, properties, folderId, policies, addAces,
          removeAces, extension);
      LOG.debug("stop createFolder()");
      return folder.getId();
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public String createPolicy(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      // TODO to be completed if ACLs are implemented
      LOG.debug("start createPolicy()");
      checkStandardParameters(repositoryId, folderId);
      StoredObject so = createPolicyIntern(repositoryId, properties, folderId, policies, addAces,
          removeAces, extension);
      LOG.debug("stop createPolicy()");
      return so == null ? null : so.getId();
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public String createRelationship(CallContext context, String repositoryId,
      PropertiesData properties, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      // TODO to be completed if relationships are implemented
      LOG.debug("start createRelationship()");
      checkRepositoryId(repositoryId);
      StoredObject so = createRelationshipIntern(repositoryId, properties, policies, addAces,
          removeAces, extension);
      LOG.debug("stop createRelationship()");
      return so == null ? null : so.getId();
    }
    finally {
      RuntimeContext.remove();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.opencmis.server.spi.CmisObjectService#create(org.opencmis.server.spi.CallContext,
   * java.lang.String, org.opencmis.client.provider.PropertiesData, java.lang.String,
   * org.opencmis.client.provider.ContentStreamData, org.opencmis.commons.enums.VersioningState,
   * org.opencmis.client.provider.ExtensionsData, org.opencmis.server.spi.ObjectInfoHolder)
   * 
   * An additional create call compared to the ObjectService from the CMIS spec. This one is needed
   * because the Atom binding in the server implementation does not know what kind of object needs
   * to be created. Also the ObjectInfoHolder needs to be filled.
   */
  @SuppressWarnings("unchecked")
  public ObjectData create(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, ContentStreamData contentStream, VersioningState versioningState,
      List<String> policies, ExtensionsData extension, ObjectInfoHolder objectInfos) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      if (null == properties || null == properties.getProperties())
        throw new RuntimeException("Cannot create object, without properties.");

      // Find out what kind of object needs to be created
      PropertyData<String> pd = (PropertyData<String>) properties.getProperties().get(
          PropertyIds.CMIS_OBJECT_TYPE_ID);
      String typeId = pd == null ? null : pd.getFirstValue();
      if (null == typeId)
        throw new RuntimeException(
            "Cannot create object, without a type (no property with id CMIS_OBJECT_TYPE_ID).");

      TypeDefinitionContainer typeDefC = fStoreManager.getTypeById(repositoryId, typeId);
      if (typeDefC == null)
        throw new RuntimeException("Cannot create object, a type with id " + typeId + " is unknown");

      // check if the given type is a document type
      BaseObjectTypeIds typeBaseId = typeDefC.getTypeDefinition().getBaseId();
      StoredObject so = null;
      if (typeBaseId.equals(InMemoryDocumentTypeDefinition.getRootDocumentType().getBaseId())) {
        so = createDocumentIntern(repositoryId, properties, folderId, contentStream,
            versioningState, null, null, null, null);
      }
      else if (typeBaseId.equals(InMemoryFolderTypeDefinition.getRootFolderType().getBaseId())) {
        so = createFolderIntern(repositoryId, properties, folderId, null, null, null, null);
      }
      else if (typeBaseId.equals(InMemoryPolicyTypeDefinition.getRootPolicyType().getBaseId())) {
        so = createPolicyIntern(repositoryId, properties, folderId, null, null, null, null);
      }
      else if (typeBaseId.equals(InMemoryRelationshipTypeDefinition.getRootRelationshipType()
          .getBaseId())) {
        so = createRelationshipIntern(repositoryId, properties, null, null, null, null);
      }
      else
        LOG.error("The type contains an unknown base object id, object can't be created");

      // Make a call to getObject to convert the resulting id into an ObjectData
      ObjectData od = PropertyCreationHelper.getObjectData(fStoreManager, so, null, false,
          IncludeRelationships.NONE, null, false, false, extension);

      fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfos);
      return od;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public void deleteContentStream(CallContext context, String repositoryId,
      Holder<String> objectId, Holder<String> changeToken, ExtensionsData extension) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      LOG.debug("start deleteContentStream()");
      StoredObject so = checkStandardParameters(repositoryId, objectId.getValue());

      if (so == null)
        throw new CmisObjectNotFoundException("Unknown object id: " + objectId);

      if (!(so instanceof Content))
        throw new CmisObjectNotFoundException("Id" + objectId
            + " does not refer to a document, but only documents can have content");

      ((Content) so).setContent(null, true);
      LOG.debug("stop deleteContentStream()");
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public void deleteObjectOrCancelCheckOut(CallContext context, String repositoryId,
      String objectId, Boolean allVersions, ExtensionsData extension) {

    try {
      LOG.debug("start deleteObject()");
      checkStandardParameters(repositoryId, objectId);
      ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);
      LOG.info("delete object for id: " + objectId);

      // check if it is the root folder
      if (objectId.equals(objectStore.getRootFolder().getId()))
        throw new CmisNotSupportedException("You can't delete a root folder");

      objectStore.deleteObject(objectId);
      LOG.debug("stop deleteObject()");
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public FailedToDeleteData deleteTree(CallContext context, String repositoryId, String folderId,
      Boolean allVersions, UnfileObjects unfileObjects, Boolean continueOnFailure,
      ExtensionsData extension) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      LOG.debug("start deleteTree()");
      StoredObject so = checkStandardParameters(repositoryId, folderId);
      List<String> failedToDeleteIds = new ArrayList<String>();
      FailedToDeleteDataImpl result = new FailedToDeleteDataImpl();

      if (null == allVersions)
        allVersions = true;
      if (null == unfileObjects)
        unfileObjects = UnfileObjects.DELETE;
      if (null == continueOnFailure)
        continueOnFailure = false;

      ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);

      if (null == so)
        throw new RuntimeException("Cannot delete object with id  " + folderId
            + ". Object does not exist.");

      if (!(so instanceof Folder))
        throw new RuntimeException("deleteTree can only be invoked on a folder, but id " + folderId
            + " does not refer to a folder");

      if (unfileObjects == UnfileObjects.UNFILE)
        throw new CmisNotSupportedException("This repository does not support unfile operations.");

      // check if it is the root folder
      if (folderId.equals(objectStore.getRootFolder().getId()))
        throw new CmisNotSupportedException("You can't delete a root folder");

      // recursively delete folder
      deleteRecursive(objectStore, (Folder) so, continueOnFailure, allVersions, failedToDeleteIds);

      result.setIds(failedToDeleteIds);
      LOG.debug("stop deleteTree()");
      return result;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public AllowableActionsData getAllowableActions(CallContext context, String repositoryId,
      String objectId, ExtensionsData extension) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      LOG.debug("start getAllowableActions()");
      StoredObject so = checkStandardParameters(repositoryId, objectId);
      ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);

      if (so == null)
        throw new CmisObjectNotFoundException("Unknown object id: " + objectId);

      AllowableActionsData allowableActions = DataObjectCreator.fillAllowableActions(objectStore,
          so);
      LOG.debug("stop getAllowableActions()");
      return allowableActions;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public ContentStreamData getContentStream(CallContext context, String repositoryId,
      String objectId, String streamId, BigInteger offset, BigInteger length,
      ExtensionsData extension) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      LOG.debug("start getContentStream()");
      StoredObject so = checkStandardParameters(repositoryId, objectId);

      if (so == null)
        throw new CmisObjectNotFoundException("Unknown object id: " + objectId);

      if (!(so instanceof Content))
        throw new CmisObjectNotFoundException("Id" + objectId
            + " does not refer to a document or version, but only those can have content");

      ContentStreamData csd = getContentStream(so, streamId, offset, length);
      LOG.debug("stop getContentStream()");
      return csd;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public ObjectData getObject(CallContext context, String repositoryId, String objectId,
      String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl,
      ExtensionsData extension, ObjectInfoHolder objectInfos) {

    try {
      LOG.debug("start getObject()");

      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      StoredObject so = checkStandardParameters(repositoryId, objectId);

      if (so == null)
        throw new CmisObjectNotFoundException("Unknown object id: " + objectId);

      ObjectData od = PropertyCreationHelper.getObjectData(fStoreManager, so, filter,
          includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds,
          includeAcl, extension);

      fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfos);

      LOG.debug("stop getObject()");

      return od;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public ObjectData getObjectByPath(CallContext context, String repositoryId, String path,
      String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl,
      ExtensionsData extension, ObjectInfoHolder objectInfos) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      LOG.debug("start getObjectByPath()");
      checkRepositoryId(repositoryId);
      ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);
      StoredObject so = objectStore.getObjectByPath(path);

      if (so == null)
        throw new CmisObjectNotFoundException("Unknown path: " + path);

      ObjectData od = PropertyCreationHelper.getObjectData(fStoreManager, so, filter,
          includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds,
          includeAcl, extension);

      LOG.debug("stop getObjectByPath()");

      // To be able to provide all Atom links in the response we need additional information:
      fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfos);

      return od;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public PropertiesData getProperties(CallContext context, String repositoryId, String objectId,
      String filter, ExtensionsData extension) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      LOG.debug("start getProperties()");
      StoredObject so = checkStandardParameters(repositoryId, objectId);

      if (so == null)
        throw new CmisObjectNotFoundException("Unknown object id: " + objectId);

      // build properties collection
      List<String> requestedIds = FilterParser.getRequestedIdsFromFilter(filter);
      PropertiesData props = PropertyCreationHelper.getPropertiesFromObject(repositoryId, so,
          fStoreManager, requestedIds);
      LOG.debug("stop getProperties()");
      return props;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public List<RenditionData> getRenditions(CallContext context, String repositoryId,
      String objectId, String renditionFilter, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      // TODO to be completed if renditions are implemented
      LOG.debug("start getRenditions()");
      checkStandardParameters(repositoryId, objectId);
      LOG.debug("stop getRenditions()");
      return null;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public ObjectData moveObject(CallContext context, String repositoryId, Holder<String> objectId,
      String targetFolderId, String sourceFolderId, ExtensionsData extension,
      ObjectInfoHolder objectInfos) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      LOG.debug("start moveObject()");
      StoredObject so = checkStandardParameters(repositoryId, objectId.getValue());
      Folder targetFolder = null;
      Folder sourceFolder = null;
      ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);
      Filing spo = null;

      if (null == so)
        throw new CmisObjectNotFoundException("Unknown object: " + objectId.getValue());
      else if (so instanceof Filing)
        spo = (Filing) so;
      else
        throw new CmisInvalidArgumentException("Object must be folder or document: "
            + objectId.getValue());

      StoredObject soTarget = objectStore.getObjectById(targetFolderId);
      if (null == soTarget)
        throw new CmisObjectNotFoundException("Unknown target folder: " + targetFolderId);
      else if (soTarget instanceof Folder)
        targetFolder = (Folder) soTarget;
      else
        throw new CmisNotSupportedException("Destination " + targetFolderId
            + " of a move operation must be a folder");

      StoredObject soSource = objectStore.getObjectById(sourceFolderId);
      if (null == soSource)
        throw new CmisObjectNotFoundException("Unknown source folder: " + sourceFolderId);
      else if (soSource instanceof Folder)
        sourceFolder = (Folder) soSource;
      else
        throw new CmisNotSupportedException("Source " + sourceFolderId
            + " of a move operation must be a folder");

      boolean foundOldParent = false;
      for (Folder parent : spo.getParents()) {
        if (parent.getId().equals(soSource.getId())) {
          foundOldParent = true;
          break;
        }
      }
      if (!foundOldParent)
        throw new CmisNotSupportedException("Cannot move object, source folder " + sourceFolderId
            + "is not a parent of object " + objectId.getValue());

      if (so instanceof Folder && hasDescendant((Folder) so, targetFolder)) {
        throw new CmisNotSupportedException(
            "Destination of a move cannot be a subfolder of the source");
      }

      spo.move(sourceFolder, targetFolder);
      objectId.setValue(so.getId());
      LOG.debug("stop moveObject()");

      // To be able to provide all Atom links in the response we need additional information:
      fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfos);

      ObjectData od = PropertyCreationHelper.getObjectData(fStoreManager, so, null, false,
          IncludeRelationships.NONE, null, false, false, extension);

      return od;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public void setContentStream(CallContext context, String repositoryId, Holder<String> objectId,
      Boolean overwriteFlag, Holder<String> changeToken, ContentStreamData contentStream,
      ExtensionsData extension) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      LOG.debug("start setContentStream()");
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
      }
      else
        throw new IllegalArgumentException(
            "Content cannot be set on this object (must be document or version)");

      if (!overwriteFlag && content.getContent(0, -1) != null)
        throw new CmisConstraintException(
            "cannot overwrite existing content if overwrite flag is not set");

      content.setContent(contentStream, true);
      LOG.debug("stop setContentStream()");
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public ObjectData updateProperties(CallContext context, String repositoryId,
      Holder<String> objectId, Holder<String> changeToken, PropertiesData properties,
      AccessControlList acl, ExtensionsData extension, ObjectInfoHolder objectInfos) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      LOG.debug("start updateProperties()");
      StoredObject so = checkStandardParameters(repositoryId, objectId.getValue());

      // Validation
      TypeDefinition typeDef = getTypeDefinition(repositoryId, so);
      boolean isCheckedOut = false;

      // if the object is a versionable object it must be checked-out
      if (so instanceof VersionedDocument || so instanceof DocumentVersion) {
        String user = RuntimeContext.getRuntimeConfigValue(CallContext.USERNAME);
        // VersionedDocument verDoc = testIsNotCheckedOutBySomeoneElse(so, user);
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
        }
        else {
          if (propDef.getUpdatability().equals(Updatability.WHENCHECKEDOUT) && !isCheckedOut)
            throw new CmisConstraintException(
                "updateProperties failed, following property can't be updated, because it is not checked-out: "
                    + key);
          else if (!propDef.getUpdatability().equals(Updatability.READWRITE))
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
          LOG.info("updateProperties(), new change token is: " + changeTokenVal);
          changeToken.setValue(changeTokenVal);
        }
      }

      if (null != acl) {
        LOG.warn("Setting ACLs is currently not supported by this implementation, acl is ignored");
        // if implemented add this call:
        // fAclService.appyAcl(context, repositoryId, acl, null, AclPropagation.OBJECTONLY,
        // extension);
      }

      // To be able to provide all Atom links in the response we need additional information:
      fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfos);

      ObjectData od = PropertyCreationHelper.getObjectData(fStoreManager, so, null, false,
          IncludeRelationships.NONE, null, false, false, extension);

      LOG.debug("stop updateProperties()");

      return od;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  // ///////////////////////////////////////////////////////
  // private helper methods

  private StoredObject createDocumentIntern(String repositoryId, PropertiesData properties,
      String folderId, ContentStreamData contentStream, VersioningState versioningState,
      List<String> policies, AccessControlList addACEs, AccessControlList removeACEs,
      ExtensionsData extension) {
    checkRepositoryId(repositoryId);

    ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);

    // get name from properties
    PropertyData<?> pd = properties.getProperties().get(PropertyIds.CMIS_NAME);
    String name = (String) pd.getFirstValue();

    // Validation stuff
    TypeValidator.validateRequiredSystemProperties(properties);
    TypeDefinition typeDef = getTypeDefinition(repositoryId, properties);

    Folder folder = null;
    if (null != folderId) {
      StoredObject so = objectStore.getObjectById(folderId);

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
    if (!typeDef.getBaseId().equals(BaseObjectTypeIds.CMIS_DOCUMENT))
      throw new RuntimeException("Cannot create a document, with a non-document type: "
          + typeDef.getId());

    // check name syntax
    if (!NameValidator.isValidId(name))
      throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_NAME);

    TypeValidator.validateVersionStateForCreate((DocumentTypeDefinition) typeDef, versioningState);
    TypeValidator.validateProperties(typeDef, properties, true);

    // set user, creation date, etc.
    String user = RuntimeContext.getRuntimeConfigValue(CallContext.USERNAME);
    if (user == null)
      user = "unknown";

    StoredObject so = null;

    // Now we are sure to have document type definition:
    if (((DocumentTypeDefinition) typeDef).isVersionable()) {
      VersionedDocument verDoc = fStoreManager.getObjectStore(repositoryId)
          .createVersionedDocument(name);
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
      so = version; // return the version and not the version series to caller
    }
    else {
      Document doc = fStoreManager.getObjectStore(repositoryId).createDocument(name);
      doc.setContent(contentStream, false);
      // add document to folder
      doc.createSystemBasePropertiesWhenCreated(properties.getProperties(), user);
      doc.setCustomProperties(properties.getProperties());
      if (null != folder)
        folder.addChildDocument(doc); // add document to folder and set parent in doc
      else
        doc.persist();
      so = doc;
    }

    // policies, addACEs, removeACEs, extension are ignored for
    // now.
    return so;
  }

  private Folder createFolderIntern(String repositoryId, PropertiesData properties,
      String folderId, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    checkStandardParameters(repositoryId, folderId);

    ObjectStore fs = fStoreManager.getObjectStore(repositoryId);
    StoredObject so = null;
    Folder parent = null;

    // get required properties
    PropertyData<?> pd = properties.getProperties().get(PropertyIds.CMIS_NAME);
    String folderName = (String) pd.getFirstValue();
    if (null == folderName || folderName.length() == 0)
      throw new CmisInvalidArgumentException("Cannot create a folder without a name.");

    // check name syntax
    if (!NameValidator.isValidId(folderName))
      throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_NAME);


    TypeValidator.validateRequiredSystemProperties(properties);

    TypeDefinition typeDef = getTypeDefinition(repositoryId, properties);

    // check if the given type is a folder type
    if (!typeDef.getBaseId().equals(BaseObjectTypeIds.CMIS_FOLDER))
      throw new RuntimeException("Cannot create a folder, with a non-folder type: "
          + typeDef.getId());

    TypeValidator.validateProperties(typeDef, properties, true);

    // create folder
    try {
      LOG.info("get folder for id: " + folderId);
      so = fs.getObjectById(folderId);
    }
    catch (Exception e) {
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
      LOG.debug("stop createFolder()");
      return newFolder;
    }
    catch (Exception e) {
      throw new CmisInvalidArgumentException("Failed to create child folder.", e);
    }
  }

  private StoredObject createPolicyIntern(String repositoryId, PropertiesData properties,
      String folderId, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension) {
    return null;
  }

  private StoredObject createRelationshipIntern(String repositoryId, PropertiesData properties,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {
    return null;
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

  /**
   * Recursively delete a tree by traversing it and first deleting all children and then the object
   * itself
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
      }
      else {
        try {
          folderStore.deleteObject(child.getId());
        }
        catch (Exception e) {
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
