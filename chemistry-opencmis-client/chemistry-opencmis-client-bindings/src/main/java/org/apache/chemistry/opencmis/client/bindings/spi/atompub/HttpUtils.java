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
package org.apache.chemistry.opencmis.client.bindings.spi.atompub;

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
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.spi.AbstractAuthenticationProvider;
import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * HTTP helper methods.
 */
public class HttpUtils {

    private static final Log log = LogFactory.getLog(HttpUtils.class);

    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    private HttpUtils() {
    }

    public static Response invokeGET(UrlBuilder url, Session session) {
        return invoke(url, "GET", null, null, session, null, null);
    }

    public static Response invokeGET(UrlBuilder url, Session session, BigInteger offset, BigInteger length) {
        return invoke(url, "GET", null, null, session, offset, length);
    }

    public static Response invokePOST(UrlBuilder url, String contentType, Output writer, Session session) {
        return invoke(url, "POST", contentType, writer, session, null, null);
    }

    public static Response invokePUT(UrlBuilder url, String contentType, Output writer, Session session) {
        return invoke(url, "PUT", contentType, writer, session, null, null);
    }

    public static Response invokeDELETE(UrlBuilder url, Session session) {
        return invoke(url, "DELETE", null, null, session, null, null);
    }

    private static Response invoke(UrlBuilder url, String method, String contentType, Output writer, Session session,
            BigInteger offset, BigInteger length) {
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
            conn.setRequestProperty("User-Agent", "Apache Chemistry OpenCMIS");

            // set content type
            if (contentType != null) {
                conn.setRequestProperty("Content-Type", contentType);
            }

            // authenticate
            AbstractAuthenticationProvider authProvider = CmisBindingsHelper.getAuthenticationProvider(session);
            if (authProvider != null) {
                Map<String, List<String>> httpHeaders = authProvider.getHTTPHeaders(url.toString());
                if (httpHeaders != null) {
                    for (Map.Entry<String, List<String>> header : httpHeaders.entrySet()) {
                        if (header.getValue() != null) {
                            for (String value : header.getValue()) {
                                conn.setRequestProperty(header.getKey(), value);
                            }
                        }
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
            if ((session.get(SessionParameter.COMPRESSION) instanceof String)
                    && (Boolean.parseBoolean((String) session.get(SessionParameter.COMPRESSION)))) {
                conn.setRequestProperty("Accept-Encoding", "gzip");
            } else if ((session.get(SessionParameter.COMPRESSION) instanceof Boolean)
                    && ((Boolean) session.get(SessionParameter.COMPRESSION)).booleanValue()) {
                conn.setRequestProperty("Accept-Encoding", "gzip");
            }

            // send data
            if (writer != null) {
                conn.setChunkedStreamingMode(BUFFER_SIZE);
                OutputStream out = new BufferedOutputStream(conn.getOutputStream(), BUFFER_SIZE);
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
        private int responseCode;
        private String responseMessage;
        private Map<String, List<String>> headers;
        private InputStream stream;
        private String errorContent;
        private BigInteger length;

        public Response(int responseCode, String responseMessage, Map<String, List<String>> headers,
                InputStream stream, InputStream errorStream) {
            this.responseCode = responseCode;
            this.responseMessage = responseMessage;
            this.stream = stream;

            this.headers = new HashMap<String, List<String>>();
            if (headers != null) {
                for (Map.Entry<String, List<String>> e : headers.entrySet()) {
                    this.headers.put(e.getKey() == null ? null : e.getKey().toLowerCase(), e.getValue());
                }
            }

            // if there is an error page, get it
            if (errorStream != null) {
                String contentType = getContentTypeHeader();
                if ((contentType != null) && (contentType.toLowerCase().startsWith("text/"))) {
                    StringBuilder sb = new StringBuilder();

                    try {
                        InputStreamReader reader = new InputStreamReader(errorStream);
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
                } else {
                    try {
                        errorStream.close();
                    } catch (IOException e) {
                    }
                }

                if (stream != null) {
                    try {
                        stream.close();
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
                            this.stream = new GZIPInputStream(stream, 4096);
                        } catch (IOException e) {
                            errorContent = e.getMessage();
                            try {
                                stream.close();
                            } catch (IOException ec) {
                            }
                        }
                    } else if (encoding.toLowerCase().trim().equals("deflate")) {
                        // if the stream is deflate encoded, decode it
                        length = null;
                        this.stream = new InflaterInputStream(stream, new Inflater(true), 4096);
                    }
                }

                String transferEncoding = getContentTransferEncoding();
                if ((transferEncoding != null) && (transferEncoding.toLowerCase().trim().equals("base64"))) {
                    // if the stream is base64 encoded, decode it
                    length = null;
                    this.stream = new Base64.InputStream(stream);
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
