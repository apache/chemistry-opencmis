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
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_DEPTH;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_FILTER;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_MAX_ITEMS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_ORDER_BY;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_PATH_SEGMENT;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_RELATIONSHIPS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_RELATIVE_PATH_SEGMENT;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_RENDITION_FILTER;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_SKIP_COUNT;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.CONTEXT_OBJECT_ID;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBigIntegerParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBooleanParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getEnumParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getStringParameter;

import java.math.BigInteger;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.TypeCache;
import org.apache.chemistry.opencmis.commons.impl.json.JSONArray;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;

/**
 * Navigation Service operations.
 */
public final class NavigationService {

    private NavigationService() {
    }

    /**
     * getChildren.
     */
    public static void getChildren(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String folderId = (String) context.get(CONTEXT_OBJECT_ID);
        String filter = getStringParameter(request, PARAM_FILTER);
        String orderBy = getStringParameter(request, PARAM_ORDER_BY);
        Boolean includeAllowableActions = getBooleanParameter(request, PARAM_ALLOWABLE_ACTIONS);
        IncludeRelationships includeRelationships = getEnumParameter(request, PARAM_RELATIONSHIPS,
                IncludeRelationships.class);
        String renditionFilter = getStringParameter(request, PARAM_RENDITION_FILTER);
        Boolean includePathSegment = getBooleanParameter(request, PARAM_PATH_SEGMENT);
        BigInteger maxItems = getBigIntegerParameter(request, PARAM_MAX_ITEMS);
        BigInteger skipCount = getBigIntegerParameter(request, PARAM_SKIP_COUNT);
        boolean succinct = getBooleanParameter(request, Constants.PARAM_SUCCINCT, false);

        // execute
        ObjectInFolderList children = service.getChildren(repositoryId, folderId, filter, orderBy,
                includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, maxItems,
                skipCount, null);

        if (children == null) {
            throw new CmisRuntimeException("Children are null!");
        }

        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        JSONObject jsonChildren = JSONConverter.convert(children, typeCache, succinct);

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON(jsonChildren, request, response);
    }

    /**
     * getDescendants.
     */
    public static void getDescendants(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String folderId = (String) context.get(CONTEXT_OBJECT_ID);
        BigInteger depth = getBigIntegerParameter(request, PARAM_DEPTH);
        String filter = getStringParameter(request, PARAM_FILTER);
        Boolean includeAllowableActions = getBooleanParameter(request, PARAM_ALLOWABLE_ACTIONS);
        IncludeRelationships includeRelationships = getEnumParameter(request, PARAM_RELATIONSHIPS,
                IncludeRelationships.class);
        String renditionFilter = getStringParameter(request, PARAM_RENDITION_FILTER);
        Boolean includePathSegment = getBooleanParameter(request, PARAM_PATH_SEGMENT);
        boolean succinct = getBooleanParameter(request, Constants.PARAM_SUCCINCT, false);

        // execute
        List<ObjectInFolderContainer> descendants = service.getDescendants(repositoryId, folderId, depth, filter,
                includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, null);

        if (descendants == null) {
            throw new CmisRuntimeException("Descendants are null!");
        }

        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        JSONArray jsonDescendants = new JSONArray();
        for (ObjectInFolderContainer descendant : descendants) {
            jsonDescendants.add(JSONConverter.convert(descendant, typeCache, succinct));
        }

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON(jsonDescendants, request, response);
    }

    /**
     * getFolderTree.
     */
    public static void getFolderTree(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String folderId = (String) context.get(CONTEXT_OBJECT_ID);
        BigInteger depth = getBigIntegerParameter(request, PARAM_DEPTH);
        String filter = getStringParameter(request, PARAM_FILTER);
        Boolean includeAllowableActions = getBooleanParameter(request, PARAM_ALLOWABLE_ACTIONS);
        IncludeRelationships includeRelationships = getEnumParameter(request, PARAM_RELATIONSHIPS,
                IncludeRelationships.class);
        String renditionFilter = getStringParameter(request, PARAM_RENDITION_FILTER);
        Boolean includePathSegment = getBooleanParameter(request, PARAM_PATH_SEGMENT);
        boolean succinct = getBooleanParameter(request, Constants.PARAM_SUCCINCT, false);

        // execute
        List<ObjectInFolderContainer> folderTree = service.getFolderTree(repositoryId, folderId, depth, filter,
                includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, null);

        if (folderTree == null) {
            throw new CmisRuntimeException("Folder Tree are null!");
        }

        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        JSONArray jsonDescendants = new JSONArray();
        for (ObjectInFolderContainer descendant : folderTree) {
            jsonDescendants.add(JSONConverter.convert(descendant, typeCache, succinct));
        }

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON(jsonDescendants, request, response);
    }

    /**
     * getFolderParent.
     */
    public static void getFolderParent(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String filter = getStringParameter(request, PARAM_FILTER);
        boolean succinct = getBooleanParameter(request, Constants.PARAM_SUCCINCT, false);

        // execute
        ObjectData parent = service.getFolderParent(repositoryId, objectId, filter, null);

        if (parent == null) {
            throw new CmisRuntimeException("Parent is null!");
        }

        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        JSONObject jsonObject = JSONConverter.convert(parent, typeCache, false, succinct);

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON(jsonObject, request, response);
    }

    /**
     * getObjectParents.
     */
    public static void getObjectParents(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String filter = getStringParameter(request, PARAM_FILTER);
        Boolean includeAllowableActions = getBooleanParameter(request, PARAM_ALLOWABLE_ACTIONS);
        IncludeRelationships includeRelationships = getEnumParameter(request, PARAM_RELATIONSHIPS,
                IncludeRelationships.class);
        String renditionFilter = getStringParameter(request, PARAM_RENDITION_FILTER);
        Boolean includeRelativePathSegment = getBooleanParameter(request, PARAM_RELATIVE_PATH_SEGMENT);
        boolean succinct = getBooleanParameter(request, Constants.PARAM_SUCCINCT, false);

        // execute
        List<ObjectParentData> parents = service.getObjectParents(repositoryId, objectId, filter,
                includeAllowableActions, includeRelationships, renditionFilter, includeRelativePathSegment, null);

        if (parents == null) {
            throw new CmisRuntimeException("Parents are null!");
        }

        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        JSONArray jsonParents = new JSONArray();
        for (ObjectParentData parent : parents) {
            jsonParents.add(JSONConverter.convert(parent, typeCache, succinct));
        }

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON(jsonParents, request, response);
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
        boolean succinct = getBooleanParameter(request, Constants.PARAM_SUCCINCT, false);

        // execute
        ObjectList checkedout = service.getCheckedOutDocs(repositoryId, folderId, filter, orderBy,
                includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount, null);

        if (checkedout == null) {
            throw new CmisRuntimeException("Checked out list is null!");
        }

        TypeCache typeCache = new ServerTypeCacheImpl(repositoryId, service);
        JSONObject jsonCheckedOut = JSONConverter.convert(checkedout, typeCache, false, succinct);

        response.setStatus(HttpServletResponse.SC_OK);
        BrowserBindingUtils.writeJSON(jsonCheckedOut, request, response);
    }
}
