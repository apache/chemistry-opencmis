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
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.INFO;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

public class BulkUpdatePropertiesTest extends AbstractSessionTest {

    private static final String CONTENT = "Bluk update test content.";
    private static final String NEW_NAME = "bunewname.txt";

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Bulk Update Properties Test");
        setDescription("Creates a few folders and documents, renames all documents at once, and deletes all created objects.");
    }

    @Override
    public void run(Session session) {
        if (session.getRepositoryInfo().getCmisVersion() == CmisVersion.CMIS_1_0) {
            addResult(createResult(SKIPPED, "Bulk Update Properties is not supported by CMIS 1.0. Test skipped!"));
            return;
        }

        CmisTestResult failure = null;
        int numOfObjects = 20;

        // create a test folder
        Folder testFolder = createTestFolder(session);

        try {
            Map<String, Folder> folders = new HashMap<String, Folder>();
            Map<String, Document> documents = new HashMap<String, Document>();

            // create folders and documents
            for (int i = 0; i < numOfObjects; i++) {
                Folder newFolder = createFolder(session, testFolder, "bufolder" + i);
                folders.put(newFolder.getId(), newFolder);
                Document newDocument = createDocument(session, newFolder, "budoc" + i + ".txt", CONTENT);
                documents.put(newDocument.getId(), newDocument);
            }

            // update cmis:name of all the documents
            List<CmisObject> objects = new ArrayList<CmisObject>(documents.values());
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(PropertyIds.NAME, NEW_NAME);

            List<BulkUpdateObjectIdAndChangeToken> updatedIds = session.bulkUpdateProperties(objects, properties, null,
                    null);

            // check the result
            if (getBinding() == BindingType.WEBSERVICES) {
                // TODO: review after TC clarification
                addResult(createResult(INFO, "The Web Services binding does not return the updated ids."
                        + " This issue has to be clarified by the CMIS TC and the test to adopted later."));
            } else {
                if (updatedIds == null || updatedIds.isEmpty()) {
                    addResult(createResult(FAILURE, "Bulk Update Properties did not update any documents!"));
                } else {
                    failure = createResult(FAILURE, "Bulk Update Properties did not update all test documents!");
                    addResult(assertEquals(documents.size(), updatedIds.size(), null, failure));
                }
            }

            // check all documents
            for (Folder folder : folders.values()) {
                List<CmisObject> children = new ArrayList<CmisObject>();
                for (CmisObject child : folder.getChildren(SELECT_ALL_NO_CACHE_OC)) {
                    children.add(child);
                }

                if (children.size() != 1) {
                    addResult(createResult(FAILURE,
                            "Test folder should have exactly one child, but it has " + children.size() + "!"));
                } else {
                    failure = createResult(FAILURE, "Document does not have the new name! Id: "
                            + children.get(0).getId());
                    addResult(assertEquals(NEW_NAME, children.get(0).getName(), null, failure));
                }
            }

            // delete folders and documents
            for (Folder folder : folders.values()) {
                folder.deleteTree(true, null, true);
            }
        } finally {
            // delete the test folder
            deleteTestFolder();
        }

    }
}
