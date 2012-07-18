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

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.server.shared.HttpUtils;
import org.apache.chemistry.opencmis.server.shared.QueryStringHttpServletRequestWrapper;

public class POSTHttpServletRequestWrapper extends QueryStringHttpServletRequestWrapper {
    private final boolean isMultipart;
    private String filename;
    private String contentType;
    private BigInteger size;
    private InputStream stream;

    public POSTHttpServletRequestWrapper(HttpServletRequest request, File tempDir, int memoryThreshold,
            long maxContentSize) throws Exception {
        super(request);

        // check multipart
        isMultipart = MultipartParser.isMultipartContent(request);

        if (isMultipart) {
            MultipartParser parser = new MultipartParser(request, tempDir, memoryThreshold, maxContentSize);

            while (parser.readNext()) {
                if (parser.isContent()) {
                    filename = parser.getFilename();
                    contentType = parser.getContentType();
                    size = parser.getSize();
                    stream = parser.getStream();
                } else {
                    addParameter(parser.getName(), parser.getValue());
                }
            }

            String filenameControl = HttpUtils.getStringParameter(this, Constants.CONTROL_FILENAME);
            if ((filenameControl) != null && (filenameControl.trim().length() > 0)) {
                filename = filenameControl;
            }

            String contentTypeControl = HttpUtils.getStringParameter(this, Constants.CONTROL_CONTENT_TYPE);
            if ((contentTypeControl != null) && (contentTypeControl.trim().length() > 0)) {
                contentType = contentTypeControl;
            }
        } else {
            // form data processing
            StringBuilder sb = new StringBuilder();

            InputStreamReader sr = new InputStreamReader(request.getInputStream(), "UTF-8");
            char[] buffer = new char[4096];
            int c = 0;
            while ((c = sr.read(buffer)) > -1) {
                sb.append(buffer, 0, c);
            }

            parseFormData(sb.toString());
        }
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public BigInteger getSize() {
        return size;
    }

    public InputStream getStream() {
        return stream;
    }
}
