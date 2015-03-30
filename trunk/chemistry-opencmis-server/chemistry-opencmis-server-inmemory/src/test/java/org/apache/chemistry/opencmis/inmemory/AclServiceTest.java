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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumBasicPermissions;
import org.apache.chemistry.opencmis.inmemory.ObjectServiceTest.ObjectTestTypeSystemCreator;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.InMemoryAce;
import org.apache.chemistry.opencmis.inmemory.types.DocumentTypeCreationHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AclServiceTest extends AbstractServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(AclServiceTest.class);
    private ObjectCreator fCreator;
    private static final String DOCUMENT_NAME = "DocumentWithAcl";
    private static final String FOLDER_NAME = "FolderWithAcl";
    private static final String DOCUMENT_TYPE_ID = DocumentTypeCreationHelper.getCmisDocumentType().getId();
    private static final String FOLDER_TYPE_ID = DocumentTypeCreationHelper.getCmisFolderType().getId();
    private static final String USER = "user";
    private static final String ALICE = "alice";
    private static final String BOB = "bob";
    private static final String CHRIS = "chris";
    private static final String DAN = "dan";
    private Acl defaultAcl = null;

    @Override
    @Before
    public void setUp() {
        super.setTypeCreatorClass(ObjectTestTypeSystemCreator.class.getName());
        super.setUp();
        fCreator = new ObjectCreator(fFactory, fObjSvc, fRepositoryId);

        List<Ace> defaultACEs = new ArrayList<Ace>(1);
        defaultACEs.add(fFactory.createAccessControlEntry(InMemoryAce.getAnyoneUser(),
                Collections.singletonList(EnumBasicPermissions.CMIS_ALL.value())));
        defaultAcl = fFactory.createAccessControlList(defaultACEs);

    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testCreateDocumentWithAcl() {
        LOG.info("starting testCreateDocumentWithAcl() ...");
        Acl removeAces = defaultAcl;
        Acl acl = createSimpleTestAcl();

        String id = createDocument(fRootFolderId, acl, removeAces);
        LOG.debug("created document with id: " + id);

        // get ACL using AclService
        Acl aclReturn = fAclSvc.getAcl(fRepositoryId, id, true, null);
        checkSimpleTestAcl(acl, aclReturn);

        // get ACL using ObjectService getObject
        ObjectData objData = fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE, null, false,
                true, null);
        checkSimpleTestAcl(acl, aclReturn);

        // get ACL using ObjectService getObjectByPath
        objData = fObjSvc.getObjectByPath(fRepositoryId, "/" + DOCUMENT_NAME, "*", false, IncludeRelationships.NONE,
                null, false, true, null);
        assertNotNull(objData);
        aclReturn = objData.getAcl();
        checkSimpleTestAcl(acl, aclReturn);

        LOG.info("... testCreateDocumentWithAcl() finished.");
    }

    @Test
    public void testCreateFolderWithAcl() {
        LOG.info("starting testCreateFolderWithAcl() ...");
        Acl removeAces = defaultAcl;
        Acl acl = createSimpleTestAcl();

        String id = createFolder(fRootFolderId, acl, removeAces);
        LOG.debug("created folder with id: " + id);

        // get ACL using AclService
        Acl aclReturn = fAclSvc.getAcl(fRepositoryId, id, true, null);
        checkSimpleTestAcl(acl, aclReturn);

        // get ACL using ObjectService getObject
        ObjectData objData = fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE, null, false,
                true, null);
        checkSimpleTestAcl(acl, aclReturn);

        // get ACL using ObjectService getObjectByPath
        objData = fObjSvc.getObjectByPath(fRepositoryId, "/" + FOLDER_NAME, "*", false, IncludeRelationships.NONE,
                null, false, true, null);
        assertNotNull(objData);
        aclReturn = objData.getAcl();
        checkSimpleTestAcl(acl, aclReturn);
        LOG.info("... testCreateFolderWithAcl() finished.");
    }

    @Test
    public void testApplyAcl() {
        LOG.info("starting testApplyAcl() ...");
        Acl acl = createSimpleTestAcl();

        String id = createDocument(fRootFolderId, null, null);
        LOG.debug("created document with id: " + id);

        // apply absolute ACL using AclService
        Acl acl1 = fAclSvc.applyAcl(fRepositoryId, id, acl, defaultAcl, AclPropagation.OBJECTONLY, null);
        checkSimpleTestAcl(acl, acl1);

        // get ACL using AclService
        Acl aclReturn = fAclSvc.getAcl(fRepositoryId, id, true, null);
        checkSimpleTestAcl(acl, aclReturn);

        LOG.info("... testApplyAcl() finished.");
    }

    @Test
    public void testAddRemoveAcl() {

        LOG.info("starting testAddRemoveAcl() ...");
        Acl acl = createAdvancedTestAcl();

        String id = createDocument(fRootFolderId, acl, defaultAcl);
        LOG.debug("created document with id: " + id);

        Acl aclAdd = createAclAdd();
        Acl aclRemove = createAclRemove();
        // apply absolute ACL using AclService
        Acl aclReturn = fAclSvc.applyAcl(fRepositoryId, id, aclAdd, aclRemove, AclPropagation.OBJECTONLY, null);

        checkAclAfterAddRemove(aclReturn);

        LOG.info("... testAddRemoveAcl() finished.");
    }

    @Test
    public void testAddRemoveDuplicatedAcl() {
        final String DOCUMENT_NAME_1 = "DocumentWithAcl-1";
        final String DOCUMENT_NAME_2 = "DocumentWithAcl-2";

        LOG.info("starting testAddRemoveDuplicatedAcl() ...");
        Acl acl = createAdvancedTestAcl();
        String id1 = createDocument(DOCUMENT_NAME_1, fRootFolderId, acl, defaultAcl);
        String id2 = createDocument(DOCUMENT_NAME_2, fRootFolderId, acl, defaultAcl);

        // // modify ACL of first doc
        // List<Ace> acesRemove = Arrays.asList(new Ace[] {
        // createAce(BOB, EnumBasicPermissions.CMIS_WRITE.value()),
        // });
        // Acl aclRemove = new AccessControlListImpl(acesRemove);
        // List<Ace> acesAdd = Arrays.asList(new Ace[] {
        // createAce(DAN, EnumBasicPermissions.CMIS_WRITE.value()),
        // });
        // Acl aclAdd = new AccessControlListImpl(acesAdd);
        Acl aclAdd = createAclAdd();
        Acl aclRemove = createAclRemove();
        Acl aclReturn = fAclSvc.applyAcl(fRepositoryId, id1, aclAdd, aclRemove, AclPropagation.OBJECTONLY, null);

        checkAclAfterAddRemove(aclReturn);

        // Ensure that ACL of second doc is unchanged
        aclReturn = fAclSvc.getAcl(fRepositoryId, id2, true, null);
        checkAdvancedTestAcl(acl, aclReturn);
        LOG.info("... testAddRemoveDuplicatedAcl() finished.");
    }

    @Test
    public void testApplyAclRecursiveSimple() {
        LOG.info("starting testApplyAclRecursiveSimple() ...");
        Acl acl = createSimpleTestAcl();
        String[] ids = createHierarchy(acl, defaultAcl);
        fAclSvc.applyAcl(fRepositoryId, ids[0], acl, null, AclPropagation.PROPAGATE, null);
        checkAclRecursiveSimple(ids, acl);
        LOG.info("... testApplyAclRecursiveSimple() finished.");
    }

    @Test
    public void testApplyAclRecursiveIncremental() {
        LOG.info("starting testApplyAclRecursiveIncremental() ...");
        Acl acl = createAdvancedTestAcl();
        String[] ids = createHierarchy(acl, defaultAcl);

        Acl aclRemove = createAclRemove();
        Acl aclAdd = createAclAdd();

        Acl aclReturn = fAclSvc.applyAcl(fRepositoryId, ids[0], aclAdd, aclRemove, AclPropagation.PROPAGATE, null);
        checkAclAfterAddRemove(aclReturn);
        for (String id : ids) {
            aclReturn = fAclSvc.getAcl(fRepositoryId, id, true, null);
            checkAclAfterAddRemove(aclReturn);
        }
        LOG.info("... testApplyAclRecursiveIncremental() finished.");
    }

    @Test
    public void testRemoveAllAcls() {
        LOG.info("starting testRemoveAllAcls() ...");

        Acl acl = createAdvancedTestAcl();
        String id = createDocument(fRootFolderId, acl, defaultAcl);
        LOG.debug("created document with id: " + id);

        Acl aclReturn = fAclSvc.applyAcl(fRepositoryId, id, null, acl, AclPropagation.OBJECTONLY, null);
        assertNotNull(aclReturn);
        assertEquals(1, aclReturn.getAces().size());
        assertTrue(aclHasPermission(aclReturn, "anyone", EnumBasicPermissions.CMIS_ALL.value()));

        LOG.info("... testRemoveAllAcls() finished.");
    }

    private String createDocument(String name, String folderId, Acl addAces, Acl removeAces) {
        return createDocumentNoCatch(name, folderId, DOCUMENT_TYPE_ID, VersioningState.NONE, false, addAces, removeAces);
    }

    private String createDocument(String folderId, Acl addAces, Acl removeAces) {
        return createDocumentNoCatch(DOCUMENT_NAME, folderId, DOCUMENT_TYPE_ID, VersioningState.NONE, false, addAces,
                removeAces);
    }

    private String createFolder(String folderId, Acl addAces, Acl removeAces) {
        return createFolderNoCatch(FOLDER_NAME, folderId, FOLDER_TYPE_ID, addAces, removeAces);
    }

    private String[] createHierarchy(Acl addAces, Acl removeAces) {
        String result[] = new String[6];
        String rootFolderId = createFolderNoCatch(FOLDER_NAME, fRootFolderId, FOLDER_TYPE_ID, addAces, removeAces);
        result[0] = rootFolderId;
        result[1] = createDocument(DOCUMENT_NAME + "-1", rootFolderId, addAces, removeAces);
        result[2] = createDocument(DOCUMENT_NAME + "-2", rootFolderId, addAces, removeAces);
        String subFolderId = createFolderNoCatch(FOLDER_NAME, rootFolderId, FOLDER_TYPE_ID, addAces, removeAces);
        result[3] = subFolderId;
        result[4] = createDocument(DOCUMENT_NAME + "-1", subFolderId, addAces, removeAces);
        result[5] = createDocument(DOCUMENT_NAME + "-2", subFolderId, addAces, removeAces);
        return result;
    }

    private void checkAclRecursiveSimple(String[] ids, Acl acl) {
        // get ACL using ObjectService getObject
        for (String id : ids) {
            ObjectData objData = fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE, null,
                    false, true, null);
            checkSimpleTestAcl(acl, objData.getAcl());
        }
    }

    private Acl createSimpleTestAcl() {
        List<Ace> aces = Arrays.asList(new Ace[] { createAce(USER, EnumBasicPermissions.CMIS_READ.value()) });
        return fFactory.createAccessControlList(aces);
    }

    private void checkSimpleTestAcl(Acl acl, Acl aclReturn) {
        assertNotNull(aclReturn);
        assertEquals(acl.getAces().size(), aclReturn.getAces().size());
        assertTrue(aclHasPermission(aclReturn, USER, EnumBasicPermissions.CMIS_READ.value()));
    }

    private Acl createAdvancedTestAcl() {
        List<Ace> aces = Arrays.asList(new Ace[] { createAce(ALICE, EnumBasicPermissions.CMIS_READ.value()),
                createAce(BOB, EnumBasicPermissions.CMIS_WRITE.value()),
                createAce(CHRIS, EnumBasicPermissions.CMIS_ALL.value()), });
        return fFactory.createAccessControlList(aces);
    }

    private Acl createAclAdd() {
        List<Ace> acesAdd = Arrays.asList(new Ace[] { createAce(DAN, EnumBasicPermissions.CMIS_WRITE.value()), });
        return fFactory.createAccessControlList(acesAdd);
    }

    private Acl createAclRemove() {
        List<Ace> acesRemove = Arrays.asList(new Ace[] { createAce(BOB, EnumBasicPermissions.CMIS_WRITE.value()),
                createAce(CHRIS, EnumBasicPermissions.CMIS_ALL.value()) });
        return fFactory.createAccessControlList(acesRemove);
    }

    private void checkAclAfterAddRemove(Acl aclReturn) {
        assertNotNull(aclReturn);
        assertEquals(2, aclReturn.getAces().size());
        assertTrue(aclHasPermission(aclReturn, ALICE, EnumBasicPermissions.CMIS_READ.value()));
        assertTrue(aclHasPermission(aclReturn, DAN, EnumBasicPermissions.CMIS_WRITE.value()));
        assertFalse(aclHasPermission(aclReturn, BOB, EnumBasicPermissions.CMIS_WRITE.value()));
        assertFalse(aclHasPermission(aclReturn, CHRIS, EnumBasicPermissions.CMIS_ALL.value()));
        assertTrue(aclHasNoPermission(aclReturn, BOB));
        assertTrue(aclHasNoPermission(aclReturn, CHRIS));
    }

    private void checkAdvancedTestAcl(Acl acl, Acl aclReturn) {
        assertNotNull(aclReturn);
        assertEquals(acl.getAces().size(), aclReturn.getAces().size());
        assertTrue(aclHasPermission(aclReturn, ALICE, EnumBasicPermissions.CMIS_READ.value()));
        assertTrue(aclHasPermission(aclReturn, BOB, EnumBasicPermissions.CMIS_WRITE.value()));
        assertTrue(aclHasPermission(aclReturn, CHRIS, EnumBasicPermissions.CMIS_ALL.value()));
    }

    private Ace createAce(String principalId, String permission) {
        return fFactory.createAccessControlEntry(principalId, Collections.singletonList(permission));
    }

    private boolean aclHasPermission(Acl acl, String principalId, String permission) {
        for (Ace ace : acl.getAces()) {
            if (ace.getPrincipalId().equals(principalId) && aceContainsPermission(ace, permission)) {
                return true;
            }
        }
        return false;
    }

    private boolean aclHasNoPermission(Acl acl, String principalId) {
        if (null == acl) {
            return false;
        }

        for (Ace ace : acl.getAces()) {
            if (ace.getPrincipalId().equals(principalId)) {
                return false;
            }
        }
        return true;
    }

    private boolean aceContainsPermission(Ace ace, String permission) {
        for (String acePerm : ace.getPermissions()) {
            if (permission.equals(acePerm)) {
                return true;
            }
        }
        return false;
    }
}
