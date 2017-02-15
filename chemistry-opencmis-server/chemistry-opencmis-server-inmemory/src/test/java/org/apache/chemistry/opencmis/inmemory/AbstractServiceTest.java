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
package org.apache.chemistry.opencmis.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.AclService;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.apache.chemistry.opencmis.commons.spi.DiscoveryService;
import org.apache.chemistry.opencmis.commons.spi.MultiFilingService;
import org.apache.chemistry.opencmis.commons.spi.NavigationService;
import org.apache.chemistry.opencmis.commons.spi.ObjectService;
import org.apache.chemistry.opencmis.commons.spi.PolicyService;
import org.apache.chemistry.opencmis.commons.spi.RelationshipService;
import org.apache.chemistry.opencmis.commons.spi.RepositoryService;
import org.apache.chemistry.opencmis.commons.spi.VersioningService;
import org.apache.chemistry.opencmis.inmemory.server.InMemoryServiceFactoryImpl;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.ContentStreamDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceTest.class);

    protected static final String REPOSITORY_ID = "UnitTestRepository";
    protected BindingsObjectFactory fFactory = new BindingsObjectFactoryImpl();
    protected String fRootFolderId;
    protected String fRepositoryId;
    protected ObjectService fObjSvc;
    protected NavigationService fNavSvc;
    protected RepositoryService fRepSvc;
    protected VersioningService fVerSvc;
    protected MultiFilingService fMultiSvc;
    protected DiscoveryService fDiscSvc;
    protected AclService fAclSvc;
    protected RelationshipService fRelSvc;
    protected PolicyService fPolSvc;

    protected CallContext fTestCallContext;
    private String fTypeCreatorClassName;
    private CmisBinding binding;

    public AbstractServiceTest() {
        // The in-memory server unit tests can either be run directly against
        // the
        // service implementation or against a clocal binding interface. The
        // local
        // binding interfaces offers some benefits like type system caching etc.
        // The default is using the direct implementation. Subclasses may
        // override this behavior.

        // Init with default types, can be overridden by subclasses:
        fTypeCreatorClassName = UnitTestTypeSystemCreator.class.getName();
    }

    // Subclasses may want to use their own types
    protected void setTypeCreatorClass(String typeCreatorClassName) {
        fTypeCreatorClassName = typeCreatorClassName;
    }

    protected String getTypeCreatorClass() {
        return fTypeCreatorClassName;
    }

    protected void setUp() {
    	setUp(false);
    }
    
    protected void setUp(boolean relaxedParserMode) {
        LOG.debug("Initializing InMemory Test with type creator class: " + fTypeCreatorClassName);
        Map<String, String> parameters = new HashMap<String, String>();

        // attach repository info to the session:
        parameters.put(ConfigConstants.TYPE_CREATOR_CLASS, fTypeCreatorClassName);
        parameters.put(ConfigConstants.REPOSITORY_ID, REPOSITORY_ID);
        if (relaxedParserMode) {
        	parameters.put(ConfigConstants.PARSER_MODE, "ParserModeRelaxed");
        }

        // give subclasses a chance to provide additional parameters for special
        // tests
        addParameters(parameters);

        fTestCallContext = new DummyCallContext();
        // Attach a standard CallContext to a thread before the services are
        // initialized.
        // RuntimeContext.attachCfg(fTestCallContext);

        initializeUsingLocalBinding(parameters);

        assertNotNull(fRepSvc);
        assertNotNull(fObjSvc);
        assertNotNull(fNavSvc);

        RepositoryInfo rep = fRepSvc.getRepositoryInfo(REPOSITORY_ID, null);
        fRootFolderId = rep.getRootFolderId();
        fRepositoryId = rep.getId();

        assertNotNull(fRepositoryId);
        assertNotNull(fRootFolderId);
    }

    // Override this method in subclasses if you want to provide additional
    // configuration
    // parameters. Default implementation is empty
    protected void addParameters(Map<String, String> parameters) {
    }

    protected void tearDown() {
        LOG.debug("Close local binding.");
        binding.close();
        LOG.debug("Local binding closed.");
    }

    public void testDummy() {
        // dummy test to make tools happy that complain if there are no tests
        // available in a test class
    }

    protected String createFolder(String folderName, String parentFolderId, String typeId) {
        String id = null;
        try {
            id = createFolderNoCatch(folderName, parentFolderId, typeId);
            if (null == id) {
                fail("createFolder failed.");
            }
        } catch (Exception e) {
            fail("createFolder() failed with exception: " + e);
        }
        return id;
    }

    protected String createFolderNoCatch(String folderName, String parentFolderId, String typeId) {
        return createFolderNoCatch(folderName, parentFolderId, typeId, null, null);
    }

    protected String createFolderNoCatch(String folderName, String parentFolderId, String typeId, Acl addACEs,
            Acl removeACEs) {
        Properties props = createFolderProperties(folderName, typeId);
        String id = fObjSvc.createFolder(fRepositoryId, props, parentFolderId, null, addACEs, removeACEs, null);
        return id;
    }

    protected String createDocumentNoCatch(String name, String folderId, String typeId,
            VersioningState versioningState, boolean withContent) {
        return createDocumentNoCatch(name, folderId, typeId, versioningState, withContent, null, null);
    }

    protected String createDocumentNoCatch(String name, String folderId, String typeId,
            VersioningState versioningState, boolean withContent, Acl addACEs, Acl removeACEs) {
        ContentStream contentStream = null;
        List<String> policies = null;
        ExtensionsData extension = null;

        Properties props = createDocumentProperties(name, typeId);

        if (withContent) {
            contentStream = createContent();
        }

        String id = fObjSvc.createDocument(fRepositoryId, props, folderId, contentStream, versioningState, policies,
                addACEs, removeACEs, extension);
        return id;
    }

    protected String createDocument(String name, String folderId, String typeId, VersioningState versioningState,
            boolean withContent) {
        String id = null;
        try {
            id = createDocumentNoCatch(name, folderId, typeId, versioningState, withContent);
            if (null == id) {
                fail("createDocument failed.");
            }
        } catch (Exception e) {
            fail("createDocument() failed with exception: " + e);
        }
        return id;
    }

    protected String createDocument(String name, String folderId, String typeId, boolean withContent) {
        VersioningState versioningState = VersioningState.NONE;
        return createDocument(name, folderId, typeId, versioningState, withContent);
    }

    protected Properties createDocumentProperties(String name, String typeId) {
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, name));
        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, typeId));
        Properties props = fFactory.createPropertiesData(properties);
        return props;
    }

    protected Properties createFolderProperties(String folderName, String typeId) {
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, folderName));
        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, typeId));
        Properties props = fFactory.createPropertiesData(properties);
        return props;
    }

    protected ContentStream createContent() {
        return createContent(32);
    }

    protected ContentStream createContent(int sizeInKB) {
        return createContent(sizeInKB, 0, null);
    }

    protected ContentStream createContent(int sizeInKB, long maxSizeInKB, String mimeType) {
        ContentStreamDataImpl content = new ContentStreamDataImpl(maxSizeInKB);
        content.setFileName("data.txt");

        if (null == mimeType) {
            content.setMimeType("text/plain");
        } else {
            content.setMimeType(mimeType);
        }
        int len = sizeInKB * 1024;
        byte[] b = { 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x0c, 0x0a,
                0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x0c, 0x0a }; // 32
        int noBlocks = len / b.length;

        ByteArrayOutputStream ba = new ByteArrayOutputStream(len);
        try {
            for (int i = 0; i < noBlocks; i++) {
                ba.write(b);
            }
            content.setContent(new ByteArrayInputStream(ba.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to fill content stream with data", e);
        }
        return content;
    }

    protected ContentStream createContent(char ch) {
        ContentStreamDataImpl content = new ContentStreamDataImpl(0);
        content.setFileName("data.txt");
        content.setMimeType("text/plain");
        int len = 32 * 1024;
        byte[] b = new byte[32];
        for (int i = 0; i < 32; i++) {
            b[i] = (byte) Character.getNumericValue(ch);
        }
        ByteArrayOutputStream ba = new ByteArrayOutputStream(len);
        try {
            for (int i = 0; i < 1024; i++) {
                ba.write(b);
            }
            content.setContent(new ByteArrayInputStream(ba.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to fill content stream with data", e);
        }
        return content;
    }

    protected void verifyContentResult(ContentStream sd) {
        verifyContentResult(sd, 32);
    }

    protected void verifyContentResult(ContentStream sd, int sizeInK) {
        assertEquals("text/plain", sd.getMimeType());
        assertEquals("data.txt", sd.getFileName());
        assertEquals(sizeInK * 1024, sd.getBigLength().longValue());
        byte[] ba = new byte[32];
        InputStream is = sd.getStream();
        int counter = 0;
        try {
            while (is.read(ba) == ba.length) {
                ++counter;
                assertEquals(0x61, ba[0]);
                assertEquals(0x6e, ba[29]);
                assertEquals(0x0c, ba[30]);
                assertEquals(0x0a, ba[31]);
            }
        } catch (IOException e) {
            fail("reading from content stream failed");
        }
        assertEquals(sizeInK * 1024 / 32, counter);
    }

    protected String getByPath(String id, String path) {
        ObjectData res = null;
        try {
            res = fObjSvc.getObjectByPath(fRepositoryId, path, "*", false, IncludeRelationships.NONE, null, false,
                    false, null);
            assertNotNull(res);
            assertEquals(id, res.getId());
        } catch (Exception e) {
            fail("getObject() failed with exception: " + e);
        }
        return res.getId();
    }

    @SuppressWarnings("unchecked")
    protected String getPathOfFolder(String id) {
        String path = null;
        try {
            String filter = PropertyIds.PATH;
            Properties res = fObjSvc.getProperties(fRepositoryId, id, filter, null);
            assertNotNull(res);
            PropertyData<String> pd = (PropertyData<String>) res.getProperties().get(PropertyIds.PATH);
            assertNotNull(pd);
            path = pd.getFirstValue();
            assertNotNull(path);
        } catch (Exception e) {
            fail("getProperties() failed with exception: " + e);
        }
        return path;
    }

    @SuppressWarnings("unchecked")
    protected String getPathOfDocument(String id) {
        String path = null;
        String filter = "*";
        List<ObjectParentData> parentData = fNavSvc.getObjectParents(fRepositoryId, id, filter, false,
                IncludeRelationships.NONE, null, true, null);
        String name = parentData.get(0).getRelativePathSegment();
        PropertyData<String> pd = (PropertyData<String>) parentData.get(0).getObject().getProperties().getProperties()
                .get(PropertyIds.PATH);
        assertNotNull(pd);
        path = pd.getFirstValue() + "/" + name;
        return path;
    }

    protected ObjectData getDocumentObjectData(String id) {
        ObjectData res = null;
        try {
            String returnedId = null;
            res = fObjSvc.getObject(fRepositoryId, id, "*", false, IncludeRelationships.NONE, null, false, false, null);
            assertNotNull(res);
            returnedId = res.getId();
            testReturnedProperties(returnedId, res.getProperties().getProperties());
            assertEquals(id, returnedId);
        } catch (Exception e) {
            fail("getObject() failed with exception: " + e);
        }
        return res;
    }

    protected String getDocument(String id) {
        ObjectData res = getDocumentObjectData(id);
        assertNotNull(res);
        return res.getId();
    }

    protected void testReturnedProperties(String objectId, Map<String, PropertyData<?>> props) {
        for (PropertyData<?> pd : props.values()) {
            LOG.debug("return property id: " + pd.getId() + ", value: " + pd.getValues());
        }

        PropertyData<?> pd = props.get(PropertyIds.OBJECT_ID);
        assertNotNull(pd);
        assertEquals(objectId, pd.getFirstValue());
    }

    /**
     * Instantiates the services by using the client provider interface.
     * 
     * @param parameters
     *            configuration parameters for client provider interface and
     *            in-memory provider
     */
    protected void initializeUsingLocalBinding(Map<String, String> parameters) {

        LOG.info("Initialize unit test using the local binding interface.");

        // add parameters for local binding:
        parameters.put(SessionParameter.BINDING_SPI_CLASS, SessionParameter.LOCAL_FACTORY);
        parameters.put(SessionParameter.LOCAL_FACTORY, InMemoryServiceFactoryImpl.class.getName());
        parameters.put(ConfigConstants.OVERRIDE_CALL_CONTEXT, "true");
        InMemoryServiceFactoryImpl.setOverrideCallContext(fTestCallContext);

        // get factory and create binding
        CmisBindingFactory factory = CmisBindingFactory.newInstance();
        binding = factory.createCmisLocalBinding(parameters);
        assertNotNull(binding);
        fFactory = binding.getObjectFactory();
        fRepSvc = binding.getRepositoryService();
        fObjSvc = binding.getObjectService();
        fNavSvc = binding.getNavigationService();
        fVerSvc = binding.getVersioningService();
        fMultiSvc = binding.getMultiFilingService();
        fDiscSvc = binding.getDiscoveryService();
        fAclSvc = binding.getAclService();
        fRelSvc = binding.getRelationshipService();
        fPolSvc = binding.getPolicyService();
    }

    protected String getStringProperty(ObjectData objData, String propertyKey) {
        PropertyData<? extends Object> pd = objData.getProperties().getProperties().get(PropertyIds.PATH);
        assertNotNull(pd.getFirstValue());
        assertTrue(pd.getFirstValue() instanceof String);
        return (String) pd.getFirstValue();
    }

    protected void deleteDocument(String docId) {
        fObjSvc.deleteObject(fRepositoryId, docId, true, null);
    }

}
