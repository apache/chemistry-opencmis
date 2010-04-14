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

import org.apache.opencmis.commons.provider.CmisProvider;
import org.apache.opencmis.commons.provider.NavigationService;
import org.apache.opencmis.commons.provider.ObjectService;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.commons.provider.RepositoryService;

public class MultiThreadedObjectGenerator {
  
  static public enum Action { CreateDocument, CreateTree, CreateFolders };

  static class ObjectGeneratorRunner implements Runnable {
    private Action fAction;
    ObjectGenerator fObjGen;
    private String fRootFolderId;
    private int fFoldersPerFolders;
    private int fDepth;
    private int fCount;
    
    public ObjectGeneratorRunner(ObjectGenerator objGen, Action action) {
      fObjGen = objGen;
      fAction = action;
    }
    
    public void run() {
      if (fAction == Action.CreateDocument) {
        doCreateDocument();
      }
      else if (fAction == Action.CreateTree) {
        doCreateTree();
      } else if (fAction == Action.CreateFolders) {
        doCreateFolder();
      }
    }
    
    public String[] doCreateDocument() {
      String ids[] = fObjGen.createDocuments(fRootFolderId, fCount);
      return ids;
    }
    
    public void doCreateTree() {
      fObjGen.createFolderHierachy(fDepth, fFoldersPerFolders, fRootFolderId);            
    }

    public String[] doCreateFolder() {
      return fObjGen.createFolders(fRootFolderId, fCount);
    }

    public ObjectGenerator getObjectGenerator() {
      return fObjGen;      
    }
    
  }  // ObjectCreatorRunner
  
  static private ObjectGenerator createObjectGenerator(CmisProvider provider, String repoId,
      int docsPerFolder, int foldersPerFolders, int depth, String documentType, String folderType,
      int contentSizeInKB, String rootFolderId, boolean doCleanup) {

    ProviderObjectFactory objectFactory = provider.getObjectFactory();
    NavigationService navSvc = provider.getNavigationService();
    ObjectService objSvc = provider.getObjectService();
    
    ObjectGenerator gen = new ObjectGenerator(objectFactory, navSvc, objSvc, repoId);
    gen.setUseUuidsForNames(true);
    gen.setNumberOfDocumentsToCreatePerFolder(docsPerFolder);
    // Set the type id for all created documents:
    gen.setDocumentTypeId(documentType);
    // Set the type id for all created folders:
    gen.setFolderTypeId(folderType);
    // Set contentSize
    gen.setContentSizeInKB(contentSizeInKB);
    gen.setCleanUpAfterCreate(doCleanup);

    return gen;
  }
  
  static private String getRootFolderId(CmisProvider provider, String repositoryId, String rootFolderId) {
    RepositoryService repSvc = provider.getRepositoryService();

    RepositoryInfoData rep = repSvc.getRepositoryInfo(repositoryId, null);
    if (null == rootFolderId || rootFolderId.length() == 0)
      rootFolderId = rep.getRootFolderId();
    
    return rootFolderId;
  }
  
  public static ObjectGeneratorRunner prepareForCreateTree(CmisProvider provider, String repoId,
      int docsPerFolder, int foldersPerFolders, int depth, String documentType, String folderType,
      int contentSizeInKB, String rootFolderId, boolean doCleanup) {
    
    ObjectGenerator objGen = createObjectGenerator(provider, repoId, docsPerFolder,
        foldersPerFolders, depth, documentType, folderType, contentSizeInKB, rootFolderId,
        doCleanup);    

    ObjectGeneratorRunner gen = new ObjectGeneratorRunner(objGen, Action.CreateTree);
    gen.fFoldersPerFolders = foldersPerFolders;
    gen.fDepth = depth;
    gen.fRootFolderId = getRootFolderId(provider, repoId, rootFolderId);
    return gen;
  }
  
  public static ObjectGeneratorRunner[] prepareForCreateTreeMT(CmisProvider provider, String repoId,
      int docsPerFolder, int foldersPerFolders, int depth, String documentType, String folderType,
      int contentSizeInKB, String[] rootFolderIds, boolean doCleanup) {
    
    ObjectGeneratorRunner[] runners = new ObjectGeneratorRunner[rootFolderIds.length];
    for (int i=0; i<rootFolderIds.length; i++) {
      ObjectGenerator objGen = createObjectGenerator(provider, repoId, docsPerFolder,
          foldersPerFolders, depth, documentType, folderType, contentSizeInKB, rootFolderIds[i],
          doCleanup);    

      ObjectGeneratorRunner gen = new ObjectGeneratorRunner(objGen, Action.CreateTree);
      gen.fFoldersPerFolders = foldersPerFolders;
      gen.fDepth = depth;
      gen.fRootFolderId = rootFolderIds[i];
      runners[i] = gen;
    }
    return runners;
  }
  
  public static ObjectGeneratorRunner prepareForCreateDocument(CmisProvider provider, String repoId,
      String documentType, int contentSizeInKB, String rootFolderId, int noDocuments, boolean doCleanup) {
    
    ObjectGenerator objGen = createObjectGenerator(provider, repoId, 0, 0, 0, documentType, null,
        contentSizeInKB, rootFolderId, doCleanup);
    
    ObjectGeneratorRunner gen = new ObjectGeneratorRunner(objGen, Action.CreateDocument);
    gen.fRootFolderId = getRootFolderId(provider, repoId, rootFolderId);
    gen.fCount = noDocuments;
    return gen;
  }

  public static ObjectGeneratorRunner[] prepareForCreateDocumentMT(int threadCount,
      CmisProvider provider, String repoId, String documentType, int contentSizeInKB,
      String rootFolderId, int noDocuments, boolean doCleanup) {
    
    ObjectGeneratorRunner[] runners = new ObjectGeneratorRunner[threadCount];
    for (int i=0; i<threadCount; i++) {
      ObjectGenerator objGen = createObjectGenerator(provider, repoId, 0, 0, 0, documentType, null,
          contentSizeInKB, rootFolderId, doCleanup);
      
      ObjectGeneratorRunner gen = new ObjectGeneratorRunner(objGen, Action.CreateDocument);
      gen.fRootFolderId = getRootFolderId(provider, repoId, rootFolderId);
      gen.fCount = noDocuments;
      runners[i] = gen;
    }
    return runners;
  }

  public static ObjectGeneratorRunner prepareForCreateFolder(CmisProvider provider, String repoId,
      String folderType, String rootFolderId, int noFolders, boolean doCleanup) {
    
    ObjectGenerator objGen = createObjectGenerator(provider, repoId, 0, 0, 0, null, folderType,
        0, rootFolderId, doCleanup);
    
    ObjectGeneratorRunner gen = new ObjectGeneratorRunner(objGen, Action.CreateFolders);
    gen.fRootFolderId = getRootFolderId(provider, repoId, rootFolderId);
    gen.fCount = noFolders;
    return gen;
  }

  public static ObjectGeneratorRunner[] prepareForCreateFolderMT(int threadCount,
      CmisProvider provider, String repoId, String folderType, String rootFolderId, int noFolders,
      boolean doCleanup) {
    
    ObjectGeneratorRunner[] runners = new ObjectGeneratorRunner[threadCount];
    for (int i=0; i<threadCount; i++) {
      ObjectGenerator objGen = createObjectGenerator(provider, repoId, 0, 0, 0, null, folderType,
          0, rootFolderId, doCleanup);
      
      ObjectGeneratorRunner gen = new ObjectGeneratorRunner(objGen, Action.CreateFolders);
      gen.fRootFolderId = getRootFolderId(provider, repoId, rootFolderId);
      gen.fCount = noFolders;
      runners[i] = gen;
    }
    return runners;
  }

  public static void runMultiThreaded(ObjectGeneratorRunner[] runner) {
    int threadCount = runner.length;
    Thread threads[] = new Thread[threadCount];
    for (int i=0; i<threadCount; i++) {
      Thread thread = new Thread(runner[i], "ObjectGeneratorThread-" + i);
      threads[i] = thread;
      thread.start();
    }
    
    try {
      for (Thread thread : threads) {
        thread.join();
      }
      // Print all timings to System.out
      System.out.println();
      System.out.println("Result:");      
      TimeLogger[] loggersCreateDoc = new TimeLogger[threadCount];
      TimeLogger[] loggersCreateFolder = new TimeLogger[threadCount];
      TimeLogger[] loggersDelete = new TimeLogger[threadCount];
      for (int i=0; i<threadCount; i++) {
        loggersCreateDoc[i] = runner[i].fObjGen.getCreateDocumentTimeLogger();
        loggersCreateFolder[i] = runner[i].fObjGen.getCreateFolderTimeLogger();
        loggersDelete[i] = runner[i].fObjGen.getDeleteTimeLogger();
      }
      TimeLogger.printTimes(loggersCreateDoc);
      TimeLogger.printTimes(loggersCreateFolder);
      TimeLogger.printTimes(loggersDelete);
      
    } catch (InterruptedException e) {
      System.out.println("Failed to wait for termination of threads: " + e);
    }
  }

}
