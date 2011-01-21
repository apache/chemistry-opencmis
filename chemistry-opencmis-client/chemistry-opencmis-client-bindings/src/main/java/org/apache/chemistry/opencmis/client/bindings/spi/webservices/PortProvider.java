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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.chemistry.opencmis.commons.impl.jaxb.DiscoveryService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.MultiFilingService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.NavigationService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.ObjectService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.PolicyService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.RelationshipService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.RepositoryService;
import org.apache.chemistry.opencmis.commons.impl.jaxb.VersioningService;
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
public class PortProvider extends AbstractPortProvider {

    private static Log log = LogFactory.getLog(PortProvider.class);

    private boolean useCompression;

    /**
     * Constructor.
     */
    public PortProvider(Session session) {
        this.session = session;

        useCompression = false;
        if ((session.get(SessionParameter.COMPRESSION) instanceof String)
                && (Boolean.parseBoolean((String) session.get(SessionParameter.COMPRESSION)))) {
            useCompression = true;
        }
        if ((session.get(SessionParameter.COMPRESSION) instanceof Boolean)
                && ((Boolean) session.get(SessionParameter.COMPRESSION)).booleanValue()) {
            useCompression = true;
        }
    }

    /**
     * Creates a port object.
     */
    protected Object createPortObject(Service service) {
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
                    threshold = Integer.parseInt((String) session.get(SessionParameter.WEBSERVICES_MEMORY_THRESHOLD));
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
            AbstractAuthenticationProvider authProvider = CmisBindingsHelper.getAuthenticationProvider(session);
            Map<String, List<String>> httpHeaders = null;
            if (authProvider != null) {
                // SOAP header
                Element soapHeader = authProvider.getSOAPHeaders(portObject);
                if (soapHeader != null) {
                    ((WSBindingProvider) portObject).setOutboundHeaders(Headers.create(soapHeader));
                }

                // HTTP header
                httpHeaders = authProvider.getHTTPHeaders(service.getWSDLDocumentLocation().toString());
            }

            if (useCompression) {
                if (httpHeaders == null) {
                    httpHeaders = new HashMap<String, List<String>>();
                }
                httpHeaders.put("Accept-Encoding", Collections.singletonList("gzip"));
            }

            if (httpHeaders != null) {
                ((BindingProvider) portObject).getRequestContext()
                        .put(MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);
            }

        } catch (CmisBaseException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CmisConnectionException("Cannot initalize Web Services port object: " + e.getMessage(), e);
        }

        return portObject;
    }
}
