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
 * Multifiling test.
 */
public class MultifilingTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Multifiling Test");
        setDescription("Creates two folders and a document in one of the folders, "
                + "adds the document to the second folder and then removes it again for the second folder.");
    }

    @Override
    public void run(Session session) {
        if (supportsMultifiling(session)) {
            CmisTestResult f;

            int count1;
            int count2;
            int parents;

            try {
                // create folders
                Folder testFolder = createTestFolder(session);
                Folder folder1 = createFolder(session, testFolder, "folder1");
                Folder folder2 = createFolder(session, testFolder, "folder2");

                // create document
                Document doc1 = createDocument(session, folder1, "testdoc.txt", "multifiling test");

                addResult(checkChildren(session, folder1, "Folder 1 after createDocument()"));
                addResult(checkChildren(session, folder2, "Folder 2 after createDocument()"));

                count1 = countFolderChildren(folder1);
                f = createResult(FAILURE, "Folder 1 should have exactly one child but has " + count1 + " children!");
                addResult(assertEquals(1, count1, null, f));

                count2 = countFolderChildren(folder2);
                f = createResult(FAILURE, "Folder 2 should not have children but has " + count2 + " children!");
                addResult(assertEquals(0, count2, null, f));

                parents = doc1.getParents().size();
                f = createResult(FAILURE, "Document should have one parent but has " + parents + " parents!");
                addResult(assertEquals(1, parents, null, f));

                // add to other folder
                doc1.addToFolder(folder2, true);

                addResult(checkChildren(session, folder1, "Folder 1 after addToFolder()"));
                addResult(checkChildren(session, folder2, "Folder 2 after addToFolder()"));

                count1 = countFolderChildren(folder1);
                f = createResult(FAILURE, "Folder 1 should have exactly one child but has " + count1 + " children!");
                addResult(assertEquals(1, count1, null, f));

                count2 = countFolderChildren(folder2);
                f = createResult(FAILURE, "Folder 2 should have exactly one child but has " + count2 + " children!");
                addResult(assertEquals(1, count2, null, f));

                parents = doc1.getParents().size();
                f = createResult(FAILURE, "Document should have two parents but has " + parents + " parents!");
                addResult(assertEquals(2, parents, null, f));

                // remove from first folder
                doc1.removeFromFolder(folder2);

                addResult(checkChildren(session, folder1, "Folder 1 after removeFromFolder()"));
                addResult(checkChildren(session, folder2, "Folder 2 after removeFromFolder()"));

                count1 = countFolderChildren(folder1);
                f = createResult(FAILURE, "Folder 1 should have exactly one child but has " + count1 + " children!");
                addResult(assertEquals(1, count1, null, f));

                count2 = countFolderChildren(folder2);
                f = createResult(FAILURE, "Folder 2 should not have children but has " + count2 + " children!");
                addResult(assertEquals(0, count2, null, f));

                parents = doc1.getParents().size();
                f = createResult(FAILURE, "Document should have one parent but has " + parents + " parents!");
                addResult(assertEquals(1, parents, null, f));

                // delete everything
                deleteObject(doc1);
                deleteObject(folder2);
                deleteObject(folder1);
            } finally {
                // clean up
                deleteTestFolder();
            }
        } else {
            addResult(createResult(SKIPPED, "Multifling not supported. Test Skipped!"));
        }
    }

    protected boolean supportsMultifiling(Session session) {
        RepositoryInfo repository = session.getRepositoryInfo();

        if (repository.getCapabilities().isMultifilingSupported() == null) {
            return false;
        }

        return repository.getCapabilities().isMultifilingSupported().booleanValue();
    }
}
