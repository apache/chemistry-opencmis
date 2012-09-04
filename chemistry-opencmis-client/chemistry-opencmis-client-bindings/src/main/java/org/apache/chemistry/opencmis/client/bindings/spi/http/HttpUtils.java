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
package org.apache.chemistry.opencmis.client.bindings.spi.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.chemistry.opencmis.client.bindings.impl.ClientVersion;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.spi.AuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP helper methods.
 */
public class HttpUtils {

    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    private HttpUtils() {
    }

    public static Response invokeGET(UrlBuilder url, BindingSession session) {
        return invoke(url, "GET", null, null, null, session, null, null);
    }

    public static Response invokeGET(UrlBuilder url, BindingSession session, BigInteger offset, BigInteger length) {
        return invoke(url, "GET", null, null, null, session, offset, length);
    }

    public static Response invokePOST(UrlBuilder url, String contentType, Output writer, BindingSession session) {
        return invoke(url, "POST", contentType, null, writer, session, null, null);
    }

    public static Response invokePUT(UrlBuilder url, String contentType, Map<String, String> headers, Output writer,
            BindingSession session) {
        return invoke(url, "PUT", contentType, headers, writer, session, null, null);
    }

    public static Response invokeDELETE(UrlBuilder url, BindingSession session) {
        return invoke(url, "DELETE", null, null, null, session, null, null);
    }

    private static Response invoke(UrlBuilder url, String method, String contentType, Map<String, String> headers,
            Output writer, BindingSession session, BigInteger offset, BigInteger length) {
        try {
            // log before connect
            if (log.isDebugEnabled()) {
                log.debug(method + " " + url);
            }

            // connect
            HttpURLConnection conn = (HttpURLConnection) (new URL(url.toString())).openConnection();
            conn.setRequestMethod(method);
            conn.setDoInput(true);
            conn.setDoOutput(writer != null);
            conn.setAllowUserInteraction(false);
            conn.setUseCaches(false);
            conn.setRequestProperty("User-Agent", ClientVersion.OPENCMIS_CLIENT);

            // timeouts
            int connectTimeout = session.get(SessionParameter.CONNECT_TIMEOUT, -1);
            if (connectTimeout >= 0) {
                conn.setConnectTimeout(connectTimeout);
            }

            int readTimeout = session.get(SessionParameter.READ_TIMEOUT, -1);
            if (readTimeout >= 0) {
                conn.setReadTimeout(readTimeout);
            }

            // set content type
            if (contentType != null) {
                conn.setRequestProperty("Content-Type", contentType);
            }
            // set other headers
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    conn.addRequestProperty(header.getKey(), header.getValue());
                }
            }

            // authenticate
            AuthenticationProvider authProvider = CmisBindingsHelper.getAuthenticationProvider(session);
            if (authProvider != null) {
                Map<String, List<String>> httpHeaders = authProvider.getHTTPHeaders(url.toString());
                if (httpHeaders != null) {
                    for (Map.Entry<String, List<String>> header : httpHeaders.entrySet()) {
                        if (header.getValue() != null) {
                            for (String value : header.getValue()) {
                                conn.addRequestProperty(header.getKey(), value);
                            }
                        }
                    }
                }

                if (conn instanceof HttpsURLConnection) {
                    SSLSocketFactory sf = authProvider.getSSLSocketFactory();
                    if (sf != null) {
                        ((HttpsURLConnection) conn).setSSLSocketFactory(sf);
                    }

                    HostnameVerifier hv = authProvider.getHostnameVerifier();
                    if (hv != null) {
                        ((HttpsURLConnection) conn).setHostnameVerifier(hv);
                    }
                }
            }

            // range
            if ((offset != null) || (length != null)) {
                StringBuilder sb = new StringBuilder("bytes=");

                if ((offset == null) || (offset.signum() == -1)) {
                    offset = BigInteger.ZERO;
                }

                sb.append(offset.toString());
                sb.append("-");

                if ((length != null) && (length.signum() == 1)) {
                    sb.append(offset.add(length.subtract(BigInteger.ONE)).toString());
                }

                conn.setRequestProperty("Range", sb.toString());
            }

            // compression
            Object compression = session.get(SessionParameter.COMPRESSION);
            if ((compression != null) && Boolean.parseBoolean(compression.toString())) {
                conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            }

            // locale
            if (session.get(CmisBindingsHelper.ACCEPT_LANGUAGE) instanceof String) {
                conn.setRequestProperty("Accept-Language", session.get(CmisBindingsHelper.ACCEPT_LANGUAGE).toString());
            }

            // send data
            if (writer != null) {
                conn.setChunkedStreamingMode((64 * 1024) - 1);

                OutputStream connOut = null;

                Object clientCompression = session.get(SessionParameter.CLIENT_COMPRESSION);
                if ((clientCompression != null) && Boolean.parseBoolean(clientCompression.toString())) {
                    conn.setRequestProperty("Content-Encoding", "gzip");
                    connOut = new GZIPOutputStream(conn.getOutputStream(), 4096);
                } else {
                    connOut = conn.getOutputStream();
                }

                OutputStream out = new BufferedOutputStream(connOut, BUFFER_SIZE);
                writer.write(out);
                out.flush();
            }

            // connect
            conn.connect();

            // get stream, if present
            int respCode = conn.getResponseCode();
            InputStream inputStream = null;
            if ((respCode == 200) || (respCode == 201) || (respCode == 203) || (respCode == 206)) {
                inputStream = conn.getInputStream();
            }

            // log after connect
            if (log.isTraceEnabled()) {
                log.trace(method + " " + url + " > Headers: " + conn.getHeaderFields());
            }

            // forward response HTTP headers
            if (authProvider != null) {
                authProvider.putResponseHeaders(url.toString(), respCode, conn.getHeaderFields());
            }

            // get the response
            return new Response(respCode, conn.getResponseMessage(), conn.getHeaderFields(), inputStream,
                    conn.getErrorStream());
        } catch (Exception e) {
            throw new CmisConnectionException("Cannot access " + url + ": " + e.getMessage(), e);
        }
    }

    /**
     * HTTP Response.
     */
    public static class Response {
        private final int responseCode;
        private final String responseMessage;
        private final Map<String, List<String>> headers;
        private InputStream stream;
        private String errorContent;
        private BigInteger length;
        private String charset;

        public Response(int responseCode, String responseMessage, Map<String, List<String>> headers,
                InputStream responseStream, InputStream errorStream) {
            this.responseCode = responseCode;
            this.responseMessage = responseMessage;
            stream = responseStream;

            this.headers = new HashMap<String, List<String>>();
            if (headers != null) {
                for (Map.Entry<String, List<String>> e : headers.entrySet()) {
                    this.headers.put(e.getKey() == null ? null : e.getKey().toLowerCase(), e.getValue());
                }
            }

            // determine charset
            charset = "UTF-8";
            String contentType = getContentTypeHeader();
            if (contentType != null) {
                String[] parts = contentType.split(";");
                for (int i = 1; i < parts.length; i++) {
                    String part = parts[i].trim().toLowerCase();
                    if (part.startsWith("charset")) {
                        int x = part.indexOf('=');
                        charset = part.substring(x + 1).trim();
                        break;
                    }
                }
            }

            // if there is an error page, get it
            if (errorStream != null) {
                if (contentType != null) {
                    String contentTypeLower = contentType.toLowerCase().split(";")[0];
                    if (contentTypeLower.startsWith("text/") || contentTypeLower.endsWith("+xml")
                            || contentTypeLower.startsWith("application/xml")
                            || contentTypeLower.startsWith("application/json")) {
                        StringBuilder sb = new StringBuilder();

                        try {
                            String encoding = getContentEncoding();
                            if (encoding != null) {
                                if (encoding.toLowerCase().trim().equals("gzip")) {
                                    try {
                                        errorStream = new GZIPInputStream(errorStream, 4096);
                                    } catch (IOException e) {
                                    }
                                } else if (encoding.toLowerCase().trim().equals("deflate")) {
                                    errorStream = new InflaterInputStream(errorStream, new Inflater(true), 4096);
                                }
                            }

                            InputStreamReader reader = new InputStreamReader(errorStream, charset);
                            char[] buffer = new char[4096];
                            int b;
                            while ((b = reader.read(buffer)) > -1) {
                                sb.append(buffer, 0, b);
                            }
                            reader.close();

                            errorContent = sb.toString();
                        } catch (IOException e) {
                            errorContent = "Unable to retrieve content: " + e.getMessage();
                        }
                    }
                } else {
                    try {
                        errorStream.close();
                    } catch (IOException e) {
                    }
                }

                if (responseStream != null) {
                    try {
                        responseStream.close();
                    } catch (IOException e) {
                    }
                }

                return;
            }

            // get the stream length
            String lengthStr = getHeader("Content-Length");
            if (lengthStr != null) {
                try {
                    length = new BigInteger(lengthStr);
                } catch (NumberFormatException e) {
                }
            }

            if (stream != null) {
                String encoding = getContentEncoding();
                if (encoding != null) {
                    if (encoding.toLowerCase().trim().equals("gzip")) {
                        // if the stream is gzip encoded, decode it
                        length = null;
                        try {
                            stream = new GZIPInputStream(stream, 4096);
                        } catch (IOException e) {
                            errorContent = e.getMessage();
                            stream = null;
                            try {
                                responseStream.close();
                            } catch (IOException ec) {
                            }
                        }
                    } else if (encoding.toLowerCase().trim().equals("deflate")) {
                        // if the stream is deflate encoded, decode it
                        length = null;
                        stream = new InflaterInputStream(stream, new Inflater(true), 4096);
                    }
                }

                String transferEncoding = getContentTransferEncoding();
                if ((stream != null) && (transferEncoding != null)
                        && (transferEncoding.toLowerCase().trim().equals("base64"))) {
                    // if the stream is base64 encoded, decode it
                    length = null;
                    stream = new Base64.InputStream(stream);
                }
            }
        }

        public int getResponseCode() {
            return responseCode;
        }

        public String getResponseMessage() {
            return responseMessage;
        }

        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        public String getHeader(String name) {
            List<String> list = headers.get(name.toLowerCase(Locale.US));
            if ((list == null) || (list.isEmpty())) {
                return null;
            }

            return list.get(0);
        }

        public String getContentTypeHeader() {
            return getHeader("Content-Type");
        }

        public BigInteger getContentLengthHeader() {
            String lengthStr = getHeader("Content-Length");
            if (lengthStr == null) {
                return null;
            }

            try {
                return new BigInteger(lengthStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        public String getLocactionHeader() {
            return getHeader("Location");
        }

        public String getContentLocactionHeader() {
            return getHeader("Content-Location");
        }

        public String getContentTransferEncoding() {
            return getHeader("Content-Transfer-Encoding");
        }

        public String getContentEncoding() {
            return getHeader("Content-Encoding");
        }

        public String getCharset() {
            return charset;
        }

        public BigInteger getContentLength() {
            return length;
        }

        public InputStream getStream() {
            return stream;
        }

        public String getErrorContent() {
            return errorContent;
        }
    }

    /**
     * Output interface.
     */
    public interface Output {
        void write(OutputStream out) throws Exception;
    }
}
