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
import static org.apache.chemistry.opencmis.commons.impl.Converter.convertTypeContainerList;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.MTOM;

import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisRepositoryEntryType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisRepositoryInfoType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeContainer;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeDefinitionListType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeDefinitionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.RepositoryServicePort;
import org.apache.chemistry.opencmis.commons.server.CmisService;

/**
 * CMIS Repository Service.
 */
@MTOM
@WebService(endpointInterface = "org.apache.chemistry.opencmis.commons.impl.jaxb.RepositoryServicePort")
public class RepositoryService extends AbstractService implements RepositoryServicePort {
    @Resource
    public WebServiceContext wsContext;

    public List<CmisRepositoryEntryType> getRepositories(CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, null);

            List<RepositoryInfo> infoDataList = service.getRepositoryInfos(convert(extension));

            if (infoDataList == null) {
                return null;
            }

            List<CmisRepositoryEntryType> result = new ArrayList<CmisRepositoryEntryType>();
            for (RepositoryInfo infoData : infoDataList) {
                CmisRepositoryEntryType entry = new CmisRepositoryEntryType();
                entry.setRepositoryId(infoData.getId());
                entry.setRepositoryName(infoData.getName());

                result.add(entry);
            }

            return result;
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public CmisRepositoryInfoType getRepositoryInfo(String repositoryId, CmisExtensionType extension)
            throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            return convert(service.getRepositoryInfo(repositoryId, convert(extension)));
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public CmisTypeDefinitionListType getTypeChildren(String repositoryId, String typeId,
            Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount, CmisExtensionType extension)
            throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            return convert(service.getTypeChildren(repositoryId, typeId, includePropertyDefinitions, maxItems,
                    skipCount, convert(extension)));
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public CmisTypeDefinitionType getTypeDefinition(String repositoryId, String typeId, CmisExtensionType extension)
            throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            return convert(service.getTypeDefinition(repositoryId, typeId, convert(extension)));
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public List<CmisTypeContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            List<CmisTypeContainer> result = new ArrayList<CmisTypeContainer>();
            convertTypeContainerList(service.getTypeDescendants(repositoryId, typeId, depth,
                    includePropertyDefinitions, convert(extension)), result);

            return result;
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }
}
