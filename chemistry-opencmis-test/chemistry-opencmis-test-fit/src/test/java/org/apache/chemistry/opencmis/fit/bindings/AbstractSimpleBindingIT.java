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
package org.apache.chemistry.opencmis.fit.bindings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.api.ContentStream;
import org.apache.chemistry.opencmis.commons.api.ObjectData;
import org.apache.chemistry.opencmis.commons.api.Properties;
import org.apache.chemistry.opencmis.commons.api.PropertyData;
import org.apache.chemistry.opencmis.commons.api.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.junit.Test;

/**
 * Really simple binding layer test. It just contains a few smoke tests to make sure that the
 * provider layer is working properly in the integration test environment.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public abstract class AbstractSimpleBindingIT extends AbstractBindingIT {

  @Test
  public void testRepositoryInfo() {
    RepositoryInfo ri = getBinding().getRepositoryService().getRepositoryInfo(
        getRepositoryId(), null);
    assertNotNull(ri);

    assertEquals(getRepositoryId(), ri.getId());
    assertNotNull(ri.getProductName());
    assertNotNull(ri.getRootFolderId());
    assertNotNull(ri.getCapabilities());
  }

  @Test
  public void testCreateDocument() {
    // set up properties
    List<PropertyData<?>> propertyList = new ArrayList<PropertyData<?>>();
    propertyList.add(getBinding().getObjectFactory().createPropertyStringData(
        PropertyIds.NAME, "testdoc.txt"));
    propertyList.add(getBinding().getObjectFactory().createPropertyIdData(
        PropertyIds.OBJECT_TYPE_ID, DOCUMENT_TYPE));

    Properties properties = getBinding().getObjectFactory().createPropertiesData(propertyList);

    // set up content
    byte[] content = "This is a test file!".getBytes();

    ContentStream contentStream = getBinding().getObjectFactory()
        .createContentStream("test", BigInteger.valueOf(content.length), "text/plain",
            new ByteArrayInputStream(content));

    // create document
    String docId = getBinding().getObjectService().createDocument(getRepositoryId(), properties,
        getTestFolderId(), contentStream, VersioningState.NONE, null, null, null, null);
    assertNotNull(docId);

    // get the document
    ObjectData object = getBinding().getObjectService().getObject(getRepositoryId(), docId, null,
        false, IncludeRelationships.NONE, null, false, false, null);
    assertNotNull(object);
    assertEquals(docId, object.getId());

    // delete the document
    getBinding().getObjectService().deleteObject(getRepositoryId(), docId, true, null);
  }
}
