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

import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_DOCUMENT_TYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_FOLDER_TYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_POLICY_TYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_RELATIONSHIP_TYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.CONTENT_SRC;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.LINK_HREF;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.LINK_REL;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.LINK_TYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ACL;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ALLOWABLEACTIONS;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CHILDREN;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_COLLECTION;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_COLLECTION_TYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CONTENT;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ENTRY;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_FEED;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_HTML;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_LINK;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_NUM_ITEMS;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_OBJECT;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_PATH_SEGMENT;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_RELATIVE_PATH_SEGMENT;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPOSITORY_INFO;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_SERVICE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_TEMPLATE_TEMPLATE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_TEMPLATE_TYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_TYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_URI_TEMPLATE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_WORKSPACE;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomAcl;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomAllowableActions;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomBase;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomElement;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomEntry;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomFeed;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomLink;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.HtmlDoc;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.RepositoryWorkspace;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.ServiceDoc;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.JaxBHelper;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisAccessControlListType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisAllowableActionsType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisProperty;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisPropertyId;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisRepositoryInfoType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeDefinitionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeDocumentDefinitionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeFolderDefinitionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypePolicyDefinitionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeRelationshipDefinitionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumPropertiesBase;

/**
 * AtomPub Parser.
 */
public class AtomPubParser {

    // public constants
    public static final String LINK_REL_CONTENT = "@@content@@";

    private final InputStream stream;
    private AtomBase parseResult;

    public AtomPubParser(InputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("No stream.");
        }

        this.stream = stream;
    }

    /**
     * Parses the stream.
     */
    public void parse() throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader parser = factory.createXMLStreamReader(stream);

        try {
            while (true) {
                int event = parser.getEventType();
                if (event == XMLStreamReader.START_ELEMENT) {
                    QName name = parser.getName();

                    if (Constants.NAMESPACE_ATOM.equals(name.getNamespaceURI())) {
                        if (TAG_FEED.equals(name.getLocalPart())) {
                            parseResult = parseFeed(parser);
                            break;
                        } else if (TAG_ENTRY.equals(name.getLocalPart())) {
                            parseResult = parseEntry(parser);
                            break;
                        }
                    } else if (Constants.NAMESPACE_CMIS.equals(name.getNamespaceURI())) {
                        if (TAG_ALLOWABLEACTIONS.equals(name.getLocalPart())) {
                            parseResult = parseAllowableActions(parser);
                            break;
                        } else if (TAG_ACL.equals(name.getLocalPart())) {
                            parseResult = parseACL(parser);
                            break;
                        }
                    } else if (Constants.NAMESPACE_APP.equals(name.getNamespaceURI())) {
                        if (TAG_SERVICE.equals(name.getLocalPart())) {
                            parseResult = parseServiceDoc(parser);
                            break;
                        }
                    } else if (TAG_HTML.equalsIgnoreCase(name.getLocalPart())) {
                        parseResult = new HtmlDoc();
                        break;
                    }
                }

                if (!next(parser)) {
                    break;
                }
            }

            parser.close();
        } finally {
            // make sure the stream is read and closed in all cases
            try {
                byte[] buffer = new byte[4096];
                while (stream.read(buffer) > -1) {
                }
            } catch (Exception e) {
            }

            try {
                stream.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Return the parse results.
     */
    public AtomBase getResults() {
        return parseResult;
    }

    /**
     * Parses a service document.
     */
    private static ServiceDoc parseServiceDoc(XMLStreamReader parser) throws Exception {
        ServiceDoc result = new ServiceDoc();

        next(parser);

        while (true) {
            int event = parser.getEventType();
            if (event == XMLStreamReader.START_ELEMENT) {
                QName name = parser.getName();

                if (Constants.NAMESPACE_APP.equals(name.getNamespaceURI())) {
                    if (TAG_WORKSPACE.equals(name.getLocalPart())) {
                        result.addWorkspace(parseWorkspace(parser));
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

        return result;
    }

    /**
     * Parses a workspace element in a service document.
     */
    private static RepositoryWorkspace parseWorkspace(XMLStreamReader parser) throws Exception {
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
            } else if (event == XMLStreamReader.END_ELEMENT) {
                break;
            } else {
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
                    } else if (TAG_ENTRY.equals(name.getLocalPart())) {
                        result.addEntry(parseEntry(parser));
                    } else {
                        skip(parser);
                    }
                } else if (Constants.NAMESPACE_RESTATOM.equals(name.getNamespaceURI())) {
                    if (TAG_NUM_ITEMS.equals(name.getLocalPart())) {
                        result.addElement(parseBigInteger(parser));
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
                        for (CmisProperty prop : ((CmisObjectType) element.getObject()).getProperties().getProperty()) {
                            if (EnumPropertiesBase.CMIS_OBJECT_ID.value().equals(prop.getPropertyDefinitionId())) {
                                result.setId(((CmisPropertyId) prop).getValue().get(0));
                            }
                        }
                    } else if (element.getObject() instanceof CmisTypeDefinitionType) {
                        result.setId(((CmisTypeDefinitionType) element.getObject()).getId());
                    }
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

        return result;
    }

    /**
     * Parses an Allowable Actions document.
     */
    private static AtomAllowableActions parseAllowableActions(XMLStreamReader parser) throws Exception {
        AtomElement elemenet = unmarshalElement(parser, CmisAllowableActionsType.class);
        return new AtomAllowableActions((CmisAllowableActionsType) elemenet.getObject());
    }

    /**
     * Parses an ACL document.
     */
    private static AtomAcl parseACL(XMLStreamReader parser) throws Exception {
        AtomElement elemenet = unmarshalElement(parser, CmisAccessControlListType.class);
        return new AtomAcl((CmisAccessControlListType) elemenet.getObject());
    }

    /**
     * Parses an element.
     */
    private AtomElement parseElement(XMLStreamReader parser) throws Exception {
        QName name = parser.getName();

        if (Constants.NAMESPACE_RESTATOM.equals(name.getNamespaceURI())) {
            if (TAG_OBJECT.equals(name.getLocalPart())) {
                return unmarshalElement(parser, CmisObjectType.class);
            } else if (TAG_PATH_SEGMENT.equals(name.getLocalPart())
                    || TAG_RELATIVE_PATH_SEGMENT.equals(name.getLocalPart())) {
                return parseText(parser);
            } else if (TAG_TYPE.equals(name.getLocalPart())) {
                // workaround for old Chemistry code - ignore the type namespace
                String typeAttr = parser.getAttributeValue(Constants.NAMESPACE_XSI, "type");
                if (typeAttr == null) {
                    return unmarshalElement(parser, CmisTypeDefinitionType.class);
                } else if (typeAttr.endsWith(ATTR_DOCUMENT_TYPE)) {
                    return unmarshalElement(parser, CmisTypeDocumentDefinitionType.class);
                } else if (typeAttr.endsWith(ATTR_FOLDER_TYPE)) {
                    return unmarshalElement(parser, CmisTypeFolderDefinitionType.class);
                } else if (typeAttr.endsWith(ATTR_RELATIONSHIP_TYPE)) {
                    return unmarshalElement(parser, CmisTypeRelationshipDefinitionType.class);
                } else if (typeAttr.endsWith(ATTR_POLICY_TYPE)) {
                    return unmarshalElement(parser, CmisTypePolicyDefinitionType.class);
                }
                throw new CmisRuntimeException("Cannot read type definition!");
            } else if (TAG_CHILDREN.equals(name.getLocalPart())) {
                return parseChildren(parser);
            }
        } else if (Constants.NAMESPACE_ATOM.equals(name.getNamespaceURI())) {
            if (TAG_LINK.equals(name.getLocalPart())) {
                return parseLink(parser);
            } else if (TAG_CONTENT.equals(name.getLocalPart())) {
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
    private static <T> AtomElement unmarshalElement(XMLStreamReader parser, Class<T> cmisType) throws Exception {
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

        return result;
    }

    /**
     * Parses a workspace element.
     */
    private static AtomElement parseWorkspaceElement(XMLStreamReader parser) throws Exception {
        QName name = parser.getName();

        if (Constants.NAMESPACE_RESTATOM.equals(name.getNamespaceURI())) {
            if (TAG_REPOSITORY_INFO.equals(name.getLocalPart())) {
                return unmarshalElement(parser, CmisRepositoryInfoType.class);
            } else if (TAG_URI_TEMPLATE.equals(name.getLocalPart())) {
                return parseTemplate(parser);
            }
        } else if (Constants.NAMESPACE_ATOM.equals(name.getNamespaceURI())) {
            if (TAG_LINK.equals(name.getLocalPart())) {
                return parseLink(parser);
            }
        } else if (Constants.NAMESPACE_APP.equals(name.getNamespaceURI())) {
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
    private static AtomElement parseCollection(XMLStreamReader parser) throws Exception {
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

        return new AtomElement(name, result);
    }

    /**
     * Parses a template tag.
     */
    private static AtomElement parseTemplate(XMLStreamReader parser) throws Exception {
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
                    } else if (TAG_TEMPLATE_TYPE.equals(tagName.getLocalPart())) {
                        result.put("type", readText(parser));
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

        return new AtomElement(name, result);
    }

    /**
     * Parses a link tag.
     */
    private static AtomElement parseLink(XMLStreamReader parser) throws Exception {
        QName name = parser.getName();
        AtomLink result = new AtomLink();

        // save attributes
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (LINK_REL.equals(parser.getAttributeLocalName(i))) {
                result.setRel(parser.getAttributeValue(i));
            } else if (LINK_HREF.equals(parser.getAttributeLocalName(i))) {
                result.setHref(parser.getAttributeValue(i));
            } else if (LINK_TYPE.equals(parser.getAttributeLocalName(i))) {
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
    private static AtomElement parseAtomContentSrc(XMLStreamReader parser) throws Exception {
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
    private static AtomElement parseText(XMLStreamReader parser) throws Exception {
        QName name = parser.getName();
        return new AtomElement(name, readText(parser));
    }

    /**
     * Parses a text tag and convert it into an integer.
     */
    private static AtomElement parseBigInteger(XMLStreamReader parser) throws Exception {
        QName name = parser.getName();
        return new AtomElement(name, new BigInteger(readText(parser)));
    }

    /**
     * Parses a tag that contains text.
     */
    private static String readText(XMLStreamReader parser) throws Exception {
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
     * Skips a tag or subtree.
     */
    private static void skip(XMLStreamReader parser) throws Exception {
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

    private static boolean next(XMLStreamReader parser) throws Exception {
        if (parser.hasNext()) {
            parser.next();
            return true;
        }

        return false;
    }
}
