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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.impl.CallContextImpl;

/**
 * Utility methods that are used by the AtomPub and Browser binding.
 */
public class HttpUtils {

    private HttpUtils() {
    }

    /**
     * Creates a {@link CallContext} object from a servlet request.
     */
    public static CallContext createContext(HttpServletRequest request, HttpServletResponse response,
            ServletContext servletContext, String binding, CallContextHandler callContextHandler, File tempDir,
            int memoryThreshold, long maxContentSize) {
        String[] pathFragments = splitPath(request);

        String repositoryId = null;
        if (pathFragments.length > 0) {
            repositoryId = pathFragments[0];
        }

        CallContextImpl context = new CallContextImpl(binding, repositoryId,
                CallContext.BINDING_ATOMPUB.equals(binding));

        // call call context handler
        if (callContextHandler != null) {
            Map<String, String> callContextMap = callContextHandler.getCallContextMap(request);
            if (callContextMap != null) {
                for (Map.Entry<String, String> e : callContextMap.entrySet()) {
                    context.put(e.getKey(), e.getValue());
                }
            }
        }

        // servlet context and HTTP servlet request and response
        context.put(CallContext.SERVLET_CONTEXT, servletContext);
        context.put(CallContext.HTTP_SERVLET_REQUEST, request);
        context.put(CallContext.HTTP_SERVLET_RESPONSE, response);

        // content
        context.put(CallContext.TEMP_DIR, tempDir);
        context.put(CallContext.MEMORY_THRESHOLD, memoryThreshold);
        context.put(CallContext.MAX_CONTENT_SIZE, maxContentSize);

        // decode range
        String rangeHeader = request.getHeader("Range");
        if (rangeHeader != null) {
            rangeHeader = rangeHeader.trim().toLowerCase(Locale.ENGLISH);

            if (rangeHeader.length() > 6 && rangeHeader.startsWith("bytes=") && rangeHeader.indexOf(',') == -1
                    && rangeHeader.charAt(6) != '-') {
                BigInteger offset = null;
                BigInteger length = null;

                int ds = rangeHeader.indexOf('-');
                if (ds > 6) {
                    try {
                        String firstBytePosStr = rangeHeader.substring(6, ds);
                        if (firstBytePosStr.length() > 0) {
                            offset = new BigInteger(firstBytePosStr);
                        }

                        if (!rangeHeader.endsWith("-")) {
                            String lastBytePosStr = rangeHeader.substring(ds + 1);
                            if (offset == null) {
                                length = (new BigInteger(lastBytePosStr)).add(BigInteger.ONE);
                            } else {
                                length = (new BigInteger(lastBytePosStr)).subtract(offset).add(BigInteger.ONE);
                            }
                        }

                        if (offset != null) {
                            context.put(CallContext.OFFSET, offset);
                        }
                        if (length != null) {
                            context.put(CallContext.LENGTH, length);
                        }
                    } catch (NumberFormatException e) {
                        // invalid Range header must be ignored
                    }
                }
            }
        }

        // get locale
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null) {
            String[] locale = acceptLanguage.split("-");
            context.put(CallContext.LOCALE_ISO639_LANGUAGE, locale[0].trim());
            if (locale.length > 1) {
                int x = locale[1].indexOf(',');
                if (x == -1) {
                    context.put(CallContext.LOCALE_ISO3166_COUNTRY, locale[1].trim());
                } else {
                    context.put(CallContext.LOCALE_ISO3166_COUNTRY, locale[1].substring(0, x).trim());
                }
            }
        }

        return context;
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
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // --- parameters ---
    // -------------------------------------------------------------------------

    /**
     * Extracts a string parameter.
     */
    @SuppressWarnings("unchecked")
    public static String getStringParameter(HttpServletRequest request, String name) {
        if (name == null) {
            return null;
        }

        Map<String, String[]> parameters = (Map<String, String[]>) request.getParameterMap();
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
     * Extracts a boolean parameter (with default).
     */
    public static boolean getBooleanParameter(HttpServletRequest request, String name, boolean def) {
        String value = getStringParameter(request, name);
        if ((value == null) || (value.length() == 0)) {
            return def;
        }

        return Boolean.valueOf(value);
    }

    /**
     * Extracts a boolean parameter.
     */
    public static Boolean getBooleanParameter(HttpServletRequest request, String name) {
        String value = getStringParameter(request, name);
        if ((value == null) || (value.length() == 0)) {
            return null;
        }

        return Boolean.valueOf(value);
    }

    /**
     * Extracts an integer parameter (with default).
     */
    public static BigInteger getBigIntegerParameter(HttpServletRequest request, String name, long def) {
        BigInteger result = getBigIntegerParameter(request, name);
        if (result == null) {
            result = BigInteger.valueOf(def);
        }

        return result;
    }

    /**
     * Extracts an integer parameter.
     */
    public static BigInteger getBigIntegerParameter(HttpServletRequest request, String name) {
        String value = getStringParameter(request, name);
        if ((value == null) || (value.length() == 0)) {
            return null;
        }

        try {
            return new BigInteger(value);
        } catch (Exception e) {
            throw new CmisInvalidArgumentException("Invalid parameter '" + name + "'!");
        }
    }

    /**
     * Extracts an enum parameter.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getEnumParameter(HttpServletRequest request, String name, Class<T> clazz) {
        String value = getStringParameter(request, name);
        if ((value == null) || (value.length() == 0)) {
            return null;
        }

        try {
            Method m = clazz.getMethod("fromValue", new Class[] { String.class });
            return (T) m.invoke(null, new Object[] { value });
        } catch (Exception e) {
            if (e instanceof InvocationTargetException && e.getCause() instanceof IllegalArgumentException) {
                throw new CmisInvalidArgumentException("Invalid parameter '" + name + "'!");
            }

            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }
}
