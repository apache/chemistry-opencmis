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
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Move test.
 */
public class MoveTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Move Test");
        setDescription("Creates two folders and a document and moves the document from one folder to the other.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        try {
            // create folders
            Folder testFolder = createTestFolder(session);
            Folder folder1 = createFolder(session, testFolder, "movefolder1");
            Folder folder2 = createFolder(session, testFolder, "movefolder2");

            // create document
            Document doc1 = createDocument(session, folder1, "movetestdoc.txt", "move test");

            // move
            Document doc2 = (Document) doc1.move(folder1, folder2, SELECT_ALL_NO_CACHE_OC);

            if (doc2 == null) {
                addResult(createResult(FAILURE, "Moved document is null!"));
            } else {
                addResult(checkObject(session, doc2, getAllProperties(doc2),
                        "Moved document check. Id: + " + doc2.getName()));
            }

            int count1 = countFolderChildren(folder1);
            f = createResult(FAILURE, "Source folder should be empty after move but has " + count1 + " children!");
            addResult(assertEquals(0, count1, null, f));

            int count2 = countFolderChildren(folder2);
            f = createResult(FAILURE, "Target folder should have exactly one child but has " + count2 + " children!");
            addResult(assertEquals(1, count2, null, f));
        } finally {
            // clean up
            deleteTestFolder();
        }
    }
}
