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
package org.apache.chemistry.opencmis.client.runtime;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Locale;

import org.apache.chemistry.opencmis.client.util.ContentStreamUtils;
import org.apache.chemistry.opencmis.client.util.ContentStreamUtils.AutoCloseInputStream;
import org.apache.chemistry.opencmis.commons.data.MutableContentStream;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.junit.Test;

public class ContentStreamUtilsTest {

    private static final String CONTENT = "content";
    private static final byte[] CONTENT_BYTES = IOUtils.toUTF8Bytes(CONTENT);

    @Test
    public void testTextContentStream() throws IOException {
        MutableContentStream contentStream = ContentStreamUtils.createTextContentStream("filename", CONTENT);

        assertNotNull(contentStream);
        assertEquals("filename", contentStream.getFileName());
        assertEquals("text/plain; charset=UTF-8", contentStream.getMimeType());
        assertEquals(CONTENT_BYTES.length, contentStream.getLength());
        assertNotNull(contentStream.getStream());
        assertTrue(contentStream.getStream() instanceof ContentStreamUtils.AutoCloseInputStream);

        AutoCloseInputStream in = (AutoCloseInputStream) contentStream.getStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        if (in.markSupported()) {
            in.mark(1024);
            in.read();
            in.reset();
        }

        assertEquals(CONTENT_BYTES.length, in.available());

        IOUtils.copy(in, out);

        assertArrayEquals(CONTENT_BYTES, out.toByteArray());

        try {
            in.read();
            fail();
        } catch (IOException ioe) {
            // excpeted
        }

        assertFalse(in.markSupported());
    }

    @Test
    public void testEmptyContentStream() throws IOException {
        MutableContentStream contentStream = ContentStreamUtils.createByteArrayContentStream(null, new byte[0]);

        assertNotNull(contentStream);
        assertEquals("content", contentStream.getFileName());
        assertEquals("application/octet-stream", contentStream.getMimeType());
        assertEquals(0, contentStream.getLength());
        assertEquals(BigInteger.ZERO, contentStream.getBigLength());
        assertNotNull(contentStream.getStream());

        InputStream in = (InputStream) contentStream.getStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        IOUtils.copy(in, out);

        assertEquals(0, out.toByteArray().length);

        contentStream.setFileName("myFile");
        assertEquals("myFile", contentStream.getFileName());
        contentStream.setMimeType("mime/type");
        assertEquals("mime/type", contentStream.getMimeType());

        try {
            in.read();
            fail();
        } catch (IOException ioe) {
            // excpeted
        }
    }

    @Test
    public void testNullContentStream() throws IOException {
        MutableContentStream contentStream = ContentStreamUtils.createByteArrayContentStream(null, null, null);

        assertNotNull(contentStream);
        assertEquals("content", contentStream.getFileName());
        assertEquals("application/octet-stream", contentStream.getMimeType());
        assertEquals(-1, contentStream.getLength());
        assertNull(contentStream.getBigLength());
        assertNull(contentStream.getStream());
    }

    @Test
    public void testByteArrayContentStream() throws IOException {
        byte[] content = IOUtils.toUTF8Bytes("1234567890");

        MutableContentStream contentStream = ContentStreamUtils.createByteArrayContentStream("array", content, 2, 5,
                "text/plain");

        assertNotNull(contentStream);
        assertEquals("array", contentStream.getFileName());
        assertEquals("text/plain", contentStream.getMimeType());
        assertEquals(5, contentStream.getLength());
        assertNotNull(contentStream.getBigLength());
        assertNotNull(contentStream.getStream());
    }

    @Test
    public void testFileContentStream() throws IOException {
        File tmpFile = File.createTempFile("test", ".txt");

        FileOutputStream fos = new FileOutputStream(tmpFile);
        fos.write(CONTENT_BYTES);
        fos.close();

        MutableContentStream contentStream = ContentStreamUtils.createFileContentStream(tmpFile);

        assertNotNull(contentStream);
        assertEquals(tmpFile.getName(), contentStream.getFileName());
        assertTrue(contentStream.getMimeType().toLowerCase(Locale.ENGLISH).startsWith("text/plain"));
        assertEquals(CONTENT_BYTES.length, contentStream.getLength());
        assertNotNull(contentStream.getStream());
        assertTrue(contentStream.getStream() instanceof ContentStreamUtils.AutoCloseInputStream);

        AutoCloseInputStream in = (AutoCloseInputStream) contentStream.getStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        IOUtils.copy(in, out);

        assertArrayEquals(CONTENT_BYTES, out.toByteArray());

        try {
            in.read();
            fail();
        } catch (IOException ioe) {
            // excpeted
        }

        in.closeQuietly();
    }
}
