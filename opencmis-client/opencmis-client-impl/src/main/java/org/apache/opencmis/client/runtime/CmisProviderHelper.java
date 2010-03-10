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
package org.apache.opencmis.client.runtime;

import java.util.Map;

import org.apache.opencmis.client.provider.factory.CmisProviderFactory;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.enums.BindingType;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.CmisProvider;

/**
 * Helper methods for provider handling.
 */
public class CmisProviderHelper {
  /**
   * Creates a {@link CmisProvider} object.
   */
  public static CmisProvider createProvider(Map<String, String> parameters) {
    if (parameters == null || parameters.isEmpty()) {
      throw new CmisRuntimeException("Session parameter not set!");
    }

    if (!parameters.containsKey(SessionParameter.BINDING_TYPE)) {
      parameters.put(SessionParameter.BINDING_TYPE, BindingType.CUSTOM.value());
    }

    BindingType bt = BindingType.fromValue(parameters.get(SessionParameter.BINDING_TYPE));

    switch (bt) {
    case ATOMPUB:
      return createAtomPubProvider(parameters);
    case WEBSERVICES:
      return createWebServiceProvider(parameters);
    case CUSTOM:
      return createCustomProvider(parameters);
    default:
      throw new CmisRuntimeException("Ambiguous session parameter: " + parameters);
    }
  }

  /**
   * Creates a provider with custom parameters.
   */
  private static CmisProvider createCustomProvider(Map<String, String> parameters) {
    CmisProviderFactory factory = CmisProviderFactory.newInstance();
    CmisProvider provider = factory.createCmisProvider(parameters);

    return provider;
  }

  /**
   * Creates a Web Services provider.
   */
  private static CmisProvider createWebServiceProvider(Map<String, String> parameters) {
    CmisProviderFactory factory = CmisProviderFactory.newInstance();
    CmisProvider provider = factory.createCmisWebServicesProvider(parameters);

    return provider;
  }

  /**
   * Creates an AtomPub provider.
   */
  private static CmisProvider createAtomPubProvider(Map<String, String> parameters) {
    CmisProviderFactory factory = CmisProviderFactory.newInstance();
    CmisProvider provider = factory.createCmisAtomPubProvider(parameters);

    return provider;
  }
}
