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
    public static final String NAME = "cmis:name";
    public static final String OBJECT_ID = "cmis:objectId";
    public static final String OBJECT_TYPE_ID = "cmis:objectTypeId";
    public static final String BASE_TYPE_ID = "cmis:baseTypeId";
    public static final String CREATED_BY = "cmis:createdBy";
    public static final String CREATION_DATE = "cmis:creationDate";
    public static final String LAST_MODIFIED_BY = "cmis:lastModifiedBy";
    public static final String LAST_MODIFICATION_DATE = "cmis:lastModificationDate";
    public static final String CHANGE_TOKEN = "cmis:changeToken";

    // ---- document ----
    public static final String IS_IMMUTABLE = "cmis:isImmutable";
    public static final String IS_LATEST_VERSION = "cmis:isLatestVersion";
    public static final String IS_MAJOR_VERSION = "cmis:isMajorVersion";
    public static final String IS_LATEST_MAJOR_VERSION = "cmis:isLatestMajorVersion";
    public static final String VERSION_LABEL = "cmis:versionLabel";
    public static final String VERSION_SERIES_ID = "cmis:versionSeriesId";
    public static final String IS_VERSION_SERIES_CHECKED_OUT = "cmis:isVersionSeriesCheckedOut";
    public static final String VERSION_SERIES_CHECKED_OUT_BY = "cmis:versionSeriesCheckedOutBy";
    public static final String VERSION_SERIES_CHECKED_OUT_ID = "cmis:versionSeriesCheckedOutId";
    public static final String CHECKIN_COMMENT = "cmis:checkinComment";
    public static final String CONTENT_STREAM_LENGTH = "cmis:contentStreamLength";
    public static final String CONTENT_STREAM_MIME_TYPE = "cmis:contentStreamMimeType";
    public static final String CONTENT_STREAM_FILE_NAME = "cmis:contentStreamFileName";
    public static final String CONTENT_STREAM_ID = "cmis:contentStreamId";

    // ---- folder ----
    public static final String PARENT_ID = "cmis:parentId";
    public static final String ALLOWED_CHILD_OBJECT_TYPE_IDS = "cmis:allowedChildObjectTypeIds";
    public static final String PATH = "cmis:path";

    // ---- relationship ----
    public static final String SOURCE_ID = "cmis:sourceId";
    public static final String TARGET_ID = "cmis:targetId";

    // ---- policy ----
    public static final String POLICY_TEXT = "cmis:policyText";
}
