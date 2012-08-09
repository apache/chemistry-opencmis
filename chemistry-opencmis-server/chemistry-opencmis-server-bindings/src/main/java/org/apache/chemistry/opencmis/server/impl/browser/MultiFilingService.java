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

import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_FOLDER_ID;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CONTENT;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileBaseUrl;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileUrl;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.CONTEXT_OBJECT_ID;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.getSimpleObject;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.setStatus;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.writeJSON;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBooleanParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getStringParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.TypeCache;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.spi.Holder;

/**
 * MultiFiling Service operations.
 */
public class MultiFilingService {

    /*
     * addObjectToFolder.
     */
    public static void addObjectToFolder(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String folderId = getStringParameter(request, PARAM_FOLDER_ID);
        Boolean allVersions = getBooleanParameter(request, Constants.PARAM_ALL_VERSIONS);
        boolean succinct = getBooleanParameter(request, Constants.PARAM_SUCCINCT, false);

        // execute
        Holder<String> objectIdHolder = new Holder<String>(objectId);
        service.addObjectToFolder(repositoryId, objectId, folderId, allVersions, null);

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

    /*
     * removeObjectFromFolder.
     */
    public static void removeObjectFromFolder(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String folderId = getStringParameter(request, PARAM_FOLDER_ID);
        boolean succinct = getBooleanParameter(request, Constants.CONTROL_SUCCINCT, false);

        // execute
        Holder<String> objectIdHolder = new Holder<String>(objectId);
        service.removeObjectFromFolder(repositoryId, objectId, folderId, null);

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
