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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
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

            // check out
            ObjectId pwcId = doc.checkOut();
            Document pwc = (Document) session.getObject(pwcId, SELECT_ALL_NO_CACHE_OC);

            addResult(checkObject(session, pwc, getAllProperties(pwc), "PWC spec compliance - test 1"));

            checkCheckedOut(pwc);

            // check version series
             addResult(checkVersionSeries(session,
             pwc.getAllVersions(SELECT_ALL_NO_CACHE_OC), propertiesToCheck,
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

            if (versions.size() > 0) {
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
            addResult(checkVersionSeries(session, pwc.getAllVersions(SELECT_ALL_NO_CACHE_OC), propertiesToCheck,
                    "Test version series after check in"));

            // remove the document
            deleteObject(doc);
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

        int countLatest = 0;
        for (Document version : versions) {
            addResult(results, checkObject(session, version, properties, "Version object check: " + version.getId()));

            if (Boolean.TRUE.equals(version.isLatestVersion())) {
                countLatest++;
            }
        }

        CmisTestResult f = createResult(FAILURE, "A version series should have one latest version, but it has "
                + countLatest + "!");
        addResult(results, assertEquals(1, countLatest, null, f));

        CmisTestResultImpl result = createResult(getWorst(results), message);
        result.getChildren().addAll(results);

        return (result.getStatus().getLevel() <= OK.getLevel() ? null : result);
    }
}
