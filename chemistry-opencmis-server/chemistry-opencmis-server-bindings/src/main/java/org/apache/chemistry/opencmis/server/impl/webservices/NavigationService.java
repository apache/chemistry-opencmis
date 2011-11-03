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
import javax.xml.ws.soap.MTOM;

import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
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
import org.apache.chemistry.opencmis.commons.server.CmisService;

/**
 * CMIS Navigation Service.
 */
@MTOM
@WebService(endpointInterface = "org.apache.chemistry.opencmis.commons.impl.jaxb.NavigationServicePort")
public class NavigationService extends AbstractService implements NavigationServicePort {
    @Resource
    public WebServiceContext wsContext;

    public CmisObjectListType getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            return convert(service.getCheckedOutDocs(repositoryId, folderId, filter, orderBy, includeAllowableActions,
                    convert(IncludeRelationships.class, includeRelationships), renditionFilter, maxItems, skipCount,
                    convert(extension)));
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public CmisObjectInFolderListType getChildren(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, CmisExtensionType extension)
            throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            return convert(service.getChildren(repositoryId, folderId, filter, orderBy, includeAllowableActions,
                    convert(IncludeRelationships.class, includeRelationships), renditionFilter, includePathSegment,
                    maxItems, skipCount, convert(extension)));
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public List<CmisObjectInFolderContainerType> getDescendants(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            List<CmisObjectInFolderContainerType> result = new ArrayList<CmisObjectInFolderContainerType>();

            List<ObjectInFolderContainer> serviceResult = service.getDescendants(repositoryId, folderId, depth, filter,
                    includeAllowableActions, convert(IncludeRelationships.class, includeRelationships),
                    renditionFilter, includePathSegment, convert(extension));

            if (serviceResult != null) {
                for (ObjectInFolderContainer container : serviceResult) {
                    result.add(convert(container));
                }
            }

            return result;
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public CmisObjectType getFolderParent(String repositoryId, String folderId, String filter,
            CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            return convert(service.getFolderParent(repositoryId, folderId, filter, convert(extension)));
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public List<CmisObjectInFolderContainerType> getFolderTree(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            List<CmisObjectInFolderContainerType> result = new ArrayList<CmisObjectInFolderContainerType>();

            List<ObjectInFolderContainer> serviceResult = service.getFolderTree(repositoryId, folderId, depth, filter,
                    includeAllowableActions, convert(IncludeRelationships.class, includeRelationships),
                    renditionFilter, includePathSegment, convert(extension));

            if (serviceResult != null) {
                for (ObjectInFolderContainer container : serviceResult) {
                    result.add(convert(container));
                }
            }

            return result;
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public List<CmisObjectParentsType> getObjectParents(String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            List<CmisObjectParentsType> result = new ArrayList<CmisObjectParentsType>();

            List<ObjectParentData> serviceResult = service.getObjectParents(repositoryId, objectId, filter,
                    includeAllowableActions, convert(IncludeRelationships.class, includeRelationships),
                    renditionFilter, includeRelativePathSegment, convert(extension));

            if (serviceResult != null) {
                for (ObjectParentData parent : serviceResult) {
                    result.add(convert(parent));
                }
            }

            return result;
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }
}
