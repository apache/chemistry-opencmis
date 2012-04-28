package org.apache.chemistry.opencmis.inmemory;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumBaseObjectTypeIds;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumBasicPermissions;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.InMemoryAce;
import org.apache.chemistry.opencmis.server.support.query.CalendarHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AclPermissionsTest extends AbstractServiceTest  {

	private static Logger LOG = LoggerFactory.getLogger(AclPermissionsTest.class);
	private static final BigInteger MINUS_ONE = BigInteger.valueOf(-1L);
	
    protected ObjectCreator fCreator;
	protected ObjectStore objectStore = null;
	protected List<Ace> addACEs = null;
	protected Acl addAcl = null;
	protected List<Ace> standardACEs = null;
	protected Acl standardAcl = null;
	protected List<Ace> noReadACEs = null;
	protected Acl noReadAcl = null;
	protected List<Ace>readACEs = null;
	protected Acl readAcl = null;
	protected List<Ace>readWriteACEs = null;
	protected Acl readWriteAcl = null;
	protected List<Ace> writerReadACEs = null;
	protected Acl writerReadAcl = null;
	protected List<Ace> adminACEs = null;
	protected Acl adminAcl = null;
	protected List<Ace> testUserACEs = null;
	protected Acl testUserAcl = null;
	protected Acl defaultAcl = null;
	
	protected static Map<String, String> idMap = new HashMap<String, String>();
	
    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    @Override
    @Before
    public void setUp() {
        super.setTypeCreatorClass(UnitTestTypeSystemCreator.class.getName());
        super.setUp();
        fCreator = new ObjectCreator(fFactory, fObjSvc, fRepositoryId);
		 List<String> principalIds = new ArrayList<String>(3);
		 principalIds.add("TestAdmin");
		 principalIds.add("Writer");
		 principalIds.add("Reader");
		 principalIds.add("TestUser");
		addACEs = new ArrayList<Ace>(4);
		addACEs.add(createAce("TestAdmin", EnumBasicPermissions.CMIS_ALL));
		addACEs.add(createAce("Writer", EnumBasicPermissions.CMIS_WRITE));
		addACEs.add(createAce("TestUser", EnumBasicPermissions.CMIS_WRITE));
		addACEs.add(createAce("Reader", EnumBasicPermissions.CMIS_READ));
		addAcl = fFactory.createAccessControlList(addACEs);
		
		standardACEs = new ArrayList<Ace>(3);
		standardACEs.add(createAce("TestAdmin", EnumBasicPermissions.CMIS_ALL));
		standardACEs.add(createAce("Writer", EnumBasicPermissions.CMIS_WRITE));
		standardACEs.add(createAce("Reader", EnumBasicPermissions.CMIS_READ));
		standardAcl = fFactory.createAccessControlList(standardACEs);	
		
		noReadACEs = new ArrayList<Ace>(2);
		noReadACEs.add(createAce("TestAdmin", EnumBasicPermissions.CMIS_ALL));
		noReadACEs.add(createAce("Writer", EnumBasicPermissions.CMIS_WRITE));
		noReadAcl = fFactory.createAccessControlList(noReadACEs);	
		
		readACEs = new ArrayList<Ace>(1);
		readACEs.add(createAce("Reader", EnumBasicPermissions.CMIS_READ));
		readAcl = fFactory.createAccessControlList(readACEs);	
		
		readWriteACEs = new ArrayList<Ace>(2);
		readWriteACEs.add(createAce("Reader", EnumBasicPermissions.CMIS_READ));
		readWriteACEs.add(createAce("Writer", EnumBasicPermissions.CMIS_WRITE));
		readWriteAcl = fFactory.createAccessControlList(readWriteACEs);	
		
		testUserACEs = new ArrayList<Ace>(1);
		testUserACEs.add(createAce("TestUser", EnumBasicPermissions.CMIS_WRITE));
		testUserAcl = fFactory.createAccessControlList(testUserACEs);	
		
		writerReadACEs = new ArrayList<Ace>(1);
		writerReadACEs.add(createAce("Writer", EnumBasicPermissions.CMIS_READ));
		writerReadAcl = fFactory.createAccessControlList(writerReadACEs);	
		
		adminACEs = new ArrayList<Ace>(1);
		adminACEs.add(createAce("TestAdmin", EnumBasicPermissions.CMIS_ALL));
		adminAcl = fFactory.createAccessControlList(adminACEs);	
		
		List<Ace> defaultACEs = new ArrayList<Ace>(1);
        defaultACEs.add(createAce(InMemoryAce.getAnyoneUser(), EnumBasicPermissions.CMIS_ALL));
        defaultAcl = fFactory.createAccessControlList(defaultACEs); 
	}

	@Test
	public void testCreateObjectsWithAcl()
	{
		// create a document with initial ACL
		String docId = createDocumentWithAcls("complexDocument",  fRootFolderId, UnitTestTypeSystemCreator.COMPLEX_TYPE,
				addAcl, defaultAcl);
		Acl acl1 = fAclSvc.getAcl(fRepositoryId, docId, true, null);
		assertTrue(aclEquals(addAcl, acl1));
		
		// create a folder with initial ACL
		String folderId = createFolderWithAcls("folderWithAcl", fRootFolderId, BaseTypeId.CMIS_FOLDER.value(),
				addAcl, defaultAcl);
		Acl acl2 = fAclSvc.getAcl(fRepositoryId, folderId, true, null);
		assertTrue(aclEquals(addAcl, acl2));
		
		// add acl later
		String docId2 = createVersionedDocument("complexDocument2",  fRootFolderId);
        Acl acl = fAclSvc.applyAcl(fRepositoryId, docId2, addAcl, defaultAcl, AclPropagation.OBJECTONLY, null);
		assertTrue(aclEquals(addAcl, acl));
		
		String folderId2 = createFolder("folder2", fRootFolderId, "cmis:folder");
		acl2 = fAclSvc.applyAcl(fRepositoryId, folderId2, addAcl, defaultAcl, AclPropagation.OBJECTONLY, null);
		assertTrue(aclEquals(addAcl, acl2));
		
		// add a subfolder
		String subFolderId = createFolder("subFolder", folderId,  BaseTypeId.CMIS_FOLDER.value());
		// folder should inherit acl
		Acl subAcl = fAclSvc.getAcl(fRepositoryId, subFolderId, true, null);
		assertTrue(aclEquals(addAcl, subAcl));
		
		// add a document
		String subDocId = createVersionedDocument("subDoc", subFolderId);
		// document should inherit acl
		Acl subAclDoc = fAclSvc.getAcl(fRepositoryId, subDocId, true, null);
		assertTrue(aclEquals(addAcl, subAclDoc));
		
		// remove an ace, no permission is left for TestUser
		Acl removeAcl = createAcl("TestUser", EnumBasicPermissions.CMIS_WRITE);
		Acl acl3 = fAclSvc.applyAcl(fRepositoryId, docId2, null, removeAcl, AclPropagation.OBJECTONLY, null);
		
		List<Ace> compareRemoveACEs = new ArrayList<Ace>(3);
		compareRemoveACEs.add(createAce("TestAdmin", EnumBasicPermissions.CMIS_ALL));
		compareRemoveACEs.add(createAce("Writer", EnumBasicPermissions.CMIS_WRITE));
		compareRemoveACEs.add(createAce("Reader", EnumBasicPermissions.CMIS_READ));
		Acl compareRemoveAcl = fFactory.createAccessControlList(compareRemoveACEs);
		
		assertTrue(aclEquals(compareRemoveAcl, acl3));
		
		// addACE not propagated
		Acl addPropAcl = createAcl("TestUser", EnumBasicPermissions.CMIS_WRITE);
		
		Acl acl4 = fAclSvc.applyAcl(fRepositoryId, subFolderId, addPropAcl, null, AclPropagation.OBJECTONLY, null);
		Acl subAclDoc2 = fAclSvc.getAcl(fRepositoryId, subDocId, true, null);
		assertTrue(aclEquals(addAcl, subAclDoc2));  // acl of doc did not change
		
		List<Ace> compareRemoveACEs2 = new ArrayList<Ace>(4);
		compareRemoveACEs2.add(createAce("TestAdmin", EnumBasicPermissions.CMIS_ALL));
		compareRemoveACEs2.add(createAce("Writer", EnumBasicPermissions.CMIS_WRITE));
		compareRemoveACEs2.add(createAce("TestUser", EnumBasicPermissions.CMIS_ALL));
		compareRemoveACEs2.add(createAce("Reader", EnumBasicPermissions.CMIS_READ));
		Acl compareRemoveAcl2 = fFactory.createAccessControlList(compareRemoveACEs2);
		assertTrue(aclEquals(compareRemoveAcl2, acl4)); 
		
		// addACE propagated
		Acl acl5 = fAclSvc.applyAcl(fRepositoryId, subFolderId, addPropAcl, null, AclPropagation.PROPAGATE, null);
		Acl subAclDoc3 = fAclSvc.getAcl(fRepositoryId, subDocId, true, null);
		assertTrue(aclEquals(compareRemoveAcl2, subAclDoc3));  // acl of doc did change
		assertTrue(aclEquals(compareRemoveAcl2, acl5)); 
	}
		
	
	@Test
	public void checkNavigationServiceGeneralAccess()
	{
		// starts with call context TestUser
		switchCallContext("TestAdmin");
		String docId = createDocumentWithAcls("doc",  fRootFolderId, "ComplexType",
				standardAcl, defaultAcl);
		String folderId = createFolderWithAcls("folder", fRootFolderId, "cmis:folder", standardAcl, defaultAcl);
//		fTestCallContext = new DummyCallContext("Writer");
		String subFolderId = createFolderWithAcls("subFolder", folderId, "cmis:folder", standardAcl, null);
		
		
		// TestUser has no permission at all
		switchCallContext("TestUser");
		boolean exceptionThrown = false;
		try
		{
			ObjectInFolderList list = fNavSvc.getChildren(fRepositoryId, folderId, null, null, false, IncludeRelationships.NONE, null, null, 
					BigInteger.ZERO , BigInteger.ZERO, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions)");
		
		switchCallContext("Reader");
		ObjectInFolderList list = fNavSvc.getChildren(fRepositoryId, folderId, null, null, false, IncludeRelationships.NONE, null, null,
				BigInteger.ZERO , BigInteger.ZERO, null);
		
		
		switchCallContext("TestUser");
		exceptionThrown = false;
		try
		{
			List<ObjectInFolderContainer> list2 = fNavSvc.getDescendants(fRepositoryId, folderId, MINUS_ONE, null, null, null, null, null, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions)");
		
		switchCallContext("Reader");
		List<ObjectInFolderContainer> list2 = fNavSvc.getDescendants(fRepositoryId, folderId, MINUS_ONE, null, null, null, null, null, null);
		
		
		switchCallContext("TestUser");
		exceptionThrown = false;
		try
		{
			List<ObjectInFolderContainer> list3 = fNavSvc.getFolderTree(fRepositoryId, folderId, BigInteger.ONE, null, null, null, null, null, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions)");
		
		switchCallContext("Reader");
		List<ObjectInFolderContainer> list3 = fNavSvc.getFolderTree(fRepositoryId, folderId, BigInteger.ONE, null, null, null, null, null, null);
		
		switchCallContext("TestUser");
		exceptionThrown = false;
		try
		{
			List<ObjectParentData> list4 = fNavSvc.getObjectParents(fRepositoryId, folderId, null, null, null, null, null, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions)");
		
		switchCallContext("Reader");
		List<ObjectParentData> list4 = fNavSvc.getObjectParents(fRepositoryId, folderId, null, null, null, null, null, null);
		
		switchCallContext("TestUser");
		exceptionThrown = false;
		try
		{
			ObjectData list5 = fNavSvc.getFolderParent(fRepositoryId, folderId, null, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions)");
		
		switchCallContext("Reader");
		ObjectData list5 = fNavSvc.getFolderParent(fRepositoryId, folderId, null, null);
	
		switchCallContext("TestUser");
		exceptionThrown = false;
		try
		{
			ObjectList list6 = fNavSvc.getCheckedOutDocs(fRepositoryId, folderId, null, null, null, IncludeRelationships.NONE, 
					null, MINUS_ONE, MINUS_ONE, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions)");
		
		switchCallContext("Reader");
		ObjectList list6 = fNavSvc.getCheckedOutDocs(fRepositoryId, folderId, null, null, null, IncludeRelationships.NONE, 
				null, MINUS_ONE, MINUS_ONE, null);
	}
	
	@Test
	public void testAclServiceGeneralAccess()
	{
	    List<Ace> initialACEs = new ArrayList<Ace>(4);
	    initialACEs.addAll(standardACEs);
	    initialACEs.add(createAce("Admin2", EnumBasicPermissions.CMIS_ALL));
        Acl initialAcl = fFactory.createAccessControlList(initialACEs);   
        
        List<Ace> expectedACEs = new ArrayList<Ace>(5);
        expectedACEs.addAll(initialACEs);
        expectedACEs.addAll(testUserACEs);
        Acl expectedAcl = fFactory.createAccessControlList(expectedACEs);   
        
	    List<Ace> removeACEs = new ArrayList<Ace>(1);
        removeACEs.add(createAce("TestAdmin", EnumBasicPermissions.CMIS_ALL));
		Acl removeAcl = fFactory.createAccessControlList(removeACEs);
		
		List<Ace> removeACEs2 = new ArrayList<Ace>(2);
		removeACEs2.add(createAce("TestAdmin", EnumBasicPermissions.CMIS_ALL));
		removeACEs2.add(createAce("Reader", EnumBasicPermissions.CMIS_READ));
		Acl removeAcl2 = fFactory.createAccessControlList(removeACEs2);
		
		List<Ace> testUserACEs = new ArrayList<Ace>(1);
		testUserACEs.add(createAce("TestUser", EnumBasicPermissions.CMIS_WRITE));
		Acl testUserAcl = fFactory.createAccessControlList(testUserACEs);
		
		switchCallContext("TestAdmin");
		String docId = createDocumentWithAcls("doc",  fRootFolderId, "ComplexType",
		        initialAcl, defaultAcl);
		String folderId = createFolderWithAcls("folder", fRootFolderId, "cmis:folder", initialAcl, defaultAcl);
		String subFolderId = createFolderWithAcls("subFolder", folderId, "cmis:folder", initialAcl, defaultAcl);
		
		// getAcl of a folder
		switchCallContext("TestUser");
		boolean exceptionThrown = false;
		try
		{
			Acl acl = fAclSvc.getAcl(fRepositoryId, folderId, null, null); 
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions to get acl of folder");
		
		switchCallContext("Reader");
		Acl acl = fAclSvc.getAcl(fRepositoryId, folderId, null, null);
		
		// getAcl of a document
		switchCallContext("TestUser");
		exceptionThrown = false;
		try
		{
			Acl docAcl = fAclSvc.getAcl(fRepositoryId, docId, true, null); 
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions to get acl of doc)");
		
		switchCallContext("Reader");
		Acl docAcl = fAclSvc.getAcl(fRepositoryId, docId, true, null);
		
		// applyAcl
		switchCallContext("Reader");
		exceptionThrown = false;
		try
		{
			Acl docAcl2 = fAclSvc.applyAcl(fRepositoryId, docId, initialAcl, null, AclPropagation.OBJECTONLY, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions)");
		
//		switchCallContext("Writer");
        switchCallContext("TestAdmin");
		Acl docAcl2 = fAclSvc.applyAcl(fRepositoryId, docId, initialAcl, null, AclPropagation.OBJECTONLY, null);
		
		// applyAcl when not allowed to subItem
		switchCallContext("TestAdmin");
		Acl docAcl4 = fAclSvc.applyAcl(fRepositoryId, subFolderId, null, removeAcl, AclPropagation.OBJECTONLY, null);
		
//        switchCallContext("Writer");
        switchCallContext("TestAdmin");
        // apply an ACL where the current user has permission to modify ACL on folder but not on sub-folder:
		Acl docAcl5 = fAclSvc.applyAcl(fRepositoryId, folderId, testUserAcl, null, AclPropagation.PROPAGATE, null);
		switchCallContext("Admin");
		Acl docAcl6 = fAclSvc.getAcl(fRepositoryId, folderId, true, null);
		assertTrue(aclEquals(expectedAcl, docAcl6)); 
		Acl docAcl7 = fAclSvc.getAcl(fRepositoryId, subFolderId, true, null);
		assertTrue(aclEquals(standardAcl, docAcl7)); 
	}
	
	@Test
	public void testObjectServiceGeneralAccess()
	{
			
		// starts with call context TestUser
		switchCallContext("TestAdmin");
		String docId = createDocumentWithAcls("doc",  fRootFolderId, "ComplexType",
				standardAcl, defaultAcl);
		String folderId = createFolderWithAcls("folder", fRootFolderId, "cmis:folder", standardAcl, defaultAcl);
//		fTestCallContext = new DummyCallContext("Writer");
		String subFolderId = createFolderWithAcls("subFolder", folderId, "cmis:folder", standardAcl, null);
		String noReadFolderId = createFolderWithAcls("noReadFolder", folderId, "cmis:folder", null, readAcl);
		String adminFolderId = createFolderWithAcls("adminFolder", folderId, "cmis:folder", null, readWriteAcl);
		
		// TestUser has no permission at all
		switchCallContext("TestUser");
		boolean exceptionThrown = false;
		try
		{
			Properties properties = createDocumentProperties("doc", "ComplexType");
			String id = fObjSvc.createDocument(fRepositoryId, properties, folderId, null, null, null, null,
					null, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions to create a document");
		
		exceptionThrown = false;
		try
		{
			String id = fObjSvc.createFolder(fRepositoryId, null, folderId, null, null, null, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions to create a folder");
		
		/*
		exceptionThrown = false;
		try
		{
			Properties properties = createRelationshipProperties(folderId, fRootFolderId);
			String id1 = fObjSvc.createRelationship(fRepositoryId, properties, null, null, null, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions to create a relationship: missing read permission for source id");
		
		exceptionThrown = false;
		Properties properties = createRelationshipProperties( fRootFolderId, folderId);
		try
		{
			String id2 = fObjSvc.createRelationship(fRepositoryId, properties, null, null, null, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions to create a relationship: missing read permission for destination");
		*/
		
		exceptionThrown = false;
		try
		{
			Properties props = fObjSvc.getProperties(fRepositoryId,  folderId, null, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions to get properties of the folder");
		
		exceptionThrown = false;
		try
		{
			Properties props = fObjSvc.getProperties(fRepositoryId,  docId, null, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions to get properties of the document");
		
		exceptionThrown = false;
		try
		{
			List<RenditionData> renditions = fObjSvc.getRenditions(fRepositoryId,  docId, null, BigInteger.valueOf(-1),
					BigInteger.valueOf(-1), null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions to get renditions of the document");
		
		exceptionThrown = false;
		try
		{
			ContentStream contentStream =  fObjSvc.getContentStream(fRepositoryId,  docId, null, BigInteger.valueOf(-1),
					BigInteger.valueOf(-1), null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions to get contentStream of the document");
		
		switchCallContext("Reader");
		exceptionThrown = false;
		Properties properties = createDocumentProperties( "name", "typeId");
		try
		{	
			fObjSvc.updateProperties(fRepositoryId,
					new Holder<String>(docId), new Holder<String>("changeToken"), properties, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("Reader has no permissions to update properties of the document");
		
		exceptionThrown = false;
		properties = createDocumentProperties( "name", "typeId");
		try
		{	
			fObjSvc.updateProperties(fRepositoryId,
					new Holder<String>(docId), new Holder<String>("changeToken"), properties, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("Reader has no permissions to update properties of the document");
		
		exceptionThrown = false;
		try
		{	
			fObjSvc.moveObject(fRepositoryId, new Holder<String>(docId), subFolderId,
					fRootFolderId, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("Reader has no permissions to move document");
		
		switchCallContext("Writer");
		exceptionThrown = false;
		try
		{	
			fObjSvc.moveObject(fRepositoryId,new Holder<String>(docId), adminFolderId,
					fRootFolderId, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("Writer has no permissions to move document to admin folder");
		
		switchCallContext("Reader");
		exceptionThrown = false;
		try
		{	
			fObjSvc.deleteObject(fRepositoryId, docId, true, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("Reader has no permissions to delete document ");
		
		exceptionThrown = false;
		try
		{	
			fObjSvc.deleteObject(fRepositoryId, adminFolderId, true, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("Reader has no permissions to delete admin folder ");
		
		exceptionThrown = false;
		try
		{	
			fObjSvc.setContentStream(fRepositoryId, new Holder<String> (docId), true,
					new Holder<String>("changeToken"), null, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("Reader has no permissions to set content ");
		
		exceptionThrown = false;
		try
		{	
			fObjSvc.deleteContentStream(fRepositoryId, new Holder<String> (docId), 
					new Holder<String>("changeToken"), null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("Reader has no permissions to delete content ");
		
		exceptionThrown = false;
		try
		{	
			fObjSvc.deleteTree(fRepositoryId, folderId, true,
					 null, false, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("Reader has no permissions to delete tree ");
	}
	
	@Test
	public void testMultiFilingServiceGeneralAccess()
	{
		// starts with call context TestUser
		switchCallContext("TestAdmin");
		String docId = createDocumentWithAcls("doc",  fRootFolderId, "ComplexType",
				standardAcl, defaultAcl);
		String folderId = createFolderWithAcls("folder", fRootFolderId, "cmis:folder", 
				addAcl, defaultAcl);
		String noReadFolderId = createFolderWithAcls("noReadFolder", folderId, "cmis:folder", 
				null, readAcl);
		
		// TestUser has no permission at the document
		switchCallContext("TestUser");
		boolean exceptionThrown = false;
		try
		{
			
			fMultiSvc.addObjectToFolder(fRepositoryId, docId, folderId, true, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permissions at the document to add a parent");
		
		exceptionThrown = false;
		switchCallContext("Reader");  // has no permission at the folder
		try
		{
			
			fMultiSvc.addObjectToFolder(fRepositoryId, docId, noReadFolderId, true, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("Reader has no permission at the folder to add a parent");
		
		switchCallContext("TestAdmin");
		fMultiSvc.addObjectToFolder(fRepositoryId, docId, noReadFolderId, true, null);
		fMultiSvc.addObjectToFolder(fRepositoryId, docId, folderId, true, null);
		
		switchCallContext("Reader");  
		try
		{
			
			fMultiSvc.removeObjectFromFolder(fRepositoryId, docId, noReadFolderId, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("Reader has no permission at the folder to remove a parent");
		
		switchCallContext("TestUser"); 
		try
		{
			
			fMultiSvc.removeObjectFromFolder(fRepositoryId, docId, folderId, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permission at the object to remove a parent");
	}
	
	@Test
	public void testVersioningServiceGeneralAccess()
	{
		// starts with call context TestUser
		switchCallContext("TestAdmin");
		String docId = createDocumentWithAcls("doc",  fRootFolderId, UnitTestTypeSystemCreator.VERSIONED_TYPE,
		        VersioningState.MAJOR, standardAcl, defaultAcl);
	
		// TestUser has no permission at all
		switchCallContext("TestUser");
		boolean exceptionThrown = false;
		try
		{
			Holder<String> docIdHolder = new Holder<String>(docId);
			fVerSvc.checkOut(fRepositoryId, docIdHolder, null, 
					new Holder<Boolean>(false));
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permission to checkout)");
		
		// Reader has only read permission
		switchCallContext("Reader");
		exceptionThrown = false;
		try
		{
			fVerSvc.checkOut(fRepositoryId, new Holder<String>(docId), null, 
					new Holder<Boolean>(false));
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("Reader has not enough permission to checkout)");
		
		// checkout
		switchCallContext("TestAdmin");
		fAclSvc.applyAcl(fRepositoryId, docId, testUserAcl, null, AclPropagation.OBJECTONLY, null);
		switchCallContext("TestUser");
		Holder<String> docIdHolder = new Holder<String>(docId);
		fVerSvc.checkOut(fRepositoryId, docIdHolder, null, 
				new Holder<Boolean>(false));
	
        switchCallContext("TestAdmin");
		fAclSvc.applyAcl(fRepositoryId, docId, null, testUserAcl, AclPropagation.OBJECTONLY, null);
		
		// TestUser has no permission at all, only checkout user can checkin
		switchCallContext("TestUser");
		exceptionThrown = false;
		try
		{
			fVerSvc.cancelCheckOut(fRepositoryId, docId, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permission to cancelCheckOut)");
		switchCallContext("TestAdmin");
		fAclSvc.applyAcl(fRepositoryId, docId, testUserAcl, null, AclPropagation.OBJECTONLY, null);
		switchCallContext("TestUser");
		fVerSvc.cancelCheckOut(fRepositoryId, docId, null);
		
		// writer looses write permission
		switchCallContext("Writer");
		fVerSvc.checkOut(fRepositoryId, new Holder<String>(docId), null, 
				new Holder<Boolean>(false));

		switchCallContext("TestAdmin");
		fAclSvc.applyAcl(fRepositoryId, docId, null, readWriteAcl, AclPropagation.OBJECTONLY, null);
	
		switchCallContext("Writer");
        exceptionThrown = false;
		try
		{
			fVerSvc.cancelCheckOut(fRepositoryId, docId, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("Reader has not enough permission to cancelCheckOut)");
		switchCallContext("TestAdmin");
		fAclSvc.applyAcl(fRepositoryId, docId, readWriteAcl, null, AclPropagation.OBJECTONLY, null);
		switchCallContext("Writer");
		fVerSvc.cancelCheckOut(fRepositoryId, docId, null);
				
		// TestUser has no permission at all
		switchCallContext("TestAdmin");
		fAclSvc.applyAcl(fRepositoryId, docId, testUserAcl, null, AclPropagation.OBJECTONLY, null);
		switchCallContext("TestUser");
		fVerSvc.checkOut(fRepositoryId, new Holder<String>(docId), null, 
				new Holder<Boolean>(false));

		switchCallContext("TestAdmin");
		fAclSvc.applyAcl(fRepositoryId, docId, null, testUserAcl, AclPropagation.OBJECTONLY, null);
	
		switchCallContext("TestUser");
		exceptionThrown = false;
		try
		{
			fVerSvc.checkIn(fRepositoryId, new Holder<String>(docId), true,  null, null, null, null,
					null, null, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has no permission to checkIn)");
		switchCallContext("TestAdmin");
		fAclSvc.applyAcl(fRepositoryId, docId, testUserAcl, null, AclPropagation.OBJECTONLY, null);
		switchCallContext("TestUser");
		fVerSvc.checkIn(fRepositoryId, new Holder<String>(docId), true,  null, null, null, null,
				null, null, null);

		switchCallContext("TestAdmin");
		fAclSvc.applyAcl(fRepositoryId, docId, null, testUserAcl, AclPropagation.OBJECTONLY, null);
		
		// writer looses write permission
		switchCallContext("Writer");
		fVerSvc.checkOut(fRepositoryId, new Holder<String>(docId), null, 
				new Holder<Boolean>(false));
        
		switchCallContext("TestAdmin");
		fAclSvc.applyAcl(fRepositoryId, docId, null, readWriteAcl, AclPropagation.OBJECTONLY, null);

		switchCallContext("Writer");	
		exceptionThrown = false;
		try
		{
			fVerSvc.checkIn(fRepositoryId, new Holder<String>(docId), true,  null, null, null, null,
					null, null, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("Writer has not enough permission to checkIn)");
		switchCallContext("TestAdmin");
		fAclSvc.applyAcl(fRepositoryId, docId, readWriteAcl, null, AclPropagation.OBJECTONLY, null);
		switchCallContext("Writer");
		fVerSvc.checkIn(fRepositoryId, new Holder<String>(docId), true,  null, null, null, null,
				null, null, null);
		
		// TestUser has no permission at all
		switchCallContext("TestUser");
		exceptionThrown = false;
		try
		{
			ObjectData objectData = fVerSvc.getObjectOfLatestVersion(fRepositoryId, docId, null, true,
	            null, false, IncludeRelationships.NONE,
	            null, false, false, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has not enough permission to getObjectOfLatestVersion)");
		
		exceptionThrown = false;
		try
		{
			 List<ObjectData> objectDataList = fVerSvc.getAllVersions(fRepositoryId, docId, docId, null,
	            false, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has not enough permission to getAllVersions)");


		exceptionThrown = false;
		try
		{
			Properties properties = fVerSvc.getPropertiesOfLatestVersion(fRepositoryId, docId, null, 
					false, null, null);
		}
		catch (CmisPermissionDeniedException e)
		{
			exceptionThrown = true;
		}
		if (!exceptionThrown)
			Assert.fail("TestUser has not enough permission to getAllVersions)");
	}
	
		
	@Test
	public void testVisibleObjects()
	{
        LOG.debug("start test checkVisibleObjects()...");
		switchCallContext("TestAdmin");
		String docId = createDocumentWithAcls("doc",  fRootFolderId, UnitTestTypeSystemCreator.VERSIONED_TYPE,
				VersioningState.MAJOR, standardAcl, defaultAcl);
		String docId2 = createDocumentWithAcls("doc2",  fRootFolderId, UnitTestTypeSystemCreator.VERSIONED_TYPE,
		        VersioningState.MAJOR, addAcl, defaultAcl);
		String folderId = createFolderWithAcls("folder", fRootFolderId, "cmis:folder", 
				standardAcl, defaultAcl);
		String folderId2 = createFolderWithAcls("folder2", fRootFolderId, "cmis:folder", 
				addAcl, defaultAcl);
		LOG.debug("checkVisibleObjects(): folderId2 is: " + folderId2);
		String subFolderId = createFolderWithAcls("subFolder", folderId2, "cmis:folder", 
				null, testUserAcl);
        LOG.debug("checkVisibleObjects(): subFolderId is: " + subFolderId);
		String subFolderId2 = createFolderWithAcls("subFolder2", folderId2, "cmis:folder", 
				addAcl, null);
        LOG.debug("checkVisibleObjects(): subFolderId2 is: " + subFolderId2);
		String subDocId = createDocumentWithAcls("subDoc",  folderId2, UnitTestTypeSystemCreator.VERSIONED_TYPE,
		        VersioningState.MAJOR, null, testUserAcl);
        LOG.debug("checkVisibleObjects(): subDocId is: " + subDocId);
		String subDocId2 = createDocumentWithAcls("subDoc2", folderId2, UnitTestTypeSystemCreator.VERSIONED_TYPE,
		        VersioningState.MAJOR, addAcl, null);
        LOG.debug("checkVisibleObjects(): subDocId2 is: " + subDocId2);
		String noAclDocId2 = createDocumentWithAcls("noAclDoc2", fRootFolderId, "ComplexType",
				null, null);
        LOG.debug("checkVisibleObjects(): noAclDocId2 is: " + noAclDocId2);
		
		// TestUser has no permission in standardAcl
		switchCallContext("TestUser");
		
		ObjectInFolderList list = fNavSvc.getChildren(fRepositoryId, folderId2, null, null, false, IncludeRelationships.NONE, null, null, 
					null, null, null);
		List<ObjectInFolderData> objects = list.getObjects();
		assertObjectDataListIds(objects, subDocId2);
        assertObjectDataListIds(objects, subFolderId2);
		
		list = fNavSvc.getChildren(fRepositoryId, fRootFolderId, null, null, false, IncludeRelationships.NONE, null, null, 
				null, null, null);
		objects = list.getObjects();
		assertObjectDataListIds(objects, docId2);
        assertObjectDataListIds(objects, folderId2);
        assertObjectDataListIds(objects, noAclDocId2);
		
		List<ObjectInFolderContainer> descList = fNavSvc.getDescendants(fRepositoryId, fRootFolderId, MINUS_ONE,
				null, false, IncludeRelationships.NONE, null, false, null);
		assertObjectInFolderContainerIds(descList, docId2);
        assertObjectInFolderContainerIds(descList, folderId2);
        assertObjectInFolderContainerIds(descList, noAclDocId2);
		
		List<ObjectInFolderContainer> folderList = fNavSvc.getFolderTree(fRepositoryId, fRootFolderId, MINUS_ONE,
				null, false, IncludeRelationships.NONE, null, false, null);
        assertObjectInFolderContainerIds(folderList, folderId2);
        assertObjectInFolderContainerIds(folderList, subFolderId2);
		
		// check out
		switchCallContext("TestAdmin");
		Holder<String> holderDocId = new Holder<String>(docId);
		Holder<String> holderDocId2 = new Holder<String>(docId2);
		Holder<String> holderSubDocId = new Holder<String>(subDocId);
		Holder<String> holderSubDocId2 = new Holder<String>(subDocId2);
		fVerSvc.checkOut(fRepositoryId, holderDocId, null, null);
		fVerSvc.checkOut(fRepositoryId, holderDocId2, null, null);
		fVerSvc.checkOut(fRepositoryId, holderSubDocId, null, null);
		fVerSvc.checkOut(fRepositoryId, holderSubDocId2, null, null);
		
		switchCallContext("TestUser");
		ObjectList objectList = fNavSvc.getCheckedOutDocs(fRepositoryId, null, null, null, false,
				IncludeRelationships.NONE, null, MINUS_ONE, MINUS_ONE, null);
		assertObjectInObjectListIds(objectList, holderDocId2.getValue());
        assertObjectInObjectListIds(objectList, holderSubDocId2.getValue());
		
		// only direct children are returned
		ObjectList objectList2 = fNavSvc.getCheckedOutDocs(fRepositoryId, fRootFolderId, null, null, false,
				IncludeRelationships.NONE, null, MINUS_ONE, MINUS_ONE, null);
		List<String> docIds2 = new ArrayList<String>(1);
		docIds2.add(docId2);
		Assert.assertEquals(BigInteger.valueOf(1L), objectList2.getNumItems());
		
		// multi filing, get object parents
		switchCallContext("TestAdmin");
		String secFolderId = createFolderWithAcls("secondFolder", fRootFolderId, "cmis:folder", 
				standardAcl, defaultAcl);  	
		String docId3 = createDocumentWithAcls("thirdDoc", folderId2, "ComplexType",
				addAcl, null);
		fMultiSvc.addObjectToFolder(fRepositoryId, docId3, secFolderId, true, null);
		
		switchCallContext("TestUser");  // second parent is not visible
		List<ObjectParentData> objectParentData = fNavSvc.getObjectParents(fRepositoryId, docId3, null, null, null, null, true, null);
		Assert.assertEquals(1, objectParentData.size());
		Assert.assertEquals(folderId2, objectParentData.get(0).getObject().getId());
        LOG.debug("...stop test checkVisibleObjects()");
	}
		
	@Test
	public void testQueryAccess()
	{
		createCustomPropertyDocuments();
		
		String queryStatement;
		List<ObjectData> objectDataList;
		ObjectList objectList;
		ObjectData first;
		
		switchCallContext("TestUser"); // Testuser has no permissions to view a document
		queryStatement = "select * from cmis:document";
		objectList = fDiscSvc.query(fRepositoryId, queryStatement, null, null, null,
				null, MINUS_ONE, MINUS_ONE, null);
		assertTrue ( 0L == objectList.getNumItems().longValue());
		
		// add a permission for a document
		switchCallContext("TestAdmin"); 
		String docId20 = idMap.get("customDocId20");
		fAclSvc.applyAcl(fRepositoryId, idMap.get("customDocId20"), testUserAcl, null, AclPropagation.OBJECTONLY, null);
		
		switchCallContext("TestUser"); // Testuser has has only permissions for customDocId20
		queryStatement = "select * from ComplexType where IntProp <= 20";
		
		objectList = fDiscSvc.query(fRepositoryId, queryStatement, null, null, null,
				null, MINUS_ONE, MINUS_ONE, null); 
		assertTrue ( 1L == objectList.getNumItems().longValue());
		objectDataList = objectList.getObjects();
		first = objectDataList.get(0);
		assertTrue(first.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT ));
	}
	
	protected String createDocumentWithAcls(String name, String folderId, String typeId, 
			Acl addACEs, Acl removeACEs)
	{
		return createDocumentWithAcls(name, folderId, typeId, VersioningState.NONE, addACEs, removeACEs);
	}
	
    protected String createDocumentWithAcls(String name, String folderId, String typeId, VersioningState versioningState,
            Acl addACEs, Acl removeACEs)
    {
        ContentStream contentStream = null;
        List<String> policies = null;
        ExtensionsData extension = null;

        Properties props = createDocumentProperties(name, typeId);

        String id = fObjSvc.createDocument(fRepositoryId, props, folderId, contentStream, versioningState , policies,
                addACEs, removeACEs, extension);
        return id;
    }
	
	protected String createFolderWithAcls(String name, String folderId, String typeId, 
			Acl addACEs, Acl removeACEs)
	{
		List<String> policies = null;
		ExtensionsData extension = null;

		Properties props = createFolderProperties(name, typeId);

	
		String id = fObjSvc.createFolder(fRepositoryId, props, folderId, policies,
				addACEs, removeACEs, extension);
		return id;
	}
	
	 protected Properties createRelationshipProperties(String sourceId, String targetId) {
	        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
	        properties.add(fFactory.createPropertyIdData(PropertyIds.SOURCE_ID, sourceId));
	        properties.add(fFactory.createPropertyIdData(PropertyIds.TARGET_ID, targetId));
	        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, 
	        		EnumBaseObjectTypeIds.CMIS_RELATIONSHIP.value()));
	        Properties props = fFactory.createPropertiesData(properties);
	        return props;
	 }
	 
	 private void switchCallContext(String user) {
	     ((DummyCallContext) fTestCallContext).put(CallContext.USERNAME, user);
	 }
	
	protected void createCustomPropertyDocuments()
	{
		switchCallContext("TestAdmin"); 
		// create folder
		String folderId = createFolderWithAcls("customFolder", fRootFolderId, "cmis:folder", standardAcl, defaultAcl);
		idMap.put("customFolder", folderId);

		// create documents
		List<PropertyData<?>> properties10 = new ArrayList<PropertyData<?>>();
		properties10.add(fFactory.createPropertyIntegerData("IntProp", BigInteger.valueOf(10)));
		properties10.add(fFactory.createPropertyStringData("StringProp", "10 string"));
		properties10.add(fFactory.createPropertyBooleanData("BooleanProp", true));
		GregorianCalendar gregorianCalendar = CalendarHelper.fromString("2010-07-10T12:00:00.000-01:00");
		properties10.add(fFactory.createPropertyDateTimeData("DateTimeProp", gregorianCalendar));
		String customDocId10 = createDocumentWithProperties("customDocument10", folderId, "ComplexType",
				properties10, false);
		idMap.put("customDocId10", customDocId10);

		List<PropertyData<?>>  properties20 = new ArrayList<PropertyData<?>>();
		properties20.add(fFactory.createPropertyIntegerData("IntProp", BigInteger.valueOf(20)));
		properties20.add(fFactory.createPropertyStringData("StringProp", "20 string"));
		properties20.add(fFactory.createPropertyBooleanData("BooleanProp", false));
		gregorianCalendar = CalendarHelper.fromString("2010-07-20T12:00:00.000-01:00");
		properties20.add(fFactory.createPropertyDateTimeData("DateTimeProp", gregorianCalendar));
		String customDocId20 = createDocumentWithProperties("customDocument20", folderId, "ComplexType", 
				properties20,false);
		idMap.put("customDocId20", customDocId20);

		List<PropertyData<?>>  properties30 = new ArrayList<PropertyData<?>>();
		properties30.add(fFactory.createPropertyIntegerData("IntProp", BigInteger.valueOf(30)));
		properties30.add(fFactory.createPropertyStringData("StringProp", "30 string"));
		properties30.add(fFactory.createPropertyBooleanData("BooleanProp", true));
		gregorianCalendar = CalendarHelper.fromString("2010-07-30T12:00:00.000-01:00");
		properties30.add(fFactory.createPropertyDateTimeData("DateTimeProp", gregorianCalendar));
		String customDocId30 = createDocumentWithProperties("customDocument30", folderId, "ComplexType",
				properties30, false);
		idMap.put("customDocId30", customDocId30);
	
	}
	
	  protected String createDocumentWithProperties(String name, String folderId, String typeId, List<PropertyData<?>> properties,
	            boolean withContent) {
	        ContentStream contentStream = null;
	        
	        // add document properties
	        properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, name));
	        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, typeId));
	        Properties props = fFactory.createPropertiesData(properties);

	        if (withContent)
	            contentStream = createContent();

	        String id = null;
	        try {
	            id = fObjSvc.createDocument(fRepositoryId, props, folderId, contentStream, VersioningState.NONE, null,
	                    null, null, null);
	            if (null == id)
	                fail("createDocument failed.");
	        } catch (Exception e) {
	            fail("createDocument() failed with exception: " + e);
	        }
	        return id;

	    }

	    private Acl createAcl(String principalId, EnumBasicPermissions permission) {
	        List<Ace> acesAdd = Arrays.asList(new Ace[] { 
	                createAce(principalId, permission),
	                });
	       return fFactory.createAccessControlList(acesAdd);                
	    }

	    private Ace createAce(String principalId, EnumBasicPermissions permission) {
	        return  fFactory.createAccessControlEntry(principalId, Collections.singletonList( permission.value() ));
	    }

	    private static boolean aclEquals(Acl acl1, Acl acl2) {
	        if (acl1 == acl2)
	            return true;
	        if (acl1 == null || acl2 == null)
	            return false;
	        if (acl1.getClass() != acl2.getClass())
	            return false;
            if (acl1.getAces().size() != acl2.getAces().size())
                return false;
	        for (int i=0; i<acl1.getAces().size(); i++) {
	            aclHasAce(acl1.getAces(), acl2.getAces().get(i));
	        }
	        return true;
	    }

	    private static boolean aclHasAce( List<Ace> aces, Ace ace) {
	        for (Ace ace2 : aces) {
                if (!ace.getPrincipalId().equals(ace2.getPrincipalId()))
                    continue;
                if (ace.getPermissions().size() != ace2.getPermissions().size())
                    continue;
                for (int i=0; i<ace2.getPermissions().size(); i++)
                    if (!aceHasPermission(ace.getPermissions(), ace2.getPermissions().get(i)))
                        continue;

                return true;
	        }
	        return false;
	    }

	   private static boolean aceHasPermission( List<String> permissions, String permission) {
	      for (String permission2 : permissions)
	          if (permission2.equals(permission))
	              return true;
	      return false;
	   }

	    private String createVersionedDocument(String name, String folderId) {

	        VersioningState versioningState = VersioningState.MAJOR;
	        String id = null;
	        Map<String, String> properties = new HashMap<String, String>();
	        id = fCreator.createDocument(name, UnitTestTypeSystemCreator.VERSIONED_TYPE, folderId,
	                versioningState, properties);

	        return id;
	    }
	    
	    private void assertObjectDataListIds(List<ObjectInFolderData> folderData, String id) {
	        boolean found = false;
	        for (ObjectInFolderData folder : folderData) {
	            LOG.info("   found folder id " + folder.getObject().getId());
	            if (id.equals(folder.getObject().getId()))
	                found = true;
	        }
            assertTrue("Failed to find folder id " + id, found);          
	    }
	    
	    private void assertObjectInFolderContainerIds(List<ObjectInFolderContainer> folderList, String id) {
            boolean found = objectInFolderContainerHasId(folderList, id);
            assertTrue("Failed to find folder id " + id, found);                  
	    }
	    
        private boolean objectInFolderContainerHasId(List<ObjectInFolderContainer> folderList, String id) {
            for (ObjectInFolderContainer fc : folderList) {
                if (id.equals(fc.getObject().getObject().getId()))
                    return true;
                List<ObjectInFolderContainer> children = fc.getChildren();
                if (children != null && objectInFolderContainerHasId(children, id))
                    return true;
            }
            return false;                  
        }

	    private void assertObjectInObjectListIds(ObjectList objList, String id) {
            boolean found = false;
            for (ObjectData od : objList.getObjects()) {
                LOG.info("   found object id " + od.getId());
                if (id.equals(od.getId()))
                    found = true;

            }
            assertTrue("Failed to find object id " + id, found);                  
        }

}
