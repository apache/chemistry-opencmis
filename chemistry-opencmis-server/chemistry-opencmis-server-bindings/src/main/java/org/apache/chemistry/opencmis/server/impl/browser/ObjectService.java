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
package org.apache.chemistry.opencmis.server.impl.browser;

import static org.apache.chemistry.opencmis.commons.impl.Constants.MEDIATYPE_OCTETSTREAM;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_ACL;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_ALLOWABLE_ACTIONS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_DOWNLOAD;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_FILTER;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_POLICY_IDS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_RELATIONSHIPS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_RENDITION_FILTER;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_RETURN_VERSION;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_SOURCE_FOLDER_ID;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_SOURCE_ID;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_STREAM_ID;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_TARGET_FOLDER_ID;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_TOKEN;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_VERSIONIG_STATE;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CONTENT;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileBaseUrl;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileUrl;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.CONTEXT_OBJECT_ID;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.CONTEXT_OBJECT_TYPE_ID;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createAddAcl;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createContentStream;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createCookieValue;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createPolicies;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createProperties;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createRemoveAcl;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.getSimpleObject;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.setCookie;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.setStatus;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.writeEmpty;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.writeJSON;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBigIntegerParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBooleanParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getEnumParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getStringParameter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.LastModifiedContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.MimeHelper;
import org.apache.chemistry.opencmis.commons.impl.ReturnVersion;
import org.apache.chemistry.opencmis.commons.impl.TypeCache;
import org.apache.chemistry.opencmis.commons.impl.json.JSONArray;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.spi.Holder;

/**
 * Object Service operations.
 */
public final class ObjectService {

    private static final int BUFFER_SIZE = 64 * 1024;

    private ObjectService() {
    }

    /**
     * Create document.
     */
    public static void createDocument(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String folderId = (String) context.get(CONTEXT_OBJECT_ID);
        VersioningState versioningState = getEnumParameter(request, PARAM_VERSIONIG_STATE, VersioningState.class);
        String token = getStringParameter(request, PARAM_TOKEN);
        boolean succinct = getBooleanParameter(request, Constants.CONTROL_SUCCINCT, false);

        // execute
        ControlParser cp = new ControlParser(request);
        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);

        String newObjectId = service.createDocument(repositoryId, createProperties(cp, null, typeCache), folderId,
                createContentStream(request), versioningState, createPolicies(cp), createAddAcl(cp),
                createRemoveAcl(cp), null);

        ObjectData object = getSimpleObject(service, repositoryId, newObjectId);
        if (object == null) {
            throw new CmisRuntimeException("New document is null!");
        }

        // return object
        JSONObject jsonObject = JSONConverter.convert(object, typeCache, false, succinct);

        setStatus(request, response, HttpServletResponse.SC_CREATED);
        setCookie(request, response, repositoryId, token,
                createCookieValue(HttpServletResponse.SC_CREATED, object.getId(), null, null));

        writeJSON(jsonObject, request, response);
    }

    /**
     * Create document from source.
     */
    public static void createDocumentFromSource(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String folderId = (String) context.get(CONTEXT_OBJECT_ID);
        String sourceId = getStringParameter(request, PARAM_SOURCE_ID);
        VersioningState versioningState = getEnumParameter(request, PARAM_VERSIONIG_STATE, VersioningState.class);
        String token = getStringParameter(request, PARAM_TOKEN);
        boolean succinct = getBooleanParameter(request, Constants.CONTROL_SUCCINCT, false);

        // execute
        ControlParser cp = new ControlParser(request);
        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);

        ObjectData sourceDoc = getSimpleObject(service, repositoryId, sourceId);
        PropertyData<?> sourceTypeId = sourceDoc.getProperties().getProperties().get(PropertyIds.OBJECT_TYPE_ID);
        if (sourceTypeId == null || sourceTypeId.getFirstValue() == null) {
            throw new CmisRuntimeException("Source object has no type!?!");
        }

        String newObjectId = service.createDocumentFromSource(repositoryId, sourceId,
                createProperties(cp, sourceTypeId.getFirstValue().toString(), typeCache), folderId, versioningState,
                createPolicies(cp), createAddAcl(cp), createRemoveAcl(cp), null);

        ObjectData object = getSimpleObject(service, repositoryId, newObjectId);
        if (object == null) {
            throw new CmisRuntimeException("New document is null!");
        }

        // return object
        JSONObject jsonObject = JSONConverter.convert(object, typeCache, false, succinct);

        setStatus(request, response, HttpServletResponse.SC_CREATED);
        setCookie(request, response, repositoryId, token,
                createCookieValue(HttpServletResponse.SC_CREATED, object.getId(), null, null));

        writeJSON(jsonObject, request, response);
    }

    /**
     * Create folder.
     */
    public static void createFolder(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String folderId = (String) context.get(CONTEXT_OBJECT_ID);
        String token = getStringParameter(request, PARAM_TOKEN);
        boolean succinct = getBooleanParameter(request, Constants.CONTROL_SUCCINCT, false);

        // execute
        ControlParser cp = new ControlParser(request);
        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);

        String newObjectId = service.createFolder(repositoryId, createProperties(cp, null, typeCache), folderId,
                createPolicies(cp), createAddAcl(cp), createRemoveAcl(cp), null);

        ObjectData object = getSimpleObject(service, repositoryId, newObjectId);
        if (object == null) {
            throw new CmisRuntimeException("New folder is null!");
        }

        // return object
        JSONObject jsonObject = JSONConverter.convert(object, typeCache, false, succinct);

        setStatus(request, response, HttpServletResponse.SC_CREATED);
        setCookie(request, response, repositoryId, token,
                createCookieValue(HttpServletResponse.SC_CREATED, object.getId(), null, null));

        writeJSON(jsonObject, request, response);
    }

    /**
     * Create policy.
     */
    public static void createPolicy(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String folderId = (String) context.get(CONTEXT_OBJECT_ID);
        String token = getStringParameter(request, PARAM_TOKEN);
        boolean succinct = getBooleanParameter(request, Constants.CONTROL_SUCCINCT, false);

        // execute
        ControlParser cp = new ControlParser(request);
        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);

        String newObjectId = service.createPolicy(repositoryId, createProperties(cp, null, typeCache), folderId,
                createPolicies(cp), createAddAcl(cp), createRemoveAcl(cp), null);

        ObjectData object = getSimpleObject(service, repositoryId, newObjectId);
        if (object == null) {
            throw new CmisRuntimeException("New policy is null!");
        }

        // return object
        JSONObject jsonObject = JSONConverter.convert(object, typeCache, false, succinct);

        setStatus(request, response, HttpServletResponse.SC_CREATED);
        setCookie(request, response, repositoryId, token,
                createCookieValue(HttpServletResponse.SC_CREATED, object.getId(), null, null));

        writeJSON(jsonObject, request, response);
    }

    /**
     * Create relationship.
     */
    public static void createRelationship(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String token = getStringParameter(request, PARAM_TOKEN);
        boolean succinct = getBooleanParameter(request, Constants.CONTROL_SUCCINCT, false);

        // execute
        ControlParser cp = new ControlParser(request);
        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);

        String newObjectId = service.createRelationship(repositoryId, createProperties(cp, null, typeCache),
                createPolicies(cp), createAddAcl(cp), createRemoveAcl(cp), null);

        ObjectData object = getSimpleObject(service, repositoryId, newObjectId);
        if (object == null) {
            throw new CmisRuntimeException("New relationship is null!");
        }

        // return object
        JSONObject jsonObject = JSONConverter.convert(object, typeCache, false, succinct);

        setStatus(request, response, HttpServletResponse.SC_CREATED);
        setCookie(request, response, repositoryId, token,
                createCookieValue(HttpServletResponse.SC_CREATED, object.getId(), null, null));

        writeJSON(jsonObject, request, response);
    }

    /**
     * updateProperties.
     */
    public static void updateProperties(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String typeId = (String) context.get(CONTEXT_OBJECT_TYPE_ID);
        String changeToken = getStringParameter(request, Constants.PARAM_CHANGE_TOKEN);
        String token = getStringParameter(request, PARAM_TOKEN);
        boolean succinct = getBooleanParameter(request, Constants.CONTROL_SUCCINCT, false);

        // execute
        ControlParser cp = new ControlParser(request);
        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        Holder<String> objectIdHolder = new Holder<String>(objectId);
        Holder<String> changeTokenHolder = (changeToken == null ? null : new Holder<String>(changeToken));

        service.updateProperties(repositoryId, objectIdHolder, changeTokenHolder,
                createProperties(cp, typeId, typeCache), null);

        String newObjectId = (objectIdHolder.getValue() == null ? objectId : objectIdHolder.getValue());

        ObjectData object = getSimpleObject(service, repositoryId, newObjectId);
        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        // return object
        JSONObject jsonObject = JSONConverter.convert(object, typeCache, false, succinct);

        int status = HttpServletResponse.SC_OK;
        if (!objectId.equals(newObjectId)) {
            status = HttpServletResponse.SC_CREATED;
        }

        setStatus(request, response, status);
        setCookie(request, response, repositoryId, token, createCookieValue(status, object.getId(), null, null));

        writeJSON(jsonObject, request, response);
    }

    /**
     * getProperties.
     */
    public static void getProperties(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        ReturnVersion returnVersion = getEnumParameter(request, PARAM_RETURN_VERSION, ReturnVersion.class);
        String filter = getStringParameter(request, PARAM_FILTER);
        boolean succinct = getBooleanParameter(request, Constants.PARAM_SUCCINCT, false);

        // execute
        Properties properties;

        if (returnVersion == ReturnVersion.LATEST || returnVersion == ReturnVersion.LASTESTMAJOR) {
            properties = service.getPropertiesOfLatestVersion(repositoryId, objectId, null,
                    returnVersion == ReturnVersion.LASTESTMAJOR, filter, null);
        } else {
            properties = service.getProperties(repositoryId, objectId, filter, null);
        }

        if (properties == null) {
            throw new CmisRuntimeException("Properties are null!");
        }

        // return object
        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        JSONObject jsonObject = JSONConverter.convert(properties, objectId, typeCache, false, succinct);

        response.setStatus(HttpServletResponse.SC_OK);
        writeJSON(jsonObject, request, response);
    }

    /**
     * getObject.
     */
    public static void getObject(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        ReturnVersion returnVersion = getEnumParameter(request, PARAM_RETURN_VERSION, ReturnVersion.class);
        String filter = getStringParameter(request, PARAM_FILTER);
        Boolean includeAllowableActions = getBooleanParameter(request, PARAM_ALLOWABLE_ACTIONS);
        IncludeRelationships includeRelationships = getEnumParameter(request, PARAM_RELATIONSHIPS,
                IncludeRelationships.class);
        String renditionFilter = getStringParameter(request, PARAM_RENDITION_FILTER);
        Boolean includePolicyIds = getBooleanParameter(request, PARAM_POLICY_IDS);
        Boolean includeAcl = getBooleanParameter(request, PARAM_ACL);
        boolean succinct = getBooleanParameter(request, Constants.PARAM_SUCCINCT, false);

        // execute
        ObjectData object;

        if (returnVersion == ReturnVersion.LATEST || returnVersion == ReturnVersion.LASTESTMAJOR) {
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

        // return object
        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        JSONObject jsonObject = JSONConverter.convert(object, typeCache, false, succinct);

        response.setStatus(HttpServletResponse.SC_OK);
        writeJSON(jsonObject, request, response);
    }

    /**
     * getAllowableActions.
     */
    public static void getAllowableActions(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);

        AllowableActions allowableActions = service.getAllowableActions(repositoryId, objectId, null);

        JSONObject jsonAllowableActions = JSONConverter.convert(allowableActions);

        response.setStatus(HttpServletResponse.SC_OK);
        writeJSON(jsonAllowableActions, request, response);
    }

    /**
     * getRenditions.
     */
    public static void getRenditions(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String renditionFilter = getStringParameter(request, PARAM_RENDITION_FILTER);
        BigInteger maxItems = getBigIntegerParameter(request, Constants.PARAM_MAX_ITEMS);
        BigInteger skipCount = getBigIntegerParameter(request, Constants.PARAM_SKIP_COUNT);

        // execute
        List<RenditionData> renditions = service.getRenditions(repositoryId, objectId, renditionFilter, maxItems,
                skipCount, null);

        JSONArray jsonRenditions = new JSONArray();
        if (renditions != null) {
            for (RenditionData rendition : renditions) {
                jsonRenditions.add(JSONConverter.convert(rendition));
            }
        }

        response.setStatus(HttpServletResponse.SC_OK);
        writeJSON(jsonRenditions, request, response);
    }

    /**
     * getContentStream.
     */
    public static void getContentStream(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String streamId = getStringParameter(request, PARAM_STREAM_ID);
        boolean download = "attachment".equalsIgnoreCase(getStringParameter(request, PARAM_DOWNLOAD));

        BigInteger offset = context.getOffset();
        BigInteger length = context.getLength();

        // execute
        ContentStream content = service.getContentStream(repositoryId, objectId, streamId, offset, length, null);

        if (content == null || content.getStream() == null) {
            throw new CmisRuntimeException("Content stream is null!");
        }

        // check if Last-Modified header should be set
        if (content instanceof LastModifiedContentStream) {
            GregorianCalendar lastModified = ((LastModifiedContentStream) content).getLastModified();
            if (lastModified != null) {
                long lastModifiedSecs = (long) Math.floor((double) lastModified.getTimeInMillis() / 1000);

                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");

                String modifiedSinceStr = request.getHeader("If-Modified-Since");
                if (modifiedSinceStr != null) {
                    try {
                        Date modifiedSince = sdf.parse(modifiedSinceStr);
                        long modifiedSinceSecs = (long) Math.floor((double) modifiedSince.getTime() / 1000);

                        if (modifiedSinceSecs >= lastModifiedSecs) {
                            // close stream
                            content.getStream().close();

                            // send not modified status code
                            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                            response.setContentLength(0);
                            return;
                        }
                    } catch (ParseException e) {
                        // ignore
                    }
                }

                response.setHeader("Last-Modified", sdf.format(lastModifiedSecs * 1000));
            }
        }

        String contentType = content.getMimeType();
        if (contentType == null) {
            contentType = MEDIATYPE_OCTETSTREAM;
        }

        // set headers
        if (offset == null && length == null) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            setStatus(request, response, HttpServletResponse.SC_PARTIAL_CONTENT);
        }
        response.setContentType(contentType);

        String contentFilename = content.getFileName();
        if (contentFilename == null) {
            contentFilename = "content";
        }

        if (download) {
            response.setHeader(MimeHelper.CONTENT_DISPOSITION,
                    MimeHelper.encodeContentDisposition(MimeHelper.DISPOSITION_ATTACHMENT, contentFilename));
        } else {
            response.setHeader(MimeHelper.CONTENT_DISPOSITION,
                    MimeHelper.encodeContentDisposition(MimeHelper.DISPOSITION_INLINE, contentFilename));
        }

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
     * deleteObject.
     */
    public static void deleteObject(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        Boolean allVersions = getBooleanParameter(request, Constants.PARAM_ALL_VERSIONS);

        service.deleteObject(repositoryId, objectId, allVersions, null);

        response.setStatus(HttpServletResponse.SC_OK);
        writeEmpty(request, response);
    }

    /**
     * deleteTree.
     */
    public static void deleteTree(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        Boolean allVersions = getBooleanParameter(request, Constants.PARAM_ALL_VERSIONS);
        UnfileObject unfileObjects = getEnumParameter(request, Constants.PARAM_UNFILE_OBJECTS, UnfileObject.class);
        Boolean continueOnFailure = getBooleanParameter(request, Constants.PARAM_CONTINUE_ON_FAILURE);

        // execute
        FailedToDeleteData ftd = service.deleteTree(repositoryId, objectId, allVersions, unfileObjects,
                continueOnFailure, null);

        response.setStatus(HttpServletResponse.SC_OK);

        if ((ftd != null) && (ftd.getIds() != null) && (ftd.getIds().size() > 0)) {
            JSONObject jsonObject = JSONConverter.convert(ftd);
            writeJSON(jsonObject, request, response);
            return;
        }

        writeEmpty(request, response);
    }

    /**
     * Delete content stream.
     */
    public static void deleteContentStream(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String changeToken = getStringParameter(request, Constants.PARAM_CHANGE_TOKEN);
        boolean succinct = getBooleanParameter(request, Constants.CONTROL_SUCCINCT, false);

        // execute
        Holder<String> objectIdHolder = new Holder<String>(objectId);
        Holder<String> changeTokenHolder = (changeToken == null ? null : new Holder<String>(changeToken));
        service.deleteContentStream(repositoryId, objectIdHolder, changeTokenHolder, null);

        String newObjectId = (objectIdHolder.getValue() == null ? objectId : objectIdHolder.getValue());

        ObjectData object = getSimpleObject(service, repositoryId, newObjectId);
        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        response.setStatus(HttpServletResponse.SC_OK);

        // return object
        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        JSONObject jsonObject = JSONConverter.convert(object, typeCache, false, succinct);

        writeJSON(jsonObject, request, response);
    }

    /**
     * Set content stream.
     */
    public static void setContentStream(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String changeToken = getStringParameter(request, Constants.PARAM_CHANGE_TOKEN);
        Boolean overwriteFlag = getBooleanParameter(request, Constants.PARAM_OVERWRITE_FLAG);
        boolean succinct = getBooleanParameter(request, Constants.CONTROL_SUCCINCT, false);

        // execute
        Holder<String> objectIdHolder = new Holder<String>(objectId);
        Holder<String> changeTokenHolder = (changeToken == null ? null : new Holder<String>(changeToken));
        service.setContentStream(repositoryId, objectIdHolder, overwriteFlag, changeTokenHolder,
                createContentStream(request), null);

        String newObjectId = (objectIdHolder.getValue() == null ? objectId : objectIdHolder.getValue());

        ObjectData object = getSimpleObject(service, repositoryId, newObjectId);
        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        // set headers
        String location = compileUrl(compileBaseUrl(request, repositoryId), RESOURCE_CONTENT, newObjectId);

        setStatus(request, response, HttpServletResponse.SC_CREATED);
        response.setHeader("Location", location);

        // return object
        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        JSONObject jsonObject = JSONConverter.convert(object, typeCache, false, succinct);

        writeJSON(jsonObject, request, response);
    }

    /**
     * moveObject.
     */
    public static void moveObject(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String targetFolderId = getStringParameter(request, PARAM_TARGET_FOLDER_ID);
        String sourceFolderId = getStringParameter(request, PARAM_SOURCE_FOLDER_ID);
        boolean succinct = getBooleanParameter(request, Constants.CONTROL_SUCCINCT, false);

        // execute
        Holder<String> objectIdHolder = new Holder<String>(objectId);
        service.moveObject(repositoryId, objectIdHolder, targetFolderId, sourceFolderId, null);

        String newObjectId = (objectIdHolder.getValue() == null ? objectId : objectIdHolder.getValue());

        ObjectData object = getSimpleObject(service, repositoryId, newObjectId);
        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        // set headers
        String location = compileUrl(compileBaseUrl(request, repositoryId), RESOURCE_CONTENT, newObjectId);

        setStatus(request, response, HttpServletResponse.SC_CREATED);
        response.setHeader("Location", location);

        // return object
        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        JSONObject jsonObject = JSONConverter.convert(object, typeCache, false, succinct);

        writeJSON(jsonObject, request, response);
    }
}
