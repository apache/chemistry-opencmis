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
package org.apache.opencmis.fit;

import java.util.HashMap;
import java.util.Map;

import org.apache.opencmis.client.api.Session;
import org.apache.opencmis.client.provider.factory.CmisProviderFactory;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.provider.CmisProvider;

/**
 * Session factory for integration tests.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class SessionFactory {

  private static final String HOST = "localhost";
  private static final int PORT = 9080;

  private static final String REPOSITORY_ID = "test";
  private static final String USER = "test";
  private static final String PASSWORD = "test";

  private static final String ATOMPUB_PATH = "/opencmis/atom";
  private static final String WEBSERVICES_PATH = "/opencmis/services/";

  /**
   * Returns the repository id of the test repository.
   */
  public static String getRepositoryId() {
    return REPOSITORY_ID;
  }

  /**
   * Returns the user that is logged in.
   */
  public static String getUsername() {
    return USER;
  }

  /**
   * Creates a new Session object that uses the AtomPub binding.
   */
  public static Session createAtomPubSession() {
    String url = "http://" + HOST + ":" + PORT + ATOMPUB_PATH;

    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put(SessionParameter.USER, USER);
    parameters.put(SessionParameter.PASSWORD, PASSWORD);
    parameters.put(SessionParameter.ATOMPUB_URL, url);

    // ToDo: creates session
    return null;
  }

  /**
   * Creates a new Session object that uses the Web Services binding.
   */
  public static Session createWebServicesSession() {
    String url = "http://" + HOST + ":" + PORT + WEBSERVICES_PATH;

    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put(SessionParameter.USER, USER);
    parameters.put(SessionParameter.PASSWORD, PASSWORD);
    parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, url + "RepositoryService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, url + "NavigationService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, url + "ObjectService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, url + "VersioningService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, url + "DiscoveryService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, url
        + "RelationshipService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, url
        + "MultiFilingService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, url + "PolicyService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, url + "ACLService?wsdl");

    // ToDo: create session
    return null;
  }

  /**
   * Creates a new CmisProvider object that uses the AtomPub binding. For low-level tests only!
   */
  public static CmisProvider createAtomPubProvider() {
    String url = "http://" + HOST + ":" + PORT + ATOMPUB_PATH;

    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put(SessionParameter.USER, USER);
    parameters.put(SessionParameter.PASSWORD, PASSWORD);
    parameters.put(SessionParameter.ATOMPUB_URL, url);

    return CmisProviderFactory.newInstance().createCmisAtomPubProvider(parameters);
  }

  /**
   * Creates a new CmisProvider object that uses the Web Services binding. For low-level tests only!
   */
  public static CmisProvider createWebServicesProvider() {
    String url = "http://" + HOST + ":" + PORT + WEBSERVICES_PATH;

    Map<String, String> parameters = new HashMap<String, String>();

    parameters.put(SessionParameter.USER, USER);
    parameters.put(SessionParameter.PASSWORD, PASSWORD);
    parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, url + "RepositoryService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, url + "NavigationService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, url + "ObjectService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, url + "VersioningService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, url + "DiscoveryService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, url
        + "RelationshipService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, url
        + "MultiFilingService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, url + "PolicyService?wsdl");
    parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, url + "ACLService?wsdl");

    return CmisProviderFactory.newInstance().createCmisWebServicesProvider(parameters);
  }
}