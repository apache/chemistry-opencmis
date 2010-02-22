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
package org.apache.opencmis.server.support;

import java.math.BigInteger;
import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.api.TypeDefinitionList;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisRepositoryService;

/**
 * Repository service wrapper.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RepositoryServiceWrapper extends AbstractServiceWrapper implements
    CmisRepositoryService {

  private CmisRepositoryService fService;

  /**
   * Constructor.
   * 
   * @param service
   *          the real service object
   * @param defaultMaxItems
   *          default value for <code>maxItems</code> parameters
   * @param defaultDepth
   *          default value for <code>depth</code> parameters
   */
  public RepositoryServiceWrapper(CmisRepositoryService service, BigInteger defaultMaxItems,
      BigInteger defaultDepth) {
    if (service == null) {
      throw new IllegalArgumentException("Service must be set!");
    }

    fService = service;
    setDefaultMaxItems(defaultMaxItems);
    setDefaultDepth(defaultDepth);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisRepositoryService#getRepositoryInfo(org.apache.opencmis.
   * server.spi.CallContext, java.lang.String, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public RepositoryInfoData getRepositoryInfo(CallContext context, String repositoryId,
      ExtensionsData extension) {
    checkRepositoryId(repositoryId);

    try {
      return fService.getRepositoryInfo(context, repositoryId, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisRepositoryService#getRepositoryInfos(org.apache.opencmis
   * .server.spi.CallContext, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public List<RepositoryInfoData> getRepositoryInfos(CallContext context, ExtensionsData extension) {
    try {
      return fService.getRepositoryInfos(context, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisRepositoryService#getTypeChildren(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, java.lang.String, java.lang.Boolean, java.math.BigInteger,
   * java.math.BigInteger, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public TypeDefinitionList getTypeChildren(CallContext context, String repositoryId,
      String typeId, Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    includePropertyDefinitions = getDefaultFalse(includePropertyDefinitions);
    maxItems = getMaxItems(maxItems);
    skipCount = getSkipCount(skipCount);

    try {
      return fService.getTypeChildren(context, repositoryId, typeId, includePropertyDefinitions,
          maxItems, skipCount, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisRepositoryService#getTypeDefinition(org.apache.opencmis.
   * server.spi.CallContext, java.lang.String, java.lang.String,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public TypeDefinition getTypeDefinition(CallContext context, String repositoryId, String typeId,
      ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkId("Type Id", typeId);

    try {
      return fService.getTypeDefinition(context, repositoryId, typeId, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisRepositoryService#getTypeDescendants(org.apache.opencmis
   * .server.spi.CallContext, java.lang.String, java.lang.String, java.math.BigInteger,
   * java.lang.Boolean, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public List<TypeDefinitionContainer> getTypeDescendants(CallContext context, String repositoryId,
      String typeId, BigInteger depth, Boolean includePropertyDefinitions, ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    includePropertyDefinitions = getDefaultFalse(includePropertyDefinitions);
    depth = getDepth(depth);

    try {
      return fService.getTypeDescendants(context, repositoryId, typeId, depth,
          includePropertyDefinitions, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

}
