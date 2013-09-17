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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.junit.Test;

/**
 * Base64 tests. (Placeholder for more tests.)
 */
public class Base64Test {

    @Test
    public void testSimpleBase64() throws Exception {
        byte[] input = IOUtils.toUTF8Bytes("test");

        assertEquals("dGVzdA==", Base64.encodeBytes(input, 0, input.length));
        assertArrayEquals(input, Base64.decode("dGVzdA=="));
        assertArrayEquals(input, Base64.decode(Base64.encodeBytes(input)));
    }

    @Test
    public void testBase64Stream() throws Exception {
        ByteArrayInputStream stream = new ByteArrayInputStream("dGVzdA==".getBytes("US-ASCII"));
        InputStream base64stream = new Base64.InputStream(stream);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        IOUtils.copy(base64stream, output);

        base64stream.close();

        assertArrayEquals("test".getBytes("US-ASCII"), output.toByteArray());
    }
}
