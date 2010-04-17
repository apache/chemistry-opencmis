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
package org.apache.chemistry.opencmis.server.impl.webservices;

import static org.apache.chemistry.opencmis.commons.impl.Converter.convert;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.apache.chemistry.opencmis.commons.api.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.api.ObjectParentData;
import org.apache.chemistry.opencmis.commons.api.server.CallContext;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectInFolderContainerType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectInFolderListType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectListType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectParentsType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumIncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.jaxb.NavigationServicePort;
import org.apache.chemistry.opencmis.server.spi.AbstractServicesFactory;
import org.apache.chemistry.opencmis.server.spi.CmisNavigationService;

/**
 * CMIS Navigation Service.
 */
@WebService(endpointInterface = "org.apache.chemistry.opencmis.commons.impl.jaxb.NavigationServicePort")
public class NavigationService extends AbstractService implements NavigationServicePort {
	@Resource
	WebServiceContext wsContext;

	public CmisObjectListType getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
			Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships, String renditionFilter,
			BigInteger maxItems, BigInteger skipCount, CmisExtensionType extension) throws CmisException {
		try {
			AbstractServicesFactory factory = getServicesFactory(wsContext);
			CmisNavigationService service = factory.getNavigationService();
			CallContext context = createContext(wsContext, repositoryId);

			return convert(service.getCheckedOutDocs(context, repositoryId, folderId, filter, orderBy,
					includeAllowableActions, convert(IncludeRelationships.class, includeRelationships),
					renditionFilter, maxItems, skipCount, convert(extension), null));
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	public CmisObjectInFolderListType getChildren(String repositoryId, String folderId, String filter, String orderBy,
			Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships, String renditionFilter,
			Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, CmisExtensionType extension)
			throws CmisException {
		try {
			AbstractServicesFactory factory = getServicesFactory(wsContext);
			CmisNavigationService service = factory.getNavigationService();
			CallContext context = createContext(wsContext, repositoryId);

			return convert(service.getChildren(context, repositoryId, folderId, filter, orderBy,
					includeAllowableActions, convert(IncludeRelationships.class, includeRelationships),
					renditionFilter, includePathSegment, maxItems, skipCount, convert(extension), null));
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	public List<CmisObjectInFolderContainerType> getDescendants(String repositoryId, String folderId, BigInteger depth,
			String filter, Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships,
			String renditionFilter, Boolean includePathSegment, CmisExtensionType extension) throws CmisException {
		try {
			AbstractServicesFactory factory = getServicesFactory(wsContext);
			CmisNavigationService service = factory.getNavigationService();
			CallContext context = createContext(wsContext, repositoryId);

			List<CmisObjectInFolderContainerType> result = new ArrayList<CmisObjectInFolderContainerType>();

			List<ObjectInFolderContainer> serviceResult = service.getDescendants(context, repositoryId, folderId,
					depth, filter, includeAllowableActions, convert(IncludeRelationships.class, includeRelationships),
					renditionFilter, includePathSegment, convert(extension), null);

			if (serviceResult != null) {
				for (ObjectInFolderContainer container : serviceResult) {
					result.add(convert(container));
				}
			}

			return result;
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	public CmisObjectType getFolderParent(String repositoryId, String folderId, String filter,
			CmisExtensionType extension) throws CmisException {
		try {
			AbstractServicesFactory factory = getServicesFactory(wsContext);
			CmisNavigationService service = factory.getNavigationService();
			CallContext context = createContext(wsContext, repositoryId);

			return convert(service.getFolderParent(context, repositoryId, folderId, filter, convert(extension), null));
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	public List<CmisObjectInFolderContainerType> getFolderTree(String repositoryId, String folderId, BigInteger depth,
			String filter, Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships,
			String renditionFilter, Boolean includePathSegment, CmisExtensionType extension) throws CmisException {
		try {
			AbstractServicesFactory factory = getServicesFactory(wsContext);
			CmisNavigationService service = factory.getNavigationService();
			CallContext context = createContext(wsContext, repositoryId);

			List<CmisObjectInFolderContainerType> result = new ArrayList<CmisObjectInFolderContainerType>();

			List<ObjectInFolderContainer> serviceResult = service.getFolderTree(context, repositoryId, folderId, depth,
					filter, includeAllowableActions, convert(IncludeRelationships.class, includeRelationships),
					renditionFilter, includePathSegment, convert(extension), null);

			if (serviceResult != null) {
				for (ObjectInFolderContainer container : serviceResult) {
					result.add(convert(container));
				}
			}

			return result;
		} catch (Exception e) {
			throw convertException(e);
		}
	}

	public List<CmisObjectParentsType> getObjectParents(String repositoryId, String objectId, String filter,
			Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships, String renditionFilter,
			Boolean includeRelativePathSegment, CmisExtensionType extension) throws CmisException {
		try {
			AbstractServicesFactory factory = getServicesFactory(wsContext);
			CmisNavigationService service = factory.getNavigationService();
			CallContext context = createContext(wsContext, repositoryId);

			List<CmisObjectParentsType> result = new ArrayList<CmisObjectParentsType>();

			List<ObjectParentData> serviceResult = service.getObjectParents(context, repositoryId, objectId, filter,
					includeAllowableActions, convert(IncludeRelationships.class, includeRelationships),
					renditionFilter, includeRelativePathSegment, convert(extension), null);

			if (serviceResult != null) {
				for (ObjectParentData parent : serviceResult) {
					result.add(convert(parent));
				}
			}

			return result;
		} catch (Exception e) {
			throw convertException(e);
		}
	}
}
