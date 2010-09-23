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
package org.apache.chemistry.opencmis.client.runtime;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectFactory;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.spi.Holder;

public class PersistentDocumentImpl extends AbstractPersistentFilableCmisObject implements Document {

    /**
     * Constructor.
     */
    public PersistentDocumentImpl(PersistentSessionImpl session, ObjectType objectType, ObjectData objectData,
            OperationContext context) {
        initialize(session, objectType, objectData, context);
    }

    // properties

    public String getCheckinComment() {
        return getPropertyValue(PropertyIds.CHECKIN_COMMENT);
    }

    public String getVersionLabel() {
        return getPropertyValue(PropertyIds.VERSION_LABEL);
    }

    public String getVersionSeriesId() {
        return getPropertyValue(PropertyIds.VERSION_SERIES_ID);
    }

    public String getVersionSeriesCheckedOutId() {
        return getPropertyValue(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID);
    }

    public String getVersionSeriesCheckedOutBy() {
        return getPropertyValue(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY);
    }

    public Boolean isImmutable() {
        return getPropertyValue(PropertyIds.IS_IMMUTABLE);
    }

    public Boolean isLatestMajorVersion() {
        return getPropertyValue(PropertyIds.IS_LATEST_MAJOR_VERSION);
    }

    public Boolean isLatestVersion() {
        return getPropertyValue(PropertyIds.IS_LATEST_VERSION);
    }

    public Boolean isMajorVersion() {
        return getPropertyValue(PropertyIds.IS_MAJOR_VERSION);
    }

    public Boolean isVersionSeriesCheckedOut() {
        return getPropertyValue(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT);
    }

    public long getContentStreamLength() {
        BigInteger bigInt = getPropertyValue(PropertyIds.CONTENT_STREAM_LENGTH);
        return (bigInt == null) ? (long) -1 : bigInt.longValue();
    }

    public String getContentStreamMimeType() {
        return getPropertyValue(PropertyIds.CONTENT_STREAM_MIME_TYPE);
    }

    public String getContentStreamFileName() {
        return getPropertyValue(PropertyIds.CONTENT_STREAM_FILE_NAME);
    }

    public String getContentStreamId() {
        return getPropertyValue(PropertyIds.CONTENT_STREAM_ID);
    }

    // operations

    public Document copy(List<Property<?>> properties, VersioningState versioningState, List<Policy> policies,
            List<Ace> addACEs, List<Ace> removeACEs) {
        throw new CmisRuntimeException("not implemented");
    }

    public void deleteAllVersions() {
        delete(true);
    }

    // versioning

    public ObjectId checkOut() {
        String objectId = getObjectId();
        Holder<String> objectIdHolder = new Holder<String>(objectId);

        getBinding().getVersioningService().checkOut(getRepositoryId(), objectIdHolder, null, null);

        if (objectIdHolder.getValue() == null) {
            return null;
        }

        return getSession().createObjectId(objectIdHolder.getValue());
    }

    public void cancelCheckOut() {
        String objectId = getObjectId();

        getBinding().getVersioningService().cancelCheckOut(getRepositoryId(), objectId, null);
    }

    public ObjectId checkIn(boolean major, Map<String, ?> properties, ContentStream contentStream,
            String checkinComment, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces) {
        String objectId;
        ObjectType type;
        readLock();
        try {
            objectId = getObjectId();
            type = getType();
        } finally {
            readUnlock();
        }

        Holder<String> objectIdHolder = new Holder<String>(objectId);

        ObjectFactory of = getObjectFactory();

        Set<Updatability> updatebility = new HashSet<Updatability>();
        updatebility.add(Updatability.READWRITE);
        updatebility.add(Updatability.WHENCHECKEDOUT);

        getBinding().getVersioningService()
                .checkIn(getRepositoryId(), objectIdHolder, major,
                        of.convertProperties(properties, type, updatebility), of.convertContentStream(contentStream),
                        checkinComment, of.convertPolicies(policies), of.convertAces(addAces),
                        of.convertAces(removeAces), null);

        if (objectIdHolder.getValue() == null) {
            return null;
        }

        return getSession().createObjectId(objectIdHolder.getValue());

    }

    public List<Document> getAllVersions() {
        return getAllVersions(getSession().getDefaultContext());
    }

    public List<Document> getAllVersions(OperationContext context) {
        String objectId;
        String versionSeriesId;

        readLock();
        try {
            objectId = getObjectId();
            versionSeriesId = getVersionSeriesId();
        } finally {
            readUnlock();
        }

        List<ObjectData> versions = getBinding().getVersioningService().getAllVersions(getRepositoryId(), objectId,
                versionSeriesId, context.getFilterString(), context.isIncludeAllowableActions(), null);

        ObjectFactory objectFactory = getSession().getObjectFactory();

        List<Document> result = new ArrayList<Document>();
        if (versions != null) {
            for (ObjectData objectData : versions) {
                CmisObject doc = objectFactory.convertObject(objectData, context);
                if (!(doc instanceof Document)) {
                    // should not happen...
                    continue;
                }

                result.add((Document) doc);
            }
        }

        return result;

    }

    public Document getObjectOfLatestVersion(boolean major) {
        return getObjectOfLatestVersion(major, getSession().getDefaultContext());
    }

    public Document getObjectOfLatestVersion(boolean major, OperationContext context) {
        String objectId;
        String versionSeriesId;

        readLock();
        try {
            objectId = getObjectId();
            versionSeriesId = getVersionSeriesId();
        } finally {
            readUnlock();
        }

        if (versionSeriesId == null) {
            throw new CmisRuntimeException("Version series id is unknown!");
        }

        ObjectData objectData = getBinding().getVersioningService().getObjectOfLatestVersion(getRepositoryId(),
                objectId, versionSeriesId, major, context.getFilterString(), context.isIncludeAllowableActions(),
                context.getIncludeRelationships(), context.getRenditionFilterString(), context.isIncludePolicies(),
                context.isIncludeAcls(), null);

        ObjectFactory objectFactory = getSession().getObjectFactory();

        CmisObject result = objectFactory.convertObject(objectData, context);
        if (!(result instanceof Document)) {
            throw new CmisRuntimeException("Latest version is not a document!");
        }

        return (Document) result;
    }

    // content operations

    public ContentStream getContentStream() {
        return getContentStream(null);
    }

    public ContentStream getContentStream(String streamId) {
        String objectId = getObjectId();

        // get the stream
        ContentStream contentStream;
        try {
            contentStream = getBinding().getObjectService().getContentStream(getRepositoryId(), objectId, streamId,
                    null, null, null);
        } catch (CmisConstraintException e) {
            // no content stream
            return null;
        }

        // the AtomPub binding doesn't return a file name
        // -> get the file name from properties, if present
        String filename = contentStream.getFileName();
        if (filename == null) {
            filename = getContentStreamFileName();
        }

        // TODO: what should happen if the length is not set?
        long length = (contentStream.getBigLength() == null ? -1 : contentStream.getBigLength().longValue());

        // convert and return stream object
        return getSession().getObjectFactory().createContentStream(filename, length, contentStream.getMimeType(),
                contentStream.getStream());
    }

    public ObjectId setContentStream(ContentStream contentStream, boolean overwrite) {
        String objectId;
        String changeToken;

        readLock();
        try {
            objectId = getObjectId();
            changeToken = getPropertyValue(PropertyIds.CHANGE_TOKEN);
        } finally {
            readUnlock();
        }

        Holder<String> objectIdHolder = new Holder<String>(objectId);
        Holder<String> changeTokenHolder = new Holder<String>(changeToken);

        getBinding().getObjectService().setContentStream(getRepositoryId(), objectIdHolder, overwrite,
                changeTokenHolder, getObjectFactory().convertContentStream(contentStream), null);

        if (objectIdHolder.getValue() == null) {
            return null;
        }

        return getSession().createObjectId(objectIdHolder.getValue());
    }

    public ObjectId deleteContentStream() {
        String objectId;
        String changeToken;

        readLock();
        try {
            objectId = getObjectId();
            changeToken = getPropertyValue(PropertyIds.CHANGE_TOKEN);
        } finally {
            readUnlock();
        }

        Holder<String> objectIdHolder = new Holder<String>(objectId);
        Holder<String> changeTokenHolder = new Holder<String>(changeToken);

        getBinding().getObjectService().deleteContentStream(getRepositoryId(), objectIdHolder, changeTokenHolder, null);

        if (objectIdHolder.getValue() == null) {
            return null;
        }

        return getSession().createObjectId(objectIdHolder.getValue());
    }

    public ObjectId checkIn(boolean major, Map<String, ?> properties, ContentStream contentStream, String checkinComment) {
        return this.checkIn(major, properties, contentStream, checkinComment, null, null, null);
    }

    public Document copy(List<Property<?>> properties, VersioningState versioningState) {
        return this.copy(properties, versioningState, null, null, null);
    }
}
