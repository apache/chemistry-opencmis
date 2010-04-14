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
package org.apache.opencmis.inmemory.clientprovider;

import java.math.BigInteger;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.provider.DiscoveryService;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.inmemory.server.InMemoryDiscoveryServiceImpl;

public class DiscoveryServiceImpl  extends AbstractService implements DiscoveryService {

  private InMemoryDiscoveryServiceImpl fDiscoverySvc;

  public DiscoveryServiceImpl(InMemoryDiscoveryServiceImpl disSvc) {
    fDiscoverySvc = disSvc;
  }

  public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken,
      Boolean includeProperties, String filter, Boolean includePolicyIds, Boolean includeAcl,
      BigInteger maxItems, ExtensionsData extension) {

    return fDiscoverySvc.getContentChanges(fDummyCallContext, repositoryId, changeLogToken,
        includeProperties, filter, includePolicyIds, includeAcl, maxItems, extension, null);
  }

  public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {

    return fDiscoverySvc.query(fDummyCallContext, repositoryId, statement, searchAllVersions,
        includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount,
        extension);
  }

}
