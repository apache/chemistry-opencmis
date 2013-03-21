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
package org.apache.chemistry.opencmis.tck.tests.types;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

public class SecondaryTypesTest extends AbstractSessionTest {
    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Secondary Types Test");
        setDescription("Creates documents, attaches and detaches secondary types, checks the properties, and finally deletes the test documents.");
    }

    @Override
    public void run(Session session) {
        if (session.getRepositoryInfo().getCmisVersion() == CmisVersion.CMIS_1_0) {
            addResult(createResult(SKIPPED, "Secondary types are not supporetd by CMIS 1.0. Test skipped!"));
            return;
        }

        if (!hasSecondaries(session)) {
            addResult(createResult(SKIPPED, "Repository doesn't support secondary types. Test skipped!"));
            return;
        }

        // check cmis:secondaryObjectTypeIds property definition
        ObjectType docType = session.getTypeDefinition(getDocumentTestTypeId());
        PropertyDefinition<?> secTypesPropDef = docType.getPropertyDefinitions().get(
                PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
        if (secTypesPropDef == null) {
            addResult(createResult(FAILURE, "Test document type has no " + PropertyIds.SECONDARY_OBJECT_TYPE_IDS
                    + " property!"));
            return;
        } else if (secTypesPropDef.getUpdatability() != Updatability.READWRITE) {
            addResult(createResult(SKIPPED,
                    "Test document type does not allow attaching secondary types. Test skipped!"));
            return;
        }

        // create a test folder
        Folder testFolder = createTestFolder(session);

        try {
            createDocumentAndAttachSecondaryType(session, testFolder);
            createDocumentWithSecondaryType(session, testFolder);
        } finally {
            // delete the test folder
            deleteTestFolder();
        }
    }

    private void createDocumentAndAttachSecondaryType(Session session, Folder testFolder) {
        CmisTestResult f;

        Document doc = createDocument(session, testFolder, "createandattach.txt", "Secondary Type Test");

        try {
            String secondaryTestTypeId = getSecondaryTestTypeId();

            // -- attach secondary type
            List<String> secondaryTypes = new ArrayList<String>();

            // copy already attached secondary types, if there are any
            if (doc.getSecondaryTypes() != null) {
                for (SecondaryType secType : doc.getSecondaryTypes()) {
                    secondaryTypes.add(secType.getId());
                }
            }

            secondaryTypes.add(secondaryTestTypeId);

            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, secondaryTypes);

            // attach secondary type
            Document newDoc = (Document) doc.updateProperties(properties);

            // check if the secondary type is there
            boolean found = false;
            if (newDoc.getSecondaryTypes() == null) {
                addResult(createResult(FAILURE, "Document does not have the attached secondary type!"));
            } else {
                for (SecondaryType secType : newDoc.getSecondaryTypes()) {
                    if (secondaryTestTypeId.equals(secType.getId())) {
                        found = true;
                        break;
                    }
                }

                f = createResult(FAILURE, "Document does not have the attached secondary type!");
                addResult(assertIsTrue(found, null, f));
            }

            // -- detach secondary type
            if (found) {
                secondaryTypes = new ArrayList<String>();

                for (SecondaryType secType : newDoc.getSecondaryTypes()) {
                    if (!secondaryTestTypeId.equals(secType.getId())) {
                        secondaryTypes.add(secType.getId());
                    }
                }

                properties = new HashMap<String, Object>();
                properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, secondaryTypes);

                // attach secondary type
                Document newDoc2 = (Document) doc.updateProperties(properties);

                found = false;
                if (newDoc2.getSecondaryTypes() != null) {
                    for (SecondaryType secType : newDoc2.getSecondaryTypes()) {
                        if (secondaryTestTypeId.equals(secType.getId())) {
                            found = true;
                            break;
                        }
                    }
                }

                f = createResult(FAILURE, "Document still has the detached secondary type!");
                addResult(assertIsFalse(found, null, f));
            }

        } finally {
            deleteObject(doc);
        }
    }

    private void createDocumentWithSecondaryType(Session session, Folder testFolder) {
        // TODO
    }

}
