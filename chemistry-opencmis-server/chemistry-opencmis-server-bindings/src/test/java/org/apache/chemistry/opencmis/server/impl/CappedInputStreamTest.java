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

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;

import org.apache.chemistry.opencmis.server.shared.CappedInputStream;
import org.junit.Test;

public class CappedInputStreamTest {

    @Test
    public void testStream1() {
        baseTest(64 * 1024, 10 * 1024, 1000, 60 * 1024, true);
    }

    @Test
    public void testStream2() {
        baseTest(64 * 1024, 10 * 1024, 1000, 2000, false);
    }

    @Test
    public void testStream3() {
        baseTest(100, 20, 0, 79, true);
    }

    @Test
    public void testStream4() {
        baseTest(100, 20, 10, 89, true);
    }

    @Test
    public void testStream5() {
        baseTest(100, 20, 0, 30, false);
    }

    @Test
    public void testStream6() {
        baseTest(100, 20, 19, 99, true);
    }

    private void baseTest(int bufferSize, int max, int goodBegin, int goodEnd, boolean success) {
        byte[] byteBuffer = new byte[bufferSize];
        for (int i = 0; i < byteBuffer.length; i++) {
            byteBuffer[i] = (byte) (i >= goodBegin && i <= goodEnd ? 1 : 0);
        }

        // test read with buffer
        ByteArrayInputStream originStream = new ByteArrayInputStream(byteBuffer);

        try {
            CappedInputStream stream = new CappedInputStream(originStream, max);

            int b = 0;
            byte[] buffer = new byte[10];
            while ((b = stream.read(buffer)) > -1) {
                int counter = 0;

                for (int i = 0; i < b; i++) {
                    if (buffer[i] == 1) {
                        counter++;
                    }
                }

                stream.deductBytes(counter);
            }

            stream.close();
        } catch (Exception e) {
            if (success) {
                fail();
            }
        }

        // test single byte read
        originStream = new ByteArrayInputStream(byteBuffer);

        try {
            CappedInputStream stream = new CappedInputStream(originStream, max);

            int b = 0;
            while ((b = stream.read()) > -1) {
                if (b == 1) {
                    stream.deductBytes(1);
                }
            }

            stream.close();
        } catch (Exception e) {
            if (success) {
                fail();
            }
        }
    }
}
