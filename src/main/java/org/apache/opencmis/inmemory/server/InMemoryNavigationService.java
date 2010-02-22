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
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.provider.NavigationService;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectInFolderContainer;
import org.apache.opencmis.commons.provider.ObjectInFolderList;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.commons.provider.ObjectParentData;
import org.apache.opencmis.inmemory.NavigationServiceImpl;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisNavigationService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

public class InMemoryNavigationService implements CmisNavigationService  {
  StoreManager fStoreManager;
  NavigationService fNavigationService; // real implementation of the service
  AtomLinkInfoProvider fAtomLinkProvider;
  
  InMemoryNavigationService(StoreManager storeManager) {
    fStoreManager = storeManager;
    fNavigationService = new NavigationServiceImpl(fStoreManager);
    fAtomLinkProvider = new AtomLinkInfoProvider(fStoreManager);
  }

  public ObjectList getCheckedOutDocs(CallContext context, String repositoryId, String folderId,
      String filter, String orderBy, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems,
      BigInteger skipCount, ExtensionsData extension, ObjectInfoHolder objectInfos) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);
    
    ObjectList res = fNavigationService.getCheckedOutDocs(repositoryId, folderId, filter, orderBy, includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount, extension);
    
    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, folderId, objectInfos, res);

    return res;
  }

  public ObjectInFolderList getChildren(CallContext context, String repositoryId, String folderId,
      String filter, String orderBy, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter,
      Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension, ObjectInfoHolder objectInfos) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);
    
    ObjectInFolderList res = fNavigationService.getChildren(repositoryId, folderId, filter, orderBy, includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, maxItems, skipCount, extension);
    
    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, folderId, objectInfos, res);
    
    return res;
  }
  

  public List<ObjectInFolderContainer> getDescendants(CallContext context, String repositoryId,
      String folderId, BigInteger depth, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter,
      Boolean includePathSegment, ExtensionsData extension, ObjectInfoHolder objectInfos) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    List<ObjectInFolderContainer> res = fNavigationService.getDescendants(repositoryId, folderId, depth, filter, includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, extension);
    
    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, folderId, objectInfos, res);

    return res;
  }

  public ObjectData getFolderParent(CallContext context, String repositoryId, String folderId,
      String filter, ExtensionsData extension, ObjectInfoHolder objectInfos) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    ObjectData res = fNavigationService.getFolderParent(repositoryId, folderId, filter, extension);

    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, folderId, objectInfos);
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, res.getId(), objectInfos);

    return res;
  }

  public List<ObjectInFolderContainer> getFolderTree(CallContext context, String repositoryId,
      String folderId, BigInteger depth, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter,
      Boolean includePathSegment, ExtensionsData extension, ObjectInfoHolder objectInfos) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    List<ObjectInFolderContainer> res = fNavigationService.getFolderTree(repositoryId, folderId, depth, filter, includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, extension);
    
    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, folderId, objectInfos, res);
    
    return res;
  }

  public List<ObjectParentData> getObjectParents(CallContext context, String repositoryId,
      String objectId, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter,
      Boolean includeRelativePathSegment, ExtensionsData extension, ObjectInfoHolder objectInfos) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    List<ObjectParentData> res = fNavigationService.getObjectParents(repositoryId, objectId, filter, includeAllowableActions, includeRelationships, renditionFilter, includeRelativePathSegment, extension);
    
    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinksGetParents(repositoryId, objectId, objectInfos, res);

    return res;
  }

  public NavigationService getNavigationService() {
    return fNavigationService;
  }
}
