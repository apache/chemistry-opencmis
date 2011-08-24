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
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Copy test.
 */
public class SetAndDeleteContentTest extends AbstractSessionTest {

    private static final String CONTENT1 = "one";
    private static final String CONTENT2 = "two";

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Set and Delete content Test");
        setDescription("Creates a new document and tries to set and delete its content.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        if (getContentStreamUpdatesCapbility(session) == CapabilityContentStreamUpdates.NONE) {
            addResult(createInfoResult("Stream updates are not supported. Test skipped!"));
            return;
        }

        try {
            // create folder and document
            Folder testFolder = createTestFolder(session);
            Document doc = createDocument(session, testFolder, "contenttest.txt", CONTENT1);
            Document workDoc = doc;

            // test if check out is required and possible
            boolean checkedout = false;
            if (getContentStreamUpdatesCapbility(session) == CapabilityContentStreamUpdates.PWCONLY) {
                DocumentTypeDefinition docType = (DocumentTypeDefinition) doc.getType();

                if (!docType.isVersionable()) {
                    addResult(createResult(SKIPPED,
                            "Content stream operations only work if PWCs and the the test type is not versionable. Test skipped!"));
                    doc.delete(true);
                    return;
                }

                workDoc = (Document) session.getObject(doc.checkOut(), SELECT_ALL_NO_CACHE_OC);
                checkedout = true;
            }

            // delete content stream
            try {
                workDoc.deleteContentStream(true);

                f = createResult(FAILURE, "Document still has content after deleteContentStream() has been called!");
                addResult(assertNull(workDoc.getContentStream(), null, f));
            } catch (CmisNotSupportedException e) {
                addResult(createResult(WARNING, "deleteContentStream() is not supported!"));
            }

            // set a new content stream
            byte[] contentBytes = new byte[0];
            try {
                contentBytes = CONTENT2.getBytes("UTF-8");
            } catch (Exception e) {
            }

            ContentStream contentStream = new ContentStreamImpl(workDoc.getName(),
                    BigInteger.valueOf(contentBytes.length), "text/plain", new ByteArrayInputStream(contentBytes));

            workDoc.setContentStream(contentStream, true, true);

            // test new content
            try {
                String content = getStringFromContentStream(workDoc.getContentStream());
                f = createResult(FAILURE, "Document content doesn't match the content set by setContentStream()!");
                addResult(assertEquals(CONTENT2, content, null, f));
            } catch (IOException e) {
                addResult(createResult(UNEXPECTED_EXCEPTION,
                        "Document content couldn't be read! Exception: " + e.getMessage(), e, true));
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

    private CapabilityContentStreamUpdates getContentStreamUpdatesCapbility(Session session) {
        if (session.getRepositoryInfo().getCapabilities() == null) {
            return null;
        }

        return session.getRepositoryInfo().getCapabilities().getContentStreamUpdatesCapability();
    }
}
