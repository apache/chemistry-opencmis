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

import java.util.Map;

import org.apache.opencmis.commons.provider.AclService;
import org.apache.opencmis.commons.provider.DiscoveryService;
import org.apache.opencmis.commons.provider.MultiFilingService;
import org.apache.opencmis.commons.provider.NavigationService;
import org.apache.opencmis.commons.provider.ObjectService;
import org.apache.opencmis.commons.provider.PolicyService;
import org.apache.opencmis.commons.provider.RelationshipService;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.commons.provider.RepositoryService;
import org.apache.opencmis.commons.provider.VersioningService;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.inmemory.storedobj.impl.StoreManagerFactory;
import org.apache.opencmis.inmemory.storedobj.impl.StoreManagerImpl;

/**
 * General class to manage all the services. Directly used for unit test of the
 * in-memory server, indirectly used for the client provider implementation of
 * the in-memory provider
 * 
 * @author Jens
 *
 */
public class CmisInMemoryProvider {

  protected StoreManager fStoreManager;
  protected RepositoryService fRepositoryService;
  protected NavigationService fNavigationService;
  protected ObjectService fObjectService;
  protected VersioningService fVersioningService;
  protected RepositoryInfoData fRepositoryInfo;

  public CmisInMemoryProvider(Map<String, String> cfgParams) {
    setup (cfgParams);    
  }
  
  protected CmisInMemoryProvider() {  
  }
  
  RepositoryService getRepositoryService() {
    return fRepositoryService;
  }

  NavigationService getNavigationService() {
    return fNavigationService;
  }

  ObjectService getObjectService() {
    return fObjectService;    
  }

  VersioningService getVersioningService() {
   return fVersioningService; 
  }

  RelationshipService getRelationshipService() {
    return null;
  }

  DiscoveryService getDiscoveryService() {
    return null;
  }

  MultiFilingService getMultiFilingService() {
    return null;
  }

  AclService getAclService() {
    return null;
  }

  PolicyService getPolicyService() {
    return null;
  }

  protected void setup(Map<String, String> cfgParams) {
    String repositoryClassName = (String) cfgParams.get(ConfigConstants.REPOSITORY_CLASS);
    if (null==repositoryClassName)
      repositoryClassName = StoreManagerImpl.class.getName();
    
    fStoreManager = StoreManagerFactory.createInstance(repositoryClassName);
    String repositoryId  = (String) cfgParams.get(ConfigConstants.REPOSITORY_ID);
    
    // first create repository
    if (null != repositoryId ) {
      // then create/initialize type system
      String typeCreatorClassName = (String) cfgParams.get(ConfigConstants.TYPE_CREATOR_CLASS);
      fStoreManager.createAndInitRepository(repositoryId, typeCreatorClassName);
    } else {    
      // then init repository (note: loads root folder which requires cmis:folder type available)
      fStoreManager.initRepository(null);
    }
    
//    String repoInfoCreatorClassName = (String) fSession.get(ConfigConstants.REPOSITORY_INFO_CREATOR_CLASS);
//    fStoreManager.initRepositoryInfo(repositoryId, repoInfoCreatorClassName);    

    // initialize services
    fRepositoryService = new RepositoryServiceImpl(fStoreManager);
    fNavigationService = new NavigationServiceImpl(fStoreManager);
    fObjectService = new ObjectServiceImpl(fStoreManager);
    fVersioningService = new VersioningServiceImpl(fStoreManager, fObjectService);    
  }

  
}
