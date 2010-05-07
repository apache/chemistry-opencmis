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
package org.apache.chemistry.opencmis.fit.runtime;

import java.util.List;

import junit.framework.Assert;

import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.FolderType;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.PolicyType;
import org.apache.chemistry.opencmis.client.api.RelationshipType;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.junit.Test;

public abstract class AbstractReadOnlyTypeIT extends AbstractSessionTest {

    @Test
    public void readOptionalBaseTypePolicy() {
        try {
            ObjectType otd = this.session.getTypeDefinition(ObjectType.POLICY_BASETYPE_ID);
            Assert.assertTrue(otd instanceof PolicyType);
            Assert.assertEquals(ObjectType.POLICY_BASETYPE_ID, otd.getId());
            Assert.assertEquals(null, otd.getBaseType());
        } catch (CmisObjectNotFoundException e) {
            // policies not supported
        }
    }

    @Test
    public void readOptionalBaseTypeRelation() {
        try {
            ObjectType otd = this.session.getTypeDefinition(ObjectType.RELATIONSHIP_BASETYPE_ID);
            Assert.assertNotNull(otd);
            Assert.assertTrue(otd instanceof RelationshipType);
            Assert.assertEquals(ObjectType.RELATIONSHIP_BASETYPE_ID, otd.getId());
            Assert.assertEquals(null, otd.getBaseType());
        } catch (CmisObjectNotFoundException e) {
            // policies not supported
        }
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
        this.session.getDefaultContext().setMaxItemsPerPage(2);
        ItemIterable<ObjectType> pc = this.session.getTypeChildren(otd.getId(), true);
        Assert.assertNotNull(pc);

        for (ObjectType ot1 : pc) {
            ObjectType ot2 = this.session.getTypeDefinition(ot1.getId());
            Assert.assertEquals(ot1.getId(), ot2.getId());
        }
    }

    @Test
    public void readTypeChildrenDocumentSkip() {
        ObjectType otd = this.session.getTypeDefinition(ObjectType.DOCUMENT_BASETYPE_ID);
        Assert.assertNotNull(otd);
        this.session.getDefaultContext().setMaxItemsPerPage(2);
        ItemIterable<ObjectType> pc = this.session.getTypeChildren(otd.getId(), true);
        Assert.assertNotNull(pc);

        ItemIterable<ObjectType> pcc = pc.skipTo(2).getPage(2);
        for (ObjectType ot1 : pcc) {
            ObjectType ot2 = this.session.getTypeDefinition(ot1.getId());
            Assert.assertEquals(ot1.getId(), ot2.getId());
        }
    }

    @Test
    public void readTypeChildrenFolder() {
        ObjectType otd = this.session.getTypeDefinition(ObjectType.FOLDER_BASETYPE_ID);
        Assert.assertNotNull(otd);
        this.session.getDefaultContext().setMaxItemsPerPage(2);
        ItemIterable<ObjectType> pc = this.session.getTypeChildren(otd.getId(), true);
        Assert.assertNotNull(pc);

        for (ObjectType ot1 : pc) {
            ObjectType ot2 = this.session.getTypeDefinition(ot1.getId());
            Assert.assertEquals(ot1, ot2);
        }
    }

    @Test
    public void readTypeChildrenFolderSkip() {
        ObjectType otd = this.session.getTypeDefinition(ObjectType.FOLDER_BASETYPE_ID);
        Assert.assertNotNull(otd);
        this.session.getDefaultContext().setMaxItemsPerPage(2);
        ItemIterable<ObjectType> pc = this.session.getTypeChildren(otd.getId(), true);
        Assert.assertNotNull(pc);

        ItemIterable<ObjectType> pcc = pc.skipTo(0).getPage(2);
        for (ObjectType ot1 : pcc) {
            ObjectType ot2 = this.session.getTypeDefinition(ot1.getId());
            Assert.assertEquals(ot1, ot2);
        }
    }

    @Test
    public void readTypeDescandantsDocument() {
        ObjectType otd = this.session.getTypeDefinition(ObjectType.DOCUMENT_BASETYPE_ID);
        Assert.assertNotNull(otd);
        List<Tree<ObjectType>> desc = this.session.getTypeDescendants(otd.getId(), 1, true);
        Assert.assertNotNull(desc);
        Assert.assertFalse(desc.isEmpty());
    }

    @Test
    public void readTypeDescandantsFolder() {
        ObjectType otd = this.session.getTypeDefinition(ObjectType.FOLDER_BASETYPE_ID);
        Assert.assertNotNull(otd);
        List<Tree<ObjectType>> desc = this.session.getTypeDescendants(otd.getId(), 1, true);
        Assert.assertNotNull(desc);
        desc.isEmpty();
    }

}
