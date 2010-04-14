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
import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.provider.NavigationService;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectInFolderContainer;
import org.apache.opencmis.commons.provider.ObjectInFolderList;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.commons.provider.ObjectParentData;
import org.apache.opencmis.inmemory.server.InMemoryNavigationServiceImpl;

/**
 * Navigation Service interface. See CMIS 1.0 domain model for details.
 * 
 * @author Jens
 */

public class NavigationServiceImpl extends AbstractService implements NavigationService {
  private InMemoryNavigationServiceImpl fNavSvc;

  public NavigationServiceImpl(InMemoryNavigationServiceImpl navSvc) {
    fNavSvc = navSvc;
  }

  public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter,
      String orderBy, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {

    return fNavSvc.getCheckedOutDocs(fDummyCallContext, repositoryId, folderId, filter, orderBy,
        includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount,
        extension, null);
  }

  public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter,
      String orderBy, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePathSegment, BigInteger maxItems,
      BigInteger skipCount, ExtensionsData extension) {

    return fNavSvc.getChildren(fDummyCallContext, repositoryId, folderId, filter, orderBy,
        includeAllowableActions, includeRelationships, renditionFilter, includePathSegment,
        maxItems, skipCount, extension, null);
  }

  public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId,
      BigInteger depth, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter,
      Boolean includePathSegment, ExtensionsData extension) {

    return fNavSvc.getDescendants(fDummyCallContext, repositoryId, folderId, depth, filter,
        includeAllowableActions, includeRelationships, renditionFilter, includePathSegment,
        extension, null);
  }

  public ObjectData getFolderParent(String repositoryId, String folderId, String filter,
      ExtensionsData extension) {

    return fNavSvc.getFolderParent(fDummyCallContext, repositoryId, folderId, filter, extension,
        null);
  }

  public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId,
      BigInteger depth, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter,
      Boolean includePathSegment, ExtensionsData extension) {

    return fNavSvc.getDescendants(fDummyCallContext, repositoryId, folderId, depth, filter,
        includeAllowableActions, includeRelationships, renditionFilter, includePathSegment,
        extension, null);
  }

  public List<ObjectParentData> getObjectParents(String repositoryId, String objectId,
      String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includeRelativePathSegment, ExtensionsData extension) {

    return fNavSvc.getObjectParents(fDummyCallContext, repositoryId, objectId, filter,
        includeAllowableActions, includeRelationships, renditionFilter, includeRelativePathSegment,
        extension, null);
  }
}
