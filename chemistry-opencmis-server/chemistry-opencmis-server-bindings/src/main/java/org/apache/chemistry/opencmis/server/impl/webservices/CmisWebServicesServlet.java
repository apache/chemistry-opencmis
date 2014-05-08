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
package org.apache.chemistry.opencmis.server.impl.webservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceFeature;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.server.impl.CmisRepositoryContextListener;
import org.apache.chemistry.opencmis.server.shared.Dispatcher;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.developer.StreamingAttachmentFeature;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.WSServlet;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;

public class CmisWebServicesServlet extends WSServlet {

    public static final String PARAM_CMIS_VERSION = "cmisVersion";
    public static final String CMIS_VERSION = "org.apache.chemistry.opencmis.cmisVersion";

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(CmisWebServicesServlet.class.getName());

    private static final int MAX_SOAP_SIZE = 10 * 1024 * 1024;

    private static final String CMIS10_PATH = "/WEB-INF/cmis10/";
    private static final String CMIS11_PATH = "/WEB-INF/cmis11/";

    private static final Pattern BASE_PATTERN = Pattern.compile("<%cmisbase%>");
    private static final Pattern CORE_PATTERN = Pattern.compile("<%cmiscore%>");
    private static final Pattern MSG_PATTERN = Pattern.compile("<%cmismsg%>");

    private CmisVersion cmisVersion;

    private Map<String, String> docs;

    @Override
    public void init(ServletConfig config) throws ServletException {

        // get CMIS version
        String cmisVersionStr = config.getInitParameter(PARAM_CMIS_VERSION);
        if (cmisVersionStr != null) {
            try {
                cmisVersion = CmisVersion.fromValue(cmisVersionStr);
            } catch (IllegalArgumentException e) {
                LOG.warn("CMIS version is invalid! Setting it to CMIS 1.0.");
                cmisVersion = CmisVersion.CMIS_1_0;
            }
        } else {
            LOG.warn("CMIS version is not defined! Setting it to CMIS 1.0.");
            cmisVersion = CmisVersion.CMIS_1_0;
        }

        // set up WSDL and XSD documents
        docs = new HashMap<String, String>();

        String path = (cmisVersion == CmisVersion.CMIS_1_0 ? CMIS10_PATH : CMIS11_PATH);

        docs.put("wsdl", readFile(config, path + "CMISWS-Service.wsdl.template"));
        docs.put("core", readFile(config, path + "CMIS-Core.xsd.template"));
        docs.put("msg", readFile(config, path + "CMIS-Messaging.xsd.template"));

        super.init(config);
    }

    private String readFile(ServletConfig config, String path) throws ServletException {
        InputStream stream = config.getServletContext().getResourceAsStream(path);
        if (stream == null) {
            throw new ServletException("Cannot find file '" + path + "'!");
        }

        try {
            return IOUtils.readAllLines(stream);
        } catch (IOException e) {
            throw new ServletException("Cannot read file '" + path + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // set CMIS version
        request.setAttribute(CMIS_VERSION, cmisVersion);

        // handle GET requests
        if (request.getMethod().equals("GET")) {
            UrlBuilder baseUrl = compileBaseUrl(request, response);

            String queryString = request.getQueryString();
            if (queryString != null) {
                String doc = docs.get(queryString.toLowerCase(Locale.ENGLISH));
                if (doc != null) {
                    printXml(request, response, doc, baseUrl);
                    return;
                }
            }

            printPage(request, response, baseUrl);
            return;
        }

        // handle other non-POST requests
        if (!request.getMethod().equals("POST")) {
            printError(request, response, "Not a HTTP POST request.");
            return;
        }

        // handle POST requests
        ProtectionRequestWrapper requestWrapper = null;
        try {
            requestWrapper = new ProtectionRequestWrapper(request, MAX_SOAP_SIZE);
        } catch (ServletException e) {
            printError(request, response, "The request is not MTOM encoded.");
            return;
        }

        super.service(requestWrapper, response);
    }

    private void printXml(HttpServletRequest request, HttpServletResponse response, String doc, UrlBuilder baseUrl)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/xml");
        response.setCharacterEncoding(IOUtils.UTF8);

        String respDoc = doc;
        respDoc = BASE_PATTERN.matcher(respDoc).replaceAll(baseUrl.toString());
        respDoc = CORE_PATTERN.matcher(respDoc).replaceAll(
                (new UrlBuilder(baseUrl)).addPath("cmis").addParameter("core").toString());
        respDoc = MSG_PATTERN.matcher(respDoc).replaceAll(
                (new UrlBuilder(baseUrl)).addPath("cmis").addParameter("msg").toString());

        PrintWriter pw = response.getWriter();
        pw.print(respDoc);
        pw.flush();
    }

    private void printPage(HttpServletRequest request, HttpServletResponse response, UrlBuilder baseUrl)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");
        response.setCharacterEncoding(IOUtils.UTF8);

        String urlEscaped = StringEscapeUtils.escapeHtml((new UrlBuilder(baseUrl)).addPath("cmis").addParameter("wsdl")
                .toString());

        PrintWriter pw = response.getWriter();

        pw.print("<html><head><title>Apache Chemistry OpenCMIS - CMIS "
                + cmisVersion.value()
                + " Web Services</title>"
                + "<style><!--H1 {font-size:24px;line-height:normal;font-weight:bold;background-color:#f0f0f0;color:#003366;border-bottom:1px solid #3c78b5;padding:2px;} "
                + "BODY {font-family:Verdana,arial,sans-serif;color:black;font-size:14px;} "
                + "HR {color:#3c78b5;height:1px;}--></style></head><body>");
        pw.print("<h1>CMIS " + cmisVersion.value() + " Web Services</h1>");
        pw.print("<p>CMIS WSDL for all services: <a href=\"" + urlEscaped + "\">" + urlEscaped + "</a></p>");

        pw.print("</html></body>");
        pw.flush();
    }

    private void printError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("text/xml");
        response.setCharacterEncoding(IOUtils.UTF8);

        PrintWriter pw = response.getWriter();

        String messageEscaped = StringEscapeUtils.escapeXml(message);

        pw.println("<?xml version='1.0' encoding='UTF-8'?>");
        pw.println("<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">");
        pw.println("<S:Body>");
        pw.println("<S:Fault>");
        pw.println("<faultcode>S:Client</faultcode>");
        pw.println("<faultstring>" + messageEscaped + "</faultstring>");
        pw.println("<detail>");
        pw.println("<cmisFault xmlns=\"http://docs.oasis-open.org/ns/cmis/messaging/200908/\">");
        pw.println("<type>runtime</type>");
        pw.println("<code>0</code>");
        pw.println("<message>" + messageEscaped + "</message>");
        pw.println("</cmisFault>");
        pw.println("</detail>");
        pw.println("</S:Fault>");
        pw.println("</S:Body>");
        pw.println("</S:Envelope>");

        pw.flush();
    }

    private UrlBuilder compileBaseUrl(HttpServletRequest request, HttpServletResponse response) {
        UrlBuilder result;

        String baseUrl = (String) request.getAttribute(Dispatcher.BASE_URL_ATTRIBUTE);
        if (baseUrl != null) {
            result = new UrlBuilder(baseUrl);
        } else {
            result = new UrlBuilder(request.getScheme(), request.getServerName(), request.getServerPort(), null);
            result.addPath(request.getContextPath());
            result.addPath(request.getServletPath());
        }

        return result;
    }

    @Override
    protected WSServletDelegate getDelegate(ServletConfig servletConfig) {
        WSServletDelegate delegate = super.getDelegate(servletConfig);

        // set temp directory and the threshold for all services with a
        // StreamingAttachment annotation
        if (delegate.adapters != null) {
            // get the CmisService factory
            CmisServiceFactory factory = (CmisServiceFactory) getServletContext().getAttribute(
                    CmisRepositoryContextListener.SERVICES_FACTORY);

            if (factory == null) {
                throw new CmisRuntimeException("Service factory not available! Configuration problem?");
            }

            // iterate of all adapters
            for (ServletAdapter adapter : delegate.adapters) {
                WSFeatureList wsfl = adapter.getEndpoint().getBinding().getFeatures();
                for (WebServiceFeature ft : wsfl) {
                    if (ft instanceof StreamingAttachmentFeature) {
                        ((StreamingAttachmentFeature) ft).setDir(factory.getTempDirectory().getAbsolutePath());
                        setMemoryThreshold(factory, (StreamingAttachmentFeature) ft);
                    }
                }
            }
        }

        return delegate;
    }

    private void setMemoryThreshold(CmisServiceFactory factory, StreamingAttachmentFeature ft) {
        try {
            // JAX-WS RI 2.1
            ft.setMemoryThreshold(factory.getMemoryThreshold());
        } catch (NoSuchMethodError e) {
            // JAX-WS RI 2.2
            // see CMIS-626
            try {
                Method m = ft.getClass().getMethod("setMemoryThreshold", long.class);
                m.invoke(ft, (long) factory.getMemoryThreshold());
            } catch (Exception e2) {
                LOG.warn("Could not set memory threshold for streaming");
            }
        }
    }
}
