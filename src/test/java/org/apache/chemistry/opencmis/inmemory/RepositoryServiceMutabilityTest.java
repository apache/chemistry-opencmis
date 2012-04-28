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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.inmemory.server.InMemoryObjectServiceImpl;
import org.apache.chemistry.opencmis.inmemory.server.InMemoryRepositoryServiceImpl;
import org.apache.chemistry.opencmis.inmemory.server.InMemoryServiceFactoryImpl;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.chemistry.opencmis.inmemory.types.InMemoryDocumentTypeDefinition;
import org.apache.chemistry.opencmis.inmemory.types.PropertyCreationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Temporary test class until CMIS 1.1 bindings are completed. Until then
 * we use a special setup procedure to directly connect to the repository
 * service implementation of InMemory.
 * 
 * @author Jens
 */
public class RepositoryServiceMutabilityTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(RepositoryServiceTest.class);
    private static final String REPOSITORY_ID = "UnitTestRepository";
    private static final String TYPE_ID_MUTABILITY = "BookTypeAddedLater";
    private static final String PROPERTY_ID_TITLE = "Title";
    private static final String PROPERTY_ID_NUMBER = "Number";

    private InMemoryRepositoryServiceImpl repSvc;
    private InMemoryObjectServiceImpl objSvc;

    @Override
    @Before
    public void setUp() {
        super.setTypeCreatorClass(UnitTestTypeSystemCreator.class.getName());
        super.setUp();
        
        Map<String, String> parameters = new HashMap<String, String>();

        // attach repository info to the session:
        parameters.put(ConfigConstants.TYPE_CREATOR_CLASS, getTypeCreatorClass());
        parameters.put(ConfigConstants.REPOSITORY_ID, REPOSITORY_ID);
        
        InMemoryServiceFactoryImpl factory = new InMemoryServiceFactoryImpl();
        factory.init(parameters);
        StoreManager storeManager = factory.getStoreManger();
        repSvc = new InMemoryRepositoryServiceImpl(storeManager);
        objSvc = new InMemoryObjectServiceImpl(storeManager);
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    // This test is just added because this test class uses a different setup to connect to the
    // server as long as the server bindings do not support the type mutability extension of 
    // CMIS 1.1. If this test fails then the setUp() fails! 
    @Test
    public void testRepositoryInfo() {
        log.info("starting testRepositoryInfo() ...");
        List<RepositoryInfo> repositories = repSvc.getRepositoryInfos(fTestCallContext, null);
        assertNotNull(repositories);
        assertFalse(repositories.isEmpty());

        log.info("geRepositoryInfo(), found " + repositories.size() + " repository/repositories).");

        for (RepositoryInfo repository : repositories) {
            RepositoryInfo repository2 = repSvc.getRepositoryInfo(fTestCallContext, repository.getId(), null);
            assertNotNull(repository2);
            assertEquals(repository.getId(), repository2.getId());
            log.info("found repository" + repository2.getId());
        }

        log.info("... testRepositoryInfo() finished.");
    }
    

    @Test
    public void testTypeMutabilityCreation() throws Exception {
        log.info("");
        log.info("starting testTypeMutabilityCreation() ...");
        TypeDefinition typeDefRef = createTypeForAddingAtRuntime();
        String repositoryId = getRepositoryId();
        // add type.
        repSvc.createTypeDefinition(repositoryId, new Holder<TypeDefinition>(typeDefRef), null);
        TypeDefinition type = repSvc.getTypeDefinition(fTestCallContext, repositoryId, typeDefRef.getId(), null);
        assertEquals(typeDefRef.getId(), type.getId());
        assertEquals(typeDefRef.getDescription(), type.getDescription());
        assertEquals(typeDefRef.getDisplayName(), type.getDisplayName());
        assertEquals(typeDefRef.getLocalName(), type.getLocalName());
        assertEquals(typeDefRef.getLocalNamespace(), type.getLocalNamespace());
        RepositoryServiceTest.containsAllBasePropertyDefinitions(type);
        log.info("... testTypeMutabilityCreation() finished.");
    }
    
    @Test
    public void testTypeMutabilityCreateDuplicate() throws Exception {
        log.info("");
        log.info("starting testTypeMutabilityCreateDuplicate() ...");
        TypeDefinition typeDefRef = createTypeForAddingAtRuntime();
        String repositoryId = getRepositoryId();
        // add type.
        repSvc.createTypeDefinition(repositoryId, new Holder<TypeDefinition>(typeDefRef), null);
        // add type again should fail
        checkAddingType(repositoryId, typeDefRef, CmisInvalidArgumentException.class);
        // type should still exist then
        TypeDefinition type = repSvc.getTypeDefinition(fTestCallContext, repositoryId, typeDefRef.getId(), null);
        assertEquals(typeDefRef.getId(), type.getId());
        log.info("... testTypeMutabilityCreateDuplicate() finished.");
    }
    
    
    @Test
    public void testTypeMutabilityTypeNameConstraints() throws Exception {
        log.info("starting testTypeMutabilityTypeNameConstraints() ...");
        
        String repositoryId = getRepositoryId();
        
        // test illegal type id
        InMemoryDocumentTypeDefinition typeDefRef = createTypeForAddingAtRuntime();
        typeDefRef.setId(typeDefRef.getId() + "!!!");
        checkAddingType(repositoryId, typeDefRef, CmisInvalidArgumentException.class);

        // test illegal parent type id
        typeDefRef = createTypeForAddingAtRuntime();
        typeDefRef.setParentTypeId("NonExistingParentType");
        checkAddingType(repositoryId, typeDefRef, CmisInvalidArgumentException.class);

        // test null type id
        typeDefRef = createTypeForAddingAtRuntime();
        typeDefRef.setId(null);
        checkAddingType(repositoryId, typeDefRef, CmisInvalidArgumentException.class);
        
        // test null query name
        typeDefRef = createTypeForAddingAtRuntime();
        typeDefRef.setQueryName(null);
        checkAddingType(repositoryId, typeDefRef, CmisInvalidArgumentException.class);

        // test illegal query name
        typeDefRef = createTypeForAddingAtRuntime();
        typeDefRef.setQueryName(typeDefRef.getQueryName() + "!!!");
        checkAddingType(repositoryId, typeDefRef, CmisInvalidArgumentException.class);

        // test null local name
        typeDefRef = createTypeForAddingAtRuntime();
        typeDefRef.setLocalName(null);
        checkAddingType(repositoryId, typeDefRef, CmisInvalidArgumentException.class);

        // test illegal local name
        typeDefRef = createTypeForAddingAtRuntime();
        typeDefRef.setLocalName(typeDefRef.getLocalName() + "!!!");
        checkAddingType(repositoryId, typeDefRef, CmisInvalidArgumentException.class);

        log.info("... testTypeMutabilityTypeNameConstraints() finished.");              
    }
    
    @Test
    public void testTypeMutabilityPropertyNameConstraints() throws Exception {
        log.info("starting testTypeMutabilityPropertyNameConstraints() ...");
        
        String repositoryId = getRepositoryId();
        
        // test null property id
        InMemoryDocumentTypeDefinition typeDef = createTypeForAddingAtRuntime();
        PropertyStringDefinitionImpl pd = getPropertyDefinitionImpl(typeDef);
        pd.setId(null);
        checkAddingType(repositoryId, typeDef, CmisInvalidArgumentException.class);
        
        // test illegal property id
        typeDef = createTypeForAddingAtRuntime();
        pd = getPropertyDefinitionImpl(typeDef);
        pd.setQueryName(pd.getQueryName() + "!*!");
        checkAddingType(repositoryId, typeDef, CmisInvalidArgumentException.class);

        // test null property query name
        typeDef = createTypeForAddingAtRuntime();
        pd = getPropertyDefinitionImpl(typeDef);
        pd.setQueryName(null);
        checkAddingType(repositoryId, typeDef, CmisInvalidArgumentException.class);

        // test illegal property query name
        typeDef = createTypeForAddingAtRuntime();
        pd = getPropertyDefinitionImpl(typeDef);
        pd.setQueryName(pd.getQueryName() + "!!!");
        checkAddingType(repositoryId, typeDef, CmisInvalidArgumentException.class);

        // test null property local name
        typeDef = createTypeForAddingAtRuntime();
        pd = getPropertyDefinitionImpl(typeDef);
        pd.setLocalName(null);
        checkAddingType(repositoryId, typeDef, CmisInvalidArgumentException.class);

        // test illegal property local name
        typeDef = createTypeForAddingAtRuntime();
        pd = getPropertyDefinitionImpl(typeDef);
        pd.setLocalName(typeDef.getLocalName() + "!!!");
        checkAddingType(repositoryId, typeDef, CmisInvalidArgumentException.class);

        log.info("... testTypeMutabilityPropertyNameConstraints() finished.");              
    }
    
    private void checkAddingType(String repositoryId, TypeDefinition typeDef, Class<? extends Exception> clazz) {
        try { 
            repSvc.createTypeDefinition(repositoryId, new Holder<TypeDefinition>(typeDef), null);
            fail("Illegal type should throw a " + clazz.getName());
        } catch (RuntimeException e) {
            assertTrue("Illegal type name threw wrong exception type (should be a " + clazz.getName() + ")",
                    clazz.isInstance(e));
        }        
    }
    
    @Test
    public void testTypeMutabilityUpdate() throws Exception {
        log.info("");
        log.info("starting testTypeMutabilityUpdate() ...");
        TypeDefinition typeDefRef = createTypeForAddingAtRuntime();
        String repositoryId = getRepositoryId();
        repSvc.createTypeDefinition(repositoryId, new Holder<TypeDefinition>(typeDefRef), null);
        // update type.
        try {
            repSvc.updateTypeDefinition(repositoryId, new Holder<TypeDefinition>(typeDefRef), null);
            fail("updating a type should throw exception.");
        } catch (Exception e) {
            assert(e instanceof CmisNotSupportedException);
        }
        log.info("... testTypeMutabilityUpdate() finished.");
    }
   
    @Test
    public void testTypeMutabilityDeletion() throws Exception {
        log.info("");
        log.info("starting testTypeMutabilityDeletion() ...");
        TypeDefinition typeDefRef = createTypeForAddingAtRuntime();
        String repositoryId = getRepositoryId();
        repSvc.createTypeDefinition(repositoryId, new Holder<TypeDefinition>(typeDefRef), null);
        
        String docId = createDoc("Book1", getRootFolderId(REPOSITORY_ID), TYPE_ID_MUTABILITY);
        
        // try deleting type, should fail, because in use.
        try {
            repSvc.deleteTypeDefinition(repositoryId, TYPE_ID_MUTABILITY, null);
            fail("deleting a type which is in use should throw exception.");
        } catch (Exception e) {
            assert(e instanceof CmisInvalidArgumentException);
        }

        objSvc.deleteObject(fTestCallContext, fRepositoryId, docId, true, null);
        
        try {
            repSvc.deleteTypeDefinition(repositoryId, TYPE_ID_MUTABILITY, null);
        } catch (Exception e) {
            fail("deleting a type which is in not in use should not throw exception! Exception is: " + e);
        }
        
        try {
            repSvc.getTypeDefinition(fTestCallContext, repositoryId, TYPE_ID_MUTABILITY, null);
            fail("getting a type after it was deleted should fail.");
        } catch (Exception e) {
        }

        try {
            repSvc.deleteTypeDefinition(repositoryId, BaseTypeId.CMIS_DOCUMENT.name(), null);
            fail("deleting a CMIS base type throw exception.");
        } catch (Exception e) {
            assert(e instanceof CmisInvalidArgumentException);
        }
        try {
            repSvc.deleteTypeDefinition(repositoryId, BaseTypeId.CMIS_FOLDER.name(), null);
            fail("deleting a CMIS base type throw exception.");
        } catch (Exception e) {
            assert(e instanceof CmisInvalidArgumentException);
        }

        log.info("... testTypeMutabilityDeletion() finished.");
    }

    private String getRepositoryId() {
        List<RepositoryInfo> repositories = repSvc.getRepositoryInfos(fTestCallContext, null);
        RepositoryInfo repository = repositories.get(0);
        assertNotNull(repository);
        return repository.getId();
    }

    private String getRootFolderId(String repositoryId) {
        RepositoryInfo repository = repSvc.getRepositoryInfo(fTestCallContext, repositoryId, null);
        assertNotNull(repository);
        return repository.getRootFolderId();
    }

    private PropertyStringDefinitionImpl getPropertyDefinitionImpl(TypeDefinition typeDef) {
        @SuppressWarnings("unchecked")
        PropertyStringDefinitionImpl pd = (PropertyStringDefinitionImpl) typeDef.getPropertyDefinitions().get(PROPERTY_ID_TITLE);
        return pd;
    }
    
    private InMemoryDocumentTypeDefinition createTypeForAddingAtRuntime() {
        
        InMemoryDocumentTypeDefinition cmisLaterType = new InMemoryDocumentTypeDefinition(TYPE_ID_MUTABILITY,
                "Type with two properties", InMemoryDocumentTypeDefinition.getRootDocumentType());

        Map<String, PropertyDefinition<?>> propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();

        PropertyIntegerDefinitionImpl prop1 = PropertyCreationHelper.createIntegerDefinition(PROPERTY_ID_NUMBER,
                "Sample Int Property", Updatability.READWRITE);
        propertyDefinitions.put(prop1.getId(), prop1);

        PropertyStringDefinitionImpl prop2 = PropertyCreationHelper.createStringDefinition(PROPERTY_ID_TITLE,
                "Sample String Property", Updatability.READWRITE);
        propertyDefinitions.put(prop2.getId(), prop2);
        
        cmisLaterType.addCustomPropertyDefinitions(propertyDefinitions);
        
        return cmisLaterType;
    }

    String createDoc(String name, String folderId, String typeId) {
        ContentStream contentStream = null;
        List<String> policies = null;
        ExtensionsData extension = null;

        Properties props = createDocumentProperties(name, typeId);

        String id = objSvc.createDocument(fTestCallContext, fRepositoryId, props, folderId, contentStream,
                VersioningState.NONE, policies, null, null, extension);
        return id;        
    }
}
