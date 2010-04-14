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
package org.apache.opencmis.client.provider.spi.atompub;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.client.provider.impl.CmisProviderHelper;
import org.apache.opencmis.client.provider.spi.AbstractAuthenticationProvider;
import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.opencmis.commons.impl.UrlBuilder;

/**
 * HTTP helper methods.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class HttpUtils {

  private static final Log log = LogFactory.getLog(HttpUtils.class);

  private static final int BUFFER_SIZE = 4096;

  private HttpUtils() {
  }

  public static Response invokeGET(UrlBuilder url, Session session) {
    return invoke(url, "GET", null, null, session, null, null);
  }

  public static Response invokeGET(UrlBuilder url, Session session, BigInteger offset,
      BigInteger length) {
    return invoke(url, "GET", null, null, session, offset, length);
  }

  public static Response invokePOST(UrlBuilder url, String contentType, Output writer,
      Session session) {
    return invoke(url, "POST", contentType, writer, session, null, null);
  }

  public static Response invokePUT(UrlBuilder url, String contentType, Output writer,
      Session session) {
    return invoke(url, "PUT", contentType, writer, session, null, null);
  }

  public static Response invokeDELETE(UrlBuilder url, Session session) {
    return invoke(url, "DELETE", null, null, session, null, null);
  }

  private static Response invoke(UrlBuilder url, String method, String contentType, Output writer,
      Session session, BigInteger offset, BigInteger length) {
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

      // set content type
      if (contentType != null) {
        conn.setRequestProperty("Content-Type", contentType);
      }

      // authenticate
      AbstractAuthenticationProvider authProvider = CmisProviderHelper
          .getAuthenticationProvider(session);
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

      // send data
      if (writer != null) {
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
    }
    catch (Exception e) {
      throw new CmisConnectionException("Cannot access " + url + ": " + e.getMessage(), e);
    }
  }

  /**
   * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
   * 
   */
  public static class Response {
    private int fResponseCode;
    private String fResponseMessage;
    private Map<String, List<String>> fHeaders;
    private InputStream fStream;
    private String fErrorContent;

    public Response(int responseCode, String responseMessage, Map<String, List<String>> headers,
        InputStream stream, InputStream errorStream) {
      fResponseCode = responseCode;
      fResponseMessage = responseMessage;
      fStream = stream;

      fHeaders = new HashMap<String, List<String>>();
      if (headers != null) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
          fHeaders.put(e.getKey() == null ? null : e.getKey().toLowerCase(), e.getValue());
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

            fErrorContent = sb.toString();
          }
          catch (IOException e) {
            fErrorContent = "Unable to retrieve content: " + e.getMessage();
          }
        }
      }
    }

    public int getResponseCode() {
      return fResponseCode;
    }

    public String getResponseMessage() {
      return fResponseMessage;
    }

    public Map<String, List<String>> getHeaders() {
      return fHeaders;
    }

    public String getHeader(String name) {
      List<String> list = fHeaders.get(name.toLowerCase(Locale.US));
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
      }
      catch (NumberFormatException e) {
        return null;
      }
    }

    public String getLocactionHeader() {
      return getHeader("Location");
    }

    public String getContentLocactionHeader() {
      return getHeader("Content-Location");
    }

    public BigInteger getContentLength() {
      String lenStr = getHeader("Content-Length");
      if (lenStr == null) {
        return null;
      }

      try {
        return new BigInteger(lenStr);
      }
      catch (NumberFormatException nfe) {
        return null;
      }
    }

    public InputStream getStream() {
      return fStream;
    }

    public String getErrorContent() {
      return fErrorContent;
    }
  }

  /**
   * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
   * 
   */
  public interface Output {
    void write(OutputStream out) throws Exception;
  }
}
