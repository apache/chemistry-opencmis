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
package org.apache.chemistry.opencmis.server.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.filter.ProxyHttpServletRequestWrapper;
import org.apache.chemistry.opencmis.server.impl.atompub.AbstractAtomPubServiceCall;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ProxyRequestTest {
    private static final int FORWARDED_SERVER_PORT = 2443;
    private static final String FORWARDED_SERVER_NAME = "www.frontend.org";
    private static final String FORWARDED_HOST = FORWARDED_SERVER_NAME + ":" + FORWARDED_SERVER_PORT;
    private static final String FORWARDED_HTTPS_PROTO = ProxyHttpServletRequestWrapper.HTTPS_SCHEME;
    private static final String FORWARDED_HTTP_PROTO = ProxyHttpServletRequestWrapper.HTTP_SCHEME;
    private static final String CONTEXT_PATH = "/context";
    private static final String SERVLET_PATH = "cmisatom";
    private static final String REPOSITORY_ID = "22d2880a- bae5-4cfc-a5a9-3b2618e6e11c";
    private static final String EXPECTED_PATH = CONTEXT_PATH + "/" + SERVLET_PATH + "/" + REPOSITORY_ID;
    private static final int BACKEND_SERVER_PORT = 8080;
    private static final String BACKEND_SERVER_NAME = "www.backend.be";
    private static final String BACKEND_SERVER_PROTO = ProxyHttpServletRequestWrapper.HTTP_SCHEME;

    private static final UrlSerivceCall URL_SERVICE_CALL = new UrlSerivceCall();

    @Mock
    private HttpServletRequest request;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(this.request.getScheme()).thenReturn(BACKEND_SERVER_PROTO);
        when(this.request.getServerName()).thenReturn(BACKEND_SERVER_NAME);
        when(this.request.getServerPort()).thenReturn(BACKEND_SERVER_PORT);
        when(this.request.getContextPath()).thenReturn(CONTEXT_PATH);
        when(this.request.getServletPath()).thenReturn(SERVLET_PATH);
        when(this.request.getRequestURI()).thenReturn(CONTEXT_PATH + "/" + SERVLET_PATH);
    }

    @Test
    public void testGetProxiedProtoBaseAddress() throws URISyntaxException {
        when(this.request.getHeader(ProxyHttpServletRequestWrapper.FORWARDED_PROTO_HEADER)).thenReturn(
                FORWARDED_HTTPS_PROTO);

        ProxyHttpServletRequestWrapper proxyRequest = new ProxyHttpServletRequestWrapper(request, null);

        assertEquals(FORWARDED_HTTPS_PROTO, proxyRequest.getScheme());

        URI baseUri = new URI(URL_SERVICE_CALL.compileBaseUrl(proxyRequest, REPOSITORY_ID).toString());

        assertEquals(FORWARDED_HTTPS_PROTO, baseUri.getScheme());
        assertEquals(BACKEND_SERVER_NAME, baseUri.getHost());
        assertEquals(BACKEND_SERVER_PORT, baseUri.getPort());
        assertEquals(EXPECTED_PATH, baseUri.getPath());
    }

    @Test
    public void testGetProxiedHostBaseAddressAndHttpsProto() throws URISyntaxException {
        when(this.request.getHeader(ProxyHttpServletRequestWrapper.FORWARDED_PROTO_HEADER)).thenReturn(
                FORWARDED_HTTPS_PROTO);
        when(this.request.getHeader(ProxyHttpServletRequestWrapper.FORWARDED_HOST_HEADER)).thenReturn(
                FORWARDED_SERVER_NAME);

        ProxyHttpServletRequestWrapper proxyRequest = new ProxyHttpServletRequestWrapper(request, null);

        assertEquals(FORWARDED_HTTPS_PROTO, proxyRequest.getScheme());
        assertEquals(FORWARDED_SERVER_NAME, proxyRequest.getServerName());

        URI baseUri = new URI(URL_SERVICE_CALL.compileBaseUrl(proxyRequest, REPOSITORY_ID).toString());

        assertEquals(FORWARDED_HTTPS_PROTO, baseUri.getScheme());
        assertEquals(FORWARDED_SERVER_NAME, baseUri.getHost());
        assertEquals(-1, baseUri.getPort());
        assertEquals(EXPECTED_PATH, baseUri.getPath());
    }

    @Test
    public void testGetProxiedHostBaseAddress() throws URISyntaxException {
        when(this.request.getHeader(ProxyHttpServletRequestWrapper.FORWARDED_HOST_HEADER)).thenReturn(
                FORWARDED_SERVER_NAME);

        ProxyHttpServletRequestWrapper proxyRequest = new ProxyHttpServletRequestWrapper(request, null);

        assertEquals(FORWARDED_SERVER_NAME, proxyRequest.getServerName());

        URI baseUri = new URI(URL_SERVICE_CALL.compileBaseUrl(proxyRequest, REPOSITORY_ID).toString());

        assertEquals(BACKEND_SERVER_PROTO, baseUri.getScheme());
        assertEquals(FORWARDED_SERVER_NAME, baseUri.getHost());
        assertEquals(-1, baseUri.getPort());
        assertEquals(EXPECTED_PATH, baseUri.getPath());
    }

    @Test
    public void testGetProxiedHostBaseAddressAndPath() throws URISyntaxException {
        String path = "/test";

        when(this.request.getHeader(ProxyHttpServletRequestWrapper.FORWARDED_HOST_HEADER)).thenReturn(
                FORWARDED_SERVER_NAME);

        ProxyHttpServletRequestWrapper proxyRequest = new ProxyHttpServletRequestWrapper(request, path);

        assertEquals(FORWARDED_SERVER_NAME, proxyRequest.getServerName());

        URI baseUri = new URI(URL_SERVICE_CALL.compileBaseUrl(proxyRequest, REPOSITORY_ID).toString());

        assertEquals(BACKEND_SERVER_PROTO, baseUri.getScheme());
        assertEquals(FORWARDED_SERVER_NAME, baseUri.getHost());
        assertEquals(-1, baseUri.getPort());
        assertEquals(path + "/" + SERVLET_PATH + "/" + REPOSITORY_ID, baseUri.getPath());
    }

    @Test
    public void testGetProxiedHostAndPortBaseAddress() throws URISyntaxException {
        when(this.request.getHeader(ProxyHttpServletRequestWrapper.FORWARDED_HOST_HEADER)).thenReturn(FORWARDED_HOST);

        ProxyHttpServletRequestWrapper proxyRequest = new ProxyHttpServletRequestWrapper(request, null);

        assertTrue(FORWARDED_HOST.startsWith(proxyRequest.getServerName()));

        URI baseUri = new URI(URL_SERVICE_CALL.compileBaseUrl(proxyRequest, REPOSITORY_ID).toString());

        assertEquals(BACKEND_SERVER_PROTO, baseUri.getScheme());
        assertEquals(FORWARDED_SERVER_NAME, baseUri.getHost());
        assertEquals(FORWARDED_SERVER_PORT, baseUri.getPort());
        assertEquals(EXPECTED_PATH, baseUri.getPath());
    }

    @Test
    public void testGetProxiedHostPortAndProtoBaseAddress() throws URISyntaxException {
        when(this.request.getHeader(ProxyHttpServletRequestWrapper.FORWARDED_PROTO_HEADER)).thenReturn(
                FORWARDED_HTTPS_PROTO);
        when(this.request.getHeader(ProxyHttpServletRequestWrapper.FORWARDED_HOST_HEADER)).thenReturn(FORWARDED_HOST);

        ProxyHttpServletRequestWrapper proxyRequest = new ProxyHttpServletRequestWrapper(request, null);

        assertEquals(FORWARDED_HTTPS_PROTO, proxyRequest.getScheme());
        assertTrue(FORWARDED_HOST.startsWith(proxyRequest.getServerName()));

        URI baseUri = new URI(URL_SERVICE_CALL.compileBaseUrl(proxyRequest, REPOSITORY_ID).toString());

        assertEquals(FORWARDED_HTTPS_PROTO, baseUri.getScheme());
        assertEquals(FORWARDED_SERVER_NAME, baseUri.getHost());
        assertEquals(FORWARDED_SERVER_PORT, baseUri.getPort());
        assertEquals(EXPECTED_PATH, baseUri.getPath());
    }

    @Test
    public void testGetProxiedHostBadHttpPortAndProtoBaseAddress() throws URISyntaxException {
        when(this.request.getHeader(ProxyHttpServletRequestWrapper.FORWARDED_PROTO_HEADER)).thenReturn(
                FORWARDED_HTTP_PROTO);
        when(this.request.getHeader(ProxyHttpServletRequestWrapper.FORWARDED_HOST_HEADER)).thenReturn(
                FORWARDED_SERVER_NAME + ":noportnumber");

        ProxyHttpServletRequestWrapper proxyRequest = new ProxyHttpServletRequestWrapper(request, null);

        assertEquals(FORWARDED_HTTP_PROTO, proxyRequest.getScheme());
        assertTrue(FORWARDED_HOST.startsWith(proxyRequest.getServerName()));

        URI baseUri = new URI(URL_SERVICE_CALL.compileBaseUrl(proxyRequest, REPOSITORY_ID).toString());

        assertEquals(FORWARDED_HTTP_PROTO, baseUri.getScheme());
        assertEquals(FORWARDED_SERVER_NAME, baseUri.getHost());
        assertEquals(-1, baseUri.getPort());
        assertEquals(EXPECTED_PATH, baseUri.getPath());
    }

    @Test
    public void testGetProxiedHostBadHttpsPortAndProtoBaseAddress() throws URISyntaxException {
        when(this.request.getHeader(ProxyHttpServletRequestWrapper.FORWARDED_PROTO_HEADER)).thenReturn(
                FORWARDED_HTTPS_PROTO);
        when(this.request.getHeader(ProxyHttpServletRequestWrapper.FORWARDED_HOST_HEADER)).thenReturn(
                FORWARDED_SERVER_NAME + ":noportnumber");

        ProxyHttpServletRequestWrapper proxyRequest = new ProxyHttpServletRequestWrapper(request, null);

        assertEquals(FORWARDED_HTTPS_PROTO, proxyRequest.getScheme());
        assertTrue(FORWARDED_HOST.startsWith(proxyRequest.getServerName()));

        URI baseUri = new URI(URL_SERVICE_CALL.compileBaseUrl(proxyRequest, REPOSITORY_ID).toString());

        assertEquals(FORWARDED_HTTPS_PROTO, baseUri.getScheme());
        assertEquals(FORWARDED_SERVER_NAME, baseUri.getHost());
        assertEquals(-1, baseUri.getPort());
        assertEquals(EXPECTED_PATH, baseUri.getPath());
    }

    @Test
    public void testCompileBaseUrl() throws URISyntaxException {
        URI baseUri = new URI(URL_SERVICE_CALL.compileBaseUrl(request, REPOSITORY_ID).toString());

        assertEquals(BACKEND_SERVER_PROTO, baseUri.getScheme());
        assertEquals(BACKEND_SERVER_NAME, baseUri.getHost());
        assertEquals(BACKEND_SERVER_PORT, baseUri.getPort());
        assertEquals(EXPECTED_PATH, baseUri.getPath());
    }

    static class UrlSerivceCall extends AbstractAtomPubServiceCall {
        @Override
        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            // no implementation

        }
    }
}
