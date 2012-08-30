/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.chemistry.opencmis.jcr;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.spi.ServiceRegistry;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;

import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.jcr.impl.DefaultDocumentTypeHandler;
import org.apache.chemistry.opencmis.jcr.impl.DefaultFolderTypeHandler;
import org.apache.chemistry.opencmis.jcr.impl.DefaultUnversionedDocumentTypeHandler;
import org.apache.chemistry.opencmis.jcr.type.JcrTypeHandlerManager;
import org.apache.chemistry.opencmis.server.support.CmisServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link CmisServiceFactory} implementation which returns {@link JcrService} instances.  
 */
public class JcrServiceFactory extends AbstractServiceFactory {
    private static final Logger log = LoggerFactory.getLogger(JcrServiceFactory.class);

    public static final String MOUNT_PATH_CONFIG = "mount-path";
    public static final String PREFIX_JCR_CONFIG = "jcr.";

    public static final BigInteger DEFAULT_MAX_ITEMS_TYPES = BigInteger.valueOf(50);
    public static final BigInteger DEFAULT_DEPTH_TYPES = BigInteger.valueOf(-1);
    public static final BigInteger DEFAULT_MAX_ITEMS_OBJECTS = BigInteger.valueOf(200);
    public static final BigInteger DEFAULT_DEPTH_OBJECTS = BigInteger.valueOf(10);

    protected JcrTypeManager typeManager;
    protected Map<String, String> jcrConfig;
    protected String mountPath;
    protected JcrRepository jcrRepository;

    @Override
    public void init(Map<String, String> parameters) {
        typeManager = createTypeManager();
        readConfiguration(parameters);
        PathManager pathManger = new PathManager(mountPath);
        JcrTypeHandlerManager typeHandlerManager = createTypeHandlerManager(pathManger, typeManager);
        jcrRepository = new JcrRepository(acquireJcrRepository(jcrConfig), pathManger, typeManager, typeHandlerManager);
    }

    @Override
    public void destroy() {
        jcrRepository = null;
        typeManager = null;
    }

    @Override
    public CmisService getService(CallContext context) {
        CmisServiceWrapper<JcrService> serviceWrapper = new CmisServiceWrapper<JcrService>(
                createJcrService(jcrRepository, context), DEFAULT_MAX_ITEMS_TYPES, DEFAULT_DEPTH_TYPES,
                DEFAULT_MAX_ITEMS_OBJECTS, DEFAULT_DEPTH_OBJECTS);

        serviceWrapper.getWrappedService().setCallContext(context);
        return serviceWrapper;
    }

    //------------------------------------------< factories >---

    /**
     * Acquire the JCR repository given a configuration. This implementation used
     * {@link javax.imageio.spi.ServiceRegistry#lookupProviders(Class)} for
     * locating <code>RepositoryFactory</code> instances. The first instance
     * which can handle the <code>jcrConfig</code> parameters is used to
     * acquire the repository. 
     *
     * @param jcrConfig  configuration determining the JCR repository to be returned
     * @return
     * @throws RepositoryException
     */
    protected Repository acquireJcrRepository(Map<String, String> jcrConfig) {
        try {
            Iterator<RepositoryFactory> factories = ServiceRegistry.lookupProviders(RepositoryFactory.class);
            while (factories.hasNext()) {
                RepositoryFactory factory = factories.next();
                log.debug("Trying to acquire JCR repository from factory " + factory);
                Repository repository = factory.getRepository(jcrConfig);
                if (repository != null) {
                    log.debug("Successfully acquired JCR repository from factory " + factory);
                    return repository;
                }
                else {
                    log.debug("Could not acquire JCR repository from factory " + factory);
                }
            }
            throw new CmisConnectionException("No JCR repository factory for configured parameters");
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisConnectionException(e.getMessage(), e);
        }
    }

    /**
     * Create a <code>JcrService</code> from a <code>JcrRepository</code>JcrRepository> and
     * <code>CallContext</code>.
     * 
     * @param jcrRepository
     * @param context
     * @return
     */
    protected JcrService createJcrService(JcrRepository jcrRepository, CallContext context) {
        return new JcrService(jcrRepository);
    }

    protected JcrTypeManager createTypeManager() {
        return new JcrTypeManager();
    }

    protected JcrTypeHandlerManager createTypeHandlerManager(PathManager pathManager, JcrTypeManager typeManager) {
        JcrTypeHandlerManager typeHandlerManager = new JcrTypeHandlerManager(pathManager, typeManager);
        typeHandlerManager.addHandler(new DefaultFolderTypeHandler());
        typeHandlerManager.addHandler(new DefaultDocumentTypeHandler());
        typeHandlerManager.addHandler(new DefaultUnversionedDocumentTypeHandler());
        return typeHandlerManager;
    }

    //------------------------------------------< private >---

    private void readConfiguration(Map<String, String> parameters) {
        Map<String, String> map = new HashMap<String, String>();
        List<String> keys = new ArrayList<String>(parameters.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            if (key.startsWith(PREFIX_JCR_CONFIG)) {
                String jcrKey = key.substring(PREFIX_JCR_CONFIG.length());
                String jcrValue = replaceSystemProperties(parameters.get(key));
                map.put(jcrKey, jcrValue);
            }

            else if (MOUNT_PATH_CONFIG.equals(key)) {
                mountPath = parameters.get(key);
                log.debug("Configuration: " + MOUNT_PATH_CONFIG + '=' + mountPath);
            }

            else {
                log.warn("Configuration: unrecognized key: " + key);
            }
        }

        jcrConfig = Collections.unmodifiableMap(map);
        log.debug("Configuration: jcr=" + jcrConfig);
    }

    private static String replaceSystemProperties(String s) {
        if (s == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        StringBuilder property = null;
        boolean inProperty = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (inProperty) {
                if (c == '}') {
                    String value = System.getProperty(property.toString());
                    if (value != null) {
                        result.append(value);
                    }
                    inProperty = false;
                } else {
                    property.append(c);
                }
            } else {
                if (c == '{') {
                    property = new StringBuilder();
                    inProperty = true;
                } else {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }

    public JcrTypeManager getTypeManager() {
        return typeManager;
    }


    public JcrRepository getJcrRepository() {
        return jcrRepository;
    }

}
