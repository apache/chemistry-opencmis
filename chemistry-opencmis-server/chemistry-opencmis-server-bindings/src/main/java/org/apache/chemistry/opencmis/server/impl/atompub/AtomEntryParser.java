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
package org.apache.chemistry.opencmis.server.impl.atompub;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.Converter;
import org.apache.chemistry.opencmis.commons.impl.JaxBHelper;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.commons.codec.binary.Base64;

/**
 * Parser for Atom Entries.
 */
public class AtomEntryParser {

    private final static String TAG_ENTRY = "entry";
    private final static String TAG_TITLE = "title";
    private final static String TAG_OBJECT = "object";
    private final static String TAG_CONTENT = "content";
    private final static String TAG_BASE64 = "base64";
    private final static String TAG_MEDIATYPE = "mediatype";

    private final static String ATTR_SRC = "src";
    private final static String ATTR_TYPE = "type";

    private ObjectData fObject;
    private ContentStreamImpl fAtomContentStream;
    private ContentStreamImpl fCmisContentStream;

    /**
     * Constructor.
     */
    public AtomEntryParser() {
    }

    /**
     * Constructor that immediately parses the given stream.
     */
    public AtomEntryParser(InputStream stream) throws Exception {
        parse(stream);
    }

    /**
     * Returns the object.
     */
    public ObjectData getObject() {
        return fObject;
    }

    /**
     * Returns the properties of the object.
     */
    public Properties getProperties() {
        return (fObject == null ? null : fObject.getProperties());
    }

    /**
     * Returns the Id of the object.
     */
    public String getId() {
        Properties properties = getProperties();
        if (properties == null) {
            return null;
        }

        Map<String, PropertyData<?>> propertiesMap = properties.getProperties();
        if (propertiesMap == null) {
            return null;
        }

        PropertyData<?> property = propertiesMap.get(PropertyIds.OBJECT_ID);
        if (property instanceof PropertyId) {
            return ((PropertyId) property).getFirstValue();
        }

        return null;
    }

    /**
     * Returns the ACL of the object.
     */
    public Acl getAcl() {
        return (fObject == null ? null : fObject.getAcl());
    }

    /**
     * Returns the policy id list of the object.
     */
    public List<String> getPolicyIds() {
        if ((fObject == null) || (fObject.getPolicyIds() == null)) {
            return null;
        }

        return fObject.getPolicyIds().getPolicyIds();
    }

    /**
     * Returns the content stream.
     */
    public ContentStream getContentStream() {
        return (fCmisContentStream == null ? fAtomContentStream : fCmisContentStream);
    }

    /**
     * Parses the stream.
     */
    public void parse(InputStream stream) throws Exception {
        fObject = null;
        fAtomContentStream = null;
        fCmisContentStream = null;

        if (stream == null) {
            return;
        }

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader parser = factory.createXMLStreamReader(stream);

        while (true) {
            int event = parser.getEventType();
            if (event == XMLStreamReader.START_ELEMENT) {
                QName name = parser.getName();

                if (Constants.NAMESPACE_ATOM.equals(name.getNamespaceURI()) && (TAG_ENTRY.equals(name.getLocalPart()))) {
                    parseEntry(parser);
                    break;
                } else {
                    throw new CmisInvalidArgumentException("XML is not an Atom entry!");
                }
            }

            if (!next(parser)) {
                break;
            }
        }

        parser.close();
    }

    /**
     * Parses an Atom entry.
     */
    private void parseEntry(XMLStreamReader parser) throws Exception {
        String atomTitle = null;

        next(parser);

        // walk through all tags in entry
        while (true) {
            int event = parser.getEventType();
            if (event == XMLStreamReader.START_ELEMENT) {
                QName name = parser.getName();

                if (Constants.NAMESPACE_RESTATOM.equals(name.getNamespaceURI())) {
                    if (TAG_OBJECT.equals(name.getLocalPart())) {
                        parseObject(parser);
                    } else if (TAG_CONTENT.equals(name.getLocalPart())) {
                        parseCmisContent(parser);
                    } else {
                        skip(parser);
                    }
                } else if (Constants.NAMESPACE_ATOM.equals(name.getNamespaceURI())) {
                    if (TAG_CONTENT.equals(name.getLocalPart())) {
                        parseAtomContent(parser);
                    } else if (TAG_TITLE.equals(name.getLocalPart())) {
                        atomTitle = readText(parser);
                    } else {
                        skip(parser);
                    }
                } else {
                    skip(parser);
                }
            } else if (event == XMLStreamReader.END_ELEMENT) {
                break;
            } else {
                if (!next(parser)) {
                    break;
                }
            }
        }

        // overwrite cmis:name with Atom title
        if ((fObject != null) && (fObject.getProperties() != null) && (atomTitle != null) && (atomTitle.length() > 0)) {
            PropertyString nameProperty = new PropertyStringImpl(PropertyIds.NAME, atomTitle);
            ((PropertiesImpl) fObject.getProperties()).addProperty(nameProperty);
        }
    }

    /**
     * Parses a CMIS object.
     */
    private void parseObject(XMLStreamReader parser) throws Exception {
        Unmarshaller u = JaxBHelper.createUnmarshaller();
        JAXBElement<CmisObjectType> object = u.unmarshal(parser, CmisObjectType.class);

        if (object != null) {
            fObject = Converter.convert(object.getValue());
        }
    }

    /**
     * Extract the content stream.
     */
    private void parseAtomContent(XMLStreamReader parser) throws Exception {
        fAtomContentStream = new ContentStreamImpl();

        // read attributes
        String type = "text";
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            QName attrName = parser.getAttributeName(i);
            if (ATTR_TYPE.equals(attrName.getLocalPart())) {
                fAtomContentStream.setMimeType(parser.getAttributeValue(i));
                if (parser.getAttributeValue(i) != null) {
                    type = parser.getAttributeValue(i).trim().toLowerCase();
                }
            } else if (ATTR_SRC.equals(attrName.getLocalPart())) {
                throw new CmisNotSupportedException("External content not supported!");
            }
        }

        byte[] bytes = null;
        if (type.equals("text") || type.equals("html")) {
            bytes = readText(parser).getBytes("UTF-8");
        } else if (type.equals("xhtml")) {
            bytes = copy(parser);
        } else if (type.endsWith("/xml") || type.endsWith("+xml")) {
            bytes = copy(parser);
        } else if (type.startsWith("text/")) {
            bytes = readText(parser).getBytes("UTF-8");
        } else {
            bytes = Base64.decodeBase64(readText(parser));
        }

        if (bytes != null) {
            fAtomContentStream.setStream(new ByteArrayInputStream(bytes));
            fAtomContentStream.setLength(BigInteger.valueOf(bytes.length));
        }
    }

    /**
     * Extract the content stream.
     */
    private void parseCmisContent(XMLStreamReader parser) throws Exception {
        fCmisContentStream = new ContentStreamImpl();

        next(parser);

        // walk through all tags in content
        while (true) {
            int event = parser.getEventType();
            if (event == XMLStreamReader.START_ELEMENT) {
                QName name = parser.getName();

                if (Constants.NAMESPACE_RESTATOM.equals(name.getNamespaceURI())) {
                    if (TAG_MEDIATYPE.equals(name.getLocalPart())) {
                        fCmisContentStream.setMimeType(readText(parser));
                    } else if (TAG_BASE64.equals(name.getLocalPart())) {
                        byte[] bytes = Base64.decodeBase64(readText(parser));
                        fCmisContentStream.setStream(new ByteArrayInputStream(bytes));
                        fCmisContentStream.setLength(BigInteger.valueOf(bytes.length));
                    } else {
                        skip(parser);
                    }
                } else {
                    skip(parser);
                }
            } else if (event == XMLStreamReader.END_ELEMENT) {
                break;
            } else {
                if (!next(parser)) {
                    break;
                }
            }
        }

        next(parser);
    }

    /**
     * Parses a tag that contains text.
     */
    private String readText(XMLStreamReader parser) throws Exception {
        StringBuilder sb = new StringBuilder();

        next(parser);

        while (true) {
            int event = parser.getEventType();
            if (event == XMLStreamReader.END_ELEMENT) {
                break;
            } else if (event == XMLStreamReader.CHARACTERS) {
                String s = parser.getText();
                if (s != null) {
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

        return sb.toString();
    }

    /**
     * Copies a subtree into a stream.
     */
    private byte[] copy(XMLStreamReader parser) throws Exception {
        // create a writer
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);

        writer.writeStartDocument();

        // copy subtree
        int level = 1;
        while (next(parser)) {
            int event = parser.getEventType();
            if (event == XMLStreamReader.START_ELEMENT) {
                copyStartElement(parser, writer);
                level++;
            } else if (event == XMLStreamReader.CHARACTERS) {
                writer.writeCharacters(parser.getText());
            } else if (event == XMLStreamReader.COMMENT) {
                writer.writeComment(parser.getText());
            } else if (event == XMLStreamReader.CDATA) {
                writer.writeCData(parser.getText());
            } else if (event == XMLStreamReader.END_ELEMENT) {
                level--;
                if (level == 0) {
                    break;
                }
                writer.writeEndElement();
            } else {
                break;
            }
        }

        writer.writeEndDocument();

        next(parser);

        return out.toByteArray();
    }

    /**
     * Copies a XML start element.
     */
    private void copyStartElement(XMLStreamReader parser, XMLStreamWriter writer) throws Exception {
        String namespaceUri = parser.getNamespaceURI();
        String prefix = parser.getPrefix();
        String localName = parser.getLocalName();

        // write start element
        if (namespaceUri != null) {
            if ((prefix == null) || (prefix.length() == 0)) {
                writer.writeStartElement(localName);
            } else {
                writer.writeStartElement(prefix, localName, namespaceUri);
            }
        } else {
            writer.writeStartElement(localName);
        }

        // set namespaces
        for (int i = 0; i < parser.getNamespaceCount(); i++) {
            addNamespace(writer, parser.getNamespacePrefix(i), parser.getNamespaceURI(i));
        }
        addNamespaceIfMissing(writer, prefix, namespaceUri);

        // write attributes
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String attrNamespaceUri = parser.getAttributeNamespace(i);
            String attrPrefix = parser.getAttributePrefix(i);
            String attrName = parser.getAttributeLocalName(i);
            String attrValue = parser.getAttributeValue(i);

            if ((attrNamespaceUri == null) || (attrNamespaceUri.trim().length() == 0)) {
                writer.writeAttribute(attrName, attrValue);
            } else if ((attrPrefix == null) || (attrPrefix.trim().length() == 0)) {
                writer.writeAttribute(attrNamespaceUri, attrName, attrValue);
            } else {
                addNamespaceIfMissing(writer, attrPrefix, attrNamespaceUri);
                writer.writeAttribute(attrPrefix, attrNamespaceUri, attrName, attrValue);
            }
        }
    }

    /**
     * Checks if the given prefix is assigned to the given namespace.
     */
    @SuppressWarnings("unchecked")
    private void addNamespaceIfMissing(XMLStreamWriter writer, String prefix, String namespaceUri) throws Exception {
        if ((namespaceUri == null) || (namespaceUri.trim().length() == 0)) {
            return;
        }

        if (prefix == null) {
            prefix = "";
        }

        Iterator<String> iter = (Iterator<String>) writer.getNamespaceContext().getPrefixes(namespaceUri);
        if (iter == null) {
            return;
        }

        while (iter.hasNext()) {
            String p = iter.next();
            if ((p != null) && (p.equals(prefix))) {
                return;
            }
        }

        addNamespace(writer, prefix, namespaceUri);
    }

    /**
     * Adds a namespace to a XML element.
     */
    private void addNamespace(XMLStreamWriter writer, String prefix, String namespaceUri) throws Exception {
        if ((prefix == null) || (prefix.trim().length() == 0)) {
            writer.setDefaultNamespace(namespaceUri);
            writer.writeDefaultNamespace(namespaceUri);
        } else {
            writer.setPrefix(prefix, namespaceUri);
            writer.writeNamespace(prefix, namespaceUri);
        }
    }

    /**
     * Skips a tag or subtree.
     */
    private void skip(XMLStreamReader parser) throws Exception {
        int level = 1;
        while (next(parser)) {
            int event = parser.getEventType();
            if (event == XMLStreamReader.START_ELEMENT) {
                level++;
            } else if (event == XMLStreamReader.END_ELEMENT) {
                level--;
                if (level == 0) {
                    break;
                }
            }
        }

        next(parser);
    }

    private boolean next(XMLStreamReader parser) throws Exception {
        if (parser.hasNext()) {
            try {
                parser.next();
            } catch (XMLStreamException e) {
                return false;
            }
            return true;
        }

        return false;
    }
}