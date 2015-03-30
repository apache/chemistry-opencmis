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
package org.apache.chemistry.opencmis.tck.tests.basics;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.OK;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.AclCapabilities;
import org.apache.chemistry.opencmis.commons.data.ExtensionFeature;
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Repository info test.
 */
public class RepositoryInfoTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Repository Info Test");
        setDescription("Gets and checks the repository info.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult success;
        CmisTestResult failure;

        RepositoryInfo ri = getRepositoryInfo(session);

        // ID
        success = createResult(OK, "Repository ID: " + ri.getId());
        failure = createResult(FAILURE, "Repository id is not set!", true);
        addResult(assertStringNotEmpty(ri.getId(), success, failure));

        // name
        failure = createResult(FAILURE, "Repository name is not set!");
        addResult(assertNotNull(ri.getName(), null, failure));

        success = createResult(OK, "Repository name: " + ri.getName());
        failure = createResult(WARNING, "Repository name is empty!");
        addResult(assertStringNotEmpty(ri.getName(), success, failure));

        // description
        failure = createResult(FAILURE, "Repository description is not set!");
        addResult(assertNotNull(ri.getDescription(), null, failure));

        success = createResult(OK, "Repository description: " + ri.getDescription());
        failure = createResult(WARNING, "Repository description is empty!");
        addResult(assertStringNotEmpty(ri.getDescription(), success, failure));

        // vendor
        failure = createResult(FAILURE, "Vendor name is not set!");
        addResult(assertStringNotEmpty(ri.getVendorName(), null, failure));

        success = createResult(OK, "Vendor name: " + ri.getVendorName());
        failure = createResult(WARNING, "Vendor name is empty!");
        addResult(assertStringNotEmpty(ri.getVendorName(), success, failure));

        // product name
        failure = createResult(FAILURE, "Product name is not set!");
        addResult(assertStringNotEmpty(ri.getProductName(), null, failure));

        success = createResult(OK, "Product name: " + ri.getProductName());
        failure = createResult(WARNING, "Product name is empty!");
        addResult(assertStringNotEmpty(ri.getProductName(), success, failure));

        // product version
        failure = createResult(FAILURE, "Product version is not set!");
        addResult(assertStringNotEmpty(ri.getProductVersion(), null, failure));

        success = createResult(OK, "Product version: " + ri.getProductVersion());
        failure = createResult(WARNING, "Product version is empty!");
        addResult(assertStringNotEmpty(ri.getProductVersion(), success, failure));

        // CMIS version supported
        success = createResult(OK, "CMIS Version Supported: " + ri.getCmisVersionSupported());
        failure = createResult(FAILURE, "CMIS Version Supported is not set!");
        addResult(assertStringNotEmpty(ri.getCmisVersionSupported(), success, failure));

        if (!"1.0".equals(ri.getCmisVersionSupported()) && !"1.1".equals(ri.getCmisVersionSupported())) {
            addResult(createResult(FAILURE, "CMIS Version Supported is neither '1.0' nor '1.1'!"));
        }

        // root folder
        success = createResult(OK, "Root folder ID: " + ri.getRootFolderId());
        failure = createResult(FAILURE, "Root folder ID is not set!");
        addResult(assertStringNotEmpty(ri.getRootFolderId(), success, failure));

        // thin client URI
        success = createResult(OK, "Thin client URI: " + ri.getThinClientUri());
        failure = createResult(WARNING, "Thin client URI is not set!");
        addResult(assertStringNotEmpty(ri.getThinClientUri(), success, failure));

        if (ri.getThinClientUri() != null && ri.getThinClientUri().length() > 0) {
            try {
                HttpURLConnection conn = (HttpURLConnection) (new URL(ri.getThinClientUri())).openConnection();

                conn.connect();

                int responseCode = conn.getResponseCode();
                failure = createResult(WARNING, "Unable to connect to thin client '" + ri.getThinClientUri()
                        + "'. HTTP status code: " + responseCode);
                addResult(assertIsTrue(responseCode >= 200 && responseCode < 400, null, failure));

                conn.disconnect();
            } catch (Exception e) {
                addResult(createResult(WARNING, "Unable to connect to thin client '" + ri.getThinClientUri() + "': "
                        + e.getMessage(), e, false));
            }
        }

        // principal ID anonymous
        success = createResult(OK, "Principal ID anonymous: " + ri.getPrincipalIdAnonymous());
        failure = createResult(WARNING, "Principal ID anonymous is not set!");
        addResult(assertStringNotEmpty(ri.getPrincipalIdAnonymous(), success, failure));

        // principal ID anyone
        success = createResult(OK, "Principal ID anyone: " + ri.getPrincipalIdAnyone());
        failure = createResult(WARNING, "Principal Id anyone is not set!");
        addResult(assertStringNotEmpty(ri.getPrincipalIdAnyone(), success, failure));

        // latest change log token
        success = createResult(OK, "Latest change log token: " + ri.getLatestChangeLogToken());
        failure = createResult(WARNING, "Latest change log token is not set!");
        addResult(assertStringNotEmpty(ri.getLatestChangeLogToken(), success, failure));

        // changes incomplete
        success = createResult(OK, "Changes Incomplete: " + ri.getChangesIncomplete());
        failure = createResult(WARNING, "Changes Incomplete is not set!");
        addResult(assertNotNull(ri.getChangesIncomplete(), success, failure));

        // changes on type
        success = createResult(OK, "Changes on type: " + ri.getChangesOnType());
        failure = createResult(WARNING, "Changes on type is not set!");
        addResult(assertNotNull(ri.getChangesOnType(), success, failure));

        if (ri.getChangesOnType() != null) {
            failure = createResult(WARNING, "Changes on type has more than 4 entries!");
            addResult(assertIsTrue(ri.getChangesOnType().size() < 5, null, failure));
        }

        // features
        if (ri.getCmisVersion() != CmisVersion.CMIS_1_0) {
            success = createResult(OK, "Repository features exposed.");
            failure = createResult(OK, "No repository features exposed.");
            addResult(assertNotNull(ri.getExtensionFeatures(), success, failure));

            if (ri.getExtensionFeatures() != null) {
                for (ExtensionFeature feature : ri.getExtensionFeatures()) {
                    failure = createResult(FAILURE, "At least one repository features has no ID!");
                    addResult(assertStringNotEmpty(feature.getId(), null, failure));
                }
            }
        }

        // capabilities
        if (ri.getCapabilities() == null) {
            addResult(createResult(FAILURE, "Capabilities are not set!"));
        } else {
            RepositoryCapabilities cap = ri.getCapabilities();

            // ACL capability
            success = createResult(OK, "ACL capability: " + cap.getAclCapability());
            failure = createResult(FAILURE, "ACL capability is not set!");
            addResult(assertNotNull(cap.getAclCapability(), success, failure));

            // changes capability
            success = createResult(OK, "Changes capability: " + cap.getChangesCapability());
            failure = createResult(WARNING, "Changes capability is not set!");
            addResult(assertNotNull(cap.getChangesCapability(), success, failure));

            // content stream updates capability
            success = createResult(OK, "Content stream updates capability: " + cap.getContentStreamUpdatesCapability());
            failure = createResult(FAILURE, "Content stream updates is not set!");
            addResult(assertNotNull(cap.getContentStreamUpdatesCapability(), success, failure));

            // get descendants capability
            success = createResult(OK, "Get descendants capability: " + cap.isGetDescendantsSupported());
            failure = createResult(FAILURE, "Get descendants capability is not set!");
            addResult(assertNotNull(cap.isGetDescendantsSupported(), success, failure));

            // get folder tree capability
            success = createResult(OK, "Get folder tree capability: " + cap.isGetFolderTreeSupported());
            failure = createResult(FAILURE, "Get folder tree capability is not set!");
            addResult(assertNotNull(cap.isGetFolderTreeSupported(), success, failure));

            // multifiling capability
            success = createResult(OK, "Multifiling capability: " + cap.isMultifilingSupported());
            failure = createResult(FAILURE, "Multifiling capability is not set!");
            addResult(assertNotNull(cap.isMultifilingSupported(), success, failure));

            // unfiling capability
            success = createResult(OK, "Unfiling capability: " + cap.isUnfilingSupported());
            failure = createResult(FAILURE, "Unfiling capability is not set!");
            addResult(assertNotNull(cap.isUnfilingSupported(), success, failure));

            // version specific filing capability
            success = createResult(OK, "Version specific filing capability: " + cap.isVersionSpecificFilingSupported());
            failure = createResult(FAILURE, "Version specific filing capability is not set!");
            addResult(assertNotNull(cap.isVersionSpecificFilingSupported(), success, failure));

            // query capability
            success = createResult(OK, "Query capability: " + cap.getQueryCapability());
            failure = createResult(FAILURE, "Query capability is not set!");
            addResult(assertNotNull(cap.getQueryCapability(), success, failure));

            // JOIN capability
            success = createResult(OK, "JOIN capability: " + cap.getJoinCapability());
            failure = createResult(FAILURE, "JOIN capability is not set!");
            addResult(assertNotNull(cap.getJoinCapability(), success, failure));

            // all versions searchable capability
            success = createResult(OK, "All versions searchable capability: " + cap.isAllVersionsSearchableSupported());
            failure = createResult(FAILURE, "All versions searchable capability is not set!");
            addResult(assertNotNull(cap.isAllVersionsSearchableSupported(), success, failure));

            // PWC searchable capability
            success = createResult(OK, "PWC searchable capability: " + cap.isPwcSearchableSupported());
            failure = createResult(FAILURE, "PWC searchable capability is not set!");
            addResult(assertNotNull(cap.isPwcSearchableSupported(), success, failure));

            // PWC updatable capability
            success = createResult(OK, "PWC updatable capability: " + cap.isPwcUpdatableSupported());
            failure = createResult(FAILURE, "PWC updatable capability is not set!");
            addResult(assertNotNull(cap.isPwcUpdatableSupported(), success, failure));

            // renditions capability
            success = createResult(OK, "Renditions capability: " + cap.getRenditionsCapability());
            failure = createResult(FAILURE, "Renditions capability is not set!");
            addResult(assertNotNull(cap.getRenditionsCapability(), success, failure));

            if (ri.getCmisVersion() != CmisVersion.CMIS_1_0) {
                // new type settable attributes
                success = createResult(OK, "'New type settable attributes' flags are set.");
                failure = createResult(WARNING, "'New type settable attributes' flags are not set!");
                addResult(assertNotNull(cap.getNewTypeSettableAttributes(), success, failure));

                // creatable property types
                success = createResult(OK, "'Creatable property types' flags are set.");
                failure = createResult(WARNING, "'Creatable property types' flags are not set!");
                addResult(assertNotNull(cap.getCreatablePropertyTypes(), success, failure));
            }
        }

        // ACL capabilities
        if (ri.getAclCapabilities() == null) {
            addResult(createResult(WARNING, "ACL capabilities are not set!"));
        } else {
            AclCapabilities aclCap = ri.getAclCapabilities();

            // supported permissions
            success = createResult(OK, "Supported permissions: " + aclCap.getSupportedPermissions());
            failure = createResult(WARNING, "Supported permissions are not set!");
            addResult(assertNotNull(aclCap.getSupportedPermissions(), success, failure));

            // ACL propagation
            success = createResult(OK, "ACL propagation: " + aclCap.getAclPropagation());
            failure = createResult(WARNING, "ACL propagation is not set!");
            addResult(assertNotNull(aclCap.getAclPropagation(), success, failure));

            // permissions
            success = createResult(OK, "Permissions: "
                    + (aclCap.getPermissions() == null ? "?" : aclCap.getPermissions().size()));
            failure = createResult(FAILURE, "Permissions are not set!");
            addResult(assertNotNull(aclCap.getPermissions(), success, failure));

            if (aclCap.getPermissions() != null) {
                int i = 0;
                for (PermissionDefinition permDef : aclCap.getPermissions()) {
                    failure = createResult(FAILURE, "Permission #" + i + " is not set!");
                    addResult(assertNotNull(permDef, null, failure));

                    if (permDef != null) {
                        failure = createResult(FAILURE, "ID of permission #" + i + " is not set!");
                        addResult(assertStringNotEmpty(permDef.getId(), null, failure));
                    }

                    i++;
                }
            }

            // permission mapping
            success = createResult(OK, "Permission mapping: "
                    + (aclCap.getPermissionMapping() == null ? "?" : aclCap.getPermissionMapping().size()));
            failure = createResult(WARNING, "Permission mapping is not set!");
            addResult(assertNotNull(aclCap.getPermissionMapping(), success, failure));
        }

        if (ri.getExtensionFeatures() != null) {
            for (ExtensionFeature ef : ri.getExtensionFeatures()) {
                success = createResult(OK, "Extension Feature: " + ef.getId());
                failure = createResult(FAILURE, "Extension Feature without ID!");
                addResult(assertStringNotEmpty(ef.getId(), success, failure));

                failure = createResult(WARNING, "Extension Feature without common name: " + ef.getId());
                addResult(assertStringNotEmpty(ef.getCommonName(), null, failure));

                failure = createResult(WARNING, "Extension Feature without version label: " + ef.getId());
                addResult(assertStringNotEmpty(ef.getVersionLabel(), null, failure));

                failure = createResult(WARNING, "Extension Feature without URL: " + ef.getId());
                addResult(assertStringNotEmpty(ef.getUrl(), null, failure));
            }
        }
    }
}
