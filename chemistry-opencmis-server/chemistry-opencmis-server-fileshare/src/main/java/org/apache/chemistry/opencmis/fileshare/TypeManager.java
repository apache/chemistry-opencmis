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
package org.apache.chemistry.opencmis.fileshare;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.definitions.MutableDocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableFolderTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutablePropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionListImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.support.TypeDefinitionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Type Manager.
 */
public class TypeManager {
    public static final String DOCUMENT_TYPE_ID = BaseTypeId.CMIS_DOCUMENT.value();
    public static final String FOLDER_TYPE_ID = BaseTypeId.CMIS_FOLDER.value();
    public static final String RELATIONSHIP_TYPE_ID = BaseTypeId.CMIS_RELATIONSHIP.value();
    public static final String POLICY_TYPE_ID = BaseTypeId.CMIS_POLICY.value();
    public static final String ITEM_TYPE_ID = BaseTypeId.CMIS_ITEM.value();
    public static final String SECONDARY_TYPE_ID = BaseTypeId.CMIS_SECONDARY.value();

    private static final String NAMESPACE = "http://chemistry.apache.org/opencmis/fileshare";

    private static final Logger LOG = LoggerFactory.getLogger(TypeManager.class);

    private final CmisVersion cmisVersion;
    private final TypeDefinitionFactory tdf;
    private Map<String, TypeDefinitionContainerImpl> types;
    private List<TypeDefinitionContainer> typesList;

    public TypeManager(CmisVersion cmisVersion) {
        this.cmisVersion = cmisVersion;

        tdf = TypeDefinitionFactory.newInstance();
        tdf.setDefaultNamespace(NAMESPACE);
        tdf.setDefaultControllableAcl(false);
        tdf.setDefaultControllablePolicy(false);
        tdf.setDefaultQueryable(false);
        tdf.setDefaultTypeMutability(tdf.createTypeMutability(false, false, false));

        setup();
    }

    /**
     * Creates the base types.
     */
    private void setup() {
        types = new HashMap<String, TypeDefinitionContainerImpl>();
        typesList = new ArrayList<TypeDefinitionContainer>();

        try {
            // folder type
            MutableFolderTypeDefinition folderType = tdf.createBaseFolderTypeDefinition(cmisVersion);
            removeQueryableAndOrderableFlags(folderType);
            addTypeInteral(folderType);

            // document type
            MutableDocumentTypeDefinition documentType = tdf.createBaseDocumentTypeDefinition(cmisVersion);
            removeQueryableAndOrderableFlags(documentType);
            addTypeInteral(documentType);

            // relationship types
            // not supported - don't expose it
            // addTypeInteral(tdf.createBaseRelationshipTypeDefinition(cmisVersion));

            // policy type
            // not supported - don't expose it
            // addTypeInteral(tdf.createBasePolicyTypeDefinition(cmisVersion));

            if (cmisVersion != CmisVersion.CMIS_1_0) {
                // item type
                // not supported - don't expose it
                // addTypeInteral(tdf.createBaseItemTypeDefinition(cmisVersion));

                // secondary type
                // not supported - don't expose it
                // addTypeInteral(tdf.createBaseSecondaryTypeDefinition(cmisVersion));
            }
        } catch (Exception e) {
            throw new CmisRuntimeException("Cannot set up type defintions!", e);
        }
    }

    /**
     * Removes the queryable and orderable flags from the property definitions
     * of a type definition because this implementations does neither support
     * queries nor can order objects.
     */
    private void removeQueryableAndOrderableFlags(MutableTypeDefinition type) {
        for (PropertyDefinition<?> propDef : type.getPropertyDefinitions().values()) {
            MutablePropertyDefinition<?> mutablePropDef = (MutablePropertyDefinition<?>) propDef;
            mutablePropDef.setIsQueryable(false);
            mutablePropDef.setIsOrderable(false);
        }
    }

    /**
     * Adds a type to the collection with inheriting base type properties.
     */
    public boolean addType(TypeDefinition type) {
        if (type == null) {
            return false;
        }

        if (type.getBaseTypeId() == null) {
            return false;
        }

        // find base type
        TypeDefinitionContainer baseTypeContainer = types.get(type.getBaseTypeId().value());
        if (baseTypeContainer == null) {
            return false;
        }
        TypeDefinition baseType = baseTypeContainer.getTypeDefinition();

        MutableTypeDefinition newType = tdf.copy(type, true);

        // copy base type property definitions and mark them as inherited
        for (PropertyDefinition<?> propDef : baseType.getPropertyDefinitions().values()) {
            MutablePropertyDefinition<?> basePropDef = tdf.copy(propDef);
            basePropDef.setIsInherited(true);
            newType.addPropertyDefinition(basePropDef);
        }

        // add it
        addTypeInteral(newType);

        LOG.info("Added type '" + newType.getId() + "'.");

        return true;
    }

    /**
     * Adds a type to collection.
     */
    private void addTypeInteral(MutableTypeDefinition type) {
        if (type == null) {
            return;
        }

        if (types.containsKey(type.getId())) {
            // can't overwrite a type
            return;
        }

        TypeDefinitionContainerImpl tc = new TypeDefinitionContainerImpl();
        tc.setTypeDefinition(type);

        // add to parent
        if (type.getParentTypeId() != null) {
            TypeDefinitionContainerImpl tdc = types.get(type.getParentTypeId());
            if (tdc != null) {
                tdc.getChildren().add(tc);
            }
        }

        types.put(type.getId(), tc);
        typesList.add(tc);
    }

    /**
     * CMIS getTypesChildren.
     */
    public TypeDefinitionList getTypesChildren(CallContext context, String typeId, boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount) {
        TypeDefinitionListImpl result = new TypeDefinitionListImpl(new ArrayList<TypeDefinition>());

        int skip = (skipCount == null ? 0 : skipCount.intValue());
        if (skip < 0) {
            skip = 0;
        }

        int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
        if (max < 1) {
            return result;
        }

        if (typeId == null) {
            if (skip < 1) {
                result.getList().add(
                        tdf.copy(types.get(FOLDER_TYPE_ID).getTypeDefinition(), includePropertyDefinitions,
                                context.getCmisVersion()));
                max--;
            }
            if ((skip < 2) && (max > 0)) {
                result.getList().add(
                        tdf.copy(types.get(DOCUMENT_TYPE_ID).getTypeDefinition(), includePropertyDefinitions,
                                context.getCmisVersion()));
                max--;
            }

            result.setHasMoreItems((result.getList().size() + skip) < 2);
            result.setNumItems(BigInteger.valueOf(2));
        } else {
            TypeDefinitionContainer tc = types.get(typeId);
            if ((tc == null) || (tc.getChildren() == null)) {
                return result;
            }

            for (TypeDefinitionContainer child : tc.getChildren()) {
                if (skip > 0) {
                    skip--;
                    continue;
                }

                result.getList().add(tdf.copy(child.getTypeDefinition(), includePropertyDefinitions));

                max--;
                if (max == 0) {
                    break;
                }
            }

            result.setHasMoreItems((result.getList().size() + skip) < tc.getChildren().size());
            result.setNumItems(BigInteger.valueOf(tc.getChildren().size()));
        }

        return result;
    }

    /**
     * CMIS getTypesDescendants.
     */
    public List<TypeDefinitionContainer> getTypeDescendants(CallContext context, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions) {
        List<TypeDefinitionContainer> result = new ArrayList<TypeDefinitionContainer>();

        // check depth
        int d = (depth == null ? -1 : depth.intValue());
        if (d == 0) {
            throw new CmisInvalidArgumentException("Depth must not be 0!");
        }
        if (typeId == null) {
            d = -1;
        }

        // set property definition flag to default value if not set
        boolean ipd = (includePropertyDefinitions == null ? false : includePropertyDefinitions.booleanValue());

        if (typeId == null) {
            result.add(getTypesDescendants(context, d, types.get(FOLDER_TYPE_ID), ipd));
            result.add(getTypesDescendants(context, d, types.get(DOCUMENT_TYPE_ID), ipd));
        } else {
            TypeDefinitionContainer tc = types.get(typeId);
            if (tc != null) {
                result.add(getTypesDescendants(context, d, tc, ipd));
            }
        }

        return result;
    }

    /**
     * Gathers the type descendants tree.
     */
    private TypeDefinitionContainer getTypesDescendants(CallContext context, int depth, TypeDefinitionContainer tc,
            boolean includePropertyDefinitions) {
        TypeDefinitionContainerImpl result = new TypeDefinitionContainerImpl();

        TypeDefinition type = tdf.copy(tc.getTypeDefinition(), includePropertyDefinitions, context.getCmisVersion());

        result.setTypeDefinition(type);

        if (depth != 0) {
            for (TypeDefinitionContainer tdc : tc.getChildren()) {
                result.getChildren().add(
                        getTypesDescendants(context, depth < 0 ? -1 : depth - 1, tdc, includePropertyDefinitions));
            }
        }

        return result;
    }

    /**
     * For internal use.
     */
    public TypeDefinition getType(String typeId) {
        TypeDefinitionContainer tc = types.get(typeId);
        if (tc == null) {
            return null;
        }

        return tc.getTypeDefinition();
    }

    /**
     * CMIS getTypeDefinition.
     */
    public TypeDefinition getTypeDefinition(CallContext context, String typeId) {
        TypeDefinitionContainer tc = types.get(typeId);
        if (tc == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        return tdf.copy(tc.getTypeDefinition(), true, context.getCmisVersion());
    }
}
