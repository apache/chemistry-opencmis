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
package org.apache.opencmis.client.provider.spi.atompub;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.codec.binary.Base64;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.JaxBHelper;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.impl.jaxb.CmisProperty;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyString;

/**
 * Writes a CMIS Atom entry to an output stream.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AtomEntryWriter implements CmisAtomPubConstants {

  private static final String PREFIX_ATOM = "atom";
  private static final String PREFIX_CMIS = "cmis";
  private static final String PREFIX_RESTATOM = "cmisra";

  private static final int BUFFER_SIZE = 4096;

  private CmisObjectType fObject;
  private InputStream fStream;
  private String fMediaType;

  /**
   * Constructor.
   */
  public AtomEntryWriter(CmisObjectType object) {
    this(object, null, null);
  }

  /**
   * Constructor.
   */
  public AtomEntryWriter(CmisObjectType object, String mediaType, InputStream stream) {
    if ((object == null) || (object.getProperties() == null)) {
      throw new CmisInvalidArgumentException("Object and properties must not be null!");
    }

    if ((stream != null) && (mediaType == null)) {
      throw new CmisInvalidArgumentException("Media type must be set if a stream is present!");
    }

    fObject = object;
    fMediaType = mediaType;
    fStream = stream;
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

    // start doc
    writer.writeStartDocument();

    // start entry
    writer.writeStartElement(Constants.NAMESPACE_ATOM, TAG_ENTRY);
    writer.writeNamespace(PREFIX_ATOM, Constants.NAMESPACE_ATOM);
    writer.writeNamespace(PREFIX_CMIS, Constants.NAMESPACE_CMIS);
    writer.writeNamespace(PREFIX_RESTATOM, Constants.NAMESPACE_RESTATOM);

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
    if (fStream != null) {
      writer.writeStartElement(Constants.NAMESPACE_RESTATOM, TAG_CONTENT);

      writer.writeStartElement(Constants.NAMESPACE_RESTATOM, TAG_CONTENT_MEDIATYPE);
      writer.writeCharacters(fMediaType);
      writer.writeEndElement();

      writer.writeStartElement(Constants.NAMESPACE_RESTATOM, TAG_CONTENT_BASE64);
      writer.writeCharacters(getContent());
      writer.writeEndElement();

      writer.writeEndElement();
    }

    // object
    JaxBHelper.marshal(JaxBHelper.CMIS_EXTRA_OBJECT_FACTORY.createObject(fObject), writer, true);

    // end entry
    writer.writeEndElement();

    // end document
    writer.writeEndDocument();

    writer.flush();
  }

  // ---- internal ----

  private String getTitle() {
    String result = "";

    for (CmisProperty property : fObject.getProperties().getProperty()) {
      if (PropertyIds.CMIS_NAME.equals(property.getPropertyDefinitionId())
          && (property instanceof CmisPropertyString)) {
        List<String> values = ((CmisPropertyString) property).getValue();
        if (!values.isEmpty()) {
          return values.get(0);
        }
      }
    }

    return result;
  }

  private String getUpdated() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    return sdf.format(new Date());
  }

  private String getContent() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    byte[] buffer = new byte[BUFFER_SIZE];
    int b;
    while ((b = fStream.read(buffer)) > -1) {
      baos.write(buffer, 0, b);
    }

    return new String(Base64.encodeBase64Chunked(baos.toByteArray()), "UTF-8");
  }
}
