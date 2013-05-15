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
package org.apache.chemistry.opencmis.inmemory.types;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.SecondaryTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeMutabilityImpl;
import org.apache.chemistry.opencmis.inmemory.NameValidator;

public class InMemorySecondaryTypeDefinition extends SecondaryTypeDefinitionImpl {

    private static final long serialVersionUID = 1L;
    private static final InMemorySecondaryTypeDefinition SECONDARY_TYPE = new InMemorySecondaryTypeDefinition();

    public static InMemorySecondaryTypeDefinition getRootSecondaryType() {
        return SECONDARY_TYPE;
    }

    /* This constructor is just for creating the root document */
    public InMemorySecondaryTypeDefinition() {
        init(BaseTypeId.CMIS_SECONDARY.value(), "Secondary Type", true);
        setParentTypeId(null);
    }

    public InMemorySecondaryTypeDefinition(String id, String displayName) {
        init(id, displayName, false);
        setParentTypeId(SECONDARY_TYPE.getId());
    }

    public InMemorySecondaryTypeDefinition(String id, String displayName, InMemoryDocumentTypeDefinition parentType) {
        // get root type
        init(id, displayName, false);
        if (parentType != null) {
            setBaseTypeId(parentType.getBaseTypeId());
        } else {
            throw new IllegalArgumentException("Must provide a parent type when creating a document type definition");
        }
        setParentTypeId(parentType.getId());
    }

    /**
     * Set the property definitions for this type. The parameter
     * propertyDefinitions should only contain the custom property definitions
     * for this type. The standard property definitions are added automatically.
     *
     * @see org.apache.opencmis.commons.impl.dataobjects.AbstractTypeDefinition#
     * setPropertyDefinitions(java.util.Map)
     */
    public void addCustomPropertyDefinitions(Map<String, PropertyDefinition<?>> propertyDefinitions) {
        DocumentTypeCreationHelper.mergePropertyDefinitions(getPropertyDefinitions(), propertyDefinitions);
    }

    private void init(String id, String displayName, boolean isBaseType) {
        if (!NameValidator.isValidId(id)) {
            throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_ID);
        }

        setBaseTypeId(BaseTypeId.CMIS_SECONDARY);
        setId(id);
        if (displayName == null) {
            displayName = id;
        }
        setDisplayName(displayName);
        // create some suitable defaults for convenience
        setDescription("Description of " + getDisplayName() + " Type");
        setLocalName(id);
        setLocalNamespace(null);
        setQueryName(id);
        setIsControllableAcl(true);
        setIsControllablePolicy(false);
        setIsCreatable(false);
        setIsFileable(false);
        setIsFulltextIndexed(false);
        setIsIncludedInSupertypeQuery(true);
        setIsQueryable(false);

        TypeMutabilityImpl typeMutability = new TypeMutabilityImpl();
        typeMutability.setCanCreate(true);
        typeMutability.setCanDelete(!isBaseType);
        typeMutability.setCanUpdate(!isBaseType);
        setTypeMutability (typeMutability);

        Map<String, PropertyDefinition<?>> props = new HashMap<String, PropertyDefinition<?>>();
        setPropertyDefinitions(props); // set initial empty set of properties

    }
}
