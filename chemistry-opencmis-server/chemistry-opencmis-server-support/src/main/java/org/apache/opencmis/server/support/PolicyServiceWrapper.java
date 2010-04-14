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

import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisPolicyService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

/**
 * Policy service wrapper.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class PolicyServiceWrapper extends AbstractServiceWrapper implements CmisPolicyService {

  private CmisPolicyService fService;

  /**
   * Constructor.
   * 
   * @param service
   *          the real service object
   */
  public PolicyServiceWrapper(CmisPolicyService service) {
    if (service == null) {
      throw new IllegalArgumentException("Service must be set!");
    }

    fService = service;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisPolicyService#applyPolicy(org.apache.opencmis.server.spi
   * .CallContext, java.lang.String, java.lang.String, java.lang.String,
   * org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData applyPolicy(CallContext context, String repositoryId, String policyId,
      String objectId, ExtensionsData extension, ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    checkId("Policy Id", policyId);
    checkId("Object Id", objectId);

    try {
      return fService
          .applyPolicy(context, repositoryId, policyId, objectId, extension, objectInfos);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisPolicyService#getAppliedPolicies(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, java.lang.String, java.lang.String,
   * org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public List<ObjectData> getAppliedPolicies(CallContext context, String repositoryId,
      String objectId, String filter, ExtensionsData extension, ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    checkId("Object Id", objectId);

    try {
      return fService.getAppliedPolicies(context, repositoryId, objectId, filter, extension,
          objectInfos);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisPolicyService#removePolicy(org.apache.opencmis.server.spi
   * .CallContext, java.lang.String, java.lang.String, java.lang.String,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public void removePolicy(CallContext context, String repositoryId, String policyId,
      String objectId, ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkId("Policy Id", policyId);
    checkId("Object Id", objectId);

    try {
      fService.removePolicy(context, repositoryId, policyId, objectId, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }
}
