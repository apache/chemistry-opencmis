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

import org.apache.chemistry.opencmis.client.bindings.cache.Cache;
import org.apache.chemistry.opencmis.client.bindings.cache.impl.CacheImpl;
import org.apache.chemistry.opencmis.client.bindings.cache.impl.LruCacheLevelImpl;
import org.apache.chemistry.opencmis.client.bindings.cache.impl.MapCacheLevelImpl;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;

/**
 * A cache for type definition objects.
 */
public class TypeDefinitionCache implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int CACHE_SIZE_REPOSITORIES = 10;
    private static final int CACHE_SIZE_TYPES = 100;

    private final Cache cache;

    /**
     * Constructor.
     * 
     * @param session
     *            the session object
     */
    public TypeDefinitionCache(BindingSession session) {
        int repCount = session.get(SessionParameter.CACHE_SIZE_REPOSITORIES, CACHE_SIZE_REPOSITORIES);
        if (repCount < 1) {
            repCount = CACHE_SIZE_REPOSITORIES;
        }

        int typeCount = session.get(SessionParameter.CACHE_SIZE_TYPES, CACHE_SIZE_TYPES);
        if (typeCount < 1) {
            typeCount = CACHE_SIZE_TYPES;
        }

        cache = new CacheImpl("Type Definition Cache");
        cache.initialize(new String[] {
                MapCacheLevelImpl.class.getName() + " " + MapCacheLevelImpl.CAPACITY + "=" + repCount, // repository
                LruCacheLevelImpl.class.getName() + " " + LruCacheLevelImpl.MAX_ENTRIES + "=" + typeCount // type
        });
    }

    /**
     * Adds a type definition object to the cache.
     * 
     * @param repositoryId
     *            the repository id
     * @param typeDefinition
     *            the type definition object
     */
    public void put(String repositoryId, TypeDefinition typeDefinition) {
        if ((typeDefinition == null) || (typeDefinition.getId() == null)) {
            return;
        }

        cache.put(typeDefinition, repositoryId, typeDefinition.getId());
    }

    /**
     * Retrieves a type definition object from the cache.
     * 
     * @param repositoryId
     *            the repository id
     * @param typeId
     *            the type id
     * @return the type definition object or <code>null</code> if the object is
     *         not in the cache
     */
    public TypeDefinition get(String repositoryId, String typeId) {
        return (TypeDefinition) cache.get(repositoryId, typeId);
    }

    /**
     * Removes a type definition object from the cache.
     * 
     * @param repositoryId
     *            the repository id
     * @param typeId
     *            the type id
     */
    public void remove(String repositoryId, String typeId) {
        cache.remove(repositoryId, typeId);
    }

    /**
     * Removes all type definition objects of a repository from the cache.
     * 
     * @param repositoryId
     *            the repository id
     */
    public void remove(String repositoryId) {
        cache.remove(repositoryId);
    }

    @Override
    public String toString() {
        return cache.toString();
    }
}
