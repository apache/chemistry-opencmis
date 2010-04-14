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

import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.opencmis.client.provider.spi.atompub.objects.Acl;
import org.apache.opencmis.client.provider.spi.atompub.objects.AllowableActions;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomBase;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomElement;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomEntry;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomFeed;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomLink;
import org.apache.opencmis.client.provider.spi.atompub.objects.RepositoryWorkspace;
import org.apache.opencmis.client.provider.spi.atompub.objects.ServiceDoc;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.JaxBHelper;
import org.apache.opencmis.commons.impl.jaxb.CmisAccessControlListType;
import org.apache.opencmis.commons.impl.jaxb.CmisAllowableActionsType;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.impl.jaxb.CmisProperty;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyId;
import org.apache.opencmis.commons.impl.jaxb.CmisRepositoryInfoType;
import org.apache.opencmis.commons.impl.jaxb.CmisTypeDefinitionType;
import org.apache.opencmis.commons.impl.jaxb.EnumPropertiesBase;

/**
 * AtomPub Parser.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AtomPubParser implements CmisAtomPubConstants {

  // public constants
  public static final String LINK_REL_CONTENT = "@@content@@";

  private InputStream fStream;
  private AtomBase fParseResult;

  public AtomPubParser(InputStream stream) {
    if (stream == null) {
      throw new IllegalArgumentException("No stream.");
    }

    fStream = stream;
  }

  /**
   * Parses the stream.
   */
  public void parse() throws Exception {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    XMLStreamReader parser = factory.createXMLStreamReader(fStream);

    while (true) {
      int event = parser.getEventType();
      if (event == XMLStreamReader.START_ELEMENT) {
        QName name = parser.getName();

        if (Constants.NAMESPACE_ATOM.equals(name.getNamespaceURI())) {
          if (TAG_FEED.equals(name.getLocalPart())) {
            fParseResult = parseFeed(parser);
            break;
          }
          else if (TAG_ENTRY.equals(name.getLocalPart())) {
            fParseResult = parseEntry(parser);
            break;
          }
        }
        else if (Constants.NAMESPACE_CMIS.equals(name.getNamespaceURI())) {
          if (TAG_ALLOWABLEACTIONS.equals(name.getLocalPart())) {
            fParseResult = parseAllowableActions(parser);
            break;
          }
          else if (TAG_ACL.equals(name.getLocalPart())) {
            fParseResult = parseACL(parser);
            break;
          }
        }
        else if (Constants.NAMESPACE_APP.equals(name.getNamespaceURI())) {
          if (TAG_SERVICE.equals(name.getLocalPart())) {
            fParseResult = parseServiceDoc(parser);
            break;
          }
        }
      }

      if (!next(parser)) {
        break;
      }
    }

    parser.close();
  }

  /**
   * Return the parse results.
   */
  public AtomBase getResults() {
    return fParseResult;
  }

  /**
   * Parses a service document.
   */
  private ServiceDoc parseServiceDoc(XMLStreamReader parser) throws Exception {
    ServiceDoc result = new ServiceDoc();

    next(parser);

    while (true) {
      int event = parser.getEventType();
      if (event == XMLStreamReader.START_ELEMENT) {
        QName name = parser.getName();

        if (Constants.NAMESPACE_APP.equals(name.getNamespaceURI())) {
          if (TAG_WORKSPACE.equals(name.getLocalPart())) {
            result.addWorkspace(parseWorkspace(parser));
          }
          else {
            skip(parser);
          }
        }
        else {
          skip(parser);
        }
      }
      else if (event == XMLStreamReader.END_ELEMENT) {
        break;
      }
      else {
        if (!next(parser)) {
          break;
        }
      }
    }

    return result;
  }

  /**
   * Parses a workspace element in a service document.
   */
  private RepositoryWorkspace parseWorkspace(XMLStreamReader parser) throws Exception {
    RepositoryWorkspace workspace = new RepositoryWorkspace();

    next(parser);

    while (true) {
      int event = parser.getEventType();
      if (event == XMLStreamReader.START_ELEMENT) {
        AtomElement element = parseWorkspaceElement(parser);

        // check if we can extract the workspace id
        if ((element != null) && (element.getObject() instanceof CmisRepositoryInfoType)) {
          workspace.setId(((CmisRepositoryInfoType) element.getObject()).getRepositoryId());
        }

        // add to workspace
        workspace.addElement(element);
      }
      else if (event == XMLStreamReader.END_ELEMENT) {
        break;
      }
      else {
        if (!next(parser)) {
          break;
        }
      }
    }

    next(parser);

    return workspace;
  }

  /**
   * Parses an Atom feed.
   */
  private AtomFeed parseFeed(XMLStreamReader parser) throws Exception {
    AtomFeed result = new AtomFeed();

    next(parser);

    while (true) {
      int event = parser.getEventType();
      if (event == XMLStreamReader.START_ELEMENT) {
        QName name = parser.getName();

        if (Constants.NAMESPACE_ATOM.equals(name.getNamespaceURI())) {
          if (TAG_LINK.equals(name.getLocalPart())) {
            result.addElement(parseLink(parser));
          }
          else if (TAG_ENTRY.equals(name.getLocalPart())) {
            result.addEntry(parseEntry(parser));
          }
          else {
            skip(parser);
          }
        }
        else if (Constants.NAMESPACE_RESTATOM.equals(name.getNamespaceURI())) {
          if (TAG_NUM_ITEMS.equals(name.getLocalPart())) {
            result.addElement(parseBigInteger(parser));
          }
          else {
            skip(parser);
          }
        }
        else {
          skip(parser);
        }
      }
      else if (event == XMLStreamReader.END_ELEMENT) {
        break;
      }
      else {
        if (!next(parser)) {
          break;
        }
      }
    }

    next(parser);

    return result;
  }

  /**
   * Parses an Atom entry.
   */
  private AtomEntry parseEntry(XMLStreamReader parser) throws Exception {
    AtomEntry result = new AtomEntry();

    next(parser);

    // walk through all tags in entry
    while (true) {
      int event = parser.getEventType();
      if (event == XMLStreamReader.START_ELEMENT) {
        AtomElement element = parseElement(parser);
        if (element != null) {
          // add to entry
          result.addElement(element);

          // find and set object id
          if (element.getObject() instanceof CmisObjectType) {
            for (CmisProperty prop : ((CmisObjectType) element.getObject()).getProperties()
                .getProperty()) {
              if (EnumPropertiesBase.CMIS_OBJECT_ID.value().equals(prop.getPropertyDefinitionId())) {
                result.setId(((CmisPropertyId) prop).getValue().get(0));
              }
            }
          }
          else if (element.getObject() instanceof CmisTypeDefinitionType) {
            result.setId(((CmisTypeDefinitionType) element.getObject()).getId());
          }
        }
      }
      else if (event == XMLStreamReader.END_ELEMENT) {
        break;
      }
      else {
        if (!next(parser)) {
          break;
        }
      }
    }

    next(parser);

    return result;
  }

  /**
   * Parses an Allowable Actions document.
   */
  private AllowableActions parseAllowableActions(XMLStreamReader parser) throws Exception {
    AtomElement elemenet = unmarshalElement(parser, CmisAllowableActionsType.class);
    return new AllowableActions((CmisAllowableActionsType) elemenet.getObject());
  }

  /**
   * Parses an ACL document.
   */
  private Acl parseACL(XMLStreamReader parser) throws Exception {
    AtomElement elemenet = unmarshalElement(parser, CmisAccessControlListType.class);
    return new Acl((CmisAccessControlListType) elemenet.getObject());
  }

  /**
   * Parses an element.
   */
  private AtomElement parseElement(XMLStreamReader parser) throws Exception {
    QName name = parser.getName();

    if (Constants.NAMESPACE_RESTATOM.equals(name.getNamespaceURI())) {
      if (TAG_OBJECT.equals(name.getLocalPart())) {
        return unmarshalElement(parser, CmisObjectType.class);
      }
      else if (TAG_PATH_SEGMENT.equals(name.getLocalPart())
          || TAG_RELATIVE_PATH_SEGMENT.equals(name.getLocalPart())) {
        return parseText(parser);
      }
      else if (TAG_TYPE.equals(name.getLocalPart())) {
        return unmarshalElement(parser, CmisTypeDefinitionType.class);
      }
      else if (TAG_CHILDREN.equals(name.getLocalPart())) {
        return parseChildren(parser);
      }
    }
    else if (Constants.NAMESPACE_ATOM.equals(name.getNamespaceURI())) {
      if (TAG_LINK.equals(name.getLocalPart())) {
        return parseLink(parser);
      }
      else if (TAG_CONTENT.equals(name.getLocalPart())) {
        return parseAtomContentSrc(parser);
      }
    }

    // we don't know it - skip it
    skip(parser);

    return null;
  }

  /**
   * Unmarshals a JAXB element.
   */
  private <T> AtomElement unmarshalElement(XMLStreamReader parser, Class<T> cmisType)
      throws Exception {
    QName name = parser.getName();

    Unmarshaller u = JaxBHelper.createUnmarshaller();
    JAXBElement<T> object = u.unmarshal(parser, cmisType);

    return new AtomElement(name, object.getValue());
  }

  /**
   * Parses a children element.
   */
  private AtomElement parseChildren(XMLStreamReader parser) throws Exception {
    AtomElement result = null;
    QName childName = parser.getName();

    next(parser);

    // walk through the children tag
    while (true) {
      int event = parser.getEventType();
      if (event == XMLStreamReader.START_ELEMENT) {
        QName name = parser.getName();

        if (Constants.NAMESPACE_ATOM.equals(name.getNamespaceURI())) {
          if (TAG_FEED.equals(name.getLocalPart())) {
            result = new AtomElement(childName, parseFeed(parser));
          }
          else {
            skip(parser);
          }
        }
        else {
          skip(parser);
        }
      }
      else if (event == XMLStreamReader.END_ELEMENT) {
        break;
      }
      else {
        if (!next(parser)) {
          break;
        }
      }
    }

    next(parser);

    return result;
  }

  /**
   * Parses a workspace element.
   */
  private AtomElement parseWorkspaceElement(XMLStreamReader parser) throws Exception {
    QName name = parser.getName();

    if (Constants.NAMESPACE_RESTATOM.equals(name.getNamespaceURI())) {
      if (TAG_REPOSITORY_INFO.equals(name.getLocalPart())) {
        return unmarshalElement(parser, CmisRepositoryInfoType.class);
      }
      else if (TAG_URI_TEMPLATE.equals(name.getLocalPart())) {
        return parseTemplate(parser);
      }
    }
    else if (Constants.NAMESPACE_ATOM.equals(name.getNamespaceURI())) {
      if (TAG_LINK.equals(name.getLocalPart())) {
        return parseLink(parser);
      }
    }
    else if (Constants.NAMESPACE_APP.equals(name.getNamespaceURI())) {
      if (TAG_COLLECTION.equals(name.getLocalPart())) {
        return parseCollection(parser);
      }
    }

    // we don't know it - skip it
    skip(parser);

    return null;
  }

  /**
   * Parses a collection tag.
   */
  private AtomElement parseCollection(XMLStreamReader parser) throws Exception {
    QName name = parser.getName();
    Map<String, String> result = new HashMap<String, String>();

    result.put("href", parser.getAttributeValue(null, "href"));

    next(parser);

    while (true) {
      int event = parser.getEventType();
      if (event == XMLStreamReader.START_ELEMENT) {
        QName tagName = parser.getName();
        if (Constants.NAMESPACE_RESTATOM.equals(tagName.getNamespaceURI())
            && TAG_COLLECTION_TYPE.equals(tagName.getLocalPart())) {
          result.put("collectionType", readText(parser));
        }
        else {
          skip(parser);
        }
      }
      else if (event == XMLStreamReader.END_ELEMENT) {
        break;
      }
      else {
        if (!next(parser)) {
          break;
        }
      }
    }

    next(parser);

    return new AtomElement(name, result);
  }

  /**
   * Parses a template tag.
   */
  private AtomElement parseTemplate(XMLStreamReader parser) throws Exception {
    QName name = parser.getName();
    Map<String, String> result = new HashMap<String, String>();

    next(parser);

    while (true) {
      int event = parser.getEventType();
      if (event == XMLStreamReader.START_ELEMENT) {
        QName tagName = parser.getName();
        if (Constants.NAMESPACE_RESTATOM.equals(tagName.getNamespaceURI())) {
          if (TAG_TEMPLATE_TEMPLATE.equals(tagName.getLocalPart())) {
            result.put("template", readText(parser));
          }
          else if (TAG_TEMPLATE_TYPE.equals(tagName.getLocalPart())) {
            result.put("type", readText(parser));
          }
          else {
            skip(parser);
          }
        }
        else {
          skip(parser);
        }
      }
      else if (event == XMLStreamReader.END_ELEMENT) {
        break;
      }
      else {
        if (!next(parser)) {
          break;
        }
      }
    }

    next(parser);

    return new AtomElement(name, result);
  }

  /**
   * Parses a link tag.
   */
  private AtomElement parseLink(XMLStreamReader parser) throws Exception {
    QName name = parser.getName();
    AtomLink result = new AtomLink();

    // save attributes
    for (int i = 0; i < parser.getAttributeCount(); i++) {
      if (LINK_REL.equals(parser.getAttributeLocalName(i))) {
        result.setRel(parser.getAttributeValue(i));
      }
      else if (LINK_HREF.equals(parser.getAttributeLocalName(i))) {
        result.setHref(parser.getAttributeValue(i));
      }
      else if (LINK_TYPE.equals(parser.getAttributeLocalName(i))) {
        result.setType(parser.getAttributeValue(i));
      }
    }

    // skip enclosed tags, if any
    skip(parser);

    return new AtomElement(name, result);
  }

  /**
   * Parses a link tag.
   */
  private AtomElement parseAtomContentSrc(XMLStreamReader parser) throws Exception {
    QName name = parser.getName();
    AtomLink result = new AtomLink();
    result.setRel(LINK_REL_CONTENT);

    // save attributes
    for (int i = 0; i < parser.getAttributeCount(); i++) {
      if (CONTENT_SRC.equals(parser.getAttributeLocalName(i))) {
        result.setHref(parser.getAttributeValue(i));
      }
    }

    // skip enclosed tags, if any
    skip(parser);

    return new AtomElement(name, result);
  }

  /**
   * Parses a text tag.
   */
  private AtomElement parseText(XMLStreamReader parser) throws Exception {
    QName name = parser.getName();
    return new AtomElement(name, readText(parser));
  }

  /**
   * Parses a text tag and convert it into an integer.
   */
  private AtomElement parseBigInteger(XMLStreamReader parser) throws Exception {
    QName name = parser.getName();
    return new AtomElement(name, new BigInteger(readText(parser)));
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
      }
      else if (event == XMLStreamReader.CHARACTERS) {
        String s = parser.getText();
        if (s != null) {
          sb.append(s);
        }
      }
      else if (event == XMLStreamReader.START_ELEMENT) {
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
   * Skips a tag or subtree.
   */
  private void skip(XMLStreamReader parser) throws Exception {
    int level = 1;
    while (next(parser)) {
      int event = parser.getEventType();
      if (event == XMLStreamReader.START_ELEMENT) {
        level++;
      }
      else if (event == XMLStreamReader.END_ELEMENT) {
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
      parser.next();
      return true;
    }

    return false;
  }
}
