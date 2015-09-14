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
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.util.Map;

import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

public class InvalidQueryTest extends AbstractQueryTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Invalid Query Test");
        setDescription("Tests invalid queries.");
    }

    @Override
    public void run(Session session) {
        if (supportsQuery(session)) {
            doInvalidQuery(session, "");
            doInvalidQuery(session, "SELECT");
            doInvalidQuery(session, "SELECT *");
            doInvalidQuery(session, "THIS_IS_NOT_A_SELECT");
            doInvalidQuery(session, "SELECT FROM cmis:document");
            doInvalidQuery(session, "SELECT ,cmis:name FROM cmis:document");
        } else {
            try {
                doQuery(session, "SELECT * FROM cmis:document");
                addResult(createResult(WARNING, "Query capability is set to 'none' but calling query() works."));
            } catch (CmisNotSupportedException nse) {
                // excepted
            } catch (CmisObjectNotFoundException onfe) {
                // excepted for AtomPub
                if (getBinding() != BindingType.ATOMPUB) {
                    addResult(createResult(FAILURE,
                            "Query capability is set to 'none' but calling query() throws an exception, which is not a notSupported exception("
                                    + onfe.toString() + ").", onfe, false));
                }
            } catch (Exception ex) {
                addResult(createResult(FAILURE,
                        "Query capability is set to 'none' but calling query() throws an exception, which is not a notSupported exception("
                                + ex.toString() + ").", ex, false));
            }
        }
    }

    private void doQuery(Session session, String stmt) {
        for (QueryResult qr : session.query(stmt, false)) {
            qr.getPropertyByQueryName("cmis:name");
        }
    }

    private void doInvalidQuery(Session session, String stmt) {
        try {
            doQuery(session, stmt);
            addResult(createResult(FAILURE, "This query is invalid but has been accepted: " + stmt));
        } catch (CmisInvalidArgumentException e) {
            // excepted
        } catch (Exception ex) {
            addResult(createResult(FAILURE, "This query is invalid and an unexcepted exception (" + ex.toString()
                    + ") has been thrown: " + stmt, ex, false));
        }
    }
}
