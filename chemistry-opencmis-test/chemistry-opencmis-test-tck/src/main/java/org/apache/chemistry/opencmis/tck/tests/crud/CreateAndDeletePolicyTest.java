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
package org.apache.chemistry.opencmis.tck.tests.crud;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.INFO;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Simple folder test.
 */
public class CreateAndDeletePolicyTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Create and Delete Policy Test");
        setDescription(
                "Creates a policy object, checks the newly created policy object, applys and removes it from a document, and finally deletes the created policy object.");
    }

    @Override
    public void run(Session session) {

        if (hasPolicies(session)) {
            CmisTestResult f;

            // create a test folder
            Folder testFolder = createTestFolder(session);

            try {
                // create policy object
                Policy policy = createPolicy(session, testFolder, "testPolicy", "TCK Test Policy");

                // create document and apply policy
                Document doc = createDocument(session, testFolder, "testDocument", "Policy Test");

                if (Boolean.TRUE.equals(doc.getType().isControllablePolicy())) {
                    doc.applyPolicy(policy);

                    // check if policy has been applied
                    List<Policy> policies1 = doc.getPolicies();
                    boolean found1 = false;
                    for (Policy p : policies1) {
                        if (p.getId().equals(policy.getId())) {
                            found1 = true;
                            break;
                        }
                    }

                    f = createResult(FAILURE, "Policy has not been applied to document! Policy Id: " + policy.getId()
                            + ", Doc Id: " + doc.getId());
                    addResult(assertIsTrue(found1, null, f));

                    // check if policy IDs and policy object match
                    f = createResult(WARNING, "Not all policy IDs can be resolved to policy objects.");
                    addResult(assertEquals(doc.getPolicyIds().size(), doc.getPolicies().size(), null, f));

                    // get the policies
                    List<ObjectData> policiesData2 = session.getBinding().getPolicyService()
                            .getAppliedPolicies(session.getRepositoryInfo().getId(), doc.getId(), "*", null);

                    boolean found2 = false;
                    if (policiesData2 != null && !policiesData2.isEmpty()) {
                        for (ObjectData p : policiesData2) {
                            if (p.getId().equals(policy.getId())) {
                                found2 = true;
                                break;
                            }
                        }
                    }

                    f = createResult(FAILURE, "Applied policy is not returned by the repository! Policy Id: "
                            + policy.getId() + ", Doc Id: " + doc.getId());
                    addResult(assertIsTrue(found2, null, f));

                    // remove policy
                    doc.removePolicy(policy);

                    // check if policy has been applied
                    List<Policy> policies3 = doc.getPolicies();
                    if (policies3 != null) {
                        boolean found3 = false;
                        for (Policy p : policies3) {
                            if (p.getId().equals(policy.getId())) {
                                found3 = true;
                                break;
                            }
                        }

                        f = createResult(FAILURE, "Policy has not been removed from document! Policy Id: "
                                + policy.getId() + ", Doc Id: " + doc.getId());
                        addResult(assertIsFalse(found3, null, f));
                    }
                } else {
                    addResult(createResult(INFO, "Document type " + doc.getType().getId()
                            + " does not allow applying and removing policies. Choose a different document type for this test."));
                }

                // delete document
                deleteObject(doc);

                // delete policy object
                deleteObject(policy);

            } finally {
                // delete the test folder
                deleteTestFolder();
            }
        } else {
            addResult(createResult(SKIPPED, "Policies not supported. Test skipped!"));
        }
    }
}
