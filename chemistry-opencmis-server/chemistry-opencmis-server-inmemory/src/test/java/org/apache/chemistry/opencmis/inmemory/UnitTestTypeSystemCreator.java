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

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * @author Jens
 * 
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChoiceImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriDefinitionImpl;
import org.apache.chemistry.opencmis.inmemory.types.InMemoryDocumentTypeDefinition;
import org.apache.chemistry.opencmis.inmemory.types.InMemoryFolderTypeDefinition;
import org.apache.chemistry.opencmis.inmemory.types.PropertyCreationHelper;

public class UnitTestTypeSystemCreator implements TypeCreator {
    public static final List<TypeDefinition> singletonTypes = buildTypesList();
    public static final String COMPLEX_TYPE = "ComplexType";
    public static final String TOPLEVEL_TYPE = "DocumentTopLevel";
    public static final String LEVEL1_TYPE = "DocumentLevel1";
    public static final String LEVEL2_TYPE = "DocumentLevel2";
    public static final String VERSIONED_TYPE = "MyVersionedType";
    public static final String VERSION_PROPERTY_ID = "StringProp";
    public static final String FOLDER_TYPE = "FolderType";
    
    public static final String PROP_ID_BOOLEAN = "BooleanProp";
    public static final String PROP_ID_DATETIME = "DateTimeProp";
    public static final String PROP_ID_DECIMAL = "DecimalProp";
    public static final String PROP_ID_HTML = "HtmlProp";
    public static final String PROP_ID_ID = "IdProp";
    public static final String PROP_ID_INT = "IntProp";
    public static final String PROP_ID_STRING = "StringProp";
    public static final String PROP_ID_URI = "UriProp";
    public static final String PROP_ID_BOOLEAN_MULTI_VALUE = "BooleanPropMV";
    public static final String PROP_ID_DATETIME_MULTI_VALUE = "DateTimePropMV";
    public static final String PROP_ID_DECIMAL_MULTI_VALUE = "DecimalPropMV";
    public static final String PROP_ID_HTML_MULTI_VALUE = "HtmlPropMV";
    public static final String PROP_ID_ID_MULTI_VALUE = "IdPropMV";
    public static final String PROP_ID_INT_MULTI_VALUE = "IntPropMV";
    public static final String PROP_ID_STRING_MULTI_VALUE = "StringPropMV";
    public static final String PROP_ID_URI_MULTI_VALUE = "UriPropMV";

    /**
     * in the public interface of this class we return the singleton containing
     * the required types for testing
     */
    public List<TypeDefinition> createTypesList() {
        return singletonTypes;
    }

    public static List<TypeDefinition> getTypesList() {
        return singletonTypes;
    }

    public static TypeDefinition getTypeById(String typeId) {
        for (TypeDefinition typeDef : singletonTypes)
            if (typeDef.getId().equals(typeId))
                return typeDef;
        return null;
    }

    /**
     * create root types and a collection of sample types
     * 
     * @return typesMap map filled with created types
     */
    private static List<TypeDefinition> buildTypesList() {
        // always add CMIS default types
        List<TypeDefinition> typesList = new LinkedList<TypeDefinition>();

        InMemoryDocumentTypeDefinition cmisType1 = new InMemoryDocumentTypeDefinition("MyDocType1",
                "My Type 1 Level 1", InMemoryDocumentTypeDefinition.getRootDocumentType());
        typesList.add(cmisType1);

        InMemoryDocumentTypeDefinition cmisType2 = new InMemoryDocumentTypeDefinition("MyDocType2",
                "My Type 2 Level 1", InMemoryDocumentTypeDefinition.getRootDocumentType());
        typesList.add(cmisType2);

        InMemoryDocumentTypeDefinition cmisType11 = new InMemoryDocumentTypeDefinition("MyDocType1.1",
                "My Type 3 Level 2", cmisType1);
        typesList.add(cmisType11);

        InMemoryDocumentTypeDefinition cmisType111 = new InMemoryDocumentTypeDefinition("MyDocType1.1.1",
                "My Type 4 Level 3", cmisType11);
        typesList.add(cmisType111);

        InMemoryDocumentTypeDefinition cmisType112 = new InMemoryDocumentTypeDefinition("MyDocType1.1.2",
                "My Type 5 Level 3", cmisType11);
        typesList.add(cmisType112);

        InMemoryDocumentTypeDefinition cmisType12 = new InMemoryDocumentTypeDefinition("MyDocType1.2",
                "My Type 6 Level 2", cmisType1);
        typesList.add(cmisType12);

        InMemoryDocumentTypeDefinition cmisType21 = new InMemoryDocumentTypeDefinition("MyDocType2.1",
                "My Type 7 Level 2", cmisType2);
        typesList.add(cmisType21);

        InMemoryDocumentTypeDefinition cmisType22 = new InMemoryDocumentTypeDefinition("MyDocType2.2",
                "My Type 8 Level 2", cmisType2);
        typesList.add(cmisType22);
        InMemoryDocumentTypeDefinition cmisType23 = new InMemoryDocumentTypeDefinition("MyDocType2.3",
                "My Type 9 Level 2", cmisType2);
        typesList.add(cmisType23);
        InMemoryDocumentTypeDefinition cmisType24 = new InMemoryDocumentTypeDefinition("MyDocType2.4",
                "My Type 10 Level 2", cmisType2);
        typesList.add(cmisType24);
        InMemoryDocumentTypeDefinition cmisType25 = new InMemoryDocumentTypeDefinition("MyDocType2.5",
                "My Type 11 Level 2", cmisType2);
        typesList.add(cmisType25);

        InMemoryDocumentTypeDefinition cmisType26 = new InMemoryDocumentTypeDefinition("MyDocType2.6",
                "My Type 12 Level 2", cmisType2);
        typesList.add(cmisType26);
        InMemoryDocumentTypeDefinition cmisType27 = new InMemoryDocumentTypeDefinition("MyDocType2.7",
                "My Type 13 Level 2", cmisType2);
        typesList.add(cmisType27);
        InMemoryDocumentTypeDefinition cmisType28 = new InMemoryDocumentTypeDefinition("MyDocType2.8",
                "My Type 14 Level 2", cmisType2);
        typesList.add(cmisType28);
        InMemoryDocumentTypeDefinition cmisType29 = new InMemoryDocumentTypeDefinition("MyDocType2.9",
                "My Type 15 Level 2", cmisType2);
        typesList.add(cmisType29);

        // create a complex type with properties
        InMemoryDocumentTypeDefinition cmisComplexType = new InMemoryDocumentTypeDefinition(COMPLEX_TYPE,
                "Complex type with properties, Level 1", InMemoryDocumentTypeDefinition.getRootDocumentType());

        // create a boolean property definition

        Map<String, PropertyDefinition<?>> propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();

        PropertyDefinition<Boolean> prop = PropertyCreationHelper.createBooleanDefinition(PROP_ID_BOOLEAN,
                "Sample Boolean Property", Updatability.READONLY);
        propertyDefinitions.put(prop.getId(), prop);

        prop = PropertyCreationHelper.createBooleanMultiDefinition(PROP_ID_BOOLEAN_MULTI_VALUE,
                "Sample Boolean multi-value Property", Updatability.READONLY);
        propertyDefinitions.put(prop.getId(), prop);

        PropertyDateTimeDefinitionImpl prop2 = PropertyCreationHelper.createDateTimeDefinition(PROP_ID_DATETIME,
                "Sample DateTime Property", Updatability.READONLY);
        propertyDefinitions.put(prop2.getId(), prop2);

        prop2 = PropertyCreationHelper.createDateTimeMultiDefinition(PROP_ID_DATETIME_MULTI_VALUE,
                "Sample DateTime multi-value Property", Updatability.READONLY);
        propertyDefinitions.put(prop2.getId(), prop2);

        PropertyDecimalDefinitionImpl prop3 = PropertyCreationHelper.createDecimalDefinition(PROP_ID_DECIMAL,
                "Sample Decimal Property", Updatability.READONLY);
        propertyDefinitions.put(prop3.getId(), prop3);

        prop3 = PropertyCreationHelper.createDecimalMultiDefinition(PROP_ID_DECIMAL_MULTI_VALUE, "Sample Decimal multi-value Property", Updatability.READONLY);
        propertyDefinitions.put(prop3.getId(), prop3);

        PropertyHtmlDefinitionImpl prop4 = PropertyCreationHelper.createHtmlDefinition(PROP_ID_HTML,
                "Sample Html Property", Updatability.READONLY);
        propertyDefinitions.put(prop4.getId(), prop4);

        prop4 = PropertyCreationHelper.createHtmlDefinition(PROP_ID_HTML_MULTI_VALUE, "Sample Html multi-value Property", Updatability.READONLY);
        propertyDefinitions.put(prop4.getId(), prop4);

        PropertyIdDefinitionImpl prop5 = PropertyCreationHelper.createIdDefinition(PROP_ID_ID, "Sample Id Property", Updatability.READONLY);
        propertyDefinitions.put(prop5.getId(), prop5);

        prop5 = PropertyCreationHelper.createIdMultiDefinition(PROP_ID_ID_MULTI_VALUE, "Sample Id Html multi-value Property", Updatability.READONLY);
        propertyDefinitions.put(prop5.getId(), prop5);

        PropertyIntegerDefinitionImpl prop6 = PropertyCreationHelper.createIntegerDefinition(PROP_ID_INT,
                "Sample Int Property", Updatability.READONLY);
        propertyDefinitions.put(prop6.getId(), prop6);

        prop6 = PropertyCreationHelper.createIntegerMultiDefinition(PROP_ID_INT_MULTI_VALUE, "Sample Int multi-value Property", Updatability.READONLY);
        propertyDefinitions.put(prop6.getId(), prop6);

        PropertyStringDefinitionImpl prop7 = PropertyCreationHelper.createStringDefinition(PROP_ID_STRING,
                "Sample String Property", Updatability.READONLY);
        propertyDefinitions.put(prop7.getId(), prop7);

        prop7 = PropertyCreationHelper.createStringMultiDefinition(PROP_ID_STRING_MULTI_VALUE,
        "Sample String multi-value Property", Updatability.READONLY);
        propertyDefinitions.put(prop7.getId(), prop7);

        PropertyUriDefinitionImpl prop8 = PropertyCreationHelper.createUriDefinition(PROP_ID_URI, "Sample Uri Property", Updatability.READONLY);
        propertyDefinitions.put(prop8.getId(), prop8);

        prop8 = PropertyCreationHelper.createUriMultiDefinition(PROP_ID_URI_MULTI_VALUE, "Sample Uri multi-value Property", Updatability.READONLY);
        propertyDefinitions.put(prop8.getId(), prop8);

        PropertyStringDefinitionImpl prop9 = PropertyCreationHelper.createStringDefinition("PickListProp",
                "Sample Pick List Property", Updatability.READONLY);
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
        propertyDefinitions.put(prop9.getId(), prop9);

        /*
         * try short form: / PropertyCreationHelper.addElemToPicklist(prop9,
         * "red"); PropertyCreationHelper.addElemToPicklist(prop9, "green");
         * PropertyCreationHelper.addElemToPicklist(prop9, "blue");
         * PropertyCreationHelper.addElemToPicklist(prop9, "black");
         * PropertyCreationHelper.setDefaultValue(prop9, "blue"); /
         */

        cmisComplexType.addCustomPropertyDefinitions(propertyDefinitions);

        // add type to types collection
        typesList.add(cmisComplexType);

        // create a type hierarchy with inherited properties
        InMemoryDocumentTypeDefinition cmisDocTypeTopLevel = new InMemoryDocumentTypeDefinition(TOPLEVEL_TYPE,
                "Document type with properties, Level 1", InMemoryDocumentTypeDefinition.getRootDocumentType());

        InMemoryDocumentTypeDefinition cmisDocTypeLevel1 = new InMemoryDocumentTypeDefinition(LEVEL1_TYPE,
                "Document type with inherited properties, Level 2", cmisDocTypeTopLevel);

        InMemoryDocumentTypeDefinition cmisDocTypeLevel2 = new InMemoryDocumentTypeDefinition(LEVEL2_TYPE,
                "Document type with inherited properties, Level 3", cmisDocTypeLevel1);

        propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();
        PropertyStringDefinitionImpl propTop = PropertyCreationHelper.createStringDefinition("StringPropTopLevel",
                "Sample String Property", Updatability.READONLY);
        propertyDefinitions.put(propTop.getId(), propTop);
        cmisDocTypeTopLevel.addCustomPropertyDefinitions(propertyDefinitions);

        propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();
        PropertyStringDefinitionImpl propLevel1 = PropertyCreationHelper.createStringDefinition("StringPropLevel1",
                "String Property Level 1", Updatability.READONLY);
        propertyDefinitions.put(propLevel1.getId(), propLevel1);
        cmisDocTypeLevel1.addCustomPropertyDefinitions(propertyDefinitions);

        propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();
        PropertyStringDefinitionImpl propLevel2 = PropertyCreationHelper.createStringDefinition("StringPropLevel2",
                "String Property Level 2", Updatability.READONLY);
        propertyDefinitions.put(propLevel2.getId(), propLevel2);
        cmisDocTypeLevel2.addCustomPropertyDefinitions(propertyDefinitions);

        // create a versioned type with properties
        InMemoryDocumentTypeDefinition cmisVersionedType = new InMemoryDocumentTypeDefinition(VERSIONED_TYPE,
                "VersionedType", InMemoryDocumentTypeDefinition.getRootDocumentType());

        // create a single String property definition

        propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();

        PropertyStringDefinitionImpl prop1 = PropertyCreationHelper.createStringDefinition(VERSION_PROPERTY_ID,
                "Sample String Property", Updatability.READONLY);
        propertyDefinitions.put(prop1.getId(), prop1);

        cmisVersionedType.addCustomPropertyDefinitions(propertyDefinitions);
        cmisVersionedType.setIsVersionable(true); // make it a versionable type;

        // create a folder type
        // create a complex type with properties
        InMemoryFolderTypeDefinition cmisFolderType = new InMemoryFolderTypeDefinition(FOLDER_TYPE,
                "Folder type with properties", InMemoryFolderTypeDefinition.getRootFolderType());

        // create a two property definitions for folder

        propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();

        prop6 = PropertyCreationHelper.createIntegerDefinition(PROP_ID_INT, "Sample Folder Int Property", Updatability.READONLY);
        propertyDefinitions.put(prop6.getId(), prop6);

        prop7 = PropertyCreationHelper.createStringDefinition(PROP_ID_STRING,
            "Sample Folder String Property", Updatability.READONLY);
        propertyDefinitions.put(prop7.getId(), prop7);
        cmisFolderType.addCustomPropertyDefinitions(propertyDefinitions);

        
        // add type to types collection
        typesList.add(cmisDocTypeTopLevel);
        typesList.add(cmisDocTypeLevel1);
        typesList.add(cmisDocTypeLevel2);
        typesList.add(cmisVersionedType);
        typesList.add(cmisFolderType);

        return typesList;
    }

}
