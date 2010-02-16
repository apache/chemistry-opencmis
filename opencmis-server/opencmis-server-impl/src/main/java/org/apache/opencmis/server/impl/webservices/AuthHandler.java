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
package org.apache.opencmis.server.impl.webservices;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.opencmis.server.spi.CallContext;

/**
 * This class tries to extract a user name and a password from a UsernameToken.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AuthHandler implements SOAPHandler<SOAPMessageContext> {

  private static final String WSSE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
  private static final QName WSSE_SECURITY = new QName(WSSE_NS, "Security");
  private static final QName WSSE_USERNAME_TOKEN = new QName(WSSE_NS, "UsernameToken");
  private static final QName WSSE_USERNAME = new QName(WSSE_NS, "Username");
  private static final QName WSSE_PASSWORD = new QName(WSSE_NS, "Password");

  private static final Set<QName> HEADERS = new HashSet<QName>();
  static {
    HEADERS.add(WSSE_SECURITY);
  }

  public Set<QName> getHeaders() {
    return HEADERS;
  }

  public void close(MessageContext context) {
  }

  public boolean handleFault(SOAPMessageContext context) {
    return true;
  }

  public boolean handleMessage(SOAPMessageContext context) {
    Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    if (outboundProperty.booleanValue()) {
      // we are only looking at inbound messages
      return true;
    }

    try {
      // read the header
      SOAPMessage msg = context.getMessage();
      SOAPHeader sh = msg.getSOAPHeader();
      SOAPElement securityElement = (SOAPElement) sh.getChildElements(WSSE_SECURITY).next();
      SOAPElement tokenElement = (SOAPElement) securityElement
          .getChildElements(WSSE_USERNAME_TOKEN).next();
      SOAPElement userElement = (SOAPElement) tokenElement.getChildElements(WSSE_USERNAME).next();
      SOAPElement passwordElement = (SOAPElement) tokenElement.getChildElements(WSSE_PASSWORD)
          .next();

      // add user and password to context
      Map<String, String> callContextMap = new HashMap<String, String>();
      callContextMap.put(CallContext.USERNAME, userElement.getValue());
      callContextMap.put(CallContext.PASSWORD, passwordElement.getValue());

      context.put(AbstractService.CALL_CONTEXT_MAP, callContextMap);
      context.setScope(AbstractService.CALL_CONTEXT_MAP, Scope.APPLICATION);
    }
    catch (Exception e) {
      // something went wrong, e.g. a part of the SOAP header wasn't set
      throw new RuntimeException("UsernameToken not set!", e);
    }

    return true;
  }
}
