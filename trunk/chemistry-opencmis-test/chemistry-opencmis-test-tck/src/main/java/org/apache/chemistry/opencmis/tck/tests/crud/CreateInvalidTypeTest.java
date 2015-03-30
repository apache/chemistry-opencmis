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

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

public class CreateInvalidTypeTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Create Object With Invalid Type Test");
        setDescription("Tries to create document with a folder type and folder with a document type.");
    }

    @Override
    public void run(Session session) {
        // create a test folder
        Folder testFolder = createTestFolder(session);

        try {
            // test document creation
            try {
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put(PropertyIds.NAME, "never.txt");
                properties.put(PropertyIds.OBJECT_TYPE_ID, getFolderTestTypeId());

                byte[] contentBytes = IOUtils.toUTF8Bytes("nothing");
                ContentStream contentStream = new ContentStreamImpl("never.txt",
                        BigInteger.valueOf(contentBytes.length), "text/plain", new ByteArrayInputStream(contentBytes));

                testFolder.createDocument(properties, contentStream, null);

                addResult(createResult(FAILURE, "Creation of a document with a folder type shouldn't work!"));
            } catch (Exception e) {
                if (!(e instanceof CmisInvalidArgumentException) && !(e instanceof CmisConstraintException)) {
                    addResult(createResult(WARNING,
                            "Creation of a document with a folder type threw an unexcpeted exception: " + e.toString()));
                }
            }

            // test folder creation
            try {
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put(PropertyIds.NAME, "never");
                properties.put(PropertyIds.OBJECT_TYPE_ID, getDocumentTestTypeId());

                testFolder.createFolder(properties);

                addResult(createResult(FAILURE, "Creation of a folder with a document type shouldn't work!"));
            } catch (Exception e) {
                if (!(e instanceof CmisInvalidArgumentException) && !(e instanceof CmisConstraintException)) {
                    addResult(createResult(WARNING,
                            "Creation of a folder with a document type threw an unexcpeted exception: " + e.toString()));
                }
            }
        } finally {
            // delete the test folder
            deleteTestFolder();
        }
    }
}
