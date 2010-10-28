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
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Types test.
 */
public class TypesTest extends AbstractSessionTest {
    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Types");
    }

    @Override
    public void run(Session session) {
        CmisTestResult failure;

        // document
        try {
            TypeDefinition documentType = session.getTypeDefinition(BaseTypeId.CMIS_DOCUMENT.value());
            addResult(checkTypeDefinition(session, documentType, "Document type spec compliance."));

            failure = createResult(FAILURE, "Document type has the wrong base type: " + documentType.getBaseTypeId());
            addResult(assertEquals(BaseTypeId.CMIS_DOCUMENT, documentType.getBaseTypeId(), null, failure));
        } catch (CmisObjectNotFoundException e) {
            addResult(createResult(FAILURE, "Document type not available!", e, false));
        }

        // folder
        try {
            TypeDefinition folderType = session.getTypeDefinition(BaseTypeId.CMIS_FOLDER.value());

            addResult(checkTypeDefinition(session, folderType, "Folder type spec compliance."));

            failure = createResult(FAILURE, "Folder type has the wrong base type: " + folderType.getBaseTypeId());
            addResult(assertEquals(BaseTypeId.CMIS_FOLDER, folderType.getBaseTypeId(), null, failure));
        } catch (CmisObjectNotFoundException e) {
            addResult(createResult(FAILURE, "Folder type not available!", e, false));
        }

        // relationship
        try {
            TypeDefinition relationshipType = session.getTypeDefinition(BaseTypeId.CMIS_RELATIONSHIP.value());
            addResult(checkTypeDefinition(session, relationshipType, "Relationship type spec compliance."));

            failure = createResult(FAILURE,
                    "Relationship type has the wrong base type: " + relationshipType.getBaseTypeId());
            addResult(assertEquals(BaseTypeId.CMIS_RELATIONSHIP, relationshipType.getBaseTypeId(), null, failure));
        } catch (CmisObjectNotFoundException e) {
            addResult(createResult(WARNING, "Relationship type not available!", e, false));
        }

        // policy
        try {
            TypeDefinition policyType = session.getTypeDefinition(BaseTypeId.CMIS_POLICY.value());
            addResult(checkTypeDefinition(session, policyType, "Policy type spec compliance."));

            failure = createResult(FAILURE, "Policy type has the wrong base type: " + policyType.getBaseTypeId());
            addResult(assertEquals(BaseTypeId.CMIS_POLICY, policyType.getBaseTypeId(), null, failure));
        } catch (CmisObjectNotFoundException e) {
            addResult(createResult(WARNING, "Policy type not available!", e, false));
        }

        runTypeChecks(session, session.getTypeDescendants(null, -1, true));
    }

    private void runTypeChecks(Session session, List<Tree<ObjectType>> types) {
        if (types == null) {
            return;
        }

        CmisTestResult failure;

        for (Tree<ObjectType> tree : types) {
            failure = createResult(FAILURE, "Types tree contains null leaf!");
            addResult(assertNotNull(tree, null, failure));

            if (tree != null) {
                addResult(checkTypeDefinition(session, tree.getItem(), "Type spec compliance: "
                        + tree.getItem().getId()));
                runTypeChecks(session, tree.getChildren());
            }
        }
    }
}
