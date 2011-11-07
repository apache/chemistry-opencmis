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
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_FILTER;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_POLICY_IDS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_RELATIONSHIPS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_RENDITION_FILTER;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_RETURN_VERSION;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_STREAM_ID;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_VERSIONIG_STATE;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CONTENT;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileBaseUrl;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileUrl;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.CONTEXT_OBJECT_ID;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.PARAM_TRANSACTION;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createAddAcl;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createContentStream;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createCookieValue;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createPolicies;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createProperties;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createRemoveAcl;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.setCookie;
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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.ReturnVersion;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.server.impl.browser.json.JSONConverter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
        String transaction = getStringParameter(request, PARAM_TRANSACTION);

        ControlParser cp = new ControlParser(request);

        TypeCache typeCache = new TypeCache(repositoryId, service);

        String newObjectId = service.createDocument(repositoryId, createProperties(cp, null, typeCache), folderId,
                createContentStream(request), versioningState, createPolicies(cp), createAddAcl(cp),
                createRemoveAcl(cp), null);

        ObjectInfo objectInfo = service.getObjectInfo(repositoryId, newObjectId);
        if (objectInfo == null) {
            throw new CmisRuntimeException("Object Info is missing!");
        }

        ObjectData object = objectInfo.getObject();
        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        JSONObject jsonObject = JSONConverter.convert(object, typeCache);

        response.setStatus(HttpServletResponse.SC_CREATED);
        setCookie(request, response, repositoryId, transaction,
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
        String transaction = getStringParameter(request, PARAM_TRANSACTION);

        ControlParser cp = new ControlParser(request);

        TypeCache typeCache = new TypeCache(repositoryId, service);

        String newObjectId = service.createFolder(repositoryId, createProperties(cp, null, typeCache), folderId,
                createPolicies(cp), createAddAcl(cp), createRemoveAcl(cp), null);

        ObjectInfo objectInfo = service.getObjectInfo(repositoryId, newObjectId);
        if (objectInfo == null) {
            throw new CmisRuntimeException("Object Info is missing!");
        }

        ObjectData object = objectInfo.getObject();
        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        JSONObject jsonObject = JSONConverter.convert(object, typeCache);

        response.setStatus(HttpServletResponse.SC_CREATED);
        setCookie(request, response, repositoryId, transaction,
                createCookieValue(HttpServletResponse.SC_CREATED, object.getId(), null, null));

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

        TypeCache typeCache = new TypeCache(repositoryId, service);
        JSONObject jsonObject = JSONConverter.convert(properties, objectId, typeCache);

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

        TypeCache typeCache = new TypeCache(repositoryId, service);
        JSONObject jsonObject = JSONConverter.convert(object, typeCache);

        response.setStatus(HttpServletResponse.SC_OK);
        writeJSON(jsonObject, request, response);
    }

    /**
     * getRenditions.
     */
    @SuppressWarnings("unchecked")
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

        BigInteger offset = context.getOffset();
        BigInteger length = context.getLength();

        // execute
        ContentStream content = service.getContentStream(repositoryId, objectId, streamId, offset, length, null);

        if (content == null || content.getStream() == null) {
            throw new CmisRuntimeException("Content stream is null!");
        }

        String contentType = content.getMimeType();
        if (contentType == null) {
            contentType = MEDIATYPE_OCTETSTREAM;
        }

        // set headers
        if (offset == null && length == null) {
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
     * deleteObject.
     */
    public static void deleteObject(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        Boolean allVersions = getBooleanParameter(request, Constants.PARAM_ALL_VERSIONS);

        service.deleteObject(repositoryId, objectId, allVersions, null);

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
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

        if ((ftd != null) && (ftd.getIds() != null) && (ftd.getIds().size() > 0)) {
            // TODO
        }

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * Delete content stream.
     */
    public static void deleteContentStream(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String changeToken = getStringParameter(request, Constants.PARAM_CHANGE_TOKEN);

        // execute
        Holder<String> objectIdHolder = new Holder<String>(objectId);
        Holder<String> changeTokenHolder = (changeToken == null ? null : new Holder<String>(changeToken));
        service.deleteContentStream(repositoryId, objectIdHolder, changeTokenHolder, null);

        // set headers
        String newObjectId = (objectIdHolder.getValue() == null ? objectId : objectIdHolder.getValue());

        response.setStatus(HttpServletResponse.SC_OK);

        ObjectData object = service.getObject(repositoryId, newObjectId, null, false, IncludeRelationships.NONE,
                "cmis:none", false, false, null);

        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        TypeCache typeCache = new TypeCache(repositoryId, service);
        JSONObject jsonObject = JSONConverter.convert(object, typeCache);

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

        // execute
        Holder<String> objectIdHolder = new Holder<String>(objectId);
        Holder<String> changeTokenHolder = (changeToken == null ? null : new Holder<String>(changeToken));
        service.setContentStream(repositoryId, objectIdHolder, overwriteFlag, changeTokenHolder,
                createContentStream(request), null);

        String newObjectId = (objectIdHolder.getValue() == null ? objectId : objectIdHolder.getValue());

        // set headers
        String location = compileUrl(compileBaseUrl(request, repositoryId), RESOURCE_CONTENT, newObjectId);

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setHeader("Location", location);

        ObjectData object = service.getObject(repositoryId, newObjectId, null, false, IncludeRelationships.NONE,
                "cmis:none", false, false, null);

        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        TypeCache typeCache = new TypeCache(repositoryId, service);
        JSONObject jsonObject = JSONConverter.convert(object, typeCache);

        writeJSON(jsonObject, request, response);
    }
}
