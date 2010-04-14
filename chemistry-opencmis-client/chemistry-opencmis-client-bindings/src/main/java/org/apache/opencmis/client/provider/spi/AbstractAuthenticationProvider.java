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
package org.apache.opencmis.client.provider.spi;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.opencmis.commons.SessionParameter;
import org.w3c.dom.Element;

/**
 * Authentication provider class.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public abstract class AbstractAuthenticationProvider implements Serializable {

  private static final long serialVersionUID = 1L;

  private Session fSession;

  /**
   * Sets the {@link Session} the authentication provider lives in.
   */
  public void setSession(Session session) {
    fSession = session;
  }

  /**
   * Returns {@link Session}.
   */
  public Session getSession() {
    return fSession;
  }

  /**
   * Returns a set of HTTP headers (key-value pairs) that should be added to a HTTP call. This will
   * be called by the AtomPub and the Web Services binding. You might want to check the binding in
   * use before you set the headers.
   * 
   * @param url
   *          the URL of the HTTP call
   * 
   * @return the HTTP headers or <code>null</code> if no additional headers should be set
   */
  public Map<String, List<String>> getHTTPHeaders(String url) {
    return null;
  }

  /**
   * Returns a SOAP header that should be added to a Web Services call.
   * 
   * @param portObject
   *          the port object
   * 
   * @return the SOAP headers or <code>null</code> if no additional headers should be set
   */
  public Element getSOAPHeaders(Object portObject) {
    return null;
  }

  /**
   * Gets the user name from the session.
   * 
   * @return the user name or <code>null</code> if the user name is not set
   */
  protected String getUser() {
    Object userObject = getSession().get(SessionParameter.USER);
    if (userObject instanceof String) {
      return (String) userObject;
    }

    return null;
  }

  /**
   * Gets the password from the session.
   * 
   * @return the password or <code>null</code> if the password is not set
   */
  protected String getPassword() {
    Object passwordObject = getSession().get(SessionParameter.PASSWORD);
    if (passwordObject instanceof String) {
      return (String) passwordObject;
    }

    return null;
  }
}
