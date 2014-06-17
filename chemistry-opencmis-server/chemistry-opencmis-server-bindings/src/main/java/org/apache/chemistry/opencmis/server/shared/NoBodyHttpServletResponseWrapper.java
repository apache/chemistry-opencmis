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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * A response wrapper that includes no body, i.e., that just 
 * swallows the body, counting the bytes in order to set
 * the content length appropriately.
 */
public final class NoBodyHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private HttpServletResponse resp;
    private NoBodyOutputStream noBody;
    private PrintWriter writer;
    private boolean didSetContentLength;

    public NoBodyHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
        
        resp = (HttpServletResponse) super.getResponse();
        noBody = new NoBodyOutputStream();
    }

    public void setContentLength() {
        if (!didSetContentLength) {
            resp.setContentLength(noBody.getContentLength());
        }
    }

    @Override
    public void setContentLength(int len) {
        resp.setContentLength(len);
        didSetContentLength = true;
    }
    
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return noBody;
    }
    
    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            OutputStreamWriter w = new OutputStreamWriter(noBody, getCharacterEncoding());
            writer = new PrintWriter(w);
        }
        return writer;
    }
    

    /**
     * @See javax.servlet.HttpServlet.NoBodyOutputStream
     */
    class NoBodyOutputStream extends ServletOutputStream {

        private int contentLength = 0;

        public NoBodyOutputStream() {
        }

        int getContentLength() {
            return contentLength;
        }

        @Override
        public void write(int b) {
            contentLength++;
        }

        @Override
        public void write(byte buf[], int offset, int len) throws IOException {
            if (len >= 0) {
                contentLength += len;
            } else {
                throw new IOException("negative length");
            }
        }
    }

}
