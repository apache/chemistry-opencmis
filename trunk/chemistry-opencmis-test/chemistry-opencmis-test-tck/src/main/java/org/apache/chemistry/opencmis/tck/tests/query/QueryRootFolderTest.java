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

import java.util.GregorianCalendar;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.QueryStatement;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.tck.CmisTestResult;

/**
 * Root folder query test.
 */
public class QueryRootFolderTest extends AbstractQueryTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Query Root Folder Test");
        setDescription("Performs a query that should return the root folder name and id.");
    }

    @Override
    public void run(Session session) {
        if (supportsQuery(session) && !isFulltextOnly(session)) {
            if (Boolean.TRUE.equals(session.getRootFolder().getType().isQueryable())) {
                queryById(session);
                queryByDate(session);
            } else {
                addResult(createResult(SKIPPED, "Root folder type (" + session.getRootFolder().getType().getId()
                        + ") is not queryable. Test Skipped!"));
            }
        } else {
            addResult(createResult(SKIPPED, "Metadata query not supported. Test Skipped!"));
        }
    }

    protected void queryById(Session session) {
        CmisTestResult f;

        String testType = "cmis:folder";

        Folder rootFolder = session.getRootFolder();

        QueryStatement statement = session
                .createQueryStatement("SELECT ? AS folderName, ? AS folderId FROM ? WHERE ? = ?");

        statement.setProperty(1, testType, PropertyIds.NAME);
        statement.setProperty(2, testType, PropertyIds.OBJECT_ID);
        statement.setType(3, testType);
        statement.setProperty(4, testType, PropertyIds.OBJECT_ID);
        statement.setString(5, rootFolder.getId());

        addResult(createInfoResult("Query: " + statement.toQueryString()));

        int count = 0;
        ItemIterable<QueryResult> results = statement.query(false);
        for (QueryResult qr : results) {
            count++;

            String folderName = qr.getPropertyValueByQueryName("folderName");
            String folderId = qr.getPropertyValueByQueryName("folderId");

            f = createResult(FAILURE, "Query result does not match root folder name!");
            addResult(assertEquals(rootFolder.getName(), folderName, null, f));

            f = createResult(FAILURE, "Query result does not match root folder id!");
            addResult(assertEquals(rootFolder.getId(), folderId, null, f));
        }

        f = createResult(FAILURE, "The query should return exactly one result but returned " + count + "!");
        addResult(assertEquals(1, count, null, f));

        if (results.getTotalNumItems() >= 0) {
            f = createResult(FAILURE,
                    "The query returned a total number of items != 1, but there can be only exactly one hit!");
            addResult(assertEquals(1L, results.getTotalNumItems(), null, f));
        }
    }

    protected void queryByDate(Session session) {
        CmisTestResult f;

        String testType = "cmis:folder";

        Folder rootFolder = session.getRootFolder();

        GregorianCalendar before = new GregorianCalendar();
        before.setTimeInMillis(rootFolder.getCreationDate().getTimeInMillis() - (60 * 60 * 1000));

        GregorianCalendar after = new GregorianCalendar();
        after.setTimeInMillis(rootFolder.getCreationDate().getTimeInMillis() + (60 * 60 * 1000));

        QueryStatement statement = session
                .createQueryStatement("SELECT ? AS folderName, ? AS folderId FROM ? WHERE ? > TIMESTAMP ? AND ? < TIMESTAMP ?");

        statement.setProperty(1, testType, PropertyIds.NAME);
        statement.setProperty(2, testType, PropertyIds.OBJECT_ID);
        statement.setType(3, testType);
        statement.setProperty(4, testType, PropertyIds.CREATION_DATE);
        statement.setDateTime(5, before);
        statement.setProperty(6, testType, PropertyIds.CREATION_DATE);
        statement.setDateTime(7, after);

        addResult(createInfoResult("Query: " + statement.toQueryString()));

        boolean found = false;
        ItemIterable<QueryResult> results = statement.query(false);
        for (QueryResult qr : results) {
            String folderId = qr.getPropertyValueByQueryName("folderId");

            if (rootFolder.getId().equals(folderId)) {
                found = true;

                String folderName = qr.getPropertyValueByQueryName("folderName");

                f = createResult(FAILURE, "Query result does not match root folder name!");
                addResult(assertEquals(rootFolder.getName(), folderName, null, f));
                break;
            }
        }

        f = createResult(FAILURE, "The query should return the root folder but does not!");
        addResult(assertIsTrue(found, null, f));

        if (results.getTotalNumItems() >= 0) {
            f = createResult(FAILURE,
                    "The query returned a total number of items < 1, but there must be at least one hit!");
            addResult(assertIsTrue(results.getTotalNumItems() >= 1L, null, f));
        }
    }
}
