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
package org.apache.chemistry.opencmis.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.ws.handler.HandlerResolver;

import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.commons.server.TempStoreOutputStream;
import org.apache.chemistry.opencmis.commons.spi.AuthenticationProvider;
import org.junit.Test;
import org.w3c.dom.Element;

public class SessionParameterMapTest {

    @Test
    public void testMap() throws IOException {
        SessionParameterMap map = new SessionParameterMap();

        // bindings
        map.setAtomPubBindingUrl("http://atomoub/url");
        assertEquals(BindingType.ATOMPUB.value(), map.get(SessionParameter.BINDING_TYPE));
        assertEquals("http://atomoub/url", map.get(SessionParameter.ATOMPUB_URL));

        map.setWebServicesBindingUrl("http://webservices/url");
        assertEquals(BindingType.WEBSERVICES.value(), map.get(SessionParameter.BINDING_TYPE));
        assertEquals("http://webservices/url", map.get(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE));
        assertEquals("http://webservices/url", map.get(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE));
        assertEquals("http://webservices/url", map.get(SessionParameter.WEBSERVICES_OBJECT_SERVICE));
        assertEquals("http://webservices/url", map.get(SessionParameter.WEBSERVICES_VERSIONING_SERVICE));
        assertEquals("http://webservices/url", map.get(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE));
        assertEquals("http://webservices/url", map.get(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE));
        assertEquals("http://webservices/url", map.get(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE));
        assertEquals("http://webservices/url", map.get(SessionParameter.WEBSERVICES_ACL_SERVICE));
        assertEquals("http://webservices/url", map.get(SessionParameter.WEBSERVICES_POLICY_SERVICE));

        map.setBrowserBindingUrl("http://browser/url");
        assertEquals(BindingType.BROWSER.value(), map.get(SessionParameter.BINDING_TYPE));
        assertEquals("http://browser/url", map.get(SessionParameter.BROWSER_URL));

        map.setLocalBindingClass(TestLocalSessionFactory.class);
        assertEquals(BindingType.LOCAL.value(), map.get(SessionParameter.BINDING_TYPE));
        assertEquals(TestLocalSessionFactory.class.getName(), map.get(SessionParameter.LOCAL_FACTORY));

        map.setAtomPubBindingUrl(null);
        assertNull(map.get(SessionParameter.BINDING_TYPE));
        assertNull(map.get(SessionParameter.ATOMPUB_URL));

        // user and password
        map.setUserAndPassword("user", "password");
        assertEquals("user", map.get(SessionParameter.USER));
        assertEquals("password", map.get(SessionParameter.PASSWORD));

        map.setUserAndPassword(null, "password");
        assertFalse(map.containsKey(SessionParameter.USER));
        assertFalse(map.containsKey(SessionParameter.PASSWORD));

        map.setProxyUserAndPassword("user", "password");
        assertEquals("user", map.get(SessionParameter.PROXY_USER));
        assertEquals("password", map.get(SessionParameter.PROXY_PASSWORD));

        // repository id
        map.setRepositoryId("repid");
        assertEquals("repid", map.get(SessionParameter.REPOSITORY_ID));
        map.setRepositoryId(null);
        assertFalse(map.containsKey(SessionParameter.REPOSITORY_ID));

        // authentication
        map.setAuthenticationProvider(TestAuthenticationProvider.class);
        assertEquals(TestAuthenticationProvider.class.getName(),
                map.get(SessionParameter.AUTHENTICATION_PROVIDER_CLASS));

        map.setAuthenticationProvider(null);
        assertNull(map.get(SessionParameter.AUTHENTICATION_PROVIDER_CLASS));

        map.setNtlmAuthentication("user", "password");
        assertEquals("user", map.get(SessionParameter.USER));
        assertEquals("password", map.get(SessionParameter.PASSWORD));
        assertEquals("false", map.get(SessionParameter.AUTH_HTTP_BASIC));
        assertEquals("false", map.get(SessionParameter.AUTH_SOAP_USERNAMETOKEN));
        assertNotNull(map.get(SessionParameter.AUTHENTICATION_PROVIDER_CLASS));

        map.setBasicAuthentication("user1", "password1");
        assertEquals("user1", map.get(SessionParameter.USER));
        assertEquals("password1", map.get(SessionParameter.PASSWORD));
        assertEquals("true", map.get(SessionParameter.AUTH_HTTP_BASIC));
        assertEquals("false", map.get(SessionParameter.AUTH_SOAP_USERNAMETOKEN));

        map.setUsernameTokenAuthentication("user2", "password2", true);
        assertEquals("user2", map.get(SessionParameter.USER));
        assertEquals("password2", map.get(SessionParameter.PASSWORD));
        assertEquals("true", map.get(SessionParameter.AUTH_HTTP_BASIC));
        assertEquals("true", map.get(SessionParameter.AUTH_SOAP_USERNAMETOKEN));

        map.setNoAuthentication();
        assertEquals("false", map.get(SessionParameter.AUTH_HTTP_BASIC));
        assertEquals("false", map.get(SessionParameter.AUTH_SOAP_USERNAMETOKEN));

        // locale
        map.setLocale(new Locale("de", "DE"));
        assertEquals("de", map.get(SessionParameter.LOCALE_ISO639_LANGUAGE));
        assertEquals("DE", map.get(SessionParameter.LOCALE_ISO3166_COUNTRY));

        // HTTP related
        map.setCookies(true);
        assertEquals("true", map.get(SessionParameter.COOKIES));
        map.setCookies(false);
        assertEquals("false", map.get(SessionParameter.COOKIES));

        map.setCompression(true);
        assertEquals("true", map.get(SessionParameter.COMPRESSION));

        map.setClientCompression(false);
        assertEquals("false", map.get(SessionParameter.CLIENT_COMPRESSION));

        map.setConnectionTimeout(12345);
        assertEquals("12345", map.get(SessionParameter.CONNECT_TIMEOUT));

        map.setReadTimeout(98765);
        assertEquals("98765", map.get(SessionParameter.READ_TIMEOUT));

        // header
        map.addHeader(null, "value");
        map.addHeader("header0", "value0");
        map.addHeader("header1", "value1");
        map.addHeader("header2", "value2");

        assertEquals("header0:value0", map.get(SessionParameter.HEADER + ".0"));
        assertEquals("header1:value1", map.get(SessionParameter.HEADER + ".1"));
        assertEquals("header2:value2", map.get(SessionParameter.HEADER + ".2"));

        // store and load
        File tmp = File.createTempFile("session", "parameters");
        try {
            map.store(tmp);

            SessionParameterMap map2 = new SessionParameterMap();
            map2.load(tmp);

            assertEquals(map.size(), map2.size());
            for (String key : map.keySet()) {
                assertEquals(map.get(key), map2.get(key));
            }
        } finally {
            tmp.delete();
        }

        // parse
        String parameters = map.toString();
        SessionParameterMap map2 = new SessionParameterMap();
        map2.parse(parameters);

        assertEquals(map.size(), map2.size());
        for (String key : map.keySet()) {
            assertEquals(map.get(key), map2.get(key));
        }
    }

    private class TestAuthenticationProvider implements AuthenticationProvider {

        private static final long serialVersionUID = 1L;

        @Override
        public Map<String, List<String>> getHTTPHeaders(String url) {
            return null;
        }

        @Override
        public Element getSOAPHeaders(Object portObject) {
            return null;
        }

        @Override
        public HandlerResolver getHandlerResolver() {
            return null;
        }

        @Override
        public SSLSocketFactory getSSLSocketFactory() {
            return null;
        }

        @Override
        public HostnameVerifier getHostnameVerifier() {
            return null;
        }

        @Override
        public void putResponseHeaders(String url, int statusCode, Map<String, List<String>> headers) {
        }
    }

    private class TestLocalSessionFactory implements CmisServiceFactory {

        @Override
        public void init(Map<String, String> parameters) {
        }

        @Override
        public void destroy() {
        }

        @Override
        public CmisService getService(CallContext context) {
            return null;
        }

        @Override
        public File getTempDirectory() {
            return null;
        }

        @Override
        public boolean encryptTempFiles() {
            return false;
        }

        @Override
        public int getMemoryThreshold() {
            return 0;
        }

        @Override
        public long getMaxContentSize() {
            return 0;
        }

        @Override
        public TempStoreOutputStream getTempFileOutputStream(String repositoryId) {
            return null;
        }
    }
}
