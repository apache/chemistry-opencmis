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

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.junit.Test;

public class TypeDefinitionConverterTest extends AbstractXMLConverterTest {

    @Test
    public void testTypeDefinition() throws Exception {
        DocumentTypeDefinitionImpl typeDef = new DocumentTypeDefinitionImpl();
        typeDef.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
        typeDef.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);
        typeDef.setDescription("description");
        typeDef.setDisplayName("displayName");
        typeDef.setId("id");
        typeDef.setIsControllableAcl(Boolean.TRUE);
        typeDef.setIsControllablePolicy(Boolean.FALSE);
        typeDef.setIsCreatable(Boolean.TRUE);
        typeDef.setIsFileable(Boolean.TRUE);
        typeDef.setIsIncludedInSupertypeQuery(Boolean.TRUE);
        typeDef.setIsFulltextIndexed(Boolean.FALSE);
        typeDef.setIsQueryable(Boolean.TRUE);
        typeDef.setIsVersionable(Boolean.TRUE);
        typeDef.setLocalName("localName");
        typeDef.setLocalNamespace("localNamespace");
        typeDef.setParentTypeId("parentId");
        typeDef.setQueryName("queryName");
        typeDef.setIsVersionable(Boolean.TRUE);
        typeDef.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);

        PropertyStringDefinitionImpl propDefString = new PropertyStringDefinitionImpl();
        propDefString.setId("stringProp");
        propDefString.setDescription("description");
        propDefString.setDisplayName("displayName");
        propDefString.setPropertyType(PropertyType.STRING);
        propDefString.setLocalName("localname");
        propDefString.setLocalNamespace("localnamespace");
        propDefString.setCardinality(Cardinality.SINGLE);
        propDefString.setUpdatability(Updatability.READWRITE);
        propDefString.setIsQueryable(Boolean.TRUE);
        propDefString.setQueryName("queryname");
        propDefString.setIsInherited(Boolean.FALSE);
        propDefString.setIsRequired(Boolean.FALSE);
        propDefString.setIsOrderable(Boolean.TRUE);

        typeDef.addPropertyDefinition(propDefString);

        assertTypeDefinition(typeDef, true);
    }

    protected void assertTypeDefinition(TypeDefinition typeDef, boolean validate) throws Exception {
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
        assertDataObjectsEquals("TypeDefinition", typeDef, result);
        assertNull(result.getExtensions());
    }
}
