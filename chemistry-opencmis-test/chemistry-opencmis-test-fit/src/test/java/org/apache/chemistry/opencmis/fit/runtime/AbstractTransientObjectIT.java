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
package org.apache.chemistry.opencmis.fit.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.TransientDocument;
import org.apache.chemistry.opencmis.client.api.TransientFolder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.junit.Test;

public abstract class AbstractTransientObjectIT extends AbstractSessionTest {

    @Test
    public void transientUpdate() throws Exception {
        ObjectId parentId = session
                .createObjectId(this.fixture.getTestRootId());
        String filename1 = UUID.randomUUID().toString();
        String typeId = FixtureData.DOCUMENT_TYPE_ID.value();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, filename1);
        properties.put(PropertyIds.OBJECT_TYPE_ID, typeId);

        String mimetype = "text/html; charset=UTF-8";
        String content1 = "Im Walde rauscht ein Wasserfall. Wenn's nicht mehr rauscht ist's Wasser all.";

        byte[] buf1 = content1.getBytes("UTF-8");
        ByteArrayInputStream in1 = new ByteArrayInputStream(buf1);
        ContentStream contentStream1 = session.getObjectFactory()
                .createContentStream(filename1, buf1.length, mimetype, in1);
        assertNotNull(contentStream1);

        ObjectId id = session.createDocument(properties, parentId,
                contentStream1, VersioningState.NONE);
        assertNotNull(id);

        // prepare new non-cache operation context
        OperationContext oc = session.createOperationContext();
        oc.setFilterString("*");
        oc.setCacheEnabled(false);

        // set new name and save
        Document doc2 = (Document) session.getObject(id, oc);
        TransientDocument tdoc2 = doc2.getTransientDocument();

        assertEquals(filename1, tdoc2.getName());

        ContentStream cs2 = tdoc2.getContentStream();
        assertNotNull(cs2);
        assertContent(buf1, readContent(cs2));

        String filename2 = UUID.randomUUID().toString();
        tdoc2.setName(filename2);
        assertEquals(filename2, tdoc2.getName());

        ObjectId id2 = tdoc2.save();
        assertNotNull(id2);

        // set new content and save
        Document doc3 = (Document) session.getObject(id2, oc);
        TransientDocument tdoc3 = doc3.getTransientDocument();

        assertEquals(filename2, tdoc3.getName());

        ContentStream cs3 = tdoc3.getContentStream();
        assertNotNull(cs3);
        assertContent(buf1, readContent(cs3));

        String content3 = "Es rauscht noch.";

        byte[] buf3 = content3.getBytes("UTF-8");
        ByteArrayInputStream in3 = new ByteArrayInputStream(buf3);
        ContentStream contentStream3 = session.getObjectFactory()
                .createContentStream(tdoc3.getName(), buf3.length, mimetype,
                        in3);
        assertNotNull(contentStream3);

        tdoc3.setContentStream(contentStream3, true);

        ObjectId id3 = tdoc3.save();
        assertNotNull(id3);

        // set new name, delete content and save
        Document doc4 = (Document) session.getObject(id3, oc);
        TransientDocument tdoc4 = doc4.getTransientDocument();

        assertEquals(tdoc3.getName(), tdoc4.getName());

        ContentStream cs4 = tdoc4.getContentStream();
        assertNotNull(cs4);
        assertContent(buf3, readContent(cs4));

        String filename4 = UUID.randomUUID().toString();
        tdoc4.setName(filename4);
        assertEquals(filename4, tdoc4.getName());

        tdoc4.deleteContentStream();

        ObjectId id4 = tdoc4.save();
        assertNotNull(id4);

        // delete object
        Document doc5 = (Document) session.getObject(id4, oc);
        TransientDocument tdoc5 = doc5.getTransientDocument();

        assertEquals(filename4, tdoc5.getName());

        ContentStream cs5 = tdoc4.getContentStream();
        assertNull(cs5);

        assertEquals(false, tdoc5.isMarkedForDelete());

        tdoc5.delete(true);

        assertEquals(true, tdoc5.isMarkedForDelete());

        ObjectId id5 = tdoc5.save();
        assertNull(id5);

        // check
        try {
            this.session.getObject(id4, oc);
            fail("CmisObjectNotFoundException expected!");
        } catch (CmisObjectNotFoundException e) {
            // expected
        }
    }

    @Test
    public void transientFolderSessionCheck() {
        String path = "/" + Fixture.TEST_ROOT_FOLDER_NAME + "/"
                + FixtureData.FOLDER1_NAME;
        Folder folder1 = (Folder) session.getObjectByPath(path);
        assertNotNull("folder not found: " + path, folder1);

        TransientFolder tfolder = folder1.getTransientFolder();
        assertNotNull(tfolder);

        String newFolderName = UUID.randomUUID().toString();
        tfolder.setPropertyValue(PropertyIds.NAME, newFolderName);

        Folder folder2 = (Folder) session2.getObjectByPath(path);
        assertNotNull(folder2);
        assertEquals(folder2.getProperty(PropertyIds.NAME).getValueAsString(),
                FixtureData.FOLDER1_NAME.toString());
        assertEquals(tfolder.getProperty(PropertyIds.NAME).getValueAsString(),
                newFolderName);

        tfolder.save();
        session2.clear();

        ObjectId id = session2.createObjectId(tfolder.getId());

        Folder folder3 = (Folder) session2.getObject(id);
        assertNotNull(folder3);
        assertEquals(folder3.getProperty(PropertyIds.NAME).getValueAsString(),
                newFolderName);
    }

    @Test
    public void transientDocumentSessionCheck() {
        String path = "/" + Fixture.TEST_ROOT_FOLDER_NAME + "/"
                + FixtureData.DOCUMENT1_NAME;
        Document document1 = (Document) session.getObjectByPath(path);
        assertNotNull("document not found: " + path, document1);

        TransientDocument tdoc = document1.getTransientDocument();
        assertNotNull(tdoc);

        String newDocName = UUID.randomUUID().toString();
        tdoc.setPropertyValue(PropertyIds.NAME, newDocName);

        Document doc2 = (Document) session2.getObjectByPath(path);
        assertNotNull(doc2);
        assertEquals(doc2.getProperty(PropertyIds.NAME).getValueAsString(),
                FixtureData.DOCUMENT1_NAME.toString());
        assertEquals(tdoc.getProperty(PropertyIds.NAME).getValueAsString(),
                newDocName);

        tdoc.save();
        session2.clear();

        ObjectId id = session2.createObjectId(tdoc.getId());

        Document doc3 = (Document) session2.getObject(id);
        assertNotNull(doc3);
        assertEquals(doc3.getProperty(PropertyIds.NAME).getValueAsString(),
                newDocName);
    }

    private static byte[] readContent(ContentStream contentStream)
            throws Exception {
        assertNotNull(contentStream);
        assertNotNull(contentStream.getStream());

        InputStream stream = contentStream.getStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int b;
        while ((b = stream.read(buffer)) > -1) {
            baos.write(buffer, 0, b);
        }

        return baos.toByteArray();
    }

    private static void assertContent(byte[] expected, byte[] actual) {
        assertNotNull(expected);
        assertNotNull(actual);

        assertEquals("Content size:", expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {
            assertEquals("Content not equal.", expected[i], actual[i]);
        }
    }
}
