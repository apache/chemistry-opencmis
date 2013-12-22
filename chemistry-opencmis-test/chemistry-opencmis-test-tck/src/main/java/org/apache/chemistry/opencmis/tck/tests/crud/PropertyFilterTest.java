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
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.util.OperationContextUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

public class PropertyFilterTest extends AbstractSessionTest {

    private static final String CONTENT = "TCK test content.";
    private static final String INVALID_PROPERTY = "cmis:tck:thisPropertyDoesNotExist";

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Property Filter Test");
        setDescription("Tests different property filter combinations for documents and folders.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        // filter with MIME type
        OperationContext testContext1 = OperationContextUtils.createMinimumOperationContext();
        Set<String> testfilter1 = new HashSet<String>(testContext1.getFilter());
        testfilter1.add(PropertyIds.CONTENT_STREAM_MIME_TYPE);
        testContext1.setFilter(testfilter1);

        // filter with path
        OperationContext testContext2 = OperationContextUtils.createMinimumOperationContext();
        Set<String> testfilter2 = new HashSet<String>(testContext2.getFilter());
        testfilter2.add(PropertyIds.PATH);
        testContext2.setFilter(testfilter2);

        // filter with invalid property
        OperationContext testContext3 = OperationContextUtils.createMinimumOperationContext();
        Set<String> testfilter3 = new HashSet<String>(testContext3.getFilter());
        testfilter3.add(INVALID_PROPERTY);
        testContext3.setFilter(testfilter3);

        // create a test folder
        Folder testFolder = createTestFolder(session);

        try {
            Document doc = createDocument(session, testFolder, "doc", CONTENT);

            Document doc1 = (Document) session.getObject(doc, testContext1);

            // check document
            f = createResult(FAILURE, "Document should have the property " + PropertyIds.CONTENT_STREAM_MIME_TYPE + "!");
            addResult(assertNotNull(doc1.getProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE), null, f));
            f = createResult(FAILURE, "Document should not have the property " + PropertyIds.PATH + "!");
            addResult(assertNull(doc1.getProperty(PropertyIds.PATH), null, f));
            f = createResult(FAILURE, "Document should not have the property " + INVALID_PROPERTY + "!");
            addResult(assertNull(doc1.getProperty(INVALID_PROPERTY), null, f));

            Document doc2 = (Document) session.getObject(doc, testContext2);

            f = createResult(WARNING, "Document should not have the property " + PropertyIds.CONTENT_STREAM_MIME_TYPE
                    + "!");
            addResult(assertNull(doc2.getProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE), null, f));
            f = createResult(FAILURE, "Document should not have the property " + PropertyIds.PATH + "!");
            addResult(assertNull(doc2.getProperty(PropertyIds.PATH), null, f));
            f = createResult(FAILURE, "Document should not have the property " + INVALID_PROPERTY + "!");
            addResult(assertNull(doc2.getProperty(INVALID_PROPERTY), null, f));

            Document doc3 = (Document) session.getObject(doc, testContext3);

            f = createResult(WARNING, "Document should not have the property" + PropertyIds.CONTENT_STREAM_MIME_TYPE
                    + "!");
            addResult(assertNull(doc3.getProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE), null, f));
            f = createResult(FAILURE, "Document should not have the property" + PropertyIds.PATH + "!");
            addResult(assertNull(doc3.getProperty(PropertyIds.PATH), null, f));
            f = createResult(FAILURE, "Document should not have the property" + INVALID_PROPERTY + "!");
            addResult(assertNull(doc3.getProperty(INVALID_PROPERTY), null, f));

            // check folder
            Folder folder1 = (Folder) session.getObject(testFolder, testContext1);

            f = createResult(FAILURE, "Folder should not have the property " + PropertyIds.CONTENT_STREAM_MIME_TYPE
                    + "!");
            addResult(assertNull(folder1.getProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE), null, f));
            f = createResult(WARNING, "Folder should not have the property " + PropertyIds.PATH + "!");
            addResult(assertNull(folder1.getProperty(PropertyIds.PATH), null, f));
            f = createResult(FAILURE, "Folder should not have the property " + INVALID_PROPERTY + "!");
            addResult(assertNull(folder1.getProperty(INVALID_PROPERTY), null, f));

            Folder folder2 = (Folder) session.getObject(testFolder, testContext2);

            f = createResult(FAILURE, "Folder should not have the property " + PropertyIds.CONTENT_STREAM_MIME_TYPE
                    + "!");
            addResult(assertNull(folder2.getProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE), null, f));
            f = createResult(FAILURE, "Folder should have the property " + PropertyIds.PATH + "!");
            addResult(assertNotNull(folder2.getProperty(PropertyIds.PATH), null, f));
            f = createResult(FAILURE, "Folder should not have the property " + INVALID_PROPERTY + "!");
            addResult(assertNull(folder2.getProperty(INVALID_PROPERTY), null, f));

            Folder folder3 = (Folder) session.getObject(testFolder, testContext3);

            f = createResult(FAILURE, "Folder should not have the property " + PropertyIds.CONTENT_STREAM_MIME_TYPE
                    + "!");
            addResult(assertNull(folder3.getProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE), null, f));
            f = createResult(WARNING, "Folder should not have the property " + PropertyIds.PATH + "!");
            addResult(assertNull(folder3.getProperty(PropertyIds.PATH), null, f));
            f = createResult(FAILURE, "Folder should not have the property " + INVALID_PROPERTY + "!");
            addResult(assertNull(folder3.getProperty(INVALID_PROPERTY), null, f));

            // check children
            CmisObject obj1 = testFolder.getChildren(testContext1).iterator().next();

            f = createResult(WARNING, "Child should have the property " + PropertyIds.CONTENT_STREAM_MIME_TYPE + "!");
            addResult(assertNotNull(obj1.getProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE), null, f));
            f = createResult(WARNING, "Child should not have the property " + PropertyIds.PATH + "!");
            addResult(assertNull(obj1.getProperty(PropertyIds.PATH), null, f));
            f = createResult(FAILURE, "Child should not have the property " + INVALID_PROPERTY + "!");
            addResult(assertNull(obj1.getProperty(INVALID_PROPERTY), null, f));

            CmisObject obj2 = testFolder.getChildren(testContext2).iterator().next();

            f = createResult(WARNING, "Child should not have the property " + PropertyIds.CONTENT_STREAM_MIME_TYPE
                    + "!");
            addResult(assertNull(obj2.getProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE), null, f));
            f = createResult(WARNING, "Child should not have the property " + PropertyIds.PATH + "!");
            addResult(assertNull(obj2.getProperty(PropertyIds.PATH), null, f));
            f = createResult(FAILURE, "Child should not have the property " + INVALID_PROPERTY + "!");
            addResult(assertNull(obj2.getProperty(INVALID_PROPERTY), null, f));

            CmisObject obj3 = testFolder.getChildren(testContext3).iterator().next();

            f = createResult(WARNING, "Child should not have the property" + PropertyIds.CONTENT_STREAM_MIME_TYPE + "!");
            addResult(assertNull(obj3.getProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE), null, f));
            f = createResult(WARNING, "Child should not have the property" + PropertyIds.PATH + "!");
            addResult(assertNull(obj3.getProperty(PropertyIds.PATH), null, f));
            f = createResult(FAILURE, "Child should not have the property" + INVALID_PROPERTY + "!");
            addResult(assertNull(obj3.getProperty(INVALID_PROPERTY), null, f));
        } finally {
            // delete the test folder
            deleteTestFolder();
        }
    }
}
