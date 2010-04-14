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
import org.apache.opencmis.commons.enums.RelationshipDirection;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisRelationshipService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

/**
 * Relationship service wrapper.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RelationshipServiceWrapper extends AbstractServiceWrapper implements
    CmisRelationshipService {

  private CmisRelationshipService fService;

  /**
   * Constructor.
   * 
   * @param service
   *          the real service object
   * @param defaultMaxItems
   *          default value for <code>maxItems</code> parameters
   */
  public RelationshipServiceWrapper(CmisRelationshipService service, BigInteger defaultMaxItems) {
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
   * org.apache.opencmis.server.spi.CmisRelationshipService#getObjectRelationships(org.apache.opencmis
   * .server.spi.CallContext, java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.enums.RelationshipDirection, java.lang.String, java.lang.String,
   * java.lang.Boolean, java.math.BigInteger, java.math.BigInteger,
   * org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectList getObjectRelationships(CallContext context, String repositoryId,
      String objectId, Boolean includeSubRelationshipTypes,
      RelationshipDirection relationshipDirection, String typeId, String filter,
      Boolean includeAllowableActions, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension, ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    checkId("Object Id", objectId);
    includeSubRelationshipTypes = getDefaultFalse(includeSubRelationshipTypes);
    relationshipDirection = getDefault(relationshipDirection);
    includeAllowableActions = getDefaultFalse(includeAllowableActions);
    maxItems = getMaxItems(maxItems);
    skipCount = getSkipCount(skipCount);

    try {
      return fService.getObjectRelationships(context, repositoryId, objectId,
          includeSubRelationshipTypes, relationshipDirection, typeId, filter,
          includeAllowableActions, maxItems, skipCount, extension, objectInfos);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

}
