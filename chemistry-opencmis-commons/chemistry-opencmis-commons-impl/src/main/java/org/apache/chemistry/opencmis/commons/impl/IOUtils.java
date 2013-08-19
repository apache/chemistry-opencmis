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
package org.apache.chemistry.opencmis.commons.impl;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

public final class IOUtils {

    /** UTF-8 character set name. */
    public static final String UTF8 = "UTF-8";

    private IOUtils() {
    }

    /**
     * Return UTF-8 bytes of the given string or throws a
     * {@link CmisRuntimeException} if the charset 'UTF-8' is not available.
     */
    public static byte[] getUTF8Bytes(String s) {
        if (s == null) {
            return null;
        }

        try {
            return s.getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new CmisRuntimeException("Unsupported encoding 'UTF-8'!", e);
        }
    }

    /**
     * URL encodes the given string or throws a {@link CmisRuntimeException} if
     * the charset 'UTF-8' is not available.
     */
    public static String encodeURL(String s) {
        if (s == null) {
            return null;
        }

        try {
            return URLEncoder.encode(s, UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new CmisRuntimeException("Unsupported encoding 'UTF-8'!", e);
        }
    }

    /**
     * URL dencodes the given string or throws a {@link CmisRuntimeException} if
     * the charset 'UTF-8' is not available.
     */
    public static String decodeURL(String s) {
        if (s == null) {
            return null;
        }

        try {
            return URLDecoder.decode(s, UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new CmisRuntimeException("Unsupported encoding 'UTF-8'!", e);
        }
    }

    /**
     * Closes a stream and ignores any exceptions.
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }

    /**
     * Closes the stream of a {@link ContentStream} object and ignores any
     * exceptions.
     */
    public static void closeQuietly(final ContentStream contentStream) {
        if (contentStream != null) {
            closeQuietly(contentStream.getStream());
        }
    }

    /**
     * Consumes and closes the provided stream.
     */
    @SuppressWarnings({ "PMD.EmptyCatchBlock", "PMD.EmptyWhileStmt" })
    public static void consumeAndClose(final InputStream stream) {
        if (stream == null) {
            return;
        }

        try {
            final byte[] buffer = new byte[4096];
            while (stream.read(buffer) > -1) {
                // just consume
            }
        } catch (IOException e) {
            // ignore
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Consumes and closes the provided reader.
     */
    @SuppressWarnings({ "PMD.EmptyCatchBlock", "PMD.EmptyWhileStmt" })
    public static void consumeAndClose(final Reader reader) {
        if (reader == null) {
            return;
        }

        try {
            final char[] buffer = new char[4096];
            while (reader.read(buffer) > -1) {
                // just consume
            }
        } catch (IOException e) {
            // ignore
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Reads lines from an UTF-8 encoded stream.
     * 
     * @param stream
     *            the stream
     * @param handler
     *            a handler the processes each line.
     */
    public static void readLinesFromStream(InputStream stream, LineHandler handler) throws IOException {
        if (stream == null) {
            return;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream, UTF8));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!handler.handle(line)) {
                    break;
                }
            }
        } finally {
            closeQuietly(reader);
        }
    }

    /**
     * Reads the first line from a stream.
     */
    public static String readFirstLine(InputStream stream) throws IOException {
        final StringBuilder result = new StringBuilder();

        readLinesFromStream(stream, new LineHandler() {
            public boolean handle(String line) {
                result.append(line);
                return false;
            }
        });

        return result.toString();
    }

    /**
     * Reads all lines from a stream.
     */
    public static String readAllLines(InputStream stream) throws IOException {
        final StringBuilder result = new StringBuilder();

        readLinesFromStream(stream, new LineHandler() {
            public boolean handle(String line) {
                result.append(line);
                result.append('\n');
                return true;
            }
        });

        return result.toString();
    }

    /**
     * Reads all lines from a stream and removes the header.
     */
    public static String readAllLinesAndRemoveHeader(InputStream stream) throws IOException {
        final StringBuilder result = new StringBuilder();

        readLinesFromStream(stream, new SkipHeaderLineHandler() {
            public boolean handle(String line) {
                if (!isHeaderLine(line)) {
                    result.append(line);
                    result.append('\n');
                }
                return true;
            }
        });

        return result.toString();
    }

    /**
     * Reads all lines from a stream and ignore all comments.
     */
    public static String readAllLinesAndIgnoreComments(InputStream stream) throws IOException {
        final StringBuilder result = new StringBuilder();

        readLinesFromStream(stream, new IgnoreCommentsLineHandler() {
            public boolean handle(String line) {
                if (!isComment(line)) {
                    result.append(line);
                    result.append('\n');
                }
                return true;
            }
        });

        return result.toString();
    }

    /**
     * Reads all lines from a stream and ignore all comments.
     */
    public static List<String> readAllLinesAsList(InputStream stream) throws IOException {
        final List<String> result = new ArrayList<String>();

        readLinesFromStream(stream, new IgnoreCommentsLineHandler() {
            public boolean handle(String line) {
                if (!isComment(line)) {
                    result.add(line);
                }
                return true;
            }
        });

        return result;
    }

    /**
     * Reads all lines from a stream and ignore all comments.
     */
    public static Map<String, String> readAllLinesAsMap(InputStream stream) throws IOException {
        final Map<String, String> result = new HashMap<String, String>();

        readLinesFromStream(stream, new IgnoreCommentsLineHandler() {
            public boolean handle(String line) {
                if (!isComment(line)) {
                    int x = line.indexOf('=');
                    if (x < 0) {
                        result.put(line.trim(), "");
                    } else {
                        result.put(line.substring(0, x).trim(), line.substring(x + 1).trim());
                    }
                }
                return true;
            }
        });

        return result;
    }

    public interface LineHandler {
        /**
         * Handles a line.
         * 
         * @param line
         *            the line to handle
         * 
         * @return <code>true</code> if the next line should be processed,
         *         <code>false</code> if the processing should stop.
         */
        boolean handle(String line);
    }

    public abstract static class SkipHeaderLineHandler implements LineHandler {

        private boolean header = true;

        public boolean isHeaderLine(String line) {
            if (!header) {
                return false;
            }

            String trim = line.trim();
            if (trim.length() == 0) {
                header = false;
                return true;
            }

            char c = trim.charAt(0);
            return (c == '/') || (c == '*') || (c == '#');
        }
    }

    public abstract static class IgnoreCommentsLineHandler implements LineHandler {

        public boolean isComment(String line) {
            String trim = line.trim();
            if (trim.length() == 0) {
                return true;
            }

            return trim.charAt(0) == '#';
        }
    }
}
