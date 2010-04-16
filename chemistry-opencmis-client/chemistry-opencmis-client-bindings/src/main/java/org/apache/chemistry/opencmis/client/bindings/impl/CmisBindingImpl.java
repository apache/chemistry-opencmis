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
package org.apache.chemistry.opencmis.client.bindings.impl;

import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.AbstractAuthenticationProvider;
import org.apache.chemistry.opencmis.client.bindings.spi.CmisSpi;
import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.api.AclService;
import org.apache.chemistry.opencmis.commons.api.BindingsObjectFactory;
import org.apache.chemistry.opencmis.commons.api.CmisBinding;
import org.apache.chemistry.opencmis.commons.api.DiscoveryService;
import org.apache.chemistry.opencmis.commons.api.MultiFilingService;
import org.apache.chemistry.opencmis.commons.api.NavigationService;
import org.apache.chemistry.opencmis.commons.api.ObjectService;
import org.apache.chemistry.opencmis.commons.api.PolicyService;
import org.apache.chemistry.opencmis.commons.api.RelationshipService;
import org.apache.chemistry.opencmis.commons.api.RepositoryService;
import org.apache.chemistry.opencmis.commons.api.VersioningService;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;

/**
 * CMIS binding implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class CmisBindingImpl implements CmisBinding {

	private static final long serialVersionUID = 1L;

	private Session fSession;
	private BindingsObjectFactory fObjectFactory;
	private RepositoryService fRepositoryService;

	/**
	 * Constructor.
	 * 
	 * @param sessionParameters
	 *            the session parameters
	 */
	public CmisBindingImpl(Map<String, String> sessionParameters) {
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
			} catch (Exception e) {
				throw new IllegalArgumentException("Could not load authentication provider: " + e, e);
			}

			if (!(authProviderObj instanceof AbstractAuthenticationProvider)) {
				throw new IllegalArgumentException(
						"Authentication provider does not extend AbstractAuthenticationProvider!");
			}

			fSession.put(CmisBindingsHelper.AUTHENTICATION_PROVIDER_OBJECT,
					(AbstractAuthenticationProvider) authProviderObj);
			((AbstractAuthenticationProvider) authProviderObj).setSession(fSession);
		}

		// set up caches
		clearAllCaches();

		// initialize the SPI
		CmisBindingsHelper.getSPI(fSession);

		// set up object factory
		fObjectFactory = new BindingsObjectFactoryImpl();

		// set up repository service
		fRepositoryService = new RepositoryServiceImpl(fSession);
	}

	public RepositoryService getRepositoryService() {
		return fRepositoryService;
	}

	public NavigationService getNavigationService() {
		CmisSpi spi = CmisBindingsHelper.getSPI(fSession);
		return spi.getNavigationService();
	}

	public ObjectService getObjectService() {
		CmisSpi spi = CmisBindingsHelper.getSPI(fSession);
		return spi.getObjectService();
	}

	public DiscoveryService getDiscoveryService() {
		CmisSpi spi = CmisBindingsHelper.getSPI(fSession);
		return spi.getDiscoveryService();
	}

	public RelationshipService getRelationshipService() {
		CmisSpi spi = CmisBindingsHelper.getSPI(fSession);
		return spi.getRelationshipService();
	}

	public VersioningService getVersioningService() {
		CmisSpi spi = CmisBindingsHelper.getSPI(fSession);
		return spi.getVersioningService();
	}

	public AclService getAclService() {
		CmisSpi spi = CmisBindingsHelper.getSPI(fSession);
		return spi.getAclService();
	}

	public MultiFilingService getMultiFilingService() {
		CmisSpi spi = CmisBindingsHelper.getSPI(fSession);
		return spi.getMultiFilingService();
	}

	public PolicyService getPolicyService() {
		CmisSpi spi = CmisBindingsHelper.getSPI(fSession);
		return spi.getPolicyService();
	}

	public BindingsObjectFactory getObjectFactory() {
		return fObjectFactory;
	}

	public void clearAllCaches() {
		fSession.writeLock();
		try {
			fSession.put(CmisBindingsHelper.REPOSITORY_INFO_CACHE, new RepositoryInfoCache(fSession));
			fSession.put(CmisBindingsHelper.TYPE_DEFINTION_CACHE, new TypeDefinitionCache(fSession));

			CmisSpi spi = CmisBindingsHelper.getSPI(fSession);
			spi.clearAllCaches();
		} finally {
			fSession.writeUnlock();
		}
	}

	public void clearRepositoryCache(String repositoryId) {
		if (repositoryId == null) {
			return;
		}

		fSession.writeLock();
		try {
			RepositoryInfoCache repInfoCache = (RepositoryInfoCache) fSession
					.get(CmisBindingsHelper.REPOSITORY_INFO_CACHE);
			repInfoCache.remove(repositoryId);

			TypeDefinitionCache typeDefCache = (TypeDefinitionCache) fSession
					.get(CmisBindingsHelper.TYPE_DEFINTION_CACHE);
			typeDefCache.remove(repositoryId);

			CmisSpi spi = CmisBindingsHelper.getSPI(fSession);
			spi.clearRepositoryCache(repositoryId);
		} finally {
			fSession.writeUnlock();
		}
	}
}
