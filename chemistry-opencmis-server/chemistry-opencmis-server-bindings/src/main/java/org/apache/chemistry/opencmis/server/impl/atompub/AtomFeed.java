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

import java.math.BigInteger;
import java.util.GregorianCalendar;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;

/**
 * Atom Feed class.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AtomFeed extends AtomDocumentBase {

    public static final BigInteger DEFAULT_PAGE_SIZE = BigInteger.valueOf(100);

    /**
     * Creates an Atom feed document.
     */
    public AtomFeed() {
    }

    /**
     * Creates an Atom feed that is embedded somewhere.
     */
    public AtomFeed(XMLStreamWriter writer) {
        setWriter(writer);
    }

    /**
     * Opens the feed tag.
     */
    public void startFeed(boolean isRoot) throws XMLStreamException {
        getWriter().writeStartElement(Constants.NAMESPACE_ATOM, "feed");

        if (isRoot) {
            writeNamespace(Constants.NAMESPACE_ATOM);
            writeNamespace(Constants.NAMESPACE_CMIS);
            writeNamespace(Constants.NAMESPACE_RESTATOM);
            writeNamespace(Constants.NAMESPACE_APP);
            writeAllCustomNamespace();
        }
    }

    /**
     * Opens the children tag.
     */
    public void startChildren() throws XMLStreamException {
        XMLStreamWriter writer = getWriter();
        writer.writeStartElement(Constants.NAMESPACE_RESTATOM, "children");
    }

    /**
     * Closes the feed tag.
     */
    public void endChildren() throws XMLStreamException {
        getWriter().writeEndElement();
    }

    /**
     * Closes the feed tag.
     */
    public void endFeed() throws XMLStreamException {
        getWriter().writeEndElement();
    }

    /**
     * Writes the feed elements that are required by Atom.
     */
    public void writeFeedElements(String id, String atomId, String author, String title, GregorianCalendar updated,
            String pathSegment, BigInteger numItems) throws XMLStreamException {
        writeAuthor(author);
        writeId(atomId == null ? generateAtomId(id) : atomId);
        writeTitle(title);
        writeUpdated(updated);
        writePathSegment(pathSegment);
        writeNumItems(numItems);
    }

    /**
     * Writes a CMIS numItems tag.
     */
    public void writeNumItems(BigInteger numItems) throws XMLStreamException {
        if (numItems == null) {
            return;
        }

        writeSimpleTag(Constants.NAMESPACE_RESTATOM, "numItems", numItems.toString());
    }

    /**
     * Writes paging links.
     */
    public void writePagingLinks(UrlBuilder pagingUrl, BigInteger maxItems, BigInteger skipCount, BigInteger numItems,
            Boolean hasMoreItems, BigInteger pageSize) throws XMLStreamException {

        if ((skipCount == null) || (skipCount.compareTo(BigInteger.ZERO) == -1)) {
            skipCount = BigInteger.ZERO;
        }

        if ((maxItems == null) || (maxItems.compareTo(BigInteger.ZERO) == -1)) {
            if ((pageSize == null) || (pageSize.compareTo(BigInteger.ZERO) == -1)) {
                maxItems = DEFAULT_PAGE_SIZE;
            } else {
                maxItems = pageSize;
            }
        }

        // if not first page -> add "first" and "previous" link
        if (skipCount.compareTo(BigInteger.ZERO) == 1) {
            // first link
            UrlBuilder firstLink = new UrlBuilder(pagingUrl);
            firstLink.addParameter(Constants.PARAM_SKIP_COUNT, "0");
            firstLink.addParameter(Constants.PARAM_MAX_ITEMS, maxItems);
            writeFirstLink(firstLink.toString());

            // previous link
            UrlBuilder previousLink = new UrlBuilder(pagingUrl);
            previousLink.addParameter(Constants.PARAM_SKIP_COUNT, skipCount.subtract(maxItems).max(BigInteger.ZERO));
            previousLink.addParameter(Constants.PARAM_MAX_ITEMS, maxItems);
            writePreviousLink(previousLink.toString());
        }

        // if has more -> add "next" link
        if ((hasMoreItems != null) && hasMoreItems.booleanValue()) {
            // next link
            UrlBuilder nextLink = new UrlBuilder(pagingUrl);
            nextLink.addParameter(Constants.PARAM_SKIP_COUNT, skipCount.add(maxItems));
            nextLink.addParameter(Constants.PARAM_MAX_ITEMS, maxItems);
            writeNextLink(nextLink.toString());
        }

        // if not last page -> add "last" link
        if ((numItems != null) && (numItems.compareTo(BigInteger.ZERO) == 1)) {
            BigInteger lastSkip = numItems.subtract(maxItems).max(BigInteger.ZERO);
            if (lastSkip.compareTo(BigInteger.ZERO) == 1) {
                // last link
                UrlBuilder lastLink = new UrlBuilder(pagingUrl);
                lastLink.addParameter(Constants.PARAM_SKIP_COUNT, lastSkip);
                lastLink.addParameter(Constants.PARAM_MAX_ITEMS, maxItems);
                writeLastLink(lastLink.toString());
            }
        }
    }
}
