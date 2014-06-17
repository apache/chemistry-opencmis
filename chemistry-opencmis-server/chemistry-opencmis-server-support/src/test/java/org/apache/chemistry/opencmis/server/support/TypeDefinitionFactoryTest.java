/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.chemistry.opencmis.server.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.junit.Test;

public class TypeDefinitionFactoryTest {

    @Test
    public void testTypeDefinitionFactory() {
        TypeDefinitionFactory tdf = TypeDefinitionFactory.newInstance();
        assertNotNull(tdf);
        assertNotNull(tdf.getDocumentTypeDefinitionClass());
        assertNotNull(tdf.getFolderTypeDefinitionClass());
        assertNotNull(tdf.getPolicyTypeDefinitionClass());
        assertNotNull(tdf.getRelationshipTypeDefinitionClass());
        assertNotNull(tdf.getItemTypeDefinitionClass());
        assertNotNull(tdf.getSecondaryTypeDefinitionClass());
    }

    @Test
    public void testCreateBaseTypes() {
        TypeDefinitionFactory tdf = TypeDefinitionFactory.newInstance();
        CmisVersion cmisVersion = CmisVersion.CMIS_1_1;

        assertTypeDefinition(tdf.createBaseDocumentTypeDefinition(cmisVersion));
        assertTypeDefinition(tdf.createBaseFolderTypeDefinition(cmisVersion));
        assertTypeDefinition(tdf.createBasePolicyTypeDefinition(cmisVersion));
        assertTypeDefinition(tdf.createBaseRelationshipTypeDefinition(cmisVersion));
        assertTypeDefinition(tdf.createBaseItemTypeDefinition(cmisVersion));
        assertTypeDefinition(tdf.createBaseSecondaryTypeDefinition(cmisVersion));
    }

    @Test
    public void testCopy11() {
        TypeDefinitionFactory tdf = TypeDefinitionFactory.newInstance();
        CmisVersion cmisVersion = CmisVersion.CMIS_1_1;

        TypeDefinition docType1 = tdf.createBaseDocumentTypeDefinition(cmisVersion);
        TypeDefinition docType2 = tdf.copy(docType1, false, cmisVersion);
        TypeDefinition docType3 = tdf.copy(docType1, true, cmisVersion);

        assertTrue(docType2.getPropertyDefinitions().isEmpty());
        assertEquals(docType1.getPropertyDefinitions().size(), docType3.getPropertyDefinitions().size());
    }

    @Test
    public void testCopy10() {
        TypeDefinitionFactory tdf = TypeDefinitionFactory.newInstance();
        CmisVersion cmisVersion = CmisVersion.CMIS_1_0;

        TypeDefinition docType1 = tdf.createBaseDocumentTypeDefinition(cmisVersion);
        TypeDefinition docType2 = tdf.copy(docType1, false, cmisVersion);
        TypeDefinition docType3 = tdf.copy(docType1, true, cmisVersion);

        assertTrue(docType2.getPropertyDefinitions().isEmpty());
        assertEquals(docType1.getPropertyDefinitions().size(), docType3.getPropertyDefinitions().size());
    }

    @Test
    public void testCreateTypeDefinitionList() {
        TypeDefinitionFactory tdf = TypeDefinitionFactory.newInstance();
        CmisVersion cmisVersion = CmisVersion.CMIS_1_1;
        Map<String, TypeDefinition> types = new HashMap<String, TypeDefinition>();

        TypeDefinition type;
        TypeDefinition docType;
        TypeDefinition folderType;

        docType = tdf.createBaseDocumentTypeDefinition(cmisVersion);
        types.put(docType.getId(), docType);

        type = tdf.createChildTypeDefinition(docType, "test:docType1");
        types.put(type.getId(), type);

        type = tdf.createChildTypeDefinition(docType, "test:docType2");
        types.put(type.getId(), type);

        type = tdf.createChildTypeDefinition(docType, "test:docType3");
        types.put(type.getId(), type);

        type = tdf.createChildTypeDefinition(docType, "test:docType4");
        types.put(type.getId(), type);

        type = tdf.createChildTypeDefinition(docType, "test:docType5");
        types.put(type.getId(), type);

        folderType = tdf.createBaseFolderTypeDefinition(cmisVersion);
        types.put(folderType.getId(), folderType);

        TypeDefinitionList tdl1 = tdf.createTypeDefinitionList(types, null, true, null, null);
        assertNotNull(tdl1);
        assertEquals(2, tdl1.getList().size());
        assertEquals(2, tdl1.getNumItems().intValue());
        assertEquals(Boolean.FALSE, tdl1.hasMoreItems());

        assertEquals("cmis:document", tdl1.getList().get(0).getId());
        assertEquals("cmis:folder", tdl1.getList().get(1).getId());
        assertEquals(26, tdl1.getList().get(0).getPropertyDefinitions().size());
        assertEquals(14, tdl1.getList().get(1).getPropertyDefinitions().size());

        TypeDefinitionList tdl2 = tdf.createTypeDefinitionList(types, "cmis:document", false, null, null);
        assertNotNull(tdl2);
        assertEquals(5, tdl2.getList().size());
        assertEquals(5, tdl2.getNumItems().intValue());
        assertEquals(Boolean.FALSE, tdl2.hasMoreItems());
        assertTrue(tdl2.getList().get(0).getPropertyDefinitions().isEmpty());

        TypeDefinitionList tdl3 = tdf.createTypeDefinitionList(types, "cmis:document", true, BigInteger.valueOf(3),
                BigInteger.ZERO);
        assertNotNull(tdl3);
        assertEquals(3, tdl3.getList().size());
        assertEquals(5, tdl3.getNumItems().intValue());
        assertEquals(Boolean.TRUE, tdl3.hasMoreItems());
        assertFalse(tdl3.getList().get(0).getPropertyDefinitions().isEmpty());

        TypeDefinitionList tdl4 = tdf.createTypeDefinitionList(types, "cmis:document", true, BigInteger.valueOf(3),
                BigInteger.valueOf(2));
        assertNotNull(tdl4);
        assertEquals(3, tdl4.getList().size());
        assertEquals(5, tdl4.getNumItems().intValue());
        assertEquals(Boolean.FALSE, tdl4.hasMoreItems());

        TypeDefinitionList tdl5 = tdf.createTypeDefinitionList(types, "cmis:document", true, BigInteger.valueOf(2),
                BigInteger.valueOf(2));
        assertNotNull(tdl5);
        assertEquals(2, tdl5.getList().size());
        assertEquals(5, tdl5.getNumItems().intValue());
        assertEquals(Boolean.TRUE, tdl5.hasMoreItems());

        assertEquals("test:docType1", tdl2.getList().get(0).getId());
        assertEquals("test:docType2", tdl2.getList().get(1).getId());
        assertEquals("test:docType3", tdl2.getList().get(2).getId());
        assertEquals("test:docType4", tdl2.getList().get(3).getId());
        assertEquals("test:docType5", tdl2.getList().get(4).getId());

        assertEquals(tdl2.getList().get(0).getId(), tdl3.getList().get(0).getId());
        assertEquals(tdl2.getList().get(2).getId(), tdl4.getList().get(0).getId());
        assertEquals(tdl2.getList().get(2).getId(), tdl5.getList().get(0).getId());
        assertEquals(tdl4.getList().get(0).getId(), tdl5.getList().get(0).getId());
    }

    @Test
    public void testCreateTypeDefinitionListObject() {
        TypeDefinitionFactory tdf = TypeDefinitionFactory.newInstance();

        assertNotNull(tdf.createTypeDefinitionList(Collections.<TypeDefinition> emptyList(), true, null));

        try {
            tdf.createTypeDefinitionList(null, true, null);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            tdf.createTypeDefinitionList(Collections.<TypeDefinition> emptyList(), true, BigInteger.valueOf(-1));
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testCreateTypeDescendants() {
        TypeDefinitionFactory tdf = TypeDefinitionFactory.newInstance();
        CmisVersion cmisVersion = CmisVersion.CMIS_1_1;
        Map<String, TypeDefinition> types = new HashMap<String, TypeDefinition>();

        TypeDefinition type;
        TypeDefinition type2;
        TypeDefinition docType;
        TypeDefinition folderType;

        docType = tdf.createBaseDocumentTypeDefinition(cmisVersion);
        types.put(docType.getId(), docType);

        type = tdf.createChildTypeDefinition(docType, "test:docType1");
        types.put(type.getId(), type);

        type2 = tdf.createChildTypeDefinition(docType, "test:docType2");
        types.put(type2.getId(), type2);

        type = tdf.createChildTypeDefinition(type2, "test:docType2-1");
        types.put(type.getId(), type);

        type = tdf.createChildTypeDefinition(type2, "test:docType2-2");
        types.put(type.getId(), type);

        type = tdf.createChildTypeDefinition(type2, "test:docType2-3");
        types.put(type.getId(), type);

        folderType = tdf.createBaseFolderTypeDefinition(cmisVersion);
        types.put(folderType.getId(), folderType);

        List<TypeDefinitionContainer> typeDefs;

        typeDefs = tdf.createTypeDescendants(types, null, BigInteger.valueOf(-1), null);
        assertNotNull(typeDefs);
        assertEquals(2, typeDefs.size());

        typeDefs = tdf.createTypeDescendants(types, null, BigInteger.valueOf(1), null);
        assertNotNull(typeDefs);
        assertEquals(2, typeDefs.size());
    }

    @Test
    public void testCreateTypeContainerObject() {
        TypeDefinitionFactory tdf = TypeDefinitionFactory.newInstance();

        assertNotNull(tdf.createTypeDefinitionContainer(tdf.createBaseDocumentTypeDefinition(CmisVersion.CMIS_1_1),
                null));

        try {
            tdf.createTypeDefinitionContainer(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    private void assertTypeDefinition(TypeDefinition typeDef) {
        assertNotNull(typeDef);
        assertNotNull(typeDef.getBaseTypeId());
        assertNotNull(typeDef.getId());

    }
}
