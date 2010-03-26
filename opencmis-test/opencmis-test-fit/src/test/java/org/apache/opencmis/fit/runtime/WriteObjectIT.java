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
package org.apache.opencmis.fit.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.opencmis.client.api.ContentStream;
import org.apache.opencmis.client.api.Document;
import org.apache.opencmis.client.api.ObjectId;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.enums.CmisProperties;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
import org.junit.Test;

public class WriteObjectIT extends AbstractSessionTest {

	@Test
	public void createFolder() {
		ObjectId parentId = this.session
				.createObjectId(Fixture.getTestRootId());
		String folderName = UUID.randomUUID().toString();
		String typeId = FixtureData.FOLDER_TYPE_ID.value();

		ObjectType ot = this.session.getTypeDefinition(typeId);
		Collection<PropertyDefinition<?>> pdefs = ot.getPropertyDefintions()
				.values();
		List<Property<?>> properties = new ArrayList<Property<?>>();
		Property<?> prop = null;

		for (PropertyDefinition<?> pd : pdefs) {
			try {
				CmisProperties cmisp = CmisProperties.fromValue(pd.getId());
				switch (cmisp) {
				case NAME:
					prop = this.session.getObjectFactory().createProperty(pd,
							folderName);
					properties.add(prop);
					break;
				case OBJECT_TYPE_ID:
					prop = this.session.getObjectFactory().createProperty(pd,
							typeId);
					properties.add(prop);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				// custom property definition
			}

		}

		ObjectId id = this.session.createFolder(properties, parentId, null,
				null, null);
		assertNotNull(id);
	}

	@Test
	public void createDocument() throws IOException {
		ObjectId parentId = this.session
				.createObjectId(Fixture.getTestRootId());
		String folderName = UUID.randomUUID().toString();
		String typeId = FixtureData.DOCUMENT_TYPE_ID.value();

		ObjectType ot = this.session.getTypeDefinition(typeId);
		Collection<PropertyDefinition<?>> pdefs = ot.getPropertyDefintions()
				.values();
		List<Property<?>> properties = new ArrayList<Property<?>>();
		Property<?> prop = null;

		for (PropertyDefinition<?> pd : pdefs) {
			try {
				CmisProperties cmisp = CmisProperties.fromValue(pd.getId());
				switch (cmisp) {
				case NAME:
					prop = this.session.getObjectFactory().createProperty(pd,
							folderName);
					properties.add(prop);
					break;
				case OBJECT_TYPE_ID:
					prop = this.session.getObjectFactory().createProperty(pd,
							typeId);
					properties.add(prop);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				/*
				 * custom property definition (note: document type should not
				 * have further mandatory properties)
				 */
				this.log
						.info(
								"Custom property found but not supported in test case!",
								e);
			}
		}

		String filename = UUID.randomUUID().toString();
		String mimetype = "text/html; charset=UTF-8";
		String content1 = "Im Walde rauscht ein Wasserfall. Wenn's nicht mehr rauscht ist's Wasser all.";

		byte[] buf1 = content1.getBytes("UTF-8");
		ByteArrayInputStream in1 = new ByteArrayInputStream(buf1);
		ContentStream contentStream = this.session.getObjectFactory()
				.createContentStream(filename, buf1.length, mimetype, in1);
		assertNotNull(contentStream);

		ObjectId id = this.session.createDocument(properties, parentId,
				contentStream, VersioningState.NONE, null, null, null);
		assertNotNull(id);

		// verify content 
		Document doc = (Document) this.session.getObject(id);
		assertNotNull(doc);
		// Assert.assertEquals(buf1.length, doc.getContentStreamLength());
		// Assert.assertEquals(mimetype, doc.getContentStreamMimeType());
		// Assert.assertEquals(filename, doc.getContentStreamFileName());
		String content2 = this.getContentAsString(doc.getContentStream());
		assertEquals(content1, content2);
	}

	@Test
	public void createDocumentFromSource() throws IOException {
		try {
			// verify content 
			String path = "/" + Fixture.TEST_ROOT_FOLDER_NAME + "/"
					+ FixtureData.DOCUMENT1_NAME;
			Document srcDocument = (Document) this.session
					.getObjectByPath(path);
			assertNotNull("Document not found: " + path, srcDocument);
			String srcContent = this.getContentAsString(srcDocument
					.getContentStream());

			ObjectId parentFolder = session.createObjectId(Fixture
					.getTestRootId());
			List<Property<?>> srcProperties = srcDocument.getProperties();
			assertNotNull(srcProperties);
			List<Property<?>> dstProperties = new ArrayList<Property<?>>();

			for (Property<?> p : srcProperties) {
				if (p.getId().equalsIgnoreCase(CmisProperties.NAME.value())) {
					// change the name
					String name = UUID.randomUUID().toString();
					Property<String> pn = this.session.getObjectFactory()
							.createProperty(p.getDefinition(), name);
					dstProperties.add(pn);
				} else {
					dstProperties.add(p);
				}
			}

			ObjectId dstDocumentId = this.session.createDocumentFromSource(
					srcDocument, dstProperties, parentFolder,
					VersioningState.NONE, null, null, null);
			assertNotNull(dstDocumentId);
			Document dstDocument = (Document) this.session
					.getObject(dstDocumentId);
			String dstContent = this.getContentAsString(dstDocument
					.getContentStream());
			assertEquals(srcContent, dstContent);

		} catch (CmisNotSupportedException e) {
			// not an error
			this.log.info(e.getMessage());
		}
	}

	@Test
	public void deleteAndCreateContent() throws IOException {
		// verify content

		String path = "/" + Fixture.TEST_ROOT_FOLDER_NAME + "/"
				+ FixtureData.DOCUMENT1_NAME;
		Document document = (Document) this.session.getObjectByPath(path);
		assertNotNull("Document not found: " + path, document);

		// check default content
		ContentStream contentStream = document.getContentStream();
		assertNotNull(contentStream);
		String contentString = this.getContentAsString(contentStream);
		assertNotNull(contentString);

		// delete and set new content
		// ObjectId id = (return id not supported by AtomPub)
			document.deleteContentStream();
		// assertNotNull(id);

		String filename = UUID.randomUUID().toString();
		String mimetype = "text/html; charset=UTF-8";
		String content1 = "Im Walde rauscht ein Wasserfall. Wenn's nicht mehr rauscht ist's Wasser all.";

		byte[] buf1 = content1.getBytes("UTF-8");
		ByteArrayInputStream in1 = new ByteArrayInputStream(buf1);
		contentStream = this.session.getObjectFactory().createContentStream(
				filename, buf1.length, mimetype, in1);
		assertNotNull(contentStream);

		document.setContentStream(true, contentStream);

		// check default content
		ContentStream contentStream2 = document.getContentStream();
		assertNotNull(contentStream2);
		String contentString2 = this.getContentAsString(contentStream2);
		assertNotNull(contentString2);

		assertEquals(content1, contentString2);
	}

	@Test
	public void updateProperties() {
		// verify content
		String path = "/" + Fixture.TEST_ROOT_FOLDER_NAME + "/"
				+ FixtureData.DOCUMENT1_NAME;
		Document document = (Document) this.session.getObjectByPath(path);
		assertNotNull("Document not found: " + path, document);

		document.setProperty(CmisProperties.NAME.value(), "Neuer Name");
		document.updateProperties();
		assertTrue(true);
	}

	private String getContentAsString(ContentStream stream) throws IOException {
		assertNotNull(stream);
		InputStream in2 = stream.getStream();
		assertNotNull(in2);
		StringBuffer sbuf = null;
		sbuf = new StringBuffer(in2.available());
		int count;
		byte[] buf2 = new byte[100];
		while ((count = in2.read(buf2)) != -1) {
			for (int i = 0; i < count; i++) {
				sbuf.append((char) buf2[i]);
			}
		}
		in2.close();
		return sbuf.toString();
	}
}
