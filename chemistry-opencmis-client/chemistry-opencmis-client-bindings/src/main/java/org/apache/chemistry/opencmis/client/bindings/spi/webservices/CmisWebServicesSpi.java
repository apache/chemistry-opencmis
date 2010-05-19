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
import org.apache.chemistry.opencmis.commons.spi.AclService;
import org.apache.chemistry.opencmis.commons.spi.DiscoveryService;
import org.apache.chemistry.opencmis.commons.spi.MultiFilingService;
import org.apache.chemistry.opencmis.commons.spi.NavigationService;
import org.apache.chemistry.opencmis.commons.spi.ObjectService;
import org.apache.chemistry.opencmis.commons.spi.PolicyService;
import org.apache.chemistry.opencmis.commons.spi.RelationshipService;
import org.apache.chemistry.opencmis.commons.spi.RepositoryService;
import org.apache.chemistry.opencmis.commons.spi.VersioningService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CMIS Web Services SPI implementation.
 */
public class CmisWebServicesSpi implements CmisSpiFactory, CmisSpi {

    private static final long serialVersionUID = 1L;

    private static Log log = LogFactory.getLog(CmisWebServicesSpi.class);

    private RepositoryService repositoryService;
    private NavigationService navigationService;
    private ObjectService objectService;
    private VersioningService versioningService;
    private DiscoveryService discoveryService;
    private MultiFilingService multiFilingService;
    private RelationshipService relationshipService;
    private PolicyService policyService;
    private AclService aclService;

    /**
     * Constructor.
     */
    public CmisWebServicesSpi() {
    }

    public CmisSpi getSpiInstance(Session session) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing Web Services SPI...");
        }

        PortProvider portProvider = new PortProvider(session);

        repositoryService = new RepositoryServiceImpl(session, portProvider);
        navigationService = new NavigationServiceImpl(session, portProvider);
        objectService = new ObjectServiceImpl(session, portProvider);
        versioningService = new VersioningServiceImpl(session, portProvider);
        discoveryService = new DiscoveryServiceImpl(session, portProvider);
        multiFilingService = new MultiFilingServiceImpl(session, portProvider);
        relationshipService = new RelationshipServiceImpl(session, portProvider);
        policyService = new PolicyServiceImpl(session, portProvider);
        aclService = new AclServiceImpl(session, portProvider);

        return this;
    }

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    public NavigationService getNavigationService() {
        return navigationService;
    }

    public ObjectService getObjectService() {
        return objectService;
    }

    public DiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    public VersioningService getVersioningService() {
        return versioningService;
    }

    public MultiFilingService getMultiFilingService() {
        return multiFilingService;
    }

    public RelationshipService getRelationshipService() {
        return relationshipService;
    }

    public PolicyService getPolicyService() {
        return policyService;
    }

    public AclService getAclService() {
        return aclService;
    }

    public void clearAllCaches() {
    }

    public void clearRepositoryCache(String repositoryId) {
    }

    public void close() {
        // no-op for Web Services
    }
}
