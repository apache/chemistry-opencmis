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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;

import org.apache.chemistry.opencmis.commons.server.TempStoreOutputStream;
import org.apache.chemistry.opencmis.server.shared.TempStoreOutputStreamFactory;
import org.apache.chemistry.opencmis.server.shared.ThresholdOutputStream;
import org.apache.chemistry.opencmis.server.shared.ThresholdOutputStream.ThresholdInputStream;
import org.junit.Test;

public class ThresholdOutputStreamTest {

    private static final String MIME_TYPE_1 = "some/type";
    private static final String MIME_TYPE_2 = "another/type";
    private static final String FILE_NAME_1 = "file1.txt";
    private static final String FILE_NAME_2 = "the_other_file";
    private static final byte[] CONTENT = "Hello".getBytes();

    @Test
    public void testInMemory() throws Exception {
        TempStoreOutputStreamFactory streamFactory = TempStoreOutputStreamFactory.newInstance(null, 1024, 1024, false);

        TempStoreOutputStream tempStream = streamFactory.newOutputStream();
        tempStream.setMimeType(MIME_TYPE_1);
        tempStream.setFileName(FILE_NAME_1);
        assertTrue(tempStream instanceof ThresholdOutputStream);

        // set content
        ThresholdOutputStream tos = (ThresholdOutputStream) tempStream;
        tos.write(CONTENT);
        tos.close();

        // get and check input stream
        ThresholdInputStream tis = (ThresholdInputStream) tos.getInputStream();

        assertTrue(tis.isInMemory());
        assertNull(tis.getTemporaryFile());
        assertTrue(tis.markSupported());
        assertEquals(CONTENT.length, tis.getLength());
        assertArrayEquals(CONTENT, getBytesFromArray(tis.getBytes(), (int) tis.getLength()));
        assertEquals(MIME_TYPE_1, tis.getMimeType());
        assertEquals(FILE_NAME_1, tis.getFileName());

        // read stream
        byte[] buffer = new byte[CONTENT.length];
        int len = tis.read(buffer);
        assertEquals(CONTENT.length, len);
        assertArrayEquals(CONTENT, buffer);

        // rewind and read again
        tis.rewind();
        len = tis.read(buffer);
        assertEquals(CONTENT.length, len);
        assertArrayEquals(CONTENT, buffer);

        // mark and reset
        tis.rewind();
        tis.read();
        tis.mark(1024);
        tis.read();
        tis.read();
        tis.reset();
        len = tis.read(buffer);
        assertEquals(CONTENT.length - 1, len);

        // close and check
        tis.close();

        assertEquals(-1, tis.read());

        try {
            tis.getBytes();
            fail("IllegalStateException expected!");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testTempFile() throws Exception {
        TempStoreOutputStreamFactory streamFactory = TempStoreOutputStreamFactory.newInstance(null, 0, 1024, false);

        TempStoreOutputStream tempStream = streamFactory.newOutputStream();
        tempStream.setMimeType(MIME_TYPE_2);
        tempStream.setFileName(FILE_NAME_2);
        assertTrue(tempStream instanceof ThresholdOutputStream);

        // set content
        ThresholdOutputStream tos = (ThresholdOutputStream) tempStream;
        tos.write(CONTENT);
        tos.close();

        // get and check input stream
        ThresholdInputStream tis = (ThresholdInputStream) tos.getInputStream();

        assertFalse(tis.isInMemory());
        assertTrue(tis.markSupported());
        assertNull(tis.getBytes());
        assertEquals(CONTENT.length, tis.getLength());
        assertEquals(MIME_TYPE_2, tis.getMimeType());
        assertEquals(FILE_NAME_2, tis.getFileName());

        assertTrue(tis.getTemporaryFile().exists());
        assertEquals(CONTENT.length, tis.getTemporaryFile().length());

        // read stream
        byte[] buffer = new byte[CONTENT.length];
        int len = tis.read(buffer);
        assertEquals(CONTENT.length, len);
        assertArrayEquals(CONTENT, buffer);
        assertTrue(tis.getTemporaryFile().exists());

        // rewind and read again
        tis.rewind();
        len = tis.read(buffer);
        assertEquals(CONTENT.length, len);
        assertArrayEquals(CONTENT, buffer);
        assertTrue(tis.getTemporaryFile().exists());

        // mark and reset
        tis.rewind();
        tis.read();
        tis.mark(1024);
        tis.read();
        tis.read();
        tis.reset();
        len = tis.read(buffer);
        assertEquals(CONTENT.length - 1, len);

        File tempFile = tis.getTemporaryFile();

        // close and check
        tis.close();

        assertEquals(-1, tis.read());
        assertFalse(tempFile.exists());

        try {
            tis.getTemporaryFile();
            fail("IllegalStateException expected!");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testThreshold() throws Exception {
        int threshold = 8;

        TempStoreOutputStreamFactory streamFactory = TempStoreOutputStreamFactory.newInstance(null, threshold, 1024,
                false);

        for (int i = 0; i < 20; i++) {
            TempStoreOutputStream tempStream = streamFactory.newOutputStream();
            assertTrue(tempStream instanceof ThresholdOutputStream);

            ThresholdOutputStream tos = (ThresholdOutputStream) tempStream;
            for (int j = 0; j < i; j++) {
                tos.write('0' + j);
            }
            tos.close();

            ThresholdInputStream tis = (ThresholdInputStream) tos.getInputStream();
            if (i > threshold) {
                assertFalse(tis.isInMemory());
            } else {
                assertTrue(tis.isInMemory());
            }

            File tempFile = tis.getTemporaryFile();

            tis.close();

            assertEquals(-1, tis.read());
            if (tis.isInMemory()) {
                assertNull(tempFile);
            } else {
                assertNotNull(tempFile);
                assertFalse(tempFile.exists());
            }
        }
    }

    @Test
    public void testClose() throws Exception {
        TempStoreOutputStreamFactory streamFactory = TempStoreOutputStreamFactory.newInstance(null, 0, 1024, false);

        TempStoreOutputStream tempStream = streamFactory.newOutputStream();
        tempStream.setMimeType(MIME_TYPE_2);
        tempStream.setFileName(FILE_NAME_2);
        assertTrue(tempStream instanceof ThresholdOutputStream);

        // set content
        ThresholdOutputStream tos = (ThresholdOutputStream) tempStream;
        tos.write(CONTENT);
        tos.close();

        // get and check input stream
        ThresholdInputStream tis = (ThresholdInputStream) tos.getInputStream();

        assertFalse(tis.isInMemory());

        File tempFile = tis.getTemporaryFile();
        assertTrue(tempFile.exists());

        // close stream -> delete temp file
        tis.close();

        // check temp file
        assertFalse(tempFile.exists());
    }

    @Test
    public void testDestroy() throws Exception {
        TempStoreOutputStreamFactory streamFactory = TempStoreOutputStreamFactory.newInstance(null, 0, 1024, false);

        TempStoreOutputStream tempStream = streamFactory.newOutputStream();
        tempStream.setMimeType(MIME_TYPE_2);
        tempStream.setFileName(FILE_NAME_2);
        assertTrue(tempStream instanceof ThresholdOutputStream);

        // set content
        ThresholdOutputStream tos = (ThresholdOutputStream) tempStream;
        tos.write(CONTENT);
        tos.close();

        // get temp file
        Field tempFileField = ThresholdOutputStream.class.getDeclaredField("tempFile");
        tempFileField.setAccessible(true);
        File tempFile = (File) tempFileField.get(tos);

        // destroy -> delete temp file
        tempStream.destroy(new Exception("ohoh"));

        // check temp file
        assertFalse(tempFile.exists());
    }

    @Test
    public void testEncrypt() throws Exception {
        TempStoreOutputStreamFactory streamFactory = TempStoreOutputStreamFactory.newInstance(null, 0, 1024, true);

        TempStoreOutputStream tempStream = streamFactory.newOutputStream();
        tempStream.setMimeType(MIME_TYPE_2);
        tempStream.setFileName(FILE_NAME_2);
        assertTrue(tempStream instanceof ThresholdOutputStream);

        // set content
        ThresholdOutputStream tos = (ThresholdOutputStream) tempStream;
        tos.write(CONTENT);
        tos.close();

        // get and check input stream
        ThresholdInputStream tis = (ThresholdInputStream) tos.getInputStream();

        assertFalse(tis.isInMemory());

        File tempFile = tis.getTemporaryFile();
        assertTrue(tempFile.exists());

        // temp file must not contain clear data
        boolean isDifferent = false;

        FileInputStream tempFileStream = new FileInputStream(tis.getTemporaryFile());
        for (byte b1 : CONTENT) {
            byte b2 = (byte) tempFileStream.read();
            if (b1 != b2) {
                isDifferent = true;
                break;
            }
        }

        tempFileStream.close();

        assertTrue(isDifferent);

        // close
        tis.close();

        // check temp file
        assertFalse(tempFile.exists());
    }

    @Test
    public void testNoThreshold() throws Exception {
        int size = 128 * 1024;

        TempStoreOutputStreamFactory streamFactory = TempStoreOutputStreamFactory.newInstance(null, 0, size * 2, false);

        TempStoreOutputStream tempStream = streamFactory.newOutputStream();
        assertTrue(tempStream instanceof ThresholdOutputStream);

        ThresholdOutputStream tos = (ThresholdOutputStream) tempStream;

        byte[] bytes = new byte[size];
        tos.write(bytes);
        tos.close();

        ThresholdInputStream tis = (ThresholdInputStream) tos.getInputStream();
        assertFalse(tis.isInMemory());

        File tempFile = tis.getTemporaryFile();
        assertEquals(size, tempFile.length());

        tis.close();
    }

    private byte[] getBytesFromArray(byte[] buffer, int len) {
        byte[] result = new byte[len];

        System.arraycopy(buffer, 0, result, 0, len);

        return result;
    }
}
