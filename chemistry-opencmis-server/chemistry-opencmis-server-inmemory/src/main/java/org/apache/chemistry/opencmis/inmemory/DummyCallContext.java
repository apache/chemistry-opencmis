package org.apache.chemistry.opencmis.inmemory;
/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */


import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.server.CallContext;

public class DummyCallContext implements CallContext {
    private Map<String, String> fParameter = new HashMap<String, String>();

    public DummyCallContext() {
        fParameter.put(USERNAME, "TestUser");
        fParameter.put(PASSWORD, "secret");
        fParameter.put(LOCALE, "en");
    }

    public boolean isObjectInfoRequired() {
        return false;
    }

    public String get(String key) {
        return fParameter.get(key);
    }

    public String getBinding() {
        return BINDING_ATOMPUB;
    }

    public String getRepositoryId() {
        return get(REPOSITORY_ID);
    }

    public String getLocale() {
        return get(LOCALE);
    }

    public String getPassword() {
        return get(PASSWORD);
    }

    public String getUsername() {
        return get(USERNAME);
    }

    public void put(String key, String value) {
        fParameter.put(key, value);
    }
}
