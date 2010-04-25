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
package org.apache.chemistry.opencmis.commons.api;

import java.math.BigInteger;
import java.util.List;

import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;

/**
 * Navigation Service interface.
 * 
 * <p>
 * <em>
 * See CMIS 1.0 specification for details on the operations, parameters,
 * exceptions and the domain model.
 * </em>
 * </p>
 */
public interface NavigationService {

    /**
     * Gets the list of child objects contained in the specified folder.
     * 
     * @param repositoryId
     *            the identifier for the repository
     * @param maxItems
     *            <em>(optional)</em> the maximum number of items to return in a
     *            response (default is repository specific)
     * @param skipCount
     *            <em>(optional)</em> number of potential results that the
     *            repository MUST skip/page over before returning any results
     *            (default is 0)
     */
    ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension);

    /**
     * Gets the set of descendant objects contained in the specified folder or
     * any of its child folders.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, ExtensionsData extension);

    /**
     * Gets the set of descendant folder objects contained in the specified
     * folder.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, ExtensionsData extension);

    /**
     * Gets the parent folder(s) for the specified non-folder, fileable object.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, ExtensionsData extension);

    /**
     * Gets the parent folder object for the specified folder object.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension);

    /**
     * Gets the list of documents that are checked out that the user has access
     * to.
     * 
     * @param repositoryId
     *            the identifier for the repository
     * @param maxItems
     *            <em>(optional)</em> the maximum number of items to return in a
     *            response (default is repository specific)
     * @param skipCount
     *            <em>(optional)</em> number of potential results that the
     *            repository MUST skip/page over before returning any results
     *            (default is 0)
     */
    ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension);
}
