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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import com.sun.xml.ws.api.handler.MessageHandler;
import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;

/**
 * This class tries to extract a user name and a password from a UsernameToken.
 */
public class AuthHandler extends AbstractUsernameTokenAuthHandler implements MessageHandler<MessageHandlerContext> {

    public Set<QName> getHeaders() {
        return HEADERS;
    }

    public void close(MessageContext context) {
    }

    public boolean handleFault(MessageHandlerContext context) {
        return true;
    }

    public boolean handleMessage(MessageHandlerContext context) {
        Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outboundProperty.booleanValue()) {
            // we are only looking at inbound messages
            return true;
        }

        Map<String, String> callContextMap = null;

        try {
            // read the header
            Message msg = context.getMessage();
            HeaderList hl = msg.getHeaders();
            Header securityHeader = hl.get(WSSE_SECURITY, true);

            JAXBElement<SecurityHeaderType> sht = securityHeader.readAsJAXB(WSSE_CONTEXT.createUnmarshaller());

            callContextMap = extractUsernamePassword(sht);
        } catch (Exception e) {
            // something went wrong, e.g. a part of the SOAP header wasn't set
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
