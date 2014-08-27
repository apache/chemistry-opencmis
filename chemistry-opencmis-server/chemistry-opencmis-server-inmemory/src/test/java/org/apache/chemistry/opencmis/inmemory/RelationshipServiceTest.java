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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.inmemory.ObjectServiceTest.ObjectTestTypeSystemCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelationshipServiceTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ObjectServiceTest.class);
    private static final String MY_DOC_1 = "SourceDocument_1";
    private static final String MY_DOC_2 = "SourceDocument_2";
    private static final String MY_DOC_3 = "SourceDocument_3";
    private static final String MY_DOC_4 = "SourceDocument_4";
    private static final String MY_DOC_TARGET = "TargetDocument";
    private static final String REL_CUSTOM_PROP_VALUE = "Simple Cross Reference";

    ObjectCreator fCreator;
    String docId1;
    String docId2;
    String docId3;
    String docId4;
    String targetId;

    @Override
    @Before
    public void setUp() {
        super.setTypeCreatorClass(ObjectTestTypeSystemCreator.class.getName());
        super.setUp();
        fCreator = new ObjectCreator(fFactory, fObjSvc, fRepositoryId);

        // create test data
        docId1 = createDocument(MY_DOC_1, fRootFolderId, BaseTypeId.CMIS_DOCUMENT.value(), false);
        docId2 = createDocument(MY_DOC_2, fRootFolderId, BaseTypeId.CMIS_DOCUMENT.value(), true);
        targetId = createDocument(MY_DOC_TARGET, fRootFolderId, BaseTypeId.CMIS_DOCUMENT.value(), false);
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testCreateGetRelationship() {

        log.info("starting testCreateRelationship() ...");
        List<PropertyData<?>> relProps1 = createRelationshipProperties(REL_CUSTOM_PROP_VALUE);
        final String id1 = createRelationship("CrossReference1", ObjectServiceTest.TEST_RELATION_TYPE_ID, docId1,
                targetId, relProps1);
        assertNotNull(id1);
        List<PropertyData<?>> relProps2 = createRelationshipProperties(REL_CUSTOM_PROP_VALUE);
        final String id2 = createRelationship("CrossReference2", ObjectServiceTest.TEST_RELATION_TYPE_ID, docId2,
                targetId, relProps2);
        if (id2 != null) {
            log.info("createRelationship succeeded with created id: " + id2);
        }
        assertNotNull(id2);

        List<String> relIds = new ArrayList<String>() {
            {
                add(id1);
                add(id2);
            }
        };

        // Get the relationships back
        // source
        ObjectList objectList = fRelSvc.getObjectRelationships(REPOSITORY_ID, targetId, false,
                RelationshipDirection.TARGET, ObjectServiceTest.TEST_RELATION_TYPE_ID, null, null, null, null, null);
        assertEquals(2, objectList.getNumItems().longValue());
        List<ObjectData> objectDataList = objectList.getObjects();
        List<String> ids = new ArrayList<String>(relIds);
        for (ObjectData objectData : objectDataList) {
            verifyRelation(objectData, ids, ObjectServiceTest.TEST_RELATION_TYPE_ID, targetId,
                    RelationshipDirection.TARGET);
        }
        assertTrue(ids.isEmpty());

        // target
        objectList = fRelSvc.getObjectRelationships(REPOSITORY_ID, docId1, false, RelationshipDirection.SOURCE,
                ObjectServiceTest.TEST_RELATION_TYPE_ID, null, null, null, null, null);

        assertEquals(1, objectList.getNumItems().longValue());
        ObjectData objectData = objectList.getObjects().get(0);
        ids = new ArrayList<String>(Collections.singletonList(id1));
        verifyRelation(objectData, ids, ObjectServiceTest.TEST_RELATION_TYPE_ID, docId1, RelationshipDirection.SOURCE);
        assertTrue(ids.isEmpty());

        // both
        ids = new ArrayList<String>(relIds);
        objectList = fRelSvc.getObjectRelationships(REPOSITORY_ID, targetId, false, RelationshipDirection.EITHER,
                ObjectServiceTest.TEST_RELATION_TYPE_ID, null, null, null, null, null);
        assertEquals(2, objectList.getNumItems().longValue());
        objectDataList = objectList.getObjects();
        ids = new ArrayList<String>(relIds);
        for (ObjectData od : objectDataList) {
            verifyRelation(od, ids, ObjectServiceTest.TEST_RELATION_TYPE_ID, targetId, RelationshipDirection.EITHER);
        }
        assertTrue(ids.isEmpty());

        log.info("... testCreateRelationship() finished.");

    }

    @Test
    public void testRelationToFolder() {
        // test create with a folder as source
        String folderId = createFolder("folder1", fRootFolderId, BaseTypeId.CMIS_FOLDER.value());
        List<PropertyData<?>> relProps = createRelationshipProperties(REL_CUSTOM_PROP_VALUE);
        final String id = createRelationship("CrossReference3", ObjectServiceTest.TEST_RELATION_TYPE_ID, folderId,
                targetId, relProps);
        assertNotNull(id);

        ObjectList objectList = fRelSvc.getObjectRelationships(REPOSITORY_ID, folderId, false,
                RelationshipDirection.SOURCE, ObjectServiceTest.TEST_RELATION_TYPE_ID, null, null, null, null, null);

        assertEquals(1, objectList.getNumItems().longValue());
        ObjectData objectData = objectList.getObjects().get(0);
        ArrayList<String> ids = new ArrayList<String>(Collections.singletonList(id));
        verifyRelation(objectData, ids, ObjectServiceTest.TEST_RELATION_TYPE_ID, folderId, RelationshipDirection.SOURCE);
        assertTrue(ids.isEmpty());

    }

    @Test
    public void testCreateIllegalTypes() {
        // test create a relationship with a folder type, should fail:
        try {
            createRelationship("CrossReference1", BaseTypeId.CMIS_FOLDER.value(), docId1, docId2, null);
            fail("Creating  document with a folder type should fail.");
        } catch (CmisInvalidArgumentException e) {
            log.info("Creating a relationship with a folder type failed as expected.");
        } catch (Exception e) {
            fail("Creating a relationship with a folder type should fail with a CmisInvalidArgumentException, but was: "
                    + e);
        }
    }

    @Test
    public void getRelationshipSubTypes() {
        log.info("starting testCreateRelationship() ...");
        final String id1 = createRelationship("CrossReference1", BaseTypeId.CMIS_RELATIONSHIP.value(), docId1,
                targetId, null);
        assertNotNull(id1);
        final String id2 = createRelationship("CrossReference2", ObjectServiceTest.TEST_RELATION_TYPE_ID, docId2,
                targetId, null);
        if (id2 != null) {
            log.info("createRelationship succeeded with created id: " + id2);
        }
        assertNotNull(id2);

        List<String> relIds = new ArrayList<String>() {
            {
                add(id1);
                add(id2);
            }
        };

        // Get the relationships back
        ObjectList objectList = fRelSvc.getObjectRelationships(REPOSITORY_ID, targetId, false,
                RelationshipDirection.TARGET, BaseTypeId.CMIS_RELATIONSHIP.value(), null, null, null, null, null);
        assertEquals(1, objectList.getNumItems().longValue());
        List<ObjectData> objectDataList = objectList.getObjects();
        List<String> ids = new ArrayList<String>(Collections.singletonList(id1));
        verifyRelation(objectDataList.get(0), ids, BaseTypeId.CMIS_RELATIONSHIP.value(), targetId,
                RelationshipDirection.TARGET);
        assertTrue(ids.isEmpty());

        objectList = fRelSvc.getObjectRelationships(REPOSITORY_ID, targetId, true, RelationshipDirection.TARGET,
                BaseTypeId.CMIS_RELATIONSHIP.value(), null, null, null, null, null);
        assertEquals(2, objectList.getNumItems().longValue());
        objectDataList = objectList.getObjects();
        ids = new ArrayList<String>(relIds);
        for (ObjectData objectData : objectDataList) {
            verifyRelation(objectData, ids, null, targetId, RelationshipDirection.TARGET);
        }
        assertTrue(ids.isEmpty());

        // Test the same with passing null for the type id
        objectList = fRelSvc.getObjectRelationships(REPOSITORY_ID, targetId, true, RelationshipDirection.TARGET, null,
                null, null, null, null, null);
        assertEquals(2, objectList.getNumItems().longValue());
        objectDataList = objectList.getObjects();
        ids = new ArrayList<String>(relIds);
        for (ObjectData objectData : objectDataList) {
            verifyRelation(objectData, ids, null, targetId, RelationshipDirection.TARGET);
        }
        assertTrue(ids.isEmpty());

    }

    @Test
    public void testAllowedTypes() {
        String docSub1 = createDocument("CustomDoc1", fRootFolderId, ObjectServiceTest.TEST_CUSTOM_DOCUMENT_TYPE_ID,
                false);
        String docSub2 = createDocument("CustomDoc2", fRootFolderId, ObjectServiceTest.TEST_CUSTOM_DOCUMENT_TYPE_ID,
                false);

        try {
            createRelationship(fRootFolderId, ObjectServiceTest.TEST_RESTRICTED_RELATION_TYPE_ID, docId1, targetId,
                    null);
            fail("Creating a relationship with a type cmis:document as source should fail, not an allowedSourceType.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
            log.info("Creating document with a folder as source and target failed as expected due to alledSourceType restriction.");
        }

        try {
            createRelationship(fRootFolderId, ObjectServiceTest.TEST_RESTRICTED_RELATION_TYPE_ID, docSub1, targetId,
                    null);
            fail("Creating a relationship with a type cmis:document as target should fail, not an allowedSourceType.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
            log.info("Creating document with a folder as source failed as expected due to alledSourceType restriction.");
        }

        try {
            createRelationship(fRootFolderId, ObjectServiceTest.TEST_RESTRICTED_RELATION_TYPE_ID, docId1, docSub1, null);
            fail("Creating a relationship with a type cmis:document as source should fail, not an allowedSourceType.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
            log.info("Creating document with a folder as source failed as expected due to alledSourceType restriction.");
        }
        try {
            String id = createRelationship(fRootFolderId, ObjectServiceTest.TEST_RESTRICTED_RELATION_TYPE_ID, docSub1,
                    docSub2, null);
            assertNotNull(id);
        } catch (Exception e) {
            fail("Creating a relationship with an allowed type as source and target should succeed.");
        }

    }

    @Test
    public void testGetObject() {

        final String id1 = createRelationship("CrossReference1", BaseTypeId.CMIS_RELATIONSHIP.value(), docId1,
                targetId, null);
        assertNotNull(id1);
        final String id2 = createRelationship("CrossReference2", BaseTypeId.CMIS_RELATIONSHIP.value(), docId2,
                targetId, null);
        assertNotNull(id1);
        List<String> relIds = new ArrayList<String>() {
            {
                add(id1);
                add(id2);
            }
        };

        // get document object, source
        ObjectData objectData = fObjSvc.getObject(fRepositoryId, docId1, null, false, IncludeRelationships.SOURCE,
                null, false, false, null);
        List<ObjectData> odRelationships = objectData.getRelationships();

        assertEquals(1, odRelationships.size());
        List<String> ids = new ArrayList<String>(Collections.singletonList(id1));
        for (ObjectData objectDataRel : odRelationships) {
            verifyRelation(objectDataRel, ids, null, docId1, RelationshipDirection.SOURCE);
        }
        assertTrue(ids.isEmpty());

        // get document object, target
        objectData = fObjSvc.getObject(fRepositoryId, targetId, null, false, IncludeRelationships.TARGET, null, false,
                false, null);
        odRelationships = objectData.getRelationships();

        assertEquals(2, odRelationships.size());
        ids = new ArrayList<String>(relIds);
        for (ObjectData objectDataRel : odRelationships) {
            verifyRelation(objectDataRel, ids, null, targetId, RelationshipDirection.TARGET);
        }
        assertTrue(ids.isEmpty());

        // get document object, BOTH
        objectData = fObjSvc.getObject(fRepositoryId, targetId, null, false, IncludeRelationships.BOTH, null, false,
                false, null);
        odRelationships = objectData.getRelationships();

        assertEquals(2, odRelationships.size());
        ids = new ArrayList<String>(relIds);
        for (ObjectData objectDataRel : odRelationships) {
            verifyRelation(objectDataRel, ids, null, targetId, RelationshipDirection.EITHER);
        }
        assertTrue(ids.isEmpty());

        // get document object, none
        objectData = fObjSvc.getObject(fRepositoryId, targetId, null, false, IncludeRelationships.NONE, null, false,
                false, null);
        assertTrue(objectData.getRelationships().isEmpty());

        // Folder
        String folderId = createFolder("folder1", fRootFolderId, BaseTypeId.CMIS_FOLDER.value());
        String id = createRelationship("FolderRelationship", ObjectServiceTest.TEST_RELATION_TYPE_ID, folderId,
                targetId, null);
        assertNotNull(id);
        objectData = fObjSvc.getObject(fRepositoryId, folderId, null, false, IncludeRelationships.SOURCE, null, false,
                false, null);
        odRelationships = objectData.getRelationships();

        assertEquals(1, odRelationships.size());
        ids = new ArrayList<String>(Collections.singletonList(id));
        for (ObjectData objectDataRel : odRelationships) {
            verifyRelation(objectDataRel, ids, null, folderId, RelationshipDirection.SOURCE);
        }
        assertTrue(ids.isEmpty());

        // Versioned document
        String verId = createVersionedDocument();
        id = createRelationship("VersionRelationship", ObjectServiceTest.TEST_RELATION_TYPE_ID, verId, targetId, null);
        assertNotNull(id);
        objectData = fObjSvc.getObject(fRepositoryId, verId, null, false, IncludeRelationships.SOURCE, null, false,
                false, null);
        odRelationships = objectData.getRelationships();

        assertEquals(1, odRelationships.size());
        ids = new ArrayList<String>(Collections.singletonList(id));
        for (ObjectData objectDataRel : odRelationships) {
            verifyRelation(objectDataRel, ids, null, verId, RelationshipDirection.SOURCE);
        }
        assertTrue(ids.isEmpty());

        // item
        String itemId = createItem();
        id = createRelationship("ItemRelationship", ObjectServiceTest.TEST_RELATION_TYPE_ID, itemId, targetId, null);
        assertNotNull(id);
        objectData = fObjSvc.getObject(fRepositoryId, itemId, null, false, IncludeRelationships.SOURCE, null, false,
                false, null);
        odRelationships = objectData.getRelationships();

        assertEquals(1, odRelationships.size());
        ids = new ArrayList<String>(Collections.singletonList(id));
        for (ObjectData objectDataRel : odRelationships) {
            verifyRelation(objectDataRel, ids, null, itemId, RelationshipDirection.SOURCE);
        }
        assertTrue(ids.isEmpty());
    }

    @Test
    public void testGetObjetByPath() {
        // getObjectByPath
        final String id1 = createRelationship("CrossReference1", BaseTypeId.CMIS_RELATIONSHIP.value(), docId1,
                targetId, null);
        assertNotNull(id1);
        ObjectData objectData = fObjSvc.getObjectByPath(fRepositoryId, "/" + MY_DOC_1, null, null,
                IncludeRelationships.SOURCE, null, false, false, null);
        List<ObjectData> odRelationships = objectData.getRelationships();

        assertEquals(1, odRelationships.size());
        List<String> ids = new ArrayList<String>(Collections.singletonList(id1));
        for (ObjectData objectDataRel : odRelationships) {
            verifyRelation(objectDataRel, ids, null, docId1, RelationshipDirection.SOURCE);
        }
        assertTrue(ids.isEmpty());
    }

    @Test
    public void testGetCheckedOut() {
        // getObjectByPath
        final String did = createCheckedOutDocument();
        assertNotNull(did);
        final String id1 = createRelationship("CrossReference1", ObjectServiceTest.TEST_RELATION_TYPE_ID, did,
                targetId, null);

        ObjectList objectList = fNavSvc.getCheckedOutDocs(fRepositoryId, fRootFolderId, null, null, false,
                IncludeRelationships.BOTH, null, null, null, null);
        assertEquals(1, objectList.getNumItems().intValue());
        List<ObjectData> odRelationships = objectList.getObjects().get(0).getRelationships();
        assertEquals(1, odRelationships.size());
        List<String> ids = new ArrayList<String>(Collections.singletonList(id1));
        for (ObjectData objectDataRel : odRelationships) {
            verifyRelation(objectDataRel, ids, null, did, RelationshipDirection.EITHER);
        }
        assertTrue(ids.isEmpty());
    }

    @Test
    public void testGetDescendants() {
        boolean found1 = false;
        boolean found2 = false;
        boolean found3 = false;
        boolean found4 = false;
        boolean found5 = false;

        createHierarchy();

        final String id1 = createRelationship("CrossReference1", ObjectServiceTest.TEST_RELATION_TYPE_ID, docId1,
                targetId, null);
        assertNotNull(id1);
        final String id2 = createRelationship("CrossReference2", ObjectServiceTest.TEST_RELATION_TYPE_ID, docId2,
                targetId, null);
        assertNotNull(id2);
        final String id3 = createRelationship("CrossReference3", ObjectServiceTest.TEST_RELATION_TYPE_ID, docId3,
                targetId, null);
        assertNotNull(id3);
        final String id4 = createRelationship("CrossReference4", ObjectServiceTest.TEST_RELATION_TYPE_ID, docId4,
                targetId, null);
        assertNotNull(id4);

        List<ObjectInFolderContainer> ofcs = fNavSvc.getDescendants(fRepositoryId, fRootFolderId,
                BigInteger.valueOf(-1), null, false, IncludeRelationships.BOTH, null, false, null);

        Map<String, List<ObjectData>> relMap = new HashMap<String, List<ObjectData>>();
        getRelationshipsOfDescendants(ofcs, relMap);
        assertEquals(5, relMap.size());

        for (Entry<String, List<ObjectData>> relEntry : relMap.entrySet()) {
            List<ObjectData> rels = relEntry.getValue();
            String srcId = relEntry.getKey();
            if (srcId.equals(docId1)) {
                assertEquals(1, rels.size());
                assertEquals(id1, rels.get(0).getId());
                found1 = true;
            } else if (srcId.equals(docId2)) {
                assertEquals(1, rels.size());
                assertEquals(id2, rels.get(0).getId());
                found2 = true;
            } else if (srcId.equals(docId3)) {
                assertEquals(1, rels.size());
                assertEquals(id3, rels.get(0).getId());
                found3 = true;
            } else if (srcId.equals(docId4)) {
                assertEquals(1, rels.size());
                assertEquals(id4, rels.get(0).getId());
                found4 = true;
            } else if (srcId.equals(targetId)) {
                assertEquals(4, rels.size());
                found5 = true;
            }
        }
        assertTrue(found1 && found2 && found3 && found4 && found5);
    }

    @Test
    public void testGetChildren() {
        final String id1 = createRelationship("CrossReference1", BaseTypeId.CMIS_RELATIONSHIP.value(), docId1,
                targetId, null);
        assertNotNull(id1);
        final String id2 = createRelationship("CrossReference1", BaseTypeId.CMIS_RELATIONSHIP.value(), docId2,
                targetId, null);
        assertNotNull(id2);

        ObjectInFolderList ods = fNavSvc.getChildren(fRepositoryId, fRootFolderId, null, null, false,
                IncludeRelationships.SOURCE, null, false, null, null, null);
        assertEquals(3, ods.getNumItems().intValue());
        for (ObjectInFolderData of : ods.getObjects()) {
            String id = of.getObject().getId();
            List<ObjectData> rels = of.getObject().getRelationships();
            if (id.equals(docId1)) {
                assertEquals(1, rels.size());
                ArrayList<String> ids = new ArrayList<String>(Collections.singletonList(id1));
                verifyRelation(rels.get(0), ids, null, docId1, RelationshipDirection.SOURCE);
                assertTrue(ids.isEmpty());
            } else if (id.equals(docId2)) {
                assertEquals(1, rels.size());
                ArrayList<String> ids = new ArrayList<String>(Collections.singletonList(id2));
                verifyRelation(rels.get(0), ids, null, docId2, RelationshipDirection.SOURCE);
                assertTrue(ids.isEmpty());
            } else if (id.equals(targetId)) {
                assertEquals(0, rels.size());
            } else {
                fail("Unexpected object in getChildren");
            }
        }
    }

    @Test
    public void testGetParents() {
        final String id1 = createRelationship("CrossReference1", ObjectServiceTest.TEST_RELATION_TYPE_ID, docId1,
                fRootFolderId, null);
        assertNotNull(id1);
        final String id2 = createRelationship("CrossReference2", ObjectServiceTest.TEST_RELATION_TYPE_ID, docId2,
                fRootFolderId, null);

        List<ObjectParentData> ods = fNavSvc.getObjectParents(fRepositoryId, docId1, null, false,
                IncludeRelationships.TARGET, null, false, null);
        assertEquals(1, ods.size());
        List<ObjectData> rels = ods.get(0).getObject().getRelationships();
        assertEquals(2, rels.size());
        List<String> relIds = new ArrayList<String>() {
            {
                add(id1);
                add(id2);
            }
        };
        for (ObjectData rel : rels) {
            verifyRelation(rel, relIds, null, fRootFolderId, RelationshipDirection.TARGET);
        }
        assertTrue(relIds.isEmpty());
    }

    @Test
    public void testQueryIncludeRelationships() {
        final String id1 = createRelationship("CrossReference1", ObjectServiceTest.TEST_RELATION_TYPE_ID, docId1,
                targetId, null);
        assertNotNull(id1);
        final String id2 = createRelationship("CrossReference2", ObjectServiceTest.TEST_RELATION_TYPE_ID, docId2,
                targetId, null);

        String statement = "SELECT * from cmis:document WHERE cmis:name = '" + MY_DOC_1 + "'";
        ObjectList res = fDiscSvc.query(fRepositoryId, statement, false, false, IncludeRelationships.SOURCE, null,
                null, null, null);
        assertEquals(1, res.getNumItems().intValue());

        List<ObjectData> rels = res.getObjects().get(0).getRelationships();
        assertEquals(1, rels.size());
        ArrayList<String> ids = new ArrayList<String>(Collections.singletonList(id1));
        verifyRelation(rels.get(0), ids, null, docId1, RelationshipDirection.SOURCE);
        assertTrue(ids.isEmpty());
    }

    @Test
    public void testQueryRelationships() {
        final String id1 = createRelationship("CrossReference1", ObjectServiceTest.TEST_RELATION_TYPE_ID, docId1,
                targetId, null);
        assertNotNull(id1);
        final String id2 = createRelationship("CrossReference2", ObjectServiceTest.TEST_RELATION_TYPE_ID, docId2,
                targetId, null);

        String statement = "SELECT * from cmis:relationship WHERE " + PropertyIds.TARGET_ID + " = '" + targetId + "'";
        ObjectList res = fDiscSvc.query(fRepositoryId, statement, false, false, IncludeRelationships.NONE, null, null,
                null, null);
        assertEquals(2, res.getNumItems().intValue());
        ArrayList<String> ids = new ArrayList<String>() {
            {
                add(id1);
                add(id2);
            }
        };
        for (ObjectData od : res.getObjects()) {
            ids.remove(od.getId());
        }
        assertTrue(ids.isEmpty());
    }

    private void verifyRelation(ObjectData objectData, List<String> expectedRelIds, String expectedTypeId,
            String refId, RelationshipDirection direction) {
        String relId = (String) objectData.getProperties().getProperties().get(PropertyIds.OBJECT_ID).getFirstValue();
        assertTrue(expectedRelIds.remove(relId));
        assertEquals(BaseTypeId.CMIS_RELATIONSHIP, objectData.getBaseTypeId());
        if (null != expectedTypeId) {
            assertEquals(expectedTypeId, objectData.getProperties().getProperties().get(PropertyIds.OBJECT_TYPE_ID)
                    .getFirstValue());
        }
        if (expectedTypeId != null && expectedTypeId.equals(ObjectServiceTest.TEST_RELATION_TYPE_ID)) {
            assertEquals(REL_CUSTOM_PROP_VALUE,
                    objectData.getProperties().getProperties().get(ObjectServiceTest.REL_STRING_PROP).getFirstValue());
        }
        String sourceId = (String) objectData.getProperties().getProperties().get(PropertyIds.SOURCE_ID)
                .getFirstValue();
        String targetId = (String) objectData.getProperties().getProperties().get(PropertyIds.TARGET_ID)
                .getFirstValue();
        if (RelationshipDirection.SOURCE == direction) {
            assertEquals(refId, sourceId);
        } else if (RelationshipDirection.TARGET == direction) {
            assertEquals(refId, targetId);
        } else {
            assertTrue(refId.equals(sourceId) || refId.equals(targetId));
        }
    }

    private String createRelationship(String name, String typeId, String sourceId, String targetId,
            List<PropertyData<?>> additionalProperties) {
        Properties props = createRelationshipProperties(name, typeId, sourceId, targetId, additionalProperties);

        String id = fObjSvc.createRelationship(fRepositoryId, props, null, null, null, null);
        return id;
    }

    private List<PropertyData<?>> createRelationshipProperties(String crossReferenceType) {
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyStringData(ObjectServiceTest.REL_STRING_PROP, crossReferenceType));
        return properties;
    }

    // create with a name
    private Properties createRelationshipProperties(String name, String typeId, String sourceId, String targetId,
            List<PropertyData<?>> additionalProperties) {

        List<PropertyData<?>> properties = additionalProperties;
        if (null == additionalProperties) {
            properties = new ArrayList<PropertyData<?>>();
        }

        properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, name));
        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, typeId));
        properties.add(fFactory.createPropertyIdData(PropertyIds.SOURCE_ID, sourceId));
        properties.add(fFactory.createPropertyIdData(PropertyIds.TARGET_ID, targetId));
        Properties props = fFactory.createPropertiesData(properties);
        return props;
    }

    private String createVersionedDocument() {
        String id = null;
        id = fCreator.createDocument("VersionedDocument1", ObjectServiceTest.TEST_VERSION_DOCUMENT_TYPE_ID,
                fRootFolderId, VersioningState.MAJOR, null);
        return id;
    }

    private String createCheckedOutDocument() {
        String id = createVersionedDocument();
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        Holder<String> checkedOutId = new Holder<String>(id);
        fVerSvc.checkOut(fRepositoryId, checkedOutId, null, contentCopied);
        return checkedOutId.getValue();
    }

    private String createItem() {
        String id;
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, "Item1"));
        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, ObjectServiceTest.TEST_ITEM_TYPE_ID));
        Properties props = fFactory.createPropertiesData(properties);
        id = fObjSvc.createItem(fRepositoryId, props, fRootFolderId, null, null, null, null);
        return id;
    }

    private void createHierarchy() {
        String folderId1 = createFolder("MyFolder", fRootFolderId, BaseTypeId.CMIS_FOLDER.value());
        assertNotNull(folderId1);
        String folderId2 = createFolder("MySubFolder", folderId1, BaseTypeId.CMIS_FOLDER.value());
        assertNotNull(folderId2);
        docId3 = createDocument(MY_DOC_3, folderId1, BaseTypeId.CMIS_DOCUMENT.value(), true);
        assertNotNull(docId2);
        docId4 = createDocument(MY_DOC_4, folderId2, BaseTypeId.CMIS_DOCUMENT.value(), true);
        assertNotNull(docId3);
    }

    private void getRelationshipsOfDescendants(List<ObjectInFolderContainer> ofcs, Map<String, List<ObjectData>> rels) {
        for (ObjectInFolderContainer ofc : ofcs) {
            log.debug("found desc: " + ofc.getObject().getObject().getProperties().getProperties().get("cmis:name"));
            log.debug("   base: " + ofc.getObject().getObject().getBaseTypeId());
            if (ofc.getObject().getObject().getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
                rels.put(ofc.getObject().getObject().getId(), ofc.getObject().getObject().getRelationships());
            } else {
                getRelationshipsOfDescendants(ofc.getChildren(), rels);
                // for ( ObjectInFolderContainer child : ofc.getChildren()) {
                // }
            }
        }
    }
}
