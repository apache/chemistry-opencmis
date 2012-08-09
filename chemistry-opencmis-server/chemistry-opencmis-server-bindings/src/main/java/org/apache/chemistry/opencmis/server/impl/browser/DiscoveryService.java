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

import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_ACL;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_CHANGE_LOG_TOKEN;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_FILTER;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_POLICY_IDS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_PROPERTIES;
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
import org.apache.chemistry.opencmis.commons.impl.JSONConstants;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.TypeCache;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.spi.Holder;

/**
 * Discovery Service operations.
 */
public class DiscoveryService {

    private DiscoveryService() {
    }

    /**
     * query.
     */
    public static void query(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // get parameters
        String statement = getStringParameter(request, Constants.PARAM_STATEMENT);
        if (statement == null || statement.length() == 0) {
            statement = getStringParameter(request, Constants.PARAM_Q);
        }
        Boolean searchAllVersions = getBooleanParameter(request, Constants.PARAM_SEARCH_ALL_VERSIONS);
        Boolean includeAllowableActions = getBooleanParameter(request, Constants.PARAM_ALLOWABLE_ACTIONS);
        IncludeRelationships includeRelationships = getEnumParameter(request, Constants.PARAM_RELATIONSHIPS,
                IncludeRelationships.class);
        String renditionFilter = getStringParameter(request, Constants.PARAM_RENDITION_FILTER);
        BigInteger maxItems = getBigIntegerParameter(request, Constants.PARAM_MAX_ITEMS);
        BigInteger skipCount = getBigIntegerParameter(request, Constants.PARAM_SKIP_COUNT);
        boolean succinct = getBooleanParameter(request, Constants.PARAM_SUCCINCT, false);

        // execute
        ObjectList results = service.query(repositoryId, statement, searchAllVersions, includeAllowableActions,
                includeRelationships, renditionFilter, maxItems, skipCount, null);

        if (results == null) {
            throw new CmisRuntimeException("Results are null!");
        }

        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        JSONObject jsonResults = JSONConverter.convert(results, typeCache, true, succinct);

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON(jsonResults, request, response);
    }

    /**
     * getContentChanges.
     */
    public static void getContentChanges(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String changeLogToken = getStringParameter(request, PARAM_CHANGE_LOG_TOKEN);
        Boolean includeProperties = getBooleanParameter(request, PARAM_PROPERTIES);
        String filter = getStringParameter(request, PARAM_FILTER);
        Boolean includePolicyIds = getBooleanParameter(request, PARAM_POLICY_IDS);
        Boolean includeAcl = getBooleanParameter(request, PARAM_ACL);
        BigInteger maxItems = getBigIntegerParameter(request, Constants.PARAM_MAX_ITEMS);
        boolean succinct = getBooleanParameter(request, Constants.PARAM_SUCCINCT, false);

        Holder<String> changeLogTokenHolder = new Holder<String>(changeLogToken);
        ObjectList changes = service.getContentChanges(repositoryId, changeLogTokenHolder, includeProperties, filter,
                includePolicyIds, includeAcl, maxItems, null);

        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        JSONObject jsonChanges = JSONConverter.convert(changes, typeCache, false, succinct);
        jsonChanges.put(JSONConstants.JSON_OBJECTLIST_CHANGE_LOG_TOKEN, changeLogTokenHolder.getValue());

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON(jsonChanges, request, response);
    }
}
