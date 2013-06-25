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
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.UNEXPECTED_EXCEPTION;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Content test.
 */
public class SetAndDeleteContentTest extends AbstractSessionTest {

    private static final String CONTENT1 = "one";
    private static final String CONTENT2 = "two";
    private static final String CONTENT3 = "three";

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Set, Append, and Delete Content Test");
        setDescription("Creates a new document and tries to set, append, and delete its content.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        if (getContentStreamUpdatesCapbility(session) == CapabilityContentStreamUpdates.NONE) {
            addResult(createResult(SKIPPED, "Stream updates are not supported. Test skipped!"));
            return;
        }

        try {
            // create folder and document
            Folder testFolder = createTestFolder(session);
            Document doc = createDocument(session, testFolder, "contenttest.txt", CONTENT1);
            Document workDoc = doc;
            DocumentTypeDefinition docType = (DocumentTypeDefinition) doc.getType();

            // test if check out is required and possible
            boolean checkedout = false;
            if (!doc.getAllowableActions().getAllowableActions().contains(Action.CAN_SET_CONTENT_STREAM)) {
                if (!docType.isVersionable()) {
                    addResult(createResult(SKIPPED,
                            "The test document does not accept a new content stream. Test skipped!"));
                    doc.delete(true);
                    return;
                } else {
                    workDoc = (Document) session.getObject(doc.checkOut(), SELECT_ALL_NO_CACHE_OC);
                    checkedout = true;

                    if (!workDoc.getAllowableActions().getAllowableActions().contains(Action.CAN_SET_CONTENT_STREAM)) {
                        addResult(createResult(SKIPPED,
                                "The test PWC does not accept a new content stream. Test skipped!"));
                        workDoc.cancelCheckOut();
                        doc.delete(true);
                        return;
                    }
                }
            }

            // test if the content stream can be deleted
            if (docType.getContentStreamAllowed() == ContentStreamAllowed.REQUIRED) {
                addResult(createResult(SKIPPED,
                        "A content stream is required for this document type. deleteContentStream() test skipped!"));
            } else {
                // delete content stream
                try {
                    ObjectId newObjectId = workDoc.deleteContentStream(true);

                    // deleteContentStream may have created a new version
                    Document contentDoc = getNewVersion(session, workDoc, checkedout, newObjectId,
                            "deleteContentStream()");

                    f = createResult(FAILURE, "Document still has content after deleteContentStream() has been called!");
                    addResult(assertNull(contentDoc.getContentStream(), null, f));

                    workDoc = contentDoc;
                } catch (CmisNotSupportedException e) {
                    addResult(createResult(WARNING, "deleteContentStream() is not supported!"));
                }
            }

            // set a new content stream
            byte[] contentBytes = new byte[0];
            try {
                contentBytes = CONTENT2.getBytes("UTF-8");
            } catch (Exception e) {
            }

            try {
                ContentStream contentStream = session.getObjectFactory().createContentStream(workDoc.getName(),
                        contentBytes.length, "text/plain", new ByteArrayInputStream(contentBytes));

                ObjectId newObjectId = workDoc.setContentStream(contentStream, true, true);

                try {
                    contentStream.getStream().close();
                } catch (Exception e) {
                }

                // setContentStream may have created a new version
                Document contentDoc = getNewVersion(session, workDoc, checkedout, newObjectId, "setContentStream()");

                // test new content
                try {
                    String content = getStringFromContentStream(contentDoc.getContentStream());
                    f = createResult(FAILURE, "Document content doesn't match the content set by setContentStream()!");
                    addResult(assertEquals(CONTENT2, content, null, f));
                } catch (IOException e) {
                    addResult(createResult(UNEXPECTED_EXCEPTION,
                            "Document content couldn't be read! Exception: " + e.getMessage(), e, true));
                }

                workDoc = contentDoc;
            } catch (CmisNotSupportedException e) {
                addResult(createResult(WARNING, "setContentStream() is not supported!"));
            }

            // test appendContentStream
            if (session.getRepositoryInfo().getCmisVersion() != CmisVersion.CMIS_1_0) {
                contentBytes = new byte[0];
                try {
                    contentBytes = CONTENT3.getBytes("UTF-8");
                } catch (Exception e) {
                }

                try {
                    ContentStream contentStream = session.getObjectFactory().createContentStream(workDoc.getName(),
                            contentBytes.length, "text/plain", new ByteArrayInputStream(contentBytes));

                    ObjectId newObjectId = workDoc.appendContentStream(contentStream, true);

                    // appendContentStream may have created a new version
                    Document contentDoc = getNewVersion(session, workDoc, checkedout, newObjectId,
                            "appendContentStream()");

                    // test new content
                    try {
                        String content = getStringFromContentStream(contentDoc.getContentStream());
                        f = createResult(FAILURE,
                                "Document content doesn't match the content set by setContentStream() followed by appendContentStream()!");
                        addResult(assertEquals(CONTENT2 + CONTENT3, content, null, f));
                    } catch (IOException e) {
                        addResult(createResult(UNEXPECTED_EXCEPTION, "Document content couldn't be read! Exception: "
                                + e.getMessage(), e, true));
                    }
                } catch (CmisNotSupportedException e) {
                    addResult(createResult(WARNING, "appendContentStream() is not supported!"));
                }
            }

            // cancel a possible check out
            if (checkedout) {
                workDoc.cancelCheckOut();
            }

            // remove the document
            deleteObject(doc);
            
            runContentTest(session, getTestFolder());
        } finally {
            deleteTestFolder();
        }
    }

    private CapabilityContentStreamUpdates getContentStreamUpdatesCapbility(Session session) {
        if (session.getRepositoryInfo().getCapabilities() == null) {
            return null;
        }

        return session.getRepositoryInfo().getCapabilities().getContentStreamUpdatesCapability();
    }
    
    private void runContentTest(Session session, Folder testFolder) {
        if (session.getRepositoryInfo().getCapabilities().getContentStreamUpdatesCapability() != CapabilityContentStreamUpdates.ANYTIME) {
            addResult(createResult(SKIPPED, "Repository doesn't allow to replace content. Test skipped!"));
            return;
        }

        Document doc = createDocument(session, testFolder, "content1.txt", "Hello World!");

        try {
            if (doc.getChangeToken() == null) {
                addResult(createResult(SKIPPED, "Repository does not provide change tokens. Test skipped!"));
                return;
            }

            if (!doc.getAllowableActions().getAllowableActions().contains(Action.CAN_SET_CONTENT_STREAM)) {
                addResult(createResult(SKIPPED, "Document content can't be changed. Test skipped!"));
                return;
            }

            byte[] contentBytes = "New content".getBytes();
            ContentStream contentStream = new ContentStreamImpl("content2.txt",
                    BigInteger.valueOf(contentBytes.length), "text/plain", new ByteArrayInputStream(contentBytes));

            doc.setContentStream(contentStream, true, false);

            try {
                doc.setContentStream(contentStream, true, false);

                addResult(createResult(FAILURE, "Updating content a second time with the same change token "
                        + "should result in an UpdateConflict exception!"));
            } catch (CmisUpdateConflictException e) {
                // expected exception
            }
        } finally {
            deleteObject(doc);
        }
    }

    private Document getNewVersion(Session session, Document orgDoc, boolean checkedout, ObjectId newObjectId,
            String operation) {
        Document result = orgDoc;

        if (newObjectId != null) {
            // -> Non AtomPub binding
            if (!orgDoc.getId().equals(newObjectId.getId())) {
                if (checkedout) {
                    addResult(createResult(FAILURE, operation + " created a new version from a PWC!"));
                } else {
                    result = (Document) session.getObject(newObjectId, SELECT_ALL_NO_CACHE_OC);
                    addResult(checkObject(session, result, getAllProperties(result), "Version created by " + operation
                            + "  compliance"));
                }
            }
        } else {
            if (getBinding() != BindingType.ATOMPUB) {
                addResult(createResult(FAILURE, operation + " did not return an object id!"));
            }

            // -> AtomPub binding or incompliant other binding
            if (checkedout) {
                // we cannot check if the repository does the right thing,
                // but if there is a problem the versioning tests should
                // catch it
            } else if (Boolean.TRUE.equals(((DocumentTypeDefinition) orgDoc.getType()).isVersionable())) {
                List<Document> versions = orgDoc.getAllVersions();
                if (versions == null || versions.isEmpty()) {
                    addResult(createResult(FAILURE, operation
                            + " created a new version but the version history is empty!"));
                } else if (!orgDoc.getId().equals(versions.get(0).getId())) {
                    result = (Document) session.getObject(versions.get(0), SELECT_ALL_NO_CACHE_OC);
                    addResult(checkObject(session, result, getAllProperties(result), "Version created by " + operation
                            + " compliance"));
                }
            }
        }

        return result;
    }
}
