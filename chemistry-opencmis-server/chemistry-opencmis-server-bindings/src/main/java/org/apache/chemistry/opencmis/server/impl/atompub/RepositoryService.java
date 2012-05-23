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
package org.apache.chemistry.opencmis.server.impl.atompub;

import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.PAGE_SIZE;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CHANGES;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CHECKEDOUT;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CHILDREN;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_DESCENDANTS;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_FOLDERTREE;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_OBJECTBYID;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_OBJECTBYPATH;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_QUERY;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_TYPE;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_TYPES;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_TYPESDESC;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_UNFILED;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.TYPE_AUTHOR;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileBaseUrl;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileUrl;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileUrlBuilder;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.getNamespaces;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.writeTypeEntry;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBigIntegerParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBooleanParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getStringParameter;

import java.math.BigInteger;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.impl.CallContextImpl;

/**
 * Repository Service operations.
 */
public final class RepositoryService {

    /**
     * Private constructor.
     */
    private RepositoryService() {
    }

    /**
     * Renders the service document.
     */
    public static void getRepositories(CallContext context, CmisService service, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // get parameters
        String repositoryId = getStringParameter(request, Constants.PARAM_REPOSITORY_ID);

        // execute
        List<RepositoryInfo> infoDataList = null;

        if (repositoryId == null) {
            infoDataList = service.getRepositoryInfos(null);
        } else {
            infoDataList = Collections.singletonList(service.getRepositoryInfo(repositoryId, null));
            if (context instanceof CallContextImpl) {
                ((CallContextImpl) context).put(CallContext.REPOSITORY_ID, repositoryId);
            }
        }

        // set headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(Constants.MEDIATYPE_SERVICE);

        // write XML
        ServiceDocument serviceDoc = new ServiceDocument();

        serviceDoc.startDocument(response.getOutputStream(), getNamespaces(service));
        serviceDoc.startServiceDocument();

        if (infoDataList != null) {
            for (RepositoryInfo infoData : infoDataList) {
                if (infoData == null) {
                    continue;
                }

                String repId = infoData.getId();
                UrlBuilder baseUrl = compileBaseUrl(request, repId);

                boolean supportsQuery = false;
                boolean supportsUnFiling = false;
                boolean supportsMultifiling = false;
                boolean supportsFolderTree = false;
                boolean supportsRootDescendants = false;
                boolean supportsChanges = false;

                if (infoData.getCapabilities() != null) {
                    RepositoryCapabilities cap = infoData.getCapabilities();

                    if (cap.getQueryCapability() != null) {
                        supportsQuery = (cap.getQueryCapability() != CapabilityQuery.NONE);
                    }

                    if (cap.isUnfilingSupported() != null) {
                        supportsUnFiling = cap.isUnfilingSupported();
                    }

                    if (cap.isMultifilingSupported() != null) {
                        supportsMultifiling = cap.isMultifilingSupported();
                    }

                    if (cap.isGetFolderTreeSupported() != null) {
                        supportsFolderTree = cap.isGetFolderTreeSupported();
                    }

                    if (cap.isGetDescendantsSupported() != null) {
                        supportsRootDescendants = cap.isGetDescendantsSupported();
                    }

                    if (cap.getChangesCapability() != null) {
                        supportsChanges = (cap.getChangesCapability() != CapabilityChanges.NONE);
                    }
                }

                serviceDoc.startWorkspace(infoData.getId());

                // add collections

                // - root collection
                serviceDoc.writeCollection(compileUrl(baseUrl, RESOURCE_CHILDREN, infoData.getRootFolderId()),
                        Constants.COLLECTION_ROOT, "Root Collection", Constants.MEDIATYPE_ENTRY,
                        Constants.MEDIATYPE_CMISATOM);

                // - types collection
                serviceDoc.writeCollection(compileUrl(baseUrl, RESOURCE_TYPES, null), Constants.COLLECTION_TYPES,
                        "Types Collection", "");

                // - query collection
                if (supportsQuery) {
                    serviceDoc.writeCollection(compileUrl(baseUrl, RESOURCE_QUERY, null), Constants.COLLECTION_QUERY,
                            "Query Collection", Constants.MEDIATYPE_QUERY);
                }

                // - checked out collection collection
                serviceDoc.writeCollection(compileUrl(baseUrl, RESOURCE_CHECKEDOUT, null),
                        Constants.COLLECTION_CHECKEDOUT, "Checked Out Collection", Constants.MEDIATYPE_CMISATOM);

                // - unfiled collection collection
                if (supportsUnFiling || supportsMultifiling) {
                    serviceDoc.writeCollection(compileUrl(baseUrl, RESOURCE_UNFILED, null),
                            Constants.COLLECTION_UNFILED, "Unfiled Collection", Constants.MEDIATYPE_CMISATOM);

                }

                // add repository info
                serviceDoc.writeRepositoryInfo(infoData);

                // add links

                // - types descendants
                serviceDoc.writeLink(Constants.REP_REL_TYPEDESC, compileUrl(baseUrl, RESOURCE_TYPESDESC, null),
                        Constants.MEDIATYPE_FEED, null);

                // - folder tree
                if (supportsFolderTree) {
                    serviceDoc.writeLink(Constants.REP_REL_FOLDERTREE,
                            compileUrl(baseUrl, RESOURCE_FOLDERTREE, infoData.getRootFolderId()),
                            Constants.MEDIATYPE_DESCENDANTS, null);
                }

                // - root descendants
                if (supportsRootDescendants) {
                    serviceDoc.writeLink(Constants.REP_REL_ROOTDESC,
                            compileUrl(baseUrl, RESOURCE_DESCENDANTS, infoData.getRootFolderId()),
                            Constants.MEDIATYPE_DESCENDANTS, infoData.getRootFolderId());
                }

                // - changes
                if (supportsChanges) {
                    serviceDoc.writeLink(Constants.REP_REL_CHANGES, compileUrl(baseUrl, RESOURCE_CHANGES, null),
                            Constants.MEDIATYPE_FEED, null);
                }

                // add URI templates

                // - object by id
                String url = compileUrl(baseUrl, RESOURCE_OBJECTBYID, null)
                        + "?id={id}&filter={filter}&includeAllowableActions={includeAllowableActions}&includeACL={includeACL}&includePolicyIds={includePolicyIds}&includeRelationships={includeRelationships}&renditionFilter={renditionFilter}";
                serviceDoc.writeUriTemplate(url, Constants.TEMPLATE_OBJECT_BY_ID, Constants.MEDIATYPE_ENTRY);

                // - object by path
                url = compileUrl(baseUrl, RESOURCE_OBJECTBYPATH, null)
                        + "?path={path}&filter={filter}&includeAllowableActions={includeAllowableActions}&includeACL={includeACL}&includePolicyIds={includePolicyIds}&includeRelationships={includeRelationships}&renditionFilter={renditionFilter}";
                serviceDoc.writeUriTemplate(url, Constants.TEMPLATE_OBJECT_BY_PATH, Constants.MEDIATYPE_ENTRY);

                // - type by id
                url = compileUrl(baseUrl, RESOURCE_TYPE, null) + "?id={id}";
                serviceDoc.writeUriTemplate(url, Constants.TEMPLATE_TYPE_BY_ID, Constants.MEDIATYPE_ENTRY);

                // - query
                if (supportsQuery) {
                    url = compileUrl(baseUrl, RESOURCE_QUERY, null)
                            + "?q={q}&searchAllVersions={searchAllVersions}&includeAllowableActions={includeAllowableActions}&includeRelationships={includeRelationships}&maxItems={maxItems}&skipCount={skipCount}";
                    serviceDoc.writeUriTemplate(url, Constants.TEMPLATE_QUERY, Constants.MEDIATYPE_FEED);
                }

                serviceDoc.endWorkspace();
            }
        }

        serviceDoc.endServiceDocument();
        serviceDoc.endDocument();
    }

    /**
     * Renders a type children collection.
     */
    public static void getTypeChildren(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String typeId = getStringParameter(request, Constants.PARAM_TYPE_ID);
        boolean includePropertyDefinitions = getBooleanParameter(request, Constants.PARAM_PROPERTY_DEFINITIONS, false);
        BigInteger maxItems = getBigIntegerParameter(request, Constants.PARAM_MAX_ITEMS);
        BigInteger skipCount = getBigIntegerParameter(request, Constants.PARAM_SKIP_COUNT);

        // execute
        TypeDefinitionList typeList = service.getTypeChildren(repositoryId, typeId, includePropertyDefinitions,
                maxItems, skipCount, null);

        BigInteger numItems = (typeList == null ? null : typeList.getNumItems());
        Boolean hasMoreItems = (typeList == null ? null : typeList.hasMoreItems());

        String parentTypeId = null;
        String typeName = "Type Children";

        // in order to get the parent type, we need the type definition of this
        // type as well
        if (typeId != null) {
            TypeDefinition typeDefinition = service.getTypeDefinition(repositoryId, typeId, null);

            parentTypeId = (typeDefinition == null ? null : typeDefinition.getParentTypeId());
            typeName = (typeDefinition == null ? typeId : typeDefinition.getDisplayName());
        }

        // write XML
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(Constants.MEDIATYPE_FEED);

        AtomFeed feed = new AtomFeed();
        feed.startDocument(response.getOutputStream(), getNamespaces(service));
        feed.startFeed(true);

        // write basic Atom feed elements
        feed.writeFeedElements(typeId, null, TYPE_AUTHOR, typeName, new GregorianCalendar(), null, numItems);

        // write links
        UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

        feed.writeServiceLink(baseUrl.toString(), repositoryId);

        UrlBuilder selfLink = compileUrlBuilder(baseUrl, RESOURCE_TYPES, null);
        selfLink.addParameter(Constants.PARAM_TYPE_ID, typeId);
        selfLink.addParameter(Constants.PARAM_PROPERTY_DEFINITIONS, includePropertyDefinitions);
        feed.writeSelfLink(selfLink.toString(), typeId);

        feed.writeViaLink(compileUrl(baseUrl, RESOURCE_TYPE, typeId));

        UrlBuilder downLink = compileUrlBuilder(baseUrl, RESOURCE_TYPESDESC, null);
        downLink.addParameter(Constants.PARAM_TYPE_ID, typeId);
        feed.writeDownLink(downLink.toString(), Constants.MEDIATYPE_DESCENDANTS);

        if (parentTypeId != null) {
            feed.writeUpLink(compileUrl(baseUrl, RESOURCE_TYPE, parentTypeId), Constants.MEDIATYPE_ENTRY);
        }

        // write paging links
        UrlBuilder pagingUrl = compileUrlBuilder(baseUrl, RESOURCE_TYPES, null);
        pagingUrl.addParameter(Constants.PARAM_TYPE_ID, typeId);
        pagingUrl.addParameter(Constants.PARAM_PROPERTY_DEFINITIONS, includePropertyDefinitions);
        feed.writePagingLinks(pagingUrl, maxItems, skipCount, numItems, hasMoreItems, PAGE_SIZE);

        // write collection
        UrlBuilder collectionUrl = compileUrlBuilder(baseUrl, RESOURCE_TYPES, null);
        collectionUrl.addParameter(Constants.PARAM_TYPE_ID, typeId);
        feed.writeCollection(collectionUrl.toString(), null, "Types Collection", "");

        // write type entries
        if ((typeList != null) && (typeList.getList() != null)) {
            AtomEntry entry = new AtomEntry(feed.getWriter());
            for (TypeDefinition type : typeList.getList()) {
                writeTypeEntry(entry, type, null, repositoryId, baseUrl, false);
            }
        }

        // we are done
        feed.endFeed();
        feed.endDocument();
    }

    /**
     * Renders a type descendants feed.
     */
    public static void getTypeDescendants(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String typeId = getStringParameter(request, Constants.PARAM_TYPE_ID);
        BigInteger depth = getBigIntegerParameter(request, Constants.PARAM_DEPTH);
        boolean includePropertyDefinitions = getBooleanParameter(request, Constants.PARAM_PROPERTY_DEFINITIONS, false);

        // execute
        List<TypeDefinitionContainer> typeTree = service.getTypeDescendants(repositoryId, typeId, depth,
                includePropertyDefinitions, null);

        String parentTypeId = null;
        String typeName = "Type Children";

        // in order to get the parent type, we need the type definition of this
        // type as well
        if (typeId != null) {
            TypeDefinition typeDefinition = service.getTypeDefinition(repositoryId, typeId, null);

            parentTypeId = (typeDefinition == null ? null : typeDefinition.getParentTypeId());
            typeName = (typeDefinition == null ? typeId : typeDefinition.getDisplayName());
        }

        // write XML
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(Constants.MEDIATYPE_FEED);

        AtomFeed feed = new AtomFeed();
        feed.startDocument(response.getOutputStream(), getNamespaces(service));
        feed.startFeed(true);

        // write basic Atom feed elements
        feed.writeFeedElements(typeId, null, TYPE_AUTHOR, typeName, new GregorianCalendar(), null, null);

        // write links
        UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

        feed.writeServiceLink(baseUrl.toString(), repositoryId);

        UrlBuilder selfLink = compileUrlBuilder(baseUrl, RESOURCE_TYPESDESC, null);
        selfLink.addParameter(Constants.PARAM_TYPE_ID, typeId);
        selfLink.addParameter(Constants.PARAM_DEPTH, depth);
        selfLink.addParameter(Constants.PARAM_PROPERTY_DEFINITIONS, includePropertyDefinitions);
        feed.writeSelfLink(selfLink.toString(), typeId);

        feed.writeViaLink(compileUrl(baseUrl, RESOURCE_TYPE, typeId));

        UrlBuilder downLink = compileUrlBuilder(baseUrl, RESOURCE_TYPES, null);
        downLink.addParameter(Constants.PARAM_TYPE_ID, typeId);
        feed.writeDownLink(downLink.toString(), Constants.MEDIATYPE_FEED);

        if (parentTypeId != null) {
            feed.writeUpLink(compileUrl(baseUrl, RESOURCE_TYPE, parentTypeId), Constants.MEDIATYPE_ENTRY);
        }

        // write tree
        if (typeTree != null) {
            AtomEntry entry = new AtomEntry(feed.getWriter());

            for (TypeDefinitionContainer container : typeTree) {
                if ((container != null) && (container.getTypeDefinition() != null)) {
                    writeTypeEntry(entry, container.getTypeDefinition(), container.getChildren(), repositoryId,
                            baseUrl, false);
                }
            }
        }

        // we are done
        feed.endFeed();
        feed.endDocument();
    }

    /**
     * Renders a type definition.
     */
    public static void getTypeDefinition(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String typeId = getStringParameter(request, Constants.PARAM_ID);

        // execute
        TypeDefinition type = service.getTypeDefinition(repositoryId, typeId, null);

        // write XML
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(Constants.MEDIATYPE_ENTRY);

        AtomEntry entry = new AtomEntry();
        entry.startDocument(response.getOutputStream(), getNamespaces(service));
        writeTypeEntry(entry, type, null, repositoryId, compileBaseUrl(request, repositoryId), true);
        entry.endDocument();
    }
}
