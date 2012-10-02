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

import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_DEFINITION_ID;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ATOM_ID;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ATOM_TITLE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ATOM_UPDATED;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CONTENT;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CONTENT_BASE64;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CONTENT_MEDIATYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ENTRY;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_QUERY;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_QUERY_STATEMENT;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_VALUE;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.PropertyBoolean;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyDateTime;
import org.apache.chemistry.opencmis.commons.data.PropertyDecimal;
import org.apache.chemistry.opencmis.commons.data.PropertyHtml;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyInteger;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.data.PropertyUri;
import org.apache.chemistry.opencmis.commons.enums.AtomPropertyType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

/**
 * Writes a CMIS Atom entry to an output stream.
 */
public class AtomEntryWriter {

    private static final String PREFIX_ATOM = "atom";
    private static final String PREFIX_CMIS = "cmis";
    private static final String PREFIX_RESTATOM = "cmisra";
    private static final String ENCODING = "UTF-8";

    private static final int BUFFER_SIZE = 64 * 1024;

    private final ObjectData object;
    private final InputStream stream;
    private final String mediaType;
    
    private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * Constructor.
     */
    public AtomEntryWriter(ObjectData object) {
        this(object, null, null);
    }

    /**
     * Constructor.
     */
    public AtomEntryWriter(ObjectData object, String mediaType, InputStream stream) {
        if ((object == null) || (object.getProperties() == null)) {
            throw new CmisInvalidArgumentException("Object and properties must not be null!");
        }

        if ((stream != null) && (mediaType == null)) {
            throw new CmisInvalidArgumentException("Media type must be set if a stream is present!");
        }

        this.object = object;
        this.mediaType = mediaType;

        if (stream != null && !(stream instanceof BufferedInputStream) && !(stream instanceof ByteArrayInputStream)) {
            // avoid double buffering
            stream = new BufferedInputStream(stream, BUFFER_SIZE);
        }

        this.stream = stream;
    }

    /**
     * Writes the entry to an output stream.
     */
    public void write(OutputStream out) throws Exception {
        XmlSerializer writer = Xml.newSerializer();
        writer.setOutput(out, ENCODING);

        // start doc
        writer.startDocument(ENCODING, false);
        writer.setPrefix(PREFIX_ATOM, Constants.NAMESPACE_ATOM);
        writer.setPrefix(PREFIX_CMIS, Constants.NAMESPACE_CMIS);
        writer.setPrefix(PREFIX_RESTATOM, Constants.NAMESPACE_RESTATOM);

        // start entry
        writer.startTag(Constants.NAMESPACE_ATOM, TAG_ENTRY);
        writer.attribute("", PREFIX_ATOM, Constants.NAMESPACE_ATOM);
        writer.attribute("", PREFIX_CMIS, Constants.NAMESPACE_CMIS);
        writer.attribute("", PREFIX_RESTATOM, Constants.NAMESPACE_RESTATOM);

        // atom:id
        writeTag(writer, Constants.NAMESPACE_ATOM, TAG_ATOM_ID, "urn:uuid:00000000-0000-0000-0000-00000000000");

        // atom:title
        writeTag(writer, Constants.NAMESPACE_ATOM, TAG_ATOM_TITLE, getTitle());

        // atom:updated
        writeTag(writer, Constants.NAMESPACE_ATOM, TAG_ATOM_UPDATED, getUpdated());

        // content
        if (stream != null) {
            writer.startTag(Constants.NAMESPACE_RESTATOM, TAG_CONTENT);

            writeTag(writer, Constants.NAMESPACE_RESTATOM, TAG_CONTENT_MEDIATYPE, mediaType);

            writer.startTag(Constants.NAMESPACE_RESTATOM, TAG_CONTENT_BASE64);
            writeContent(writer);
            writer.endTag(Constants.NAMESPACE_RESTATOM, TAG_CONTENT_BASE64);

            writer.endTag(Constants.NAMESPACE_RESTATOM, TAG_CONTENT);
        }

        // object
        writeObject(writer, object);

        // end entry
        writer.endTag(Constants.NAMESPACE_ATOM, TAG_ENTRY);

        // end document
        writer.endDocument();

        writer.flush();
    }

    public static void writeQuery(OutputStream out, Map<String, String> queryParams) throws Exception {
        XmlSerializer writer = Xml.newSerializer();
        writer.setOutput(out, ENCODING);
        writer.setPrefix(PREFIX_CMIS, Constants.NAMESPACE_CMIS);
        writer.startTag(Constants.NAMESPACE_CMIS, TAG_QUERY);
        writer.attribute(null, PREFIX_CMIS, Constants.NAMESPACE_CMIS);

        writer.startTag(Constants.NAMESPACE_CMIS, TAG_QUERY_STATEMENT);
        writer.cdsect(queryParams.get(TAG_QUERY_STATEMENT));
        writer.endTag(Constants.NAMESPACE_CMIS, TAG_QUERY_STATEMENT);
        writeTagIfNotNull(writer, Constants.NAMESPACE_CMIS, Constants.PARAM_SEARCH_ALL_VERSIONS,
                queryParams.get(Constants.PARAM_SEARCH_ALL_VERSIONS));
        writeTagIfNotNull(writer, Constants.NAMESPACE_CMIS, Constants.PARAM_ALLOWABLE_ACTIONS,
                queryParams.get(Constants.PARAM_ALLOWABLE_ACTIONS));
        writeTagIfNotNull(writer, Constants.NAMESPACE_CMIS, Constants.PARAM_RELATIONSHIPS,
                queryParams.get(Constants.PARAM_RELATIONSHIPS));
        writeTagIfNotNull(writer, Constants.NAMESPACE_CMIS, Constants.PARAM_RENDITION_FILTER,
                queryParams.get(Constants.PARAM_RENDITION_FILTER));
        writeTagIfNotNull(writer, Constants.NAMESPACE_CMIS, Constants.PARAM_MAX_ITEMS,
                queryParams.get(Constants.PARAM_MAX_ITEMS));
        writeTagIfNotNull(writer, Constants.NAMESPACE_CMIS, Constants.PARAM_SKIP_COUNT,
                queryParams.get(Constants.PARAM_SKIP_COUNT));

        writer.endTag(Constants.NAMESPACE_CMIS, TAG_QUERY);
        writer.flush();
    }

    public static void writeACL(OutputStream out, Acl acl) throws Exception {
        XmlSerializer writer = Xml.newSerializer();
        writer.setOutput(out, ENCODING);
        writer.setPrefix(PREFIX_CMIS, Constants.NAMESPACE_CMIS);
        writer.startTag(Constants.NAMESPACE_CMIS, "acl");
        writer.attribute(null, PREFIX_CMIS, Constants.NAMESPACE_CMIS);

        writer.startTag(Constants.NAMESPACE_CMIS, "permission");

        // TODO Implements
        // writeTag(writer, Constants.NAMESPACE_CMIS, "direct",
        // acl.getAces().get(0).)

        writer.endTag(Constants.NAMESPACE_CMIS, "permission");

        writer.endTag(Constants.NAMESPACE_CMIS, "acl");
        writer.flush();
    }

    // ---- internal ----

    private String getTitle() {
        String result = "";

        List<PropertyData<?>> list = object.getProperties().getPropertyList();
        for (PropertyData<?> propertyData : list) {
            if (PropertyIds.NAME.equals(propertyData.getId()) && (propertyData instanceof PropertyString)) {
                List<String> values = ((PropertyString) propertyData).getValues();
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

    private void writeContent(XmlSerializer writer) throws Exception {
        Base64.InputStream b64stream = new Base64.InputStream(stream, Base64.ENCODE);

        byte[] buffer = new byte[BUFFER_SIZE * 3 / 4];
        int b;
        while ((b = b64stream.read(buffer)) > -1) {
            if (b > 0) {
                writer.text(new String(buffer, 0, b, "US-ASCII"));
            }
        }

        b64stream.close();
    }

    private static void writeObject(XmlSerializer writer, ObjectData object) throws Exception {
        writer.startTag(Constants.NAMESPACE_RESTATOM, Constants.SELECTOR_OBJECT);
        if (object.getProperties() != null) {
            writer.startTag(Constants.NAMESPACE_CMIS, Constants.SELECTOR_PROPERTIES);
            writeProperties(writer, object.getProperties().getPropertyList());

            if (object.getProperties().getExtensions() != null
                    && object.getProperties().getExtensions().isEmpty() == false) {
                writeExtensions(writer, object.getProperties().getExtensions());
            }

            writer.endTag(Constants.NAMESPACE_CMIS, Constants.SELECTOR_PROPERTIES);
        }
        writer.endTag(Constants.NAMESPACE_RESTATOM, Constants.SELECTOR_OBJECT);
    }

    private static void writeTagIfNotNull(XmlSerializer writer, String tagNameSpace, String tagName, String text)
            throws Exception {
        if (text != null) {
            writeTag(writer, tagNameSpace, tagName, text);
        }
    }

    private static void writeTag(XmlSerializer writer, String tagNameSpace, String tagName, String text)
            throws Exception {
        writer.startTag(tagNameSpace, tagName);
        writer.text(text);
        writer.endTag(tagNameSpace, tagName);
    }

    private static void writeProperties(XmlSerializer writer, List<PropertyData<?>> props) throws Exception {
        for (PropertyData<?> propertyData : props) {
            writeProperty(writer, propertyData);
        }
    }

    private static void writeProperty(XmlSerializer writer, PropertyData<?> prop) throws Exception {
        writer.startTag(Constants.NAMESPACE_CMIS, getPropertyTypeTag(prop));
        writer.attribute(null, ATTR_PROPERTY_DEFINITION_ID, prop.getId());
        writeValues(writer, prop.getValues());
        writer.endTag(Constants.NAMESPACE_CMIS, getPropertyTypeTag(prop));
    }

    private static void writeExtensions(XmlSerializer writer, List<CmisExtensionElement> extensions) throws Exception {
        for (CmisExtensionElement cmisExtensionElement : extensions) {
            writer.startTag(cmisExtensionElement.getNamespace(), cmisExtensionElement.getName());
            writeAttributes(writer, cmisExtensionElement.getAttributes());
            if (cmisExtensionElement.getChildren() != null && cmisExtensionElement.getChildren().isEmpty() == false) {
                writeExtensions(writer, cmisExtensionElement.getChildren());
            } else if (cmisExtensionElement.getValue() != null) {
                writer.text(cmisExtensionElement.getValue());
            }
            writer.endTag(cmisExtensionElement.getNamespace(), cmisExtensionElement.getName());
        }
    }

    private static void writeAttributes(XmlSerializer writer, Map<String, String> values) throws Exception {
        for (Map.Entry<String, String> value : values.entrySet()) {
            writer.attribute(null, value.getKey(), value.getValue());
        }
    }

    private static void writeValues(XmlSerializer writer, List<?> values) throws Exception {
        for (Object value : values) {
            writeTag(writer, Constants.NAMESPACE_CMIS, TAG_VALUE, convertPropertyValue(value));
        }
    }

    private static String convertPropertyValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof GregorianCalendar) {
            return dateFormatter.format(((GregorianCalendar) value).getTime());
        }

        return value.toString();
    }

    // Not sure its the right way
    private static String getPropertyTypeTag(PropertyData<?> prop) {
        if (prop instanceof PropertyString) {
            return AtomPropertyType.STRING.value();
        } else if (prop instanceof PropertyId) {
            return AtomPropertyType.ID.value();
        } else if (prop instanceof PropertyBoolean) {
            return AtomPropertyType.BOOLEAN.value();
        } else if (prop instanceof PropertyInteger) {
            return AtomPropertyType.INTEGER.value();
        } else if (prop instanceof PropertyDecimal) {
            return AtomPropertyType.DECIMAL.value();
        } else if (prop instanceof PropertyDateTime) {
            return AtomPropertyType.DATETIME.value();
        } else if (prop instanceof PropertyHtml) {
            return AtomPropertyType.HTML.value();
        } else if (prop instanceof PropertyUri) {
            return AtomPropertyType.URI.value();
        } else {
            return null;
        }
    }

}
