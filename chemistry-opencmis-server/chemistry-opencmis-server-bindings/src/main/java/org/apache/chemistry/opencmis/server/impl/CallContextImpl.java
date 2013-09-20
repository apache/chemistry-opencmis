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
package org.apache.chemistry.opencmis.server.impl;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.commons.server.MutableCallContext;
import org.apache.chemistry.opencmis.server.shared.ThresholdOutputStreamFactory;

/**
 * Implementation of the {@link CallContext} interface.
 */
public class CallContextImpl implements MutableCallContext {

    private final String binding;
    private final boolean objectInfoRequired;
    private final Map<String, Object> parameter = new HashMap<String, Object>();

    public CallContextImpl(String binding, CmisVersion cmisVersion, String repositoryId, ServletContext servletContext,
            HttpServletRequest request, HttpServletResponse response, CmisServiceFactory factory,
            ThresholdOutputStreamFactory streamFactory) {
        this.binding = binding;
        this.objectInfoRequired = BINDING_ATOMPUB.equals(binding);
        put(REPOSITORY_ID, repositoryId);

        // CMIS version
        put(CallContext.CMIS_VERSION, cmisVersion);

        // servlet context and HTTP servlet request and response
        put(CallContext.SERVLET_CONTEXT, servletContext);
        put(CallContext.HTTP_SERVLET_REQUEST, request);
        put(CallContext.HTTP_SERVLET_RESPONSE, response);

        if (streamFactory != null) {
            put(TEMP_DIR, streamFactory.getTempDir());
            put(MEMORY_THRESHOLD, streamFactory.getMemoryThreshold());
            put(MAX_CONTENT_SIZE, streamFactory.getMaxContentSize());
            put(ENCRYPT_TEMP_FILE, streamFactory.isEncrypted());
            put(STREAM_FACTORY, streamFactory);
        } else if (factory != null) {
            put(TEMP_DIR, factory.getTempDirectory());
            put(MEMORY_THRESHOLD, factory.getMemoryThreshold());
            put(MAX_CONTENT_SIZE, -1);
            put(ENCRYPT_TEMP_FILE, false);
        }
    }

    public void setRange(String rangeHeader) {
        if (rangeHeader == null) {
            return;
        }

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
                        put(OFFSET, offset);
                    }
                    if (length != null) {
                        put(LENGTH, length);
                    }
                } catch (NumberFormatException e) {
                    // invalid Range header must be ignored
                }
            }
        }
    }

    public void setAcceptLanguage(String acceptLanguageHeader) {
        if (acceptLanguageHeader == null) {
            return;
        }

        String[] locale = acceptLanguageHeader.split("-");
        put(LOCALE_ISO639_LANGUAGE, locale[0].trim());
        if (locale.length > 1) {
            int x = locale[1].indexOf(',');
            if (x == -1) {
                put(LOCALE_ISO3166_COUNTRY, locale[1].trim());
            } else {
                put(LOCALE_ISO3166_COUNTRY, locale[1].substring(0, x).trim());
            }
        }
    }

    public String getBinding() {
        return binding;
    }

    public boolean isObjectInfoRequired() {
        return objectInfoRequired;
    }

    public Object get(String key) {
        return parameter.get(key);
    }

    public CmisVersion getCmisVersion() {
        return (CmisVersion) get(CMIS_VERSION);
    }

    public String getRepositoryId() {
        return (String) get(REPOSITORY_ID);
    }

    public String getUsername() {
        return (String) get(USERNAME);
    }

    public String getPassword() {
        return (String) get(PASSWORD);
    }

    public String getLocale() {
        return (String) get(LOCALE);
    }

    public BigInteger getOffset() {
        return (BigInteger) get(OFFSET);
    }

    public BigInteger getLength() {
        return (BigInteger) get(LENGTH);
    }

    public File getTempDirectory() {
        return (File) get(TEMP_DIR);
    }

    public boolean encryptTempFiles() {
        return Boolean.TRUE.equals(get(ENCRYPT_TEMP_FILE));
    }

    public int getMemoryThreshold() {
        return (Integer) get(MEMORY_THRESHOLD);
    }

    public long getMaxContentSize() {
        return (Long) get(MAX_CONTENT_SIZE);
    }

    public final void put(String key, Object value) {
        parameter.put(key, value);
    }

    public final Object remove(String key) {
        return parameter.remove(key);
    }
}
