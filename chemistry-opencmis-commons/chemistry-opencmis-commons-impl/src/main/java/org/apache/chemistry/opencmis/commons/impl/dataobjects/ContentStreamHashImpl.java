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
package org.apache.chemistry.opencmis.commons.impl.dataobjects;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.chemistry.opencmis.commons.data.ContentStreamHash;

public class ContentStreamHashImpl implements ContentStreamHash {

    public static final String ALGORITHM_MD5 = "md5";
    public static final String ALGORITHM_SHA1 = "sha-1";
    public static final String ALGORITHM_SHA224 = "sha-224";
    public static final String ALGORITHM_SHA256 = "sha-256";
    public static final String ALGORITHM_SHA384 = "sha-384";
    public static final String ALGORITHM_SHA512 = "sha-512";
    public static final String ALGORITHM_SHA3 = "sha-3";

    private final static char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    private String propertyValue;
    private String algorithm = null;
    private String hash = null;

    /**
     * Constructs an object from the {@code cmis:contentStreamHash} property
     * value.
     * 
     * @param propertyValue
     *            the property value
     */
    public ContentStreamHashImpl(String propertyValue) {
        this.propertyValue = propertyValue;

        if (propertyValue == null) {
            return;
        }

        String pv = propertyValue.trim();
        int algEnd = pv.indexOf('}');
        if (pv.charAt(0) != '{' || algEnd < 1) {
            return;
        }

        this.algorithm = pv.substring(1, algEnd).toLowerCase(Locale.ENGLISH);
        this.hash = pv.substring(algEnd + 1).replaceAll("\\s", "").toLowerCase(Locale.ENGLISH);
    }

    /**
     * Constructs an object from the algorithm and hash.
     * 
     * @param algorithm
     *            the algorithm
     * @param hash
     *            the hash value
     */
    public ContentStreamHashImpl(String algorithm, String hash) {
        if (algorithm == null || algorithm.trim().length() == 0) {
            throw new IllegalArgumentException("Algorithm must be set!");
        }

        if (hash == null || hash.trim().length() == 0) {
            throw new IllegalArgumentException("Hash must be set!");
        }

        this.algorithm = algorithm.toLowerCase(Locale.ENGLISH);
        this.hash = hash.replaceAll("\\s", "").toLowerCase(Locale.ENGLISH);
        this.propertyValue = "{" + algorithm + "}" + hash;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getHash() {
        return hash;
    }

    /**
     * Creates a list of content hashes from a stream
     * <p>
     * This method consumes the stream but doesn't close it.
     * 
     * @param stream
     *            the stream
     * @param algorithm
     *            the algorithms
     * @return the list of content hashes
     */
    public static List<ContentStreamHash> createContentStreamHashes(InputStream stream, String... algorithm)
            throws IOException, NoSuchAlgorithmException {
        if (stream == null) {
            throw new IllegalArgumentException("Stream must be set!");
        }

        if (algorithm == null || algorithm.length == 0) {
            throw new IllegalArgumentException("Algorithm must be set!");
        }

        MessageDigest[] md = new MessageDigest[algorithm.length];
        for (int i = 0; i < algorithm.length; i++) {
            md[i] = MessageDigest.getInstance(algorithm[i]);
        }

        int b;
        byte[] buffer = new byte[64 * 1024];
        while ((b = stream.read(buffer)) > -1) {
            for (int j = 0; j < md.length; j++) {
                md[j].update(buffer, 0, b);
            }
        }

        List<ContentStreamHash> result = new ArrayList<ContentStreamHash>();

        for (int i = 0; i < md.length; i++) {
            result.add(new ContentStreamHashImpl(algorithm[i], byteArrayToHexString(md[i].digest())));
        }

        return result;
    }

    protected static String byteArrayToHexString(byte[] bytes) {
        int n = bytes.length;
        char[] hashHex = new char[n * 2];
        for (int i = 0; i < n; i++) {
            hashHex[i * 2] = HEX_DIGITS[(0xF0 & bytes[i]) >>> 4];
            hashHex[i * 2 + 1] = HEX_DIGITS[0x0F & bytes[i]];
        }

        return new String(hashHex);
    }
}
