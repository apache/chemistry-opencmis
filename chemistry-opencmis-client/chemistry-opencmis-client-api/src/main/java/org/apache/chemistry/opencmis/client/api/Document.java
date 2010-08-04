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

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

/**
 * CMIS Document.
 * 
 * Domain Model 2.4
 */
public interface Document extends FileableCmisObject {

    // object service

    /**
     * Deletes this document and all its versions.
     */
    void deleteAllVersions();

    /**
     * Retrieves the content stream of this document.
     */
    ContentStream getContentStream();

    /**
     * Retrieves the content stream that is associated with the given stream id.
     * This is usually a rendition of the document.
     */
    ContentStream getContentStream(String streamId);

    /**
     * Sets a new content stream for the document.
     */
    ObjectId setContentStream(ContentStream contentStream, boolean overwrite);

    /**
     * Removes the current content stream from the document.
     */
    ObjectId deleteContentStream();

    // versioning service

    /**
     * Checks out the document and returns the object id of the PWC (private
     * working copy).
     * 
     * @return PWC id
     */
    ObjectId checkOut(); // returns the PWC id

    /**
     * If this is a PWC (private working copy) the check out will be reversed.
     * If this is not a PWC it an exception will be thrown.
     */
    void cancelCheckOut();

    /**
     * If this is a PWC (private working copy) it performs a check in. If this
     * is not a PWC it an exception will be thrown.
     * 
     * @return new document id
     */
    ObjectId checkIn(boolean major, Map<String, ?> properties, ContentStream contentStream, String checkinComment,
            List<Policy> policies, List<Ace> addAces, List<Ace> removeAces);

    /**
     * Fetches the latest major or minor version of this document.
     * 
     * @param major
     *            if <code>true</code> the latest major version will be
     *            returned, otherwise the very last version will be returned
     * 
     * @return the latest document object
     */
    Document getObjectOfLatestVersion(boolean major);

    /**
     * Fetches the latest major or minor version of this document using the
     * given {@link OperationContext}.
     * 
     * @param major
     *            if <code>true</code> the latest major version will be
     *            returned, otherwise the very last version will be returned
     * 
     * @return the latest document object
     */
    Document getObjectOfLatestVersion(boolean major, OperationContext context);

    /**
     * Fetches all versions of this document.
     */
    List<Document> getAllVersions();

    /**
     * Fetches all versions of this document using the given
     * {@link OperationContext}.
     */
    List<Document> getAllVersions(OperationContext context);

    // document specific properties

    Boolean isImmutable(); // cmis:isImmutable

    Boolean isLatestVersion(); // cmis:isLatestVersion

    Boolean isMajorVersion(); // cmis:isMajorVersion

    Boolean isLatestMajorVersion(); // cmis:isLatestMajorVersion

    String getVersionLabel(); // cmis:versionLabel

    String getVersionSeriesId(); // cmis:versionSeriesId

    Boolean isVersionSeriesCheckedOut(); // cmis:isVersionSeriesCheckedOut

    String getVersionSeriesCheckedOutBy(); // cmis:versionSeriesCheckedOutBy

    String getVersionSeriesCheckedOutId(); // cmis:versionSeriesCheckedOutId

    String getCheckinComment(); // cmis:checkinComment

    long getContentStreamLength(); // cmis:contentStreamLength

    String getContentStreamMimeType(); // cmis:contentStreamMimeType

    String getContentStreamFileName(); // cmis:contentStreamFileName

    String getContentStreamId(); // cmis:contentStreamId

    /**
     * Creates a (content) copy of this document.
     */
    Document copy(List<Property<?>> properties, VersioningState versioningState, List<Policy> policies,
            List<Ace> addACEs, List<Ace> removeACEs);

}
