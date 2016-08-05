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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
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
            addResult(createResult(SKIPPED, "Secondary types are not supported by CMIS 1.0. Test skipped!"));
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
            String secondaryTestTypeId = getSecondaryTestTypeId();
            ObjectType secondaryTestType = session.getTypeDefinition(secondaryTestTypeId);

            createDocumentAndAttachSecondaryType(session, testFolder, secondaryTestType);
            createDocumentWithSecondaryType(session, testFolder, secondaryTestType);
        } finally {
            // delete the test folder
            deleteTestFolder();
        }
    }

    private void createDocumentAndAttachSecondaryType(Session session, Folder testFolder, ObjectType secondaryTestType) {
        Document doc = createDocument(session, testFolder, "createandattach.txt", "Secondary Type Test");
        Document workDoc = doc;

        try {
            // test if check out is required
            boolean checkedout = false;
            if (needsCheckOut(doc)) {
                workDoc = (Document) session.getObject(doc.checkOut(), SELECT_ALL_NO_CACHE_OC);
                checkedout = true;
            }

            // attach secondary type
            ObjectId newId = workDoc.updateProperties(null, Collections.singletonList(secondaryTestType.getId()), null);
            Document newDoc = (Document) session.getObject(newId, SELECT_ALL_NO_CACHE_OC);

            // check if the secondary type is there
            boolean found = checkSecondaryType(newDoc, secondaryTestType);

            // -- detach secondary type
            if (found) {
                detachSecondaryType(session, newDoc, secondaryTestType);
            }

            // cancel a possible check out
            if (checkedout) {
                workDoc.cancelCheckOut();
            }
        } finally {
            deleteObject(doc);
        }
    }

    private void createDocumentWithSecondaryType(Session session, Folder testFolder, ObjectType secondaryTestType) {
        Document doc = createDocument(session, testFolder, "createwithsecondarytype.txt", getDocumentTestTypeId(),
                new String[] { secondaryTestType.getId() }, "Secondary Type Test");

        try {
            // check if the secondary type is there
            boolean found = checkSecondaryType(doc, secondaryTestType);

            // detach secondary type
            if (found && !needsCheckOut(doc)) {
                detachSecondaryType(session, doc, secondaryTestType);
            }
        } finally {
            deleteObject(doc);
        }
    }

    private boolean needsCheckOut(Document doc) {
        DocumentTypeDefinition type = (DocumentTypeDefinition) doc.getType();
        PropertyDefinition<?> secTypeIdsPropDef = type.getPropertyDefinitions().get(
                PropertyIds.SECONDARY_OBJECT_TYPE_IDS);

        return secTypeIdsPropDef.getUpdatability() == Updatability.WHENCHECKEDOUT
                || (!doc.getAllowableActions().getAllowableActions().contains(Action.CAN_UPDATE_PROPERTIES) && Boolean.TRUE
                        .equals(type.isVersionable()));
    }

    private boolean checkSecondaryType(Document doc, ObjectType secondaryTestType) {
        CmisTestResult f;

        // check if the secondary type is there
        boolean found = false;
        if (doc.getSecondaryTypes() == null) {
            addResult(createResult(FAILURE, "Document does not have the attached secondary type!"));
        } else {
            for (SecondaryType secType : doc.getSecondaryTypes()) {
                if (secondaryTestType.getId().equals(secType.getId())) {
                    found = true;
                    break;
                }
            }

            f = createResult(FAILURE, "Document does not have the attached secondary type!");
            addResult(assertIsTrue(found, null, f));
        }

        // check properties of secondary type
        if (found) {
            Set<String> secondaryTypeProperties = new HashSet<String>();

            if (secondaryTestType.getPropertyDefinitions() != null) {
                for (PropertyDefinition<?> propDef : secondaryTestType.getPropertyDefinitions().values()) {
                    secondaryTypeProperties.add(propDef.getId());
                }
            }

            for (Property<?> prop : doc.getProperties()) {
                secondaryTypeProperties.remove(prop.getId());
            }

            f = createResult(FAILURE, "Documents lacks the following secondary type properties: "
                    + secondaryTypeProperties);
            addResult(assertIsTrue(secondaryTypeProperties.isEmpty(), null, f));
        }

        return found;
    }

    private void detachSecondaryType(Session session, Document doc, ObjectType secondaryTestType) {
        CmisTestResult f;

        // detach secondary type
        ObjectId newId = doc.updateProperties(null, null, Collections.singletonList(secondaryTestType.getId()));
        Document newDoc = (Document) session.getObject(newId, SELECT_ALL_NO_CACHE_OC);

        boolean found = false;
        if (newDoc.getSecondaryTypes() != null) {
            for (SecondaryType secType : newDoc.getSecondaryTypes()) {
                if (secondaryTestType.getId().equals(secType.getId())) {
                    found = true;
                    break;
                }
            }
        }

        f = createResult(FAILURE, "Document still has the detached secondary type!");
        addResult(assertIsFalse(found, null, f));

        // check properties
        ObjectType primaryType = newDoc.getType();
        List<SecondaryType> secondaryTypes = newDoc.getSecondaryTypes();

        for (Property<?> prop : doc.getProperties()) {
            if (!primaryType.getPropertyDefinitions().containsKey(prop.getId())) {
                f = createResult(FAILURE, "Property '" + prop.getId()
                        + "' is neither defined by the primary type nor by a secondary type!");

                if (secondaryTypes == null) {
                    addResult(f);
                } else {
                    boolean foundProperty = false;
                    for (SecondaryType secondaryType : secondaryTypes) {
                        if (secondaryType.getPropertyDefinitions() != null
                                && secondaryType.getPropertyDefinitions().containsKey(prop.getId())) {
                            foundProperty = true;
                            break;
                        }
                    }
                    addResult(assertIsTrue(foundProperty, null, f));
                }
            }
        }
    }
}
