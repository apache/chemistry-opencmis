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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/*
 * @See javax.servlet.http.HttpServlet.NoBodyResponse
 * 
 * A response wrapper for use in (dumb) "HEAD" support.
 * This just swallows that body, counting the bytes in order to set
 * the content length appropriately. All other methods delegate to the
 * wrapped HTTP Servlet Response object.
 */
public class NoBodyHttpServletResponseWrapper extends
        HttpServletResponseWrapper {
    private NoBodyOutputStream noBody;
    private PrintWriter writer;
    private boolean didSetContentLength;

    public NoBodyHttpServletResponseWrapper(HttpServletResponse r) {
        super(r);
        noBody = new NoBodyOutputStream();
    }

    public void setContentLength() {
        if (!didSetContentLength) {
            if (writer != null) {
                writer.flush();
            }
            super.setContentLength(noBody.getContentLength());
        }
    }

    // SERVLET RESPONSE interface methods

    @Override
    public void setContentLength(int len) {
        super.setContentLength(len);
        didSetContentLength = true;
    }

    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        checkHeader(name);
    }

    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        checkHeader(name);
    }

    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
        checkHeader(name);
    }

    @Override
    public void addIntHeader(String name, int value) {
        super.addIntHeader(name, value);
        checkHeader(name);
    }

    private void checkHeader(String name) {
        if ("content-length".equalsIgnoreCase(name)) {
            didSetContentLength = true;
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return noBody;
    }

    @Override
    public PrintWriter getWriter() throws UnsupportedEncodingException {

        if (writer == null) {
            OutputStreamWriter w;

            w = new OutputStreamWriter(noBody, getCharacterEncoding());
            writer = new PrintWriter(w);
        }
        return writer;
    }

    /*
     * Servlet output stream that gobbles up all its data.
     */

    // file private
    static class NoBodyOutputStream extends ServletOutputStream {

/*        
        private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";
        private static ResourceBundle lStrings = ResourceBundle
                .getBundle(LSTRING_FILE);
*/
        private static ResourceBundle lStrings = new ListResourceBundle() {
            protected Object[][] getContents() {
                return new Object[][] {
                    {"err.io.nullArray", "Null passed for byte array in write method"},
                    {"err.io.indexOutOfBounds", "Invalid offset [{0}] and / or " +
                            "length [{1}] specified for array of size [{2}]"}
                };
            }
        };

        private int contentLength = 0;

        // file private
        NoBodyOutputStream() {
            // NOOP
        }

        // file private
        int getContentLength() {
            return contentLength;
        }

        @Override
        public void write(int b) {
            contentLength++;
        }

        @Override
        public void write(byte buf[], int offset, int len) throws IOException {
            if (buf == null) {
                throw new NullPointerException(
                        lStrings.getString("err.io.nullArray"));
            }

            if (offset < 0 || len < 0 || offset + len > buf.length) {
                String msg = lStrings.getString("err.io.indexOutOfBounds");
                Object[] msgArgs = new Object[3];
                msgArgs[0] = Integer.valueOf(offset);
                msgArgs[1] = Integer.valueOf(len);
                msgArgs[2] = Integer.valueOf(buf.length);
                msg = MessageFormat.format(msg, msgArgs);
                throw new IndexOutOfBoundsException(msg);
            }

            contentLength += len;
        }
    }
}