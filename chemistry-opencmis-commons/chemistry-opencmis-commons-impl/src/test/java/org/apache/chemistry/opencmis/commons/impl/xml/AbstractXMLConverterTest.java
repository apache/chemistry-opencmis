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

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.XMLConstants;
import org.apache.chemistry.opencmis.commons.impl.XMLUtils;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractXMLConverterTest extends AbstractConverterTest {

    protected final static String TEST_NAMESPACE = "http://chemistry.apache.org/test/schema";
    protected final static String TEST_PREFIX = "test";

    private final static Logger LOG = LoggerFactory.getLogger(AbstractXMLConverterTest.class);

    protected final static String TEST_SCHEMA10 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
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
            + "<xs:element name=\"query\" type=\"cmis:cmisQueryType\" minOccurs=\"0\" maxOccurs=\"1\" />"
            + "</xs:sequence>" //
            + "</xs:complexType>" //
            + "<xs:element name=\"test\" type=\"test:testType\"/>" //
            + "</xs:schema>";

    protected final static String TEST_SCHEMA11 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
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
            + "<xs:element name=\"query\" type=\"cmis:cmisQueryType\" minOccurs=\"0\" maxOccurs=\"1\" />"
            + "<xs:element name=\"bulkUpdate\" type=\"cmis:cmisBulkUpdateType\" minOccurs=\"0\" maxOccurs=\"1\" />"
            + "</xs:sequence>" //
            + "</xs:complexType>" //
            + "<xs:element name=\"test\" type=\"test:testType\"/>" //
            + "</xs:schema>";

    protected Schema schema10;
    protected Schema schema11;

    /**
     * Sets up the schema.
     */
    @Override
    @Before
    public void init() throws Exception {
        super.init();

        SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        InputStream schema10stream = AbstractXMLConverterTest.class.getResourceAsStream("/schema/cmis10/CMIS-Core.xsd");
        if (schema10stream != null) {
            StreamSource core10 = new StreamSource(schema10stream);
            StreamSource test10 = new StreamSource(new ByteArrayInputStream(IOUtils.toUTF8Bytes(TEST_SCHEMA10)));
            schema10 = sf.newSchema(new Source[] { core10, test10 });
        }

        InputStream schema11stream = AbstractXMLConverterTest.class.getResourceAsStream("/schema/cmis11/CMIS-Core.xsd");
        if (schema11stream != null) {
            StreamSource core11 = new StreamSource(schema11stream);
            StreamSource test11 = new StreamSource(new ByteArrayInputStream(IOUtils.toUTF8Bytes(TEST_SCHEMA11)));
            schema11 = sf.newSchema(new Source[] { core11, test11 });
        }
    }

    /**
     * Writes root tag of the test XML.
     */
    protected void writeRootTag(XMLStreamWriter writer) throws XMLStreamException {
        writer.setPrefix(TEST_PREFIX, TEST_NAMESPACE);
        writer.writeStartElement(TEST_NAMESPACE, TEST_PREFIX);
        writer.writeNamespace(XMLConstants.PREFIX_XSI, XMLConstants.NAMESPACE_XSI);
        writer.writeNamespace(XMLConstants.PREFIX_CMIS, XMLConstants.NAMESPACE_CMIS);
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
    protected void validate(byte[] xmlDocument, CmisVersion cmisVersion) {
        Validator validator = null;
        if (cmisVersion == CmisVersion.CMIS_1_0) {
            if (schema10 != null) {
                validator = schema10.newValidator();
            } else {
                LOG.warn("CMIS 1.0 schema not loaded. Cannot validate XML.");
                return;
            }
        } else {
            if (schema11 != null) {
                validator = schema11.newValidator();
            } else {
                LOG.warn("CMIS 1.1 schema not loaded. Cannot validate XML.");
                return;
            }
        }

        Source source = new StreamSource(new ByteArrayInputStream(xmlDocument));

        try {
            validator.validate(source);
        } catch (Exception e) {
            try {
                LOG.error("Schema validation failed:\n" + format(xmlDocument));
                System.out.println(format(xmlDocument));
            } catch (TransformerException e1) {
            }
            fail("Schema " + cmisVersion.value() + " validation failed: " + e);
        }
    }

    /**
     * Formats an XML document.
     */
    protected String format(byte[] xmlDocument) throws TransformerException {
        Transformer transformer = XMLUtils.newTransformer(2);

        StreamResult result = new StreamResult(new StringWriter());
        Source source = new StreamSource(new ByteArrayInputStream(xmlDocument));
        transformer.transform(source, result);

        return result.getWriter().toString();
    }
}
