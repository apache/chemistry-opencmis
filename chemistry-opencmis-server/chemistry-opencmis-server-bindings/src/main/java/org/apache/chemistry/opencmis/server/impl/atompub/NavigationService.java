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

import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_ACL;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_ALLOWABLEACIONS;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CHECKEDOUT;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CHILDREN;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CONTENT;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_DESCENDANTS;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_ENTRY;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_FOLDERTREE;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_PARENTS;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_POLICIES;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_RELATIONSHIPS;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_TYPE;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileBaseUrl;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileUrl;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileUrlBuilder;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.getNamespaces;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.writeObjectEntry;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBigIntegerParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBooleanParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getEnumParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getStringParameter;

import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.server.RenditionInfo;

/**
 * Navigation Service operations.
 */
public final class NavigationService {

    private NavigationService() {
    }

    /**
     * Children Collection GET.
     */
    public static void getChildren(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String folderId = getStringParameter(request, Constants.PARAM_ID);
        String filter = getStringParameter(request, Constants.PARAM_FILTER);
        String orderBy = getStringParameter(request, Constants.PARAM_ORDER_BY);
        Boolean includeAllowableActions = getBooleanParameter(request, Constants.PARAM_ALLOWABLE_ACTIONS);
        IncludeRelationships includeRelationships = getEnumParameter(request, Constants.PARAM_RELATIONSHIPS,
                IncludeRelationships.class);
        String renditionFilter = getStringParameter(request, Constants.PARAM_RENDITION_FILTER);
        Boolean includePathSegment = getBooleanParameter(request, Constants.PARAM_PATH_SEGMENT);
        BigInteger maxItems = getBigIntegerParameter(request, Constants.PARAM_MAX_ITEMS);
        BigInteger skipCount = getBigIntegerParameter(request, Constants.PARAM_SKIP_COUNT);

        // execute
        ObjectInFolderList children = service.getChildren(repositoryId, folderId, filter, orderBy,
                includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, maxItems,
                skipCount, null);

        if (children == null) {
            throw new CmisRuntimeException("Children are null!");
        }

        ObjectInfo folderInfo = service.getObjectInfo(repositoryId, folderId);
        if (folderInfo == null) {
            throw new CmisRuntimeException("Folder Object Info is missing!");
        }

        // set headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(Constants.MEDIATYPE_FEED);

        // write XML
        AtomFeed feed = new AtomFeed();
        feed.startDocument(response.getOutputStream(), getNamespaces(service));
        feed.startFeed(true);

        // write basic Atom feed elements
        feed.writeFeedElements(folderInfo.getId(), folderInfo.getAtomId(), folderInfo.getCreatedBy(),
                folderInfo.getName(), folderInfo.getLastModificationDate(), null, children.getNumItems());

        // write links
        UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

        feed.writeServiceLink(baseUrl.toString(), repositoryId);

        feed.writeSelfLink(compileUrl(baseUrl, RESOURCE_CHILDREN, folderInfo.getId()), null);

        feed.writeDescribedByLink(compileUrl(baseUrl, RESOURCE_TYPE, folderInfo.getTypeId()));

        feed.writeAllowableActionsLink(compileUrl(baseUrl, RESOURCE_ALLOWABLEACIONS, folderInfo.getId()));

        feed.writeDownLink(compileUrl(baseUrl, RESOURCE_CHILDREN, folderInfo.getId()), Constants.MEDIATYPE_FEED);

        if (folderInfo.supportsDescendants()) {
            feed.writeDownLink(compileUrl(baseUrl, RESOURCE_DESCENDANTS, folderInfo.getId()),
                    Constants.MEDIATYPE_DESCENDANTS);
        }

        if (folderInfo.supportsFolderTree()) {
            feed.writeFolderTreeLink(compileUrl(baseUrl, RESOURCE_FOLDERTREE, folderInfo.getId()));
        }

        if (folderInfo.hasParent()) {
            feed.writeUpLink(compileUrl(baseUrl, RESOURCE_PARENTS, folderInfo.getId()), Constants.MEDIATYPE_FEED);
        }

        if (folderInfo.getRenditionInfos() != null) {
            for (RenditionInfo ri : folderInfo.getRenditionInfos()) {
                feed.writeAlternateLink(compileUrl(baseUrl, RESOURCE_CONTENT, ri.getId()), ri.getContenType(),
                        ri.getKind(), ri.getTitle(), ri.getLength());
            }
        }

        if (folderInfo.hasAcl()) {
            feed.writeAclLink(compileUrl(baseUrl, RESOURCE_ACL, folderInfo.getId()));
        }

        if (folderInfo.supportsPolicies()) {
            feed.writeAclLink(compileUrl(baseUrl, RESOURCE_POLICIES, folderInfo.getId()));
        }

        if (folderInfo.supportsRelationships()) {
            feed.writeRelationshipsLink(compileUrl(baseUrl, RESOURCE_RELATIONSHIPS, folderInfo.getId()));
        }

        UrlBuilder pagingUrl = new UrlBuilder(compileUrlBuilder(baseUrl, RESOURCE_CHILDREN, folderInfo.getId()));
        pagingUrl.addParameter(Constants.PARAM_FILTER, filter);
        pagingUrl.addParameter(Constants.PARAM_ORDER_BY, orderBy);
        pagingUrl.addParameter(Constants.PARAM_ALLOWABLE_ACTIONS, includeAllowableActions);
        pagingUrl.addParameter(Constants.PARAM_RELATIONSHIPS, includeRelationships);
        pagingUrl.addParameter(Constants.PARAM_RENDITION_FILTER, renditionFilter);
        pagingUrl.addParameter(Constants.PARAM_PATH_SEGMENT, includePathSegment);
        feed.writePagingLinks(pagingUrl, maxItems, skipCount, children.getNumItems(), children.hasMoreItems(),
                AtomPubUtils.PAGE_SIZE);

        // write collection
        feed.writeCollection(compileUrl(baseUrl, RESOURCE_CHILDREN, folderInfo.getId()), null, "Folder collection",
                Constants.MEDIATYPE_CMISATOM);

        // write entries
        if (children.getObjects() != null) {
            AtomEntry entry = new AtomEntry(feed.getWriter());
            for (ObjectInFolderData object : children.getObjects()) {
                if ((object == null) || (object.getObject() == null)) {
                    continue;
                }
                writeObjectEntry(service, entry, object.getObject(), null, repositoryId, object.getPathSegment(), null,
                        baseUrl, false);
            }
        }

        // we are done
        feed.endFeed();
        feed.endDocument();
    }

    /**
     * Descendants feed GET.
     */
    public static void getDescendants(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String folderId = getStringParameter(request, Constants.PARAM_ID);
        BigInteger depth = getBigIntegerParameter(request, Constants.PARAM_DEPTH);
        String filter = getStringParameter(request, Constants.PARAM_FILTER);
        Boolean includeAllowableActions = getBooleanParameter(request, Constants.PARAM_ALLOWABLE_ACTIONS);
        IncludeRelationships includeRelationships = getEnumParameter(request, Constants.PARAM_RELATIONSHIPS,
                IncludeRelationships.class);
        String renditionFilter = getStringParameter(request, Constants.PARAM_RENDITION_FILTER);
        Boolean includePathSegment = getBooleanParameter(request, Constants.PARAM_PATH_SEGMENT);

        // execute
        List<ObjectInFolderContainer> descendants = service.getDescendants(repositoryId, folderId, depth, filter,
                includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, null);

        if (descendants == null) {
            throw new CmisRuntimeException("Descendants are null!");
        }

        ObjectInfo folderInfo = service.getObjectInfo(repositoryId, folderId);
        if (folderInfo == null) {
            throw new CmisRuntimeException("Folder Object Info is missing!");
        }

        // set headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(Constants.MEDIATYPE_FEED);

        // write XML
        AtomFeed feed = new AtomFeed();
        feed.startDocument(response.getOutputStream(), getNamespaces(service));
        feed.startFeed(true);

        // write basic Atom feed elements
        feed.writeFeedElements(folderInfo.getId(), folderInfo.getAtomId(), folderInfo.getCreatedBy(),
                folderInfo.getName(), folderInfo.getLastModificationDate(), null, null);

        // write links
        UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

        feed.writeServiceLink(baseUrl.toString(), repositoryId);

        feed.writeSelfLink(compileUrl(baseUrl, RESOURCE_DESCENDANTS, folderInfo.getId()), null);

        feed.writeViaLink(compileUrl(baseUrl, RESOURCE_ENTRY, folderInfo.getId()));

        feed.writeDownLink(compileUrl(baseUrl, RESOURCE_CHILDREN, folderInfo.getId()), Constants.MEDIATYPE_FEED);

        if (folderInfo.supportsFolderTree()) {
            feed.writeFolderTreeLink(compileUrl(baseUrl, RESOURCE_FOLDERTREE, folderInfo.getId()));
        }

        if (folderInfo.hasParent()) {
            feed.writeUpLink(compileUrl(baseUrl, RESOURCE_PARENTS, folderInfo.getId()), Constants.MEDIATYPE_FEED);
        }

        // write entries
        AtomEntry entry = new AtomEntry(feed.getWriter());
        for (ObjectInFolderContainer container : descendants) {
            if ((container == null) || (container.getObject() == null) || (container.getObject().getObject() == null)) {
                continue;
            }
            writeObjectEntry(service, entry, container.getObject().getObject(), container.getChildren(), repositoryId,
                    container.getObject().getPathSegment(), null, baseUrl, false);
        }

        // we are done
        feed.endFeed();
        feed.endDocument();
    }

    /**
     * Folder tree feed GET.
     */
    public static void getFolderTree(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String folderId = getStringParameter(request, Constants.PARAM_ID);
        BigInteger depth = getBigIntegerParameter(request, Constants.PARAM_DEPTH);
        String filter = getStringParameter(request, Constants.PARAM_FILTER);
        Boolean includeAllowableActions = getBooleanParameter(request, Constants.PARAM_ALLOWABLE_ACTIONS);
        IncludeRelationships includeRelationships = getEnumParameter(request, Constants.PARAM_RELATIONSHIPS,
                IncludeRelationships.class);
        String renditionFilter = getStringParameter(request, Constants.PARAM_RENDITION_FILTER);
        Boolean includePathSegment = getBooleanParameter(request, Constants.PARAM_PATH_SEGMENT);

        // execute
        List<ObjectInFolderContainer> folderTree = service.getFolderTree(repositoryId, folderId, depth, filter,
                includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, null);

        if (folderTree == null) {
            throw new CmisRuntimeException("Folder tree is null!");
        }

        ObjectInfo folderInfo = service.getObjectInfo(repositoryId, folderId);
        if (folderInfo == null) {
            throw new CmisRuntimeException("Folder Object Info is missing!");
        }

        // set headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(Constants.MEDIATYPE_FEED);

        // write XML
        AtomFeed feed = new AtomFeed();
        feed.startDocument(response.getOutputStream(), getNamespaces(service));
        feed.startFeed(true);

        // write basic Atom feed elements
        feed.writeFeedElements(folderInfo.getId(), folderInfo.getAtomId(), folderInfo.getCreatedBy(),
                folderInfo.getName(), folderInfo.getLastModificationDate(), null, null);

        // write links
        UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

        feed.writeServiceLink(baseUrl.toString(), repositoryId);

        feed.writeSelfLink(compileUrl(baseUrl, RESOURCE_DESCENDANTS, folderInfo.getId()), null);

        feed.writeViaLink(compileUrl(baseUrl, RESOURCE_ENTRY, folderInfo.getId()));

        feed.writeDownLink(compileUrl(baseUrl, RESOURCE_CHILDREN, folderInfo.getId()), Constants.MEDIATYPE_FEED);

        if (folderInfo.supportsDescendants()) {
            feed.writeDownLink(compileUrl(baseUrl, RESOURCE_DESCENDANTS, folderInfo.getId()),
                    Constants.MEDIATYPE_DESCENDANTS);
        }

        if (folderInfo.hasParent()) {
            feed.writeUpLink(compileUrl(baseUrl, RESOURCE_PARENTS, folderInfo.getId()), Constants.MEDIATYPE_FEED);
        }

        // write entries
        AtomEntry entry = new AtomEntry(feed.getWriter());
        for (ObjectInFolderContainer container : folderTree) {
            if ((container == null) || (container.getObject() == null) || (container.getObject().getObject() == null)) {
                continue;
            }
            writeObjectEntry(service, entry, container.getObject().getObject(), container.getChildren(), repositoryId,
                    container.getObject().getPathSegment(), null, baseUrl, false);
        }

        // we are done
        feed.endFeed();
        feed.endDocument();
    }

    /**
     * Object parents feed GET.
     */
    public static void getObjectParents(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = getStringParameter(request, Constants.PARAM_ID);
        String filter = getStringParameter(request, Constants.PARAM_FILTER);
        Boolean includeAllowableActions = getBooleanParameter(request, Constants.PARAM_ALLOWABLE_ACTIONS);
        IncludeRelationships includeRelationships = getEnumParameter(request, Constants.PARAM_RELATIONSHIPS,
                IncludeRelationships.class);
        String renditionFilter = getStringParameter(request, Constants.PARAM_RENDITION_FILTER);
        Boolean includeRelativePathSegment = getBooleanParameter(request, Constants.PARAM_RELATIVE_PATH_SEGMENT);

        // execute
        List<ObjectParentData> parents = service.getObjectParents(repositoryId, objectId, filter,
                includeAllowableActions, includeRelationships, renditionFilter, includeRelativePathSegment, null);

        if (parents == null) {
            throw new CmisRuntimeException("Parents are null!");
        }

        ObjectInfo objectInfo = service.getObjectInfo(repositoryId, objectId);
        if (objectInfo == null) {
            throw new CmisRuntimeException("Object Info is missing!");
        }

        // set headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(Constants.MEDIATYPE_FEED);

        // write XML
        AtomFeed feed = new AtomFeed();
        feed.startDocument(response.getOutputStream(), getNamespaces(service));
        feed.startFeed(true);

        // write basic Atom feed elements
        feed.writeFeedElements(objectInfo.getId(), objectInfo.getAtomId(), objectInfo.getCreatedBy(),
                objectInfo.getName(), objectInfo.getLastModificationDate(), null, null);

        // write links
        UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

        feed.writeServiceLink(baseUrl.toString(), repositoryId);

        feed.writeSelfLink(compileUrl(baseUrl, RESOURCE_PARENTS, objectInfo.getId()), null);

        // write entries
        AtomEntry entry = new AtomEntry(feed.getWriter());
        for (ObjectParentData object : parents) {
            if ((object == null) || (object.getObject() == null)) {
                continue;
            }
            writeObjectEntry(service, entry, object.getObject(), null, repositoryId, null,
                    object.getRelativePathSegment(), baseUrl, false);
        }

        // we are done
        feed.endFeed();
        feed.endDocument();
    }

    /**
     * Checked Out Collection GET.
     */
    public static void getCheckedOutDocs(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String folderId = getStringParameter(request, Constants.PARAM_FOLDER_ID);
        String filter = getStringParameter(request, Constants.PARAM_FILTER);
        String orderBy = getStringParameter(request, Constants.PARAM_ORDER_BY);
        Boolean includeAllowableActions = getBooleanParameter(request, Constants.PARAM_ALLOWABLE_ACTIONS);
        IncludeRelationships includeRelationships = getEnumParameter(request, Constants.PARAM_RELATIONSHIPS,
                IncludeRelationships.class);
        String renditionFilter = getStringParameter(request, Constants.PARAM_RENDITION_FILTER);
        BigInteger maxItems = getBigIntegerParameter(request, Constants.PARAM_MAX_ITEMS);
        BigInteger skipCount = getBigIntegerParameter(request, Constants.PARAM_SKIP_COUNT);

        // execute
        ObjectList checkedOut = service.getCheckedOutDocs(repositoryId, folderId, filter, orderBy,
                includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount, null);

        if (checkedOut == null) {
            throw new CmisRuntimeException("Checked Out list is null!");
        }

        ObjectInfo folderInfo = null;
        if (folderId != null) {
            folderInfo = service.getObjectInfo(repositoryId, folderId);
            if (folderInfo == null) {
                throw new CmisRuntimeException("Folder Object Info is missing!");
            }
        } else {
            folderInfo = new ObjectInfoImpl();
            GregorianCalendar now = new GregorianCalendar();

            ((ObjectInfoImpl) folderInfo).setId("uri:x-checkedout");
            ((ObjectInfoImpl) folderInfo).setName("Checked Out");
            ((ObjectInfoImpl) folderInfo).setCreatedBy("");
            ((ObjectInfoImpl) folderInfo).setCreationDate(now);
            ((ObjectInfoImpl) folderInfo).setLastModificationDate(now);
            ((ObjectInfoImpl) folderInfo).setHasParent(false);
            ((ObjectInfoImpl) folderInfo).setSupportsDescendants(false);
            ((ObjectInfoImpl) folderInfo).setSupportsFolderTree(false);
        }

        // set headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(Constants.MEDIATYPE_FEED);

        // write XML
        AtomFeed feed = new AtomFeed();
        feed.startDocument(response.getOutputStream(), getNamespaces(service));
        feed.startFeed(true);

        // write basic Atom feed elements
        feed.writeFeedElements(folderInfo.getId(), folderInfo.getAtomId(), folderInfo.getCreatedBy(),
                folderInfo.getName(), folderInfo.getLastModificationDate(), null, checkedOut.getNumItems());

        // write links
        UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

        feed.writeServiceLink(baseUrl.toString(), repositoryId);

        feed.writeSelfLink(compileUrl(baseUrl, RESOURCE_CHECKEDOUT, folderInfo.getId()), null);

        UrlBuilder pagingUrl = new UrlBuilder(compileUrlBuilder(baseUrl, RESOURCE_CHECKEDOUT, folderInfo.getId()));
        pagingUrl.addParameter(Constants.PARAM_FILTER, filter);
        pagingUrl.addParameter(Constants.PARAM_ORDER_BY, orderBy);
        pagingUrl.addParameter(Constants.PARAM_ALLOWABLE_ACTIONS, includeAllowableActions);
        pagingUrl.addParameter(Constants.PARAM_RELATIONSHIPS, includeRelationships);
        pagingUrl.addParameter(Constants.PARAM_RENDITION_FILTER, renditionFilter);
        feed.writePagingLinks(pagingUrl, maxItems, skipCount, checkedOut.getNumItems(), checkedOut.hasMoreItems(),
                AtomPubUtils.PAGE_SIZE);

        // write entries
        if (checkedOut.getObjects() != null) {
            AtomEntry entry = new AtomEntry(feed.getWriter());
            for (ObjectData object : checkedOut.getObjects()) {
                if (object == null) {
                    continue;
                }
                writeObjectEntry(service, entry, object, null, repositoryId, null, null, baseUrl, false);
            }
        }

        // we are done
        feed.endFeed();
        feed.endDocument();
    }
}
