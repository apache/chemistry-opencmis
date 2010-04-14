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
package org.apache.opencmis.commons.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.RelationshipDirection;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;

/**
 * Utility class that helps building URLs.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class UrlBuilder {

  private StringBuilder fUrl;
  private StringBuilder fQuery;

  /**
   * Constructor.
   * 
   * @param url
   *          initial URL
   */
  public UrlBuilder(String url) {
    if (url == null) {
      throw new IllegalArgumentException("URL must be set");
    }

    fUrl = new StringBuilder();
    fQuery = new StringBuilder();

    int qm = url.indexOf('?');
    if (qm == -1) {
      fUrl.append(url);
    }
    else {
      fUrl.append(url.substring(0, qm));
      if (qm < url.length()) {
        fQuery.append(url.substring(qm + 1));
      }
    }
  }

  /**
   * Constructor.
   * 
   * @param scheme
   *          scheme
   * @param host
   *          host
   * @param port
   *          port
   * @param path
   *          path
   */
  public UrlBuilder(String scheme, String host, int port, String path) {

    if ("http".equalsIgnoreCase(scheme) && (port == 80)) {
      port = -1;
    }
    if ("https".equalsIgnoreCase(scheme) && (port == 443)) {
      port = -1;
    }

    fUrl = new StringBuilder();
    fQuery = new StringBuilder();

    fUrl.append(scheme);
    fUrl.append("://");
    fUrl.append(host);
    if (port > 0) {
      fUrl.append(":" + port);
    }
    if (path != null) {
      fUrl.append(path);
    }
  }

  /**
   * Copy constructor.
   */
  public UrlBuilder(UrlBuilder urlBuilder) {
    fUrl = new StringBuilder(urlBuilder.fUrl);
    fQuery = new StringBuilder(urlBuilder.fQuery);
  }

  /**
   * Adds a parameter to the URL.
   * 
   * @param name
   *          parameter name
   * @param value
   *          parameter value
   */
  public void addParameter(String name, Object value) {
    if ((name == null) || (value == null)) {
      return;
    }

    String valueStr = normalizeParameter(value);

    if (fQuery.length() > 0) {
      fQuery.append('&');
    }
    fQuery.append(name);
    fQuery.append("=");
    try {
      fQuery.append(URLEncoder.encode(valueStr, "UTF-8"));
    }
    catch (UnsupportedEncodingException e) {
    }
  }

  /**
   * Adds a path segment to the URL.
   * 
   * @param pathSegment
   *          the path segment.
   */
  public void addPath(String pathSegment) {
    if ((pathSegment == null) || (pathSegment.length() == 0)) {
      return;
    }

    if (fUrl.charAt(fUrl.length() - 1) != '/') {
      fUrl.append('/');
    }

    if (pathSegment.charAt(0) == '/') {
      pathSegment = pathSegment.substring(1);
    }

    try {
      fUrl.append(URLEncoder.encode(pathSegment, "UTF-8"));
    }
    catch (UnsupportedEncodingException e) {
    }
  }

  /**
   * Converts an object to a String that can be used as a parameter value.
   */
  public static String normalizeParameter(Object value) {
    if (value == null) {
      return null;
    }
    else if (value instanceof IncludeRelationships) {
      return ((IncludeRelationships) value).value();
    }
    else if (value instanceof VersioningState) {
      return ((VersioningState) value).value();
    }
    else if (value instanceof UnfileObjects) {
      return ((UnfileObjects) value).value();
    }
    else if (value instanceof RelationshipDirection) {
      return ((RelationshipDirection) value).value();
    }
    else if (value instanceof ReturnVersion) {
      return ((ReturnVersion) value).value();
    }
    else if (value instanceof AclPropagation) {
      return ((AclPropagation) value).value();
    }

    return value.toString();
  }

  @Override
  public String toString() {
    return fUrl.toString() + (fQuery.length() == 0 ? "" : "?" + fQuery.toString());
  }
}
