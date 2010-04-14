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
package org.apache.opencmis.commons.provider;

import java.math.BigInteger;
import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;

/**
 * Navigation Service interface. See CMIS 1.0 domain model for details.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 * @see <a href="http://www.oasis-open.org/committees/tc_home.php?wg_abbrev=cmis">OASIS CMIS
 *      Technical Committee</a>
 */
public interface NavigationService {

  List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId,
      BigInteger depth, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter,
      Boolean includePathSegment, ExtensionsData extension);

  ObjectInFolderList getChildren(String repositoryId, String folderId, String filter,
      String orderBy, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePathSegment, BigInteger maxItems,
      BigInteger skipCount, ExtensionsData extension);

  ObjectData getFolderParent(String repositoryId, String folderId, String filter,
      ExtensionsData extension);

  List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId,
      BigInteger depth, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter,
      Boolean includePathSegment, ExtensionsData extension);

  List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includeRelativePathSegment, ExtensionsData extension);

  ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension);
}
