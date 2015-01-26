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
package org.apache.chemistry.opencmis.server.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.server.impl.atompub.AtomEntryParser;
import org.apache.chemistry.opencmis.server.shared.TempStoreOutputStreamFactory;
import org.apache.chemistry.opencmis.server.shared.ThresholdOutputStream.ThresholdInputStream;
import org.junit.Test;

/**
 * AtomEntryParser test.
 */
public class AtomEntryParserTest {

    private static final int THRESHOLD = 4 * 1024 * 1024;
    private static final int MAX_SIZE = -1;

    private static final String CMIS_ENTRY_CONTENT = "This is my content!";
    private static final String CMIS_ENTRY = "<?xml version='1.0' encoding='utf-8'?>"
            + "<atom:entry xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:cmis=\"http://docs.oasis-open.org/ns/cmis/core/200908/\" xmlns:cmisra=\"http://docs.oasis-open.org/ns/cmis/restatom/200908/\">"
            + "<atom:author><atom:name>test</atom:name></atom:author>" + "<atom:id>http://test/id</atom:id>"
            + "<atom:published>2009-12-31T12:53:37Z</atom:published>" + "<atom:title>test.txt</atom:title>"
            + "<atom:updated>2010-01-01T00:00:00Z</atom:updated>"
            + "<cmisra:object xmlns:ns3=\"http://docs.oasis-open.org/ns/cmis/messaging/200908/\">"
            + "  <cmis:properties>" + "    <cmis:propertyId propertyDefinitionId=\"cmis:objectId\">"
            + "      <cmis:value>id</cmis:value>" + "    </cmis:propertyId>"
            + "    <cmis:propertyString propertyDefinitionId=\"cmis:name\">"
            + "      <cmis:value>test.txt</cmis:value>" + "    </cmis:propertyString>"
            + "    <cmis:propertyId propertyDefinitionId=\"cmis:objectTypeId\">"
            + "      <cmis:value>cmis:document</cmis:value>" + "    </cmis:propertyId>" + "  </cmis:properties>"
            + "</cmisra:object>" + "<cmisra:content>" + "  <cmisra:mediatype>text/plain</cmisra:mediatype>"
            + "  <cmisra:base64>" + Base64.encodeBytes(CMIS_ENTRY_CONTENT.getBytes()) + "</cmisra:base64>"
            + "</cmisra:content>" + "</atom:entry>";

    private static final String ATOM_ENTRY_TEXT_CONTENT = "This is plain text!";
    private static final String ATOM_ENTRY_TEXT = "<?xml version='1.0' encoding='utf-8'?>"
            + "<atom:entry xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:cmis=\"http://docs.oasis-open.org/ns/cmis/core/200908/\" xmlns:cmisra=\"http://docs.oasis-open.org/ns/cmis/restatom/200908/\">"
            + "<atom:author><atom:name>test</atom:name></atom:author>" + "<atom:id>http://test/id</atom:id>"
            + "<atom:published>2009-12-31T12:53:37Z</atom:published>" + "<atom:title>test.txt</atom:title>"
            + "<atom:updated>2010-01-01T00:00:00Z</atom:updated>"
            + "<cmisra:object xmlns:ns3=\"http://docs.oasis-open.org/ns/cmis/messaging/200908/\">"
            + "  <cmis:properties>" + "    <cmis:propertyId propertyDefinitionId=\"cmis:objectId\">"
            + "      <cmis:value>id</cmis:value>" + "    </cmis:propertyId>"
            + "    <cmis:propertyString propertyDefinitionId=\"cmis:name\">"
            + "      <cmis:value>test.txt</cmis:value>" + "    </cmis:propertyString>"
            + "    <cmis:propertyId propertyDefinitionId=\"cmis:objectTypeId\">"
            + "      <cmis:value>cmis:document</cmis:value>" + "    </cmis:propertyId>" + "  </cmis:properties>"
            + "</cmisra:object>" + "<atom:content type=\"text\">" + ATOM_ENTRY_TEXT_CONTENT + "</atom:content>"
            + "</atom:entry>";

    private static final String ATOM_ENTRY_XML_CONTENT = "<first xmlns=\"http://test/1\"><second myattr=\"Cool, a value!\">hey, this is text</second><myns:third xmlns:myns=\"http://test/2\">guess what's here ... more text</myns:third></first>";
    private static final String ATOM_ENTRY_XML = "<?xml version='1.0' encoding='utf-8'?>"
            + "<atom:entry xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:cmis=\"http://docs.oasis-open.org/ns/cmis/core/200908/\" xmlns:cmisra=\"http://docs.oasis-open.org/ns/cmis/restatom/200908/\">"
            + "<atom:author><atom:name>test</atom:name></atom:author>" + "<atom:id>http://test/id</atom:id>"
            + "<atom:published>2009-12-31T12:53:37Z</atom:published>" + "<atom:title>test.txt</atom:title>"
            + "<atom:updated>2010-01-01T00:00:00Z</atom:updated>"
            + "<cmisra:object xmlns:ns3=\"http://docs.oasis-open.org/ns/cmis/messaging/200908/\">"
            + "  <cmis:properties>" + "    <cmis:propertyId propertyDefinitionId=\"cmis:objectId\">"
            + "      <cmis:value>id</cmis:value>" + "    </cmis:propertyId>"
            + "    <cmis:propertyString propertyDefinitionId=\"cmis:name\">"
            + "      <cmis:value>test.txt</cmis:value>" + "    </cmis:propertyString>"
            + "    <cmis:propertyId propertyDefinitionId=\"cmis:objectTypeId\">"
            + "      <cmis:value>cmis:document</cmis:value>" + "    </cmis:propertyId>" + "  </cmis:properties>"
            + "</cmisra:object>" + "<atom:content type=\"text/xml\">" + ATOM_ENTRY_XML_CONTENT + "</atom:content>"
            + "</atom:entry>";

    private static final String ATOM_ENTRY_XHTML_CONTENT = "<div xmlns=\"http://www.w3.org/1999/xhtml\">This is <b>XHTML</b> content.</div>";
    private static final String ATOM_ENTRY_XHTML = "<?xml version='1.0' encoding='utf-8'?>"
            + "<atom:entry xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:cmis=\"http://docs.oasis-open.org/ns/cmis/core/200908/\" xmlns:cmisra=\"http://docs.oasis-open.org/ns/cmis/restatom/200908/\">"
            + "<atom:author><atom:name>test</atom:name></atom:author>" + "<atom:id>http://test/id</atom:id>"
            + "<atom:published>2009-12-31T12:53:37Z</atom:published>" + "<atom:title>test.txt</atom:title>"
            + "<atom:updated>2010-01-01T00:00:00Z</atom:updated>"
            + "<cmisra:object xmlns:ns3=\"http://docs.oasis-open.org/ns/cmis/messaging/200908/\">"
            + "  <cmis:properties>" + "    <cmis:propertyId propertyDefinitionId=\"cmis:objectId\">"
            + "      <cmis:value>id</cmis:value>" + "    </cmis:propertyId>"
            + "    <cmis:propertyString propertyDefinitionId=\"cmis:name\">"
            + "      <cmis:value>test.txt</cmis:value>" + "    </cmis:propertyString>"
            + "    <cmis:propertyId propertyDefinitionId=\"cmis:objectTypeId\">"
            + "      <cmis:value>cmis:document</cmis:value>" + "    </cmis:propertyId>" + "  </cmis:properties>"
            + "</cmisra:object>" + "<atom:content type=\"xhtml\">" + ATOM_ENTRY_XHTML_CONTENT + "</atom:content>"
            + "</atom:entry>";

    private static final String ATOM_ENTRY_BASE64_CONTENT = "This is another content!";
    private static final String ATOM_ENTRY_BASE64 = "<?xml version='1.0' encoding='utf-8'?>"
            + "<atom:entry xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:cmis=\"http://docs.oasis-open.org/ns/cmis/core/200908/\" xmlns:cmisra=\"http://docs.oasis-open.org/ns/cmis/restatom/200908/\">"
            + "<atom:author><atom:name>test</atom:name></atom:author>" + "<atom:id>http://test/id</atom:id>"
            + "<atom:published>2009-12-31T12:53:37Z</atom:published>" + "<atom:title>test.txt</atom:title>"
            + "<atom:updated>2010-01-01T00:00:00Z</atom:updated>"
            + "<cmisra:object xmlns:ns3=\"http://docs.oasis-open.org/ns/cmis/messaging/200908/\">"
            + "  <cmis:properties>" + "    <cmis:propertyId propertyDefinitionId=\"cmis:objectId\">"
            + "      <cmis:value>id</cmis:value>" + "    </cmis:propertyId>"
            + "    <cmis:propertyString propertyDefinitionId=\"cmis:name\">"
            + "      <cmis:value>test.txt</cmis:value>" + "    </cmis:propertyString>"
            + "    <cmis:propertyId propertyDefinitionId=\"cmis:objectTypeId\">"
            + "      <cmis:value>cmis:document</cmis:value>" + "    </cmis:propertyId>" + "  </cmis:properties>"
            + "</cmisra:object>" + "<atom:content type=\"application/something\">"
            + Base64.encodeBytes(ATOM_ENTRY_BASE64_CONTENT.getBytes()) + "</atom:content>" + "</atom:entry>";

    private static final String ATOM_ENTRY_NAME = "<?xml version='1.0' encoding='utf-8'?>"
            + "<atom:entry xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:cmis=\"http://docs.oasis-open.org/ns/cmis/core/200908/\" xmlns:cmisra=\"http://docs.oasis-open.org/ns/cmis/restatom/200908/\">"
            + "<atom:author><atom:name>test</atom:name></atom:author>" + "<atom:id>http://test/id</atom:id>"
            + "<atom:published>2009-12-31T12:53:37Z</atom:published>" + "<atom:title>atom.title</atom:title>"
            + "<atom:updated>2010-01-01T00:00:00Z</atom:updated>"
            + "<cmisra:object xmlns:ns3=\"http://docs.oasis-open.org/ns/cmis/messaging/200908/\">"
            + "  <cmis:properties>" + "    <cmis:propertyId propertyDefinitionId=\"cmis:objectId\">"
            + "      <cmis:value>id</cmis:value>" + "    </cmis:propertyId>"
            + "    <cmis:propertyString propertyDefinitionId=\"cmis:name\">"
            + "      <cmis:value>cmis.name</cmis:value>" + "    </cmis:propertyString>"
            + "    <cmis:propertyId propertyDefinitionId=\"cmis:objectTypeId\">"
            + "      <cmis:value>cmis:document</cmis:value>" + "    </cmis:propertyId>" + "  </cmis:properties>"
            + "</cmisra:object>" + "</atom:entry>";

    @Test
    public void testCmisContent() throws Exception {
        byte[] content = parse(CMIS_ENTRY.getBytes(), "text/plain");
        assertEquals(CMIS_ENTRY_CONTENT, new String(content));
    }

    @Test
    public void testAtomContentText() throws Exception {
        byte[] content = parse(ATOM_ENTRY_TEXT.getBytes(), "text/plain");
        assertEquals(ATOM_ENTRY_TEXT_CONTENT, new String(content));
    }

    @Test
    public void testAtomContentXml() throws Exception {
        byte[] content = parse(ATOM_ENTRY_XML.getBytes(), "text/xml");
        String xmlContent = new String(content);
        assertTrue(xmlContent.indexOf('>') > -1);
        assertEquals(ATOM_ENTRY_XML_CONTENT, xmlContent.substring(xmlContent.indexOf('>') + 1));
    }

    @Test
    public void testAtomContentXHtml() throws Exception {
        byte[] content = parse(ATOM_ENTRY_XHTML.getBytes(), "application/xhtml+xml");
        String xmlContent = new String(content);
        assertTrue(xmlContent.indexOf('>') > -1);
        assertEquals(ATOM_ENTRY_XHTML_CONTENT, xmlContent.substring(xmlContent.indexOf('>') + 1));
    }

    @Test
    public void testAtomContentBase64() throws Exception {
        byte[] content = parse(ATOM_ENTRY_BASE64.getBytes(), "application/something");
        assertEquals(ATOM_ENTRY_BASE64_CONTENT, new String(content));
    }

    @Test
    public void testAtomTitle() throws Exception {
        TempStoreOutputStreamFactory streamFactory = TempStoreOutputStreamFactory.newInstance(null, THRESHOLD,
                MAX_SIZE, false);
        AtomEntryParser aep = new AtomEntryParser(new ByteArrayInputStream(ATOM_ENTRY_NAME.getBytes()), streamFactory);

        assertNotNull(aep);
        assertNotNull(aep.getObject());
        assertNotNull(aep.getObject().getProperties());
        assertNotNull(aep.getObject().getProperties().getProperties());
        assertNotNull(aep.getObject().getProperties().getProperties().get(PropertyIds.NAME) instanceof PropertyString);

        PropertyString nameProperty = (PropertyString) aep.getObject().getProperties().getProperties()
                .get(PropertyIds.NAME);

        assertEquals("atom.title", nameProperty.getFirstValue());

        aep.release();
        assertNull(aep.getContentStream());
    }

    @Test
    public void testNullStream() throws Exception {
        TempStoreOutputStreamFactory streamFactory = TempStoreOutputStreamFactory.newInstance(null, THRESHOLD,
                MAX_SIZE, false);
        AtomEntryParser aep = new AtomEntryParser(null, streamFactory);

        assertNotNull(aep);
        assertNull(aep.getId());
        assertNull(aep.getObject());
        assertNull(aep.getContentStream());
        assertNull(aep.getProperties());
    }

    @Test(expected = XMLStreamException.class)
    public void testEmptyStream() throws Exception {
        TempStoreOutputStreamFactory streamFactory = TempStoreOutputStreamFactory.newInstance(null, THRESHOLD,
                MAX_SIZE, false);
        new AtomEntryParser(new ByteArrayInputStream(new byte[0]), streamFactory);
    }

    @Test
    public void testBigStream() throws Exception {
        byte[] begin = IOUtils
                .toUTF8Bytes("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                        + "<entry xmlns=\"http://www.w3.org/2005/Atom\" xmlns:app=\"http://www.w3.org/2007/app\" xmlns:cmisra=\"http://docs.oasis-open.org/ns/cmis/restatom/200908/\">"
                        + "<cmisra:content><cmisra:mediatype>video/raw</cmisra:mediatype><cmisra:base64>");
        byte[] end = IOUtils
                .toUTF8Bytes("</cmisra:base64></cmisra:content><cmisra:object xmlns:cmis=\"http://docs.oasis-open.org/ns/cmis/core/200908/\">"
                        + "<cmis:properties></cmis:properties></cmisra:object><title>some.file</title></entry>");

        int contenSize = 50 * 1024 * 1024;
        int base64Size = 4 * (contenSize + 2 - ((contenSize + 2) % 3)) / 3;

        byte[] entry = new byte[begin.length + base64Size + end.length];

        System.arraycopy(begin, 0, entry, 0, begin.length);
        System.arraycopy(end, 0, entry, entry.length - end.length, end.length);
        Arrays.fill(entry, begin.length, entry.length - end.length, (byte) 'a');

        TempStoreOutputStreamFactory streamFactory = TempStoreOutputStreamFactory.newInstance(null, THRESHOLD,
                MAX_SIZE, false);
        AtomEntryParser aep = new AtomEntryParser(new ByteArrayInputStream(entry), streamFactory);
        ContentStream contentStream = aep.getContentStream();

        assertNotNull(contentStream);
        assertNotNull(contentStream.getStream());

        contentStream.getStream().close();

        aep.release();
        assertNull(aep.getContentStream());
    }

    private static byte[] parse(byte[] entry, String mimeType) throws Exception {
        TempStoreOutputStreamFactory streamFactory = TempStoreOutputStreamFactory.newInstance(null, THRESHOLD,
                MAX_SIZE, false);
        AtomEntryParser aep = new AtomEntryParser(new ByteArrayInputStream(entry), streamFactory);
        ContentStream contentStream = aep.getContentStream();

        assertNotNull(contentStream);
        assertNotNull(contentStream.getStream());

        assertEquals(mimeType, contentStream.getMimeType());
        if (contentStream.getStream() instanceof ThresholdInputStream) {
            assertEquals(mimeType, ((ThresholdInputStream) contentStream.getStream()).getMimeType());
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        IOUtils.copy(contentStream.getStream(), baos);

        contentStream.getStream().close();

        aep.release();
        assertNull(aep.getContentStream());

        return baos.toByteArray();
    }
}
