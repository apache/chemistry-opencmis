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
package org.apache.opencmis.inmemory.server;

import java.math.BigInteger;
import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.api.TypeDefinitionList;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.commons.provider.RepositoryService;
import org.apache.opencmis.inmemory.RepositoryServiceImpl;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisRepositoryService;

public class InMemoryRepositoryService implements CmisRepositoryService {
  StoreManager fStoreManager;
  RepositoryService fRepositoryService; // real implementation of the service
  
  InMemoryRepositoryService(StoreManager storeManager) {
    fStoreManager = storeManager;
    fRepositoryService = new RepositoryServiceImpl(fStoreManager);
  }

  public RepositoryInfoData getRepositoryInfo(CallContext context, String repositoryId,
      ExtensionsData extension) {
    
    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);
    
    return fRepositoryService.getRepositoryInfo(repositoryId, extension);
  }

  public List<RepositoryInfoData> getRepositoryInfos(CallContext context, ExtensionsData extension) {
    
    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    return fRepositoryService.getRepositoryInfos(extension);
  }

  public TypeDefinitionList getTypeChildren(CallContext context, String repositoryId,
      String typeId, Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension) {
    
    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    return fRepositoryService.getTypeChildren(repositoryId, typeId, includePropertyDefinitions, maxItems, skipCount, extension);
  }

  public TypeDefinition getTypeDefinition(CallContext context, String repositoryId,
      String typeId, ExtensionsData extension) {
    
    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    return fRepositoryService.getTypeDefinition(repositoryId, typeId, extension);
  }

  public List<TypeDefinitionContainer> getTypeDescendants(CallContext context, String repositoryId,
      String typeId, BigInteger depth, Boolean includePropertyDefinitions, ExtensionsData extension) {
    
    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    return fRepositoryService.getTypeDescendants(repositoryId, typeId, depth, includePropertyDefinitions, extension);
  }

  public RepositoryService getRepositoryService() {
    return fRepositoryService;
  }

}
