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
package org.apache.chemistry.opencmis.tck.tests.crud;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Copy test.
 */
public class UpdateSmokeTest extends AbstractSessionTest {

    private static final String NAME1 = "updatetest1.txt";
    private static final String NAME2 = "updatetest2.txt";

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Update Smoke Test");
        setDescription("Creates a document, updates its name and finally deletes it.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        try {
            // create folders
            Folder testFolder = createTestFolder(session);

            // create document
            Document doc1 = createDocument(session, testFolder, NAME1, "rename me!");

            f = createResult(FAILURE, "Document name doesn't match with given name!");
            addResult(assertEquals(NAME1, doc1.getName(), null, f));

            // update
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(PropertyIds.NAME, NAME2);

            Document doc2 = (Document) doc1.updateProperties(properties);

            addResult(checkObject(session, doc2, getAllProperties(doc2), "Updated document compliance"));

            f = createResult(FAILURE, "Document name doesn't match updated value!");
            addResult(assertEquals(NAME2, doc2.getName(), null, f));

            // delete
            deleteObject(doc2);
        } finally {
            // clean up
            deleteTestFolder();
        }
    }
}
