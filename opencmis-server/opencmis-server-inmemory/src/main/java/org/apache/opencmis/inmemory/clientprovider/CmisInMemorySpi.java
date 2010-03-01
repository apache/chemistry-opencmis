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

import java.util.HashMap;
import java.util.Map;

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
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.inmemory.storedobj.impl.StoreManagerImpl;

/**
 * InMemory test SPI.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * @author Jens
 * 
 */
public class CmisInMemorySpi extends CmisInMemoryProvider implements CmisSpi {

  // private static Log log = LogFactory.getLog(CmisInMemorySpi.class);
  private Session fSession;
  
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
    return fMultiService;
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
    Map<String, String> cfgParams = new HashMap<String, String>();
    String[] configParamKeys = {ConfigConstants.REPOSITORY_CLASS, ConfigConstants.REPOSITORY_ID, 
        ConfigConstants.TYPE_CREATOR_CLASS };

    
    for (String key : configParamKeys) {
      String value = (String) fSession.get(key);
      if (null != value)
        cfgParams.put(key, value);
    }
    

    super.setup(cfgParams);
  }

  
}
