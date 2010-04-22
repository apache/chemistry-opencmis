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
package org.apache.chemistry.opencmis.client.bindings.spi.atompub;

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
 * CMIS AtomPub SPI implementation.
 */
public class CmisAtomPubSpi implements CmisSpiFactory, CmisSpi {

    private static final long serialVersionUID = 1L;

    private static Log log = LogFactory.getLog(CmisAtomPubSpi.class);

    private Session session;

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
    public CmisAtomPubSpi() {
    }

    public CmisSpi getSpiInstance(Session session) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing AtomPub SPI...");
        }

        this.session = session;

        repositoryService = new RepositoryServiceImpl(session);
        navigationService = new NavigationServiceImpl(session);
        objectService = new ObjectServiceImpl(session);
        versioningService = new VersioningServiceImpl(session);
        discoveryService = new DiscoveryServiceImpl(session);
        multiFilingService = new MultiFilingServiceImpl(session);
        relationshipService = new RelationshipServiceImpl(session);
        policyService = new PolicyServiceImpl(session);
        aclService = new AclServiceImpl(session);

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
        session.remove(SpiSessionParameter.LINK_CACHE);
    }

    public void clearRepositoryCache(String repositoryId) {
        LinkCache linkCache = (LinkCache) session.get(SpiSessionParameter.LINK_CACHE);
        if (linkCache != null) {
            linkCache.clearRepository(repositoryId);
        }
    }

    public void close() {
        // no-op for AtomPub
    }
}
