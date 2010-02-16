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

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisAclService;

/**
 * ACL Service.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AclService implements CmisAclService {

  private RepositoryMap fRepositoryMap;

  /**
   * Constructor.
   */
  public AclService(RepositoryMap repositoryMap) {
    fRepositoryMap = repositoryMap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.server.spi.CmisAclService#applyAcl(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, java.lang.String, org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.enums.AclPropagation)
   */
  public AccessControlList applyAcl(CallContext context, String repositoryId, String objectId,
      AccessControlList aces, AclPropagation aclPropagation) {
    fRepositoryMap.getAuthenticatedRepository(context, repositoryId);
    throw new CmisNotSupportedException("applyAcl not supported!");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.server.spi.CmisAclService#applyAcl(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, java.lang.String, org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.provider.AccessControlList, org.apache.opencmis.commons.enums.AclPropagation,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public AccessControlList applyAcl(CallContext context, String repositoryId, String objectId,
      AccessControlList addAces, AccessControlList removeAces, AclPropagation aclPropagation,
      ExtensionsData extension) {
    fRepositoryMap.getAuthenticatedRepository(context, repositoryId);
    throw new CmisNotSupportedException("applyAcl not supported!");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.server.spi.CmisAclService#getAcl(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, java.lang.String, java.lang.Boolean, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public AccessControlList getAcl(CallContext context, String repositoryId, String objectId,
      Boolean onlyBasicPermissions, ExtensionsData extension) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).getAcl(context,
        objectId, objectId);
  }
}
