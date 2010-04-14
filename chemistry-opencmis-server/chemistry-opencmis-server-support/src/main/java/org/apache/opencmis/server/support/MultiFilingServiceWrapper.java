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

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisMultiFilingService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

/**
 * MultiFiling service wrapper.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class MultiFilingServiceWrapper extends AbstractServiceWrapper implements
    CmisMultiFilingService {

  private CmisMultiFilingService fService;

  /**
   * Constructor.
   * 
   * @param service
   *          the real service object
   */
  public MultiFilingServiceWrapper(CmisMultiFilingService service) {
    if (service == null) {
      throw new IllegalArgumentException("Service must be set!");
    }

    fService = service;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisMultiFilingService#addObjectToFolder(org.apache.opencmis
   * .server.spi.CallContext, java.lang.String, java.lang.String, java.lang.String,
   * java.lang.Boolean, org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData addObjectToFolder(CallContext context, String repositoryId, String objectId,
      String folderId, Boolean allVersions, ExtensionsData extension, ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    checkId("Object Id", objectId);
    checkId("Folder Id", folderId);
    allVersions = getDefaultTrue(allVersions);

    try {
      return fService.addObjectToFolder(context, repositoryId, objectId, folderId, allVersions,
          extension, objectInfos);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisMultiFilingService#removeObjectFromFolder(org.apache.opencmis
   * .server.spi.CallContext, java.lang.String, java.lang.String, java.lang.String,
   * org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData removeObjectFromFolder(CallContext context, String repositoryId,
      String objectId, String folderId, ExtensionsData extension, ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    checkId("Object Id", objectId);

    try {
      return fService.removeObjectFromFolder(context, repositoryId, objectId, folderId, extension,
          objectInfos);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

}
