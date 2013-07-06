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
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.spi.AuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Axis2 JAX-WS implementation
 * 
 * !!! EXPERIMENTAL !!!
 */
public class Axis2PortProvider extends AbstractPortProvider {
    private static final Logger LOG = LoggerFactory.getLogger(Axis2PortProvider.class);

    /**
     * Creates a port object.
     */
    protected BindingProvider createPortObject(CmisServiceHolder serviceHolder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating Web Service port object of " + serviceHolder.getServiceName() + "...");
        }

        try {
            // create port object
            BindingProvider portObject = createPortObjectFromServiceHolder(serviceHolder, new MTOMFeature());
            ServiceClient serviceClient = ((Stub) portObject)._getServiceClient();

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

                    serviceClient.addStringHeader(new QName(soapHeader.getNamespaceURI(), soapHeader.getLocalName()),
                            headerXml.toString());
                    /*
                     * Map<QName, List<String>> header = new HashMap<QName,
                     * List<String>>(); header.put(new
                     * QName(soapHeader.getNamespaceURI(),
                     * soapHeader.getLocalName()),
                     * Collections.singletonList(headerXml.toString()));
                     * portObject.getRequestContext().put(
                     * "jaxws.binding.soap.headers.outbound", header);
                     */
                }

                // HTTP header
                String url = (serviceHolder.getEndpointUrl() != null ? serviceHolder.getEndpointUrl().toString()
                        : serviceHolder.getServiceObject().getWSDLDocumentLocation().toString());
                httpHeaders = authProvider.getHTTPHeaders(url);

                // TODO: set SSL Factory

                // TODO: set Hostname Verifier
            }

            // set HTTP headers
            setHTTPHeaders(portObject, httpHeaders);

            // set endpoint URL
            setEndpointUrl(portObject, serviceHolder.getEndpointUrl());

            // HTTP settings
            serviceClient.getOptions().setProperty(HTTPConstants.REUSE_HTTP_CLIENT, "true");

            // timeouts
            int connectTimeout = getSession().get(SessionParameter.CONNECT_TIMEOUT, -1);
            if (connectTimeout >= 0) {
                serviceClient.getOptions().setProperty(HTTPConstants.CONNECTION_TIMEOUT,
                        Integer.valueOf(connectTimeout));
            }

            int readTimeout = getSession().get(SessionParameter.READ_TIMEOUT, -1);
            if (readTimeout >= 0) {
                serviceClient.getOptions().setProperty(HTTPConstants.SO_TIMEOUT, Integer.valueOf(readTimeout));
            }

            return portObject;
        } catch (CmisBaseException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CmisConnectionException("Cannot initalize Web Services port object: " + e.getMessage(), e);
        }
    }
}
