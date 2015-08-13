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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.data.CacheHeaderContentStream;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.server.impl.atompub.AbstractAtomPubServiceCall;
import org.apache.chemistry.opencmis.server.impl.atompub.CmisAtomPubServlet;
import org.apache.chemistry.opencmis.server.impl.browser.CmisBrowserBindingServlet;
import org.apache.chemistry.opencmis.server.shared.Dispatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class HttpRequestHeadMethodTest {
    private static final String CONTEXT_PATH = "/context";
    private static final String ATOMPUB_SERVLET_PATH = "/cmisatom";
    private static final String BROWSER_SERVLET_PATH = "/cmisjson";
    private static final String REPOSITORY_ID = "22d2880a-bae5-4cfc-a5a9-3b2618e6e11c";
    private static final int BACKEND_SERVER_PORT = 8080;
    private static final String BACKEND_SERVER_NAME = "www.backend.be";
    private static final String BACKEND_SERVER_PROTO = "http";

    @Mock
    private CmisServiceFactory cmisServiceFactory;

    @Mock
    private CmisService cmisService;

    @Mock
    private CacheHeaderContentStream contentStream;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletConfig config;

    @Mock
    private ServletContext context;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(this.request.getScheme()).thenReturn(BACKEND_SERVER_PROTO);
        when(this.request.getServerName()).thenReturn(BACKEND_SERVER_NAME);
        when(this.request.getServerPort()).thenReturn(BACKEND_SERVER_PORT);
        when(this.request.getContextPath()).thenReturn(CONTEXT_PATH);
        when(this.config.getServletContext()).thenReturn(context);
        when(this.context.getAttribute(CmisRepositoryContextListener.SERVICES_FACTORY)).thenReturn(cmisServiceFactory);
        when(cmisServiceFactory.getService((CallContext) any())).thenReturn(cmisService);
    }

    @Test
    public void testAtomPubHeadRequest() throws URISyntaxException, IOException, ServletException {
        String requestURI = CONTEXT_PATH + ATOMPUB_SERVLET_PATH + "/" + REPOSITORY_ID + "/content";
        String queryString = "id=123";
        doTestHeadRequest(new CmisAtomPubServlet(), ATOMPUB_SERVLET_PATH, requestURI, queryString);
    }

    @Test
    public void testBrowserBindingHeadRequest() throws URISyntaxException, IOException, ServletException {
        String requestURI = CONTEXT_PATH + BROWSER_SERVLET_PATH + "/" + REPOSITORY_ID + "/root";
        String queryString = "cmisselector=content&objectId=123";
        doTestHeadRequest(new CmisBrowserBindingServlet(), BROWSER_SERVLET_PATH, requestURI, queryString);
    }

    private void doTestHeadRequest(Servlet servlet, String servletPath, String requestURI, String queryString)
            throws URISyntaxException, IOException, ServletException {

        when(this.request.getServletPath()).thenReturn(servletPath);
        when(this.request.getRequestURI()).thenReturn(requestURI);
        when(this.request.getQueryString()).thenReturn(queryString);
        when(this.request.getMethod()).thenReturn(Dispatcher.METHOD_HEAD);

        when(this.cmisService.getContentStream(REPOSITORY_ID, "123", null, null, null, null)).thenReturn(contentStream);
        when(this.contentStream.getStream()).thenReturn(new ByteArrayInputStream("789".getBytes()));
        when(this.contentStream.getETag()).thenReturn("456");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse response = getMockResponse(baos);
        servlet.init(config);
        servlet.service(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setHeader("ETag", "\"456\"");
    }

    private HttpServletResponse getMockResponse(OutputStream out) throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream sos = new StubServletOutputStream(out);
        PrintWriter printWriter = new PrintWriter(sos);
        when(resp.getOutputStream()).thenReturn(sos);
        when(resp.getWriter()).thenReturn(printWriter);
        return resp;
    }

    static class UrlServiceCall extends AbstractAtomPubServiceCall {
        @Override
        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            // no implementation
        }
    }

    static class StubServletOutputStream extends ServletOutputStream {
        private OutputStream os;

        public StubServletOutputStream(OutputStream os) {
            this.os = os;
        }

        @Override
        public void write(int i) throws IOException {
            os.write(i);
        }
    }
}