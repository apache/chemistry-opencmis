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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

public class VersioningStateCreateTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Versioning State Create Test");
        setDescription("Creates documents in different versioing states.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        try {
            // create folder and document
            Folder testFolder = createTestFolder(session);

            DocumentTypeDefinition docType = (DocumentTypeDefinition) session
                    .getTypeDefinition(getDocumentTestTypeId());

            if (!docType.isVersionable()) {
                addResult(createResult(SKIPPED, "Test type is not versionable. Test skipped!"));
                return;
            }

            // major version
            Document docMajor = testFolder.createDocument(getProperties("major.txt"), getContentStream(),
                    VersioningState.MAJOR, null, null, null, SELECT_ALL_NO_CACHE_OC);
            addResult(checkObject(session, docMajor, getAllProperties(docMajor), "Major version compliance"));

            f = createResult(FAILURE, "Document should be major version.");
            addResult(assertIsTrue(docMajor.isMajorVersion(), null, f));

            List<Document> versions = docMajor.getAllVersions();

            f = createResult(FAILURE, "Version series should have one version but has " + versions.size() + ".");
            addResult(assertEquals(1, versions.size(), null, f));

            deleteObject(docMajor);

            // minor version
            try {
                Document docMinor = testFolder.createDocument(getProperties("minor.txt"), getContentStream(),
                        VersioningState.MINOR, null, null, null, SELECT_ALL_NO_CACHE_OC);
                addResult(checkObject(session, docMinor, getAllProperties(docMinor), "Minor version compliance"));

                f = createResult(FAILURE, "Document should be minor version.");
                addResult(assertIsFalse(docMinor.isMajorVersion(), null, f));

                versions = docMinor.getAllVersions();

                f = createResult(FAILURE, "Version series should have one version but has " + versions.size() + ".");
                addResult(assertEquals(1, versions.size(), null, f));

                deleteObject(docMinor);
            } catch (CmisConstraintException ce) {
                addResult(createResult(WARNING, "Creating a minor version failed! "
                        + "The repository might not support minor versions. Exception: " + ce, ce, false));
            } catch (CmisInvalidArgumentException iae) {
                addResult(createResult(WARNING, "Creating a minor version failed! "
                        + "The repository might not support minor versions.  Exception: " + iae, iae, false));
            }

            // checked out version
            try {
                Document docCheckedOut = testFolder.createDocument(getProperties("checkout.txt"), getContentStream(),
                        VersioningState.CHECKEDOUT, null, null, null, SELECT_ALL_NO_CACHE_OC);
                addResult(checkObject(session, docCheckedOut, getAllProperties(docCheckedOut),
                        "Checked out version compliance"));

                f = createResult(FAILURE, "Version series should be checked out.");
                addResult(assertIsTrue(docCheckedOut.isVersionSeriesCheckedOut(), null, f));

                versions = docCheckedOut.getAllVersions();

                f = createResult(FAILURE, "Version series should have one version but has " + versions.size() + ".");
                addResult(assertEquals(1, versions.size(), null, f));

                docCheckedOut.cancelCheckOut();
            } catch (CmisConstraintException ce) {
                addResult(createResult(WARNING, "Creating a checked out version failed! "
                        + "The repository might not support creating checked out versions. Exception: " + ce, ce, false));
            } catch (CmisInvalidArgumentException iae) {
                addResult(createResult(WARNING, "Creating a checked out version failed! "
                        + "The repository might not  support creating checked out versions.  Exception: " + iae, iae,
                        false));
            }

        } finally {
            deleteTestFolder();
        }
    }

    private Map<String, Object> getProperties(String name) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, name);
        properties.put(PropertyIds.OBJECT_TYPE_ID, getDocumentTestTypeId());

        return properties;
    }

    private ContentStream getContentStream() {
        byte[] contentBytes = null;
        try {
            contentBytes = "some content".getBytes("UTF-8");
        } catch (Exception e) {
            contentBytes = "some content".getBytes();
        }

        return new ContentStreamImpl("content.txt", BigInteger.valueOf(contentBytes.length), "text/plain",
                new ByteArrayInputStream(contentBytes));
    }
}