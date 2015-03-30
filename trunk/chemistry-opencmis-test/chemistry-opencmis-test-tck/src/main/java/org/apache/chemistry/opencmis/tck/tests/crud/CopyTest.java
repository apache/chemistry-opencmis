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

import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Copy test.
 */
public class CopyTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Copy Test");
        setDescription("Creates two folders and a document and copies the document from one folder to the other.");
    }

    @Override
    public void run(Session session) {
        // if (getBinding() == BindingType.ATOMPUB) {
        // addResult(createResult(SKIPPED,
        // "AtomPub binding does not support createDocumentFromSource(). Test Skipped!"));
        // return;
        // }

        CmisTestResult f;

        try {
            // create folders
            Folder testFolder = createTestFolder(session);
            Folder folder1 = createFolder(session, testFolder, "copyfolder1");
            Folder folder2 = createFolder(session, testFolder, "copyfolder2");

            // create document
            Document doc1 = createDocument(session, folder1, "copytestdoc.txt", "copy test");

            VersioningState versioningState = VersioningState.MAJOR;
            if (!((DocumentTypeDefinition) doc1.getType()).isVersionable()) {
                versioningState = VersioningState.NONE;
            }

            // copy
            Document doc2 = doc1.copy(folder2, null, versioningState, null, null, null, SELECT_ALL_NO_CACHE_OC);

            if (doc2 == null) {
                addResult(createResult(FAILURE, "Copied document is null!"));
            } else {
                addResult(checkObject(session, doc2, getAllProperties(doc2),
                        "Copied document check. Id: + " + doc2.getName()));

                f = createResult(FAILURE, "Content streams don't match!");
                addResult(assertEquals(doc1.getContentStream(), doc2.getContentStream(), null, f));
            }

            int count1 = countFolderChildren(folder1);
            f = createResult(FAILURE, "Source folder should have exactly one child but has " + count1 + " children!");
            addResult(assertEquals(1, count1, null, f));

            int count2 = countFolderChildren(folder2);
            f = createResult(FAILURE, "Target folder should have exactly one child but has " + count2 + " children!");
            addResult(assertEquals(1, count2, null, f));

            deleteObject(doc2);
            deleteObject(doc1);
        } finally {
            // clean up
            deleteTestFolder();
        }
    }
}
