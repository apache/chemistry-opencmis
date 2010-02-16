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
package org.apache.opencmis.inmemory.clientprovider;

import org.apache.opencmis.client.provider.spi.CmisSpi;
import org.apache.opencmis.client.provider.spi.Session;
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
import org.apache.opencmis.inmemory.ConfigConstants;
import org.apache.opencmis.inmemory.NavigationServiceImpl;
import org.apache.opencmis.inmemory.ObjectServiceImpl;
import org.apache.opencmis.inmemory.RepositoryServiceImpl;
import org.apache.opencmis.inmemory.VersioningServiceImpl;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.inmemory.storedobj.impl.SessionConfigReader;
import org.apache.opencmis.inmemory.storedobj.impl.StoreManagerFactory;
import org.apache.opencmis.inmemory.storedobj.impl.StoreManagerImpl;

/**
 * InMemory test SPI.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * @author Jens
 * 
 */
public class CmisInMemorySpi implements CmisSpi {

  // private static Log log = LogFactory.getLog(CmisInMemorySpi.class);
  private Session fSession;
  private RepositoryService fRepositoryService;
  private NavigationService fNavigationService;
  private ObjectService fObjectService;
  private VersioningService fVersioningService;
  private RepositoryInfoData fRepositoryInfo;
  private StoreManager fStoreManager;
  
  CmisInMemorySpi(Session session) { // package visibility
    fSession = session;
    setup();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#getRepositoryService()
   */
  public RepositoryService getRepositoryService() {
    return fRepositoryService;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.CMISSPIFactory#getSPIInstance(org.apache.opencmis.client.
   * provider.spi.Session)
   */
  public CmisSpi getSpiInstance(Session session) {
    fSession = session;
    setup();
    return this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#getACLService()
   */
  public AclService getAclService() {
    // TODO to be completed if ACLs are implemented
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#getDiscoveryService()
   */
  public DiscoveryService getDiscoveryService() {
    // TODO to be completed if query is implemented
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#getMultiFilingService()
   */
  public MultiFilingService getMultiFilingService() {
    // TODO to be completed if multi-filing implemented
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#getNavigationService()
   */
  public NavigationService getNavigationService() {
    return fNavigationService;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#getObjectService()
   */
  public ObjectService getObjectService() {
    return fObjectService;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#getPolicyService()
   */
  public PolicyService getPolicyService() {
    // TODO to be completed if policies are implemented
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#getRelationshipService()
   */
  public RelationshipService getRelationshipService() {
    // TODO to be completed if relationships are implemented
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#getVersioningService()
   */
  public VersioningService getVersioningService() {
    return fVersioningService;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#clearAllCaches()
   */
  public void clearAllCaches() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#clearRepositoryCache(java.lang.String)
   */
  public void clearRepositoryCache(String repositoryId) {
  }

  
  /**
   * Returns the repository info.
   */
  public RepositoryInfoData getRepositoryInfo() {
    return fRepositoryInfo;
  }

  public String getStoredObjectFactoryName() {
    return StoreManagerImpl.class.getName();
  }
  
  public StoreManager getStoreManager() {
		return fStoreManager;
	}

  // ---- internal ----
    
  private void setup() {
    String repositoryClassName = (String) fSession.get(ConfigConstants.REPOSITORY_CLASS);
    if (null==repositoryClassName)
      repositoryClassName = StoreManagerImpl.class.getName();
    
    fStoreManager = StoreManagerFactory.createInstance(repositoryClassName);
    SessionConfigReader cfgReader = new SessionConfigReader(fSession);
    fStoreManager.setConfigReader(cfgReader);
    String repositoryId  = (String) fSession.get(ConfigConstants.REPOSITORY_ID);
    
    // first create repository
    if (null != repositoryId ) {
      fStoreManager.createRepository(repositoryId);
    }
    
    // then create/initialize type system
    String typeCreatorClassName = (String) fSession.get(ConfigConstants.TYPE_CREATOR_CLASS);
    fStoreManager.initTypeSystem(repositoryId, typeCreatorClassName);

    // then init repository (note: loads root folder which requires cmis:folder type available)
    fStoreManager.initRepository(repositoryId, true);

    String repoInfoCreatorClassName = (String) fSession.get(ConfigConstants.REPOSITORY_INFO_CREATOR_CLASS);
    fStoreManager.initRepositoryInfo(repositoryId, repoInfoCreatorClassName);    

    // initialize services
    fRepositoryService = new RepositoryServiceImpl(fStoreManager);
    fNavigationService = new NavigationServiceImpl(fStoreManager);
    fObjectService = new ObjectServiceImpl(fStoreManager);
    fVersioningService = new VersioningServiceImpl(fStoreManager, fObjectService);    
  }

  
}
