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
package org.apache.opencmis.inmemory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.AllowableActionsData;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectInFolderList;
import org.apache.opencmis.commons.provider.ObjectParentData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.inmemory.types.InMemoryDocumentTypeDefinition;
import org.apache.opencmis.inmemory.types.InMemoryFolderTypeDefinition;
import org.apache.opencmis.inmemory.types.PropertyCreationHelper;
import org.apache.opencmis.util.repository.ObjectGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Jens
 */

public class ObjectServiceTest extends AbstractServiceTst {

  private static Log log = LogFactory.getLog(ObjectServiceTest.class);
  private static final String TEST_FOLDER_TYPE_ID = "MyFolderType";
  private static final String TEST_DOCUMENT_TYPE_ID = "MyDocumentType";
  private static final String TEST_FOLDER_STRING_PROP_ID = "MyFolderStringProp";
  private static final String TEST_DOCUMENT_STRING_PROP_ID = "MyDocumentStringProp";
  private static final String TEST_CUSTOM_DOCUMENT_TYPE_ID = "MyCustomDocumentType";
  private static final String TEST_INHERITED_CUSTOM_DOCUMENT_TYPE_ID ="MyCustomInheritedDocType";
  private static final String TEST_DOCUMENT_MY_STRING_PROP_ID  = "MyCustomDocumentStringProp";
  private static final String TEST_DOCUMENT_MY_INT_PROP_ID  = "MyCustomDocumentIntProp";
  private static final String TEST_DOCUMENT_MY_SUB_STRING_PROP_ID  = "MyInheritedStringProp";
  private static final String TEST_DOCUMENT_MY_SUB_INT_PROP_ID  = "MyInheritedIntProp";

  private static final String DOCUMENT_TYPE_ID =  InMemoryDocumentTypeDefinition.getRootDocumentType().getId();
  private static final String DOCUMENT_ID =  "Document_1";
  private static final String FOLDER_TYPE_ID =  InMemoryFolderTypeDefinition.getRootFolderType().getId();
  private static final String FOLDER_ID =  "Folder_1";
  private static final String MY_CUSTOM_NAME = "My Custom Document";
  
  ObjectCreator fCreator;
  
  @Before
  public void setUp() throws Exception {
    super.setTypeCreatorClass(ObjectTestTypeSystemCreator.class.getName());
    super.setUp();
    fCreator = new ObjectCreator(fFactory, fObjSvc, fRepositoryId);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  public void testCreateDocument() {
    log.info("starting testCreateObject() ...");
    String id = createDocument(fRootFolderId, false);
    if (id != null)
      log.info("createDocument succeeded with created id: " + id);
    log.info("... testCreateObject() finished.");
  }


  @Test
  public void testGetObject() {
    log.info("starting testGetObject() ...");
    log.info("  creating object");
    String id = createDocument(fRootFolderId, false);
    if (id != null)
      log.info("  createDocument succeeded with created id: " + id);

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
    if (id != null)
      log.info("createDocument succeeded with created id: " + id);
    
    ContentStreamData sd = fObjSvc.getContentStream(fRepositoryId, id, null, BigInteger.valueOf(-1) /* offset */,
        BigInteger.valueOf(-1) /* length */, null);
    verifyContentResult(sd);
    
    // delete content again
    Holder<String> idHolder = new Holder<String>(id);
    fObjSvc.deleteContentStream(fRepositoryId, idHolder, null, null);
    sd = fObjSvc.getContentStream(fRepositoryId, id, null, BigInteger.valueOf(-1) /* offset */,
        BigInteger.valueOf(-1) /* length */, null);
    assertNull(sd);
    
    //create content again in a second call
    ContentStreamData contentStream = createContent();
    fObjSvc.setContentStream(fRepositoryId, idHolder, true, null, contentStream, null);
    sd = fObjSvc.getContentStream(fRepositoryId, id, null, BigInteger.valueOf(-1) /* offset */,
        BigInteger.valueOf(-1) /* length */, null);
    verifyContentResult(sd);
    
    // update content and do not set overwrite flag, expect failure
    try {
      fObjSvc.setContentStream(fRepositoryId, idHolder, false, null, contentStream, null);
      fail ("setContentStream with existing content and no overWriteFlag should fail");
    } catch (Exception e) {
      assertTrue (e instanceof CmisConstraintException);
    }

    // cleanup
    fObjSvc.deleteObject(fRepositoryId, id, true, null);
    
    log.info("... testCreateDocumentWithContent() finished.");
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
      PropertiesData props = createDocumentPropertiesForDocumentFromSource("Document From Source");
      id2 = fObjSvc.createDocumentFromSource(fRepositoryId, id1, props, fRootFolderId, versioningState,
          null, null, null, null);
      if (null == id2)
        fail("createDocumentFromSource failed.");
    } catch (Exception e) {
      fail("createDocumentFromSource() failed with exception: " + e);
    }

    // get content from second document and compare it with original one
    ContentStreamData sd = fObjSvc.getContentStream(fRepositoryId, id2, null, BigInteger.valueOf(-1) /* offset */,
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
    if (id != null)
      log.info("  createDocument succeeded with created id: " + id);

    log.info("  getting object");
    try {
      ObjectData res = fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE,
          null, false, false, null);
      assertNotNull(res);

      String returnedId = res.getId();
      assertEquals(id, returnedId); 
      Map<String, PropertyData<?>> props = res.getProperties().getProperties();
      for (PropertyData<?> pd : props.values()) {
        log.info("return property id: " + pd.getId() + ", value: " + pd.getValues());
      }

      PropertyData<?> pd = props.get(PropertyIds.CMIS_NAME);
      assertNotNull(pd);
      assertEquals(MY_CUSTOM_NAME, pd.getFirstValue());
      
      pd = props.get(PropertyIds.CMIS_OBJECT_TYPE_ID);
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

    ObjectGenerator gen = new ObjectGenerator(fFactory, fNavSvc, fObjSvc, fRepositoryId);
    int levels = 2;  // create a hierarchy with two levels
    int childrenPerLevel = 2; // create two folders on each level

    gen.setNumberOfDocumentsToCreatePerFolder(1); // create one document in each folder

    // Set the type id for all created documents:
    gen.setDocumentTypeId(TEST_DOCUMENT_TYPE_ID);
    
    // Set the type id for all created folders:
    gen.setFolderTypeId(TEST_FOLDER_TYPE_ID);
    
    // set the properties the generator should fill with values for documents:
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
    if (id != null)
      log.info("  createDocument succeeded with created id: " + id);

    log.info("  getting object");
    retrieveDocument(id);    
    log.info("  deleting object");
    try {
      fObjSvc.deleteObject(fRepositoryId, id, true, null);
    }
    catch (Exception e) {
      fail("deleteObject() for document failed with exception: " + e);
    }    
    
    // check that it does not exist anymore
    try {
      fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE,
          null, false, false, null);
      fail("object should not longer exist after it was deleted.");
    }
    catch (CmisObjectNotFoundException e) {
      assertTrue(e instanceof CmisObjectNotFoundException);
    }    
    catch (Exception e) {
      fail("getting deleted object should raise CMISObjectNotFoundException, but got " + e);
    }    
      
    log.info("Testing to delete an empty folder");
    // create and delete an empty folder
    id = createFolder();
    try {
      fObjSvc.deleteObject(fRepositoryId, id, true, null);
    }
    catch (Exception e) {
      fail("deleteObject() for folder failed with exception: " + e);
    }    
    // check that it does not exist anymore
    try {
      fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE,
          null, false, false, null);
      fail("object should not longer exist after it was deleted.");
    }
    catch (CmisObjectNotFoundException e) {
      assertTrue(e instanceof CmisObjectNotFoundException);
    }    
    catch (Exception e) {
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
    }
    catch (Exception e) {
      assertTrue(e instanceof CmisConstraintException);
    }    
    // should succeed if we first delete document then folder
    try {
      fObjSvc.deleteObject(fRepositoryId, id, true, null);
      fObjSvc.deleteObject(fRepositoryId, folderId, true, null);
    }
    catch (Exception e) {
      fail("deleteObject() for document and folder failed with exception: " + e);
    }    
    // check that it does not exist anymore
    try {
      fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE,
          null, false, false, null);
      fail("object should not longer exist after it was deleted.");
    }
    catch (CmisObjectNotFoundException e) {
      assertTrue(e instanceof CmisObjectNotFoundException);
    }    
    catch (Exception e) {
      fail("getting deleted object should raise CMISObjectNotFoundException, but got " + e);
    }    
    try {
      fObjSvc.getObject(fRepositoryId, folderId, "*", false, IncludeRelationships.NONE,
          null, false, false, null);
      fail("object should not longer exist after it was deleted.");
    }
    catch (CmisObjectNotFoundException e) {
      assertTrue(e instanceof CmisObjectNotFoundException);
    } catch (Exception e) {
      fail("getting deleted object should raise CMISObjectNotFoundException, but got " + e);
    }    
    log.info("... testDeleteObject() finished.");    
  }
  
  @Test
  public void testDeleteTree() {
    log.info("starting testDeleteTree() ...");
    ObjectGenerator gen = new ObjectGenerator(fFactory, fNavSvc, fObjSvc, fRepositoryId);
    String rootFolderId = createFolder();
    // Set the type id for all created documents:
    gen.setDocumentTypeId(InMemoryDocumentTypeDefinition.getRootDocumentType().getId());    
    // Set the type id for all created folders:
    gen.setFolderTypeId(InMemoryFolderTypeDefinition.getRootFolderType().getId());
    gen.setNumberOfDocumentsToCreatePerFolder(2); // create two documents in each folder    
    gen.createFolderHierachy(1, 1, rootFolderId);
    try {
          fObjSvc.deleteTree(fRepositoryId, rootFolderId, null /*true*/, UnfileObjects.DELETE, true, null);
    } catch (Exception e) {
      fail("deleteTree failed unexpected. " + e);
    }    
    log.info("Dumping folder, should only contain one empty folder under root");
    gen.dumpFolder(fRootFolderId, "*");
    
    // After that we should be not be able to get the root folder, because it should be deleted
    try {
      fObjSvc.getObject(fRepositoryId, rootFolderId, "*", false, IncludeRelationships.NONE,
          null, false, false, null);
      fail("object should not longer exist after it was deleted.");
    }
    catch (CmisObjectNotFoundException e) {
      assertTrue(e instanceof CmisObjectNotFoundException);
    }    
    catch (Exception e) {
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
    log.info("starting testUpdateProperties() ...");
    String oldChangeToken, newChangeToken;
    String id = createDocumentWithCustomType(fRootFolderId, false);
    if (id != null)
      log.info("createDocument succeeded with created id: " + id);
    
    log.info("  getting object");
    try {
      ObjectData res = fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE,
          null, false, false, null);
      assertNotNull(res);
      Map<String, PropertyData<?>> props = res.getProperties().getProperties();
      
      // check returned properties
      for (PropertyData<?> pd : props.values()) {
        log.info("  return property id: " + pd.getId() + ", value: " + pd.getValues());
      }
      
      String returnedId = res.getId();
      assertEquals(id, returnedId);    
      PropertyData<?> pd = props.get(PropertyIds.CMIS_NAME);
      assertNotNull(pd);
      assertEquals(MY_CUSTOM_NAME, pd.getFirstValue());
      pd = props.get(PropertyIds.CMIS_OBJECT_TYPE_ID);
      assertEquals(TEST_CUSTOM_DOCUMENT_TYPE_ID, pd.getFirstValue());
      pd = props.get(TEST_DOCUMENT_MY_STRING_PROP_ID);
      assertEquals("My pretty string", pd.getFirstValue());
      pd = props.get(TEST_DOCUMENT_MY_INT_PROP_ID);
      assertEquals(BigInteger.valueOf(4711), pd.getFirstValue());
      
      // update properties:
      log.info("updating property");
      final String newStringPropVal = "My ugly string";
      final BigInteger newIntPropVal = BigInteger.valueOf(815);        
      List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
//      properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_NAME, MY_CUSTOM_NAME));
//      properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_OBJECT_TYPE_ID, TEST_CUSTOM_DOCUMENT_TYPE_ID));
      // Generate some property values for custom attributes
      properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_MY_STRING_PROP_ID, newStringPropVal));      
      properties.add(fFactory.createPropertyIntegerData(TEST_DOCUMENT_MY_INT_PROP_ID, newIntPropVal));     
      PropertiesData newProps = fFactory.createPropertiesData(properties);

      Holder<String> idHolder = new Holder<String>(id);
      Holder<String> changeTokenHolder = new Holder<String>();
      fObjSvc.updateProperties(fRepositoryId, idHolder, changeTokenHolder, newProps, null);
      oldChangeToken = changeTokenHolder.getValue(); // store for later use
      // check if we now retrieve new values
      res = fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE,
          null, false, false, null);
      assertNotNull(res);
      props = res.getProperties().getProperties();
      for (PropertyData<?> pd2 : props.values()) {
        log.info("  return property id: " + pd2.getId() + ", value: " + pd2.getValues());
      }     
      returnedId = res.getId();
      assertEquals(id, returnedId);    
      pd = props.get(PropertyIds.CMIS_NAME);
      assertNotNull(pd);
      assertEquals(MY_CUSTOM_NAME, pd.getFirstValue());
      pd = props.get(PropertyIds.CMIS_OBJECT_TYPE_ID);
      assertEquals(TEST_CUSTOM_DOCUMENT_TYPE_ID, pd.getFirstValue());
      pd = props.get(TEST_DOCUMENT_MY_STRING_PROP_ID);
      assertEquals(newStringPropVal, pd.getFirstValue());
      pd = props.get(TEST_DOCUMENT_MY_INT_PROP_ID);
      assertEquals(newIntPropVal, pd.getFirstValue());
      
      // Test delete properties
      log.info("deleting property");
      properties = new ArrayList<PropertyData<?>>();
      properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_MY_STRING_PROP_ID, (String)null));      
      newProps = fFactory.createPropertiesData(properties);
      Thread.sleep(100); // ensure new change token, timer resolution is not good enough
      fObjSvc.updateProperties(fRepositoryId, idHolder, changeTokenHolder, newProps, null);
      res = fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE,
          null, false, false, null);
      assertNotNull(res);
      props = res.getProperties().getProperties();
      for (PropertyData<?> pd2 : props.values()) {
        log.info("  return property id: " + pd2.getId() + ", value: " + pd2.getValues());
      }     
      pd = props.get(TEST_DOCUMENT_MY_STRING_PROP_ID);
      assertNull(pd);
      // delete a required property and expect exception:
      properties = new ArrayList<PropertyData<?>>();
      properties.add(fFactory.createPropertyIntegerData(TEST_DOCUMENT_MY_INT_PROP_ID, (BigInteger)null));      
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
      properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_MY_STRING_PROP_ID, "A very long String ABCDEFHIJKLMNOPQRSTUVWXYZ"));      
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
      properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_NAME, newName));      
      newProps = fFactory.createPropertiesData(properties);
      changeTokenHolder.setValue(newChangeToken);
      fObjSvc.updateProperties(fRepositoryId, idHolder, changeTokenHolder, newProps, null);
      id = idHolder.getValue(); // note that id is path and has changed!
      res = fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE,
          null, false, false, null);
      assertNotNull(res);
      props = res.getProperties().getProperties();
      pd = props.get(PropertyIds.CMIS_NAME);
      assertNotNull(pd);
      assertEquals(newName, pd.getFirstValue());
      
      // test rename with a conflicting name
      createDocumentWithCustomType(fRootFolderId, false);
      properties = new ArrayList<PropertyData<?>>();
      properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_NAME, MY_CUSTOM_NAME));      
      newProps = fFactory.createPropertiesData(properties);
      // now rename to old name
      try {
        fObjSvc.updateProperties(fRepositoryId, idHolder, changeTokenHolder, newProps, null);
        fail("Update with a conflicting name should fail.");
      } catch (Exception e) {
        assertTrue(e instanceof CmisConstraintException);
      }
      
    } catch (Exception e) {
      fail("getObject() failed with exception: " + e);
    }    
    log.info("... testUpdateProperties() finished.");
  }

  @Test
  public void testAllowableActions() {
    log.info("starting testAllowableActions() ...");
    String id = createDocument(fRootFolderId, false);
    
    // get allowable actions via getObject
    ObjectData res = fObjSvc.getObject(fRepositoryId, id, "*", true, IncludeRelationships.NONE,
        null, false, false, null);
    assertNotNull(res.getAllowableActions());
    Map<String, Boolean> actions = res.getAllowableActions().getAllowableActions();
    assertNotNull(actions);
    verifyAllowableActions(actions);
    
    // get allowable actions via getAllowableActions
    AllowableActionsData allowableActions = fObjSvc.getAllowableActions(fRepositoryId, id, null);
    assertNotNull(allowableActions);
    actions = allowableActions.getAllowableActions();
    assertNotNull(actions);
    verifyAllowableActions(actions);

    // cleanup
    fObjSvc.deleteObject(fRepositoryId, id, true, null);
    log.info("... testAllowableActions() finished.");    
  }
  
  private void verifyAllowableActions(Map<String, Boolean> actions) {
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_DELETE_OBJECT));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_UPDATE_PROPERTIES));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_GET_PROPERTIES));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_GET_OBJECT_RELATIONSHIPS));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_GET_OBJECT_PARENTS));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_GET_FOLDER_PARENT));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_GET_FOLDER_TREE));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_GET_DESCENDANTS));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_MOVE_OBJECT));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_DELETE_CONTENT_STREAM));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_CHECK_OUT));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_CANCEL_CHECK_OUT));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_CHECK_IN));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_SET_CONTENT_STREAM));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_GET_ALL_VERSIONS));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_ADD_OBJECT_TO_FOLDER));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_REMOVE_OBJECT_FROM_FOLDER));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_GET_CONTENT_STREAM));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_APPLY_POLICY));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_GET_APPLIED_POLICIES));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_REMOVE_POLICY));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_GET_CHILDREN));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_CREATE_DOCUMENT));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_CREATE_FOLDER));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_CREATE_RELATIONSHIP));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_CREATE_POLICY));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_DELETE_TREE));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_GET_RENDITIONS));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_GET_ACL));
    assertNotNull(actions.get(AllowableActionsData.ACTION_CAN_APPLY_ACL));
  }
  
  private String retrieveDocument(String id) {
    ObjectData res = getDocumentObjectData(id);
    String returnedId = res.getId();
    testReturnedProperties(returnedId, DOCUMENT_ID, DOCUMENT_TYPE_ID, res.getProperties().getProperties());
    return returnedId;
  }
  
  private void moveObjectTest(boolean isFolder) {
    final String propertyFilter=PropertyIds.CMIS_OBJECT_ID+","+PropertyIds.CMIS_NAME; //+","+PropertyIds.CMIS_OBJECT_TYPE_ID+","+PropertyIds.CMIS_BASE_TYPE_ID;
    String rootFolderId = createFolder();
    ObjectGenerator gen = new ObjectGenerator(fFactory, fNavSvc, fObjSvc, fRepositoryId);
    // Set the type id for all created documents:
    gen.setDocumentTypeId(InMemoryDocumentTypeDefinition.getRootDocumentType().getId());    
    // Set the type id for all created folders:
    gen.setNumberOfDocumentsToCreatePerFolder(1); // create one document in each folder    
    gen.createFolderHierachy(3, 2, rootFolderId);
    gen.setFolderTypeId(InMemoryFolderTypeDefinition.getRootFolderType().getId());
    gen.dumpFolder(fRootFolderId, propertyFilter);
    Holder<String> holder = new Holder<String>();
    String sourceIdToMove = gen.getFolderId(rootFolderId, 2, 1);
    if (!isFolder) // get first document in this folder
      sourceIdToMove = gen.getDocumentId(sourceIdToMove, 0);
    holder.setValue(sourceIdToMove); // "/Folder_1/My Folder 0/My Folder 1");
    String sourceFolderId = getSourceFolder(sourceIdToMove);
    log.info("Id before moveObject: " + holder.getValue());
    fObjSvc.moveObject(fRepositoryId, holder, rootFolderId, sourceFolderId, null);
    log.info("Id after moveObject: " + holder.getValue());
    gen.dumpFolder(fRootFolderId, propertyFilter);

    List<ObjectParentData> result = fNavSvc.getObjectParents(fRepositoryId, holder.getValue(), null, Boolean.FALSE, IncludeRelationships.NONE, null, Boolean.FALSE,  null);
    // check that new parent is set correctly
    String newParentId =result.get(0).getObject().getId();
    assertEquals(rootFolderId, newParentId);
    
    if (isFolder) {
      log.info("testing moveFolder to a subfolder");
      ObjectInFolderList ch = fNavSvc.getChildren(fRepositoryId, holder.getValue(), propertyFilter, null,
          false, IncludeRelationships.NONE, null, false, null, null, null);
      String subFolderId = ch.getObjects().get(0).getObject().getId();
      
      try {
        fObjSvc.moveObject(fRepositoryId, holder, subFolderId, sourceFolderId, null);
        fail("moveObject to a folder that is a descendant of the source must fail.");
      } catch (Exception e) {
        assertTrue(e instanceof CmisNotSupportedException );
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

  private PropertiesData createDocumentPropertiesForDocumentFromSource(String name) {
    // We only provide a name but not a type id, as spec says to copy missing attributes 
    // from the existing one
    List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
    properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_NAME, name));
    PropertiesData props = fFactory.createPropertiesData(properties);
    return props;
  }

  
  private void testReturnedProperties(String objectId, String objectName, String typeId, Map<String, PropertyData<?>> props) {
    super.testReturnedProperties(objectId, props);
    
    PropertyData<?> pd = props.get(PropertyIds.CMIS_NAME);
    assertNotNull(pd);
    assertEquals(objectName, pd.getFirstValue());
    pd = props.get(PropertyIds.CMIS_OBJECT_TYPE_ID);
    assertEquals(typeId, pd.getFirstValue());
  }
 
  private String createDocumentWithCustomType(String folderId, boolean withContent) {
    ContentStreamData contentStream = null;
    VersioningState versioningState = VersioningState.NONE;
    List<String> policies = null;
    AccessControlList addACEs = null;
    AccessControlList removeACEs = null;
    ExtensionsData extension = null;

    // create the properties:
    List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
    properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_NAME, MY_CUSTOM_NAME));
    properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_OBJECT_TYPE_ID, TEST_CUSTOM_DOCUMENT_TYPE_ID));
    // Generate some property values for custom attributes
    properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_MY_STRING_PROP_ID, "My pretty string"));      
    properties.add(fFactory.createPropertyIntegerData(TEST_DOCUMENT_MY_INT_PROP_ID, BigInteger.valueOf(4711)));      

    PropertiesData props = fFactory.createPropertiesData(properties);
    
    if (withContent)
      contentStream = createContent();
    
    // create the document 
    String id = null;
    id = fObjSvc.createDocument(fRepositoryId, props, folderId, contentStream, versioningState,
        policies, addACEs, removeACEs, extension);
    if (null == id)
      throw new RuntimeException("createDocument failed.");
    return id;
  }
  
  private String createDocumentInheritedProperties(String folderId, boolean withContent) {
    ContentStreamData contentStream = null;
    VersioningState versioningState = VersioningState.NONE;
    List<String> policies = null;
    AccessControlList addACEs = null;
    AccessControlList removeACEs = null;
    ExtensionsData extension = null;

    // create the properties:
    List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
    properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_NAME, MY_CUSTOM_NAME));
    properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_OBJECT_TYPE_ID, TEST_INHERITED_CUSTOM_DOCUMENT_TYPE_ID));
    // Generate some property values for custom attributes
    properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_MY_STRING_PROP_ID, "My pretty string"));      
    properties.add(fFactory.createPropertyIntegerData(TEST_DOCUMENT_MY_INT_PROP_ID, BigInteger.valueOf(4711)));      
    properties.add(fFactory.createPropertyStringData(TEST_DOCUMENT_MY_SUB_STRING_PROP_ID, "another cool string"));
    properties.add(fFactory.createPropertyIntegerData(TEST_DOCUMENT_MY_SUB_INT_PROP_ID, BigInteger.valueOf(4712)));
        
    PropertiesData props = fFactory.createPropertiesData(properties);
    
    if (withContent)
      contentStream = createContent();
    
    // create the document 
    String id = null;
    id = fObjSvc.createDocument(fRepositoryId, props, folderId, contentStream, versioningState,
        policies, addACEs, removeACEs, extension);
    if (null == id)
      throw new RuntimeException("createDocument failed.");
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

    /**
     * create root types and a sample type for folder and document
     * 
     * @return typesMap map filled with created types
     */
    public List<TypeDefinition> createTypesList() {
      List<TypeDefinition> typesList = new LinkedList<TypeDefinition>();
      InMemoryDocumentTypeDefinition cmisDocumentType = new InMemoryDocumentTypeDefinition(TEST_DOCUMENT_TYPE_ID,
          "My Document Type", InMemoryDocumentTypeDefinition.getRootDocumentType());

      InMemoryFolderTypeDefinition cmisFolderType = new InMemoryFolderTypeDefinition(TEST_FOLDER_TYPE_ID,
          "My Folder Type", InMemoryFolderTypeDefinition.getRootFolderType());
      // create a simple string property type and
      // attach the property definition to the type definition for document and folder type
      Map<String, PropertyDefinition<?>> propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();
      PropertyStringDefinitionImpl prop = PropertyCreationHelper.createStringDefinition(
          TEST_DOCUMENT_STRING_PROP_ID, "Sample Doc String Property");
      propertyDefinitions.put(prop.getId(), prop);
      cmisDocumentType.addCustomPropertyDefinitions(propertyDefinitions);

      propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();
      prop = PropertyCreationHelper.createStringDefinition(TEST_FOLDER_STRING_PROP_ID, "Sample Folder String Property");
      propertyDefinitions.put(prop.getId(), prop);
      cmisFolderType.addCustomPropertyDefinitions(propertyDefinitions);

      InMemoryDocumentTypeDefinition customDocType = createCustomTypeWithStringIntProperty();
      // add type to types collection
      typesList.add(cmisDocumentType);
      typesList.add(cmisFolderType);
      typesList.add(customDocType);
      typesList.add(createCustomInheritedType(customDocType));
      return typesList;
    }
    
    private static InMemoryDocumentTypeDefinition createCustomTypeWithStringIntProperty() {
      InMemoryDocumentTypeDefinition cmisDocumentType = new InMemoryDocumentTypeDefinition(TEST_CUSTOM_DOCUMENT_TYPE_ID,
          "My Custom Document Type", InMemoryDocumentTypeDefinition.getRootDocumentType());
      Map<String, PropertyDefinition<?>> propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();
      PropertyStringDefinitionImpl prop = PropertyCreationHelper.createStringDefinition(
          TEST_DOCUMENT_MY_STRING_PROP_ID, "My String Property");
      prop.setIsRequired(false);
      prop.setMaxLength(BigInteger.valueOf(20)); // max len to 20
      propertyDefinitions.put(prop.getId(), prop);

      PropertyIntegerDefinitionImpl prop2 = PropertyCreationHelper.createIntegerDefinition(
          TEST_DOCUMENT_MY_INT_PROP_ID, "My Integer Property");
      prop2.setIsRequired(true);
      prop2.setMinValue(BigInteger.valueOf(-10000));
      prop2.setMaxValue(BigInteger.valueOf(10000));
      propertyDefinitions.put(prop2.getId(), prop2);
      cmisDocumentType.addCustomPropertyDefinitions(propertyDefinitions);
      return cmisDocumentType;
    }
    
    private static InMemoryDocumentTypeDefinition createCustomInheritedType(InMemoryDocumentTypeDefinition baseType) {
      InMemoryDocumentTypeDefinition cmisDocumentType = new InMemoryDocumentTypeDefinition(TEST_INHERITED_CUSTOM_DOCUMENT_TYPE_ID,
          "My Custom Document Type", baseType);
      Map<String, PropertyDefinition<?>> propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();
      PropertyStringDefinitionImpl prop = PropertyCreationHelper.createStringDefinition(
          TEST_DOCUMENT_MY_SUB_STRING_PROP_ID, "Subtype String Property");
      prop.setIsRequired(false);
      propertyDefinitions.put(prop.getId(), prop);

      PropertyIntegerDefinitionImpl prop2 = PropertyCreationHelper.createIntegerDefinition(
          TEST_DOCUMENT_MY_SUB_INT_PROP_ID, "Subtype");
      prop2.setIsRequired(true);
      propertyDefinitions.put(prop2.getId(), prop2);
      cmisDocumentType.addCustomPropertyDefinitions(propertyDefinitions);
      return cmisDocumentType;
    }
    
  }

}
