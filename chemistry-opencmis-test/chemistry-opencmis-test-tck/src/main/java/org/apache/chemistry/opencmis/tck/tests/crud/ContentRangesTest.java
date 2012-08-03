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

import java.math.BigInteger;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Content Range Test.
 */
public class ContentRangesTest extends AbstractSessionTest {

    private static final String CONTENT = "0123456789012345678901234567890";

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Content Ranges Test");
        setDescription("Creates a document and reads different excerpts of the content.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        // create a test folder
        Folder testFolder = createTestFolder(session);
        Document doc = null;

        try {
            // create the document
            doc = createDocument(session, testFolder, "testcontent.txt", CONTENT);

            String excerpt;
            ContentStream content;

            // offset, no length
            try {
                content = doc.getContentStream(BigInteger.valueOf(3), null);
                excerpt = getStringFromContentStream(content);

                if (CONTENT.equals(excerpt)) {
                    addResult(createResult(WARNING,
                            "Retrieved full stream instead of an excerpt {offset=3, length=null}! Content ranges supported?"));
                } else {
                    f = createResult(FAILURE, "Retrieved stream excerpt {offset=3, length=null} doesn't match!");
                    addResult(assertEquals(CONTENT.substring(3), excerpt, null, f));
                }
            } catch (Exception e) {
                addResult(createResult(FAILURE,
                        "Unexpected exception while retrieving stream {offset=3, length=null}: " + e, e, false));
            }

            // no offset, length
            try {
                content = doc.getContentStream(null, BigInteger.valueOf(12));
                excerpt = getStringFromContentStream(content);

                if (CONTENT.equals(excerpt)) {
                    addResult(createResult(WARNING,
                            "Retrieved full stream instead of an excerpt {offset=null, length=12}! Content ranges supported?"));
                } else {
                    f = createResult(FAILURE, "Retrieved stream excerpt {offset=null, length=12} doesn't match!");
                    addResult(assertEquals(CONTENT.substring(0, 12), excerpt, null, f));
                }
            } catch (Exception e) {
                addResult(createResult(FAILURE,
                        "Unexpected exception while retrieving stream {offset=null, length=12}: " + e, e, false));
            }

            // offset and length
            try {
                content = doc.getContentStream(BigInteger.valueOf(5), BigInteger.valueOf(17));
                excerpt = getStringFromContentStream(content);

                if (CONTENT.equals(excerpt)) {
                    addResult(createResult(WARNING,
                            "Retrieved full stream instead of an excerpt {offset=5, length=17}! Content ranges supported?"));
                } else {
                    f = createResult(FAILURE, "Retrieved stream excerpt {offset=5, length=17} doesn't match!");
                    addResult(assertEquals(CONTENT.substring(5, 5 + 17), excerpt, null, f));
                }
            } catch (Exception e) {
                addResult(createResult(FAILURE, "Unexpected exception while retrieving stream {offset=5, length=17}: "
                        + e, e, false));
            }

            // offset and length > content size
            try {
                content = doc.getContentStream(BigInteger.valueOf(9), BigInteger.valueOf(123));
                excerpt = getStringFromContentStream(content);

                if (CONTENT.equals(excerpt)) {
                    addResult(createResult(WARNING,
                            "Retrieved full stream instead of an excerpt {offset=9, length=123}! Content ranges supported?"));
                } else {
                    f = createResult(FAILURE, "Retrieved stream excerpt {offset=9, length=123} doesn't match!");
                    addResult(assertEquals(CONTENT.substring(9), excerpt, null, f));
                }
            } catch (Exception e) {
                addResult(createResult(FAILURE, "Unexpected exception while retrieving stream {offset=9, length=123}: "
                        + e, e, false));
            }
        } finally {
            // clean up
            deleteObject(doc);
            deleteTestFolder();
        }
    }
}
