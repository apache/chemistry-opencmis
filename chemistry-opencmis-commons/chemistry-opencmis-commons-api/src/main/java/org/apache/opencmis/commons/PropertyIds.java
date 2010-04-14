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
package org.apache.opencmis.commons;

/**
 * Collection of CMIS property ids.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public final class PropertyIds {

  private PropertyIds() {
  }

  // ---- base ----
  public static final String CMIS_NAME = "cmis:name";
  public static final String CMIS_OBJECT_ID = "cmis:objectId";
  public static final String CMIS_OBJECT_TYPE_ID = "cmis:objectTypeId";
  public static final String CMIS_BASE_TYPE_ID = "cmis:baseTypeId";
  public static final String CMIS_CREATED_BY = "cmis:createdBy";
  public static final String CMIS_CREATION_DATE = "cmis:creationDate";
  public static final String CMIS_LAST_MODIFIED_BY = "cmis:lastModifiedBy";
  public static final String CMIS_LAST_MODIFICATION_DATE = "cmis:lastModificationDate";
  public static final String CMIS_CHANGE_TOKEN = "cmis:changeToken";

  // ---- document ----
  public static final String CMIS_IS_IMMUTABLE = "cmis:isImmutable";
  public static final String CMIS_IS_LATEST_VERSION = "cmis:isLatestVersion";
  public static final String CMIS_IS_MAJOR_VERSION = "cmis:isMajorVersion";
  public static final String CMIS_IS_LATEST_MAJOR_VERSION = "cmis:isLatestMajorVersion";
  public static final String CMIS_VERSION_LABEL = "cmis:versionLabel";
  public static final String CMIS_VERSION_SERIES_ID = "cmis:versionSeriesId";
  public static final String CMIS_IS_VERSION_SERIES_CHECKED_OUT = "cmis:isVersionSeriesCheckedOut";
  public static final String CMIS_VERSION_SERIES_CHECKED_OUT_BY = "cmis:versionSeriesCheckedOutBy";
  public static final String CMIS_VERSION_SERIES_CHECKED_OUT_ID = "cmis:versionSeriesCheckedOutId";
  public static final String CMIS_CHECKIN_COMMENT = "cmis:checkinComment";
  public static final String CMIS_CONTENT_STREAM_LENGTH = "cmis:contentStreamLength";
  public static final String CMIS_CONTENT_STREAM_MIME_TYPE = "cmis:contentStreamMimeType";
  public static final String CMIS_CONTENT_STREAM_FILE_NAME = "cmis:contentStreamFileName";
  public static final String CMIS_CONTENT_STREAM_ID = "cmis:contentStreamId";

  // ---- folder ----
  public static final String CMIS_PARENT_ID = "cmis:parentId";
  public static final String CMIS_ALLOWED_CHILD_OBJECT_TYPE_IDS = "cmis:allowedChildObjectTypeIds";
  public static final String CMIS_PATH = "cmis:path";

  // ---- relationship ----
  public static final String CMIS_SOURCE_ID = "cmis:sourceId";
  public static final String CMIS_TARGET_ID = "cmis:targetId";

  // ---- policy ----
  public static final String CMIS_POLICY_TEXT = "cmis:policyText";
}
