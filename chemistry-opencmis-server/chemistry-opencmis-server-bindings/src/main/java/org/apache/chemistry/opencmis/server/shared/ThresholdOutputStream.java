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
package org.apache.chemistry.opencmis.server.shared;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;

/**
 * An OutputStream that stores the data in main memory until it reaches a
 * threshold. If the threshold is passed the data is written to a temporary
 * file.
 * 
 * It it is important to close this OutputStream before
 * {@link #getInputStream()} is called or call {@link #destroy()} if the
 * InputStream isn't required!
 */
public class ThresholdOutputStream extends OutputStream {
    private static final int MAX_GROW = 10 * 1024 * 1024; // 10 MiB
    private static final int DEFAULT_THRESHOLD = 4 * 1024 * 1024; // 4 MiB

    private File tempDir;
    private int memoryThreshold;
    private long maxContentSize;

    private byte[] buf = null;
    private int bufSize = 0;
    private long size = 0;
    private File tempFile;
    private OutputStream tmpStream;

    /**
     * Constructor.
     * 
     * @param tempDir
     *            temp directory
     * @param memoryThreshold
     *            memory threshold in bytes
     * @param maxContentSize
     *            max size of the content in bytes (-1 to disable the check)
     */
    public ThresholdOutputStream(File tempDir, int memoryThreshold, long maxContentSize) {
        this(64 * 1024, tempDir, memoryThreshold, maxContentSize);
    }

    /**
     * Constructor.
     * 
     * @param initSize
     *            initial internal buffer size
     * @param tempDir
     *            temp directory
     * @param memoryThreshold
     *            memory threshold in bytes
     * @param maxContentSize
     *            max size of the content in bytes (-1 to disable the check)
     */
    public ThresholdOutputStream(int initSize, File tempDir, int memoryThreshold, long maxContentSize) {
        if (initSize < 0) {
            throw new IllegalArgumentException("Negative initial size: " + initSize);
        }

        this.tempDir = tempDir;
        this.memoryThreshold = (memoryThreshold < 0 ? DEFAULT_THRESHOLD : memoryThreshold);
        this.maxContentSize = maxContentSize;

        buf = new byte[initSize];
    }

    private void expand(int nextBufferSize) throws IOException {
        if (bufSize + nextBufferSize <= buf.length) {
            return;
        }

        if (bufSize + nextBufferSize > memoryThreshold) {
            if (tmpStream == null) {
                tempFile = File.createTempFile("opencmis", null, tempDir);
                tmpStream = new BufferedOutputStream(new FileOutputStream(tempFile));
            }
            tmpStream.write(buf, 0, bufSize);

            if (buf.length != memoryThreshold) {
                buf = new byte[memoryThreshold];
            }
            bufSize = 0;

            return;
        }

        int newSize = ((bufSize + nextBufferSize) * 2 < MAX_GROW ? (bufSize + nextBufferSize) * 2 : buf.length
                + nextBufferSize + MAX_GROW);
        byte[] newbuf = new byte[newSize];
        System.arraycopy(buf, 0, newbuf, 0, bufSize);
        buf = newbuf;
    }

    public long getSize() {
        return size;
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    @Override
    public void write(byte[] buffer, int offset, int len) throws IOException {
        try {
            if (len == 0) {
                return;
            }

            if ((maxContentSize > -1) && (size + len > maxContentSize)) {
                destroy();
                throw new CmisConstraintException("Content too big!");
            }

            expand(len);
            System.arraycopy(buffer, offset, buf, bufSize, len);
            bufSize += len;
            size += len;
        } catch (IOException ioe) {
            destroy();
            throw ioe;
        }
    }

    @Override
    public void write(int oneByte) throws IOException {
        try {
            if ((maxContentSize > -1) && (size + 1 > maxContentSize)) {
                destroy();
                throw new CmisConstraintException("Content too big!");
            }

            if (bufSize == buf.length) {
                expand(1);
            }

            buf[bufSize++] = (byte) oneByte;
            size++;
        } catch (IOException ioe) {
            destroy();
            throw ioe;
        }
    }

    @Override
    public void flush() throws IOException {
        if (tmpStream != null) {
            try {
                if (bufSize > 0) {
                    tmpStream.write(buf, 0, bufSize);
                    bufSize = 0;
                }
                tmpStream.flush();
            } catch (IOException ioe) {
                destroy();
                throw ioe;
            }
        }
    }

    @Override
    public void close() throws IOException {
        flush();

        if (tmpStream != null) {
            tmpStream.close();
        }
    }

    /**
     * Destroys the object before it has been read.
     */
    public void destroy() {
        try {
            close();
        } catch (Exception e) {
            // ignore
        }

        if (tempFile != null) {
            tempFile.delete();
        }

        buf = null;
    }

    /**
     * Returns the data as an InputStream.
     */
    public InputStream getInputStream() throws Exception {
        if (tmpStream != null) {
            close();
            buf = null;

            return new InternalTempFileInputStream();
        } else {
            return new InternalBufferInputStream();
        }
    }

    /**
     * Provides information about the input stream.
     */
    public interface ThresholdInputStream {

        /**
         * Returns <code>true</code> if the data is in memory. Returns
         * <code>false</code> if the data resides in a temporary file.
         */
        boolean isInMemory();

        /**
         * Returns the temporary file if the data stored in a file. Returns
         * <code>null</code> is the data is stored in memory.
         */
        File getTemporaryFile();

        /**
         * Returns content as a byte array if the data is stored in memory.
         * Returns <code>null</code> is the data is stored in a file.
         */
        byte[] getBytes();
    }

    /**
     * InputStream for in-memory data.
     */
    private class InternalBufferInputStream extends InputStream implements ThresholdInputStream {

        private int pos = 0;

        public boolean isInMemory() {
            return true;
        }

        public File getTemporaryFile() {
            return null;
        }

        public byte[] getBytes() {
            return buf;
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public int available() {
            return bufSize - pos;
        }

        @Override
        public int read() {
            return (pos < bufSize) && (buf != null) ? (buf[pos++] & 0xff) : -1;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) {
            if ((pos >= bufSize) || (buf == null)) {
                return -1;
            }

            if ((pos + len) > bufSize) {
                len = (bufSize - pos);
            }

            System.arraycopy(buf, pos, b, off, len);
            pos += len;

            return len;
        }

        @Override
        public long skip(long n) {
            if ((pos + n) > bufSize) {
                n = bufSize - pos;
            }

            if (n < 0) {
                return 0;
            }

            pos += n;

            return n;
        }

        @Override
        public void close() throws IOException {
            buf = null;
        }
    }

    /**
     * InputStream for file data.
     */
    private class InternalTempFileInputStream extends FilterInputStream implements ThresholdInputStream {

        private boolean isDeleted = false;

        public InternalTempFileInputStream() throws FileNotFoundException {
            super(new BufferedInputStream(new FileInputStream(tempFile), memoryThreshold));
        }

        public boolean isInMemory() {
            return false;
        }

        public File getTemporaryFile() {
            return tempFile;
        }

        public byte[] getBytes() {
            return null;
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();

            if (b == -1 && !isDeleted) {
                super.close();
                isDeleted = tempFile.delete();
            }

            return b;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = super.read(b, off, len);

            if (n == -1 && !isDeleted) {
                super.close();
                isDeleted = tempFile.delete();
            }

            return n;
        }

        @Override
        public void close() throws IOException {
            if (!isDeleted) {
                super.close();
                isDeleted = tempFile.delete();
            }
        }
    }
}
