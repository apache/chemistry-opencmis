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
package org.apache.chemistry.opencmis.server.impl.dummy;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.commons.api.ExtensionsData;
import org.apache.chemistry.opencmis.commons.api.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.api.TypeDefinition;
import org.apache.chemistry.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.api.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.api.server.CallContext;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;
import org.apache.chemistry.opencmis.server.spi.CmisRepositoryService;

/**
 * Simplest Repository Service implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class DummyRepositoryService implements CmisRepositoryService {

    private RepositoryInfoImpl fRepInfo;

    public DummyRepositoryService(String id, String name) {
        fRepInfo = new RepositoryInfoImpl();

        fRepInfo.setRepositoryId(id);
        fRepInfo.setRepositoryName(name);
        fRepInfo.setRepositoryDescription(name);
        fRepInfo.setCmisVersionSupported("1.0");
        fRepInfo.setRootFolder("root");

        fRepInfo.setVendorName("OpenCMIS");
        fRepInfo.setProductName("OpenCMIS Server");
        fRepInfo.setProductVersion("1.0");
    }

    public RepositoryInfo getRepositoryInfo(CallContext context, String repositoryId, ExtensionsData extension) {

        if (!fRepInfo.getId().equals(repositoryId)) {
            throw new CmisObjectNotFoundException("A repository with repository id '" + repositoryId
                    + "' does not exist!");
        }

        return fRepInfo;
    }

    public List<RepositoryInfo> getRepositoryInfos(CallContext context, ExtensionsData extension) {
        return Collections.singletonList((RepositoryInfo) fRepInfo);
    }

    public TypeDefinitionList getTypeChildren(CallContext context, String repositoryId, String typeId,
            Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        throw new CmisNotSupportedException();
    }

    public TypeDefinition getTypeDefinition(CallContext context, String repositoryId, String typeId,
            ExtensionsData extension) {
        throw new CmisNotSupportedException();
    }

    public List<TypeDefinitionContainer> getTypeDescendants(CallContext context, String repositoryId, String typeId,
            BigInteger depth, Boolean includePropertyDefinitions, ExtensionsData extension) {
        throw new CmisNotSupportedException();
    }

}
