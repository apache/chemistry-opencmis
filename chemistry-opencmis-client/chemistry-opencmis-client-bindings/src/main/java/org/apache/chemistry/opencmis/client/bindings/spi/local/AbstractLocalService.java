/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.chemistry.opencmis.client.bindings.spi.local;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;

/**
 * Base class for all local clients.
 */
public abstract class AbstractLocalService {

    private BindingSession session;
    private CmisServiceFactory factory;

    private String user;
    private String password;

    /**
     * Sets the current session.
     */
    protected void setSession(BindingSession session) {
        this.session = session;

        Object userObj = session.get(SessionParameter.USER);
        user = userObj instanceof String ? userObj.toString() : null;

        Object passwordObj = session.get(SessionParameter.PASSWORD);
        password = passwordObj instanceof String ? passwordObj.toString() : null;
    }

    /**
     * Gets the current session.
     */
    protected BindingSession getSession() {
        return session;
    }

    /**
     * Sets the service factory.
     */
    protected void setServiceFactory(CmisServiceFactory factory) {
        this.factory = factory;
    }

    /**
     * Gets the service factory.
     */
    protected CmisServiceFactory getServiceFactory() {
        return factory;
    }

    /**
     * creates a local call context.
     */
    protected CallContext createCallContext(String repositoryId) {
        return new LocalCallContext(repositoryId, user, password);
    }

    protected CmisService getService(String repositoryId) {
        return factory.getService(createCallContext(repositoryId));
    }

    // ------------------------------------------------------------------

    /**
     * Simple {@link CallContext} implementation.
     */
    static class LocalCallContext implements CallContext {

        private final Map<String, Object> contextMap = new HashMap<String, Object>();

        public LocalCallContext(String repositoryId, String user, String password) {
            contextMap.put(REPOSITORY_ID, repositoryId);
            contextMap.put(USERNAME, user);
            contextMap.put(PASSWORD, password);
        }

        public String getBinding() {
            return BINDING_LOCAL;
        }

        public Object get(String key) {
            return contextMap.get(key);
        }

        public String getRepositoryId() {
            return (String) get(REPOSITORY_ID);
        }

        public String getUsername() {
            return (String) get(USERNAME);
        }

        public String getPassword() {
            return (String) get(PASSWORD);
        }

        public String getLocale() {
            return null;
        }

        public BigInteger getOffset() {
            return (BigInteger) get(OFFSET);
        }

        public BigInteger getLength() {
            return (BigInteger) get(LENGTH);
        }

        public boolean isObjectInfoRequired() {
            return false;
        }

        public File getTempDirectory() {
            return null;
        }

        public int getMemoryThreshold() {
            return 0;
        }

        public long getMaxContentSize() {
            return -1;
        }
    }
}
