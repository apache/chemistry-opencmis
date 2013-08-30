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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.junit.Test;

public class IOUtilsTest {

    @Test
    public void testEncoding() {
        String url = "http://example.com/abc?key=äöü @!§$%&/";

        assertEquals(url, IOUtils.decodeURL(IOUtils.encodeURL(url)));
        assertNull(IOUtils.encodeURL(null));
        assertNull(IOUtils.decodeURL(null));
    }

    @Test
    public void testConsumeAndClose() {
        ByteArrayInputStream stream = new ByteArrayInputStream(IOUtils.getUTF8Bytes("test"));

        IOUtils.consumeAndClose(stream);
        assertEquals(0, stream.available());

        ByteArrayInputStream stream2 = new ByteArrayInputStream(IOUtils.getUTF8Bytes("test"));
        InputStreamReader reader = new InputStreamReader(stream2);

        IOUtils.consumeAndClose(reader);
        assertEquals(0, stream2.available());

        try {
            reader.read();
            fail("Reader should be closed.");
        } catch (IOException e) {
            // expected
        }

        IOUtils.closeQuietly(reader);
        IOUtils.closeQuietly((Closeable) null);
        IOUtils.closeQuietly((ContentStream) null);
    }

    @Test
    public void testCopy() throws Exception {
        byte[] input = IOUtils.getUTF8Bytes("test");
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        IOUtils.copy(in, out);

        assertArrayEquals(input, out.toByteArray());

        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
    }

    @Test
    public void testFirstLine() throws IOException {
        String content = "line 1\nline 2\nline 3";
        InputStream stream = createStream(content);

        assertEquals("line 1", IOUtils.readFirstLine(stream));
    }

    @Test
    public void testAllLines() throws IOException {
        String content = "line 1\nline 2\nline 3";
        InputStream stream = createStream(content);

        assertEquals("line 1\nline 2\nline 3\n", IOUtils.readAllLines(stream));
    }

    @Test
    public void testAllLinesAndSkipHeader1() throws IOException {
        String content = "#header\n\nline 1\nline 2";
        InputStream stream = createStream(content);

        assertEquals("line 1\nline 2\n", IOUtils.readAllLinesAndRemoveHeader(stream));
    }

    @Test
    public void testAllLinesAndSkipHeader2() throws IOException {
        String content = "/* header 1\n * header 2\n*/\n\nline 1\nline 2";
        InputStream stream = createStream(content);

        assertEquals("line 1\nline 2\n", IOUtils.readAllLinesAndRemoveHeader(stream));
    }

    @Test
    public void testIgnoreComments() throws IOException {
        String content = "#comment1\n\nline 1\n  # comment 2\nline 2\nline 3";
        InputStream stream = createStream(content);

        assertEquals("line 1\nline 2\nline 3\n", IOUtils.readAllLinesAndIgnoreComments(stream));
    }

    @Test
    public void testAsList() throws IOException {
        String content = "#comment1\n\nline 1\n  # comment 2\nline 2\nline 3";
        InputStream stream = createStream(content);

        List<String> list = IOUtils.readAllLinesAsList(stream);

        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("line 1", list.get(0));
        assertEquals("line 2", list.get(1));
        assertEquals("line 3", list.get(2));
    }

    @Test
    public void testAsMap() throws IOException {
        String content = "#comment1\nkey1\n  # comment 2\nkey2=value2\nkey3 = value3";
        InputStream stream = createStream(content);

        Map<String, String> map = IOUtils.readAllLinesAsMap(stream);

        assertNotNull(map);
        assertEquals(3, map.size());
        assertEquals("", map.get("key1"));
        assertEquals("value2", map.get("key2"));
        assertEquals("value3", map.get("key3"));
    }

    private InputStream createStream(String content) {
        return new ByteArrayInputStream(IOUtils.getUTF8Bytes(content));
    }
}
