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
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Checked out test.
 */
public class CheckedOutTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Checked out Test");
        setDescription("Calls getCheckedOutDocs() and checks the returned objects.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        Document pwc = null;
        try {
            // create folder and a checked-out document
            Folder testFolder = createTestFolder(session);
            Document doc = createDocument(session, testFolder, "checkedouttest.txt", "checked out");
            DocumentTypeDefinition docType = (DocumentTypeDefinition) doc.getType();

            if (!docType.isVersionable()) {
                addResult(createResult(WARNING, "Test type is not versionable. Check out skipped!"));
            } else {
                ObjectId pwcId = doc.checkOut();
                pwc = (Document) session.getObject(pwcId, SELECT_ALL_NO_CACHE_OC);
            }

            // test all checked-out documents
            int sessionCheckedOut = checkPWCs(session, session.getCheckedOutDocs(SELECT_ALL_NO_CACHE_OC_ORDER_BY_NAME));
            addResult(createInfoResult(sessionCheckedOut + " checked out document(s) overall."));

            if (pwc != null) {
                f = createResult(FAILURE, "There should be at least one checked out document in the repository!");
                addResult(assertIsTrue(sessionCheckedOut >= 1, null, f));
            }

            // test checked-out documents in the test folder
            int testFolderCheckedOut = checkPWCs(session,
                    testFolder.getCheckedOutDocs(SELECT_ALL_NO_CACHE_OC_ORDER_BY_NAME));
            addResult(createInfoResult(testFolderCheckedOut + " checked out document(s) in the test folder."));

            if (pwc != null) {
                f = createResult(FAILURE, "There should be at least one checked out document in the test folder!");
                addResult(assertIsTrue(testFolderCheckedOut >= 1, null, f));
            }

            // remove the PWC and document
            if (pwc != null) {
                pwc.cancelCheckOut();
                pwc = null;
            }
            deleteObject(doc);
        } finally {
            if (pwc != null) {
                pwc.cancelCheckOut();
            }
            deleteTestFolder();
        }
    }

    private int checkPWCs(Session session, ItemIterable<Document> pwcs) {
        if (pwcs == null) {
            return 0;
        }

        CmisTestResult f;

        int i = 0;
        int orderByNameIssues = 0;
        String lastName = null;

        for (Document pwc : pwcs) {
            String[] propertiesToCheck = getAllProperties(pwc);
            addResult(checkObject(session, pwc, propertiesToCheck, "PWC check: " + (pwc == null ? "?" : pwc.getId())));

            if (pwc != null) {
                f = createResult(WARNING, "PWC is not the latest version! Id: " + pwc.getId()
                        + " (Note: The words of the CMIS specification define that the PWC is the latest version."
                        + " But that is not the intention of the spec and will be changed in CMIS 1.1."
                        + " Thus this a warning, not an error.)");
                addResult(assertIsTrue(pwc.isLatestVersion(), null, f));

                if (lastName != null && pwc.getName() != null) {
                    if (pwc.getName().compareToIgnoreCase(lastName) < 0) {
                        orderByNameIssues++;
                    }
                }

                lastName = pwc.getName();
            }

            i++;
        }

        f = createResult(WARNING,
                "Checked-out documents should be ordered by cmis:name, but they are not! (It might be a collation mismatch.)");
        addResult(assertEquals(0, orderByNameIssues, null, f));

        return i;
    }
}
