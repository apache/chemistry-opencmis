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
package org.apache.opencmis.fileshare;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.api.TypeDefinitionList;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisRepositoryService;

/**
 * Repository Service.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RepositoryService implements CmisRepositoryService {

  private RepositoryMap fRepositoryMap;

  /**
   * Constructor.
   */
  public RepositoryService(RepositoryMap repositoryMap) {
    fRepositoryMap = repositoryMap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisRepositoryService#getRepositoryInfo(org.apache.opencmis.server.spi.CallContext
   * , java.lang.String, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public RepositoryInfoData getRepositoryInfo(CallContext context, String repositoryId,
      ExtensionsData extension) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).getRepositoryInfo(
        context);
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.apache.opencmis.server.spi.CmisRepositoryService#getRepositoryInfos(org.apache.opencmis.server.spi.
   * CallContext, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public List<RepositoryInfoData> getRepositoryInfos(CallContext context, ExtensionsData extension) {
    List<RepositoryInfoData> result = new ArrayList<RepositoryInfoData>();

    for (FileShareRepository fsr : fRepositoryMap.getRepositories()) {
      result.add(fsr.getRepositoryInfo(context));
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisRepositoryService#getTypeChildren(org.apache.opencmis.server.spi.CallContext
   * , java.lang.String, java.lang.String, java.lang.Boolean, java.math.BigInteger,
   * java.math.BigInteger, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public TypeDefinitionList getTypeChildren(CallContext context, String repositoryId,
      String typeId, Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).getTypesChildren(
        context, typeId, includePropertyDefinitions, maxItems, skipCount);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisRepositoryService#getTypeDefinition(org.apache.opencmis.server.spi.CallContext
   * , java.lang.String, java.lang.String, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public TypeDefinition getTypeDefinition(CallContext context, String repositoryId, String typeId,
      ExtensionsData extension) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).getTypeDefinition(
        context, typeId);
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.apache.opencmis.server.spi.CmisRepositoryService#getTypeDescendants(org.apache.opencmis.server.spi.
   * CallContext, java.lang.String, java.lang.String, java.math.BigInteger, java.lang.Boolean,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public List<TypeDefinitionContainer> getTypeDescendants(CallContext context, String repositoryId,
      String typeId, BigInteger depth, Boolean includePropertyDefinitions, ExtensionsData extension) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).getTypesDescendants(
        context, typeId, depth, includePropertyDefinitions);
  }
}
