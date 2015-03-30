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
package org.apache.chemistry.opencmis.commons.impl.misc;

import static org.junit.Assert.assertEquals;

import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.ReturnVersion;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.junit.Test;

/**
 * UrlBuilder tests.
 */
public class UrlBuilderTest {

    @Test
    public void testUrls() {
        assertEquals("http://example.com", new UrlBuilder("http://example.com").toString());

        // path
        assertEquals("http://example.com/path", (new UrlBuilder("http://example.com")).addPath("path").toString());
        assertEquals("http://example.com/path?param=value", (new UrlBuilder("http://example.com")).addPath("path")
                .addParameter("param", "value").toString());
        assertEquals("http://example.com/path?param=value",
                (new UrlBuilder("http://example.com")).addParameter("param", "value").addPath("path").toString());
        assertEquals("http://example.com/path", (new UrlBuilder("http://example.com")).addPath("/path").toString());
        assertEquals("http://example.com/path", (new UrlBuilder("http://example.com/")).addPath("path").toString());
        assertEquals("http://example.com/path", (new UrlBuilder("http://example.com/")).addPath("/path").toString());
        assertEquals("http://example.com/path1/path2",
                new UrlBuilder((new UrlBuilder("http://example.com")).addPath("path1").addPath("path2")).toString());
        assertEquals("http://example.com", new UrlBuilder("http://example.com").addPath("").toString());
        assertEquals("http://example.com", new UrlBuilder("http://example.com").addPath(null).toString());

        // path segments
        assertEquals("http://example.com/path1/path2", (new UrlBuilder("http://example.com")).addPath("path1/path2")
                .toString());
        assertEquals("http://example.com/path1%2Fpath2",
                (new UrlBuilder("http://example.com")).addPathSegment("path1/path2").toString());

        // parameters
        assertEquals("http://example.com?param", (new UrlBuilder("http://example.com")).addParameter("param")
                .toString());
        assertEquals("http://example.com?param=value",
                (new UrlBuilder("http://example.com")).addParameter("param", "value").toString());
        assertEquals("http://example.com?x=y&param=value",
                (new UrlBuilder("http://example.com?x=y")).addParameter("param", "value").toString());
        assertEquals(
                "http://example.com?param=both",
                (new UrlBuilder("http://example.com")).addParameter("param",
                        UrlBuilder.normalizeParameter(IncludeRelationships.BOTH)).toString());
        assertEquals(
                "http://example.com?param=latest",
                (new UrlBuilder("http://example.com")).addParameter("param",
                        UrlBuilder.normalizeParameter(ReturnVersion.LATEST)).toString());
        assertEquals("http://example.com", (new UrlBuilder("http://example.com")).addParameter(null).toString());
        assertEquals("http://example.com", (new UrlBuilder("http://example.com")).addParameter(null, "value")
                .toString());
        assertEquals("http://example.com", (new UrlBuilder("http://example.com")).addParameter("param", null)
                .toString());
        assertEquals("http://example.com?param=&param2=value2",
                (new UrlBuilder("http://example.com")).addParameter("param", "").addParameter("param2", "value2")
                        .toString());
        assertEquals("http://example.com/path?param=value", (new UrlBuilder("http://example.com")).addPath("path")
                .addParameter("param", "value").toString());

        // other constructor
        assertEquals("http://example.com/path?param=value", (new UrlBuilder("http", "example.com", 80, "path"))
                .addParameter("param", "value").toString());
        assertEquals("https://example.com/path?param=value", (new UrlBuilder("https", "example.com", 443, "path"))
                .addParameter("param", "value").toString());
        assertEquals("http://example.com:1234/path?param=value", (new UrlBuilder("http", "example.com", 1234, "path"))
                .addParameter("param", "value").toString());
    }

    @Test
    public void testInvalid() {
        try {
            new UrlBuilder((String) null);
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            new UrlBuilder((UrlBuilder) null);
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
