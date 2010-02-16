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
package org.apache.opencmis.client.provider.webservices;

import java.util.HashMap;
import java.util.Map;

import org.apache.opencmis.client.provider.factory.CmisProviderFactory;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.provider.CmisProvider;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class WebServicesTestProviderFactory {

  public static CmisProvider createProvider(String url, String username, String password) {
    boolean isPrefix = true;
    String urlLower = url.toLowerCase();

    if (urlLower.endsWith("?wsdl")) {
      isPrefix = false;
    }
    else if (urlLower.endsWith(".wsdl")) {
      isPrefix = false;
    }
    else if (urlLower.endsWith(".xml")) {
      isPrefix = false;
    }

    return createProvider(url, isPrefix, username, password);
  }

  public static CmisProvider createProvider(String url, boolean isPrefix, String username,
      String password) {
    // gather parameters
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(SessionParameter.USER, username);
    parameters.put(SessionParameter.PASSWORD, password);
    
    if (!isPrefix) {
      parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, url);
    }
    else {
      parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, url
          + "RepositoryService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, url
          + "NavigationService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, url + "ObjectService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, url
          + "VersioningService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, url + "DiscoveryService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, url
          + "RelationshipService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, url
          + "MultiFilingService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, url + "PolicyService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, url + "ACLService?wsdl");
    }

    // get factory and create provider
    CmisProviderFactory factory = CmisProviderFactory.newInstance();
    CmisProvider provider = factory.createCmisWebServicesProvider(parameters);

    return provider;
  }
}
