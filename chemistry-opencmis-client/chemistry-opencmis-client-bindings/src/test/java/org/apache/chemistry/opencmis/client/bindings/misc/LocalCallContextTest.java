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
package org.apache.chemistry.opencmis.client.bindings.misc;

import junit.framework.TestCase;

import org.apache.chemistry.opencmis.client.bindings.spi.local.LocalCallContext;
import org.apache.chemistry.opencmis.commons.server.CallContext;

public class LocalCallContextTest extends TestCase {

    public void testLocalCallContextSimple() {
        LocalCallContext lcc = new LocalCallContext("repId", "user", "password");

        assertEquals(CallContext.BINDING_LOCAL, lcc.getBinding());
        assertEquals("repId", lcc.getRepositoryId());
        assertEquals("user", lcc.getUsername());
        assertEquals("password", lcc.getPassword());
        assertNull(lcc.get(CallContext.LOCALE_ISO639_LANGUAGE));
        assertNull(lcc.get(CallContext.LOCALE_ISO3166_COUNTRY));
        assertNull(lcc.getLocale());
    }

    public void testLocalCallContextLang() {
        LocalCallContext lcc = new LocalCallContext("repId", "user", "password", "de", null);

        assertEquals(CallContext.BINDING_LOCAL, lcc.getBinding());
        assertEquals("repId", lcc.getRepositoryId());
        assertEquals("user", lcc.getUsername());
        assertEquals("password", lcc.getPassword());
        assertEquals("de", lcc.get(CallContext.LOCALE_ISO639_LANGUAGE));
        assertNull(lcc.get(CallContext.LOCALE_ISO3166_COUNTRY));
        assertEquals("de", lcc.getLocale());
    }

    public void testLocalCallContextLangCountry() {
        LocalCallContext lcc = new LocalCallContext("repId", "user", "password", "de", "ch");

        assertEquals(CallContext.BINDING_LOCAL, lcc.getBinding());
        assertEquals("repId", lcc.getRepositoryId());
        assertEquals("user", lcc.getUsername());
        assertEquals("password", lcc.getPassword());
        assertEquals("de", lcc.get(CallContext.LOCALE_ISO639_LANGUAGE));
        assertEquals("ch", lcc.get(CallContext.LOCALE_ISO3166_COUNTRY));
        assertEquals("de-ch", lcc.getLocale());
    }
}
