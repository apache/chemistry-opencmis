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

import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.w3c.dom.Element;

/**
 * Authentication provider class.
 */
public abstract class AbstractAuthenticationProvider implements SessionAwareAuthenticationProvider {

    private static final long serialVersionUID = 1L;

    private BindingSession session;

    /**
     * Sets the {@link BindingSession} the authentication provider lives in.
     */
    public void setSession(BindingSession session) {
        this.session = session;
    }

    /**
     * Returns {@link BindingSession}.
     */
    public BindingSession getSession() {
        return session;
    }

    public Map<String, List<String>> getHTTPHeaders(String url) {
        return null;
    }

    public Element getSOAPHeaders(Object portObject) {
        return null;
    }

    public void putResponseHeaders(String url, int statusCode, Map<String, List<String>> headers) {
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return null;
    }

    public HostnameVerifier getHostnameVerifier() {
        return null;
    }

    /**
     * Gets the user name from the session.
     * 
     * @return the user name or <code>null</code> if the user name is not set
     */
    protected String getUser() {
        Object userObject = getSession().get(SessionParameter.USER);
        if (userObject instanceof String) {
            return (String) userObject;
        }

        return null;
    }

    /**
     * Gets the password from the session.
     * 
     * @return the password or <code>null</code> if the password is not set
     */
    protected String getPassword() {
        Object passwordObject = getSession().get(SessionParameter.PASSWORD);
        if (passwordObject instanceof String) {
            return (String) passwordObject;
        }

        return null;
    }

    /**
     * Gets the proxy user name from the session.
     * 
     * @return the proxy user name or <code>null</code> if the user name is not
     *         set
     */
    protected String getProxyUser() {
        Object userObject = getSession().get(SessionParameter.PROXY_USER);
        if (userObject instanceof String) {
            return (String) userObject;
        }

        return null;
    }

    /**
     * Gets the proxy password from the session.
     * 
     * @return the proxy password or <code>null</code> if the password is not
     *         set
     */
    protected String getProxyPassword() {
        Object passwordObject = getSession().get(SessionParameter.PROXY_PASSWORD);
        if (passwordObject instanceof String) {
            return (String) passwordObject;
        }

        return null;
    }
}
