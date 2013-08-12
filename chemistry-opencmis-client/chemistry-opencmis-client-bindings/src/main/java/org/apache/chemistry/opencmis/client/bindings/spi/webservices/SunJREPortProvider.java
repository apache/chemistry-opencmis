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
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.spi.AuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.sun.xml.internal.ws.api.message.Headers;
import com.sun.xml.internal.ws.developer.JAXWSProperties;
import com.sun.xml.internal.ws.developer.StreamingAttachmentFeature;
import com.sun.xml.internal.ws.developer.WSBindingProvider;

/**
 * Sun JRE JAX-WS implementation.
 */
public class SunJREPortProvider extends AbstractPortProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SunJREPortProvider.class);

    /**
     * Creates a port object.
     */
    protected BindingProvider createPortObject(CmisServiceHolder serviceHolder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating Web Service port object of " + serviceHolder.getServiceName() + "...");
        }

        try {
            // prepare features
            WebServiceFeature[] features;
            if (serviceHolder.getService().handlesContent()) {
                int threshold = getSession().get(SessionParameter.WEBSERVICES_MEMORY_THRESHOLD, 4 * 1024 * 1024);
                features = new WebServiceFeature[] { new MTOMFeature(),
                        new StreamingAttachmentFeature(null, true, threshold) };
            } else {
                features = new WebServiceFeature[] { new MTOMFeature() };
            }

            // create port object
            BindingProvider portObject = createPortObjectFromServiceHolder(serviceHolder, features);

            // set streaming for services that transport content
            if (serviceHolder.getService().handlesContent()) {
                portObject.getRequestContext().put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, CHUNK_SIZE);
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
                String url = (serviceHolder.getEndpointUrl() != null ? serviceHolder.getEndpointUrl().toString()
                        : serviceHolder.getServiceObject().getWSDLDocumentLocation().toString());
                httpHeaders = authProvider.getHTTPHeaders(url);

                // SSL Factory
                SSLSocketFactory sf = authProvider.getSSLSocketFactory();
                if (sf != null) {
                    portObject.getRequestContext().put(JAXWSProperties.SSL_SOCKET_FACTORY, sf);
                }

                // Hostname Verifier
                HostnameVerifier hv = authProvider.getHostnameVerifier();
                if (hv != null) {
                    portObject.getRequestContext().put(JAXWSProperties.HOSTNAME_VERIFIER, hv);
                }
            }

            // set HTTP headers
            setHTTPHeaders(portObject, httpHeaders);

            // set endpoint URL
            setEndpointUrl(portObject, serviceHolder.getEndpointUrl());

            // timeouts
            int connectTimeout = getSession().get(SessionParameter.CONNECT_TIMEOUT, -1);
            if (connectTimeout >= 0) {
                portObject.getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT, connectTimeout);
            }

            int readTimeout = getSession().get(SessionParameter.READ_TIMEOUT, -1);
            if (readTimeout >= 0) {
                portObject.getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, readTimeout);
            }

            return portObject;
        } catch (CmisBaseException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CmisConnectionException("Cannot initalize Web Services port object: " + e.getMessage(), e);
        }
    }
}
