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

import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_FILTER;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_POLICY_ID;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.CONTEXT_OBJECT_ID;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.getSimpleObject;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.writeJSON;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBooleanParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getStringParameter;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.TypeCache;
import org.apache.chemistry.opencmis.commons.impl.json.JSONArray;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;

/**
 * Policy Service operations.
 */
public class PolicyService {

    /**
     * getAppliedPolicies.
     */
    public static void getAppliedPolicies(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String filter = getStringParameter(request, PARAM_FILTER);
        boolean succinct = getBooleanParameter(request, Constants.PARAM_SUCCINCT, false);

        // execute
        List<ObjectData> policies = service.getAppliedPolicies(repositoryId, objectId, filter, null);

        JSONArray jsonPolicies = new JSONArray();
        if (policies != null) {
            TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
            for (ObjectData policy : policies) {
                jsonPolicies.add(JSONConverter.convert(policy, typeCache, false, succinct));
            }
        }

        response.setStatus(HttpServletResponse.SC_OK);
        writeJSON(jsonPolicies, request, response);
    }

    /**
     * applyPolicy.
     */
    public static void applyPolicy(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String policyId = getStringParameter(request, PARAM_POLICY_ID);
        boolean succinct = getBooleanParameter(request, Constants.CONTROL_SUCCINCT, false);

        // execute
        service.applyPolicy(repositoryId, policyId, objectId, null);

        ObjectData object = getSimpleObject(service, repositoryId, objectId);
        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        // return object
        response.setStatus(HttpServletResponse.SC_OK);

        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        JSONObject jsonObject = JSONConverter.convert(object, typeCache, false, succinct);

        writeJSON(jsonObject, request, response);
    }

    /**
     * removePolicy.
     */
    public static void removePolicy(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String policyId = getStringParameter(request, PARAM_POLICY_ID);
        boolean succinct = getBooleanParameter(request, Constants.CONTROL_SUCCINCT, false);

        // execute
        service.removePolicy(repositoryId, policyId, objectId, null);

        ObjectData object = getSimpleObject(service, repositoryId, objectId);
        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        // return object
        response.setStatus(HttpServletResponse.SC_OK);

        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        JSONObject jsonObject = JSONConverter.convert(object, typeCache, false, succinct);

        writeJSON(jsonObject, request, response);
    }
}
