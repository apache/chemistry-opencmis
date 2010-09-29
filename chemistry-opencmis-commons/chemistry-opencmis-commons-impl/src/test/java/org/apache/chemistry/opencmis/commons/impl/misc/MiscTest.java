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
 *     Florian Mueller
 *     Florent Guillaume, Nuxeo
 */
package org.apache.chemistry.opencmis.commons.impl.misc;

import junit.framework.TestCase;

import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;

/**
 * Tests miscellaneous details.
 */
public class MiscTest extends TestCase {

    public void testUrlBuilder() {
        assertEquals("http://host/test", (new UrlBuilder("http://host/test")).toString());
        assertEquals("http://host/test?query=value", (new UrlBuilder("http://host/test?query=value")).toString());
        assertEquals("http://host/test", (new UrlBuilder("http://host/test?")).toString());
    }

    public void testUrlBuilderAddParameter() {
        UrlBuilder urlBuilder;

        urlBuilder = new UrlBuilder("http://host/test");
        urlBuilder.addParameter("query", "value");
        assertEquals("http://host/test?query=value", urlBuilder.toString());

        urlBuilder = new UrlBuilder("http://host/test?foo=bar");
        urlBuilder.addParameter("query", "value");
        assertEquals("http://host/test?foo=bar&query=value", urlBuilder.toString());

        // special chars, space turns into plus
        urlBuilder = new UrlBuilder("http://host/test");
        urlBuilder.addParameter("query", "caf\u00e9 cr\u00e8me");
        assertEquals("http://host/test?query=caf%C3%A9+cr%C3%A8me", urlBuilder.toString());
    }

    public void testUrlBuilderAddPath() {
        UrlBuilder urlBuilder;

        urlBuilder = new UrlBuilder("http://host/test");
        urlBuilder.addParameter("query", "value");
        assertEquals("http://host/test?query=value", urlBuilder.toString());

        urlBuilder = new UrlBuilder("http://host/test");
        urlBuilder.addPath("path");
        assertEquals("http://host/test/path", urlBuilder.toString());

        urlBuilder = new UrlBuilder("http://host/test/");
        urlBuilder.addPath("path");
        assertEquals("http://host/test/path", urlBuilder.toString());

        urlBuilder = new UrlBuilder("http://host/test");
        urlBuilder.addPath("/path");
        assertEquals("http://host/test/path", urlBuilder.toString());

        urlBuilder = new UrlBuilder("http://host/test/");
        urlBuilder.addPath("/path");
        assertEquals("http://host/test/path", urlBuilder.toString());

        // multi-segment path with special chars, space turns into %20
        urlBuilder = new UrlBuilder("http://host/test/");
        urlBuilder.addPath("path/caf\u00e9 d@d");
        assertEquals("http://host/test/path/caf%C3%A9%20d%40d", urlBuilder.toString());
    }

    public void testUrlBuilderAddPathSegment() {
        UrlBuilder urlBuilder;

        urlBuilder = new UrlBuilder("http://host/test");
        urlBuilder.addParameter("query", "value");
        assertEquals("http://host/test?query=value", urlBuilder.toString());

        urlBuilder = new UrlBuilder("http://host/test");
        urlBuilder.addPathSegment("path");
        assertEquals("http://host/test/path", urlBuilder.toString());

        urlBuilder = new UrlBuilder("http://host/test/");
        urlBuilder.addPathSegment("path");
        assertEquals("http://host/test/path", urlBuilder.toString());

        urlBuilder = new UrlBuilder("http://host/test");
        urlBuilder.addPathSegment("/path");
        assertEquals("http://host/test/path", urlBuilder.toString());

        urlBuilder = new UrlBuilder("http://host/test/");
        urlBuilder.addPathSegment("/path");
        assertEquals("http://host/test/path", urlBuilder.toString());

        // with special chars and slash, space turns into %20
        urlBuilder = new UrlBuilder("http://host/test/");
        urlBuilder.addPathSegment("path/caf\u00e9 d@d");
        assertEquals("http://host/test/path%2Fcaf%C3%A9%20d%40d", urlBuilder.toString());
    }

}
