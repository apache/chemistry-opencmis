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
package org.apache.chemistry.opencmis.inmemory;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.PolicyIdList;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.inmemory.ObjectServiceTest.ObjectTestTypeSystemCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PolicyTest extends AbstractServiceTest {
    private static final String TEST_CUSTOM_VALUE = "SimpleAuditPolicy";
    private static final String TEST_POLICY_TEXT = "Test Policy Unit Test";
    private static final String TEST_POLICY_NAME = "TestPolicy";
    private static final String MY_DOC_1 = "Document_1";
    private static final String VER_DOC_NAME = "VersionedDocument";

    private String polId;

    @Before
    public void setUp() {
        super.setTypeCreatorClass(ObjectTestTypeSystemCreator.class.getName());
        super.setUp();
        createPolicy();
    }

    @After
    public void tearDown() {
        super.tearDown();
    }
    
    @Test
    public void createGetPolicy() {
        assertNotNull(polId);
        ObjectData od = fObjSvc.getObject(fRepositoryId, polId, null, false, IncludeRelationships.NONE, null, false,
                false, null);
        assertEquals(od.getBaseTypeId(), BaseTypeId.CMIS_POLICY);
        assertEquals(polId, od.getId());
        assertNull(od.getPolicyIds());
        assertEquals(TEST_POLICY_NAME, od.getProperties().getProperties().get(PropertyIds.NAME).getFirstValue());
        assertEquals(TEST_POLICY_TEXT, od.getProperties().getProperties().get(PropertyIds.POLICY_TEXT).getFirstValue());
        assertEquals(TEST_CUSTOM_VALUE,
                od.getProperties().getProperties().get(ObjectServiceTest.TEST_POLICY_PROPERTY_ID).getFirstValue());
    }

    @Test
    public void applyGetPolicyTest() {
        String docId1 = createDocumentWithPolicy(polId);
        assertNotNull(docId1);
        ObjectData od = fObjSvc.getObject(fRepositoryId, docId1, null, false, IncludeRelationships.NONE, null, true,
                false, null);
        assertEquals(docId1, od.getId());
        PolicyIdList polIds = od.getPolicyIds();
        assertNotNull(polIds.getPolicyIds());
        assertEquals(1, polIds.getPolicyIds().size());
        String polIdRes = polIds.getPolicyIds().get(0);
        assertEquals(polId, polIdRes);

        String path = "/" + MY_DOC_1;
        od = fObjSvc.getObjectByPath(fRepositoryId, path, null, false, IncludeRelationships.NONE, null, true, false,
                null);
        polIds = od.getPolicyIds();
        assertNotNull(polIds.getPolicyIds());
        assertEquals(1, polIds.getPolicyIds().size());
        polIdRes = polIds.getPolicyIds().get(0);
        assertEquals(polId, polIdRes);
    }

    @Test
    public void applyGetPolicyTestVersioned() {
        String docId = createCheckedOutDocument();
        Holder<String> holder = new Holder<String>(docId);
        List<String> policies = Collections.singletonList(polId);
        fVerSvc.checkIn(fRepositoryId, holder, true, null, null, "Version with policies", policies, null, null, null);
        ObjectData od = fObjSvc.getObject(fRepositoryId, docId, null, false, IncludeRelationships.NONE, null, true,
                false, null);
        PolicyIdList polIds = od.getPolicyIds();
        assertNotNull(polIds.getPolicyIds());
        assertEquals(1, polIds.getPolicyIds().size());
        String polIdRes = polIds.getPolicyIds().get(0);
        assertEquals(polId, polIdRes);

        String versionSeriesId = (String) od.getProperties().getProperties().get(PropertyIds.VERSION_SERIES_ID).getFirstValue();
        assertNotNull(versionSeriesId);
        od = fVerSvc.getObjectOfLatestVersion(fRepositoryId, docId, versionSeriesId, false, null, false,
                IncludeRelationships.NONE, null, true, false, null);
        polIds = od.getPolicyIds();
        assertNotNull(polIds);
        assertNotNull(polIds.getPolicyIds());
        assertEquals(1, polIds.getPolicyIds().size());
        polIdRes = polIds.getPolicyIds().get(0);
        assertEquals(polId, polIdRes);
    }

    private void createPolicy() {
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, TEST_POLICY_NAME));
        properties
                .add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, ObjectServiceTest.TEST_POLICY_TYPE_ID));
        properties.add(fFactory.createPropertyIdData(PropertyIds.POLICY_TEXT, TEST_POLICY_TEXT));
        properties.add(fFactory.createPropertyIdData(ObjectServiceTest.TEST_POLICY_PROPERTY_ID, TEST_CUSTOM_VALUE));
        Properties props = fFactory.createPropertiesData(properties);

        polId = fObjSvc.createPolicy(fRepositoryId, props, null, null, null, null, null);
    }

    private String createDocumentWithPolicy(String policyId) {
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, MY_DOC_1));
        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value()));
        Properties props = fFactory.createPropertiesData(properties);

        List<String> policies = Collections.singletonList(policyId);
        String id = fObjSvc.createDocument(fRepositoryId, props, fRootFolderId, null, VersioningState.NONE, policies,
                null, null, null);
        return id;
    }

    private String createVersionedDocument() {
        String id = null;
        ContentStream contentStream = null;
        List<String> policies = null;

        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, VER_DOC_NAME));
        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID,
                ObjectServiceTest.TEST_VERSION_DOCUMENT_TYPE_ID));
        Properties props = fFactory.createPropertiesData(properties);

        id = fObjSvc.createDocument(fRepositoryId, props, fRootFolderId, contentStream, VersioningState.MAJOR, policies,
                null, null, null);

        return id;
    }

    private String createCheckedOutDocument() {
        String id = createVersionedDocument();
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        Holder<String> checkedOutId = new Holder<String>(id);
        fVerSvc.checkOut(fRepositoryId, checkedOutId, null, contentCopied);
        assertTrue(!id.equals(checkedOutId.getValue()));
        return checkedOutId.getValue();
    }

}
