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
package org.apache.chemistry.opencmis.tck.tests.query;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.QueryStatement;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.tck.CmisTestResult;

public class QueryInFolderTest extends AbstractQueryTest {

    private static final String CONTENT = "TCK test content.";

    private static final int LEVEL1_DOCS = 5;
    private static final int LEVEL1_FOLDERS = 5;
    private static final int LEVEL2_DOCS = 5;
    private static final int LEVEL2_FOLDERS = 5;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Query IN_FOLDER and IN_TREE Test");
        setDescription("Performs IN_FOLDER and IN_TREE queries.");
    }

    @Override
    public void run(Session session) {
        if (supportsQuery(session) && !isFulltextOnly(session)) {
            // create a test folder
            Folder testFolder = createTestFolder(session);

            try {
                Set<String> topLevelDocs = new HashSet<String>();
                Set<String> topLevelFolders = new HashSet<String>();

                // create documents
                for (int i = 0; i < LEVEL1_DOCS; i++) {
                    Document newDocument = createDocument(session, testFolder, "doc" + i, CONTENT);
                    topLevelDocs.add(newDocument.getId());
                }

                // create folders
                for (int i = 0; i < LEVEL1_FOLDERS; i++) {
                    Folder newFolder = createFolder(session, testFolder, "folder" + i);
                    topLevelFolders.add(newFolder.getId());

                    for (int j = 0; j < LEVEL2_DOCS; j++) {
                        createDocument(session, newFolder, "doc" + j, CONTENT);
                    }

                    for (int j = 0; j < LEVEL2_FOLDERS; j++) {
                        createFolder(session, newFolder, "folder" + j);
                    }
                }

                doQuery(session, testFolder, "cmis:document", false, topLevelDocs, topLevelFolders);
                doQuery(session, testFolder, "cmis:document", true, topLevelDocs, topLevelFolders);
                doQuery(session, testFolder, "cmis:folder", false, topLevelDocs, topLevelFolders);
                doQuery(session, testFolder, "cmis:folder", true, topLevelDocs, topLevelFolders);
            } finally {
                // delete the test folder
                deleteTestFolder();
            }
        } else {
            addResult(createResult(SKIPPED, "Metadata query not supported. Test Skipped!"));
        }
    }

    private void doQuery(Session session, ObjectId testFolder, String type, boolean deep, Set<String> topLevelDocs,
            Set<String> topLevelFolders) {
        CmisTestResult f;

        String inWhat = (deep ? "IN_TREE" : "IN_FOLDER");

        QueryStatement statement = session.createQueryStatement("SELECT ? FROM ? WHERE " + inWhat + "(?)");
        statement.setProperty(1, type, PropertyIds.OBJECT_ID);
        statement.setType(2, type);
        statement.setString(3, testFolder.getId());

        addResult(createInfoResult("Query: " + statement.toQueryString()));

        try {
            int count = 0;

            for (QueryResult qr : statement.query(false).getPage(100)) {
                count++;

                String objectId = qr.getPropertyValueByQueryName("cmis:objectId");

                f = createResult(FAILURE, inWhat + " query returned an invalid object ID!");
                addResult(assertStringNotEmpty(objectId, null, f));

                FileableCmisObject object = null;
                try {
                    object = (FileableCmisObject) session.getObject(objectId);
                } catch (CmisObjectNotFoundException onf) {
                    addResult(createResult(FAILURE, inWhat
                            + " query returned an object ID of an object that doesn't exist!"));
                }

                if (!deep && object != null) {
                    f = createResult(FAILURE, inWhat + " query returned an object that should not be there!");
                    addResult(assertIsTrue(
                            topLevelDocs.contains(object.getId()) || topLevelFolders.contains(object.getId()), null, f));

                    boolean found = false;
                    for (Folder parent : object.getParents()) {
                        if (testFolder.getId().equals(parent.getId())) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        addResult(createResult(FAILURE, inWhat
                                + " query returned an object, which hasn't the test folder as a parent folder!"));
                    }
                }
            }

            addResult(createInfoResult("Hits: " + count));
        } catch (CmisBaseException e) {
            addResult(createResult(FAILURE,
                    inWhat + " query failed: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e, false));
        }
    }
}
