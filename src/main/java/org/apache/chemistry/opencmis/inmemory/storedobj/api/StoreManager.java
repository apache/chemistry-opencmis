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
package org.apache.chemistry.opencmis.inmemory.storedobj.api;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;

/**
 * interface to a repository implementation. This interface is the entry point
 * to a repository that can persist CMIS objects. Using this interface the type
 * information can be retrieved or set, a repository can be created or for a
 * given repository the store can be retrieved.
 * 
 * @author Jens
 */
public interface StoreManager {

    /**
     * return a list of all available repositories
     * 
     * @return
     */
    List<String> getAllRepositoryIds();

    /**
     * Initialize the store for the given repository. Only called for
     * repositories that exist on startup (i.e. for each repository id returned
     * in a previous getAllRepositoryIds() call.
     * 
     * @param repositoryId
     *            id of repository to initialize
     * @param isCreated
     *            true if the repository was just created and is initialized for
     *            the first time false if it existed before and is reloaded
     */
    void initRepository(String repositoryId);

    /**
     * get the object store for the given repository id.
     * 
     * @param repositoryId
     * @return the object store in which objects for this repository are stored.
     */
    ObjectStore getObjectStore(String repositoryId);

    /**
     * get a permission and parameter validating instance
     * 
     * @return
     *      validator and permission checker
     */
    CmisServiceValidator getServiceValidator();
    
    /**
     * create a new repository with the given id. Create the repository,
     * initiate the type system and initialize it so that it is ready for use
     * 
     * @param repositoryId
     *            id of repository
     * @param typeCreatorClassName
     *            class implementing the type creation, the class must implement
     *            the interface TypeCreator
     */
    void createAndInitRepository(String repositoryId, String typeCreatorClassName);

    /**
     * retrieve a list with all type definitions.
     * 
     * @param repositoryId
     *            id of repository
     * @param includePropertyDefinitions
     *            indicates whether to include property definitions in returned
     *            type
     * @return map with type definition
     */
    Collection<TypeDefinitionContainer> getTypeDefinitionList(String repositoryId, boolean includePropertyDefinitions);

    /**
     * Retrieve a type definition for a give repository and type id
     * 
     * @param repositoryId
     *            id of repository
     * @param typeId
     *            id of type definition
     * @return type definition
     */
    TypeDefinitionContainer getTypeById(String repositoryId, String typeId);

    /**
     * Retrieve a type definition for a give repository and type id with or
     * without property definitions and limited to depth in hierarchy
     * 
     * @param repositoryId
     *            id of repository
     * @param typeId
     *            id of type definition
     * @param includePropertyDefinitions
     *            indicates whether to include property definitions in returned
     *            type
     * @param depth
     *            limit depth of type hierarchy in return (-1 means unlimited)
     * @return type definition
     */
    TypeDefinitionContainer getTypeById(String repositoryId, String typeId, boolean includePropertyDefinitions,
            int depth);

    /**
     * Retrieve a factory to create CMIS data structures used as containers
     * 
     * @return factory object
     */
    BindingsObjectFactory getObjectFactory();

    /**
     * Retrieve a list of root types in the repositories. Root types are
     * available by definition and need to to be created by a client. CMIS
     * supports documents, folders, relations and policies as root types
     * 
     * @param repositoryId
     *            id of repository
     * @return list of root types
     */
    List<TypeDefinitionContainer> getRootTypes(String repositoryId);

    /**
     * Retrieve the repository information for a repository
     * 
     * @param repositoryId
     *            id of repository
     * @return repository information
     */
    RepositoryInfo getRepositoryInfo(String repositoryId);
    
    /**
     * retrieve the type manager for a given repository
     * @param repositoryId
     *      id of repository
     * @return
     *      type manager for this repository or null if repository is unknown
     */
    TypeManagerCreatable getTypeManager(String repositoryId);
    
    /**
     * Execute a query against the repository (same parameter as the discovery service
     * query method
     * 
     * @param user
     * @param repositoryId
     * @param statement
     * @param searchAllVersions
     * @param includeAllowableActions
     * @param includeRelationships
     * @param renditionFilter
     * @param maxItems
     * @param skipCount
     * @return
     */
    ObjectList query(String user, String repositoryId, String statement, Boolean searchAllVersions,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount);
    
 }