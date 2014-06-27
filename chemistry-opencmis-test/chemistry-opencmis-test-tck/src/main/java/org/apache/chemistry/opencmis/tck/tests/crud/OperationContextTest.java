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

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.CmisTestResultStatus;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

public class OperationContextTest extends AbstractSessionTest {

    private static final String CONTENT = "TCK test content.";

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Operation Context");
        setDescription("Creates a document, retrieves a minimal set of details, checks it, and finally deletes the document.");
    }

    @Override
    public void run(Session session) {

        // create a test folder
        Folder testFolder = createTestFolder(session);

        try {
            // create a test document
            Document document = createDocument(session, testFolder, "testdoc.txt", CONTENT);

            // high-level API tests
            runHighLevelApiTests(session, testFolder, document);

            // low-level API tests
            runLowLevelApiTests(session, testFolder, document);

            // clean up
            document.delete(true);
        } finally {
            // delete the test folder
            deleteTestFolder();
        }
    }

    /**
     * Checks for not requested properties, Allowable Actions, ACLs, renditions,
     * relationships, and policies.
     */
    public void runHighLevelApiTests(Session session, Folder testFolder, Document testDocument) {
        CmisTestResult f;

        // only select some base properties
        Set<String> properties = new HashSet<String>();
        properties.add("cmis:objectId");
        properties.add("cmis:baseTypeId");
        properties.add("cmis:objectTypeId");

        OperationContext context = session.createOperationContext();
        context.setCacheEnabled(false);
        context.setFilter(properties);
        context.setIncludeAcls(false);
        context.setIncludeAllowableActions(false);
        context.setIncludePathSegments(false);
        context.setIncludePolicies(false);
        context.setIncludeRelationships(IncludeRelationships.NONE);
        context.setLoadSecondaryTypeProperties(false);
        context.setRenditionFilterString("cmis:none");

        // get the object with the OperationContext
        Document doc1 = (Document) session.getObject(testDocument, context);

        // check properties
        for (Property<?> prop : doc1.getProperties()) {
            if (!properties.contains(prop.getDefinition().getQueryName())) {
                addResult(createResult(CmisTestResultStatus.WARNING,
                        "getObject() delivered the property '" + prop.getId()
                                + "', although it has not been requested."));
            }
        }

        // check other details
        f = createResult(CmisTestResultStatus.WARNING,
                "getObject() delivered ACLs, although they have not been requested.");
        addResult(assertNull(doc1.getAcl(), null, f));

        f = createResult(CmisTestResultStatus.WARNING,
                "getObject() delivered Allowable Actions, although they have not been requested.");
        addResult(assertNull(doc1.getAllowableActions(), null, f));

        f = createResult(CmisTestResultStatus.WARNING,
                "getObject() delivered policies, although they have not been requested.");
        addResult(assertListNullOrEmpty(doc1.getPolicies(), null, f));

        f = createResult(CmisTestResultStatus.WARNING,
                "getObject() delivered relationships, although they have not been requested.");
        addResult(assertListNullOrEmpty(doc1.getRelationships(), null, f));

        f = createResult(CmisTestResultStatus.WARNING,
                "getObject() delivered renditions, although they have not been requested.");
        addResult(assertListNullOrEmpty(doc1.getRenditions(), null, f));

        // get the test folder children with the OperationContext
        for (CmisObject child : testFolder.getChildren(context)) {
            if (child.getId().equals(testDocument.getId())) {
                // check properties
                for (Property<?> prop : child.getProperties()) {
                    if (!properties.contains(prop.getDefinition().getQueryName())) {
                        addResult(createResult(CmisTestResultStatus.WARNING, "getChildren() delivered the property '"
                                + prop.getId() + "', although it has not been requested."));
                    }
                }

                // check other details
                f = createResult(CmisTestResultStatus.INFO, "getChildren() delivered ACLs, which is not required.");
                addResult(assertNull(child.getAcl(), null, f));

                f = createResult(CmisTestResultStatus.WARNING,
                        "getChildren() delivered Allowable Actions, although they have not been requested.");
                addResult(assertNull(child.getAllowableActions(), null, f));

                f = createResult(CmisTestResultStatus.INFO, "getChildren() delivered policies, which is not required.");
                addResult(assertListNullOrEmpty(child.getPolicies(), null, f));

                f = createResult(CmisTestResultStatus.WARNING,
                        "getChildren() delivered relationships, although they have not been requested.");
                addResult(assertListNullOrEmpty(child.getRelationships(), null, f));

                f = createResult(CmisTestResultStatus.WARNING,
                        "getChildren() delivered renditions, although they have not been requested.");
                addResult(assertListNullOrEmpty(child.getRenditions(), null, f));

                break;
            }
        }
    }

    /**
     * Checks for change events and path segments.
     */
    public void runLowLevelApiTests(Session session, Folder testFolder, Document testDocument) {
        CmisTestResult f;

        String repositoryId = session.getRepositoryInfo().getId();
        String filter = "cmis:objectId,cmis:baseTypeId,cmis:objectTypeId";

        // get the object
        ObjectData doc1 = session
                .getBinding()
                .getObjectService()
                .getObject(repositoryId, testDocument.getId(), filter, Boolean.FALSE, IncludeRelationships.NONE,
                        "cmis:none", Boolean.FALSE, Boolean.FALSE, null);

        // check for change events
        f = createResult(CmisTestResultStatus.WARNING,
                "getObject() delivered a change event, which doesn't make sense.");
        addResult(assertNull(doc1.getChangeEventInfo(), null, f));

        // get the test folder children
        ObjectInFolderList children = session
                .getBinding()
                .getNavigationService()
                .getChildren(repositoryId, testFolder.getId(), filter, null, Boolean.FALSE, IncludeRelationships.NONE,
                        "cmis:none", Boolean.FALSE, null, BigInteger.ZERO, null);

        // check for path segments
        for (ObjectInFolderData child : children.getObjects()) {
            f = createResult(CmisTestResultStatus.WARNING,
                    "getChildren() delivered a path segment, although it hasn't been requested.");
            addResult(assertNull(child.getPathSegment(), null, f));
        }

        // get the document parent
        List<ObjectParentData> parents = session
                .getBinding()
                .getNavigationService()
                .getObjectParents(repositoryId, testDocument.getId(), filter, Boolean.FALSE, IncludeRelationships.NONE,
                        "cmis:none", Boolean.FALSE, null);

        // check for relative path segments
        for (ObjectParentData parent : parents) {
            f = createResult(CmisTestResultStatus.WARNING,
                    "getObjectParents() delivered a relative path segment, although it hasn't been requested.");
            addResult(assertNull(parent.getRelativePathSegment(), null, f));
        }
    }
}
