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
package org.apache.opencmis.client.provider.spi.webservices;

import static org.apache.opencmis.commons.impl.Converter.convert;
import static org.apache.opencmis.commons.impl.Converter.convertTypeContainerList;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionList;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.jaxb.CmisException;
import org.apache.opencmis.commons.impl.jaxb.CmisRepositoryEntryType;
import org.apache.opencmis.commons.impl.jaxb.CmisRepositoryInfoType;
import org.apache.opencmis.commons.impl.jaxb.RepositoryServicePort;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.commons.provider.RepositoryService;

/**
 * Repository Service Web Services client.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RepositoryServiceImpl extends AbstractWebServicesService implements RepositoryService {

  private PortProvider fPortProvider;

  /**
   * Constructor.
   */
  public RepositoryServiceImpl(Session session, PortProvider portProvider) {
    setSession(session);
    fPortProvider = portProvider;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.RepositoryService#getRepositoryInfos(org.apache.opencmis.client.provider
   * .ExtensionsData)
   */
  public List<RepositoryInfoData> getRepositoryInfos(ExtensionsData extension) {
    RepositoryServicePort port = fPortProvider.getRepositoryServicePort();

    List<RepositoryInfoData> infos = null;
    try {
      // get the list of repositories
      List<CmisRepositoryEntryType> entries = port.getRepositories(convert(extension));

      if (entries != null) {
        infos = new ArrayList<RepositoryInfoData>();

        // iterate through the list and fetch repository infos
        for (CmisRepositoryEntryType entry : entries) {
          CmisRepositoryInfoType info = port.getRepositoryInfo(entry.getRepositoryId(), null);
          infos.add(convert(info));
        }
      }
    }
    catch (CmisException e) {
      throw convertException(e);
    }
    catch (Exception e) {
      throw new CmisRuntimeException("Error: " + e.getMessage(), e);
    }

    return infos;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getRepositoryInfo(java.lang.String,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public RepositoryInfoData getRepositoryInfo(String repositoryId, ExtensionsData extension) {
    RepositoryServicePort port = fPortProvider.getRepositoryServicePort();

    try {
      return convert(port.getRepositoryInfo(repositoryId, convert(extension)));
    }
    catch (CmisException e) {
      throw convertException(e);
    }
    catch (Exception e) {
      throw new CmisRuntimeException("Error: " + e.getMessage(), e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getTypeDefinition(java.lang.String,
   * java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public TypeDefinition getTypeDefinition(String repositoryId, String typeId,
      ExtensionsData extension) {
    RepositoryServicePort port = fPortProvider.getRepositoryServicePort();

    try {
      return convert(port.getTypeDefinition(repositoryId, typeId, convert(extension)));
    }
    catch (CmisException e) {
      throw convertException(e);
    }
    catch (Exception e) {
      throw new CmisRuntimeException("Error: " + e.getMessage(), e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getTypeChildren(java.lang.String,
   * java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public TypeDefinitionList getTypeChildren(String repositoryId, String typeId,
      Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension) {
    RepositoryServicePort port = fPortProvider.getRepositoryServicePort();

    try {
      return convert(port.getTypeChildren(repositoryId, typeId, includePropertyDefinitions,
          maxItems, skipCount, convert(extension)));
    }
    catch (CmisException e) {
      throw convertException(e);
    }
    catch (Exception e) {
      throw new CmisRuntimeException("Error: " + e.getMessage(), e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getTypeDescendants(java.lang.String,
   * java.lang.String, java.math.BigInteger, java.lang.Boolean,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId,
      BigInteger depth, Boolean includePropertyDefinitions, ExtensionsData extension) {
    RepositoryServicePort port = fPortProvider.getRepositoryServicePort();

    try {
      return convertTypeContainerList(port.getTypeDescendants(repositoryId, typeId, depth,
          includePropertyDefinitions, convert(extension)));
    }
    catch (CmisException e) {
      throw convertException(e);
    }
    catch (Exception e) {
      throw new CmisRuntimeException("Error: " + e.getMessage(), e);
    }
  }

}
