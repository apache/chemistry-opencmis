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

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

/**
 * CMIS Document.
 * <p>
 * Domain Model 2.4
 */
public interface Document extends FileableCmisObject, DocumentProperties {

    TransientDocument getTransientDocument();

    // object service

    /**
     * Deletes this document and all its versions.
     */
    void deleteAllVersions();

    /**
     * Retrieves the content stream of this document.
     * 
     * @return the content stream, or {@code null}
     */
    ContentStream getContentStream();

    /**
     * Retrieves the content stream of this document.
     * 
     * @param offset
     *            the offset of the stream or <code>null</code> to read the
     *            stream from the beginning
     * @param length
     *            the maximum length of the stream or <code>null</code> to read
     *            to the end of the stream
     * @return the content stream, or {@code null}
     */
    ContentStream getContentStream(BigInteger offset, BigInteger length);

    /**
     * Retrieves the content stream that is associated with the given stream id.
     * This is usually a rendition of the document.
     * 
     * @param streamId
     *            the stream id
     * 
     * @return the content stream, or {@code null}
     */
    ContentStream getContentStream(String streamId);

    /**
     * Retrieves the content stream that is associated with the given stream id.
     * This is usually a rendition of the document.
     * 
     * @param streamId
     *            the stream id
     * @param offset
     *            the offset of the stream or <code>null</code> to read the
     *            stream from the beginning
     * @param length
     *            the maximum length of the stream or <code>null</code> to read
     *            to the end of the stream
     * @return the content stream, or {@code null}
     */
    ContentStream getContentStream(String streamId, BigInteger offset, BigInteger length);

    /**
     * Sets a new content stream for the document and refreshes this object
     * afterwards. If the repository created a new version, this new document is
     * returned. Otherwise the current document is returned.
     * 
     * The stream in <code>contentStream</code> is consumed but not closed by
     * this method.
     * 
     * @return the updated document
     */
    Document setContentStream(ContentStream contentStream, boolean overwrite);

    /**
     * Sets a new content stream for the document.
     * 
     * The stream in <code>contentStream</code> is consumed but not closed by
     * this method.
     * 
     * @return the updated object id
     */
    ObjectId setContentStream(ContentStream contentStream, boolean overwrite, boolean refresh);

    /**
     * Removes the current content stream from the document and refreshes this
     * object afterwards. If the repository created a new version, this new
     * document is returned. Otherwise the current document is returned.
     * 
     * @return the updated document
     */
    Document deleteContentStream();

    /**
     * Removes the current content stream from the document.
     */
    ObjectId deleteContentStream(boolean refresh);

    // versioning service

    /**
     * Checks out the document and returns the object id of the PWC (private
     * working copy).
     * 
     * @return PWC object id
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
     * The stream in <code>contentStream</code> is consumed but not closed by
     * this method.
     * 
     * @return new document id
     */
    ObjectId checkIn(boolean major, Map<String, ?> properties, ContentStream contentStream, String checkinComment,
            List<Policy> policies, List<Ace> addAces, List<Ace> removeAces);

    /**
     * If this is a PWC (private working copy) it performs a check in. If this
     * is not a PWC it an exception will be thrown.
     * 
     * The stream in <code>contentStream</code> is consumed but not closed by
     * this method.
     * 
     * @return new document id
     */
    ObjectId checkIn(boolean major, Map<String, ?> properties, ContentStream contentStream, String checkinComment);

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

    /**
     * Creates a copy of this document, including content.
     * 
     * @return the new document object
     */
    Document copy(ObjectId targetFolderId);

    /**
     * Creates a copy of this document, including content.
     * 
     * @return the new document object or {@code null} if the parameter
     *         {@code context} was set to {@code null}
     */
    Document copy(ObjectId targetFolderId, Map<String, ?> properties, VersioningState versioningState,
            List<Policy> policies, List<Ace> addACEs, List<Ace> removeACEs, OperationContext context);

}
