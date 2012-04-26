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
package org.apache.chemistry.opencmis.client.bindings.spi;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.chemistry.opencmis.client.bindings.spi.cookies.CmisCookieManager;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Standard authentication provider class.
 * 
 * Adds a basic authentication HTTP header and a WS-Security UsernameToken SOAP
 * header.
 */
public class StandardAuthenticationProvider extends AbstractAuthenticationProvider {

    private static final long serialVersionUID = 1L;

    private static final String WSSE_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    private static final String WSU_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    private boolean sendBasicAuth;
    private boolean sendUsernameToken;
    private CmisCookieManager cookieManager;
    private Map<String, List<String>> fixedHeaders = new HashMap<String, List<String>>();

    @Override
    public void setSession(BindingSession session) {
        super.setSession(session);

        sendBasicAuth = isTrue(SessionParameter.AUTH_HTTP_BASIC);
        sendUsernameToken = isTrue(SessionParameter.AUTH_SOAP_USERNAMETOKEN);

        if (isTrue(SessionParameter.COOKIES)) {
            cookieManager = new CmisCookieManager();
        }

        // authentication
        if (sendBasicAuth) {
            // get user and password
            String user = getUser();
            String password = getPassword();

            // if no user is set, don't set basic auth header
            if (user != null) {
                fixedHeaders.put("Authorization", createBasicAuthHeaderValue(user, password));
            }

            // get proxy user and password
            String proxyUser = getProxyUser();
            String proxyPassword = getProxyPassword();

            // if no proxy user is set, don't set basic auth header
            if (proxyUser != null) {
                fixedHeaders.put("Proxy-Authorization", createBasicAuthHeaderValue(proxyUser, proxyPassword));
            }
        }

        // other headers
        int x = 0;
        Object headerParam;
        while ((headerParam = getSession().get(SessionParameter.HEADER + "." + x)) != null) {
            String header = headerParam.toString();
            int colon = header.indexOf(':');
            if (colon > -1) {
                String key = header.substring(0, colon).trim();
                if (key.length() > 0) {
                    String value = header.substring(colon + 1).trim();
                    List<String> values = fixedHeaders.get(key);
                    if (values == null) {
                        fixedHeaders.put(key, Collections.singletonList(value));
                    } else {
                        List<String> newValues = new ArrayList<String>(values);
                        newValues.add(value);
                        fixedHeaders.put(key, newValues);
                    }
                }
            }
            x++;
        }
    }

    @Override
    public Map<String, List<String>> getHTTPHeaders(String url) {
        Map<String, List<String>> result = new HashMap<String, List<String>>(fixedHeaders);

        // cookies
        if (cookieManager != null) {
            Map<String, List<String>> cookies = cookieManager.get(url, result);
            if (!cookies.isEmpty()) {
                result.putAll(cookies);
            }
        }

        return result.isEmpty() ? null : result;
    }

    @Override
    public void putResponseHeaders(String url, int statusCode, Map<String, List<String>> headers) {
        if (cookieManager != null) {
            cookieManager.put(url, headers);
        }
    }

    @Override
    public Element getSOAPHeaders(Object portObject) {
        // only send SOAP header if configured
        if (!sendUsernameToken) {
            return null;
        }

        // get user and password
        String user = getUser();
        String password = getPassword();

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
            tsCreatedElement.appendChild(document.createTextNode(sdf.format(created)));
            wsuTimestampElement.appendChild(tsCreatedElement);

            Element tsExpiresElement = document.createElementNS(WSU_NAMESPACE, "Expires");
            tsExpiresElement.appendChild(document.createTextNode(sdf.format(expires)));
            wsuTimestampElement.appendChild(tsExpiresElement);

            Element usernameTokenElement = document.createElementNS(WSSE_NAMESPACE, "UsernameToken");
            wsseSecurityElement.appendChild(usernameTokenElement);

            Element usernameElement = document.createElementNS(WSSE_NAMESPACE, "Username");
            usernameElement.appendChild(document.createTextNode(user));
            usernameTokenElement.appendChild(usernameElement);

            Element passwordElement = document.createElementNS(WSSE_NAMESPACE, "Password");
            passwordElement.appendChild(document.createTextNode(password));
            passwordElement.setAttribute("Type",
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
            usernameTokenElement.appendChild(passwordElement);

            Element createdElement = document.createElementNS(WSU_NAMESPACE, "Created");
            createdElement.appendChild(document.createTextNode(sdf.format(created)));
            usernameTokenElement.appendChild(createdElement);

            return wsseSecurityElement;
        } catch (ParserConfigurationException e) {
            // shouldn't happen...
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns the HTTP headers that are sent with all requests. The returned
     * map is mutable but not synchronized!
     */
    protected Map<String, List<String>> getFixedHeaders() {
        return fixedHeaders;
    }

    /**
     * Creates a basic authentication header value from a username and a
     * password.
     */
    protected List<String> createBasicAuthHeaderValue(String username, String password) {
        if (password == null) {
            password = "";
        }

        try {
            return Collections.singletonList("Basic "
                    + Base64.encodeBytes((username + ":" + password).getBytes("ISO-8859-1")));
        } catch (UnsupportedEncodingException e) {
            // shouldn't happen...
            return Collections.emptyList();
        }
    }

    /**
     * Returns <code>true</code> if the given parameter exists in the session
     * and is set to true, <code>false</code> otherwise.
     */
    protected boolean isTrue(String parameterName) {
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
