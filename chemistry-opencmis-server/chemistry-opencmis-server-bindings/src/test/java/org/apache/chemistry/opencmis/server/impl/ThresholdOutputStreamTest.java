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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.chemistry.opencmis.server.shared.ThresholdOutputStream;
import org.apache.chemistry.opencmis.server.shared.ThresholdOutputStream.ThresholdInputStream;
import org.apache.chemistry.opencmis.server.shared.ThresholdOutputStreamFactory;
import org.junit.Test;

public class ThresholdOutputStreamTest {

    private static final byte[] CONTENT = "Hello".getBytes();

    @Test
    public void testInMemory() throws Exception {
        ThresholdOutputStreamFactory streamFactory = ThresholdOutputStreamFactory.newInstance(null, 1024, 1024, false);

        // set content
        ThresholdOutputStream tos = streamFactory.newOutputStream();
        tos.write(CONTENT);
        tos.close();

        // get and check input stream
        ThresholdInputStream tis = (ThresholdInputStream) tos.getInputStream();

        assertTrue(tis.isInMemory());
        assertNull(tis.getTemporaryFile());
        assertTrue(tis.markSupported());
        assertEquals(CONTENT.length, tis.length());
        assertArrayEquals(CONTENT, getBytesFromArray(tis.getBytes(), (int) tis.length()));

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
        ThresholdOutputStreamFactory streamFactory = ThresholdOutputStreamFactory.newInstance(null, 0, 1024, false);

        // set content
        ThresholdOutputStream tos = streamFactory.newOutputStream();
        tos.write(CONTENT);
        tos.close();

        // get and check input stream
        ThresholdInputStream tis = (ThresholdInputStream) tos.getInputStream();

        assertFalse(tis.isInMemory());
        assertTrue(tis.markSupported());
        assertNull(tis.getBytes());
        assertEquals(CONTENT.length, tis.length());

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

        ThresholdOutputStreamFactory streamFactory = ThresholdOutputStreamFactory.newInstance(null, threshold, 1024,
                false);

        for (int i = 0; i < 20; i++) {
            ThresholdOutputStream tos = streamFactory.newOutputStream();
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

            tos.close();
        }
    }

    private byte[] getBytesFromArray(byte[] buffer, int len) {
        byte[] result = new byte[len];

        System.arraycopy(buffer, 0, result, 0, len);

        return result;
    }
}
