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
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.spi.AuthenticationProvider;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.headers.Header;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Apache CXF JAX-WS implementation.
 */
public class CXFPortProvider extends AbstractPortProvider {
    private static final Logger LOG = LoggerFactory.getLogger(CXFPortProvider.class);

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

            Binding binding = portObject.getBinding();
            ((SOAPBinding) binding).setMTOMEnabled(true);

            // add SOAP and HTTP authentication headers
            AuthenticationProvider authProvider = CmisBindingsHelper.getAuthenticationProvider(getSession());
            Map<String, List<String>> httpHeaders = null;
            if (authProvider != null) {
                // SOAP header
                Element soapHeader = authProvider.getSOAPHeaders(portObject);
                if (soapHeader != null) {
                    portObject.getRequestContext().put(
                            Header.HEADER_LIST,
                            Collections.singletonList(new Header(new QName(soapHeader.getNamespaceURI(), soapHeader
                                    .getLocalName()), soapHeader)));
                }

                // HTTP header
                String url = (serviceHolder.getEndpointUrl() != null ? serviceHolder.getEndpointUrl().toString()
                        : serviceHolder.getServiceObject().getWSDLDocumentLocation().toString());
                httpHeaders = authProvider.getHTTPHeaders(url);
            }

            // set HTTP headers
            setHTTPHeaders(portObject, httpHeaders);

            // set endpoint URL
            setEndpointUrl(portObject, serviceHolder.getEndpointUrl());

            Client client = ClientProxy.getClient(portObject);
            HTTPConduit http = (HTTPConduit) client.getConduit();
            HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
            httpClientPolicy.setAllowChunking(true);

            // timeouts
            int connectTimeout = getSession().get(SessionParameter.CONNECT_TIMEOUT, -1);
            if (connectTimeout >= 0) {
                httpClientPolicy.setConnectionTimeout(connectTimeout);
            }

            int readTimeout = getSession().get(SessionParameter.READ_TIMEOUT, -1);
            if (readTimeout >= 0) {
                httpClientPolicy.setReceiveTimeout(readTimeout);
            }

            http.setClient(httpClientPolicy);

            return portObject;
        } catch (CmisBaseException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CmisConnectionException("Cannot initalize Web Services port object: " + e.getMessage(), e);
        }
    }
}
