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
package org.apache.chemistry.opencmis.commons;

/**
 * Collection of CMIS property ids.
 */
public final class PropertyIds {

    private PropertyIds() {
    }

    // ---- base ----
    /** @cmis 1.0 */
    public static final String NAME = "cmis:name";
    /** @cmis 1.0 */
    public static final String OBJECT_ID = "cmis:objectId";
    /** @cmis 1.0 */
    public static final String OBJECT_TYPE_ID = "cmis:objectTypeId";
    /** @cmis 1.0 */
    public static final String BASE_TYPE_ID = "cmis:baseTypeId";
    /** @cmis 1.0 */
    public static final String CREATED_BY = "cmis:createdBy";
    /** @cmis 1.0 */
    public static final String CREATION_DATE = "cmis:creationDate";
    /** @cmis 1.0 */
    public static final String LAST_MODIFIED_BY = "cmis:lastModifiedBy";
    /** @cmis 1.0 */
    public static final String LAST_MODIFICATION_DATE = "cmis:lastModificationDate";
    /** @cmis 1.0 */
    public static final String CHANGE_TOKEN = "cmis:changeToken";
    /** @cmis 1.1 */
    public static final String DESCRIPTION = "cmis:description";
    /** @cmis 1.1 */
    public static final String SECONDARY_OBJECT_TYPE_IDS = "cmis:secondaryObjectTypeIds";

    // ---- document ----
    /** @cmis 1.0 */
    public static final String IS_IMMUTABLE = "cmis:isImmutable";
    /** @cmis 1.0 */
    public static final String IS_LATEST_VERSION = "cmis:isLatestVersion";
    /** @cmis 1.0 */
    public static final String IS_MAJOR_VERSION = "cmis:isMajorVersion";
    /** @cmis 1.0 */
    public static final String IS_LATEST_MAJOR_VERSION = "cmis:isLatestMajorVersion";
    /** @cmis 1.0 */
    public static final String VERSION_LABEL = "cmis:versionLabel";
    /** @cmis 1.0 */
    public static final String VERSION_SERIES_ID = "cmis:versionSeriesId";
    /** @cmis 1.0 */
    public static final String IS_VERSION_SERIES_CHECKED_OUT = "cmis:isVersionSeriesCheckedOut";
    /** @cmis 1.0 */
    public static final String VERSION_SERIES_CHECKED_OUT_BY = "cmis:versionSeriesCheckedOutBy";
    /** @cmis 1.0 */
    public static final String VERSION_SERIES_CHECKED_OUT_ID = "cmis:versionSeriesCheckedOutId";
    /** @cmis 1.0 */
    public static final String CHECKIN_COMMENT = "cmis:checkinComment";
    /** @cmis 1.0 */
    public static final String CONTENT_STREAM_LENGTH = "cmis:contentStreamLength";
    /** @cmis 1.0 */
    public static final String CONTENT_STREAM_MIME_TYPE = "cmis:contentStreamMimeType";
    /** @cmis 1.0 */
    public static final String CONTENT_STREAM_FILE_NAME = "cmis:contentStreamFileName";
    /** @cmis 1.0 */
    public static final String CONTENT_STREAM_ID = "cmis:contentStreamId";
    /** @cmis 1.1 */
    public static final String IS_PRIVATE_WORKING_COPY = "cmis:isPrivateWorkingCopy";

    // ---- folder ----
    /** @cmis 1.0 */
    public static final String PARENT_ID = "cmis:parentId";
    /** @cmis 1.0 */
    public static final String ALLOWED_CHILD_OBJECT_TYPE_IDS = "cmis:allowedChildObjectTypeIds";
    /** @cmis 1.0 */
    public static final String PATH = "cmis:path";

    // ---- relationship ----
    /** @cmis 1.0 */
    public static final String SOURCE_ID = "cmis:sourceId";
    /** @cmis 1.0 */
    public static final String TARGET_ID = "cmis:targetId";

    // ---- policy ----
    /** @cmis 1.0 */
    public static final String POLICY_TEXT = "cmis:policyText";

    // ---- retention ---
    /** @cmis 1.1 */
    public static final String EXPIRATION_DATE = "cmis:rm_expirationDate";
    /** @cmis 1.1 */
    public static final String START_OF_RETENTION = "cmis:rm_startOfRetention";
    /** @cmis 1.1 */
    public static final String DESTRUCTION_DATE = "cmis:rm_destructionDate";
    /** @cmis 1.1 */
    public static final String HOLD_IDS = "cmis:rm_holdIds";

}
