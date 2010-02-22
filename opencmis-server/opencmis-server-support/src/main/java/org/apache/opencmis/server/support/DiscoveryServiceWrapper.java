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

import java.math.BigInteger;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisDiscoveryService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

/**
 * Discovery service wrapper.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class DiscoveryServiceWrapper extends AbstractServiceWrapper implements CmisDiscoveryService {

  private CmisDiscoveryService fService;

  /**
   * Constructor.
   * 
   * @param service
   *          the real service object
   * @param defaultMaxItems
   *          default value for <code>maxItems</code> parameters
   */
  public DiscoveryServiceWrapper(CmisDiscoveryService service, BigInteger defaultMaxItems) {
    if (service == null) {
      throw new IllegalArgumentException("Service must be set!");
    }

    fService = service;
    setDefaultMaxItems(defaultMaxItems);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisDiscoveryService#getContentChanges(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, org.apache.opencmis.commons.provider.Holder,
   * java.lang.Boolean, java.lang.String, java.lang.Boolean, java.lang.Boolean,
   * java.math.BigInteger, org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectList getContentChanges(CallContext context, String repositoryId,
      Holder<String> changeLogToken, Boolean includeProperties, String filter,
      Boolean includePolicyIds, Boolean includeAcl, BigInteger maxItems, ExtensionsData extension,
      ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    includeProperties = getDefaultFalse(includeProperties);
    includePolicyIds = getDefaultFalse(includePolicyIds);
    includeAcl = getDefaultFalse(includeAcl);
    maxItems = getMaxItems(maxItems);

    try {
      return fService.getContentChanges(context, repositoryId, changeLogToken, includeProperties,
          filter, includePolicyIds, includeAcl, maxItems, extension, objectInfos);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.apache.opencmis.server.spi.CmisDiscoveryService#query(org.apache.opencmis.server.spi.
   * CallContext, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean,
   * org.apache.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.math.BigInteger,
   * java.math.BigInteger, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public ObjectList query(CallContext context, String repositoryId, String statement,
      Boolean searchAllVersions, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems,
      BigInteger skipCount, ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkQueryStatement(statement);
    searchAllVersions = getDefaultFalse(searchAllVersions);
    includeAllowableActions = getDefaultFalse(includeAllowableActions);
    includeRelationships = getDefault(includeRelationships);
    renditionFilter = getDefaultRenditionFilter(renditionFilter);
    maxItems = getMaxItems(maxItems);
    skipCount = getSkipCount(skipCount);

    try {
      return fService.query(context, repositoryId, statement, searchAllVersions,
          includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount,
          extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }
}
