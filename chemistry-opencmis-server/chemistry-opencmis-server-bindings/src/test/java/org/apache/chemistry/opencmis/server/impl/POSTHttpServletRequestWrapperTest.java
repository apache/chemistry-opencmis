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
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.server.impl.browser.POSTHttpServletRequestWrapper;
import org.junit.Test;

public class POSTHttpServletRequestWrapperTest {

    private static final String[] CHARSETS = new String[] { "utf-8", "utf-16", "ISO-8859-1", "Cp1252", "UTF-16BE",
            "UTF-16LE" };

    @Test
    public void testSimple() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        addNameValue(out, "name1", "value1", IOUtils.UTF8);
        out.write('&');
        addNameValue(out, "name2", "value2", IOUtils.UTF8);

        HttpServletRequest request = HttpRequestMockHelper.createFormRequest(null, out.toByteArray());
        POSTHttpServletRequestWrapper postWrapper = new POSTHttpServletRequestWrapper(request, null);

        assertParameter(postWrapper, "name1", "value1");
        assertParameter(postWrapper, "name2", "value2");
    }

    @Test
    public void testEmpty() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpServletRequest request = HttpRequestMockHelper.createFormRequest(null, out.toByteArray());
        POSTHttpServletRequestWrapper postWrapper = new POSTHttpServletRequestWrapper(request, null);

        assertEquals(0, postWrapper.getParameterMap().size());
    }

    @Test
    public void testShort() throws IOException {
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            addNameValue(out, "n", "v", IOUtils.UTF8);

            HttpServletRequest request = HttpRequestMockHelper.createFormRequest(null, out.toByteArray());
            POSTHttpServletRequestWrapper postWrapper = new POSTHttpServletRequestWrapper(request, null);

            assertParameter(postWrapper, "n", "v");
        }
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            addNameValue(out, "a", "1", IOUtils.UTF8);
            out.write('&');
            addNameValue(out, "b", "2", IOUtils.UTF8);

            HttpServletRequest request = HttpRequestMockHelper.createFormRequest(null, out.toByteArray());
            POSTHttpServletRequestWrapper postWrapper = new POSTHttpServletRequestWrapper(request, null);

            assertParameter(postWrapper, "a", "1");
            assertParameter(postWrapper, "b", "2");
        }
    }

    @Test
    public void testMany() throws IOException {
        int count = 100000;

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 0; i < count; i++) {
            if (i > 0) {
                out.write('&');
            }
            addNameValue(out, "name" + i, "välju" + i, IOUtils.UTF8);
        }

        HttpServletRequest request = HttpRequestMockHelper.createFormRequest(IOUtils.UTF8, out.toByteArray());
        POSTHttpServletRequestWrapper postWrapper = new POSTHttpServletRequestWrapper(request, null);

        assertEquals(count, postWrapper.getParameterMap().size());

        for (int i = 0; i < count; i++) {
            assertParameter(postWrapper, "name" + i, "välju" + i);
        }
    }

    @Test
    public void testMultipleValues() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        addNameValue(out, "name1", "value1a", IOUtils.UTF8);
        out.write('&');
        addNameValue(out, "name1", "value1b", IOUtils.UTF8);
        out.write('&');
        addNameValue(out, "name1", "value1c", IOUtils.UTF8);
        out.write('&');
        addNameValue(out, "name2", "value2", IOUtils.UTF8);

        HttpServletRequest request = HttpRequestMockHelper.createFormRequest(null, out.toByteArray());
        POSTHttpServletRequestWrapper postWrapper = new POSTHttpServletRequestWrapper(request, null);

        assertParameter(postWrapper, "name1", "value1a", "value1b", "value1c");
        assertParameter(postWrapper, "name2", "value2");
    }

    @Test
    public void testNull() throws IOException {
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            addNameValue(out, "name1", "value1", IOUtils.UTF8);
            out.write('&');
            addNameValue(out, null, "noame", IOUtils.UTF8);
            out.write('&');
            addNameValue(out, "novalue", null, IOUtils.UTF8);
            out.write('&');
            addNameValue(out, "name2", "value2", IOUtils.UTF8);

            HttpServletRequest request = HttpRequestMockHelper.createFormRequest(null, out.toByteArray());
            POSTHttpServletRequestWrapper postWrapper = new POSTHttpServletRequestWrapper(request, null);

            assertParameter(postWrapper, "name1", "value1");
            assertParameter(postWrapper, "name2", "value2");
            assertParameter(postWrapper, "novalue", (String[]) null);
            assertEquals(3, postWrapper.getParameterMap().size());
        }
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            addNameValue(out, null, "noame", IOUtils.UTF8);
            out.write('&');
            addNameValue(out, "novalue", null, IOUtils.UTF8);
            out.write('&');
            addNameValue(out, "name2", "value2", IOUtils.UTF8);

            HttpServletRequest request = HttpRequestMockHelper.createFormRequest(null, out.toByteArray());
            POSTHttpServletRequestWrapper postWrapper = new POSTHttpServletRequestWrapper(request, null);

            assertParameter(postWrapper, "name2", "value2");
            assertParameter(postWrapper, "novalue", (String[]) null);
            assertEquals(2, postWrapper.getParameterMap().size());
        }
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            addNameValue(out, "name1", "value1", IOUtils.UTF8);
            out.write('&');
            addNameValue(out, null, "noame", IOUtils.UTF8);
            out.write('&');
            addNameValue(out, "novalue", null, IOUtils.UTF8);

            HttpServletRequest request = HttpRequestMockHelper.createFormRequest(null, out.toByteArray());
            POSTHttpServletRequestWrapper postWrapper = new POSTHttpServletRequestWrapper(request, null);

            assertParameter(postWrapper, "name1", "value1");
            assertParameter(postWrapper, "novalue", (String[]) null);
            assertEquals(2, postWrapper.getParameterMap().size());
        }
    }

    @Test
    public void testUrlEncoded() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        addNameValue(out, "n/a&m%e1", "v§a*l  #u+e1", IOUtils.UTF8);
        out.write('&');
        addNameValue(out, "n/ä&m%e2", "v§a*l#u+e2", IOUtils.UTF8);

        HttpServletRequest request = HttpRequestMockHelper.createFormRequest(null, out.toByteArray());
        POSTHttpServletRequestWrapper postWrapper = new POSTHttpServletRequestWrapper(request, null);

        assertParameter(postWrapper, "n/a&m%e1", "v§a*l  #u+e1");
        assertParameter(postWrapper, "n/ä&m%e2", "v§a*l#u+e2");
    }

    @Test
    public void testCharsetField() throws IOException {
        for (String charset : CHARSETS) {
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                addNameValue(out, "n/a&m%e1", "v§a*l  #u+e1", charset);
                out.write('&');
                addNameValue(out, "_charset_", charset, IOUtils.ISO_8859_1);
                out.write('&');
                addNameValue(out, "n/ä&m%e2", "v§a*l#u+e2", charset);

                HttpServletRequest request = HttpRequestMockHelper.createFormRequest(null, out.toByteArray());
                POSTHttpServletRequestWrapper postWrapper = new POSTHttpServletRequestWrapper(request, null);

                assertParameter(postWrapper, "n/a&m%e1", "v§a*l  #u+e1");
                assertParameter(postWrapper, "n/ä&m%e2", "v§a*l#u+e2");
            }
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                addNameValue(out, "_charset_", charset, IOUtils.ISO_8859_1);
                out.write('&');
                addNameValue(out, "n/a&m%e1", "v§a*l  #u+e1", charset);
                out.write('&');
                addNameValue(out, "n/ä&m%e2", "v§a*l#u+e2", charset);

                HttpServletRequest request = HttpRequestMockHelper.createFormRequest(null, out.toByteArray());
                POSTHttpServletRequestWrapper postWrapper = new POSTHttpServletRequestWrapper(request, null);

                assertParameter(postWrapper, "n/a&m%e1", "v§a*l  #u+e1");
                assertParameter(postWrapper, "n/ä&m%e2", "v§a*l#u+e2");
            }
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                addNameValue(out, "n/a&m%e1", "v§a*l  #u+e1", charset);
                out.write('&');
                addNameValue(out, "n/ä&m%e2", "v§a*l#u+e2", charset);
                out.write('&');
                addNameValue(out, "_charset_", charset, IOUtils.ISO_8859_1);

                HttpServletRequest request = HttpRequestMockHelper.createFormRequest(null, out.toByteArray());
                POSTHttpServletRequestWrapper postWrapper = new POSTHttpServletRequestWrapper(request, null);

                assertParameter(postWrapper, "n/a&m%e1", "v§a*l  #u+e1");
                assertParameter(postWrapper, "n/ä&m%e2", "v§a*l#u+e2");
            }
        }
    }

    @Test
    public void testCharsetHeader() throws IOException {
        for (String charset : CHARSETS) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            addNameValue(out, "n/a&m%e1", "v§a*l  #u+e1", charset);
            out.write('&');
            addNameValue(out, "n/ä&m%e2", "v§a*l#u+e2", charset);

            HttpServletRequest request = HttpRequestMockHelper.createFormRequest(charset, out.toByteArray());
            POSTHttpServletRequestWrapper postWrapper = new POSTHttpServletRequestWrapper(request, null);

            assertParameter(postWrapper, "n/a&m%e1", "v§a*l  #u+e1");
            assertParameter(postWrapper, "n/ä&m%e2", "v§a*l#u+e2");
        }
    }

    protected void addNameValue(OutputStream stream, String name, String value, String encoding) throws IOException {
        if (name != null) {
            stream.write(URLEncoder.encode(name, encoding).getBytes(IOUtils.ISO_8859_1));
        }

        stream.write('=');

        if (value != null) {
            stream.write(URLEncoder.encode(value, encoding).getBytes(IOUtils.ISO_8859_1));
        }
    }

    protected void assertParameter(HttpServletRequestWrapper wrapper, String name, String... values) {
        String[] pValues = wrapper.getParameterValues(name);
        assertNotNull(pValues);

        if (values == null) {
            assertEquals(1, pValues.length);
            assertEquals("", pValues[0]);
        } else {
            assertEquals(values.length, pValues.length);
            for (int i = 0; i < values.length; i++) {
                assertEquals(values[i], pValues[i]);
            }
        }
    }
}
