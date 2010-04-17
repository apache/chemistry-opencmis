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
package org.apache.chemistry.opencmis.client.bindings.spi.webservices;

import org.apache.chemistry.opencmis.client.bindings.spi.CmisSpi;
import org.apache.chemistry.opencmis.client.bindings.spi.CmisSpiFactory;
import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.commons.api.AclService;
import org.apache.chemistry.opencmis.commons.api.DiscoveryService;
import org.apache.chemistry.opencmis.commons.api.MultiFilingService;
import org.apache.chemistry.opencmis.commons.api.NavigationService;
import org.apache.chemistry.opencmis.commons.api.ObjectService;
import org.apache.chemistry.opencmis.commons.api.PolicyService;
import org.apache.chemistry.opencmis.commons.api.RelationshipService;
import org.apache.chemistry.opencmis.commons.api.RepositoryService;
import org.apache.chemistry.opencmis.commons.api.VersioningService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	 * org.apache.opencmis.client.provider.spi.CMISSPIFactory#getSPIInstance
	 * (org.apache.opencmis.client .provider .spi.Session)
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

	public RepositoryService getRepositoryService() {
		return fRepositoryService;
	}

	public NavigationService getNavigationService() {
		return fNavigationService;
	}

	public ObjectService getObjectService() {
		return fObjectService;
	}

	public DiscoveryService getDiscoveryService() {
		return fDiscoveryService;
	}

	public VersioningService getVersioningService() {
		return fVersioningService;
	}

	public MultiFilingService getMultiFilingService() {
		return fMultiFilingService;
	}

	public RelationshipService getRelationshipService() {
		return fRelationshipService;
	}

	public PolicyService getPolicyService() {
		return fPolicyService;
	}

	public AclService getAclService() {
		return fACLService;
	}

	public void clearAllCaches() {
	}

	public void clearRepositoryCache(String repositoryId) {
	}

	public void close() {
		// no-op for Web Services
	}
}
