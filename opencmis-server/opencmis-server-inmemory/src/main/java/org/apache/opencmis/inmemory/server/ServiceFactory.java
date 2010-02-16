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
import org.apache.opencmis.inmemory.MapConfigReader;
import org.apache.opencmis.inmemory.NavigationServiceImpl;
import org.apache.opencmis.inmemory.ObjectServiceImpl;
import org.apache.opencmis.inmemory.RepositoryServiceImpl;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.inmemory.storedobj.impl.StoreManagerFactory;
import org.apache.opencmis.inmemory.storedobj.impl.StoreManagerImpl;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CmisDiscoveryService;
import org.apache.opencmis.server.spi.CmisNavigationService;
import org.apache.opencmis.server.spi.CmisObjectService;
import org.apache.opencmis.server.spi.CmisRepositoryService;
import org.apache.opencmis.server.spi.CmisVersioningService;
import org.apache.opencmis.util.repository.ObjectGenerator;

public class ServiceFactory extends AbstractServicesFactory {

  private static final Log LOG = LogFactory.getLog(ServiceFactory.class.getName());
  private static StoreManager STORE_MANAGER; // singleton root of everything
  public static StoreManager getInstance() {
    if (null == STORE_MANAGER)
      throw new RuntimeException("Application not initialized correctly");
    return STORE_MANAGER;
  }

  private InMemoryRepositoryService fRepositoryService;
  private InMemoryNavigationService fNavigationService;
  private InMemoryObjectService fObjectService;
  private InMemoryVersioningService fVersioningService;
  private InMemoryDiscoveryService fDiscoveryService;

  @Override
  public void init(Map<String, String> parameters) {
    LOG.info("Initializing in-memory repository...");
    
    // initialize in-memory management
    String repositoryClassName = (String) parameters.get(ConfigConstants.REPOSITORY_CLASS);
    if (null==repositoryClassName)
      repositoryClassName = StoreManagerImpl.class.getName();
    
    if (null == STORE_MANAGER)
      STORE_MANAGER = StoreManagerFactory.createInstance(repositoryClassName);

    MapConfigReader cfgReader = new MapConfigReader(parameters);
    STORE_MANAGER.setConfigReader(cfgReader);
    String repositoryId = parameters.get(ConfigConstants.REPOSITORY_ID);
    
    List<String> allAvailableRepositories = STORE_MANAGER.getAllRepositoryIds();
    
    // init existing repositories
    for (String existingRepId : allAvailableRepositories)
      STORE_MANAGER.initRepository(existingRepId, false);

    // create repository
    if (null != repositoryId) {
      if (allAvailableRepositories.contains(repositoryId)) 
        LOG.warn("Repostory " + repositoryId + " already exists and will not be created.");
      else {
        STORE_MANAGER.createRepository(repositoryId);
        // then create/initialize type system
        String typeCreatorClassName = parameters.get(ConfigConstants.TYPE_CREATOR_CLASS);        
        STORE_MANAGER.initTypeSystem(repositoryId, typeCreatorClassName);
        // then init repository (note: loads root folder which requires cmis:folder type available)
        
        STORE_MANAGER.initRepository(repositoryId, true);
      }
    }

    if (repositoryId != null) {
      String repoInfoCreatorClassName = parameters.get(ConfigConstants.REPOSITORY_INFO_CREATOR_CLASS);
      STORE_MANAGER.initRepositoryInfo(repositoryId, repoInfoCreatorClassName);    
    }
    
    // initialize services
    fRepositoryService = new InMemoryRepositoryService(STORE_MANAGER);
    fNavigationService = new InMemoryNavigationService(STORE_MANAGER);
    fObjectService = new InMemoryObjectService(STORE_MANAGER);
    fVersioningService = new InMemoryVersioningService(STORE_MANAGER, fObjectService.getObjectService());
    // Begin temporary implementation for discover service
    fDiscoveryService = new InMemoryDiscoveryService(STORE_MANAGER, fRepositoryService.getRepositoryService(),  fNavigationService.getNavigationService());
    // End temporary implementation
    
    // With some special configuration settings fill the repository with some documents and folders if is empty
    if (!allAvailableRepositories.contains(repositoryId))
      fillRepositoryIfConfigured(cfgReader, repositoryId);
    
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
    
  private void fillRepositoryIfConfigured(MapConfigReader cfgReader, String repositoryId) {
    
    String doFillRepositoryStr = cfgReader.get(ConfigConstants.USE_REPOSITORY_FILER);
    boolean doFillRepository = doFillRepositoryStr == null ? false : Boolean
        .parseBoolean(doFillRepositoryStr);

    if (!doFillRepository)
      return;
    
    ProviderObjectFactory objectFactory = new ProviderObjectFactoryImpl();
    NavigationService navSvc = new NavigationServiceImpl(STORE_MANAGER);
    ObjectService objSvc = new ObjectServiceImpl(STORE_MANAGER);
    RepositoryService repSvc = new RepositoryServiceImpl(STORE_MANAGER);
        
    String levelsStr = cfgReader.get(ConfigConstants.FILLER_DEPTH);
    int levels = 1;
    if (null!=levelsStr)
      levels = Integer.parseInt(levelsStr);  
       
    String docsPerLevelStr = cfgReader.get(ConfigConstants.FILLER_DOCS_PER_FOLDER);
    int docsPerLevel = 1;
    if (null!=docsPerLevelStr)
      docsPerLevel = Integer.parseInt(docsPerLevelStr);

    String childrenPerLevelStr = cfgReader.get(ConfigConstants.FILLER_FOLDERS_PER_FOLDER);
    int childrenPerLevel = 2;
    if (null!=childrenPerLevelStr)
      childrenPerLevel = Integer.parseInt(childrenPerLevelStr);
    
    String documentTypeId =  cfgReader.get(ConfigConstants.FILLER_DOCUMENT_TYPE_ID);
    if (null == documentTypeId)
      documentTypeId = BaseObjectTypeIds.CMIS_DOCUMENT.value();
    
    String folderTypeId =  cfgReader.get(ConfigConstants.FILLER_FOLDER_TYPE_ID);
    if (null == folderTypeId)
      folderTypeId = BaseObjectTypeIds.CMIS_FOLDER.value();

    int contentSizeKB = 0;
    String contentSizeKBStr = cfgReader.get(ConfigConstants.FILLER_CONTENT_SIZE);
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
   
    List<String> propsToSet = readPropertiesToSetFromConfig(cfgReader,
        ConfigConstants.FILLER_DOCUMENT_PROPERTY);
    if (null != propsToSet)
      gen.setDocumentPropertiesToGenerate(propsToSet);
    
    propsToSet = readPropertiesToSetFromConfig(cfgReader,
        ConfigConstants.FILLER_FOLDER_PROPERTY);
    if (null != propsToSet)
      gen.setFolderPropertiesToGenerate(propsToSet);

    // Simulate a runtime context with configuration parameters
    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(cfgReader);

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
  
  private List<String> readPropertiesToSetFromConfig(MapConfigReader cfgReader, String keyPrefix) {
    List<String> propsToSet = new ArrayList<String>();
    for (int i=0; ; ++i) {
      String propertyKey = keyPrefix + Integer.toString(i);
      String propertyToAdd = cfgReader.get(propertyKey);
      if (null == propertyToAdd)
        break;
      else
        propsToSet.add(propertyToAdd);
    }
    return propsToSet;
  }
}
