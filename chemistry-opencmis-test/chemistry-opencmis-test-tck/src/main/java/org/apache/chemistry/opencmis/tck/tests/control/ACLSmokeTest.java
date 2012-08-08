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
package org.apache.chemistry.opencmis.tck.tests.control;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;
import org.apache.chemistry.opencmis.tck.impl.TestParameters;

/**
 * Query smoke test.
 */
public class ACLSmokeTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("ACL Test");
        setDescription("Creates a document and checks its ACL.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        if (supportsACLs(session)) {
            try {
                // create folder and document
                Folder testFolder = createTestFolder(session);
                Document doc = createDocument(session, testFolder, "acltest.txt", "ACL test");

                // check if there is an ACL
                Acl acl = doc.getAcl();

                f = createResult(FAILURE, "ACLs are supported but newly created document has no ACL!");
                addResult(assertNotNull(acl, null, f));

                // check basic permissions
                Acl basicAcl = session.getAcl(doc, true);

                f = createResult(FAILURE,
                        "ACLs are supported but repository does not return a basic ACL for the newly created document!");
                addResult(assertNotNull(basicAcl, null, f));

                if (basicAcl != null) {
                    addResult(checkACL(session, basicAcl, "Basic ACL"));

                    if (basicAcl.getAces() != null) {
                        for (Ace ace : basicAcl.getAces()) {
                            if (ace.getPermissions() != null) {
                                for (String permission : ace.getPermissions()) {
                                    if (!"cmis:read".equals(permission) && !"cmis:write".equals(permission)
                                            && !"cmis:all".equals(permission)) {
                                        addResult(createResult(FAILURE, "ACE contains a non-basic permission: "
                                                + permission));
                                    }
                                }
                            }
                        }
                    }
                }

                if (getAclCapability(session) == CapabilityAcl.MANAGE) {
                    String principal = getParameters().get(TestParameters.DEFAULT_ACL_PRINCIPAL);
                    if (principal == null) {
                        principal = TestParameters.DEFAULT_ACL_PRINCIPAL_VALUE;
                    }

                    // apply permission "cmis:read"
                    List<Ace> aces = new ArrayList<Ace>();
                    aces.add(session.getObjectFactory().createAce(principal, Collections.singletonList("cmis:read")));

                    session.applyAcl(doc, aces, null, null);

                    // set permission "cmis:read" and "cmis:write"
                    aces = new ArrayList<Ace>();
                    aces.add(session.getObjectFactory().createAce(principal, Arrays.asList("cmis:all")));

                    session.setAcl(doc, aces);
                }

                deleteObject(doc);
            } finally {
                deleteTestFolder();
            }
        } else {
            addResult(createResult(SKIPPED, "ACLs are not supported. Test Skipped!"));
        }
    }

    protected boolean supportsACLs(Session session) {
        CapabilityAcl aclCap = getAclCapability(session);
        return (aclCap != null) && (aclCap != CapabilityAcl.NONE);
    }

    protected CapabilityAcl getAclCapability(Session session) {
        RepositoryInfo repository = session.getRepositoryInfo();

        if (repository.getCapabilities().getAclCapability() == null) {
            return null;
        }

        return repository.getCapabilities().getAclCapability();
    }
}
