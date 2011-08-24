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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Simple document test.
 */
public class DeleteTreeTest extends AbstractSessionTest {

    private static final String CONTENT = "TCK test content.";

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Delete Tree Test");
        setDescription("Creates a few documents in a folder, deletes the folder and checks if all documents are gone.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        int numOfDocuments = 20;

        // create a test folder
        Folder testFolder = createTestFolder(session);

        Map<String, Document> documents = new HashMap<String, Document>();

        // create documents
        for (int i = 0; i < numOfDocuments; i++) {
            Document newDocument = createDocument(session, testFolder, "doc" + i, CONTENT);
            documents.put(newDocument.getId(), newDocument);
        }

        // delete tree
        List<String> failedIds = testFolder.deleteTree(true, UnfileObject.DELETE, true);

        // check failed ids
        if (failedIds != null && failedIds.size() > 0) {
            f = createResult(FAILURE, "deleteTree() could not delete " + failedIds.size() + " out of " + numOfDocuments
                    + " objects in the folder!");
            addResult(assertEquals(0, failedIds.size(), null, f));
        }

        // check documents
        for (Document doc : documents.values()) {
            f = createResult(FAILURE, "Document still exists but should have been deleted. Id: " + doc.getId());
            addResult(assertIsFalse(exists(doc), null, f));
        }

        // check folder
        f = createResult(FAILURE, "Folder still exists but should have been deleted. Id: " + testFolder.getId());
        addResult(assertIsFalse(exists(testFolder), null, f));

        if (exists(testFolder)) {
            // try to clean up
            try {
                deleteObject(testFolder);
            } catch (Exception e) {
            }
        }
    }
}
