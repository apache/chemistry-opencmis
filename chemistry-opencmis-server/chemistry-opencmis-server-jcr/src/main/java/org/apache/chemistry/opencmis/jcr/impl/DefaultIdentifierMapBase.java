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
package org.apache.chemistry.opencmis.jcr.impl;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.jcr.query.IdentifierMapBase;

/**
 * Provides mappings to standard jcr properties.
 */
public class DefaultIdentifierMapBase extends IdentifierMapBase {

    public DefaultIdentifierMapBase(String jcrTypeName) {
        super(jcrTypeName);
        cmis2Jcr.put(PropertyIds.OBJECT_ID, "@jcr:uuid");
        cmis2Jcr.put(PropertyIds.NAME, "fn:name()");
        cmis2Jcr.put(PropertyIds.CREATED_BY, "@jcr:createdBy");
        cmis2Jcr.put(PropertyIds.CREATION_DATE, "@jcr:created");
        cmis2Jcr.put(PropertyIds.LAST_MODIFIED_BY, "@jcr:lastModifiedBy");
        cmis2Jcr.put(PropertyIds.LAST_MODIFICATION_DATE, "@jcr:lastModified");
        // xxx not supported: BASE_TYPE_ID, CHANGE_TOKEN
    }
}
