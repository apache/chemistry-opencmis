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
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

public class VersionDeleteTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Versioning Delete Test");
        setDescription("Creates a document, adds three versions and deletes the current version until the document is gone.");
    }

    @Override
    public void run(Session session) {
        try {
            // create folder and document
            Folder testFolder = createTestFolder(session);
            Document doc = createDocument(session, testFolder, "versiondeletetest.txt", "v1");
            DocumentTypeDefinition docType = (DocumentTypeDefinition) doc.getType();

            if (!docType.isVersionable()) {
                addResult(createResult(SKIPPED, "Test type is not versionable. Test skipped!"));
                doc.delete(true);
                return;
            }

            // add versions
            Document doc2 = createVersion(session, doc, "v2", 2);
            Document doc3 = createVersion(session, doc2, "v3", 3);
            Document doc4 = createVersion(session, doc3, "v4", 4);

            // delete versions
            deleteVersion(doc4, doc3, 4);
            deleteVersion(doc3, doc2, 3);
            deleteVersion(doc2, doc, 2);
            deleteVersion(doc, null, 1);

        } finally {
            deleteTestFolder();
        }
    }

    private Document createVersion(Session session, Document doc, String content, int version) {
        CmisTestResult f;

        // check out
        ObjectId pwcId = doc.checkOut();
        Document pwc = (Document) session.getObject(pwcId, SELECT_ALL_NO_CACHE_OC);

        addResult(checkObject(session, pwc, getAllProperties(pwc), "PWC " + version + " compliance"));

        // check in
        byte[] contentBytes = IOUtils.toUTF8Bytes(content);

        ContentStream contentStream = new ContentStreamImpl(doc.getName(), BigInteger.valueOf(contentBytes.length),
                "text/plain", new ByteArrayInputStream(contentBytes));

        ObjectId newVersionId = pwc.checkIn(true, null, contentStream, "test version " + version);

        IOUtils.closeQuietly(contentStream);

        Document newVersion = (Document) session.getObject(newVersionId, SELECT_ALL_NO_CACHE_OC);

        addResult(checkObject(session, newVersion, getAllProperties(newVersion), "Version " + version + " compliance"));

        // check version history
        List<Document> versions = doc.getAllVersions();

        f = createResult(FAILURE, "Version series should have " + version + " versions but has " + versions.size()
                + "!");
        addResult(assertEquals(version, versions.size(), null, f));

        if (!versions.isEmpty()) {
            f = createResult(FAILURE, "Newly created version " + version + " is not the latest version!");
            addResult(assertEquals(newVersion.getId(), versions.get(0).getId(), null, f));

            if (versions.size() > 1) {
                f = createResult(FAILURE, "The previous version of version " + version
                        + " is not the document it has been created from!");
                addResult(assertEquals(doc.getId(), versions.get(1).getId(), null, f));
            }
        }

        return newVersion;
    }

    private void deleteVersion(Document versionDoc, Document previousDoc, int version) {
        CmisTestResult f;

        // check Allowable Action
        if (!versionDoc.hasAllowableAction(Action.CAN_DELETE_OBJECT)) {
            addResult(createResult(WARNING, "Version " + version
                    + " does not have the Allowable Action 'canDeleteObject'."));
            return;
        }

        // get version history before delete
        List<Document> versionsBefore = versionDoc.getAllVersions();

        // delete and check
        try {
            versionDoc.delete(false);
        } catch (CmisInvalidArgumentException iae) {
            addResult(createResult(WARNING, "Deletion of version " + version
                    + " failed with an invalidArgument exception. "
                    + "Removing just one version doesn't seem to be supported."));
            return;
        } catch (CmisConstraintException ce) {
            addResult(createResult(WARNING, "Deletion of version " + version + " failed with an constraint exception. "
                    + "Removing just one version doesn't seem to be supported."));
            return;
        }

        f = createResult(FAILURE, "Deleted version " + version + " still exists!");
        addResult(assertIsFalse(exists(versionDoc), null, f));

        // check version history after delete
        if (previousDoc != null) {
            List<Document> versionsAfter = previousDoc.getAllVersions();

            f = createResult(FAILURE, "After version " + version
                    + " has been deleted, the version history should consist of " + (versionsBefore.size() - 1)
                    + "  documents but is has " + versionsAfter.size() + " !");
            addResult(assertEquals(versionsBefore.size() - 1, versionsAfter.size(), null, f));
        }
    }
}
