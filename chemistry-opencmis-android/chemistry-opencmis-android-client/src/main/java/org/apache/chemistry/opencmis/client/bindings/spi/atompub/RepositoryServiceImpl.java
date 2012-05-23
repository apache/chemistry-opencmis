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
package org.apache.chemistry.opencmis.client.bindings.spi.atompub;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomElement;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomEntry;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomFeed;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomLink;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpUtils;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionListImpl;
import org.apache.chemistry.opencmis.commons.spi.RepositoryService;

/**
 * Repository Service AtomPub client.
 */
public class RepositoryServiceImpl extends AbstractAtomPubService implements RepositoryService {

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

        // find the repository
        for (RepositoryInfo info : repositoryInfos) {
            if (info.getId() == null) {
                continue;
            }

            if (info.getId().equals(repositoryId)) {
                return info;
            }
        }

        throw new CmisObjectNotFoundException("Repository not found!");
    }

    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
        return getTypeDefinitionInternal(repositoryId, typeId);
    }

    public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        TypeDefinitionListImpl result = new TypeDefinitionListImpl();

        // find the link
        String link = null;
        if (typeId == null) {
            link = loadCollection(repositoryId, Constants.COLLECTION_TYPES);
        } else {
            link = loadTypeLink(repositoryId, typeId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);
        }

        if (link == null) {
            throw new CmisObjectNotFoundException("Unknown repository or type!");
        }

        UrlBuilder url = new UrlBuilder(link);
        url.addParameter(Constants.PARAM_TYPE_ID, typeId);
        url.addParameter(Constants.PARAM_PROPERTY_DEFINITIONS, includePropertyDefinitions);
        url.addParameter(Constants.PARAM_MAX_ITEMS, maxItems);
        url.addParameter(Constants.PARAM_SKIP_COUNT, skipCount);

        // read and parse
        HttpUtils.Response resp = read(url);
        AtomFeed feed = parse(resp.getStream(), AtomFeed.class);

        // handle top level
        for (AtomElement element : feed.getElements()) {
            if (element.getObject() instanceof AtomLink) {
                if (isNextLink(element)) {
                    result.setHasMoreItems(Boolean.TRUE);
                }
            } else if (isInt(NAME_NUM_ITEMS, element)) {
                result.setNumItems((BigInteger) element.getObject());
            }
        }

        result.setList(new ArrayList<TypeDefinition>(feed.getEntries().size()));

        // get the children
        if (!feed.getEntries().isEmpty()) {
            for (AtomEntry entry : feed.getEntries()) {
                TypeDefinition child = null;

                lockTypeLinks();
                try {
                    // walk through the entry
                    for (AtomElement element : entry.getElements()) {
                        if (element.getObject() instanceof AtomLink) {
                            addTypeLink(repositoryId, entry.getId(), (AtomLink) element.getObject());
                        } else if (element.getObject() instanceof TypeDefinition) {
                            child = (TypeDefinition) element.getObject();
                        }
                    }
                } finally {
                    unlockTypeLinks();
                }

                if (child != null) {
                    result.getList().add(child);
                }
            }
        }

        return result;
    }

    public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension) {
        List<TypeDefinitionContainer> result = new ArrayList<TypeDefinitionContainer>();

        // find the link
        String link = null;
        if (typeId == null) {
            link = loadRepositoryLink(repositoryId, Constants.REP_REL_TYPEDESC);
        } else {
            link = loadTypeLink(repositoryId, typeId, Constants.REL_DOWN, Constants.MEDIATYPE_DESCENDANTS);
        }

        if (link == null) {
            throw new CmisObjectNotFoundException("Unknown repository or type!");
        }

        UrlBuilder url = new UrlBuilder(link);
        url.addParameter(Constants.PARAM_TYPE_ID, typeId);
        url.addParameter(Constants.PARAM_DEPTH, depth);
        url.addParameter(Constants.PARAM_PROPERTY_DEFINITIONS, includePropertyDefinitions);

        // read and parse
        HttpUtils.Response resp = read(url);
        AtomFeed feed = parse(resp.getStream(), AtomFeed.class);

        // process tree
        addTypeDescendantsLevel(repositoryId, feed, result);

        return result;
    }

    /**
     * Adds type descendants level recursively.
     */
    private void addTypeDescendantsLevel(String repositoryId, AtomFeed feed, List<TypeDefinitionContainer> containerList) {
        if ((feed == null) || (feed.getEntries().isEmpty())) {
            return;
        }

        // walk through the feed
        for (AtomEntry entry : feed.getEntries()) {
            TypeDefinitionContainerImpl childContainer = null;
            List<TypeDefinitionContainer> childContainerList = new ArrayList<TypeDefinitionContainer>();

            // walk through the entry
            lockTypeLinks();
            try {
                for (AtomElement element : entry.getElements()) {
                    if (element.getObject() instanceof AtomLink) {
                        addTypeLink(repositoryId, entry.getId(), (AtomLink) element.getObject());
                    } else if (element.getObject() instanceof TypeDefinition) {
                        childContainer = new TypeDefinitionContainerImpl((TypeDefinition) element.getObject());
                    } else if (element.getObject() instanceof AtomFeed) {
                        addTypeDescendantsLevel(repositoryId, (AtomFeed) element.getObject(), childContainerList);
                    }
                }
            } finally {
                unlockTypeLinks();
            }

            if (childContainer != null) {
                childContainer.setChildren(childContainerList);
                containerList.add(childContainer);
            }
        }
    }
}
