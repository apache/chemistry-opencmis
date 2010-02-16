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
package org.apache.opencmis.commons.enums;

/**
 * Predefined Property Enum.
 */
public enum CmisProperties {

  OBJECT_ID("cmis:objectId"), BASE_TYPE_ID("cmis:baseTypeId"), OBJECT_TYPE_ID("cmis:objectTypeId"), NAME(
      "cmis:name"), CREATED_BY("cmis:createdBy"), CREATION_DATE("cmis:creationDate"), LAST_MODIFIED_BY(
      "cmis:lastModifiedBy"), LAST_MODIFCATION_DATE("cmis:lastModificationDate"), CHANGE_TOKEN(
      "cmis:changeToken"), IS_IMMUTABLE("cmis:isImmutable"), IS_LATEST_VERSION(
      "cmis:isLatestVersion"), IS_MAJOR_VERSION("cmis:isMajorVersion"), IS_LATEST_MAJOR_VERSION(
      "cmis:isLatestMajorVersion"), VERSION_LABEL("cmis:versionLabel"), VERSION_SERIES_ID(
      "cmis:versionSeriesId"), IS_VERSION_SERIES_CHECKED_OUT("cmis:isVersionSeriesCheckedOut"), VERSION_SERIES_CHECKED_OUT_BY(
      "cmis:versionSeriesCheckedOutBy"), VERSION_SERIES_CHECKED_OUT_ID(
      "cmis:versionSeriesCheckedOutId"), CHECKIN_COMMENT("cmis:checkinComment"), CONTENT_STREAM_LENGTH(
      "cmis:contentStreamLength"), CONTENT_STREAM_MIME_TYPE("cmis:contentStreamMimeType"), CONTENT_STREAM_FILE_NAME(
      "cmis:contentStreamFileName"), CONTENT_STREAM_ID("cmis:contentStreamId"), PARENT_ID(
      "cmis:parentId"), PATH("cmis:path"), ALLOWED_CHILD_OBJECT_TYPE_IDS(
      "cmis:allowedChildObjectTypeIds"), SOURCE_ID("cmis:sourceId"), TARGET_ID("cmis:targetId"), POLICY_TEXT(
      "cmis:policyText");

  private final String value;

  CmisProperties(String v) {
    value = v;
  }

  public String value() {
    return value;
  }

  public static CmisProperties fromValue(String v) {
    for (CmisProperties c : CmisProperties.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }

}
