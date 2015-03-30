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

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * NTLM authentication provider class. USE WITH CARE!
 * 
 * This authentication provider sets a {@link java.net.Authenticator} which will
 * replace the current authenticator, if any. It will fail if this authenticator
 * will be replaced by another part of the code.
 * 
 * Since {@link java.net.Authenticator} is a system-wide authenticator, it will
 * not reliably work in multi-user environments! To achieve that you have to
 * wrap OpenCMIS into its own class loader.
 */
public class NTLMAuthenticationProvider extends AbstractAuthenticationProvider {

    private static final long serialVersionUID = 1L;

    // java.net.Authenticator is static, so this can be static too
    private static final OpenCMISAuthenticator AUTHENTICATOR = new OpenCMISAuthenticator();
    static {
        Authenticator.setDefault(AUTHENTICATOR);
    }

    @Override
    public Map<String, List<String>> getHTTPHeaders(String url) {
        // get user and password
        String user = getUser();
        String password = getPassword();

        // if no user is set, reset the authenticator
        if (user == null) {
            AUTHENTICATOR.reset();
            return null;
        }

        if (password == null) {
            password = "";
        }

        // set user and password
        AUTHENTICATOR.setPasswordAuthentication(user, password);

        // OpenCMIS is not in charge of the authentication
        // -> no HTTP header to set
        return null;
    }

    @Override
    public Element getSOAPHeaders(Object portObject) {
        // no SOAP headers to set
        return null;
    }

    /**
     * OpenCMIS Authenticator class.
     */
    static class OpenCMISAuthenticator extends Authenticator {

        private PasswordAuthentication passwordAuthentication;

        /**
         * Resets the user and password. The next request will not be
         * authenticated.
         */
        public synchronized void reset() {
            passwordAuthentication = null;
        }

        /**
         * Sets a new user and password.
         */
        public synchronized void setPasswordAuthentication(String user, String password) {
            passwordAuthentication = new PasswordAuthentication(user, password.toCharArray());
        }

        @Override
        protected synchronized PasswordAuthentication getPasswordAuthentication() {
            return passwordAuthentication;
        }
    }
}
