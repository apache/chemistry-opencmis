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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;
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
import org.apache.opencmis.commons.provider.VersioningService;
import org.apache.opencmis.inmemory.ObjectServiceImpl;
import org.apache.opencmis.inmemory.VersioningServiceImpl;
import org.apache.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.opencmis.inmemory.storedobj.api.VersionedDocument;
import org.apache.opencmis.inmemory.types.InMemoryDocumentTypeDefinition;
import org.apache.opencmis.inmemory.types.InMemoryFolderTypeDefinition;
import org.apache.opencmis.inmemory.types.InMemoryPolicyTypeDefinition;
import org.apache.opencmis.inmemory.types.InMemoryRelationshipTypeDefinition;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisObjectService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

public class InMemoryObjectService implements CmisObjectService {
  private static final Log LOG = LogFactory.getLog(ServiceFactory.class.getName());

  StoreManager fStoreManager;
  VersioningService fVersioningService; // real implementation of the service
  ObjectService fObjectService; // real implementation of the service
  AtomLinkInfoProvider fAtomLinkProvider;

  InMemoryObjectService(StoreManager storeManager) {
    fStoreManager = storeManager;
    fObjectService = new ObjectServiceImpl(fStoreManager);
    fAtomLinkProvider = new AtomLinkInfoProvider(fStoreManager);
    fVersioningService = new VersioningServiceImpl(fStoreManager, fObjectService);
  }
  
  ObjectService getObjectService() {
    return fObjectService;
  }
  
  public String createDocument(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, ContentStreamData contentStream, VersioningState versioningState,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    return fObjectService.createDocument(repositoryId, properties, folderId, contentStream,
        versioningState, policies, addAces, removeAces, extension);
  }

  public String createDocumentFromSource(CallContext context, String repositoryId, String sourceId,
      PropertiesData properties, String folderId, VersioningState versioningState,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    return fObjectService.createDocumentFromSource(repositoryId, sourceId, properties, folderId,
        versioningState, policies, addAces, removeAces, extension);
  }

  public String createFolder(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    return fObjectService.createFolder(repositoryId, properties, folderId, policies, addAces,
        removeAces, extension);
  }

  public String createPolicy(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    return fObjectService.createPolicy(repositoryId, properties, folderId, policies, addAces,
        removeAces, extension);
  }

  public String createRelationship(CallContext context, String repositoryId,
      PropertiesData properties, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    return fObjectService.createRelationship(repositoryId, properties, policies, addAces,
        removeAces, extension);
  }

  /* (non-Javadoc)
   * @see org.opencmis.server.spi.CmisObjectService#create(org.opencmis.server.spi.CallContext, java.lang.String, org.opencmis.client.provider.PropertiesData, java.lang.String, org.opencmis.client.provider.ContentStreamData, org.opencmis.commons.enums.VersioningState, org.opencmis.client.provider.ExtensionsData, org.opencmis.server.spi.ObjectInfoHolder)
   * 
   * An additional create call compared to the ObjectService from the CMIS spec.
   * This one is needed because the Atom binding in the server implementation does 
   * not know what kind of object needs to be created. Also the ObjectInfoHolder needs
   * to be filled.
   * 
   */
  public ObjectData create(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, ContentStreamData contentStream, VersioningState versioningState, List<String> policies,
      ExtensionsData extension, ObjectInfoHolder objectInfos) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);
    
    if (null==properties || null==properties.getProperties())
        throw new RuntimeException("Cannot create object, without properties.");

    // Find out what kind of object needs to be created
    PropertyData<String> pd = (PropertyData<String>) properties.getProperties().get(PropertyIds.CMIS_OBJECT_TYPE_ID);
    String typeId = pd==null ? null : pd.getFirstValue(); 
    if (null==typeId)
      throw new RuntimeException("Cannot create object, without a type (no property with id CMIS_OBJECT_TYPE_ID).");
    
    TypeDefinitionContainer typeDefC = fStoreManager.getTypeById(repositoryId, typeId);
    if (typeDefC == null)
      throw new RuntimeException("Cannot create object, a type with id " + typeId + " is unknown");

    // check if the given type is a document type
    BaseObjectTypeIds typeBaseId = typeDefC.getTypeDefinition().getBaseId();
    String resId = null;
    if (typeBaseId.equals(InMemoryDocumentTypeDefinition.getRootDocumentType().getBaseId())) {
      resId = createDocument(context, repositoryId, properties, folderId, contentStream, versioningState, 
          null, null, null, null);
    } else if (typeBaseId.equals(InMemoryFolderTypeDefinition.getRootFolderType().getBaseId())) {
      resId = createFolder(context, repositoryId, properties, folderId, null, null, null, null);
    } else if (typeBaseId.equals(InMemoryPolicyTypeDefinition.getRootPolicyType().getBaseId())) {
      resId = createPolicy(context, repositoryId, properties, folderId, null, null, null, null);
    } else if (typeBaseId.equals(InMemoryRelationshipTypeDefinition.getRootRelationshipType().getBaseId())) {
      resId = createRelationship(context, repositoryId, properties,  null, null, null, null);
    } else
      LOG.error("The type contains an unknown base object id, object can't be created");

    // Make a call to getObject to convert the resulting id into an ObjectData
    ObjectData res = null;
    if (null!=resId) {
      res = getObject(context, repositoryId, resId, "*", false, IncludeRelationships.NONE,
          null, false, false, extension, objectInfos);
    }
    
    return res;
  }
  
  public void deleteContentStream(CallContext context, String repositoryId,
      Holder<String> objectId, Holder<String> changeToken, ExtensionsData extension) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    fObjectService.deleteContentStream(repositoryId, objectId, changeToken, extension);
  }

  public void deleteObjectOrCancelCheckOut(CallContext context, String repositoryId, String objectId,
      Boolean allVersions, ExtensionsData extension) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    StoredObject so = fStoreManager.getObjectStore(repositoryId).getObjectById(objectId);
    boolean mustCancelCheckout = false;
    if (so instanceof DocumentVersion) {
      mustCancelCheckout = ((DocumentVersion)so).getParentDocument().isCheckedOut();
    } else if (so instanceof VersionedDocument) {
      mustCancelCheckout = ((VersionedDocument)so).isCheckedOut();      
    }
    
    if (mustCancelCheckout)
      fVersioningService.cancelCheckOut(repositoryId, objectId, extension);
    else
      fObjectService.deleteObject(repositoryId, objectId, allVersions, extension);
  }

  public FailedToDeleteData deleteTree(CallContext context, String repositoryId, String folderId,
      Boolean allVersions, UnfileObjects unfileObjects, Boolean continueOnFailure,
      ExtensionsData extension) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    return fObjectService.deleteTree(repositoryId, folderId, allVersions, unfileObjects,
        continueOnFailure, extension);
  }

  public AllowableActionsData getAllowableActions(CallContext context, String repositoryId,
      String objectId, ExtensionsData extension) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    return fObjectService.getAllowableActions(repositoryId, objectId, extension);
  }

  public ContentStreamData getContentStream(CallContext context, String repositoryId,
      String objectId, String streamId, BigInteger offset, BigInteger length,
      ExtensionsData extension) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    return fObjectService.getContentStream(repositoryId, objectId, streamId, offset, length,
        extension);
  }

  public ObjectData getObject(CallContext context, String repositoryId, String objectId,
      String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl,
      ExtensionsData extension, ObjectInfoHolder objectInfos) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    ObjectData res = fObjectService.getObject(repositoryId, objectId, filter,
        includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds,
        includeAcl, extension);

    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, objectId, objectInfos);
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, res == null ? null : res.getId(),
        objectInfos);

    return res;
  }

  public ObjectData getObjectByPath(CallContext context, String repositoryId, String path,
      String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl,
      ExtensionsData extension, ObjectInfoHolder objectInfos) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    ObjectData res = fObjectService.getObjectByPath(repositoryId, path, filter,
        includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds,
        includeAcl, extension);

    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, res == null ? null : res.getId(),
        objectInfos);

    return res;
  }

  public PropertiesData getProperties(CallContext context, String repositoryId, String objectId,
      String filter, ExtensionsData extension) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    PropertiesData res = fObjectService.getProperties(repositoryId, objectId, filter, extension);

    return res;
  }

  public List<RenditionData> getRenditions(CallContext context, String repositoryId,
      String objectId, String renditionFilter, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    return fObjectService.getRenditions(repositoryId, objectId, renditionFilter, maxItems,
        skipCount, extension);
  }

  public ObjectData moveObject(CallContext context, String repositoryId, Holder<String> objectId,
      String targetFolderId, String sourceFolderId, ExtensionsData extension,
      ObjectInfoHolder objectInfos) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    fObjectService.moveObject(repositoryId, objectId, targetFolderId, sourceFolderId, extension);

    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, objectId.getValue(), objectInfos);
  
    // Make a call to getObject afterwards to be able to provide all the required information
    ObjectData res = null;
    if (null != objectId.getValue()) {
      res = getObject(context, repositoryId, objectId.getValue(), "*", false, IncludeRelationships.NONE,
          null, false, false, extension, objectInfos);
    }
    return res;
  }

  public void setContentStream(CallContext context, String repositoryId, Holder<String> objectId,
      Boolean overwriteFlag, Holder<String> changeToken, ContentStreamData contentStream,
      ExtensionsData extension) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    fObjectService.setContentStream(repositoryId, objectId, overwriteFlag, changeToken,
        contentStream, extension);
  }

  public ObjectData updateProperties(CallContext context, String repositoryId, Holder<String> objectId,
      Holder<String> changeToken, PropertiesData properties, AccessControlList acl, ExtensionsData extension,
      ObjectInfoHolder objectInfos) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    if (null!=properties)
      fObjectService.updateProperties(repositoryId, objectId, changeToken, properties, extension);
    
    if (null != acl) {
      LOG.warn("Setting ACLs is currently not supported by this implementation, acl is ignored");
      // if implemented add this call:
      // fAclService.appyAcl(context, repositoryId, acl, null, AclPropagation.OBJECTONLY, extension);
    }

    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, objectId.getValue(), objectInfos);

    // Make a call to getObject afterwards to be able to provide all the required information
    ObjectData res = null;
    if (null != objectId.getValue()) {
      res = getObject(context, repositoryId, objectId.getValue(), "*", false, IncludeRelationships.NONE,
          null, false, false, extension, objectInfos);
    }
    return res;
  }

}
