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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

public class HttpUtils {

    /**
     * Extracts a string parameter.
     */
    public static String getStringParameter(final HttpServletRequest request, final String name) {
        if (name == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, String[]> parameters = request.getParameterMap();
        for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
            if (name.equalsIgnoreCase(parameter.getKey())) {
                if (parameter.getValue() == null) {
                    return null;
                }
                return parameter.getValue()[0];
            }
        }

        return null;
    }

    /**
     * Splits the path into its fragments.
     */
    public static String[] splitPath(final HttpServletRequest request) {
        int prefixLength = request.getContextPath().length() + request.getServletPath().length();
        String p = request.getRequestURI().substring(prefixLength);

        if (p.length() == 0) {
            return new String[0];
        }

        String[] result = p.substring(1).split("/");
        for (int i = 0; i < result.length; i++) {
            try {
                result[i] = URLDecoder.decode(result[i], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // should not happen
                throw new CmisRuntimeException("Unsupported encoding 'UTF-8'", e);
            }
        }

        return result;
    }
}
