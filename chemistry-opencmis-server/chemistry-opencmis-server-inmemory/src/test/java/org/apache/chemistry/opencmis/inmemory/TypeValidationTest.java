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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableDocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChoiceImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriDefinitionImpl;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.inmemory.types.DocumentTypeCreationHelper;
import org.apache.chemistry.opencmis.inmemory.types.PropertyCreationHelper;
import org.apache.chemistry.opencmis.server.support.TypeDefinitionFactory;
import org.apache.chemistry.opencmis.server.support.TypeManager;
import org.apache.chemistry.opencmis.server.support.TypeValidator;
import org.junit.Test;

public class TypeValidationTest extends TestCase {

    private static final String MY_DOC_TYPE = "MyDocType1";
    private static final String STRING_DOC_TYPE = "StringDocType";
    private static final String STRING_PROP_TYPE = "StringProp";
    private static final String INT_DOC_TYPE = "IntegerDocType";
    private static final String INT_PROP_TYPE = "IntegerProp";
    private static final String DECIMAL_DOC_TYPE = "DecimalDocType";
    private static final String DECIMAL_PROP_TYPE = "DecimalProp";
    private static final String PICK_LIST_DOC_TYPE = "PickListDocType";
    private static final String PICK_LIST_PROP_DEF = "PickListProp";
    private static final String DOC_TYPE_SUPER = "SuperDocType";
    private static final String DOC_TYPE_SUB = "SubDocType";
    private static final String STRING_PROP_TYPE_SUPER = "StringPropSuper";
    private static final String STRING_PROP_TYPE_SUB = "StringPropSub";
    private static final BindingsObjectFactory FACTORY = new BindingsObjectFactoryImpl();
    static TypeDefinitionFactory typeFactory = DocumentTypeCreationHelper.getTypeDefinitionFactory();

    private static List<PropertyData<?>> createPropertiesWithNameAndTypeId(String typeId) {
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(FACTORY.createPropertyIdData(PropertyIds.NAME, "Document_1"));
        properties.add(FACTORY.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, typeId));
        return properties;
    }

    @Test
    public void testMandatoryPropertyValidation() {
        // create properties in the same way as we would pass them to a
        // createDocument call
        // of the ObjectService

        List<PropertyData<?>> properties = createPropertiesWithNameAndTypeId(MY_DOC_TYPE);
        Properties props = FACTORY.createPropertiesData(properties);

        // validate properties according to type
        TypeDefinition typeDef = buildMyType();

        // try missing mandatory Boolean property
        try {
            TypeValidator.validateProperties(typeDef, props, true);
            fail("TypeValidator should throw CMISConstraintException if mandatory property is missing.");
        } catch (CmisConstraintException e) {
            assertTrue(e.getMessage().contains("mandatory properties are missing"));
        }

        // add missing mandatory Boolean property and try again
        properties.add(FACTORY.createPropertyBooleanData("BooleanProp", true));
        props = FACTORY.createPropertiesData(properties);
        try {
            TypeValidator.validateProperties(typeDef, props, true);
        } catch (CmisConstraintException e) {
            fail("TypeValidator should not throw CMISConstraintException if mandatory property is present.");
        }
    }

    @Test
    public void testStringPropertyValidation() {
        TypeDefinition typeDef = buildTypeWithStringProp(); // we only have one

        List<PropertyData<?>> properties = createPropertiesWithNameAndTypeId(STRING_DOC_TYPE);
        properties.add(FACTORY.createPropertyStringData(STRING_PROP_TYPE,
                "A String property with quite a long value exceeding the max. length."));
        Properties props = FACTORY.createPropertiesData(properties);

        // try exceeding string length
        try {
            TypeValidator.validateProperties(typeDef, props, true);
            fail("TypeValidator should throw CMISConstraintException if max string length is exceeded");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
        }

        properties = createPropertiesWithNameAndTypeId(STRING_DOC_TYPE);
        properties.add(FACTORY.createPropertyStringData(STRING_PROP_TYPE, "short val"));
        props = FACTORY.createPropertiesData(properties);
        try {
            TypeValidator.validateProperties(typeDef, props, true);
        } catch (Exception e) {
            fail("TypeValidator should not throw exception if string length is valid" + e);
        }
    }

    @Test
    public void testIntegerPropertyValidation() {

        TypeDefinition typeDef = buildTypeWithIntegerProp();

        List<PropertyData<?>> properties = createPropertiesWithNameAndTypeId(INT_DOC_TYPE);
        properties.add(FACTORY.createPropertyIntegerData(INT_PROP_TYPE, BigInteger.valueOf(-100))); // try
        // wrong
        // value
        Properties props = FACTORY.createPropertiesData(properties);

        // try exceeding string length
        try {
            TypeValidator.validateProperties(typeDef, props, true);
            fail("TypeValidator should throw CMISConstraintException if integer value is out of range");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
        }

        properties = createPropertiesWithNameAndTypeId(INT_DOC_TYPE);
        properties.add(FACTORY.createPropertyIntegerData(INT_PROP_TYPE, BigInteger.valueOf(1))); // try
        // correct
        // value
        props = FACTORY.createPropertiesData(properties);
        try {
            TypeValidator.validateProperties(typeDef, props, true);
        } catch (Exception e) {
            fail("TypeValidator should not throw exception if integer value is valid. " + e);
        }

    }

    @Test
    public void testDecimalPropertyValidation() {
        TypeDefinition typeDef = buildTypeWithDecimalProp();

        List<PropertyData<?>> properties = createPropertiesWithNameAndTypeId(DECIMAL_DOC_TYPE);
        properties.add(FACTORY.createPropertyDecimalData(DECIMAL_PROP_TYPE, BigDecimal.valueOf(-11.11)));
        Properties props = FACTORY.createPropertiesData(properties);

        // try exceeding string length
        try {
            TypeValidator.validateProperties(typeDef, props, true);
            fail("TypeValidator should throw CMISConstraintException if decimal value is out of range");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
        }

        properties = createPropertiesWithNameAndTypeId(DECIMAL_DOC_TYPE);
        properties.add(FACTORY.createPropertyDecimalData(DECIMAL_PROP_TYPE, BigDecimal.valueOf(1.23)));
        props = FACTORY.createPropertiesData(properties);
        try {
            TypeValidator.validateProperties(typeDef, props, true);
        } catch (Exception e) {
            fail("TypeValidator should not throw exception if decimal value is valid. " + e);
        }
    }

    @Test
    public void testPickListValidationSingleValue() {
        TypeDefinition typeDef = buildTypeWithPickList(Cardinality.SINGLE);

        List<PropertyData<?>> properties = createPropertiesWithNameAndTypeId(PICK_LIST_DOC_TYPE);
        properties.add(FACTORY.createPropertyStringData(PICK_LIST_PROP_DEF, "pink"));
        Properties props = FACTORY.createPropertiesData(properties);

        // try wrong value
        try {
            TypeValidator.validateProperties(typeDef, props, true);
            fail("TypeValidator should throw CMISConstraintException if choice value is not in list of valid values");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
        }

        properties = createPropertiesWithNameAndTypeId(PICK_LIST_DOC_TYPE);
        properties.add(FACTORY.createPropertyStringData(PICK_LIST_PROP_DEF, "blue"));
        props = FACTORY.createPropertiesData(properties);

        try {
            TypeValidator.validateProperties(typeDef, props, true);
        } catch (Exception e) {
            fail("TypeValidator should not throw CMISConstraintException if choice value is in list of valid values"
                    + e);
        }
    }

    @Test
    public void testPickListValidationMultiValue() {
        TypeDefinition typeDef = buildTypeWithPickList(Cardinality.MULTI);

        List<PropertyData<?>> properties = createPropertiesWithNameAndTypeId(PICK_LIST_DOC_TYPE);
        List<String> propValues = new ArrayList<String>();
        propValues.add("red");
        propValues.add("pink");
        properties.add(FACTORY.createPropertyStringData(PICK_LIST_PROP_DEF, propValues));
        Properties props = FACTORY.createPropertiesData(properties);

        // try wrong value
        try {
            TypeValidator.validateProperties(typeDef, props, true);
            fail("TypeValidator should throw CMISConstraintException if choice value is not in list of valid values");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
        }

        properties = createPropertiesWithNameAndTypeId(PICK_LIST_DOC_TYPE);
        propValues = new ArrayList<String>();
        propValues.add("red");
        propValues.add("green");
        properties.add(FACTORY.createPropertyStringData(PICK_LIST_PROP_DEF, propValues));
        props = FACTORY.createPropertiesData(properties);
        try {
            TypeValidator.validateProperties(typeDef, props, true);
        } catch (Exception e) {
            fail("TypeValidator should not throw CMISConstraintException if choice value is in list of valid values"
                    + e);
        }
    }

    @Test
    public void testPickListValidationMultiUsingMultipleValueLists() {
        TypeDefinition typeDef = buildTypeWithMultiPickList();

        List<PropertyData<?>> properties = createPropertiesWithNameAndTypeId(PICK_LIST_DOC_TYPE);
        List<String> propValues = new ArrayList<String>();
        propValues.add("red");
        propValues.add("green");
        propValues.add("pink");
        properties.add(FACTORY.createPropertyStringData(PICK_LIST_PROP_DEF, propValues));
        Properties props = FACTORY.createPropertiesData(properties);

        // try wrong value
        try {
            TypeValidator.validateProperties(typeDef, props, true);
            fail("TypeValidator should throw CMISConstraintException if choice value is not in list of valid values");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
        }

        properties = createPropertiesWithNameAndTypeId(PICK_LIST_DOC_TYPE);
        propValues = new ArrayList<String>();
        propValues.add("red");
        propValues.add("green");
        propValues.add("blue");
        properties.add(FACTORY.createPropertyStringData(PICK_LIST_PROP_DEF, propValues));
        props = FACTORY.createPropertiesData(properties);

        try {
            TypeValidator.validateProperties(typeDef, props, true);
        } catch (Exception e) {
            fail("TypeValidator should not throw CMISConstraintException if choice value is in list of valid values"
                    + e);
        }
    }

    @Test
    public void testHierachicalPickListValidationSingleValue() {
        TypeDefinition typeDef = buildTypeWithHierachicalPickList(Cardinality.SINGLE);

        List<PropertyData<?>> properties = createPropertiesWithNameAndTypeId(PICK_LIST_DOC_TYPE);
        properties.add(FACTORY.createPropertyStringData(PICK_LIST_PROP_DEF, "frankfurt"));
        Properties props = FACTORY.createPropertiesData(properties);

        // try wrong value
        try {
            TypeValidator.validateProperties(typeDef, props, true);
            fail("TypeValidator should throw CMISConstraintException if choice value is not in list of valid values");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
        }

        properties = createPropertiesWithNameAndTypeId(PICK_LIST_DOC_TYPE);
        properties.add(FACTORY.createPropertyStringData(PICK_LIST_PROP_DEF, "munich"));
        props = FACTORY.createPropertiesData(properties);

        try {
            TypeValidator.validateProperties(typeDef, props, true);
        } catch (Exception e) {
            fail("TypeValidator should not throw CMISConstraintException if choice value is in list of valid values"
                    + e);
        }
    }

    @Test
    public void testHierachicalPickListValidationMultiValue() {
        TypeDefinition typeDef = buildTypeWithHierachicalPickList(Cardinality.MULTI);

        List<PropertyData<?>> properties = createPropertiesWithNameAndTypeId(PICK_LIST_DOC_TYPE);
        List<String> propValues = new ArrayList<String>();
        propValues.add("stuttgart");
        propValues.add("hintertupfingen");
        properties.add(FACTORY.createPropertyStringData(PICK_LIST_PROP_DEF, propValues));
        Properties props = FACTORY.createPropertiesData(properties);

        // try wrong value
        try {
            TypeValidator.validateProperties(typeDef, props, true);
            fail("TypeValidator should throw CMISConstraintException if choice value is not in list of valid values");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
        }

        properties = createPropertiesWithNameAndTypeId(PICK_LIST_DOC_TYPE);
        propValues = new ArrayList<String>();
        propValues.add("munich");
        propValues.add("walldorf");
        properties.add(FACTORY.createPropertyStringData(PICK_LIST_PROP_DEF, propValues));
        props = FACTORY.createPropertiesData(properties);

        try {
            TypeValidator.validateProperties(typeDef, props, true);
        } catch (Exception e) {
            fail("TypeValidator should not throw CMISConstraintException if choice value is in list of valid values"
                    + e);
        }
    }

    @Test
    public void testInheritedPropertyValidation() {
        TypeManager tm = buildInheritedTypes();
        TypeDefinition superType = tm.getTypeById(DOC_TYPE_SUPER).getTypeDefinition();
        TypeDefinition subType = tm.getTypeById(DOC_TYPE_SUB).getTypeDefinition();

        List<PropertyData<?>> properties = createPropertiesWithNameAndTypeId(DOC_TYPE_SUB);
        properties.add(FACTORY.createPropertyStringData(STRING_PROP_TYPE_SUB,
                "A String property with quite a long value exceeding the max. length."));
        Properties props = FACTORY.createPropertiesData(properties);

        // try exceeding string length on org property
        try {
            TypeValidator.validateProperties(superType, props, true);
            fail("TypeValidator should throw CMISConstraintException if max string length is exceeded");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
        }

        // try exceeding string length on inherited property
        properties = createPropertiesWithNameAndTypeId(DOC_TYPE_SUB);
        properties.add(FACTORY.createPropertyStringData(STRING_PROP_TYPE_SUPER,
                "A String property with quite a long value exceeding the max. length."));
        props = FACTORY.createPropertiesData(properties);

        // try exceeding string length
        try {
            TypeValidator.validateProperties(subType, props, true);
            fail("TypeValidator should throw CMISConstraintException if max string length is exceeded");
        } catch (Exception e) {
            assertTrue(e instanceof CmisConstraintException);
        }

        properties = createPropertiesWithNameAndTypeId(DOC_TYPE_SUB);
        properties.add(FACTORY.createPropertyStringData(STRING_PROP_TYPE_SUPER, "super val"));
        properties.add(FACTORY.createPropertyStringData(STRING_PROP_TYPE_SUB, "sub val"));
        props = FACTORY.createPropertiesData(properties);
        try {
            TypeValidator.validateProperties(subType, props, true);
        } catch (Exception e) {
            fail("TypeValidator should not throw exception if string length is valid" + e);
        }
    }

    @Test
    public void testAllPropertiesKnown() {

        String unknownPropertyId = "UnknownProperty";
        List<PropertyData<?>> properties = createPropertiesWithNameAndTypeId(MY_DOC_TYPE);
        properties.add(FACTORY.createPropertyBooleanData("BooleanProp", true));
        // add unknown property
        properties.add(FACTORY.createPropertyStringData(unknownPropertyId, "SomeValue"));
        Properties props = FACTORY.createPropertiesData(properties);

        // validate properties according to type
        TypeDefinition typeDef = buildMyType();

        try {
            TypeValidator.validateProperties(typeDef, props, true);
            fail("TypeValidator should throw CMISConstraintException if property is not known in type.");
        } catch (CmisConstraintException e) {
            assertTrue(e.getMessage().contains("Unknown property"));
            assertTrue(e.getMessage().contains(unknownPropertyId));
        }
    }

    @Test
    public void testAllPropertiesKnownSecondaryTypes() {

        TypeDefinition primaryType = buildTypeWithStringProp();
        TypeDefinition secondaryType1 = buildTypeWithIntegerProp();
        TypeDefinition secondaryType2 = buildTypeWithDecimalProp();

        String unknownPropertyId = "UnknownProperty";
        List<PropertyData<?>> properties = createPropertiesWithSecondaryTypes(MY_DOC_TYPE,
                Arrays.asList(INT_DOC_TYPE, DECIMAL_DOC_TYPE));

        // add unknown property
        properties.add(FACTORY.createPropertyStringData(unknownPropertyId, "SomeValue"));
        Properties props = FACTORY.createPropertiesData(properties);

        // validate properties according to type
        try {
            TypeValidator.validateProperties(Arrays.asList(primaryType, secondaryType1, secondaryType2), props, true);
            fail("TypeValidator should throw CMISConstraintException if property is not known in type.");
        } catch (CmisConstraintException e) {
            assertTrue(e.getMessage().contains("properties are not known in any of the types"));
            assertTrue(e.getMessage().contains(unknownPropertyId));
        }
    }

    private static List<PropertyData<?>> createPropertiesWithSecondaryTypes(String typeIdPrimary,
            List<String> secondaryTypeIds) {
        List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
        properties.add(FACTORY.createPropertyIdData(PropertyIds.NAME, "Document_1"));
        properties.add(FACTORY.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, typeIdPrimary));
        properties.add(FACTORY.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, secondaryTypeIds));
        properties.add(FACTORY.createPropertyBooleanData("BooleanProp", true));
        properties.add(FACTORY.createPropertyIntegerData(INT_PROP_TYPE, BigInteger.valueOf(0)));
        properties.add(FACTORY.createPropertyDecimalData(DECIMAL_PROP_TYPE, BigDecimal.valueOf(0.5)));
        return properties;
    }

    /**
     * create sample type
     * 
     * @return type definition of sample type
     */
    private static DocumentTypeDefinition buildMyType() {
        // always add CMIS default types

        try {
            MutableDocumentTypeDefinition cmisType;
            cmisType = (MutableDocumentTypeDefinition) typeFactory.createChildTypeDefinition(
                    DocumentTypeCreationHelper.getCmisDocumentType(), MY_DOC_TYPE);
            cmisType.setDisplayName("Document Type for Validation");
            cmisType.setDescription("InMemory test type definition " + MY_DOC_TYPE);

            // create a boolean property definition

            PropertyDefinition<Boolean> prop = PropertyCreationHelper.createBooleanDefinition("BooleanProp",
                    "Sample Boolean Property", Updatability.READONLY);
            ((PropertyBooleanDefinitionImpl) prop).setIsRequired(true);
            cmisType.addPropertyDefinition(prop);

            prop = PropertyCreationHelper.createBooleanMultiDefinition("BooleanPropMV",
                    "Sample Boolean multi-value Property", Updatability.READONLY);
            cmisType.addPropertyDefinition(prop);

            PropertyDateTimeDefinitionImpl prop2 = PropertyCreationHelper.createDateTimeDefinition("DateTimeProp",
                    "Sample DateTime Property", Updatability.READONLY);
            cmisType.addPropertyDefinition(prop2);

            prop2 = PropertyCreationHelper.createDateTimeMultiDefinition("DateTimePropMV",
                    "Sample DateTime multi-value Property", Updatability.READONLY);
            cmisType.addPropertyDefinition(prop2);

            PropertyDecimalDefinitionImpl prop3 = PropertyCreationHelper.createDecimalDefinition("DecimalProp",
                    "Sample Decimal Property", Updatability.READONLY);
            cmisType.addPropertyDefinition(prop3);

            prop3 = PropertyCreationHelper.createDecimalDefinition("DecimalPropMV",
                    "Sample Decimal multi-value Property", Updatability.READONLY);
            cmisType.addPropertyDefinition(prop3);

            PropertyHtmlDefinitionImpl prop4 = PropertyCreationHelper.createHtmlDefinition("HtmlProp",
                    "Sample Html Property", Updatability.READONLY);
            cmisType.addPropertyDefinition(prop4);

            prop4 = PropertyCreationHelper.createHtmlDefinition("HtmlPropMV", "Sample Html multi-value Property",
                    Updatability.READONLY);
            cmisType.addPropertyDefinition(prop4);

            PropertyIdDefinitionImpl prop5 = PropertyCreationHelper.createIdDefinition("IdProp", "Sample Id Property",
                    Updatability.READONLY);
            cmisType.addPropertyDefinition(prop5);

            prop5 = PropertyCreationHelper.createIdDefinition("IdPropMV", "Sample Id Html multi-value Property",
                    Updatability.READONLY);
            cmisType.addPropertyDefinition(prop5);

            PropertyIntegerDefinitionImpl prop6 = PropertyCreationHelper.createIntegerDefinition("IntProp",
                    "Sample Int Property", Updatability.READONLY);
            cmisType.addPropertyDefinition(prop6);

            prop6 = PropertyCreationHelper.createIntegerDefinition("IntPropMV", "Sample Int multi-value Property",
                    Updatability.READONLY);
            cmisType.addPropertyDefinition(prop6);

            PropertyStringDefinitionImpl prop7 = PropertyCreationHelper.createStringDefinition("StringProp",
                    "Sample String Property", Updatability.READONLY);
            cmisType.addPropertyDefinition(prop7);

            PropertyUriDefinitionImpl prop8 = PropertyCreationHelper.createUriDefinition("UriProp",
                    "Sample Uri Property", Updatability.READONLY);
            cmisType.addPropertyDefinition(prop8);

            prop8 = PropertyCreationHelper.createUriDefinition("UriPropMV", "Sample Uri multi-value Property",
                    Updatability.READONLY);
            cmisType.addPropertyDefinition(prop8);

            PropertyStringDefinitionImpl prop9 = PropertyCreationHelper.createStringDefinition(PICK_LIST_PROP_DEF,
                    "Sample Pick List Property", Updatability.READONLY);

            PropertyCreationHelper.addElemToPicklist(prop9, "red");
            PropertyCreationHelper.addElemToPicklist(prop9, "green");
            PropertyCreationHelper.addElemToPicklist(prop9, "blue");
            PropertyCreationHelper.addElemToPicklist(prop9, "black");
            PropertyCreationHelper.setDefaultValue(prop9, "blue");
            cmisType.addPropertyDefinition(prop9);

            return cmisType;
        } catch (Exception e) {
            throw new CmisRuntimeException("Error when creating built-in InMemory types.", e);
        }
    }

    private static DocumentTypeDefinition buildTypeWithStringProp() {
        try {
            MutableDocumentTypeDefinition cmisType;
            cmisType = (MutableDocumentTypeDefinition) typeFactory.createChildTypeDefinition(
                    DocumentTypeCreationHelper.getCmisDocumentType(), STRING_DOC_TYPE);
            cmisType.setDisplayName("String Document Type for Validation");
            cmisType.setDescription("InMemory test type definition " + STRING_DOC_TYPE);

            // create a String property definition
            PropertyStringDefinitionImpl propDef = PropertyCreationHelper.createStringDefinition(STRING_PROP_TYPE,
                    "Sample String Property", Updatability.READONLY);
            propDef.setMaxLength(BigInteger.valueOf(10));
            cmisType.addPropertyDefinition(propDef);

            return cmisType;
        } catch (Exception e) {
            throw new CmisRuntimeException("Error when creating built-in InMemory types.", e);
        }
    }

    private static DocumentTypeDefinition buildTypeWithIntegerProp() {
        try {
            MutableDocumentTypeDefinition cmisType;
            cmisType = (MutableDocumentTypeDefinition) typeFactory.createChildTypeDefinition(
                    DocumentTypeCreationHelper.getCmisDocumentType(), INT_DOC_TYPE);
            cmisType.setDisplayName("Int Document Type for Validation");
            cmisType.setDescription("InMemory test type definition " + INT_DOC_TYPE);

            // create a String property definition

            PropertyIntegerDefinitionImpl propDef = PropertyCreationHelper.createIntegerDefinition(INT_PROP_TYPE,
                    "Sample Integer Property", Updatability.READONLY);
            propDef.setMinValue(BigInteger.valueOf(-1));
            propDef.setMaxValue(BigInteger.valueOf(1));
            cmisType.addPropertyDefinition(propDef);

            return cmisType;
        } catch (Exception e) {
            throw new CmisRuntimeException("Error when creating built-in InMemory types.", e);
        }
    }

    private static DocumentTypeDefinition buildTypeWithDecimalProp() {
        try {
            MutableDocumentTypeDefinition cmisType;
            cmisType = (MutableDocumentTypeDefinition) typeFactory.createChildTypeDefinition(
                    DocumentTypeCreationHelper.getCmisDocumentType(), DECIMAL_DOC_TYPE);
            cmisType.setDisplayName("Decimal Type for Validation");
            cmisType.setDescription("InMemory test type definition " + DECIMAL_DOC_TYPE);

            // create a String property definition

            PropertyDecimalDefinitionImpl propDef = PropertyCreationHelper.createDecimalDefinition(DECIMAL_PROP_TYPE,
                    "Sample Decimal Property", Updatability.READONLY);
            propDef.setMinValue(BigDecimal.valueOf(-1.5));
            propDef.setMaxValue(BigDecimal.valueOf(1.5));
            cmisType.addPropertyDefinition(propDef);

            return cmisType;
        } catch (Exception e) {
            throw new CmisRuntimeException("Error when creating built-in InMemory types.", e);
        }
    }

    public static DocumentTypeDefinition buildTypeWithPickList(Cardinality cardinality) {
        try {
            MutableDocumentTypeDefinition cmisType;
            cmisType = (MutableDocumentTypeDefinition) typeFactory.createChildTypeDefinition(
                    DocumentTypeCreationHelper.getCmisDocumentType(), PICK_LIST_DOC_TYPE);
            cmisType.setDisplayName("PickList Type for Validation");
            cmisType.setDescription("InMemory test type definition " + PICK_LIST_DOC_TYPE);

            // create a String property definition
            PropertyStringDefinitionImpl propDef = PropertyCreationHelper.createStringDefinition(PICK_LIST_PROP_DEF,
                    "Sample PickList (choice) Property", Updatability.READONLY);
            List<Choice<String>> choiceList = new ArrayList<Choice<String>>();
            ChoiceImpl<String> elem = new ChoiceImpl<String>();
            elem.setValue(Collections.singletonList("red"));
            elem.setDisplayName("Red");
            choiceList.add(elem);
            elem = new ChoiceImpl<String>();
            elem.setValue(Collections.singletonList("green"));
            elem.setDisplayName("Green");
            choiceList.add(elem);
            elem = new ChoiceImpl<String>();
            elem.setValue(Collections.singletonList("blue"));
            elem.setDisplayName("Blue");
            choiceList.add(elem);
            elem = new ChoiceImpl<String>();
            elem.setValue(Collections.singletonList("black"));
            elem.setDisplayName("Black");
            choiceList.add(elem);
            propDef.setChoices(choiceList);
            propDef.setDefaultValue(Collections.singletonList("blue"));
            propDef.setCardinality(cardinality);
            cmisType.addPropertyDefinition(propDef);

            return cmisType;
        } catch (Exception e) {
            throw new CmisRuntimeException("Error when creating built-in InMemory types.", e);
        }
    }

    public static DocumentTypeDefinition buildTypeWithMultiPickList() {
        try {
            MutableDocumentTypeDefinition cmisType;
            cmisType = (MutableDocumentTypeDefinition) typeFactory.createChildTypeDefinition(
                    DocumentTypeCreationHelper.getCmisDocumentType(), PICK_LIST_DOC_TYPE);
            cmisType.setDisplayName("MDocument Type for Validation");
            cmisType.setDescription("PickList test type definition " + PICK_LIST_DOC_TYPE);

            // create a String property definition
            PropertyStringDefinitionImpl propDef = PropertyCreationHelper.createStringDefinition(PICK_LIST_PROP_DEF,
                    "Sample PickList (choice) Property", Updatability.READONLY);
            List<Choice<String>> choiceList = new ArrayList<Choice<String>>();
            ChoiceImpl<String> elem = new ChoiceImpl<String>();
            List<String> valueList = new ArrayList<String>();
            valueList.add("red");
            valueList.add("green");
            valueList.add("blue");
            elem.setValue(valueList);
            elem.setDisplayName("RGB");
            choiceList.add(elem);

            elem = new ChoiceImpl<String>();
            valueList = new ArrayList<String>();
            valueList.add("cyan");
            valueList.add("magenta");
            valueList.add("yellow");
            valueList.add("black");
            elem.setValue(valueList);
            elem.setDisplayName("CMYK");
            choiceList.add(elem);

            propDef.setChoices(choiceList);
            // propDef.setDefaultValue(...);
            propDef.setCardinality(Cardinality.MULTI);
            cmisType.addPropertyDefinition(propDef);

            return cmisType;
        } catch (Exception e) {
            throw new CmisRuntimeException("Error when creating built-in InMemory types.", e);
        }
    }

    public static DocumentTypeDefinition buildTypeWithHierachicalPickList(Cardinality cardinality) {
        try {
            MutableDocumentTypeDefinition cmisType;
            cmisType = (MutableDocumentTypeDefinition) typeFactory.createChildTypeDefinition(
                    DocumentTypeCreationHelper.getCmisDocumentType(), PICK_LIST_DOC_TYPE);
            cmisType.setDisplayName("PickList Type for Validation");
            cmisType.setDescription("InMemory test type definition " + PICK_LIST_DOC_TYPE);

            // create a String property definition

            // Create a two-level pick list with an outer property list state
            // and an
            // inner
            // list of city
            PropertyStringDefinitionImpl propDef = PropertyCreationHelper.createStringDefinition(PICK_LIST_PROP_DEF,
                    "Sample PickList (choice) Property", Updatability.READONLY);
            List<Choice<String>> choiceListOuter = new ArrayList<Choice<String>>();

            ChoiceImpl<String> elemOuter = new ChoiceImpl<String>();
            elemOuter.setDisplayName("Bavaria");
            List<Choice<String>> choiceListInner = new ArrayList<Choice<String>>();
            ChoiceImpl<String> elemInner = new ChoiceImpl<String>();
            elemInner.setDisplayName("Munich");
            elemInner.setValue(Collections.singletonList("munich"));
            choiceListInner.add(elemInner);
            elemInner = new ChoiceImpl<String>();
            elemInner.setDisplayName("Ingolstadt");
            elemInner.setValue(Collections.singletonList("ingolstadt"));
            choiceListInner.add(elemInner);
            elemInner = new ChoiceImpl<String>();
            elemInner.setDisplayName("Passau");
            elemInner.setValue(Collections.singletonList("passau"));
            choiceListInner.add(elemInner);
            elemOuter.setChoice(choiceListInner);
            choiceListOuter.add(elemOuter);

            elemOuter = new ChoiceImpl<String>();
            elemOuter.setDisplayName("Baden Wurtemberg");
            choiceListInner = new ArrayList<Choice<String>>();
            elemInner = new ChoiceImpl<String>();
            elemInner.setDisplayName("Stuttgart");
            elemInner.setValue(Collections.singletonList("stuttgart"));
            choiceListInner.add(elemInner);
            elemInner = new ChoiceImpl<String>();
            elemInner.setDisplayName("Karlsruhe");
            elemInner.setValue(Collections.singletonList("karlsruhe"));
            choiceListInner.add(elemInner);
            elemInner = new ChoiceImpl<String>();
            elemInner.setDisplayName("Walldorf");
            elemInner.setValue(Collections.singletonList("walldorf"));
            choiceListInner.add(elemInner);
            elemOuter.setChoice(choiceListInner);
            choiceListOuter.add(elemOuter);

            propDef.setChoices(choiceListOuter);
            propDef.setCardinality(cardinality);
            cmisType.addPropertyDefinition(propDef);

            return cmisType;
        } catch (Exception e) {
            throw new CmisRuntimeException("Error when creating built-in InMemory types.", e);
        }
    }

    private static TypeManager buildInheritedTypes() {
        try {

            TypeManagerImpl tm = new TypeManagerImpl();
            tm.initTypeSystem(null, true); // create CMIS default types

            // create super type
            MutableDocumentTypeDefinition cmisSuperType;
            cmisSuperType = (MutableDocumentTypeDefinition) typeFactory.createChildTypeDefinition(
                    DocumentTypeCreationHelper.getCmisDocumentType(), DOC_TYPE_SUPER);
            cmisSuperType.setDisplayName("Document Type With a child");
            cmisSuperType.setDescription("InMemory test type definition " + DOC_TYPE_SUPER);

            // create a String property definition
            PropertyStringDefinitionImpl propDef = PropertyCreationHelper.createStringDefinition(
                    STRING_PROP_TYPE_SUPER, "Sample String Property SuperType", Updatability.READONLY);
            propDef.setMaxLength(BigInteger.valueOf(10));
            cmisSuperType.addPropertyDefinition(propDef);

            tm.addTypeDefinition(cmisSuperType, false);

            // create sub type
            MutableDocumentTypeDefinition cmisSubType;
            cmisSubType = (MutableDocumentTypeDefinition) typeFactory.createChildTypeDefinition(cmisSuperType,
                    DOC_TYPE_SUB);
            cmisSubType.setDisplayName("Document Type With a parent");
            cmisSubType.setDescription("InMemory test type definition " + DOC_TYPE_SUB);

            // create a String property definition
            propDef = PropertyCreationHelper.createStringDefinition(STRING_PROP_TYPE_SUB,
                    "Sample String Property Subtype", Updatability.READONLY);
            propDef.setMaxLength(BigInteger.valueOf(20));
            cmisSubType.addPropertyDefinition(propDef);

            tm.addTypeDefinition(cmisSubType, false);

            return tm;
        } catch (Exception e) {
            throw new CmisRuntimeException("Error when creating built-in InMemory types.", e);
        }
    }

}
