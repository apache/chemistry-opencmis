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
package org.apache.chemistry.opencmis.client.bindings.impl;

import java.lang.reflect.Constructor;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.CmisSpi;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.spi.AuthenticationProvider;

/**
 * A collection of static methods that are used in multiple places within the
 * bindings implementation.
 */
public final class CmisBindingsHelper {

    public static final String REPOSITORY_INFO_CACHE = "org.apache.chemistry.opencmis.binding.repositoryInfoCache";
    public static final String TYPE_DEFINTION_CACHE = "org.apache.chemistry.opencmis.binding.typeDefintionCache";
    public static final String SPI_OBJECT = "org.apache.chemistry.opencmis.binding.spi.object";
    public static final String AUTHENTICATION_PROVIDER_OBJECT = "org.apache.chemistry.opencmis.binding.auth.object";
    public static final String ACCEPT_LANGUAGE = "org.apache.chemistry.opencmis.binding.acceptLanguage";

    /**
     * Private constructor.
     */
    private CmisBindingsHelper() {
    }

    /**
     * Gets the SPI object for the given session. If there is already a SPI
     * object in the session it will be returned. If there is no SPI object it
     * will be created and put into the session.
     * 
     * @param session
     *            the session object
     * 
     * @return the SPI object
     */
    public static CmisSpi getSPI(BindingSession session) {
        // fetch from session
        CmisSpi spi = (CmisSpi) session.get(SPI_OBJECT);
        if (spi != null) {
            return spi;
        }

        session.writeLock();
        try {
            // try again
            spi = (CmisSpi) session.get(SPI_OBJECT);
            if (spi != null) {
                return spi;
            }

            // ok, we have to create it...
            try {
                String spiName = (String) session.get(SessionParameter.BINDING_SPI_CLASS);
                Constructor<?> c = Class.forName(spiName).getConstructor(BindingSession.class);
                spi = (CmisSpi) c.newInstance(session);
            } catch (CmisBaseException e) {
                throw e;
            } catch (Exception e) {
                throw new CmisRuntimeException("SPI cannot be initialized: " + e.getMessage(), e);
            }

            // we have a SPI object -> put it into the session
            session.put(SPI_OBJECT, spi, true);
        } finally {
            session.writeUnlock();
        }

        return spi;
    }

    /**
     * Returns the authentication provider from the session or <code>null</code>
     * if no authentication provider is set.
     */
    public static AuthenticationProvider getAuthenticationProvider(BindingSession session) {
        return (AuthenticationProvider) session.get(AUTHENTICATION_PROVIDER_OBJECT);
    }

    /**
     * Returns the repository info cache from the session.
     */
    public static RepositoryInfoCache getRepositoryInfoCache(BindingSession session) {
        return (RepositoryInfoCache) session.get(REPOSITORY_INFO_CACHE);
    }

    /**
     * Returns the type definition cache from the session.
     */
    public static TypeDefinitionCache getTypeDefinitionCache(BindingSession session) {
        return (TypeDefinitionCache) session.get(TYPE_DEFINTION_CACHE);
    }
}
