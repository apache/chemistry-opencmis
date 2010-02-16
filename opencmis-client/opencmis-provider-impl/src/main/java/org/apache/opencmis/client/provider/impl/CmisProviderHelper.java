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
package org.apache.opencmis.client.provider.impl;

import org.apache.opencmis.client.provider.spi.AbstractAuthenticationProvider;
import org.apache.opencmis.client.provider.spi.CmisSpi;
import org.apache.opencmis.client.provider.spi.CmisSpiFactory;
import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.exceptions.CmisBaseException;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;

/**
 * A collection of static methods that are used in multiple places within the provider
 * implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public final class CmisProviderHelper {

  public static final String REPOSITORY_INFO_CACHE = "org.apache.opencmis.provider.repositoryInfoCache";
  public static final String TYPE_DEFINTION_CACHE = "org.apache.opencmis.provider.typeDefintionCache";
  public static final String SPI_OBJECT = "org.apache.opencmis.provider.spi.object";
  public static final String AUTHENTICATION_PROVIDER_OBJECT = "org.apache.opencmis.provider.auth.object";

  /**
   * Private constructor.
   */
  private CmisProviderHelper() {
  }

  /**
   * Gets the SPI object for the given session. If there is already a SPI object in the session it
   * will be returned. If there is no SPI object it will be created and put into the session.
   * 
   * @param session
   *          the session object
   * 
   * @return the SPI object
   */
  public static CmisSpi getSPI(Session session) {
    // fetch from session
    CmisSpi spi = (CmisSpi) session.get(SPI_OBJECT);
    if (spi != null) {
      return spi;
    }

    // ok, we have to create it...
    try {
      String spiFactoryName = (String) session.get(SessionParameter.BINDING_SPI_CLASS);
      Class<?> spiFactoryClass = Class.forName(spiFactoryName);
      Object spiFactory = spiFactoryClass.newInstance();

      if (!(spiFactory instanceof CmisSpiFactory)) {
        throw new CmisRuntimeException("Not a CMISSPIFactory class!");
      }

      spi = ((CmisSpiFactory) spiFactory).getSpiInstance(session);
      if (spi == null) {
        throw new CmisRuntimeException("SPI factory returned null!");
      }
    }
    catch (CmisBaseException e) {
      throw e;
    }
    catch (Exception e) {
      throw new CmisRuntimeException("SPI cannot be initialized: " + e.getMessage(), e);
    }

    // we have a SPI object -> put it into the session
    session.put(SPI_OBJECT, spi, true);

    return spi;
  }

  /**
   * Returns the authentication provider from the session or <code>null</code> if no authentication
   * provider is set.
   */
  public static AbstractAuthenticationProvider getAuthenticationProvider(Session session) {
    return (AbstractAuthenticationProvider) session.get(AUTHENTICATION_PROVIDER_OBJECT);
  }

  /**
   * Returns the repository info cache from the session.
   */
  public static RepositoryInfoCache getRepositoryInfoCache(Session session) {
    return (RepositoryInfoCache) session.get(REPOSITORY_INFO_CACHE);
  }

  /**
   * Returns the type definition cache from the session.
   */
  public static TypeDefinitionCache getTypeDefinitionCache(Session session) {
    return (TypeDefinitionCache) session.get(TYPE_DEFINTION_CACHE);
  }
}
