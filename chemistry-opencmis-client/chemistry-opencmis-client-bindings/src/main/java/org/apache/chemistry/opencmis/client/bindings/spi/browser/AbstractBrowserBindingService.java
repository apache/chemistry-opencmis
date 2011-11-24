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
package org.apache.chemistry.opencmis.client.bindings.spi.browser;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.LinkAccess;

/**
 * Base class for all Browser Binding client services.
 */
public abstract class AbstractBrowserBindingService implements LinkAccess {

    private BindingSession session;

    /**
     * Sets the current session.
     */
    protected void setSession(BindingSession session) {
        this.session = session;
    }

    /**
     * Gets the current session.
     */
    protected BindingSession getSession() {
        return session;
    }

    // ---- LinkAccess interface ----

    public String loadLink(String repositoryId, String objectId, String rel, String type) {
        // AtomPub specific -> return null
        return null;
    }

    public String loadContentLink(String repositoryId, String documentId) {
        // TODO Auto-generated method stub
        return null;
    }
}
