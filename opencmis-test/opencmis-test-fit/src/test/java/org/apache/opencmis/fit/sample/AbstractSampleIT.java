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

import org.apache.opencmis.client.api.Session;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.repository.RepositoryInfo;
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
  }

  /**
   * Simple types test.
   */
  @Test
  public void testTypes() {
    String documnetBaseId = "cmis:document";
    String folderBaseId = "cmis:folder";

    ObjectType documentType = getSession().getTypeDefinition(documnetBaseId);
    assertNotNull(documentType);
    assertEquals(documnetBaseId, documentType.getId());
    assertEquals(BaseObjectTypeIds.CMIS_DOCUMENT, documentType.getBaseTypeId());
    assertTrue(documentType.isBaseType());
    assertNull(documentType.getBaseType());
    assertNull(documentType.getParent());
    assertNotNull(documentType.getPropertyDefintions());
    assertFalse(documentType.getPropertyDefintions().isEmpty());

    ObjectType folderType = getSession().getTypeDefinition(folderBaseId);
    assertNotNull(folderType);
    assertEquals(folderBaseId, folderType.getId());
    assertEquals(BaseObjectTypeIds.CMIS_FOLDER, folderType.getBaseTypeId());
    assertTrue(folderType.isBaseType());
    assertNull(folderType.getBaseType());
    assertNull(folderType.getParent());
    assertNotNull(folderType.getPropertyDefintions());
    assertFalse(folderType.getPropertyDefintions().isEmpty());
  }
}
