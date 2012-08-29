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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.impl.Constants;

/**
 * Atom base class.
 */
public abstract class AtomDocumentBase extends XMLDocumentBase {

	private static final String ID_PREFIX = "http://chemistry.apache.org/";
	private static final String ID_DUMMY = "http://chemistry.apache.org/no-id";

	private SimpleDateFormat dateFormater;

	/**
	 * Formats a DateTime.
	 */
	public String formatDate(long millis) {
		if (dateFormater == null) {
			dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
					Locale.US);
			dateFormater.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		return dateFormater.format(millis);
	}

	/**
	 * Generates a valid Atom id.
	 */
	public String generateAtomId(String input) {
		if (input == null) {
			return ID_DUMMY;
		}

		try {
			return ID_PREFIX + Base64.encodeBytes(input.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return ID_DUMMY;
		}
	}

	/**
	 * Writes a simple tag.
	 */
	public void writeSimpleTag(String namespace, String name, String value)
			throws XMLStreamException {
		if (value == null) {
			return;
		}

		XMLStreamWriter xsw = getWriter();

		xsw.writeStartElement(namespace, name);
		xsw.writeCharacters(value);
		xsw.writeEndElement();
	}

	/**
	 * Writes a simple date tag.
	 */
	public void writeSimpleDate(String namespace, String name,
			GregorianCalendar value) throws XMLStreamException {
		if (value == null) {
			return;
		}

		writeSimpleTag(namespace, name, formatDate(value.getTimeInMillis()));
	}

	/**
	 * Writes a simple date tag.
	 */
	public void writeSimpleDate(String namespace, String name, long millis)
			throws XMLStreamException {
		writeSimpleTag(namespace, name, formatDate(millis));
	}

	/**
	 * Writes an Atom id tag.
	 */
	public void writeId(String id) throws XMLStreamException {
		writeSimpleTag(Constants.NAMESPACE_ATOM, "id", id);
	}

	/**
	 * Writes an Atom title tag.
	 */
	public void writeTitle(String title) throws XMLStreamException {
		writeSimpleTag(Constants.NAMESPACE_ATOM, "title", title);
	}

	/**
	 * Writes an Atom author tag.
	 */
	public void writeAuthor(String author) throws XMLStreamException {
		XMLStreamWriter xsw = getWriter();

		xsw.writeStartElement(Constants.NAMESPACE_ATOM, "author");
		writeSimpleTag(Constants.NAMESPACE_ATOM, "name", author);
		xsw.writeEndElement();
	}

	/**
	 * Writes an Atom updated tag.
	 */
	public void writeUpdated(GregorianCalendar updated)
			throws XMLStreamException {
		writeSimpleDate(Constants.NAMESPACE_APP, "edited", updated);
		writeSimpleDate(Constants.NAMESPACE_ATOM, "updated", updated);
	}

	/**
	 * Writes an Atom updated tag.
	 */
	public void writeUpdated(long updated) throws XMLStreamException {
		writeSimpleDate(Constants.NAMESPACE_APP, "edited", updated);
		writeSimpleDate(Constants.NAMESPACE_ATOM, "updated", updated);
	}

	/**
	 * Writes an Atom published tag.
	 */
	public void writePublished(GregorianCalendar published)
			throws XMLStreamException {
		writeSimpleDate(Constants.NAMESPACE_ATOM, "published", published);
	}

	/**
	 * Writes an Atom published tag.
	 */
	public void writePublished(long published) throws XMLStreamException {
		writeSimpleDate(Constants.NAMESPACE_ATOM, "published", published);
	}

	/**
	 * Writes a CMIS pathSegment tag.
	 */
	public void writePathSegment(String pathSegment) throws XMLStreamException {
		writeSimpleTag(Constants.NAMESPACE_RESTATOM, "pathSegment", pathSegment);
	}

	/**
	 * Writes a CMIS relativePathSegment tag.
	 */
	public void writeRelativePathSegment(String relativePathSegment)
			throws XMLStreamException {
		writeSimpleTag(Constants.NAMESPACE_RESTATOM, "relativePathSegment",
				relativePathSegment);
	}

	/**
	 * Writes an Atom collection.
	 */
	public void writeCollection(String href, String collectionType,
			String text, String... accept) throws XMLStreamException {
		XMLStreamWriter xsw = getWriter();

		xsw.writeStartElement(Constants.NAMESPACE_APP, "collection");
		xsw.writeAttribute("href", href);

		if (collectionType != null) {
			xsw.writeStartElement(Constants.NAMESPACE_RESTATOM,
					"collectionType");
			xsw.writeCharacters(collectionType);
			xsw.writeEndElement();
		}

		xsw.writeStartElement(Constants.NAMESPACE_ATOM, "title");
		xsw.writeAttribute("type", "text");
		xsw.writeCharacters(text);
		xsw.writeEndElement();

		for (String ct : accept) {
			xsw.writeStartElement(Constants.NAMESPACE_APP, "accept");
			xsw.writeCharacters(ct);
			xsw.writeEndElement();
		}

		xsw.writeEndElement();
	}

	/**
	 * Writes a link.
	 */
	public void writeLink(String rel, String href, String type, String id)
			throws XMLStreamException {
		XMLStreamWriter xsw = getWriter();

		xsw.writeStartElement(Constants.NAMESPACE_ATOM, "link");

		xsw.writeAttribute("rel", rel);
		xsw.writeAttribute("href", href);
		if (type != null) {
			xsw.writeAttribute("type", type);
		}
		if (id != null) {
			xsw.writeAttribute(Constants.NAMESPACE_RESTATOM, "id", id);
		}

		xsw.writeEndElement();
	}

	public void writeServiceLink(String href, String repositoryId)
			throws XMLStreamException {
		try {
			writeLink(Constants.REL_SERVICE, href + "?repositoryId="
					+ URLEncoder.encode(repositoryId, "UTF-8"),
					Constants.MEDIATYPE_SERVICE, null);
		} catch (UnsupportedEncodingException e) {
		}
	}

	public void writeSelfLink(String href, String id) throws XMLStreamException {
		writeLink(Constants.REL_SELF, href, Constants.MEDIATYPE_ENTRY, id);
	}

	public void writeEnclosureLink(String href) throws XMLStreamException {
		writeLink(Constants.REL_ENCLOSURE, href, Constants.MEDIATYPE_ENTRY,
				null);
	}

	public void writeEditLink(String href) throws XMLStreamException {
		writeLink(Constants.REL_EDIT, href, Constants.MEDIATYPE_ENTRY, null);
	}

	public void writeAlternateLink(String href, String type, String kind,
			String title, BigInteger length) throws XMLStreamException {
		XMLStreamWriter xsw = getWriter();

		xsw.writeStartElement(Constants.NAMESPACE_ATOM, "link");

		xsw.writeAttribute("rel", Constants.REL_ALTERNATE);
		xsw.writeAttribute("href", href);
		if (type != null) {
			xsw.writeAttribute("type", type);
		}
		if (kind != null) {
			xsw.writeAttribute(Constants.NAMESPACE_RESTATOM, "renditionKind",
					kind);
		}
		if (title != null) {
			xsw.writeAttribute("title", title);
		}
		if (length != null) {
			xsw.writeAttribute("length", length.toString());
		}

		xsw.writeEndElement();
	}

	public void writeWorkingCopyLink(String href) throws XMLStreamException {
		writeLink(Constants.REL_WORKINGCOPY, href, Constants.MEDIATYPE_ENTRY,
				null);
	}

	public void writeUpLink(String href, String type) throws XMLStreamException {
		writeLink(Constants.REL_UP, href, type, null);
	}

	public void writeDownLink(String href, String type)
			throws XMLStreamException {
		writeLink(Constants.REL_DOWN, href, type, null);
	}

	public void writeVersionHistoryLink(String href) throws XMLStreamException {
		writeLink(Constants.REL_VERSIONHISTORY, href, Constants.MEDIATYPE_FEED,
				null);
	}

	public void writeCurrentVerionsLink(String href) throws XMLStreamException {
		writeLink(Constants.REL_CURRENTVERSION, href,
				Constants.MEDIATYPE_ENTRY, null);
	}

	public void writeEditMediaLink(String href, String type)
			throws XMLStreamException {
		writeLink(Constants.REL_EDITMEDIA, href, type, null);
	}

	public void writeDescribedByLink(String href) throws XMLStreamException {
		writeLink(Constants.REL_DESCRIBEDBY, href, Constants.MEDIATYPE_ENTRY,
				null);
	}

	public void writeAllowableActionsLink(String href)
			throws XMLStreamException {
		writeLink(Constants.REL_ALLOWABLEACTIONS, href,
				Constants.MEDIATYPE_ALLOWABLEACTION, null);
	}

	public void writeAclLink(String href) throws XMLStreamException {
		writeLink(Constants.REL_ACL, href, Constants.MEDIATYPE_ACL, null);
	}

	public void writePoliciesLink(String href) throws XMLStreamException {
		writeLink(Constants.REL_POLICIES, href, Constants.MEDIATYPE_FEED, null);
	}

	public void writeRelationshipsLink(String href) throws XMLStreamException {
		writeLink(Constants.REL_RELATIONSHIPS, href, Constants.MEDIATYPE_FEED,
				null);
	}

	public void writeRelationshipSourceLink(String href)
			throws XMLStreamException {
		writeLink(Constants.REL_SOURCE, href, Constants.MEDIATYPE_ENTRY, null);
	}

	public void writeRelationshipTargetLink(String href)
			throws XMLStreamException {
		writeLink(Constants.REL_TARGET, href, Constants.MEDIATYPE_ENTRY, null);
	}

	public void writeFolderTreeLink(String href) throws XMLStreamException {
		writeLink(Constants.REL_FOLDERTREE, href,
				Constants.MEDIATYPE_FEED, null);
	}

	public void writeTypeUpLink(String href, String type)
			throws XMLStreamException {
		writeLink(Constants.REL_UP, href, type, null);
	}

	public void writeTypeDownLink(String href, String type)
			throws XMLStreamException {
		writeLink(Constants.REL_DOWN, href, type, null);
	}

	public void writeViaLink(String href) throws XMLStreamException {
		writeLink(Constants.REL_VIA, href, Constants.MEDIATYPE_ENTRY, null);
	}

	public void writeFirstLink(String href) throws XMLStreamException {
		writeLink(Constants.REL_FIRST, href, Constants.MEDIATYPE_FEED, null);
	}

	public void writeLastLink(String href) throws XMLStreamException {
		writeLink(Constants.REL_LAST, href, Constants.MEDIATYPE_FEED, null);
	}

	public void writePreviousLink(String href) throws XMLStreamException {
		writeLink(Constants.REL_PREV, href, Constants.MEDIATYPE_FEED, null);
	}

	public void writeNextLink(String href) throws XMLStreamException {
		writeLink(Constants.REL_NEXT, href, Constants.MEDIATYPE_FEED, null);
	}
}
