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
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.OK;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.CmisTestResultImpl;

/**
 * Query smoke test.
 */
public class QuerySmokeTest extends AbstractQueryTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Query Smoke Test");
        setDescription("Performs a simple query and checks if the format of the results is correct. It does not check if the results are complete!");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        if (supportsQuery(session)) {
            String testType = "cmis:document";
            String statement = "SELECT * FROM " + testType;

            addResult(createInfoResult("Query: " + statement));

            ObjectType type = session.getTypeDefinition(testType);

            f = createResult(FAILURE, "Test type definition '" + testType + "' not found!");
            addResult(assertNotNull(type, null, f));
            if (type == null) {
                return;
            }

            PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);

            f = createResult(FAILURE, "Object Id property definition does not exist!");
            addResult(assertNotNull(objectIdPropDef, null, f));

            String objectIdQueryName = null;
            if (objectIdPropDef != null) {
                objectIdQueryName = objectIdPropDef.getQueryName();
            }

            int pageSize = 100;

            ItemIterable<QueryResult> resultSet = session.query(statement, false);

            if (resultSet == null) {
                addResult(createResult(FAILURE, "Query result set is null! (OpenCMIS issue???)"));
            } else {
                int i = 0;
                // testing 100 results should be sufficient for this test
                ItemIterable<QueryResult> queryIterable = resultSet.getPage(pageSize);
                for (QueryResult qr : queryIterable) {
                    if (qr == null) {
                        addResult(createResult(FAILURE, "Query result is null! (OpenCMIS issue???)"));
                    } else {
                        addResult(checkQueryResult(session, qr, type, "Query result: " + i));

                        if (objectIdQueryName != null) {
                            String objectId = (String) qr.getPropertyByQueryName(objectIdQueryName).getFirstValue();

                            try {
                                CmisObject object = session.getObject(objectId, SELECT_ALL_NO_CACHE_OC);
                                addResult(checkObject(session, object, getAllProperties(object),
                                        "Query hit check. Id: " + objectId));
                            } catch (CmisObjectNotFoundException e) {
                                addResult(createResult(FAILURE,
                                        "Query hit references an object that doesn't exist. Id: " + objectId, e, false));
                            }
                        }
                        // TODO: check more
                    }
                    i++;
                }

                f = createResult(FAILURE, "More query results (" + i + ") than expected (page size = " + pageSize
                        + ")!");
                addResult(assertIsFalse((i > pageSize), null, f));

                if (queryIterable.getTotalNumItems() == -1) {
                    addResult(createResult(WARNING, "Repository did not return numItems."));
                }

                addResult(createInfoResult(i + " query results for \"" + statement + "\" (page size = " + pageSize
                        + ")"));
            }
        } else {
            addResult(createResult(SKIPPED, "Query not supported. Test Skipped!"));
        }
    }

    protected CmisTestResult checkQueryResult(Session session, QueryResult qr, ObjectType type, String message) {
        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if (qr.getProperties().isEmpty()) {
            addResult(results, createResult(FAILURE, "Query result is empty!"));
        } else {
            for (PropertyDefinition<?> propDef : type.getPropertyDefinitions().values()) {
                if (propDef.getQueryName() == null) {
                    continue;
                }

                PropertyData<?> pd = qr.getPropertyByQueryName(propDef.getQueryName());

                if (pd == null) {
                    addResult(results,
                            createResult(FAILURE, "Query property not in result set: " + propDef.getQueryName()));
                } else {
                    if (PropertyIds.OBJECT_ID.equals(propDef.getId())
                            || PropertyIds.OBJECT_TYPE_ID.equals(propDef.getId())
                            || PropertyIds.BASE_TYPE_ID.equals(propDef.getId())) {
                        f = createResult(FAILURE, "Query property must not be empty: " + propDef.getQueryName());
                        addResult(results, assertStringNotEmpty((String) pd.getFirstValue(), null, f));
                    }
                }
            }
        }

        CmisTestResultImpl result = createResult(getWorst(results), message);
        result.getChildren().addAll(results);

        return result.getStatus().getLevel() <= OK.getLevel() ? null : result;
    }
}
