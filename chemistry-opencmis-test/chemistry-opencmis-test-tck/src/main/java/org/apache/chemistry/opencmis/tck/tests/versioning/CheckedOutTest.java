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
package org.apache.chemistry.opencmis.tck.tests.versioning;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;

import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Checked out test.
 */
public class CheckedOutTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Checked out Test");
    }

    @Override
    public void run(Session session) {
        ItemIterable<Document> pwcs = session.getCheckedOutDocs(SELECT_ALL_NO_CACHE_OC);
        if (pwcs != null) {
            for (Document pwc : pwcs) {
                String[] propertiesToCheck = getAllProperties(pwc);
                addResult(checkObject(session, pwc, propertiesToCheck, "PWC check: " + pwc.getId()));

                if (pwc != null) {
                    if (!pwc.isLatestMajorVersion()) {
                        addResult(createResult(FAILURE, "PWC is not latest version! Id: " + pwc.getId()));
                    }
                }
            }
        }
    }
}
