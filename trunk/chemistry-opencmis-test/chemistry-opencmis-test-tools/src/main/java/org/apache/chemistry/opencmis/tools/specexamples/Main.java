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
package org.apache.chemistry.opencmis.tools.specexamples;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CmisExtensionElementImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ExtensionDataImpl;
import org.apache.chemistry.opencmis.commons.spi.AclService;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.apache.chemistry.opencmis.commons.spi.DiscoveryService;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.commons.spi.MultiFilingService;
import org.apache.chemistry.opencmis.commons.spi.NavigationService;
import org.apache.chemistry.opencmis.commons.spi.ObjectService;
import org.apache.chemistry.opencmis.commons.spi.RepositoryService;
import org.apache.chemistry.opencmis.commons.spi.VersioningService;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final String MULTIFILED_DOCUMENT = "MultifiledDocument";
	private static final String MULTIFILED_FOLDER_2 = "MultifiledFolder2";
	private static final String MULTIFILED_FOLDER_1 = "MultifiledFolder1";
	private static final Logger LOG = LoggerFactory.getLogger(Main.class.getName());
    private static final BigInteger TYPE_DEPTH_ALL = BigInteger.valueOf(-1);
    private static final BigInteger MAX_ITEMS = null;
    private static final BigInteger SKIP_COUNT = BigInteger.valueOf(0);
    private static final String TOPLEVEL_TYPE = "DocumentTopLevel";
    private static final String VERSIONED_TYPE = "VersionableType";
    private static final String VERSIONED_PROP = "VersionedStringProp";
    private static String LOGDIR = System.getProperty("java.io.tmpdir");// + File.separator;
    private static String ROOT_URL = "http://localhost:8080/inmemory"; 
    private static String ROOT_URL_OASIS = "http://www.example.com:8080/inmemory"; // required by OASIS rules, add this host to your hosts file
    static int NO_FILES_LOGGED = 0;

    private String targetDir = System.getProperty("java.io.tmpdir");// + File.separator;
    private BindingsObjectFactory objFactory = new BindingsObjectFactoryImpl();
    private BindingType bindingType;
    private String rootFolderId;
    private String repositoryId;
    private ObjectService objSvc;
    private NavigationService navSvc;
    private RepositoryService repSvc;
    private VersioningService verSvc;
    private MultiFilingService multiSvc;
    private DiscoveryService discSvc;
    private AclService aclSvc;
    
    private List<String> idsToDelete = new ArrayList<String>();
    private String multiFiledDoc;
    private String multiFiledFolder1;
    private String multiFiledFolder2;
	private String changeToken;
    
    private static final String[] URLS = {ROOT_URL + "/atom", 
        ROOT_URL + "/services", 
        ROOT_URL + "/browser"};
    private static final BindingType[] BINDINGS = {BindingType.ATOMPUB, BindingType.WEBSERVICES, BindingType.BROWSER};

    public Main() {
    }

    public void runAllBindings() {
    	cleanLogFilterDir(); // delete directory where Logging filter writes to ensure not to include unwanted files        

    	for (int i = 0; i < BINDINGS.length; i++) {
    		bindingType = BINDINGS[i];
    		init(URLS[i], BINDINGS[i]);
    		run();
    	}
    	String dirs[] = {BindingType.ATOMPUB.value(), BindingType.WEBSERVICES.value(), BindingType.BROWSER.value() };        
    	createZipFile("CMIS-Spec-Examples.zip", dirs);
     
    }
    
    public void run() {
        LOG.debug("Generating spec examples for Binding: " + bindingType.value());
        
        try {
        // Repository Service:
        getRepositories();

        repositoryId = "A1";
        getRepositoryInfo(); // get root folder id here!

        getTypeDefinition("cmis:folder");
        
        String docId = getTestDocId();
        String folderId = getTestFolderId();

        getTypeChildren(TOPLEVEL_TYPE);

        // Navigation Service:
        getChildren(folderId);
        getDescendants(folderId);
        getObjectParents(folderId);
        removeObjectFromFolder();

        // Object Service:
        getObject(docId);
        getAcl(docId);
        String id1 = createDocument("SampleDocument", TOPLEVEL_TYPE, rootFolderId, VersioningState.NONE);
        updateProperties(id1, PropertyIds.NAME, "RenamedDocument");
        getAllowableActions(id1);
        
        deleteObject(id1);

        // Discovery Service:
        doQuery();
        getContentChanges(changeToken);

        // Versioning Service
        String id2 = prepareVersionSeries("VersionedDocument", VERSIONED_TYPE, rootFolderId);
        checkOut(id2);
        checkIn(id2, true, "final version in series");
        getAllVersions(id2);

        // collect all captured files and store them in a ZIP file
        } catch (Exception e) {
        	LOG.error("Failed to create spec examples: ", e);
        } 

        // delete all generated objects
        cleanup();
    }

    private void init(String url, BindingType bindingType) {
        LOG.debug("Initializing connection to InMemory server: ");
        LOG.debug("   Binding: " + bindingType.value());
        LOG.debug("   URL: " + url);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(SessionParameter.USER, "admin");
        parameters.put(SessionParameter.PASSWORD, "admin");

        parameters.put(SessionParameter.BINDING_TYPE, bindingType.value());

        // get factory and create binding
        CmisBindingFactory factory = CmisBindingFactory.newInstance();
        CmisBinding binding = null;
        
        if (bindingType == BindingType.ATOMPUB)  {
            parameters.put(SessionParameter.ATOMPUB_URL, url);
            binding = factory.createCmisAtomPubBinding(parameters);
        } else if (bindingType == BindingType.WEBSERVICES) {
            parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, url + "/ACLService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE,  url + "/DiscoveryService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE,  url + "/MultiFilingService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE,  url + "/NavigationService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE,  url + "/ObjectService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE,  url + "/PolicyService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE,  url + "/RelatinshipService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE,  url + "/RepositoryService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE,  url + "/VersioningService?wsdl");
            binding = factory.createCmisWebServicesBinding(parameters);            
        } else if (bindingType == BindingType.BROWSER) {
            parameters.put(SessionParameter.BROWSER_URL, url); 
            binding = factory.createCmisBrowserBinding(parameters);            
        } else {
            LOG.error("Unknown binding type: " + bindingType.value());
            return;
        }
        objFactory = binding.getObjectFactory();
        repSvc = binding.getRepositoryService();
        objSvc = binding.getObjectService();
        navSvc = binding.getNavigationService();
        verSvc = binding.getVersioningService();
        multiSvc = binding.getMultiFilingService();
        discSvc = binding.getDiscoveryService();
        aclSvc = binding.getAclService();
        
        // create a folder where target files will be stored:
        targetDir = bindingType.value();
        File in = new File(targetDir);
        deleteDirRecursive(in); // avoid that there are unwanted files from previous runs
        boolean ok = in.mkdir();
        
        LOG.debug("creating target directory for files: " + ok);
        LOG.debug("Initializing done. ");
    }

    private void getRepositories() {
        LOG.debug("getRepositories()");
        List<RepositoryInfo> repositories = repSvc.getRepositoryInfos(null);
        this.repositoryId = repositories.get(0).getId();
        renameFiles("getRepositoryInfos");
        LOG.debug("getRepositoryInfo() done.");
    }

    private void getRepositoryInfo() {
        LOG.debug("getting repository info for repository " + repositoryId);
        
        // Because some bindings silently retrieve all repositories on the first request we call it twice
        // and use a dummy extension data element to prevent caching
        RepositoryInfo repoInfo = repSvc.getRepositoryInfo(repositoryId, null);
        ExtensionDataImpl dummyExt = new ExtensionDataImpl();
        @SuppressWarnings("serial")
		List<CmisExtensionElement> extList = new ArrayList<CmisExtensionElement>() {{ add(new CmisExtensionElementImpl("foo", "foo", null, "bar")); }};
        dummyExt.setExtensions(extList);
        repoInfo = repSvc.getRepositoryInfo(repositoryId, dummyExt);

        LOG.debug("repository id is: " + repoInfo.getId());
        rootFolderId = repoInfo.getRootFolderId();
        changeToken = repoInfo.getLatestChangeLogToken();
        LOG.debug("root folder id is: " + repoInfo.getRootFolderId());
        renameFiles("getRepositoryInfo");
        LOG.debug("getRepositoryInfo() done.");
    }

    private void getObject(String objectId) {
        LOG.debug("getObject " + objectId);
        objSvc.getObject(repositoryId, objectId, "*", true /* includeAllowableActions */,
                IncludeRelationships.NONE /* includeRelationships */, null /* renditionFilter */,
                false /* includePolicyIds */, true /* includeAcl */, null);
        renameFiles("getObject");
        LOG.debug("getObject() done.");
    }

    private void getChildren(String folderId) {
        LOG.debug("getChildren " + folderId);
        navSvc.getChildren(repositoryId, folderId, "*", null /* orderBy */, true /* includeAllowableActions */,
                IncludeRelationships.NONE, null /* renditionFilter */, true /* includePathSegment */, MAX_ITEMS,
                SKIP_COUNT, null);
        renameFiles("getChildren");
        LOG.debug("getChildren() done.");
    }

    private void getDescendants(String folderId) {
        final BigInteger DEPTH = BigInteger.valueOf(3);
        LOG.debug("getDescendants " + folderId);
        navSvc.getDescendants(repositoryId, folderId, DEPTH, "*", true /* includeAllowableActions */,
                IncludeRelationships.NONE, null /* renditionFilter */, true /* includePathSegment */, null);
        renameFiles("getDescendants");
        LOG.debug("getDescendants() done.");
    }
    
    private void getObjectParents(String folderId) {
    	// get object parents first add object to two folders then get parents
        LOG.debug("getObjectsParents " + folderId);
        multiFiledFolder1 = createFolderIntern(MULTIFILED_FOLDER_1, BaseTypeId.CMIS_FOLDER.value(), folderId);
        idsToDelete.add(multiFiledFolder1);
        multiFiledFolder2 = createFolderIntern(MULTIFILED_FOLDER_2, BaseTypeId.CMIS_FOLDER.value(), folderId);
        idsToDelete.add(multiFiledFolder2);
        multiFiledDoc = createDocumentIntern(MULTIFILED_DOCUMENT, BaseTypeId.CMIS_DOCUMENT.value(), multiFiledFolder1, VersioningState.NONE);
        idsToDelete.add(0, multiFiledDoc); // add at the beginning must be removed before folders!
    	multiSvc.addObjectToFolder(repositoryId, multiFiledDoc, multiFiledFolder2, true, null);
    	navSvc.getObjectParents(repositoryId, multiFiledDoc, "*", false, IncludeRelationships.NONE, null, true, null);
        renameFiles("getObjectParents");
        LOG.debug("getObjectParents() done.");
    }
    
    private void removeObjectFromFolder() {
        LOG.debug("removeObjectFromFolder");
        multiSvc.removeObjectFromFolder(repositoryId, multiFiledDoc, multiFiledFolder2, null);
    	renameFiles("removeObjectFromFolder");
    	try {
			Thread.sleep(200);
		} catch (InterruptedException e) {

		}
        LOG.debug("removeObjectFromFolder() done.");    	
    }

    private void doQuery() {
        LOG.debug("doQuery ");
        String statement = "SELECT * from cmis:document WHERE IN_FOLDER('" + rootFolderId + "')";
        discSvc.query(repositoryId, statement, false /* searchAllVersions */, true /* includeAllowableActions */,
                IncludeRelationships.NONE, null, MAX_ITEMS, SKIP_COUNT, null);
        renameFiles("doQuery");
        LOG.debug("doQuery() done.");
    }

    private void getContentChanges(String token) {    	
        LOG.debug("getContentChanges");
        Holder<String> changeLogToken = new Holder<String>("token");
		discSvc.getContentChanges(repositoryId, changeLogToken, false, "*", false, false, null, null);
    	renameFiles("getContentChanges");
        LOG.debug("getContentChanges() done.");    	
    }

    private void getTypeChildren(String typeId) {
        LOG.debug("getTypeChildren " + typeId);
        repSvc.getTypeChildren(repositoryId, typeId, true /* includePropertyDefinitions */, MAX_ITEMS, SKIP_COUNT, null);
        renameFiles("getTypeChildren");
        LOG.debug("getTypeChildren() done.");
    }

    private String createDocument(String name, String typeId, String folderId, VersioningState versioningState) {
        LOG.debug("createDocument " + typeId);

        String id = createDocumentIntern(name, typeId, folderId, versioningState);
        renameFiles("createDocument");
        LOG.debug("createDocument() done.");

        return id;
    }

    private String createDocumentIntern(String name, String typeId, String folderId, VersioningState versioningState) {
        ContentStream contentStream = null;
        List<String> policies = null;
        Acl addACEs = null;
        Acl removeACEs = null;
        ExtensionsData extension = null;

        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(objFactory.createPropertyIdData(PropertyIds.NAME, name));
        properties.add(objFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, typeId));
        Properties props = objFactory.createPropertiesData(properties);

        contentStream = createContent();

        String id = null;
        try {
            id = objSvc.createDocument(repositoryId, props, folderId, contentStream, versioningState, policies, addACEs,
                    removeACEs, extension);
        } catch (CmisBaseException e) {
        	// folder already there, get it:
            ObjectInFolderList result = navSvc.getChildren(repositoryId, folderId, "*", null, false,
                    IncludeRelationships.NONE, null, true, MAX_ITEMS, SKIP_COUNT, null);

            List<ObjectInFolderData> children = result.getObjects();
            LOG.debug(" found " + children.size() + " folders in getChildren()");
            for (ObjectInFolderData child : children) {
            	String nameChild = (String) child.getObject().getProperties().getProperties().get(PropertyIds.NAME).getFirstValue();
                if (name.equals(nameChild))
                    return child.getObject().getId();
            }
        }
        return id;
    }

    private String createFolderIntern(String name, String typeId, String parentFolderId) {
        List<String> policies = null;
        Acl addACEs = null;
        Acl removeACEs = null;
        ExtensionsData extension = null;

        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(objFactory.createPropertyIdData(PropertyIds.NAME, name));
        properties.add(objFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, typeId));
        Properties props = objFactory.createPropertiesData(properties);

        String id = null;
        try {
        id = objSvc.createFolder(repositoryId, props, parentFolderId, policies, addACEs,
                removeACEs, extension);
        } catch (CmisBaseException e) {
        	// folder already there, get it:
            ObjectInFolderList result = navSvc.getChildren(repositoryId, parentFolderId, "*", null, false,
                    IncludeRelationships.NONE, null, true, MAX_ITEMS, SKIP_COUNT, null);

            List<ObjectInFolderData> children = result.getObjects();
            LOG.debug(" found " + children.size() + " folders in getChildren()");
            for (ObjectInFolderData child : children) {
            	String nameChild = (String) child.getObject().getProperties().getProperties().get(PropertyIds.NAME).getFirstValue();
                if (name.equals(nameChild))
                    return child.getObject().getId();
            }
        }
        return id;
    }

    private ContentStream createContent() {
        ContentStreamImpl content = new ContentStreamImpl();
        content.setFileName("data.txt");
        content.setMimeType("text/plain");
        int len = 32 * 1024;
        byte[] b = { 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x0c, 0x0a,
                0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x0c, 0x0a }; // 32
        // Bytes
        ByteArrayOutputStream ba = new ByteArrayOutputStream(len);
        try {
            for (int i = 0; i < 1024; i++) {
                ba.write(b);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to fill content stream with data", e);
        }
        content.setStream(new ByteArrayInputStream(ba.toByteArray()));
        content.setLength(BigInteger.valueOf(len));
        return content;
    }

    private void updateProperties(String id, String propertyId, String propertyValue) {
        LOG.debug("updateProperties " + id);
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(objFactory.createPropertyStringData(propertyId, propertyValue));
        Properties newProps = objFactory.createPropertiesData(properties);

        Holder<String> idHolder = new Holder<String>(id);
        Holder<String> changeTokenHolder = new Holder<String>();
        objSvc.updateProperties(repositoryId, idHolder, changeTokenHolder, newProps, null);
        renameFiles("updateProperties");
        LOG.debug("updateProperties() done.");
    }

    private void deleteObject(String id) {
        LOG.debug("deleteObject " + id);
        objSvc.deleteObject(repositoryId, id, true, null);
        renameFiles("deleteObject");
        LOG.debug("deleteObject() done.");
    }

    private void cleanup () {
        LOG.debug("cleaning up...");
        for (String id : idsToDelete) {
            LOG.debug("deleting object " + id);
            objSvc.deleteObject(repositoryId, id, true, null);            
        }
        idsToDelete.clear();
        LOG.debug("... cleaning up done");
    }
    
    /**
     * enumerate the children of the root folder and return the id of the first
     * document
     * 
     * @return id of first doc in root folder
     */
    private String getTestDocId() {
        return getTestId(BaseTypeId.CMIS_DOCUMENT);
    }

    /**
     * enumerate the children of the root folder and return the id of the first
     * sub-folder
     * 
     * @return id of first doc in root folder
     */
    private String getTestFolderId() {
        return getTestId(BaseTypeId.CMIS_FOLDER);
    }

    private String getTestId(BaseTypeId baseTypeId) {
        LOG.debug("getTestDocId()");
        ObjectInFolderList result = navSvc.getChildren(repositoryId, rootFolderId, "*", null, false,
                IncludeRelationships.NONE, null, true, MAX_ITEMS, SKIP_COUNT, null);

        List<ObjectInFolderData> children = result.getObjects();
        LOG.debug(" found " + children.size() + " folders in getChildren()");
        for (ObjectInFolderData child : children) {
            if (baseTypeId.equals(child.getObject().getBaseTypeId()))
                return child.getObject().getId();
        }
        return null;
    }

    private String prepareVersionSeries(String name, String typeId, String folderId) {
        String id = createDocumentIntern(name, typeId, folderId, VersioningState.MAJOR);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        Holder<String> idHolder = new Holder<String>(id);

        verSvc.checkOut(repositoryId, idHolder, null, contentCopied);
        String checkinComment = "Checkin V2.0";
        verSvc.checkIn(repositoryId, idHolder, true /*major*/, null /*properties*/, null /*content*/,
                checkinComment, null/*policies*/, null/*addAcl*/, null /*removeAcl*/, null /*extension*/);

        verSvc.checkOut(repositoryId, idHolder, null, contentCopied);
        checkinComment = "Checkin V2.1";
        verSvc.checkIn(repositoryId, idHolder, false /*major*/, null /*properties*/, null /*content*/,
                checkinComment, null/*policies*/, null/*addAcl*/, null /*removeAcl*/, null /*extension*/);
        
        return idHolder.getValue();
    }
    
    private void checkOut(String id) {
        LOG.debug("checkOut()");        
        Holder<String> idHolder = new Holder<String>(id);
        Holder<Boolean> contentCopied = new Holder<Boolean>(true);
        verSvc.checkOut(repositoryId, idHolder, null, contentCopied);
        renameFiles("checkOut");
        LOG.debug("checkOut done.");
    }

    private void checkIn(String id, boolean major, String checkinComment) {
        LOG.debug("checkIn()");
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(objFactory.createPropertyStringData(VERSIONED_PROP, "updated value"));
        Properties newProps = objFactory.createPropertiesData(properties);

        Holder<String> idHolder = new Holder<String>(id);
        verSvc.checkIn(repositoryId, idHolder, major /*major*/, newProps /*properties*/, null /*content*/,
                checkinComment, null/*policies*/, null/*addAcl*/, null /*removeAcl*/, null /*extension*/);
        renameFiles("checkIn");
        LOG.debug("checkIn done.");
    }

    private void getAllVersions(String id) {
        LOG.debug("getAllVersions()");     
        verSvc.getAllVersions(repositoryId, id/* object id */, id/* series id */, "*"/* filter */,
                false /* includeAllowableActions */, null /* extension */);
        renameFiles("getAllVersions");
        LOG.debug("getAllVersions done.");
    }
    
    private void getAcl(String objectId) {
        LOG.debug("getting Acl() " + objectId);

        // get old ACL first:
        Acl oldAcl = aclSvc.getAcl(repositoryId, objectId, true, null);

        // create a new ACL for the test doc
        List<Ace> aces = new ArrayList<Ace>();
        aces.add(objFactory.createAccessControlEntry("Alice", Collections.singletonList("cmis:read")));
        aces.add(objFactory.createAccessControlEntry("Bob", Collections.singletonList("cmis:write")));
        aces.add(objFactory.createAccessControlEntry("admin", Collections.singletonList("cmis:all")));
        Acl acl = objFactory.createAccessControlList(aces);

        // add the new ACL and remove the old one
        aclSvc.applyAcl(repositoryId, objectId, acl, oldAcl, AclPropagation.OBJECTONLY, null);
            
        aclSvc.getAcl(repositoryId, objectId, true, null);
        renameFiles("getAcl");
        LOG.debug("getting Acl() done.");
    }

    private void getTypeDefinition(String typeId) {
        LOG.debug("getTypeDefinition " + typeId);
        repSvc.getTypeDefinition(repositoryId, typeId, null);
        renameFiles("getTypeDefinition");
        LOG.debug("getTypeDefinition() done.");
    }

    private void getTypeDescendants(String typeId) {
        LOG.debug("getTypeDescendants " + typeId);
        repSvc.getTypeDescendants(repositoryId, typeId, TYPE_DEPTH_ALL, true /* includePropertyDefinitions */, null);
        renameFiles("getTypeDescendants");
        LOG.debug("getTypeDescendants() done.");
    }

    private void getAllowableActions(String objectId) {
        LOG.debug("getAllowableActions " + objectId);
        objSvc.getAllowableActions(repositoryId, objectId, null);
        renameFiles("getAllowableActions");
        LOG.debug("getAllowableActions() done.");
    }

    private void renameFiles(String name) {
        String fileNameInReq = findLastFile(LOGDIR, "*-request.log");
        String fileNameInResp = findLastFile(LOGDIR, "*-response.log");
        if (null == fileNameInReq) {
            LOG.error("Failed to find captured request file for " + name);
            return;
        }
        if (null == fileNameInResp) {
            LOG.error("Failed to find captured response file for " + name);
            return;
        }
        File in = new File(fileNameInReq);
        File out = new File(targetDir + File.separator + name + "-request.log");
        if (out.exists())
            out.delete();
        boolean ok = in.renameTo(out);
        if (ok)
            LOG.debug("Renaming file " + in.getAbsolutePath() + " to " + out.getAbsolutePath() + " succeeded.");
        else
            LOG.warn("Renaming file " + in.getAbsolutePath() + " to " + out.getAbsolutePath() + " failed.");

        in = new File(fileNameInResp);
        out = new File(targetDir + File.separator + name + "-response.log");
        if (out.exists())
            out.delete();
        ok = in.renameTo(out);
        if (ok)
            LOG.debug("Renaming file " + in.getAbsolutePath() + "to " + out.getAbsolutePath() + " succeeded.");
        else
            LOG.warn("Renaming file " + in.getAbsolutePath() + " to " + out.getAbsolutePath() + " failed.");
    }
    
    private void createZipFile(String zipFileName, String[] dirs) {
        
        File out = new File(zipFileName);
        if (out.exists())
            out.delete();
        
        FileOutputStream fout = null;
        ZipOutputStream zout =null;
        try {
            fout = new FileOutputStream(zipFileName);
            zout = new ZipOutputStream(fout);
            for (String dir: dirs) {
                File dirToZip = new File(dir);
                addDirectory(zout, dir, dirToZip);
            }
        } catch (Exception e) {
            LOG.error("Creating ZIP file failed: " + e);
        } finally {
            try {
                if (zout != null)
                    zout.close();
                if (fout != null)
                    fout.close();
            } catch (IOException e) {
                LOG.error(e.toString(), e);
            }
        }
    }
    
    private static void addDirectory(ZipOutputStream zout, String prefix, File sourceDir) throws IOException {
        
        File[] files = sourceDir.listFiles();
        LOG.debug("Create Zip, adding directory " + sourceDir.getName());
               
        if (null != files) {
            for(int i=0; i < files.length; i++)
            {
                if(files[i].isDirectory())
                {
                    addDirectory(zout, prefix + File.separator + files[i].getName(), files[i]);
                } else {
                    LOG.debug("Create Zip, adding file " + files[i].getName());
                    byte[] buffer = new byte[65536];
                    FileInputStream fin = new FileInputStream(files[i]);
                    String zipEntryName = prefix + File.separator + files[i].getName();
                    LOG.debug("   adding entry " + zipEntryName);
                    zout.putNextEntry(new ZipEntry(zipEntryName));

                    int length;
                    while((length = fin.read(buffer)) > 0)
                    {
                        zout.write(buffer, 0, length);
                    }

                    zout.closeEntry();
                    fin.close();
                }
            }      
        }
    }
    
    public static void clean() {
        LOG.debug("Cleaning generated and captured request and response logs...");
        
        cleanFilesWithFilter(LOGDIR, "*-request.log");
        cleanFilesWithFilter(LOGDIR, "*-response.log");
        for (int i = 0; i < BINDINGS.length; i++) {
            String dir = BINDINGS[i].value();
            
            cleanFilesWithFilter(dir, "*-request.log");
            cleanFilesWithFilter(dir, "*-response.log");

            File dirToDelete = new File (dir);
            boolean ok = dirToDelete.delete();
            if (ok)
                LOG.debug("Deleting dir " + dirToDelete.getAbsolutePath() + " succeeded.");
            else
                LOG.warn("Deleting dir " + dirToDelete.getAbsolutePath() + " failed.");
        }
        new File("./target/logs/log4j.log").delete();
        LOG.debug("... done.");        
    }
    
    private static void cleanFilesWithFilter(String directoryPath, String wildcardFilter) {
        File dir = new File(directoryPath);
        FileFilter fileFilter = new WildcardFileFilter(wildcardFilter);
        File[] files = dir.listFiles(fileFilter);
        if (files != null)
            for (int i = 0; i < files.length; i++) {
                boolean ok = files[i].delete();
                LOG.debug("Deleting file: " + files[i] + ", success: " + ok);
            }        
    }
    
    private static String findLastFile(String directoryPath, String wildcardFilter) {
        File dir = new File(directoryPath);
        FileFilter fileFilter = new WildcardFileFilter(wildcardFilter);
        File[] files = dir.listFiles(fileFilter);
        LOG.debug("Number of files in filter dir " + files.length);
        if (files.length < NO_FILES_LOGGED) {
        	LOG.warn("WARNING TOO FEW FILES!");
        	// There might be some problem with disk caching, seems that listFiles
        	// does not always get the most recent state, ugly workaround
        	try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
        	files = dir.listFiles(fileFilter);
            if (files.length < NO_FILES_LOGGED) 
            	LOG.error("WARNING TOO FEW FILES EVEN AFTER SECOND TRY!!!");
        }
        NO_FILES_LOGGED = files.length;
        Arrays.sort(files);
        if (files.length == 0)
            return null;
        else
            return files[files.length-1].getAbsolutePath();
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("-clean")) {
            LOG.debug("Cleaning up generated files...");
            Main.clean();
            LOG.debug("... cleaning up done.");
        } else {
            LOG.debug("Starting generating spec examples...");
            Main main = new Main();
            main.runAllBindings();
            LOG.debug("... finished generating spec examples.");
        }
    }
    
    static private boolean deleteDirRecursive(File path) {
        if( path.exists() ) {
          File[] files = path.listFiles();
          for(int i=0; i<files.length; i++) {
             if(files[i].isDirectory()) {
                 deleteDirRecursive(files[i]);
             }
             else {
               files[i].delete();
             }
          }
        }
        return( path.delete() );
      }

    private void cleanLogFilterDir() {
        File dir = new File(LOGDIR);
        FileFilter fileFilter = new WildcardFileFilter("*-request.log");
        File[] files = dir.listFiles(fileFilter);
        for (File f : files) {
            f.delete();
        }
        fileFilter = new WildcardFileFilter("*-response.log");
        files = dir.listFiles(fileFilter);
        for (File f : files) {
            f.delete();
        }
    }
}
