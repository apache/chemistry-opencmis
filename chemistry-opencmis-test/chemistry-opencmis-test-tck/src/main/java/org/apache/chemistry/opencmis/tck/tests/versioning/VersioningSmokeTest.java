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
package org.apache.chemistry.opencmis.tck.tests.versioning;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.OK;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;
import org.apache.chemistry.opencmis.tck.impl.CmisTestResultImpl;

/**
 * Checked out test.
 */
public class VersioningSmokeTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Versioning Smoke Test");
        setDescription("Creates a document, checks it out, cancels the check out, checks it out again and finally checks it in.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        try {
            // create folder and document
            Folder testFolder = createTestFolder(session);
            Document doc = createDocument(session, testFolder, "versioningtest.txt", "versioning");
            DocumentTypeDefinition docType = (DocumentTypeDefinition) doc.getType();

            if (!docType.isVersionable()) {
                addResult(createResult(SKIPPED, "Test type is not versionable. Test skipped!"));
                doc.delete(true);
                return;
            }

            // gather properties for later
            String[] propertiesToCheck = new String[doc.getType().getPropertyDefinitions().size()];

            int i = 0;
            for (String propId : doc.getType().getPropertyDefinitions().keySet()) {
                propertiesToCheck[i++] = propId;
            }

            Map<String, Object> writableProperties = new HashMap<String, Object>();
            for (Property<?> property : doc.getProperties()) {
                if (property.getDefinition().getUpdatability() == Updatability.READWRITE) {
                    writableProperties.put(property.getId(), property.getValue());
                }
            }

            // check out
            ObjectId pwcId = doc.checkOut();
            Document pwc = (Document) session.getObject(pwcId, SELECT_ALL_NO_CACHE_OC);

            addResult(checkObject(session, pwc, getAllProperties(pwc), "PWC spec compliance - test 1"));

            checkCheckedOut(pwc);

            // check version series
            addResult(checkVersionSeries(session, pwc.getAllVersions(SELECT_ALL_NO_CACHE_OC), propertiesToCheck,
                    "Test version series after check out"));

            // cancel checkout
            pwc.cancelCheckOut();

            doc.refresh();
            checkCheckedIn(doc);

            // check out again
            pwcId = doc.checkOut();
            pwc = (Document) session.getObject(pwcId, SELECT_ALL_NO_CACHE_OC);

            addResult(checkObject(session, pwc, getAllProperties(pwc), "PWC spec compliance - test 2"));

            checkCheckedOut(pwc);

            // check in
            ObjectId newVersionId = pwc.checkIn(true, null, null, "Test Version 2");
            Document newVersion = (Document) session.getObject(newVersionId, SELECT_ALL_NO_CACHE_OC);

            addResult(checkObject(session, newVersion, getAllProperties(newVersion), "New version compliance"));

            checkCheckedIn(newVersion);

            // check version history
            List<Document> versions = newVersion.getAllVersions(SELECT_ALL_NO_CACHE_OC);
            f = createResult(FAILURE, "Version series should have 2 versions but has " + versions.size() + "!");
            addResult(assertEquals(2, versions.size(), null, f));

            if (!versions.isEmpty()) {
                f = createResult(FAILURE,
                        "Version history order is incorrect! The first version should be the new version.");
                addResult(assertEquals(newVersion.getId(), versions.get(0).getId(), null, f));

                f = createResult(FAILURE,
                        "The new version should be the latest version, but cmis:isLatestVersion is not TRUE.");
                addResult(assertEquals(true, versions.get(0).isLatestVersion(), null, f));

                f = createResult(FAILURE,
                        "The new version should be the latest major version, but cmis:isLatestMajorVersion is not TRUE.");
                addResult(assertEquals(true, versions.get(0).isLatestMajorVersion(), null, f));
            }

            if (versions.size() > 1) {
                f = createResult(FAILURE,
                        "Version history order is incorrect! The second version should be the origin document.");
                addResult(assertEquals(doc.getId(), versions.get(1).getId(), null, f));
            }

            // check version series
            addResult(checkVersionSeries(session, versions, propertiesToCheck, "Test version series after check in"));

            // check out again
            pwcId = newVersion.checkOut();
            pwc = (Document) session.getObject(pwcId, SELECT_ALL_NO_CACHE_OC);

            addResult(checkObject(session, pwc, getAllProperties(pwc), "PWC spec compliance - test 3"));

            checkCheckedOut(pwc);

            // check in giving back all updateable properties
            ObjectId thirdVersionId = pwc.checkIn(true, writableProperties, null, "Test Version 3");
            Document thirdVersion = (Document) session.getObject(thirdVersionId, SELECT_ALL_NO_CACHE_OC);

            addResult(checkObject(session, thirdVersion, getAllProperties(thirdVersion), "New version compliance"));

            // check out again
            pwcId = thirdVersion.checkOut();
            pwc = (Document) session.getObject(pwcId, SELECT_ALL_NO_CACHE_OC);

            addResult(checkObject(session, pwc, getAllProperties(pwc), "PWC spec compliance - test 4"));

            checkCheckedOut(pwc);

            // check in giving a new content stream
            String fourthContent = "new content";
            byte[] fourthContentBytes = IOUtils.toUTF8Bytes(fourthContent);
            ContentStream fourthContentStream = new ContentStreamImpl("version4",
                    BigInteger.valueOf(fourthContentBytes.length), "text/plain", new ByteArrayInputStream(
                            fourthContentBytes));

            ObjectId fourthVersionId = pwc.checkIn(true, null, fourthContentStream, "Test Version 5");
            Document fourthVersion = (Document) session.getObject(fourthVersionId, SELECT_ALL_NO_CACHE_OC);

            addResult(checkObject(session, fourthVersion, getAllProperties(fourthVersion), "New version compliance"));

            checkCheckedIn(fourthVersion);

            // check out again
            pwcId = fourthVersion.checkOut();
            pwc = (Document) session.getObject(pwcId, SELECT_ALL_NO_CACHE_OC);

            addResult(checkObject(session, pwc, getAllProperties(pwc), "PWC spec compliance - test 5"));

            checkCheckedOut(pwc);

            // check in giving properties and a new content stream
            String fifthContent = "brand-new content";
            byte[] fifthContentBytes = IOUtils.toUTF8Bytes(fifthContent);
            ContentStream fifthContentStream = new ContentStreamImpl("version5",
                    BigInteger.valueOf(fifthContentBytes.length), "text/plain", new ByteArrayInputStream(
                            fifthContentBytes));

            ObjectId fifthVersionId = pwc.checkIn(true, writableProperties, fifthContentStream, "Test Version 5");
            Document fifthVersion = (Document) session.getObject(fifthVersionId, SELECT_ALL_NO_CACHE_OC);

            addResult(checkObject(session, fifthVersion, getAllProperties(fifthVersion), "New version compliance"));

            checkCheckedIn(fifthVersion);

            // test the latest version
            Document latest = session.getLatestDocumentVersion(doc, SELECT_ALL_NO_CACHE_OC);

            f = createResult(FAILURE, "getObjectOfLatestVersion() did not return the expected version!");
            addResult(assertEquals(fifthVersion.getId(), latest.getId(), null, f));

            // test if checking out a non-latest version works for this
            // repository
            try {
                pwcId = doc.checkOut();
                pwc = (Document) session.getObject(pwcId, SELECT_ALL_NO_CACHE_OC);
                pwc.cancelCheckOut();

                addResult(createInfoResult("Repository allows check out on a version that is not the latest version."));
            } catch (CmisBaseException e) {
                addResult(createInfoResult("Repository only support check out on the latest version."));
            }

            // remove the document
            deleteObject(doc);

            // test if all versions have been deleted
            f = createResult(FAILURE, "Version 2 has not been deleted!");
            addResult(assertIsFalse(session.exists(newVersion), null, f));

            f = createResult(FAILURE, "Version 3 has not been deleted!");
            addResult(assertIsFalse(session.exists(thirdVersion), null, f));

            f = createResult(FAILURE, "Version 4 has not been deleted!");
            addResult(assertIsFalse(session.exists(fourthVersion), null, f));

            f = createResult(FAILURE, "Version 5 has not been deleted!");
            addResult(assertIsFalse(session.exists(fifthVersion), null, f));
        } finally {
            deleteTestFolder();
        }
    }

    private void checkCheckedOut(Document pwc) {
        CmisTestResult f;

        f = createResult(FAILURE, "Version series has a PWC but cmis:isVersionSeriesCheckedOut is not TRUE!");
        addResult(assertIsTrue(pwc.isVersionSeriesCheckedOut(), null, f));

        if (pwc.getVersionSeriesCheckedOutId() == null) {
            addResult(createResult(WARNING, "cmis:versionSeriesCheckedOutId is not set!"));
        } else {
            f = createResult(FAILURE, "PWC id and cmis:versionSeriesCheckedOutId don't match!");
            addResult(assertEquals(pwc.getId(), pwc.getVersionSeriesCheckedOutId(), null, f));
        }

        f = createResult(WARNING, "PWC does not have a value for cmis:versionSeriesCheckedOutBy!");
        addResult(assertStringNotEmpty(pwc.getVersionSeriesCheckedOutBy(), null, f));
    }

    private void checkCheckedIn(Document doc) {
        CmisTestResult f;

        f = createResult(FAILURE, "Version series is not checked out but cmis:isVersionSeriesCheckedOut is not FALSE!");
        addResult(assertIsFalse(doc.isVersionSeriesCheckedOut(), null, f));

        f = createResult(FAILURE, "Version series is not checked out but cmis:versionSeriesCheckedOutId has a value!");
        addResult(assertNull(doc.getVersionSeriesCheckedOutId(), null, f));

        f = createResult(FAILURE, "Version series is not checked out but cmis:versionSeriesCheckedOutBy has a value!");
        addResult(assertNull(doc.getVersionSeriesCheckedOutBy(), null, f));
    }

    private CmisTestResult checkVersionSeries(Session session, List<Document> versions, String[] properties,
            String message) {
        List<CmisTestResult> results = new ArrayList<CmisTestResult>();
        CmisTestResult f;

        // make sure there is only one latest version
        // and zero or one latest major version
        int countLatest = 0;
        int countLatestMajor = 0;
        String latestId = null;
        for (Document version : versions) {
            addResult(results, checkObject(session, version, properties, "Version object check: " + version.getId()));

            if (Boolean.TRUE.equals(version.isLatestVersion())) {
                countLatest++;
                latestId = version.getId();
            }

            if (Boolean.TRUE.equals(version.isLatestMajorVersion())) {
                countLatestMajor++;
            }
        }

        f = createResult(FAILURE, "The version series must have exactly one latest version, but it has " + countLatest
                + "!");
        addResult(results, assertEquals(1, countLatest, null, f));

        f = createResult(FAILURE, "The version series must have zero or one latest major version, but it has "
                + countLatestMajor + "!");
        addResult(results, assertIsTrue(countLatestMajor < 2, null, f));

        // check getObjectOfLatestVersion()
        if (countLatest == 1) {
            Document latestVersion = versions.get(0).getObjectOfLatestVersion(false, SELECT_ALL_NO_CACHE_OC);
            addResult(
                    results,
                    checkObject(session, latestVersion, properties,
                            "Latest version object check: " + latestVersion.getId()));

            f = createResult(FAILURE,
                    "The version that is flagged as latest version is not returned by getObjectOfLatestVersion()!");
            addResult(results, assertEquals(latestId, latestVersion.getId(), null, f));

            // check with session.getLatestDocumentVersion()
            Document latestVersion2 = session.getLatestDocumentVersion(versions.get(versions.size() - 1).getId(),
                    SELECT_ALL_NO_CACHE_OC);

            addResult(
                    results,
                    checkObject(session, latestVersion2, properties, "Latest version object check (2): "
                            + latestVersion2.getId()));

            f = createResult(FAILURE,
                    "The version that is flagged as latest version is not returned by getObjectOfLatestVersion()!");
            addResult(results, assertEquals(latestId, latestVersion2.getId(), null, f));
        }

        CmisTestResultImpl result = createResult(getWorst(results), message);
        result.getChildren().addAll(results);

        return result.getStatus().getLevel() <= OK.getLevel() ? null : result;
    }
}
