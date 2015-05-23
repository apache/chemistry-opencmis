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
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.UNEXPECTED_EXCEPTION;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.chemistry.opencmis.client.api.AsyncSession;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.async.AbstractExecutorServiceAsyncSession;
import org.apache.chemistry.opencmis.client.runtime.async.AsyncSessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Simple document test.
 */
public class AsyncCreateAndDeleteDocumentTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Asynchronous Create and Delete Document Test");
        setDescription("Creates documents in parallel, checks the newly created documents and finally deletes the created documents in parallel.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        int numOfDocuments = 100;
        String mimeType = "text/plain";

        byte[] contentBytes = new byte[64 * 1024];
        for (int i = 0; i < contentBytes.length; i++) {
            contentBytes[i] = (byte) ('0' + i % 10);
        }

        // create an async session
        AsyncSession asyncSession = AsyncSessionFactoryImpl.newInstance().createAsyncSession(session, 10);

        // create a test folder
        Folder testFolder = createTestFolder(session);

        try {
            // create documents
            List<Future<ObjectId>> docFutures = new ArrayList<Future<ObjectId>>();
            for (int i = 0; i < numOfDocuments; i++) {
                String name = "asyncdoc" + i + ".txt";

                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put(PropertyIds.NAME, name);
                properties.put(PropertyIds.OBJECT_TYPE_ID, getDocumentTestTypeId());

                ContentStream contentStream = new ContentStreamImpl(name, BigInteger.valueOf(contentBytes.length),
                        mimeType, new ByteArrayInputStream(contentBytes));

                Future<ObjectId> newDocument = asyncSession.createDocument(properties, testFolder, contentStream, null);

                docFutures.add(newDocument);
            }

            // wait for all document being created
            List<ObjectId> docIds = new ArrayList<ObjectId>();
            try {
                for (Future<ObjectId> docFuture : docFutures) {
                    ObjectId id = docFuture.get();
                    docIds.add(id);
                }
            } catch (Exception e) {
                addResult(createResult(UNEXPECTED_EXCEPTION,
                        "Documents could not been created! Exception: " + e.getMessage(), e, true));
            }

            // check children of the test folder
            int count = countChildren(testFolder);
            f = createResult(FAILURE, "Test folder should have " + numOfDocuments + " children but has " + count + "!");
            addResult(assertEquals(count, numOfDocuments, null, f));

            // simple children test
            addResult(checkChildren(session, testFolder, "Test folder children check"));

            // get documents
            Map<String, Future<CmisObject>> getObjectFutures = new HashMap<String, Future<CmisObject>>();
            Map<String, Future<ContentStream>> contentStreamFutures = new HashMap<String, Future<ContentStream>>();
            Map<String, ByteArrayOutputStream> content = new HashMap<String, ByteArrayOutputStream>();

            for (ObjectId docId : docIds) {
                Future<CmisObject> getObjectFuture = asyncSession.getObject(docId, SELECT_ALL_NO_CACHE_OC);
                getObjectFutures.put(docId.getId(), getObjectFuture);

                ByteArrayOutputStream out = new ByteArrayOutputStream(contentBytes.length);
                content.put(docId.getId(), out);

                Future<ContentStream> contentStreamFuture = asyncSession.storeContentStream(docId, out);
                contentStreamFutures.put(docId.getId(), contentStreamFuture);
            }

            // wait for all document being fetched
            try {
                for (Map.Entry<String, Future<CmisObject>> getObjectFuture : getObjectFutures.entrySet()) {
                    CmisObject object = getObjectFuture.getValue().get();

                    f = createResult(FAILURE, "Fetching document failed!");
                    addResult(assertIsTrue(object instanceof Document, null, f));

                    if (object != null) {
                        f = createResult(FAILURE, "Fetched wrong document!");
                        addResult(assertEquals(getObjectFuture.getKey(), object.getId(), null, f));
                    }
                }
            } catch (Exception e) {
                addResult(createResult(UNEXPECTED_EXCEPTION,
                        "Documents could not been fetched! Exception: " + e.getMessage(), e, true));
            }

            // wait for all document content being fetched
            try {
                for (Map.Entry<String, Future<ContentStream>> contentStreamFuture : contentStreamFutures.entrySet()) {
                    ContentStream contentStream = contentStreamFuture.getValue().get();

                    f = createResult(FAILURE, "Fetching document content failed!");
                    addResult(assertNotNull(contentStream, null, f));

                    if (contentStream != null) {
                        if (contentStream.getMimeType() == null) {
                            addResult(createResult(FAILURE, "Content MIME type is null!"));
                        } else {
                            f = createResult(WARNING, "Content MIME types don't match!");
                            addResult(assertIsTrue(contentStream.getMimeType().trim().toLowerCase(Locale.ENGLISH)
                                    .startsWith(mimeType.toLowerCase(Locale.ENGLISH)), null, f));
                        }
                    }

                    ByteArrayOutputStream out = content.get(contentStreamFuture.getKey());
                    byte[] readBytes = out.toByteArray();

                    f = createResult(FAILURE, "Read content length doesn't match document content length!");
                    addResult(assertEquals(contentBytes.length, readBytes.length, null, f));

                    f = createResult(FAILURE, "Read content doesn't match document content!");
                    addResult(assertEqualArray(contentBytes, readBytes, null, f));
                }
            } catch (Exception e) {
                addResult(createResult(UNEXPECTED_EXCEPTION,
                        "Document content could not been fetched! Exception: " + e.getMessage(), e, true));
            }

            // delete documents
            List<Future<?>> delFutures = new ArrayList<Future<?>>();
            for (ObjectId docId : docIds) {
                Future<?> delFuture = asyncSession.delete(docId);
                delFutures.add(delFuture);
            }

            // wait for all document being deleted
            try {
                for (Future<?> delFuture : delFutures) {
                    delFuture.get();
                }
            } catch (Exception e) {
                addResult(createResult(UNEXPECTED_EXCEPTION,
                        "Documents could not been deleted! Exception: " + e.getMessage(), e, true));
            }

            // check children of the test folder
            count = countChildren(testFolder);
            f = createResult(FAILURE, "Test folder should be empty but has " + count + " children!");
            addResult(assertEquals(count, 0, null, f));
        } finally {
            // delete the test folder
            deleteTestFolder();

            if (asyncSession instanceof AbstractExecutorServiceAsyncSession<?>) {
                ((AbstractExecutorServiceAsyncSession<?>) asyncSession).shutdown();
            }
        }

        addResult(createInfoResult("Tested the parallel creation and deletion of " + numOfDocuments + " documents."));
    }

    private int countChildren(Folder folder) {
        int count = 0;
        ItemIterable<CmisObject> children = folder.getChildren(SELECT_ALL_NO_CACHE_OC);
        for (CmisObject child : children) {
            if (child instanceof Document) {
                count++;
            }
        }

        return count;
    }
}
