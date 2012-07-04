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
import static org.apache.chemistry.opencmis.commons.impl.Converter.convertExtensionHolder;
import static org.apache.chemistry.opencmis.commons.impl.Converter.convertHolder;
import static org.apache.chemistry.opencmis.commons.impl.Converter.setExtensionValues;
import static org.apache.chemistry.opencmis.commons.impl.Converter.setHolderValue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.MTOM;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisAccessControlListType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisAllowableActionsType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisContentStreamType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisPropertiesType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisRenditionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.DeleteTreeResponse.FailedToDelete;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumIncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumUnfileObject;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumVersioningState;
import org.apache.chemistry.opencmis.commons.impl.jaxb.ObjectServicePort;
import org.apache.chemistry.opencmis.commons.server.CmisService;

import com.sun.xml.ws.developer.StreamingAttachment;

/**
 * CMIS Object Service.
 */
@MTOM
@StreamingAttachment(parseEagerly = true, memoryThreshold = 4 * 1024 * 1204)
@WebService(endpointInterface = "org.apache.chemistry.opencmis.commons.impl.jaxb.ObjectServicePort")
public class ObjectService extends AbstractService implements ObjectServicePort {
    @Resource
    public WebServiceContext wsContext;

    public void createDocument(String repositoryId, CmisPropertiesType properties, String folderId,
            CmisContentStreamType contentStream, EnumVersioningState versioningState, List<String> policies,
            CmisAccessControlListType addAces, CmisAccessControlListType removeAces,
            Holder<CmisExtensionType> extension, Holder<String> objectId) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            ExtensionsData extData = convertExtensionHolder(extension);

            String id = service.createDocument(repositoryId, convert(properties), folderId, convert(contentStream),
                    convert(VersioningState.class, versioningState), policies, convert(addAces, null),
                    convert(removeAces, null), extData);

            if (objectId != null) {
                objectId.value = id;
            }

            setExtensionValues(extData, extension);
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public void createDocumentFromSource(String repositoryId, String sourceId, CmisPropertiesType properties,
            String folderId, EnumVersioningState versioningState, List<String> policies,
            CmisAccessControlListType addAces, CmisAccessControlListType removeAces,
            Holder<CmisExtensionType> extension, Holder<String> objectId) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            ExtensionsData extData = convertExtensionHolder(extension);

            String id = service.createDocumentFromSource(repositoryId, sourceId, convert(properties), folderId,
                    convert(VersioningState.class, versioningState), policies, convert(addAces, null),
                    convert(removeAces, null), extData);

            if (objectId != null) {
                objectId.value = id;
            }

            setExtensionValues(extData, extension);
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public void createFolder(String repositoryId, CmisPropertiesType properties, String folderId,
            List<String> policies, CmisAccessControlListType addAces, CmisAccessControlListType removeAces,
            Holder<CmisExtensionType> extension, Holder<String> objectId) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            ExtensionsData extData = convertExtensionHolder(extension);

            String id = service.createFolder(repositoryId, convert(properties), folderId, policies,
                    convert(addAces, null), convert(removeAces, null), extData);

            if (objectId != null) {
                objectId.value = id;
            }

            setExtensionValues(extData, extension);
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public void createPolicy(String repositoryId, CmisPropertiesType properties, String folderId,
            List<String> policies, CmisAccessControlListType addAces, CmisAccessControlListType removeAces,
            Holder<CmisExtensionType> extension, Holder<String> objectId) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            ExtensionsData extData = convertExtensionHolder(extension);

            String id = service.createPolicy(repositoryId, convert(properties), folderId, policies,
                    convert(addAces, null), convert(removeAces, null), extData);

            if (objectId != null) {
                objectId.value = id;
            }

            setExtensionValues(extData, extension);
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public void createRelationship(String repositoryId, CmisPropertiesType properties, List<String> policies,
            CmisAccessControlListType addAces, CmisAccessControlListType removeAces,
            Holder<CmisExtensionType> extension, Holder<String> objectId) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            ExtensionsData extData = convertExtensionHolder(extension);

            String id = service.createRelationship(repositoryId, convert(properties), policies, convert(addAces, null),
                    convert(removeAces, null), extData);

            if (objectId != null) {
                objectId.value = id;
            }

            setExtensionValues(extData, extension);
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            Holder<CmisExtensionType> extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            org.apache.chemistry.opencmis.commons.spi.Holder<String> objectIdHolder = convertHolder(objectId);
            org.apache.chemistry.opencmis.commons.spi.Holder<String> changeTokenHolder = convertHolder(changeToken);
            ExtensionsData extData = convertExtensionHolder(extension);

            service.deleteContentStream(repositoryId, objectIdHolder, changeTokenHolder, extData);

            setHolderValue(objectIdHolder, objectId);
            setHolderValue(changeTokenHolder, changeToken);
            setExtensionValues(extData, extension);
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public void deleteObject(String repositoryId, String objectId, Boolean allVersions,
            Holder<CmisExtensionType> extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            ExtensionsData extData = convertExtensionHolder(extension);

            service.deleteObject(repositoryId, objectId, allVersions, extData);

            setExtensionValues(extData, extension);
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public FailedToDelete deleteTree(String repositoryId, String folderId, Boolean allVersions,
            EnumUnfileObject unfileObjects, Boolean continueOnFailure, CmisExtensionType extension)
            throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            return convert(service.deleteTree(repositoryId, folderId, allVersions,
                    convert(UnfileObject.class, unfileObjects), continueOnFailure, convert(extension)));
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public CmisAllowableActionsType getAllowableActions(String repositoryId, String objectId,
            CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            return convert(service.getAllowableActions(repositoryId, objectId, convert(extension)));
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public CmisContentStreamType getContentStream(String repositoryId, String objectId, String streamId,
            BigInteger offset, BigInteger length, CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            return convert(service.getContentStream(repositoryId, objectId, streamId, offset, length,
                    convert(extension)));
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public CmisObjectType getObject(String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePolicyIds, Boolean includeAcl, CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            return convert(service.getObject(repositoryId, objectId, filter, includeAllowableActions,
                    convert(IncludeRelationships.class, includeRelationships), renditionFilter, includePolicyIds,
                    includeAcl, convert(extension)));
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public CmisObjectType getObjectByPath(String repositoryId, String path, String filter,
            Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePolicyIds, Boolean includeAcl, CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            return convert(service.getObjectByPath(repositoryId, path, filter, includeAllowableActions,
                    convert(IncludeRelationships.class, includeRelationships), renditionFilter, includePolicyIds,
                    includeAcl, convert(extension)));
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public CmisPropertiesType getProperties(String repositoryId, String objectId, String filter,
            CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            return convert(service.getProperties(repositoryId, objectId, filter, convert(extension)));
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public List<CmisRenditionType> getRenditions(String repositoryId, String objectId, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            List<CmisRenditionType> result = new ArrayList<CmisRenditionType>();

            List<RenditionData> renditionList = service.getRenditions(repositoryId, objectId, renditionFilter,
                    maxItems, skipCount, convert(extension));

            if (renditionList != null) {
                for (RenditionData rendition : renditionList) {
                    result.add(convert(rendition));
                }
            }

            return result;
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
            Holder<CmisExtensionType> extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            org.apache.chemistry.opencmis.commons.spi.Holder<String> objectIdHolder = convertHolder(objectId);
            ExtensionsData extData = convertExtensionHolder(extension);

            service.moveObject(repositoryId, objectIdHolder, targetFolderId, sourceFolderId, extData);

            setHolderValue(objectIdHolder, objectId);
            setExtensionValues(extData, extension);
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
            Holder<String> changeToken, CmisContentStreamType contentStream, Holder<CmisExtensionType> extension)
            throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            org.apache.chemistry.opencmis.commons.spi.Holder<String> objectIdHolder = convertHolder(objectId);
            org.apache.chemistry.opencmis.commons.spi.Holder<String> changeTokenHolder = convertHolder(changeToken);
            ExtensionsData extData = convertExtensionHolder(extension);

            service.setContentStream(repositoryId, objectIdHolder, overwriteFlag, changeTokenHolder,
                    convert(contentStream), extData);

            setHolderValue(objectIdHolder, objectId);
            setHolderValue(changeTokenHolder, changeToken);
            setExtensionValues(extData, extension);
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            CmisPropertiesType properties, Holder<CmisExtensionType> extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            org.apache.chemistry.opencmis.commons.spi.Holder<String> objectIdHolder = convertHolder(objectId);
            org.apache.chemistry.opencmis.commons.spi.Holder<String> changeTokenHolder = convertHolder(changeToken);
            ExtensionsData extData = convertExtensionHolder(extension);

            service.updateProperties(repositoryId, objectIdHolder, changeTokenHolder, convert(properties), extData);

            setHolderValue(objectIdHolder, objectId);
            setHolderValue(changeTokenHolder, changeToken);
            setExtensionValues(extData, extension);
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }
}
