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

import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Document names test.
 */
public class NameCharsetTest extends AbstractSessionTest {

    private static final String[] NAMES = new String[] { //
    "\u0064\u006f\u0063\u0075\u006d\u0065\u006e\u0074", //
            "\u0053\u0063\u0068\u0072\u0069\u0066\u0074\u0073\u0074\u00fc\u0063\u006b", //
            "\u0648\u062b\u064a\u0642\u0629", //
            "\u0073\u0259\u006e\u0259\u0064", //
            "\u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442", //
            "\u6587\u4ef6", //
            "\u03ad\u03b3\u03b3\u03c1\u03b1\u03c6\u03bf", //
            "\u0aa6\u0ab8\u0acd\u0aa4\u0abe\u0ab5\u0ac7\u0a9c", //
            "\u0926\u0938\u094d\u0924\u093e\u0935\u0947\u091c\u093c", //
            "\u0064\u006f\u0069\u0063\u0069\u006d\u00e9\u0061\u0064", //
            "\u30c9\u30ad\u30e5\u30e1\u30f3\u30c8", //
            "\u05d3\u05d0\u05b8\u05e7\u05d5\u05de\u05e2\u05e0\u05d8", //
            "\u0ca6\u0cbe\u0c96\u0cb2\u0cc6", //
            "\ubb38\uc11c", //
            "\u0633\u0646\u062f", //
            "\u0b86\u0bb5\u0ba3\u0bae\u0bcd", //
            "\u0c2a\u0c24\u0c4d\u0c30\u0c02", //
            "\u0e40\u0e2d\u0e01\u0e2a\u0e32\u0e23", //
            "\u062f\u0633\u062a\u0627\u0648\u06cc\u0632", //
            "\u0074\u00e0\u0069\u0020\u006c\u0069\u1ec7\u0075", //
            "a&b", //
            "abc%_Pxyz" };

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Name Charset Test");
        setDescription("Creates and deletes documents with special characters in cmis:name.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        // create a test folder
        Folder testFolder = createTestFolder(session);

        try {
            for (int i = 0; i < NAMES.length; i++) {
                Document doc = null;
                Document doc2 = null;
                try {
                    doc = null;
                    doc = createDocument(session, testFolder, NAMES[i], NAMES[i]);

                    // get the newly created object by path
                    String path = doc.getPaths().get(0);
                    doc2 = (Document) session.getObjectByPath(path, SELECT_ALL_NO_CACHE_OC);
                    addResult(checkObject(session, doc2, getAllProperties(doc2), "New document object spec compliance"));

                    f = createResult(FAILURE, "Names of the created and the fetched document don't match!");
                    assertEquals(NAMES[i], doc2.getName(), null, f);

                    ContentStream contentStream = doc.getContentStream();

                    f = createResult(FAILURE, "Document has no content!");
                    assertNotNull(contentStream, null, f);

                    IOUtils.consumeAndClose(contentStream.getStream());
                } catch (Exception e) {
                    addResult(createResult(WARNING, "The name '" + NAMES[i] + "' raised this exception: " + e, e, false));
                } finally {
                    if (doc != null) {
                        // delete it
                        try {
                            doc.delete(true);
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
            }

            addResult(createInfoResult("Tested " + NAMES.length + " different names."));
        } finally {
            // delete the test folder
            deleteTestFolder();
        }
    }
}
