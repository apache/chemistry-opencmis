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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.PolicyIdList;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.inmemory.ObjectServiceTest.ObjectTestTypeSystemCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PolicyTest extends AbstractServiceTest {
    private static final String TEST_CUSTOM_VALUE = "SimpleAuditPolicy";
    private static final String TEST_POLICY_TEXT = "Test Policy Unit Test";
    private static final String TEST_POLICY_NAME = "TestPolicy";
    private static final String TEST_POLICY_2_NAME = "TestPolicy2";
    private static final String MY_DOC_1 = "Document_1";
    private static final String VER_DOC_NAME = "VersionedDocument";

    private String polId;

    @Override
    @Before
    public void setUp() {
        super.setTypeCreatorClass(ObjectTestTypeSystemCreator.class.getName());
        super.setUp();
        createPolicy();
    }

    @Override
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

        String versionSeriesId = (String) od.getProperties().getProperties().get(PropertyIds.VERSION_SERIES_ID)
                .getFirstValue();
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

    @Test
    public void testPolicyServiceGetAppliedPolicies() {
        assertNotNull(polId);
        String docId1 = createDocumentWithPolicy(polId);
        List<ObjectData> pols = fPolSvc.getAppliedPolicies(fRepositoryId, docId1, null, null);
        assertEquals(1, pols.size());
        ObjectData od = pols.get(0);
        assertEquals(polId, od.getId());
        assertNull(od.getPolicyIds());
    }

    @Test
    public void testPolicyServiceApplyPolicies() {

        assertNotNull(polId);
        String docId = createDocumentWithoutPolicy("Document_2", polId);
        List<ObjectData> pols = fPolSvc.getAppliedPolicies(fRepositoryId, docId, null, null);
        assertEquals(0, pols.size());

        // apply a policy
        fPolSvc.applyPolicy(fRepositoryId, polId, docId, null);
        pols = fPolSvc.getAppliedPolicies(fRepositoryId, docId, null, null);
        assertEquals(1, pols.size());
        ObjectData od = pols.get(0);
        assertEquals(polId, od.getId());
        assertNull(od.getPolicyIds());

        String polId2 = createPolicy2();
        fPolSvc.applyPolicy(fRepositoryId, polId2, docId, null);
        pols = fPolSvc.getAppliedPolicies(fRepositoryId, docId, null, null);
        assertEquals(2, pols.size());
        od = pols.get(0);
        assertEquals(polId, od.getId());
        assertNull(od.getPolicyIds());
        od = pols.get(1);
        assertEquals(polId2, od.getId());

        // assign an unknown id as policy
        docId = createDocumentWithoutPolicy("Document_3", polId);
        try {
            fPolSvc.applyPolicy(fRepositoryId, "UnknownId", docId, null);
            fail("applyPolicy with unknown id should fail.");
        } catch (CmisObjectNotFoundException e) {
        } catch (Exception ex) {
            fail("applyPolicy with unknown id should throw a CmisInvalidArgumentException, but was a " + ex.getClass());
        }

        // apply policy with a doc id
        try {
            String docId2 = createDocumentWithoutPolicy(polId);
            fPolSvc.applyPolicy(fRepositoryId, docId2, docId, null);
            fail("applyPolicy with document id as policy should fail.");
        } catch (CmisInvalidArgumentException e) {
        } catch (Exception ex) {
            fail("applyPolicy with unknown id should throw a CmisInvalidArgumentException, but was a " + ex.getClass());
        }

        // apply a policy to a policy
        try {
            fPolSvc.applyPolicy(fRepositoryId, polId2, polId, null);
            fail("applyPolicy to a policy id should fail.");
        } catch (CmisInvalidArgumentException e) {
        } catch (Exception ex) {
            fail("applyPolicy with unknown id should throw a CmisInvalidArgumentException, but was a " + ex.getClass());
        }

    }

    @Test
    public void testPolicyServiceRemovePolicies() {
        assertNotNull(polId);
        String docId = createDocumentWithPolicy(polId);
        String polId2 = createPolicy2();
        String docId2 = createDocumentWithoutPolicy("Document_3", polId);

        fPolSvc.applyPolicy(fRepositoryId, polId2, docId, null);

        fPolSvc.removePolicy(fRepositoryId, polId, docId, null);
        List<ObjectData> pols = fPolSvc.getAppliedPolicies(fRepositoryId, docId, null, null);
        assertEquals(1, pols.size());
        ObjectData od = pols.get(0);
        assertEquals(polId2, od.getId());

        fPolSvc.removePolicy(fRepositoryId, polId2, docId, null);
        pols = fPolSvc.getAppliedPolicies(fRepositoryId, docId, null, null);
        assertEquals(0, pols.size());

        // try again should fail
        try {
            fPolSvc.removePolicy(fRepositoryId, polId2, docId, null);
            fail("Removing a non-existing policy should fail.");
        } catch (CmisInvalidArgumentException e) {

        } catch (Exception e) {
            fail("Removing a non-existing policy should raise a CmisObjectNotFoundException, but was a " + e);
        }

        // try removing a non existing id
        try {
            fPolSvc.removePolicy(fRepositoryId, polId2, docId, null);
            fail("Removing a non-existing policy should fail.");
        } catch (CmisInvalidArgumentException e) {
        } catch (Exception e) {
            fail("Removing a non-existing policy should raise a CmisInvalidArgumentException, but was a " + e);
        }

        // try removing a non policy id
        try {
            fPolSvc.removePolicy(fRepositoryId, docId2, docId, null);
            fail("Removing a non-existing policy should fail.");
        } catch (CmisInvalidArgumentException e) {
        } catch (Exception e) {
            fail("Removing a non-policy should raise a CmisInvalidArgumentException, but was a " + e);
        }

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

    private String createPolicy2() {
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, TEST_POLICY_2_NAME));
        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_POLICY.value()));
        properties.add(fFactory.createPropertyIdData(PropertyIds.POLICY_TEXT, "ReadAuditLogging"));
        Properties props = fFactory.createPropertiesData(properties);

        return fObjSvc.createPolicy(fRepositoryId, props, null, null, null, null, null);
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

    private String createDocumentWithoutPolicy(String policyId) {
        return createDocumentWithoutPolicy(MY_DOC_1, policyId);
    }

    private String createDocumentWithoutPolicy(String name, String policyId) {
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(fFactory.createPropertyIdData(PropertyIds.NAME, name));
        properties.add(fFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value()));
        Properties props = fFactory.createPropertiesData(properties);
        String id = fObjSvc.createDocument(fRepositoryId, props, fRootFolderId, null, VersioningState.NONE, null, null,
                null, null);
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

        id = fObjSvc.createDocument(fRepositoryId, props, fRootFolderId, contentStream, VersioningState.MAJOR,
                policies, null, null, null);

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
