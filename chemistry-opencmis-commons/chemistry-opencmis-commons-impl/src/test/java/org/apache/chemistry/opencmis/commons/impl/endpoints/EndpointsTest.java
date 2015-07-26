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
package org.apache.chemistry.opencmis.commons.impl.endpoints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.endpoints.CmisAuthentication;
import org.apache.chemistry.opencmis.commons.endpoints.CmisEndpoint;
import org.apache.chemistry.opencmis.commons.endpoints.CmisEndpointsDocument;
import org.junit.Test;

public class EndpointsTest {

    private static final String TEST_JSON = "{" + //
            "  \"endpoints\" : [" + //
            "    {" + //
            "      \"displayName\" : \"DocServ CMIS 1.1 AtomPub Binding\"," + //
            "      \"cmisVersion\" : \"1.1\"," + //
            "      \"binding\" : \"atompub\"," + //
            "      \"url\" : \"https://host:8080/cmis/atompub\"," + //
            "      \"cookies\" : \"recommended\"," + //
            "      \"compression\" : \"server\"," + //
            "      \"csrfHeader\" : \"X-CSRF-Token\"," + //
            "      \"csrfParameter\" : \"x-token\"," + //
            "      \"authentication\" : " + //
            "      [" + //
            "        {" + //
            "          \"type\" : \"basic\"," + //
            "          \"displayName\" : \"HTTP basic authentication\"," + //
            "          \"documentationUrl\" : \"http://www.example.com/docserv/cmis/basic\"," + //
            "          \"preference\" : 5" + //
            "        }," + //
            "        {" + //
            "          \"type\" : \"certificate\"," + //
            "          \"displayName\" : \"SSL certificate authentication for employees\"," + //
            "          \"documentationUrl\" : \"http://www.example.com/docserv/cmis/cert\"," + //
            "          \"preference\" : 2" + //
            "        }" + //
            "      ]" + //
            "    }," + //
            "    {" + //
            "      \"displayName\" : \"DocServ CMIS 1.1 Browser Binding\"," + //
            "      \"cmisVersion\" : \"1.1\"," + //
            "      \"binding\" : \"browser\"," + //
            "      \"url\" : \"https://host:8080/cmis/browser\"," + //
            "      \"cookies\" : \"required\"," + //
            "      \"compression\" : \"server\"," + //
            "      \"csrfHeader\" : \"X-CSRF-Token\"," + //
            "      \"csrfParameter\" : \"x-token\"," + //
            "      \"authentication\" : " + //
            "      [" + //
            "        {" + //
            "          \"type\" : \"basic\"," + //
            "          \"displayName\" : \"HTTP basic authentication\"," + //
            "          \"documentationUrl\" : \"http://www.example.com/docserv/cmis/basic\"," + //
            "          \"preference\" : 4" + //
            "        }," + //
            "        {" + //
            "          \"type\" : \"certificate\"," + //
            "          \"displayName\" : \"SSL certificate authentication for employees\"," + //
            "          \"documentationUrl\" : \"http://www.example.com/docserv/cmis/cert\"," + //
            "          \"preference\" : 1" + //
            "        }," + //
            "        {" + //
            "          \"type\" : \"form\"," + //
            "          \"displayName\" : \"Form-based authentication for guests\"," + //
            "          \"loginUrl\" : \"http://host:8080/login\"," + //
            "          \"documentationUrl\" : \"http://www.example.com/docserv/cmis/form\"," + //
            "          \"preference\" : 3" + //
            "        }" + //
            "      ]" + //
            "    }" + //
            "  ]" + //
            "}";

    @Test
    public void testReadWrite() throws Exception {
        StringReader sr = new StringReader(TEST_JSON);

        CmisEndpointsDocument doc = CmisEndpointsDocumentHelper.read(sr);
        assertNotNull(doc);
        assertEquals(2, doc.getEndpoints().size());

        List<CmisAuthentication> prefs = doc.getAuthenticationsSortedByPreference();
        assertNotNull(prefs);
        assertEquals(5, prefs.size());

        assertEquals(CmisEndpoint.BINDING_BROWSER, prefs.get(0).getEndpoint().getBinding());
        assertEquals(CmisEndpoint.COOKIES_REQUIRED, prefs.get(0).getEndpoint().getCookies());
        assertEquals(CmisEndpoint.COMPRESSION_SERVER, prefs.get(0).getEndpoint().getCompression());
        assertEquals(CmisAuthentication.AUTH_CERT, prefs.get(0).getType());
        assertTrue(prefs.get(0).requiresCookies());

        assertEquals(CmisEndpoint.BINDING_ATOMPUB, prefs.get(1).getEndpoint().getBinding());
        assertEquals(CmisEndpoint.COOKIES_RECOMMENDED, prefs.get(1).getEndpoint().getCookies());
        assertEquals(CmisEndpoint.COMPRESSION_SERVER, prefs.get(1).getEndpoint().getCompression());
        assertEquals(CmisAuthentication.AUTH_CERT, prefs.get(1).getType());
        assertTrue(prefs.get(1).requiresCookies());

        assertEquals(CmisEndpoint.BINDING_BROWSER, prefs.get(2).getEndpoint().getBinding());
        assertEquals(CmisEndpoint.COOKIES_REQUIRED, prefs.get(2).getEndpoint().getCookies());
        assertEquals(CmisEndpoint.COMPRESSION_SERVER, prefs.get(2).getEndpoint().getCompression());
        assertEquals(CmisAuthentication.AUTH_FORM, prefs.get(2).getType());
        assertTrue(prefs.get(2).requiresCookies());
        assertEquals("http://host:8080/login", prefs.get(2).get("loginUrl"));

        assertEquals(CmisEndpoint.BINDING_BROWSER, prefs.get(3).getEndpoint().getBinding());
        assertEquals(CmisEndpoint.COOKIES_REQUIRED, prefs.get(3).getEndpoint().getCookies());
        assertEquals(CmisEndpoint.COMPRESSION_SERVER, prefs.get(3).getEndpoint().getCompression());
        assertEquals(CmisAuthentication.AUTH_BASIC, prefs.get(3).getType());
        assertTrue(prefs.get(3).requiresCookies());

        assertEquals(CmisEndpoint.BINDING_ATOMPUB, prefs.get(4).getEndpoint().getBinding());
        assertEquals(CmisEndpoint.COOKIES_RECOMMENDED, prefs.get(4).getEndpoint().getCookies());
        assertEquals(CmisEndpoint.COMPRESSION_SERVER, prefs.get(4).getEndpoint().getCompression());
        assertEquals(CmisAuthentication.AUTH_BASIC, prefs.get(4).getType());
        assertTrue(prefs.get(1).requiresCookies());

        // -- write --
        StringWriter sw = new StringWriter();
        CmisEndpointsDocumentHelper.write(doc, sw);
        assertNotNull(sw.toString());

        // -- read --
        CmisEndpointsDocument copyDoc = CmisEndpointsDocumentHelper.read(sw.toString());
        assertNotNull(copyDoc);

        // -- check --
        assertEqualsDocument(doc, copyDoc);
    }

    @Test
    public void testWriteRead() throws Exception {
        CmisEndpointImpl epA1 = new CmisEndpointImpl(CmisEndpoint.VERSION_1_0, CmisEndpoint.BINDING_ATOMPUB);
        epA1.put(CmisEndpoint.KEY_DISPLAY_NAME, "My Endpoint 1");
        epA1.put(CmisEndpoint.KEY_URL, "http://server1/cmis1");
        epA1.put(CmisEndpoint.KEY_COOKIES, CmisEndpoint.COOKIES_REQUIRED);
        epA1.put(CmisEndpoint.KEY_COMPRESSION, CmisEndpoint.COMPRESSION_BOTH);
        epA1.put(CmisEndpoint.KEY_CSRF_HEADER, "X-CSRF");

        CmisEndpointImpl epA2 = new CmisEndpointImpl(CmisEndpoint.VERSION_1_1, CmisEndpoint.BINDING_BROWSER);
        epA2.put(CmisEndpoint.KEY_DISPLAY_NAME, "My Endpoint 2");
        epA2.put(CmisEndpoint.KEY_URL, "http://server2/cmis2");

        CmisEndpointImpl epA3 = new CmisEndpointImpl(CmisEndpoint.VERSION_1_1, CmisEndpoint.BINDING_WEBSERVICES);
        epA3.put(CmisEndpoint.KEY_SOAP_VERSION, CmisEndpoint.SOAP_VERSION_1_1);

        List<CmisEndpoint> endpoints = new ArrayList<CmisEndpoint>();
        endpoints.add(epA1);
        endpoints.add(epA2);
        endpoints.add(epA3);

        CmisEndpointsDocumentImpl docA = new CmisEndpointsDocumentImpl(endpoints);
        assertEquals(3, docA.getEndpoints().size());

        // -- write --
        String json = CmisEndpointsDocumentHelper.write(docA);
        assertNotNull(json);

        // -- read --
        CmisEndpointsDocument docB = CmisEndpointsDocumentHelper.read(json);
        assertNotNull(docB);

        // -- check --
        assertEqualsDocument(docA, docB);
    }

    private void assertEqualsDocument(CmisEndpointsDocument expected, CmisEndpointsDocument actual) {
        assertNotNull(expected);
        assertNotNull(actual);

        assertEquals(expected.getEndpoints().size(), actual.getEndpoints().size());

        int n = expected.getEndpoints().size();
        for (int i = 0; i < n; i++) {
            assertEqualsEndpoint(expected.getEndpoints().get(i), actual.getEndpoints().get(i));
        }
    }

    private void assertEqualsEndpoint(CmisEndpoint expected, CmisEndpoint actual) {
        assertNotNull(expected);
        assertNotNull(actual);

        assertEquals(expected.getDisplayName(), actual.getDisplayName());
        assertEquals(expected.getCmisVersion(), actual.getCmisVersion());
        assertEquals(expected.getBinding(), actual.getBinding());
        assertEquals(expected.getUrl(), actual.getUrl());
        assertEquals(expected.getRepositoryServiceWdsl(), actual.getRepositoryServiceWdsl());
        assertEquals(expected.getNavigationServiceWdsl(), actual.getNavigationServiceWdsl());
        assertEquals(expected.getObjectServiceWdsl(), actual.getObjectServiceWdsl());
        assertEquals(expected.getMultifilingServiceWdsl(), actual.getMultifilingServiceWdsl());
        assertEquals(expected.getDiscoveryServiceWdsl(), actual.getDiscoveryServiceWdsl());
        assertEquals(expected.getVersioningServiceWdsl(), actual.getVersioningServiceWdsl());
        assertEquals(expected.getRelationshipServiceWdsl(), actual.getRelationshipServiceWdsl());
        assertEquals(expected.getPolicyServiceWdsl(), actual.getPolicyServiceWdsl());
        assertEquals(expected.getSoapVersion(), actual.getSoapVersion());
        assertEquals(expected.getCookies(), actual.getCookies());
        assertEquals(expected.getCompression(), actual.getCompression());
        assertEquals(expected.getCsrfHeader(), actual.getCsrfHeader());
        assertEquals(expected.getCsrfParameter(), actual.getCsrfParameter());

        assertEquals(expected.getAuthentications().size(), actual.getAuthentications().size());

        int n = expected.getAuthentications().size();
        for (int i = 0; i < n; i++) {
            assertEqualsAuthentication(expected.getAuthentications().get(i), actual.getAuthentications().get(i));
        }
    }

    private void assertEqualsAuthentication(CmisAuthentication expected, CmisAuthentication actual) {
        assertNotNull(expected);
        assertNotNull(actual);

        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getDisplayName(), actual.getDisplayName());
        assertEquals(expected.getDocumentationUrl(), actual.getDocumentationUrl());
        assertEquals(expected.getPreference(), actual.getPreference());
    }
}
