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
package org.apache.chemistry.opencmis.client.bindings.factory;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.api.CmisBinding;

/**
 * Default factory for a CMIS binding instance.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public final class CmisBindingFactory {

  /** Default CMIS AtomPub binding SPI implementation */
  public static final String BINDING_SPI_ATOMPUB = "org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubSpi";
  /** Default CMIS Web Services binding SPI implementation */
  public static final String BINDING_SPI_WEBSERVICES = "org.apache.chemistry.opencmis.client.bindings.spi.webservices.CmisWebServicesSpi";
  /** CMIS In Memory binding SPI implementation (for testing only!) */
  public static final String BINDING_SPI_INMEMORY = "org.apache.chemistry.opencmis.inmemory.clientprovider.CmisInMemorySpiFactory";

  /** Standard authentication provider class */
  public static final String STANDARD_AUTHENTICATION_PROVIDER = "org.apache.chemistry.opencmis.client.bindings.spi.StandardAuthenticationProvider";

  private Map<String, String> fDefaults;

  /**
   * Private constructor -- it's a factory.
   */
  private CmisBindingFactory() {
    fDefaults = createNewDefaultParameters();
  }

  /**
   * Creates a new factory instance.
   */
  public static CmisBindingFactory newInstance() {
    return new CmisBindingFactory();
  }

  /**
   * Returns the default session parameters.
   */
  public Map<String, String> getDefaultSessionParameters() {
    return fDefaults;
  }

  /**
   * Sets the default session parameters.
   */
  public void setDefaultSessionParameters(Map<String, String> sessionParameters) {
    if (sessionParameters == null) {
      fDefaults = createNewDefaultParameters();
    }
    else {
      fDefaults = sessionParameters;
    }
  }

  /**
   * Creates a CMIS binding instance. A binding class has to be provided in the session parameters.
   */
  public CmisBinding createCmisBinding(Map<String, String> sessionParameters) {
    checkSessionParameters(sessionParameters, true);

    addDefaultParameters(sessionParameters);

    return new CmisBindingImpl(sessionParameters);
  }

  /**
   * Creates a default CMIS AtomPub binding instance.
   */
  public CmisBinding createCmisAtomPubBinding(Map<String, String> sessionParameters) {
    checkSessionParameters(sessionParameters, false);

    sessionParameters.put(SessionParameter.BINDING_SPI_CLASS, BINDING_SPI_ATOMPUB);
    if (!sessionParameters.containsKey(SessionParameter.AUTHENTICATION_PROVIDER_CLASS)) {
      sessionParameters.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS,
          STANDARD_AUTHENTICATION_PROVIDER);
    }
    sessionParameters.put(SessionParameter.AUTH_HTTP_BASIC, "true");
    sessionParameters.put(SessionParameter.AUTH_SOAP_USERNAMETOKEN, "false");
    addDefaultParameters(sessionParameters);

    check(sessionParameters, SessionParameter.ATOMPUB_URL);

    return new CmisBindingImpl(sessionParameters);
  }

  /**
   * Creates a default CMIS Web Services binding instance.
   */
  public CmisBinding createCmisWebServicesBinding(Map<String, String> sessionParameters) {
    checkSessionParameters(sessionParameters, false);

    sessionParameters.put(SessionParameter.BINDING_SPI_CLASS, BINDING_SPI_WEBSERVICES);
    if (!sessionParameters.containsKey(SessionParameter.AUTHENTICATION_PROVIDER_CLASS)) {
      sessionParameters.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS,
          STANDARD_AUTHENTICATION_PROVIDER);
    }
    sessionParameters.put(SessionParameter.AUTH_HTTP_BASIC, "true");
    sessionParameters.put(SessionParameter.AUTH_SOAP_USERNAMETOKEN, "true");
    addDefaultParameters(sessionParameters);

    check(sessionParameters, SessionParameter.WEBSERVICES_ACL_SERVICE);
    check(sessionParameters, SessionParameter.WEBSERVICES_DISCOVERY_SERVICE);
    check(sessionParameters, SessionParameter.WEBSERVICES_MULTIFILING_SERVICE);
    check(sessionParameters, SessionParameter.WEBSERVICES_NAVIGATION_SERVICE);
    check(sessionParameters, SessionParameter.WEBSERVICES_OBJECT_SERVICE);
    check(sessionParameters, SessionParameter.WEBSERVICES_POLICY_SERVICE);
    check(sessionParameters, SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE);
    check(sessionParameters, SessionParameter.WEBSERVICES_REPOSITORY_SERVICE);
    check(sessionParameters, SessionParameter.WEBSERVICES_VERSIONING_SERVICE);

    return new CmisBindingImpl(sessionParameters);
  }

  // ---- internal ----

  /**
   * Checks the passed session parameters.
   */
  private void checkSessionParameters(Map<String, String> sessionParameters, boolean mustContainSPI) {
    // don't accept null
    if (sessionParameters == null) {
      throw new IllegalArgumentException("Session parameter map not set!");
    }

    // check binding entry
    String SPIClass = sessionParameters.get(SessionParameter.BINDING_SPI_CLASS);
    if (mustContainSPI) {
      if ((SPIClass == null) || (SPIClass.trim().length() == 0)) {
        throw new IllegalArgumentException("SPI class entry (" + SessionParameter.BINDING_SPI_CLASS
            + ") is missing!");
      }
    }
  }

  /**
   * Checks if the given parameter is present. If not, throw an
   * <code>IllegalArgumentException</code>.
   */
  private void check(Map<String, String> sessionParameters, String parameter) {
    if (!sessionParameters.containsKey(parameter)) {
      throw new IllegalArgumentException("Parameter '" + parameter + "' is missing!");
    }
  }

  /**
   * Add the default session parameters to the given map without override existing entries.
   */
  private void addDefaultParameters(Map<String, String> sessionParameters) {
    for (String key : fDefaults.keySet()) {
      if (!sessionParameters.containsKey(key)) {
        sessionParameters.put(key, fDefaults.get(key));
      }
    }
  }

  /**
   * Creates a default session parameters map with some reasonable defaults.
   */
  private Map<String, String> createNewDefaultParameters() {
    Map<String, String> result = new HashMap<String, String>();

    result.put(SessionParameter.CACHE_SIZE_REPOSITORIES, "10");
    result.put(SessionParameter.CACHE_SIZE_TYPES, "100");
    result.put(SessionParameter.CACHE_SIZE_OBJECTS, "400");

    return result;
  }
}
