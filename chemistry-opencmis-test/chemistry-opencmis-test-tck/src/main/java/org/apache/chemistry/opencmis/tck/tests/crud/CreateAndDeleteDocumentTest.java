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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Simple document test.
 */
public class CreateAndDeleteDocumentTest extends AbstractSessionTest {

    private static final String CONTENT = "TCK test content.";

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Create and Delete Document Test");
        setDescription("Creates a few documents, checks the newly created documents and their parent and finally deletes the created documents.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        int numOfDocuments = 20;

        // create a test folder
        Folder testFolder = createTestFolder(session);

        try {
            Map<String, Document> documents = new HashMap<String, Document>();

            // create documents
            for (int i = 0; i < numOfDocuments; i++) {
                Document newDocument = createDocument(session, testFolder, "doc" + i, CONTENT);
                documents.put(newDocument.getId(), newDocument);
            }

            // simple children test
            addResult(checkChildren(session, testFolder, "Test folder children check"));

            // check if all documents are there
            ItemIterable<CmisObject> children = testFolder.getChildren(SELECT_ALL_NO_CACHE_OC);
            List<String> childrenIds = new ArrayList<String>();
            for (CmisObject child : children) {
                if (child != null) {
                    childrenIds.add(child.getId());
                    Document document = documents.get(child.getId());

                    f = createResult(FAILURE, "Document and test folder child don't match! Id: " + child.getId());
                    addResult(assertShallowEquals(document, child, null, f));
                }
            }

            f = createResult(FAILURE, "Number of created folders does not match the number of existing folders!");
            addResult(assertEquals(numOfDocuments, childrenIds.size(), null, f));

            for (Document document : documents.values()) {
                if (!childrenIds.contains(document.getId())) {
                    addResult(createResult(FAILURE, "Created document not found in test folder children! Id: "
                            + document.getId()));
                }
            }

            // check paging
            int pageSize = 5;
            CmisObject lastObject = null;

            int count = 0;
            ItemIterable<CmisObject> page1 = testFolder.getChildren(SELECT_ALL_NO_CACHE_OC_ORDER_BY_NAME).getPage(
                    pageSize);
            for (CmisObject child : page1) {
                count++;
                lastObject = child;
            }

            f = createResult(FAILURE, "Returned number of children doesn't match the page size!");
            addResult(assertEquals(pageSize, count, null, f));

            if (page1.getTotalNumItems() == -1) {
                addResult(createResult(WARNING, "Repository did not return numItems for the first test page."));
            } else {
                f = createResult(FAILURE, "Returned numItems doesn't match the number of documents!");
                addResult(assertEquals((long) numOfDocuments, page1.getTotalNumItems(), null, f));
            }

            f = createResult(FAILURE, "hasMoreItems of the first test page must be TRUE!");
            addResult(assertEquals(true, page1.getHasMoreItems(), null, f));

            // check second page
            count = 0;
            ItemIterable<CmisObject> page2 = testFolder.getChildren(SELECT_ALL_NO_CACHE_OC_ORDER_BY_NAME)
                    .skipTo(pageSize - 1).getPage(pageSize);
            for (CmisObject child : page2) {
                count++;

                if (count == 1) {
                    f = createResult(FAILURE,
                            "Last object of the first page doesn't match the first object of the second page.");
                    addResult(assertEquals(lastObject.getId(), child.getId(), null, f));
                }
            }

            f = createResult(FAILURE, "Returned number of children doesn't match the page size!");
            addResult(assertEquals(pageSize, count, null, f));

            if (page2.getTotalNumItems() == -1) {
                addResult(createResult(WARNING, "Repository did not return numItems for the second test page."));
            } else {
                f = createResult(FAILURE, "Returned numItems doesn't match the number of documents!");
                addResult(assertEquals((long) numOfDocuments, page2.getTotalNumItems(), null, f));
            }

            f = createResult(FAILURE, "hasMoreItems of the second test page must be TRUE!");
            addResult(assertEquals(true, page2.getHasMoreItems(), null, f));

            // check third page
            count = 0;
            ItemIterable<CmisObject> page3 = testFolder.getChildren(SELECT_ALL_NO_CACHE_OC_ORDER_BY_NAME)
                    .skipTo(numOfDocuments - 5).getPage(10);
            for (@SuppressWarnings("unused")
            CmisObject child : page3) {
                count++;
            }

            f = createResult(FAILURE,
                    "Returned number of children should be 5 because page startetd at (numOfDocuments - 5).");
            addResult(assertEquals(5, count, null, f));

            if (page3.getTotalNumItems() == -1) {
                addResult(createResult(WARNING, "Repository did not return numItems for the third test page."));
            } else {
                f = createResult(FAILURE, "Returned numItems doesn't match the number of documents!");
                addResult(assertEquals((long) numOfDocuments, page3.getTotalNumItems(), null, f));
            }

            f = createResult(FAILURE, "hasMoreItems of the third test page must be FALSE!");
            addResult(assertEquals(false, page3.getHasMoreItems(), null, f));

            // check non-existing page
            count = 0;
            ItemIterable<CmisObject> pageNotExisting = testFolder.getChildren(SELECT_ALL_NO_CACHE_OC_ORDER_BY_NAME)
                    .skipTo(100000).getPage(pageSize);
            for (@SuppressWarnings("unused")
            CmisObject child : pageNotExisting) {
                count++;
            }

            f = createResult(FAILURE, "The page size of a non-existing page must be 0!");
            addResult(assertEquals(0, count, null, f));

            if (pageNotExisting.getTotalNumItems() == -1) {
                addResult(createResult(WARNING, "Repository did not return numItems for a non-existing page."));
            } else {
                f = createResult(FAILURE, "Returned numItems doesn't match the number of documents!");
                addResult(assertEquals((long) numOfDocuments, pageNotExisting.getTotalNumItems(), null, f));
            }

            f = createResult(FAILURE, "hasMoreItems of a non-existing page must be FALSE!");
            addResult(assertEquals(false, pageNotExisting.getHasMoreItems(), null, f));

            // check content
            for (Document document : documents.values()) {
                ContentStream contentStream = document.getContentStream();
                if (contentStream == null || contentStream.getStream() == null) {
                    addResult(createResult(FAILURE, "Document has no content! Id: " + document.getId()));
                    continue;
                } else {
                    try {
                        contentStream.getStream().close();
                    } catch (IOException e) {
                    }
                }

                // TODO: content checks
            }

            // delete all documents
            for (Document document : documents.values()) {
                document.delete(true);

                f = createResult(FAILURE,
                        "Document should not exist anymore but it is still there! Id: " + document.getId());
                addResult(assertIsFalse(exists(document), null, f));
            }
        } finally {
            // delete the test folder
            deleteTestFolder();
        }

        addResult(createInfoResult("Tested the creation and deletion of " + numOfDocuments + " documents."));
    }
}
