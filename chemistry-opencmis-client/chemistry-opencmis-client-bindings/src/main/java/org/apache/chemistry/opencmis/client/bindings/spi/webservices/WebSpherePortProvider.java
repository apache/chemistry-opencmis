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

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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

/**
 * Provides CMIS Web Services port objects for WebSphere. Handles authentication
 * headers.
 */
public class WebSpherePortProvider extends AbstractPortProvider {
    private static final Logger log = LoggerFactory.getLogger(WebSpherePortProvider.class);

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
                portObject = ((ObjectService) service).getObjectServicePort(new MTOMFeature());
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
            AuthenticationProvider authProvider = CmisBindingsHelper.getAuthenticationProvider(getSession());
            Map<String, List<String>> httpHeaders = null;
            if (authProvider != null) {
                // SOAP header
                Element soapHeader = authProvider.getSOAPHeaders(portObject);
                if (soapHeader != null) {
                    TransformerFactory transFactory = TransformerFactory.newInstance();
                    Transformer transformer = transFactory.newTransformer();
                    StringWriter headerXml = new StringWriter();
                    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    transformer.transform(new DOMSource(soapHeader), new StreamResult(headerXml));

                    Map<QName, List<String>> header = new HashMap<QName, List<String>>();
                    header.put(new QName(soapHeader.getNamespaceURI(), soapHeader.getLocalName()),
                            Collections.singletonList(headerXml.toString()));
                    ((BindingProvider) portObject).getRequestContext().put("jaxws.binding.soap.headers.outbound",
                            header);
                }

                // HTTP header
                httpHeaders = authProvider.getHTTPHeaders(service.getWSDLDocumentLocation().toString());
            }

            // set HTTP headers
            setHTTPHeaders(portObject, httpHeaders);

            // timeouts
            int connectTimeout = getSession().get(SessionParameter.CONNECT_TIMEOUT, -1);
            if (connectTimeout >= 0) {
                ((BindingProvider) portObject).getRequestContext().put("connection_timeout", connectTimeout);
            }

            int readTimeout = getSession().get(SessionParameter.READ_TIMEOUT, -1);
            if (readTimeout >= 0) {
                ((BindingProvider) portObject).getRequestContext().put("request_timeout", readTimeout);
            }
        } catch (CmisBaseException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CmisConnectionException("Cannot initalize Web Services port object: " + e.getMessage(), e);
        }

        return portObject;
    }
}
