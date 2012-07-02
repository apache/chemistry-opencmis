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
package org.apache.chemistry.opencmis.client.bindings.spi.atompub;

import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ATOM_ID;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ATOM_TITLE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ATOM_UPDATED;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CONTENT;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CONTENT_BASE64;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CONTENT_FILENAME;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CONTENT_MEDIATYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ENTRY;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.JaxBHelper;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisProperty;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisPropertyString;

/**
 * Writes a CMIS Atom entry to an output stream.
 */
public class AtomEntryWriter {

    private static final String PREFIX_ATOM = "atom";
    private static final String PREFIX_CMIS = "cmis";
    private static final String PREFIX_RESTATOM = "cmisra";
    private static final String PREFIX_APACHE_CHEMISTY = "chemistry";

    private static final int BUFFER_SIZE = 64 * 1024;

    private final CmisObjectType object;
    private final ContentStream contentStream;
    private final InputStream stream;

    /**
     * Constructor.
     */
    public AtomEntryWriter(CmisObjectType object) {
        this(object, null);
    }

    /**
     * Constructor.
     */
    public AtomEntryWriter(CmisObjectType object, ContentStream contentStream) {
        if ((object == null) || (object.getProperties() == null)) {
            throw new CmisInvalidArgumentException("Object and properties must not be null!");
        }

        if ((contentStream != null) && (contentStream.getMimeType() == null)) {
            throw new CmisInvalidArgumentException("Media type must be set if a stream is present!");
        }

        this.object = object;
        this.contentStream = contentStream;
        if (contentStream != null && contentStream.getStream() != null) {
            InputStream in = contentStream.getStream();

            // avoid double buffering
            if (!(in instanceof BufferedInputStream) && !(in instanceof ByteArrayInputStream)) {
                stream = new BufferedInputStream(in, BUFFER_SIZE);
            } else {
                stream = in;
            }
        } else {
            stream = null;
        }
    }

    /**
     * Writes the entry to an output stream.
     */
    public void write(OutputStream out) throws Exception {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(out, "UTF-8");

        writer.setPrefix(PREFIX_ATOM, Constants.NAMESPACE_ATOM);
        writer.setPrefix(PREFIX_CMIS, Constants.NAMESPACE_CMIS);
        writer.setPrefix(PREFIX_RESTATOM, Constants.NAMESPACE_RESTATOM);
        writer.setPrefix(PREFIX_APACHE_CHEMISTY, Constants.NAMESPACE_APACHE_CHEMISTRY);

        // start doc
        writer.writeStartDocument();

        // start entry
        writer.writeStartElement(Constants.NAMESPACE_ATOM, TAG_ENTRY);
        writer.writeNamespace(PREFIX_ATOM, Constants.NAMESPACE_ATOM);
        writer.writeNamespace(PREFIX_CMIS, Constants.NAMESPACE_CMIS);
        writer.writeNamespace(PREFIX_RESTATOM, Constants.NAMESPACE_RESTATOM);
        if (contentStream != null && contentStream.getFileName() != null) {
            writer.writeNamespace(PREFIX_APACHE_CHEMISTY, Constants.NAMESPACE_APACHE_CHEMISTRY);
        }

        // atom:id
        writer.writeStartElement(Constants.NAMESPACE_ATOM, TAG_ATOM_ID);
        writer.writeCharacters("urn:uuid:00000000-0000-0000-0000-00000000000");
        writer.writeEndElement();

        // atom:title
        writer.writeStartElement(Constants.NAMESPACE_ATOM, TAG_ATOM_TITLE);
        writer.writeCharacters(getTitle());
        writer.writeEndElement();

        // atom:updated
        writer.writeStartElement(Constants.NAMESPACE_ATOM, TAG_ATOM_UPDATED);
        writer.writeCharacters(getUpdated());
        writer.writeEndElement();

        // content
        if (stream != null) {
            writer.writeStartElement(Constants.NAMESPACE_RESTATOM, TAG_CONTENT);

            writer.writeStartElement(Constants.NAMESPACE_RESTATOM, TAG_CONTENT_MEDIATYPE);
            writer.writeCharacters(contentStream.getMimeType());
            writer.writeEndElement();

            if (contentStream.getFileName() != null) {
                writer.writeStartElement(Constants.NAMESPACE_APACHE_CHEMISTRY, TAG_CONTENT_FILENAME);
                writer.writeCharacters(contentStream.getFileName());
                writer.writeEndElement();
            }

            writer.writeStartElement(Constants.NAMESPACE_RESTATOM, TAG_CONTENT_BASE64);
            writeContent(writer);
            writer.writeEndElement();

            writer.writeEndElement();
        }

        // object
        JaxBHelper.marshal(JaxBHelper.CMIS_EXTRA_OBJECT_FACTORY.createObject(object), writer, true);

        // end entry
        writer.writeEndElement();

        // end document
        writer.writeEndDocument();

        writer.flush();
    }

    // ---- internal ----

    private String getTitle() {
        String result = "";

        for (CmisProperty property : object.getProperties().getProperty()) {
            if (PropertyIds.NAME.equals(property.getPropertyDefinitionId()) && (property instanceof CmisPropertyString)) {
                List<String> values = ((CmisPropertyString) property).getValue();
                if (!values.isEmpty()) {
                    return values.get(0);
                }
            }
        }

        return result;
    }

    private static String getUpdated() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return sdf.format(new Date());
    }

    private void writeContent(XMLStreamWriter writer) throws Exception {
        Base64.InputStream b64stream = new Base64.InputStream(stream, Base64.ENCODE);

        byte[] buffer = new byte[BUFFER_SIZE * 3 / 4];
        int b;
        while ((b = b64stream.read(buffer)) > -1) {
            if (b > 0) {
                writer.writeCharacters(new String(buffer, 0, b, "US-ASCII"));
            }
        }
    }
}
