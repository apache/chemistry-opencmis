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
import java.util.Map;

import org.apache.chemistry.opencmis.commons.server.CallContext;

/**
 * Implementation of the {@link CallContext} interface.
 */
public class CallContextImpl implements CallContext {

    private final String binding;
    private final boolean objectInfoRequired;
    private final Map<String, Object> parameter = new HashMap<String, Object>();

    public CallContextImpl(String binding, String repositoryId, boolean objectInfoRequired) {
        this.binding = binding;
        this.objectInfoRequired = objectInfoRequired;
        put(REPOSITORY_ID, repositoryId);
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

    public int getMemoryThreshold() {
        return (Integer) get(MEMORY_THRESHOLD);
    }

    public long getMaxContentSize() {
        return (Long) get(MAX_CONTENT_SIZE);
    }

    /**
     * Adds a parameter.
     */
    public void put(String key, Object value) {
        parameter.put(key, value);
    }

    /**
     * Removes a parameter.
     */
    public Object remove(String key) {
        return parameter.remove(key);
    }
}
