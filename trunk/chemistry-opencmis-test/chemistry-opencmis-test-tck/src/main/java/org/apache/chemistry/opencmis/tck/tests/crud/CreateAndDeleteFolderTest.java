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
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Simple folder test.
 */
public class CreateAndDeleteFolderTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Create and Delete Folder Test");
        setDescription("Creates a few folders, checks the newly created folders and their parent and finally deletes the created folders.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        int numOfFolders = 20;

        // create a test folder
        Folder testFolder = createTestFolder(session);

        try {
            Map<String, Folder> folders = new HashMap<String, Folder>();

            // create folders
            for (int i = 0; i < numOfFolders; i++) {
                Folder newFolder = createFolder(session, testFolder, "folder" + i);
                folders.put(newFolder.getId(), newFolder);
            }

            // simple children test
            addResult(checkChildren(session, testFolder, "Test folder children check"));

            // check if all folders are there
            ItemIterable<CmisObject> children = testFolder.getChildren(SELECT_ALL_NO_CACHE_OC);
            List<String> childrenIds = new ArrayList<String>();
            for (CmisObject child : children) {
                if (child != null) {
                    childrenIds.add(child.getId());
                    Folder folder = folders.get(child.getId());

                    f = createResult(FAILURE, "Folder and test folder child don't match! Id: " + child.getId());
                    addResult(assertShallowEquals(folder, child, null, f));
                }
            }

            f = createResult(FAILURE, "Number of created folders does not match the number of existing folders!");
            addResult(assertEquals(numOfFolders, childrenIds.size(), null, f));

            for (Folder folder : folders.values()) {
                if (!childrenIds.contains(folder.getId())) {
                    addResult(createResult(FAILURE,
                            "Created folder not found in test folder children! Id: " + folder.getId()));
                }
            }

            // delete all folders
            for (Folder folder : folders.values()) {
                // empty folders should be deleteable like this
                folder.delete(true);

                f = createResult(FAILURE,
                        "Folder should not exist anymore but it is still there! Id: " + folder.getId());
                addResult(assertIsFalse(exists(folder), null, f));
            }
        } finally {
            // delete the test folder
            deleteTestFolder();
        }

        addResult(createInfoResult("Tested the creation and deletion of " + numOfFolders + " folders."));
    }
}
