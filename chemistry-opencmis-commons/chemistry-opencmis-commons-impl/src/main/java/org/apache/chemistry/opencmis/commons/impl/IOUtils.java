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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.chemistry.opencmis.commons.data.ContentStream;

public class IOUtils {

    /**
     * Closes a stream and ignores any exceptions.
     */
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
    public static void consumeAndClose(final InputStream stream) {
        try {
            byte[] buffer = new byte[4096];
            while (stream.read(buffer) > -1) {
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
    public static void consumeAndClose(final Reader reader) {
        try {
            char[] buffer = new char[4096];
            while (reader.read(buffer) > -1) {
            }
        } catch (IOException e) {
            // ignore
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }
}
