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
package org.apache.opencmis.inmemory.storedobj.api;

import java.util.Collection;
import java.util.List;

import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.inmemory.ConfigMap;
import org.apache.opencmis.inmemory.RepositoryInfo;

/**
 * interface to a repository implementation. This interface is the entry point to a 
 * repository that can persist CMIS objects. Using this interface the type information
 * can be retrieved or set, a repository can be created or for a given repository the
 * store can be retrieved. 
 * 
 * @author Jens
 */
public interface StoreManager {

  /**
   * return a list of all available repositories
   * @return
   */
  List<String> getAllRepositoryIds();
  
  /**
   * Initialize the store for the given repository (first call done on a StoreManager after creation)
   * @param repositoryId
   *    id of repository to initialize
   * @param isCreated
   *    true if the repository was just created and is initialized for the first time
   *    false if it existed before and is reloaded
   */
  public void initRepository(String repositoryId, boolean isCreated);
  
  /**
   * get the object store for the given repository id.
   * 
   * @param repositoryId
   * @return the object store in which objects for this repository are stored.
   */
  ObjectStore getObjectStore(String repositoryId);

  /**
   * create a new repository with the given id
   * 
   * @param repositoryId
   *    id of repository
   */
  void createRepository(String repositoryId);
  
  /**
   * load an existing repository with the given id
   * 
   * @param repositoryId
   *    id of repository
   */
  RepositoryInfo loadRepository(String repositoryId);
  
  
  /**
   * retrieve a parameter that a client has attached to a session
   * 
   * @param parameter
   *    name of the parameter
   * @return
   *    value of the parameter
   */
  String getParameter(String parameter);

  /**
   * retrieve a list with all type definitions. 
   * @param repositoryId
   *    id of repository 
   * @return
   *    map with type definition
   */
  Collection<TypeDefinitionContainer> getTypeDefinitionList(String repositoryId);
  
  /**
   * Retrieve a type definition for a give repository and type id
   * 
   * @param repositoryId
   *    id of repository 
   * @param typeId
   *    id of type definition
   * @return
   *    type definition
   */
  TypeDefinitionContainer getTypeById(String repositoryId, String typeId);

  /**
   * Retrieve a factory to create CMIS data structures used as containers
   * 
   * @return
   *    factory object
   */
  ProviderObjectFactory getObjectFactory();

  /**
   * Retrieve a list of root types in the repositories. Root types are available by
   * definition and need to to be created by a client. CMIS supports documents,
   * folders, relations and policies as root types
   * 
   * @param repositoryId
   *    id of repository
   * @return
   *    list of root types
   */
  List<TypeDefinitionContainer> getRootTypes(String repositoryId);

  /**
   * Retrieve the repository information for a repository
   * 
   * @param repositoryId
   *    id of repository
   * @return
   *    repository information
   */
  RepositoryInfoData getRepositoryInfo(String repositoryId);

  /**
   * Initialize the type system. Helper class to create types in the repository
   * implemented in an implementation specific way. CMIS does not yet provide a 
   * standard to create type definitions. The SPI will call this method in case
   * a class name was attached to the session. The implementation of this method
   * should call initTypeSystem of the StoreManager to add the types to the 
   * system. 
   * 
   * @param repositoryId
   *    id of repository where this type gets stored
   * @param typeCreatorClassName
   *    class implementing the type creation, the class must implement the interface
   *    TypeCreator
   *    
   */
  void initTypeSystem(String repositoryId, String typeCreatorClassName);

  /**
   * Initialize the repository information. A client can assign specific values that
   * should be set for subsequent getRepositoryInfo() call. Mainly uses for unit tests.
   * 
   * @param repoInfoCreatorClassName
   *    class name of class setting repository information. The class must implement
   *    the interface RepositoryInfoCreator
   */
  void initRepositoryInfo(String repositoryInfo, String repoInfoCreatorClassName);

  /**
   * Assign a reader that can retrieve context parameters to this repository manager
   * @param session
   */
  void setConfigReader(ConfigMap configMap);  
}