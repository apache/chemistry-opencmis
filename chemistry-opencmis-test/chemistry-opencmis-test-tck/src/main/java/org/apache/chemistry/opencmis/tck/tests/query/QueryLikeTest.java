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
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.tck.CmisTestResult;

public class QueryLikeTest extends AbstractQueryTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Query LIKE Test");
        setDescription("Performs a query that should return the root folder name and id.");
    }

    @Override
    public void run(Session session) {
        if (supportsQuery(session)) {

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
                long timestamp = Long.MIN_VALUE;

                for (CmisObject o : session
                        .queryObjects("cmis:document", "cmis:name LIKE '" + c + "%'", false, context).getPage(10)) {

                    if (o.getName() == null) {
                        addResult(createResult(
                                FAILURE,
                                "Documents without name should not be returned by this query! Document id: "
                                        + o.getId()));
                    } else {
                        f = createResult(FAILURE,
                                "Document name should start with '" + c + "' but the name is '" + o.getName() + "'");
                        addResult(assertIsTrue(o.getName().startsWith("" + c), null, f));
                    }

                    if (o.getCreationDate() == null) {
                        addResult(createResult(FAILURE,
                                "Found document without creation date! Document id: " + o.getId()));
                    } else {
                        f = createResult(FAILURE,
                                "Query results should be ordered by cmis:creationDate but they are not!");
                        addResult(assertIsTrue(timestamp <= o.getCreationDate().getTimeInMillis(), null, f));

                        timestamp = o.getCreationDate().getTimeInMillis();
                    }
                }
            }

        } else {
            addResult(createResult(SKIPPED, "Query not supported. Test Skipped!"));
        }
    }
}
