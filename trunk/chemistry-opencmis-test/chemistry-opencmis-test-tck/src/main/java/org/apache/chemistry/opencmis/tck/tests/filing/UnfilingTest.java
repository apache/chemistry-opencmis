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
package org.apache.chemistry.opencmis.tck.tests.filing;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;

import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Unfiling test.
 */
public class UnfilingTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Unfiling Test");
        setDescription("Creates a folder and a document, removes the document from the folder and then adds it again.");
    }

    @Override
    public void run(Session session) {
        if (supportsUnfiling(session)) {
            CmisTestResult f;

            int count;
            int parents;

            try {
                // create folders
                Folder testFolder = createTestFolder(session);
                Folder folder1 = createFolder(session, testFolder, "folder1");

                // create document
                Document doc1 = createDocument(session, folder1, "testdoc.txt", "unfiling test");

                addResult(checkChildren(session, folder1, "Folder after createDocument()"));

                count = countFolderChildren(folder1);
                f = createResult(FAILURE, "Folder should have exactly one child but has " + count + " children!");
                addResult(assertEquals(1, count, null, f));

                parents = doc1.getParents().size();
                f = createResult(FAILURE, "Document should have one parent but has " + parents + " parents!");
                addResult(assertEquals(1, parents, null, f));

                // remove from folder
                doc1.removeFromFolder(folder1);

                addResult(checkChildren(session, folder1, "Folder after removeFromFolder()"));

                count = countFolderChildren(folder1);
                f = createResult(FAILURE, "Folder should have no children but has " + count + " children!");
                addResult(assertEquals(0, count, null, f));

                parents = doc1.getParents().size();
                f = createResult(FAILURE, "Document should not have no parents but has " + parents + " parents!");
                addResult(assertEquals(0, parents, null, f));

                // add to folder again
                doc1.addToFolder(folder1, true);

                addResult(checkChildren(session, folder1, "Folder after addToFolder()"));

                count = countFolderChildren(folder1);
                f = createResult(FAILURE, "Folder should have exactly one child but has " + count + " children!");
                addResult(assertEquals(1, count, null, f));

                parents = doc1.getParents().size();
                f = createResult(FAILURE, "Document should have one parent but has " + parents + " parents!");
                addResult(assertEquals(1, parents, null, f));

                // delete everything
                deleteObject(doc1);
                deleteObject(folder1);
            } finally {
                // clean up
                deleteTestFolder();
            }
        } else {
            addResult(createResult(SKIPPED, "Unfiling not supported. Test Skipped!"));
        }
    }

    protected boolean supportsUnfiling(Session session) {
        RepositoryInfo repository = session.getRepositoryInfo();

        if (repository.getCapabilities().isUnfilingSupported() == null) {
            return false;
        }

        return repository.getCapabilities().isUnfilingSupported().booleanValue();
    }
}
