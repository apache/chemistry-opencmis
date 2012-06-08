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
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.server.shared.HttpUtils;

public class POSTHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private final boolean isMultipart;
    private Map<String, String[]> parameters;
    private String filename;
    private String contentType;
    private BigInteger size;
    private InputStream stream;

    public POSTHttpServletRequestWrapper(HttpServletRequest request, File tempDir, int memoryThreshold,
            long maxContentSize) throws Exception {
        super(request);

        parameters = new HashMap<String, String[]>();

        // parse query string
        parseFormData(request.getQueryString());

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

    private void parseFormData(String data) throws Exception {
        if (data == null || data.length() < 3) {
            return;
        }

        String[] nameValuePairs = data.split("&");
        for (String nameValuePair : nameValuePairs) {
            int x = nameValuePair.indexOf('=');
            if (x > 0) {
                String name = URLDecoder.decode(nameValuePair.substring(0, x), "UTF-8");
                String value = (x == nameValuePair.length() - 1 ? "" : URLDecoder.decode(
                        nameValuePair.substring(x + 1), "UTF-8"));
                addParameter(name, value);
            }
        }
    }

    private void addParameter(String name, String value) {
        String[] values = parameters.get(name);

        if (values == null) {
            parameters.put(name, new String[] { value });
        } else {
            String[] newValues = new String[values.length + 1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[newValues.length - 1] = value;
            parameters.put(name, newValues);
        }
    }

    @Override
    public String getParameter(String name) {
        String[] values = parameters.get(name);
        if ((values == null) || (values.length == 0)) {
            return null;
        }

        return values[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameters;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameters.get(name);
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
