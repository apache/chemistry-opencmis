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
package org.apache.opencmis.client.provider.spi;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.opencmis.commons.SessionParameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Standard authentication provider class.
 * 
 * Adds a basic authentication HTTP header and a WS-Security UsernameToken SOAP header.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class StandardAuthenticationProvider extends AbstractAuthenticationProvider {

  private static final long serialVersionUID = 1L;

  private static final String WSSE_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
  private static final String WSU_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

  @Override
  public Map<String, List<String>> getHTTPHeaders(String url) {
    Map<String, List<String>> result = null;

    // only send HTTP header if configured
    if (!isTrue(SessionParameter.AUTH_HTTP_BASIC)) {
      return null;
    }

    // get user and password
    String user = getUser();
    String password = getPassword();

    // if no user is set, don't create HTTP headers
    if (user == null) {
      return null;
    }

    if (password == null) {
      password = "";
    }

    String authHeader = "";
    try {
      authHeader = "Basic "
          + new String(Base64.encodeBase64((user + ":" + password).getBytes("ISO-8859-1")),
              "ISO-8859-1");
    }
    catch (UnsupportedEncodingException e) {
      // shouldn't happen...
      return null;
    }

    result = new HashMap<String, List<String>>();
    result.put("Authorization", Collections.singletonList(authHeader));

    return result;
  }

  @Override
  public Element getSOAPHeaders(Object portObject) {
    // get user and password
    String user = getUser();
    String password = getPassword();

    // only send SOAP header if configured
    if (!isTrue(SessionParameter.AUTH_SOAP_USERNAMETOKEN)) {
      return null;
    }

    // if no user is set, don't create SOAP header
    if (user == null) {
      return null;
    }

    if (password == null) {
      password = "";
    }

    // set time
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    long created = System.currentTimeMillis();
    long expires = created + 24 * 60 * 60 * 1000; // 24 hours

    // create the SOAP header
    try {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

      Element wsseSecurityElement = document.createElementNS(WSSE_NAMESPACE, "Security");

      Element wsuTimestampElement = document.createElementNS(WSU_NAMESPACE, "Timestamp");
      wsseSecurityElement.appendChild(wsuTimestampElement);

      Element tsCreatedElement = document.createElementNS(WSU_NAMESPACE, "Created");
      tsCreatedElement.setTextContent(sdf.format(created));
      wsuTimestampElement.appendChild(tsCreatedElement);

      Element tsExpiresElement = document.createElementNS(WSU_NAMESPACE, "Expires");
      tsExpiresElement.setTextContent(sdf.format(expires));
      wsuTimestampElement.appendChild(tsExpiresElement);

      Element usernameTokenElement = document.createElementNS(WSSE_NAMESPACE, "UsernameToken");
      wsseSecurityElement.appendChild(usernameTokenElement);

      Element usernameElement = document.createElementNS(WSSE_NAMESPACE, "Username");
      usernameElement.setTextContent(user);
      usernameTokenElement.appendChild(usernameElement);

      Element passwordElement = document.createElementNS(WSSE_NAMESPACE, "Password");
      passwordElement.setTextContent(password);
      passwordElement
          .setAttribute("Type",
              "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
      usernameTokenElement.appendChild(passwordElement);

      Element createdElement = document.createElementNS(WSU_NAMESPACE, "Created");
      createdElement.setTextContent(sdf.format(created));
      usernameTokenElement.appendChild(createdElement);

      return wsseSecurityElement;
    }
    catch (ParserConfigurationException e) {
      // shouldn't happen...
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Returns <code>true</code> if the given parameter exists in the session and is set to true,
   * <code>false</code> otherwise.
   */
  private boolean isTrue(String parameterName) {
    Object value = getSession().get(parameterName);

    if (value instanceof Boolean) {
      return ((Boolean) value).booleanValue();
    }

    if (value instanceof String) {
      return Boolean.parseBoolean((String) value);
    }

    return false;
  }
}
