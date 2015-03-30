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
package org.apache.chemistry.opencmis.commons.impl.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.DateTimeFormat;
import org.apache.chemistry.opencmis.commons.enums.DateTimeResolution;
import org.apache.chemistry.opencmis.commons.enums.DecimalPrecision;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.WSConverter;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChoiceImpl;
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
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeMutabilityImpl;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeContainer;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeDefinitionListType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeDefinitionType;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;
import org.junit.Test;

public class TypeDefinitionConverterTest extends AbstractXMLConverterTest {

    private static Set<String> cmis10ignoreMethods = new HashSet<String>();
    static {
        cmis10ignoreMethods.add("getTypeMutability");
    }

    @Test
    public void testDocumentTypeDefinition() throws Exception {
        // run the test a few times with different values
        for (int i = 0; i < 5; i++) {
            DocumentTypeDefinitionImpl typeDef = new DocumentTypeDefinitionImpl();
            fillTypeDefintion(typeDef, BaseTypeId.CMIS_DOCUMENT);
            typeDef.setIsVersionable(randomBoolean());
            typeDef.setContentStreamAllowed(randomEnum(ContentStreamAllowed.class));

            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.BOOLEAN));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DATETIME));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DECIMAL));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.HTML));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.ID));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.INTEGER));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.STRING));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.URI));

            assertTypeDefinition10(typeDef, true);
            assertTypeDefinition11(typeDef, true);
        }
    }

    @Test
    public void testFolderTypeDefinition() throws Exception {
        // run the test a few times with different values
        for (int i = 0; i < 5; i++) {
            FolderTypeDefinitionImpl typeDef = new FolderTypeDefinitionImpl();
            fillTypeDefintion(typeDef, BaseTypeId.CMIS_FOLDER);

            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.BOOLEAN));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DATETIME));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DECIMAL));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.HTML));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.ID));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.INTEGER));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.STRING));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.URI));

            assertTypeDefinition10(typeDef, true);
            assertTypeDefinition11(typeDef, true);
        }
    }

    @Test
    public void testRelationshipTypeDefinition() throws Exception {
        // run the test a few times with different values
        for (int i = 0; i < 5; i++) {
            RelationshipTypeDefinitionImpl typeDef = new RelationshipTypeDefinitionImpl();
            fillTypeDefintion(typeDef, BaseTypeId.CMIS_RELATIONSHIP);
            typeDef.setAllowedSourceTypes(Arrays.asList(randomString(), randomString(), randomString()));
            typeDef.setAllowedTargetTypes(Arrays.asList(randomString(), randomString(), randomString(), randomString()));

            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.BOOLEAN));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DATETIME));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DECIMAL));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.HTML));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.ID));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.INTEGER));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.STRING));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.URI));

            assertTypeDefinition10(typeDef, true);
            assertTypeDefinition11(typeDef, true);
        }
    }

    @Test
    public void testPolicyTypeDefinition() throws Exception {
        // run the test a few times with different values
        for (int i = 0; i < 5; i++) {
            PolicyTypeDefinitionImpl typeDef = new PolicyTypeDefinitionImpl();
            fillTypeDefintion(typeDef, BaseTypeId.CMIS_POLICY);

            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.BOOLEAN));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DATETIME));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DECIMAL));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.HTML));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.ID));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.INTEGER));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.STRING));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.URI));

            assertTypeDefinition10(typeDef, true);
            assertTypeDefinition11(typeDef, true);
        }
    }

    @Test
    public void testItemTypeDefinition() throws Exception {
        // run the test a few times with different values
        for (int i = 0; i < 5; i++) {
            ItemTypeDefinitionImpl typeDef = new ItemTypeDefinitionImpl();
            fillTypeDefintion(typeDef, BaseTypeId.CMIS_ITEM);

            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.BOOLEAN));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DATETIME));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DECIMAL));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.HTML));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.ID));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.INTEGER));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.STRING));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.URI));

            assertTypeDefinition11(typeDef, true);
        }
    }

    @Test
    public void testSecondaryTypeDefinition() throws Exception {
        // run the test a few times with different values
        for (int i = 0; i < 5; i++) {
            SecondaryTypeDefinitionImpl typeDef = new SecondaryTypeDefinitionImpl();
            fillTypeDefintion(typeDef, BaseTypeId.CMIS_SECONDARY);

            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.BOOLEAN));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DATETIME));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DECIMAL));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.HTML));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.ID));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.INTEGER));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.STRING));
            typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.URI));

            assertTypeDefinition11(typeDef, true);
        }
    }

    @Test
    public void testTypeDefinitionList() throws Exception {
        // run the test a few times with different values
        for (int i = 0; i < 5; i++) {
            List<TypeDefinition> list = new ArrayList<TypeDefinition>();

            for (int j = 0; j < randomInt(5); j++) {
                DocumentTypeDefinitionImpl typeDef = new DocumentTypeDefinitionImpl();
                fillTypeDefintion(typeDef, BaseTypeId.CMIS_DOCUMENT);
                typeDef.setIsVersionable(randomBoolean());
                typeDef.setContentStreamAllowed(randomEnum(ContentStreamAllowed.class));

                typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.BOOLEAN));
                typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DATETIME));
                typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DECIMAL));
                typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.HTML));
                typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.ID));
                typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.INTEGER));
                typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.STRING));
                typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.URI));

                list.add(typeDef);
            }

            // type definition list
            TypeDefinitionListImpl typeDefList = new TypeDefinitionListImpl();
            typeDefList.setList(list);
            if (randomBoolean()) {
                typeDefList.setNumItems(BigInteger.valueOf(list.size()));
                typeDefList.setHasMoreItems(false);
            } else {
                typeDefList.setNumItems(BigInteger.valueOf(list.size() + 1 + randomInt(99)));
                typeDefList.setHasMoreItems(true);
            }

            assertTypeDefinitionList(typeDefList);
        }
    }

    @Test
    public void testTypeDefinitionContainer() throws Exception {
        // run the test a few times with different values
        for (int i = 0; i < 5; i++) {
            TypeDefinitionContainer typeDefContainter = createTypeDefinitionContainer(randomInt(7) + 2);
            assertTypeDefinitionContainer(typeDefContainter);
        }
    }

    private TypeDefinitionContainer createTypeDefinitionContainer(int level) {
        DocumentTypeDefinitionImpl typeDef = new DocumentTypeDefinitionImpl();
        fillTypeDefintion(typeDef, BaseTypeId.CMIS_DOCUMENT);
        typeDef.setIsVersionable(randomBoolean());
        typeDef.setContentStreamAllowed(randomEnum(ContentStreamAllowed.class));

        typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.BOOLEAN));
        typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DATETIME));
        typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.DECIMAL));
        typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.HTML));
        typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.ID));
        typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.INTEGER));
        typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.STRING));
        typeDef.addPropertyDefinition(createPropertyDefintion(PropertyType.URI));

        TypeDefinitionContainerImpl result = new TypeDefinitionContainerImpl();
        result.setTypeDefinition(typeDef);

        if (level > 0) {
            List<TypeDefinitionContainer> children = new ArrayList<TypeDefinitionContainer>();
            for (int i = 0; i < randomInt(3); i++) {
                children.add(createTypeDefinitionContainer(level - 1));
            }

            result.setChildren(children);
        }

        return result;
    }

    protected void fillTypeDefintion(AbstractTypeDefinition typeDef, BaseTypeId baseTypeId) {
        typeDef.setBaseTypeId(baseTypeId);
        typeDef.setDescription(randomString());
        typeDef.setDisplayName(randomString());
        typeDef.setId(randomString());
        typeDef.setIsControllableAcl(randomBoolean());
        typeDef.setIsControllablePolicy(randomBoolean());
        typeDef.setIsCreatable(randomBoolean());
        typeDef.setIsFileable(randomBoolean());
        typeDef.setIsIncludedInSupertypeQuery(randomBoolean());
        typeDef.setIsFulltextIndexed(randomBoolean());
        typeDef.setIsQueryable(randomBoolean());
        typeDef.setLocalName(randomString());
        typeDef.setLocalNamespace(randomUri());
        typeDef.setParentTypeId(randomString());
        typeDef.setQueryName(randomString());

        TypeMutabilityImpl tm = new TypeMutabilityImpl();
        tm.setCanCreate(randomBoolean());
        tm.setCanDelete(randomBoolean());
        tm.setCanUpdate(randomBoolean());
        typeDef.setTypeMutability(tm);
    }

    protected AbstractPropertyDefinition<?> createPropertyDefintion(PropertyType propertyType) {
        AbstractPropertyDefinition<?> result = null;
        Cardinality cardinality = randomEnum(Cardinality.class);

        switch (propertyType) {
        case BOOLEAN:
            result = new PropertyBooleanDefinitionImpl();
            ((PropertyBooleanDefinitionImpl) result).setDefaultValue(Arrays.asList(randomBoolean()));
            ((PropertyBooleanDefinitionImpl) result).setChoices(createChoiceList(Boolean.class, cardinality));
            break;
        case DATETIME:
            result = new PropertyDateTimeDefinitionImpl();
            ((PropertyDateTimeDefinitionImpl) result).setDefaultValue(Arrays.asList(randomDateTime()));
            ((PropertyDateTimeDefinitionImpl) result)
                    .setChoices(createChoiceList(GregorianCalendar.class, cardinality));
            ((PropertyDateTimeDefinitionImpl) result).setDateTimeResolution(randomEnum(DateTimeResolution.class));
            break;
        case DECIMAL:
            result = new PropertyDecimalDefinitionImpl();
            ((PropertyDecimalDefinitionImpl) result).setDefaultValue(Arrays.asList(randomDecimal()));
            ((PropertyDecimalDefinitionImpl) result).setChoices(createChoiceList(BigDecimal.class, cardinality));
            ((PropertyDecimalDefinitionImpl) result).setMaxValue(randomDecimal());
            ((PropertyDecimalDefinitionImpl) result).setMinValue(randomDecimal());
            ((PropertyDecimalDefinitionImpl) result).setPrecision(randomEnum(DecimalPrecision.class));
            break;
        case HTML:
            result = new PropertyHtmlDefinitionImpl();
            ((PropertyHtmlDefinitionImpl) result).setDefaultValue(Arrays.asList(randomString()));
            ((PropertyHtmlDefinitionImpl) result).setChoices(createChoiceList(String.class, cardinality));
            break;
        case ID:
            result = new PropertyIdDefinitionImpl();
            ((PropertyIdDefinitionImpl) result).setDefaultValue(Arrays.asList(randomString()));
            ((PropertyIdDefinitionImpl) result).setChoices(createChoiceList(String.class, cardinality));
            break;
        case INTEGER:
            result = new PropertyIntegerDefinitionImpl();
            ((PropertyIntegerDefinitionImpl) result).setDefaultValue(Arrays.asList(randomInteger()));
            ((PropertyIntegerDefinitionImpl) result).setChoices(createChoiceList(BigInteger.class, cardinality));
            ((PropertyIntegerDefinitionImpl) result).setMaxValue(randomInteger());
            ((PropertyIntegerDefinitionImpl) result).setMinValue(randomInteger());
            break;
        case STRING:
            result = new PropertyStringDefinitionImpl();
            ((PropertyStringDefinitionImpl) result).setDefaultValue(Arrays.asList(randomString()));
            ((PropertyStringDefinitionImpl) result).setChoices(createChoiceList(String.class, cardinality));
            ((PropertyStringDefinitionImpl) result).setMaxLength(randomInteger());
            break;
        case URI:
            result = new PropertyUriDefinitionImpl();
            ((PropertyUriDefinitionImpl) result).setDefaultValue(Arrays.asList(randomUri()));
            ((PropertyUriDefinitionImpl) result).setChoices(createChoiceList(String.class, cardinality));
            break;
        default:
            return null;
        }

        result.setId(randomString());
        result.setDescription(randomString());
        result.setDisplayName(randomString());
        result.setPropertyType(propertyType);
        result.setLocalName(randomString());
        result.setLocalNamespace(randomUri());
        result.setCardinality(cardinality);
        result.setUpdatability(randomEnum(Updatability.class));
        result.setIsQueryable(randomBoolean());
        result.setQueryName(randomString());
        result.setIsInherited(randomBoolean());
        result.setIsRequired(randomBoolean());
        result.setIsOrderable(randomBoolean());

        return result;
    }

    @SuppressWarnings("unchecked")
    protected <T> List<Choice<T>> createChoiceList(Class<T> clazz, Cardinality cardinality) {
        List<Choice<T>> result = new ArrayList<Choice<T>>();

        for (int i = 0; i < randomInt(10); i++) {
            ChoiceImpl<T> choice = new ChoiceImpl<T>();
            choice.setDisplayName(randomString());

            List<T> values = new ArrayList<T>();
            choice.setValue(values);

            int valueCount = (cardinality == Cardinality.SINGLE ? 1 : randomInt(5));
            for (int j = 0; j < valueCount; j++) {
                if (clazz == Boolean.class) {
                    values.add((T) randomBoolean());
                } else if (clazz == String.class) {
                    values.add((T) randomUri());
                } else if (clazz == BigInteger.class) {
                    values.add((T) randomInteger());
                } else if (clazz == BigDecimal.class) {
                    values.add((T) randomDecimal());
                } else if (clazz == GregorianCalendar.class) {
                    values.add((T) randomDateTime());
                } else {
                    assert false;
                }
            }

            result.add(choice);
        }

        return result;
    }

    // --- asserts ---

    protected void assertTypeDefinition10(TypeDefinition typeDef, boolean validate) throws Exception {
        assertXmlTypeDefinition10(typeDef, validate);
        assertWsTypeDefinition10(typeDef);
    }

    protected void assertXmlTypeDefinition10(TypeDefinition typeDef, boolean validate) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        XMLStreamWriter writer = createWriter(out);
        XMLConverter.writeTypeDefinition(writer, CmisVersion.CMIS_1_0, TEST_NAMESPACE, typeDef);
        closeWriter(writer);

        byte[] xml = out.toByteArray();

        if (validate) {
            validate(xml, CmisVersion.CMIS_1_0);
        }

        XMLStreamReader parser = createParser(xml);
        TypeDefinition result = XMLConverter.convertTypeDefinition(parser);
        closeParser(parser);

        assertNotNull(result);
        assertDataObjectsEquals("TypeDefinition", typeDef, result, cmis10ignoreMethods);
        assertNull(result.getExtensions());
    }

    protected void assertWsTypeDefinition10(TypeDefinition typeDef) throws Exception {
        CmisTypeDefinitionType ws = WSConverter.convert(typeDef);

        TypeDefinition result = WSConverter.convert(ws);

        assertNotNull(result);
        assertDataObjectsEquals("TypeDefinition", typeDef, result, null);
        assertNull(result.getExtensions());
    }

    protected void assertTypeDefinition11(TypeDefinition typeDef, boolean validate) throws Exception {
        assertXmlTypeDefinition11(typeDef, validate);
        assertWsTypeDefinition11(typeDef);
        assertJsonTypeDefinition11(typeDef);
    }

    protected void assertXmlTypeDefinition11(TypeDefinition typeDef, boolean validate) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        XMLStreamWriter writer = createWriter(out);
        XMLConverter.writeTypeDefinition(writer, CmisVersion.CMIS_1_1, TEST_NAMESPACE, typeDef);
        closeWriter(writer);

        byte[] xml = out.toByteArray();

        if (validate) {
            validate(xml, CmisVersion.CMIS_1_1);
        }

        XMLStreamReader parser = createParser(xml);
        TypeDefinition result = XMLConverter.convertTypeDefinition(parser);
        closeParser(parser);

        assertNotNull(result);
        assertDataObjectsEquals("TypeDefinition", typeDef, result, null);
        assertNull(result.getExtensions());
    }

    protected void assertWsTypeDefinition11(TypeDefinition typeDef) throws Exception {
        CmisTypeDefinitionType ws = WSConverter.convert(typeDef);

        TypeDefinition result = WSConverter.convert(ws);

        assertNotNull(result);
        assertDataObjectsEquals("TypeDefinition", typeDef, result, null);
        assertNull(result.getExtensions());
    }

    protected void assertJsonTypeDefinition11(TypeDefinition typeDef) throws Exception {
        StringWriter sw = new StringWriter();

        JSONConverter.convert(typeDef, DateTimeFormat.SIMPLE).writeJSONString(sw);

        Object json = (new JSONParser()).parse(sw.toString());
        assertTrue(json instanceof Map<?, ?>);
        @SuppressWarnings("unchecked")
        TypeDefinition result = JSONConverter.convertTypeDefinition((Map<String, Object>) json);

        assertNotNull(result);
        assertDataObjectsEquals("TypeDefinition", typeDef, result, null);
        assertNull(result.getExtensions());
    }

    protected void assertTypeDefinitionList(TypeDefinitionList typeDefList) throws Exception {
        assertWsTypeDefinitionList(typeDefList);
        assertJsonTypeDefinitionList(typeDefList);
    }

    protected void assertWsTypeDefinitionList(TypeDefinitionList typeDefList) throws Exception {
        CmisTypeDefinitionListType ws = WSConverter.convert(typeDefList);

        TypeDefinitionList result = WSConverter.convert(ws);

        assertNotNull(result);
        assertDataObjectsEquals("TypeDefinitionList", typeDefList, result, null);
        assertNull(result.getExtensions());
    }

    protected void assertJsonTypeDefinitionList(TypeDefinitionList typeDefList) throws Exception {
        StringWriter sw = new StringWriter();

        JSONConverter.convert(typeDefList, DateTimeFormat.SIMPLE).writeJSONString(sw);

        Object json = (new JSONParser()).parse(sw.toString());
        assertTrue(json instanceof Map<?, ?>);
        @SuppressWarnings("unchecked")
        TypeDefinitionList result = JSONConverter.convertTypeChildren((Map<String, Object>) json);

        assertNotNull(result);
        assertDataObjectsEquals("TypeDefinitionList", typeDefList, result, null);
        assertNull(result.getExtensions());
    }

    protected void assertTypeDefinitionContainer(TypeDefinitionContainer typeDefContainer) throws Exception {
        assertWsTypeDefinitionContainer(typeDefContainer);
        assertJsonTypeDefinitionContainer(typeDefContainer);
    }

    protected void assertWsTypeDefinitionContainer(TypeDefinitionContainer typeDefContainer) throws Exception {
        List<CmisTypeContainer> target = new ArrayList<CmisTypeContainer>();
        WSConverter.convertTypeContainerList(Collections.singletonList(typeDefContainer), target);

        assertEquals(1, target.size());

        List<TypeDefinitionContainer> result = WSConverter.convertTypeContainerList(target);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertDataObjectsEquals("TypeDefinitionContainer", typeDefContainer, result.get(0), null);
    }

    protected void assertJsonTypeDefinitionContainer(TypeDefinitionContainer typeDefContainer) throws Exception {
        StringWriter sw = new StringWriter();

        JSONConverter.convert(typeDefContainer, DateTimeFormat.SIMPLE).writeJSONString(sw);

        Object json = (new JSONParser()).parse(sw.toString());
        assertTrue(json instanceof Map<?, ?>);

        List<TypeDefinitionContainer> result = JSONConverter.convertTypeDescendants(Collections.singletonList(json));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertDataObjectsEquals("TypeDefinitionContainer", typeDefContainer, result.get(0), null);
    }
}
