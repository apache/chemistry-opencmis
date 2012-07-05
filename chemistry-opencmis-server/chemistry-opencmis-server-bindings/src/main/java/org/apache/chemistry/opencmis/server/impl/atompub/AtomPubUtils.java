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
 *
 * Contributors:
 *     Florian Mueller
 *     Florent Guillaume, Nuxeo
 */
package org.apache.chemistry.opencmis.server.impl.atompub;

import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.ReturnVersion;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.NamespaceDefinitions;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.server.RenditionInfo;
import org.apache.chemistry.opencmis.server.shared.Dispatcher;

/**
 * This class contains operations used by all services.
 */
public final class AtomPubUtils {

    public static final String RESOURCE_CHILDREN = "children";
    public static final String RESOURCE_DESCENDANTS = "descendants";
    public static final String RESOURCE_FOLDERTREE = "foldertree";
    public static final String RESOURCE_TYPE = "type";
    public static final String RESOURCE_TYPES = "types";
    public static final String RESOURCE_TYPESDESC = "typedesc";
    public static final String RESOURCE_ENTRY = "entry";
    public static final String RESOURCE_PARENTS = "parents";
    public static final String RESOURCE_VERSIONS = "versions";
    public static final String RESOURCE_ALLOWABLEACIONS = "allowableactions";
    public static final String RESOURCE_ACL = "acl";
    public static final String RESOURCE_POLICIES = "policies";
    public static final String RESOURCE_RELATIONSHIPS = "relationships";
    public static final String RESOURCE_OBJECTBYID = "id";
    public static final String RESOURCE_OBJECTBYPATH = "path";
    public static final String RESOURCE_QUERY = "query";
    public static final String RESOURCE_CHECKEDOUT = "checkedout";
    public static final String RESOURCE_UNFILED = "unfiled";
    public static final String RESOURCE_CHANGES = "changes";
    public static final String RESOURCE_CONTENT = "content";

    public static final BigInteger PAGE_SIZE = BigInteger.valueOf(100);

    public static final String TYPE_AUTHOR = "unknown";

    /**
     * Private constructor.
     */
    private AtomPubUtils() {
    }

    /**
     * Compiles the base URL for links, collections and templates.
     */
    public static UrlBuilder compileBaseUrl(HttpServletRequest request, String repositoryId) {
        String baseUrl = (String) request.getAttribute(Dispatcher.BASE_URL_ATTRIBUTE);
        if (baseUrl != null) {
            return new UrlBuilder(baseUrl);
        }
        
        UrlBuilder url = new UrlBuilder(request.getScheme(), request.getServerName(), request.getServerPort(), null);

        url.addPath(request.getContextPath());
        url.addPath(request.getServletPath());

        if (repositoryId != null) {
            url.addPathSegment(repositoryId);
        }

        return url;
    }

    /**
     * Compiles a URL for links, collections and templates.
     */
    public static String compileUrl(UrlBuilder baseUrl, String resource, String id) {
        return compileUrlBuilder(baseUrl, resource, id).toString();
    }

    /**
     * Compiles a URL for links, collections and templates.
     */
    public static UrlBuilder compileUrlBuilder(UrlBuilder baseUrl, String resource, String id) {
        UrlBuilder url = new UrlBuilder(baseUrl);
        url.addPathSegment(resource);

        if (id != null) {
            url.addParameter("id", id);
        }

        return url;
    }

    // -------------------------------------------------------------------------
    // --- namespaces ---
    // -------------------------------------------------------------------------

    public static Map<String, String> getNamespaces(Object obj) {
        if (obj instanceof NamespaceDefinitions) {
            return ((NamespaceDefinitions) obj).getNamespaces();
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // --- entry builder ---
    // -------------------------------------------------------------------------

    /**
     * Writes the a object entry.
     */
    public static void writeObjectEntry(CmisService service, AtomEntry entry, ObjectData object,
            List<ObjectInFolderContainer> children, String repositoryId, String pathSegment,
            String relativePathSegment, UrlBuilder baseUrl, boolean isRoot) throws XMLStreamException, JAXBException {
        if (object == null) {
            throw new CmisRuntimeException("Object not set!");
        }

        ObjectInfo info = service.getObjectInfo(repositoryId, object.getId());
        if (info == null) {
            throw new CmisRuntimeException("Object Info not found for: " + object.getId());
        }

        // start
        entry.startEntry(isRoot);

        // write object
        String contentSrc = null;

        if (info.hasContent()) {
            UrlBuilder contentSrcBuilder = compileUrlBuilder(baseUrl, RESOURCE_CONTENT, info.getId());
            if (info.getFileName() != null) {
                contentSrcBuilder.addPathSegment(info.getFileName());
            }

            contentSrc = contentSrcBuilder.toString();
        }

        entry.writeObject(object, info, contentSrc, info.getContentType(), pathSegment, relativePathSegment);

        // write links
        entry.writeServiceLink(baseUrl.toString(), repositoryId);

        entry.writeSelfLink(compileUrl(baseUrl, RESOURCE_ENTRY, info.getId()), info.getId());
        entry.writeEnclosureLink(compileUrl(baseUrl, RESOURCE_ENTRY, info.getId()));
        entry.writeEditLink(compileUrl(baseUrl, RESOURCE_ENTRY, info.getId()));
        entry.writeDescribedByLink(compileUrl(baseUrl, RESOURCE_TYPE, info.getTypeId()));
        entry.writeAllowableActionsLink(compileUrl(baseUrl, RESOURCE_ALLOWABLEACIONS, info.getId()));

        if (info.hasParent()) {
            entry.writeUpLink(compileUrl(baseUrl, RESOURCE_PARENTS, info.getId()), Constants.MEDIATYPE_FEED);
        }

        if (info.getBaseType() == BaseTypeId.CMIS_FOLDER) {
            entry.writeDownLink(compileUrl(baseUrl, RESOURCE_CHILDREN, info.getId()), Constants.MEDIATYPE_FEED);

            if (info.supportsDescendants()) {
                entry.writeDownLink(compileUrl(baseUrl, RESOURCE_DESCENDANTS, info.getId()),
                        Constants.MEDIATYPE_DESCENDANTS);
            }

            if (info.supportsFolderTree()) {
                entry.writeFolderTreeLink(compileUrl(baseUrl, RESOURCE_FOLDERTREE, info.getId()));
            }
        }

        if (info.getVersionSeriesId() != null) {
            UrlBuilder vsUrl = compileUrlBuilder(baseUrl, RESOURCE_VERSIONS, info.getId());
            vsUrl.addParameter(Constants.PARAM_VERSION_SERIES_ID, info.getVersionSeriesId());
            entry.writeVersionHistoryLink(vsUrl.toString());
        }

        if (!info.isCurrentVersion()) {
            UrlBuilder cvUrl = compileUrlBuilder(baseUrl, RESOURCE_ENTRY, info.getId());
            cvUrl.addParameter(Constants.PARAM_RETURN_VERSION, ReturnVersion.LATEST);
            entry.writeEditLink(cvUrl.toString());
        }

        if (info.getBaseType() == BaseTypeId.CMIS_DOCUMENT) {
            entry.writeEditMediaLink(compileUrl(baseUrl, RESOURCE_CONTENT, info.getId()), info.getContentType());
        }

        if (info.getWorkingCopyId() != null) {
            entry.writeWorkingCopyLink(compileUrl(baseUrl, RESOURCE_ENTRY, info.getWorkingCopyId()));
        }

        if (info.getWorkingCopyOriginalId() != null) {
            entry.writeViaLink(compileUrl(baseUrl, RESOURCE_ENTRY, info.getWorkingCopyOriginalId()));
        }

        if (info.getRenditionInfos() != null) {
            for (RenditionInfo ri : info.getRenditionInfos()) {
                UrlBuilder rurl = compileUrlBuilder(baseUrl, RESOURCE_CONTENT, info.getId());
                rurl.addParameter(Constants.PARAM_STREAM_ID, ri.getId());
                entry.writeAlternateLink(rurl.toString(), ri.getContenType(), ri.getKind(), ri.getTitle(),
                        ri.getLength());
            }
        }

        if (info.hasAcl()) {
            entry.writeAclLink(compileUrl(baseUrl, RESOURCE_ACL, info.getId()));
        }

        if (info.supportsPolicies()) {
            entry.writePoliciesLink(compileUrl(baseUrl, RESOURCE_POLICIES, info.getId()));
        }

        if (info.supportsRelationships()) {
            entry.writeRelationshipsLink(compileUrl(baseUrl, RESOURCE_RELATIONSHIPS, info.getId()));
        }

        if (info.getRelationshipSourceIds() != null) {
            for (String id : info.getRelationshipSourceIds()) {
                entry.writeRelationshipSourceLink(compileUrl(baseUrl, RESOURCE_ENTRY, id));
            }
        }

        if (info.getRelationshipTargetIds() != null) {
            for (String id : info.getRelationshipTargetIds()) {
                entry.writeRelationshipTargetLink(compileUrl(baseUrl, RESOURCE_ENTRY, id));
            }
        }

        // write children
        if ((children != null) && (children.size() > 0)) {
            writeObjectChildren(service, entry, info, children, repositoryId, baseUrl);
        }

        // we are done
        entry.endEntry();
    }

    /**
     * Writes the a object entry in a content changes list.
     * 
     * Content changes objects need special treatment because some of them could
     * have been deleted and an object info cannot be generated.
     */
    public static void writeContentChangesObjectEntry(CmisService service, AtomEntry entry, ObjectData object,
            List<ObjectInFolderContainer> children, String repositoryId, String pathSegment,
            String relativePathSegment, UrlBuilder baseUrl, boolean isRoot) throws XMLStreamException, JAXBException {
        if (object == null) {
            throw new CmisRuntimeException("Object not set!");
        }

        ObjectInfo info = null;
        try {
            info = service.getObjectInfo(repositoryId, object.getId());
        } catch (Exception e) {
            // ignore all exceptions
        }

        if (info != null) {
            writeObjectEntry(service, entry, object, children, repositoryId, pathSegment, relativePathSegment, baseUrl,
                    isRoot);
            return;
        }

        // start delete object entry
        entry.startEntry(isRoot);

        // write object
        entry.writeDeletedObject(object);

        // write links
        entry.writeServiceLink(baseUrl.toString(), repositoryId);

        // we are done
        entry.endEntry();
    }

    /**
     * Writes an objects entry children feed.
     */
    public static void writeObjectChildren(CmisService service, AtomEntry entry, ObjectInfo folderInfo,
            List<ObjectInFolderContainer> children, String repositoryId, UrlBuilder baseUrl) throws XMLStreamException,
            JAXBException {

        // start
        AtomFeed feed = new AtomFeed(entry.getWriter());
        feed.startChildren();
        feed.startFeed(false);

        // write basic Atom feed elements
        feed.writeFeedElements(folderInfo.getId(), folderInfo.getAtomId(), folderInfo.getCreatedBy(),
                folderInfo.getName(), folderInfo.getLastModificationDate(), null, null);

        // write links
        feed.writeServiceLink(baseUrl.toString(), repositoryId);

        feed.writeSelfLink(compileUrl(baseUrl, RESOURCE_DESCENDANTS, folderInfo.getId()), null);

        feed.writeViaLink(compileUrl(baseUrl, RESOURCE_ENTRY, folderInfo.getId()));

        feed.writeDownLink(compileUrl(baseUrl, RESOURCE_CHILDREN, folderInfo.getId()), Constants.MEDIATYPE_FEED);

        feed.writeDownLink(compileUrl(baseUrl, RESOURCE_FOLDERTREE, folderInfo.getId()),
                Constants.MEDIATYPE_DESCENDANTS);

        feed.writeUpLink(compileUrl(baseUrl, RESOURCE_PARENTS, folderInfo.getId()), Constants.MEDIATYPE_FEED);

        for (ObjectInFolderContainer container : children) {
            if ((container != null) && (container.getObject() != null)) {
                writeObjectEntry(service, entry, container.getObject().getObject(), container.getChildren(),
                        repositoryId, container.getObject().getPathSegment(), null, baseUrl, false);
            }
        }

        // we are done
        feed.endFeed();
        feed.endChildren();
    }

    /**
     * Writes the a type entry.
     */
    public static void writeTypeEntry(AtomEntry entry, TypeDefinition type, List<TypeDefinitionContainer> children,
            String repositoryId, UrlBuilder baseUrl, boolean isRoot) throws XMLStreamException, JAXBException {

        // start
        entry.startEntry(isRoot);

        // write type
        entry.writeType(type);

        // write links
        entry.writeServiceLink(baseUrl.toString(), repositoryId);

        entry.writeSelfLink(compileUrl(baseUrl, RESOURCE_TYPE, type.getId()), type.getId());
        entry.writeEnclosureLink(compileUrl(baseUrl, RESOURCE_TYPE, type.getId()));
        if (type.getParentTypeId() != null) {
            entry.writeUpLink(compileUrl(baseUrl, RESOURCE_TYPE, type.getParentTypeId()), Constants.MEDIATYPE_ENTRY);
        }
        UrlBuilder downLink = compileUrlBuilder(baseUrl, RESOURCE_TYPES, null);
        downLink.addParameter(Constants.PARAM_TYPE_ID, type.getId());
        entry.writeDownLink(downLink.toString(), Constants.MEDIATYPE_CHILDREN);
        UrlBuilder downLink2 = compileUrlBuilder(baseUrl, RESOURCE_TYPESDESC, null);
        downLink2.addParameter(Constants.PARAM_TYPE_ID, type.getId());
        entry.writeDownLink(downLink2.toString(), Constants.MEDIATYPE_DESCENDANTS);
        entry.writeDescribedByLink(compileUrl(baseUrl, RESOURCE_TYPE, type.getBaseTypeId().value()));

        // write children
        if ((children != null) && (children.size() > 0)) {
            writeTypeChildren(entry, type, children, repositoryId, baseUrl);
        }

        // we are done
        entry.endEntry();
    }

    /**
     * Writes the a type entry children feed.
     */
    private static void writeTypeChildren(AtomEntry entry, TypeDefinition type, List<TypeDefinitionContainer> children,
            String repositoryId, UrlBuilder baseUrl) throws XMLStreamException, JAXBException {

        // start
        AtomFeed feed = new AtomFeed(entry.getWriter());
        feed.startChildren();
        feed.startFeed(false);

        // write basic Atom feed elements
        feed.writeFeedElements(type.getId(), null, TYPE_AUTHOR, type.getDisplayName(), new GregorianCalendar(), null,
                null);

        feed.writeServiceLink(baseUrl.toString(), repositoryId);

        UrlBuilder selfLink = compileUrlBuilder(baseUrl, RESOURCE_TYPESDESC, null);
        selfLink.addParameter(Constants.PARAM_TYPE_ID, type.getId());
        feed.writeSelfLink(selfLink.toString(), type.getId());

        feed.writeViaLink(compileUrl(baseUrl, RESOURCE_TYPE, type.getId()));

        UrlBuilder downLink = compileUrlBuilder(baseUrl, RESOURCE_TYPES, null);
        downLink.addParameter(Constants.PARAM_TYPE_ID, type.getId());
        feed.writeDownLink(downLink.toString(), Constants.MEDIATYPE_FEED);

        if (type.getParentTypeId() != null) {
            feed.writeUpLink(compileUrl(baseUrl, RESOURCE_TYPE, type.getParentTypeId()), Constants.MEDIATYPE_ENTRY);
        }

        // write tree
        for (TypeDefinitionContainer container : children) {
            if ((container != null) && (container.getTypeDefinition() != null)) {
                writeTypeEntry(entry, container.getTypeDefinition(), container.getChildren(), repositoryId, baseUrl,
                        false);
            }
        }

        // we are done
        feed.endFeed();
        feed.endChildren();
    }
}
