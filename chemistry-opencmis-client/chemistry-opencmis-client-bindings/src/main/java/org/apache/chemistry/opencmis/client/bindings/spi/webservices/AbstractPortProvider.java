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

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPException;

import org.apache.chemistry.opencmis.client.bindings.impl.ClientVersion;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisProxyAuthenticationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
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
import org.apache.chemistry.opencmis.commons.impl.webservices.ObjectServicePort10;
import org.apache.chemistry.opencmis.commons.impl.webservices.RepositoryServicePort10;
import org.apache.chemistry.opencmis.commons.spi.AuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPortProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPortProvider.class);

    protected static final int CHUNK_SIZE = (64 * 1024) - 1;

    protected enum CmisWebSerivcesService {
        REPOSITORY_SERVICE("RepositoryService", false, RepositoryService.class, RepositoryServicePort10.class,
                RepositoryServicePort.class, SessionParameter.WEBSERVICES_REPOSITORY_SERVICE,
                SessionParameter.WEBSERVICES_REPOSITORY_SERVICE_ENDPOINT),

        NAVIGATION_SERVICE("NavigationService", false, NavigationService.class, NavigationServicePort.class,
                NavigationServicePort.class, SessionParameter.WEBSERVICES_NAVIGATION_SERVICE,
                SessionParameter.WEBSERVICES_NAVIGATION_SERVICE_ENDPOINT),

        OBJECT_SERVICE("ObjectService", true, ObjectService.class, ObjectServicePort10.class, ObjectServicePort.class,
                SessionParameter.WEBSERVICES_OBJECT_SERVICE, SessionParameter.WEBSERVICES_OBJECT_SERVICE_ENDPOINT),

        VERSIONING_SERVICE("VersioningService", true, VersioningService.class, VersioningServicePort.class,
                VersioningServicePort.class, SessionParameter.WEBSERVICES_VERSIONING_SERVICE,
                SessionParameter.WEBSERVICES_VERSIONING_SERVICE_ENDPOINT),

        DISCOVERY_SERVICE("DiscoveryService", false, DiscoveryService.class, DiscoveryServicePort.class,
                DiscoveryServicePort.class, SessionParameter.WEBSERVICES_DISCOVERY_SERVICE,
                SessionParameter.WEBSERVICES_DISCOVERY_SERVICE_ENDPOINT),

        MULTIFILING_SERVICE("MultiFilingService", false, MultiFilingService.class, MultiFilingServicePort.class,
                MultiFilingServicePort.class, SessionParameter.WEBSERVICES_MULTIFILING_SERVICE,
                SessionParameter.WEBSERVICES_MULTIFILING_SERVICE_ENDPOINT),

        RELATIONSHIP_SERVICE("RelationshipService", false, RelationshipService.class, RelationshipServicePort.class,
                RelationshipServicePort.class, SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE,
                SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE_ENDPOINT),

        POLICY_SERVICE("PolicyService", false, PolicyService.class, PolicyServicePort.class, PolicyServicePort.class,
                SessionParameter.WEBSERVICES_POLICY_SERVICE, SessionParameter.WEBSERVICES_POLICY_SERVICE_ENDPOINT),

        ACL_SERVICE("ACLService", false, ACLService.class, ACLServicePort.class, ACLServicePort.class,
                SessionParameter.WEBSERVICES_ACL_SERVICE, SessionParameter.WEBSERVICES_ACL_SERVICE_ENDPOINT);

        private String name;
        private QName qname;
        private boolean handlesContent;
        private Class<? extends Service> serviceClass;
        private Class<?> port10Class;
        private Class<?> port11Class;
        private String wsdlKey;
        private String endpointKey;

        CmisWebSerivcesService(String localname, boolean handlesContent, Class<? extends Service> serviceClass,
                Class<?> port10Class, Class<?> port11Class, String wsdlKey, String endpointKey) {
            this.name = localname;
            this.qname = new QName("http://docs.oasis-open.org/ns/cmis/ws/200908/", localname);
            this.handlesContent = handlesContent;
            this.serviceClass = serviceClass;
            this.port10Class = port10Class;
            this.port11Class = port11Class;
            this.wsdlKey = wsdlKey;
            this.endpointKey = endpointKey;
        }

        public String getServiceName() {
            return name;
        }

        public QName getQName() {
            return qname;
        }

        public boolean handlesContent() {
            return handlesContent;
        }

        public Class<? extends Service> getServiceClass() {
            return serviceClass;
        }

        public Class<?> getPort10Class() {
            return port10Class;
        }

        public Class<?> getPort11Class() {
            return port11Class;
        }

        public String getWsdlKey() {
            return wsdlKey;
        }

        public String getEndpointKey() {
            return endpointKey;
        }
    }

    static class CmisServiceHolder {
        private CmisWebSerivcesService service;
        private Service serviceObject;
        private URL endpointUrl;

        public CmisServiceHolder(CmisWebSerivcesService service, Service serviceObject, URL endpointUrl) {
            this.service = service;
            this.serviceObject = serviceObject;
            this.endpointUrl = endpointUrl;
        }

        public CmisWebSerivcesService getService() {
            return service;
        }

        public Service getServiceObject() {
            return serviceObject;
        }

        public URL getEndpointUrl() {
            return endpointUrl;
        }

        public String getServiceName() {
            return service.getServiceName();
        }
    }

    private BindingSession session;
    protected boolean useCompression;
    protected boolean useClientCompression;
    protected String acceptLanguage;

    public BindingSession getSession() {
        return session;
    }

    public void setSession(BindingSession session) {
        this.session = session;

        Object compression = session.get(SessionParameter.COMPRESSION);
        useCompression = (compression != null) && Boolean.parseBoolean(compression.toString());

        Object clientCompression = session.get(SessionParameter.CLIENT_COMPRESSION);
        useClientCompression = (clientCompression != null) && Boolean.parseBoolean(clientCompression.toString());

        if (session.get(CmisBindingsHelper.ACCEPT_LANGUAGE) instanceof String) {
            acceptLanguage = session.get(CmisBindingsHelper.ACCEPT_LANGUAGE).toString();
        }
    }

    /**
     * Return the Repository Service port object.
     */
    public RepositoryServicePort getRepositoryServicePort() {
        return (RepositoryServicePort) getPortObject(CmisWebSerivcesService.REPOSITORY_SERVICE);
    }

    /**
     * Return the Navigation Service port object.
     */
    public NavigationServicePort getNavigationServicePort() {
        return (NavigationServicePort) getPortObject(CmisWebSerivcesService.NAVIGATION_SERVICE);
    }

    /**
     * Return the Object Service port object.
     */
    public ObjectServicePort getObjectServicePort() {
        return (ObjectServicePort) getPortObject(CmisWebSerivcesService.OBJECT_SERVICE);
    }

    /**
     * Return the Versioning Service port object.
     */
    public VersioningServicePort getVersioningServicePort() {
        return (VersioningServicePort) getPortObject(CmisWebSerivcesService.VERSIONING_SERVICE);
    }

    /**
     * Return the Discovery Service port object.
     */
    public DiscoveryServicePort getDiscoveryServicePort() {
        return (DiscoveryServicePort) getPortObject(CmisWebSerivcesService.DISCOVERY_SERVICE);
    }

    /**
     * Return the MultiFiling Service port object.
     */
    public MultiFilingServicePort getMultiFilingServicePort() {
        return (MultiFilingServicePort) getPortObject(CmisWebSerivcesService.MULTIFILING_SERVICE);
    }

    /**
     * Return the Relationship Service port object.
     */
    public RelationshipServicePort getRelationshipServicePort() {
        return (RelationshipServicePort) getPortObject(CmisWebSerivcesService.RELATIONSHIP_SERVICE);
    }

    /**
     * Return the Policy Service port object.
     */
    public PolicyServicePort getPolicyServicePort() {
        return (PolicyServicePort) getPortObject(CmisWebSerivcesService.POLICY_SERVICE);
    }

    /**
     * Return the ACL Service port object.
     */
    public ACLServicePort getACLServicePort() {
        return (ACLServicePort) getPortObject(CmisWebSerivcesService.ACL_SERVICE);
    }

    public void endCall(Object portObject) {
        AuthenticationProvider authProvider = CmisBindingsHelper.getAuthenticationProvider(session);
        if (authProvider != null && portObject instanceof BindingProvider) {
            BindingProvider bp = (BindingProvider) portObject;
            String url = (String) bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            @SuppressWarnings("unchecked")
            Map<String, List<String>> headers = (Map<String, List<String>>) bp.getResponseContext().get(
                    MessageContext.HTTP_RESPONSE_HEADERS);
            Integer statusCode = (Integer) bp.getResponseContext().get(MessageContext.HTTP_RESPONSE_CODE);
            authProvider.putResponseHeaders(url, statusCode == null ? -1 : statusCode, headers);
        }
    }

    // ---- internal ----

    @SuppressWarnings("unchecked")
    protected BindingProvider getPortObject(CmisWebSerivcesService service) {
        Map<String, CmisServiceHolder> serviceMap = (Map<String, CmisServiceHolder>) session
                .get(SpiSessionParameter.SERVICES);

        // does the service map exist?
        if (serviceMap == null) {
            session.writeLock();
            try {
                // try again
                serviceMap = (Map<String, CmisServiceHolder>) session.get(SpiSessionParameter.SERVICES);
                if (serviceMap == null) {
                    serviceMap = Collections.synchronizedMap(new HashMap<String, CmisServiceHolder>());
                    session.put(SpiSessionParameter.SERVICES, serviceMap, true);
                }

                if (serviceMap.containsKey(service.getServiceName())) {
                    return createPortObject(serviceMap.get(service.getServiceName()));
                }

                // create service object
                CmisServiceHolder serviceholder = initServiceObject(service);
                serviceMap.put(service.getServiceName(), serviceholder);

                // create port object
                return createPortObject(serviceholder);
            } finally {
                session.writeUnlock();
            }
        }

        // is the service in the service map?
        if (!serviceMap.containsKey(service.getServiceName())) {
            session.writeLock();
            try {
                // try again
                if (serviceMap.containsKey(service.getServiceName())) {
                    return createPortObject(serviceMap.get(service.getServiceName()));
                }

                // create object
                CmisServiceHolder serviceholder = initServiceObject(service);
                serviceMap.put(service.getServiceName(), serviceholder);

                return createPortObject(serviceholder);
            } finally {
                session.writeUnlock();
            }
        }

        return createPortObject(serviceMap.get(service.getServiceName()));
    }

    /**
     * Creates a service object.
     */
    protected CmisServiceHolder initServiceObject(CmisWebSerivcesService service) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Initializing Web Service " + service.getServiceName() + "...");
        }

        try {
            // get URLs
            URL wsdlUrl = null;
            URL endpointUrl = null;

            String wsdlUrlStr = (String) session.get(service.getWsdlKey());
            if (wsdlUrlStr != null) {
                wsdlUrl = new URL(wsdlUrlStr);
            } else {
                String endpointUrlStr = (String) session.get(service.getEndpointKey());
                if (endpointUrlStr != null) {
                    endpointUrl = new URL(endpointUrlStr);
                }
            }

            if (wsdlUrl == null && endpointUrl == null) {
                throw new CmisRuntimeException("Neither a WSDL URL nor an endpoint URL is specified for the service "
                        + service.getServiceName() + "!");
            }

            // build the requested service object
            Constructor<? extends Service> serviceConstructor = service.getServiceClass().getConstructor(
                    new Class<?>[] { URL.class, QName.class });
            Service serviceObject = serviceConstructor.newInstance(new Object[] { wsdlUrl, service.getQName() });

            return new CmisServiceHolder(service, serviceObject, endpointUrl);
        } catch (CmisBaseException ce) {
            throw ce;
        } catch (Exception e) {
            String message = "Cannot initalize Web Services service object [" + service.getServiceName() + "]: "
                    + e.getMessage();

            if (e instanceof HTTPException) {
                HTTPException he = (HTTPException) e;
                if (he.getStatusCode() == 401) {
                    throw new CmisUnauthorizedException(message, e);
                } else if (he.getStatusCode() == 407) {
                    throw new CmisProxyAuthenticationException(message, e);
                }
            }

            throw new CmisConnectionException(message, e);
        }
    }

    /**
     * Sets the default HTTP headers on a {@link BindingProvider} object.
     */
    protected void setHTTPHeaders(BindingProvider portObject, Map<String, List<String>> httpHeaders) {
        if (httpHeaders == null) {
            httpHeaders = new HashMap<String, List<String>>();
        }

        // CMIS client header
        httpHeaders.put("X-CMIS-Client", Collections.singletonList(ClientVersion.OPENCMIS_CLIENT));

        // compression
        if (useCompression) {
            httpHeaders.put("Accept-Encoding", Collections.singletonList("gzip"));
        }

        // client compression
        if (useClientCompression) {
            httpHeaders.put("Content-Encoding", Collections.singletonList("gzip"));
        }

        // locale
        if (acceptLanguage != null) {
            httpHeaders.put("Accept-Language", Collections.singletonList(acceptLanguage));
        }

        portObject.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);
    }

    /**
     * Sets the endpoint URL if the URL is not <code>null</code>.
     */
    protected void setEndpointUrl(BindingProvider portObject, URL endpointUrl) {
        if (endpointUrl == null) {
            return;
        }

        portObject.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl.toString());
    }

    /**
     * Creates a simple port object from a CmisServiceHolder object.
     */
    protected BindingProvider createPortObjectFromServiceHolder(CmisServiceHolder serviceHolder,
            WebServiceFeature... features) {
        return (BindingProvider) serviceHolder.getServiceObject().getPort(serviceHolder.getService().getPort10Class(),
                features);
    }

    /**
     * Creates a port object.
     */
    protected abstract BindingProvider createPortObject(CmisServiceHolder serviceHolder);
}
