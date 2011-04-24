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
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.TransientDocument;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.spi.Holder;

public class TransientDocumentImpl extends AbstractTransientFileableCmisObject implements TransientDocument {

    private ContentStream contentStream;
    private boolean contentOverwrite;
    private boolean deleteContent;

    @Override
    protected void initialize(Session session, CmisObject object) {
        super.initialize(session, object);

        contentStream = null;
        contentOverwrite = false;
        deleteContent = false;
    }

    public void deleteAllVersions() {
        delete(true);
    }

    public ContentStream getContentStream() {
        return ((Document) getCmisObject()).getContentStream();
    }

    public ContentStream getContentStream(String streamId) {
        return ((Document) getCmisObject()).getContentStream(streamId);
    }

    public void setContentStream(ContentStream contentStream, boolean overwrite) {
        this.contentStream = contentStream;
        this.contentOverwrite = overwrite;

        deleteContent = false;
        isModified = true;
    }

    public void deleteContentStream() {
        deleteContent = true;

        contentStream = null;
        isModified = true;
    }

    public Document getObjectOfLatestVersion(boolean major) {
        return ((Document) getCmisObject()).getObjectOfLatestVersion(major);
    }

    public Document getObjectOfLatestVersion(boolean major, OperationContext context) {
        return ((Document) getCmisObject()).getObjectOfLatestVersion(major, context);
    }

    public List<Document> getAllVersions() {
        return ((Document) getCmisObject()).getAllVersions();
    }

    public List<Document> getAllVersions(OperationContext context) {
        return ((Document) getCmisObject()).getAllVersions(context);
    }

    public Document copy(ObjectId targetFolderId) {
        return ((Document) getCmisObject()).copy(targetFolderId);
    }

    public Document copy(ObjectId targetFolderId, Map<String, ?> properties, VersioningState versioningState,
            List<Policy> policies, List<Ace> addACEs, List<Ace> removeACEs, OperationContext context) {
        return ((Document) getCmisObject()).copy(targetFolderId, properties, versioningState, policies, addACEs,
                removeACEs, context);
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

    // override save()

    @Override
    public ObjectId save() {
        if (!isModified()) {
            // nothing has change, so there is nothing to do
            return getObjectId();
        }

        String objectId = getId();

        if (saveDelete(objectId)) {
            // object has been deleted, there is nothing else to do
            // ... and there is no object id anymore
            return null;
        }

        String newObjectId = objectId;
        String newChangeToken = getChangeToken();

        newObjectId = saveProperties(getId(), newChangeToken);

        if (isPropertyUpdateRequired && ((contentStream != null) || deleteContent)) {
            // we only need a new change token if the properties have changed
            // AND the content should be modified
            newChangeToken = getLatestChangeToken(newObjectId);
        }

        newObjectId = saveContent(newObjectId, newChangeToken);

        saveACL(newObjectId);
        savePolicies(newObjectId);

        return getSession().createObjectId(newObjectId);
    }

    protected String saveContent(String objectId, String changeToken) {
        Holder<String> objectIdHolder = new Holder<String>(objectId);
        Holder<String> changeTokenHolder = new Holder<String>(changeToken);

        if (contentStream != null) {
            getBinding().getObjectService().setContentStream(getRepositoryId(), objectIdHolder, contentOverwrite,
                    changeTokenHolder, contentStream, null);
        } else if (deleteContent) {
            getBinding().getObjectService().deleteContentStream(getRepositoryId(), objectIdHolder, changeTokenHolder,
                    null);
        }

        if (objectIdHolder.getValue() != null) {
            return objectIdHolder.getValue();
        }

        return objectId;
    }

    public ObjectId checkIn(boolean major, String checkinComment) {
        Holder<String> objectIdHolder = new Holder<String>(getId());

        // convert properties
        Properties checkinProperties = prepareProperties();

        // prepare policies
        List<String> checkinPolicies = null;
        if ((addPolicies != null) && (!addPolicies.isEmpty())) {
            checkinPolicies = new ArrayList<String>(addPolicies);
        }

        // prepare ACLs
        List<AceChangeHolder> checkinAddAces = new ArrayList<AceChangeHolder>();
        List<AceChangeHolder> checkinRemoveAces = new ArrayList<AceChangeHolder>();
        for (AclPropagation ap : AclPropagation.values()) {
            if (addAces.containsKey(ap)) {
                checkinAddAces.addAll(addAces.get(ap));
            }
            if (removeAces.containsKey(ap)) {
                checkinAddAces.addAll(removeAces.get(ap));
            }
        }
        if (addAces.containsKey(null)) {
            checkinAddAces.addAll(addAces.get(null));
        }
        if (removeAces.containsKey(null)) {
            checkinAddAces.addAll(removeAces.get(null));
        }

        // check in
        getBinding().getVersioningService().checkIn(getRepositoryId(), objectIdHolder, major, checkinProperties,
                contentStream, checkinComment, checkinPolicies, prepareAcl(checkinAddAces),
                prepareAcl(checkinRemoveAces), null);

        if (objectIdHolder.getValue() != null) {
            return getSession().createObjectId(objectIdHolder.getValue());
        }

        return getObjectId();
    }
}
