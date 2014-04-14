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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.server.impl.atompub.CmisAtomPubServlet;
import org.apache.chemistry.opencmis.server.impl.browser.CmisBrowserBindingServlet;
import org.apache.chemistry.opencmis.server.shared.AbstractCmisHttpServlet;
import org.apache.chemistry.opencmis.server.shared.Dispatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/*
 *
 */
public class HttpRequestMethodsTest {
    private static final String CONTEXT_PATH = "/context";
    private static final String ATOMPUB_SERVLET_PATH = "/cmisatom";
    private static final String BROWSER_SERVLET_PATH = "cmisjson";
    private static final String REPOSITORY_ID = "22d2880a-bae5-4cfc-a5a9-3b2618e6e11c";
    private static final int BACKEND_SERVER_PORT = 8080;
    private static final String BACKEND_SERVER_NAME = "www.backend.be";
    private static final String BACKEND_SERVER_PROTO = "http";
    private static final String CONTENT_VALUE = "Hello World!";

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
        when(this.context.getAttribute(CmisRepositoryContextListener.SERVICES_FACTORY)).thenReturn(
                cmisServiceFactory);
        when(cmisServiceFactory.getService((CallContext) any())).thenReturn(cmisService);
    }

    @Test
    public void testAtomPubOptionsRequest() 
            throws URISyntaxException, IOException, ServletException {
        String optionsAllowHeader = Dispatcher.METHOD_DELETE + "," +
                Dispatcher.METHOD_GET + "," + Dispatcher.METHOD_POST + "," + 
                Dispatcher.METHOD_PUT + "," + AbstractCmisHttpServlet.METHOD_HEAD + "," + 
                AbstractCmisHttpServlet.METHOD_OPTIONS;
        doTestOptionsRequest(new CmisAtomPubServlet(), optionsAllowHeader);
    }
    
    @Test
    public void testBrowserBindingOptionsRequest() 
            throws URISyntaxException, IOException, ServletException {
        String optionsAllowHeader = Dispatcher.METHOD_GET + "," + 
                Dispatcher.METHOD_POST + "," + AbstractCmisHttpServlet.METHOD_HEAD + "," + 
                AbstractCmisHttpServlet.METHOD_OPTIONS;
        doTestOptionsRequest(new CmisBrowserBindingServlet(), optionsAllowHeader);
    }
    
    private void doTestOptionsRequest(Servlet servlet, String optionsAllowHeader) 
            throws URISyntaxException, IOException, ServletException {
        when(this.request.getMethod()).thenReturn(AbstractCmisHttpServlet.METHOD_OPTIONS);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse response = getMockResponse(baos);
        servlet.init(config);
        servlet.service(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).addHeader("Allow", optionsAllowHeader);
        assertEquals(0, baos.size());
    }
    
    @Test
    public void testAtomPubHeadRequest() throws URISyntaxException, IOException, ServletException {
        when(this.request.getServletPath()).thenReturn(ATOMPUB_SERVLET_PATH);
        when(this.request.getRequestURI()).thenReturn(CONTEXT_PATH + ATOMPUB_SERVLET_PATH + "/" + 
                REPOSITORY_ID + "/content");
        when(this.request.getQueryString()).thenReturn("id=123");
        doTestHeadRequest(new CmisAtomPubServlet());
    }

    @Test
    public void testBrowserBindingHeadRequest() 
            throws URISyntaxException, IOException, ServletException {
        when(this.request.getServletPath()).thenReturn(BROWSER_SERVLET_PATH);
        when(this.request.getRequestURI()).thenReturn(CONTEXT_PATH + BROWSER_SERVLET_PATH + "/" + 
                REPOSITORY_ID + "/root");
        when(this.request.getQueryString()).thenReturn("objectId=123&cmisselector=content");
        doTestHeadRequest(new CmisBrowserBindingServlet());
    }

    private void doTestHeadRequest(Servlet servlet)
            throws URISyntaxException, IOException, ServletException {
        when(this.request.getMethod()).thenReturn(AbstractCmisHttpServlet.METHOD_HEAD);
        
        when(this.cmisService.getContentStream(
                REPOSITORY_ID, "123", null, null, null, null)).thenReturn(contentStream);
        String digest = "12345";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse response = getHeadResponse(servlet, digest, baos);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setHeader("ETag", "\"" + digest + "\"");
        verify(response).setContentType(Constants.MEDIATYPE_OCTETSTREAM);
        verify(response).setContentLength(CONTENT_VALUE.length());
        assertEquals(0, baos.size());
    }

    private HttpServletResponse getHeadResponse(Servlet servlet, String digest, OutputStream out)
            throws IOException, ServletException {
        final byte[] contentBytes = CONTENT_VALUE.getBytes();
        InputStream in = new ByteArrayInputStream(contentBytes);
        when(this.contentStream.getStream()).thenReturn(in);
        when(this.contentStream.getETag()).thenReturn(digest);
        
        HttpServletResponse response = getMockResponse(out);
        servlet.init(config);
        servlet.service(request, response);
        return response;
    }
    
    private HttpServletResponse getMockResponse(OutputStream out)
            throws IOException {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream sos = new StubServletOutputStream(out);
        PrintWriter printWriter = new PrintWriter(sos);
        when(resp.getOutputStream()).thenReturn(sos);
        when(resp.getWriter()).thenReturn(printWriter);
        return resp;
    }    

    static class StubServletOutputStream extends ServletOutputStream {
        private OutputStream os;
        
        public StubServletOutputStream(OutputStream os) {
            this.os = os;
        }
        
        public void write(int i) throws IOException {
            os.write(i);
        }
    }
}
