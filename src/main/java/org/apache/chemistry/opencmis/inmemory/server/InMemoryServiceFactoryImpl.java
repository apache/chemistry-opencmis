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
package org.apache.chemistry.opencmis.inmemory.server;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.Converter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeDefinitionType;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.inmemory.ConfigConstants;
import org.apache.chemistry.opencmis.inmemory.ConfigurationSettings;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.TypeManagerCreatable;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.StoreManagerFactory;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.StoreManagerImpl;
import org.apache.chemistry.opencmis.inmemory.types.InMemoryJaxbHelper;
import org.apache.chemistry.opencmis.inmemory.types.TypeDefinitions;
import org.apache.chemistry.opencmis.server.support.CmisServiceWrapper;
import org.apache.chemistry.opencmis.server.support.TypeManager;
import org.apache.chemistry.opencmis.util.repository.ObjectGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryServiceFactoryImpl extends AbstractServiceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryServiceFactoryImpl.class.getName());
    private static final BigInteger DEFAULT_MAX_ITEMS_OBJECTS = BigInteger.valueOf(1000);
    private static final BigInteger DEFAULT_MAX_ITEMS_TYPES = BigInteger.valueOf(100);
    private static final BigInteger DEFAULT_DEPTH_OBJECTS = BigInteger.valueOf(2);
    private static final BigInteger DEFAULT_DEPTH_TYPES = BigInteger.valueOf(-1);
    private static CallContext OVERRIDE_CTX;

    private Map<String, String> inMemoryServiceParameters;
    private ThreadLocal<CmisServiceWrapper<InMemoryService>> threadLocalService = new ThreadLocal<CmisServiceWrapper<InMemoryService>>();
    private boolean fUseOverrideCtx = false;
    private StoreManager storeManager; // singleton root of everything
    private CleanManager cleanManager = null;

    private File tempDir;
    private int memoryThreshold;
    private long maxContentSize;

    @Override
    public void init(Map<String, String> parameters) {
        LOG.info("Initializing in-memory repository...");

        System.out.println(parameters);

        inMemoryServiceParameters = parameters;
        String overrideCtx = parameters.get(ConfigConstants.OVERRIDE_CALL_CONTEXT);
        if (null != overrideCtx) {
            fUseOverrideCtx = true;
        }

        ConfigurationSettings.init(parameters);

        String repositoryClassName = (String) parameters.get(ConfigConstants.REPOSITORY_CLASS);
        if (null == repositoryClassName) {
            repositoryClassName = StoreManagerImpl.class.getName();
        }

        if (null == storeManager) {
            storeManager = StoreManagerFactory.createInstance(repositoryClassName);
        }

        String tempDirStr = parameters.get(ConfigConstants.TEMP_DIR);
        tempDir = (tempDirStr == null ? super.getTempDirectory() : new File(tempDirStr));

        String memoryThresholdStr = parameters.get(ConfigConstants.MEMORY_THRESHOLD);
        memoryThreshold = (memoryThresholdStr == null ? super.getMemoryThreshold() : Integer
                .parseInt(memoryThresholdStr));

        String maxContentSizeStr = parameters.get(ConfigConstants.MAX_CONTENT_SIZE);
        maxContentSize = (maxContentSizeStr == null ? super.getMaxContentSize() : Long.parseLong(maxContentSizeStr));

        Date deploymentTime = new Date();
        String strDate = new SimpleDateFormat("EEE MMM dd hh:mm:ss a z yyyy", Locale.US).format(deploymentTime);

        parameters.put(ConfigConstants.DEPLOYMENT_TIME, strDate);

        initStorageManager(parameters);

        fillRepositoryIfConfigured(parameters);

        Long cleanInterval = ConfigurationSettings
                .getConfigurationValueAsLong(ConfigConstants.CLEAN_REPOSITORY_INTERVAL);
        if (null != cleanInterval && cleanInterval > 0) {
            scheduleCleanRepositoryJob(cleanInterval);
        }

        LOG.info("...initialized in-memory repository.");
    }

    public static void setOverrideCallContext(CallContext ctx) {
        OVERRIDE_CTX = ctx;
    }

    @Override
    public CmisService getService(CallContext context) {
        LOG.debug("start getService()");

        // Attach the CallContext to a thread local context that can be
        // accessed from everywhere
        // Some unit tests set their own context. So if we find one then we use
        // this one and ignore the provided one. Otherwise we set a new context.
        if (fUseOverrideCtx && null != OVERRIDE_CTX) {
            context = OVERRIDE_CTX;
        }

        CmisServiceWrapper<InMemoryService> wrapperService = threadLocalService.get();
        if (wrapperService == null) {
            wrapperService = new CmisServiceWrapper<InMemoryService>(new InMemoryService(inMemoryServiceParameters,
                    storeManager), DEFAULT_MAX_ITEMS_TYPES, DEFAULT_DEPTH_TYPES, DEFAULT_MAX_ITEMS_OBJECTS,
                    DEFAULT_DEPTH_OBJECTS);
            threadLocalService.set(wrapperService);
        }

        wrapperService.getWrappedService().setCallContext(context);

        LOG.debug("stop getService()");
        return wrapperService.getWrappedService(); // wrapperService;
    }

    @Override
    public File getTempDirectory() {
        return tempDir;
    }

    @Override
    public int getMemoryThreshold() {
        return memoryThreshold;
    }

    @Override
    public long getMaxContentSize() {
        return maxContentSize;
    }

    @Override
    public void destroy() {
        if (null != cleanManager) {
            cleanManager.stopCleanRepositoryJob();
        }
        threadLocalService = null;
    }

    public StoreManager getStoreManger() {
        return storeManager;
    }

    private void initStorageManager(Map<String, String> parameters) {
        // initialize in-memory management
        String repositoryClassName = (String) parameters.get(ConfigConstants.REPOSITORY_CLASS);
        if (null == repositoryClassName) {
            repositoryClassName = StoreManagerImpl.class.getName();
        }

        if (null == storeManager) {
            storeManager = StoreManagerFactory.createInstance(repositoryClassName);
        }

        String repositoryId = parameters.get(ConfigConstants.REPOSITORY_ID);

        List<String> allAvailableRepositories = storeManager.getAllRepositoryIds();

        // init existing repositories
        for (String existingRepId : allAvailableRepositories) {
            storeManager.initRepository(existingRepId);
        }

        // create repository if configured as a startup parameter
        if (null != repositoryId) {
            if (allAvailableRepositories.contains(repositoryId)) {
                LOG.warn("Repostory " + repositoryId + " already exists and will not be created.");
            } else {
                String typeCreatorClassName = parameters.get(ConfigConstants.TYPE_CREATOR_CLASS);
                storeManager.createAndInitRepository(repositoryId, typeCreatorClassName);
            }
        }

        // check if a type definitions XML file is configured. if yes import
        // type definitions
        String typeDefsFileName = parameters.get(ConfigConstants.TYPE_XML);
        if (null == typeDefsFileName)
            LOG.info("No file name for type definitions given, no types will be created.");
        else {
            TypeManager typeManager = storeManager.getTypeManager(repositoryId);
            if (typeManager instanceof TypeManagerCreatable) {
                TypeManagerCreatable tmc = (TypeManagerCreatable) typeManager;
                importTypesFromFile(tmc, typeDefsFileName);
            } else {
                LOG.warn("Type Definitions are configured in XML file but type manager cannot create types. Type definitions are ignored.");
            }
        }

    }

    private void importTypesFromFile(TypeManagerCreatable tmc, String typeDefsFileName) {

        InputStream is = this.getClass().getResourceAsStream("/" + typeDefsFileName);

        if (null == is) {
            LOG.warn("Resource file with type definitions " + typeDefsFileName
                    + " could not be found, no types will be created.");
            return;
        }

        try {
            TypeDefinition typeDef = null;
            Unmarshaller u = InMemoryJaxbHelper.createUnmarshaller();
            JAXBElement<TypeDefinitions> types = (JAXBElement<TypeDefinitions>) u.unmarshal(is);
            for (CmisTypeDefinitionType td : types.getValue().getTypeDefinitions()) {
                LOG.debug("Found type in file: " + td.getLocalName());
                typeDef = Converter.convert(td);
                if (typeDef.getPropertyDefinitions() == null) {
                    ((AbstractTypeDefinition) typeDef)
                            .setPropertyDefinitions(new LinkedHashMap<String, PropertyDefinition<?>>());
                }
                tmc.addTypeDefinition(typeDef);
            }
        } catch (Exception e) {
            LOG.error("Could not load type definitions from file '" + typeDefsFileName + "': " + e);
        }
    }

    private static List<String> readPropertiesToSetFromConfig(Map<String, String> parameters, String keyPrefix) {
        List<String> propsToSet = new ArrayList<String>();
        for (int i = 0;; ++i) {
            String propertyKey = keyPrefix + Integer.toString(i);
            String propertyToAdd = parameters.get(propertyKey);
            if (null == propertyToAdd) {
                break;
            } else {
                propsToSet.add(propertyToAdd);
            }
        }
        return propsToSet;
    }

    private void fillRepositoryIfConfigured(Map<String, String> parameters) {

        class DummyCallContext implements CallContext {

            public String get(String key) {
                return null;
            }

            public String getBinding() {
                return null;
            }

            public boolean isObjectInfoRequired() {
                return false;
            }

            public String getRepositoryId() {
                return null;
            }

            public String getLocale() {
                return null;
            }

            public BigInteger getOffset() {
                return null;
            }

            public BigInteger getLength() {
                return null;
            }

            public String getPassword() {
                return null;
            }

            public String getUsername() {
                return null;
            }

            public File getTempDirectory() {

                return tempDir;
            }

            public int getMemoryThreshold() {
                return memoryThreshold;
            }

            public long getMaxContentSize() {
                return maxContentSize;
            }
        }

        // List<String> allAvailableRepositories =
        // storeManager.getAllRepositoryIds();
        String repositoryId = parameters.get(ConfigConstants.REPOSITORY_ID);
        String doFillRepositoryStr = parameters.get(ConfigConstants.USE_REPOSITORY_FILER);
        String contentKindStr = parameters.get(ConfigConstants.CONTENT_KIND);
        boolean doFillRepository = doFillRepositoryStr == null ? false : Boolean.parseBoolean(doFillRepositoryStr);

        if (doFillRepository /*
                              * &&
                              * !allAvailableRepositories.contains(repositoryId)
                              */) {

            // create an initial temporary service instance to fill the
            // repository

            InMemoryService svc = new InMemoryService(inMemoryServiceParameters, storeManager);

            BindingsObjectFactory objectFactory = new BindingsObjectFactoryImpl();

            String levelsStr = parameters.get(ConfigConstants.FILLER_DEPTH);
            int levels = 1;
            if (null != levelsStr) {
                levels = Integer.parseInt(levelsStr);
            }

            String docsPerLevelStr = parameters.get(ConfigConstants.FILLER_DOCS_PER_FOLDER);
            int docsPerLevel = 1;
            if (null != docsPerLevelStr) {
                docsPerLevel = Integer.parseInt(docsPerLevelStr);
            }

            String childrenPerLevelStr = parameters.get(ConfigConstants.FILLER_FOLDERS_PER_FOLDER);
            int childrenPerLevel = 2;
            if (null != childrenPerLevelStr) {
                childrenPerLevel = Integer.parseInt(childrenPerLevelStr);
            }

            String documentTypeId = parameters.get(ConfigConstants.FILLER_DOCUMENT_TYPE_ID);
            if (null == documentTypeId) {
                documentTypeId = BaseTypeId.CMIS_DOCUMENT.value();
            }

            String folderTypeId = parameters.get(ConfigConstants.FILLER_FOLDER_TYPE_ID);
            if (null == folderTypeId) {
                folderTypeId = BaseTypeId.CMIS_FOLDER.value();
            }

            int contentSizeKB = 0;
            String contentSizeKBStr = parameters.get(ConfigConstants.FILLER_CONTENT_SIZE);
            if (null != contentSizeKBStr) {
                contentSizeKB = Integer.parseInt(contentSizeKBStr);
            }

            ObjectGenerator.CONTENT_KIND contentKind;
            if (null == contentKindStr)
                contentKind = ObjectGenerator.CONTENT_KIND.LoremIpsumText;
            else {
                if (contentKindStr.equals("static/text"))
                    contentKind = ObjectGenerator.CONTENT_KIND.StaticText;
                else if (contentKindStr.equals("lorem/text"))
                    contentKind = ObjectGenerator.CONTENT_KIND.LoremIpsumText;
                else if (contentKindStr.equals("lorem/html"))
                    contentKind = ObjectGenerator.CONTENT_KIND.LoremIpsumHtml;
                else if (contentKindStr.equals("fractal/jpeg"))
                    contentKind = ObjectGenerator.CONTENT_KIND.ImageFractalJpeg;
                else
                    contentKind = ObjectGenerator.CONTENT_KIND.StaticText;
            }
            // Create a hierarchy of folders and fill it with some documents
            ObjectGenerator gen = new ObjectGenerator(objectFactory, svc, svc, svc, repositoryId, contentKind);

            gen.setNumberOfDocumentsToCreatePerFolder(docsPerLevel);

            // Set the type id for all created documents:
            gen.setDocumentTypeId(documentTypeId);

            // Set the type id for all created folders:
            gen.setFolderTypeId(folderTypeId);

            // Set contentSize
            gen.setContentSizeInKB(contentSizeKB);

            // set properties that need to be filled
            // set the properties the generator should fill with values for
            // documents:
            // Note: must be valid properties in configured document and folder
            // type

            List<String> propsToSet = readPropertiesToSetFromConfig(parameters,
                    ConfigConstants.FILLER_DOCUMENT_PROPERTY);
            if (null != propsToSet) {
                gen.setDocumentPropertiesToGenerate(propsToSet);
            }

            propsToSet = readPropertiesToSetFromConfig(parameters, ConfigConstants.FILLER_FOLDER_PROPERTY);
            if (null != propsToSet) {
                gen.setFolderPropertiesToGenerate(propsToSet);
            }

            // Simulate a runtime context with configuration parameters
            // Attach the CallContext to a thread local context that can be
            // accessed
            // from everywhere
            DummyCallContext ctx = new DummyCallContext();
            svc.setCallContext(ctx);

            // Build the tree
            RepositoryInfo rep = svc.getRepositoryInfo(repositoryId, null);
            String rootFolderId = rep.getRootFolderId();

            try {
                gen.createFolderHierachy(levels, childrenPerLevel, rootFolderId);
                // Dump the tree
                gen.dumpFolder(rootFolderId, "*");
            } catch (Exception e) {
                LOG.error("Could not create folder hierarchy with documents. " + e);
                e.printStackTrace();
            }
        } // if

    } // fillRepositoryIfConfigured

    class CleanManager {

        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> cleanerHandle = null;

        public void startCleanRepositoryJob(long intervalInMinutes) {

            final Runnable cleaner = new Runnable() {
                public void run() {
                    LOG.info("Cleaning repository as part of a scheduled maintenance job.");
                    for (String repositoryId : storeManager.getAllRepositoryIds()) {
                        ObjectStore store = storeManager.getObjectStore(repositoryId);
                        store.clear();
                        fillRepositoryIfConfigured(ConfigurationSettings.getParameters());
                    }
                    LOG.info("Repository cleaned. Freeing memory.");
                    System.gc();
                }
            };

            LOG.info("Repository Clean Job starting clean job, interval " + intervalInMinutes + " min");
            cleanerHandle = scheduler.scheduleAtFixedRate(cleaner, intervalInMinutes, intervalInMinutes,
                    TimeUnit.MINUTES);
        }

        public void stopCleanRepositoryJob() {
            LOG.info("Repository Clean Job cancelling clean job.");
            boolean ok = cleanerHandle.cancel(true);
            LOG.info("Repository Clean Job cancelled with result: " + ok);
            scheduler.shutdownNow();
        }
    }

    private void scheduleCleanRepositoryJob(long minutes) {
        cleanManager = new CleanManager();
        cleanManager.startCleanRepositoryJob(minutes);
    }

}
