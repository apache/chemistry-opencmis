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
package org.apache.chemistry.opencmis.browser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * CMIS Browser Servlet.
 */
public class BrowseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(BrowseServlet.class);

    private static final String CONTEXT_PREFIX = "{ctx}";
    private static final String PARAM_URL = "url";
    private static final String PARAM_OVERRIDE_STYLESHEET = "overrideStylesheet";
    private static final int PARAM_URL_MIN_LEN = PARAM_URL.length() + "=".length() + "http".length() + "://".length()
            + 1;
    private static final String INIT_PARAM_AUXROOT = "auxroot";
    private static final String INIT_PARAM_ALLOW = "allow";
    private static final String INIT_PARAM_STYLESHEET = "stylesheet:";
    private static final String INIT_PARAM_OVERRIDE_STYLESHEET = "override-stylesheet:";

    private static final int BUFFER_SIZE = 64 * 1024;

    private String fAuxRoot = "";
    private String fAllow = ".*";
    private Map<String, Source> fStyleSheets;
    private Map<String, Source> fOverrideStyleSheets;

    /**
     * Initializes the browser servlet.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void init(ServletConfig config) throws ServletException {
        fStyleSheets = new HashMap<String, Source>();
        fOverrideStyleSheets = new HashMap<String, Source>();

        DocumentBuilder builder = null;
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builder = builderFactory.newDocumentBuilder();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Enumeration<String> initParams = config.getInitParameterNames();
        while (initParams.hasMoreElements()) {
            String param = initParams.nextElement();
            String stylesheetKey = null;
            boolean isOverride = false;

            if (param.startsWith(INIT_PARAM_STYLESHEET)) {
                stylesheetKey = param.substring(INIT_PARAM_STYLESHEET.length());
            } else if (param.startsWith(INIT_PARAM_OVERRIDE_STYLESHEET)) {
                stylesheetKey = param.substring(INIT_PARAM_OVERRIDE_STYLESHEET.length());
                isOverride = true;
            }

            if (stylesheetKey != null) {
                String stylesheetFileName = config.getInitParameter(param);
                InputStream stream = config.getServletContext().getResourceAsStream(stylesheetFileName);
                if (stream != null) {
                    try {
                        Document xslDoc = builder.parse(stream);
                        addStylesheet(stylesheetKey, new DOMSource(xslDoc), isOverride);

                        log.info("Stylesheet: '" + stylesheetKey + "' -> '" + stylesheetFileName + "'");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        String initAuxRoot = config.getInitParameter(INIT_PARAM_AUXROOT);
        if (initAuxRoot != null) {
            fAuxRoot = initAuxRoot;
            log.info("Auxiliary root: " + fAuxRoot);
        }

        String initAllow = config.getInitParameter(INIT_PARAM_ALLOW);
        if (initAllow != null) {
            fAllow = initAllow;
            log.info("Allow pattern: " + fAllow);
        }
    }

    /**
     * Handles GET requests.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String browseUrl = null;
        String overrideStylesheet = null;

        // The url parameter must come first!
        String queryString = req.getQueryString();

        if ((queryString != null) && (queryString.startsWith(PARAM_URL))) {
            int urlEnd = queryString.indexOf(PARAM_OVERRIDE_STYLESHEET + "=");
            if (urlEnd == -1) {
                urlEnd = queryString.length();
            } else {
                overrideStylesheet = queryString.substring(urlEnd + PARAM_OVERRIDE_STYLESHEET.length() + 1);
                --urlEnd;
            }

            browseUrl = queryString.substring(PARAM_URL.length() + 1, urlEnd);

            if (browseUrl.length() < PARAM_URL_MIN_LEN) {
                browseUrl = null;
            }
        }

        if (browseUrl == null) {
            printInput(req, resp);
            return;
        }

        doBrowse(req, resp, browseUrl, overrideStylesheet);
    }

    /**
     * Main method of the browser.
     */
    protected void doBrowse(HttpServletRequest req, HttpServletResponse resp, String browseUrl,
            String overrideStylesheet) throws ServletException, IOException {
        // check if decoding is necessary
        // (if the char after 'http' or 'https' is not a colon, then it must be
        // decoded)
        if (browseUrl.charAt(4) != ':' && browseUrl.charAt(5) != ':') {
            browseUrl = URLDecoder.decode(browseUrl, "UTF-8");
        }

        // check URL
        if (!browseUrl.matches(fAllow)) {
            printError(req, resp, "Prohibited URL!", null);
            return;
        }

        try {
            // get content
            URL url = new URL(browseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setRequestMethod("GET");
            String authHeader = req.getHeader("Authorization");
            if (authHeader != null) {
                conn.setRequestProperty("Authorization", authHeader);
            }
            conn.connect();

            // ask for login
            if (conn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                resp.setHeader("WWW-Authenticate", conn.getHeaderField("WWW-Authenticate"));
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required");
                return;
            }

            // debug messages
            if (log.isDebugEnabled()) {
                log.debug("'" + browseUrl + "' -> '" + conn.getContentType() + "'");
            }

            // find stylesheet
            Source stylesheet = getStylesheet(conn.getContentType(), overrideStylesheet);

            OutputStream out = null;
            InputStream in = new BufferedInputStream(conn.getInputStream(), BUFFER_SIZE);

            if (stylesheet == null) {
                // no stylesheet found -> conduct content
                resp.setContentType(conn.getContentType());
                out = new BufferedOutputStream(resp.getOutputStream(), BUFFER_SIZE);

                byte[] buffer = new byte[BUFFER_SIZE];
                int b;
                while ((b = in.read(buffer)) > -1) {
                    out.write(buffer, 0, b);
                }
            } else {
                // apply stylesheet
                TransformerFactory f = TransformerFactory.newInstance();
                Transformer t = f.newTransformer(stylesheet);
                t.setParameter("browseUrl", getServletUrl(req) + "?" + PARAM_URL + "=");
                t.setParameter("auxRoot", getAuxRoot(req, fAuxRoot));
                t.setParameter("browseOverrideStylesheet", "&" + PARAM_OVERRIDE_STYLESHEET + "=");

                resp.setContentType("text/html");
                out = new BufferedOutputStream(resp.getOutputStream(), BUFFER_SIZE);

                Source s = new StreamSource(in);
                Result r = new StreamResult(out);
                t.transform(s, r);
            }

            try {
                out.flush();
                out.close();
            } catch (Exception e) {
            }

            try {
                in.close();
            } catch (Exception e) {
            }
        } catch (Exception e) {
            printError(req, resp, e.getMessage(), e);
            return;
        }
    }

    // ---- utilities ----

    /**
     * Assigns a stylesheet to a content type.
     */
    private void addStylesheet(String contentType, Source source, boolean override) {
        if ((contentType == null) || (source == null)) {
            return;
        }

        if (override) {
            fOverrideStyleSheets.put(contentType.trim().toLowerCase(), source);
        } else {
            fStyleSheets.put(contentType.trim().toLowerCase(), source);
        }
    }

    /**
     * Returns the stylesheet for the given override stylesheet, if there is is
     * not override stylesheet given then the stylesheet for the given content
     * type or <code>null</code> if no stylesheet is assigned to content type.
     */
    private Source getStylesheet(String contentType, String overrideStylesheet) {
        if (contentType == null) {
            return null;
        }

        Source source = null;

        // First check if there is an override given and it has a match.
        if (overrideStylesheet != null && overrideStylesheet.length() > 0) {
            source = fOverrideStyleSheets.get(overrideStylesheet);
        }

        // If there is not match then check the content type map.
        if (source == null) {
            String[] ctp = contentType.trim().toLowerCase().split(";");

            StringBuilder match = new StringBuilder();
            int i = 0;
            while (source == null && i < ctp.length) {
                if (i > 0) {
                    match.append(";");
                }
                match.append(ctp[i]);
                source = fStyleSheets.get(match.toString());
                i++;
            }
        }

        return source;
    }

    /**
     * Returns the context URL of this servlet.
     */
    private String getContextUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        int port = request.getServerPort();

        if ("http".equals(scheme) && (port == 80)) {
            port = -1;
        }
        if ("https".equals(scheme) && (port == 443)) {
            port = -1;
        }

        return scheme + "://" + request.getServerName() + (port > 0 ? ":" + port : "") + request.getContextPath();
    }

    /**
     * Returns the URL of this servlet.
     */
    private String getServletUrl(HttpServletRequest request) {
        return getContextUrl(request) + request.getServletPath();
    }

    /**
     * Returns the root URL of auxiliary files.
     */
    private String getAuxRoot(HttpServletRequest request, String auxRoot) {
        if (auxRoot == null) {
            return getContextUrl(request);
        } else if (auxRoot.startsWith(CONTEXT_PREFIX)) {
            return getContextUrl(request) + auxRoot.substring(CONTEXT_PREFIX.length());
        } else {
            return auxRoot;
        }
    }

    // --- HTML methods ----

    /**
     * Prints a HTML header with styles.
     */
    private void printHeader(PrintWriter pw, String title) {
        pw.print("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
        pw.println("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        pw.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        pw.println("<head>");
        pw.println("<title>" + title + "</title>");
        pw.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />");
        pw.println("<style type=\"text/css\">");
        pw.println("body { font-family: arial,sans-serif; font-size: 10pt; }");
        pw.println("div.box { background-color: #f6f1de; margin-top: 10px;"
                + " margin-bottom: 10px; margin-left: 0px; margin-right: 0px;"
                + " padding: 5px; border-style: solid; border-width: 1px; border-color: #888a85; }");
        pw.println("</style>");
        pw.println("</head>");
        pw.println("<body>");
    }

    /**
     * Prints a HTML footer.
     */
    private void printFooter(PrintWriter pw) {
        pw.println("</body>");
        pw.println("</html>");
    }

    /**
     * Prints a HTML error message.
     */
    private void printError(HttpServletRequest req, HttpServletResponse resp, String message, Exception e)
            throws IOException {
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter pw = resp.getWriter();

        printHeader(pw, "Error");
        pw.println("<div class=\"box\">");
        pw.println("<h3>" + message + "</h3>");

        if (e != null) {
            pw.print("<pre>");
            e.printStackTrace(pw);
            pw.println("</pre>");
        }

        pw.println("</div>");
        printFooter(pw);
    }

    /**
     * Prints an HTML input box.
     */
    private void printInput(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter pw = resp.getWriter();

        printHeader(pw, "OpenCMIS Browser");
        pw.println("<img src=\"" + getAuxRoot(req, fAuxRoot) + "cmis.png\" " + "style=\"float: right;\" />");
        pw.println("<h1 style=\"font-family: Georgia;\">OpenCMIS Browser</h1>");
        pw.println("<div class=\"box\">");
        pw.println("<form action=\"\" method=\"GET\">");
        pw.println("CMIS AtomPub URL: ");
        pw.println("<input name=\"url\" type=\"text\" size=\"100\" value=\"\"/>");
        pw.println("<input type=\"submit\" value=\" GO \"/>");
        pw.println("</form>");
        pw.println("</div>");
        printFooter(pw);
    }
}
