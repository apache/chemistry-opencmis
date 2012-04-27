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
package org.apache.chemistry.opencmis.commons.server;

import java.io.File;
import java.math.BigInteger;

/**
 * An object implementing this interface holds context data of the current call.
 */
public interface CallContext {

    String BINDING_ATOMPUB = "atompub";
    String BINDING_WEBSERVICES = "webservices";
    String BINDING_BROWSER = "browser";
    String BINDING_LOCAL = "local";

    String REPOSITORY_ID = "repositoryId";
    String USERNAME = "username";
    String PASSWORD = "password";
    String LOCALE = "locale";
    String OFFSET = "offset";
    String LENGTH = "length";
    String LOCALE_ISO639_LANGUAGE = "language";
    String LOCALE_ISO3166_COUNTRY = "country";

    String SERVLET_CONTEXT = "servletContext";
    String HTTP_SERVLET_REQUEST = "httpServletRequest";
    String HTTP_SERVLET_RESPONSE = "httpServletResponse";

    String TEMP_DIR = "tempDir";
    String MEMORY_THRESHOLD = "memoryThreshold";
    String MAX_CONTENT_SIZE = "maxContentSize";

    /**
     * Returns the binding. Usually it returns
     * {@link CallContext#BINDING_ATOMPUB},
     * {@link CallContext#BINDING_WEBSERVICES},
     * {@link CallContext#BINDING_BROWSER} or {@link CallContext#BINDING_LOCAL}.
     */
    String getBinding();

    /**
     * Returns if <code>true</code> object infos can improve the performance.
     */
    boolean isObjectInfoRequired();

    /**
     * Returns context data by key.
     * 
     * @param key
     *            the key
     * @return the data if the key is valid, <code>null</code> otherwise
     */
    Object get(String key);

    /**
     * Returns the repository id.
     */
    String getRepositoryId();

    /**
     * Returns the user name.
     */
    String getUsername();

    /**
     * Returns the password.
     */
    String getPassword();

    /**
     * Returns the locale.
     */
    String getLocale();

    /**
     * Returns the content offset if set, <code>null</code> otherwise
     */
    BigInteger getOffset();

    /**
     * Returns the content length if set, <code>null</code> otherwise
     */
    BigInteger getLength();

    /**
     * Returns the temp directory.
     */
    File getTempDirectory();

    /**
     * Returns the memory threshold.
     */
    int getMemoryThreshold();

    /**
     * Returns the may size of content.
     */
    long getMaxContentSize();
}
