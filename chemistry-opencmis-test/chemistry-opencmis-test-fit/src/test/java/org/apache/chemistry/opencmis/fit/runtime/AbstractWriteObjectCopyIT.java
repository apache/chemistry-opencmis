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
import static org.junit.Assert.assertNotSame;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.junit.Test;

public abstract class AbstractWriteObjectCopyIT extends AbstractSessionTest {

    @Test
    public void copyDocument() {
        String pathr = "/" + Fixture.TEST_ROOT_FOLDER_NAME;
        Folder testRoot = (Folder) this.session.getObjectByPath(pathr);

        // create a folder to copy into
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, "newfolder");
        properties.put(PropertyIds.OBJECT_TYPE_ID,
                FixtureData.FOLDER_TYPE_ID.value());
        ObjectId folderId = session.createFolder(properties, testRoot);
        assertNotNull(folderId);

        // get the document
        String pathd = "/" + Fixture.TEST_ROOT_FOLDER_NAME + "/"
                + FixtureData.DOCUMENT1_NAME;
        Document document = (Document) session.getObjectByPath(pathd);

        // copy the document
        properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, "newdocname");
        Document copy = document.copy(folderId, properties, null, null, null, null, session.getDefaultContext());

        assertEquals(folderId.getId(), copy.getParents().get(0).getId());
        assertEquals("newdocname", copy.getPropertyValue(PropertyIds.NAME));

        // old doc still unchanged
        session.clear();
        document = (Document) session.getObjectByPath(pathd);
        assertNotSame("newdocname", document.getPropertyValue(PropertyIds.NAME));
    }

}
