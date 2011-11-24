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
package org.apache.chemistry.opencmis.client.bindings.spi.browser;

import java.math.BigInteger;
import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.spi.RepositoryService;

/**
 * Repository Service Browser Binding client.
 */
public class RepositoryServiceImpl extends AbstractBrowserBindingService implements RepositoryService {

    /**
     * Constructor.
     */
    public RepositoryServiceImpl(BindingSession session) {
        setSession(session);
    }

    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }
}
