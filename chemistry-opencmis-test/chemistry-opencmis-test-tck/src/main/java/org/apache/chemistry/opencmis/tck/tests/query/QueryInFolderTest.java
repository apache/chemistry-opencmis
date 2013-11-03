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

import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.QueryStatement;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.tck.CmisTestResult;

public class QueryInFolderTest extends AbstractQueryTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Query IN_FOLDER and IN_TREE Test");
        setDescription("Performs IN_FOLDER and IN_TREE queries.");
    }

    @Override
    public void run(Session session) {
        if (supportsQuery(session) && !isFulltextOnly(session)) {
            doQuery(session, "cmis:document", false);
            doQuery(session, "cmis:document", true);
            doQuery(session, "cmis:folder", false);
            doQuery(session, "cmis:folder", true);
        } else {
            addResult(createResult(SKIPPED, "Metadata query not supported. Test Skipped!"));
        }
    }

    private void doQuery(Session session, String type, boolean deep) {
        String inWhat = (deep ? "IN_TREE" : "IN_FOLDER");

        QueryStatement statement = session.createQueryStatement("SELECT ? FROM ? WHERE " + inWhat + "(?)");
        statement.setProperty(1, type, PropertyIds.OBJECT_ID);
        statement.setType(2, type);
        statement.setString(3, session.getRepositoryInfo().getRootFolderId());

        addResult(createInfoResult("Query: " + statement.toQueryString()));

        try {
            for (QueryResult qr : statement.query(false).getPage(10)) {
                String objectId = qr.getPropertyValueByQueryName("cmis:objectId");

                CmisTestResult f = createResult(FAILURE, inWhat + " query returned an invalid object ID!");
                addResult(assertStringNotEmpty(objectId, null, f));

                FileableCmisObject object = null;
                try {
                    object = (FileableCmisObject) session.getObject(objectId);
                } catch (CmisObjectNotFoundException onf) {
                    addResult(createResult(FAILURE, inWhat
                            + " query returned an object ID of an object that doesn't exist!"));
                }

                if (!deep && object != null) {
                    boolean found = false;
                    for (Folder parent : object.getParents()) {
                        if (session.getRepositoryInfo().getRootFolderId().equals(parent.getId())) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        addResult(createResult(FAILURE, inWhat
                                + " query returned an object, which hasn't the root folder as a parent folder!"));
                    }
                }
            }
        } catch (CmisBaseException e) {
            addResult(createResult(FAILURE,
                    inWhat + " query failed: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e, false));
        }
    }
}
