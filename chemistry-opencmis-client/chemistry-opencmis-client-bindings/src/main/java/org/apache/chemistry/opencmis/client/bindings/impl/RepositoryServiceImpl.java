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

package org.apache.chemistry.opencmis.client.bindings.impl;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.CmisSpi;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.spi.RepositoryService;

/**
 * Repository Service implementation.
 * 
 * Passes requests to the SPI and handles caching.
 */
public class RepositoryServiceImpl implements RepositoryService, Serializable {

    private static final long serialVersionUID = 1L;

    private final BindingSession session;

    /**
     * Constructor.
     */
    public RepositoryServiceImpl(BindingSession session) {
        this.session = session;
    }

    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
        RepositoryInfo result = null;
        boolean hasExtension = (extension != null) && (!extension.getExtensions().isEmpty());

        RepositoryInfoCache cache = CmisBindingsHelper.getRepositoryInfoCache(session);

        // if extension is not set, check the cache first
        if (!hasExtension) {
            result = cache.get(repositoryId);
            if (result != null) {
                return result;
            }
        }

        // it was not in the cache -> get the SPI and fetch the repository info
        CmisSpi spi = CmisBindingsHelper.getSPI(session);
        result = spi.getRepositoryService().getRepositoryInfo(repositoryId, extension);

        // put it into the cache
        if (!hasExtension) {
            cache.put(result);
        }

        return result;
    }

    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
        List<RepositoryInfo> result = null;
        boolean hasExtension = (extension != null) && (!extension.getExtensions().isEmpty());

        // get the SPI and fetch the repository infos
        CmisSpi spi = CmisBindingsHelper.getSPI(session);
        result = spi.getRepositoryService().getRepositoryInfos(extension);

        // put it into the cache
        if (!hasExtension && (result != null)) {
            RepositoryInfoCache cache = CmisBindingsHelper.getRepositoryInfoCache(session);
            for (RepositoryInfo rid : result) {
                cache.put(rid);
            }
        }

        return result;
    }

    public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        TypeDefinitionList result = null;
        boolean hasExtension = (extension != null) && (!extension.getExtensions().isEmpty());
        boolean propDefs = (includePropertyDefinitions == null ? false : includePropertyDefinitions.booleanValue());

        // get the SPI and fetch the type definitions
        CmisSpi spi = CmisBindingsHelper.getSPI(session);
        result = spi.getRepositoryService().getTypeChildren(repositoryId, typeId, includePropertyDefinitions, maxItems,
                skipCount, extension);

        // put it into the cache
        if (!hasExtension && propDefs && (result != null)) {
            TypeDefinitionCache cache = CmisBindingsHelper.getTypeDefinitionCache(session);

            for (TypeDefinition tdd : result.getList()) {
                cache.put(repositoryId, tdd);
            }
        }

        return result;
    }

    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
        TypeDefinition result = null;
        boolean hasExtension = (extension != null) && (!extension.getExtensions().isEmpty());

        TypeDefinitionCache cache = CmisBindingsHelper.getTypeDefinitionCache(session);

        // if extension is not set, check the cache first
        if (!hasExtension) {
            result = cache.get(repositoryId, typeId);
            if (result != null) {
                return result;
            }
        }

        // it was not in the cache -> get the SPI and fetch the type definition
        CmisSpi spi = CmisBindingsHelper.getSPI(session);
        result = spi.getRepositoryService().getTypeDefinition(repositoryId, typeId, extension);

        // put it into the cache
        if (!hasExtension && (result != null)) {
            cache.put(repositoryId, result);
        }

        return result;
    }

    public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension) {
        List<TypeDefinitionContainer> result = null;
        boolean hasExtension = (extension != null) && (!extension.getExtensions().isEmpty());
        boolean propDefs = (includePropertyDefinitions == null ? false : includePropertyDefinitions.booleanValue());

        // get the SPI and fetch the type definitions
        CmisSpi spi = CmisBindingsHelper.getSPI(session);
        result = spi.getRepositoryService().getTypeDescendants(repositoryId, typeId, depth, includePropertyDefinitions,
                extension);

        // put it into the cache
        if (!hasExtension && propDefs && (result != null)) {
            TypeDefinitionCache cache = CmisBindingsHelper.getTypeDefinitionCache(session);
            addToTypeCache(cache, repositoryId, result);
        }

        return result;
    }

    private void addToTypeCache(TypeDefinitionCache cache, String repositoryId, List<TypeDefinitionContainer> containers) {
        if (containers == null) {
            return;
        }

        for (TypeDefinitionContainer container : containers) {
            cache.put(repositoryId, container.getTypeDefinition());
            addToTypeCache(cache, repositoryId, container.getChildren());
        }
    }
}
