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
package org.apache.chemistry.opencmis.server.support.wrapper;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.ClassLoaderUtil;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a list of CMIS service wrappers.
 */
public class CmisServiceWrapperManager {

    private static final Logger LOG = LoggerFactory.getLogger(CmisServiceWrapperManager.class);

    private static final String PARAMS_SERVICE_WRAPPER_PREFIX = "servicewrapper.";

    private final LinkedList<WrapperDefinition> wrapperDefinitions;

    public CmisServiceWrapperManager() {
        wrapperDefinitions = new LinkedList<WrapperDefinition>();
    }

    /**
     * Adds an outer-most (called first) wrapper.
     * 
     * @param wrapperClass
     *            the wrapper class
     * @param params
     *            wrapper parameters
     */
    public void addOuterWrapper(Class<? extends AbstractCmisServiceWrapper> wrapperClass, Object... params) {
        wrapperDefinitions.addLast(new WrapperDefinition(wrapperClass, params));

        LOG.debug("Added outer service wrapper: {}", wrapperClass.getName());
    }

    /**
     * Adds an inner-most (called last) wrapper.
     * 
     * @param wrapperClass
     *            the wrapper class
     * @param params
     *            wrapper parameters
     */
    public void addInnerWrapper(Class<? extends AbstractCmisServiceWrapper> wrapperClass, Object... params) {
        wrapperDefinitions.addFirst(new WrapperDefinition(wrapperClass, params));

        LOG.debug("Added inner service wrapper: {}", wrapperClass.getName());
    }

    /**
     * Gets wrapper settings from the service factory parameters and adds them
     * to the wrappers.
     * 
     * @param parameters
     *            service factory parameters
     */
    public void addWrappersFromServiceFactoryParameters(Map<String, String> parameters) {
        if (parameters == null) {
            return;
        }

        TreeMap<Integer, WrapperDefinition> wrappers = new TreeMap<Integer, WrapperDefinition>();

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey().trim().toLowerCase(Locale.ENGLISH);
            if (key.startsWith(PARAMS_SERVICE_WRAPPER_PREFIX) && entry.getKey() != null) {
                int index = 0;
                try {
                    Integer.valueOf(key.substring(PARAMS_SERVICE_WRAPPER_PREFIX.length()));
                } catch (NumberFormatException e) {
                    throw new CmisRuntimeException("Invalid service wrapper configuration: " + key, e);
                }

                String[] value = entry.getValue().trim().split(",");
                if (value.length > 0) {
                    Class<?> wrapperClass = null;
                    try {
                        wrapperClass = ClassLoaderUtil.loadClass(value[0]);
                    } catch (ClassNotFoundException e) {
                        throw new CmisRuntimeException("Service wrapper class not found: " + value[0], e);
                    }

                    if (!AbstractCmisServiceWrapper.class.isAssignableFrom(wrapperClass)) {
                        throw new CmisRuntimeException("Class is not a service wrapper: " + value[0]);
                    }

                    Object[] params = null;
                    if (value.length > 1) {
                        params = new Object[value.length - 1];
                        System.arraycopy(value, 1, params, 0, params.length);
                    }

                    if (wrappers.containsKey(index)) {
                        throw new CmisRuntimeException("More than one service wrapper at the same position: " + index);
                    }

                    LOG.debug("Found wrapper [{}] {} ({})", index, wrapperClass.getName(),
                            params == null ? "" : params.toString());

                    wrappers.put(index, new WrapperDefinition(
                            (Class<? extends AbstractCmisServiceWrapper>) wrapperClass, params));
                }
            }
        }

        for (WrapperDefinition def : wrappers.values()) {
            wrapperDefinitions.add(def);
            LOG.debug("Added outer service wrapper: {}", def.getWrapperClass().getName());
        }
    }

    /**
     * Removes the outer-most wrapper.
     */
    public void removeOuterWrapper() {
        if (!wrapperDefinitions.isEmpty()) {
            wrapperDefinitions.removeLast();
        }
    }

    /**
     * Removes the inner-most wrapper.
     */
    public void removeInnerWrapper() {
        if (!wrapperDefinitions.isEmpty()) {
            wrapperDefinitions.removeFirst();
        }
    }

    /**
     * Wraps a service with all configured wrappers.
     * 
     * @param service
     *            the CMIS service object
     * @return the wrapped service
     */
    public CmisService wrap(CmisService service) {
        CmisService result = service;

        for (WrapperDefinition def : wrapperDefinitions) {
            result = def.createWrapperObject(result);
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ ");

        int i = 0;
        for (WrapperDefinition def : wrapperDefinitions) {
            if (i > 0) {
                sb.append(",");
            }

            sb.append(i);
            sb.append(": ");
            sb.append(def.getWrapperClass().getName());
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * Wrapper Definition.
     */
    private static class WrapperDefinition {

        private static final Class<?>[] CONSTRUCTOR_PARAMETERS = new Class<?>[] { CmisService.class };

        private final Class<? extends AbstractCmisServiceWrapper> wrapperClass;
        private final Constructor<? extends AbstractCmisServiceWrapper> wrapperConstructor;
        private final Object[] params;

        public WrapperDefinition(Class<? extends AbstractCmisServiceWrapper> wrapperClass, Object... params) {
            this.wrapperClass = wrapperClass;
            this.params = params;

            if (wrapperClass == null) {
                throw new CmisRuntimeException("Wrapper class must be set!");
            }

            try {
                wrapperConstructor = wrapperClass.getConstructor(CONSTRUCTOR_PARAMETERS);
            } catch (Exception e) {
                throw new CmisRuntimeException("Could not access constructor of service wrapper "
                        + wrapperClass.getName() + ": " + e.toString(), e);
            }
        }

        public Class<? extends AbstractCmisServiceWrapper> getWrapperClass() {
            return wrapperClass;
        }

        public AbstractCmisServiceWrapper createWrapperObject(CmisService service) {
            try {
                AbstractCmisServiceWrapper wrapper = wrapperConstructor.newInstance(service);
                wrapper.initialize(params);

                return wrapper;
            } catch (Exception e) {
                throw new CmisRuntimeException("Could not instantiate service wrapper " + wrapperClass.getName() + ": "
                        + e.toString(), e);
            }
        }
    }
}
