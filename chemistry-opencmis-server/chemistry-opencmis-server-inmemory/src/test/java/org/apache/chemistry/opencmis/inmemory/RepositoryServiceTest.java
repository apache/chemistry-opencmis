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
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.inmemory.types.DocumentTypeCreationHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryServiceTest extends AbstractServiceTest {

    // private CmisProvider fProvider;

    private static final Logger log = LoggerFactory.getLogger(RepositoryServiceTest.class);

    @Override
    @Before
    public void setUp() {
        super.setTypeCreatorClass(UnitTestTypeSystemCreator.class.getName());
        super.setUp();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testRepositoryInfo() {
        log.info("starting testRepositoryInfo() ...");
        List<RepositoryInfo> repositories = fRepSvc.getRepositoryInfos(null);
        assertNotNull(repositories);
        assertFalse(repositories.isEmpty());

        log.info("geRepositoryInfo(), found " + repositories.size() + " repository/repositories).");

        for (RepositoryInfo repository : repositories) {
            RepositoryInfo repository2 = fRepSvc.getRepositoryInfo(repository.getId(), null);
            assertNotNull(repository2);
            assertEquals(repository.getId(), repository2.getId());
            log.info("found repository" + repository2.getId());
        }

        log.info("... testRepositoryInfo() finished.");
    }

    @Test
    public void testTypeDefinition() {
        log.info("");
        log.info("starting testTypeDefinition() ...");
        String repositoryId = getRepositoryId();
        String typeId = "MyDocType1";
        TypeDefinition ref = UnitTestTypeSystemCreator.getTypeById(typeId);
        TypeDefinition type = fRepSvc.getTypeDefinition(repositoryId, typeId, null);
        assertEquals(ref.getId(), type.getId());
        assertEquals(ref.getDescription(), type.getDescription());
        assertEquals(ref.getDisplayName(), type.getDisplayName());
        assertEquals(ref.getLocalName(), type.getLocalName());
        assertEquals(ref.getLocalNamespace(), type.getLocalNamespace());
        containsAllBasePropertyDefinitions(type);
        log.info("... testTypeDefinition() finished.");
    }

    @Test
    public void testGetAllTypesUnlimitedDepth() {
        log.info("");
        log.info("starting testGetAllTypesUnlimitedDepth()...");
        String repositoryId = getRepositoryId();

        // get types
        List<TypeDefinitionContainer> types = fRepSvc.getTypeDescendants(repositoryId, null, BigInteger.valueOf(-1),
                Boolean.TRUE, null);
        assertNotNull(types);

        // check that we got all types
        int expectedSize = UnitTestTypeSystemCreator.getTypesList().size()
                + DocumentTypeCreationHelper.getDefaultTypes().size();
        int totalSize = getRecursiveSize(types);

        assertEquals(expectedSize, totalSize);
        assertEquals(6, types.size());

        for (TypeDefinitionContainer type : types) {
            assertNotNull(type);
            TypeDefinition typeDef = type.getTypeDefinition();
            assertNotNull(typeDef);
            assertNotNull(typeDef.getId());
            assertNotNull(typeDef.getBaseTypeId());
            log.info("Found type: " + typeDef.getId() + ", display name is: " + typeDef.getDisplayName());
            log.info("  Base type is: " + typeDef.getBaseTypeId());
            log.info("  Number of children types is: " + type.getChildren().size());
            Map<String, PropertyDefinition<?>> propDefs = type.getTypeDefinition().getPropertyDefinitions();
            log.info("  Number of properties is: " + (propDefs == null ? 0 : propDefs.size()));
            containsAllBasePropertyDefinitions(typeDef);
        }

        log.info("... testGetAllTypesUnlimitedDepth() finished.");
    }

    @Test
    public void testGetTypesWihtoutPropDefs() {
        log.info("");
        log.info("starting testGetTypesWihtoutPropDefs()...");
        String repositoryId = getRepositoryId();

        // get types
        List<TypeDefinitionContainer> types = fRepSvc.getTypeDescendants(repositoryId,
                BaseTypeId.CMIS_DOCUMENT.value(), BigInteger.valueOf(-1), Boolean.FALSE, null);
        // List<TypeDefinitionContainer> types =
        // fRepSvc.getTypeDescendants(repositoryId, "MyDocType1",
        // BigInteger.valueOf(-1),
        // Boolean.FALSE, null);
        assertNotNull(types);

        int totalSize = getRecursiveSize(types);
        log.info("Found " + totalSize + " number of type definitions. ");

        List<TypeDefinition> descendants = getTypeDefsFlattened(types);
        for (TypeDefinition typeDef : descendants) {
            assertNotNull(typeDef);
            assertNotNull(typeDef.getId());
            assertNotNull(typeDef.getBaseTypeId());
            assertEquals(BaseTypeId.CMIS_DOCUMENT, typeDef.getBaseTypeId());
            log.info("Found type: " + typeDef.getId() + ", display name is: " + typeDef.getDisplayName());
            log.info("  Base type is: " + typeDef.getBaseTypeId());
            Map<String, PropertyDefinition<?>> propDefs = typeDef.getPropertyDefinitions();
            log.info("  Property definitions (must be null): " + propDefs);
            assertTrue(propDefs.isEmpty());
        }

        log.info("... testGetTypesWihtoutPropDefs() finished.");
    }

    @Test
    public void testGetAllTypesLimitedDepth() {
        log.info("");
        log.info("starting testGetAllTypesLimitedDepth()...");
        String repositoryId = getRepositoryId();

        // get types
        int depth = 1;
        List<TypeDefinitionContainer> types = fRepSvc.getTypeDescendants(repositoryId,
                BaseTypeId.CMIS_DOCUMENT.value(), BigInteger.valueOf(depth), Boolean.TRUE, null);
        assertNotNull(types);
        log.info("Found in repository " + repositoryId + " " + types.size() + " type(s) with depth " + depth + ".");

        for (TypeDefinitionContainer type : types) {
            TypeDefinition typeDef = type.getTypeDefinition();
            log.info("Found type: " + typeDef.getId() + ", display name is: " + typeDef.getDisplayName());
            log.info("  Base type is: " + typeDef.getBaseTypeId());
            log.info("  Number of children types is: " + type.getChildren().size());
            containsAllBasePropertyDefinitions(typeDef);
        }

        int totalSize = getRecursiveSize(types);
        assertEquals(5, totalSize); // all RepositoryTestTypeSystemCreator types
        // minus one in level two plus cmis.docment
        assertFalse(containsTypeByIdRecursive(BaseTypeId.CMIS_DOCUMENT.value(), types));
        assertFalse(containsTypeByIdRecursive(BaseTypeId.CMIS_FOLDER.value(), types));

        assertTrue(containsTypeByIdRecursive("MyDocType1", types));
        assertTrue(containsTypeByIdRecursive("MyDocType2", types));

        assertFalse(containsTypeByIdRecursive(UnitTestTypeSystemCreator.LEVEL2_TYPE, types));

        for (TypeDefinitionContainer type : types) {
            assertNotNull(type);
            TypeDefinition typeDef = type.getTypeDefinition();
            assertNotNull(typeDef);
            assertNotNull(typeDef.getId());
            assertNotNull(typeDef.getBaseTypeId());
        }

        log.info("... testGetAllTypesLimitedDepth() finished.");
    }

    @Test
    public void testGetSpecificTypeLimitedDepth() {
        log.info("");
        log.info("starting testGetSpecificTypeLimitedDepth()...");
        String repositoryId = getRepositoryId();

        // get types
        int depth = 2;
        String typeId = "MyDocType1";
        List<TypeDefinitionContainer> types = fRepSvc.getTypeDescendants(repositoryId, typeId,
                BigInteger.valueOf(depth), Boolean.TRUE, null);
        assertNotNull(types);
        log.info("Found in repository " + repositoryId + " for type " + typeId + ", " + types.size()
                + " type(s) with depth " + depth + ".");

        assertEquals(4, getRecursiveSize(types));
        assertTrue(containsTypeByIdRecursive("MyDocType1.1", types));
        assertTrue(containsTypeByIdRecursive("MyDocType1.2", types));
        assertTrue(containsTypeByIdRecursive("MyDocType1.1.1", types));
        assertTrue(containsTypeByIdRecursive("MyDocType1.1.2", types));
        for (TypeDefinitionContainer type : types) {
            assertNotNull(type);
            TypeDefinition typeDef = type.getTypeDefinition();
            assertNotNull(typeDef);
            assertNotNull(typeDef.getId());
            assertNotNull(typeDef.getBaseTypeId());
            log.info("Found type: " + typeDef.getId() + ", display name is: " + typeDef.getDisplayName());
            log.info("  Base type is: " + typeDef.getBaseTypeId());
            log.info("  Number of children types is: " + type.getChildren().size());
            containsAllBasePropertyDefinitions(typeDef);
        }

        log.info("... testGetSpecificTypeLimitedDepth() finished.");
    }

    @Test
    public void testGetTypeChildren() {
        log.info("");
        log.info("starting testGetTypeChildren()...");
        String repositoryId = getRepositoryId();
        String typeId = "MyDocType2";

        // get all children
        BigInteger maxItems = BigInteger.valueOf(1000);
        BigInteger skipCount = BigInteger.valueOf(0);
        TypeDefinitionList children = fRepSvc.getTypeChildren(repositoryId, typeId, true, maxItems, skipCount, null);

        for (TypeDefinition type : children.getList()) {
            log.info("Found type: " + type.getId() + ", display name is: " + type.getDisplayName());
            containsAllBasePropertyDefinitions(type);
        }
        assertEquals(9, children.getList().size());
        assertEquals(9, children.getNumItems().intValue());
        assertFalse(children.hasMoreItems());

        // get a chunk
        maxItems = BigInteger.valueOf(5);
        skipCount = BigInteger.valueOf(3);
        children = fRepSvc.getTypeChildren(repositoryId, typeId, true, maxItems, skipCount, null);

        for (TypeDefinition type : children.getList()) {
            log.info("Found type: " + type.getId() + ", display name is: " + type.getDisplayName());
            containsAllBasePropertyDefinitions(type);
        }
        assertEquals(5, children.getList().size());
        assertEquals(9, children.getNumItems().intValue());
        assertTrue(children.hasMoreItems());

        log.info("... testGetTypeChildren() finished.");
    }

    @Test
    public void testGetTypeChildrenNoProperties() {
        log.info("");
        log.info("starting testGetTypeChildrenNoProperties()...");
        String repositoryId = getRepositoryId();
        String typeId = "cmis:document";

        // get all children
        BigInteger maxItems = BigInteger.valueOf(1000);
        BigInteger skipCount = BigInteger.valueOf(0);
        TypeDefinitionList children = fRepSvc.getTypeChildren(repositoryId, typeId, null, maxItems, skipCount, null);

        children = fRepSvc.getTypeChildren(repositoryId, typeId, null, maxItems, null, null);

        for (TypeDefinition type : children.getList()) {
            assertTrue(type.getPropertyDefinitions().isEmpty());
        }

        log.info("... testGetTypeChildrenNoProperties() finished.");
    }

    @Test
    public void testGetWrongParameters() {
        log.info("");
        log.info("starting testGetWrongParameters()...");
        String repositoryId = getRepositoryId();
        String wrongRepositoryId = "NonExistantRepository";

        // get types
        int depth = -1;
        String wrongTypeId = "UnknownType";

        try {
            RepositoryInfo repInf = fRepSvc.getRepositoryInfo(wrongRepositoryId, null);
            log.debug("getRepositoryInfo(): " + repInf);
            fail("getRepositoryInfo() with illegal repository id should throw InvalidArgumentException.");
        } catch (CmisInvalidArgumentException e) {
            log.info("getRepositoryInfo() with depth==0 raised expected exception: " + e);
        }

        try {
            List<TypeDefinitionContainer> types = fRepSvc.getTypeDescendants(repositoryId, "CMISDocument",
                    BigInteger.valueOf(0), Boolean.TRUE, null);
            log.debug("getTypeDescendants(): " + types);
            fail("getTypeDescendants() with depth 0 should throw InvalidArgumentException.");
        } catch (CmisInvalidArgumentException e) {
            log.info("getTypeDescendants() with depth==0 raised expected exception: " + e);
        }

        try {
            List<TypeDefinitionContainer> types = fRepSvc.getTypeDescendants(repositoryId, wrongTypeId,
                    BigInteger.valueOf(depth), Boolean.TRUE, null);
            log.debug("getTypeDescendants(): " + types);
            fail("getTypeDescendants() with unknown type should throw exception.");
        } catch (CmisInvalidArgumentException e) {
            log.info("getTypeDescendants() with unknown type raised expected exception: " + e);
        }

        try {
            TypeDefinition type = fRepSvc.getTypeDefinition(wrongRepositoryId, "CMISDocument", null);
            log.debug("getTypeDefinition(): " + type);
            fail("getTypeDefinition() with unknown repository id should throw exception.");
        } catch (CmisInvalidArgumentException e) {
            log.info("getTypeDefinition() with unknown repository id raised expected exception: " + e);
        }

        try {
            TypeDefinition type = fRepSvc.getTypeDefinition(repositoryId, wrongTypeId, null);
            log.debug("getTypeDefinition(): " + type);
            fail("getTypeDefinition() with unknown type should throw exception.");
        } catch (CmisObjectNotFoundException e) {
            log.info("getTypeDefinition() with unknown type raised expected exception: " + e);
        }

        try {
            TypeDefinitionList types = fRepSvc.getTypeChildren(wrongRepositoryId, "CMISDocument", Boolean.TRUE,
                    BigInteger.valueOf(100), BigInteger.ZERO, null);
            log.debug("getTypeChildren(): " + types);
            fail("getTypeDescendants() with unknown type should throw InvalidArgumentException.");
        } catch (CmisInvalidArgumentException e) {
            log.info("getTypeDescendants() with unknown repository id raised expected exception: " + e);
        }

        try {
            TypeDefinitionList types = fRepSvc.getTypeChildren(repositoryId, wrongTypeId, Boolean.TRUE,
                    BigInteger.valueOf(100), BigInteger.ZERO, null);
            log.debug("getTypeChildren(): " + types);
            fail("getTypeDescendants() with unknown type should throw exception.");
        } catch (CmisInvalidArgumentException e) {
            log.info("getTypeDescendants() with unknown type raised expected exception: " + e);
        }

        log.info("... testGetUnknownType() testGetWrongParameters.");

    }

    @Test
    public void testInheritedProperties() {
        log.info("");
        log.info("starting testInheritedProperties()...");
        String repositoryId = getRepositoryId();
        String typeId = UnitTestTypeSystemCreator.TOPLEVEL_TYPE;

        // get top level type
        TypeDefinition typeContainer = fRepSvc.getTypeDefinition(repositoryId, typeId, null);
        assertNotNull(typeContainer);
        Map<String, PropertyDefinition<?>> propDefMap = typeContainer.getPropertyDefinitions();
        assertTrue(propDefMap.containsKey("StringPropTopLevel"));
        assertFalse(propDefMap.get("StringPropTopLevel").isInherited());
        assertFalse(propDefMap.containsKey("StringPropLevel1"));
        assertFalse(propDefMap.containsKey("StringPropLevel2"));
        containsAllBasePropertyDefinitions(typeContainer);

        // get level 1 type
        typeId = UnitTestTypeSystemCreator.LEVEL1_TYPE;
        typeContainer = fRepSvc.getTypeDefinition(repositoryId, typeId, null);
        assertNotNull(typeContainer);
        propDefMap = typeContainer.getPropertyDefinitions();
        assertTrue(propDefMap.containsKey("StringPropTopLevel"));
        assertTrue(propDefMap.get("StringPropTopLevel").isInherited());
        assertTrue(propDefMap.containsKey("StringPropLevel1"));
        assertFalse(propDefMap.get("StringPropLevel1").isInherited());
        assertFalse(propDefMap.containsKey("StringPropLevel2"));
        containsAllBasePropertyDefinitions(typeContainer);

        // get level 2 type
        typeId = UnitTestTypeSystemCreator.LEVEL2_TYPE;
        typeContainer = fRepSvc.getTypeDefinition(repositoryId, typeId, null);
        assertNotNull(typeContainer);
        propDefMap = typeContainer.getPropertyDefinitions();
        assertTrue(propDefMap.containsKey("StringPropTopLevel"));
        assertTrue(propDefMap.get("StringPropTopLevel").isInherited());
        assertTrue(propDefMap.containsKey("StringPropLevel1"));
        assertTrue(propDefMap.get("StringPropLevel1").isInherited());
        assertTrue(propDefMap.containsKey("StringPropLevel2"));
        assertFalse(propDefMap.get("StringPropLevel2").isInherited());
        containsAllBasePropertyDefinitions(typeContainer);

        log.info("... testInheritedProperties() finished.");
    }

    private String getRepositoryId() {
        List<RepositoryInfo> repositories = fRepSvc.getRepositoryInfos(null);
        RepositoryInfo repository = repositories.get(0);
        assertNotNull(repository);
        return repository.getId();
    }

    private boolean containsTypeByIdRecursive(String typeId, List<TypeDefinitionContainer> types) {
        for (TypeDefinitionContainer type : types) {
            if (containsTypeByIdRecursive(typeId, type)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsTypeByIdRecursive(String typeId, TypeDefinitionContainer typeContainer) {
        if (typeId.equals(typeContainer.getTypeDefinition().getId())) {
            return true;
        }

        for (TypeDefinitionContainer type : typeContainer.getChildren()) {
            if (containsTypeByIdRecursive(typeId, type)) {
                return true;
            }
        }
        return false;
    }

    static void containsAllBasePropertyDefinitions(TypeDefinition typeDef) {
        Map<String, PropertyDefinition<?>> propDefs = typeDef.getPropertyDefinitions();
        String baseTypeId = typeDef.getBaseTypeId().value();

        if (!typeDef.getId().equals(BaseTypeId.CMIS_SECONDARY.value())) {
            assertTrue(propDefs.containsKey(PropertyIds.NAME));
            assertTrue(propDefs.containsKey(PropertyIds.OBJECT_ID));
            assertTrue(propDefs.containsKey(PropertyIds.OBJECT_TYPE_ID));
            assertTrue(propDefs.containsKey(PropertyIds.BASE_TYPE_ID));
            assertTrue(propDefs.containsKey(PropertyIds.CREATED_BY));
            assertTrue(propDefs.containsKey(PropertyIds.CREATION_DATE));
            assertTrue(propDefs.containsKey(PropertyIds.LAST_MODIFIED_BY));
            assertTrue(propDefs.containsKey(PropertyIds.LAST_MODIFICATION_DATE));
            assertTrue(propDefs.containsKey(PropertyIds.CHANGE_TOKEN));
        }

        if (baseTypeId.equals(BaseTypeId.CMIS_DOCUMENT.value())) {
            assertTrue(propDefs.containsKey(PropertyIds.IS_IMMUTABLE));
            assertTrue(propDefs.containsKey(PropertyIds.IS_LATEST_VERSION));
            assertTrue(propDefs.containsKey(PropertyIds.IS_MAJOR_VERSION));
            assertTrue(propDefs.containsKey(PropertyIds.IS_LATEST_MAJOR_VERSION));
            assertTrue(propDefs.containsKey(PropertyIds.VERSION_LABEL));
            assertTrue(propDefs.containsKey(PropertyIds.VERSION_SERIES_ID));
            assertTrue(propDefs.containsKey(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT));
            assertTrue(propDefs.containsKey(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY));
            assertTrue(propDefs.containsKey(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID));
            assertTrue(propDefs.containsKey(PropertyIds.CHECKIN_COMMENT));
            assertTrue(propDefs.containsKey(PropertyIds.CONTENT_STREAM_LENGTH));
            assertTrue(propDefs.containsKey(PropertyIds.CONTENT_STREAM_MIME_TYPE));
            assertTrue(propDefs.containsKey(PropertyIds.CONTENT_STREAM_FILE_NAME));
            assertTrue(propDefs.containsKey(PropertyIds.CONTENT_STREAM_ID));
            assertTrue(propDefs.containsKey(PropertyIds.CHANGE_TOKEN));
        } else if (baseTypeId.equals(BaseTypeId.CMIS_FOLDER.value())) {
            assertTrue(propDefs.containsKey(PropertyIds.PARENT_ID));
            assertTrue(propDefs.containsKey(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS));
            assertTrue(propDefs.containsKey(PropertyIds.PATH));
        } else if (baseTypeId.equals(BaseTypeId.CMIS_POLICY.value())) {
            assertTrue(propDefs.containsKey(PropertyIds.POLICY_TEXT));
        } else if (baseTypeId.equals(BaseTypeId.CMIS_RELATIONSHIP.value())) {
            assertTrue(propDefs.containsKey(PropertyIds.SOURCE_ID));
            assertTrue(propDefs.containsKey(PropertyIds.TARGET_ID));
        } else if (baseTypeId.equals(BaseTypeId.CMIS_ITEM.value())) {
        } else if (baseTypeId.equals(BaseTypeId.CMIS_SECONDARY.value())) {
        } else {
            fail("Unknown base type id in type definition");
        }
    }

    private int getRecursiveSize(List<TypeDefinitionContainer> types) {
        if (null == types) {
            return 0;
        }

        int size = types.size();
        for (TypeDefinitionContainer type : types) {
            size += getRecursiveSize(type.getChildren());
        }

        return size;
    }

    private List<TypeDefinition> getTypeDefsFlattened(List<TypeDefinitionContainer> types) {
        List<TypeDefinition> flattened = new ArrayList<TypeDefinition>();

        for (TypeDefinitionContainer type : types) {
            flattened.add(type.getTypeDefinition());
            if (null != type.getChildren()) {
                List<TypeDefinition> children = getTypeDefsFlattened(type.getChildren());
                flattened.addAll(children);
            }
        }

        return flattened;
    }

}
