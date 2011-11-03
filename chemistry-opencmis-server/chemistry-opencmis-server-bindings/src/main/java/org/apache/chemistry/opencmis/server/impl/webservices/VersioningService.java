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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.MTOM;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisAccessControlListType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisContentStreamType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisPropertiesType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumIncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.jaxb.VersioningServicePort;
import org.apache.chemistry.opencmis.commons.server.CmisService;

import com.sun.xml.ws.developer.StreamingAttachment;

/**
 * CMIS Versioning Service.
 */
@MTOM
@StreamingAttachment(parseEagerly = true, memoryThreshold = 4 * 1024 * 1204)
@WebService(endpointInterface = "org.apache.chemistry.opencmis.commons.impl.jaxb.VersioningServicePort")
public class VersioningService extends AbstractService implements VersioningServicePort {
    @Resource
    public WebServiceContext wsContext;

    public void cancelCheckOut(String repositoryId, String objectId, Holder<CmisExtensionType> extension)
            throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            ExtensionsData extData = convertExtensionHolder(extension);

            service.cancelCheckOut(repositoryId, objectId, extData);

            setExtensionValues(extData, extension);
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, CmisPropertiesType properties,
            CmisContentStreamType contentStream, String checkinComment, List<String> policies,
            CmisAccessControlListType addAces, CmisAccessControlListType removeAces, Holder<CmisExtensionType> extension)
            throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            org.apache.chemistry.opencmis.commons.spi.Holder<String> objectIdHolder = convertHolder(objectId);
            ExtensionsData extData = convertExtensionHolder(extension);

            service.checkIn(repositoryId, objectIdHolder, major, convert(properties), convert(contentStream),
                    checkinComment, policies, convert(addAces, null), convert(removeAces, null), extData);

            setHolderValue(objectIdHolder, objectId);
            setExtensionValues(extData, extension);
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public void checkOut(String repositoryId, Holder<String> objectId, Holder<CmisExtensionType> extension,
            Holder<Boolean> contentCopied) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            org.apache.chemistry.opencmis.commons.spi.Holder<String> objectIdHolder = convertHolder(objectId);
            org.apache.chemistry.opencmis.commons.spi.Holder<Boolean> contentCopiedHolder = new org.apache.chemistry.opencmis.commons.spi.Holder<Boolean>();
            ExtensionsData extData = convertExtensionHolder(extension);

            service.checkOut(repositoryId, objectIdHolder, extData, contentCopiedHolder);

            if (contentCopied != null) {
                contentCopied.value = contentCopiedHolder.getValue();
            }

            setHolderValue(objectIdHolder, objectId);
            setExtensionValues(extData, extension);
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public List<CmisObjectType> getAllVersions(String repositoryId, String versionSeriesId, String filter,
            Boolean includeAllowableActions, CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            List<ObjectData> versions = service.getAllVersions(repositoryId, null, versionSeriesId, filter,
                    includeAllowableActions, convert(extension));

            if (versions == null) {
                return null;
            }

            List<CmisObjectType> result = new ArrayList<CmisObjectType>();
            for (ObjectData object : versions) {
                result.add(convert(object));
            }

            return result;
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public CmisObjectType getObjectOfLatestVersion(String repositoryId, String versionSeriesId, Boolean major,
            String filter, Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, CmisExtensionType extension)
            throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            return convert(service.getObjectOfLatestVersion(repositoryId, null, versionSeriesId, major, filter,
                    includeAllowableActions, convert(IncludeRelationships.class, includeRelationships),
                    renditionFilter, includePolicyIds, includeAcl, convert(extension)));
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public CmisPropertiesType getPropertiesOfLatestVersion(String repositoryId, String versionSeriesId, Boolean major,
            String filter, CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            return convert(service.getPropertiesOfLatestVersion(repositoryId, null, versionSeriesId, major, filter,
                    convert(extension)));
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }
}