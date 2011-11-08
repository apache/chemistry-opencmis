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

import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_ALLOWABLE_ACTIONS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_FILTER;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_MAX_ITEMS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_ORDER_BY;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_RELATIONSHIPS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_RENDITION_FILTER;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_SKIP_COUNT;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.CONTEXT_OBJECT_ID;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBigIntegerParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBooleanParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getEnumParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getStringParameter;

import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.impl.browser.json.JSONConverter;
import org.json.simple.JSONObject;

/**
 * Discovery Service operations.
 */
public class DiscoveryService {

    private DiscoveryService() {
    }

    /**
     * getCheckedOutDocs.
     */
    public static void getCheckedOutDocs(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String folderId = (String) context.get(CONTEXT_OBJECT_ID);
        String filter = getStringParameter(request, PARAM_FILTER);
        String orderBy = getStringParameter(request, PARAM_ORDER_BY);
        Boolean includeAllowableActions = getBooleanParameter(request, PARAM_ALLOWABLE_ACTIONS);
        IncludeRelationships includeRelationships = getEnumParameter(request, PARAM_RELATIONSHIPS,
                IncludeRelationships.class);
        String renditionFilter = getStringParameter(request, PARAM_RENDITION_FILTER);
        BigInteger maxItems = getBigIntegerParameter(request, PARAM_MAX_ITEMS);
        BigInteger skipCount = getBigIntegerParameter(request, PARAM_SKIP_COUNT);

        // execute
        ObjectList checkedout = service.getCheckedOutDocs(repositoryId, folderId, filter, orderBy,
                includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount, null);

        if (checkedout == null) {
            throw new CmisRuntimeException("Checked out list is null!");
        }

        TypeCache typeCache = new TypeCache(repositoryId, service);
        JSONObject jsonCheckedOut = JSONConverter.convert(checkedout, typeCache);

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON(jsonCheckedOut, request, response);
    }

    public static void query(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // get parameters
        String statement = getStringParameter(request, Constants.PARAM_Q);
        Boolean searchAllVersions = getBooleanParameter(request, Constants.PARAM_SEARCH_ALL_VERSIONS);
        Boolean includeAllowableActions = getBooleanParameter(request, Constants.PARAM_ALLOWABLE_ACTIONS);
        IncludeRelationships includeRelationships = getEnumParameter(request, Constants.PARAM_RELATIONSHIPS,
                IncludeRelationships.class);
        String renditionFilter = null;
        BigInteger maxItems = getBigIntegerParameter(request, Constants.PARAM_MAX_ITEMS);
        BigInteger skipCount = getBigIntegerParameter(request, Constants.PARAM_SKIP_COUNT);

        // execute
        ObjectList results = service.query(repositoryId, statement, searchAllVersions, includeAllowableActions,
                includeRelationships, renditionFilter, maxItems, skipCount, null);

        if (results == null) {
            throw new CmisRuntimeException("Results are null!");
        }

        TypeCache typeCache = new TypeCache(repositoryId, service);
        JSONObject jsonResults = JSONConverter.convert(results, typeCache);

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON(jsonResults, request, response);
    }
}
