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
package org.apache.opencmis.util.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.apache.opencmis.client.provider.factory.CmisProviderFactory;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.exceptions.CmisBaseException;
import org.apache.opencmis.commons.provider.CmisProvider;
import org.apache.opencmis.commons.provider.NavigationService;
import org.apache.opencmis.commons.provider.ObjectService;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.commons.provider.RepositoryService;

public class ObjGenApp {
  public static final String DEFAULT_USER = "";
  public static final String DEFAULT_PWD = "";
  public static final String PROP_ATOMPUB_URL = "opencmis.test.atompub.url";
  public static final String PROP_URL = "url";
  public static final String PROP_WS_URL = "opencmis.test.webservices.url";
  public static final String DEFAULT_ATOMPUB_URL = "http://localhost:8080/opencmis/atom"; 
  public static final String DEFAULT_WS_URL = "http://localhost:8080/cmis/services/";

  private final static String CMD = "Command";
  private final static String REPOSITORY_ID = "RepositoryId";
  private final static String FILLER_DOCUMENT_TYPE_ID = "DocumentTypeId";
  private final static String FILLER_FOLDER_TYPE_ID = "FolderTypeId";
  private final static String FILLER_DOCS_PER_FOLDER = "DocsPerFolder";
  private final static String FILLER_FOLDERS_PER_FOLDER = "FoldersPerFolder";
  private final static String FILLER_DEPTH = "Depth";
  private final static String FILLER_CONTENT_SIZE = "ContentSizeInKB";
  private final static String COUNT = "Count";
  private final static String BINDING = "Binding";
  private final static String BINDING_ATOM = "AtomPub";
  private final static String BINDING_WS = "WebService";

  private CmisProvider fProvider;
  private boolean fUsingAtom; 
  private String fUrlStr;
  
  OptionSpec<String> fCmd;
  OptionSpec<Integer> fDepth;
  OptionSpec<Integer> fContentSize;
  OptionSpec<Integer> fFolderPerFolder;
  OptionSpec<Integer> fDocsPerFolder;
  OptionSpec<String> fFolderType;
  OptionSpec<String> fDocType;
  OptionSpec<String> fRepoId;
  OptionSpec<Integer> fCount;
  OptionSpec<String> fBinding;
  
  public static void main(String[] args) {

    ObjGenApp app = new ObjGenApp();
    try {
      app.processCmdLine(args);
    }
    catch (CmisBaseException ce) {
      System.out.println("Error: Could not process command. " + ce);
      System.out.println("Extended error: " + ce.getErrorContent());
      ce.printStackTrace();
    }
    catch (Exception e) {
      System.out.println("Could not fill repository " + e);
      e.printStackTrace();
    }
  }

  private void processCmdLine(String[] args) {

    OptionParser parser = new OptionParser();
    fCmd = parser.accepts(CMD).withRequiredArg();
    fRepoId = parser.accepts(REPOSITORY_ID).withOptionalArg();
    fDocType = parser.accepts(FILLER_DOCUMENT_TYPE_ID).withOptionalArg().defaultsTo(
        BaseObjectTypeIds.CMIS_DOCUMENT.value());
    fFolderType = parser.accepts(FILLER_FOLDER_TYPE_ID).withOptionalArg().defaultsTo(
        BaseObjectTypeIds.CMIS_FOLDER.value());
    fDocsPerFolder = parser.accepts(FILLER_DOCS_PER_FOLDER).withOptionalArg().ofType(Integer.class);
    fFolderPerFolder = parser.accepts(FILLER_FOLDERS_PER_FOLDER).withOptionalArg().ofType(
        Integer.class);
    fDepth = parser.accepts(FILLER_DEPTH).withOptionalArg().ofType(Integer.class);
    fContentSize = parser.accepts(FILLER_CONTENT_SIZE).withOptionalArg().ofType(Integer.class);
    fCount = parser.accepts(COUNT).withOptionalArg().ofType(Integer.class).defaultsTo(10);
    fBinding = parser.accepts(BINDING).withOptionalArg().ofType(String.class).defaultsTo(BINDING_ATOM);

    OptionSet options = parser.parse(args);

    if (options.has("?"))
      try {
        parser.printHelpOn(System.out);
      }
      catch (IOException e) {
        e.printStackTrace();
      }

    if (options.valueOf(fBinding).equals(BINDING_WS)) {
      System.out.println("Using Web Service binding.");
      fUsingAtom = false;
    } else if (options.valueOf(fBinding).equals(BINDING_ATOM)) {
      System.out.println("Using AtomPub binding.");
      fUsingAtom = true;
    } else {
      System.out.println("Unknown option <Binding>: " + options.valueOf(fBinding)
          + " allowed values: " + BINDING_WS + " or " + BINDING_ATOM);
      return;
    }

    if (options.valueOf(fCmd).equals("FillRepository")) {
      fillRepository(options);
    } else if (options.valueOf(fCmd).equals("CreateDocument")) {
      createSingleDocument(options);
    } else if (options.valueOf(fCmd).equals("RepositoryInfo")) {
      repositoryInfo(options);
    } else if (options.valueOf(fCmd).equals("GetUrl")) {
      getUrl(getConfiguredUrl());
    } else {
      System.out.println("Unknown cmd: " + options.valueOf(fCmd));
    }
  }

//  private void preInitExpensiveTasks() {
//    // JAXB initialization is very expensive, count this separate:
//    TimeLogger logger = new TimeLogger("Initialization");
//    logger.start();
//    try {
//      JaxBHelper.createMarshaller();
//    }
//    catch (JAXBException e) {
//      System.out.print("Failuer in JAXB init: " + e);
//      e.printStackTrace();
//    } // dummy call just to get initialized
//    logger.stop();
//    logger.printTimes();    
//  }

  private void fillRepository(OptionSet options) {
    System.out.println();
    if (fUsingAtom)
      System.out.println("Connecting to  " + getAtomPubUrl());
    else
      System.out.println("Connecting to  " + getWsUrl());
      
    System.out.println("Filling repository " + options.valueOf(fRepoId)
        + " with following arguments:");
    System.out.println("Documents per folder: " + options.valueOf(fDocsPerFolder));
    System.out.println("Folder per folder: " + options.valueOf(fFolderPerFolder));
    System.out.println("Depth: " + options.valueOf(fDepth));
    System.out.println("Content size: " + options.valueOf(fContentSize));
    System.out.println("Document Type: " + options.valueOf(fDocType));
    System.out.println("Folder Type: " + options.valueOf(fFolderType));

    try {
      fillRepository(options.valueOf(fRepoId), options.valueOf(fDocsPerFolder), options
          .valueOf(fFolderPerFolder), options.valueOf(fDepth), options.valueOf(fDocType), options
          .valueOf(fFolderType), options.valueOf(fContentSize));
    }
    catch (Exception e) {
      System.out.println("Filling repository failed with exception: " + e);
      e.printStackTrace();
    }
  }

  private void fillRepository(String repoId, int docsPerFolder, int foldersPerFolders, int depth,
      String documentType, String folderType, int contentSizeInKB) {

    ProviderObjectFactory objectFactory = getObjectFactory();
    NavigationService navSvc = getProvider().getNavigationService();
    ObjectService objSvc = getProvider().getObjectService();
    RepositoryService repSvc = getProvider().getRepositoryService();

    ObjectGenerator gen = new ObjectGenerator(objectFactory, navSvc, objSvc, repoId);
    gen.setUseUuidsForNames(true);
    gen.setNumberOfDocumentsToCreatePerFolder(docsPerFolder);
    // Set the type id for all created documents:
    gen.setDocumentTypeId(documentType);
    // Set the type id for all created folders:
    gen.setFolderTypeId(folderType);
    // Set contentSize
    gen.setContentSizeInKB(contentSizeInKB);
    // Simulate a runtime context with configuration parameters
    // Attach the CallContext to a thread local context that can be accessed from everywhere

    // Build the tree
    RepositoryInfoData rep = repSvc.getRepositoryInfo(repoId, null);
    String rootFolderId = rep.getRootFolderId();

    gen.resetCounters();
    gen.createFolderHierachy(depth, foldersPerFolders, rootFolderId);
    System.out.println("Filling repository succeeded.");
    System.out.println("Number of documents created: " + gen.getDocumentsInTotal());
    System.out.println("Number of folders created: " + gen.getFoldersInTotal());
    gen.printTimings();
  }

  private void createSingleDocument(OptionSet options) {
    System.out.println();
    System.out.println("Connecting to  " + getAtomPubUrl());
    System.out.println("Creating doc in repository " + options.valueOf(fRepoId));

    createSingleDocument(options.valueOf(fRepoId), options.valueOf(fContentSize));

  }

  private void createSingleDocument(String repoId, int contentSizeInKB) {

    ProviderObjectFactory objectFactory = getObjectFactory();
    NavigationService navSvc = getProvider().getNavigationService();
    ObjectService objSvc = getProvider().getObjectService();
    RepositoryService repSvc = getProvider().getRepositoryService();
    ObjectGenerator gen = new ObjectGenerator(objectFactory, navSvc, objSvc, repoId);
    TimeLogger timeLogger = new TimeLogger("Initialize");
    timeLogger.start();
    RepositoryInfoData rep = repSvc.getRepositoryInfo(repoId, null);
    timeLogger.stop();
    timeLogger.printTimes();
    String rootFolderId = rep.getRootFolderId();
    gen.setContentSizeInKB(contentSizeInKB);
    gen.setUseUuidsForNames(true);
    gen.createSingleDocument(rootFolderId);
    System.out.println("Document creation succeeded.");
    gen.printTimings();

    gen.resetCounters();
  }

  private void callRepoInfo(String repositoryId) {
    RepositoryService repSvc = getProvider().getRepositoryService();
    TimeLogger timeLogger = new TimeLogger("RepoInfoTest");
    int n = 5;
    for (int i = 0; i < n; i++) {
      fProvider.clearRepositoryCache(repositoryId);
      timeLogger.start();
      repSvc.getRepositoryInfo(repositoryId, null);
      timeLogger.stop();
    }
    timeLogger.printTimes();
  }

  private void repositoryInfo(OptionSet options) {
    callRepoInfo(options.valueOf(fRepoId));
  }

  private ProviderObjectFactory getObjectFactory() {
    return getProvider().getObjectFactory();
  }

  private CmisProvider getProvider() {
    if (fProvider == null) {
      if (fUsingAtom)
        fProvider = createAtomProvider(getAtomPubUrl(), DEFAULT_USER, DEFAULT_PWD);
      else
        fProvider = createWsProvider(getWsUrl(), DEFAULT_USER, DEFAULT_PWD);
    }
    return fProvider;
  }

  private static void filLoginParams(Map<String, String> parameters, String user, String password) {
    if (user != null && !user.isEmpty())
      parameters.put(SessionParameter.USER, user);
    if (user != null && !user.isEmpty())
      parameters.put(SessionParameter.PASSWORD, password);    
  }
  
  private static CmisProvider createAtomProvider(String url, String user, String password) {
    
    // gather parameters
    Map<String, String> parameters = new HashMap<String, String>();
    filLoginParams(parameters, user, password);

    // get factory and create provider
    CmisProviderFactory factory = CmisProviderFactory.newInstance();
    parameters.put(SessionParameter.ATOMPUB_URL, url);
    CmisProvider provider = factory.createCmisAtomPubProvider(parameters);
    return provider;
  }

  private static CmisProvider createWsProvider(String url, String username, String password) {
    boolean isPrefix = true;
    String urlLower = url.toLowerCase();

    if (urlLower.endsWith("?wsdl")) {
      isPrefix = false;
    }
    else if (urlLower.endsWith(".wsdl")) {
      isPrefix = false;
    }
    else if (urlLower.endsWith(".xml")) {
      isPrefix = false;
    }

    return createProvider(url, isPrefix, username, password);
  }

  public static CmisProvider createProvider(String url, boolean isPrefix, String username,
      String password) {
    // gather parameters
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(SessionParameter.USER, username);
    parameters.put(SessionParameter.PASSWORD, password);

    if (!isPrefix) {
      parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, url);
      parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, url);
    }
    else {
      parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, url
          + "RepositoryService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, url
          + "NavigationService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, url + "ObjectService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, url
          + "VersioningService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, url + "DiscoveryService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, url
          + "RelationshipService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, url
          + "MultiFilingService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, url + "PolicyService?wsdl");
      parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, url + "ACLService?wsdl");
    }

    // get factory and create provider
    CmisProviderFactory factory = CmisProviderFactory.newInstance();
    CmisProvider provider = factory.createCmisWebServicesProvider(parameters);

    return provider;
  }

    
  private String getAtomPubUrl() {
    return System.getProperty(PROP_ATOMPUB_URL, DEFAULT_ATOMPUB_URL);
  }

  private String getWsUrl() {
    return System.getProperty(PROP_WS_URL, DEFAULT_WS_URL);
  }

  private String getConfiguredUrl() {
    return System.getProperty(PROP_URL, fUrlStr);
  }

  private void getUrl(String urlStr) {
    URL url;
    InputStream is;
    InputStreamReader isr;
    BufferedReader r;
    String str;

    try {
      System.out.println("Reading URL: " + urlStr);
      url = new URL(urlStr);
      is = url.openStream();
      isr = new InputStreamReader(is);
      r = new BufferedReader(isr);
      do {
        str = r.readLine();
        if (str != null)
          System.out.println(str);
      } while (str != null);
    }
    catch (MalformedURLException e) {
      System.out.println("Must enter a valid URL" + e);
    }
    catch (IOException e) {
      System.out.println("Can not connect" + e);
    }
  }

  
}
