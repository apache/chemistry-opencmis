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
import static org.apache.chemistry.opencmis.commons.impl.MimeHelper.getBoundaryFromMultiPart;
import static org.apache.chemistry.opencmis.commons.impl.MimeHelper.getChallengesFromAuthenticateHeader;
import static org.apache.chemistry.opencmis.commons.impl.MimeHelper.getCharsetFromContentType;
import static org.junit.Assert.assertArrayEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.junit.Test;

public class MimeHelperTest extends TestCase {

    @Test
    public void testEncodeContentDisposition() {
        assertEquals("inline; filename=foo.bar", encodeContentDisposition("inline", "foo.bar"));
        assertEquals("attachment; filename=foo.bar", encodeContentDisposition(null, "foo.bar"));
        assertEquals("attachment; filename*=UTF-8''caf%C3%A9.pdf", encodeContentDisposition(null, "caf\u00e9.pdf"));
        assertEquals("attachment; filename*=UTF-8''%20%27%2A%25%20abc%20%C2%81%C2%82%0D%0A%09",
                encodeContentDisposition(null, " '*% abc \u0081\u0082\r\n\t"));
    }

    @Test
    public void testDecodeContentDisposition() {
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String> expected = new HashMap<String, String>();

        assertEquals("attachment", decodeContentDisposition("attachment; a=b; c=d", params));
        expected.put("a", "b");
        expected.put("c", "d");
        assertEquals(expected, params);
        params.clear();
        expected.clear();

        assertEquals("inline", decodeContentDisposition("  inline ; a = \"b b\" (this is a comment) ; c =d;", params));
        expected.put("a", "b b");
        expected.put("c", "d");
        assertEquals(expected, params);
        params.clear();
        expected.clear();

        assertEquals("inline",
                decodeContentDisposition("inline; modification-date=\"Wed, 12 Feb 1997 16:29:51 -0500\"", params));
        assertEquals(Collections.singletonMap("modification-date", "Wed, 12 Feb 1997 16:29:51 -0500"), params);
        params.clear();
    }

    @Test
    public void testDecodeContentDispositionFilename() {
        assertNull(decodeContentDispositionFilename("attachment; a=b; c=d;"));
        assertNull(decodeContentDispositionFilename("inline"));
        assertNull(decodeContentDispositionFilename("inline; modification-date=\"Wed, 12 Feb 1997 16:29:51 -0500\""));
        assertEquals("foo.bar", decodeContentDispositionFilename("attachment; filename=foo.bar"));
        assertEquals("foo.bar", decodeContentDispositionFilename("attachment; filename = \"foo.bar\""));
        assertEquals("foo.bar",
                decodeContentDispositionFilename(" guess ; filename = (this is rfc822 a comment) \"foo.bar\""));
        assertEquals("caf\u00e9.pdf", decodeContentDispositionFilename("foo; filename*=UTF-8''caf%C3%A9.pdf"));
        assertEquals("caf\u00e9.pdf", decodeContentDispositionFilename("bar; filename*=ISO-8859-1''caf%E9.pdf"));
        assertEquals("flask-docs.pdf",
                decodeContentDispositionFilename("attachment; filename*=UTF-8''%66%6c%61%73%6b%2d%64%6f%63%73.pdf;"
                        + " size=893099; creation-date=Mon, 12 Aug 2013 22:02:54 -0700;"
                        + " modification-date=Mon, 12 Aug 2013 22:02:55 -0700;"));
    }

    @Test
    public void testCharsetFromContentType() {
        assertEquals("utf-8", getCharsetFromContentType("text/plain;charset=utf-8"));
        assertEquals("utf-8", getCharsetFromContentType("text/plain;charset=\"utf-8\""));
        assertEquals("utf-8", getCharsetFromContentType("text/plain  ;  charset    =    \"utf-8\"   "));
    }

    @Test
    public void testBoundaryFromMultiPart() throws Exception {
        byte boundary[] = "thisisaBoundary".getBytes(IOUtils.ISO_8859_1);

        assertNull(getBoundaryFromMultiPart("multipart/form-data"));
        assertArrayEquals(boundary, getBoundaryFromMultiPart("multipart/form-data;boundary="
                + new String(boundary, IOUtils.ISO_8859_1)));
    }

    @Test
    public void testAuthenticateHeaderParameters() {
        Map<String, Map<String, String>> challenges = null;

        challenges = getChallengesFromAuthenticateHeader(null);
        assertNull(challenges);

        challenges = getChallengesFromAuthenticateHeader("");
        assertNull(challenges);

        challenges = getChallengesFromAuthenticateHeader("Basic");
        assertNotNull(challenges);
        assertTrue(challenges.containsKey("basic"));
        assertTrue(challenges.get("basic").isEmpty());

        challenges = getChallengesFromAuthenticateHeader("Basic realm=\"example\"");
        assertNotNull(challenges);
        assertTrue(challenges.containsKey("basic"));
        assertEquals("example", challenges.get("basic").get("realm"));

        challenges = getChallengesFromAuthenticateHeader("Basic realm= \"example\" ");
        assertNotNull(challenges);
        assertTrue(challenges.containsKey("basic"));
        assertEquals("example", challenges.get("basic").get("realm"));

        challenges = getChallengesFromAuthenticateHeader("Basic realm=example");
        assertNotNull(challenges);
        assertTrue(challenges.containsKey("basic"));
        assertEquals("example", challenges.get("basic").get("realm"));

        challenges = getChallengesFromAuthenticateHeader("Basic realm=\"example\",charset=\"UTF-8\"");
        assertNotNull(challenges);
        assertTrue(challenges.containsKey("basic"));
        assertEquals("example", challenges.get("basic").get("realm"));
        assertEquals("UTF-8", challenges.get("basic").get("charset"));

        challenges = getChallengesFromAuthenticateHeader("Bearer realm=\"example\", error=\"invalid_token\"");
        assertNotNull(challenges);
        assertTrue(challenges.containsKey("bearer"));
        assertEquals("example", challenges.get("bearer").get("realm"));
        assertEquals("invalid_token", challenges.get("bearer").get("error"));

        challenges = getChallengesFromAuthenticateHeader("Bearer realm=\"example\", error=invalid_token");
        assertNotNull(challenges);
        assertEquals("example", challenges.get("bearer").get("realm"));
        assertEquals("invalid_token", challenges.get("bearer").get("error"));

        challenges = getChallengesFromAuthenticateHeader("Bearer realm=\"example\", error=\"invalid_token\", error_description=\"The access token expired\"");
        assertNotNull(challenges);
        assertEquals("example", challenges.get("bearer").get("realm"));
        assertEquals("invalid_token", challenges.get("bearer").get("error"));
        assertEquals("The access token expired", challenges.get("bearer").get("error_description"));

        challenges = getChallengesFromAuthenticateHeader("Bearer realm=\"example\",error_description=\"It's expires, really!\", error=\"invalid_token\",");
        assertNotNull(challenges);
        assertEquals("example", challenges.get("bearer").get("realm"));
        assertEquals("invalid_token", challenges.get("bearer").get("error"));
        assertEquals("It's expires, really!", challenges.get("bearer").get("error_description"));

        challenges = getChallengesFromAuthenticateHeader("Newauth realm=\"apps\", type=1, title=\"Login to \\\"apps\\\"\", Basic realm=\"simple\"");
        assertNotNull(challenges);
        assertEquals("apps", challenges.get("newauth").get("realm"));
        assertEquals("1", challenges.get("newauth").get("type"));
        assertEquals("Login to \"apps\"", challenges.get("newauth").get("title"));
        assertEquals("simple", challenges.get("basic").get("realm"));

        challenges = getChallengesFromAuthenticateHeader("a1, a2 ,a3 ,a4");
        assertNotNull(challenges);
        assertTrue(challenges.containsKey("a1"));
        assertTrue(challenges.containsKey("a2"));
        assertTrue(challenges.containsKey("a3"));
        assertTrue(challenges.containsKey("a4"));
    }
}
