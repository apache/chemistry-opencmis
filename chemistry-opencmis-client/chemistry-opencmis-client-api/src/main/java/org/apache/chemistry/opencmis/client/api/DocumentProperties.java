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
package org.apache.chemistry.opencmis.client.api;

/**
 * Accessors to CMIS document properties.
 *
 * @see CmisObjectProperties
 */
public interface DocumentProperties {

    /**
     * Returns <code>true</code> if this CMIS object is immutable (CMIS property
     * <code>cmis:isImmutable</code>).
     */
    Boolean isImmutable();

    /**
     * Returns if this CMIS object is the latest version (CMIS property
     * <code>cmis:isLatestVersion</code>).
     */
    Boolean isLatestVersion();

    /**
     * Returns
     * <code>true<code> if this CMIS object is the latest version (CMIS property
     * <code>cmis:isMajorVersion</code>).
     */
    Boolean isMajorVersion();

    /**
     * Returns
     * <code>true</code> if this CMIS object is the latest major version (CMIS property
     * <code>cmis:isLatestMajorVersion</code>).
     */
    Boolean isLatestMajorVersion();

    /**
     * Returns the version label (CMIS property <code>cmis:versionLabel</code>).
     */
    String getVersionLabel();

    /**
     * Returns the version series id (CMIS property
     * <code>cmis:versionSeriesId</code>).
     */
    String getVersionSeriesId();

    /**
     * Returns
     * <code>true</code> if this version series is checked out (CMIS property
     * <code>cmis:isVersionSeriesCheckedOut</code>).
     */
    Boolean isVersionSeriesCheckedOut();

    /**
     * Returns the user who checked out this version series (CMIS property
     * <code>cmis:versionSeriesCheckedOutBy</code>).
     */
    String getVersionSeriesCheckedOutBy();

    /**
     * Returns the PWC id of this version series (CMIS property
     * <code>cmis:versionSeriesCheckedOutId</code>).
     */
    String getVersionSeriesCheckedOutId();

    /**
     * Returns the checkin comment (CMIS property
     * <code>cmis:checkinComment</code>).
     */
    String getCheckinComment();

    /**
     * Returns the content stream length or -1 if the document has no content
     * (CMIS property <code>cmis:contentStreamLength</code>).
     */
    long getContentStreamLength();

    /**
     * Returns the content stream MIME type or <code>null</code> if the document
     * has no content (CMIS property <code>cmis:contentStreamMimeType</code>).
     */
    String getContentStreamMimeType();

    /**
     * Returns the content stream filename or <code>null</code> if the document
     * has no content (CMIS property <code>cmis:contentStreamFileName</code>).
     */
    String getContentStreamFileName();

    /**
     * Returns the content stream id or <code>null</code> if the document has no
     * content (CMIS property <code>cmis:contentStreamId</code>).
     */
    String getContentStreamId();
}
