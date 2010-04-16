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
package org.apache.chemistry.opencmis.server.spi;

import java.math.BigInteger;
import java.util.List;

import org.apache.chemistry.opencmis.commons.api.ExtensionsData;
import org.apache.chemistry.opencmis.commons.api.ObjectData;
import org.apache.chemistry.opencmis.commons.api.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.api.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.api.ObjectList;
import org.apache.chemistry.opencmis.commons.api.ObjectParentData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;

/**
 * CMIS Navigation Service interface. Please refer to the CMIS specification and
 * the OpenCMIS documentation for details.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public interface CmisNavigationService {

	/**
	 * Get the descendants on a folder.
	 * 
	 * <p>
	 * Bindings: AtomPub, Web Services
	 * </p>
	 */
	List<ObjectInFolderContainer> getDescendants(CallContext context, String repositoryId, String folderId,
			BigInteger depth, String filter, Boolean includeAllowableActions,
			IncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegment,
			ExtensionsData extension, ObjectInfoHolder objectInfos);

	/**
	 * Get the folder tree on a folder.
	 * 
	 * <p>
	 * Bindings: AtomPub, Web Services
	 * </p>
	 */
	List<ObjectInFolderContainer> getFolderTree(CallContext context, String repositoryId, String folderId,
			BigInteger depth, String filter, Boolean includeAllowableActions,
			IncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegment,
			ExtensionsData extension, ObjectInfoHolder objectInfos);

	/**
	 * Get the children on a folder.
	 * 
	 * <p>
	 * Bindings: AtomPub, Web Services
	 * </p>
	 */
	ObjectInFolderList getChildren(CallContext context, String repositoryId, String folderId, String filter,
			String orderBy, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
			String renditionFilter, Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount,
			ExtensionsData extension, ObjectInfoHolder objectInfos);

	/**
	 * Gets the parent on a folder.
	 * 
	 * <p>
	 * Bindings: AtomPub, Web Services
	 * </p>
	 */
	ObjectData getFolderParent(CallContext context, String repositoryId, String folderId, String filter,
			ExtensionsData extension, ObjectInfoHolder objectInfos);

	/**
	 * Gets the parents on an object.
	 * 
	 * <p>
	 * Bindings: AtomPub, Web Services
	 * </p>
	 */
	List<ObjectParentData> getObjectParents(CallContext context, String repositoryId, String objectId, String filter,
			Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
			Boolean includeRelativePathSegment, ExtensionsData extension, ObjectInfoHolder objectInfos);

	/**
	 * Gets the the list of checked out documents.
	 * 
	 * <p>
	 * Bindings: AtomPub, Web Services
	 * </p>
	 */
	ObjectList getCheckedOutDocs(CallContext context, String repositoryId, String folderId, String filter,
			String orderBy, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
			String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension,
			ObjectInfoHolder objectInfos);
}
