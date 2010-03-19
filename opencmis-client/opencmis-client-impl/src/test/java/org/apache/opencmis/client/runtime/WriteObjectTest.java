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
package org.apache.opencmis.client.runtime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.opencmis.client.api.ContentStream;
import org.apache.opencmis.client.api.Document;
import org.apache.opencmis.client.api.ObjectId;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.enums.CmisProperties;
import org.apache.opencmis.commons.enums.VersioningState;
import org.junit.Ignore;
import org.junit.Test;

public class WriteObjectTest extends AbstractSessionTest {

  @Test
  public void createFolder() {
    ObjectId parentId = this.session.createObjectId(Fixture.getTestRootId());
    String folderName = UUID.randomUUID().toString();
    String typeId = FixtureData.FOLDER_TYPE_ID.value();

    ObjectType ot = this.session.getTypeDefinition(typeId);
    Collection<PropertyDefinition<?>> pdefs = ot.getPropertyDefintions().values();
    List<Property<?>> properties = new ArrayList<Property<?>>();
    Property<?> prop = null;

    for (PropertyDefinition<?> pd : pdefs) {
      try {
        CmisProperties cmisp = CmisProperties.fromValue(pd.getId());
        switch (cmisp) {
        case NAME:
          prop = this.session.getObjectFactory().createProperty(pd, folderName);
          properties.add(prop);
          break;
        case OBJECT_TYPE_ID:
          prop = this.session.getObjectFactory().createProperty(pd, typeId);
          properties.add(prop);
          break;
        default:
          break;
        }
      }
      catch (Exception e) {
        // custom property definition
      }

    }

    ObjectId id = this.session.createFolder(properties, parentId, null, null, null);
    Assert.assertNotNull(id);
  }

  @Test
  public void createDocument() throws IOException {
    ObjectId parentId = this.session.createObjectId(Fixture.getTestRootId());
    String folderName = UUID.randomUUID().toString();
    String typeId = FixtureData.DOCUMENT_TYPE_ID.value();

    ObjectType ot = this.session.getTypeDefinition(typeId);
    Collection<PropertyDefinition<?>> pdefs = ot.getPropertyDefintions().values();
    List<Property<?>> properties = new ArrayList<Property<?>>();
    Property<?> prop = null;

    for (PropertyDefinition<?> pd : pdefs) {
      try {
        CmisProperties cmisp = CmisProperties.fromValue(pd.getId());
        switch (cmisp) {
        case NAME:
          prop = this.session.getObjectFactory().createProperty(pd, folderName);
          properties.add(prop);
          break;
        case OBJECT_TYPE_ID:
          prop = this.session.getObjectFactory().createProperty(pd, typeId);
          properties.add(prop);
          break;
        default:
          break;
        }
      }
      catch (Exception e) {
        // custom property definition (note: document type should not have further mandatory
        // properties)
      }

    }

    String filename = UUID.randomUUID().toString();
    String mimetype = "text/html; charset=UTF-8";
    String content1 = "Im Walde rauscht ein Wasserfall. Wenn's nicht mehr rauscht ist's Wasser all.";

    byte[] buf1 = content1.getBytes("UTF-8");
    ByteArrayInputStream in1 = new ByteArrayInputStream(buf1);
    ContentStream contentStream = this.session.getObjectFactory().createContentStream(filename,
        buf1.length, mimetype, in1);
    Assert.assertNotNull(contentStream);

    ObjectId id = this.session.createDocument(properties, parentId, contentStream,
        VersioningState.NONE, null, null, null);
    Assert.assertNotNull(id);

    // verify content (which is not supported by mock)
    if (this.isMock()) {
      return;  
    }
    Document doc = (Document) this.session.getObject(id);
    Assert.assertNotNull(doc);
//    Assert.assertEquals(buf1.length, doc.getContentStreamLength());
//    Assert.assertEquals(mimetype, doc.getContentStreamMimeType());
//    Assert.assertEquals(filename, doc.getContentStreamFileName());
    ContentStream readStream = doc.getContentStream();
    InputStream in2 = readStream.getStream();
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
    String content2 = sbuf.toString();
    Assert.assertEquals(content1, content2);
  }
}
