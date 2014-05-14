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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.ContentStreamHash;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamHashImpl;
import org.junit.Test;

public class ContentStreamHashTest {

    @Test
    public void testHash() throws Exception {
        ContentStreamHashImpl hash1 = new ContentStreamHashImpl("{alg}0123456789abcdef");
        ContentStreamHashImpl hash2 = new ContentStreamHashImpl("alg", "0123456789abcdef");

        assertEquals("alg", hash1.getAlgorithm());
        assertEquals("0123456789abcdef", hash1.getHash());

        assertEquals(hash1.getPropertyValue(), hash2.getPropertyValue());
        assertEquals(hash1.getAlgorithm(), hash2.getAlgorithm());
        assertEquals(hash1.getHash(), hash2.getHash());
    }

    @Test
    public void testHashCorrected() throws Exception {
        ContentStreamHashImpl hash1 = new ContentStreamHashImpl("{alg} 01 23 45 67 89 AB CD EF ");
        ContentStreamHashImpl hash2 = new ContentStreamHashImpl("ALG", "0123 4567 89ab cdef");
        ContentStreamHashImpl hash3 = new ContentStreamHashImpl("aLg", new byte[] { (byte) 0x01, (byte) 0x23,
                (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef });

        assertEquals("alg", hash1.getAlgorithm());
        assertEquals("0123456789abcdef", hash1.getHash());

        assertEquals(hash1.getAlgorithm(), hash2.getAlgorithm());
        assertEquals(hash1.getHash(), hash2.getHash());

        assertEquals(hash1.getAlgorithm(), hash3.getAlgorithm());
        assertEquals(hash1.getHash(), hash3.getHash());

        assertFalse(hash1.equals(hash2));
        assertFalse(hash1.equals(hash2));
        assertTrue(hash2.equals(hash3));
    }

    @Test
    public void testHashStreamMD5() throws Exception {
        byte[] content = IOUtils.toUTF8Bytes("Hello World!");
        ByteArrayInputStream stream = new ByteArrayInputStream(content);

        ContentStreamHash hash = ContentStreamHashImpl.createContentStreamHashes(stream,
                ContentStreamHashImpl.ALGORITHM_MD5).get(0);

        assertEquals(ContentStreamHashImpl.ALGORITHM_MD5, hash.getAlgorithm());
        assertEquals("ed076287532e86365e841e92bfc50d8c", hash.getHash());
        assertEquals("{md5}ed076287532e86365e841e92bfc50d8c", hash.getPropertyValue());

        stream.close();
    }

    @Test
    public void testHashStreamSHA1() throws Exception {
        byte[] content = IOUtils.toUTF8Bytes("Hello World!");
        ByteArrayInputStream stream = new ByteArrayInputStream(content);

        ContentStreamHash hash = ContentStreamHashImpl.createContentStreamHashes(stream,
                ContentStreamHashImpl.ALGORITHM_SHA1).get(0);

        assertEquals(ContentStreamHashImpl.ALGORITHM_SHA1, hash.getAlgorithm());
        assertEquals("2ef7bde608ce5404e97d5f042f95f89f1c232871", hash.getHash());
        assertEquals("{sha-1}2ef7bde608ce5404e97d5f042f95f89f1c232871", hash.getPropertyValue());

        stream.close();
    }

    @Test
    public void testHashStreamSHA256() throws Exception {
        byte[] content = IOUtils.toUTF8Bytes("Hello World!");
        ByteArrayInputStream stream = new ByteArrayInputStream(content);

        ContentStreamHash hash = ContentStreamHashImpl.createContentStreamHashes(stream,
                ContentStreamHashImpl.ALGORITHM_SHA256).get(0);

        assertEquals(ContentStreamHashImpl.ALGORITHM_SHA256, hash.getAlgorithm());
        assertEquals("7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069", hash.getHash());
        assertEquals("{sha-256}7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069",
                hash.getPropertyValue());

        stream.close();
    }

    @Test
    public void testHashStreamSHA512() throws Exception {
        byte[] content = IOUtils.toUTF8Bytes("Hello World!");
        ByteArrayInputStream stream = new ByteArrayInputStream(content);

        ContentStreamHash hash = ContentStreamHashImpl.createContentStreamHashes(stream,
                ContentStreamHashImpl.ALGORITHM_SHA512).get(0);

        assertEquals(ContentStreamHashImpl.ALGORITHM_SHA512, hash.getAlgorithm());
        assertEquals(
                "861844d6704e8573fec34d967e20bcfef3d424cf48be04e6dc08f2bd58c729743371015ead891cc3cf1c9d34b49264b510751b1ff9e537937bc46b5d6ff4ecc8",
                hash.getHash());
        assertEquals(
                "{sha-512}"
                        + "861844d6704e8573fec34d967e20bcfef3d424cf48be04e6dc08f2bd58c729743371015ead891cc3cf1c9d34b49264b510751b1ff9e537937bc46b5d6ff4ecc8",
                hash.getPropertyValue());

        stream.close();
    }

    @Test
    public void testHashStreams() throws Exception {
        byte[] content = IOUtils.toUTF8Bytes("Hello World!");
        ByteArrayInputStream stream = new ByteArrayInputStream(content);

        List<ContentStreamHash> hashes = ContentStreamHashImpl.createContentStreamHashes(stream,
                ContentStreamHashImpl.ALGORITHM_SHA1, ContentStreamHashImpl.ALGORITHM_SHA256,
                ContentStreamHashImpl.ALGORITHM_SHA512);

        assertNotNull(hashes);
        assertEquals(3, hashes.size());

        assertEquals("{sha-1}2ef7bde608ce5404e97d5f042f95f89f1c232871", hashes.get(0).getPropertyValue());
        assertEquals("{sha-256}7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069", hashes.get(1)
                .getPropertyValue());
        assertEquals(
                "{sha-512}"
                        + "861844d6704e8573fec34d967e20bcfef3d424cf48be04e6dc08f2bd58c729743371015ead891cc3cf1c9d34b49264b510751b1ff9e537937bc46b5d6ff4ecc8",
                hashes.get(2).getPropertyValue());

        stream.close();
    }
}
