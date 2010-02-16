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

package org.apache.opencmis.client.provider.impl;

import java.math.BigInteger;
import java.util.List;

import org.apache.opencmis.client.provider.spi.CmisSpi;
import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionList;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.commons.provider.RepositoryService;

/**
 * Repository Service implementation.
 * 
 * Passes requests to the SPI and handles caching.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RepositoryServiceImpl implements RepositoryService {

  private Session fSession;

  /**
   * Constructor.
   */
  public RepositoryServiceImpl(Session session) {
    fSession = session;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getRepositoryInfo(java.lang.String,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public RepositoryInfoData getRepositoryInfo(String repositoryId, ExtensionsData extension) {
    RepositoryInfoData result = null;
    boolean hasExtension = (extension != null) && (!extension.getExtensions().isEmpty());

    RepositoryInfoCache cache = CmisProviderHelper.getRepositoryInfoCache(fSession);

    // if extension is not set, check the cache first
    if (!hasExtension) {
      result = cache.get(repositoryId);
      if (result != null) {
        return result;
      }
    }

    // it was not in the cache -> get the SPI and fetch the repository info
    CmisSpi spi = CmisProviderHelper.getSPI(fSession);
    result = spi.getRepositoryService().getRepositoryInfo(repositoryId, extension);

    // put it into the cache
    if (!hasExtension) {
      cache.put(result);
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.RepositoryService#getRepositoryInfos(org.apache.opencmis.client.provider
   * .ExtensionsData)
   */
  public List<RepositoryInfoData> getRepositoryInfos(ExtensionsData extension) {
    List<RepositoryInfoData> result = null;
    boolean hasExtension = (extension != null) && (!extension.getExtensions().isEmpty());

    // get the SPI and fetch the repository infos
    CmisSpi spi = CmisProviderHelper.getSPI(fSession);
    result = spi.getRepositoryService().getRepositoryInfos(extension);

    // put it into the cache
    if (!hasExtension && (result != null)) {
      RepositoryInfoCache cache = CmisProviderHelper.getRepositoryInfoCache(fSession);
      for (RepositoryInfoData rid : result) {
        cache.put(rid);
      }
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getTypeChildren(java.lang.String,
   * java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public TypeDefinitionList getTypeChildren(String repositoryId, String typeId,
      Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension) {
    TypeDefinitionList result = null;
    boolean hasExtension = (extension != null) && (!extension.getExtensions().isEmpty());
    boolean propDefs = (includePropertyDefinitions == null ? false : includePropertyDefinitions
        .booleanValue());

    // get the SPI and fetch the type definitions
    CmisSpi spi = CmisProviderHelper.getSPI(fSession);
    result = spi.getRepositoryService().getTypeChildren(repositoryId, typeId,
        includePropertyDefinitions, maxItems, skipCount, extension);

    // put it into the cache
    if (!hasExtension && propDefs && (result != null)) {
      TypeDefinitionCache cache = CmisProviderHelper.getTypeDefinitionCache(fSession);

      for (TypeDefinition tdd : result.getList()) {
        cache.put(repositoryId, tdd);
      }
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getTypeDefinition(java.lang.String,
   * java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public TypeDefinition getTypeDefinition(String repositoryId, String typeId,
      ExtensionsData extension) {
    TypeDefinition result = null;
    boolean hasExtension = (extension != null) && (!extension.getExtensions().isEmpty());

    TypeDefinitionCache cache = CmisProviderHelper.getTypeDefinitionCache(fSession);

    // if extension is not set, check the cache first
    if (!hasExtension) {
      result = cache.get(repositoryId, typeId);
      if (result != null) {
        return result;
      }
    }

    // it was not in the cache -> get the SPI and fetch the type definition
    CmisSpi spi = CmisProviderHelper.getSPI(fSession);
    result = spi.getRepositoryService().getTypeDefinition(repositoryId, typeId, extension);

    // put it into the cache
    if (!hasExtension && (result != null)) {
      cache.put(repositoryId, result);
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getTypeDescendants(java.lang.String,
   * java.lang.String, java.math.BigInteger, java.lang.Boolean,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId,
      BigInteger depth, Boolean includePropertyDefinitions, ExtensionsData extension) {
    List<TypeDefinitionContainer> result = null;
    boolean hasExtension = (extension != null) && (!extension.getExtensions().isEmpty());
    boolean propDefs = (includePropertyDefinitions == null ? false : includePropertyDefinitions
        .booleanValue());

    // get the SPI and fetch the type definitions
    CmisSpi spi = CmisProviderHelper.getSPI(fSession);
    result = spi.getRepositoryService().getTypeDescendants(repositoryId, typeId, depth,
        includePropertyDefinitions, extension);

    // put it into the cache
    if (!hasExtension && propDefs && (result != null)) {
      TypeDefinitionCache cache = CmisProviderHelper.getTypeDefinitionCache(fSession);
      addToTypeCache(cache, repositoryId, result);
    }

    return result;
  }

  private void addToTypeCache(TypeDefinitionCache cache, String repositoryId,
      List<TypeDefinitionContainer> containers) {
    if (containers == null) {
      return;
    }

    for (TypeDefinitionContainer container : containers) {
      cache.put(repositoryId, container.getTypeDefinition());
      addToTypeCache(cache, repositoryId, container.getChildren());
    }
  }
}
