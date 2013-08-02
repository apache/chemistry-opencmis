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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.definitions.MutableDocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableItemTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutablePolicyTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableRelationshipTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableSecondaryTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChoiceImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriDefinitionImpl;
import org.apache.chemistry.opencmis.inmemory.TypeCreator;
import org.apache.chemistry.opencmis.server.support.TypeDefinitionFactory;

public class DefaultTypeSystemCreator implements TypeCreator {
    public static final List<TypeDefinition> singletonTypes = buildTypesList();
    public static final String COMPLEX_TYPE = "ComplexType";
    public static final String TOPLEVEL_TYPE = "DocumentTopLevel";
    public static final String VERSIONED_TYPE = "VersionableType";
    public static final String ITEM_TYPE = "MyItemType";
    public static final String LEVEL1_TYPE = "DocumentLevel1";
    public static final String LEVEL2_TYPE = "DocumentLevel2";
    public static final String SECONDARY_TYPE_ID = "MySecondaryType";

    /**
     * in the public interface of this class we return the singleton containing
     * the required types for testing
     */
    @Override
	public List<TypeDefinition> createTypesList() {
        return singletonTypes;
    }

    public static List<TypeDefinition> getTypesList() {
        return singletonTypes;
    }

    public static TypeDefinition getTypeById(String typeId) {
        for (TypeDefinition typeDef : singletonTypes) {
            if (typeDef.getId().equals(typeId)) {
                return typeDef;
            }
        }
        return null;
    }

    /**
     * Create root types and a collection of sample types.
     * 
     * @return typesMap a map filled with created types
     */
    private static List<TypeDefinition> buildTypesList() {
        // always add CMIS default types
        TypeDefinitionFactory typeFactory =  DocumentTypeCreationHelper.getTypeDefinitionFactory();

        List<TypeDefinition> typesList = new LinkedList<TypeDefinition>();

        MutableDocumentTypeDefinition cmisType1;        
        try {
            cmisType1 = typeFactory.createDocumentTypeDefinition(CmisVersion.CMIS_1_1, DocumentTypeCreationHelper.getCmisDocumentType().getId());
            cmisType1.setId("MyDocType1");
            cmisType1.setDisplayName("My Type 1 Level 1");
            cmisType1.setDescription("Builtin InMemory type definition MyDocType1");
            typesList.add(cmisType1);

            MutableDocumentTypeDefinition cmisType2;        
            cmisType2 = typeFactory.createDocumentTypeDefinition(CmisVersion.CMIS_1_1, DocumentTypeCreationHelper.getCmisDocumentType().getId());
            cmisType2.setId("MyDocType2");
            cmisType2.setDisplayName("My Type 2 Level 1");
            cmisType2.setDescription("Builtin InMemory type definition MyDocType2");
            typesList.add(cmisType2);

            MutableDocumentTypeDefinition cmisType11;        
            cmisType11 = typeFactory.createChildDocumentTypeDefinition(cmisType1, null);
            cmisType11.setId("MyDocType1.1");
            cmisType11.setDisplayName("My Type 3 Level 2");
            cmisType11.setDescription("Builtin InMemory type definition MyDocType1.1");
            typesList.add(cmisType11);

            MutableDocumentTypeDefinition cmisType111;        
            cmisType111 = typeFactory.createChildDocumentTypeDefinition(cmisType11, null);
            cmisType111.setId("MyDocType1.1.1");
            cmisType111.setDisplayName("My Type 4 Level 3");
            cmisType111.setDescription("Builtin InMemory type definition MyDocType1.1.1");
            typesList.add(cmisType111);

            MutableDocumentTypeDefinition cmisType112;        
            cmisType112 = typeFactory.createChildDocumentTypeDefinition(cmisType11, null);
            cmisType112.setId("MyDocType1.1.2");
            cmisType112.setDisplayName("My Type 5 Level 3");
            cmisType112.setDescription("Builtin InMemory type definition MyDocType1.1.2");
            typesList.add(cmisType112);

            MutableDocumentTypeDefinition cmisType12;        
            cmisType12 = typeFactory.createChildDocumentTypeDefinition(cmisType1, null);
            cmisType12.setId("MyDocType1.2");
            cmisType12.setDisplayName("My Type 6 Level 2");
            cmisType12.setDescription("Builtin InMemory type definition MyDocType1.2");
            typesList.add(cmisType12);

            MutableDocumentTypeDefinition cmisType21;        
            cmisType21 = typeFactory.createChildDocumentTypeDefinition(cmisType2, null);
            cmisType21.setId("MyDocType2.1");
            cmisType21.setDisplayName("My Type 7 Level 2");
            cmisType21.setDescription("Builtin InMemory type definition MyDocType2.1");
            typesList.add(cmisType21);

            MutableDocumentTypeDefinition cmisType22;        
            cmisType22 = typeFactory.createChildDocumentTypeDefinition(cmisType2, null);
            cmisType22.setId("MyDocType2.2");
            cmisType22.setDisplayName("My Type 8 Level 2");
            cmisType22.setDescription("Builtin InMemory type definition MyDocType2.2");
            typesList.add(cmisType22);

            MutableDocumentTypeDefinition cmisType23;        
            cmisType23 = typeFactory.createChildDocumentTypeDefinition(cmisType2, null);
            cmisType23.setId("MyDocType2.3");
            cmisType23.setDisplayName("My Type 9 Level 2");
            cmisType23.setDescription("Builtin InMemory type definition MyDocType2.3");
            typesList.add(cmisType23);

            MutableDocumentTypeDefinition cmisType24;        
            cmisType24 = typeFactory.createChildDocumentTypeDefinition(cmisType2, null);
            cmisType24.setId("MyDocType2.4");
            cmisType24.setDisplayName("My Type 10 Level 2");
            cmisType24.setDescription("Builtin InMemory type definition MyDocType2.4");
            typesList.add(cmisType24);

            MutableDocumentTypeDefinition cmisType25;        
            cmisType25 = typeFactory.createChildDocumentTypeDefinition(cmisType2, null);
            cmisType25.setId("MyDocType2.5");
            cmisType25.setDisplayName("My Type 11 Level 2");
            cmisType25.setDescription("Builtin InMemory type definition MyDocType2.5");
            typesList.add(cmisType25);

            MutableDocumentTypeDefinition cmisType26;        
            cmisType26 = typeFactory.createChildDocumentTypeDefinition(cmisType2, null);
            cmisType26.setId("MyDocType2.6");
            cmisType26.setDisplayName("My Type 12 Level 2");
            cmisType26.setDescription("Builtin InMemory type definition MyDocType2.6");
            typesList.add(cmisType26);

            MutableDocumentTypeDefinition cmisType27;        
            cmisType27 = typeFactory.createChildDocumentTypeDefinition(cmisType2, null);
            cmisType27.setId("MyDocType2.7");
            cmisType27.setDisplayName("My Type 13 Level 2");
            cmisType27.setDescription("Builtin InMemory type definition MyDocType2.7");
            typesList.add(cmisType27);

            MutableDocumentTypeDefinition cmisType28;        
            cmisType28 = typeFactory.createChildDocumentTypeDefinition(cmisType2, null);
            cmisType28.setId("MyDocType2.8");
            cmisType28.setDisplayName("My Type 14 Level 2");
            cmisType28.setDescription("Builtin InMemory type definition MyDocType2.8");
            typesList.add(cmisType28);

            MutableDocumentTypeDefinition cmisType29;        
            cmisType29 = typeFactory.createChildDocumentTypeDefinition(cmisType2, null);
            cmisType29.setId("MyDocType2.9");
            cmisType29.setDisplayName("My Type 15 Level 2");
            cmisType29.setDescription("Builtin InMemory type definition MyDocType2.9");
            typesList.add(cmisType29);

            // create a complex type with properties
            MutableDocumentTypeDefinition cmisComplexType;        
            cmisComplexType = typeFactory.createDocumentTypeDefinition(CmisVersion.CMIS_1_1, DocumentTypeCreationHelper.getCmisDocumentType().getId());
            cmisComplexType.setId(COMPLEX_TYPE);
            cmisComplexType.setDisplayName("Complex type with properties, Level 1");
            cmisComplexType.setDescription("Builtin InMemory type definition ComplexType");

            PropertyDefinition<Boolean> prop = PropertyCreationHelper.createBooleanDefinition("BooleanProp",
                    "Sample Boolean Property", Updatability.READWRITE);
            cmisComplexType.addPropertyDefinition(prop);

            prop = PropertyCreationHelper.createBooleanMultiDefinition("BooleanPropMV",
                    "Sample Boolean multi-value Property", Updatability.READWRITE);
            cmisComplexType.addPropertyDefinition(prop);

            PropertyDateTimeDefinitionImpl prop2 = PropertyCreationHelper.createDateTimeDefinition("DateTimeProp",
                    "Sample DateTime Property", Updatability.READWRITE);
            cmisComplexType.addPropertyDefinition(prop2);

            prop2 = PropertyCreationHelper.createDateTimeMultiDefinition("DateTimePropMV",
                    "Sample DateTime multi-value Property", Updatability.READWRITE);
            cmisComplexType.addPropertyDefinition(prop2);

            PropertyDecimalDefinitionImpl prop3 = PropertyCreationHelper.createDecimalDefinition("DecimalProp",
                    "Sample Decimal Property", Updatability.READWRITE);
            cmisComplexType.addPropertyDefinition(prop3);

            prop3 = PropertyCreationHelper.createDecimalMultiDefinition("DecimalPropMV",
                    "Sample Decimal multi-value Property", Updatability.READWRITE);
            cmisComplexType.addPropertyDefinition(prop3);

            PropertyHtmlDefinitionImpl prop4 = PropertyCreationHelper.createHtmlDefinition("HtmlProp",
                    "Sample Html Property", Updatability.READWRITE);
            cmisComplexType.addPropertyDefinition(prop4);

            prop4 = PropertyCreationHelper.createHtmlMultiDefinition("HtmlPropMV", "Sample Html multi-value Property",
                    Updatability.READWRITE);
            cmisComplexType.addPropertyDefinition(prop4);

            PropertyIdDefinitionImpl prop5 = PropertyCreationHelper.createIdDefinition("IdProp", "Sample Id Property",
                    Updatability.READWRITE);
            cmisComplexType.addPropertyDefinition(prop5);

            prop5 = PropertyCreationHelper.createIdMultiDefinition("IdPropMV", "Sample Id Html multi-value Property",
                    Updatability.READWRITE);
            cmisComplexType.addPropertyDefinition(prop5);

            PropertyIntegerDefinitionImpl prop6 = PropertyCreationHelper.createIntegerDefinition("IntProp",
                    "Sample Int Property", Updatability.READWRITE);
            cmisComplexType.addPropertyDefinition(prop6);

            prop6 = PropertyCreationHelper.createIntegerMultiDefinition("IntPropMV", "Sample Int multi-value Property",
                    Updatability.READWRITE);
            cmisComplexType.addPropertyDefinition(prop6);

            PropertyStringDefinitionImpl prop7 = PropertyCreationHelper.createStringDefinition("StringProp",
                    "Sample String Property", Updatability.READWRITE);
            cmisComplexType.addPropertyDefinition(prop7);

            PropertyUriDefinitionImpl prop8 = PropertyCreationHelper.createUriDefinition("UriProp", "Sample Uri Property",
                    Updatability.READWRITE);
            cmisComplexType.addPropertyDefinition(prop8);

            prop8 = PropertyCreationHelper.createUriMultiDefinition("UriPropMV", "Sample Uri multi-value Property",
                    Updatability.READWRITE);
            cmisComplexType.addPropertyDefinition(prop8);

            PropertyStringDefinitionImpl prop9 = PropertyCreationHelper.createStringDefinition("PickListProp",
                    "Sample Pick List Property", Updatability.READWRITE);
            List<Choice<String>> choiceList = new ArrayList<Choice<String>>();
            ChoiceImpl<String> elem = new ChoiceImpl<String>();
            elem.setValue(Collections.singletonList("red"));
            choiceList.add(elem);
            elem = new ChoiceImpl<String>();
            elem.setValue(Collections.singletonList("green"));
            choiceList.add(elem);
            elem = new ChoiceImpl<String>();
            elem.setValue(Collections.singletonList("blue"));
            choiceList.add(elem);
            elem = new ChoiceImpl<String>();
            elem.setValue(Collections.singletonList("black"));
            choiceList.add(elem);
            prop9.setChoices(choiceList);
            prop9.setDefaultValue(Collections.singletonList("blue"));
            cmisComplexType.addPropertyDefinition(prop9);

            /*
             * try short form: / PropertyCreationHelper.addElemToPicklist(prop9,
             * "red"); PropertyCreationHelper.addElemToPicklist(prop9, "green");
             * PropertyCreationHelper.addElemToPicklist(prop9, "blue");
             * PropertyCreationHelper.addElemToPicklist(prop9, "black");
             * PropertyCreationHelper.setDefaultValue(prop9, "blue"); /
             */

            // add type to types collection
            typesList.add(cmisComplexType);

            // create a type hierarchy with inherited properties
            MutableDocumentTypeDefinition cmisDocTypeTopLevel;        
            cmisDocTypeTopLevel = typeFactory.createDocumentTypeDefinition(CmisVersion.CMIS_1_1, DocumentTypeCreationHelper.getCmisDocumentType().getId());
            cmisDocTypeTopLevel.setId(TOPLEVEL_TYPE);
            cmisDocTypeTopLevel.setDisplayName("Document type with properties, Level 1");
            cmisDocTypeTopLevel.setDescription("Builtin InMemory type definition " + TOPLEVEL_TYPE);

            MutableDocumentTypeDefinition cmisDocTypeLevel1;        
            cmisDocTypeLevel1 = typeFactory.createChildDocumentTypeDefinition(cmisDocTypeTopLevel, null);
            cmisDocTypeLevel1.setId(LEVEL1_TYPE);
            cmisDocTypeLevel1.setDisplayName("Document type with inherited properties, Level 2");
            cmisDocTypeLevel1 .setDescription("Builtin InMemory type definition " + LEVEL1_TYPE);

            MutableDocumentTypeDefinition cmisDocTypeLevel2;        
            cmisDocTypeLevel2 = typeFactory.createChildDocumentTypeDefinition(cmisDocTypeLevel1, null);
            cmisDocTypeLevel2.setId(LEVEL2_TYPE);
            cmisDocTypeLevel2.setDisplayName("Document type with inherited properties, Level 3");
            cmisDocTypeLevel2.setDescription("Builtin InMemory type definition " + LEVEL2_TYPE);

            PropertyStringDefinitionImpl propTop = PropertyCreationHelper.createStringDefinition("StringPropTopLevel",
                    "Sample String Property", Updatability.READWRITE);
            cmisDocTypeTopLevel.addPropertyDefinition(propTop);

            PropertyStringDefinitionImpl propLevel1 = PropertyCreationHelper.createStringDefinition("StringPropLevel1",
                    "String Property Level 1", Updatability.READWRITE);
            cmisDocTypeLevel1.addPropertyDefinition(propLevel1);

            PropertyStringDefinitionImpl propLevel2 = PropertyCreationHelper.createStringDefinition("StringPropLevel2",
                    "String Property Level 2", Updatability.READWRITE);
            cmisDocTypeLevel2.addPropertyDefinition(propLevel2);

            // add type to types collection
            typesList.add(cmisDocTypeTopLevel);
            typesList.add(cmisDocTypeLevel1);
            typesList.add(cmisDocTypeLevel2);

            // Create a type that is versionable
            MutableDocumentTypeDefinition cmisVersionedType;        
            cmisVersionedType = typeFactory.createDocumentTypeDefinition(CmisVersion.CMIS_1_1, DocumentTypeCreationHelper.getCmisDocumentType().getId());
            cmisVersionedType.setId(VERSIONED_TYPE);
            cmisVersionedType.setDisplayName("Versioned Type");
            cmisVersionedType.setDescription("Builtin InMemory type definition " + VERSIONED_TYPE);
            cmisVersionedType.setIsVersionable(true); // make it a versionable type;

            // create a single String property definition
            PropertyStringDefinitionImpl prop1 = PropertyCreationHelper.createStringDefinition("VersionedStringProp",
                    "Sample String Property", Updatability.WHENCHECKEDOUT);
            cmisVersionedType.addPropertyDefinition(prop1);

            // add type to types collection
            typesList.add(cmisVersionedType);

            // CMIS 1.1 create an item item type

            MutableItemTypeDefinition itemType;        
            itemType = typeFactory.createItemTypeDefinition(CmisVersion.CMIS_1_1, DocumentTypeCreationHelper.getCmisItemType().getId()); // ??? DocumentTypeCreationHelper.getCmisItemType());
            itemType.setId(ITEM_TYPE);
            itemType.setDisplayName("MyItemType");
            itemType.setDescription("Builtin InMemory type definition " + ITEM_TYPE);
            DocumentTypeCreationHelper.setDefaultTypeCapabilities(itemType);

            // create a single String property definition

            prop1 = PropertyCreationHelper.createStringDefinition("ItemStringProp",
                    "Item String Property", Updatability.READWRITE);
            itemType.addPropertyDefinition(prop1);
            // add type to types collection
            typesList.add(itemType);


            MutableSecondaryTypeDefinition cmisSecondaryType;        
            cmisSecondaryType = typeFactory.createSecondaryTypeDefinition(CmisVersion.CMIS_1_1, DocumentTypeCreationHelper.getCmisSecondaryType().getId());
            cmisSecondaryType.setId(SECONDARY_TYPE_ID);
            cmisSecondaryType.setDisplayName("MySecondaryType");
            cmisSecondaryType.setDescription("Builtin InMemory type definition " + SECONDARY_TYPE_ID);
            DocumentTypeCreationHelper.setDefaultTypeCapabilities(cmisSecondaryType);
            cmisSecondaryType.setIsFileable(false);
            
            // create a single String property definition
            PropertyStringDefinitionImpl propS1 = PropertyCreationHelper.createStringDefinition("SecondaryStringProp",
                    "Secondary String Property", Updatability.READWRITE);
            cmisSecondaryType.addPropertyDefinition(propS1);
            PropertyIntegerDefinitionImpl propS2 = PropertyCreationHelper.createIntegerDefinition("SecondaryIntegerProp",
                    "Secondary Integer Property", Updatability.READWRITE);
            propS2.setIsRequired(true);
            cmisSecondaryType.addPropertyDefinition(propS2);
            // add type to types collection
            typesList.add(cmisSecondaryType);

            // add relationship type
            MutableRelationshipTypeDefinition relType;        
            relType = typeFactory.createRelationshipTypeDefinition(CmisVersion.CMIS_1_1, DocumentTypeCreationHelper.getCmisRelationshipType().getId());
            relType.setId("CrossReferenceType");
            relType.setDisplayName("CrossReferenceType");
            relType.setDescription("Builtin InMemory type definition CrossReferenceType");
            DocumentTypeCreationHelper.setDefaultTypeCapabilities(relType);
            relType.setIsFileable(false);

            // create a single String property definition

            prop1 = PropertyCreationHelper.createStringDefinition("CrossReferenceKind",
                    "CrossReferenceType", Updatability.READWRITE);
            relType.addPropertyDefinition(prop1);
            typesList.add(relType);            

            // add a policy type
            MutablePolicyTypeDefinition polType;        
            polType = typeFactory.createPolicyTypeDefinition(CmisVersion.CMIS_1_1, DocumentTypeCreationHelper.getCmisPolicyType().getId());
            polType.setId("AuditPolicy");
            polType.setDisplayName("Audit Policy");
            polType.setDescription("Builtin InMemory type definition AuditPolicy");
            DocumentTypeCreationHelper.setDefaultTypeCapabilities(polType);
            polType.setIsFileable(false);

            // create a String property definition
            prop1 = PropertyCreationHelper.createStringDefinition("AuditSettings",
                    "Audit Kind Property", Updatability.READWRITE);
            polType.addPropertyDefinition(prop1);
            typesList.add(polType);            

            return typesList;
        } catch (Exception e) {
            throw new CmisRuntimeException("Error when creating built-in InMemory types.", e);
        }
    }

}
