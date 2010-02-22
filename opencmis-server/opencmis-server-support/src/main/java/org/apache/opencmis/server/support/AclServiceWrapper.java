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
import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisAclService;

/**
 * ACL service wrapper.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AclServiceWrapper extends AbstractServiceWrapper implements CmisAclService {

  private CmisAclService fService;

  /**
   * Constructor.
   * 
   * @param service
   *          the real service object
   */
  public AclServiceWrapper(CmisAclService service) {
    if (service == null) {
      throw new IllegalArgumentException("Service must be set!");
    }

    fService = service;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisAclService#applyAcl(org.apache.opencmis.server.spi.CallContext
   * , java.lang.String, java.lang.String, org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.enums.AclPropagation)
   */
  public AccessControlList applyAcl(CallContext context, String repositoryId, String objectId,
      AccessControlList aces, AclPropagation aclPropagation) {
    checkRepositoryId(repositoryId);
    checkId("Object Id", objectId);
    aclPropagation = getDefault(aclPropagation);

    try {
      return fService.applyAcl(context, repositoryId, objectId, aces, aclPropagation);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisAclService#applyAcl(org.apache.opencmis.server.spi.CallContext
   * , java.lang.String, java.lang.String, org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.enums.AclPropagation,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public AccessControlList applyAcl(CallContext context, String repositoryId, String objectId,
      AccessControlList addAces, AccessControlList removeAces, AclPropagation aclPropagation,
      ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkId("Object Id", objectId);
    aclPropagation = getDefault(aclPropagation);

    try {
      return fService.applyAcl(context, repositoryId, objectId, addAces, removeAces,
          aclPropagation, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisAclService#getAcl(org.apache.opencmis.server.spi.CallContext
   * , java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public AccessControlList getAcl(CallContext context, String repositoryId, String objectId,
      Boolean onlyBasicPermissions, ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkId("Object Id", objectId);
    onlyBasicPermissions = getDefaultTrue(onlyBasicPermissions);

    try {
      return fService.getAcl(context, repositoryId, objectId, onlyBasicPermissions, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

}
