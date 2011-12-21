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
package org.apache.chemistry.opencmis.util.specexamples;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
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
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Main {

    private static final Log LOG = LogFactory.getLog(Main.class.getName());
    private static final BigInteger MAX_ITEMS = BigInteger.valueOf(-1);
    private static final BigInteger SKIP_COUNT = BigInteger.valueOf(0);
    // private static final String COMPLEX_TYPE = "ComplexType";
    private static final String TOPLEVEL_TYPE = "DocumentTopLevel";
    private static final String VERSIONED_TYPE = "VersionableType";
    private static String LOGDIR = System.getProperty("java.io.tmpdir");// + File.separator;

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

    public Main() {
        bindingType = BindingType.ATOMPUB;
        init("http://localhost:8080/inmemory/atom", bindingType);
    }

    public void run() {
        // Repository Service:
        repositoryId = "A1";
        getRepositoryInfo();
        getRepositories();
        String docId = getTestDocId();
        String folderId = getTestFolderId();

        getTypeChildren(TOPLEVEL_TYPE);

        // Navigation Service:
        getChildren(folderId);
        getDescendants(folderId);

        // Object Service:
        getObject(docId);
        getAcl(docId);
        String id = createDocument("SampleDocument", TOPLEVEL_TYPE, rootFolderId, VersioningState.NONE);
        updateProperties(id, PropertyIds.NAME, "RenamedDocument");
        deleteObject(id);

        // Discovery Service:
        doQuery();

        // Versioning Service
        id = prepareVersionSeries("VersionedDocument", VERSIONED_TYPE, rootFolderId);
        checkOut(id);
        checkIn(id, true, "final version in series");
        getAllVersions(id);        
    }

    private void init(String url, BindingType bindingType) {
        LOG.debug("Initializing connection to InMemory server: ");
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(SessionParameter.USER, "admin");
        parameters.put(SessionParameter.PASSWORD, "admin");

        parameters.put(SessionParameter.BINDING_TYPE, bindingType.value());
        parameters.put(SessionParameter.ATOMPUB_URL, url);

        // get factory and create binding
        CmisBindingFactory factory = CmisBindingFactory.newInstance();
        CmisBinding binding = factory.createCmisAtomPubBinding(parameters);

        objFactory = binding.getObjectFactory();
        repSvc = binding.getRepositoryService();
        objSvc = binding.getObjectService();
        navSvc = binding.getNavigationService();
        verSvc = binding.getVersioningService();
        multiSvc = binding.getMultiFilingService();
        discSvc = binding.getDiscoveryService();
        aclSvc = binding.getAclService();
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
        RepositoryInfo repoInfo = repSvc.getRepositoryInfo(repositoryId, null);
        LOG.debug("root folder id is: " + repoInfo.getId());
        rootFolderId = repoInfo.getRootFolderId();
        renameFiles("getRepositoryInfo");
        LOG.debug("getRepositoryInfo() done.");
    }

    private void getObject(String objectId) {
        LOG.debug("getObject " + objectId);
        objSvc.getObject(repositoryId, objectId, "*", false /* includeAllowableActions */,
                IncludeRelationships.NONE /* includeRelationships */, null /* renditionFilter */,
                false /* includePolicyIds */, false /* includeAcl */, null);
        renameFiles("getObject");
        LOG.debug("getObject() done.");
    }

    private void getChildren(String folderId) {
        LOG.debug("getChildren " + folderId);
        navSvc.getChildren(repositoryId, folderId, "*", null /* orderBy */, false /* includeAllowableActions */,
                IncludeRelationships.NONE, null /* renditionFilter */, true /* includePathSegment */, MAX_ITEMS,
                SKIP_COUNT, null);
        renameFiles("getChildren");
        LOG.debug("getChildren() done.");
    }

    private void getDescendants(String folderId) {
        final BigInteger DEPTH = BigInteger.valueOf(3);
        LOG.debug("getDescendants " + folderId);
        navSvc.getDescendants(repositoryId, folderId, DEPTH, "*", false /* includeAllowableActions */,
                IncludeRelationships.NONE, null /* renditionFilter */, true /* includePathSegment */, null);
        renameFiles("getDescendants");
        LOG.debug("getDescendants() done.");
    }

    private void doQuery() {
        LOG.debug("doQuery ");
        String statement = "SELECT * from cmis:document WHERE IN_FOLDER('" + rootFolderId + "')";
        discSvc.query(repositoryId, statement, false /* searchAllVersions */, false /* includeAllowableActions */,
                IncludeRelationships.NONE, null, MAX_ITEMS, SKIP_COUNT, null);
        renameFiles("doQuery");
        LOG.debug("doQuery() done.");
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
        id = objSvc.createDocument(repositoryId, props, folderId, contentStream, versioningState, policies, addACEs,
                removeACEs, extension);
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
        Holder<Boolean> contentCopied = new Holder<Boolean>(true);
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
        Holder<String> idHolder = new Holder<String>(id);
        verSvc.checkIn(repositoryId, idHolder, major /*major*/, null /*properties*/, null /*content*/,
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
        // apply an ACL to the test doc
        List<Ace> aces = new ArrayList<Ace>();
        aces.add(objFactory.createAccessControlEntry("Alice", Collections.singletonList("cmis:read")));
        aces.add(objFactory.createAccessControlEntry("Bob", Collections.singletonList("cmis:write")));
        aces.add(objFactory.createAccessControlEntry("admin", Collections.singletonList("cmis:all")));
        Acl acl = objFactory.createAccessControlList(aces);

        if (bindingType == BindingType.ATOMPUB)
            ;// TODO: aclSvc.applyAcl(repositoryId, objectId, acl, AclPropagation.OBJECTONLY, null);
        else
            aclSvc.applyAcl(repositoryId, objectId, acl, null, AclPropagation.OBJECTONLY, null);
            
        aclSvc.getAcl(repositoryId, objectId, true, null);
        renameFiles("getAcl");
        LOG.debug("getting Acl() done.");
    }

    private void getTypeDescendants(String typeId) {
        final BigInteger DEPTH = BigInteger.valueOf(-1);
        LOG.debug("getTypeDescendants " + typeId);
        repSvc.getTypeDescendants(repositoryId, typeId, DEPTH, true /* includePropertyDefinitions */, null);
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
        File out = new File(name + "-request.log");
        if (out.exists())
            out.delete();
        boolean ok = in.renameTo(out);
        if (ok)
            LOG.debug("Renaming file " + in.getAbsolutePath() + " to " + out.getAbsolutePath() + " succeeded.");
        else
            LOG.warn("Renaming file " + in.getAbsolutePath() + " to " + out.getAbsolutePath() + " failed.");

        in = new File(fileNameInResp);
        out = new File(name + "-response.log");
        if (out.exists())
            out.delete();
        ok = in.renameTo(out);
        if (ok)
            LOG.debug("Renaming file " + in.getAbsolutePath() + "to " + out.getAbsolutePath() + " succeeded.");
        else
            LOG.warn("Renaming file " + in.getAbsolutePath() + " to " + out.getAbsolutePath() + " failed.");
    }
    
    public static void clean() {
        LOG.debug("Cleaning generated and captured request and response logs...");
        
        cleanFilesWithFilter(LOGDIR, "*-request.log");
        cleanFilesWithFilter(LOGDIR, "*-response.log");
        cleanFilesWithFilter(".", "*-request.log");
        cleanFilesWithFilter(".", "*-response.log");

        LOG.debug("...cleaning done.");        
    }
    
    private static void cleanFilesWithFilter(String directoryPath, String wildcardFilter) {
        File dir = new File(directoryPath);
        FileFilter fileFilter = new WildcardFileFilter(wildcardFilter);
        File[] files = dir.listFiles(fileFilter);
        for (int i = 0; i < files.length; i++) {
           boolean ok = files[i].delete();
           LOG.debug("Deleting file: " + files[i] + ", success: " + ok);
        }        
    }
    
    private static String findLastFile(String directoryPath, String wildcardFilter) {
        File dir = new File(directoryPath);
        FileFilter fileFilter = new WildcardFileFilter(wildcardFilter);
        File[] files = dir.listFiles(fileFilter);
        if (files.length == 0)
            return null;
        else
            return files[files.length-1].getAbsolutePath();
    }

    public static void main(String[] args) {
        LOG.debug("Starting generating spec examples...");
        if (args.length > 0 && args[0].equals("-clean"))
            Main.clean();
        else {
            Main main = new Main();
            main.run();
        }
        LOG.debug("... finsihed generating spec examples.");
    }
}
