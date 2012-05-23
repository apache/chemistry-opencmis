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

import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CONTENT;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_ENTRY;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileBaseUrl;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileUrl;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.getNamespaces;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.writeObjectEntry;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBooleanParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getEnumParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getStringParameter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.MimeHelper;
import org.apache.chemistry.opencmis.commons.impl.ReturnVersion;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.spi.Holder;

/**
 * Object Service operations.
 */
public final class ObjectService {

    private static final int BUFFER_SIZE = 64 * 1024;

    private ObjectService() {
    }

    /**
     * Create.
     */
    public static void create(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String folderId = getStringParameter(request, Constants.PARAM_ID);
        String sourceFolderId = getStringParameter(request, Constants.PARAM_SOURCE_FOLDER_ID);
        VersioningState versioningState = getEnumParameter(request, Constants.PARAM_VERSIONIG_STATE,
                VersioningState.class);

        AtomEntryParser parser = new AtomEntryParser(context.getTempDirectory(), context.getMemoryThreshold(),
                context.getMaxContentSize());
        parser.setIgnoreAtomContentSrc(true); // needed for some clients
        parser.parse(request.getInputStream());

        String objectId = parser.getId();

        // execute
        String newObjectId = null;

        if (objectId == null) {
            // create
            newObjectId = service.create(repositoryId, parser.getProperties(), folderId, parser.getContentStream(),
                    versioningState, parser.getPolicyIds(), null);
        } else {
            if ((sourceFolderId == null) || (sourceFolderId.trim().length() == 0)) {
                // addObjectToFolder
                service.addObjectToFolder(repositoryId, objectId, folderId, null, null);
                newObjectId = objectId;
            } else {
                // move
                Holder<String> objectIdHolder = new Holder<String>(objectId);
                service.moveObject(repositoryId, objectIdHolder, folderId, sourceFolderId, null);
                newObjectId = objectIdHolder.getValue();
            }
        }

        ObjectInfo objectInfo = service.getObjectInfo(repositoryId, newObjectId);
        if (objectInfo == null) {
            throw new CmisRuntimeException("Object Info is missing!");
        }

        ObjectData object = objectInfo.getObject();
        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        // set headers
        UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setContentType(Constants.MEDIATYPE_ENTRY);
        response.setHeader("Location", compileUrl(baseUrl, RESOURCE_ENTRY, newObjectId));

        // write XML
        AtomEntry entry = new AtomEntry();
        entry.startDocument(response.getOutputStream(), getNamespaces(service));
        writeObjectEntry(service, entry, object, null, repositoryId, null, null, baseUrl, true);
        entry.endDocument();
    }

    /**
     * Create relationship.
     */
    public static void createRelationship(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        AtomEntryParser parser = new AtomEntryParser(request.getInputStream(), context.getTempDirectory(),
                context.getMemoryThreshold(), context.getMaxContentSize());

        // execute
        String newObjectId = service.createRelationship(repositoryId, parser.getProperties(), parser.getPolicyIds(),
                null, null, null);

        ObjectInfo objectInfo = service.getObjectInfo(repositoryId, newObjectId);
        if (objectInfo == null) {
            throw new CmisRuntimeException("Object Info is missing!");
        }

        ObjectData object = objectInfo.getObject();
        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        // set headers
        UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setContentType(Constants.MEDIATYPE_ENTRY);
        response.setHeader("Location", compileUrl(baseUrl, RESOURCE_ENTRY, newObjectId));

        // write XML
        AtomEntry entry = new AtomEntry();
        entry.startDocument(response.getOutputStream(), getNamespaces(service));
        writeObjectEntry(service, entry, object, null, repositoryId, null, null, baseUrl, true);
        entry.endDocument();
    }

    /**
     * Delete object.
     */
    public static void deleteObject(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) {
        // get parameters
        String objectId = getStringParameter(request, Constants.PARAM_ID);
        Boolean allVersions = getBooleanParameter(request, Constants.PARAM_ALL_VERSIONS);

        // execute
        service.deleteObjectOrCancelCheckOut(repositoryId, objectId, allVersions, null);

        // set headers
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * Delete content stream.
     */
    public static void deleteContentStream(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) {
        // get parameters
        String objectId = getStringParameter(request, Constants.PARAM_ID);
        String changeToken = getStringParameter(request, Constants.PARAM_CHANGE_TOKEN);

        // execute
        service.deleteContentStream(repositoryId, new Holder<String>(objectId), changeToken == null ? null
                : new Holder<String>(changeToken), null);

        // set headers
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * Set content stream.
     */
    public static void setContentStream(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = getStringParameter(request, Constants.PARAM_ID);
        String changeToken = getStringParameter(request, Constants.PARAM_CHANGE_TOKEN);
        Boolean overwriteFlag = getBooleanParameter(request, Constants.PARAM_OVERWRITE_FLAG);

        ContentStreamImpl contentStream = new ContentStreamImpl();
        contentStream.setStream(request.getInputStream());
        contentStream.setMimeType(request.getHeader("Content-Type"));
        String lengthStr = request.getHeader("Content-Length");
        if (lengthStr != null) {
            try {
                contentStream.setLength(new BigInteger(lengthStr));
            } catch (NumberFormatException e) {
            }
        }
        String contentDisposition = request.getHeader(MimeHelper.CONTENT_DISPOSITION);
        if (contentDisposition != null) {
            contentStream.setFileName(MimeHelper.decodeContentDispositionFilename(contentDisposition));
        }

        // execute
        Holder<String> objectIdHolder = new Holder<String>(objectId);
        service.setContentStream(repositoryId, objectIdHolder, overwriteFlag, changeToken == null ? null
                : new Holder<String>(changeToken), contentStream, null);

        // set headers
        String newObjectId = (objectIdHolder.getValue() == null ? objectId : objectIdHolder.getValue());
        String location = compileUrl(compileBaseUrl(request, repositoryId), RESOURCE_CONTENT, newObjectId);

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setHeader("Content-Location", location);
        response.setHeader("Location", location);
    }

    /**
     * Delete tree.
     */
    public static void deleteTree(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String folderId = getStringParameter(request, Constants.PARAM_ID);
        Boolean allVersions = getBooleanParameter(request, Constants.PARAM_ALL_VERSIONS);
        UnfileObject unfileObjects = getEnumParameter(request, Constants.PARAM_UNFILE_OBJECTS, UnfileObject.class);
        Boolean continueOnFailure = getBooleanParameter(request, Constants.PARAM_CONTINUE_ON_FAILURE);

        // execute
        FailedToDeleteData ftd = service.deleteTree(repositoryId, folderId, allVersions, unfileObjects,
                continueOnFailure, null);

        if ((ftd != null) && (ftd.getIds() != null) && (ftd.getIds().size() > 0)) {
            // print ids that could not be deleted
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain");

            PrintWriter pw = response.getWriter();

            pw.println("Failed to delete the following objects:");
            for (String id : ftd.getIds()) {
                pw.println(id);
            }

            pw.flush();

            return;
        }

        // set headers
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * getObject.
     */
    public static void getObject(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = getStringParameter(request, Constants.PARAM_ID);
        ReturnVersion returnVersion = getEnumParameter(request, Constants.PARAM_RETURN_VERSION, ReturnVersion.class);
        String filter = getStringParameter(request, Constants.PARAM_FILTER);
        Boolean includeAllowableActions = getBooleanParameter(request, Constants.PARAM_ALLOWABLE_ACTIONS);
        IncludeRelationships includeRelationships = getEnumParameter(request, Constants.PARAM_RELATIONSHIPS,
                IncludeRelationships.class);
        String renditionFilter = getStringParameter(request, Constants.PARAM_RENDITION_FILTER);
        Boolean includePolicyIds = getBooleanParameter(request, Constants.PARAM_POLICY_IDS);
        Boolean includeAcl = getBooleanParameter(request, Constants.PARAM_ACL);

        // execute
        ObjectData object = null;

        if ((returnVersion == ReturnVersion.LATEST) || (returnVersion == ReturnVersion.LASTESTMAJOR)) {
            object = service.getObjectOfLatestVersion(repositoryId, objectId, null,
                    returnVersion == ReturnVersion.LASTESTMAJOR, filter, includeAllowableActions, includeRelationships,
                    renditionFilter, includePolicyIds, includeAcl, null);
        } else {
            object = service.getObject(repositoryId, objectId, filter, includeAllowableActions, includeRelationships,
                    renditionFilter, includePolicyIds, includeAcl, null);
        }

        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        ObjectInfo objectInfo = service.getObjectInfo(repositoryId, objectId);
        if (objectInfo == null) {
            throw new CmisRuntimeException("Object Info is missing!");
        }

        // set headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(Constants.MEDIATYPE_ENTRY);

        // write XML
        UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

        AtomEntry entry = new AtomEntry();
        entry.startDocument(response.getOutputStream(), getNamespaces(service));
        writeObjectEntry(service, entry, object, null, repositoryId, null, null, baseUrl, true);
        entry.endDocument();
    }

    /**
     * objectByPath URI template.
     */
    public static void getObjectByPath(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String path = getStringParameter(request, Constants.PARAM_PATH);
        String filter = getStringParameter(request, Constants.PARAM_FILTER);
        Boolean includeAllowableActions = getBooleanParameter(request, Constants.PARAM_ALLOWABLE_ACTIONS);
        IncludeRelationships includeRelationships = getEnumParameter(request, Constants.PARAM_RELATIONSHIPS,
                IncludeRelationships.class);
        String renditionFilter = getStringParameter(request, Constants.PARAM_RENDITION_FILTER);
        Boolean includePolicyIds = getBooleanParameter(request, Constants.PARAM_POLICY_IDS);
        Boolean includeAcl = getBooleanParameter(request, Constants.PARAM_ACL);

        // execute
        ObjectData object = service.getObjectByPath(repositoryId, path, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePolicyIds, includeAcl, null);

        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        ObjectInfo objectInfo = service.getObjectInfo(repositoryId, object.getId());
        if (objectInfo == null) {
            throw new CmisRuntimeException("Object Info is missing!");
        }

        // set headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(Constants.MEDIATYPE_ENTRY);

        // write XML
        UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

        AtomEntry entry = new AtomEntry();
        entry.startDocument(response.getOutputStream(), getNamespaces(service));
        writeObjectEntry(service, entry, object, null, repositoryId, null, null, baseUrl, true);
        entry.endDocument();
    }

    /**
     * Allowable Actions.
     */
    public static void getAllowableActions(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = getStringParameter(request, Constants.PARAM_ID);

        // execute
        AllowableActions allowableActions = service.getAllowableActions(repositoryId, objectId, null);

        if (allowableActions == null) {
            throw new CmisRuntimeException("Allowable Actions is null!");
        }

        // set headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(Constants.MEDIATYPE_ALLOWABLEACTION);

        // write XML
        AllowableActionsDocument allowableActionsDocument = new AllowableActionsDocument();
        allowableActionsDocument.writeAllowableActions(allowableActions, response.getOutputStream());
    }

    /**
     * getContentStream.
     */
    public static void getContentStream(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = getStringParameter(request, Constants.PARAM_ID);
        String streamId = getStringParameter(request, Constants.PARAM_STREAM_ID);

        BigInteger offset = context.getOffset();
        BigInteger length = context.getLength();

        // execute
        ContentStream content = service.getContentStream(repositoryId, objectId, streamId, offset, length, null);

        if ((content == null) || (content.getStream() == null)) {
            throw new CmisRuntimeException("Content stream is null!");
        }

        String contentType = content.getMimeType();
        if (contentType == null) {
            contentType = Constants.MEDIATYPE_OCTETSTREAM;
        }

        // set headers
        if ((offset == null) && (length == null)) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        }
        response.setContentType(contentType);

        // send content
        InputStream in = new BufferedInputStream(content.getStream(), BUFFER_SIZE);
        OutputStream out = new BufferedOutputStream(response.getOutputStream());

        byte[] buffer = new byte[BUFFER_SIZE];
        int b;
        while ((b = in.read(buffer)) > -1) {
            out.write(buffer, 0, b);
        }

        in.close();
        out.flush();
    }

    /**
     * UpdateProperties.
     */
    public static void updateProperties(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = getStringParameter(request, Constants.PARAM_ID);
        Boolean checkin = getBooleanParameter(request, Constants.PARAM_CHECK_IN);
        String checkinComment = getStringParameter(request, Constants.PARAM_CHECKIN_COMMENT);
        Boolean major = getBooleanParameter(request, Constants.PARAM_MAJOR);

        AtomEntryParser parser = new AtomEntryParser(request.getInputStream(), context.getTempDirectory(),
                context.getMemoryThreshold(), context.getMaxContentSize());

        // execute
        Holder<String> objectIdHolder = new Holder<String>(objectId);

        if ((checkin != null) && (checkin.booleanValue())) {
            service.checkIn(repositoryId, objectIdHolder, major, parser.getProperties(), parser.getContentStream(),
                    checkinComment, parser.getPolicyIds(), null, null, null);
        } else {
            String changeToken = extractChangeToken(parser.getProperties());

            service.updateProperties(repositoryId, objectIdHolder, changeToken == null ? null : new Holder<String>(
                    changeToken), parser.getProperties(), null);
        }

        ObjectInfo objectInfo = service.getObjectInfo(repositoryId, objectIdHolder.getValue());
        if (objectInfo == null) {
            throw new CmisRuntimeException("Object Info is missing!");
        }

        ObjectData object = objectInfo.getObject();
        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        // set headers
        UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);
        String location = compileUrl(baseUrl, RESOURCE_ENTRY, objectIdHolder.getValue());

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setContentType(Constants.MEDIATYPE_ENTRY);
        response.setHeader("Content-Location", location);
        response.setHeader("Location", location);

        // write XML
        AtomEntry entry = new AtomEntry();
        entry.startDocument(response.getOutputStream(), getNamespaces(service));
        writeObjectEntry(service, entry, object, null, repositoryId, null, null, baseUrl, true);
        entry.endDocument();
    }

    /**
     * Gets the change token from a property set.
     */
    private static String extractChangeToken(Properties properties) {
        if (properties == null) {
            return null;
        }

        Map<String, PropertyData<?>> propertiesMap = properties.getProperties();
        if (propertiesMap == null) {
            return null;
        }

        PropertyData<?> changeLogProperty = propertiesMap.get(PropertyIds.CHANGE_TOKEN);
        if (!(changeLogProperty instanceof PropertyString)) {
            return null;
        }

        return ((PropertyString) changeLogProperty).getFirstValue();
    }
}
