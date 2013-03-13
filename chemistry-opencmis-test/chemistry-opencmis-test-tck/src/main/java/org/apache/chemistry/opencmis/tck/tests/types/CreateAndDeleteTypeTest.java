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
package org.apache.chemistry.opencmis.tck.tests.types;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.util.Map;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

public class CreateAndDeleteTypeTest extends AbstractSessionTest {
    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Create and Delete Type Test");
        setDescription("Creates a document type and deletes it again.");
    }

    @Override
    public void run(Session session) {
        if (session.getRepositoryInfo().getCmisVersion() == CmisVersion.CMIS_1_0) {
            addResult(createResult(SKIPPED, "Items are not supporetd by CMIS 1.0. Test skipped!"));
            return;
        }

        ObjectType parentType = session.getTypeDefinition(getDocumentTestTypeId());
        if (parentType.getTypeMutability() == null || !Boolean.TRUE.equals(parentType.getTypeMutability().canCreate())) {
            addResult(createResult(SKIPPED, "Test type doesn't allow creating a sub-type. Test skipped!"));
            return;
        }

        CmisTestResult failure = null;

        // define the type
        DocumentTypeDefinitionImpl newTypeDef = new DocumentTypeDefinitionImpl();

        newTypeDef.setId("tck:testid");
        newTypeDef.setBaseTypeId(parentType.getBaseTypeId());
        newTypeDef.setParentTypeId(parentType.getId());
        newTypeDef.setLocalName("tck:testlocal");
        newTypeDef.setLocalNamespace("tck:testlocalnamespace");
        newTypeDef.setDisplayName("TCK Document Type");
        newTypeDef.setDescription("This is the TCK document type");
        newTypeDef.setQueryName("tck:testqueryname");
        newTypeDef.setIsQueryable(false);
        newTypeDef.setIsFulltextIndexed(false);
        newTypeDef.setIsIncludedInSupertypeQuery(true);
        newTypeDef.setIsControllableAcl(false);
        newTypeDef.setIsControllablePolicy(false);
        newTypeDef.setIsCreatable(true);
        newTypeDef.setIsFileable(true);
        newTypeDef.setIsVersionable(false);
        newTypeDef.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);

        // create the type
        ObjectType newType = null;
        try {
            newType = session.createType(newTypeDef);
        } catch (CmisBaseException e) {
            addResult(createResult(FAILURE, "Creating type '" + newType.getId() + "' failed: " + e.getMessage()));
            return;
        }

        // check type
        addResult(checkTypeDefinition(session, newType, "Newly created type spec compliance."));

        // get the type
        ObjectType newType2 = null;
        try {
            newType2 = session.getTypeDefinition(newType.getId());
        } catch (CmisObjectNotFoundException e) {
            addResult(createResult(FAILURE, "Newly created type '" + newType.getId() + "' can not be fetched."));
        }

        // assert type definitions
        failure = createResult(FAILURE,
                "The type definition returned by createType() doesn't match the type definition returned by getTypeDefinition()!");
        addResult(assertEquals(newType, newType2, null, failure));

        // check if type can be deleted
        if (newType.getTypeMutability() == null || !Boolean.TRUE.equals(newType.getTypeMutability().canDelete())) {
            addResult(createResult(WARNING, "Newly created type indicates that it cannot be deleted. Trying it anyway."));
        }

        // delete the type
        try {
            session.deleteType(newType.getId());
        } catch (CmisBaseException e) {
            addResult(createResult(FAILURE, "Deleting type '" + newType.getId() + "' failed: " + e.getMessage()));
        }
    }

}
