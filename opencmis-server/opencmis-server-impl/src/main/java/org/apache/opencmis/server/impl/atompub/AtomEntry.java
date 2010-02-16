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
package org.apache.opencmis.server.impl.atompub;

import static org.apache.opencmis.commons.impl.Converter.convert;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.JaxBHelper;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.impl.jaxb.CmisTypeDefinitionType;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.server.spi.ObjectInfo;

/**
 * Atom Entry class.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AtomEntry extends AtomDocumentBase {

  private final static String DEFAULT_AUTHOR = "unknown";

  /**
   * Creates an Atom entry document.
   */
  public AtomEntry() {
  }

  /**
   * Creates an Atom entry that is embedded somewhere.
   */
  public AtomEntry(XMLStreamWriter writer) {
    setWriter(writer);
  }

  /**
   * Opens the entry tag.
   */
  public void startEntry(boolean isRoot) throws XMLStreamException {
    getWriter().writeStartElement(Constants.NAMESPACE_ATOM, "entry");

    if (isRoot) {
      writeNamespace(Constants.NAMESPACE_ATOM);
      writeNamespace(Constants.NAMESPACE_CMIS);
      writeNamespace(Constants.NAMESPACE_RESTATOM);
    }
  }

  /**
   * Closes the entry tag.
   */
  public void endEntry() throws XMLStreamException {
    getWriter().writeEndElement();
  }

  /**
   * Writes an object.
   */
  public void writeObject(ObjectData object, ObjectInfo info, String contentSrc,
      String contentType, String pathSegment, String relativePathSegment)
      throws XMLStreamException, JAXBException {
    CmisObjectType objectJaxb = convert(object);
    if (objectJaxb == null) {
      return;
    }

    writeAuthor(info.getCreatedBy());
    writeId(generateAtomId(info.getId()));
    writePublished(info.getCreationDate());
    writeTitle(info.getName());
    writeUpdated(info.getLastModificationDate());

    writeContent(contentSrc, contentType);

    JaxBHelper.marshal(JaxBHelper.CMIS_EXTRA_OBJECT_FACTORY.createObject(objectJaxb), getWriter(),
        true);

    writePathSegment(pathSegment);
    writeRelativePathSegment(relativePathSegment);
  }

  /**
   * Writes a type.
   * 
   * @throws JAXBException
   */
  public void writeType(TypeDefinition type) throws XMLStreamException, JAXBException {
    CmisTypeDefinitionType typeJaxb = convert(type);
    if (typeJaxb == null) {
      return;
    }

    long now = System.currentTimeMillis();

    writeAuthor(DEFAULT_AUTHOR);
    writeId(generateAtomId(type.getId()));
    writeTitle(type.getDisplayName());
    writeUpdated(now);

    JaxBHelper.marshal(JaxBHelper.CMIS_EXTRA_OBJECT_FACTORY.createTypeDefinition(typeJaxb),
        getWriter(), true);
  }

  /**
   * Writes a content tag.
   */
  public void writeContent(String src, String type) throws XMLStreamException {
    if (src == null) {
      return;
    }

    XMLStreamWriter xsw = getWriter();
    xsw.writeStartElement(Constants.NAMESPACE_ATOM, "content");

    xsw.writeAttribute("src", src);
    if (type != null) {
      xsw.writeAttribute("type", type);
    }

    xsw.writeEndElement();
  }
}
