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
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.impl.dataobjects.ProviderObjectFactoryImpl;
import org.apache.opencmis.commons.provider.NavigationService;
import org.apache.opencmis.commons.provider.ObjectService;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.commons.provider.RepositoryService;
import org.apache.opencmis.inmemory.ConfigConstants;
import org.apache.opencmis.inmemory.NavigationServiceImpl;
import org.apache.opencmis.inmemory.ObjectServiceImpl;
import org.apache.opencmis.inmemory.RepositoryServiceImpl;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.inmemory.storedobj.impl.StoreManagerFactory;
import org.apache.opencmis.inmemory.storedobj.impl.StoreManagerImpl;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisDiscoveryService;
import org.apache.opencmis.server.spi.CmisNavigationService;
import org.apache.opencmis.server.spi.CmisObjectService;
import org.apache.opencmis.server.spi.CmisRepositoryService;
import org.apache.opencmis.server.spi.CmisVersioningService;
import org.apache.opencmis.server.support.DiscoveryServiceWrapper;
import org.apache.opencmis.server.support.NavigationServiceWrapper;
import org.apache.opencmis.server.support.ObjectServiceWrapper;
import org.apache.opencmis.server.support.RepositoryServiceWrapper;
import org.apache.opencmis.server.support.VersioningServiceWrapper;
import org.apache.opencmis.util.repository.ObjectGenerator;

public class ServiceFactory extends AbstractServicesFactory {

  private static final Log LOG = LogFactory.getLog(ServiceFactory.class.getName());
  private static final BigInteger DEFAULT_MAX_ITEMS_OBJECTS = BigInteger.valueOf(1000);
  private static final BigInteger DEFAULT_MAX_ITEMS_TYPES = BigInteger.valueOf(100);
  private static final BigInteger DEFAULT_DEPTH_OBJECTS = BigInteger.valueOf(2);
  private static final BigInteger DEFAULT_DEPTH_TYPES = BigInteger.valueOf(-1);

  private  StoreManager fStoreManager; // singleton root of everything

  private CmisRepositoryService fRepositoryService;
  private CmisNavigationService fNavigationService;
  private CmisObjectService fObjectService;
  private CmisVersioningService fVersioningService;
  private CmisDiscoveryService fDiscoveryService;

  public StoreManager getStoreManager() {
    return fStoreManager;
  }
  
  @Override
  public void init(Map<String, String> parameters) {
    LOG.info("Initializing in-memory repository...");
    
    // initialize in-memory management
    String repositoryClassName = (String) parameters.get(ConfigConstants.REPOSITORY_CLASS);
    if (null==repositoryClassName)
      repositoryClassName = StoreManagerImpl.class.getName();
    
    if (null == fStoreManager)
      fStoreManager = StoreManagerFactory.createInstance(repositoryClassName);

    String repositoryId = parameters.get(ConfigConstants.REPOSITORY_ID);
    
    List<String> allAvailableRepositories = fStoreManager.getAllRepositoryIds();
    
    // init existing repositories
    for (String existingRepId : allAvailableRepositories)
      fStoreManager.initRepository(existingRepId);

    // create repository if configured as a startup parameter
    if (null != repositoryId) {
      if (allAvailableRepositories.contains(repositoryId)) 
        LOG.warn("Repostory " + repositoryId + " already exists and will not be created.");
      else {
        String typeCreatorClassName = parameters.get(ConfigConstants.TYPE_CREATOR_CLASS);        
        fStoreManager.createAndInitRepository(repositoryId, typeCreatorClassName);
      }
    }

    InMemoryRepositoryService repSvc = new InMemoryRepositoryService(fStoreManager);
    InMemoryNavigationService navSvc = new InMemoryNavigationService(fStoreManager);
    InMemoryObjectService objSvc = new InMemoryObjectService(fStoreManager);
    InMemoryVersioningService verSvc = new InMemoryVersioningService(fStoreManager, objSvc
        .getObjectService());
    InMemoryDiscoveryService disSvc = new InMemoryDiscoveryService(fStoreManager, repSvc
        .getRepositoryService(), navSvc.fNavigationService);
    
    // Initialize services, use the service wrappers to provide suitable default parameters and
    // paging sets
    fRepositoryService = new RepositoryServiceWrapper(repSvc,
        DEFAULT_MAX_ITEMS_TYPES, DEFAULT_DEPTH_TYPES);
    fNavigationService = new NavigationServiceWrapper(navSvc,
        DEFAULT_MAX_ITEMS_OBJECTS, DEFAULT_DEPTH_OBJECTS);
    fObjectService = new ObjectServiceWrapper(objSvc,
        DEFAULT_MAX_ITEMS_OBJECTS);
    fVersioningService = new VersioningServiceWrapper(verSvc);
    fDiscoveryService = new DiscoveryServiceWrapper(disSvc, DEFAULT_MAX_ITEMS_OBJECTS);
    
    // With some special configuration settings fill the repository with some documents and folders if is empty
    if (!allAvailableRepositories.contains(repositoryId))
      fillRepositoryIfConfigured(parameters, repositoryId);
    
    LOG.info("...initialized in-memory repository.");
  }

  @Override
  public void destroy() {
    LOG.info("Destroyed in-memory repository.");
  }

  @Override
  public CmisRepositoryService getRepositoryService() {
    return fRepositoryService;
  }

  @Override
  public CmisNavigationService getNavigationService() {
    return fNavigationService;
  }

  @Override
  public CmisObjectService getObjectService() {
    return fObjectService;
  }

  @Override
  public CmisVersioningService getVersioningService() {
    return fVersioningService;
  }

  @Override
  public CmisDiscoveryService getDiscoveryService() {
    return fDiscoveryService;
  }
    
  private void fillRepositoryIfConfigured(Map<String, String> parameters, String repositoryId) {
    class DummyCallContext implements CallContext {

      public String get(String key) {
        return null;
      }

      public String getBinding() {
        return null;
      }

      public String getLocale() {
        return null;
      }

      public String getPassword() {
        return null;
      }

      public String getUsername() {
        return null;
      }
    }
    
    String doFillRepositoryStr = parameters.get(ConfigConstants.USE_REPOSITORY_FILER);
    boolean doFillRepository = doFillRepositoryStr == null ? false : Boolean
        .parseBoolean(doFillRepositoryStr);

    if (!doFillRepository)
      return;
    
    ProviderObjectFactory objectFactory = new ProviderObjectFactoryImpl();
    NavigationService navSvc = new NavigationServiceImpl(fStoreManager);
    ObjectService objSvc = new ObjectServiceImpl(fStoreManager);
    RepositoryService repSvc = new RepositoryServiceImpl(fStoreManager);
        
    String levelsStr = parameters.get(ConfigConstants.FILLER_DEPTH);
    int levels = 1;
    if (null!=levelsStr)
      levels = Integer.parseInt(levelsStr);  
       
    String docsPerLevelStr = parameters.get(ConfigConstants.FILLER_DOCS_PER_FOLDER);
    int docsPerLevel = 1;
    if (null!=docsPerLevelStr)
      docsPerLevel = Integer.parseInt(docsPerLevelStr);

    String childrenPerLevelStr = parameters.get(ConfigConstants.FILLER_FOLDERS_PER_FOLDER);
    int childrenPerLevel = 2;
    if (null!=childrenPerLevelStr)
      childrenPerLevel = Integer.parseInt(childrenPerLevelStr);
    
    String documentTypeId =  parameters.get(ConfigConstants.FILLER_DOCUMENT_TYPE_ID);
    if (null == documentTypeId)
      documentTypeId = BaseObjectTypeIds.CMIS_DOCUMENT.value();
    
    String folderTypeId =  parameters.get(ConfigConstants.FILLER_FOLDER_TYPE_ID);
    if (null == folderTypeId)
      folderTypeId = BaseObjectTypeIds.CMIS_FOLDER.value();

    int contentSizeKB = 0;
    String contentSizeKBStr = parameters.get(ConfigConstants.FILLER_CONTENT_SIZE);
    if (null!=contentSizeKBStr)
      contentSizeKB = Integer.parseInt(contentSizeKBStr);

    // Create a hierarchy of folders and fill it with some documents
    ObjectGenerator gen = new ObjectGenerator(objectFactory, navSvc, objSvc, repositoryId);

    gen.setNumberOfDocumentsToCreatePerFolder(docsPerLevel); 

    // Set the type id for all created documents:
    gen.setDocumentTypeId(documentTypeId);
    
    // Set the type id for all created folders:
    gen.setFolderTypeId(folderTypeId);
    
    // Set contentSize
    gen.setContentSizeInKB(contentSizeKB);
    
    // set properties that need to be filled
    // set the properties the generator should fill with values for documents:
    // Note: must be valid properties in configured document and folder type
   
    List<String> propsToSet = readPropertiesToSetFromConfig(parameters,
        ConfigConstants.FILLER_DOCUMENT_PROPERTY);
    if (null != propsToSet)
      gen.setDocumentPropertiesToGenerate(propsToSet);
    
    propsToSet = readPropertiesToSetFromConfig(parameters,
        ConfigConstants.FILLER_FOLDER_PROPERTY);
    if (null != propsToSet)
      gen.setFolderPropertiesToGenerate(propsToSet);

    // Simulate a runtime context with configuration parameters
    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(new DummyCallContext());

    // Build the tree
    RepositoryInfoData rep = repSvc.getRepositoryInfo(repositoryId, null);
    String rootFolderId = rep.getRootFolderId();

    try {
      gen.createFolderHierachy(levels, childrenPerLevel, rootFolderId);
      // Dump the tree
      gen.dumpFolder(rootFolderId, "*");
    } catch (Exception e) {
      LOG.error("Could not create folder hierarchy with documents. " + e);
      e.printStackTrace();
    }

  }
  
  private List<String> readPropertiesToSetFromConfig(Map<String, String> parameters, String keyPrefix) {
    List<String> propsToSet = new ArrayList<String>();
    for (int i=0; ; ++i) {
      String propertyKey = keyPrefix + Integer.toString(i);
      String propertyToAdd = parameters.get(propertyKey);
      if (null == propertyToAdd)
        break;
      else
        propsToSet.add(propertyToAdd);
    }
    return propsToSet;
  }
}
