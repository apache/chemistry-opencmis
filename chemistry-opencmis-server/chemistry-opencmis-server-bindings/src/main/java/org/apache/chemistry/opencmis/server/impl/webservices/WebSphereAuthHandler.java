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
package org.apache.chemistry.opencmis.server.impl.webservices;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class WebSphereAuthHandler extends AbstractUsernameTokenAuthHandler implements SOAPHandler<SOAPMessageContext> {

    public Set<QName> getHeaders() {
        return HEADERS;
    }

    public void close(MessageContext context) {
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outboundProperty.booleanValue()) {
            // we are only looking at inbound messages
            return true;
        }

        Map<String, String> callContextMap = null;

        Map<QName, List<String>> requestHeaders = (Map<QName, List<String>>) context
                .get("jaxws.binding.soap.headers.inbound");

        if (requestHeaders != null) {
            List<String> secHeaders = requestHeaders.get(WSSE_SECURITY);
            if (secHeaders != null && secHeaders.size() > 0) {
                try {
                    Unmarshaller unmarshaller = WSSE_CONTEXT.createUnmarshaller();

                    for (String h : secHeaders) {
                        try {
                            JAXBElement<SecurityHeaderType> sht = (JAXBElement<SecurityHeaderType>) unmarshaller
                                    .unmarshal(new StringReader(h));

                            callContextMap = extractUsernamePassword(sht);
                            if (callContextMap != null) {
                                break;
                            }

                        } catch (Exception e) {
                            // unmarshalling failed, maybe another header -
                            // ignore
                        }
                    }
                } catch (Exception e) {
                    // JAXB problem - ignore
                }
            }
        }

        // add user and password to context
        if (callContextMap == null) {
            callContextMap = new HashMap<String, String>();
        }

        context.put(AbstractService.CALL_CONTEXT_MAP, callContextMap);
        context.setScope(AbstractService.CALL_CONTEXT_MAP, Scope.APPLICATION);

        return true;
    }
}
