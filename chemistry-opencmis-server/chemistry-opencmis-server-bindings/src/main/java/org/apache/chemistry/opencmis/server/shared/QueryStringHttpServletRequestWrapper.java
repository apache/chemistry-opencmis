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
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * HttpServletRequest wrapper that reads the query string in container
 * independent way and decodes the parameter values with UTF-8.
 */
public class QueryStringHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private Map<String, String[]> parameters;

    public QueryStringHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);

        parameters = new HashMap<String, String[]>();

        // parse query string
        parseFormData(request.getQueryString());
    }

    /**
     * Parses the query string.
     */
    protected void parseFormData(String queryString) throws IOException {
        if (queryString == null || queryString.length() < 3) {
            return;
        }

        String[] nameValuePairs = queryString.split("&");
        for (String nameValuePair : nameValuePairs) {
            int x = nameValuePair.indexOf('=');
            if (x > 0) {
                String name = URLDecoder.decode(nameValuePair.substring(0, x), "UTF-8");
                String value = (x == nameValuePair.length() - 1 ? "" : URLDecoder.decode(
                        nameValuePair.substring(x + 1), "UTF-8"));
                addParameter(name, value);
            } else {
                String name = URLDecoder.decode(nameValuePair, "UTF-8");
                addParameter(name, null);
            }
        }
    }

    /**
     * Adds a value to a parameter.
     */
    protected void addParameter(String name, String value) {
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
}
