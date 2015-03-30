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
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.INFO;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

public class ChangeTokenTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Change Token Test");
        setDescription("Creates a document and a folder and updates them with an outdated change token.");
    }

    @Override
    public void run(Session session) {
        // create a test folder
        Folder testFolder = createTestFolder(session);

        try {
            // update document properties test
            runUpdateDocumentTest(session, testFolder);

            // content update test
            runContentTest(session, testFolder);

            // update folder properties test
            runUpdateFolderTest(session, testFolder);

            // add child and update folder properties test
            runAddChildFolderTest(session, testFolder);
        } finally {
            // delete the test folder
            deleteTestFolder();
        }
    }

    private void runUpdateDocumentTest(Session session, Folder testFolder) {
        Document doc = createDocument(session, testFolder, "update1.txt", "Hello World!");

        try {
            if (doc.getChangeToken() == null) {
                addResult(createResult(SKIPPED,
                        "Repository does not provide change tokens for documents. Test skipped!"));
                return;
            }

            DocumentTypeDefinition type = (DocumentTypeDefinition) doc.getType();
            PropertyDefinition<?> namePropDef = type.getPropertyDefinitions().get(PropertyIds.NAME);
            if (namePropDef.getUpdatability() == Updatability.WHENCHECKEDOUT
                    || !doc.getAllowableActions().getAllowableActions().contains(Action.CAN_UPDATE_PROPERTIES)) {
                addResult(createResult(SKIPPED, "Document name can't be changed. Test skipped!"));
                return;
            }

            // the first update should succeed
            Map<String, Object> properties2 = new HashMap<String, Object>();
            properties2.put(PropertyIds.NAME, "update2.txt");
            ObjectId newId = doc.updateProperties(properties2, false);

            if (!doc.getId().equals(newId.getId())) {
                // the repository created a new version
                // -> a change token test does not make sense
                addResult(createResult(INFO,
                        "The repository created a new version. Change tokens are not relevant here."));
            } else {
                try {
                    Map<String, Object> properties3 = new HashMap<String, Object>();
                    properties3.put(PropertyIds.NAME, "update3.txt");
                    doc.updateProperties(properties3, false);

                    addResult(createResult(FAILURE, "Updating properties a second time with the same change token "
                            + "should result in an UpdateConflict exception!"));
                } catch (CmisUpdateConflictException e) {
                    // expected exception
                }
            }
        } finally {
            deleteObject(doc);
        }
    }

    private void runContentTest(Session session, Folder testFolder) {
        if (session.getRepositoryInfo().getCapabilities().getContentStreamUpdatesCapability() != CapabilityContentStreamUpdates.ANYTIME) {
            addResult(createResult(SKIPPED, "Repository doesn't allow to replace content. Test skipped!"));
            return;
        }

        Document doc = createDocument(session, testFolder, "content1.txt", "Hello World!");

        try {
            if (doc.getChangeToken() == null) {
                addResult(createResult(SKIPPED,
                        "Repository does not provide change tokens for documents. Test skipped!"));
                return;
            }

            if (!doc.getAllowableActions().getAllowableActions().contains(Action.CAN_SET_CONTENT_STREAM)) {
                addResult(createResult(SKIPPED, "Document content can't be changed. Test skipped!"));
                return;
            }

            byte[] contentBytes = IOUtils.toUTF8Bytes("New content");
            ContentStream contentStream = new ContentStreamImpl("content2.txt",
                    BigInteger.valueOf(contentBytes.length), "text/plain", new ByteArrayInputStream(contentBytes));

            ObjectId newId = doc.setContentStream(contentStream, true, false);

            if (newId == null) {
                // the AtomPub binding does not return an id here
                // -> get the latest id from the version series
                if (Boolean.TRUE.equals(((DocumentTypeDefinition) doc.getType()).isVersionable())) {
                    List<Document> versions = doc.getAllVersions();
                    if (versions == null || versions.size() < 1) {
                        addResult(createResult(FAILURE, "Repository returned an empty list of document versions!"));
                    } else {
                        // the latest document is at the top of the list
                        newId = versions.get(0);
                    }
                } else {
                    // the document type is not versionable
                    // -> the repository couldn't create a new version
                    newId = doc;
                }
            }

            if (newId != null) {
                if (!doc.getId().equals(newId.getId())) {
                    // the repository created a new version
                    // -> a change token test does not make sense
                    addResult(createResult(INFO,
                            "The repository created a new version. Change tokens are not relevant here."));
                } else {
                    try {
                        doc.setContentStream(contentStream, true, false);

                        addResult(createResult(FAILURE, "Updating content a second time with the same change token "
                                + "should result in an UpdateConflict exception!"));
                    } catch (CmisUpdateConflictException uce) {
                        // expected exception
                    }
                }
            }
        } finally {
            deleteObject(doc);
        }
    }

    private void runUpdateFolderTest(Session session, Folder testFolder) {
        Folder folder = createFolder(session, testFolder, "folder1");

        try {
            if (folder.getChangeToken() == null) {
                addResult(createResult(SKIPPED, "Repository does not provide change tokens for folders. Test skipped!"));
                return;
            }

            if (!folder.getAllowableActions().getAllowableActions().contains(Action.CAN_UPDATE_PROPERTIES)) {
                addResult(createResult(SKIPPED, "Folder name can't be changed. Test skipped!"));
                return;
            }

            // the first update should succeed
            Map<String, Object> properties2 = new HashMap<String, Object>();
            properties2.put(PropertyIds.NAME, "folder2");
            folder.updateProperties(properties2, false);

            try {
                Map<String, Object> properties3 = new HashMap<String, Object>();
                properties3.put(PropertyIds.NAME, "folder3");
                folder.updateProperties(properties3, false);

                addResult(createResult(FAILURE, "Updating properties a second time with the same change token "
                        + "should result in an UpdateConflict exception!"));
            } catch (CmisUpdateConflictException e) {
                // expected exception
            }

        } finally {
            deleteObject(folder);
        }
    }

    private void runAddChildFolderTest(Session session, Folder testFolder) {
        Folder folder = createFolder(session, testFolder, "folder1");

        try {
            if (folder.getChangeToken() == null) {
                addResult(createResult(SKIPPED, "Repository does not provide change tokens for folders. Test skipped!"));
                return;
            }

            if (!folder.getAllowableActions().getAllowableActions().contains(Action.CAN_UPDATE_PROPERTIES)) {
                addResult(createResult(SKIPPED, "Folder name can't be changed. Test skipped!"));
                return;
            }

            createDocument(session, folder, "doc1", "content");

            try {
                Map<String, Object> properties2 = new HashMap<String, Object>();
                properties2.put(PropertyIds.NAME, "folder2");
                folder.updateProperties(properties2, false);
            } catch (CmisUpdateConflictException e) {
                addResult(createResult(WARNING, "Adding a child to a folder changes the change token of the folder. "
                        + "CMIS clients might not expect that."));
            }

        } finally {
            deleteObject(folder);
        }
    }
}
