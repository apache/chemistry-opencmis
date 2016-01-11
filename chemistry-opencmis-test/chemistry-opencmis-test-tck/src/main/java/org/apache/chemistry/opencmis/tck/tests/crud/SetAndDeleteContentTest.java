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
import java.io.OutputStream;
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
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
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

                    f = createResult(
                            FAILURE,
                            "Document still has a MIME type after deleteContentStream() has been called: "
                                    + contentDoc.getContentStreamMimeType());
                    addResult(assertNull(contentDoc.getContentStreamMimeType(), null, f));

                    f = createResult(FAILURE,
                            "Document still has a content length after deleteContentStream() has been called: "
                                    + contentDoc.getContentStreamLength());
                    addResult(assertEquals(-1L, contentDoc.getContentStreamLength(), null, f));

                    f = createResult(
                            FAILURE,
                            "Document still has a file name after deleteContentStream() has been called: "
                                    + contentDoc.getContentStreamFileName());
                    addResult(assertNull(contentDoc.getContentStreamFileName(), null, f));

                    workDoc = contentDoc;
                } catch (CmisNotSupportedException e) {
                    addResult(createResult(WARNING, "deleteContentStream() is not supported!"));
                }
            }

            // set a new content stream
            byte[] contentBytes = IOUtils.toUTF8Bytes(CONTENT2);

            try {
                ContentStream contentStream = session.getObjectFactory().createContentStream(workDoc.getName(),
                        contentBytes.length, "text/plain", new ByteArrayInputStream(contentBytes));

                ObjectId newObjectId = workDoc.setContentStream(contentStream, true, true);

                IOUtils.closeQuietly(contentStream);

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
                contentBytes = IOUtils.toUTF8Bytes(CONTENT3);

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

                    // test append stream
                    testAppendStream(session, testFolder, 16 * 1024);
                    testAppendStream(session, testFolder, 8);
                    testAppendStream(session, testFolder, 0);
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
        } finally {
            deleteTestFolder();
        }
    }

    private void testAppendStream(Session session, Folder testFolder, int bufferSize) {
        CmisTestResult f;

        // create an empty document
        Document doc = createDocument(session, testFolder, "appendstreamtest.txt", "");
        Document workDoc = doc;

        boolean checkedout = false;
        DocumentTypeDefinition docType = (DocumentTypeDefinition) doc.getType();

        if (Boolean.TRUE.equals(docType.isVersionable())) {
            workDoc = (Document) session.getObject(doc.checkOut(), SELECT_ALL_NO_CACHE_OC);
            checkedout = true;
        }

        try {
            // create an overwrite OutputStream
            OutputStream out1 = workDoc.createOverwriteOutputStream("appendstreamtest", "text/plain", bufferSize);

            out1.write(IOUtils.toUTF8Bytes("line 1\n"));
            out1.write(IOUtils.toUTF8Bytes("line 2\n"));
            out1.flush();

            out1.write(IOUtils.toUTF8Bytes("line 3\n"));
            out1.close();

            // check document content
            workDoc.refresh();
            String content1 = getStringFromContentStream(workDoc.getContentStream());

            f = createResult(FAILURE, "Overwrite OutputStream: wrong content!");
            addResult(assertEquals("line 1\nline 2\nline 3\n", content1, null, f));

            // create an append OutputStream
            OutputStream out2 = workDoc.createAppendOutputStream(bufferSize);

            out2.write(IOUtils.toUTF8Bytes("line 4\n"));
            out2.write(IOUtils.toUTF8Bytes("line 5\n"));
            out2.flush();

            out2.write(IOUtils.toUTF8Bytes("line 6\n"));
            out2.close();

            // check document content
            workDoc.refresh();
            String content2 = getStringFromContentStream(workDoc.getContentStream());

            f = createResult(FAILURE, "Overwrite OutputStream: wrong content!");
            addResult(assertEquals("line 1\nline 2\nline 3\nline 4\nline 5\nline 6\n", content2, null, f));

        } catch (IOException e) {
            addResult(createResult(UNEXPECTED_EXCEPTION, "Appending content via an OutputStream failed! Exception: "
                    + e.getMessage(), e, false));
        } finally {
            // cancel a possible check out
            if (checkedout) {
                workDoc.cancelCheckOut();
            }

            // remove the document
            deleteObject(doc);
        }
    }

    private CapabilityContentStreamUpdates getContentStreamUpdatesCapbility(Session session) {
        if (session.getRepositoryInfo().getCapabilities() == null) {
            return null;
        }

        return session.getRepositoryInfo().getCapabilities().getContentStreamUpdatesCapability();
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
