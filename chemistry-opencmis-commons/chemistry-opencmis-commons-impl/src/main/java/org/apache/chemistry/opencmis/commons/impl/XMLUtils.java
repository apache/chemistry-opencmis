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
package org.apache.chemistry.opencmis.commons.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;

public class XMLUtils {

    public static final ThreadLocal<XMLInputFactory> THREADLOCAL_XML_INPUT_FACTORY = new ThreadLocal<XMLInputFactory>() {
        @Override
        protected XMLInputFactory initialValue() {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
            return factory;
        }
    };

    public static final ThreadLocal<XMLOutputFactory> THREADLOCAL_XML_OUTPUT_FACTORY = new ThreadLocal<XMLOutputFactory>() {
        @Override
        protected XMLOutputFactory initialValue() {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.FALSE);
            return factory;
        }
    };

    // --------------
    // --- writer ---
    // --------------

    /**
     * Creates a new XML writer.
     */
    public static XMLStreamWriter createWriter(OutputStream out) throws XMLStreamException {
        return THREADLOCAL_XML_OUTPUT_FACTORY.get().createXMLStreamWriter(out, "UTF-8");
    }

    /**
     * Starts a XML document.
     */
    public static void startXmlDocument(XMLStreamWriter writer) throws XMLStreamException {
        writer.setPrefix(XMLConstants.PREFIX_ATOM, XMLConstants.NAMESPACE_ATOM);
        writer.setPrefix(XMLConstants.PREFIX_CMIS, XMLConstants.NAMESPACE_CMIS);
        writer.setPrefix(XMLConstants.PREFIX_RESTATOM, XMLConstants.NAMESPACE_RESTATOM);
        writer.setPrefix(XMLConstants.PREFIX_APACHE_CHEMISTY, XMLConstants.NAMESPACE_APACHE_CHEMISTRY);

        writer.writeStartDocument();
    }

    /**
     * Starts an AtomPub Entry document.
     */
    public static void startEntryDocument(XMLStreamWriter writer, boolean hasContent) throws XMLStreamException {
        startXmlDocument(writer);

        writer.writeStartElement(XMLConstants.PREFIX_ATOM, "entry", XMLConstants.NAMESPACE_ATOM);
        writer.writeNamespace(XMLConstants.PREFIX_ATOM, XMLConstants.NAMESPACE_ATOM);
        writer.writeNamespace(XMLConstants.PREFIX_CMIS, XMLConstants.NAMESPACE_CMIS);
        writer.writeNamespace(XMLConstants.PREFIX_RESTATOM, XMLConstants.NAMESPACE_RESTATOM);
        if (hasContent) {
            writer.writeNamespace(XMLConstants.PREFIX_APACHE_CHEMISTY, XMLConstants.NAMESPACE_APACHE_CHEMISTRY);
        }
    }

    /**
     * Starts an AtomPub Feed document.
     */
    public static void startFeedDocument(XMLStreamWriter writer, String tag, boolean hasContent)
            throws XMLStreamException {
        startXmlDocument(writer);

        writer.writeStartElement(XMLConstants.PREFIX_ATOM, "feed", XMLConstants.NAMESPACE_ATOM);
        writer.writeNamespace(XMLConstants.PREFIX_ATOM, XMLConstants.NAMESPACE_ATOM);
        writer.writeNamespace(XMLConstants.PREFIX_CMIS, XMLConstants.NAMESPACE_CMIS);
        writer.writeNamespace(XMLConstants.PREFIX_RESTATOM, XMLConstants.NAMESPACE_RESTATOM);
    }

    /**
     * Ends a XML document.
     */
    public static void endXmlDocument(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEndDocument();
        writer.close();
    }

    /**
     * Writes a String tag.
     */
    public static void write(XMLStreamWriter writer, String prefix, String namespace, String tag, String value)
            throws XMLStreamException {
        if (value == null) {
            return;
        }

        if (namespace == null) {
            writer.writeStartElement(tag);
        } else {
            writer.writeStartElement(prefix, tag, namespace);
        }
        writer.writeCharacters(value);
        writer.writeEndElement();
    }

    /**
     * Writes an Integer tag.
     */
    public static void write(XMLStreamWriter writer, String prefix, String namespace, String tag, BigInteger value)
            throws XMLStreamException {
        if (value == null) {
            return;
        }

        write(writer, prefix, namespace, tag, value.toString());
    }

    /**
     * Writes a Decimal tag.
     */
    public static void write(XMLStreamWriter writer, String prefix, String namespace, String tag, BigDecimal value)
            throws XMLStreamException {
        if (value == null) {
            return;
        }

        write(writer, prefix, namespace, tag, value.toString());
    }

    /**
     * Writes a DateTime tag.
     */
    public static void write(XMLStreamWriter writer, String prefix, String namespace, String tag,
            GregorianCalendar value) throws XMLStreamException {
        if (value == null) {
            return;
        }

        write(writer, prefix, namespace, tag, DateTimeHelper.formatXmlDateTime(value));
    }

    /**
     * Writes a Boolean tag.
     */
    public static void write(XMLStreamWriter writer, String prefix, String namespace, String tag, Boolean value)
            throws XMLStreamException {
        if (value == null) {
            return;
        }

        write(writer, prefix, namespace, tag, value ? "true" : "false");
    }

    /**
     * Writes an Enum tag.
     */
    public static void write(XMLStreamWriter writer, String prefix, String namespace, String tag, Enum<?> value)
            throws XMLStreamException {
        if (value == null) {
            return;
        }

        Object enumValue;
        try {
            enumValue = value.getClass().getMethod("value", new Class[0]).invoke(value, new Object[0]);
        } catch (Exception e) {
            throw new XMLStreamException("Cannot get enum value", e);
        }

        write(writer, prefix, namespace, tag, enumValue.toString());
    }

    // ---------------
    // ---- parser ---
    // ---------------

    /**
     * Creates a new XML parser with OpenCMIS default settings.
     */
    public static XMLStreamReader createParser(InputStream stream) throws XMLStreamException {
        return THREADLOCAL_XML_INPUT_FACTORY.get().createXMLStreamReader(stream);
    }

    /**
     * Moves the parser to the next element.
     */
    public static boolean next(XMLStreamReader parser) throws XMLStreamException {
        if (parser.hasNext()) {
            parser.next();
            return true;
        }

        return false;
    }

    /**
     * Moves the parser to the next start element.
     * 
     * @return <code>true</code> if another start element has been found,
     *         <code>false</code> otherwise
     */
    public static boolean findNextStartElemenet(XMLStreamReader parser) throws XMLStreamException {
        while (true) {
            int event = parser.getEventType();

            if (event == XMLStreamReader.START_ELEMENT) {
                return true;
            }

            if (parser.hasNext()) {
                parser.next();
            } else {
                return false;
            }
        }
    }

    /**
     * Parses a tag that contains text.
     */
    public static String readText(XMLStreamReader parser, int maxLength) throws XMLStreamException {
        StringBuilder sb = null;

        next(parser);

        while (true) {
            int event = parser.getEventType();
            if (event == XMLStreamReader.END_ELEMENT) {
                break;
            } else if (event == XMLStreamReader.CHARACTERS) {
                String s = parser.getText();
                if (s != null) {
                    if (sb == null) {
                        sb = new StringBuilder();
                    }

                    if (sb.length() + s.length() > maxLength) {
                        throw new CmisInvalidArgumentException("String limit exceeded!");
                    }
                    sb.append(s);
                }
            } else if (event == XMLStreamReader.START_ELEMENT) {
                throw new RuntimeException("Unexpected tag: " + parser.getName());
            }

            if (!next(parser)) {
                break;
            }
        }

        next(parser);

        return sb == null ? null : sb.toString();
    }

    /**
     * Clears out cached XML factories.
     */
    public static void clear() {
        THREADLOCAL_XML_INPUT_FACTORY.remove();
        THREADLOCAL_XML_OUTPUT_FACTORY.remove();
    }
}
