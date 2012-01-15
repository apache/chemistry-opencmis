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

import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.server.shared.HttpUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class POSTHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private final boolean isMultipart;
    private Map<String, String[]> parameters;
    private String filename;
    private String contentType;
    private long size;
    private InputStream stream;

    public POSTHttpServletRequestWrapper(HttpServletRequest request) throws Exception {
        this(request, 4 * 1024 * 1024);
    }

    public POSTHttpServletRequestWrapper(HttpServletRequest request, int memoryThreshold) throws Exception {
        super(request);

        isMultipart = ServletFileUpload.isMultipartContent(request);

        if (isMultipart) {
            parameters = new HashMap<String, String[]>();

            String query = request.getQueryString();
            if (query != null) {
                String[] nameValuePairs = query.split("&");
                for (String nameValuePair : nameValuePairs) {
                    if (nameValuePair.length() > Constants.PARAM_OBJECT_ID.length()
                            && nameValuePair.toLowerCase().startsWith(Constants.PARAM_OBJECT_ID.toLowerCase())) {
                        int x = nameValuePair.indexOf('=');
                        if (x > -1 && x < nameValuePair.length() - 1) {
                            String objectId = nameValuePair.substring(x + 1);
                            parameters.put(Constants.PARAM_OBJECT_ID, new String[] { objectId });
                            break;
                        }
                    }
                }
            }

            DiskFileItemFactory itemFactory = new DiskFileItemFactory();
            itemFactory.setSizeThreshold(memoryThreshold);

            ServletFileUpload upload = new ServletFileUpload(itemFactory);
            @SuppressWarnings("unchecked")
            List<FileItem> fileItems = upload.parseRequest(request);

            for (FileItem item : fileItems) {
                if (item.isFormField()) {
                    String[] values = parameters.get(item.getFieldName());

                    if (values == null) {
                        parameters.put(item.getFieldName(), new String[] { item.getString() });
                    } else {
                        String[] newValues = new String[values.length + 1];
                        System.arraycopy(values, 0, newValues, 0, values.length);
                        newValues[newValues.length - 1] = item.getString();
                        parameters.put(item.getFieldName(), newValues);
                    }
                } else {
                    filename = item.getName();
                    contentType = (item.getContentType() == null ? Constants.MEDIATYPE_OCTETSTREAM : item
                            .getContentType());
                    size = item.getSize();
                    stream = item.getInputStream();
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
        }
    }

    @Override
    public String getParameter(String name) {
        if (!isMultipart) {
            return super.getParameter(name);
        }

        String[] values = parameters.get(name);
        if ((values == null) || (values.length == 0)) {
            return null;
        }

        return values[0];
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String[]> getParameterMap() {
        if (!isMultipart) {
            return super.getParameterMap();
        }

        return parameters;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration<String> getParameterNames() {
        if (!isMultipart) {
            return super.getParameterNames();
        }

        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        if (!isMultipart) {
            return super.getParameterValues(name);
        }

        return parameters.get(name);
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public InputStream getStream() {
        return stream;
    }
}
