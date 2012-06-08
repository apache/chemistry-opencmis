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

import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.ACLService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.DiscoveryService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.MultiFilingService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.NavigationService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.ObjectService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.PolicyService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.RelationshipService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.RepositoryService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.VersioningService;
import org.apache.chemistry.opencmis.commons.spi.AuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.developer.StreamingAttachmentFeature;
import com.sun.xml.ws.developer.WSBindingProvider;

/**
 * Provides CMIS Web Services port objects for JAX-WS RI. Handles authentication
 * headers.
 */
public class PortProvider extends AbstractPortProvider {
    private static final Logger log = LoggerFactory.getLogger(PortProvider.class);

    /**
     * Creates a port object.
     */
    protected Object createPortObject(Service service) {

        if (log.isDebugEnabled()) {
            log.debug("Creating Web Service port object of " + (service == null ? "???" : service.getServiceName())
                    + "...");
        }

        Object portObject;
        try {
            if (service instanceof RepositoryService) {
                portObject = ((RepositoryService) service).getRepositoryServicePort(new MTOMFeature());
            } else if (service instanceof NavigationService) {
                portObject = ((NavigationService) service).getNavigationServicePort(new MTOMFeature());
            } else if (service instanceof ObjectService) {
                int threshold = getSession().get(SessionParameter.WEBSERVICES_MEMORY_THRESHOLD, 4 * 1024 * 1024);
                portObject = ((ObjectService) service).getObjectServicePort(new MTOMFeature(),
                        new StreamingAttachmentFeature(null, true, threshold));
                ((BindingProvider) portObject).getRequestContext().put(
                        JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, CHUNK_SIZE);
            } else if (service instanceof VersioningService) {
                int threshold = getSession().get(SessionParameter.WEBSERVICES_MEMORY_THRESHOLD, 4 * 1024 * 1024);
                portObject = ((VersioningService) service).getVersioningServicePort(new MTOMFeature(),
                        new StreamingAttachmentFeature(null, true, threshold));
                ((BindingProvider) portObject).getRequestContext().put(
                        JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, CHUNK_SIZE);
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
            AuthenticationProvider authProvider = CmisBindingsHelper.getAuthenticationProvider(getSession());
            Map<String, List<String>> httpHeaders = null;
            if (authProvider != null) {
                // SOAP header
                Element soapHeader = authProvider.getSOAPHeaders(portObject);
                if (soapHeader != null) {
                    ((WSBindingProvider) portObject).setOutboundHeaders(Headers.create(soapHeader));
                }

                // HTTP header
                httpHeaders = authProvider.getHTTPHeaders(service.getWSDLDocumentLocation().toString());

                SSLSocketFactory sf = authProvider.getSSLSocketFactory();
                if (sf != null) {
                    ((BindingProvider) portObject).getRequestContext().put(JAXWSProperties.SSL_SOCKET_FACTORY, sf);
                }

                HostnameVerifier hv = authProvider.getHostnameVerifier();
                if (hv != null) {
                    ((BindingProvider) portObject).getRequestContext().put(JAXWSProperties.HOSTNAME_VERIFIER, hv);
                }
            }

            // set HTTP headers
            setHTTPHeaders(portObject, httpHeaders);

            // timeouts
            int connectTimeout = getSession().get(SessionParameter.CONNECT_TIMEOUT, -1);
            if (connectTimeout >= 0) {
                ((BindingProvider) portObject).getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT, connectTimeout);
            }

            int readTimeout = getSession().get(SessionParameter.READ_TIMEOUT, -1);
            if (readTimeout >= 0) {
                ((BindingProvider) portObject).getRequestContext().put("com.sun.xml.ws.request.timeout", readTimeout);
            }
        } catch (CmisBaseException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CmisConnectionException("Cannot initalize Web Services port object: " + e.getMessage(), e);
        }

        return portObject;
    }
}
