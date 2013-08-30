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
package org.apache.chemistry.opencmis.tools.main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.apache.chemistry.opencmis.commons.spi.RepositoryService;
import org.apache.chemistry.opencmis.tools.filecopy.FileCopier;
import org.apache.chemistry.opencmis.util.repository.MultiThreadedObjectGenerator;
import org.apache.chemistry.opencmis.util.repository.ObjectGenerator;
import org.apache.chemistry.opencmis.util.repository.ObjectGenerator.ContentKind;
import org.apache.chemistry.opencmis.util.repository.TimeLogger;

public class ObjGenApp {

    private static final int BUFSIZE = 64 * 1024;
    private static final String PROP_USER = SessionParameter.USER;
    private static final String PROP_PASSWORD = SessionParameter.PASSWORD;
    private static final String DEFAULT_USER = "user";
    private static final String DEFAULT_PASSWORD = "dummy";
    private static final String PROP_ATOMPUB_URL = SessionParameter.ATOMPUB_URL;
    private static final String PROP_WS_URL = "org.apache.chemistry.opencmis.binding.webservices.url";
    private static final String PROP_BROWSER_URL = SessionParameter.BROWSER_URL;
    private static final String PROP_BINDING = SessionParameter.BINDING_TYPE;
    private static final String PROP_CUSTOM = "org.apache.chemistry.opencmis.binding.header.";
    private static final String DEFAULT_ATOMPUB_URL = "http://localhost:8080/inmemory/atom";
    private static final String DEFAULT_WS_URL = "http://localhost:8080/inmemory/services/";
    private static final String DEFAULT_BROWSER_BINDING_URL = "http://localhost:8080/inmemory/browser/";
    private static final String DEFAULT_BINDING = "atompub";
    private static final String CMD = "Command";
    private static final String REPOSITORY_ID = "RepositoryId";
    private static final String FILLER_DOCUMENT_TYPE_ID = "DocumentTypeId";
    private static final String FILLER_FOLDER_TYPE_ID = "FolderTypeId";
    private static final String FILLER_DOCS_PER_FOLDER = "DocsPerFolder";
    private static final String FILLER_FOLDERS_PER_FOLDER = "FoldersPerFolder";
    private static final String FILLER_DEPTH = "Depth";
    private static final String FILLER_CONTENT_SIZE = "ContentSizeInKB";
    private static final String COUNT = "Count";
    private static final String CLEANUP = "Cleanup";
    private static final String ROOTFOLDER = "RootFolder";
    private static final String THREADS = "Threads";
    private static final String CONTENT_KIND = "ContentKind";
    private static final String FILE_NAME_PATTERN = "FileName";
    private static final String LOCAL_FILE = "File";
    private static final String LOCAL_DIR = "Dir";
    private static final String BINDING_ATOM = "atompub";
    private static final String BINDING_WS = "webservices";
    private static final String BINDING_BROWSER = "browser";

    private BindingType fBindingType;
    private ContentKind fContentKind;
    private CmisBinding binding;

    private OptionSpec<String> fCmd;
    private OptionSpec<Integer> fDepth;
    private OptionSpec<Integer> fContentSize;
    private OptionSpec<Integer> fFolderPerFolder;
    private OptionSpec<Integer> fDocsPerFolder;
    private OptionSpec<String> fFolderType;
    private OptionSpec<String> fDocType;
    private OptionSpec<String> fRepoId;
    private OptionSpec<Integer> fCount;
    private OptionSpec<Boolean> fCleanup;
    private OptionSpec<String> fRootFolder;
    private OptionSpec<Integer> fThreads;
    private OptionSpec<String> fContentKindStr;
    private OptionSpec<String> fFileNamePattern;
    private OptionSpec<String> fLocalDir;
    private OptionSpec<String> fLocalFile;

    public static void main(String[] args) {

        ObjGenApp app = new ObjGenApp();
        try {
            app.processCmdLine(args);
        } catch (CmisBaseException ce) {
            System.out.println("Error: Could not process command. " + ce);
            System.out.println("Extended error: " + ce.getErrorContent());
            ce.printStackTrace();
        } catch (Exception e) {
            System.out.println("Could not fill repository " + e);
            e.printStackTrace();
        }
    }

    private void processCmdLine(String[] args) {

        OptionParser parser = new OptionParser();
        fCmd = parser.accepts(CMD).withRequiredArg().describedAs("Command to perform (see below)");
        fRepoId = parser.accepts(REPOSITORY_ID).withOptionalArg().describedAs("Repository used");
        fDocType = parser.accepts(FILLER_DOCUMENT_TYPE_ID).withOptionalArg()
                .defaultsTo(BaseTypeId.CMIS_DOCUMENT.value()).describedAs("Document type created");
        fFolderType = parser.accepts(FILLER_FOLDER_TYPE_ID).withOptionalArg()
                .defaultsTo(BaseTypeId.CMIS_FOLDER.value()).describedAs("Folder type created");
        fDocsPerFolder = parser.accepts(FILLER_DOCS_PER_FOLDER).withOptionalArg().ofType(Integer.class)
                .describedAs("Documents on each level").defaultsTo(1);
        fFolderPerFolder = parser.accepts(FILLER_FOLDERS_PER_FOLDER).withOptionalArg().ofType(Integer.class)
                .describedAs(" Folders on each level").defaultsTo(0);
        fDepth = parser.accepts(FILLER_DEPTH).withOptionalArg().ofType(Integer.class).describedAs("Levels of folders")
                .defaultsTo(1);
        fContentSize = parser.accepts(FILLER_CONTENT_SIZE).withOptionalArg().ofType(Integer.class)
                .describedAs("Content size of each doc").defaultsTo(0);
        fCount = parser.accepts(COUNT).withOptionalArg().ofType(Integer.class).defaultsTo(1)
                .describedAs("Repeat a command n times (partially implemented)");
        fCleanup = parser.accepts(CLEANUP).withOptionalArg().ofType(Boolean.class).defaultsTo(false)
                .describedAs("Clean all created objects at the end");
        fRootFolder = parser.accepts(ROOTFOLDER).withOptionalArg().ofType(String.class)
                .describedAs("folder id used as root to create objects (default repository root folder)");
        fThreads = parser.accepts(THREADS).withOptionalArg().ofType(Integer.class).defaultsTo(1)
                .describedAs("Number of threads to start in parallel");
        fContentKindStr = parser.accepts(CONTENT_KIND).withOptionalArg().ofType(String.class).defaultsTo("lorem/text")
                .describedAs("kind of content: static/text, lorem/text, lorem/html, fractal/jpeg");
        fFileNamePattern = parser.accepts(FILE_NAME_PATTERN).withOptionalArg().ofType(String.class)
                .defaultsTo("ContentData-%03d.bin").describedAs("file name pattern to be used with CreateFiles action");
        fLocalDir = parser.accepts(LOCAL_DIR).withOptionalArg().ofType(String.class).defaultsTo(".")
                .describedAs("name of a directory to be recursively copied to the repository");
        fLocalFile = parser.accepts(LOCAL_FILE).withOptionalArg().ofType(String.class)
                .describedAs("file name of a file to be copied to the repository");
        OptionSet options = parser.parse(args);

        if (options.valueOf(fCmd) == null || options.has("?")) {
            usage(parser);
        }

        String bindingStr = getBindingProperty();

        if (bindingStr.equals(BINDING_WS)) {
            fBindingType = BindingType.WEBSERVICES;
        } else if (bindingStr.equals(BINDING_ATOM)) {
            fBindingType = BindingType.ATOMPUB;
        } else if (bindingStr.equals(BINDING_BROWSER)) {
            fBindingType = BindingType.BROWSER;
        } else {
            System.out.println("Error: Unknown binding: " + bindingStr + " allowed values: " + BINDING_WS + " or "
                    + BINDING_ATOM + " or " + BINDING_BROWSER);
            return;
        }

        String kind = options.valueOf(fContentKindStr);
        if (null == kind) {
            if (options.valueOf(fContentSize) > 0) {
                fContentKind = ObjectGenerator.ContentKind.STATIC_TEXT;
            } else {
                fContentKind = null;
            }
        } else if (kind.equals("static/text")) {
            fContentKind = ObjectGenerator.ContentKind.STATIC_TEXT;
        } else if (kind.equals("lorem/text")) {
            fContentKind = ObjectGenerator.ContentKind.LOREM_IPSUM_TEXT;
        } else if (kind.equals("lorem/html")) {
            fContentKind = ObjectGenerator.ContentKind.LOREM_IPSUM_HTML;
        } else if (kind.equals("fractal/jpeg")) {
            fContentKind = ObjectGenerator.ContentKind.IMAGE_FRACTAL_JPEG;
        } else {
            System.out.println("Unknown content kind: " + options.valueOf(fContentKindStr));
            System.out.println("  must be one of static/text, lorem/text, lorem/html, fractal/jpeg");
            usage(parser);
        }

        initClientBindings();

        if (null == options.valueOf(fCmd)) {
            System.out.println("No command given.");
            usage(parser);
        } else if (options.valueOf(fCmd).equals("FillRepository")) {
            fillRepository(options);
        } else if (options.valueOf(fCmd).equals("CreateDocument")) {
            createSingleDocument(options);
        } else if (options.valueOf(fCmd).equals("CreateFolder")) {
            createFolders(options);
        } else if (options.valueOf(fCmd).equals("RepositoryInfo")) {
            repositoryInfo(options);
        } else if (options.valueOf(fCmd).equals("CreateFiles")) {
            createFiles(options);
        } else if (options.valueOf(fCmd).equals("CopyFiles")) {
            transferFiles(options);
        } else if (options.valueOf(fCmd).equals("CopyFilesTest")) { // undocumented
            transferFilesTest(options);
        } else {
            System.out.println("Unknown cmd: " + options.valueOf(fCmd));
            usage(parser);
        }
    }

    private static void usage(OptionParser parser) {
        try {
            System.out.println();
            System.out.println("ObjGenApp is a command line tool for testing a CMIS repository.");
            System.out.println("Usage:");
            parser.printHelpOn(System.out);
            System.out.println();
            System.out
                    .println("Command is one of [CreateDocument, CreateFolder, FillRepository, RepositoryInfo, CreateFiles, "
                            + "CopyFiles, CopyFilesTest]");
            System.out.println("JVM system properties: " + PROP_ATOMPUB_URL + ", " + PROP_WS_URL + ", "
                    + PROP_BROWSER_URL);
            System.out.println("                       " + PROP_USER + ", " + PROP_PASSWORD);
            System.out.println();
            System.out.println("Example: ");
            System.out
                    .println("java -D"
                            + PROP_ATOMPUB_URL
                            + "=http://localhost:8080/inmemory/atom -cp ... "
                            + "org.apache.chemistry.opencmis.util.repository.ObjGenApp --Binding=AtomPub --Command=CreateDocument "
                            + "--RepositoryId=A1 --ContentSizeInKB=25 --ContentKind=lorem/text");
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillRepository(String repoId, int docsPerFolder, int foldersPerFolders, int depth,
            String documentType, String folderType, int contentSizeInKB, String rootFolderId, boolean doCleanup) {

        MultiThreadedObjectGenerator.ObjectGeneratorRunner runner = MultiThreadedObjectGenerator.prepareForCreateTree(
                binding, repoId, docsPerFolder, foldersPerFolders, depth, documentType, folderType, contentSizeInKB,
                rootFolderId, fContentKind, doCleanup);
        ObjectGenerator gen = runner.getObjectGenerator();
        runner.doCreateTree();

        System.out.println();
        System.out.println("Result:");
        System.out.println("Filling repository succeeded.");
        System.out.println("Folder used as root for creation (null=rootFolderId): " + rootFolderId);
        System.out.println("Number of documents created: " + gen.getDocumentsInTotal());
        System.out.println("Number of folders created: " + gen.getFoldersInTotal());
        gen.printTimings();
    }

    private void fillRepositoryMT(int noThreads, String repoId, int docsPerFolder, int foldersPerFolders, int depth,
            String documentType, String folderType, int contentSizeInKB, String rootFolderId, boolean doCleanup) {

        // Step 1: create a root folder for each thread
        MultiThreadedObjectGenerator.ObjectGeneratorRunner runner = MultiThreadedObjectGenerator
                .prepareForCreateFolder(binding, repoId, folderType, rootFolderId, noThreads, doCleanup);
        String[] folderIds = runner.doCreateFolder();

        // Step 2: fill each root folder with an object tree
        MultiThreadedObjectGenerator.ObjectGeneratorRunner[] runners = MultiThreadedObjectGenerator
                .prepareForCreateTreeMT(binding, repoId, docsPerFolder, foldersPerFolders, depth, documentType,
                        folderType, contentSizeInKB, folderIds, fContentKind, doCleanup);

        MultiThreadedObjectGenerator.runMultiThreaded(runners);
        System.out.println("Filling repository succeeded.");
    }

    private void printParameters(OptionSet options) {
        if (fBindingType == BindingType.ATOMPUB) {
            System.out.println("Using AtomPub, connecting to  " + getAtomPubUrl());
        } else if (fBindingType == BindingType.WEBSERVICES) {
            System.out.println("Using WebService, connecting to  " + getWsUrl());
        } else if (fBindingType == BindingType.BROWSER) {
            System.out.println("Using Browser binding, connecting to  " + getBrowserUrl());
        } else {
            System.out.println("Unknown binding type.");
        }

        System.out.println("Repository id is: " + options.valueOf(fRepoId));
        System.out.println("Content size: " + options.valueOf(fContentSize));
        System.out.println("Document Type: " + options.valueOf(fDocType));
        System.out.println("Folder id used as root: " + options.valueOf(fRootFolder));
        System.out.println("Delete all objects after creation: " + options.valueOf(fCleanup));
        System.out.println("Number of actions to perform: " + options.valueOf(fCount));
        System.out.println("Number of threads to start: " + options.valueOf(fThreads));
        System.out.println("Kind of created content: " + options.valueOf(fContentKindStr));
    }

    private void createSingleDocument(OptionSet options) {
        System.out.println();
        System.out.println("Creating document with parameters:");
        printParameters(options);
        int noThreads = options.valueOf(fThreads);
        if (noThreads <= 1) {
            createSingleDocument(options.valueOf(fRepoId), options.valueOf(fDocType), options.valueOf(fContentSize),
                    options.valueOf(fRootFolder), options.valueOf(fCount), options.valueOf(fCleanup));
        } else {
            createSingleDocumentMT(noThreads, options.valueOf(fRepoId), options.valueOf(fDocType),
                    options.valueOf(fContentSize), options.valueOf(fRootFolder), options.valueOf(fCount),
                    options.valueOf(fCleanup));
        }
    }

    private void fillRepository(OptionSet options) {
        System.out.println();
        printParameters(options);
        System.out.println("Creating object tree with folowing parameters: ");
        System.out.println("Documents per folder: " + options.valueOf(fDocsPerFolder));
        System.out.println("Folder per folder: " + options.valueOf(fFolderPerFolder));
        System.out.println("Depth: " + options.valueOf(fDepth));
        System.out.println("Folder Type: " + options.valueOf(fFolderType));

        int noThreads = options.valueOf(fThreads);
        if (noThreads <= 1) {
            fillRepository(options.valueOf(fRepoId), options.valueOf(fDocsPerFolder),
                    options.valueOf(fFolderPerFolder), options.valueOf(fDepth), options.valueOf(fDocType),
                    options.valueOf(fFolderType), options.valueOf(fContentSize), options.valueOf(fRootFolder),
                    options.valueOf(fCleanup));
        } else {
            fillRepositoryMT(noThreads, options.valueOf(fRepoId), options.valueOf(fDocsPerFolder),
                    options.valueOf(fFolderPerFolder), options.valueOf(fDepth), options.valueOf(fDocType),
                    options.valueOf(fFolderType), options.valueOf(fContentSize), options.valueOf(fRootFolder),
                    options.valueOf(fCleanup));
        }

    }

    private void createFolders(OptionSet options) {
        System.out.println();
        System.out.println("Creating folder with parameters:");
        printParameters(options);
        System.out.println("Folder Type: " + options.valueOf(fFolderType));
        int noThreads = options.valueOf(fThreads);
        if (noThreads <= 1) {
            createFolders(options.valueOf(fRepoId), options.valueOf(fFolderType), options.valueOf(fRootFolder),
                    options.valueOf(fCount), options.valueOf(fCleanup));
        } else {
            createFoldersMT(noThreads, options.valueOf(fRepoId), options.valueOf(fFolderType),
                    options.valueOf(fRootFolder), options.valueOf(fCount), options.valueOf(fCleanup));
        }
    }

    private void createSingleDocument(String repoId, String documentType, int contentSizeInKB, String rootFolderId,
            int docCount, boolean doCleanup) {

        MultiThreadedObjectGenerator.ObjectGeneratorRunner runner = MultiThreadedObjectGenerator
                .prepareForCreateDocument(binding, repoId, documentType, contentSizeInKB, rootFolderId, docCount,
                        fContentKind, doCleanup);
        ObjectGenerator gen = runner.getObjectGenerator();
        String[] ids = runner.doCreateDocument();
        System.out.println();
        System.out.println("Result:");
        System.out.println("Document creation succeeded.");
        System.out.println("Folder used as root for creation: " + rootFolderId);
        System.out.println("Ids of created documents: ");
        if (null == ids) {
            System.out.println("<none>");
        } else {
            for (int i = 0; i < ids.length; i++) {
                System.out.println(ids[i]);
            }
        }
        gen.printTimings();
        gen.resetCounters();
    }

    private void createSingleDocumentMT(int noThreads, String repoId, String documentType, int contentSizeInKB,
            String rootFolderId, int docCount, boolean doCleanup) {

        MultiThreadedObjectGenerator.ObjectGeneratorRunner[] runners = MultiThreadedObjectGenerator
                .prepareForCreateDocumentMT(noThreads, binding, repoId, documentType, contentSizeInKB, rootFolderId,
                        docCount, fContentKind, doCleanup);

        MultiThreadedObjectGenerator.runMultiThreaded(runners);
        System.out.println("Document creation succeeded. All threads terminated.");
    }

    private void createFolders(String repoId, String folderType, String rootFolderId, int noFolders, boolean doCleanup) {

        MultiThreadedObjectGenerator.ObjectGeneratorRunner runner = MultiThreadedObjectGenerator
                .prepareForCreateFolder(binding, repoId, folderType, rootFolderId, noFolders, doCleanup);
        ObjectGenerator gen = runner.getObjectGenerator();
        String[] ids = runner.doCreateFolder();
        System.out.println();
        System.out.println("Result:");
        System.out.println("Folder creation succeeded.");
        System.out.println("Ids of created folders: ");
        if (null == ids) {
            System.out.println("<none>");
        } else {
            for (int i = 0; i < ids.length; i++) {
                System.out.println(ids[i]);
            }
        }
        gen.printTimings();
        gen.resetCounters();
    }

    private void createFoldersMT(int noThreads, String repoId, String folderType, String rootFolderId, int noFolders,
            boolean doCleanup) {

        MultiThreadedObjectGenerator.ObjectGeneratorRunner[] runners = MultiThreadedObjectGenerator
                .prepareForCreateFolderMT(noThreads, binding, repoId, folderType, rootFolderId, noFolders, doCleanup);
        MultiThreadedObjectGenerator.runMultiThreaded(runners);
        System.out.println("Folder creation succeeded.");
    }

    private void callRepoInfo(String repositoryId, int count) {
        RepositoryService repSvc = binding.getRepositoryService();
        TimeLogger timeLogger = new TimeLogger("RepoInfoTest");
        RepositoryInfo repoInfo = null;
        for (int i = 0; i < count; i++) {
            binding.clearRepositoryCache(repositoryId);
            timeLogger.start();
            repoInfo = repSvc.getRepositoryInfo(repositoryId, null);
            timeLogger.stop();
        }
        System.out.println("Root Folder id is: " + (repoInfo == null ? "<unknown>" : repoInfo.getRootFolderId()));
        timeLogger.printTimes();
    }

    private void repositoryInfo(OptionSet options) {
        callRepoInfo(options.valueOf(fRepoId), options.valueOf(fCount));
    }

    private void createFiles(OptionSet options) {
        ContentStream contentStream = null;
        String fileNamePattern = options.valueOf(fFileNamePattern);
        int count = options.valueOf(fCount);
        int contentSize = options.valueOf(fContentSize);

        System.out.println("Creating local files with content: ");
        System.out.println("Kind: " + options.valueOf(fDocsPerFolder));
        System.out.println("Number of files: " + count);
        System.out.println("File name pattern: " + fileNamePattern);
        System.out.println("Kind of content: " + options.valueOf(fContentKindStr));
        System.out.println("Size of content (text only): " + contentSize);

        ObjectGenerator objGen = new ObjectGenerator(null, null, null, null, null, fContentKind);
        objGen.setContentSizeInKB(contentSize);

        InputStream is = null;
        FileOutputStream os = null;

        try {
            for (int i = 0; i < count; i++) {
                String fileName = String.format(fileNamePattern, i);
                System.out.println("Generating file: " + fileName);
                if (contentSize > 0) {
                    switch (fContentKind) {
                    case STATIC_TEXT:
                        contentStream = objGen.createContentStaticText();
                        break;
                    case LOREM_IPSUM_TEXT:
                        contentStream = objGen.createContentLoremIpsumText();
                        break;
                    case LOREM_IPSUM_HTML:
                        contentStream = objGen.createContentLoremIpsumHtml();
                        break;
                    case IMAGE_FRACTAL_JPEG:
                        contentStream = objGen.createContentFractalimageJpeg();
                        break;
                    }
                }

                // write to a file:
                is = contentStream.getStream();
                os = new FileOutputStream(fileName);

                IOUtils.copy(is, os, BUFSIZE);

                is.close();
                is = null;
                os.close();
                os = null;
            }
        } catch (Exception e) {
            System.err.println("Error generating file: " + e);
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    private void transferFiles(OptionSet options) {
        String fileName = options.valueOf(fLocalFile);
        String dirName = options.valueOf(fLocalDir);
        String repoId = options.valueOf(fRepoId);
        String folderId = options.valueOf(fRootFolder);
        String name = fileName;

        if ((null == fileName || fileName.length() == 0) && (null == dirName || dirName.length() == 0)) {
            System.out.println("Error: You either have to provide a --file or a --dir option to copy file(s).");
            return;
        }

        // if no file name is provided there must be a directory
        if (null == name || name.length() == 0) {
            name = dirName;
        }

        if (null == repoId || repoId.length() == 0) {
            System.out.println("Error: You have to provide a repository id");
            return;
        }
        System.out.println("Copying files to a repository: ");
        System.out.println("Repository id is: " + repoId);
        System.out.println("Folder id used as root: " + options.valueOf(fRootFolder));

        Map<String, String> parameters = getConnectionParameters(getBindingProperty(), repoId);
        FileCopier fc = new FileCopier();
        fc.connect(parameters);
        fc.copyRecursive(name, folderId);
    }

    private void transferFilesTest(OptionSet options) {
        String fileName = options.valueOf(fLocalFile);

        if ((null == fileName || fileName.length() == 0)) {
            System.out.println("Error: You have to provide a --file option to test metadata extraction.");
            return;
        }

        System.out.println("Testing metadata extraction: ");

        FileCopier fc = new FileCopier();
        fc.listMetadata(fileName);
    }

    private Map<String, String> getConnectionParameters(String binding, String repoId) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(SessionParameter.REPOSITORY_ID, repoId);
        parameters.put(SessionParameter.BINDING_TYPE, binding);

        if (binding.equals(BindingType.ATOMPUB.value())) {
            parameters.put(SessionParameter.ATOMPUB_URL, getAtomPubUrl());
            filLoginParams(parameters, getUser(), getPassword());
        } else if (binding.equals(BindingType.WEBSERVICES.value())) {
            fillWSParameters(parameters, getWsUrl(), isPrefix(getWsUrl()), getUser(), getPassword());
        } else if (binding.equals(BindingType.BROWSER.value())) {
            parameters.put(SessionParameter.BROWSER_URL, getBrowserUrl());
            filLoginParams(parameters, getUser(), getPassword());
        } else {
            System.out.println("Error unknown binding: " + binding);
        }
        fillCustomHeaders(parameters);

        return parameters;
    }

    private void initClientBindings() {
        if (binding == null) {
            if (fBindingType == BindingType.ATOMPUB) {
                binding = createAtomBinding(getAtomPubUrl(), getUser(), getPassword());
            }
            if (fBindingType == BindingType.WEBSERVICES) {
                String url = getWsUrl();
                binding = createWSBinding(url, isPrefix(url), getUser(), getPassword());
            } else if (fBindingType == BindingType.BROWSER) {
                binding = createBrowserBinding(getBrowserUrl(), getUser(), getPassword());
            }
        }
    }

    private static void filLoginParams(Map<String, String> parameters, String user, String password) {
        if (user != null && user.length() > 0) {
            parameters.put(SessionParameter.USER, user);
        }
        if (user != null && user.length() > 0) {
            parameters.put(SessionParameter.PASSWORD, password);
        }
    }

    private static void fillCustomHeaders(Map<String, String> parameters) {
        Map<String, String> customHeaders = getCustomHeaders();
        for (Map.Entry<String, String> entry : customHeaders.entrySet()) {
            parameters.put(entry.getKey(), entry.getValue());
        }
    }

    private static CmisBinding createAtomBinding(String url, String user, String password) {

        // gather parameters
        Map<String, String> parameters = new HashMap<String, String>();
        filLoginParams(parameters, user, password);
        fillCustomHeaders(parameters);

        // get factory and create binding
        CmisBindingFactory factory = CmisBindingFactory.newInstance();
        parameters.put(SessionParameter.ATOMPUB_URL, url);
        CmisBinding binding = factory.createCmisAtomPubBinding(parameters);
        return binding;
    }

    private static CmisBinding createBrowserBinding(String url, String user, String password) {

        // gather parameters
        Map<String, String> parameters = new HashMap<String, String>();
        filLoginParams(parameters, user, password);
        fillCustomHeaders(parameters);

        // get factory and create binding
        CmisBindingFactory factory = CmisBindingFactory.newInstance();
        parameters.put(SessionParameter.BROWSER_URL, url);
        CmisBinding binding = factory.createCmisBrowserBinding(parameters);
        return binding;
    }

    private static boolean isPrefix(String url) {
        boolean isPrefix = true;
        String urlLower = url.toLowerCase();

        if (urlLower.endsWith("?wsdl")) {
            isPrefix = false;
        } else if (urlLower.endsWith(".wsdl")) {
            isPrefix = false;
        } else if (urlLower.endsWith(".xml")) {
            isPrefix = false;
        }
        return isPrefix;
    }

    public static CmisBinding createWSBinding(String url, boolean isPrefix, String username, String password) {
        Map<String, String> parameters = new HashMap<String, String>();
        fillWSParameters(parameters, url, isPrefix, username, password);
        fillCustomHeaders(parameters);

        // get factory and create provider
        CmisBindingFactory factory = CmisBindingFactory.newInstance();
        CmisBinding binding = factory.createCmisWebServicesBinding(parameters);

        return binding;
    }

    public static void fillWSParameters(Map<String, String> parameters, String url, boolean isPrefix, String username,
            String password) {
        // gather parameters
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
        } else {
            parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, url + "RepositoryService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, url + "NavigationService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, url + "ObjectService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, url + "VersioningService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, url + "DiscoveryService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, url + "RelationshipService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, url + "MultiFilingService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, url + "PolicyService?wsdl");
            parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, url + "ACLService?wsdl");
        }
    }

    private static String getBindingProperty() {
        return System.getProperty(PROP_BINDING, DEFAULT_BINDING);
    }

    private static String getAtomPubUrl() {
        return System.getProperty(PROP_ATOMPUB_URL, DEFAULT_ATOMPUB_URL);
    }

    private static String getWsUrl() {
        return System.getProperty(PROP_WS_URL, DEFAULT_WS_URL);
    }

    private static String getBrowserUrl() {
        return System.getProperty(PROP_BROWSER_URL, DEFAULT_BROWSER_BINDING_URL);
    }

    private static String getUser() {
        return System.getProperty(PROP_USER, DEFAULT_USER);
    }

    private static String getPassword() {
        return System.getProperty(PROP_PASSWORD, DEFAULT_PASSWORD);
    }

    private static Map<String, String> getCustomHeaders() {
        int i = 0;
        Map<String, String> customHeaders = new HashMap<String, String>();
        while (true) {
            String val = System.getProperty(PROP_CUSTOM + i, null);
            if (null == val) {
                break;
            } else {
                customHeaders.put(PROP_CUSTOM + i++, val);
            }
        }
        return customHeaders;
    }

}
