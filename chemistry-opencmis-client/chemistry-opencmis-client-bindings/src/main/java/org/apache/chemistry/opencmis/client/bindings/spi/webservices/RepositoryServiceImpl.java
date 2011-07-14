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
package org.apache.chemistry.opencmis.client.bindings.spi.webservices;

import static org.apache.chemistry.opencmis.commons.impl.Converter.convert;
import static org.apache.chemistry.opencmis.commons.impl.Converter.convertTypeContainerList;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisRepositoryEntryType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisRepositoryInfoType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.RepositoryServicePort;
import org.apache.chemistry.opencmis.commons.spi.RepositoryService;

/**
 * Repository Service Web Services client.
 */
public class RepositoryServiceImpl extends AbstractWebServicesService implements RepositoryService {

    private final AbstractPortProvider portProvider;

    /**
     * Constructor.
     */
    public RepositoryServiceImpl(BindingSession session, AbstractPortProvider portProvider) {
        setSession(session);
        this.portProvider = portProvider;
    }

    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
        RepositoryServicePort port = portProvider.getRepositoryServicePort();

        List<RepositoryInfo> infos = null;
        try {
            // get the list of repositories
            List<CmisRepositoryEntryType> entries = port.getRepositories(convert(extension));

            if (entries != null) {
                infos = new ArrayList<RepositoryInfo>();

                // iterate through the list and fetch repository infos
                for (CmisRepositoryEntryType entry : entries) {
                    CmisRepositoryInfoType info = port.getRepositoryInfo(entry.getRepositoryId(), null);
                    infos.add(convert(info));
                }
            }
        } catch (CmisException e) {
            throw convertException(e);
        } catch (Exception e) {
            throw new CmisRuntimeException("Error: " + e.getMessage(), e);
        } finally {
            portProvider.endCall(port);
        }

        return infos;
    }

    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
        RepositoryServicePort port = portProvider.getRepositoryServicePort();

        try {
            return convert(port.getRepositoryInfo(repositoryId, convert(extension)));
        } catch (CmisException e) {
            throw convertException(e);
        } catch (Exception e) {
            throw new CmisRuntimeException("Error: " + e.getMessage(), e);
        } finally {
            portProvider.endCall(port);
        }
    }

    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
        RepositoryServicePort port = portProvider.getRepositoryServicePort();

        try {
            return convert(port.getTypeDefinition(repositoryId, typeId, convert(extension)));
        } catch (CmisException e) {
            throw convertException(e);
        } catch (Exception e) {
            throw new CmisRuntimeException("Error: " + e.getMessage(), e);
        } finally {
            portProvider.endCall(port);
        }
    }

    public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        RepositoryServicePort port = portProvider.getRepositoryServicePort();

        try {
            return convert(port.getTypeChildren(repositoryId, typeId, includePropertyDefinitions, maxItems, skipCount,
                    convert(extension)));
        } catch (CmisException e) {
            throw convertException(e);
        } catch (Exception e) {
            throw new CmisRuntimeException("Error: " + e.getMessage(), e);
        } finally {
            portProvider.endCall(port);
        }
    }

    public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension) {
        RepositoryServicePort port = portProvider.getRepositoryServicePort();

        try {
            return convertTypeContainerList(port.getTypeDescendants(repositoryId, typeId, depth,
                    includePropertyDefinitions, convert(extension)));
        } catch (CmisException e) {
            throw convertException(e);
        } catch (Exception e) {
            throw new CmisRuntimeException("Error: " + e.getMessage(), e);
        } finally {
            portProvider.endCall(port);
        }
    }
}
