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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.impl.DateTimeHelper;
import org.apache.chemistry.opencmis.commons.impl.XMLConstants;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.XMLUtils;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BulkUpdateImpl;
import org.xmlpull.v1.XmlSerializer;

/**
 * Writes a CMIS Atom entry to an output stream.
 */
public class AtomEntryWriter {

    private static final int BUFFER_SIZE = 8 * 1024;

    private final CmisVersion cmisVersion;
    private final ObjectData object;
    private final ContentStream contentStream;
    private final InputStream stream;
    private final TypeDefinition typeDef;
    private final BulkUpdateImpl bulkUpdate;

    /**
     * Constructor for objects.
     */
    public AtomEntryWriter(ObjectData object, CmisVersion cmisVersion) {
        this(object, cmisVersion, null);
    }

    /**
     * Constructor for objects.
     */
    public AtomEntryWriter(ObjectData object, CmisVersion cmisVersion, ContentStream contentStream) {
        if (object == null || object.getProperties() == null) {
            throw new CmisInvalidArgumentException("Object and properties must not be null!");
        }

        if (contentStream != null && contentStream.getMimeType() == null) {
            throw new CmisInvalidArgumentException("Media type must be set if a stream is present!");
        }

        this.object = object;
        this.cmisVersion = cmisVersion;
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
        this.typeDef = null;
        this.bulkUpdate = null;
    }

    /**
     * Constructor for types.
     */
    public AtomEntryWriter(TypeDefinition type, CmisVersion cmisVersion) {
        if (type == null) {
            throw new CmisInvalidArgumentException("Type must not be null!");
        }

        this.typeDef = type;
        this.cmisVersion = cmisVersion;
        this.object = null;
        this.contentStream = null;
        this.stream = null;
        this.bulkUpdate = null;
    }

    /**
     * Constructor for bulk updates.
     */
    public AtomEntryWriter(BulkUpdateImpl bulkUpdate) {
        if (bulkUpdate == null) {
            throw new CmisInvalidArgumentException("Bulk update data must not be null!");
        }

        this.bulkUpdate = bulkUpdate;
        this.typeDef = null;
        this.cmisVersion = CmisVersion.CMIS_1_1;
        this.object = null;
        this.contentStream = null;
        this.stream = null;
    }

    /**
     * Writes the entry to an output stream.
     */
    public void write(OutputStream out) throws IOException {
        XmlSerializer writer = XMLUtils.createWriter(out);

        // start doc
        XMLUtils.startXmlDocument(writer);

        // start entry
        writer.startTag(XMLConstants.NAMESPACE_ATOM, TAG_ENTRY);

        writer.attribute("", XMLConstants.PREFIX_ATOM, XMLConstants.NAMESPACE_ATOM);
        writer.attribute("", XMLConstants.PREFIX_CMIS, XMLConstants.NAMESPACE_CMIS);
        writer.attribute("", XMLConstants.PREFIX_RESTATOM, XMLConstants.NAMESPACE_RESTATOM);

        if (contentStream != null && contentStream.getFileName() != null) {
            writer.attribute("", XMLConstants.PREFIX_APACHE_CHEMISTY, XMLConstants.NAMESPACE_APACHE_CHEMISTRY);
        }

        // atom:id
        writeTag(writer, XMLConstants.NAMESPACE_ATOM, TAG_ATOM_ID, "urn:uuid:00000000-0000-0000-0000-00000000000");

        // atom:title
        writeTag(writer, XMLConstants.NAMESPACE_ATOM, TAG_ATOM_TITLE, getTitle());

        // atom:updated
        writeTag(writer, XMLConstants.NAMESPACE_ATOM, TAG_ATOM_UPDATED, getUpdated());

        // content
        if (stream != null) {
            writer.startTag(XMLConstants.NAMESPACE_RESTATOM, TAG_CONTENT);

            writeTag(writer, XMLConstants.NAMESPACE_RESTATOM, TAG_CONTENT_MEDIATYPE, contentStream.getMimeType());

            if (contentStream.getFileName() != null) {
                XMLUtils.write(writer, XMLConstants.PREFIX_APACHE_CHEMISTY, XMLConstants.NAMESPACE_APACHE_CHEMISTRY,
                        TAG_CONTENT_FILENAME, contentStream.getFileName());
            }

            writer.startTag(XMLConstants.NAMESPACE_RESTATOM, TAG_CONTENT_BASE64);
            writeContent(writer);
            writer.endTag(XMLConstants.NAMESPACE_RESTATOM, TAG_CONTENT_BASE64);

            writer.endTag(XMLConstants.NAMESPACE_RESTATOM, TAG_CONTENT);
        }

        // object
        if (object != null) {
            XMLConverter.writeObject(writer, cmisVersion, XMLConstants.NAMESPACE_RESTATOM, object);
        }

        // type
        if (typeDef != null) {
            XMLConverter.writeTypeDefinition(writer, cmisVersion, XMLConstants.NAMESPACE_RESTATOM, typeDef);
        }

        // bulk update
        if (bulkUpdate != null) {
            XMLConverter.writeBulkUpdate(writer, XMLConstants.NAMESPACE_RESTATOM, bulkUpdate);
        }

        // end entry
        writer.endTag(XMLConstants.NAMESPACE_ATOM, TAG_ENTRY);

        // end document
        XMLUtils.endXmlDocument(writer);
    }

    // ---- internal ----

    private String getTitle() {
        String result = "";

        if (object != null) {
            PropertyData<?> nameProperty = object.getProperties().getProperties().get(PropertyIds.NAME);
            if (nameProperty instanceof PropertyString) {
                result = ((PropertyString) nameProperty).getFirstValue();
            }
        }

        if (typeDef != null) {
            if (typeDef.getDisplayName() != null) {
                result = typeDef.getDisplayName();
            }
        }

        if (bulkUpdate != null) {
            result = "Bulk Update Properties";
        }

        return result;
    }

    private void writeContent(XmlSerializer writer) throws IOException {
        @SuppressWarnings("resource")
        Base64.InputStream b64stream = new Base64.InputStream(stream, Base64.ENCODE);
        byte[] buffer = new byte[BUFFER_SIZE];
        int numBytes;

        while ((numBytes = b64stream.read(buffer, 0, BUFFER_SIZE)) >= 0) {
            writer.text(new String(buffer, 0, numBytes, "ISO-8859-1"));
        }
    }

    private static String getUpdated() {
        return DateTimeHelper.formatXmlDateTime(new GregorianCalendar(DateTimeHelper.GMT));
    }

    private static void writeTag(XmlSerializer writer, String tagNameSpace, String tagName, String text)
            throws IOException {
        writer.startTag(tagNameSpace, tagName);
        writer.text(text);
        writer.endTag(tagNameSpace, tagName);
    }
}
