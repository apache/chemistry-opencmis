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

import java.io.OutputStream;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.impl.Constants;

/**
 * Base class for XML documents.
 */
public abstract class XMLDocumentBase {

    public static final String PREFIX_ATOM = "atom";
    public static final String PREFIX_CMIS = "cmis";
    public static final String PREFIX_RESTATOM = "cmisra";
    public static final String PREFIX_APP = "app";
    public static final String PREFIX_XSI = "xsi";

    private XMLStreamWriter writer;
    private Map<String, String> namespaces;

    /**
     * Sets the namespaces for the document.
     */
    public void setNamespaces(Map<String, String> namespaces) throws XMLStreamException {
        writer.setPrefix(PREFIX_ATOM, Constants.NAMESPACE_ATOM);
        writer.setPrefix(PREFIX_CMIS, Constants.NAMESPACE_CMIS);
        writer.setPrefix(PREFIX_RESTATOM, Constants.NAMESPACE_RESTATOM);
        writer.setPrefix(PREFIX_APP, Constants.NAMESPACE_APP);
        writer.setPrefix(PREFIX_XSI, Constants.NAMESPACE_XSI);

        if (namespaces != null) {
            this.namespaces = namespaces;
            for (Map.Entry<String, String> ns : namespaces.entrySet()) {
                writer.setPrefix(ns.getKey(), ns.getValue());
            }
        }
    }

    /**
     * Writes the namespace declaration of the given URI to the current tag.
     */
    public void writeNamespace(String namespaceUri) throws XMLStreamException {
        writer.writeNamespace(writer.getPrefix(namespaceUri), namespaceUri);
    }

    /**
     * Writes custom namespace declaration to the current tag.
     */
    public void writeAllCustomNamespace() throws XMLStreamException {
        if (namespaces != null) {
            for (Map.Entry<String, String> ns : namespaces.entrySet()) {
                writer.writeNamespace(ns.getKey(), ns.getValue());
            }
        }
    }

    /**
     * Starts the document and sets the namespaces.
     */
    public void startDocument(OutputStream out, Map<String, String> namespaces) throws XMLStreamException {
        // create a writer
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        writer = factory.createXMLStreamWriter(out, "UTF-8");

        // start the document
        writer.writeStartDocument("UTF-8", "1.0");
        setNamespaces(namespaces);
    }

    /**
     * Finishes the document.
     */
    public void endDocument() throws XMLStreamException {
        if (writer == null) {
            return;
        }

        // end the document
        writer.writeEndDocument();

        // we are done.
        writer.close();
    }

    /**
     * Returns the writer object.
     */
    public XMLStreamWriter getWriter() {
        return writer;
    }

    /**
     * Sets the writer object.
     */
    protected void setWriter(XMLStreamWriter writer) {
        this.writer = writer;
    }
}
