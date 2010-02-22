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
package org.apache.opencmis.client.runtime;

import java.util.List;

import junit.framework.Assert;

import org.apache.opencmis.client.api.objecttype.DocumentType;
import org.apache.opencmis.client.api.objecttype.FolderType;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.objecttype.PolicyType;
import org.apache.opencmis.client.api.objecttype.RelationshipType;
import org.apache.opencmis.client.api.util.PagingList;
import org.junit.Test;

public class ReadOnlyTypeTest extends AbstractSessionTest {

  @Test
  public void readBaseTypePolicy() {
    ObjectType otd = this.session.getTypeDefinition(ObjectType.POLICY_BASETYPE_ID);
    Assert.assertNotNull(otd);
    Assert.assertTrue(otd instanceof PolicyType);
    Assert.assertEquals(ObjectType.POLICY_BASETYPE_ID, otd.getId());
    Assert.assertEquals(null, otd.getBaseType());
  }

  @Test
  public void readBaseTypeRelation() {
    ObjectType otd = this.session.getTypeDefinition(ObjectType.RELATIONSHIP_BASETYPE_ID);
    Assert.assertNotNull(otd);
    Assert.assertTrue(otd instanceof RelationshipType);
    Assert.assertEquals(ObjectType.RELATIONSHIP_BASETYPE_ID, otd.getId());
    Assert.assertEquals(null, otd.getBaseType());
  }

  @Test
  public void readBaseTypeDocument() {
    ObjectType otd = this.session.getTypeDefinition(ObjectType.DOCUMENT_BASETYPE_ID);
    Assert.assertNotNull(otd);
    Assert.assertTrue(otd instanceof DocumentType);
    Assert.assertEquals(ObjectType.DOCUMENT_BASETYPE_ID, otd.getId());
    Assert.assertEquals(null, otd.getBaseType());

  }

  @Test
  public void readBaseTypeFolder() {
    ObjectType otf = this.session.getTypeDefinition(ObjectType.FOLDER_BASETYPE_ID);
    Assert.assertNotNull(otf);
    Assert.assertTrue(otf instanceof FolderType);
    Assert.assertEquals(ObjectType.FOLDER_BASETYPE_ID, otf.getId());
    Assert.assertEquals(null, otf.getBaseType());
  }

  @Test
  public void readTypeChildrenDocument() {
    ObjectType otd = this.session.getTypeDefinition(ObjectType.DOCUMENT_BASETYPE_ID);
    Assert.assertNotNull(otd);
    PagingList<ObjectType> pc = this.session.getTypeChildren(otd, true, -1);
    Assert.assertNotNull(pc);

    for (List<ObjectType> children : pc) {
      for (ObjectType ot1 : children) {
        ObjectType ot2 = this.session.getTypeDefinition(ot1.getId());
        Assert.assertEquals(ot1, ot2);
      }
    }
  }

  @Test
  public void readTypeChildrenFolder() {
    ObjectType otd = this.session.getTypeDefinition(ObjectType.FOLDER_BASETYPE_ID);
    Assert.assertNotNull(otd);
    PagingList<ObjectType> pc = this.session.getTypeChildren(otd, true, -1);
    Assert.assertNotNull(pc);

    for (List<ObjectType> children : pc) {
      for (ObjectType ot1 : children) {
        ObjectType ot2 = this.session.getTypeDefinition(ot1.getId());
        Assert.assertEquals(ot1, ot2);
      }
    }
  }

  @Test
  public void readTypeDescandantsDocument() {
    ObjectType otd = this.session.getTypeDefinition(ObjectType.DOCUMENT_BASETYPE_ID);
    Assert.assertNotNull(otd);
    PagingList<ObjectType> children = this.session.getTypeDescendants(otd, 1, true, -1);
    Assert.assertNotNull(children);
  }

  @Test
  public void readTypeDescandantsFolder() {
    ObjectType otd = this.session.getTypeDefinition(ObjectType.FOLDER_BASETYPE_ID);
    Assert.assertNotNull(otd);
    PagingList<ObjectType> children = this.session.getTypeDescendants(otd, 1, true, -1);
    Assert.assertNotNull(children);
  }

}
