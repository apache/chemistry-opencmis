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
package org.apache.opencmis.inmemory.clientprovider;

import java.math.BigInteger;
import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.api.TypeDefinitionList;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.commons.provider.RepositoryService;
import org.apache.opencmis.inmemory.server.InMemoryRepositoryServiceImpl;

/**
 * InMemory Repository Service.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RepositoryServiceImpl  extends AbstractService implements RepositoryService {

  private InMemoryRepositoryServiceImpl fRepSvc;
  /**
   * Constructor.
   */
  public RepositoryServiceImpl(InMemoryRepositoryServiceImpl repSvc) {
    fRepSvc = repSvc;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getRepositoryInfo(java.lang.String,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public RepositoryInfoData getRepositoryInfo(String repositoryId, ExtensionsData extension) {

    return fRepSvc.getRepositoryInfo(fDummyCallContext, repositoryId, extension);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getRepositoryInfos(boolean,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public List<RepositoryInfoData> getRepositoryInfos(ExtensionsData extension) {
    return fRepSvc.getRepositoryInfos(fDummyCallContext, extension);
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

    return fRepSvc.getTypeChildren(fDummyCallContext, repositoryId, typeId, includePropertyDefinitions, maxItems, skipCount, extension);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getTypeDefinition(java.lang.String,
   * java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public TypeDefinition getTypeDefinition(String repositoryId, String typeId,
      ExtensionsData extension) {
    
    return fRepSvc.getTypeDefinition(fDummyCallContext, repositoryId, typeId, extension);
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
    
    return fRepSvc.getTypeDescendants(fDummyCallContext, repositoryId, typeId, depth, includePropertyDefinitions, extension);
  }

 
}
