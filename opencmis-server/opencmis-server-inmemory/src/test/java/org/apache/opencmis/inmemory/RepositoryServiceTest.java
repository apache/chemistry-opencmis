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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.api.Choice;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.api.TypeDefinitionList;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.enums.CapabilityAcl;
import org.apache.opencmis.commons.enums.CapabilityChanges;
import org.apache.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.opencmis.commons.enums.CapabilityJoin;
import org.apache.opencmis.commons.enums.CapabilityQuery;
import org.apache.opencmis.commons.enums.CapabilityRendition;
import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.impl.dataobjects.ChoiceImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyHtmlDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyUriDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.RepositoryInfoDataImpl;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.inmemory.types.DocumentTypeCreationHelper;
import org.apache.opencmis.inmemory.types.InMemoryDocumentTypeDefinition;
import org.apache.opencmis.inmemory.types.PropertyCreationHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Jens
 */
public class RepositoryServiceTest extends AbstractServiceTst {

//  private CmisProvider fProvider;

  private static Log log = LogFactory.getLog(RepositoryServiceTest.class);
  private static final String REPOSITORY_ID = "UnitTestRepository";

  static public class UnitTestRepositoryInfo implements RepositoryInfoCreator {

    public RepositoryInfoData createRepositoryInfo() {
      RepositoryCapabilitiesDataImpl caps = new RepositoryCapabilitiesDataImpl();
      caps.setAllVersionsSearchable(false);
      caps.setCapabilityAcl(CapabilityAcl.NONE);
      caps.setCapabilityChanges(CapabilityChanges.NONE);
      caps.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.ANYTIME);
      caps.setCapabilityJoin(CapabilityJoin.NONE);
      caps.setCapabilityQuery(CapabilityQuery.NONE);
      caps.setCapabilityRendition(CapabilityRendition.NONE);
      caps.setIsPwcSearchable(false);
      caps.setIsPwcUpdatable(true);
      caps.setSupportsGetDescendants(true);
      caps.setSupportsGetFolderTree(true);
      caps.setSupportsMultifiling(false);
      caps.setSupportsUnfiling(true);
      caps.setSupportsVersionSpecificFiling(false);
      
      RepositoryInfoDataImpl repositoryInfo = new RepositoryInfoDataImpl();     
      repositoryInfo.setRepositoryId(REPOSITORY_ID);
      repositoryInfo.setRepositoryName("InMemory Repository");
      repositoryInfo.setRepositoryDescription("InMemory Test Repository");
      repositoryInfo.setCmisVersionSupported("0.7");
      repositoryInfo.setRepositoryCapabilities(caps);
      repositoryInfo.setRootFolder("/");
      repositoryInfo.setAclCapabilities(null);
      repositoryInfo.setPrincipalAnonymous("anonymous");
      repositoryInfo.setPrincipalAnyone("anyone");
      repositoryInfo.setThinClientUri(null);
      repositoryInfo.setChangesIncomplete(Boolean.TRUE);
      repositoryInfo.setChangesOnType(null);
      repositoryInfo.setLatestChangeLogToken(null);
      repositoryInfo.setVendorName("OpenCMIS");
      repositoryInfo.setProductName("OpenCMIS Client");
      repositoryInfo.setProductVersion("0.1");
      return repositoryInfo;
    }    
  }
  
  @Before
  public void setUp() throws Exception {
    super.setTypeCreatorClass(RepositoryTestTypeSystemCreator.class.getName());
    super.setUp();
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  public void testRepositoryInfo() throws Exception {
    log.info("starting testRepositoryInfo() ...");
    List<RepositoryInfoData> repositories = fRepSvc.getRepositoryInfos(
        null);
    assertNotNull(repositories);
    assertFalse(repositories.isEmpty());

    log.info("geRepositoryInfo(), found " + repositories.size() + " repository/repositories).");

    for (RepositoryInfoData repository : repositories) {
      RepositoryInfoData repository2 = fRepSvc.getRepositoryInfo(
          repository.getRepositoryId(), null);
      assertNotNull(repository2);
      assertEquals(repository.getRepositoryId(), repository2.getRepositoryId());
      log.info("found repository" + repository2.getRepositoryId());
    }

    log.info("... testRepositoryInfo() finished.");
  }

  @Test
  public void testTypeDefinition() throws Exception {
    log.info("");
    log.info("starting testTypeDefinition() ...");
    String repositoryId = getRepositoryId();
    String typeId = "MyDocType1";
    TypeDefinition ref = RepositoryTestTypeSystemCreator.getTypeById(typeId);
    TypeDefinition type = fRepSvc.getTypeDefinition(repositoryId,
        typeId, null);
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
    List<TypeDefinitionContainer> types = fRepSvc.getTypeDescendants(
        repositoryId, null/* all types */, BigInteger.valueOf(-1), Boolean.TRUE, null);
    assertNotNull(types);
    log.info("Repository " + repositoryId + " contains " + types.size() + " type(s).");
    
    
    // check that we got all types
    int expectedSize = RepositoryTestTypeSystemCreator.getTypesList().size()
        + DocumentTypeCreationHelper.getDefaultTypes().size();
    int totalSize = getRecursiveSize(types);

    assertEquals(expectedSize, totalSize);
    assertEquals(2, types.size());

    for (TypeDefinitionContainer type : types) {
      assertNotNull(type);
      TypeDefinition typeDef = type.getTypeDefinition();
      assertNotNull(typeDef);
      assertNotNull(typeDef.getId());
      assertNotNull(typeDef.getBaseId());
      log.info("Found type: " + typeDef.getId() + ", display name is: " + typeDef.getDisplayName());
      log.info("  Base type is: " + typeDef.getBaseId());
      log.info("  Number of children types is: " + type.getChildren().size());
      Map<String, PropertyDefinition<?>> propDefs = type.getTypeDefinition().getPropertyDefinitions();
      log.info("  Number of properties is: " + (propDefs==null ? 0 : propDefs.size()));
      containsAllBasePropertyDefinitions(typeDef);
    }

    log.info("... testGetAllTypesUnlimitedDepth() finished.");
  }

  @Test
  public void testGetAllTypesLimitedDepth() {
    log.info("");
    log.info("starting testGetAllTypesLimitedDepth()...");
    String repositoryId = getRepositoryId();

    // get types
    int depth = 1;
    List<TypeDefinitionContainer> types = fRepSvc.getTypeDescendants(
        repositoryId, BaseObjectTypeIds.CMIS_DOCUMENT.value(), BigInteger.valueOf(depth), Boolean.TRUE, null);
    assertNotNull(types);
    log.info("Found in repository " + repositoryId + " " + types.size() + " type(s) with depth "
        + depth + ".");

    for (TypeDefinitionContainer type : types) {
      TypeDefinition typeDef = type.getTypeDefinition();
      log.info("Found type: " + typeDef.getId() + ", display name is: " + typeDef.getDisplayName());
      log.info("  Base type is: " + typeDef.getBaseId());
      log.info("  Number of children types is: " + type.getChildren().size());
      containsAllBasePropertyDefinitions(typeDef);
    }

    int totalSize = getRecursiveSize(types);
    assertEquals(4, totalSize); // all RepositoryTestTypeSystemCreator types minus one in level two plus cmis.docment
    assertFalse(containsTypeByIdRecursive(BaseObjectTypeIds.CMIS_DOCUMENT.value(), types));
    assertFalse(containsTypeByIdRecursive(BaseObjectTypeIds.CMIS_FOLDER.value(), types));
    
    assertTrue(containsTypeByIdRecursive("MyDocType1", types));
    assertTrue(containsTypeByIdRecursive("MyDocType2", types));

    assertFalse(containsTypeByIdRecursive(RepositoryTestTypeSystemCreator.LEVEL2_TYPE, types));

    for (TypeDefinitionContainer type : types) {
      assertNotNull(type);
      TypeDefinition typeDef = type.getTypeDefinition();
      assertNotNull(typeDef);
      assertNotNull(typeDef.getId());
      assertNotNull(typeDef.getBaseId());
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
    List<TypeDefinitionContainer> types = fRepSvc.getTypeDescendants(
        repositoryId, typeId, BigInteger.valueOf(depth), Boolean.TRUE, null);
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
      assertNotNull(typeDef.getBaseId());
      log.info("Found type: " + typeDef.getId() + ", display name is: " + typeDef.getDisplayName());
      log.info("  Base type is: " + typeDef.getBaseId());
      log.info("  Number of children types is: " + type.getChildren().size());
      containsAllBasePropertyDefinitions(typeDef);
    }

    log.info("... testGetSpecificTypeLimitedDepth() finished.");
  }
  
  @Test
  public void testGetTypesWithoutProperties() {
    log.info("");
    log.info("starting testGetTypesWithoutProperties()...");
    String repositoryId = getRepositoryId();
    String typeId = BaseObjectTypeIds.CMIS_DOCUMENT.value();

    // get types
    List<TypeDefinitionContainer> types = fRepSvc.getTypeDescendants(
        repositoryId, typeId, BigInteger.valueOf(-1), false, null);
    assertNotNull(types);
    log.info("Repository " + repositoryId + " contains " + types.size() + " type(s).");

    TypeDefinitionContainer typeWithProps = null;
    for (TypeDefinitionContainer type : types) {
      if (type.getTypeDefinition().getId().equals(RepositoryTestTypeSystemCreator.COMPLEX_TYPE)) {
        typeWithProps = type;
        break;
      }
    }
    assertNotNull(typeWithProps);
    TypeDefinition typeDef = typeWithProps.getTypeDefinition();
    Map<String, PropertyDefinition<?>> propDefs = typeDef.getPropertyDefinitions();
    log.info("Found type: " + typeDef.getId() + ", display name is: " + typeDef.getDisplayName());
    log.info("  Base type is: " + typeDef.getBaseId());
    log.info("  Number of properties is: " + (propDefs==null ? 0 : propDefs.size()));
    assertTrue(propDefs==null || propDefs.size()==0);
    log.info("... testGetTypesWithoutProperties() finished.");
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
    TypeDefinitionList children = fRepSvc.getTypeChildren(repositoryId,
        typeId, true, maxItems, skipCount, null);

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
    children = fRepSvc.getTypeChildren(repositoryId, typeId, true,
        maxItems, skipCount, null);

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
  public void testGetWrongParameters() {
    log.info("");
    log.info("starting testGetWrongParameters()...");
    String repositoryId = getRepositoryId();
    String wrongRepositoryId = "NonExistantRepository";

    // get types
    int depth = -1;
    String wrongTypeId = "UnknownType";

    try {
      RepositoryInfoData repInf = fRepSvc.getRepositoryInfo(
          wrongRepositoryId, null);
      log.debug("getRepositoryInfo(): " + repInf);
      fail("getRepositoryInfo() with illegal repository id should throw InvalidArgumentException.");
    }
    catch (CmisInvalidArgumentException e) {
      log.info("getRepositoryInfo() with depth==0 raised expected exception: " + e);
    }

    try {
      List<TypeDefinitionContainer> types = fRepSvc.getTypeDescendants(
          repositoryId, "CMISDocument", BigInteger.valueOf(0), Boolean.TRUE, null);
      log.debug("getTypeDescendants(): " + types);
      fail("getTypeDescendants() with depth 0 should throw InvalidArgumentException.");
    }
    catch (CmisInvalidArgumentException e) {
      log.info("getTypeDescendants() with depth==0 raised expected exception: " + e);
    }

    try {
      List<TypeDefinitionContainer> types = fRepSvc.getTypeDescendants(
          repositoryId, wrongTypeId, BigInteger.valueOf(depth), Boolean.TRUE, null);
      log.debug("getTypeDescendants(): " + types);
      fail("getTypeDescendants() with unknown type should throw exception.");
    }
    catch (CmisInvalidArgumentException e) {
      log.info("getTypeDescendants() with unknown type raised expected exception: " + e);
    }

    try {
      TypeDefinition type = fRepSvc.getTypeDefinition(
          wrongRepositoryId, "CMISDocument", null);
      log.debug("getTypeDefinition(): " + type);
      fail("getTypeDefinition() with unknown repository id should throw exception.");
    }
    catch (CmisInvalidArgumentException e) {
      log.info("getTypeDefinition() with unknown repository id raised expected exception: " + e);
    }

    try {
      TypeDefinition type = fRepSvc.getTypeDefinition(repositoryId,
          wrongTypeId, null);
      log.debug("getTypeDefinition(): " + type);
      fail("getTypeDefinition() with unknown type should throw exception.");
    }
    catch (CmisObjectNotFoundException e) {
      log.info("getTypeDefinition() with unknown type raised expected exception: " + e);
    }

    try {
      TypeDefinitionList types = fRepSvc.getTypeChildren(
          wrongRepositoryId, "CMISDocument", Boolean.TRUE, BigInteger.valueOf(100),
          BigInteger.ZERO, null);
      log.debug("getTypeChildren(): " + types);
      fail("getTypeDescendants() with unknown type should throw InvalidArgumentException.");
    }
    catch (CmisInvalidArgumentException e) {
      log.info("getTypeDescendants() with unknown repository id raised expected exception: " + e);
    }

    try {
      TypeDefinitionList types = fRepSvc.getTypeChildren(repositoryId,
          wrongTypeId, Boolean.TRUE, BigInteger.valueOf(100), BigInteger.ZERO, null);
      log.debug("getTypeChildren(): " + types);
      fail("getTypeDescendants() with unknown type should throw exception.");
    }
    catch (CmisInvalidArgumentException e) {
      log.info("getTypeDescendants() with unknown type raised expected exception: " + e);
    }

    log.info("... testGetUnknownType() testGetWrongParameters.");

  }

  @Test
  public void testInheritedProperties() {
    log.info("");
    log.info("starting testInheritedProperties()...");
    String repositoryId = getRepositoryId();
    String typeId = RepositoryTestTypeSystemCreator.TOPLEVEL_TYPE;

    // get top level type
    TypeDefinition typeContainer = fRepSvc.getTypeDefinition(
        repositoryId, typeId, null);
    assertNotNull(typeContainer);
    Map<String, PropertyDefinition<?>> propDefMap = typeContainer.getPropertyDefinitions();
    assertTrue(propDefMap.containsKey("StringPropTopLevel"));
    assertFalse(propDefMap.get("StringPropTopLevel").isInherited());
    assertFalse(propDefMap.containsKey("StringPropLevel1"));
    assertFalse(propDefMap.containsKey("StringPropLevel2"));
    containsAllBasePropertyDefinitions(typeContainer);
    
    // get level 1 type
    typeId = RepositoryTestTypeSystemCreator.LEVEL1_TYPE;
    typeContainer = fRepSvc.getTypeDefinition(
        repositoryId, typeId, null);
    assertNotNull(typeContainer);
    propDefMap = typeContainer.getPropertyDefinitions();
    assertTrue(propDefMap.containsKey("StringPropTopLevel"));
    assertTrue(propDefMap.get("StringPropTopLevel").isInherited());
    assertTrue(propDefMap.containsKey("StringPropLevel1"));
    assertFalse(propDefMap.get("StringPropLevel1").isInherited());
    assertFalse(propDefMap.containsKey("StringPropLevel2"));
    containsAllBasePropertyDefinitions(typeContainer);
    
    // get level 2 type
    typeId = RepositoryTestTypeSystemCreator.LEVEL2_TYPE;
    typeContainer = fRepSvc.getTypeDefinition(
        repositoryId, typeId, null);
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
    List<RepositoryInfoData> repositories = fRepSvc.getRepositoryInfos(
        null);
    RepositoryInfoData repository = repositories.get(0);
    assertNotNull(repository);
    return repository.getRepositoryId();
  }

  private boolean containsTypeById(String typeId, List<TypeDefinitionContainer> types) {
    for (TypeDefinitionContainer type : types) {
      if (type.getTypeDefinition().getId().equals(typeId))
        return true;
    }
    return false;
  }
  
  private boolean containsTypeByIdRecursive(String typeId, List<TypeDefinitionContainer> types) {
    for (TypeDefinitionContainer type : types) {
      if (containsTypeByIdRecursive(typeId, type))
        return true;
    }
    return false;
  }
  
  private boolean containsTypeByIdRecursive(String typeId, TypeDefinitionContainer typeContainer) {
    if (typeId.equals(typeContainer.getTypeDefinition().getId()))
      return true;
    
    for (TypeDefinitionContainer type : typeContainer.getChildren()) {
      if (containsTypeByIdRecursive(typeId, type))
        return true;      
    }
    return false;
  }

  private void containsAllBasePropertyDefinitions(TypeDefinition typeDef) {
    Map<String, PropertyDefinition<?>> propDefs = typeDef.getPropertyDefinitions();
    String baseTypeId = typeDef.getBaseId().value();
    
    assertTrue(propDefs.containsKey(PropertyIds.CMIS_NAME));
    assertTrue(propDefs.containsKey(PropertyIds.CMIS_OBJECT_ID));
    assertTrue(propDefs.containsKey(PropertyIds.CMIS_OBJECT_TYPE_ID));
    assertTrue(propDefs.containsKey(PropertyIds.CMIS_BASE_TYPE_ID));
    assertTrue(propDefs.containsKey(PropertyIds.CMIS_CREATED_BY));
    assertTrue(propDefs.containsKey(PropertyIds.CMIS_CREATION_DATE));
    assertTrue(propDefs.containsKey(PropertyIds.CMIS_LAST_MODIFIED_BY));
    assertTrue(propDefs.containsKey(PropertyIds.CMIS_LAST_MODIFICATION_DATE));
    assertTrue(propDefs.containsKey(PropertyIds.CMIS_CHANGE_TOKEN));

    if (baseTypeId.equals(BaseObjectTypeIds.CMIS_DOCUMENT.value())) {
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_IS_IMMUTABLE));
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_IS_LATEST_VERSION));
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_IS_MAJOR_VERSION));
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_IS_LATEST_MAJOR_VERSION));
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_VERSION_LABEL));
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_VERSION_SERIES_ID));
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_IS_VERSION_SERIES_CHECKED_OUT));
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_BY));
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_ID));
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_CHECKIN_COMMENT));
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_CONTENT_STREAM_LENGTH));
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_CONTENT_STREAM_MIME_TYPE));
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_CONTENT_STREAM_FILE_NAME));
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_CONTENT_STREAM_ID));
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_CHANGE_TOKEN));      
    } else if (baseTypeId.equals(BaseObjectTypeIds.CMIS_FOLDER.value())) {
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_PARENT_ID));      
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_ALLOWED_CHILD_OBJECT_TYPE_IDS));      
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_PATH));            
    } else if (baseTypeId.equals(BaseObjectTypeIds.CMIS_POLICY.value())) {
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_POLICY_TEXT));            
    } else if (baseTypeId.equals(BaseObjectTypeIds.CMIS_RELATIONSHIP.value())) {
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_SOURCE_ID));      
      assertTrue(propDefs.containsKey(PropertyIds.CMIS_TARGET_ID));            
    } else
      fail("Unknown base type id in type definition");
  }
  
  private int getRecursiveSize(List<TypeDefinitionContainer> types) {
    if (null == types)
      return 0;
    
    int size = types.size();
    for (TypeDefinitionContainer type: types)
      size += getRecursiveSize(type.getChildren());
    
    return size;
  }
  
  public static class RepositoryTestTypeSystemCreator implements TypeCreator {

    public static final String COMPLEX_TYPE = "ComplexType";
    public static final String TOPLEVEL_TYPE = "DocumentTopLevel";
    public static final String LEVEL1_TYPE = "DocumentLevel1";;
    public static final String LEVEL2_TYPE = "DocumentLevel2";
    
    static public List<TypeDefinition> singletonTypes = buildTypesList();
    
    public List<TypeDefinition> createTypesList() {
      return singletonTypes;
    }

    private static List<TypeDefinition> buildTypesList() {
      List<TypeDefinition> typesList = new LinkedList<TypeDefinition>();

      InMemoryDocumentTypeDefinition cmisType1 = new InMemoryDocumentTypeDefinition("MyDocType1",
          "My Type 1 Level 1", InMemoryDocumentTypeDefinition.getRootDocumentType());
      typesList.add(cmisType1);
      

      InMemoryDocumentTypeDefinition cmisType2 = new InMemoryDocumentTypeDefinition("MyDocType2",
          "My Type 2 Level 1", InMemoryDocumentTypeDefinition.getRootDocumentType());
      typesList.add(cmisType2);

      InMemoryDocumentTypeDefinition cmisType11 = new InMemoryDocumentTypeDefinition("MyDocType1.1",
          "My Type 3 Level 2", cmisType1);
      typesList.add(cmisType11);

      InMemoryDocumentTypeDefinition cmisType111 = new InMemoryDocumentTypeDefinition("MyDocType1.1.1",
          "My Type 4 Level 3", cmisType11);
      typesList.add(cmisType111);

      InMemoryDocumentTypeDefinition cmisType112 = new InMemoryDocumentTypeDefinition("MyDocType1.1.2",
          "My Type 5 Level 3", cmisType11);
      typesList.add(cmisType112);

      InMemoryDocumentTypeDefinition cmisType12 = new InMemoryDocumentTypeDefinition("MyDocType1.2",
          "My Type 6 Level 2", cmisType1);
      typesList.add(cmisType12);

      InMemoryDocumentTypeDefinition cmisType21 = new InMemoryDocumentTypeDefinition("MyDocType2.1",
          "My Type 7 Level 2", cmisType2);
      typesList.add(cmisType21);

      InMemoryDocumentTypeDefinition cmisType22 = new InMemoryDocumentTypeDefinition("MyDocType2.2",
          "My Type 8 Level 2", cmisType2);
      typesList.add(cmisType22);
      InMemoryDocumentTypeDefinition cmisType23 = new InMemoryDocumentTypeDefinition("MyDocType2.3",
          "My Type 9 Level 2", cmisType2);
      typesList.add(cmisType23);
      InMemoryDocumentTypeDefinition cmisType24 = new InMemoryDocumentTypeDefinition("MyDocType2.4",
          "My Type 10 Level 2", cmisType2);
      typesList.add(cmisType24);
      InMemoryDocumentTypeDefinition cmisType25 = new InMemoryDocumentTypeDefinition("MyDocType2.5",
          "My Type 11 Level 2", cmisType2);
      typesList.add(cmisType25);

      InMemoryDocumentTypeDefinition cmisType26 = new InMemoryDocumentTypeDefinition("MyDocType2.6",
          "My Type 12 Level 2", cmisType2);
      typesList.add(cmisType26);
      InMemoryDocumentTypeDefinition cmisType27 = new InMemoryDocumentTypeDefinition("MyDocType2.7",
          "My Type 13 Level 2", cmisType2);
      typesList.add(cmisType27);
      InMemoryDocumentTypeDefinition cmisType28 = new InMemoryDocumentTypeDefinition("MyDocType2.8",
          "My Type 14 Level 2", cmisType2);
      typesList.add(cmisType28);
      InMemoryDocumentTypeDefinition cmisType29 = new InMemoryDocumentTypeDefinition("MyDocType2.9",
          "My Type 15 Level 2", cmisType2);
      typesList.add(cmisType29);

      // create a complex type with properties
      InMemoryDocumentTypeDefinition cmisComplexType = new InMemoryDocumentTypeDefinition(COMPLEX_TYPE,
          "Complex type with properties, Level 1", InMemoryDocumentTypeDefinition.getRootDocumentType());
      
      // create a boolean property definition
      
      Map<String, PropertyDefinition<?>> propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();
      
      PropertyDefinition<Boolean> prop = PropertyCreationHelper.createBooleanDefinition("BooleanProp", "Sample Boolean Property");
      propertyDefinitions.put(prop.getId(), prop);
      
      prop = PropertyCreationHelper.createBooleanMultiDefinition("BooleanPropMV", "Sample Boolean multi-value Property");
      propertyDefinitions.put(prop.getId(), prop);

      PropertyDateTimeDefinitionImpl prop2 = PropertyCreationHelper.createDateTimeDefinition("DateTimeProp", "Sample DateTime Property");
      propertyDefinitions.put(prop2.getId(), prop2);
      
      prop2 = PropertyCreationHelper.createDateTimeMultiDefinition("DateTimePropMV", "Sample DateTime multi-value Property");
      propertyDefinitions.put(prop2.getId(), prop2);

      PropertyDecimalDefinitionImpl prop3 = PropertyCreationHelper.createDecimalDefinition("DecimalProp", "Sample Decimal Property");
      propertyDefinitions.put(prop3.getId(), prop3);
      
      prop3 = PropertyCreationHelper.createDecimalDefinition("DecimalPropMV", "Sample Decimal multi-value Property");
      propertyDefinitions.put(prop3.getId(), prop3);

      PropertyHtmlDefinitionImpl prop4 = PropertyCreationHelper.createHtmlDefinition("HtmlProp", "Sample Html Property");
      propertyDefinitions.put(prop4.getId(), prop4);
      
      prop4 = PropertyCreationHelper.createHtmlDefinition("HtmlPropMV", "Sample Html multi-value Property");
      propertyDefinitions.put(prop4.getId(), prop4);

      PropertyIdDefinitionImpl prop5 = PropertyCreationHelper.createIdDefinition("IdProp", "Sample Id Property");
      propertyDefinitions.put(prop5.getId(), prop5);
      
      prop5 = PropertyCreationHelper.createIdDefinition("IdPropMV", "Sample Id Html multi-value Property");
      propertyDefinitions.put(prop5.getId(), prop5);

      PropertyIntegerDefinitionImpl prop6 = PropertyCreationHelper.createIntegerDefinition("IntProp", "Sample Int Property");
      propertyDefinitions.put(prop6.getId(), prop6);
      
      prop6 = PropertyCreationHelper.createIntegerDefinition("IntPropMV", "Sample Int multi-value Property");
      propertyDefinitions.put(prop6.getId(), prop6);

      PropertyStringDefinitionImpl prop7 = PropertyCreationHelper.createStringDefinition("StringProp", "Sample String Property");
      propertyDefinitions.put(prop7.getId(), prop7);
      
      PropertyUriDefinitionImpl prop8 = PropertyCreationHelper.createUriDefinition("UriProp", "Sample Uri Property");
      propertyDefinitions.put(prop8.getId(), prop8);
      
      prop8 = PropertyCreationHelper.createUriDefinition("UriPropMV", "Sample Uri multi-value Property");
      propertyDefinitions.put(prop8.getId(), prop8);

      PropertyStringDefinitionImpl prop9 = PropertyCreationHelper.createStringDefinition("PickListProp", "Sample Pick List Property");
      List<Choice<String>> choiceList = new ArrayList<Choice<String>>();
      ChoiceImpl<String> elem = new ChoiceImpl<String>();
      elem.setValue(Collections.singletonList("red"));
      choiceList.add(elem);
      elem = new ChoiceImpl<String>();
      elem.setValue(Collections.singletonList("green"));
      choiceList.add(elem);
      elem = new ChoiceImpl<String>();
      elem.setValue(Collections.singletonList("blue"));
      choiceList.add(elem);
      elem = new ChoiceImpl<String>();
      elem.setValue(Collections.singletonList("black"));
      choiceList.add(elem);   
      prop9.setChoices(choiceList);
      prop9.setDefaultValue(Collections.singletonList("blue"));
      
      /* try short form: * /
      PropertyCreationHelper.addElemToPicklist(prop9, "red");    
      PropertyCreationHelper.addElemToPicklist(prop9, "green");    
      PropertyCreationHelper.addElemToPicklist(prop9, "blue");    
      PropertyCreationHelper.addElemToPicklist(prop9, "black");
      PropertyCreationHelper.setDefaultValue(prop9, "blue");    
      /* */
      
      cmisComplexType.addCustomPropertyDefinitions(propertyDefinitions);    
      
      // add type to types collection
      typesList.add(cmisComplexType);

      // create a type hierarchy with inherited properties
      InMemoryDocumentTypeDefinition cmisDocTypeTopLevel = new InMemoryDocumentTypeDefinition(TOPLEVEL_TYPE,
          "Document type with properties, Level 1", InMemoryDocumentTypeDefinition.getRootDocumentType());

      InMemoryDocumentTypeDefinition cmisDocTypeLevel1 = new InMemoryDocumentTypeDefinition(LEVEL1_TYPE,
          "Document type with inherited properties, Level 2", cmisDocTypeTopLevel);
      
      InMemoryDocumentTypeDefinition cmisDocTypeLevel2 = new InMemoryDocumentTypeDefinition(LEVEL2_TYPE,
          "Document type with inherited properties, Level 3", cmisDocTypeLevel1);
      
      propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();
      PropertyStringDefinitionImpl propTop = PropertyCreationHelper.createStringDefinition("StringPropTopLevel", "Sample String Property");
      propertyDefinitions.put(propTop.getId(), propTop);
      cmisDocTypeTopLevel.addCustomPropertyDefinitions(propertyDefinitions);    
      
      propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();
      PropertyStringDefinitionImpl propLevel1 = PropertyCreationHelper.createStringDefinition("StringPropLevel1", "String Property Level 1");
      propertyDefinitions.put(propLevel1.getId(), propLevel1);
      cmisDocTypeLevel1.addCustomPropertyDefinitions(propertyDefinitions);    

      propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();
      PropertyStringDefinitionImpl propLevel2 = PropertyCreationHelper.createStringDefinition("StringPropLevel2", "String Property Level 2");
      propertyDefinitions.put(propLevel2.getId(), propLevel2);
      cmisDocTypeLevel2.addCustomPropertyDefinitions(propertyDefinitions);    

      // add type to types collection
      typesList.add(cmisDocTypeTopLevel);
      typesList.add(cmisDocTypeLevel1);
      typesList.add(cmisDocTypeLevel2);
      
      return typesList;
    }
    
    public static List<TypeDefinition> getTypesList() {
      return singletonTypes;
    }
    
    static public TypeDefinition getTypeById(String typeId) {
      for (TypeDefinition typeDef : singletonTypes)
        if (typeDef.getId().equals(typeId))
          return typeDef;
      return null;
    }
    
  }
  
}
