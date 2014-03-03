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

import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.tck.CmisTestResult;

/**
 * Query LIKE test.
 */
public class QueryLikeTest extends AbstractQueryTest {

    private static final String CONTENT = "TCK test content.";
    private static final int PAGE_SIZE = 10;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Query LIKE Test");
        setDescription("Performs a LIKE query and checks if only matching objects are returned.");
    }

    @Override
    public void run(Session session) {
        if (supportsQuery(session) && !isFulltextOnly(session)) {
            // create a test folder
            Folder testFolder = createTestFolder(session);

            try {
                for (char c = 'a'; c <= 'z'; c++) {
                    createDocument(session, testFolder, c + "Document", CONTENT);
                    createFolder(session, testFolder, c + "Folder");
                }

                OperationContext context = session.createOperationContext();
                context.setFilterString("cmis:name,cmis:creationDate");
                context.setCacheEnabled(false);
                context.setIncludeAcls(false);
                context.setIncludeAllowableActions(false);
                context.setIncludePathSegments(false);
                context.setIncludePolicies(false);
                context.setIncludeRelationships(IncludeRelationships.NONE);
                context.setRenditionFilterString("cmis:none");
                context.setOrderBy("cmis:creationDate");

                CmisTestResult f;

                for (char c = 'a'; c <= 'z'; c++) {
                    // query documents
                    long timestamp = Long.MIN_VALUE;
                    long count = 0;

                    for (CmisObject o : session.queryObjects("cmis:document", "cmis:name LIKE '" + c + "%'", false,
                            context).getPage(PAGE_SIZE)) {

                        if (o.getName() == null || o.getName().length() == 0) {
                            addResult(createResult(
                                    FAILURE,
                                    "Documents without name should not be returned by this query! Document ID: "
                                            + o.getId()));
                        } else {
                            f = createResult(FAILURE, "Document name should start with '" + c + "' but the name is '"
                                    + o.getName() + "'.");
                            addResult(assertEquals(c, Character.toLowerCase(o.getName().charAt(0)), null, f));
                        }

                        if (o.getCreationDate() == null) {
                            addResult(createResult(FAILURE,
                                    "Found document without creation date! Document ID: " + o.getId()));
                        } else {
                            f = createResult(FAILURE,
                                    "Query results should be ordered by cmis:creationDate but they are not!");
                            addResult(assertIsTrue(timestamp <= o.getCreationDate().getTimeInMillis(), null, f));

                            timestamp = o.getCreationDate().getTimeInMillis();
                        }

                        count++;
                    }

                    f = createResult(FAILURE, "No documents starting with '" + c
                            + "' have been found, but there must be at least one!");
                    addResult(assertIsTrue(count > 0, null, f));

                    f = createResult(FAILURE, "A page of " + PAGE_SIZE
                            + " query hits has been requested, but the repository returned " + count + ".");
                    addResult(assertIsTrue(count <= PAGE_SIZE, null, f));

                    // query folders
                    timestamp = Long.MIN_VALUE;
                    count = 0;

                    for (CmisObject o : session.queryObjects("cmis:folder", "cmis:name LIKE '" + c + "%'", false,
                            context).getPage(PAGE_SIZE)) {

                        if (o.getName() == null || o.getName().length() == 0) {
                            addResult(createResult(FAILURE,
                                    "Folder without name should not be returned by this query! Folder ID: " + o.getId()));
                        } else {
                            f = createResult(FAILURE,
                                    "Folder name should start with '" + c + "' but the name is '" + o.getName() + "'.");
                            addResult(assertEquals(c, Character.toLowerCase(o.getName().charAt(0)), null, f));
                        }

                        if (o.getCreationDate() == null) {
                            addResult(createResult(FAILURE,
                                    "Found folder without creation date! Folder ID: " + o.getId()));
                        } else {
                            f = createResult(FAILURE,
                                    "Query results should be ordered by cmis:creationDate but they are not!");
                            addResult(assertIsTrue(timestamp <= o.getCreationDate().getTimeInMillis(), null, f));

                            timestamp = o.getCreationDate().getTimeInMillis();
                        }

                        count++;
                    }

                    f = createResult(FAILURE, "No folders starting with '" + c
                            + "' have been found, but there must be at least one!");
                    addResult(assertIsTrue(count > 0, null, f));

                    f = createResult(FAILURE, "A page of " + PAGE_SIZE
                            + " query hits has been requested, but the repository returned " + count + ".");
                    addResult(assertIsTrue(count <= PAGE_SIZE, null, f));
                }
            } finally {
                // delete the test folder
                deleteTestFolder();
            }
        } else {
            addResult(createResult(SKIPPED, "Metadata query not supported. Test Skipped!"));
        }
    }
}
