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

/**
 * Provides mappings to standard jcr properties for documents.
 */
public class DefaultDocumentIdentifierMap extends DefaultIdentifierMapBase {

    private final boolean isVersionable;

    public DefaultDocumentIdentifierMap(boolean isVersionable) {
        super("nt:file");
        this.isVersionable = isVersionable;
        cmis2Jcr.put(PropertyIds.CREATED_BY, "jcr:content/@jcr:createdBy");
        cmis2Jcr.put(PropertyIds.CREATION_DATE, "jcr:content/@jcr:created");
        cmis2Jcr.put(PropertyIds.LAST_MODIFIED_BY, "jcr:content/@jcr:lastModifiedBy");
        cmis2Jcr.put(PropertyIds.LAST_MODIFICATION_DATE, "jcr:content/@jcr:lastModified");
        cmis2Jcr.put(PropertyIds.CONTENT_STREAM_MIME_TYPE, "jcr:content/@jcr:mimeType");
        cmis2Jcr.put(PropertyIds.CONTENT_STREAM_FILE_NAME, "fn:name()");
        // xxx not supported: IS_IMMUTABLE, IS_LATEST_VERSION, IS_MAJOR_VERSION, IS_LATEST_MAJOR_VERSION,
        // VERSION_LABEL, VERSION_SERIES_ID, IS_VERSION_SERIES_CHECKED_OUT, VERSION_SERIES_CHECKED_OUT_ID
        // VERSION_SERIES_CHECKED_OUT_BY, CHECKIN_COMMENT, CONTENT_STREAM_ID, CONTENT_STREAM_LENGTH
    }

    @Override
    public String jcrTypeCondition() {
        return (isVersionable ? "" : "not") +
                "(@jcr:mixinTypes = 'mix:simpleVersionable')";
    }
}
