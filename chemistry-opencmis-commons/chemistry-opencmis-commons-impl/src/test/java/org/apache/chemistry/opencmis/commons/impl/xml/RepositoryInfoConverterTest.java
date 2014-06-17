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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.data.ExtensionFeature;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityOrderBy;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.SupportedPermissions;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.WSConverter;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AclCapabilitiesDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CreatablePropertyTypesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ExtensionFeatureImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.NewTypeSettableAttributesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisRepositoryInfoType;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;
import org.junit.Test;

public class RepositoryInfoConverterTest extends AbstractXMLConverterTest {

    private static Set<String> cmis10ignoreMethods = new HashSet<String>();
    static {
        cmis10ignoreMethods.add("getOrderByCapability");
        cmis10ignoreMethods.add("getCreatablePropertyTypes");
        cmis10ignoreMethods.add("getNewTypeSettableAttributes");
        cmis10ignoreMethods.add("getExtensionFeatures");
    }

    @Test
    public void testRepositoryInfo() throws Exception {

        // run the test a few times with different values
        for (int i = 0; i < 10; i++) {
            RepositoryInfoImpl repInfo = new RepositoryInfoImpl();

            assertRepositoryInfo10(repInfo, false);

            // values
            repInfo = new RepositoryInfoImpl();
            repInfo.setChangesIncomplete(randomBoolean());
            repInfo.setChangesOnType(Collections.singletonList(BaseTypeId.CMIS_DOCUMENT));
            repInfo.setCmisVersionSupported("1.0");
            repInfo.setLatestChangeLogToken(randomString());
            repInfo.setPrincipalAnonymous(randomString());
            repInfo.setPrincipalAnyone(randomString());
            repInfo.setProductName(randomString());
            repInfo.setProductVersion(randomString());
            repInfo.setDescription(randomString());
            repInfo.setId(randomString());
            repInfo.setName(randomString());
            repInfo.setRootFolder(randomString());
            repInfo.setThinClientUri(randomUri());
            repInfo.setVendorName(randomString());

            RepositoryCapabilitiesImpl cap1 = new RepositoryCapabilitiesImpl();
            cap1.setAllVersionsSearchable(randomBoolean());
            cap1.setCapabilityAcl(randomEnum(CapabilityAcl.class));
            cap1.setCapabilityChanges(CapabilityChanges.ALL);
            cap1.setCapabilityContentStreamUpdates(randomEnum(CapabilityContentStreamUpdates.class));
            cap1.setCapabilityJoin(randomEnum(CapabilityJoin.class));
            cap1.setCapabilityQuery(randomEnum(CapabilityQuery.class));
            cap1.setCapabilityRendition(randomEnum(CapabilityRenditions.class));
            cap1.setIsPwcSearchable(randomBoolean());
            cap1.setIsPwcUpdatable(randomBoolean());
            cap1.setSupportsGetDescendants(randomBoolean());
            cap1.setSupportsGetFolderTree(randomBoolean());
            cap1.setSupportsMultifiling(randomBoolean());
            cap1.setCapabilityOrderBy(randomEnum(CapabilityOrderBy.class));
            cap1.setSupportsUnfiling(randomBoolean());
            cap1.setSupportsVersionSpecificFiling(randomBoolean());

            CreatablePropertyTypesImpl cpt = new CreatablePropertyTypesImpl();
            Set<PropertyType> pt = new HashSet<PropertyType>();
            pt.add(PropertyType.BOOLEAN);
            pt.add(PropertyType.ID);
            pt.add(PropertyType.INTEGER);
            pt.add(PropertyType.DATETIME);
            pt.add(PropertyType.DECIMAL);
            pt.add(PropertyType.HTML);
            pt.add(PropertyType.STRING);
            pt.add(PropertyType.URI);
            cpt.setCanCreate(pt);
            cap1.setCreatablePropertyTypes(cpt);

            NewTypeSettableAttributesImpl newTypeSettableAttributes = new NewTypeSettableAttributesImpl();
            newTypeSettableAttributes.setCanSetId(randomBoolean());
            newTypeSettableAttributes.setCanSetLocalName(randomBoolean());
            newTypeSettableAttributes.setCanSetLocalNamespace(randomBoolean());
            newTypeSettableAttributes.setCanSetDisplayName(randomBoolean());
            newTypeSettableAttributes.setCanSetQueryName(randomBoolean());
            newTypeSettableAttributes.setCanSetDescription(randomBoolean());
            newTypeSettableAttributes.setCanSetCreatable(randomBoolean());
            newTypeSettableAttributes.setCanSetFileable(randomBoolean());
            newTypeSettableAttributes.setCanSetQueryable(randomBoolean());
            newTypeSettableAttributes.setCanSetFulltextIndexed(randomBoolean());
            newTypeSettableAttributes.setCanSetIncludedInSupertypeQuery(randomBoolean());
            newTypeSettableAttributes.setCanSetControllablePolicy(randomBoolean());
            newTypeSettableAttributes.setCanSetControllableAcl(randomBoolean());
            cap1.setNewTypeSettableAttributes(newTypeSettableAttributes);

            repInfo.setCapabilities(cap1);

            AclCapabilitiesDataImpl acl1 = new AclCapabilitiesDataImpl();
            acl1.setSupportedPermissions(randomEnum(SupportedPermissions.class));
            acl1.setAclPropagation(randomEnum(AclPropagation.class));
            List<PermissionDefinition> pddList = new ArrayList<PermissionDefinition>();
            PermissionDefinitionDataImpl pdd1 = new PermissionDefinitionDataImpl();
            pdd1.setId(randomString());
            pdd1.setDescription(randomString());
            pddList.add(pdd1);
            PermissionDefinitionDataImpl pdd2 = new PermissionDefinitionDataImpl();
            pdd2.setId(randomString());
            pdd2.setDescription(randomString());
            pddList.add(pdd2);
            acl1.setPermissionDefinitionData(pddList);
            Map<String, PermissionMapping> pmd = new LinkedHashMap<String, PermissionMapping>();
            PermissionMappingDataImpl pmd1 = new PermissionMappingDataImpl();
            pmd1.setKey(PermissionMapping.CAN_CREATE_DOCUMENT_FOLDER);
            pmd1.setPermissions(Arrays.asList(new String[] { randomString(), randomString() }));
            pmd.put(pmd1.getKey(), pmd1);
            PermissionMappingDataImpl pmd2 = new PermissionMappingDataImpl();
            pmd2.setKey(PermissionMapping.CAN_DELETE_OBJECT);
            pmd2.setPermissions(Arrays.asList(new String[] { randomString(), randomString() }));
            pmd.put(pmd2.getKey(), pmd2);
            acl1.setPermissionMappingData(pmd);
            repInfo.setAclCapabilities(acl1);

            List<ExtensionFeature> extensionFeatures = new ArrayList<ExtensionFeature>();

            ExtensionFeatureImpl ef1 = new ExtensionFeatureImpl();
            ef1.setId(randomUri());
            ef1.setCommonName(randomString());
            ef1.setDescription(randomString());
            ef1.setUrl(randomUri());
            ef1.setVersionLabel(randomString());
            Map<String, String> efd1 = new HashMap<String, String>();
            efd1.put(randomString(), randomString());
            efd1.put(randomString(), randomString());
            ef1.setFeatureData(efd1);
            extensionFeatures.add(ef1);

            ExtensionFeatureImpl ef2 = new ExtensionFeatureImpl();
            ef2.setId(randomUri());
            ef2.setCommonName(randomString());
            ef2.setDescription(randomString());
            ef2.setUrl(randomUri());
            ef2.setVersionLabel(randomString());
            Map<String, String> efd2 = new HashMap<String, String>();
            efd2.put(randomString(), randomString());
            efd2.put(randomString(), randomString());
            ef2.setFeatureData(efd2);
            extensionFeatures.add(ef2);

            repInfo.setExtensionFeature(extensionFeatures);

            assertRepositoryInfo10(repInfo, true);
            assertRepositoryInfo11(repInfo, true);
        }
    }

    protected void assertRepositoryInfo10(RepositoryInfo repInfo, boolean validate) throws Exception {
        assertXmlRepositoryInfo10(repInfo, validate);
        assertWsRepositoryInfo10(repInfo);
    }

    protected void assertXmlRepositoryInfo10(RepositoryInfo repInfo, boolean validate) throws Exception {
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
        assertDataObjectsEquals("RepositoryInfo", repInfo, result, cmis10ignoreMethods);
        assertNull(result.getExtensions());
    }

    protected void assertWsRepositoryInfo10(RepositoryInfo repInfo) throws Exception {
        CmisRepositoryInfoType ws = WSConverter.convert(repInfo, CmisVersion.CMIS_1_0);

        RepositoryInfo result = WSConverter.convert(ws);

        // remove CMIS 1.1 features
        RepositoryInfoImpl repInfo2 = new RepositoryInfoImpl(repInfo);
        repInfo2.setExtensionFeature(null);
        if (repInfo.getCapabilities() != null) {
            RepositoryCapabilitiesImpl capabilities = new RepositoryCapabilitiesImpl(repInfo.getCapabilities());
            capabilities.setCapabilityOrderBy(null);
            capabilities.setCreatablePropertyTypes(null);
            capabilities.setNewTypeSettableAttributes(null);
            repInfo2.setCapabilities(capabilities);
        }

        assertNotNull(result);
        assertDataObjectsEquals("RepositoryInfo", repInfo2, result, null);
        assertNull(result.getExtensions());
    }

    protected void assertRepositoryInfo11(RepositoryInfo repInfo, boolean validate) throws Exception {
        assertXmlRepositoryInfo11(repInfo, validate);
        assertWsRepositoryInfo11(repInfo);
        assertJsonRepositoryInfo11(repInfo);
    }

    protected void assertXmlRepositoryInfo11(RepositoryInfo repInfo, boolean validate) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        XMLStreamWriter writer = createWriter(out);
        XMLConverter.writeRepositoryInfo(writer, CmisVersion.CMIS_1_1, TEST_NAMESPACE, repInfo);
        closeWriter(writer);

        byte[] xml = out.toByteArray();

        if (validate) {
            validate(xml, CmisVersion.CMIS_1_1);
        }

        XMLStreamReader parser = createParser(xml);
        RepositoryInfo result = XMLConverter.convertRepositoryInfo(parser);
        closeParser(parser);

        assertNotNull(result);
        assertDataObjectsEquals("RepositoryInfo", repInfo, result, null);
        assertNull(result.getExtensions());
    }

    protected void assertWsRepositoryInfo11(RepositoryInfo repInfo) throws Exception {
        CmisRepositoryInfoType ws = WSConverter.convert(repInfo, CmisVersion.CMIS_1_1);

        RepositoryInfo result = WSConverter.convert(ws);

        assertNotNull(result);
        assertDataObjectsEquals("RepositoryInfo", repInfo, result, null);
        assertNull(result.getExtensions());
    }

    protected void assertJsonRepositoryInfo11(RepositoryInfo repInfo) throws Exception {
        StringWriter sw = new StringWriter();

        JSONConverter.convert(repInfo, null, null, false).writeJSONString(sw);

        Object json = (new JSONParser()).parse(sw.toString());
        assertTrue(json instanceof Map<?, ?>);
        @SuppressWarnings("unchecked")
        RepositoryInfo result = JSONConverter.convertRepositoryInfo((Map<String, Object>) json);

        assertNotNull(result);
        assertDataObjectsEquals("RepositoryInfo", repInfo, result, null);
        assertNull(result.getExtensions());
    }
}
