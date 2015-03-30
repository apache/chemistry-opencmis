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

import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

public class WhitespaceInNameTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Whitespace in Name Test");
        setDescription("Creates documents with spaces in cmis:name.");
    }

    @Override
    public void run(Session session) {
        // create a test folder
        Folder testFolder = createTestFolder(session);

        try {
            testCenterSpaceSpace(session, testFolder);
            testMultipleCenterSpaceSpace(session, testFolder);
            testLeadingSpace(session, testFolder);
            testTrailingSpace(session, testFolder);
        } finally {
            // delete the test folder
            deleteTestFolder();
        }
    }

    private void testLeadingSpace(Session session, Folder testFolder) {
        String name = "leading.txt";

        try {
            Document doc = createDocument(session, testFolder, " " + name, "");

            if (doc.getName().equals(" " + name)) {
                addResult(createInfoResult("Repository does supports document names with a leading space."));
            } else {
                if (doc.getName().equals(name)) {
                    addResult(createInfoResult("Repository removes leading space from document name."));
                } else {
                    addResult(createInfoResult("Repository renames documents with a leading space."));
                }
            }
        } catch (CmisBaseException e) {
            addResult(createInfoResult("Repository does not support document names with a leading space. Exception: "
                    + e.toString()));
        }
    }

    private void testTrailingSpace(Session session, Folder testFolder) {
        String name = "trailing.txt";

        try {
            Document doc = createDocument(session, testFolder, name + " ", "");

            if (doc.getName().equals(name + " ")) {
                addResult(createInfoResult("Repository does supports document names with a trailing space."));
            } else {
                if (doc.getName().equals(name)) {
                    addResult(createInfoResult("Repository removes trailing space from document name."));
                } else {
                    addResult(createInfoResult("Repository renames documents with a trailing space."));
                }
            }
        } catch (CmisBaseException e) {
            addResult(createInfoResult("Repository does not support document names with a trailing space. Exception: "
                    + e.toString()));
        }
    }

    private void testCenterSpaceSpace(Session session, Folder testFolder) {
        String name = "center space.txt";

        try {
            Document doc = createDocument(session, testFolder, name, "");

            if (doc.getName().equals(name)) {
                addResult(createInfoResult("Repository does supports document names with a space."));
            } else {
                if (doc.getName().equals("centerspace.txt")) {
                    addResult(createInfoResult("Repository removes spaces from document name."));
                } else {
                    addResult(createInfoResult("Repository renames documents with a space."));
                }
            }
        } catch (CmisBaseException e) {
            addResult(createInfoResult("Repository does not support document names with a space. Exception: "
                    + e.toString()));
        }
    }

    private void testMultipleCenterSpaceSpace(Session session, Folder testFolder) {
        String name = "twocenter  spaces.txt";

        try {
            Document doc = createDocument(session, testFolder, name, "");

            if (doc.getName().equals(name)) {
                addResult(createInfoResult("Repository does supports document names with more than one successive spaces."));
            } else {
                if (doc.getName().equals("twocenterspaces.txt")) {
                    addResult(createInfoResult("Repository removes spaces from document name."));
                } else if (doc.getName().equals("twocenter spaces.txt")) {
                    addResult(createInfoResult("Repository combines multiple spaces into one in document names."));
                } else {
                    addResult(createInfoResult("Repository renames documents with a space."));
                }
            }
        } catch (CmisBaseException e) {
            addResult(createInfoResult("Repository does not support document names with a space. Exception: "
                    + e.toString()));
        }
    }
}
