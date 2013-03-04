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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.DateTimeResolution;
import org.apache.chemistry.opencmis.commons.enums.DecimalPrecision;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
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

        switch (propertyType) {
        case BOOLEAN:
            result = new PropertyBooleanDefinitionImpl();
            ((PropertyBooleanDefinitionImpl) result).setDefaultValue(Arrays.asList(randomBoolean()));
            break;
        case DATETIME:
            result = new PropertyDateTimeDefinitionImpl();
            ((PropertyDateTimeDefinitionImpl) result).setDefaultValue(Arrays.asList(randomDateTime()));
            ((PropertyDateTimeDefinitionImpl) result).setDateTimeResolution(randomEnum(DateTimeResolution.class));
            break;
        case DECIMAL:
            result = new PropertyDecimalDefinitionImpl();
            ((PropertyDecimalDefinitionImpl) result).setDefaultValue(Arrays.asList(randomDecimal()));
            ((PropertyDecimalDefinitionImpl) result).setMaxValue(randomDecimal());
            ((PropertyDecimalDefinitionImpl) result).setMinValue(randomDecimal());
            ((PropertyDecimalDefinitionImpl) result).setPrecision(randomEnum(DecimalPrecision.class));
            break;
        case HTML:
            result = new PropertyHtmlDefinitionImpl();
            ((PropertyHtmlDefinitionImpl) result).setDefaultValue(Arrays.asList(randomString()));
            break;
        case ID:
            result = new PropertyIdDefinitionImpl();
            ((PropertyIdDefinitionImpl) result).setDefaultValue(Arrays.asList(randomString()));
            break;
        case INTEGER:
            result = new PropertyIntegerDefinitionImpl();
            ((PropertyIntegerDefinitionImpl) result).setDefaultValue(Arrays.asList(randomInteger()));
            ((PropertyIntegerDefinitionImpl) result).setMaxValue(randomInteger());
            ((PropertyIntegerDefinitionImpl) result).setMinValue(randomInteger());
            break;
        case STRING:
            result = new PropertyStringDefinitionImpl();
            ((PropertyStringDefinitionImpl) result).setDefaultValue(Arrays.asList(randomString()));
            ((PropertyStringDefinitionImpl) result).setMaxLength(randomInteger());
            break;
        case URI:
            result = new PropertyUriDefinitionImpl();
            ((PropertyUriDefinitionImpl) result).setDefaultValue(Arrays.asList(randomUri()));
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
        result.setCardinality(randomEnum(Cardinality.class));
        result.setUpdatability(randomEnum(Updatability.class));
        result.setIsQueryable(randomBoolean());
        result.setQueryName(randomString());
        result.setIsInherited(randomBoolean());
        result.setIsRequired(randomBoolean());
        result.setIsOrderable(randomBoolean());

        return result;
    }

    protected void assertTypeDefinition10(TypeDefinition typeDef, boolean validate) throws Exception {
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

    protected void assertTypeDefinition11(TypeDefinition typeDef, boolean validate) throws Exception {
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
}
