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

import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_ENTRY;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileBaseUrl;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileUrl;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.getNamespaces;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.writeObjectEntry;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getEnumParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getStringParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;

/**
 * MultiFiling Service operations.
 */
public class MultiFilingService {

    private MultiFilingService() {
    }

    /**
     * Remove object from folder.
     */
    public static void removeObjectFromFolder(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String removeFrom = getStringParameter(request, Constants.PARAM_REMOVE_FROM);

        AtomEntryParser parser = new AtomEntryParser(context.getTempDirectory(), context.getMemoryThreshold(),
                context.getMaxContentSize());
        parser.setIgnoreAtomContentSrc(true); // needed for some clients
        parser.parse(request.getInputStream());

        String objectId = parser.getId();

        if (objectId == null && removeFrom == null) {
            // create unfiled object
            createUnfiledObject(context, service, repositoryId, request, response, parser);
            return;
        }

        // execute
        service.removeObjectFromFolder(repositoryId, objectId, removeFrom, null);

        ObjectInfo objectInfo = service.getObjectInfo(repositoryId, objectId);
        if (objectInfo == null) {
            throw new CmisRuntimeException("Object Info is missing!");
        }

        ObjectData object = objectInfo.getObject();
        if (object == null) {
            throw new CmisRuntimeException("Object is null!");
        }

        if (object.getId() == null) {
            throw new CmisRuntimeException("Object Id is null!");
        }

        // set headers
        UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setContentType(Constants.MEDIATYPE_ENTRY);
        response.setHeader("Location", compileUrl(baseUrl, RESOURCE_ENTRY, object.getId()));

        // write XML
        AtomEntry entry = new AtomEntry();
        entry.startDocument(response.getOutputStream(), getNamespaces(service));
        writeObjectEntry(service, entry, object, null, repositoryId, null, null, baseUrl, true);
        entry.endDocument();
    }

    /**
     * Create unfiled object.
     * 
     * (Creation of unfiled objects via AtomPub is not defined in the CMIS 1.0
     * specification. This implementation follow the CMIS 1.1 draft.)
     */
    private static void createUnfiledObject(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response, AtomEntryParser parser) throws Exception {
        // get additional parameters
        VersioningState versioningState = getEnumParameter(request, Constants.PARAM_VERSIONIG_STATE,
                VersioningState.class);

        // create
        String newObjectId = service.create(repositoryId, parser.getProperties(), null, parser.getContentStream(),
                versioningState, parser.getPolicyIds(), null);

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
}
