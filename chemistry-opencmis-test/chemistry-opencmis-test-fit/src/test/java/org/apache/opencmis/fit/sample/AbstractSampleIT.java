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
package org.apache.opencmis.fit.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.opencmis.client.api.Session;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.repository.RepositoryInfo;
import org.apache.opencmis.client.api.util.Container;
import org.apache.opencmis.client.api.util.PagingList;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.fit.SessionFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sample test case that demonstrates how to build integration tests.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public abstract class AbstractSampleIT {

  private static Session fSession;

  /**
   * Returns the current Session object.
   */
  protected Session getSession() {
    return fSession;
  }

  /**
   * Returns a new Session object.
   */
  protected abstract Session createSession();

  @BeforeClass
  public static void setUpClass() {
    fSession = null;
  }

  @Before
  public void setUp() {
    if (fSession == null) {
      fSession = createSession();
    }
  }

  /**
   * Simple repository info test.
   */
  @Test
  public void testRepositoryInfo() {
    RepositoryInfo ri = getSession().getRepositoryInfo();
    assertNotNull(ri);
    assertEquals(SessionFactory.getRepositoryId(), ri.getId());
    assertNotNull(ri.getName());
    assertNotNull(ri.getRootFolderId());
    assertNotNull(ri.getCmisVersionSupported());
    assertNotNull(ri.getCapabilities());
    //assertNotNull(ri.getAclCapabilities());
  }

  /**
   * Simple types test.
   */
  @Test
  public void testTypes() {
    String documentBaseId = "cmis:document";
    String folderBaseId = "cmis:folder";

    // check document type definition
    ObjectType documentType = getSession().getTypeDefinition(documentBaseId);
    checkBaseType(documentBaseId, BaseObjectTypeIds.CMIS_DOCUMENT, documentType);

    // check folder type definition
    ObjectType folderType = getSession().getTypeDefinition(folderBaseId);
    checkBaseType(folderBaseId, BaseObjectTypeIds.CMIS_FOLDER, folderType);

    // get base types via getTypesChildren
    PagingList<ObjectType> baseTypes = getSession().getTypeChildren(null, true, 10);
    assertNotNull(baseTypes);

    List<ObjectType> baseTypePage = baseTypes.get(0);
    assertNotNull(baseTypePage);
    assertTrue(baseTypePage.size() >= 2);
    assertTrue(baseTypePage.size() <= 4);

    assertEquals(1, baseTypes.size());
    assertEquals(baseTypePage.size(), baseTypes.getNumItems());

    boolean hasDocumentBaseType = false;
    boolean hasFolderBaseType = false;
    for (ObjectType ot : baseTypePage) {
      checkBaseType(null, null, ot);

      if (ot.getId().equals(documentBaseId)) {
        hasDocumentBaseType = true;
      }

      if (ot.getId().equals(folderBaseId)) {
        hasFolderBaseType = true;
      }
    }

    assertTrue(hasDocumentBaseType);
    assertTrue(hasFolderBaseType);

    // get base types via getTypeDescendants
    List<Container<ObjectType>> baseTypeDesc = getSession().getTypeDescendants(null, -1, true);
    assertNotNull(baseTypeDesc);

    hasDocumentBaseType = false;
    hasFolderBaseType = false;
    for (Container<ObjectType> cot : baseTypeDesc) {
      assertNotNull(cot);
      //checkBaseType(null, null, cot.getItem());

      if (cot.getItem().getId().equals(documentBaseId)) {
        hasDocumentBaseType = true;
      }

      if (cot.getItem().getId().equals(folderBaseId)) {
        hasFolderBaseType = true;
      }
    }

    assertTrue(hasDocumentBaseType);
    assertTrue(hasFolderBaseType);
  }

  /**
   * Checks a base type.
   */
  private void checkBaseType(String id, BaseObjectTypeIds baseType, ObjectType objectType) {
    assertNotNull(objectType);
    if (id != null) {
      assertEquals(id, objectType.getId());
    }
    if (baseType != null) {
      assertEquals(baseType, objectType.getBaseTypeId());
    }
    assertTrue(objectType.isBaseType());
    assertNull(objectType.getBaseType());
    assertNull(objectType.getParent());
    assertNotNull(objectType.getPropertyDefintions());
    assertFalse(objectType.getPropertyDefintions().isEmpty());
  }
}
