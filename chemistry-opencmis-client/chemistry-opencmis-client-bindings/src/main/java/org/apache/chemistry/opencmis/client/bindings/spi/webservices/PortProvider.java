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

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.AbstractAuthenticationProvider;
import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.ACLService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.ACLServicePort;
import org.apache.chemistry.opencmis.commons.impl.jaxb.DiscoveryService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.DiscoveryServicePort;
import org.apache.chemistry.opencmis.commons.impl.jaxb.MultiFilingService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.MultiFilingServicePort;
import org.apache.chemistry.opencmis.commons.impl.jaxb.NavigationService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.NavigationServicePort;
import org.apache.chemistry.opencmis.commons.impl.jaxb.ObjectService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.ObjectServicePort;
import org.apache.chemistry.opencmis.commons.impl.jaxb.PolicyService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.PolicyServicePort;
import org.apache.chemistry.opencmis.commons.impl.jaxb.RelationshipService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.RelationshipServicePort;
import org.apache.chemistry.opencmis.commons.impl.jaxb.RepositoryService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.RepositoryServicePort;
import org.apache.chemistry.opencmis.commons.impl.jaxb.VersioningService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.VersioningServicePort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.developer.StreamingAttachmentFeature;
import com.sun.xml.ws.developer.WSBindingProvider;

/**
 * Provides CMIS Web Services port objects. Handles authentication headers.
 */
public class PortProvider {

    private static Log log = LogFactory.getLog(PortProvider.class);

    public static final String CMIS_NAMESPACE = "http://docs.oasis-open.org/ns/cmis/ws/200908/";

    public static final String REPOSITORY_SERVICE = "RepositoryService";
    public static final String OBJECT_SERVICE = "ObjectService";
    public static final String DISCOVERY_SERVICE = "DiscoveryService";
    public static final String NAVIGATION_SERVICE = "NavigationService";
    public static final String MULTIFILING_SERVICE = "MultiFilingService";
    public static final String VERSIONING_SERVICE = "VersioningService";
    public static final String RELATIONSHIP_SERVICE = "RelationshipService";
    public static final String POLICY_SERVICE = "PolicyService";
    public static final String ACL_SERVICE = "ACLService";

    private static final int CHUNK_SIZE = 64 * 1024;

    private final Session fSession;

    /**
     * Constructor.
     */
    public PortProvider(Session session) {
        fSession = session;
    }

    /**
     * Return the Repository Service port object.
     */
    public RepositoryServicePort getRepositoryServicePort() {
        return (RepositoryServicePort) getPortObject(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE);
    }

    /**
     * Return the Navigation Service port object.
     */
    public NavigationServicePort getNavigationServicePort() {
        return (NavigationServicePort) getPortObject(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE);
    }

    /**
     * Return the Object Service port object.
     */
    public ObjectServicePort getObjectServicePort() {
        return (ObjectServicePort) getPortObject(SessionParameter.WEBSERVICES_OBJECT_SERVICE);
    }

    /**
     * Return the Versioning Service port object.
     */
    public VersioningServicePort getVersioningServicePort() {
        return (VersioningServicePort) getPortObject(SessionParameter.WEBSERVICES_VERSIONING_SERVICE);
    }

    /**
     * Return the Discovery Service port object.
     */
    public DiscoveryServicePort getDiscoveryServicePort() {
        return (DiscoveryServicePort) getPortObject(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE);
    }

    /**
     * Return the MultiFiling Service port object.
     */
    public MultiFilingServicePort getMultiFilingServicePort() {
        return (MultiFilingServicePort) getPortObject(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE);
    }

    /**
     * Return the Relationship Service port object.
     */
    public RelationshipServicePort getRelationshipServicePort() {
        return (RelationshipServicePort) getPortObject(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE);
    }

    /**
     * Return the Policy Service port object.
     */
    public PolicyServicePort getPolicyServicePort() {
        return (PolicyServicePort) getPortObject(SessionParameter.WEBSERVICES_POLICY_SERVICE);
    }

    /**
     * Return the ACL Service port object.
     */
    public ACLServicePort getACLServicePort() {
        return (ACLServicePort) getPortObject(SessionParameter.WEBSERVICES_ACL_SERVICE);
    }

    // ---- internal ----

    /**
     * Gets a port object from the session or (re-)initializes the port objects.
     */
    @SuppressWarnings("unchecked")
    private Object getPortObject(String serviceKey) {
        Map<String, Service> serviceMap = (Map<String, Service>) fSession.get(SpiSessionParameter.SERVICES);

        // does the service map exist?
        if (serviceMap == null) {
            fSession.writeLock();
            try {
                // try again
                serviceMap = (Map<String, Service>) fSession.get(SpiSessionParameter.SERVICES);
                if (serviceMap == null) {
                    serviceMap = Collections.synchronizedMap(new HashMap<String, Service>());
                    fSession.put(SpiSessionParameter.SERVICES, serviceMap, true);
                }

                if (serviceMap.containsKey(serviceKey)) {
                    return createPortObject(serviceMap.get(serviceKey));
                }

                // create service object
                Service serviceObject = initServiceObject(serviceKey);
                serviceMap.put(serviceKey, serviceObject);

                // create port object
                return createPortObject(serviceObject);
            } finally {
                fSession.writeUnlock();
            }
        }

        // is the service in the service map?
        if (!serviceMap.containsKey(serviceKey)) {
            fSession.writeLock();
            try {
                // try again
                if (serviceMap.containsKey(serviceKey)) {
                    return createPortObject(serviceMap.get(serviceKey));
                }

                // create object
                Service serviceObject = initServiceObject(serviceKey);
                serviceMap.put(serviceKey, serviceObject);

                return createPortObject(serviceObject);
            } finally {
                fSession.writeUnlock();
            }
        }

        return createPortObject(serviceMap.get(serviceKey));
    }

    /**
     * Creates a service object.
     */
    private Service initServiceObject(String serviceKey) {
        Service serviceObject = null;

        if (log.isDebugEnabled()) {
            log.debug("Initializing Web Service " + serviceKey + "...");
        }

        try {
            // get WSDL URL
            URL wsdlUrl = new URL((String) fSession.get(serviceKey));

            // build the requested service object
            if (SessionParameter.WEBSERVICES_REPOSITORY_SERVICE.equals(serviceKey)) {
                serviceObject = new RepositoryService(wsdlUrl, new QName(CMIS_NAMESPACE, REPOSITORY_SERVICE));
            } else if (SessionParameter.WEBSERVICES_NAVIGATION_SERVICE.equals(serviceKey)) {
                serviceObject = new NavigationService(wsdlUrl, new QName(CMIS_NAMESPACE, NAVIGATION_SERVICE));
            } else if (SessionParameter.WEBSERVICES_OBJECT_SERVICE.equals(serviceKey)) {
                serviceObject = new ObjectService(wsdlUrl, new QName(CMIS_NAMESPACE, OBJECT_SERVICE));
            } else if (SessionParameter.WEBSERVICES_VERSIONING_SERVICE.equals(serviceKey)) {
                serviceObject = new VersioningService(wsdlUrl, new QName(CMIS_NAMESPACE, VERSIONING_SERVICE));
            } else if (SessionParameter.WEBSERVICES_DISCOVERY_SERVICE.equals(serviceKey)) {
                serviceObject = new DiscoveryService(wsdlUrl, new QName(CMIS_NAMESPACE, DISCOVERY_SERVICE));
            } else if (SessionParameter.WEBSERVICES_MULTIFILING_SERVICE.equals(serviceKey)) {
                serviceObject = new MultiFilingService(wsdlUrl, new QName(CMIS_NAMESPACE, MULTIFILING_SERVICE));
            } else if (SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE.equals(serviceKey)) {
                serviceObject = new RelationshipService(wsdlUrl, new QName(CMIS_NAMESPACE, RELATIONSHIP_SERVICE));
            } else if (SessionParameter.WEBSERVICES_POLICY_SERVICE.equals(serviceKey)) {
                serviceObject = new PolicyService(wsdlUrl, new QName(CMIS_NAMESPACE, POLICY_SERVICE));
            } else if (SessionParameter.WEBSERVICES_ACL_SERVICE.equals(serviceKey)) {
                serviceObject = new ACLService(wsdlUrl, new QName(CMIS_NAMESPACE, ACL_SERVICE));
            } else {
                throw new CmisRuntimeException("Cannot find Web Services service object [" + serviceKey + "]!");
            }
        } catch (CmisBaseException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CmisConnectionException("Cannot initalize Web Services service object [" + serviceKey + "]: "
                    + e.getMessage(), e);
        }

        return serviceObject;
    }

    /**
     * Creates a port object.
     */
    private Object createPortObject(Service service) {
        Object portObject = null;

        if (log.isDebugEnabled()) {
            log.debug("Creating Web Service port object of " + (service == null ? "???" : service.getServiceName())
                    + "...");
        }

        try {
            if (service instanceof RepositoryService) {
                portObject = ((RepositoryService) service).getRepositoryServicePort(new MTOMFeature());
            } else if (service instanceof NavigationService) {
                portObject = ((NavigationService) service).getNavigationServicePort(new MTOMFeature());
            } else if (service instanceof ObjectService) {
                int threshold = 4 * 1024 * 1024;
                try {
                    threshold = Integer.parseInt((String) fSession.get(SessionParameter.WEBSERVICES_MEMORY_THRESHOLD));
                } catch (Exception e) {
                }
                portObject = ((ObjectService) service).getObjectServicePort(new MTOMFeature(),
                        new StreamingAttachmentFeature(null, true, threshold));
                ((BindingProvider) portObject).getRequestContext().put(
                        JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, CHUNK_SIZE);
            } else if (service instanceof VersioningService) {
                portObject = ((VersioningService) service).getVersioningServicePort(new MTOMFeature());
            } else if (service instanceof DiscoveryService) {
                portObject = ((DiscoveryService) service).getDiscoveryServicePort(new MTOMFeature());
            } else if (service instanceof MultiFilingService) {
                portObject = ((MultiFilingService) service).getMultiFilingServicePort(new MTOMFeature());
            } else if (service instanceof RelationshipService) {
                portObject = ((RelationshipService) service).getRelationshipServicePort(new MTOMFeature());
            } else if (service instanceof PolicyService) {
                portObject = ((PolicyService) service).getPolicyServicePort(new MTOMFeature());
            } else if (service instanceof ACLService) {
                portObject = ((ACLService) service).getACLServicePort(new MTOMFeature());
            } else {
                throw new CmisRuntimeException("Cannot find Web Services service object!");
            }

            // add SOAP and HTTP authentication headers
            AbstractAuthenticationProvider authProvider = CmisBindingsHelper.getAuthenticationProvider(fSession);
            if (authProvider != null) {
                // SOAP header
                Element soapHeader = authProvider.getSOAPHeaders(portObject);
                if (soapHeader != null) {
                    ((WSBindingProvider) portObject).setOutboundHeaders(Headers.create(soapHeader));
                }

                // HTTP header
                Map<String, List<String>> httpHeaders = authProvider.getHTTPHeaders(service.getWSDLDocumentLocation()
                        .toString());
                if (httpHeaders != null) {
                    ((BindingProvider) portObject).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS,
                            httpHeaders);
                }
            }
        } catch (CmisBaseException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CmisConnectionException("Cannot initalize Web Services port object: " + e.getMessage(), e);
        }

        return portObject;
    }
}
