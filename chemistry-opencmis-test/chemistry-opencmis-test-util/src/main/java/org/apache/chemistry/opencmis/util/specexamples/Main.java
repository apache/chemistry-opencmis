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
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
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
    
    private BindingsObjectFactory fFactory = new BindingsObjectFactoryImpl();
    private String rootFolderId;
    private String repositoryId;
    private ObjectService objSvc;
    private NavigationService navSvs;
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
    }
    
    private void init(String repositoryId) {
        LOG.debug("Initializing connection to InMemory server: ");
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(SessionParameter.USER, "admin");
        parameters.put(SessionParameter.PASSWORD, "admin");

        parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());        
        parameters.put(SessionParameter.ATOMPUB_URL, "http://localhost:8080/inmemory/atom");
        parameters.put(SessionParameter.USER, "david");
        parameters.put(SessionParameter.PASSWORD, "none");

        // get factory and create binding
        CmisBindingFactory factory = CmisBindingFactory.newInstance();
        CmisBinding binding = factory.createCmisAtomPubBinding(parameters);

        fFactory = binding.getObjectFactory();
        repSvc = binding.getRepositoryService();
        objSvc = binding.getObjectService();
        navSvs = binding.getNavigationService();
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
        rootFolderId =  repoInfo.getId();        
        LOG.debug("getRepositoryInfo() done.");
        renameFiles("getRepositoryInfo");
    }
    
    private void renameFiles(String name) {
        String fileNameInReq = String.format("%05d-request.log", requestCounter);
        String fileNameInResp = String.format("%05d-response.log", requestCounter++);
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
