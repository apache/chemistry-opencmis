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
package org.apache.chemistry.opencmis.jcr.query;

import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

import java.util.HashMap;
import java.util.Map;

/**
 * This abstract base class provides support for implementing {@link IdentifierMap}.
 */
public abstract class IdentifierMapBase implements IdentifierMap {

    private final String jcrTypeName;
    protected final Map<String, String> cmis2Jcr = new HashMap<String, String>();

    protected IdentifierMapBase(String jcrTypeName) {
        this.jcrTypeName = jcrTypeName;
    }

    public String jcrPathFromCol(String name) {
        String jcrPath = cmis2Jcr.get(name);
        if (jcrPath == null) {
            throw new CmisRuntimeException("Not supported: query on column " + name);
        } else {
            return jcrPath;
        }
    }

    public String jcrTypeName() {
        return jcrTypeName;
    }

    public String jcrTypeCondition() {
        return null;
    }
}
