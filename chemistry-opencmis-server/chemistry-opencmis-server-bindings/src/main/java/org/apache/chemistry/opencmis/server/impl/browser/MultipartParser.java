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
package org.apache.chemistry.opencmis.server.impl.browser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.MimeHelper;
import org.apache.chemistry.opencmis.server.shared.ThresholdOutputStream;

/**
 * Simple multi-part parser, following all necessary standards for the CMIS
 * browser binding.
 */
public class MultipartParser {

    public static final String MULTIPART = "multipart/";

    private static final int MAX_FIELD_BYTES = 10 * 1024 * 1024;

    private static final int BUFFER_SIZE = 64 * 1024;

    private static final byte CR = 0x0D;
    private static final byte LF = 0x0A;
    private static final byte DASH = 0x2D;
    private static final byte[] BOUNDARY_PREFIX = { CR, LF, DASH, DASH };

    private final HttpServletRequest request;
    private final File tempDir;
    private final int memoryThreshold;
    private final long maxContentSize;
    private final InputStream requestStream;

    private byte[] boundary;
    private int[] badCharacters;
    private int[] goodSuffixes;

    private byte[] buffer;
    private byte[] buffer2;
    private int bufferPosition;
    private int bufferCount;
    private boolean eof;

    private int fieldBytes;
    private boolean hasContent;

    private Map<String, String> headers;

    private boolean isContent;
    private String name;
    private String filename;
    private String contentType;
    private BigInteger contentSize;
    private InputStream contentStream;
    private String value;

    public MultipartParser(HttpServletRequest request, File tempDir, int memoryThreshold, long maxContentSize)
            throws IOException {
        this.request = request;
        this.tempDir = tempDir;
        this.memoryThreshold = memoryThreshold;
        this.maxContentSize = maxContentSize;

        this.requestStream = request.getInputStream();

        extractBoundary();

        buffer = new byte[BUFFER_SIZE + boundary.length];
        buffer2 = new byte[buffer.length];
        bufferPosition = 0;
        bufferCount = 0;
        eof = false;

        hasContent = false;
        fieldBytes = 0;

        skipPreamble();
    }

    private void extractBoundary() {
        String contentType = request.getContentType();

        // parse content type and extract boundary
        byte[] extractedBoundary = MimeHelper.getBoundaryFromMultiPart(contentType);
        if (extractedBoundary == null) {
            throw new CmisInvalidArgumentException("Invalid multipart request!");
        }

        boundary = new byte[BOUNDARY_PREFIX.length + extractedBoundary.length];
        System.arraycopy(BOUNDARY_PREFIX, 0, boundary, 0, BOUNDARY_PREFIX.length);
        System.arraycopy(extractedBoundary, 0, boundary, BOUNDARY_PREFIX.length, extractedBoundary.length);

        // prepare boundary search

        int m = boundary.length;

        badCharacters = new int[256];
        Arrays.fill(badCharacters, -1);

        for (int j = 0; j < m; j++) {
            badCharacters[boundary[j] & 0xff] = j;
        }

        int[] f = new int[m + 1];
        goodSuffixes = new int[m + 1];
        int i = m;
        int j = m + 1;
        f[i] = j;
        while (i > 0) {
            while (j <= m && boundary[i - 1] != boundary[j - 1]) {
                if (goodSuffixes[j] == 0) {
                    goodSuffixes[j] = j - i;
                }
                j = f[j];
            }
            i--;
            j--;
            f[i] = j;
        }

        j = f[0];
        for (i = 0; i <= m; i++) {
            if (goodSuffixes[i] == 0) {
                goodSuffixes[i] = j;
            }

            if (i == j) {
                j = f[j];
            }
        }
    }

    private int findBoundary() {
        if (bufferCount < boundary.length) {
            if (eof) {
                throw new CmisInvalidArgumentException("Unexpected end of stream!");
            } else {
                return -1;
            }
        }

        int m = boundary.length;

        int i = 0;
        while (i <= bufferCount - m) {
            int j = m - 1;
            while (j >= 0 && boundary[j] == buffer[i + j]) {
                j--;
            }

            if (j < 0) {
                return i;
            } else {
                i += Math.max(goodSuffixes[j + 1], j - badCharacters[buffer[i + j] & 0xff]);
            }
        }

        return -1;
    }

    private void readBuffer() throws IOException {
        if (bufferPosition < bufferCount) {
            System.arraycopy(buffer, bufferPosition, buffer2, 0, bufferCount - bufferPosition);
            bufferCount = bufferCount - bufferPosition;

            byte[] tmpBuffer = buffer2;
            buffer2 = buffer;
            buffer = tmpBuffer;
        } else {
            bufferCount = 0;
        }

        bufferPosition = 0;

        if (eof) {
            return;
        }

        while (true) {
            int r = requestStream.read(buffer, bufferCount, buffer.length - bufferCount);
            if (r == -1) {
                eof = true;
                break;
            }

            bufferCount += r;
            if (buffer.length == bufferCount) {
                break;
            }
        }
    }

    private int nextByte() throws IOException {
        if (bufferCount == 0) {
            if (eof) {
                return -1;
            } else {
                readBuffer();
                return nextByte();
            }
        }

        if (bufferCount > bufferPosition) {
            return buffer[bufferPosition++];
        }

        readBuffer();
        return nextByte();
    }

    private String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();

        int r;
        while ((r = nextByte()) > -1) {
            if (r == CR) {
                if (nextByte() != LF) {
                    throw new CmisInvalidArgumentException("Invalid multipart request!");
                }
                break;
            }

            sb.append((char) r);
        }

        return sb.toString();
    }

    private void readHeaders() throws IOException {
        int b = nextByte();
        if (b == -1) {
            throw new CmisInvalidArgumentException("Unexpected end of stream!");
        }

        if (b == DASH) {
            b = nextByte();
            if (b == DASH) {
                // expected end of stream
                headers = null;
                return;
            }
        } else if (b == CR) {
            b = nextByte();
            if (b == LF) {
                parseHeaders();
                return;
            }
        }

        throw new CmisInvalidArgumentException("Invalid multipart request!");
    }

    private void parseHeaders() throws IOException {
        headers = new HashMap<String, String>();

        String line;
        while ((line = readLine()).length() > 0) {
            int x = line.indexOf(':');
            if (x > 0) {
                headers.put(line.substring(0, x).toLowerCase(Locale.ENGLISH).trim(), line.substring(x + 1).trim());
            }
        }
    }

    public void readBodyAsString(String charset) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        while (true) {
            readBuffer();

            int boundaryPosition = findBoundary();

            if (boundaryPosition > -1) {
                bos.write(buffer, bufferPosition, boundaryPosition - bufferPosition);
                bufferPosition = boundaryPosition + boundary.length;
                break;
            } else {
                int len = Math.min(BUFFER_SIZE, bufferCount) - bufferPosition;
                bos.write(buffer, bufferPosition, len);
                bufferPosition = bufferPosition + len;
            }

            fieldBytes += bos.size();
            if (fieldBytes > MAX_FIELD_BYTES) {
                throw new CmisInvalidArgumentException("Limit exceeded!");
            }
        }

        try {
            value = bos.toString(charset);
        } catch (UnsupportedEncodingException uee) {
            throw new CmisInvalidArgumentException("Unknown endcoding!");
        }
    }

    public void readBodyAsStream() throws Exception {
        ThresholdOutputStream stream = new ThresholdOutputStream(tempDir, memoryThreshold, maxContentSize);

        try {
            while (true) {
                readBuffer();

                int boundaryPosition = findBoundary();

                if (boundaryPosition > -1) {
                    stream.write(buffer, bufferPosition, boundaryPosition - bufferPosition);
                    bufferPosition = boundaryPosition + boundary.length;
                    break;
                } else {
                    int len = Math.min(BUFFER_SIZE, bufferCount) - bufferPosition;
                    stream.write(buffer, bufferPosition, len);
                    bufferPosition = bufferPosition + len;
                }
            }

            stream.close();

            contentSize = BigInteger.valueOf(stream.getSize());
            contentStream = stream.getInputStream();
        } catch (Exception e) {
            // if something went wrong, make sure the temp file will
            // be deleted
            stream.destroy();
            throw e;
        }
    }

    private void readBody() throws Exception {
        String contentDisposition = headers.get("content-disposition");

        if (contentDisposition == null) {
            throw new CmisInvalidArgumentException("Invalid multipart request!");
        }

        Map<String, String> params = new HashMap<String, String>();
        MimeHelper.decodeContentDisposition(contentDisposition, params);

        name = params.get(MimeHelper.DISPOSITION_NAME);
        filename = params.get(MimeHelper.DISPOSITION_FILENAME);
        isContent = (filename != null);
        contentType = headers.get("content-type");

        if (isContent) {
            if (hasContent) {
                throw new CmisInvalidArgumentException("Only one content expected!");
            }

            hasContent = true;

            if (contentType == null) {
                contentType = Constants.MEDIATYPE_OCTETSTREAM;
            }

            readBodyAsStream();
        } else {
            contentSize = BigInteger.ZERO;

            String charset = null;

            if (contentType != null) {
                charset = MimeHelper.getCharsetFromContentType(contentType);
            }

            if (charset == null) {
                charset = "ISO-8859-1";
            }

            readBodyAsString(charset);
        }
    }

    private void skipPreamble() throws IOException {
        readBuffer();

        if (bufferCount < boundary.length - 2) {
            throw new CmisInvalidArgumentException("Invalid multipart request!");
        }

        for (int i = 2; i < boundary.length; i++) {
            if (boundary[i] != buffer[i - 2]) {
                break;
            }

            if (i == boundary.length - 1) {
                bufferPosition = boundary.length - 2;
                readBuffer();
                return;
            }
        }

        while (true) {
            int boundaryPosition = findBoundary();

            if (boundaryPosition > -1) {
                bufferPosition = boundaryPosition + boundary.length;
                readBuffer();
                break;
            }

            bufferPosition = BUFFER_SIZE + 1;
            readBuffer();
        }
    }

    private void skipEpilogue() {
        try {
            // read to the end of stream
            byte[] tmpBuf = new byte[4096];
            while (requestStream.read(tmpBuf) > -1) {
            }
        } catch (IOException e) {
            // ignore
        }
    }

    public boolean readNext() throws Exception {
        try {
            readHeaders();

            // no headers -> end of request
            if (headers == null) {
                skipEpilogue();
                return false;
            }

            readBody();

            return true;
        } catch (Exception e) {
            if (contentStream != null) {
                try {
                    contentStream.close();
                } catch (Exception e2) {
                    // ignore
                }
            }

            skipEpilogue();

            throw e;
        }
    }

    public boolean isContent() {
        return isContent;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public BigInteger getSize() {
        return contentSize;
    }

    public InputStream getStream() {
        return contentStream;
    }

    /**
     * Returns if the request is a multi-part request
     */
    public static final boolean isMultipartContent(HttpServletRequest request) {
        String contentType = request.getContentType();

        if (contentType != null && contentType.toLowerCase(Locale.ENGLISH).startsWith(MULTIPART)) {
            return true;
        }

        return false;
    }
}
