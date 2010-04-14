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

import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.opencmis.commons.impl.Constants;

/**
 * Base class for XML documents.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public abstract class XMLDocumentBase {

  public static final String PREFIX_ATOM = "atom";
  public static final String PREFIX_CMIS = "cmis";
  public static final String PREFIX_RESTATOM = "cmisra";
  public static final String PREFIX_APP = "app";
  public static final String PREFIX_XSI = "xsi";

  private XMLStreamWriter fWriter;

  /**
   * Sets the namespaces for the document.
   */
  public void setNamespaces() throws XMLStreamException {
    fWriter.setPrefix(PREFIX_ATOM, Constants.NAMESPACE_ATOM);
    fWriter.setPrefix(PREFIX_CMIS, Constants.NAMESPACE_CMIS);
    fWriter.setPrefix(PREFIX_RESTATOM, Constants.NAMESPACE_RESTATOM);
    fWriter.setPrefix(PREFIX_APP, Constants.NAMESPACE_APP);
    fWriter.setPrefix(PREFIX_XSI, Constants.NAMESPACE_XSI);
  }

  /**
   * Writes the namespace declaration of the given URI to the current tag.
   */
  public void writeNamespace(String namespaceUri) throws XMLStreamException {
    fWriter.writeNamespace(fWriter.getPrefix(namespaceUri), namespaceUri);
  }

  /**
   * Starts the document and sets the namespaces.
   */
  public void startDocument(OutputStream out) throws XMLStreamException {
    // create a writer
    XMLOutputFactory factory = XMLOutputFactory.newInstance();
    fWriter = factory.createXMLStreamWriter(out);

    // start the document
    fWriter.writeStartDocument();
    setNamespaces();
  }

  /**
   * Finishes the document.
   */
  public void endDocument() throws XMLStreamException {
    if (fWriter == null) {
      return;
    }

    // end the document
    fWriter.writeEndDocument();

    // we are done.
    fWriter.close();
  }

  /**
   * Returns the writer object.
   */
  public XMLStreamWriter getWriter() {
    return fWriter;
  }

  /**
   * Sets the writer object.
   */
  protected void setWriter(XMLStreamWriter writer) {
    fWriter = writer;
  }
}
