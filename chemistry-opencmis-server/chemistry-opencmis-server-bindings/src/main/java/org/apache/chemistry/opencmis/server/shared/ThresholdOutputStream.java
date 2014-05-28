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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;

import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(ThresholdOutputStream.class);

    private static final int MAX_GROW = 10 * 1024 * 1024; // 10 MiB
    private static final int DEFAULT_THRESHOLD = 4 * 1024 * 1024; // 4 MiB

    private static final String ALGORITHM = "AES";
    private static final String MODE = "CTR";
    private static final String PADDING = "PKCS5Padding";
    private static final String TRANSFORMATION = ALGORITHM + '/' + MODE + '/' + PADDING;
    private static final int KEY_SIZE = 128;

    private final File tempDir;
    private final int memoryThreshold;
    private final long maxContentSize;
    private final boolean encrypt;

    private byte[] buf = null;
    private int bufSize = 0;
    private long size = 0;
    private File tempFile;
    private OutputStream tmpStream;
    private Key key;
    private byte[] iv;

    /**
     * Constructor.
     * 
     * @param tempDir
     *            temp directory or {@code null} for the default temp directory
     * @param memoryThreshold
     *            memory threshold in bytes
     * @param maxContentSize
     *            max size of the content in bytes (-1 to disable the check)
     */
    public ThresholdOutputStream(File tempDir, int memoryThreshold, long maxContentSize) {
        this(64 * 1024, tempDir, memoryThreshold, maxContentSize, false);
    }

    /**
     * Constructor.
     * 
     * @param tempDir
     *            temp directory or {@code null} for the default temp directory
     * @param memoryThreshold
     *            memory threshold in bytes
     * @param maxContentSize
     *            max size of the content in bytes (-1 to disable the check)
     */
    public ThresholdOutputStream(File tempDir, int memoryThreshold, long maxContentSize, boolean encrypt) {
        this(64 * 1024, tempDir, memoryThreshold, maxContentSize, encrypt);
    }

    /**
     * Constructor.
     * 
     * @param initSize
     *            initial internal buffer size
     * @param tempDir
     *            temp directory or {@code null} for the default temp directory
     * @param memoryThreshold
     *            memory threshold in bytes
     * @param maxContentSize
     *            max size of the content in bytes (-1 to disable the check)
     * @param encrypt
     *            indicates if temporary files must be encrypted
     */
    public ThresholdOutputStream(int initSize, File tempDir, int memoryThreshold, long maxContentSize, boolean encrypt) {
        if (initSize < 0) {
            throw new IllegalArgumentException("Negative initial size: " + initSize);
        }

        this.tempDir = tempDir;
        this.memoryThreshold = (memoryThreshold < 0 ? DEFAULT_THRESHOLD : memoryThreshold);
        this.maxContentSize = maxContentSize;
        this.encrypt = encrypt;

        buf = new byte[initSize];
    }

    private void expand(int nextBufferSize) throws IOException {
        if (bufSize + nextBufferSize <= buf.length) {
            return;
        }

        if (bufSize + nextBufferSize > memoryThreshold) {
            if (tmpStream == null) {
                openTempFile();
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

    private void openTempFile() throws IOException {
        tempFile = File.createTempFile("opencmis", null, tempDir);

        if (encrypt) {
            Cipher cipher;
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
                keyGenerator.init(KEY_SIZE);
                key = keyGenerator.generateKey();

                cipher = Cipher.getInstance(TRANSFORMATION);
                cipher.init(Cipher.ENCRYPT_MODE, key);

                iv = cipher.getIV();
            } catch (Exception e) {

                if (LOG.isErrorEnabled()) {
                    LOG.error("Cannot initialize encryption cipher: {}", e.toString(), e);
                }

                throw new IOException("Cannot initialize encryption cipher!", e);
            }

            tmpStream = new BufferedOutputStream(new CipherOutputStream(new FileOutputStream(tempFile), cipher));
        } else {
            tmpStream = new BufferedOutputStream(new FileOutputStream(tempFile));
        }
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

            if (LOG.isErrorEnabled()) {
                if (tempFile != null) {
                    LOG.error("Writing to temp file {} failed: {}", tempFile.getAbsolutePath(), ioe.toString(), ioe);
                } else {
                    LOG.error("Writing to temp buffer failed: {}", ioe.toString(), ioe);
                }
            }

            throw ioe;
        }
    }

    @Override
    public void write(int oneByte) throws IOException {
        try {
            if (maxContentSize > -1 && size + 1 > maxContentSize) {
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

            if (LOG.isErrorEnabled()) {
                if (tempFile != null) {
                    LOG.error("Writing to temp file {} failed: {}", tempFile.getAbsolutePath(), ioe.toString(), ioe);
                } else {
                    LOG.error("Writing to temp buffer failed: {}", ioe.toString(), ioe);
                }
            }

            throw ioe;
        }
    }

    @Override
    public void flush() throws IOException {
        if (tmpStream == null && memoryThreshold < bufSize) {
            openTempFile();
        }

        if (tmpStream != null) {
            try {
                if (bufSize > 0) {
                    tmpStream.write(buf, 0, bufSize);
                    bufSize = 0;
                }
                tmpStream.flush();
            } catch (IOException ioe) {
                destroy();

                if (LOG.isErrorEnabled()) {
                    LOG.error("Flushing the temp file {} failed: {}", tempFile.getAbsolutePath(), ioe.toString(), ioe);
                }

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
            if (tmpStream != null) {
                tmpStream.flush();
                tmpStream.close();
            }
        } catch (Exception e) {
            // ignore
        }

        if (tempFile != null) {
            boolean isDeleted = tempFile.delete();
            if (!isDeleted) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Temp file {} could not be deleted!", tempFile.getAbsolutePath());
                }
            }
        }

        buf = null;
    }

    /**
     * Returns the data as an InputStream.
     */
    public InputStream getInputStream() throws IOException {
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
    public abstract class ThresholdInputStream extends InputStream {

        /**
         * Returns if the data is stored in memory.
         * 
         * @return {@code true} if the data is in memory and {@code false} if
         *         the data resides in a temporary file
         */
        public abstract boolean isInMemory();

        /**
         * Gets the temporary file.
         * 
         * @return the temporary file or {@code null} if the data is stored in
         *         memory
         */
        public File getTemporaryFile() {
            return null;
        }

        /**
         * Gets the byte buffer.
         * 
         * This the underlying byte buffer and might be bigger than then the
         * total length of the stream.
         * 
         * @return the content in a byte array or {@code null} if the data is
         *         stored in a file
         */
        public byte[] getBytes() {
            return null;
        }

        /**
         * Returns the length of the stream.
         * 
         * @return the length of the stream in bytes
         */
        public long length() {
            return size;
        }

        /**
         * Rewinds the stream so that it can be read from the beginning.
         */
        public abstract void rewind() throws IOException;
    }

    /**
     * InputStream for in-memory data.
     */
    private class InternalBufferInputStream extends ThresholdInputStream {

        private int pos = 0;
        private int mark = -1;

        @Override
        public boolean isInMemory() {
            return true;
        }

        @Override
        public byte[] getBytes() {
            if (buf == null) {
                throw new IllegalStateException("Stream is already closed!");
            }

            return buf;
        }

        @Override
        public void rewind() throws IOException {
            if (buf == null) {
                throw new IOException("Stream is already closed!");
            }

            pos = 0;
            mark = -1;
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public void mark(int readlimit) {
            if (buf != null) {
                mark = pos;
            }
        }

        @Override
        public void reset() throws IOException {
            if (mark < 0) {
                throw new IOException("Reset not possible.");
            }

            pos = mark;
        }

        @Override
        public int available() {
            if (buf == null) {
                return 0;
            }

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

            if (len == 0) {
                return 0;
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
            if (buf == null) {
                return -1;
            }

            if (n <= 0) {
                return 0;
            }

            if ((pos + n) > bufSize) {
                n = bufSize - pos;
            }

            pos += n;

            return n;
        }

        @Override
        public void close() throws IOException {
            buf = null;
            mark = -1;
        }
    }

    /**
     * InputStream for temp file data.
     * 
     * Call {@link #close()} to delete the temp file.
     */
    private class InternalTempFileInputStream extends ThresholdInputStream {

        private final Cipher cipher;
        private BufferedInputStream stream;
        private boolean isDeleted = false;
        private boolean isClosed = false;

        public InternalTempFileInputStream() throws IOException {

            if (encrypt) {
                try {
                    cipher = Cipher.getInstance(TRANSFORMATION);
                    cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
                } catch (Exception e) {
                    delete();

                    if (LOG.isErrorEnabled()) {
                        LOG.error("Cannot initialize decryption cipher: {}", e.toString(), e);
                    }

                    throw new IOException("Cannot initialize decryption cipher!", e);
                }
            } else {
                cipher = null;
            }

            openStream();
        }

        /**
         * Opens the temp file stream.
         */
        protected void openStream() throws FileNotFoundException {
            int bufferSize = (memoryThreshold < 4 * 1024 ? 4 * 1024 : memoryThreshold);

            if (encrypt) {
                stream = new BufferedInputStream(new CipherInputStream(new FileInputStream(tempFile), cipher),
                        bufferSize);
            } else {
                stream = new BufferedInputStream(new FileInputStream(tempFile), bufferSize);
            }
        }

        @Override
        public boolean isInMemory() {
            return false;
        }

        @Override
        public File getTemporaryFile() {
            if (isDeleted) {
                throw new IllegalStateException("Temporary file is already deleted!");
            }

            return tempFile;
        }

        @Override
        public void rewind() throws IOException {
            if (isClosed) {
                throw new IOException("Stream is already closed!");
            }

            stream.close();

            openStream();
        }

        @Override
        public int available() throws IOException {
            if (isClosed) {
                return 0;
            }

            return stream.available();
        }

        @Override
        public boolean markSupported() {
            return stream.markSupported();
        }

        @Override
        public void mark(int readlimit) {
            if (!isClosed) {
                stream.mark(readlimit);
            }
        }

        @Override
        public void reset() throws IOException {
            if (isClosed) {
                throw new IOException("Stream is already closed!");
            }

            stream.reset();
        }

        @Override
        public long skip(long n) throws IOException {
            if (isClosed) {
                return -1;
            }

            return stream.skip(n);
        }

        @Override
        public int read() throws IOException {
            if (isClosed) {
                return -1;
            }

            int b = stream.read();

            return b;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (isClosed) {
                return -1;
            }

            int n = super.read(b, off, len);

            return n;
        }

        @Override
        public void close() throws IOException {
            delete();
        }

        /**
         * Closes the temp file stream and then deletes the temp file.
         */
        protected void delete() {
            if (!isClosed) {
                try {
                    stream.close();
                    isClosed = true;
                } catch (Exception e) {
                    // ignore
                }
            }

            if (!isDeleted) {
                isDeleted = tempFile.delete();
                if (!isDeleted) {
                    LOG.warn("Temp file {} could not be deleted!", tempFile.getAbsolutePath());
                }
            }
        }
    }
}
