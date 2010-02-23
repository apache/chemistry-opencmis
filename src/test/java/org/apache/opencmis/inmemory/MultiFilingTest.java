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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.opencmis.commons.provider.ObjectParentData;
import org.apache.opencmis.inmemory.types.InMemoryFolderTypeDefinition;

public class MultiFilingTest extends AbstractServiceTst {

  private static Log LOG = LogFactory.getLog(MultiFilingTest.class);
  private static final String DOCUMENT_TYPE_ID = UnitTestTypeSystemCreator.COMPLEX_TYPE;
  private static final String FOLDER_TYPE_ID =  InMemoryFolderTypeDefinition.getRootFolderType().getId();
  
  private String fId1;
  private String fId2;
  private String fId11;

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

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
    
    LOG.debug("End testCreatUnfiledDocument()");    
  }
  
  public void xtestMakeFiledDocumentUnfiled() {
    LOG.debug("Begin testMakeFiledDocumentUnfiled()");
    
    String docId = createDocument("Filed document", fRootFolderId, DOCUMENT_TYPE_ID, true);

    fMultiSvc.removeObjectFromFolder(fRepositoryId, docId, fRootFolderId,  null);
    List<ObjectParentData> parents = fNavSvc.getObjectParents(fRepositoryId, docId, "*", false,
        IncludeRelationships.NONE, null, true, null);
    assertEquals(0, parents.size());

    LOG.debug("End testMakeFiledDocumentUnfiled()");    
  }

  public void xtestAddDocumentToFolder() {
    LOG.debug("Begin testAddDocumentToFolder()");
    
    String docId = createUnfiledDocument();
    List<String> folderIds = prepareMultiFiledDocument(docId);
    
    // get object parents, must contain all folders
    List<ObjectParentData> res = fNavSvc.getObjectParents(fRepositoryId, docId, "*", false,
        IncludeRelationships.NONE, null, true, null);
    assertEquals(3, res.size());
    for (ObjectParentData opd : res) {
      assertTrue(folderIds.contains(opd.getObject().getId()));
      assertEquals(BaseObjectTypeIds.CMIS_FOLDER.value(), opd.getObject().getBaseTypeId());
      String name = getStringProperty(opd.getObject(), PropertyIds.CMIS_NAME);
      assertEquals(name, opd.getRelativePathSegment());      
    }
    
    // try version specific filing, should fail
    try {
      fMultiSvc.addObjectToFolder(fRepositoryId, docId, fId1, false, null);
      fail("Adding not all versions to a folder should fail.");
    } catch (Exception e) {
      assertTrue(e instanceof CmisNotSupportedException);
    }
    LOG.debug("End testAddDocumentToFolder()");    
  }
  
  public void xtestRemoveDocumentFromFolder() {
    LOG.debug("Begin testRemoveDocumentFromFolder()");

    String docId = createUnfiledDocument();
    prepareMultiFiledDocument(docId);
    
    fMultiSvc.removeObjectFromFolder(fRepositoryId, docId, fId1,  null);
    List<ObjectParentData> parents = fNavSvc.getObjectParents(fRepositoryId, docId, "*", false,
        IncludeRelationships.NONE, null, true, null);
    assertEquals(2, parents.size());
    for (ObjectParentData opd : parents) {
      assertFalse(fId1.equals(opd.getObject().getId()));
    }
    
    fMultiSvc.removeObjectFromFolder(fRepositoryId, docId, fId2,  null);
    parents = fNavSvc.getObjectParents(fRepositoryId, docId, "*", false,
        IncludeRelationships.NONE, null, true, null);
    assertEquals(1, parents.size());
    for (ObjectParentData opd : parents) {
      assertFalse(fId1.equals(opd.getObject().getId()));
    }

    fMultiSvc.removeObjectFromFolder(fRepositoryId, docId, fId11,  null);
    parents = fNavSvc.getObjectParents(fRepositoryId, docId, "*", false,
        IncludeRelationships.NONE, null, true, null);
    assertEquals(0, parents.size());

    LOG.debug("End testRemoveDocumentFromFolder()");    
  }
  
  private void createFolders() {
    fId1 = createFolder("folder1", fRootFolderId, FOLDER_TYPE_ID);
    fId2 = createFolder("folder2", fRootFolderId, FOLDER_TYPE_ID);
    fId11 = createFolder("folder1.1", fId1, FOLDER_TYPE_ID);    
  }
  
  private String createUnfiledDocument() {
    return createDocument("Unfiled document", null, DOCUMENT_TYPE_ID, true);
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
}
