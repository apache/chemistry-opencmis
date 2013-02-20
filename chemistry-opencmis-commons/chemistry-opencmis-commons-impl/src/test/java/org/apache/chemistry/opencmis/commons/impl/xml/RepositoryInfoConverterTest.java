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
package org.apache.chemistry.opencmis.commons.impl.xml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.SupportedPermissions;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AclCapabilitiesDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;
import org.junit.Test;

public class RepositoryInfoConverterTest extends AbstractXMLConverterTest {

    @Test
    public void testRepositoryInfo() throws Exception {
        RepositoryInfoImpl obj1 = new RepositoryInfoImpl();

        assertRepositoryInfo(obj1, false);

        // values
        obj1 = new RepositoryInfoImpl();
        obj1.setChangesIncomplete(Boolean.TRUE);
        obj1.setChangesOnType(Collections.singletonList(BaseTypeId.CMIS_DOCUMENT));
        obj1.setCmisVersionSupported("1.0");
        obj1.setLatestChangeLogToken("changeLogToken");
        obj1.setPrincipalAnonymous("principalAnonymous");
        obj1.setPrincipalAnyone("principalAnyone");
        obj1.setProductName("productName");
        obj1.setProductVersion("productVersion");
        obj1.setDescription("description");
        obj1.setId("id");
        obj1.setName("name");
        obj1.setRootFolder("rootFolderId");
        obj1.setThinClientUri("thinClientUri");
        obj1.setVendorName("vendorName");

        RepositoryCapabilitiesImpl cap1 = new RepositoryCapabilitiesImpl();
        cap1.setAllVersionsSearchable(Boolean.TRUE);
        cap1.setCapabilityAcl(CapabilityAcl.DISCOVER);
        cap1.setCapabilityChanges(CapabilityChanges.ALL);
        cap1.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.ANYTIME);
        cap1.setCapabilityJoin(CapabilityJoin.INNERANDOUTER);
        cap1.setCapabilityQuery(CapabilityQuery.BOTHCOMBINED);
        cap1.setCapabilityRendition(CapabilityRenditions.READ);
        cap1.setIsPwcSearchable(Boolean.TRUE);
        cap1.setIsPwcUpdatable(Boolean.TRUE);
        cap1.setSupportsGetDescendants(Boolean.TRUE);
        cap1.setSupportsGetFolderTree(Boolean.TRUE);
        cap1.setSupportsMultifiling(Boolean.TRUE);
        cap1.setSupportsUnfiling(Boolean.TRUE);
        cap1.setSupportsVersionSpecificFiling(Boolean.TRUE);
        obj1.setCapabilities(cap1);

        AclCapabilitiesDataImpl acl1 = new AclCapabilitiesDataImpl();
        acl1.setSupportedPermissions(SupportedPermissions.BASIC);
        acl1.setAclPropagation(AclPropagation.PROPAGATE);
        List<PermissionDefinition> pddList = new ArrayList<PermissionDefinition>();
        PermissionDefinitionDataImpl pdd1 = new PermissionDefinitionDataImpl();
        pdd1.setPermission("test:perm1");
        pdd1.setDescription("Permission1");
        pddList.add(pdd1);
        PermissionDefinitionDataImpl pdd2 = new PermissionDefinitionDataImpl();
        pdd2.setPermission("test:perm2");
        pdd2.setDescription("Permission2");
        pddList.add(pdd2);
        acl1.setPermissionDefinitionData(pddList);
        Map<String, PermissionMapping> pmd = new LinkedHashMap<String, PermissionMapping>();
        PermissionMappingDataImpl pmd1 = new PermissionMappingDataImpl();
        pmd1.setKey(PermissionMapping.CAN_CREATE_DOCUMENT_FOLDER);
        pmd1.setPermissions(Arrays.asList(new String[] { "p1", "p2" }));
        pmd.put(pmd1.getKey(), pmd1);
        PermissionMappingDataImpl pmd2 = new PermissionMappingDataImpl();
        pmd2.setKey(PermissionMapping.CAN_DELETE_OBJECT);
        pmd2.setPermissions(Arrays.asList(new String[] { "p3", "p4" }));
        pmd.put(pmd2.getKey(), pmd2);
        acl1.setPermissionMappingData(pmd);
        obj1.setAclCapabilities(acl1);

        assertRepositoryInfo(obj1, true);
    }

    protected void assertRepositoryInfo(RepositoryInfo repInfo, boolean validate) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        XMLStreamWriter writer = createWriter(out);
        XMLConverter.writeRepositoryInfo(writer, CmisVersion.CMIS_1_0, TEST_NAMESPACE, repInfo);
        closeWriter(writer);

        byte[] xml = out.toByteArray();

        if (validate) {
            validate(xml, CmisVersion.CMIS_1_0);
        }

        XMLStreamReader parser = createParser(xml);
        RepositoryInfo result = XMLConverter.convertRepositoryInfo(parser);
        closeParser(parser);

        assertNotNull(result);
        assertDataObjectsEquals("RepositoryInfo", repInfo, result);
        assertNull(result.getExtensions());
    }
}
