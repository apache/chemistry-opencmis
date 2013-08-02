/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.chemistry.opencmis.server.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableDocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableFolderTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableItemTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutablePolicyTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutablePropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableRelationshipTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableSecondaryTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDateTimeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDecimalDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyIntegerDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyStringDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeMutability;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CmisExtensionElementImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ItemTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PolicyTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RelationshipTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.SecondaryTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeMutabilityImpl;

/**
 * Type definition factory.
 */
public class TypeDefinitionFactory {

    private Class<? extends MutableDocumentTypeDefinition> documentTypeDefinitionClass;
    private Class<? extends MutableFolderTypeDefinition> folderTypeDefinitionClass;
    private Class<? extends MutablePolicyTypeDefinition> policyTypeDefinitionClass;
    private Class<? extends MutableRelationshipTypeDefinition> relationshipTypeDefinitionClass;
    private Class<? extends MutableItemTypeDefinition> itemTypeDefinitionClass;
    private Class<? extends MutableSecondaryTypeDefinition> secondaryTypeDefinitionClass;

    private String defaultNamespace;
    private boolean defaultControllableAcl;
    private boolean defaultControllablePolicy;
    private boolean defaultQueryable;
    private boolean defaultFulltextIndexed;
    private TypeMutability defaultTypeMutability;

    private TypeDefinitionFactory() {
        documentTypeDefinitionClass = DocumentTypeDefinitionImpl.class;
        folderTypeDefinitionClass = FolderTypeDefinitionImpl.class;
        policyTypeDefinitionClass = PolicyTypeDefinitionImpl.class;
        relationshipTypeDefinitionClass = RelationshipTypeDefinitionImpl.class;
        itemTypeDefinitionClass = ItemTypeDefinitionImpl.class;
        secondaryTypeDefinitionClass = SecondaryTypeDefinitionImpl.class;

        defaultNamespace = "http://defaultNamespace";
        defaultControllableAcl = false;
        defaultControllablePolicy = false;
        defaultQueryable = true;
        defaultFulltextIndexed = false;

        TypeMutabilityImpl typeMutability = new TypeMutabilityImpl();
        typeMutability.setCanCreate(false);
        typeMutability.setCanUpdate(false);
        typeMutability.setCanDelete(false);
        defaultTypeMutability = typeMutability;
    }

    /**
     * Creates a new instance of the factory.
     */
    public static TypeDefinitionFactory newInstance() {
        return new TypeDefinitionFactory();
    }

    // --- definition classes ---

    public Class<? extends MutableDocumentTypeDefinition> getDocumentTypeDefinitionClass() {
        return documentTypeDefinitionClass;
    }

    public void setDocumentTypeDefinitionClass(
            Class<? extends MutableDocumentTypeDefinition> documentTypeDefinitionClass) {
        checkClass(documentTypeDefinitionClass);
        this.documentTypeDefinitionClass = documentTypeDefinitionClass;
    }

    protected MutableDocumentTypeDefinition createDocumentTypeDefinitionObject() {
        try {
            return documentTypeDefinitionClass.newInstance();
        } catch (Exception e) {
            throw new CmisRuntimeException("Cannot create type defintion object: " + e.getMessage(), e);
        }
    }

    public Class<? extends MutableFolderTypeDefinition> getFolderTypeDefinitionClass() {
        return folderTypeDefinitionClass;
    }

    public void setFolderTypeDefinitionClass(Class<? extends MutableFolderTypeDefinition> folderTypeDefinitionClass) {
        checkClass(folderTypeDefinitionClass);
        this.folderTypeDefinitionClass = folderTypeDefinitionClass;
    }

    protected MutableFolderTypeDefinition createFolderTypeDefinitionObject() {
        try {
            return folderTypeDefinitionClass.newInstance();
        } catch (Exception e) {
            throw new CmisRuntimeException("Cannot create type defintion object: " + e.getMessage(), e);
        }
    }

    public Class<? extends MutablePolicyTypeDefinition> getPolicyTypeDefinitionClass() {
        return policyTypeDefinitionClass;
    }

    public void setPolicyTypeDefinitionClass(Class<? extends MutablePolicyTypeDefinition> policyTypeDefinitionClass) {
        checkClass(policyTypeDefinitionClass);
        this.policyTypeDefinitionClass = policyTypeDefinitionClass;
    }

    protected MutablePolicyTypeDefinition createPolicyTypeDefinitionObject() {
        try {
            return policyTypeDefinitionClass.newInstance();
        } catch (Exception e) {
            throw new CmisRuntimeException("Cannot create type defintion object: " + e.getMessage(), e);
        }
    }

    public Class<? extends MutableRelationshipTypeDefinition> getRelationshipTypeDefinitionClass() {
        return relationshipTypeDefinitionClass;
    }

    public void setRelationshipTypeDefinitionClass(
            Class<? extends MutableRelationshipTypeDefinition> relationshipTypeDefinitionClass) {
        checkClass(relationshipTypeDefinitionClass);
        this.relationshipTypeDefinitionClass = relationshipTypeDefinitionClass;
    }

    protected MutableRelationshipTypeDefinition createRelationshipTypeDefinitionObject() {
        try {
            return relationshipTypeDefinitionClass.newInstance();
        } catch (Exception e) {
            throw new CmisRuntimeException("Cannot create type defintion object: " + e.getMessage(), e);
        }
    }

    public Class<? extends MutableItemTypeDefinition> getItemTypeDefinitionClass() {
        return itemTypeDefinitionClass;
    }

    public void setItemTypeDefinitionClass(Class<? extends MutableItemTypeDefinition> itemTypeDefinitionClass) {
        checkClass(itemTypeDefinitionClass);
        this.itemTypeDefinitionClass = itemTypeDefinitionClass;
    }

    protected MutableItemTypeDefinition createItemTypeDefinitionObject() {
        try {
            return itemTypeDefinitionClass.newInstance();
        } catch (Exception e) {
            throw new CmisRuntimeException("Cannot create type defintion object: " + e.getMessage(), e);
        }
    }

    public Class<? extends MutableSecondaryTypeDefinition> getSecondaryTypeDefinitionClass() {
        return secondaryTypeDefinitionClass;
    }

    public void setSecondaryTypeDefinitionClass(
            Class<? extends MutableSecondaryTypeDefinition> secondaryTypeDefinitionClass) {
        checkClass(secondaryTypeDefinitionClass);
        this.secondaryTypeDefinitionClass = secondaryTypeDefinitionClass;
    }

    protected MutableSecondaryTypeDefinition createSecondaryTypeDefinitionObject() {
        try {
            return secondaryTypeDefinitionClass.newInstance();
        } catch (Exception e) {
            throw new CmisRuntimeException("Cannot create type defintion object: " + e.getMessage(), e);
        }
    }

    private void checkClass(Class<? extends MutableTypeDefinition> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class must be set!");
        }

        // check for default constructor
        try {
            clazz.getConstructor(new Class[0]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Class has no accessible default constructor!", e);
        }
    }

    // --- default values ---

    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    public void setDefaultNamespace(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    public boolean getDefaultControllableAcl() {
        return defaultControllableAcl;
    }

    public void setDefaultControllableAcl(boolean defaultControllableAcl) {
        this.defaultControllableAcl = defaultControllableAcl;
    }

    public boolean getDefaultControllablePolicy() {
        return defaultControllablePolicy;
    }

    public void setDefaultControllablePolicy(boolean defaultControllablePolicy) {
        this.defaultControllablePolicy = defaultControllablePolicy;
    }

    public boolean getDefaultQueryable() {
        return defaultQueryable;
    }

    public void setDefaultQueryable(boolean defaultQueryable) {
        this.defaultQueryable = defaultQueryable;
    }

    public boolean getDefaultFulltextIndexed() {
        return defaultFulltextIndexed;
    }

    public void setDefaultFulltextIndexed(boolean defaultFulltextIndexed) {
        this.defaultFulltextIndexed = defaultFulltextIndexed;
    }

    public TypeMutability getDefaultTypeMutability() {
        return defaultTypeMutability;
    }

    public void setDefaultTypeMutability(TypeMutability defaultTypeMutability) {
        this.defaultTypeMutability = defaultTypeMutability;
    }

    // --- create methods ---

    /**
     * Creates a new type mutability object.
     */
    public TypeMutability createTypeMutability(boolean canCreate, boolean canUpdate, boolean canDelete) {
        TypeMutabilityImpl result = new TypeMutabilityImpl();

        result.setCanCreate(canCreate);
        result.setCanUpdate(canUpdate);
        result.setCanDelete(canDelete);

        return result;
    }

    /**
     * Creates a new mutable base document type definition including all
     * property definitions defined in the CMIS specification.
     */
    public MutableDocumentTypeDefinition createBaseDocumentTypeDefinition(CmisVersion cmisVersion) {
        return createDocumentTypeDefinition(cmisVersion, null);
    }

    /**
     * Creates a new mutable document type definition including all base
     * property definitions defined in the CMIS specification.
     */
    public MutableDocumentTypeDefinition createDocumentTypeDefinition(CmisVersion cmisVersion, String parentId) {
        MutableDocumentTypeDefinition documentType = createDocumentTypeDefinitionObject();
        documentType.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
        documentType.setParentTypeId(parentId);
        documentType.setIsControllableAcl(defaultControllableAcl);
        documentType.setIsControllablePolicy(defaultControllablePolicy);
        documentType.setIsCreatable(true);
        documentType.setDescription("Document");
        documentType.setDisplayName("Document");
        documentType.setIsFileable(true);
        documentType.setIsFulltextIndexed(defaultFulltextIndexed);
        documentType.setIsIncludedInSupertypeQuery(true);
        documentType.setLocalName("Document");
        documentType.setLocalNamespace(defaultNamespace);
        documentType.setIsQueryable(defaultQueryable);
        documentType.setQueryName("cmis:document");
        documentType.setId(BaseTypeId.CMIS_DOCUMENT.value());
        if (cmisVersion != CmisVersion.CMIS_1_0) {
            documentType.setTypeMutability(defaultTypeMutability);
        }

        documentType.setIsVersionable(false);
        documentType.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);

        addBasePropertyDefinitions(documentType, cmisVersion, parentId != null);
        addDocumentPropertyDefinitions(documentType, cmisVersion, parentId != null);

        return documentType;
    }

    /**
     * Creates a new mutable document type definition, which is a child of the
     * provided type definition. Property definitions are copied from the parent
     * and marked as inherited.
     * 
     * @param parentTypeDefinition
     *            the type definition of the parent
     * @param id
     *            the id of the child type definition
     * 
     * @return a mutable child type definition
     */
    public MutableDocumentTypeDefinition createChildDocumentTypeDefinition(DocumentTypeDefinition parentTypeDefinition,
            String id) {
        return createChildDocumentTypeDefinition(parentTypeDefinition, id, id, id, id, null, true);
    }

    /**
     * Creates a new mutable document type definition, which is a child of the
     * provided type definition. If the parameter
     * <code>includePropertyDefinitions</code> is to
     * <code>true</true> property definitions are copied from the parent
     * and marked as inherited.
     */
    public MutableDocumentTypeDefinition createChildDocumentTypeDefinition(DocumentTypeDefinition parentTypeDefinition,
            String id, String localName, String queryName, String displayName, String description,
            boolean includePropertyDefinitions) {
        MutableDocumentTypeDefinition documentType = createDocumentTypeDefinitionObject();
        documentType.setBaseTypeId(parentTypeDefinition.getBaseTypeId());
        documentType.setParentTypeId(parentTypeDefinition.getId());
        documentType.setIsControllableAcl(parentTypeDefinition.isControllableAcl());
        documentType.setIsControllablePolicy(parentTypeDefinition.isControllablePolicy());
        documentType.setIsCreatable(parentTypeDefinition.isCreatable());
        documentType.setDescription(description);
        documentType.setDisplayName(displayName);
        documentType.setIsFileable(parentTypeDefinition.isFileable());
        documentType.setIsFulltextIndexed(parentTypeDefinition.isFulltextIndexed());
        documentType.setIsIncludedInSupertypeQuery(parentTypeDefinition.isIncludedInSupertypeQuery());
        documentType.setLocalName(localName);
        documentType.setLocalNamespace(parentTypeDefinition.getLocalNamespace());
        documentType.setIsQueryable(parentTypeDefinition.isQueryable());
        documentType.setQueryName(queryName);
        documentType.setId(id);
        documentType.setTypeMutability(parentTypeDefinition.getTypeMutability());
        documentType.setIsVersionable(parentTypeDefinition.isVersionable());
        documentType.setContentStreamAllowed(parentTypeDefinition.getContentStreamAllowed());

        if (includePropertyDefinitions) {
            copyPropertyDefinitions(parentTypeDefinition, documentType, true);
        }

        return documentType;
    }

    /**
     * Creates a new mutable base folder type definition including all property
     * definitions defined in the CMIS specification.
     */
    public MutableFolderTypeDefinition createBaseFolderTypeDefinition(CmisVersion cmisVersion) {
        return createFolderTypeDefinition(cmisVersion, null);
    }

    /**
     * Creates a new mutable folder type definition including all base property
     * definitions defined in the CMIS specification.
     */
    public MutableFolderTypeDefinition createFolderTypeDefinition(CmisVersion cmisVersion, String parentId) {
        MutableFolderTypeDefinition folderType = createFolderTypeDefinitionObject();
        folderType.setBaseTypeId(BaseTypeId.CMIS_FOLDER);
        folderType.setParentTypeId(parentId);
        folderType.setIsControllableAcl(defaultControllableAcl);
        folderType.setIsControllablePolicy(defaultControllablePolicy);
        folderType.setIsCreatable(true);
        folderType.setDescription("Folder");
        folderType.setDisplayName("Folder");
        folderType.setIsFileable(true);
        folderType.setIsFulltextIndexed(defaultFulltextIndexed);
        folderType.setIsIncludedInSupertypeQuery(true);
        folderType.setLocalName("Folder");
        folderType.setLocalNamespace(defaultNamespace);
        folderType.setIsQueryable(defaultQueryable);
        folderType.setQueryName("cmis:folder");
        folderType.setId(BaseTypeId.CMIS_FOLDER.value());
        if (cmisVersion != CmisVersion.CMIS_1_0) {
            folderType.setTypeMutability(defaultTypeMutability);
        }

        addBasePropertyDefinitions(folderType, cmisVersion, parentId != null);
        addFolderPropertyDefinitions(folderType, cmisVersion, parentId != null);

        return folderType;
    }

    /**
     * Creates a new mutable base policy type definition including all property
     * definitions defined in the CMIS specification.
     */
    public MutablePolicyTypeDefinition createBasePolicyTypeDefinition(CmisVersion cmisVersion) {
        return createPolicyTypeDefinition(cmisVersion, null);
    }

    /**
     * Creates a new mutable policy type definition including all base property
     * definitions defined in the CMIS specification.
     */
    public MutablePolicyTypeDefinition createPolicyTypeDefinition(CmisVersion cmisVersion, String parentId) {
        MutablePolicyTypeDefinition policyType = createPolicyTypeDefinitionObject();
        policyType.setBaseTypeId(BaseTypeId.CMIS_POLICY);
        policyType.setParentTypeId(parentId);
        policyType.setIsControllableAcl(defaultControllableAcl);
        policyType.setIsControllablePolicy(defaultControllablePolicy);
        policyType.setIsCreatable(false);
        policyType.setDescription("Policy");
        policyType.setDisplayName("Policy");
        policyType.setIsFileable(false);
        policyType.setIsFulltextIndexed(defaultFulltextIndexed);
        policyType.setIsIncludedInSupertypeQuery(true);
        policyType.setLocalName("Policy");
        policyType.setLocalNamespace(defaultNamespace);
        policyType.setIsQueryable(defaultQueryable);
        policyType.setQueryName("cmis:policy");
        policyType.setId(BaseTypeId.CMIS_POLICY.value());
        if (cmisVersion != CmisVersion.CMIS_1_0) {
            policyType.setTypeMutability(defaultTypeMutability);
        }

        addBasePropertyDefinitions(policyType, cmisVersion, parentId != null);
        addPolicyPropertyDefinitions(policyType, cmisVersion, parentId != null);

        return policyType;
    }

    /**
     * Creates a new mutable base relationship type definition including all
     * property definitions defined in the CMIS specification.
     */
    public MutableRelationshipTypeDefinition createBaseRelationshipTypeDefinition(CmisVersion cmisVersion) {
        return createRelationshipTypeDefinition(cmisVersion, null);
    }

    /**
     * Creates a new mutable relationship type definition including all base
     * property definitions defined in the CMIS specification.
     */
    public MutableRelationshipTypeDefinition createRelationshipTypeDefinition(CmisVersion cmisVersion, String parentId) {
        MutableRelationshipTypeDefinition relationshipType = createRelationshipTypeDefinitionObject();
        relationshipType.setBaseTypeId(BaseTypeId.CMIS_RELATIONSHIP);
        relationshipType.setParentTypeId(parentId);
        relationshipType.setIsControllableAcl(defaultControllableAcl);
        relationshipType.setIsControllablePolicy(defaultControllablePolicy);
        relationshipType.setIsCreatable(false);
        relationshipType.setDescription("Relationship");
        relationshipType.setDisplayName("Relationship");
        relationshipType.setIsFileable(false);
        relationshipType.setIsFulltextIndexed(defaultFulltextIndexed);
        relationshipType.setIsIncludedInSupertypeQuery(true);
        relationshipType.setLocalName("Relationship");
        relationshipType.setLocalNamespace(defaultNamespace);
        relationshipType.setIsQueryable(defaultQueryable);
        relationshipType.setQueryName("cmis:relationship");
        relationshipType.setId(BaseTypeId.CMIS_RELATIONSHIP.value());
        if (cmisVersion != CmisVersion.CMIS_1_0) {
            relationshipType.setTypeMutability(defaultTypeMutability);
        }

        addBasePropertyDefinitions(relationshipType, cmisVersion, parentId != null);
        addRelationshipPropertyDefinitions(relationshipType, cmisVersion, parentId != null);

        return relationshipType;
    }

    /**
     * Creates a new mutable base item type definition including all property
     * definitions defined in the CMIS specification.
     */
    public MutableItemTypeDefinition createBaseItemTypeDefinition(CmisVersion cmisVersion) {
        return createItemTypeDefinition(cmisVersion, null);
    }

    /**
     * Creates a new mutable item type definition including all base property
     * definitions defined in the CMIS specification.
     */
    public MutableItemTypeDefinition createItemTypeDefinition(CmisVersion cmisVersion, String parentId) {
        if (cmisVersion == CmisVersion.CMIS_1_0) {
            throw new IllegalArgumentException("CMIS 1.0 doesn't support item types!");
        }

        MutableItemTypeDefinition itemType = createItemTypeDefinitionObject();
        itemType.setBaseTypeId(BaseTypeId.CMIS_ITEM);
        itemType.setParentTypeId(parentId);
        itemType.setIsControllableAcl(defaultControllableAcl);
        itemType.setIsControllablePolicy(defaultControllablePolicy);
        itemType.setIsCreatable(true);
        itemType.setDescription("Item");
        itemType.setDisplayName("Item");
        itemType.setIsFileable(true);
        itemType.setIsFulltextIndexed(defaultFulltextIndexed);
        itemType.setIsIncludedInSupertypeQuery(true);
        itemType.setLocalName("Item");
        itemType.setLocalNamespace(defaultNamespace);
        itemType.setIsQueryable(defaultQueryable);
        itemType.setQueryName("cmis:item");
        itemType.setId(BaseTypeId.CMIS_ITEM.value());
        itemType.setTypeMutability(defaultTypeMutability);

        addBasePropertyDefinitions(itemType, cmisVersion, parentId != null);

        return itemType;
    }

    /**
     * Creates a new mutable base secondary type definition.
     */
    public MutableSecondaryTypeDefinition createBaseSecondaryTypeDefinition(CmisVersion cmisVersion) {
        return createSecondaryTypeDefinition(cmisVersion, null);
    }

    /**
     * Creates a new mutable secondary type definition.
     */
    public MutableSecondaryTypeDefinition createSecondaryTypeDefinition(CmisVersion cmisVersion, String parentId) {
        if (cmisVersion == CmisVersion.CMIS_1_0) {
            throw new IllegalArgumentException("CMIS 1.0 doesn't support secondary types!");
        }

        MutableSecondaryTypeDefinition secondaryType = createSecondaryTypeDefinitionObject();
        secondaryType.setBaseTypeId(BaseTypeId.CMIS_SECONDARY);
        secondaryType.setParentTypeId(parentId);
        secondaryType.setIsControllableAcl(defaultControllableAcl);
        secondaryType.setIsControllablePolicy(defaultControllablePolicy);
        secondaryType.setIsCreatable(true);
        secondaryType.setDescription("Secondary");
        secondaryType.setDisplayName("Secondary");
        secondaryType.setIsFileable(false);
        secondaryType.setIsFulltextIndexed(false);
        secondaryType.setIsIncludedInSupertypeQuery(true);
        secondaryType.setLocalName("Secondary");
        secondaryType.setLocalNamespace(defaultNamespace);
        secondaryType.setIsQueryable(defaultQueryable);
        secondaryType.setQueryName("cmis:secondary");
        secondaryType.setId(BaseTypeId.CMIS_SECONDARY.value());
        secondaryType.setTypeMutability(defaultTypeMutability);

        return secondaryType;
    }

    // --- copy methods ---

    /**
     * Copies the given type definition and returns a mutable object.
     */
    public MutableTypeDefinition copy(TypeDefinition sourceTypeDefintion, boolean includePropertyDefinitions) {
        if (sourceTypeDefintion == null) {
            return null;
        }

        MutableTypeDefinition result = null;

        switch (sourceTypeDefintion.getBaseTypeId()) {
        case CMIS_DOCUMENT:
            result = createDocumentTypeDefinitionObject();
            break;
        case CMIS_FOLDER:
            result = createFolderTypeDefinitionObject();
            break;
        case CMIS_POLICY:
            result = createPolicyTypeDefinitionObject();
            break;
        case CMIS_RELATIONSHIP:
            result = createRelationshipTypeDefinitionObject();
            break;
        case CMIS_ITEM:
            result = createItemTypeDefinitionObject();
            break;
        case CMIS_SECONDARY:
            result = createSecondaryTypeDefinitionObject();
            break;
        default:
            throw new RuntimeException("Unknown base type!");
        }

        // TODO: copy attributes

        copyExtensions(sourceTypeDefintion, result);

        if (includePropertyDefinitions) {
            copyPropertyDefinitions(sourceTypeDefintion, result, false);
        }

        return result;
    }

    /**
     * Copies the given property definition and returns a mutable object.
     */
    public MutablePropertyDefinition<?> copy(PropertyDefinition<?> sourcePropertyDefinition) {
        if (sourcePropertyDefinition == null) {
            return null;
        }

        MutablePropertyDefinition<?> result = null;

        switch (sourcePropertyDefinition.getPropertyType()) {
        case BOOLEAN:
            result = new PropertyBooleanDefinitionImpl();
            break;
        case DATETIME:
            result = new PropertyDateTimeDefinitionImpl();
            ((PropertyDateTimeDefinitionImpl) result)
                    .setDateTimeResolution(((PropertyDateTimeDefinition) sourcePropertyDefinition)
                            .getDateTimeResolution());
            break;
        case DECIMAL:
            result = new PropertyDecimalDefinitionImpl();
            ((PropertyDecimalDefinitionImpl) result).setMinValue(((PropertyDecimalDefinition) sourcePropertyDefinition)
                    .getMinValue());
            ((PropertyDecimalDefinitionImpl) result).setMaxValue(((PropertyDecimalDefinition) sourcePropertyDefinition)
                    .getMaxValue());
            ((PropertyDecimalDefinitionImpl) result)
                    .setPrecision(((PropertyDecimalDefinition) sourcePropertyDefinition).getPrecision());
            break;
        case HTML:
            result = new PropertyHtmlDefinitionImpl();
            break;
        case ID:
            result = new PropertyIdDefinitionImpl();
            break;
        case INTEGER:
            result = new PropertyIntegerDefinitionImpl();
            ((PropertyIntegerDefinitionImpl) result).setMinValue(((PropertyIntegerDefinition) sourcePropertyDefinition)
                    .getMinValue());
            ((PropertyIntegerDefinitionImpl) result).setMaxValue(((PropertyIntegerDefinition) sourcePropertyDefinition)
                    .getMaxValue());
            break;
        case STRING:
            result = new PropertyStringDefinitionImpl();
            ((PropertyStringDefinitionImpl) result).setMaxLength((((PropertyStringDefinition) sourcePropertyDefinition)
                    .getMaxLength()));
            break;
        case URI:
            result = new PropertyUriDefinitionImpl();
            break;
        default:
            throw new RuntimeException("Unknown datatype!");
        }

        result.setId(sourcePropertyDefinition.getId());
        result.setLocalName(sourcePropertyDefinition.getLocalName());
        result.setDisplayName(sourcePropertyDefinition.getDisplayName());
        result.setDescription(sourcePropertyDefinition.getDescription());
        result.setPropertyType(sourcePropertyDefinition.getPropertyType());
        result.setCardinality(sourcePropertyDefinition.getCardinality());
        result.setUpdatability(sourcePropertyDefinition.getUpdatability());
        result.setIsInherited(sourcePropertyDefinition.isInherited());
        result.setIsRequired(sourcePropertyDefinition.isRequired());
        result.setIsQueryable(sourcePropertyDefinition.isQueryable());
        result.setIsOrderable(sourcePropertyDefinition.isOrderable());
        result.setQueryName(sourcePropertyDefinition.getQueryName());

        // TODO: handle default values and choices

        copyExtensions(sourcePropertyDefinition, result);

        return result;
    }

    // --- internal methods ---

    /**
     * Copies the property definitions from a source type to a target type.
     */
    protected void copyPropertyDefinitions(TypeDefinition source, MutableTypeDefinition target, boolean markAsInherited) {
        if (source != null && source.getPropertyDefinitions() != null) {
            for (PropertyDefinition<?> propDef : source.getPropertyDefinitions().values()) {
                MutablePropertyDefinition<?> newPropDef = copy(propDef);
                if (markAsInherited) {
                    newPropDef.setIsInherited(true);
                }
                target.addPropertyDefinition(newPropDef);
            }
        }
    }

    /**
     * Makes a deep copy of extension of a source object and adds them to a
     * target object.
     */
    protected void copyExtensions(ExtensionsData source, ExtensionsData target) {
        if (source == null || target == null) {
            return;
        }

        if (source.getExtensions() == null) {
            target.setExtensions(null);
            return;
        }

        List<CmisExtensionElement> elementList = new ArrayList<CmisExtensionElement>();
        for (CmisExtensionElement element : source.getExtensions()) {
            elementList.add(copy(element));
        }

        target.setExtensions(elementList);
    }

    /**
     * Makes a deep copy of an extension element.
     */
    private CmisExtensionElement copy(CmisExtensionElement element) {
        if (element == null) {
            return null;
        }

        Map<String, String> attrs = (element.getAttributes() != null ? new HashMap<String, String>(
                element.getAttributes()) : null);

        if (element.getChildren() == null) {
            return new CmisExtensionElementImpl(element.getNamespace(), element.getName(), attrs, element.getValue());
        } else {
            List<CmisExtensionElement> children = new ArrayList<CmisExtensionElement>();

            for (CmisExtensionElement child : element.getChildren()) {
                children.add(copy(child));
            }

            return new CmisExtensionElementImpl(element.getNamespace(), element.getName(), attrs, children);
        }
    }

    /**
     * Adds the base property definitions to a type definition.
     */
    protected void addBasePropertyDefinitions(MutableTypeDefinition type, CmisVersion cmisVersion, boolean inherited) {
        type.addPropertyDefinition(createPropDef(PropertyIds.NAME, "Name", "Name", PropertyType.STRING,
                Cardinality.SINGLE, Updatability.READWRITE, inherited, true, true, true));

        if (cmisVersion != CmisVersion.CMIS_1_0) {
            type.addPropertyDefinition(createPropDef(PropertyIds.DESCRIPTION, "Description", "Description",
                    PropertyType.STRING, Cardinality.SINGLE, Updatability.READWRITE, inherited, false, false, false));
        }

        type.addPropertyDefinition(createPropDef(PropertyIds.OBJECT_ID, "Object Id", "Object Id", PropertyType.ID,
                Cardinality.SINGLE, Updatability.READONLY, inherited, false, true, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.BASE_TYPE_ID, "Base Type Id", "Base Type Id",
                PropertyType.ID, Cardinality.SINGLE, Updatability.READONLY, inherited, false, true, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.OBJECT_TYPE_ID, "Object Type Id", "Object Type Id",
                PropertyType.ID, Cardinality.SINGLE, Updatability.ONCREATE, inherited, true, true, false));

        if (cmisVersion != CmisVersion.CMIS_1_0) {
            type.addPropertyDefinition(createPropDef(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "Secondary Type Ids",
                    "Secondary Type Ids", PropertyType.ID, Cardinality.MULTI, Updatability.READONLY, inherited, false,
                    true, false));
        }

        type.addPropertyDefinition(createPropDef(PropertyIds.CREATED_BY, "Created By", "Created By",
                PropertyType.STRING, Cardinality.SINGLE, Updatability.READONLY, inherited, false, true, true));

        type.addPropertyDefinition(createPropDef(PropertyIds.CREATION_DATE, "Creation Date", "Creation Date",
                PropertyType.DATETIME, Cardinality.SINGLE, Updatability.READONLY, inherited, false, true, true));

        type.addPropertyDefinition(createPropDef(PropertyIds.LAST_MODIFIED_BY, "Last Modified By", "Last Modified By",
                PropertyType.STRING, Cardinality.SINGLE, Updatability.READONLY, inherited, false, true, true));

        type.addPropertyDefinition(createPropDef(PropertyIds.LAST_MODIFICATION_DATE, "Last Modification Date",
                "Last Modification Date", PropertyType.DATETIME, Cardinality.SINGLE, Updatability.READONLY, inherited,
                false, true, true));

        type.addPropertyDefinition(createPropDef(PropertyIds.CHANGE_TOKEN, "Change Token", "Change Token",
                PropertyType.STRING, Cardinality.SINGLE, Updatability.READONLY, inherited, false, false, false));
    }

    protected void addDocumentPropertyDefinitions(MutableDocumentTypeDefinition type, CmisVersion cmisVersion,
            boolean inherited) {
        type.addPropertyDefinition(createPropDef(PropertyIds.IS_IMMUTABLE, "Is Immutable", "Is Immutable",
                PropertyType.BOOLEAN, Cardinality.SINGLE, Updatability.READONLY, inherited, false, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.IS_LATEST_VERSION, "Is Latest Version",
                "Is Latest Version", PropertyType.BOOLEAN, Cardinality.SINGLE, Updatability.READONLY, inherited, false,
                false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.IS_MAJOR_VERSION, "Is Major Version", "Is Major Version",
                PropertyType.BOOLEAN, Cardinality.SINGLE, Updatability.READONLY, inherited, false, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.IS_LATEST_MAJOR_VERSION, "Is Latest Major Version",
                "Is Latest Major Version", PropertyType.BOOLEAN, Cardinality.SINGLE, Updatability.READONLY, inherited,
                false, false, false));

        if (cmisVersion != CmisVersion.CMIS_1_0) {
            type.addPropertyDefinition(createPropDef(PropertyIds.IS_PRIVATE_WORKING_COPY, "Is Private Working Copy",
                    "Is Private Working Copy", PropertyType.BOOLEAN, Cardinality.SINGLE, Updatability.READONLY,
                    inherited, false, true, false));
        }

        type.addPropertyDefinition(createPropDef(PropertyIds.VERSION_LABEL, "Version Label", "Version Label",
                PropertyType.STRING, Cardinality.SINGLE, Updatability.READONLY, inherited, false, true, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.VERSION_SERIES_ID, "Version Series Id",
                "Version Series Id", PropertyType.ID, Cardinality.SINGLE, Updatability.READONLY, inherited, false,
                true, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT,
                "Is Verison Series Checked Out", "Is Verison Series Checked Out", PropertyType.BOOLEAN,
                Cardinality.SINGLE, Updatability.READONLY, inherited, false, true, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY,
                "Version Series Checked Out By", "Version Series Checked Out By", PropertyType.STRING,
                Cardinality.SINGLE, Updatability.READONLY, inherited, false, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID,
                "Version Series Checked Out Id", "Version Series Checked Out Id", PropertyType.ID, Cardinality.SINGLE,
                Updatability.READONLY, inherited, false, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.CHECKIN_COMMENT, "Checkin Comment", "Checkin Comment",
                PropertyType.STRING, Cardinality.SINGLE, Updatability.READONLY, inherited, false, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.CONTENT_STREAM_LENGTH, "Content Stream Length",
                "Content Stream Length", PropertyType.INTEGER, Cardinality.SINGLE, Updatability.READONLY, inherited,
                false, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.CONTENT_STREAM_MIME_TYPE, "MIME Type", "MIME Type",
                PropertyType.STRING, Cardinality.SINGLE, Updatability.READONLY, inherited, false, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.CONTENT_STREAM_FILE_NAME, "Filename", "Filename",
                PropertyType.STRING, Cardinality.SINGLE, Updatability.READONLY, inherited, false, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.CONTENT_STREAM_ID, "Content Stream Id",
                "Content Stream Id", PropertyType.ID, Cardinality.SINGLE, Updatability.READONLY, inherited, false,
                false, false));
    }

    protected void addFolderPropertyDefinitions(MutableFolderTypeDefinition type, CmisVersion cmisVersion,
            boolean inherited) {
        type.addPropertyDefinition(createPropDef(PropertyIds.PARENT_ID, "Parent Id", "Parent Id", PropertyType.ID,
                Cardinality.SINGLE, Updatability.READONLY, inherited, false, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.PATH, "Path", "Path", PropertyType.STRING,
                Cardinality.SINGLE, Updatability.READONLY, inherited, false, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS,
                "Allowed Child Object Type Ids", "Allowed Child Object Type Ids", PropertyType.ID, Cardinality.MULTI,
                Updatability.READONLY, inherited, false, false, false));
    }

    protected void addPolicyPropertyDefinitions(MutablePolicyTypeDefinition type, CmisVersion cmisVersion,
            boolean inherited) {
        type.addPropertyDefinition(createPropDef(PropertyIds.POLICY_TEXT, "Policy Text", "Policy Text",
                PropertyType.STRING, Cardinality.SINGLE, Updatability.READWRITE, inherited, false, false, false));
    }

    protected void addRelationshipPropertyDefinitions(MutableRelationshipTypeDefinition type, CmisVersion cmisVersion,
            boolean inherited) {
        type.addPropertyDefinition(createPropDef(PropertyIds.SOURCE_ID, "Source Id", "Source Id", PropertyType.ID,
                Cardinality.SINGLE, Updatability.READWRITE, inherited, true, false, false));

        type.addPropertyDefinition(createPropDef(PropertyIds.TARGET_ID, "Target Id", "Target Id", PropertyType.ID,
                Cardinality.SINGLE, Updatability.READWRITE, inherited, true, false, false));
    }

    /**
     * Creates a property definition object.
     */
    protected MutablePropertyDefinition<?> createPropDef(String id, String displayName, String description,
            PropertyType datatype, Cardinality cardinality, Updatability updateability, boolean inherited,
            boolean required, boolean queryable, boolean orderable) {
        MutablePropertyDefinition<?> result = null;

        switch (datatype) {
        case BOOLEAN:
            result = new PropertyBooleanDefinitionImpl();
            break;
        case DATETIME:
            result = new PropertyDateTimeDefinitionImpl();
            break;
        case DECIMAL:
            result = new PropertyDecimalDefinitionImpl();
            break;
        case HTML:
            result = new PropertyHtmlDefinitionImpl();
            break;
        case ID:
            result = new PropertyIdDefinitionImpl();
            break;
        case INTEGER:
            result = new PropertyIntegerDefinitionImpl();
            break;
        case STRING:
            result = new PropertyStringDefinitionImpl();
            break;
        case URI:
            result = new PropertyUriDefinitionImpl();
            break;
        default:
            throw new RuntimeException("Unknown datatype! Spec change?");
        }

        result.setId(id);
        result.setLocalName(id);
        result.setDisplayName(displayName);
        result.setDescription(description);
        result.setPropertyType(datatype);
        result.setCardinality(cardinality);
        result.setUpdatability(updateability);
        result.setIsInherited(inherited);
        result.setIsRequired(required);
        result.setIsQueryable(queryable);
        result.setIsOrderable(orderable);
        result.setQueryName(id);

        return result;
    }
}
