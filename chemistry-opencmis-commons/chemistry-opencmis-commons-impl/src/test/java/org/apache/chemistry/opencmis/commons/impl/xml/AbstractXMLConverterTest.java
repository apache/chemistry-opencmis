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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.chemistry.opencmis.commons.impl.XMLConstants;
import org.apache.chemistry.opencmis.commons.impl.XMLUtils;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public abstract class AbstractXMLConverterTest {

    protected final static String TEST_NAMESPACE = "http://chemistry.apache.org/test/schema";
    protected final static String TEST_PREFIX = "test";

    private Logger LOG = LoggerFactory.getLogger(AbstractXMLConverterTest.class);

    protected final static String TEST_SCHEMA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" targetNamespace=\""
            + TEST_NAMESPACE
            + "\" xmlns:test=\""
            + TEST_NAMESPACE
            + "\" xmlns:cmis=\""
            + XMLConstants.NAMESPACE_CMIS
            + "\" version=\"1.0\">"
            + "<xs:import namespace=\""
            + XMLConstants.NAMESPACE_CMIS
            + "\"/>"
            + "<xs:complexType name=\"testType\">"
            + "<xs:sequence>"
            + "<xs:element name=\"repositoryInfo\" type=\"cmis:cmisRepositoryInfoType\" minOccurs=\"0\" maxOccurs=\"1\" />"
            + "<xs:element name=\"type\" type=\"cmis:cmisTypeDefinitionType\" minOccurs=\"0\" maxOccurs=\"1\" />"
            + "<xs:element name=\"object\" type=\"cmis:cmisObjectType\" minOccurs=\"0\" maxOccurs=\"1\" />"
            + "</xs:sequence>" //
            + "</xs:complexType>" //
            + "<xs:element name=\"test\" type=\"test:testType\"/>" //
            + "</xs:schema>";

    protected static Schema schema;

    /**
     * Sets up the schema.
     */
    @BeforeClass
    public static void init() throws SAXException, UnsupportedEncodingException {
        SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        StreamSource core = new StreamSource(AbstractXMLConverterTest.class.getResourceAsStream("/wsdl/CMIS-core.xsd"));
        StreamSource test = new StreamSource(new ByteArrayInputStream(TEST_SCHEMA.getBytes("UTF-8")));

        schema = sf.newSchema(new Source[] { core, test });
    }

    /**
     * Writes root tag of the test XML.
     */
    protected void writeRootTag(XMLStreamWriter writer) throws XMLStreamException {
        writer.setPrefix(TEST_PREFIX, TEST_NAMESPACE);
        writer.writeStartElement(TEST_NAMESPACE, TEST_PREFIX);
        writer.writeNamespace(XMLUtils.PREFIX_XSI, XMLConstants.NAMESPACE_XSI);
        writer.writeNamespace(XMLUtils.PREFIX_CMIS, XMLConstants.NAMESPACE_CMIS);
        writer.writeNamespace(TEST_PREFIX, TEST_NAMESPACE);
    }

    /**
     * Creates a test XML writer.
     */
    protected XMLStreamWriter createWriter(OutputStream out) throws XMLStreamException {
        XMLStreamWriter writer = XMLUtils.createWriter(out);

        XMLUtils.startXmlDocument(writer);
        writeRootTag(writer);

        return writer;
    }

    /**
     * Closes the test XML writer.
     */
    protected void closeWriter(XMLStreamWriter writer) throws XMLStreamException {
        XMLUtils.endXmlDocument(writer);
        writer.close();
    }

    /**
     * Creates a parser and moves it to the tag that should be tested.
     */
    protected XMLStreamReader createParser(byte[] xmlDocument) throws XMLStreamException {
        XMLStreamReader parser = XMLUtils.createParser(new ByteArrayInputStream(xmlDocument));
        moveToTestTag(parser);

        return parser;
    }

    /**
     * Closes the parser.
     */
    protected void closeParser(XMLStreamReader parser) throws XMLStreamException {
        parser.close();
    }

    /**
     * Moves the parser to tag that should be tested.
     */
    protected void moveToTestTag(XMLStreamReader parser) throws XMLStreamException {
        while (XMLUtils.findNextStartElemenet(parser)) {
            if (parser.getName().getLocalPart().equals("test")) {
                XMLUtils.next(parser);
                XMLUtils.findNextStartElemenet(parser);
                break;
            }
        }
    }

    /**
     * Validates the given XML.
     */
    protected void validate(byte[] xmlDocument) {
        Validator validator = schema.newValidator();
        Source source = new StreamSource(new ByteArrayInputStream(xmlDocument));

        try {
            validator.validate(source);
        } catch (Exception e) {
            try {
                LOG.error("Schema validation failed:\n" + format(xmlDocument));
                System.out.println(format(xmlDocument));
            } catch (TransformerException e1) {
            }
            fail("Schema validation failed: " + e);
        }
    }

    /**
     * Formats an XML document.
     */
    protected String format(byte[] xmlDocument) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setAttribute("indent-number", new Integer(2));

        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StreamResult result = new StreamResult(new StringWriter());
        Source source = new StreamSource(new ByteArrayInputStream(xmlDocument));
        transformer.transform(source, result);

        return result.getWriter().toString();
    }

    /**
     * Compares two data objects.
     */
    protected void assertDataObjectsEquals(String name, Object expected, Object actual) {

        LOG.debug(name + ": " + expected + " / " + actual);

        if ((expected == null) && (actual == null)) {
            return;
        }

        if ((expected == null) || (actual == null)) {
            fail("Data object is null! name: " + name + " / expected: " + expected + " / actual: " + actual);
        }

        // handle simple types
        if ((expected instanceof String) || (expected instanceof Boolean) || (expected instanceof BigInteger)
                || (expected instanceof BigDecimal) || (expected instanceof Enum<?>)) {
            assertEquals(expected, actual);

            return;
        } else if (expected instanceof List<?>) {
            List<?> expectedList = (List<?>) expected;
            List<?> actualList = (List<?>) actual;

            assertEquals(expectedList.size(), actualList.size());

            for (int i = 0; i < expectedList.size(); i++) {
                assertDataObjectsEquals(name + "[" + i + "]", expectedList.get(i), actualList.get(i));
            }

            return;
        } else if (expected instanceof Map<?, ?>) {
            Map<?, ?> expectedMap = (Map<?, ?>) expected;
            Map<?, ?> actualMap = (Map<?, ?>) actual;

            assertEquals(expectedMap.size(), actualMap.size());

            for (Map.Entry<?, ?> entry : expectedMap.entrySet()) {
                assertTrue(actualMap.containsKey(entry.getKey()));
                assertDataObjectsEquals(name + "[" + entry.getKey() + "]", entry.getValue(),
                        actualMap.get(entry.getKey()));
            }

            return;
        }

        for (Method m : expected.getClass().getMethods()) {
            if (!m.getName().startsWith("get") && !m.getName().startsWith("is") && !m.getName().startsWith("supports")) {
                continue;
            }

            if (m.getName().equals("getClass")) {
                continue;
            }

            if (m.getParameterTypes().length != 0) {
                continue;
            }

            try {
                Object expectedValue = m.invoke(expected, new Object[0]);
                Object actualValue = m.invoke(actual, new Object[0]);

                assertDataObjectsEquals(name + "." + m.getName(), expectedValue, actualValue);
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }
}
