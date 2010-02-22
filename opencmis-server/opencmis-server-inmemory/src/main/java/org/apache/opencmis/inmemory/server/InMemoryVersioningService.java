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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectService;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.VersioningService;
import org.apache.opencmis.inmemory.VersioningServiceImpl;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisVersioningService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

public class InMemoryVersioningService implements CmisVersioningService {
  private static final Log LOG = LogFactory.getLog(CmisVersioningService.class.getName());

  StoreManager fStoreManager;
  VersioningService fVersioningService; // real implementation of the service
  ObjectService fObjectService; // real implementation of the service
  AtomLinkInfoProvider fAtomLinkProvider;

  public InMemoryVersioningService(StoreManager storeManager, ObjectService objectService) {
    fStoreManager = storeManager;
    fObjectService = objectService;
    fVersioningService = new VersioningServiceImpl(fStoreManager, objectService);
    fAtomLinkProvider = new AtomLinkInfoProvider(fStoreManager);
  }

  
  public void cancelCheckOut(CallContext context, String repositoryId, String objectId,
      ExtensionsData extension) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    fVersioningService.cancelCheckOut(repositoryId, objectId, extension);

  }

  public ObjectData checkIn(CallContext context, String repositoryId, Holder<String> objectId,
      Boolean major, PropertiesData properties, ContentStreamData contentStream,
      String checkinComment, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension, ObjectInfoHolder objectInfos) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    fVersioningService.checkIn(repositoryId, objectId, major, properties, contentStream,
        checkinComment, policies, addAces, removeAces, extension);

    ObjectData objData = null; 
    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, objectId.getValue(), objectInfos);
    if (context.getBinding().equals(CallContext.BINDING_ATOMPUB)) {
        objData = fObjectService.getObject(repositoryId, objectId.getValue(), "*", false,
            IncludeRelationships.NONE, null, false, false, extension);      
    }
   
    return objData;
  }

  public ObjectData checkOut(CallContext context, String repositoryId, Holder<String> objectId,
      ExtensionsData extension, Holder<Boolean> contentCopied, ObjectInfoHolder objectInfos) {
    
    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    fVersioningService.checkOut(repositoryId, objectId, extension, contentCopied);
    
    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, objectId.getValue(), objectInfos);

    ObjectData objData = null; 
    if (context.getBinding().equals(CallContext.BINDING_ATOMPUB)) {
      objData = fObjectService.getObject(repositoryId, objectId.getValue(), "*", false,
          IncludeRelationships.NONE, null, false, false, extension);      
    }
    
    return objData;
  }

  public List<ObjectData> getAllVersions(CallContext context, String repositoryId,
      String versionSeriesId, String filter, Boolean includeAllowableActions,
      ExtensionsData extension, ObjectInfoHolder objectInfos) {
    
    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    List<ObjectData> res = fVersioningService.getAllVersions(repositoryId, versionSeriesId, filter, includeAllowableActions, extension);
    
    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, versionSeriesId, objectInfos);
    for (ObjectData od : res) {
      fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, od.getId(), objectInfos);        
    }
    return res;
  }

  public ObjectData getObjectOfLatestVersion(CallContext context, String repositoryId,
      String versionSeriesId, Boolean major, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
      Boolean includeAcl, ExtensionsData extension, ObjectInfoHolder objectInfos) {

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    ObjectData res = fVersioningService.getObjectOfLatestVersion(repositoryId, versionSeriesId, major, filter,
        includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds,
        includeAcl, extension);
    
    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, versionSeriesId, objectInfos);

    return res;
  }

  public PropertiesData getPropertiesOfLatestVersion(CallContext context, String repositoryId,
      String versionSeriesId, Boolean major, String filter, ExtensionsData extension) {
    
    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    PropertiesData res = fVersioningService.getPropertiesOfLatestVersion(repositoryId, versionSeriesId, major, filter, extension);

    return res;
  }

}
