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
package org.apache.opencmis.client.provider.impl;

import java.util.Map;

import org.apache.opencmis.client.provider.spi.AbstractAuthenticationProvider;
import org.apache.opencmis.client.provider.spi.CmisSpi;
import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.impl.dataobjects.ProviderObjectFactoryImpl;
import org.apache.opencmis.commons.provider.AclService;
import org.apache.opencmis.commons.provider.CmisProvider;
import org.apache.opencmis.commons.provider.DiscoveryService;
import org.apache.opencmis.commons.provider.MultiFilingService;
import org.apache.opencmis.commons.provider.NavigationService;
import org.apache.opencmis.commons.provider.ObjectService;
import org.apache.opencmis.commons.provider.PolicyService;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;
import org.apache.opencmis.commons.provider.RelationshipService;
import org.apache.opencmis.commons.provider.RepositoryService;
import org.apache.opencmis.commons.provider.VersioningService;

/**
 * CMIS provider implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class CmisProviderImpl implements CmisProvider {

  private static final long serialVersionUID = 1L;

  private Session fSession;
  private ProviderObjectFactory fObjectFactory;
  private RepositoryService fRepositoryService;

  /**
   * Constructor.
   * 
   * @param sessionParameters
   *          the session parameters
   */
  public CmisProviderImpl(Map<String, String> sessionParameters) {
    // some checks first
    if (sessionParameters == null) {
      throw new IllegalArgumentException("Session parameters must be set!");
    }
    if (!sessionParameters.containsKey(SessionParameter.BINDING_SPI_CLASS)) {
      throw new IllegalArgumentException("Session parameters do not contain a SPI class name!");
    }

    // initialize session
    fSession = new SessionImpl();
    for (Map.Entry<String, String> entry : sessionParameters.entrySet()) {
      fSession.put(entry.getKey(), entry.getValue());
    }

    // create authentication provider and add it session
    String authProvider = sessionParameters.get(SessionParameter.AUTHENTICATION_PROVIDER_CLASS);
    if (authProvider != null) {
      Object authProviderObj = null;

      try {
        authProviderObj = Class.forName(authProvider).newInstance();
      }
      catch (Exception e) {
        throw new IllegalArgumentException("Could not load authentication provider: " + e, e);
      }

      if (!(authProviderObj instanceof AbstractAuthenticationProvider)) {
        throw new IllegalArgumentException(
            "Authentication provider does not extend AbstractAuthenticationProvider!");
      }

      fSession.put(CmisProviderHelper.AUTHENTICATION_PROVIDER_OBJECT,
          (AbstractAuthenticationProvider) authProviderObj);
      ((AbstractAuthenticationProvider) authProviderObj).setSession(fSession);
    }

    // set up caches
    clearAllCaches();

    // initialize the SPI
    CmisProviderHelper.getSPI(fSession);

    // set up object factory
    fObjectFactory = new ProviderObjectFactoryImpl();

    // set up repository service
    fRepositoryService = new RepositoryServiceImpl(fSession);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.CMISProvider#getRepositoryService()
   */
  public RepositoryService getRepositoryService() {
    return fRepositoryService;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.CMISProvider#getNavigationService()
   */
  public NavigationService getNavigationService() {
    CmisSpi spi = CmisProviderHelper.getSPI(fSession);
    return spi.getNavigationService();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.CMISProvider#getObjectService()
   */
  public ObjectService getObjectService() {
    CmisSpi spi = CmisProviderHelper.getSPI(fSession);
    return spi.getObjectService();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.CMISProvider#getDiscoveryService()
   */
  public DiscoveryService getDiscoveryService() {
    CmisSpi spi = CmisProviderHelper.getSPI(fSession);
    return spi.getDiscoveryService();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.CMISProvider#getRelationshipService()
   */
  public RelationshipService getRelationshipService() {
    CmisSpi spi = CmisProviderHelper.getSPI(fSession);
    return spi.getRelationshipService();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.CMISProvider#getVersioningService()
   */
  public VersioningService getVersioningService() {
    CmisSpi spi = CmisProviderHelper.getSPI(fSession);
    return spi.getVersioningService();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.CMISProvider#getACLService()
   */
  public AclService getAclService() {
    CmisSpi spi = CmisProviderHelper.getSPI(fSession);
    return spi.getAclService();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.CMISProvider#getMultiFilingService()
   */
  public MultiFilingService getMultiFilingService() {
    CmisSpi spi = CmisProviderHelper.getSPI(fSession);
    return spi.getMultiFilingService();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.CMISProvider#getPolicyService()
   */
  public PolicyService getPolicyService() {
    CmisSpi spi = CmisProviderHelper.getSPI(fSession);
    return spi.getPolicyService();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.CMISProvider#getObjectFactory()
   */
  public ProviderObjectFactory getObjectFactory() {
    return fObjectFactory;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.CMISProvider#clearAllCaches()
   */
  public void clearAllCaches() {
    fSession.writeLock();
    try {
      fSession.put(CmisProviderHelper.REPOSITORY_INFO_CACHE, new RepositoryInfoCache(fSession));
      fSession.put(CmisProviderHelper.TYPE_DEFINTION_CACHE, new TypeDefinitionCache(fSession));

      CmisSpi spi = CmisProviderHelper.getSPI(fSession);
      spi.clearAllCaches();
    }
    finally {
      fSession.writeUnlock();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.CMISProvider#clearRepositoryCache(java.lang.String)
   */
  public void clearRepositoryCache(String repositoryId) {
    if (repositoryId == null) {
      return;
    }

    fSession.writeLock();
    try {
      RepositoryInfoCache repInfoCache = (RepositoryInfoCache) fSession
          .get(CmisProviderHelper.REPOSITORY_INFO_CACHE);
      repInfoCache.remove(repositoryId);

      TypeDefinitionCache typeDefCache = (TypeDefinitionCache) fSession
          .get(CmisProviderHelper.TYPE_DEFINTION_CACHE);
      typeDefCache.remove(repositoryId);

      CmisSpi spi = CmisProviderHelper.getSPI(fSession);
      spi.clearRepositoryCache(repositoryId);
    }
    finally {
      fSession.writeUnlock();
    }
  }
}
