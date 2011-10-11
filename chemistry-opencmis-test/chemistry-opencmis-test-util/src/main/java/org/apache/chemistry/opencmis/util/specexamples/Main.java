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

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.spi.AclService;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.apache.chemistry.opencmis.commons.spi.DiscoveryService;
import org.apache.chemistry.opencmis.commons.spi.MultiFilingService;
import org.apache.chemistry.opencmis.commons.spi.NavigationService;
import org.apache.chemistry.opencmis.commons.spi.ObjectService;
import org.apache.chemistry.opencmis.commons.spi.RepositoryService;
import org.apache.chemistry.opencmis.commons.spi.VersioningService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Main {

    private static final Log LOG = LogFactory.getLog(Main.class.getName());
    private BigInteger MAX_ITEMS = BigInteger.valueOf(-1);
    private BigInteger SKIP_COUNT = BigInteger.valueOf(0);
    private static final String COMPLEX_TYPE = "ComplexType";
    private static final String TOPLEVEL_TYPE = "DocumentTopLevel";
    
    private BindingsObjectFactory objFactory = new BindingsObjectFactoryImpl();
    private String rootFolderId;
    private String repositoryId;
    private ObjectService objSvc;
    private NavigationService navSvc;
    private RepositoryService repSvc;
    private VersioningService verSvc;
    private MultiFilingService multiSvc;
    private DiscoveryService discSvc;
    private AclService aclSvc;


    private int requestCounter = 0;
    private String logDir = System.getProperty("java.io.tmpdir") + File.separator;
    
    public Main() {
        init("A1");        
    }
    
    public void run() {
        
        getRepositoryInfo();
        String docId = getTestDocId();
        requestCounter++;
        String folderId = getTestFolderId();
        getAcl(docId);
        getAllowableActions(docId);
        // getChangeLog();
        getDocumentEntry(docId);
        getFolderChildren(folderId);
        getFolderDescendants(folderId);
        getFolderEntry(folderId);
        //getPolicyEntry(id);
        //getRelationshipyEntry(id);
        doQuery();
        getTypeChildren(TOPLEVEL_TYPE);
        //getTypeDescendants();        
        getTypeDocumentWith(COMPLEX_TYPE);
//        getTypeDocumentWithout(COMPLEX_TYPE);
        getTypeFolderWith("cmis:folder");
//        getTypeFolderWithout("cmis:folder");
        //getTypePolicyWith("cmis:policy");
        //getTypePolicyWithout("cmis:policy");
        //getTypeRelationshipWith("cmis:relationship");
        //getTypeRelationshipWithout("cmis:relationship");
        
    }
    
    private void init(String repositoryId) {
        LOG.debug("Initializing connection to InMemory server: ");
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(SessionParameter.USER, "admin");
        parameters.put(SessionParameter.PASSWORD, "admin");

        parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());        
        parameters.put(SessionParameter.ATOMPUB_URL, "http://localhost:8080/inmemory/atom");

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
        
        this.repositoryId = repositoryId;
        LOG.debug("Initializing done. ");               
    }
    
    private void getRepositoryInfo() {
        LOG.debug("getting repository info for repository " + repositoryId);
        RepositoryInfo repoInfo = repSvc.getRepositoryInfo(repositoryId, null);
        LOG.debug("root folder id is: " + repoInfo.getId());
        rootFolderId =  repoInfo.getRootFolderId();        
        renameFiles("getRepositoryInfo");
        requestCounter++;
        LOG.debug("getRepositoryInfo() done.");
    }
    
    private void getAcl(String objectId) {
        LOG.debug("getting Acl() " + objectId);
        // apply an ACL to the test doc
        List<Ace> aces = new ArrayList<Ace>();
        aces.add (objFactory.createAccessControlEntry("Alice", Collections.singletonList("cmis:read")));
        aces.add (objFactory.createAccessControlEntry("Bob", Collections.singletonList("cmis:write")));
        aces.add (objFactory.createAccessControlEntry("admin", Collections.singletonList("cmis:all")));
        Acl acl = objFactory.createAccessControlList(aces);
        aclSvc.applyAcl(repositoryId, objectId, acl, null, AclPropagation.OBJECTONLY, null);
        requestCounter +=2; // note one internal in bindings layer
        aclSvc.getAcl(repositoryId, objectId, true, null);
        renameFiles("getAcl");
        requestCounter++;
        LOG.debug("getting Acl() done.");
    }

    private void getAllowableActions(String objectId) {
        LOG.debug("getAllowableActions " + objectId);
        objSvc.getAllowableActions(repositoryId, objectId, null);
        renameFiles("getAllowableActions");
        requestCounter++;
        LOG.debug("getAllowableActions() done.");
    }

    private void getChangeLog() {
        LOG.debug("getChangeLog " + repositoryId);
        LOG.debug("Not implemented.");
        // renameFiles("getChangeLog");
        // requestCounter++;
        LOG.debug("getChangeLog() done.");
    }

    private void getDocumentEntry(String objectId) {
        LOG.debug("getDocumentEntry " + objectId);
        objSvc.getObject(repositoryId, objectId, "*", false /*includeAllowableActions*/, IncludeRelationships.NONE /*includeRelationships*/,
                null /*renditionFilter*/, false /*includePolicyIds*/, false /*includeAcl*/, null);
        renameFiles("getDocumentEntry");
        requestCounter++;
        LOG.debug("getDocumentEntry() done.");
    }

    private void getFolderChildren(String folderId) {
        LOG.debug("getFolderChildren " + folderId);
        navSvc.getChildren(repositoryId, folderId, "*", null /*orderBy*/, false /*includeAllowableActions*/,
                IncludeRelationships.NONE, null /*renditionFilter*/, true /*includePathSegment*/, MAX_ITEMS, 
                SKIP_COUNT, null);
        renameFiles("getFolderChildren");
        requestCounter++;
        LOG.debug("getFolderChildren() done.");
    }

    private void getFolderDescendants(String folderId) {
        final BigInteger DEPTH = BigInteger.valueOf(3);
        LOG.debug("getFolderDescendants " + folderId);
        navSvc.getDescendants(repositoryId, folderId, DEPTH, "*", false /*includeAllowableActions*/, IncludeRelationships.NONE,
                null /*renditionFilter*/, true /*includePathSegment*/, null);
        renameFiles("getFolderDescendants");
        requestCounter++;
        LOG.debug("getFolderDescendants() done.");
    }

    private void getFolderEntry(String folderId) {
        LOG.debug("getFolderEntry " + folderId);
        objSvc.getObject(repositoryId, folderId, "*", false /*includeAllowableActions*/, IncludeRelationships.NONE /*includeRelationships*/,
                null /*renditionFilter*/, false /*includePolicyIds*/, false /*includeAcl*/, null);
        renameFiles("getFolderEntry");
        requestCounter++;
        LOG.debug("getFolderEntry() done.");
    }

    private void getPolicyEntry(String policyId) {
        LOG.debug("getPolicyEntry " + policyId);
        objSvc.getObject(repositoryId, policyId, "*", false /*includeAllowableActions*/, IncludeRelationships.NONE /*includeRelationships*/,
                null /*renditionFilter*/, false /*includePolicyIds*/, false /*includeAcl*/, null);
        renameFiles("getPolicyEntry");
        requestCounter++;
        LOG.debug("getPolicyEntry() done.");
    }

    private void getRelationshipyEntry(String relId) {
        LOG.debug("getRelationshipyEntry " + relId);
        objSvc.getObject(repositoryId, relId, "*", false /*includeAllowableActions*/, IncludeRelationships.NONE /*includeRelationships*/,
                null /*renditionFilter*/, false /*includePolicyIds*/, false /*includeAcl*/, null);
        renameFiles("getRelationshipyEntry");
        requestCounter++;
        LOG.debug("getRelationshipyEntry() done.");
    }

    private void doQuery() {
        LOG.debug("doQuery ");
        String statement = "SELECT * from cmis:document WHERE IN_FOLDER('" + rootFolderId + "')";
        discSvc.query(repositoryId, statement, false /*searchAllVersions*/, false /*includeAllowableActions*/,
                IncludeRelationships.NONE, null, MAX_ITEMS, SKIP_COUNT, null);
        renameFiles("doQuery");
        requestCounter++;
        LOG.debug("doQuery() done.");
    }

    private void getTypeChildren(String typeId) {
        LOG.debug("getTypeChildren " + typeId);
        requestCounter++;
        repSvc.getTypeChildren(repositoryId, typeId, true /*includePropertyDefinitions*/, MAX_ITEMS, 
                SKIP_COUNT, null);
        renameFiles("getTypeChildren");
        requestCounter++;
        LOG.debug("getTypeChildren() done.");
    }

    /*
    private void getTypeDescendants(String typeId) {
        final BigInteger DEPTH = BigInteger.valueOf(-1);
        LOG.debug("getTypeDescendants " + typeId);
        repSvc.getTypeDescendants(repositoryId, typeId, DEPTH, true /*includePropertyDefinitions* /,null);
        renameFiles("getTypeDescendants");
        LOG.debug("getTypeDescendants() done.");
    }
    */
    
    private void getTypeDocumentWith(String typeId) {
        LOG.debug("getTypeDocumentWith " + typeId);
        LOG.debug("Not implemented.");
        getType(typeId, true);
        renameFiles("getTypeDocumentWith");
        requestCounter++;
        LOG.debug("getTypeDocumentWith() done.");
    }

    private void getTypeDocumentWithout(String typeId) {
        LOG.debug("getTypeDocumentWithout " + typeId);
        getType(typeId, false);
        renameFiles("getTypeDocumentWithout");
        requestCounter++;
        LOG.debug("getTypeDocumentWithout() done.");
    }

    private void getTypeFolderWith(String typeId) {
        LOG.debug("getTypeFolderWith " + typeId);
        getType(typeId, true);
        renameFiles("getTypeFolderWith");
        requestCounter++;
        LOG.debug("getTypeFolderWith() done.");
    }

    private void getTypeFolderWithout(String typeId) {
        LOG.debug("getTypeFolderWithout " + typeId);
        getType(typeId, false);
        renameFiles("getTypeFolderWithout");
        requestCounter++;
        LOG.debug("getTypeFolderWithout() done.");
    }

    private void getTypePolicyWith(String typeId) {
        LOG.debug("getTypePolicyWith " + typeId);
        getType(typeId, true);
        renameFiles("getTypePolicyWith");
        requestCounter++;
        LOG.debug("getTypePolicyWith() done.");
    }

    private void getTypePolicyWithout(String typeId) {
        LOG.debug("getTypePolicyWithout " + typeId);
        getType(typeId, false);
        renameFiles("getTypePolicyWithout");
        requestCounter++;
        LOG.debug("getTypePolicyWithout() done.");
    }

    private void getTypeRelationshipWith(String typeId) {
        LOG.debug("getTypeRelationshipWith " + typeId);
        getType(typeId, true);
        renameFiles("getTypeRelationshipWith");
        requestCounter++;
        LOG.debug("getTypeRelationshipWith() done.");
    }

    private void getTypeRelationshipWithout(String typeId) {
        LOG.debug("getTypeRelationshipyWithout " + typeId);
        getType(typeId, false);
        renameFiles("getTypeRelationshipyWithout");
        requestCounter++;
        LOG.debug("getTypeRelationshipyWithout() done.");
    }
    
    /**
     * enumerate the children of the root folder and return the id
     * of the first document
     * @return id of first doc in root folder
     */
    private String getTestDocId() {
        return getTestId(BaseTypeId.CMIS_DOCUMENT);
    }
    
    /**
     * enumerate the children of the root folder and return the id
     * of the first sub-folder
     * @return id of first doc in root folder
     */
    private String getTestFolderId() {
        return getTestId(BaseTypeId.CMIS_FOLDER);
    }

    private String getTestId(BaseTypeId baseTypeId) {
        LOG.debug("getTestDocId()");
        ObjectInFolderList result = navSvc.getChildren(repositoryId, rootFolderId, "*", null, false,
                IncludeRelationships.NONE, null, true, MAX_ITEMS, SKIP_COUNT, null);
        requestCounter++;

        List<ObjectInFolderData> children = result.getObjects();
        LOG.debug(" found " + children.size() + " folders in getChildren()");
        for (ObjectInFolderData child : children) {
            if (baseTypeId.equals(child.getObject().getBaseTypeId())) 
                return child.getObject().getId();
        }
        return null;
    }
        
    private TypeDefinition getType(String typeId, boolean withPropDefs) {
        return repSvc.getTypeDefinition(repositoryId, typeId, null);
    }
    
    private void renameFiles(String name) {
        String fileNameInReq = String.format("%05d-request.xml", requestCounter);
        String fileNameInResp = String.format("%05d-response.xml", requestCounter);
        File in = new File(logDir + fileNameInReq);
        File out = new File(name + "-request.log");
        if (out.exists())
            out.delete();
        boolean ok = in.renameTo(out);
        if (ok)
            LOG.debug("Renaming file " + in.getAbsolutePath() + " to " + out.getAbsolutePath() + " succeeded.");
        else
            LOG.warn("Renaming file " + in.getAbsolutePath() + " to " + out.getAbsolutePath() + " failed.");

        in = new File(logDir + fileNameInResp);
        out = new File(name + "-response.log");
        if (out.exists())
            out.delete();
        ok = in.renameTo(out);
        if (ok)
            LOG.debug("Renaming file " + in.getAbsolutePath() +  "to " + out.getAbsolutePath() + " succeeded.");
        else
            LOG.warn("Renaming file " + in.getAbsolutePath() + " to " + out.getAbsolutePath() + " failed.");
    }
    
    public static void main(String[] args) {
        LOG.debug("Starting generating spec examples...");
        Main main = new Main();
        main.run();
        LOG.debug("... finsihed generating spec examples.");
    }
}
