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
package org.apache.chemistry.opencmis.tck.tests.crud;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Simple document test.
 */
public class CreateAndDeleteDocumentTest extends AbstractSessionTest {

    private static final String CONTENT = "TCK test content.";

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Create and Delete Document Test");
        setDescription("Creates a few documents, checks the newly created documents and their parent and finally deletes the created documents.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        // create a test folder
        Folder testFolder = createTestFolder(session);

        int numOfDocuments = 20;
        Map<String, Document> documents = new HashMap<String, Document>();
        String[] propertiesToCheck = new String[] { PropertyIds.OBJECT_ID, PropertyIds.BASE_TYPE_ID,
                PropertyIds.OBJECT_TYPE_ID };

        // create documents
        for (int i = 0; i < numOfDocuments; i++) {
            Document newDocument = createDocument(session, testFolder, "doc" + i, CONTENT);
            addResult(checkObject(session, newDocument, propertiesToCheck, "New folder object spec compliance. Id: "
                    + newDocument.getId()));
            documents.put(newDocument.getId(), newDocument);
        }

        // simple children test
        addResult(checkChildren(session, testFolder, "Test folder children check"));

        // check if all documents are there
        ItemIterable<CmisObject> children = testFolder.getChildren(SELECT_ALL_NO_CACHE_OC);
        List<String> childrenIds = new ArrayList<String>();
        for (CmisObject child : children) {
            if (child != null) {
                childrenIds.add(child.getId());
                Document document = documents.get(child.getId());

                f = createResult(FAILURE, "Document and test folder child don't match! Id: " + child.getId());
                addResult(assertShallowEquals(document, child, null, f));
            }
        }

        f = createResult(FAILURE, "Number of created folders does not match the number of existing folders!");
        addResult(assertEquals(numOfDocuments, childrenIds.size(), null, f));

        for (Document document : documents.values()) {
            if (!childrenIds.contains(document.getId())) {
                addResult(createResult(FAILURE,
                        "Created document not found in test folder children! Id: " + document.getId()));
            }
        }

        // check content
        for (Document document : documents.values()) {
            ContentStream contentStream = document.getContentStream();
            if (contentStream == null) {
                addResult(createResult(FAILURE, "Document has no content! Id: " + document.getId()));
                continue;
            }

            // TODO: content checks
        }

        // delete all documents
        for (Document document : documents.values()) {
            document.delete(true);

            f = createResult(FAILURE,
                    "Document should not exist anymore but it is still there! Id: " + document.getId());
            addResult(assertIsFalse(exists(document), null, f));
        }

        // delete the test folder
        deleteTestFolder();

        addResult(createInfoResult("Tested the creation and deletion of " + numOfDocuments + " documents."));
    }
}
