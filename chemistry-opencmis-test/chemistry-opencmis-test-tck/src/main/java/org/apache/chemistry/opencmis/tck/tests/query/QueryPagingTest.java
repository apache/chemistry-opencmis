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

import java.util.Iterator;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.QueryStatement;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.tck.CmisTestResult;

public class QueryPagingTest extends AbstractQueryTest {

    private static final String CONTENT = "TCK test content.";

    private static final int NUM_DOCS = 20;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Query Paging Test");
        setDescription("Performs IN_FOLDER queries with pages and checks if the page sizes are correct.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        if (supportsQuery(session) && !isFulltextOnly(session)) {
            // create a test folder
            Folder testFolder = createTestFolder(session);

            try {
                // create documents
                for (int i = 0; i < NUM_DOCS; i++) {
                    createDocument(session, testFolder, "doc" + i, CONTENT);
                }

                // query the folder
                QueryStatement statement = session.createQueryStatement("SELECT ? FROM ? WHERE IN_FOLDER(?)");
                statement.setProperty(1, "cmis:document", PropertyIds.OBJECT_ID);
                statement.setType(2, "cmis:document");
                statement.setString(3, testFolder.getId());

                // first page
                ItemIterable<QueryResult> iter1 = statement.query(false).skipTo(0).getPage(5);
                int count1 = countResults(iter1);
                long total1 = iter1.getTotalNumItems();

                f = createResult(FAILURE,
                        "Repository returned more hits than requested for the first test page! (maxItems=5, returned="
                                + count1 + ")");
                addResult(assertIsTrue(count1 <= 5, null, f));

                f = createInfoResult("Repository did return fewer hits than requested for the first test page. (maxItems=5, returned="
                        + count1 + ")");
                addResult(assertIsTrue(count1 >= 5, null, f));

                if (total1 == -1) {
                    addResult(createInfoResult("Repository did not return numItems for the first test page."));
                } else {
                    f = createResult(FAILURE, "Returned numItems doesn't match the number of documents!");
                    addResult(assertEquals((long) NUM_DOCS, total1, null, f));
                }

                // second page
                ItemIterable<QueryResult> iter2 = statement.query(false).skipTo(5).getPage(10);
                int count2 = countResults(iter2);
                long total2 = iter2.getTotalNumItems();

                f = createResult(FAILURE,
                        "Repository returned more hits than requested for the second test page! (maxItems=10, returned="
                                + count2 + ")");
                addResult(assertIsTrue(count2 <= 10, null, f));

                f = createInfoResult("Repository did return fewer hits than requested for the second test page. (maxItems=5, returned="
                        + count2 + ")");
                addResult(assertIsTrue(count2 >= 10, null, f));

                if (total2 == -1) {
                    addResult(createInfoResult("Repository did not return numItems for the second test page."));
                } else {
                    f = createResult(FAILURE, "Returned numItems doesn't match the number of documents!");
                    addResult(assertEquals((long) NUM_DOCS, total2, null, f));
                }
            } finally {
                // delete the test folder
                deleteTestFolder();
            }
        } else {
            addResult(createResult(SKIPPED, "Metadata query not supported. Test Skipped!"));
        }
    }

    private int countResults(ItemIterable<QueryResult> iter) {
        int count = 0;
        for (Iterator<QueryResult> iterator = iter.iterator(); iterator.hasNext();) {
            iterator.next();
            count++;
        }

        return count;
    }
}
