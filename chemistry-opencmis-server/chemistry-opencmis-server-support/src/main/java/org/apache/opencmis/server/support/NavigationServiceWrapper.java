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
import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectInFolderContainer;
import org.apache.opencmis.commons.provider.ObjectInFolderList;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.commons.provider.ObjectParentData;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisNavigationService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

/**
 * Navigation service wrapper.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class NavigationServiceWrapper extends AbstractServiceWrapper implements
    CmisNavigationService {

  private CmisNavigationService fService;

  /**
   * Constructor.
   * 
   * @param service
   *          the real service object
   * @param defaultMaxItems
   *          default value for <code>maxItems</code> parameters
   * @param defaultDepth
   *          default value for <code>depth</code> parameters
   */
  public NavigationServiceWrapper(CmisNavigationService service, BigInteger defaultMaxItems,
      BigInteger defaultDepth) {
    if (service == null) {
      throw new IllegalArgumentException("Service must be set!");
    }

    fService = service;
    setDefaultMaxItems(defaultMaxItems);
    setDefaultDepth(defaultDepth);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisNavigationService#getCheckedOutDocs(org.apache.opencmis.
   * server.spi.CallContext, java.lang.String, java.lang.String, java.lang.String, java.lang.String,
   * java.lang.Boolean, org.apache.opencmis.commons.enums.IncludeRelationships, java.lang.String,
   * java.math.BigInteger, java.math.BigInteger, org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectList getCheckedOutDocs(CallContext context, String repositoryId, String folderId,
      String filter, String orderBy, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems,
      BigInteger skipCount, ExtensionsData extension, ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    includeAllowableActions = getDefaultFalse(includeAllowableActions);
    includeRelationships = getDefault(includeRelationships);
    renditionFilter = getDefaultRenditionFilter(renditionFilter);
    maxItems = getMaxItems(maxItems);
    skipCount = getSkipCount(skipCount);

    try {
      return fService.getCheckedOutDocs(context, repositoryId, folderId, filter, orderBy,
          includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount,
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
   * org.apache.opencmis.server.spi.CmisNavigationService#getChildren(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, java.lang.String, java.lang.String, java.lang.String,
   * java.lang.Boolean, org.apache.opencmis.commons.enums.IncludeRelationships, java.lang.String,
   * java.lang.Boolean, java.math.BigInteger, java.math.BigInteger,
   * org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectInFolderList getChildren(CallContext context, String repositoryId, String folderId,
      String filter, String orderBy, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter,
      Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension, ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    checkId("Folder Id", folderId);
    includeAllowableActions = getDefaultFalse(includeAllowableActions);
    includeRelationships = getDefault(includeRelationships);
    renditionFilter = getDefaultRenditionFilter(renditionFilter);
    includePathSegment = getDefaultFalse(includePathSegment);
    maxItems = getMaxItems(maxItems);
    skipCount = getSkipCount(skipCount);

    try {
      return fService.getChildren(context, repositoryId, folderId, filter, orderBy,
          includeAllowableActions, includeRelationships, renditionFilter, includePathSegment,
          maxItems, skipCount, extension, objectInfos);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisNavigationService#getDescendants(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, java.lang.String, java.math.BigInteger, java.lang.String,
   * java.lang.Boolean, org.apache.opencmis.commons.enums.IncludeRelationships, java.lang.String,
   * java.lang.Boolean, org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public List<ObjectInFolderContainer> getDescendants(CallContext context, String repositoryId,
      String folderId, BigInteger depth, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter,
      Boolean includePathSegment, ExtensionsData extension, ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    checkId("Folder Id", folderId);
    depth = getDepth(depth);
    includeAllowableActions = getDefaultFalse(includeAllowableActions);
    includeRelationships = getDefault(includeRelationships);
    renditionFilter = getDefaultRenditionFilter(renditionFilter);
    includePathSegment = getDefaultFalse(includePathSegment);

    try {
      return fService.getDescendants(context, repositoryId, folderId, depth, filter,
          includeAllowableActions, includeRelationships, renditionFilter, includePathSegment,
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
   * org.apache.opencmis.server.spi.CmisNavigationService#getFolderParent(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, java.lang.String, java.lang.String,
   * org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData getFolderParent(CallContext context, String repositoryId, String folderId,
      String filter, ExtensionsData extension, ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    checkId("Folder Id", folderId);

    try {
      return fService.getFolderParent(context, repositoryId, folderId, filter, extension,
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
   * org.apache.opencmis.server.spi.CmisNavigationService#getFolderTree(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, java.lang.String, java.math.BigInteger, java.lang.String,
   * java.lang.Boolean, org.apache.opencmis.commons.enums.IncludeRelationships, java.lang.String,
   * java.lang.Boolean, org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public List<ObjectInFolderContainer> getFolderTree(CallContext context, String repositoryId,
      String folderId, BigInteger depth, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter,
      Boolean includePathSegment, ExtensionsData extension, ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    checkId("Folder Id", folderId);
    depth = getDepth(depth);
    includeAllowableActions = getDefaultFalse(includeAllowableActions);
    includeRelationships = getDefault(includeRelationships);
    renditionFilter = getDefaultRenditionFilter(renditionFilter);
    includePathSegment = getDefaultFalse(includePathSegment);

    try {
      return fService.getFolderTree(context, repositoryId, folderId, depth, filter,
          includeAllowableActions, includeRelationships, renditionFilter, includePathSegment,
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
   * org.apache.opencmis.server.spi.CmisNavigationService#getObjectParents(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public List<ObjectParentData> getObjectParents(CallContext context, String repositoryId,
      String objectId, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter,
      Boolean includeRelativePathSegment, ExtensionsData extension, ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    checkId("Object Id", objectId);
    includeAllowableActions = getDefaultFalse(includeAllowableActions);
    includeRelationships = getDefault(includeRelationships);
    renditionFilter = getDefaultRenditionFilter(renditionFilter);
    includeRelativePathSegment = getDefaultFalse(includeRelativePathSegment);

    try {
      return fService.getObjectParents(context, repositoryId, objectId, filter,
          includeAllowableActions, includeRelationships, renditionFilter,
          includeRelativePathSegment, extension, objectInfos);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

}
