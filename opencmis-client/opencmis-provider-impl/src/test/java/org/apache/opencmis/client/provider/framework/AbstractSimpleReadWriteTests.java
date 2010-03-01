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
package org.apache.opencmis.client.provider.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.opencmis.commons.provider.AccessControlEntry;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;

/**
 * Simple read-write test.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public abstract class AbstractSimpleReadWriteTests extends AbstractCmisTestCase {

  public static final String TEST_CREATE_FOLDER = "createFolder";
  public static final String TEST_CREATE_DOCUMENT = "createDocument";
  public static final String TEST_CREATE_FROM_SOURCE = "createDocumentFromSource";
  public static final String TEST_SET_AND_DELETE_CONTENT = "setAndDeleteContent";
  public static final String TEST_UPDATE_PROPERTIES = "updateProperties";
  public static final String TEST_DELETE_TREE = "deleteTree";
  public static final String TEST_MOVE_OBJECT = "moveObject";
  public static final String TEST_VERSIONING = "versioning";

  private static final byte[] CONTENT = "My document test content!".getBytes();
  private static final byte[] CONTENT2 = "Another test content!".getBytes();
  private static final String CONTENT_TYPE = "text/plain";

  /**
   * Tests folder creation.
   */
  public void testCreateFolder() throws Exception {
    if (!isEnabled(TEST_CREATE_FOLDER)) {
      return;
    }

    // create folder
    List<PropertyData<?>> propList = new ArrayList<PropertyData<?>>();
    propList.add(getObjectFactory().createPropertyStringData(PropertyIds.CMIS_NAME, "testfolder"));
    propList.add(getObjectFactory().createPropertyIdData(PropertyIds.CMIS_OBJECT_TYPE_ID,
        getDefaultFolderType()));

    PropertiesData properties = getObjectFactory().createPropertiesData(propList);

    String folderId = createFolder(properties, getTestRootFolder(), null, null, null);

    // delete folder
    delete(folderId, true);
  }

  /**
   * Tests document creation.
   */
  public void testCreateDocument() throws Exception {
    if (!isEnabled(TEST_CREATE_DOCUMENT)) {
      return;
    }

    VersioningState vs = (isVersionable(getDefaultDocumentType()) ? VersioningState.MAJOR
        : VersioningState.NONE);

    // create document
    List<PropertyData<?>> propList = new ArrayList<PropertyData<?>>();
    propList.add(getObjectFactory().createPropertyStringData(PropertyIds.CMIS_NAME, "testdoc.txt"));
    propList.add(getObjectFactory().createPropertyIdData(PropertyIds.CMIS_OBJECT_TYPE_ID,
        getDefaultDocumentType()));

    PropertiesData properties = getObjectFactory().createPropertiesData(propList);

    ContentStreamData contentStream = createContentStreamData(CONTENT_TYPE, CONTENT);

    String docId = createDocument(properties, getTestRootFolder(), contentStream, vs, null, null,
        null);

    // read and assert content
    ContentStreamData contentStream2 = getContent(docId, null);
    assertMimeType(CONTENT_TYPE, contentStream2.getMimeType());
    if (contentStream2.getLength() != null) {
      assertEquals(CONTENT.length, contentStream2.getLength().intValue());
    }

    byte[] content = readContent(contentStream2);
    assertContent(CONTENT, content);

    // apply an ACL
    if (supportsManageACLs()) {
      AccessControlEntry ace = getObjectFactory().createAccessControlEntry(getUsername(),
          Collections.singletonList("cmis:read"));
      AccessControlList acl = getObjectFactory().createAccessControlList(
          Collections.singletonList(ace));

      AccessControlList newAcl = getProvider().getAclService().applyAcl(getTestRepositoryId(),
          docId, acl, null, getAclPropagation(), null);
      assertNotNull(newAcl);

      AccessControlList readAcl = getProvider().getAclService().getAcl(getTestRepositoryId(),
          docId, Boolean.FALSE, null);
      assertNotNull(readAcl);

      assertEquals(newAcl, readAcl);
    }
    else {
      warning("ACLs management not supported!");
    }

    // delete document
    delete(docId, true);
  }

  /**
   * Tests document creation from source.
   */
  public void testCreateDocumentFromSource() throws Exception {
    if (!isEnabled(TEST_CREATE_FROM_SOURCE)) {
      return;
    }

    VersioningState vs = (isVersionable(getDefaultDocumentType()) ? VersioningState.MAJOR
        : VersioningState.NONE);

    String docId = createDefaultDocument(getTestRootFolder(), "testdoc.org.txt", CONTENT_TYPE,
        CONTENT);

    // create a copy
    List<PropertyData<?>> propList2 = new ArrayList<PropertyData<?>>();
    propList2.add(getObjectFactory().createPropertyStringData(PropertyIds.CMIS_NAME,
        "testdoc.copy.txt"));

    PropertiesData properties2 = getObjectFactory().createPropertiesData(propList2);

    String docId2 = createDocumentFromSource(docId, properties2, getTestRootFolder(), vs, null,
        null, null);

    // get objects
    getObject(docId);
    getObject(docId2);

    // read and assert content
    ContentStreamData contentStream2 = getContent(docId, null);
    ContentStreamData contentStream3 = getContent(docId2, null);

    assertEquals(contentStream2.getMimeType(), contentStream3.getMimeType());
    assertEquals(contentStream2.getLength(), contentStream3.getLength());

    byte[] content2 = readContent(contentStream2);
    byte[] content3 = readContent(contentStream3);
    assertContent(content2, content3);

    // delete documents
    delete(docId, true);
    delete(docId2, true);
  }

  /**
   * Tests property updates.
   */
  public void testSetAndDeleteContent() throws Exception {
    if (!isEnabled(TEST_SET_AND_DELETE_CONTENT)) {
      return;
    }

    boolean requiresCheckOut = getRepositoryInfo().getRepositoryCapabilities()
        .getCapabilityContentStreamUpdatability() == CapabilityContentStreamUpdates.PWCONLY;

    boolean isVersionable = isVersionable(getDefaultDocumentType());

    String docId = createDefaultDocument(getTestRootFolder(), "testcontent.txt", CONTENT_TYPE,
        CONTENT);

    // if a check out is required, do it
    if (requiresCheckOut) {
      if (isVersionable) {
        getProvider().getVersioningService().checkOut(getTestRepositoryId(),
            new Holder<String>(docId), null, null);
      }
      else {
        warning("Default document type is not versionable!");
        delete(docId, true);
        return;
      }
    }

    // delete content
    Holder<String> docIdHolder = new Holder<String>(docId);
    try {
      getProvider().getObjectService().deleteContentStream(getTestRepositoryId(), docIdHolder,
          null, null);
    }
    catch (CmisNotSupportedException e) {
      warning("deleteContentStream not supported!");
    }

    // set content
    ContentStreamData contentStream2 = createContentStreamData(CONTENT_TYPE, CONTENT2);

    docIdHolder = new Holder<String>(docId);
    getProvider().getObjectService().setContentStream(getTestRepositoryId(), docIdHolder, true,
        null, contentStream2, null);

    // read and assert content
    if (docIdHolder.getValue() != null) {
      docId = docIdHolder.getValue();
    }
    ContentStreamData contentStream3 = getContent(docId, null);
    assertMimeType(CONTENT_TYPE, contentStream3.getMimeType());
    if (contentStream3.getLength() != null) {
      assertEquals(CONTENT2.length, contentStream3.getLength().intValue());
    }

    byte[] content = readContent(contentStream3);
    assertContent(CONTENT2, content);

    // if it has been checked out, cancel that
    if (requiresCheckOut) {
      getProvider().getVersioningService().cancelCheckOut(getTestRepositoryId(), docId, null);
    }

    // delete document
    delete(docId, true);
  }

  /**
   * Tests property updates.
   */
  public void testUpdateProperties() throws Exception {
    if (!isEnabled(TEST_UPDATE_PROPERTIES)) {
      return;
    }

    String name1 = "updateTest1.txt";
    String name2 = "updateTest2.txt";

    // create document
    String docId = createDefaultDocument(getTestRootFolder(), name1, CONTENT_TYPE, CONTENT);

    // update
    List<PropertyData<?>> updatePropList = new ArrayList<PropertyData<?>>();
    updatePropList.add(getObjectFactory().createPropertyStringData(PropertyIds.CMIS_NAME, name2));

    PropertiesData updateProperties = getObjectFactory().createPropertiesData(updatePropList);

    Holder<String> docIdHolder = new Holder<String>(docId);
    getProvider().getObjectService().updateProperties(getTestRepositoryId(), docIdHolder, null,
        updateProperties, null);

    // get new id and check name property
    docId = docIdHolder.getValue();

    ObjectData updatedObject = getObject(docId);
    String updatedName = (String) updatedObject.getProperties().getProperties().get(
        PropertyIds.CMIS_NAME).getFirstValue();
    assertNotNull(updatedName);
    assertEquals(name2, updatedName);

    // delete document
    delete(docId, true);
  }

  /**
   * Tests delete tree.
   */
  public void testDeleteTree() throws Exception {
    if (!isEnabled(TEST_DELETE_TREE)) {
      return;
    }

    // create a folder tree
    String folder1 = createDefaultFolder(getTestRootFolder(), "folder1");
    String folder11 = createDefaultFolder(folder1, "folder11");
    String folder12 = createDefaultFolder(folder1, "folder12");
    String folder121 = createDefaultFolder(folder12, "folder121");
    String folder122 = createDefaultFolder(folder12, "folder122");

    // create a few documents
    String doc111 = createDefaultDocument(folder11, "doc111.txt", CONTENT_TYPE, CONTENT);
    String doc1221 = createDefaultDocument(folder122, "doc1221.txt", CONTENT_TYPE, CONTENT2);

    // delete the tree
    getProvider().getObjectService().deleteTree(getTestRepositoryId(), folder1, Boolean.TRUE,
        UnfileObjects.DELETE, Boolean.TRUE, null);

    assertFalse(existsObject(folder1));
    assertFalse(existsObject(folder11));
    assertFalse(existsObject(folder12));
    assertFalse(existsObject(folder121));
    assertFalse(existsObject(folder122));
    assertFalse(existsObject(doc111));
    assertFalse(existsObject(doc1221));
  }

  /**
   * Tests move object.
   */
  public void testMoveObject() throws Exception {
    if (!isEnabled(TEST_MOVE_OBJECT)) {
      return;
    }

    // create folders
    String folder1 = createDefaultFolder(getTestRootFolder(), "folder1");
    String folder2 = createDefaultFolder(getTestRootFolder(), "folder2");

    // create document
    String docId = createDefaultDocument(folder1, "testdoc.txt", CONTENT_TYPE, CONTENT);

    // move it
    Holder<String> docIdHolder = new Holder<String>(docId);
    getProvider().getObjectService().moveObject(getTestRepositoryId(), docIdHolder, folder2,
        folder1, null);
    assertNotNull(docIdHolder.getValue());

    assertTrue(existsObject(docIdHolder.getValue()));
    getChild(folder2, docIdHolder.getValue());

    deleteTree(folder1);
    deleteTree(folder2);
  }

  /**
   * Test check-in/check-out.
   */
  public void testVersioning() throws Exception {
    if (!isEnabled(TEST_VERSIONING)) {
      return;
    }

    if (!isVersionable(getDefaultDocumentType())) {
      warning("Default document type is not versionable!");
      return;
    }

    // create document
    String docId = createDefaultDocument(getTestRootFolder(), "versionTest.txt", CONTENT_TYPE,
        CONTENT);

    // there must be only one version in the version series
    List<ObjectData> allVersions = getProvider().getVersioningService().getAllVersions(
        getTestRepositoryId(), getVersionSeriesId(docId), "*", Boolean.FALSE, null);
    assertNotNull(allVersions);
    assertEquals(1, allVersions.size());

    assertEquals(docId, allVersions.get(0).getId());

    // check out
    Holder<String> versionIdHolder = new Holder<String>(docId);
    getProvider().getVersioningService().checkOut(getTestRepositoryId(), versionIdHolder, null,
        null);
    String versionId = versionIdHolder.getValue();

    // object must be marked as checked out
    assertTrue(isCheckedOut(docId));

    // cancel check out
    getProvider().getVersioningService().cancelCheckOut(getTestRepositoryId(), versionId, null);

    // object must NOT be marked as checked out
    assertFalse(isCheckedOut(docId));

    // check out again
    versionIdHolder.setValue(docId);
    getProvider().getVersioningService().checkOut(getTestRepositoryId(), versionIdHolder, null,
        null);
    versionId = versionIdHolder.getValue();

    // object must be marked as checked out
    assertTrue(isCheckedOut(docId));

    versionIdHolder.setValue(versionId);
    getProvider().getVersioningService().checkIn(getTestRepositoryId(), versionIdHolder,
        Boolean.TRUE, null, null, "Test Version 2", null, null, null, null);
    docId = versionIdHolder.getValue();

    // object must NOT be marked as checked out
    assertFalse(isCheckedOut(docId));

    // there must be exactly two versions in the version series
    allVersions = getProvider().getVersioningService().getAllVersions(getTestRepositoryId(),
        getVersionSeriesId(docId), "*", Boolean.FALSE, null);
    assertNotNull(allVersions);
    assertEquals(2, allVersions.size());

    // delete document
    delete(docId, true);
  }

  private boolean isCheckedOut(String docId) {
    ObjectData object = getObject(docId);
    PropertyData<?> isCheckedOut = object.getProperties().getProperties().get(
        PropertyIds.CMIS_IS_VERSION_SERIES_CHECKED_OUT);
    assertNotNull(isCheckedOut);
    assertTrue(isCheckedOut.getFirstValue() instanceof Boolean);

    return ((Boolean) isCheckedOut.getFirstValue()).booleanValue();
  }
}
