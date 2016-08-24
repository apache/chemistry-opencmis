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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableDocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableFolderTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableItemTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutablePolicyTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableRelationshipTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableSecondaryTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BulkUpdateObjectIdAndChangeTokenImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.inmemory.content.ObjectGenerator;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.ContentStreamDataImpl;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.RenditionUtil;
import org.apache.chemistry.opencmis.inmemory.types.DocumentTypeCreationHelper;
import org.apache.chemistry.opencmis.inmemory.types.PropertyCreationHelper;
import org.apache.chemistry.opencmis.server.support.TypeDefinitionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectServiceTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ObjectServiceTest.class);
    public static final String TEST_FOLDER_TYPE_ID = "MyFolderType";
    public static final String TEST_DOCUMENT_TYPE_ID = "MyDocumentType";
    public static final String TEST_DOC_TYPE_WITH_DEFAULTS_ID = "DocumentTypeWithDefault";
    public static final String TEST_FOLDER_TYPE_WITH_DEFAULTS_ID = "FolderTypeWithDefault";
    public static final String TEST_FOLDER_STRING_PROP_ID = "MyFolderStringProp";
    public static final String TEST_DOCUMENT_STRING_PROP_ID = "MyDocumentStringProp";
    public static final String TEST_VERSION_DOCUMENT_TYPE_ID = "MyVersionedType";
    public static final String TEST_VER_PROPERTY_ID = "VerStringProp";
    public static final String TEST_CUSTOM_DOCUMENT_TYPE_ID = "MyCustomDocumentType";
    public static final String TEST_INHERITED_CUSTOM_DOCUMENT_TYPE_ID = "MyCustomInheritedDocType";
    public static final String TEST_CUSTOM_NO_CONTENT_TYPE_ID = "NoContentType";
    public static final String TEST_CUSTOM_MUST_CONTENT_TYPE_ID = "MustHaveContentType";
    public static final String TEST_DOCUMENT_MY_STRING_PROP_ID = "MyCustomDocumentStringProp";
    public static final String TEST_DOCUMENT_MY_MULTI_STRING_PROP_ID = "MyCustomDocumentMultiStringProp";
    public static final String TEST_DOCUMENT_MY_INT_PROP_ID = "MyCustomDocumentIntProp";
    public static final String TEST_DOCUMENT_MY_INT_PROP_ID_MANDATORY_DEFAULT = "MyCustomDocumentIntPropMandatoryDefault";
    public static final String TEST_FOLDER_MY_MULTI_STRING_PROP_ID = "MyCustomDocumentMultiStringProp";
    public static final String TEST_FOLDER_MY_INT_PROP_ID = "MyCustomDocumentIntProp";
    public static final String TEST_FOLDER_MY_INT_PROP_ID_MANDATORY_DEFAULT = "MyCustomDocumentIntPropMandatoryDefault";
    public static final String TEST_DOCUMENT_MY_SUB_STRING_PROP_ID = "MyInheritedStringProp";
    public static final String TEST_DOCUMENT_MY_SUB_INT_PROP_ID = "MyInheritedIntProp";
    public static final String TEST_ITEM_TYPE_ID = "MyItemType";
    public static final String ITEM_STRING_PROP = "ItemStringProp";
    private static final String DOCUMENT_TYPE_ID = DocumentTypeCreationHelper.getCmisDocumentType().getId();
    private static final String DOCUMENT_ID = "Document_1";
    private static final String FOLDER_TYPE_ID = DocumentTypeCreationHelper.getCmisFolderType().getId();
    private static final String FOLDER_ID = "Folder_1";
    private static final String MY_CUSTOM_NAME = "My Custom Document";
    private static final int MAX_SIZE = 100;
    private static final String PNG = "image/png";
    private static final String JPEG = "image/jpeg";
    private static final String NEW_STRING_PROP_VAL = "My ugly string 1";
    private static final BigInteger NEW_INT_PROP_VAL = BigInteger.valueOf(815);
    public static final String TEST_SECONDARY_TYPE_ID = "MySecondaryType";
    public static final String SECONDARY_STRING_PROP = "SecondaryStringProp";
    public static final String SECONDARY_INTEGER_PROP = "SecondaryIntegerProp";
    public static final String REL_STRING_PROP = "CrossReferenceKind";
    public static final String TEST_RELATION_TYPE_ID = "CrossReferenceType";
    public static final String TEST_RESTRICTED_RELATION_TYPE_ID = "RestrictedRelationType";
    public static final String TEST_POLICY_TYPE_ID = "AuditPolicy";
    public static final String TEST_POLICY_PROPERTY_ID = "AuditSettings";
    private static final String ENCODING_UTF8 = "UTF-8";

    ObjectCreator fCreator;

    @Override
    @Before
    public void setUp() {
        super.setTypeCreatorClass(ObjectTestTypeSystemCreator.class.getName());
        super.setUp();
        fCreator = new ObjectCreator(fFactory, fObjSvc, fRepositoryId);
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    @Override
    protected void addParameters(Map<String, String> parameters) {
        parameters.put(ConfigConstants.MAX_CONTENT_SIZE_KB, Integer.valueOf(MAX_SIZE).toString());
    }

    @Test
    public void testCreateDocument() {
        log.info("starting testCreateObject() ...");
        String id = createDocument(fRootFolderId, false);
        if (id != null) {
            log.info("createDocument succeeded with created id: " + id);
        }
        log.info("... testCreateObject() finished.");

        // test create a document with a folder type, should fail:
        try {
            Properties props = createDocumentProperties("DocumentWithAFolderType", FOLDER_TYPE_ID);
            id = fObjSvc.createDocument(fRepositoryId, props, fRootFolderId, null, VersioningState.NONE, null, null,
                    null, null);
            assertNotNull(id);
            fail("Creating  document with a folder type should fail.");
        } catch (Exception e) {
            log.info("Creating  document with a folder type failed as expected.");
        }
        // test create a document with an illegal name, should fail:
        try {
            Properties props = createDocumentProperties("abc (:*)", DOCUMENT_TYPE_ID);
            fObjSvc.createDocument(fRepositoryId, props, fRootFolderId, null, VersioningState.NONE, null, null, null,
                    null);
            fail("Creating  document with an illegal name should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisInvalidArgumentException);
            log.info("Creating  document with an illegal name failed as expected.");
        }
    }

    @Test
    public void testCreateDocumentInvalidNames() {
        try {
            createDocumentNoCatch(null, fRootFolderId, DOCUMENT_TYPE_ID, VersioningState.NONE, false);
            fail("Document creation with null name should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisInvalidArgumentException);
        }

        try {
            createDocumentNoCatch("", fRootFolderId, DOCUMENT_TYPE_ID, VersioningState.NONE, false);
            fail("Document creation with empty name should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisInvalidArgumentException);
        }

        try {
            createDocumentNoCatch("/(%#$a������������", fRootFolderId, DOCUMENT_TYPE_ID, VersioningState.NONE, false);
            fail("Document creation with ilegal name should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisInvalidArgumentException);
        }

        try {
            createDocumentNoCatch("DuplicatedName", fRootFolderId, DOCUMENT_TYPE_ID, VersioningState.NONE, false);
            createDocumentNoCatch("DuplicatedName", fRootFolderId, DOCUMENT_TYPE_ID, VersioningState.NONE, false);
            fail("Document creation with existing name should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisNameConstraintViolationException);
        }
    }

    @Test
    public void testCreateFolderInvalidNames() {
        try {
            createFolderNoCatch(null, fRootFolderId, FOLDER_TYPE_ID);
            fail("Folder creation with null name should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisInvalidArgumentException);
        }

        try {
            createFolderNoCatch("", fRootFolderId, FOLDER_TYPE_ID);
            fail("Folder creation with empty name should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisInvalidArgumentException);
        }

        try {
            createFolderNoCatch(
                    "/(%#$���������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������",
                    fRootFolderId, FOLDER_TYPE_ID);
            fail("Folder creation with ilegal name should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisInvalidArgumentException);
        }

        try {
            createFolderNoCatch("DuplicatedName", fRootFolderId, FOLDER_TYPE_ID);
            createFolderNoCatch("DuplicatedName", fRootFolderId, FOLDER_TYPE_ID);
            fail("Folder creation with existing name should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisNameConstraintViolationException || e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testGetObject() {
        log.info("starting testGetObject() ...");
        log.info("  creating object");
        String id = createDocument(fRootFolderId, false);
        if (id != null) {
            log.info("  createDocument succeeded with created id: " + id);
        }

        log.info("  getting object");
        retrieveDocument(id);
        log.info("... testGetObject() finished.");
    }

    @Test
    public void testGetObjectByPath() {
        log.info("starting testGetObjectByPath() ...");
        log.info("  creating object");

        // create a tree for testing paths
        String f1 = createFolder("folder1", fRootFolderId, FOLDER_TYPE_ID);
        String f2 = createFolder("folder2", fRootFolderId, FOLDER_TYPE_ID);
        String f3 = createFolder("folder3", fRootFolderId, FOLDER_TYPE_ID);
        String f11 = createFolder("folder1.1", f1, FOLDER_TYPE_ID);
        String f12 = createFolder("folder1.2", f1, FOLDER_TYPE_ID);
        String f13 = createFolder("folder1.3", f1, FOLDER_TYPE_ID);
        String f31 = createFolder("folder3.1", f3, FOLDER_TYPE_ID);
        String f32 = createFolder("folder3.2", f3, FOLDER_TYPE_ID);
        String f33 = createFolder("folder3.3", f3, FOLDER_TYPE_ID);
        String f121 = createFolder("folder1.2.1", f12, FOLDER_TYPE_ID);
        String f122 = createFolder("folder1.2.2", f12, FOLDER_TYPE_ID);
        String f123 = createFolder("folder1.2.3", f12, FOLDER_TYPE_ID);
        String f331 = createFolder("folder3.3.1", f33, FOLDER_TYPE_ID);
        String f332 = createFolder("folder3.3.2", f33, FOLDER_TYPE_ID);
        String f333 = createFolder("folder3.3.3", f33, FOLDER_TYPE_ID);
        String doc12 = createDocument("Document1.2.Doc", f12, false);
        String doc33 = createDocument("Document3.3.Doc", f33, false);
        String doc331 = createDocument("Document3.3.1.Doc", f331, false);
        String doc333 = createDocument("Document3.3.3.Doc", f333, false);

        log.info("  getting object by path");
        getByPath(f1, "/folder1");
        getByPath(f2, "/folder2");
        getByPath(f3, "/folder3");
        getByPath(f11, "/folder1/folder1.1");
        getByPath(f12, "/folder1/folder1.2");
        getByPath(f13, "/folder1/folder1.3");
        getByPath(f31, "/folder3/folder3.1");
        getByPath(f32, "/folder3/folder3.2");
        getByPath(f33, "/folder3/folder3.3");
        getByPath(f121, "/folder1/folder1.2/folder1.2.1");
        getByPath(f122, "/folder1/folder1.2/folder1.2.2");
        getByPath(f123, "/folder1/folder1.2/folder1.2.3");
        getByPath(f331, "/folder3/folder3.3/folder3.3.1");
        getByPath(f332, "/folder3/folder3.3/folder3.3.2");
        getByPath(f333, "/folder3/folder3.3/folder3.3.3");
        getByPath(doc12, "/folder1/folder1.2/Document1.2.Doc");
        getByPath(doc33, "/folder3/folder3.3/Document3.3.Doc");
        getByPath(doc331, "/folder3/folder3.3/folder3.3.1/Document3.3.1.Doc");
        getByPath(doc333, "/folder3/folder3.3/folder3.3.3/Document3.3.3.Doc");

        log.info("... testGetObjectByPath() finished.");
    }

    @Test
    public void testCreateDocumentWithContent() {
        log.info("starting testCreateDocumentWithContent() ...");
        String id = createDocument(fRootFolderId, true);
        if (id != null) {
            log.info("createDocument succeeded with created id: " + id);
        }

        ContentStream sd = fObjSvc.getContentStream(fRepositoryId, id, null, BigInteger.valueOf(-1) /* offset */,
                BigInteger.valueOf(-1) /* length */, null);
        verifyContentResult(sd);

        // delete content again
        Holder<String> idHolder = new Holder<String>(id);
        Properties props = fObjSvc.getProperties(fRepositoryId, id, PropertyIds.CHANGE_TOKEN, null);
        String changeToken = (String) props.getProperties().get(PropertyIds.CHANGE_TOKEN).getFirstValue();
        Holder<String> tokenHolder = new Holder<String>(changeToken);
        fObjSvc.deleteContentStream(fRepositoryId, idHolder, tokenHolder, null);

        try {
            props = fObjSvc.getProperties(fRepositoryId, id, PropertyIds.CHANGE_TOKEN, null);
            changeToken = (String) props.getProperties().get(PropertyIds.CHANGE_TOKEN).getFirstValue();
            tokenHolder = new Holder<String>(changeToken);
            sd = fObjSvc.getContentStream(fRepositoryId, id, null, BigInteger.valueOf(-1) /* offset */,
                    BigInteger.valueOf(-1) /* length */, null);
            fail("getContentStream with non existing content should raise a CmisConstraintException");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
        }

        // create content again in a second call
        ContentStream contentStream = createContent();
        fObjSvc.setContentStream(fRepositoryId, idHolder, true, tokenHolder, contentStream, null);
        sd = fObjSvc.getContentStream(fRepositoryId, id, null, BigInteger.valueOf(-1) /* offset */,
                BigInteger.valueOf(-1) /* length */, null);
        verifyContentResult(sd);

        // update content and do not set overwrite flag, expect failure
        try {
            props = fObjSvc.getProperties(fRepositoryId, id, PropertyIds.CHANGE_TOKEN, null);
            changeToken = (String) props.getProperties().get(PropertyIds.CHANGE_TOKEN).getFirstValue();
            tokenHolder = new Holder<String>(changeToken);
            fObjSvc.setContentStream(fRepositoryId, idHolder, false, tokenHolder, contentStream, null);
            fail("setContentStream with existing content and no overWriteFlag should fail");
        } catch (Exception e) {
            assertTrue(e instanceof CmisContentAlreadyExistsException);
        }

        // cleanup
        fObjSvc.deleteObject(fRepositoryId, id, true, null);

        log.info("... testCreateDocumentWithContent() finished.");
    }

    @Test
    public void testCreateDocumentWithContentNoFileNameNoMimeType() {
        log.info("starting testCreateDocumentWithContent() ...");
        ContentStreamDataImpl contentStream = null;
        List<String> policies = null;
        Acl addACEs = null;
        Acl removeACEs = null;
        ExtensionsData extension = null;

        Properties props = createDocumentProperties(DOCUMENT_ID, DOCUMENT_TYPE_ID);

        contentStream = (ContentStreamDataImpl) createContent();
        contentStream.setFileName(null);
        contentStream.setMimeType(null);

        String id = null;
        try {
            id = fObjSvc.createDocument(fRepositoryId, props, fRootFolderId, contentStream, VersioningState.NONE,
                    policies, addACEs, removeACEs, extension);
            if (null == id) {
                fail("createDocument failed.");
            }

            ContentStream sd = fObjSvc.getContentStream(fRepositoryId, id, null, BigInteger.valueOf(-1) /* offset */,
                    BigInteger.valueOf(-1) /* length */, null);
            assertNotNull(sd.getMimeType());
            assertNotNull(sd.getFileName());
        } catch (Exception e) {
            fail("createDocument() failed with exception: " + e);
        }
    }

    @Test
    public void testCreateDocumentFromSource() {
        log.info("starting testCreateDocumentFromSource() ...");
        // create a 1st document
        String id1 = createDocument(fRootFolderId, true);
        // create a second document with first as source
        String id2 = null;
        try {
            VersioningState versioningState = VersioningState.NONE;
            Properties props = createDocumentPropertiesForDocumentFromSource("Document From Source");
            id2 = fObjSvc.createDocumentFromSource(fRepositoryId, id1, props, fRootFolderId, versioningState, null,
                    null, null, null);
            if (null == id2) {
                fail("createDocumentFromSource failed.");
            }
        } catch (Exception e) {
            fail("createDocumentFromSource() failed with exception: " + e);
        }

        // get content from second document and compare it with original one
        ContentStream sd = fObjSvc.getContentStream(fRepositoryId, id2, null, BigInteger.valueOf(-1) /* offset */,
                BigInteger.valueOf(-1) /* length */, null);
        verifyContentResult(sd);

        // cleanup
        fObjSvc.deleteObject(fRepositoryId, id1, true, null);
        fObjSvc.deleteObject(fRepositoryId, id2, true, null);

        log.info("... testCreateDocumentFromSource() finished.");
    }

    @Test
    public void testCreatedDocumentInherited() {
        log.info("starting testCreatedDocumentInherited() ...");
        log.info("  creating object");

        String id = createDocumentInheritedProperties(fRootFolderId, false);
        if (id != null) {
            log.info("  createDocument succeeded with created id: " + id);
        }

        log.info("  getting object");
        try {
            ObjectData res = fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE, null, false,
                    false, null);
            assertNotNull(res);

            String returnedId = res.getId();
            assertEquals(id, returnedId);
            Map<String, PropertyData<?>> props = res.getProperties().getProperties();
            for (PropertyData<?> pd : props.values()) {
                log.info("return property id: " + pd.getId() + ", value: " + pd.getValues());
            }

            PropertyData<?> pd = props.get(PropertyIds.NAME);
            assertNotNull(pd);
            assertEquals(MY_CUSTOM_NAME, pd.getFirstValue());

            pd = props.get(PropertyIds.OBJECT_TYPE_ID);
            assertEquals(TEST_INHERITED_CUSTOM_DOCUMENT_TYPE_ID, pd.getFirstValue());

            pd = props.get(TEST_DOCUMENT_MY_STRING_PROP_ID);
            assertEquals("My pretty string", pd.getFirstValue());

            pd = props.get(TEST_DOCUMENT_MY_INT_PROP_ID);
            assertEquals(BigInteger.valueOf(4711), pd.getFirstValue());

            pd = props.get(TEST_DOCUMENT_MY_SUB_STRING_PROP_ID);
            assertEquals("another cool string", pd.getFirstValue());

            pd = props.get(TEST_DOCUMENT_MY_SUB_INT_PROP_ID);
            assertEquals(BigInteger.valueOf(4712), pd.getFirstValue());
        } catch (Exception e) {
            fail("getObject() failed with exception: " + e);
        }
        log.info("... testCreatedDocumentInherited() finished.");
    }

    @Test
    public void testBuildFolderAndDocuments() {
        // Create a hierarchy of folders and fill it with some documents

        ObjectGenerator gen = new ObjectGenerator(fFactory, fNavSvc, fObjSvc, fRepSvc, fRepositoryId,
                ObjectGenerator.ContentKind.LOREM_IPSUM_TEXT);
        int levels = 2; // create a hierarchy with two levels
        int childrenPerLevel = 2; // create two folders on each level

        gen.setNumberOfDocumentsToCreatePerFolder(1); // create one document in
        // each folder

        // Set the type id for all created documents:
        gen.setDocumentTypeId(TEST_DOCUMENT_TYPE_ID);

        // Set the type id for all created folders:
        gen.setFolderTypeId(TEST_FOLDER_TYPE_ID);

        // set the properties the generator should fill with values for
        // documents:
        // Note: must be valid properties in type TEST_DOCUMENT_TYPE_ID
        List<String> propsToSet = new ArrayList<String>();
        propsToSet.add(TEST_DOCUMENT_STRING_PROP_ID);
        gen.setDocumentPropertiesToGenerate(propsToSet);

        // set the properties the generator should fill with values for folders:
        // Note: must be valid properties in type TEST_FOLDER_TYPE_ID
        propsToSet = new ArrayList<String>();
        propsToSet.add(TEST_FOLDER_STRING_PROP_ID);
        gen.setFolderPropertiesToGenerate(propsToSet);

        // Build the tree
        try {
            gen.createFolderHierachy(levels, childrenPerLevel, fRootFolderId);
            // Dump the tree
            gen.dumpFolder(fRootFolderId, "*");
        } catch (Exception e) {
            fail("Could not create folder hierarchy with documents. " + e);
        }
    }

    @Test
    public void testDeleteObject() {
        log.info("starting testDeleteObject() ...");
        log.info("Testing to delete a document");
        log.info("  creating object");
        String id = createDocument(fRootFolderId, false);
        if (id != null) {
            log.info("  createDocument succeeded with created id: " + id);
        }

        log.info("  getting object");
        retrieveDocument(id);
        log.info("  deleting object");
        try {
            fObjSvc.deleteObject(fRepositoryId, id, true, null);
        } catch (Exception e) {
            fail("deleteObject() for document failed with exception: " + e);
        }

        // check that it does not exist anymore
        try {
            fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE, null, false, false, null);
            fail("object should not longer exist after it was deleted.");
        } catch (CmisObjectNotFoundException e) {
            assertTrue(e instanceof CmisObjectNotFoundException);
        } catch (Exception e) {
            fail("getting deleted object should raise CMISObjectNotFoundException, but got " + e);
        }

        log.info("Testing to delete an empty folder");
        // create and delete an empty folder
        id = createFolder();
        try {
            fObjSvc.deleteObject(fRepositoryId, id, true, null);
        } catch (Exception e) {
            fail("deleteObject() for folder failed with exception: " + e);
        }
        // check that it does not exist anymore
        try {
            fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE, null, false, false, null);
            fail("object should not longer exist after it was deleted.");
        } catch (CmisObjectNotFoundException e) {
            assertTrue(e instanceof CmisObjectNotFoundException);
        } catch (Exception e) {
            fail("getting deleted object should raise CMISObjectNotFoundException, but got " + e);
        }

        // create a folder with a document and delete should fail
        // create and delete an empty folder
        log.info("Testing to delete a folder with a contained document");
        String folderId;
        folderId = createFolder();
        id = createDocument(folderId, false);

        try {
            fObjSvc.deleteObject(fRepositoryId, folderId, true, null);
            fail("deleteObject() for folder with a document should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
        }
        // should succeed if we first delete document then folder
        try {
            fObjSvc.deleteObject(fRepositoryId, id, true, null);
            fObjSvc.deleteObject(fRepositoryId, folderId, true, null);
        } catch (Exception e) {
            fail("deleteObject() for document and folder failed with exception: " + e);
        }
        // check that it does not exist anymore
        try {
            fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE, null, false, false, null);
            fail("object should not longer exist after it was deleted.");
        } catch (CmisObjectNotFoundException e) {
            assertTrue(e instanceof CmisObjectNotFoundException);
        } catch (Exception e) {
            fail("getting deleted object should raise CMISObjectNotFoundException, but got " + e);
        }
        try {
            fObjSvc.getObject(fRepositoryId, folderId, "*", false, IncludeRelationships.NONE, null, false, false, null);
            fail("object should not longer exist after it was deleted.");
        } catch (CmisObjectNotFoundException e) {
            assertTrue(e instanceof CmisObjectNotFoundException);
        } catch (Exception e) {
            fail("getting deleted object should raise CMISObjectNotFoundException, but got " + e);
        }
        log.info("... testDeleteObject() finished.");
    }

    @Test
    public void testDeleteTree() {
        log.info("starting testDeleteTree() ...");
        ObjectGenerator gen = new ObjectGenerator(fFactory, fNavSvc, fObjSvc, fRepSvc, fRepositoryId,
                ObjectGenerator.ContentKind.LOREM_IPSUM_TEXT);
        String rootFolderId = createFolder();
        // Set the type id for all created documents:
        gen.setDocumentTypeId(DocumentTypeCreationHelper.getCmisDocumentType().getId());
        // Set the type id for all created folders:
        gen.setFolderTypeId(DocumentTypeCreationHelper.getCmisFolderType().getId());
        gen.setNumberOfDocumentsToCreatePerFolder(2); // create two documents in
        // each folder
        gen.createFolderHierachy(1, 1, rootFolderId);
        try {
            fObjSvc.deleteTree(fRepositoryId, rootFolderId, null /* true */, UnfileObject.DELETE, true, null);
        } catch (Exception e) {
            fail("deleteTree failed unexpected. " + e);
        }
        log.info("Dumping folder, should only contain one empty folder under root");
        gen.dumpFolder(fRootFolderId, "*");

        // After that we should be not be able to get the root folder, because
        // it should be deleted
        try {
            fObjSvc.getObject(fRepositoryId, rootFolderId, "*", false, IncludeRelationships.NONE, null, false, false,
                    null);
            fail("object should not longer exist after it was deleted.");
        } catch (CmisObjectNotFoundException e) {
            assertTrue(e instanceof CmisObjectNotFoundException);
        } catch (Exception e) {
            fail("getting deleted object should raise CMISObjectNotFoundException, but got " + e);
        }
        log.info("... testDeleteTree() finished.");
    }

    @Test
    public void testMoveFolder() {
        log.info("starting testMoveFolder() ...");
        moveObjectTest(true);
        log.info("... testMoveFolder() finished.");
    }

    @Test
    public void testMoveDocument() {
        log.info("starting testMoveDocument() ...");
        moveObjectTest(false);
        log.info("... testMoveDocument() finished.");
    }

    @Test
    public void testUpdateProperties() {
        // TODO add test rename root folder and non root folder (must be a
        // folder)
        log.info("starting testUpdateProperties() ...");
        String oldChangeToken, newChangeToken;
        String id = createDocumentWithCustomType(MY_CUSTOM_NAME, fRootFolderId, false);
        if (id != null) {
            log.info("createDocument succeeded with created id: " + id);
        }

        log.info("  getting object");
        try {
            ObjectData res = fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE, null, false,
                    false, null);
            assertNotNull(res);
            Map<String, PropertyData<?>> props = res.getProperties().getProperties();

            // check returned properties
            for (PropertyData<?> pd : props.values()) {
                log.info("  return property id: " + pd.getId() + ", value: " + pd.getValues());
            }

            String returnedId = res.getId();
            assertEquals(id, returnedId);
            PropertyData<?> pd = props.get(PropertyIds.NAME);
            assertNotNull(pd);
            assertEquals(MY_CUSTOM_NAME, pd.getFirstValue());
            pd = props.get(PropertyIds.OBJECT_TYPE_ID);
            assertEquals(TEST_CUSTOM_DOCUMENT_TYPE_ID, pd.getFirstValue());
            pd = props.get(TEST_DOCUMENT_MY_STRING_PROP_ID);
            assertEquals("My pretty string", pd.getFirstValue());
            pd = props.get(TEST_DOCUMENT_MY_INT_PROP_ID);
            assertEquals(BigInteger.valueOf(4711), pd.getFirstValue());

            // update properties:
            log.info("updating property");
            List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
            // properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_NAME
            // , MY_CUSTOM_NAME));
            // properties.add(fFactory.createPropertyIdData(PropertyIds.
            // CMIS_OBJECT_TYPE_ID, TEST_CUSTOM_DOCUMENT_TYPE_ID));
            // Generate some property values for custom attributes
            properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_MY_STRING_PROP_ID, NEW_STRING_PROP_VAL));
            properties.add(fFactory.createPropertyIntegerData(TEST_DOCUMENT_MY_INT_PROP_ID, NEW_INT_PROP_VAL));
            Properties newProps = fFactory.createPropertiesData(properties);

            Holder<String> idHolder = new Holder<String>(id);
            Holder<String> changeTokenHolder = new Holder<String>();
            fObjSvc.updateProperties(fRepositoryId, idHolder, changeTokenHolder, newProps, null);
            oldChangeToken = changeTokenHolder.getValue(); // store for later
            verifyUpdatedProperties(id, MY_CUSTOM_NAME);

            // Test delete properties
            log.info("deleting property");
            properties = new ArrayList<PropertyData<?>>();
            properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_MY_STRING_PROP_ID, (String) null));
            newProps = fFactory.createPropertiesData(properties);
            Thread.sleep(100); // ensure new change token, timer resolution is
            // not good enough
            fObjSvc.updateProperties(fRepositoryId, idHolder, changeTokenHolder, newProps, null);
            res = fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE, null, false, false, null);
            assertNotNull(res);
            props = res.getProperties().getProperties();
            for (PropertyData<?> pd2 : props.values()) {
                log.info("  return property id: " + pd2.getId() + ", value: " + pd2.getValues());
            }
            pd = props.get(TEST_DOCUMENT_MY_STRING_PROP_ID);
            assertNull(pd.getFirstValue());
            // delete a required property and expect exception:
            properties = new ArrayList<PropertyData<?>>();
            properties.add(fFactory.createPropertyIntegerData(TEST_DOCUMENT_MY_INT_PROP_ID, (BigInteger) null));
            newProps = fFactory.createPropertiesData(properties);
            idHolder = new Holder<String>(id);
            try {
                fObjSvc.updateProperties(fRepositoryId, idHolder, changeTokenHolder, newProps, null);
                fail("Deleteing a required property should fail.");
            } catch (Exception e) {
                assertTrue(e instanceof CmisConstraintException);
            }

            // Test violation of property definition constraints
            log.info("Test violation of property definition constraints");
            properties = new ArrayList<PropertyData<?>>();
            properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_MY_STRING_PROP_ID,
                    "A very long String ABCDEFHIJKLMNOPQRSTUVWXYZ"));
            newProps = fFactory.createPropertiesData(properties);
            idHolder = new Holder<String>(id);
            try {
                fObjSvc.updateProperties(fRepositoryId, idHolder, changeTokenHolder, newProps, null);
                fail("Exceeding max String lengt h should fail.");
            } catch (Exception e) {
                assertTrue(e instanceof CmisConstraintException);
            }
            // Test stale token
            log.info("Test stale token");
            properties = new ArrayList<PropertyData<?>>();
            properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_MY_STRING_PROP_ID, "ABC"));
            newProps = fFactory.createPropertiesData(properties);
            // set outdated token
            newChangeToken = changeTokenHolder.getValue();
            changeTokenHolder.setValue(oldChangeToken);
            assertFalse(oldChangeToken.equals(newChangeToken));
            try {
                fObjSvc.updateProperties(fRepositoryId, idHolder, changeTokenHolder, newProps, null);
                fail("Update with an outdated changeToken should fail.");
            } catch (Exception e) {
                assertTrue(e instanceof CmisUpdateConflictException);
            }

            // test a rename
            log.info("Test renaming");
            final String newName = "My Renamed Document"; // MY_CUSTOM_NAME
            properties = new ArrayList<PropertyData<?>>();
            properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, newName));
            newProps = fFactory.createPropertiesData(properties);
            changeTokenHolder.setValue(newChangeToken);
            fObjSvc.updateProperties(fRepositoryId, idHolder, changeTokenHolder, newProps, null);
            id = idHolder.getValue(); // note that id is path and has changed!
            res = fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE, null, false, false, null);
            assertNotNull(res);
            props = res.getProperties().getProperties();
            pd = props.get(PropertyIds.NAME);
            assertNotNull(pd);
            assertEquals(newName, pd.getFirstValue());

            // test rename with a conflicting name
            createDocumentWithCustomType(MY_CUSTOM_NAME, fRootFolderId, false);
            properties = new ArrayList<PropertyData<?>>();
            properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, MY_CUSTOM_NAME));
            newProps = fFactory.createPropertiesData(properties);
            // now rename to old name
            try {
                fObjSvc.updateProperties(fRepositoryId, idHolder, changeTokenHolder, newProps, null);
                fail("Update with a conflicting name should fail.");
            } catch (Exception e) {
                assertTrue(e instanceof CmisNameConstraintViolationException);
            }

            // test an invalid name
            properties = new ArrayList<PropertyData<?>>();
            properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, "Invalid/Name"));
            newProps = fFactory.createPropertiesData(properties);
            try {
                fObjSvc.updateProperties(fRepositoryId, idHolder, changeTokenHolder, newProps, null);
                fail("Update with an invalid name should fail.");
            } catch (Exception e) {
                assertTrue(e instanceof CmisInvalidArgumentException);
            }

        } catch (Exception e) {
            fail("getObject() failed with exception: " + e);
        }
        log.info("... testUpdateProperties() finished.");
    }

    @Test
    public void testAllowableActions() {
        log.info("starting testAllowableActions() ...");
        final boolean withContent = false;
        String id = createDocument(fRootFolderId, withContent);

        // get allowable actions via getObject
        ObjectData res = fObjSvc.getObject(fRepositoryId, id, "*", true, IncludeRelationships.NONE, null, false, false,
                null);
        assertNotNull(res.getAllowableActions());
        Set<Action> actions = res.getAllowableActions().getAllowableActions();
        assertNotNull(actions);
        verifyAllowableActionsDocument(actions, false, withContent);

        // get allowable actions via getAllowableActions
        AllowableActions allowableActions = fObjSvc.getAllowableActions(fRepositoryId, id, null);
        assertNotNull(allowableActions);
        actions = allowableActions.getAllowableActions();
        assertNotNull(actions);
        verifyAllowableActionsDocument(actions, false, withContent);

        // cleanup
        fObjSvc.deleteObject(fRepositoryId, id, true, null);
        log.info("... testAllowableActions() finished.");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDefaultPropertiesDocument() {
        log.info("starting testDefaultPropertiesDocument() ...");
        String id = createDocument("DefPropDoc", fRootFolderId, TEST_DOC_TYPE_WITH_DEFAULTS_ID, false);
        if (id != null) {
            log.info("createDocument succeeded with created id: " + id);
        }
        ObjectData res = getDocumentObjectData(id);
        Map<String, PropertyData<?>> props = res.getProperties().getProperties();
        PropertyData<?> pd = props.get(TEST_DOCUMENT_MY_INT_PROP_ID);
        assertNotNull(pd);
        Object bi = pd.getFirstValue();
        assertNotNull(bi);
        assertEquals(BigInteger.valueOf(100), bi);

        pd = props.get(TEST_DOCUMENT_MY_MULTI_STRING_PROP_ID);
        assertNotNull(pd);
        List<String> valueList = (List<String>) pd.getValues();
        assertNotNull(valueList);
        assertTrue(valueList.contains("Apache"));
        assertTrue(valueList.contains("CMIS"));

        pd = props.get(TEST_DOCUMENT_MY_INT_PROP_ID_MANDATORY_DEFAULT);
        assertNotNull(pd);
        bi = pd.getFirstValue();
        assertNotNull(bi);
        assertEquals(BigInteger.valueOf(100), bi);

        log.info("... testDefaultPropertiesDocument() finished.");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDefaultPropertiesFolder() {
        log.info("starting testDefaultPropertiesFolder() ...");
        String id = createFolder("DefPropFolder", fRootFolderId, TEST_FOLDER_TYPE_WITH_DEFAULTS_ID);
        if (id != null) {
            log.info("createDocument succeeded with created id: " + id);
        }
        ObjectData res = getDocumentObjectData(id);
        Map<String, PropertyData<?>> props = res.getProperties().getProperties();
        PropertyData<?> pd = props.get(TEST_FOLDER_MY_INT_PROP_ID);
        assertNotNull(pd);
        Object bi = pd.getFirstValue();
        assertNotNull(bi);
        assertEquals(BigInteger.valueOf(100), bi);

        pd = props.get(TEST_FOLDER_MY_MULTI_STRING_PROP_ID);
        assertNotNull(pd);
        List<String> valueList = (List<String>) pd.getValues();
        assertNotNull(valueList);
        assertTrue(valueList.contains("Apache"));
        assertTrue(valueList.contains("CMIS"));

        pd = props.get(TEST_FOLDER_MY_INT_PROP_ID_MANDATORY_DEFAULT);
        assertNotNull(pd);
        bi = pd.getFirstValue();
        assertNotNull(bi);
        assertEquals(BigInteger.valueOf(100), bi);

        log.info("... testDefaultPropertiesFolder() finished.");
    }

    @Test
    public void testGetObjectNoObjectIdInFilter() {
        log.info("starting testGetObjectNoObjectIdInFilter() ...");
        log.info("  creating object");
        String id = createDocument(fRootFolderId, false);
        if (id != null) {
            log.info("  createDocument succeeded with created id: " + id);
        }

        log.info("  getting object");
        String filter = PropertyIds.NAME + "," + PropertyIds.CREATION_DATE + "," + PropertyIds.LAST_MODIFICATION_DATE;
        ObjectData res = fObjSvc.getObject(fRepositoryId, id, filter, false, IncludeRelationships.NONE, null, false,
                false, null);

        String returnedId = res.getId();
        assertEquals(id, returnedId);
        log.info("... testGetObjectNoObjectIdInFilter() finished.");
    }

    @Test
    public void testSpecialChars() {

        log.info("starting testGetObjectByPath() with specal chars...");
        log.info("  creating object");

        createDocument("H������nschen", fRootFolderId, false);
        log.info("  getting object by path with special chars");
        try {
            ObjectData res = fObjSvc.getObjectByPath(fRepositoryId, "/H������nschen", "*", false,
                    IncludeRelationships.NONE, null, false, false, null);
            assertNotNull(res);
            assertNotNull(res.getId());
        } catch (Exception e) {
            fail("getObject() failed with exception: " + e);
        }
    }

    @Test
    public void testNoContentAllowed() {
        log.info("starting testNoContentAllowed() ...");
        String id = createDocument("NoContentAllowedDoc1", fRootFolderId, TEST_CUSTOM_NO_CONTENT_TYPE_ID, false);
        assertNotNull(id);

        try {
            id = createDocumentNoCatch("NoContentAllowedDoc2", fRootFolderId, TEST_CUSTOM_NO_CONTENT_TYPE_ID,
                    VersioningState.NONE, true);
            fail("Creating  document with content and type allows no content should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
            log.info("Creating  document with content for no-content type failed as expected.");
        }
        log.info("... testNoContentAllowed finished.");
    }

    @Test
    public void testMustHaveContent() {
        log.info("starting testMustHaveContent() ...");
        String id = createDocument("MustHaveContentAllowedDoc1", fRootFolderId, TEST_CUSTOM_MUST_CONTENT_TYPE_ID, true);
        assertNotNull(id);

        try {
            id = createDocumentNoCatch("MustHaveContentAllowedDoc2", fRootFolderId, TEST_CUSTOM_MUST_CONTENT_TYPE_ID,
                    VersioningState.NONE, false);
            fail("Creating document without content and type requires content should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
            log.info("Creating document with content for must-have-content type failed as expected.");
        }
        log.info("... testMustHaveContent finished.");
    }

    @Test
    public void testMaxContentSize() {
        log.info("starting testMaxContentSize() ...");
        try {
            createContent(MAX_SIZE + 1, MAX_SIZE, null);
            fail("createContent with exceeded content size should fail.");
        } catch (CmisInvalidArgumentException e) {
            log.debug("createDocument with exceeded failed as excpected.");
        } catch (Exception e1) {
            log.debug("createDocument with exceeded failed with wrong exception (expected CmisInvalidArgumentException, got "
                    + e1.getClass().getName() + ").");
        }

        try {
            ContentStream contentStream = createContent(MAX_SIZE + 1, MAX_SIZE, null);
            Properties props = createDocumentProperties("TestMaxContentSize", DOCUMENT_TYPE_ID);
            fObjSvc.createDocument(fRepositoryId, props, fRootFolderId, contentStream, VersioningState.NONE, null,
                    null, null, null);
            fail("createDocument with exceeded content size should fail.");
        } catch (CmisInvalidArgumentException e) {
            log.debug("createDocument with exceeded failed as expected.");
        } catch (Exception e1) {
            log.debug("createDocument with exceeded failed with wrong exception (expected CmisInvalidArgumentException, got "
                    + e1.getClass().getName() + ").");
        }
    }

    @Test
    public void testRenditionImage() {
        // upload an image as JPEG picture
        log.info("starting testRendition() ...");

        try {
            InputStream imageStream = this.getClass().getResourceAsStream("/image.jpg");
            assertNotNull("Test setup failure no 'image.jpg' in test resources, getResourceAsStream failed",
                    imageStream);
            String id = createDocumentFromStream("TestJpegImage", fRootFolderId, DOCUMENT_TYPE_ID, imageStream, JPEG);

            assertNotNull(id);
            String renditionFilter = "*";
            List<RenditionData> renditions = fObjSvc
                    .getRenditions(fRepositoryId, id, renditionFilter, null, null, null);
            assertNotNull(renditions);
            assertEquals(1, renditions.size());
            RenditionData rd = renditions.get(0);
            assertEquals(JPEG, rd.getMimeType());
            assertEquals("cmis:thumbnail", rd.getKind());
            assertEquals(id, rd.getRenditionDocumentId());
            assertNotNull(rd.getBigHeight());
            assertNotNull(rd.getBigWidth());
            assertEquals(RenditionUtil.THUMBNAIL_SIZE, rd.getBigHeight().longValue());
            assertEquals(RenditionUtil.THUMBNAIL_SIZE, rd.getBigWidth().longValue());
            assertNotNull(rd.getStreamId());
            ContentStream renditionContent = fObjSvc.getContentStream(fRepositoryId, id, rd.getStreamId(), null, null,
                    null);
            assertEquals(rd.getMimeType(), renditionContent.getMimeType());
            readThumbnailStream(renditionContent.getStream());
        } catch (Exception e) {
            log.error("testRendition failed with exception ", e);
            fail("testRendition failed with exceetion " + e);
        }
        log.info("... testRendition finished.");
    }

    @Test
    public void testRenditionIcon() {
        // fake an office document
        log.info("starting testRendition() ...");

        try {
            ContentStream content = createContent(4, 0,
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            Properties props = createDocumentProperties("TestJOffice", DOCUMENT_TYPE_ID);
            String id = fObjSvc.createDocument(fRepositoryId, props, fRootFolderId, content, VersioningState.NONE,
                    null, null, null, null);

            assertNotNull(id);
            String renditionFilter = "*";
            List<RenditionData> renditions = fObjSvc
                    .getRenditions(fRepositoryId, id, renditionFilter, null, null, null);
            assertNotNull(renditions);
            assertEquals(1, renditions.size());
            RenditionData rd = renditions.get(0);
            assertEquals(PNG, rd.getMimeType());
            assertEquals("cmis:thumbnail", rd.getKind());
            assertEquals(id, rd.getRenditionDocumentId());
            assertNotNull(rd.getBigHeight());
            assertNotNull(rd.getBigWidth());
            assertEquals(RenditionUtil.ICON_SIZE, rd.getBigHeight().longValue());
            assertEquals(RenditionUtil.ICON_SIZE, rd.getBigWidth().longValue());
            assertNotNull(rd.getStreamId());
            ContentStream renditionContent = fObjSvc.getContentStream(fRepositoryId, id, rd.getStreamId(), null, null,
                    null);
            assertEquals(rd.getMimeType(), renditionContent.getMimeType());
            readThumbnailStream(renditionContent.getStream());
        } catch (Exception e) {
            log.error("testRendition failed with exception ", e);
            fail("testRendition failed with exceetion " + e);
        }
        log.info("... testRendition finished.");
    }

    @Test
    public void testFolderRendition() {
        // upload an image as JPEG picture
        log.info("starting testFolderRendition() ...");

        try {
            InputStream imageStream = this.getClass().getResourceAsStream("/image.jpg");
            assertNotNull("Test setup failure no 'image.jpg' in test resources, getResourceAsStream failed",
                    imageStream);
            String id = createFolder();

            assertNotNull(id);
            String renditionFilter = "*";
            List<RenditionData> renditions = fObjSvc
                    .getRenditions(fRepositoryId, id, renditionFilter, null, null, null);
            assertNotNull(renditions);
            assertEquals(1, renditions.size());
            RenditionData rd = renditions.get(0);
            assertEquals(PNG, rd.getMimeType());
            assertEquals("cmis:thumbnail", rd.getKind());
            assertEquals(id, rd.getRenditionDocumentId());
            assertNotNull(rd.getBigHeight());
            assertNotNull(rd.getBigWidth());
            assertEquals(RenditionUtil.ICON_SIZE, rd.getBigHeight().longValue());
            assertEquals(RenditionUtil.ICON_SIZE, rd.getBigWidth().longValue());
            assertNotNull(rd.getStreamId());
            ContentStream renditionContent = fObjSvc.getContentStream(fRepositoryId, id, rd.getStreamId(), null, null,
                    null);
            assertEquals(rd.getMimeType(), renditionContent.getMimeType());
            readThumbnailStream(renditionContent.getStream());
        } catch (Exception e) {
            log.error("testFolderRendition failed with exception ", e);
            fail("testFolderRendition failed with exceetion " + e);
        }
        log.info("... testFolderRendition finished.");

    }

    @Test
    public void testAppendContent() {
        log.info("starting testAppendContent() ...");
        String id = createDocument(fRootFolderId, true);
        if (id != null) {
            log.info("createDocument succeeded with created id: " + id);
        }

        // append content again in a second call
        Holder<String> idHolder = new Holder<String>(id);

        ContentStream contentStream = createContent();
        fObjSvc.appendContentStream(fRepositoryId, idHolder, null, contentStream, false, null);
        ContentStream sd = fObjSvc.getContentStream(fRepositoryId, id, null, null, null, null);
        verifyContentResult(sd, 64);

        // cleanup
        fObjSvc.deleteObject(fRepositoryId, id, true, null);

        log.info("... testAppendContent() finished.");
    }

    @Test
    public void testGetPartialContent() throws IOException, UnsupportedEncodingException {
        log.info("starting testGetPartialContent() ...");
        final String STREAM_NAME = "data.txt";
        final String MIME_TYPE = "text/plain";
        // append content again in a second call

        ContentStreamDataImpl content = new ContentStreamDataImpl(-1);
        content.setFileName(STREAM_NAME);
        content.setMimeType(MIME_TYPE);

        String prefix = "################ ~~~~This is a completeley irrelevant prefix header. ~~~~ ################";
        String main = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec a diam lectus. Sed sit amet ipsum mauris. Maecenas congue ligula ac quam viverra nec consectetur ante hendrerit. Donec et mollis dolor. Praesent et diam eget libero egestas mattis sit amet vitae augue. Nam tincidunt congue enim, ut porta lorem lacinia consectetur. Donec ut libero sed arcu vehicula ultricies a non tortor. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean ut gravida lorem. Ut turpis felis, pulvinar a semper sed, adipiscing id dolor. Pellentesque auctor nisi id magna consequat sagittis. Curabitur dapibus enim sit amet elit pharetra tincidunt feugiat nisl imperdiet. Ut convallis libero in urna ultrices accumsan. Donec sed odio eros. Donec viverra mi quis quam pulvinar at malesuada arcu rhoncus. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. In rutrum accumsan ultricies. Mauris vitae nisi at sem facilisis semper ac in est.";
        String postfix = "################ ~~~~POSTFIX Please ignore POSTFIX POSTFIX POSTFIX ~~~~ ################";

        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(ba, false, ENCODING_UTF8);
        printStream.println(prefix);
        printStream.println(main);
        printStream.println(postfix);

        content.setContent(new ByteArrayInputStream(ba.toByteArray()));

        // Create document with content
        Properties props = createDocumentProperties("PartialContentTest", BaseTypeId.CMIS_DOCUMENT.value());
        String id = fObjSvc.createDocument(fRepositoryId, props, fRootFolderId, content, VersioningState.NONE, null,
                null, null, null);
        if (id != null) {
            log.info("createDocument succeeded with created id: " + id);
        }

        int offset = prefix.length() + System.getProperty("line.separator").length(); // for
                                                                                      // \n
        int length = main.length();
        ContentStream readContent = fObjSvc.getContentStream(fRepositoryId, id, null, BigInteger.valueOf(offset),
                BigInteger.valueOf(length), null);

        assertEquals(MIME_TYPE, readContent.getMimeType());
        assertEquals(STREAM_NAME, readContent.getFileName());
        assertEquals(length, readContent.getBigLength().longValue());

        byte[] bytesRead = new byte[10240];
        InputStream is = readContent.getStream();
        int lengthRead = is.read(bytesRead);
        String result = new String(bytesRead, 0, lengthRead, ENCODING_UTF8);
        assertEquals(length, lengthRead);
        assertEquals(main, result);

        // cleanup
        fObjSvc.deleteObject(fRepositoryId, id, true, null);

        log.info("... testGetPartialContent() finished.");
    }

    @Test
    public void testBulkUpdateProperties() {
        log.info("starting testBulkUpdateProperties() ...");
        String MY_CUSTOM_NAME_2 = MY_CUSTOM_NAME + "_2";
        String id1 = createDocumentWithCustomType(MY_CUSTOM_NAME, fRootFolderId, false);
        String id2 = createDocumentWithCustomType(MY_CUSTOM_NAME_2, fRootFolderId, false);
        String changeToken1, changeToken2;
        try {
            ObjectData res = fObjSvc.getObject(fRepositoryId, id1, "*", false, IncludeRelationships.NONE, null, false,
                    false, null);
            assertNotNull(res);
            Map<String, PropertyData<?>> props = res.getProperties().getProperties();
            changeToken1 = (String) props.get(PropertyIds.CHANGE_TOKEN).getFirstValue();

            res = fObjSvc
                    .getObject(fRepositoryId, id2, "*", false, IncludeRelationships.NONE, null, false, false, null);
            assertNotNull(res);
            props = res.getProperties().getProperties();
            changeToken2 = (String) props.get(PropertyIds.CHANGE_TOKEN).getFirstValue();

            // check returned properties
            for (PropertyData<?> pd : props.values()) {
                log.info("  return property id: " + pd.getId() + ", value: " + pd.getValues());
            }

            // update properties:
            log.info("updating property");
            List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
            properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_MY_STRING_PROP_ID, NEW_STRING_PROP_VAL));
            properties.add(fFactory.createPropertyIntegerData(TEST_DOCUMENT_MY_INT_PROP_ID, NEW_INT_PROP_VAL));
            Properties newProps = fFactory.createPropertiesData(properties);
            // wait some time to get a newer change token

            List<BulkUpdateObjectIdAndChangeToken> objs = new ArrayList<BulkUpdateObjectIdAndChangeToken>();
            objs.add(new BulkUpdateObjectIdAndChangeTokenImpl(id1, changeToken1));
            objs.add(new BulkUpdateObjectIdAndChangeTokenImpl(id2, changeToken2));

            List<BulkUpdateObjectIdAndChangeToken> newObjs;
            newObjs = fObjSvc.bulkUpdateProperties(fRepositoryId, objs, newProps, null, null, null);
            assertNotNull(newObjs);
            assertEquals(objs.size(), newObjs.size());
            for (int i = 0; i < newObjs.size(); i++) {
                assertEquals(objs.get(i).getId(), newObjs.get(i).getId());
                assertTrue(!objs.get(i).getChangeToken().equals(newObjs.get(i).getChangeToken()));
            }
            // check that new properties are set
            verifyUpdatedProperties(id1, MY_CUSTOM_NAME);
            verifyUpdatedProperties(id2, MY_CUSTOM_NAME_2);

        } catch (Exception e) {
            fail("testBulkUpdateProperties() failed with exception: " + e);
        }
        log.info("... testBulkUpdateProperties() finished.");
    }

    // CMIS 1.1: test item typpe
    @Test
    public void testItemCreation() {

        log.info("starting testItemCreation() ...");
        String propVal = "abc123";
        String name = "CoolItem";
        String id = createItem(name, fRootFolderId, propVal);
        if (id != null) {
            log.info("testItemCreation succeeded with created id: " + id);
        }
        log.info("... testCreateObject() finished.");

        // read document again and check properties
        ObjectData res = getDocumentObjectData(id);
        String returnedId = res.getId();
        Map<String, PropertyData<?>> props = res.getProperties().getProperties();
        testReturnedProperties(returnedId, name, TEST_ITEM_TYPE_ID, props);
        PropertyData<?> pd = props.get(ITEM_STRING_PROP);
        assertEquals(propVal, pd.getFirstValue());
        assertEquals(12, props.size()); // should not contain all the document
                                        // properties
        log.info("... finished testItemCreation()");
    }

    @Test
    public void testSecondaryTypes() {
        log.info("starting testItemCreation() ...");
        final String strPropVal = "Secondary";
        final BigInteger intPropVal = BigInteger.valueOf(100);
        final String primaryPropVal = "Sample Doc String Property";

        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyStringData(PropertyIds.NAME, "ObjectWithSecondaryType"));
        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, TEST_DOCUMENT_TYPE_ID));
        properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_STRING_PROP_ID, primaryPropVal));
        properties.add(fFactory.createPropertyIdData(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, TEST_SECONDARY_TYPE_ID));
        properties.add(fFactory.createPropertyStringData(SECONDARY_STRING_PROP, strPropVal));
        properties.add(fFactory.createPropertyIntegerData(SECONDARY_INTEGER_PROP, intPropVal));
        Properties props = fFactory.createPropertiesData(properties);

        String id = fObjSvc.createDocument(fRepositoryId, props, fRootFolderId, null, VersioningState.NONE, null, null,
                null, null);
        assertNotNull(id);

        Properties res = fObjSvc.getProperties(fRepositoryId, id, "*", null);
        assertNotNull(res.getProperties());
        Map<String, PropertyData<?>> returnedProps = res.getProperties();
        assertNotNull(returnedProps);
        String returnedValueStr = (String) returnedProps.get(SECONDARY_STRING_PROP).getFirstValue();
        BigInteger returnedValueInt = (BigInteger) returnedProps.get(SECONDARY_INTEGER_PROP).getFirstValue();
        assertEquals(strPropVal, returnedValueStr);
        assertEquals(intPropVal, returnedValueInt);
        String returnedPrimaryPropVal = (String) returnedProps.get(TEST_DOCUMENT_STRING_PROP_ID).getFirstValue();
        assertEquals(primaryPropVal, returnedPrimaryPropVal);

        // test updating properties
        final String strPropVal2 = "Secondary updated";
        final String primaryPropVal2 = "Sample Doc String Property updated";
        properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyStringData(SECONDARY_STRING_PROP, strPropVal2));
        properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_STRING_PROP_ID, primaryPropVal2));
        props = fFactory.createPropertiesData(properties);
        fObjSvc.updateProperties(fRepositoryId, new Holder<String>(id), new Holder<String>(), props, null);

        res = fObjSvc.getProperties(fRepositoryId, id, "*", null);
        assertNotNull(res.getProperties());
        returnedProps = res.getProperties();
        assertNotNull(returnedProps);
        returnedValueStr = (String) returnedProps.get(SECONDARY_STRING_PROP).getFirstValue();
        returnedValueInt = (BigInteger) returnedProps.get(SECONDARY_INTEGER_PROP).getFirstValue();
        assertEquals(strPropVal2, returnedValueStr);
        assertEquals(intPropVal, returnedValueInt);
        returnedPrimaryPropVal = (String) returnedProps.get(TEST_DOCUMENT_STRING_PROP_ID).getFirstValue();
        assertEquals(primaryPropVal2, returnedPrimaryPropVal);

        log.info("... finished testSecondaryTypes()");
    }

    @Test
    public void testSecondaryTypePropertiesNotSet() {
        log.info("starting testSecondaryTypePropertiesNotSet() ...");

        final String primaryPropVal = "Sample Doc String Property";

        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyStringData(PropertyIds.NAME, "ObjectWithSecondaryType"));
        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, TEST_DOCUMENT_TYPE_ID));
        properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_STRING_PROP_ID, primaryPropVal));
        properties.add(fFactory.createPropertyIdData(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, TEST_SECONDARY_TYPE_ID));
        Properties props = fFactory.createPropertiesData(properties);

        String id = fObjSvc.createDocument(fRepositoryId, props, fRootFolderId, null, VersioningState.NONE, null, null,
                null, null);
        assertNotNull(id);

        Properties res = fObjSvc.getProperties(fRepositoryId, id, "*", null);
        assertNotNull(res.getProperties());
        Map<String, PropertyData<?>> returnedProps = res.getProperties();
        assertNotNull(returnedProps);
        boolean hasProp = returnedProps.containsKey(SECONDARY_STRING_PROP);
        assertTrue(hasProp);
        hasProp = returnedProps.containsKey(SECONDARY_INTEGER_PROP);
        assertTrue(hasProp);
        PropertyData<?> returnedValue = returnedProps.get(SECONDARY_STRING_PROP);
        assertNotNull(returnedValue);
        assertTrue(returnedValue.getValues().isEmpty());
        returnedValue = returnedProps.get(SECONDARY_INTEGER_PROP);
        assertNotNull(returnedValue);
        assertTrue(returnedValue.getValues().isEmpty());

        log.info("... finished testSecondaryTypePropertiesNotSet()");
    }

    @Test
    public void testUpdatePropertiesWithTypeCreation() {
        final String strPropVal = "Secondary";
        final BigInteger intPropVal = BigInteger.valueOf(100);
        final String primaryPropVal = "Sample Doc String Property";
        final String primaryPropVal2 = "Sample Doc String Property updated";

        log.info("starting testUpdatePropertiesWithTypeCreation() ...");

        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyStringData(PropertyIds.NAME, "SimpleDocument"));
        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, TEST_DOCUMENT_TYPE_ID));
        properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_STRING_PROP_ID, primaryPropVal));
        Properties props = fFactory.createPropertiesData(properties);

        String id = fObjSvc.createDocument(fRepositoryId, props, fRootFolderId, null, VersioningState.NONE, null, null,
                null, null);
        assertNotNull(id);

        properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_STRING_PROP_ID, primaryPropVal2));
        properties.add(fFactory.createPropertyIdData(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, TEST_SECONDARY_TYPE_ID));
        properties.add(fFactory.createPropertyStringData(SECONDARY_STRING_PROP, strPropVal));
        properties.add(fFactory.createPropertyIntegerData(SECONDARY_INTEGER_PROP, intPropVal));
        props = fFactory.createPropertiesData(properties);
        fObjSvc.updateProperties(fRepositoryId, new Holder<String>(id), new Holder<String>(), props, null);

        Properties res = fObjSvc.getProperties(fRepositoryId, id, "*", null);
        assertNotNull(res.getProperties());
        Map<String, PropertyData<?>> returnedProps = res.getProperties();
        assertNotNull(returnedProps);
        assertEquals(1, returnedProps.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).getValues().size());
        String secIds = (String) returnedProps.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).getFirstValue();
        assertEquals(TEST_SECONDARY_TYPE_ID, secIds);
        String returnedValueStr = (String) returnedProps.get(SECONDARY_STRING_PROP).getFirstValue();
        BigInteger returnedValueInt = (BigInteger) returnedProps.get(SECONDARY_INTEGER_PROP).getFirstValue();
        assertEquals(strPropVal, returnedValueStr);
        assertEquals(intPropVal, returnedValueInt);
        String returnedPrimaryPropVal = (String) returnedProps.get(TEST_DOCUMENT_STRING_PROP_ID).getFirstValue();
        assertEquals(primaryPropVal2, returnedPrimaryPropVal);

        log.info("... finished testUpdatePropertiesWithTypeCreation()");
    }

    @Test
    public void testDeleteSecondaryType() {
        final String primaryPropVal = "Sample Doc String Property";
        final String primaryPropVal2 = "Sample Doc String Property updated";
        final String strPropVal = "Secondary";
        final BigInteger intPropVal = BigInteger.valueOf(100);

        log.info("starting testDeleteSecondaryType() ...");

        // create a document with a secondary type
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyStringData(PropertyIds.NAME, "ObjectWithSecondaryType"));
        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, TEST_DOCUMENT_TYPE_ID));
        properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_STRING_PROP_ID, primaryPropVal));
        properties.add(fFactory.createPropertyIdData(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, TEST_SECONDARY_TYPE_ID));
        properties.add(fFactory.createPropertyStringData(SECONDARY_STRING_PROP, strPropVal));
        properties.add(fFactory.createPropertyIntegerData(SECONDARY_INTEGER_PROP, intPropVal));
        Properties props = fFactory.createPropertiesData(properties);

        String id = fObjSvc.createDocument(fRepositoryId, props, fRootFolderId, null, VersioningState.NONE, null, null,
                null, null);
        assertNotNull(id);

        // delete the secondary type
        properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_STRING_PROP_ID, primaryPropVal2));
        properties.add(fFactory.createPropertyIdData(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, (String) null)); // set
                                                                                                             // list
                                                                                                             // to
                                                                                                             // empty
        props = fFactory.createPropertiesData(properties);
        fObjSvc.updateProperties(fRepositoryId, new Holder<String>(id), new Holder<String>(), props, null);

        // test that properties are gone
        Properties res = fObjSvc.getProperties(fRepositoryId, id, "*", null);
        assertNotNull(res.getProperties());
        Map<String, PropertyData<?>> returnedProps = res.getProperties();
        assertNotNull(returnedProps);
        assertEquals(0, returnedProps.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).getValues().size());
        PropertyData<?> pd = returnedProps.get(SECONDARY_STRING_PROP);
        assertNull(pd); // property must not exist any longer
        pd = returnedProps.get(SECONDARY_INTEGER_PROP);
        assertNull(pd); // property must not exist any longer
        String returnedPrimaryPropVal = (String) returnedProps.get(TEST_DOCUMENT_STRING_PROP_ID).getFirstValue();
        assertEquals(primaryPropVal2, returnedPrimaryPropVal); // other
                                                               // properties
                                                               // should be
                                                               // updated
        // test that system properties are there
        pd = returnedProps.get(PropertyIds.NAME);
        assertNotNull(pd);
        pd = returnedProps.get(PropertyIds.OBJECT_ID);
        assertNotNull(pd);
        pd = returnedProps.get(PropertyIds.OBJECT_TYPE_ID);
        assertNotNull(pd);

        log.info("... finished testDeleteSecondaryType()");
    }

    // TODO: test constraints on secondary types

    private void verifyUpdatedProperties(String id, String name) {

        ObjectData res = fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE, null, false,
                false, null);
        assertNotNull(res);
        Map<String, PropertyData<?>> props = res.getProperties().getProperties();
        for (PropertyData<?> pd2 : props.values()) {
            log.info("  return property id: " + pd2.getId() + ", value: " + pd2.getValues());
        }

        PropertyData<?> pd;
        pd = props.get(PropertyIds.NAME);
        assertNotNull(pd);
        assertEquals(name, pd.getFirstValue());
        pd = props.get(PropertyIds.OBJECT_TYPE_ID);
        assertEquals(TEST_CUSTOM_DOCUMENT_TYPE_ID, pd.getFirstValue());
        pd = props.get(TEST_DOCUMENT_MY_STRING_PROP_ID);
        assertEquals(NEW_STRING_PROP_VAL, pd.getFirstValue());
        pd = props.get(TEST_DOCUMENT_MY_INT_PROP_ID);
        assertEquals(NEW_INT_PROP_VAL, pd.getFirstValue());
    }

    protected String createDocumentFromStream(String name, String folderId, String typeId, InputStream is,
            String contentType) throws IOException {

        Properties props = createDocumentProperties(name, typeId);

        ContentStreamDataImpl content = new ContentStreamDataImpl(0);
        content.setFileName(name);
        content.setMimeType(contentType);

        ByteArrayOutputStream ba = new ByteArrayOutputStream();

        IOUtils.copy(is, ba, 64 * 1024);

        content.setContent(new ByteArrayInputStream(ba.toByteArray()));

        String id = fObjSvc.createDocument(fRepositoryId, props, folderId, content, VersioningState.NONE, null, null,
                null, null);
        return id;
    }

    private void readThumbnailStream(InputStream stream) {

        byte[] buffer = new byte[65536];
        int noBytesRead = 0;
        int count = 0;
        try {
            while ((noBytesRead = stream.read(buffer)) >= 0) {
                count += noBytesRead;
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Reading rendition stream failed with exception " + e);
        }
        assertTrue(count > 0);
    }

    private static void verifyAllowableActionsDocument(Set<Action> actions, boolean isVersioned, boolean hasContent) {
        assertTrue(actions.contains(Action.CAN_DELETE_OBJECT));
        assertTrue(actions.contains(Action.CAN_UPDATE_PROPERTIES));
        assertTrue(actions.contains(Action.CAN_GET_PROPERTIES));
        assertFalse(actions.contains(Action.CAN_GET_OBJECT_RELATIONSHIPS));
        assertTrue(actions.contains(Action.CAN_GET_OBJECT_PARENTS));

        assertFalse(actions.contains(Action.CAN_GET_FOLDER_PARENT));
        assertFalse(actions.contains(Action.CAN_GET_FOLDER_TREE));
        assertFalse(actions.contains(Action.CAN_GET_DESCENDANTS));
        assertTrue(actions.contains(Action.CAN_MOVE_OBJECT));
        if (hasContent) {
            assertTrue(actions.contains(Action.CAN_DELETE_CONTENT_STREAM));
            assertTrue(actions.contains(Action.CAN_GET_CONTENT_STREAM));
            assertTrue(actions.contains(Action.CAN_GET_RENDITIONS));
        } else {
            assertFalse(actions.contains(Action.CAN_DELETE_CONTENT_STREAM));
            assertFalse(actions.contains(Action.CAN_GET_CONTENT_STREAM));
            assertFalse(actions.contains(Action.CAN_GET_RENDITIONS));
        }
        assertTrue(actions.contains(Action.CAN_ADD_OBJECT_TO_FOLDER));
        assertTrue(actions.contains(Action.CAN_REMOVE_OBJECT_FROM_FOLDER));

        if (isVersioned) {
            assertTrue(actions.contains(Action.CAN_CANCEL_CHECK_OUT));
            assertTrue(actions.contains(Action.CAN_CHECK_IN));
            assertTrue(actions.contains(Action.CAN_CHECK_OUT));
            assertTrue(actions.contains(Action.CAN_GET_ALL_VERSIONS));

        } else {
            assertFalse(actions.contains(Action.CAN_CANCEL_CHECK_OUT));
            assertFalse(actions.contains(Action.CAN_CHECK_IN));
            assertFalse(actions.contains(Action.CAN_CHECK_OUT));
            assertFalse(actions.contains(Action.CAN_GET_ALL_VERSIONS));
        }
        assertTrue(actions.contains(Action.CAN_SET_CONTENT_STREAM));
        assertTrue(actions.contains(Action.CAN_APPLY_POLICY));
        assertTrue(actions.contains(Action.CAN_GET_APPLIED_POLICIES));
        assertFalse(actions.contains(Action.CAN_REMOVE_POLICY));
        assertFalse(actions.contains(Action.CAN_GET_CHILDREN));
        assertFalse(actions.contains(Action.CAN_CREATE_DOCUMENT));
        assertFalse(actions.contains(Action.CAN_CREATE_FOLDER));
        assertFalse(actions.contains(Action.CAN_CREATE_RELATIONSHIP));
        assertFalse(actions.contains(Action.CAN_DELETE_TREE));
        assertTrue(actions.contains(Action.CAN_GET_ACL));
        assertTrue(actions.contains(Action.CAN_APPLY_ACL));
    }

    private String retrieveDocument(String id) {
        ObjectData res = getDocumentObjectData(id);
        String returnedId = res.getId();
        testReturnedProperties(returnedId, DOCUMENT_ID, DOCUMENT_TYPE_ID, res.getProperties().getProperties());
        return returnedId;
    }

    private void moveObjectTest(boolean isFolder) {
        final String propertyFilter = PropertyIds.OBJECT_ID + "," + PropertyIds.NAME;
        String rootFolderId = createFolder();
        ObjectGenerator gen = new ObjectGenerator(fFactory, fNavSvc, fObjSvc, fRepSvc, fRepositoryId,
                ObjectGenerator.ContentKind.LOREM_IPSUM_TEXT);
        // Set the type id for all created documents:
        gen.setDocumentTypeId(DocumentTypeCreationHelper.getCmisDocumentType().getId());
        // Set the type id for all created folders:
        gen.setNumberOfDocumentsToCreatePerFolder(1); // create one document in
        // each folder
        gen.createFolderHierachy(3, 2, rootFolderId);
        gen.setFolderTypeId(DocumentTypeCreationHelper.getCmisFolderType().getId());
        gen.dumpFolder(fRootFolderId, propertyFilter);
        Holder<String> holder = new Holder<String>();
        String sourceIdToMove = gen.getFolderId(rootFolderId, 2, 1);
        if (!isFolder) {
            sourceIdToMove = gen.getDocumentId(sourceIdToMove, 0);
        }
        holder.setValue(sourceIdToMove); // "/Folder_1/My Folder 0/My Folder 1");
        String sourceFolderId = getSourceFolder(sourceIdToMove);
        log.info("Id before moveObject: " + holder.getValue());
        fObjSvc.moveObject(fRepositoryId, holder, rootFolderId, sourceFolderId, null);
        log.info("Id after moveObject: " + holder.getValue());
        gen.dumpFolder(fRootFolderId, propertyFilter);

        List<ObjectParentData> result = fNavSvc.getObjectParents(fRepositoryId, holder.getValue(), null, Boolean.FALSE,
                IncludeRelationships.NONE, null, Boolean.FALSE, null);
        // check that new parent is set correctly
        String newParentId = result.get(0).getObject().getId();
        assertEquals(rootFolderId, newParentId);

        if (isFolder) {
            log.info("testing moveFolder to a subfolder");
            ObjectInFolderList ch = fNavSvc.getChildren(fRepositoryId, holder.getValue(), propertyFilter, null, false,
                    IncludeRelationships.NONE, null, false, null, null, null);
            String subFolderId = ch.getObjects().get(0).getObject().getId();

            try {
                fObjSvc.moveObject(fRepositoryId, holder, subFolderId, sourceFolderId, null);
                fail("moveObject to a folder that is a descendant of the source must fail.");
            } catch (Exception e) {
                assertTrue(e instanceof CmisNotSupportedException);
            }
        }
    }

    private String createFolder() {
        return createFolder(FOLDER_ID, fRootFolderId, FOLDER_TYPE_ID);
    }

    private String createDocument(String folderId, boolean withContent) {
        return createDocument(DOCUMENT_ID, folderId, withContent);
    }

    private String createDocument(String name, String folderId, boolean withContent) {
        return createDocument(name, folderId, DOCUMENT_TYPE_ID, withContent);
    }

    private String createItem(String name, String folderId, String itemPropVal) {

        // create the properties:
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyStringData(PropertyIds.NAME, name));
        properties.add(fFactory.createPropertyStringData(ITEM_STRING_PROP, itemPropVal));
        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, TEST_ITEM_TYPE_ID));
        Properties props = fFactory.createPropertiesData(properties);

        return fObjSvc.createItem(fRepositoryId, props, folderId, null, null, null, null);
    }

    private Properties createDocumentPropertiesForDocumentFromSource(String name) {
        // We only provide a name but not a type id, as spec says to copy
        // missing attributes
        // from the existing one
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, name));
        Properties props = fFactory.createPropertiesData(properties);
        return props;
    }

    private void testReturnedProperties(String objectId, String objectName, String typeId,
            Map<String, PropertyData<?>> props) {
        super.testReturnedProperties(objectId, props);

        if (null != objectName) {
            PropertyData<?> pd = props.get(PropertyIds.NAME);
            assertNotNull(pd);
            assertEquals(objectName, pd.getFirstValue());
        }
        if (null != typeId) {
            PropertyData<?> pd = props.get(PropertyIds.OBJECT_TYPE_ID);
            assertEquals(typeId, pd.getFirstValue());
        }
    }

    private String createDocumentWithCustomType(String name, String folderId, boolean withContent) {
        ContentStream contentStream = null;
        VersioningState versioningState = VersioningState.NONE;
        List<String> policies = null;
        Acl addACEs = null;
        Acl removeACEs = null;
        ExtensionsData extension = null;

        // create the properties:
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, name));
        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, TEST_CUSTOM_DOCUMENT_TYPE_ID));
        // Generate some property values for custom attributes
        properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_MY_STRING_PROP_ID, "My pretty string"));
        properties.add(fFactory.createPropertyIntegerData(TEST_DOCUMENT_MY_INT_PROP_ID, BigInteger.valueOf(4711)));

        Properties props = fFactory.createPropertiesData(properties);

        if (withContent) {
            contentStream = createContent();
        }

        // create the document
        String id = null;
        id = fObjSvc.createDocument(fRepositoryId, props, folderId, contentStream, versioningState, policies, addACEs,
                removeACEs, extension);
        if (null == id) {
            throw new RuntimeException("createDocument failed.");
        }
        return id;
    }

    private String createDocumentInheritedProperties(String folderId, boolean withContent) {
        ContentStream contentStream = null;
        VersioningState versioningState = VersioningState.NONE;
        List<String> policies = null;
        Acl addACEs = null;
        Acl removeACEs = null;
        ExtensionsData extension = null;

        // create the properties:
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, MY_CUSTOM_NAME));
        properties.add(fFactory
                .createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, TEST_INHERITED_CUSTOM_DOCUMENT_TYPE_ID));
        // Generate some property values for custom attributes
        properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_MY_STRING_PROP_ID, "My pretty string"));
        properties.add(fFactory.createPropertyIntegerData(TEST_DOCUMENT_MY_INT_PROP_ID, BigInteger.valueOf(4711)));
        properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_MY_SUB_STRING_PROP_ID, "another cool string"));
        properties.add(fFactory.createPropertyIntegerData(TEST_DOCUMENT_MY_SUB_INT_PROP_ID, BigInteger.valueOf(4712)));

        Properties props = fFactory.createPropertiesData(properties);

        if (withContent) {
            contentStream = createContent();
        }

        // create the document
        String id = null;
        id = fObjSvc.createDocument(fRepositoryId, props, folderId, contentStream, versioningState, policies, addACEs,
                removeACEs, extension);
        if (null == id) {
            throw new RuntimeException("createDocument failed.");
        }
        return id;
    }

    private String getSourceFolder(String objectId) {
        // return the first parent found in the result list of all parents
        List<ObjectParentData> parents = fNavSvc.getObjectParents(fRepositoryId, objectId, "*", false,
                IncludeRelationships.NONE, null, true, null);
        return parents.get(0).getObject().getId();
    }

    // Helper class to create some type for testing the ObjectService

    public static class ObjectTestTypeSystemCreator implements TypeCreator {

        static final TypeDefinitionFactory typeFactory = DocumentTypeCreationHelper.getTypeDefinitionFactory();

        /**
         * create root types and a sample type for folder and document
         * 
         * @return typesMap map filled with created types
         */
        @Override
        public List<TypeDefinition> createTypesList() {
            List<TypeDefinition> typesList = new LinkedList<TypeDefinition>();

            try {
                MutableTypeDefinition cmisDocumentType;
                cmisDocumentType = typeFactory.createChildTypeDefinition(
                        DocumentTypeCreationHelper.getCmisDocumentType(), TEST_DOCUMENT_TYPE_ID);
                cmisDocumentType.setDisplayName("My Document Type");
                cmisDocumentType.setDescription("InMemory test type definition " + TEST_DOCUMENT_TYPE_ID);

                MutableFolderTypeDefinition cmisFolderType;
                cmisFolderType = typeFactory.createFolderTypeDefinition(CmisVersion.CMIS_1_1,
                        DocumentTypeCreationHelper.getCmisFolderType().getId());
                cmisFolderType.setId(TEST_FOLDER_TYPE_ID);
                cmisFolderType.setDisplayName("My Folder Type");
                cmisFolderType.setDescription("InMemory test type definition " + TEST_FOLDER_TYPE_ID);
                // create a simple string property type and
                // attach the property definition to the type definition for
                // document and folder type
                Map<String, PropertyDefinition<?>> propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();
                PropertyStringDefinitionImpl prop = PropertyCreationHelper.createStringDefinition(
                        TEST_DOCUMENT_STRING_PROP_ID, "Sample Doc String Property", Updatability.READWRITE);
                propertyDefinitions.put(prop.getId(), prop);
                cmisDocumentType.addPropertyDefinition(prop);

                propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();
                prop = PropertyCreationHelper.createStringDefinition(TEST_FOLDER_STRING_PROP_ID,
                        "Sample Folder String Property", Updatability.READWRITE);
                propertyDefinitions.put(prop.getId(), prop);
                cmisFolderType.addPropertyDefinition(prop);

                DocumentTypeDefinition customDocType = createCustomTypeWithStringIntProperty();
                TypeDefinition noContentType = createCustomTypeNoContent();
                TypeDefinition mustHaveContentType = createCustomTypeMustHaveContent();
                TypeDefinition relType = createRelationshipType();
                TypeDefinition relTypeRestricted = createRelationshipTypeRestricted();
                TypeDefinition verType = createVersionableType();
                TypeDefinition polType = createPolicyType();

                // add type to types collection
                typesList.add(cmisDocumentType);
                typesList.add(cmisFolderType);
                typesList.add(customDocType);
                typesList.add(noContentType);
                typesList.add(mustHaveContentType);
                typesList.add(createCustomInheritedType(customDocType));
                typesList.add(createDocumentTypeWithDefault());
                typesList.add(createFolderTypeWithDefault());
                typesList.add(createItemType());
                typesList.add(createSecondaryType());
                typesList.add(relType);
                typesList.add(relTypeRestricted);
                typesList.add(verType);
                typesList.add(polType);
                return typesList;
            } catch (Exception e) {
                throw new CmisRuntimeException("Failed to create types.", e);
            }
        }

        private static DocumentTypeDefinition createCustomTypeWithStringIntProperty() throws InstantiationException,
                IllegalAccessException {
            MutableDocumentTypeDefinition cmisDocumentType;
            cmisDocumentType = (MutableDocumentTypeDefinition) typeFactory.createChildTypeDefinition(
                    DocumentTypeCreationHelper.getCmisDocumentType(), TEST_CUSTOM_DOCUMENT_TYPE_ID);
            cmisDocumentType.setDisplayName("My Custom Document Type");
            cmisDocumentType.setDescription("InMemory test type definition " + TEST_CUSTOM_DOCUMENT_TYPE_ID);

            PropertyStringDefinitionImpl prop = PropertyCreationHelper.createStringDefinition(
                    TEST_DOCUMENT_MY_STRING_PROP_ID, "My String Property", Updatability.READWRITE);
            prop.setIsRequired(false);
            prop.setMaxLength(BigInteger.valueOf(20)); // max len to 20
            cmisDocumentType.addPropertyDefinition(prop);

            PropertyIntegerDefinitionImpl prop2 = PropertyCreationHelper.createIntegerDefinition(
                    TEST_DOCUMENT_MY_INT_PROP_ID, "My Integer Property", Updatability.READWRITE);
            prop2.setIsRequired(true);
            prop2.setMinValue(BigInteger.valueOf(-10000));
            prop2.setMaxValue(BigInteger.valueOf(10000));
            cmisDocumentType.addPropertyDefinition(prop2);
            return cmisDocumentType;
        }

        private static TypeDefinition createCustomInheritedType(DocumentTypeDefinition baseType)
                throws InstantiationException, IllegalAccessException {
            MutableTypeDefinition cmisDocumentType;
            cmisDocumentType = typeFactory.createChildTypeDefinition(baseType, TEST_INHERITED_CUSTOM_DOCUMENT_TYPE_ID);
            cmisDocumentType.setDisplayName("My Custom Document Type");
            cmisDocumentType.setDescription("InMemory test type definition " + TEST_INHERITED_CUSTOM_DOCUMENT_TYPE_ID);

            PropertyStringDefinitionImpl prop = PropertyCreationHelper.createStringDefinition(
                    TEST_DOCUMENT_MY_SUB_STRING_PROP_ID, "Subtype String Property", Updatability.READWRITE);
            prop.setIsRequired(false);
            cmisDocumentType.addPropertyDefinition(prop);

            PropertyIntegerDefinitionImpl prop2 = PropertyCreationHelper.createIntegerDefinition(
                    TEST_DOCUMENT_MY_SUB_INT_PROP_ID, "Subtype", Updatability.READWRITE);
            prop2.setIsRequired(true);
            cmisDocumentType.addPropertyDefinition(prop2);
            return cmisDocumentType;
        }

        @SuppressWarnings("serial")
        private static TypeDefinition createDocumentTypeWithDefault() throws InstantiationException,
                IllegalAccessException {
            MutableTypeDefinition cmisDocumentType;
            cmisDocumentType = typeFactory.createChildTypeDefinition(DocumentTypeCreationHelper.getCmisDocumentType(),
                    TEST_DOC_TYPE_WITH_DEFAULTS_ID);
            cmisDocumentType.setDisplayName("Document Type With default values");
            cmisDocumentType.setDescription("InMemory test type definition " + TEST_DOC_TYPE_WITH_DEFAULTS_ID);

            PropertyStringDefinitionImpl prop = PropertyCreationHelper.createStringMultiDefinition(
                    TEST_DOCUMENT_MY_MULTI_STRING_PROP_ID, "Test Multi String Property", Updatability.READWRITE);
            prop.setIsRequired(false);
            List<String> defValS = new ArrayList<String>() {
                {
                    add("Apache");
                    add("CMIS");
                }
            };
            prop.setDefaultValue(defValS);
            cmisDocumentType.addPropertyDefinition(prop);

            PropertyIntegerDefinitionImpl prop2 = PropertyCreationHelper.createIntegerDefinition(
                    TEST_DOCUMENT_MY_INT_PROP_ID, "Test Integer Property", Updatability.READWRITE);
            prop2.setIsRequired(false);
            List<BigInteger> defVal = new ArrayList<BigInteger>() {
                {
                    add(BigInteger.valueOf(100));
                }
            };
            prop2.setDefaultValue(defVal);
            cmisDocumentType.addPropertyDefinition(prop2);

            PropertyIntegerDefinitionImpl prop3 = PropertyCreationHelper.createIntegerDefinition(
                    TEST_DOCUMENT_MY_INT_PROP_ID_MANDATORY_DEFAULT, "Test Integer Property Mandatory default",
                    Updatability.READWRITE);
            prop3.setIsRequired(true);
            List<BigInteger> defVal2 = new ArrayList<BigInteger>() {
                {
                    add(BigInteger.valueOf(100));
                }
            };
            prop3.setDefaultValue(defVal2);
            cmisDocumentType.addPropertyDefinition(prop3);

            return cmisDocumentType;
        }

        private static TypeDefinition createCustomTypeNoContent() throws InstantiationException, IllegalAccessException {
            MutableDocumentTypeDefinition cmisDocumentType;
            cmisDocumentType = (MutableDocumentTypeDefinition) typeFactory.createChildTypeDefinition(
                    DocumentTypeCreationHelper.getCmisDocumentType(), TEST_CUSTOM_NO_CONTENT_TYPE_ID);
            cmisDocumentType.setDisplayName("No Content Document Type");
            cmisDocumentType.setDescription("InMemory test type definition " + TEST_CUSTOM_NO_CONTENT_TYPE_ID);
            cmisDocumentType.setContentStreamAllowed(ContentStreamAllowed.NOTALLOWED);
            return cmisDocumentType;
        }

        private static TypeDefinition createCustomTypeMustHaveContent() throws InstantiationException,
                IllegalAccessException {
            MutableDocumentTypeDefinition cmisDocumentType;
            cmisDocumentType = (MutableDocumentTypeDefinition) typeFactory.createChildTypeDefinition(
                    DocumentTypeCreationHelper.getCmisDocumentType(), TEST_CUSTOM_MUST_CONTENT_TYPE_ID);
            cmisDocumentType.setDisplayName("Must Have Content Document Type");
            cmisDocumentType.setDescription("InMemory test type definition " + TEST_CUSTOM_MUST_CONTENT_TYPE_ID);
            cmisDocumentType.setContentStreamAllowed(ContentStreamAllowed.NOTALLOWED);
            cmisDocumentType.setContentStreamAllowed(ContentStreamAllowed.REQUIRED);
            return cmisDocumentType;
        }

        @SuppressWarnings("serial")
        private static TypeDefinition createFolderTypeWithDefault() throws InstantiationException,
                IllegalAccessException {
            MutableFolderTypeDefinition cmisFolderType;
            cmisFolderType = typeFactory.createFolderTypeDefinition(CmisVersion.CMIS_1_1, DocumentTypeCreationHelper
                    .getCmisFolderType().getId());
            cmisFolderType.setId(TEST_FOLDER_TYPE_WITH_DEFAULTS_ID);
            cmisFolderType.setDisplayName("Folder Type With default values");
            cmisFolderType.setDescription("InMemory test type definition " + TEST_FOLDER_TYPE_WITH_DEFAULTS_ID);

            PropertyStringDefinitionImpl prop = PropertyCreationHelper.createStringMultiDefinition(
                    TEST_FOLDER_MY_MULTI_STRING_PROP_ID, "Test Multi String Property", Updatability.READWRITE);
            prop.setIsRequired(false);
            List<String> defValS = new ArrayList<String>() {
                {
                    add("Apache");
                    add("CMIS");
                }
            };
            prop.setDefaultValue(defValS);
            cmisFolderType.addPropertyDefinition(prop);

            PropertyIntegerDefinitionImpl prop2 = PropertyCreationHelper.createIntegerDefinition(
                    TEST_FOLDER_MY_INT_PROP_ID, "Test Integer Property", Updatability.READWRITE);
            prop2.setIsRequired(false);
            List<BigInteger> defVal = new ArrayList<BigInteger>() {
                {
                    add(BigInteger.valueOf(100));
                }
            };
            prop2.setDefaultValue(defVal);
            cmisFolderType.addPropertyDefinition(prop2);

            PropertyIntegerDefinitionImpl prop3 = PropertyCreationHelper.createIntegerDefinition(
                    TEST_FOLDER_MY_INT_PROP_ID_MANDATORY_DEFAULT, "Test Integer Property Mandatory default",
                    Updatability.READWRITE);
            prop3.setIsRequired(true);
            List<BigInteger> defVal2 = new ArrayList<BigInteger>() {
                {
                    add(BigInteger.valueOf(100));
                }
            };
            prop3.setDefaultValue(defVal2);
            cmisFolderType.addPropertyDefinition(prop3);

            return cmisFolderType;
        }

        private TypeDefinition createRelationshipType() throws InstantiationException, IllegalAccessException {
            MutableRelationshipTypeDefinition cmisRelType;
            cmisRelType = typeFactory.createRelationshipTypeDefinition(CmisVersion.CMIS_1_1, DocumentTypeCreationHelper
                    .getCmisRelationshipType().getId());
            cmisRelType.setId(TEST_RELATION_TYPE_ID);
            cmisRelType.setDisplayName("MyRelationshipType");
            cmisRelType.setDescription("InMemory test type definition " + TEST_RELATION_TYPE_ID);
            DocumentTypeCreationHelper.setDefaultTypeCapabilities(cmisRelType);
            cmisRelType.setIsFileable(false);

            // create a single String property definition

            PropertyStringDefinitionImpl prop1 = PropertyCreationHelper.createStringDefinition(REL_STRING_PROP,
                    "CrossReferenceType", Updatability.READWRITE);
            cmisRelType.addPropertyDefinition(prop1);

            return cmisRelType;
        }

        private TypeDefinition createRelationshipTypeRestricted() throws InstantiationException, IllegalAccessException {
            MutableRelationshipTypeDefinition cmisRelType;
            cmisRelType = typeFactory.createRelationshipTypeDefinition(CmisVersion.CMIS_1_1, DocumentTypeCreationHelper
                    .getCmisRelationshipType().getId());
            cmisRelType.setId(TEST_RESTRICTED_RELATION_TYPE_ID);
            cmisRelType.setDisplayName("RestrictedRelationshipType");
            cmisRelType.setDescription("InMemory test type definition " + TEST_RESTRICTED_RELATION_TYPE_ID);
            DocumentTypeCreationHelper.setDefaultTypeCapabilities(cmisRelType);
            cmisRelType.setIsFileable(false);

            List<String> allowedTypeIds = Collections.singletonList(TEST_CUSTOM_DOCUMENT_TYPE_ID);
            cmisRelType.setAllowedSourceTypes(allowedTypeIds);
            cmisRelType.setAllowedTargetTypes(allowedTypeIds);
            return cmisRelType;
        }

        private static TypeDefinition createItemType() throws InstantiationException, IllegalAccessException {
            // CMIS 1.1 create an item item type
            MutableItemTypeDefinition cmisItemType;
            cmisItemType = typeFactory.createItemTypeDefinition(CmisVersion.CMIS_1_1, DocumentTypeCreationHelper
                    .getCmisItemType().getId()); // ???
                                                 // DocumentTypeCreationHelper.getCmisItemType());
            cmisItemType.setId(TEST_ITEM_TYPE_ID);
            cmisItemType.setDisplayName("MyItemType");
            cmisItemType.setDescription("Builtin InMemory type definition " + TEST_ITEM_TYPE_ID);
            DocumentTypeCreationHelper.setDefaultTypeCapabilities(cmisItemType);

            // create a single String property definition
            PropertyStringDefinitionImpl prop1 = PropertyCreationHelper.createStringDefinition(ITEM_STRING_PROP,
                    "Item String Property", Updatability.READWRITE);
            cmisItemType.addPropertyDefinition(prop1);
            // add type to types collection
            return cmisItemType;
        }

        private static TypeDefinition createSecondaryType() throws InstantiationException, IllegalAccessException {
            // CMIS 1.1 create an item item type
            MutableSecondaryTypeDefinition cmisSecondaryType;
            cmisSecondaryType = typeFactory.createSecondaryTypeDefinition(CmisVersion.CMIS_1_1,
                    DocumentTypeCreationHelper.getCmisSecondaryType().getId());
            cmisSecondaryType.setId(TEST_SECONDARY_TYPE_ID);
            cmisSecondaryType.setDisplayName("MySecondaryType");
            cmisSecondaryType.setDescription("InMemory test type definition " + TEST_SECONDARY_TYPE_ID);
            DocumentTypeCreationHelper.setDefaultTypeCapabilities(cmisSecondaryType);
            cmisSecondaryType.setIsCreatable(false);
            cmisSecondaryType.setIsFileable(false);

            // create a single String property definition

            PropertyStringDefinitionImpl prop1 = PropertyCreationHelper.createStringDefinition(SECONDARY_STRING_PROP,
                    "Secondary String Property", Updatability.READWRITE);
            cmisSecondaryType.addPropertyDefinition(prop1);

            PropertyIntegerDefinitionImpl prop2 = PropertyCreationHelper.createIntegerDefinition(
                    SECONDARY_INTEGER_PROP, "Secondary Integer Property", Updatability.READWRITE);
            prop2.setIsRequired(true);
            cmisSecondaryType.addPropertyDefinition(prop2);

            return cmisSecondaryType;
        }

        private static TypeDefinition createVersionableType() throws InstantiationException, IllegalAccessException {
            // create a complex type with properties
            MutableDocumentTypeDefinition verType;
            verType = (MutableDocumentTypeDefinition) typeFactory.createChildTypeDefinition(
                    DocumentTypeCreationHelper.getCmisDocumentType(), TEST_VERSION_DOCUMENT_TYPE_ID);
            verType.setDisplayName("VersionedType");
            verType.setDescription("InMemory test type definition " + TEST_VERSION_DOCUMENT_TYPE_ID);

            verType.setIsVersionable(true); // make it a versionable type;

            // create a String property definition
            PropertyStringDefinitionImpl prop1 = PropertyCreationHelper.createStringDefinition(TEST_VER_PROPERTY_ID,
                    "Sample String Property", Updatability.WHENCHECKEDOUT);
            verType.addPropertyDefinition(prop1);
            return verType;
        }

        private static TypeDefinition createPolicyType() throws InstantiationException, IllegalAccessException {
            MutablePolicyTypeDefinition polType;
            polType = typeFactory.createPolicyTypeDefinition(CmisVersion.CMIS_1_1, DocumentTypeCreationHelper
                    .getCmisPolicyType().getId());
            polType.setId(TEST_POLICY_TYPE_ID);
            polType.setDisplayName("Audit Policy");
            polType.setDescription("InMemory type definition " + TEST_POLICY_TYPE_ID);
            DocumentTypeCreationHelper.setDefaultTypeCapabilities(polType);
            polType.setIsFileable(false);

            // create a String property definition
            PropertyStringDefinitionImpl prop1 = PropertyCreationHelper.createStringDefinition(TEST_POLICY_PROPERTY_ID,
                    "Audit Kind Property", Updatability.READWRITE);
            polType.addPropertyDefinition(prop1);
            return polType;
        }
    }

}
