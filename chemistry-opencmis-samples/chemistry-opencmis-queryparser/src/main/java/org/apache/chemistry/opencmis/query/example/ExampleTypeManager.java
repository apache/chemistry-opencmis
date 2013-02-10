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
package org.apache.chemistry.opencmis.query.example;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeMutabilityImpl;
import org.apache.chemistry.opencmis.server.support.TypeManager;

public class ExampleTypeManager implements TypeManager {
    
    private static ExampleTypeManager INSTANCE = new ExampleTypeManager();
    
    DocumentTypeDefinition cmisDocumentType;
    TypeDefinitionContainerImpl cmisDocumentTypeContainer;
    
    public static TypeManager getInstance() {
        return INSTANCE;
    }
    
    private ExampleTypeManager() {
        cmisDocumentType = createDocumentTypeDefinition();
        cmisDocumentTypeContainer = new TypeDefinitionContainerImpl(cmisDocumentType);

    }
    
    public TypeDefinitionContainer getTypeById(String typeId) {
        if (BaseTypeId.CMIS_DOCUMENT.value().equals(typeId))
            return cmisDocumentTypeContainer;
        else
            return null;
    }

    public TypeDefinition getTypeByQueryName(String typeQueryName) {
        if (BaseTypeId.CMIS_DOCUMENT.value().equals(typeQueryName))
            return cmisDocumentTypeContainer.getTypeDefinition();
        else
            return null;
    }

    public Collection<TypeDefinitionContainer> getTypeDefinitionList() {
        TypeDefinitionContainer tdc = cmisDocumentTypeContainer;
        return Collections.singletonList(tdc);        
    }

    public List<TypeDefinitionContainer> getRootTypes() {
        TypeDefinitionContainer tdc = cmisDocumentTypeContainer;
        return Collections.singletonList(tdc);        
    }

    public String getPropertyIdForQueryName(TypeDefinition typeDefinition, String propQueryName) {
        for (PropertyDefinition<?> pd : typeDefinition.getPropertyDefinitions().values()) {
            if (pd.getQueryName().equals(propQueryName)) {
                return pd.getId();
            }
        }
        return null;
    }

    private static DocumentTypeDefinition createDocumentTypeDefinition() {
        
        DocumentTypeDefinitionImpl typeDef = new DocumentTypeDefinitionImpl();
        typeDef.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
        typeDef.setId(BaseTypeId.CMIS_DOCUMENT.value());
        typeDef.setDisplayName("CMIS Document");
        // create some suitable defaults for convenience
        typeDef.setDescription("Description of CMIS Document Type");
        typeDef.setLocalName(typeDef.getDisplayName());
        typeDef.setLocalNamespace("local");
        typeDef.setQueryName(typeDef.getId());
        typeDef.setIsControllableAcl(true);
        typeDef.setIsControllablePolicy(false);
        typeDef.setIsCreatable(true);
        typeDef.setIsFileable(true);
        typeDef.setIsFulltextIndexed(false);
        typeDef.setIsIncludedInSupertypeQuery(true);
        typeDef.setIsQueryable(true);

        TypeMutabilityImpl typeMutability = new TypeMutabilityImpl();
        typeMutability.setCanCreate(true);
        typeMutability.setCanDelete(false);
        typeMutability.setCanUpdate(false);
        typeDef.setTypeMutability (typeMutability);


        // document specifics:
        typeDef.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);
        typeDef.setIsVersionable(false);
        
        Map<String, PropertyDefinition<?>> props = new HashMap<String, PropertyDefinition<?>>();
        setBasicDocumentPropertyDefinitions(props);
        typeDef.setPropertyDefinitions(props); // set initial empty set of properties

        return typeDef;
    }


    private static void setBasicDocumentPropertyDefinitions(Map<String, PropertyDefinition<?>> propertyDefinitions) {
        
        setBasicPropertyDefinitions(propertyDefinitions);
        PropertyBooleanDefinitionImpl propB = createBooleanDefinition(PropertyIds.IS_IMMUTABLE,
                "Immutable", Updatability.READONLY);
        propertyDefinitions.put(propB.getId(), propB);

        propB = createBooleanDefinition(PropertyIds.IS_LATEST_VERSION,
                "Is Latest Version", Updatability.READONLY);
        propertyDefinitions.put(propB.getId(), propB);

        propB = createBooleanDefinition(PropertyIds.IS_MAJOR_VERSION,
                "Is Major Version", Updatability.READONLY);
        propertyDefinitions.put(propB.getId(), propB);

        propB = createBooleanDefinition(PropertyIds.IS_LATEST_MAJOR_VERSION,
                "Is Latest Major Version", Updatability.READONLY);
        propertyDefinitions.put(propB.getId(), propB);

        PropertyStringDefinitionImpl propS = createStringDefinition(PropertyIds.VERSION_LABEL,
                "Version Label", Updatability.READONLY);
        propertyDefinitions.put(propS.getId(), propS);

        PropertyIdDefinitionImpl propId = createIdDefinition(PropertyIds.VERSION_SERIES_ID,
                "Version Series Id", Updatability.READONLY);
        propId.setIsQueryable(false);
        propertyDefinitions.put(propId.getId(), propId);

        propB = createBooleanDefinition(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT,
                "Checked Out", Updatability.READONLY);
        propertyDefinitions.put(propB.getId(), propB);

        propS = createStringDefinition(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY,
                "Checked Out By", Updatability.READONLY);
        propertyDefinitions.put(propS.getId(), propS);

        propId = createIdDefinition(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID,
                "Checked Out Id", Updatability.READONLY);
        propertyDefinitions.put(propId.getId(), propId);

        propS = createStringDefinition(PropertyIds.CHECKIN_COMMENT,
                "Checkin Comment", Updatability.READONLY);
        // read-only, because
        // not set as property
        propertyDefinitions.put(propS.getId(), propS);

        PropertyIntegerDefinitionImpl propI = createIntegerDefinition(
                PropertyIds.CONTENT_STREAM_LENGTH, "Content Length", Updatability.READONLY);
        propertyDefinitions.put(propI.getId(), propI);

        propS = createStringDefinition(PropertyIds.CONTENT_STREAM_MIME_TYPE,
                "Mime Type", Updatability.READONLY);
        propertyDefinitions.put(propS.getId(), propS);

        propS = createStringDefinition(PropertyIds.CONTENT_STREAM_FILE_NAME,
                "File Name", Updatability.READONLY);
        propertyDefinitions.put(propS.getId(), propS);

        propId = createIdDefinition(PropertyIds.CONTENT_STREAM_ID, "Stream Id", Updatability.READONLY);
        propertyDefinitions.put(propId.getId(), propId);

        // CMIS 1.1:
        propB = createBooleanDefinition(PropertyIds.IS_PRIVATE_WORKING_COPY, "Private Working Copy", 
                Updatability.READONLY);
        propertyDefinitions.put(propB.getId(), propB);

        propertyDefinitions.put(propS.getId(), propS);
    }
    
    static void setBasicPropertyDefinitions(Map<String, PropertyDefinition<?>> propertyDefinitions) {

        PropertyStringDefinitionImpl propS = createStringDefinition(PropertyIds.NAME,
                "Name", Updatability.READWRITE);
        propS.setIsRequired(true);
        propertyDefinitions.put(propS.getId(), propS);

        PropertyIdDefinitionImpl propId = createIdDefinition(PropertyIds.OBJECT_ID,
                "Object Id", Updatability.READONLY);
        propertyDefinitions.put(propId.getId(), propId);

        propId = createIdDefinition(PropertyIds.OBJECT_TYPE_ID, "Type-Id", Updatability.ONCREATE);
        propId.setIsRequired(true);
        propertyDefinitions.put(propId.getId(), propId);

        propId = createIdDefinition(PropertyIds.BASE_TYPE_ID, "Base-Type-Id", Updatability.READONLY);
        propertyDefinitions.put(propId.getId(), propId);

        propS = createStringDefinition(PropertyIds.CREATED_BY, "Created By", Updatability.READONLY);
        propertyDefinitions.put(propS.getId(), propS);

        PropertyDateTimeDefinitionImpl propD = createDateTimeDefinition(
                PropertyIds.CREATION_DATE, "Creation Date", Updatability.READONLY);
        propertyDefinitions.put(propD.getId(), propD);

        propS = createStringDefinition(PropertyIds.LAST_MODIFIED_BY,
                "Modified By", Updatability.READONLY);
        propertyDefinitions.put(propS.getId(), propS);

        propD = createDateTimeDefinition(PropertyIds.LAST_MODIFICATION_DATE,
                "Modification Date", Updatability.READONLY);
        propertyDefinitions.put(propD.getId(), propD);

        propS = createStringDefinition(PropertyIds.CHANGE_TOKEN, "Change Token", Updatability.READONLY);
        propertyDefinitions.put(propS.getId(), propS);

        // CMIS 1.1:
        propS = createStringDefinition(PropertyIds.DESCRIPTION, "Description", Updatability.READWRITE);
        propertyDefinitions.put(propS.getId(), propS);

        propId = createIdMultiDefinition(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "Secondary Type Ids",
                Updatability.READWRITE);
        propertyDefinitions.put(propId.getId(), propId);
    }

    private static void createStandardDefinition(AbstractPropertyDefinition<?> prop, String id, PropertyType propType,
            String displayName, Cardinality card, Updatability upd) {

            prop.setId(id);
        if (displayName == null) {
            prop.setDisplayName("Sample " + prop.getId() + " boolean property");
        } else {
            prop.setDisplayName(displayName);
        }
        prop.setDescription("This is a " + prop.getDisplayName() + " property.");
        prop.setLocalName(id);
        prop.setLocalNamespace("local");
        prop.setQueryName(id);
        prop.setIsInherited(false);
        prop.setCardinality(card);
        prop.setIsOpenChoice(false);
        prop.setIsQueryable(true);
        prop.setIsRequired(false);
        prop.setIsOrderable(true);
        prop.setPropertyType(propType);
        prop.setUpdatability(upd);
    }

    private static PropertyBooleanDefinitionImpl createBooleanDefinition(String id, String displayName, Updatability upd) {
        PropertyBooleanDefinitionImpl prop = new PropertyBooleanDefinitionImpl();
        createStandardDefinition(prop, id, PropertyType.BOOLEAN, displayName, Cardinality.SINGLE, upd);
        return prop;
    }

    private static PropertyDateTimeDefinitionImpl createDateTimeDefinition(String id, String displayName,
            Updatability upd) {
        PropertyDateTimeDefinitionImpl prop = new PropertyDateTimeDefinitionImpl();
        createStandardDefinition(prop, id, PropertyType.DATETIME, displayName, Cardinality.SINGLE, upd);
        return prop;
    }

    private static PropertyIdDefinitionImpl createIdDefinition(String id, String displayName, Updatability upd) {
        PropertyIdDefinitionImpl prop = new PropertyIdDefinitionImpl();
        createStandardDefinition(prop, id, PropertyType.ID, displayName, Cardinality.SINGLE, upd);
        return prop;
    }

    public static PropertyIdDefinitionImpl createIdMultiDefinition(String id, String displayName, Updatability upd) {
        PropertyIdDefinitionImpl prop = new PropertyIdDefinitionImpl();
        createStandardDefinition(prop, id, PropertyType.ID, displayName, Cardinality.MULTI, upd);
        return prop;
    }

    public static PropertyIntegerDefinitionImpl createIntegerDefinition(String id, String displayName, Updatability upd) {
        PropertyIntegerDefinitionImpl prop = new PropertyIntegerDefinitionImpl();
        createStandardDefinition(prop, id, PropertyType.INTEGER, displayName, Cardinality.SINGLE, upd);
        return prop;
    }

    private static PropertyStringDefinitionImpl createStringDefinition(String id, String displayName, Updatability upd) {
        PropertyStringDefinitionImpl prop = new PropertyStringDefinitionImpl();
        createStandardDefinition(prop, id, PropertyType.STRING, displayName, Cardinality.SINGLE, upd);
        return prop;
    }

}
