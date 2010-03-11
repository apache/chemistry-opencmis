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
package org.apache.opencmis.client.provider.spi.webservices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.client.provider.spi.CmisSpi;
import org.apache.opencmis.client.provider.spi.CmisSpiFactory;
import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.provider.AclService;
import org.apache.opencmis.commons.provider.DiscoveryService;
import org.apache.opencmis.commons.provider.MultiFilingService;
import org.apache.opencmis.commons.provider.NavigationService;
import org.apache.opencmis.commons.provider.ObjectService;
import org.apache.opencmis.commons.provider.PolicyService;
import org.apache.opencmis.commons.provider.RelationshipService;
import org.apache.opencmis.commons.provider.RepositoryService;
import org.apache.opencmis.commons.provider.VersioningService;

/**
 * CMIS Web Services SPI implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class CmisWebServicesSpi implements CmisSpiFactory, CmisSpi {

  private static Log log = LogFactory.getLog(CmisWebServicesSpi.class);

  private Session fSession;
  private PortProvider fPortProvider;

  private RepositoryService fRepositoryService;
  private NavigationService fNavigationService;
  private ObjectService fObjectService;
  private VersioningService fVersioningService;
  private DiscoveryService fDiscoveryService;
  private MultiFilingService fMultiFilingService;
  private RelationshipService fRelationshipService;
  private PolicyService fPolicyService;
  private AclService fACLService;

  /**
   * Constructor.
   */
  public CmisWebServicesSpi() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.spi.CMISSPIFactory#getSPIInstance(org.apache.opencmis.client
   * .provider .spi.Session)
   */
  public CmisSpi getSpiInstance(Session session) {
    if (log.isDebugEnabled()) {
      log.debug("Initializing Web Services SPI...");
    }

    fSession = session;
    fPortProvider = new PortProvider(fSession);

    fRepositoryService = new RepositoryServiceImpl(fSession, fPortProvider);
    fNavigationService = new NavigationServiceImpl(fSession, fPortProvider);
    fObjectService = new ObjectServiceImpl(fSession, fPortProvider);
    fVersioningService = new VersioningServiceImpl(fSession, fPortProvider);
    fDiscoveryService = new DiscoveryServiceImpl(fSession, fPortProvider);
    fMultiFilingService = new MultiFilingServiceImpl(fSession, fPortProvider);
    fRelationshipService = new RelationshipServiceImpl(fSession, fPortProvider);
    fPolicyService = new PolicyServiceImpl(fSession, fPortProvider);
    fACLService = new AclServiceImpl(fSession, fPortProvider);

    return this;
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
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#getDiscoveryService()
   */
  public DiscoveryService getDiscoveryService() {
    return fDiscoveryService;
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
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#getMultiFilingService()
   */
  public MultiFilingService getMultiFilingService() {
    return fMultiFilingService;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#getRelationshipService()
   */
  public RelationshipService getRelationshipService() {
    return fRelationshipService;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#getPolicyService()
   */
  public PolicyService getPolicyService() {
    return fPolicyService;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.CMISSPI#getACLService()
   */
  public AclService getAclService() {
    return fACLService;
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

}
