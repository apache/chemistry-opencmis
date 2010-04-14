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
package org.apache.opencmis.server.impl.dummy;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.api.TypeDefinitionList;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.impl.dataobjects.RepositoryInfoDataImpl;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisRepositoryService;

/**
 * Simplest Repository Service implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class DummyRepositoryService implements CmisRepositoryService {

  private RepositoryInfoDataImpl fRepInfo;

  public DummyRepositoryService(String id, String name) {
    fRepInfo = new RepositoryInfoDataImpl();

    fRepInfo.setRepositoryId(id);
    fRepInfo.setRepositoryName(name);
    fRepInfo.setRepositoryDescription(name);
    fRepInfo.setCmisVersionSupported("1.0");
    fRepInfo.setRootFolder("root");

    fRepInfo.setVendorName("OpenCMIS");
    fRepInfo.setProductName("OpenCMIS Server");
    fRepInfo.setProductVersion("1.0");
  }

  public RepositoryInfoData getRepositoryInfo(CallContext context, String repositoryId,
      ExtensionsData extension) {

    if (!fRepInfo.getRepositoryId().equals(repositoryId)) {
      throw new CmisObjectNotFoundException("A repository with repository id '" + repositoryId
          + "' does not exist!");
    }

    return fRepInfo;
  }

  public List<RepositoryInfoData> getRepositoryInfos(CallContext context, ExtensionsData extension) {
    return Collections.singletonList((RepositoryInfoData) fRepInfo);
  }

  public TypeDefinitionList getTypeChildren(CallContext context, String repositoryId,
      String typeId, Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension) {
    throw new CmisNotSupportedException();
  }

  public TypeDefinition getTypeDefinition(CallContext context, String repositoryId, String typeId,
      ExtensionsData extension) {
    throw new CmisNotSupportedException();
  }

  public List<TypeDefinitionContainer> getTypeDescendants(CallContext context, String repositoryId,
      String typeId, BigInteger depth, Boolean includePropertyDefinitions, ExtensionsData extension) {
    throw new CmisNotSupportedException();
  }

}
