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
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_DEFINITION_ID;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_DISPLAYNAME;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_LOCALNAME;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_QUERYNAME;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_PROPERTY_VALUE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.ATTR_RELATIONSHIP_TYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.CONTENT_SRC;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.LINK_HREF;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.LINK_REL;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.LINK_TYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ACE_DIRECT;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ACE_PRINCIPAL;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ACE_PRINCIPAL_ID;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ACL;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ACLCAP_MAPPING;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ACLCAP_MAPPING_KEY;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ACLCAP_MAPPING_PERMISSION;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ACLCAP_PERMISSIONS;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ACLCAP_PERMISSION_DESCRIPTION;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ACLCAP_PERMISSION_PERMISSION;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ALLOWABLEACTIONS;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CHILDREN;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_COLLECTION;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_COLLECTION_TYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_CONTENT;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_ENTRY;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_FEED;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_LINK;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_NUM_ITEMS;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_OBJECT;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_OBJECT_ACL;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_OBJECT_ALLOWABLE_ACTIONS;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_OBJECT_PROPERTIES;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_OBJECT_RENDITION;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_PATH_SEGMENT;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_PROPERTY;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_RELATIVE_PATH_SEGMENT;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_RENDITION;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_ACL_CAPABILITY;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_CAPABILITIES;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPINFO_CHANGES_ON_TYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_REPOSITORY_INFO;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_SERVICE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_TEMPLATE_TEMPLATE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_TEMPLATE_TYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_TYPE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_URI_TEMPLATE;
import static org.apache.chemistry.opencmis.client.bindings.spi.atompub.CmisAtomPubConstants.TAG_WORKSPACE;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomAcl;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomAllowableActions;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomBase;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomElement;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomEntry;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomFeed;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomLink;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.RepositoryWorkspace;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.ServiceDoc;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AclCapabilities;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.AtomPubConverter;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CmisExtensionElementImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * AtomPub Parser.
 */
public class AtomPubParser {

	//private static final Logger log = LoggerFactory.getLogger(AtomPubParser.class);

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
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(stream, null);

		try {
			while (true) {
				int event = parser.getEventType();
				if (event == XmlPullParser.START_TAG) {
					QName name = new QName(parser.getNamespace(), parser.getName());

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
							parseResult = new AtomAllowableActions(parseAllowableActions(parser));
							break;
						} else if (TAG_ACL.equals(name.getLocalPart())) {
							parseResult = new AtomAcl(parseACL(parser));
							break;
						}
					} else if (Constants.NAMESPACE_APP.equals(name.getNamespaceURI())) {
						if (TAG_SERVICE.equals(name.getLocalPart())) {
							parseResult = parseServiceDoc(parser);
							break;
						}
					}
				}

				if (!next(parser)) {
					break;
				}
			}

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
	private static ServiceDoc parseServiceDoc(XmlPullParser parser) throws Exception {
		ServiceDoc result = new ServiceDoc();

		next(parser);

		while (true) {
			int event = parser.getEventType();
			if (event == XmlPullParser.START_TAG) {
				QName name = new QName(parser.getNamespace(), parser.getName());

				if (Constants.NAMESPACE_APP.equals(name.getNamespaceURI())) {
					if (TAG_WORKSPACE.equals(name.getLocalPart())) {
						result.addWorkspace(parseWorkspace(parser));
					} else {
						skip(parser);
					}
				} else {
					skip(parser);
				}
			} else if (event == XmlPullParser.END_TAG) {
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
	private static RepositoryWorkspace parseWorkspace(XmlPullParser parser) throws Exception {
		RepositoryWorkspace workspace = new RepositoryWorkspace();

		next(parser);

		while (true) {
			int event = parser.getEventType();
			if (event == XmlPullParser.START_TAG) {
				AtomElement element = parseWorkspaceElement(parser);

				// check if we can extract the workspace id
				if ((element != null) && (element.getObject() instanceof RepositoryInfo)) {
					workspace.setId(((RepositoryInfo) element.getObject()).getId());
				}

				// add to workspace
				workspace.addElement(element);
			} else if (event == XmlPullParser.END_TAG) {
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
	private AtomFeed parseFeed(XmlPullParser parser) throws Exception {
		AtomFeed result = new AtomFeed();

		next(parser);

		while (true) {
			int event = parser.getEventType();
			if (event == XmlPullParser.START_TAG) {
				QName name = new QName(parser.getNamespace(), parser.getName());

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
			} else if (event == XmlPullParser.END_TAG) {
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
	private AtomEntry parseEntry(XmlPullParser parser) throws Exception {
		AtomEntry result = new AtomEntry();

		next(parser);

		// walk through all tags in entry
		while (true) {
			int event = parser.getEventType();
			if (event == XmlPullParser.START_TAG) {
				AtomElement element = parseElement(parser);
				if (element != null) {
					// add to entry
					result.addElement(element);

					// find and set object id
					if (element.getObject() instanceof ObjectData) {
						result.setId(((ObjectData) element.getObject()).getId());
					} else if (element.getObject() instanceof TypeDefinition) {
						result.setId(((TypeDefinition) element.getObject()).getId());
					}
				}
			} else if (event == XmlPullParser.END_TAG) {
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
	 * Parses an element.
	 */
	private AtomElement parseElement(XmlPullParser parser) throws Exception {
		QName name = new QName(parser.getNamespace(), parser.getName());

		if (Constants.NAMESPACE_RESTATOM.equals(name.getNamespaceURI())) {
			if (TAG_OBJECT.equals(name.getLocalPart())) {
				return new AtomElement(name, parseObject(parser));
			} else if (TAG_PATH_SEGMENT.equals(name.getLocalPart()) || TAG_RELATIVE_PATH_SEGMENT.equals(name.getLocalPart())) {
				return parseText(parser);
			} else if (TAG_TYPE.equals(name.getLocalPart())) {
				// workaround for old Chemistry code - ignore the type namespace
				String typeAttr = parser.getAttributeValue(Constants.NAMESPACE_XSI, "type");
				if (typeAttr == null) {
					return new AtomElement(name, parseTypeDefinition(parser, null));
				} else if (typeAttr.endsWith(ATTR_DOCUMENT_TYPE)) {
					return new AtomElement(name, parseTypeDefinition(parser, BaseTypeId.CMIS_DOCUMENT));
				} else if (typeAttr.endsWith(ATTR_FOLDER_TYPE)) {
					return new AtomElement(name, parseTypeDefinition(parser, BaseTypeId.CMIS_FOLDER));
				} else if (typeAttr.endsWith(ATTR_RELATIONSHIP_TYPE)) {
					return new AtomElement(name, parseTypeDefinition(parser, BaseTypeId.CMIS_RELATIONSHIP));
				} else if (typeAttr.endsWith(ATTR_POLICY_TYPE)) {
					return new AtomElement(name, parseTypeDefinition(parser, BaseTypeId.CMIS_POLICY));
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
	 * Parses a children element.
	 */
	private AtomElement parseChildren(XmlPullParser parser) throws Exception {
		AtomElement result = null;
		QName childName = new QName(parser.getNamespace(), parser.getName());

		next(parser);

		// walk through the children tag
		while (true) {
			int event = parser.getEventType();
			if (event == XmlPullParser.START_TAG) {
				QName name = new QName(parser.getNamespace(), parser.getName());

				if (Constants.NAMESPACE_ATOM.equals(name.getNamespaceURI())) {
					if (TAG_FEED.equals(name.getLocalPart())) {
						result = new AtomElement(childName, parseFeed(parser));
					} else {
						skip(parser);
					}
				} else {
					skip(parser);
				}
			} else if (event == XmlPullParser.END_TAG) {
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
	private static AtomElement parseWorkspaceElement(XmlPullParser parser) throws Exception {
		QName name = new QName(parser.getNamespace(), parser.getName());

		if (Constants.NAMESPACE_RESTATOM.equals(name.getNamespaceURI())) {
			if (TAG_REPOSITORY_INFO.equals(name.getLocalPart())) {
				return new AtomElement(name, parseRepositoryInfo(parser));
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
	private static AtomElement parseCollection(XmlPullParser parser) throws Exception {
		QName name = new QName(parser.getNamespace(), parser.getName());
		Map<String, String> result = new HashMap<String, String>();

		result.put("href", parser.getAttributeValue(null, "href"));

		next(parser);

		while (true) {
			int event = parser.getEventType();
			if (event == XmlPullParser.START_TAG) {
				QName tagName = new QName(parser.getNamespace(), parser.getName());
				if (Constants.NAMESPACE_RESTATOM.equals(tagName.getNamespaceURI()) && TAG_COLLECTION_TYPE.equals(tagName.getLocalPart())) {
					result.put("collectionType", readText(parser));
				} else {
					skip(parser);
				}
			} else if (event == XmlPullParser.END_TAG) {
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
	private static AtomElement parseTemplate(XmlPullParser parser) throws Exception {
		QName name = new QName(parser.getNamespace(), parser.getName());
		Map<String, String> result = new HashMap<String, String>();

		next(parser);

		while (true) {
			int event = parser.getEventType();
			if (event == XmlPullParser.START_TAG) {
				QName tagName = new QName(parser.getNamespace(), parser.getName());
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
			} else if (event == XmlPullParser.END_TAG) {
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
	private static AtomElement parseLink(XmlPullParser parser) throws Exception {
		QName name = new QName(parser.getNamespace(), parser.getName());
		AtomLink result = new AtomLink();

		// save attributes
		for (int i = 0; i < parser.getAttributeCount(); i++) {
			if (LINK_REL.equals(parser.getAttributeName(i))) {
				result.setRel(parser.getAttributeValue(i));
			} else if (LINK_HREF.equals(parser.getAttributeName(i))) {
				result.setHref(parser.getAttributeValue(i));
			} else if (LINK_TYPE.equals(parser.getAttributeName(i))) {
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
	private static AtomElement parseAtomContentSrc(XmlPullParser parser) throws Exception {
		QName name = new QName(parser.getNamespace(), parser.getName());
		AtomLink result = new AtomLink();
		result.setRel(LINK_REL_CONTENT);

		// save attributes
		for (int i = 0; i < parser.getAttributeCount(); i++) {
			if (CONTENT_SRC.equals(parser.getAttributeName(i))) {
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
	private static AtomElement parseText(XmlPullParser parser) throws Exception {
		QName name = new QName(parser.getNamespace(), parser.getName());
		return new AtomElement(name, readText(parser));
	}

	/**
	 * Parses a text tag and convert it into an integer.
	 */
	private static AtomElement parseBigInteger(XmlPullParser parser) throws Exception {
		QName name = new QName(parser.getNamespace(), parser.getName());
		return new AtomElement(name, new BigInteger(readText(parser)));
	}

	/**
	 * Parses a tag that contains text.
	 */
	private static String readText(XmlPullParser parser) throws Exception {
		StringBuilder sb = new StringBuilder();

		next(parser);

		while (true) {
			int event = parser.getEventType();
			if (event == XmlPullParser.END_TAG) {
				break;
			} else if (event == XmlPullParser.TEXT) {
				String s = parser.getText();
				if (s != null) {
					sb.append(s);
				}
			} else if (event == XmlPullParser.START_TAG) {
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
	private static void skip(XmlPullParser parser) throws Exception {
		int level = 1;
		while (next(parser)) {
			int event = parser.getEventType();
			if (event == XmlPullParser.START_TAG) {
				level++;
			} else if (event == XmlPullParser.END_TAG) {
				level--;
				if (level == 0) {
					break;
				}
			}
		}

		next(parser);
	}

	private static boolean next(XmlPullParser parser) throws Exception {
		int event = parser.getEventType();
		if (event == XmlPullParser.END_DOCUMENT) {
			return false;
		} else {
			parser.next();
			return true;
		}
	}

	// -----------------------------------------------------------------------------------
	// ------------- NEW ANDROID METHOD
	// -----------------------------------------------------------------------------------

	private static RepositoryInfo parseRepositoryInfo(XmlPullParser parser) throws Exception {
		//log.debug("[START] parseRepositoryInfo...");
		int eventType = parser.next();
		String name = parser.getName();
		String namespace = parser.getNamespace();
		Map<String, String> repositoryInfoRawValues = new HashMap<String, String>(15);
		Map<String, String> repositoryCapabilitiesRawValues = new HashMap<String, String>(15);
		AclCapabilities aclCapabilities = null;
		List<String> changesOnType = new ArrayList<String>();
		List<CmisExtensionElement> extensions = new ArrayList<CmisExtensionElement>();

		while (!(eventType == XmlPullParser.END_TAG && Constants.SELECTOR_REPOSITORY_INFO.equals(name))) {
			switch (eventType) {
			case XmlPullParser.START_TAG:

				if (!CmisAtomPubConstants.REPINFO_KEYS.contains(name)) {
					extensions.addAll(parseExtensions(parser));
					eventType = parser.next();
					if (XmlPullParser.TEXT == eventType) {
						eventType = parser.next();
					}
					name = parser.getName();
					continue;
				}

				if (TAG_REPINFO_CAPABILITIES.equals(name)) {
					eventType = parser.next();
					name = parser.getName();
					while (!(eventType == XmlPullParser.END_TAG && TAG_REPINFO_CAPABILITIES.equals(name))) {
						switch (eventType) {
						case XmlPullParser.START_TAG:
							parser.next();
							repositoryCapabilitiesRawValues.put(name, parser.getText());
							break;
						}
						eventType = parser.next();
						name = parser.getName();
					}
				} else if (TAG_REPINFO_ACL_CAPABILITY.equals(name)) {
					aclCapabilities = parseAclCapabilities(parser);
				} else if (TAG_REPINFO_CHANGES_ON_TYPE.equals(name)) {
					parser.next();
					changesOnType.add(parser.getText());
				} else if (Constants.NAMESPACE_CMIS.equals(namespace)) {
					parser.next();
					repositoryInfoRawValues.put(name, parser.getText());
				}

				break;

			}
			eventType = parser.next();
			name = parser.getName();
			namespace = parser.getNamespace();
		}

		parser.next();
		// log.debug("[STOP] parseRepositoryInfo...");
		return AtomPubConverter.convertRepositoryInfo(repositoryInfoRawValues, repositoryCapabilitiesRawValues, aclCapabilities, changesOnType, extensions);
	}

	private static TypeDefinition parseTypeDefinition(XmlPullParser parser, BaseTypeId type) throws Exception {
		// log.debug("[START] parseTypeDefinition...");
		Map<String, String> definitionRawValues = new HashMap<String, String>(15);
		List<PropertyDefinition<?>> propertyDefinitionList = new ArrayList<PropertyDefinition<?>>(10);

		int eventType = parser.next();
		String name = parser.getName();
		String namespace = parser.getNamespace();
		List<CmisExtensionElement> extensions = new ArrayList<CmisExtensionElement>();

		while (!(eventType == XmlPullParser.END_TAG && TAG_TEMPLATE_TYPE.equals(name))) {
			switch (eventType) {
			case XmlPullParser.START_TAG:
				if (name.startsWith(TAG_PROPERTY)) {
					propertyDefinitionList.add(parsePropertyDefinition(parser));
				} else if (Constants.NAMESPACE_CMIS.equals(namespace)) {
					parser.next();
					definitionRawValues.put(name, parser.getText());
				} else {
					// Parse Extensions
					extensions.addAll(parseExtensions(parser));
					eventType = parser.next();
					if (XmlPullParser.TEXT == eventType) {
						eventType = parser.next();
					}
					name = parser.getName();
					continue;
				}

				break;

			}
			eventType = parser.next();
			name = parser.getName();
			namespace = parser.getNamespace();
		}

		parser.next();
		// log.debug("[STOP] parseTypeDefinition...");

		return AtomPubConverter.convertTypeDefinition(type, definitionRawValues, propertyDefinitionList, extensions);
	}

	private static PropertyDefinition<?> parsePropertyDefinition(XmlPullParser parser) throws Exception {
		// log.debug("[START] parsePropertyDefinition...");

		String id = null;
		Map<String, String> propertyTypeRawValues = new HashMap<String, String>(15);
		id = parser.getName();
		int eventType = parser.next();
		String name = parser.getName();
		String namespace = parser.getNamespace();
		List<CmisExtensionElement> extensions = new ArrayList<CmisExtensionElement>();

		while (!(eventType == XmlPullParser.END_TAG && id.equals(name))) {
			switch (eventType) {
			case XmlPullParser.START_TAG:
				if (Constants.NAMESPACE_CMIS.equals(namespace)) {
					parser.next();
					propertyTypeRawValues.put(name, parser.getText());
				} else {
					// Parse Extensions
					extensions.addAll(parseExtensions(parser));
					eventType = parser.next();
					if (XmlPullParser.TEXT == eventType) {
						eventType = parser.next();
					}
					name = parser.getName();
					continue;
				}
				break;
			}
			eventType = parser.next();
			name = parser.getName();
			namespace = parser.getNamespace();
		}

		// log.debug("[STOP] parsePropertyDefinition...");
		return AtomPubConverter.convertPropertyDefinition(id, propertyTypeRawValues, extensions);
	}

	private static ObjectData parseObject(XmlPullParser parser) throws Exception {

		// log.debug("[START] parseObject...");

		List<RenditionData> renditions = new ArrayList<RenditionData>(3);
		Properties properties = null;
		AllowableActions allowableActions = null;
		Acl acl = null;
		List<CmisExtensionElement> extensions = new ArrayList<CmisExtensionElement>(2);

		int eventType = parser.next();
		String name = parser.getName();

		while (!(eventType == XmlPullParser.END_TAG && Constants.SELECTOR_OBJECT.equals(name))) {
			switch (eventType) {
			case XmlPullParser.START_TAG:

				// EXTENSIONS
				if (!CmisAtomPubConstants.OBJECT_KEYS.contains(name)) {
					extensions.addAll(parseExtensions(parser));
					eventType = parser.next();
					if (XmlPullParser.TEXT == eventType) {
						eventType = parser.next();
					}
					name = parser.getName();
					continue;
				}

				if (TAG_OBJECT_PROPERTIES.equals(name)) {
					properties = parseProperties(parser);
				} else if (TAG_OBJECT_ALLOWABLE_ACTIONS.equals(name)) {
					allowableActions = parseAllowableActions(parser);
				} else if (TAG_OBJECT_ACL.equals(name)) {
					acl = parseACL(parser);
				} else if (TAG_OBJECT_RENDITION.equals(name)) {
					renditions.add(parseRenditions(parser));
				} else {
					skip(parser);
				}
				break;
			}
			
			
			eventType = parser.getEventType();
			name = parser.getName();
			if(!(eventType == XmlPullParser.END_TAG && Constants.SELECTOR_OBJECT.equals(name))){
				eventType = parser.next();
				name = parser.getName();	
			}
		}

		// Important !
		parser.next();

		// log.debug("[END] parseObject...");
		return AtomPubConverter.convertObject(properties, allowableActions, acl, renditions, extensions);
	}

	/**
	 * Converts properties.
	 * 
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private static Properties parseProperties(XmlPullParser parser) throws Exception {
		// log.debug("[START] parseProperties...");

		if (parser == null) {
			return null;
		}

		int eventType = parser.next();
		String name = parser.getName();
		String nameSpace = parser.getNamespace();
		int countParam = 0;
		String id = null, displayName = null, queryName = null, localName = null, paramName = null, tag = null;
		List<String> values = null;
		List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
		List<CmisExtensionElement> extensions = new ArrayList<CmisExtensionElement>();

		while (!(eventType == XmlPullParser.END_TAG && TAG_OBJECT_PROPERTIES.equals(name))) {
			switch (eventType) {
			case XmlPullParser.START_TAG:

				// Extension
				if (!Constants.NAMESPACE_CMIS.equals(nameSpace)) {
					extensions.addAll(parseExtensions(parser));
					eventType = parser.next();
					if (XmlPullParser.TEXT == eventType) {
						eventType = parser.next();
					}
					name = parser.getName();
					nameSpace = parser.getNamespace();
					continue;
				}

				tag = name;

				// attributes
				countParam = parser.getAttributeCount();
				displayName = null;
				queryName = null;
				localName = null;
				paramName = null;
				for (int i = 0; i < countParam; i++) {
					paramName = parser.getAttributeName(i);
					if (ATTR_PROPERTY_DISPLAYNAME.equals(paramName)) {
						displayName = parser.getAttributeValue(i);
					} else if (ATTR_PROPERTY_QUERYNAME.equals(paramName)) {
						queryName = parser.getAttributeValue(i);
					} else if (ATTR_PROPERTY_LOCALNAME.equals(paramName)) {
						localName = parser.getAttributeValue(i);
					} else if (ATTR_PROPERTY_DEFINITION_ID.equals(paramName)) {
						id = parser.getAttributeValue(i);
					}
				}

				// Values
				values = new ArrayList<String>(2);
				while (!(eventType == XmlPullParser.END_TAG && tag.equals(name))) {
					switch (eventType) {
					case XmlPullParser.START_TAG:
						if (ATTR_PROPERTY_VALUE.equals(name)) {
							parser.next();
							values.add(parser.getText());
						}
						break;
					}
					eventType = parser.next();
					name = parser.getName();
				}

				// Convert
				properties.add(AtomPubConverter.convertProperty(tag, id, displayName, queryName, localName, values));
				break;

			}
			eventType = parser.next();
			name = parser.getName();
			nameSpace = parser.getNamespace();
		}
		// log.debug("[END] parseProperties...");
		return AtomPubConverter.convertProperties(properties, extensions);
	}

	private static Map<String, String> getAttributes(XmlPullParser parser) {
		Map<String, String> attributes = null;
		int attributeCount = parser.getAttributeCount();
		if (parser.getAttributeCount() > 0) {
			attributes = new HashMap<String, String>(attributeCount);
			for (int i = 0; i < attributeCount; i++) {
				attributes.put(parser.getAttributeName(i), parser.getAttributeValue(i));
			}
		}
		return attributes;
	}

	private static List<CmisExtensionElement> parseExtensions(XmlPullParser parser) throws Exception {

		// log.debug("[START] parseExtensions...");

		List<CmisExtensionElement> extensions = new ArrayList<CmisExtensionElement>();

		Stack<String> names = new Stack<String>();
		Stack<String> namespaces = new Stack<String>();
		Stack<Map<String, String>> attributes = new Stack<Map<String, String>>();
		Stack<List<CmisExtensionElement>> childrenStack = new Stack<List<CmisExtensionElement>>();

		String name = parser.getName();
		String id = parser.getName();
		int eventType = parser.getEventType();

		while (!(eventType == XmlPullParser.END_TAG && id.equals(name))) {
			switch (eventType) {
			case XmlPullParser.START_TAG:

				// parse tag info
				names.push(parser.getName());
				namespaces.push(parser.getNamespace());
				attributes.push(getAttributes(parser));

				int beforeEventType = parser.getEventType();
				eventType = parser.next();
				switch (eventType) {
				case XmlPullParser.START_TAG:
					childrenStack.add(new ArrayList<CmisExtensionElement>());
					break;
				case XmlPullParser.TEXT:
					String value = parser.getText();
					if ("\n".equals(value)) {
						if (beforeEventType == XmlPullParser.START_TAG) {
							childrenStack.add(new ArrayList<CmisExtensionElement>());
						} else if (eventType == XmlPullParser.END_TAG) {
							childrenStack.get(childrenStack.size() - 1).add(new CmisExtensionElementImpl(namespaces.pop(), names.pop(), attributes.pop(), (String) null));
						}
						parser.next();
					} else {
						if (childrenStack.size() != 0) {
							childrenStack.get(childrenStack.size() - 1).add(new CmisExtensionElementImpl(namespaces.pop(), names.pop(), attributes.pop(), value));
							parser.next();
						} else if (id.equals(parser.getName())) {
							extensions.add(new CmisExtensionElementImpl(namespaces.pop(), names.pop(), attributes.pop(), value));
							return extensions;
						} else {
							parser.next();
							if (id.equals(parser.getName())) {
								extensions.add(new CmisExtensionElementImpl(namespaces.pop(), names.pop(), attributes.pop(), value));
								return extensions;
							}
						}
					}

					break;
				case XmlPullParser.END_TAG:
					// case empty tag leaf
					if (childrenStack.size() != 0) {
						childrenStack.get(childrenStack.size() - 1).add(new CmisExtensionElementImpl(namespaces.pop(), names.pop(), attributes.pop(), (String) null));
					} else if (id.equals(parser.getName())) {
						extensions.add(new CmisExtensionElementImpl(namespaces.pop(), names.pop(), attributes.pop(), (String) null));
						return extensions;
					}
					break;
				}
				break;
			case XmlPullParser.END_TAG:
				eventType = parser.next();
				if (XmlPullParser.TEXT == eventType) {
					eventType = parser.next();
				}

				if (XmlPullParser.END_TAG == eventType) {
					CmisExtensionElementImpl ext = new CmisExtensionElementImpl(namespaces.pop(), names.pop(), attributes.pop(), childrenStack.pop());
					if (childrenStack.size() == 0) {
						extensions.add(ext);
					} else {
						childrenStack.get(childrenStack.size() - 1).add(ext);
					}
				}
				break;
			}

			eventType = parser.getEventType();
			name = parser.getName();
		}

		if (extensions.isEmpty() && childrenStack.size() == 1) {
			extensions.addAll(childrenStack.pop());
		}

		// log.debug("[END] parseExtensions...");

		return extensions;
	}

	private static RenditionData parseRenditions(XmlPullParser parser) throws Exception {
		// log.debug("[START] parseRenditions...");
		Map<String, String> rendition = new HashMap<String, String>(15);
		int eventType = parser.next();
		String name = parser.getName();
		String namespace = parser.getNamespace();
		List<CmisExtensionElement> extensions = new ArrayList<CmisExtensionElement>();

		while (!(eventType == XmlPullParser.END_TAG && TAG_RENDITION.equals(name))) {
			switch (eventType) {
			case XmlPullParser.START_TAG:
				if (Constants.NAMESPACE_CMIS.equals(namespace)) {
					parser.next();
					rendition.put(name, parser.getText());
				} else {
					// Parse Extensions
					extensions.addAll(parseExtensions(parser));
					eventType = parser.next();
					if (XmlPullParser.TEXT == eventType) {
						eventType = parser.next();
					}
					name = parser.getName();
					namespace = parser.getNamespace();
					continue;
				}
				break;
			}
			eventType = parser.next();
			name = parser.getName();
			namespace = parser.getNamespace();
		}

		// log.debug("[STOP] parseRenditions...");
		return AtomPubConverter.convertRendition(rendition, extensions);
	}

	private static AllowableActions parseAllowableActions(XmlPullParser parser) throws Exception {
		// log.debug("[START] parseAllowableActions...");
		Map<String, String> allowableActionsRawValues = new HashMap<String, String>(15);
		int eventType = parser.next();
		String name = parser.getName();
		String namespace = parser.getNamespace();
		List<CmisExtensionElement> extensions = new ArrayList<CmisExtensionElement>();

		while (!(eventType == XmlPullParser.END_TAG && Constants.SELECTOR_ALLOWABLEACTIONS.equals(name))) {
			switch (eventType) {
			case XmlPullParser.START_TAG:
				if (Constants.NAMESPACE_CMIS.equals(namespace)) {
					parser.next();
					allowableActionsRawValues.put(name, parser.getText());
				} else {
					// Parse Extensions
					extensions.addAll(parseExtensions(parser));
					eventType = parser.next();
					if (XmlPullParser.TEXT == eventType) {
						eventType = parser.next();
					}
					name = parser.getName();
					namespace = parser.getNamespace();
					continue;
				}
				break;
			}
			eventType = parser.next();
			name = parser.getName();
			namespace = parser.getNamespace();
		}

		// log.debug("[STOP] parseAllowableActions...");
		return AtomPubConverter.convertAllowableActions(allowableActionsRawValues, extensions);
	}

	private static AclCapabilities parseAclCapabilities(XmlPullParser parser) throws Exception {
		// log.debug("[START] parseAclCapabilities...");

		Map<String, String> AclCapabilitiesRawValues = new HashMap<String, String>(15);
		List<PermissionDefinition> permissionDefinitionList = new ArrayList<PermissionDefinition>();
		Map<String, PermissionMapping> permMap = new HashMap<String, PermissionMapping>();

		int eventType = parser.next();
		String name = parser.getName();
		String namespace = parser.getNamespace();

		while (!(eventType == XmlPullParser.END_TAG && TAG_REPINFO_ACL_CAPABILITY.equals(name))) {
			switch (eventType) {
			case XmlPullParser.START_TAG:
				if (Constants.NAMESPACE_CMIS.equals(namespace) && TAG_ACLCAP_PERMISSIONS.equals(name)) {

					String permission = null, description = null;
					// Permissions
					while (!(eventType == XmlPullParser.END_TAG && TAG_ACLCAP_PERMISSIONS.equals(name))) {
						switch (eventType) {
						case XmlPullParser.START_TAG:
							if (Constants.NAMESPACE_CMIS.equals(namespace) && TAG_ACLCAP_PERMISSION_PERMISSION.equals(name)) {
								parser.next();
								permission = parser.getText();
							} else if (Constants.NAMESPACE_CMIS.equals(namespace) && TAG_ACLCAP_PERMISSION_DESCRIPTION.equals(name)) {
								parser.next();
								description = parser.getText();
							}
							break;
						}
						eventType = parser.next();
						name = parser.getName();
					}
					PermissionDefinitionDataImpl permDef = new PermissionDefinitionDataImpl();
					permDef.setPermission(permission);
					permDef.setDescription(description);

					// convertExtension(permissionMap, permDef,
					// ACLCAP_PERMISSION_KEYS);

					permissionDefinitionList.add(permDef);

				} else if (Constants.NAMESPACE_CMIS.equals(namespace) && TAG_ACLCAP_MAPPING.equals(name)) {

					String key = null;
					List<String> permList = new ArrayList<String>();
					// MAPPINGS
					while (!(eventType == XmlPullParser.END_TAG && TAG_ACLCAP_MAPPING.equals(name))) {
						switch (eventType) {
						case XmlPullParser.START_TAG:
							if (Constants.NAMESPACE_CMIS.equals(namespace) && TAG_ACLCAP_MAPPING_KEY.equals(name)) {
								parser.next();
								key = parser.getText();
							} else if (Constants.NAMESPACE_CMIS.equals(namespace) && TAG_ACLCAP_MAPPING_PERMISSION.equals(name)) {
								parser.next();
								permList.add(parser.getText());
							}
							break;
						}
						eventType = parser.next();
						name = parser.getName();
					}
					PermissionMappingDataImpl mapping = new PermissionMappingDataImpl();
					mapping.setKey(key);
					mapping.setPermissions(permList);

					// convertExtension(permissionMap, mapping,
					// ACLCAP_MAPPING_KEYS);

					permMap.put(key, mapping);

				} else if (Constants.NAMESPACE_CMIS.equals(namespace)) {
					parser.next();
					AclCapabilitiesRawValues.put(name, parser.getText());
				}
				break;
			}
			eventType = parser.next();
			name = parser.getName();
			namespace = parser.getNamespace();
		}

		// log.debug("[STOP] parseAclCapabilities...");
		return AtomPubConverter.convertAclCapabilities(AclCapabilitiesRawValues, permissionDefinitionList, permMap);
	}

	private static Acl parseACL(XmlPullParser parser) throws Exception {
		// log.debug("[START] parseACL...");
		int eventType = parser.next();
		String name = parser.getName();
		String namespace = parser.getNamespace();
		boolean isPermissionRootTag = false;
		List<Ace> aces = new ArrayList<Ace>();
		List<String> permissions = null;
		AccessControlEntryImpl ace = null;

		while (!(eventType == XmlPullParser.END_TAG && Constants.SELECTOR_ACL.equals(name))) {
			switch (eventType) {
			case XmlPullParser.START_TAG:
				if (Constants.NAMESPACE_CMIS.equals(namespace) && TAG_ACLCAP_PERMISSION_PERMISSION.equals(name)) {
					if (isPermissionRootTag == false) {
						isPermissionRootTag = true;
						permissions = new ArrayList<String>();
						ace = new AccessControlEntryImpl();
					} else {
						parser.next();
						permissions.add(parser.getText());
						parser.next();
					}
				} else if (Constants.NAMESPACE_CMIS.equals(namespace) && TAG_ACE_PRINCIPAL.equals(name)) {
					AccessControlPrincipalDataImpl principal = null;
					while (!(eventType == XmlPullParser.END_TAG && TAG_ACE_PRINCIPAL.equals(name))) {
						switch (eventType) {
						case XmlPullParser.START_TAG:
							if (Constants.NAMESPACE_CMIS.equals(namespace) && TAG_ACE_PRINCIPAL_ID.equals(name)) {
								parser.next();
								principal = new AccessControlPrincipalDataImpl();
								principal.setPrincipalId(parser.getText());
							}
							break;
						}
						eventType = parser.next();
						name = parser.getName();
						namespace = parser.getNamespace();
					}
					// convertExtension(jsonPrincipal, principal,
					// PRINCIPAL_KEYS);
					ace.setPrincipal(principal);
				} else if (Constants.NAMESPACE_CMIS.equals(namespace) && TAG_ACE_DIRECT.equals(name)) {
					parser.next();
					Boolean isDirect = Boolean.parseBoolean(parser.getText());
					ace.setDirect(isDirect != null ? isDirect.booleanValue() : true);
				}
				break;
			case XmlPullParser.END_TAG:
				if (Constants.NAMESPACE_CMIS.equals(namespace) && TAG_ACLCAP_PERMISSION_PERMISSION.equals(name)) {
					isPermissionRootTag = false;
					ace.setPermissions(permissions);
					aces.add(ace);
				}
				break;
			}
			eventType = parser.next();
			name = parser.getName();
			namespace = parser.getNamespace();
		}

		AccessControlListImpl result = new AccessControlListImpl();
		result.setAces(aces);
		// result.setExact(isExact);

		// convertExtension(json, result, ACL_KEYS);

		return result;
	}

}
