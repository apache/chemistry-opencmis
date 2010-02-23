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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.provider.MultiFilingService;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectService;
import org.apache.opencmis.inmemory.MultiFilingServiceImpl;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisMultiFilingService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

public class InMemoryMultiFilingService implements CmisMultiFilingService {

  private static final Log LOG = LogFactory.getLog(InMemoryMultiFilingService.class.getName());

  MultiFilingService fMultiService;
  ObjectService fObjectService;
  AtomLinkInfoProvider fAtomLinkProvider;

  public InMemoryMultiFilingService(StoreManager storeMgr, ObjectService objService) {
    fAtomLinkProvider = new AtomLinkInfoProvider(storeMgr);
    fMultiService = new MultiFilingServiceImpl(storeMgr);
    fObjectService = objService;
  }

  public ObjectData addObjectToFolder(CallContext context, String repositoryId, String objectId,
      String folderId, Boolean allVersions, ExtensionsData extension, ObjectInfoHolder objectInfos) {

    LOG.debug("Begin addObjectToFolder()");

    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    fMultiService.addObjectToFolder(repositoryId, objectId, folderId, allVersions, extension);

    // Make a call to getObject to convert the resulting id into an ObjectData
    ObjectData res = fObjectService.getObject(repositoryId, objectId, "*", false,
        IncludeRelationships.NONE, null, false, false, extension);

    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, objectId, objectInfos);
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, folderId, objectInfos);

    LOG.debug("End addObjectToFolder()");
    return res;
  }

  public ObjectData removeObjectFromFolder(CallContext context, String repositoryId,
      String objectId, String folderId, ExtensionsData extension, ObjectInfoHolder objectInfos) {

    LOG.debug("Begin removeObjectFromFolder()");
    // Attach the CallContext to a thread local context that can be accessed from everywhere
    RuntimeContext.getRuntimeConfig().attachCfg(context);

    fMultiService.removeObjectFromFolder(repositoryId, objectId, folderId, extension);

    // Make a call to getObject to convert the resulting id into an ObjectData
    ObjectData res = fObjectService.getObject(repositoryId, objectId, "*", false,
        IncludeRelationships.NONE, null, false, false, extension);

    // To be able to provide all Atom links in the response we need additional information:
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, objectId, objectInfos);
    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, folderId, objectInfos);

    LOG.debug("End removeObjectFromFolder()");

    return res;
  }

}
