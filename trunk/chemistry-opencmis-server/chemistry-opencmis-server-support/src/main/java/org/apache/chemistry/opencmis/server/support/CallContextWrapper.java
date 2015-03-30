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
package org.apache.chemistry.opencmis.server.support;

import java.io.File;
import java.math.BigInteger;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.server.CallContext;

/**
 * Provides a convenient implementation of the {@link CallContext} interface that can be
 * subclassed by developers wishing to change, add, or hide call context data.
 * 
 * This class implements the Wrapper or Decorator pattern. Methods default to
 * calling through to the wrapped request object.
 */
public class CallContextWrapper implements CallContext {

    private final CallContext context;

    public CallContextWrapper(CallContext context) {
        this.context = context;
    }

    public String getBinding() {
        return context.getBinding();
    }

    public boolean isObjectInfoRequired() {
        return context.isObjectInfoRequired();
    }

    public Object get(String key) {
        return context.get(key);
    }

    public CmisVersion getCmisVersion() {
        return context.getCmisVersion();
    }

    public String getRepositoryId() {
        return context.getRepositoryId();
    }

    public String getUsername() {
        return context.getUsername();
    }

    public String getPassword() {
        return context.getPassword();
    }

    public String getLocale() {
        return context.getLocale();
    }

    public BigInteger getOffset() {
        return context.getOffset();
    }

    public BigInteger getLength() {
        return context.getLength();
    }

    public File getTempDirectory() {
        return context.getTempDirectory();
    }

    public boolean encryptTempFiles() {
        return context.encryptTempFiles();
    }

    public int getMemoryThreshold() {
        return context.getMemoryThreshold();
    }

    public long getMaxContentSize() {
        return context.getMaxContentSize();
    }
}
