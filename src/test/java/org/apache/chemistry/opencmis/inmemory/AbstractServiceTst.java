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
import org.apache.chemistry.opencmis.commons.api.Acl;
import org.apache.chemistry.opencmis.commons.api.BindingsObjectFactory;
import org.apache.chemistry.opencmis.commons.api.CmisBinding;
import org.apache.chemistry.opencmis.commons.api.ContentStream;
import org.apache.chemistry.opencmis.commons.api.ExtensionsData;
import org.apache.chemistry.opencmis.commons.api.MultiFilingService;
import org.apache.chemistry.opencmis.commons.api.NavigationService;
import org.apache.chemistry.opencmis.commons.api.ObjectData;
import org.apache.chemistry.opencmis.commons.api.ObjectParentData;
import org.apache.chemistry.opencmis.commons.api.ObjectService;
import org.apache.chemistry.opencmis.commons.api.Properties;
import org.apache.chemistry.opencmis.commons.api.PropertyData;
import org.apache.chemistry.opencmis.commons.api.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.api.RepositoryService;
import org.apache.chemistry.opencmis.commons.api.VersioningService;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.inmemory.RepositoryServiceTest.UnitTestRepositoryInfo;
import org.apache.chemistry.opencmis.inmemory.clientprovider.CmisInMemoryProvider;
import org.apache.chemistry.opencmis.inmemory.clientprovider.DummyCallContext;
import org.apache.chemistry.opencmis.inmemory.server.RuntimeContext;
import org.apache.chemistry.opencmis.inmemory.server.InMemoryServiceFactoryImpl;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.ContentStreamDataImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AbstractServiceTst /* extends TestCase */{
	private static Log LOG = LogFactory.getLog(AbstractServiceTst.class);
	protected static final String REPOSITORY_ID = "UnitTestRepository";
	protected BindingsObjectFactory fFactory = new BindingsObjectFactoryImpl();
	protected String fRootFolderId;
	protected String fRepositoryId;
	protected ObjectService fObjSvc;
	protected NavigationService fNavSvc;
	protected RepositoryService fRepSvc;
	protected VersioningService fVerSvc;
	protected MultiFilingService fMultiSvc;
	protected DummyCallContext fTestCallContext;
	private String fTypeCreatorClassName;
	protected boolean fUseClientProviderInterface;

	public AbstractServiceTst() {
		// The in-memory server unit tests can either be run directly against the
		// service implementation or against a clocal binding interface. The local
		// binding interfaces offers some benefits like type system caching etc.
		// The default is using the direct implementation. Subclasses may
		// override this behavior.
		
		fUseClientProviderInterface = true;
		
		// Init with default types, can be overridden by subclasses:
		fTypeCreatorClassName = UnitTestTypeSystemCreator.class.getName();
	}

	// Subclasses may want to use their own types
	protected void setTypeCreatorClass(String typeCreatorClassName) {
		fTypeCreatorClassName = typeCreatorClassName;
	}

	protected void setUp() throws Exception {
		// super.setUp();
		LOG.debug("Initializing InMemory Test with type creator class: " + fTypeCreatorClassName);
		Map<String, String> parameters = new HashMap<String, String>();
		
		// attach repository info to the session:
		parameters.put(ConfigConstants.TYPE_CREATOR_CLASS, fTypeCreatorClassName);
		parameters.put(ConfigConstants.REPOSITORY_ID, REPOSITORY_ID);

		// attach repository info to the session:
		parameters.put(ConfigConstants.REPOSITORY_INFO_CREATOR_CLASS, UnitTestRepositoryInfo.class.getName());
		
		// give subclasses a chance to provide additional parameters for special
		// tests
		addParameters(parameters);

		fTestCallContext = new DummyCallContext();
		// Attach a standatrd CallContext to a thread before the services are
		// initialized.
		RuntimeContext.attachCfg(fTestCallContext);

		if (fUseClientProviderInterface)
			initializeUsingLocalBinding(parameters);
		else
			initializeDirect(parameters);

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

	protected void tearDown() throws Exception {
		// super.tearDown();
	}

	public void testDummy() {
		// dummy test to make tools happy that complain if there are no tests
		// available in a test class
	}

	protected String createFolder(String folderName, String parentFolderId, String typeId) {
		Properties props = createFolderProperties(folderName, typeId);
		String id = null;
		try {
			id = fObjSvc.createFolder(fRepositoryId, props, parentFolderId, null, null, null, null);
			if (null == id)
				fail("createFolder failed.");
		} catch (Exception e) {
			fail("createFolder() failed with exception: " + e);
		}
		return id;
	}

	protected String createDocument(String name, String folderId, String typeId, VersioningState versioningState,
			boolean withContent) {
		ContentStream contentStream = null;
		List<String> policies = null;
		Acl addACEs = null;
		Acl removeACEs = null;
		ExtensionsData extension = null;

		Properties props = createDocumentProperties(name, typeId);

		if (withContent)
			contentStream = createContent();

		String id = null;
		try {
			id = fObjSvc.createDocument(fRepositoryId, props, folderId, contentStream, versioningState, policies,
					addACEs, removeACEs, extension);
			if (null == id)
				fail("createDocument failed.");
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
		ContentStreamDataImpl content = new ContentStreamDataImpl();
		content.setFileName("data.txt");
		content.setMimeType("text/plain");
		int len = 32 * 1024;
		byte[] b = { 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x0c, 0x0a,
				0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x0c, 0x0a }; // 32
																													// Bytes
		ByteArrayOutputStream ba = new ByteArrayOutputStream(len);
		try {
			for (int i = 0; i < 1024; i++)
				ba.write(b);
			content.setContent(new ByteArrayInputStream(ba.toByteArray()));
		} catch (IOException e) {
			throw new RuntimeException("Failed to fill content stream with data", e);
		}
		return content;
	}

	protected ContentStream createContent(char ch) {
		ContentStreamDataImpl content = new ContentStreamDataImpl();
		content.setFileName("data.txt");
		content.setMimeType("text/plain");
		int len = 32 * 1024;
		byte[] b = new byte[32];
		for (int i = 0; i < 32; i++)
			b[i] = (byte) Character.getNumericValue(ch);
		ByteArrayOutputStream ba = new ByteArrayOutputStream(len);
		try {
			for (int i = 0; i < 1024; i++)
				ba.write(b);
			content.setContent(new ByteArrayInputStream(ba.toByteArray()));
		} catch (IOException e) {
			throw new RuntimeException("Failed to fill content stream with data", e);
		}
		return content;
	}

	protected void verifyContentResult(ContentStream sd) {
		assertEquals("text/plain", sd.getMimeType());
		assertEquals("data.txt", sd.getFileName());
		assertEquals(32 * 1024, sd.getBigLength().longValue());
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
		assertEquals(1024, counter);
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
	 * Instantiates the services by using directly the service implementations.
	 * 
	 * @param parameters
	 *            configuration parameters for client provider interface and
	 *            in-memory provider
	 */
	private void initializeDirect(Map<String, String> parameters) {
		LOG.info("Initialize unit test using directly the InMemory-classes.");

		CmisInMemoryProvider inMemSpi = new CmisInMemoryProvider(parameters);
		fRepSvc = inMemSpi.getRepositoryService();
		fObjSvc = inMemSpi.getObjectService();
		fNavSvc = inMemSpi.getNavigationService();
		fVerSvc = inMemSpi.getVersioningService();
		fMultiSvc = inMemSpi.getMultiFilingService();
	}

	/**
	 * Instantiates the services by using the client provider interface.
	 * 
	 * @param parameters
	 *            configuration parameters for client provider interface and
	 *            in-memory provider
	 */
	private void initializeUsingLocalBinding(Map<String, String> parameters) {

		LOG.info("Initialize unit test using the local binding interface.");
		
		// add parameters for local binding:
		parameters.put(SessionParameter.BINDING_SPI_CLASS, SessionParameter.LOCAL_FACTORY);
		parameters.put(SessionParameter.LOCAL_FACTORY, InMemoryServiceFactoryImpl.class.getName());
		parameters.put(ConfigConstants.OVERRIDE_CALL_CONTEXT, "true");
		InMemoryServiceFactoryImpl.setOverrideCallContext(fTestCallContext);

		// get factory and create binding
		CmisBindingFactory factory = CmisBindingFactory.newInstance();		
		CmisBinding binding = factory.createCmisLocalBinding(parameters);
		assertNotNull(binding);
		fFactory = binding.getObjectFactory();
		fRepSvc = binding.getRepositoryService();
		fObjSvc = binding.getObjectService();
		fNavSvc = binding.getNavigationService();
		fVerSvc = binding.getVersioningService();
		fMultiSvc = binding.getMultiFilingService();
	}

	protected String getStringProperty(ObjectData objData, String propertyKey) {
		PropertyData<? extends Object> pd = (PropertyData<? extends Object>) objData.getProperties().getProperties()
				.get(PropertyIds.PATH);
		assertNotNull(pd.getFirstValue());
		assertTrue(pd.getFirstValue() instanceof String);
		return (String) pd.getFirstValue();
	}
}
