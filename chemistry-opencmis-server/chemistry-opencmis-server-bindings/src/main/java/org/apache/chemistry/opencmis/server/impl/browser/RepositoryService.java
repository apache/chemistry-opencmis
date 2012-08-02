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

import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_DEPTH;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_MAX_ITEMS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_PROPERTY_DEFINITIONS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_SKIP_COUNT;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_TOKEN;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_TYPE_ID;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBigIntegerParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBooleanParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getStringParameter;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.json.JSONArray;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.impl.json.JSONValue;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;

/**
 * Repository Service operations.
 */
public final class RepositoryService {

    private RepositoryService() {
    }

    /**
     * getRepositories.
     */
    public static void getRepositories(CallContext context, CmisService service, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // execute
        List<RepositoryInfo> infoDataList = service.getRepositoryInfos(null);

        JSONObject result = new JSONObject();
        for (RepositoryInfo ri : infoDataList) {
            String repositoryUrl = BrowserBindingUtils.compileRepositoryUrl(request, ri.getId()).toString();
            String rootUrl = BrowserBindingUtils.compileRootUrl(request, ri.getId()).toString();

            result.put(ri.getId(), JSONConverter.convert(ri, repositoryUrl, rootUrl));
        }

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON(result, request, response);
    }

    /**
     * getRepositoryInfo.
     */
    public static void getRepositoryInfo(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // execute
        RepositoryInfo ri = service.getRepositoryInfo(repositoryId, null);

        String repositoryUrl = BrowserBindingUtils.compileRepositoryUrl(request, ri.getId()).toString();
        String rootUrl = BrowserBindingUtils.compileRootUrl(request, ri.getId()).toString();

        JSONObject result = new JSONObject();
        result.put(ri.getId(), JSONConverter.convert(ri, repositoryUrl, rootUrl));

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON(result, request, response);
    }

    /**
     * getLastResult.
     */
    public static void getLastResult(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {

        String token = getStringParameter(request, PARAM_TOKEN);
        String cookieName = BrowserBindingUtils.getCookieName(token);
        String cookieValue = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    cookieValue = URLDecoder.decode(cookie.getValue(), "UTF-8");
                    break;
                }
            }
        }

        try {
            if (cookieValue == null) {
                cookieValue = BrowserBindingUtils.createCookieValue(0, null,
                        CmisInvalidArgumentException.EXCEPTION_NAME, "Unknown transaction!");
            } else {
                JSONValue.parse(cookieValue);
            }
        } catch (Exception pe) {
            cookieValue = BrowserBindingUtils.createCookieValue(0, null, CmisRuntimeException.EXCEPTION_NAME,
                    "Cookie pasring error!");
        }

        BrowserBindingUtils.deleteCookie(request, response, repositoryId, token);

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON((JSONObject) JSONValue.parse(cookieValue), request, response);
    }

    /**
     * getTypeChildren.
     */
    public static void getTypeChildren(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String typeId = getStringParameter(request, PARAM_TYPE_ID);
        boolean includePropertyDefinitions = getBooleanParameter(request, PARAM_PROPERTY_DEFINITIONS, false);
        BigInteger maxItems = getBigIntegerParameter(request, PARAM_MAX_ITEMS);
        BigInteger skipCount = getBigIntegerParameter(request, PARAM_SKIP_COUNT);

        // execute
        TypeDefinitionList typeList = service.getTypeChildren(repositoryId, typeId, includePropertyDefinitions,
                maxItems, skipCount, null);
        JSONObject jsonTypeList = JSONConverter.convert(typeList);

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON(jsonTypeList, request, response);
    }

    public static void getTypeDescendants(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String typeId = getStringParameter(request, PARAM_TYPE_ID);
        BigInteger depth = getBigIntegerParameter(request, PARAM_DEPTH);
        boolean includePropertyDefinitions = getBooleanParameter(request, PARAM_PROPERTY_DEFINITIONS, false);

        // execute
        List<TypeDefinitionContainer> typeTree = service.getTypeDescendants(repositoryId, typeId, depth,
                includePropertyDefinitions, null);

        if (typeTree == null) {
            throw new CmisRuntimeException("Type tree is null!");
        }

        JSONArray jsonTypeTree = new JSONArray();
        for (TypeDefinitionContainer container : typeTree) {
            jsonTypeTree.add(JSONConverter.convert(container));
        }

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON(jsonTypeTree, request, response);
    }

    public static void getTypeDefinition(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String typeId = getStringParameter(request, PARAM_TYPE_ID);

        // execute
        TypeDefinition type = service.getTypeDefinition(repositoryId, typeId, null);
        JSONObject jsonType = JSONConverter.convert(type);

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON(jsonType, request, response);
    }
}
