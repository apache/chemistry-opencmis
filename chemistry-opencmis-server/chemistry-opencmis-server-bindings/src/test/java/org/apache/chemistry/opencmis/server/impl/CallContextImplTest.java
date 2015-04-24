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
package org.apache.chemistry.opencmis.server.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.junit.Test;

public class CallContextImplTest {

    @Test
    public void testLocal() {
        CallContextImpl context = new CallContextImpl(null, CmisVersion.CMIS_1_1, null, null, null, null, null, null);

        context.setAcceptLanguage(" en - us ");
        assertEquals("en", context.get(CallContext.LOCALE_ISO639_LANGUAGE));
        assertEquals("us", context.get(CallContext.LOCALE_ISO3166_COUNTRY));
        assertEquals("en-us", context.getLocale());

        context.setAcceptLanguage("en-us; q=0.8 , de-ch ; Q = 0.9, abc-123; q=0.");
        assertEquals("de", context.get(CallContext.LOCALE_ISO639_LANGUAGE));
        assertEquals("ch", context.get(CallContext.LOCALE_ISO3166_COUNTRY));
        assertEquals("de-ch", context.getLocale());

        context.setAcceptLanguage("en-us; q=0.8, abc-123; q=1.0, de-ch ;Q= 0.9");
        assertEquals("abc", context.get(CallContext.LOCALE_ISO639_LANGUAGE));
        assertEquals("123", context.get(CallContext.LOCALE_ISO3166_COUNTRY));
        assertEquals("abc-123", context.getLocale());

        context.setAcceptLanguage("en;q=0.1,*;q=0.8");
        assertNull(context.get(CallContext.LOCALE_ISO639_LANGUAGE));
        assertNull(context.get(CallContext.LOCALE_ISO3166_COUNTRY));
        assertNull(context.getLocale());

        context.setAcceptLanguage("fr");
        assertEquals("fr", context.get(CallContext.LOCALE_ISO639_LANGUAGE));
        assertNull(context.get(CallContext.LOCALE_ISO3166_COUNTRY));
        assertEquals("fr", context.getLocale());

        context.setAcceptLanguage("de-*");
        assertEquals("de", context.get(CallContext.LOCALE_ISO639_LANGUAGE));
        assertNull(context.get(CallContext.LOCALE_ISO3166_COUNTRY));
        assertEquals("de", context.getLocale());
    }

    @Test
    public void testRange() {
        CallContextImpl context = new CallContextImpl(null, CmisVersion.CMIS_1_1, null, null, null, null, null, null);

        context.setRange("bytes=100-299");
        assertEquals(100L, context.getOffset().longValue());
        assertEquals(200L, context.getLength().longValue());

        context.setRange(" bytes  = 1 - 2");
        assertEquals(1L, context.getOffset().longValue());
        assertEquals(2L, context.getLength().longValue());

        context.setRange("bytes=456-");
        assertEquals(456L, context.getOffset().longValue());
        assertNull(context.getLength());

        // not supported ranges
        context.setRange("bytes=10-20,30-40");
        assertNull(context.getOffset());
        assertNull(context.getLength());

        context.setRange("bytes=-123");
        assertNull(context.getOffset());
        assertNull(context.getLength());

        context.setRange("kb=100-299");
        assertNull(context.getOffset());
        assertNull(context.getLength());
    }
}
