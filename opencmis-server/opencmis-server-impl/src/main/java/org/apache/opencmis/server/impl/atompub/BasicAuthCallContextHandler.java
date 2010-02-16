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
package org.apache.opencmis.server.impl.atompub;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.opencmis.server.spi.CallContext;

/**
 * Call Context handler that handles basic authentication.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class BasicAuthCallContextHandler implements CallContextHandler {

  /**
   * Constructor.
   */
  public BasicAuthCallContextHandler() {
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.apache.opencmis.server.impl.atompub.CallContextHandler#getCallContextMap(javax.servlet.http.
   * HttpServletRequest)
   */
  public Map<String, String> getCallContextMap(HttpServletRequest request) {
    Map<String, String> result = null;

    String authHeader = request.getHeader("Authorization");
    if ((authHeader != null) && (authHeader.trim().toLowerCase().startsWith("basic "))) {
      int x = authHeader.lastIndexOf(' ');
      if (x == -1) {
        return result;
      }

      String credentials = null;
      try {
        credentials = new String(Base64.decodeBase64(authHeader.substring(x + 1).getBytes(
            "ISO-8859-1")), "ISO-8859-1");
      }
      catch (Exception e) {
        return result;
      }

      x = credentials.indexOf(':');
      if (x == -1) {
        return result;
      }

      // extract user and password and add them to map
      result = new HashMap<String, String>();
      result.put(CallContext.USERNAME, credentials.substring(0, x));
      result.put(CallContext.PASSWORD, credentials.substring(x + 1));
    }

    return result;
  }
}
