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
package org.apache.chemistry.opencmis.fit.runtime.extensions;

import java.util.List;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.enums.ExtensionLevel;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class AbstractExtensionTestIT {

    private static Session fSession;

    /**
     * Returns the current Session object.
     */
    protected Session getSession() {
        return fSession;
    }

    /**
     * Returns a new Session object.
     */
    protected abstract Session createSession();

    @BeforeClass
    public static void setUpClass() {
        fSession = null;
    }

    @Before
    public void setUp() {
        if (fSession == null) {
            fSession = createSession();
        }
    }

    /**
     * Simple extension test.
     */
    @Test
    public void testExtensions() {
        Folder rootFolder = getSession().getRootFolder();

        // only test getting extensions without check
        // (the InMemory repository does not expose extensions yet)
        List<CmisExtensionElement> extensions = null;
        extensions = rootFolder.getExtensions(ExtensionLevel.OBJECT);
        extensions = rootFolder.getExtensions(ExtensionLevel.PROPERTIES);
        extensions = rootFolder.getExtensions(ExtensionLevel.ACL);
    }
}
