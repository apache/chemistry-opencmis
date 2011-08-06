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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;
import org.apache.chemistry.opencmis.tck.impl.CmisTestResultImpl;

/**
 * Query smoke test.
 */
public class QuerySmokeTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Query Smoke Test");
    }

    @Override
    public void run(Session session) {
        String testType = "cmis:document";

        if (supportsQuery(session)) {
            ItemIterable<QueryResult> resultSet = session.query("SELECT * FROM " + testType, false);

            if (resultSet == null) {
                addResult(createResult(FAILURE, "Query result set is null! (OpenCMIS issue???)"));
            } else {
                int i = 0;
                // testing 100 results should be sufficient for this test
                for (QueryResult qr : resultSet.getPage(100)) {
                    if (qr == null) {
                        addResult(createResult(FAILURE, "Query result is null! (OpenCMIS issue???)"));
                    } else {
                        addResult(checkQueryResult(session, qr, testType, "Query result: " + i));

                        // TODO: check more
                    }
                    i++;
                }
            }
        } else {
            addResult(createInfoResult("Query not supported!"));
        }
    }

    protected CmisTestResult checkQueryResult(Session session, QueryResult qr, String typeId, String message) {
        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if (qr.getProperties().isEmpty()) {
            addResult(results, createResult(FAILURE, "Query result is empty!"));
        } else {
            TypeDefinition type = session.getTypeDefinition(typeId);

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

        return (result.getStatus().getLevel() <= OK.getLevel() ? null : result);
    }

    protected boolean supportsQuery(Session session) {
        RepositoryInfo repository = session.getRepositoryInfo();

        if (repository.getCapabilities().getQueryCapability() == null) {
            return false;
        }

        return repository.getCapabilities().getQueryCapability() != CapabilityQuery.NONE;
    }
}
