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
 *
 * Contributors:
 *     Florent Guillaume
 */
package org.apache.chemistry.opencmis.commons.impl.misc;

import static org.apache.chemistry.opencmis.commons.impl.MimeHelper.decodeContentDisposition;
import static org.apache.chemistry.opencmis.commons.impl.MimeHelper.decodeContentDispositionFilename;
import static org.apache.chemistry.opencmis.commons.impl.MimeHelper.encodeContentDisposition;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class MimeHelperTest extends TestCase {

    public void testEncodeContentDisposition() {
        assertEquals("inline; filename=foo.bar",
                encodeContentDisposition("inline", "foo.bar"));
        assertEquals("attachment; filename=foo.bar",
                encodeContentDisposition(null, "foo.bar"));
        assertEquals("attachment; filename*=UTF-8''caf%C3%A9.pdf",
                encodeContentDisposition(null, "caf\u00e9.pdf"));
        assertEquals(
                "attachment; filename*=UTF-8''%20%27%2A%25%20abc%20%C2%81%C2%82%0D%0A%09",
                encodeContentDisposition(null, " '*% abc \u0081\u0082\r\n\t"));
    }

    public void testDecodeContentDisposition() {
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String> expected = new HashMap<String, String>();

        assertEquals("attachment",
                decodeContentDisposition("attachment; a=b; c=d", params));
        expected.put("a", "b");
        expected.put("c", "d");
        assertEquals(expected, params);
        params.clear();
        expected.clear();

        assertEquals(
                "inline",
                decodeContentDisposition(
                        "  inline ; a = \"b b\" (this is a comment) ; c =d;",
                        params));
        expected.put("a", "b b");
        expected.put("c", "d");
        assertEquals(expected, params);
        params.clear();
        expected.clear();

        assertEquals(
                "inline",
                decodeContentDisposition(
                        "inline; modification-date=\"Wed, 12 Feb 1997 16:29:51 -0500\"",
                        params));
        assertEquals(Collections.singletonMap("modification-date",
                "Wed, 12 Feb 1997 16:29:51 -0500"), params);
        params.clear();
    }

    public void testDecodeContentDispositionFilename() {
        assertNull(decodeContentDispositionFilename("attachment; a=b; c=d;"));
        assertNull(decodeContentDispositionFilename("inline"));
        assertNull(decodeContentDispositionFilename("inline; modification-date=\"Wed, 12 Feb 1997 16:29:51 -0500\""));
        assertEquals(
                "foo.bar",
                decodeContentDispositionFilename("attachment; filename=foo.bar"));
        assertEquals(
                "foo.bar",
                decodeContentDispositionFilename("attachment; filename = \"foo.bar\""));
        assertEquals(
                "foo.bar",
                decodeContentDispositionFilename(" guess ; filename = (this is rfc822 a comment) \"foo.bar\""));
        assertEquals(
                "caf\u00e9.pdf",
                decodeContentDispositionFilename("foo; filename*=UTF-8''caf%C3%A9.pdf"));
        assertEquals(
                "caf\u00e9.pdf",
                decodeContentDispositionFilename("bar; filename*=ISO-8859-1''caf%E9.pdf"));
    }

}
