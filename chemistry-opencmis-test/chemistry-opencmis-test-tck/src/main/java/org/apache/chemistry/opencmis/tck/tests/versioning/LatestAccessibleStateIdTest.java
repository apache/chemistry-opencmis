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
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;

import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

public class LatestAccessibleStateIdTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Latest Accessible State ID Test");
        setDescription("Creates a document and tries to get it with its Latest Accessible State ID.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        try {
            // create folder and document
            Folder testFolder = createTestFolder(session);
            Document doc = createDocument(session, testFolder, "lateststate.txt", "latest state");
            DocumentTypeDefinition docType = (DocumentTypeDefinition) doc.getType();

            if (!docType.getPropertyDefinitions().containsKey(PropertyIds.LATEST_ACCESSIBLE_STATE_ID)) {
                addResult(createResult(SKIPPED,
                        "Repository does not support the Latest State Identifier feature extension. Test skipped!"));
                doc.delete(true);
                return;
            }

            // check latest accessible state ID
            f = createResult(FAILURE, "Latest Accessible State ID is not set!");
            addResult(assertStringNotEmpty(doc.getLatestAccessibleStateId(), null, f));

            // get document with latest accessible state ID
            try {
                CmisObject latestStateObject = session.getObject(doc.getLatestAccessibleStateId(),
                        SELECT_ALL_NO_CACHE_OC);

                if (latestStateObject instanceof Document) {
                    f = createResult(FAILURE,
                            "Latest Accessible State IDs of the orignal and the retrieved document don't match!");
                    addResult(assertEquals(doc.getLatestAccessibleStateId(),
                            ((Document) latestStateObject).getLatestAccessibleStateId(), null, f));
                } else {
                    addResult(createResult(FAILURE,
                            "Object retrieved with the Latest Accessible State ID is not a document!"));
                }
            } catch (CmisObjectNotFoundException onf) {
                addResult(createResult(FAILURE, "Document could not be retrieved with the Latest Accessible State ID!"));
            }

            doc.delete(true);
        } finally {
            // delete the test folder
            deleteTestFolder();
        }
    }
}
