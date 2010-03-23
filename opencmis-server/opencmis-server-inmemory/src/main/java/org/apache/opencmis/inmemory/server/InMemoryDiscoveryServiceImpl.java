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
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.TypeOfChanges;
import org.apache.opencmis.commons.impl.dataobjects.ChangeEventInfoDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectInFolderContainer;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisDiscoveryService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

public class InMemoryDiscoveryServiceImpl implements CmisDiscoveryService {

  private StoreManager fStoreManager;
  AtomLinkInfoProvider fAtomLinkProvider;
  InMemoryNavigationServiceImpl fNavigationService; // real implementation of the service
  InMemoryRepositoryServiceImpl fRepositoryService;

  public InMemoryDiscoveryServiceImpl(StoreManager storeManager,
      InMemoryRepositoryServiceImpl repSvc, InMemoryNavigationServiceImpl navSvc) {
    fStoreManager = storeManager;
    fAtomLinkProvider = new AtomLinkInfoProvider(fStoreManager);
    fNavigationService = navSvc;
    fRepositoryService = repSvc;
  }

  public ObjectList getContentChanges(CallContext context, String repositoryId,
      Holder<String> changeLogToken, Boolean includeProperties, String filter,
      Boolean includePolicyIds, Boolean includeAcl, BigInteger maxItems, ExtensionsData extension,
      ObjectInfoHolder objectInfos) {
    // dummy implementation using hard coded values

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      RepositoryInfoData rep = fRepositoryService.getRepositoryInfo(context, repositoryId, null);
      String rootFolderId = rep.getRootFolderId();

      ObjectListImpl objList = new ObjectListImpl();
      List<ObjectInFolderContainer> tempRes = fNavigationService.getDescendants(context,
          repositoryId, rootFolderId, BigInteger.valueOf(3), filter, false,
          IncludeRelationships.NONE, null, false, extension, null);

      // convert ObjectInFolderContainerList to objectList
      List<ObjectData> lod = new ArrayList<ObjectData>();
      for (ObjectInFolderContainer obj : tempRes) {
        convertList(lod, obj);
      }
      objList.setObjects(lod);
      objList.setNumItems(BigInteger.valueOf(lod.size()));

      // To be able to provide all Atom links in the response we need additional information:
      fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, null, objectInfos, objList);
      return objList;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  private void convertList(List<ObjectData> lod, ObjectInFolderContainer obj) {
    lod.add(obj.getObject().getObject());
    // add dummy event info
    ObjectData oif = obj.getObject().getObject();
    ObjectDataImpl oifImpl = (ObjectDataImpl) oif;
    ChangeEventInfoDataImpl changeEventInfo = new ChangeEventInfoDataImpl();
    changeEventInfo.setTypeOfChanges(TypeOfChanges.UPDATED);
    changeEventInfo.setChangeTime(new GregorianCalendar());
    oifImpl.setChangeEventInfo(changeEventInfo);
    if (null != obj.getChildren()) {
      for (ObjectInFolderContainer oifc : obj.getChildren()) {
        convertList(lod, oifc);
      }
    }
  }

  public ObjectList query(CallContext context, String repositoryId, String statement,
      Boolean searchAllVersions, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems,
      BigInteger skipCount, ExtensionsData extension) {
    // dummy implementation using hard coded values

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      // use descendants of root folder as result
      RepositoryInfoData rep = fRepositoryService.getRepositoryInfo(context, repositoryId, null);
      String rootFolderId = rep.getRootFolderId();
      ObjectListImpl objList = new ObjectListImpl();
      List<ObjectInFolderContainer> tempRes = fNavigationService.getDescendants(context,
          repositoryId, rootFolderId, BigInteger.valueOf(3), "*", includeAllowableActions,
          includeRelationships, renditionFilter, false, extension, null);

      // convert ObjectInFolderContainerList to objectList
      List<ObjectData> lod = new ArrayList<ObjectData>();
      for (ObjectInFolderContainer obj : tempRes) {
        convertList(lod, obj);
      }
      objList.setObjects(lod);
      objList.setNumItems(BigInteger.valueOf(lod.size()));

      return objList;
    }
    finally {
      RuntimeContext.remove();
    }
  }

}
