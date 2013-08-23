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
package org.apache.chemistry.opencmis.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.inmemory.types.DocumentTypeCreationHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiFilingTest extends AbstractServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(MultiFilingTest.class);
    private static final String DOCUMENT_TYPE_ID = UnitTestTypeSystemCreator.COMPLEX_TYPE;
    private static final String FOLDER_TYPE_ID = DocumentTypeCreationHelper.getCmisFolderType().getId();
    private static final String UNFILED_DOC_NAME = "Unfiled document";
    private static final String RENAMED_DOC_NAME = "My Renamed Document";

    private String fId1;
    private String fId2;
    private String fId11;

    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testCreateUnfiledDocument() {
        LOG.debug("Begin testCreatUnfiledDocument()");
        String docId = createUnfiledDocument();
        String docId2 = getDocument(docId);
        assertEquals(docId, docId2);

        // get object parents, must be empty
        List<ObjectParentData> res = fNavSvc.getObjectParents(fRepositoryId, docId, "*", false,
                IncludeRelationships.NONE, null, true, null);

        assertNotNull(res);
        assertEquals(res.size(), 0);

        // test with a different user than Admin:
        switchCallContext("Alice");
        docId = createDocument(UNFILED_DOC_NAME + "_2", null, DOCUMENT_TYPE_ID, true);
        docId2 = getDocument(docId);
        assertEquals(docId, docId2);

        LOG.debug("End testCreatUnfiledDocument()");
    }

    @Test
    public void testMakeFiledDocumentUnfiled() {
        LOG.debug("Begin testMakeFiledDocumentUnfiled()");

        String docId = createDocument("Filed document", fRootFolderId, DOCUMENT_TYPE_ID, true);

        fMultiSvc.removeObjectFromFolder(fRepositoryId, docId, fRootFolderId, null);
        List<ObjectParentData> parents = fNavSvc.getObjectParents(fRepositoryId, docId, "*", false,
                IncludeRelationships.NONE, null, true, null);
        assertEquals(0, parents.size());

        LOG.debug("End testMakeFiledDocumentUnfiled()");
    }

    @Test
    public void testAddDocumentToFolder() {
        LOG.debug("Begin testAddDocumentToFolder()");
        String docId = createUnfiledDocument();
        addDocumentToFolder(docId);
        LOG.debug("End testAddDocumentToFolder()");
    }

    @Test
    public void testRemoveDocumentFromFolder() {
        LOG.debug("Begin testRemoveDocumentFromFolder()");

        String docId = createUnfiledDocument();
        removeDocumentFromFolder(docId);
        LOG.debug("End testRemoveDocumentFromFolder()");
    }

    @Test
    public void testMoveMultiFiledDocument() {
        LOG.debug("begin testMoveMultiFiledDocument()");
        String docId = createUnfiledDocument();
        prepareMultiFiledDocument(docId);
        String newFolderId = createFolder("folder2.1", fId2, FOLDER_TYPE_ID);

        Holder<String> idHolder = new Holder<String>(docId);
        fObjSvc.moveObject(fRepositoryId, idHolder, newFolderId, fId11, null);
        List<ObjectParentData> parents = fNavSvc.getObjectParents(fRepositoryId, docId, "*", false,
                IncludeRelationships.NONE, null, true, null);
        assertEquals(3, parents.size());
        boolean foundNewParent = false;
        boolean foundOldParent = false;
        for (ObjectParentData parentData : parents) {
            if (parentData.getObject().getId().equals(newFolderId)) {
                foundNewParent = true;
            }
            if (parentData.getObject().getId().equals(fId11)) {
                foundOldParent = true;
            }
        }
        assertTrue("After move new target should be a parent", foundNewParent);
        assertFalse("After move old source should no longer be a parent", foundOldParent);
        LOG.debug("End testMoveMultiFiledDocument()");
    }

    @Test
    public void testRenameMultiFiledDocument() {
        LOG.debug("begin testRenameMultiFiledDocument()");
        String docId = createUnfiledDocument();
        prepareMultiFiledDocument(docId);
        renameDocumentAndCheckResult(docId);
        LOG.debug("End testRenameMultiFiledDocument()");
    }

    @Test
    public void testRenameMultiFiledDocumentWithNameConflict() {
        LOG.debug("begin testRenameMultiFiledDocument()");
        String docId = createUnfiledDocument();
        prepareMultiFiledDocument(docId);
        // create a document with the new name in one of the folders
        createDocument(RENAMED_DOC_NAME, fId11, DOCUMENT_TYPE_ID, true);
        // try to rename which should fail now
        try {
            renameDocumentAndCheckResult(docId);
            fail("A rename to an existing name in one of the filed folders should fail");
        } catch (Exception e) {
            assertTrue(e instanceof CmisNameConstraintViolationException);
        }
        LOG.debug("End testRenameMultiFiledDocument()");
    }

    @Test
    public void testRemoveDocumentFromAllFolders() {
        LOG.debug("Begin testRemoveDocumentFromAllFolders()");

        String docId = createUnfiledDocument();
        removeDocumentFromAllFolders(docId);
        LOG.debug("End testRemoveDocumentFromAllFolders()");
    }

    @Test
    public void testAddVersionedDocumentToFolder() {
        LOG.debug("Begin testAddVersionedDocumentToFolder()");
        String docId = createVersionedDocument();
        addDocumentToFolder(docId);
        LOG.debug("End testAddVersionedDocumentToFolder()");
    }

    @Test
    public void testRemoveVersionedDocumentFromFolder() {
        LOG.debug("Begin testRemoveVersionedDocumentFromFolder()");

        String docId = createVersionedDocument();
        removeDocumentFromFolder(docId);
        LOG.debug("End testRemoveVersionedDocumentFromFolder()");
    }

    @Test
    public void testRemoveVersionedDocumentFromAllFolders() {
        LOG.debug("Begin testRemoveVersionedDocumentFromAllFolders()");

        String docId = createVersionedDocument();
        removeDocumentFromAllFolders(docId);
        LOG.debug("End testRemoveVersionedDocumentFromAllFolders()");
    }

    private void createFolders() {
        fId1 = createFolder("folder1", fRootFolderId, FOLDER_TYPE_ID);
        fId2 = createFolder("folder2", fRootFolderId, FOLDER_TYPE_ID);
        fId11 = createFolder("folder1.1", fId1, FOLDER_TYPE_ID);
    }

    private void addDocumentToFolder(String docId) {

        List<String> folderIds = prepareMultiFiledDocument(docId);

        // get object parents, must contain all folders
        List<ObjectParentData> res = fNavSvc.getObjectParents(fRepositoryId, docId, "*", false,
                IncludeRelationships.NONE, null, true, null);
        assertEquals(3, res.size());
        for (ObjectParentData opd : res) {
            assertTrue(folderIds.contains(opd.getObject().getId()));
            assertEquals(BaseTypeId.CMIS_FOLDER, opd.getObject().getBaseTypeId());
            assertEquals(UNFILED_DOC_NAME, opd.getRelativePathSegment());
        }

        // try version specific filing, should fail
        try {
            fMultiSvc.addObjectToFolder(fRepositoryId, docId, fId1, false, null);
            fail("Adding not all versions to a folder should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisNotSupportedException);
        }
    }

    private void removeDocumentFromFolder(String docId) {
        prepareMultiFiledDocument(docId);

        fMultiSvc.removeObjectFromFolder(fRepositoryId, docId, fId1, null);
        List<ObjectParentData> parents = fNavSvc.getObjectParents(fRepositoryId, docId, "*", false,
                IncludeRelationships.NONE, null, true, null);
        assertEquals(2, parents.size());
        for (ObjectParentData opd : parents) {
            assertFalse(fId1.equals(opd.getObject().getId()));
        }

        fMultiSvc.removeObjectFromFolder(fRepositoryId, docId, fId2, null);
        parents = fNavSvc.getObjectParents(fRepositoryId, docId, "*", false, IncludeRelationships.NONE, null, true,
                null);
        assertEquals(1, parents.size());
        for (ObjectParentData opd : parents) {
            assertFalse(fId1.equals(opd.getObject().getId()));
        }

        fMultiSvc.removeObjectFromFolder(fRepositoryId, docId, fId11, null);
        parents = fNavSvc.getObjectParents(fRepositoryId, docId, "*", false, IncludeRelationships.NONE, null, true,
                null);
        assertEquals(0, parents.size());
    }

    private void removeDocumentFromAllFolders(String docId) {
        prepareMultiFiledDocument(docId);

        fMultiSvc.removeObjectFromFolder(fRepositoryId, docId, null, null);
        List<ObjectParentData> parents = fNavSvc.getObjectParents(fRepositoryId, docId, "*", false,
                IncludeRelationships.NONE, null, true, null);
        assertEquals(0, parents.size());
    }

    private String createUnfiledDocument() {
        return createDocument(UNFILED_DOC_NAME, null, DOCUMENT_TYPE_ID, true);
    }

    private List<String> prepareMultiFiledDocument(String docId) {
        createFolders();

        // add the document to three folders
        fMultiSvc.addObjectToFolder(fRepositoryId, docId, fId1, true, null);
        fMultiSvc.addObjectToFolder(fRepositoryId, docId, fId2, true, null);
        fMultiSvc.addObjectToFolder(fRepositoryId, docId, fId11, true, null);

        List<String> folderIds = new ArrayList<String>();
        folderIds.add(fId1);
        folderIds.add(fId2);
        folderIds.add(fId11);

        return folderIds;
    }

    private void renameDocumentAndCheckResult(String docId) {
        Holder<String> idHolder = new Holder<String>(docId);
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, RENAMED_DOC_NAME));
        Properties newProps = fFactory.createPropertiesData(properties);
        Holder<String> changeTokenHolder = new Holder<String>();
        fObjSvc.updateProperties(fRepositoryId, idHolder, changeTokenHolder, newProps, null);
        docId = idHolder.getValue();
        ObjectData res = fObjSvc.getObject(fRepositoryId, docId, "*", false, IncludeRelationships.NONE, null, false,
                false, null);
        assertNotNull(res);
        Map<String, PropertyData<?>> propMap = res.getProperties().getProperties();
        PropertyData<?> pd = propMap.get(PropertyIds.NAME);
        assertNotNull(pd);
        assertEquals(RENAMED_DOC_NAME, pd.getFirstValue());
    }

    private String createVersionedDocument() {

        return createDocument(UNFILED_DOC_NAME, null, UnitTestTypeSystemCreator.VERSIONED_TYPE, VersioningState.MAJOR,
                true);

    }

    private void switchCallContext(String user) {
        ((DummyCallContext) fTestCallContext).put(CallContext.USERNAME, user);
    }

}
