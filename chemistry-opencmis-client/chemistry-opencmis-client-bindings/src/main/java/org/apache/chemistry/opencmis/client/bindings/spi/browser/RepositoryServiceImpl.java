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
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpUtils;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
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
        return getRepositoriesInternal(null);
    }

    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
        List<RepositoryInfo> repositoryInfos = getRepositoriesInternal(repositoryId);

        if (repositoryInfos.size() == 0) {
            throw new CmisObjectNotFoundException("Repository '" + repositoryId + "'not found!");
        }

        if (repositoryInfos.size() == 1) {
            return repositoryInfos.get(0);
        }

        // find the repository
        for (RepositoryInfo info : repositoryInfos) {
            if (info.getId() == null) {
                continue;
            }

            if (info.getId().equals(repositoryId)) {
                return info;
            }
        }

        throw new CmisObjectNotFoundException("Repository '" + repositoryId + "'not found!");
    }

    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
        return getTypeDefinitionInternal(repositoryId, typeId);
    }

    public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        // build URL
        UrlBuilder url = getRepositoryUrl(repositoryId, Constants.SELECTOR_TYPE_CHILDREN);
        url.addParameter(Constants.PARAM_TYPE_ID, typeId);
        url.addParameter(Constants.PARAM_PROPERTY_DEFINITIONS, includePropertyDefinitions);
        url.addParameter(Constants.PARAM_MAX_ITEMS, maxItems);
        url.addParameter(Constants.PARAM_SKIP_COUNT, skipCount);

        // read and parse
        HttpUtils.Response resp = read(url);
        Map<String, Object> json = parseObject(resp.getStream(), resp.getCharset());

        return JSONConverter.convertTypeChildren(json);
    }

    public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension) {
        // build URL
        UrlBuilder url = getRepositoryUrl(repositoryId, Constants.SELECTOR_TYPE_DESCENDANTS);
        url.addParameter(Constants.PARAM_TYPE_ID, typeId);
        url.addParameter(Constants.PARAM_DEPTH, depth);
        url.addParameter(Constants.PARAM_PROPERTY_DEFINITIONS, includePropertyDefinitions);

        // read and parse
        HttpUtils.Response resp = read(url);
        List<Object> json = parseArray(resp.getStream(), resp.getCharset());

        return JSONConverter.convertTypeDescendants(json);
    }
}
