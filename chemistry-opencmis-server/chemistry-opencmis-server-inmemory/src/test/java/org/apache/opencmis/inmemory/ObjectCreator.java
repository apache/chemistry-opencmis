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
package org.apache.opencmis.inmemory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.impl.dataobjects.ContentStreamDataImpl;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectService;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.PropertyStringData;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;

public class ObjectCreator {
  
  private ProviderObjectFactory fFactory;
  private ObjectService fObjSvc;
  private String fRepositoryId;

  public ObjectCreator(ProviderObjectFactory factory, ObjectService objSvc, String repositoryId) {
    fObjSvc = objSvc;
    fFactory = factory;
    fRepositoryId = repositoryId;
  }
  
  public String createDocument(String name, String typeId, String folderId, VersioningState versioningState, Map<String, String> propsToSet) {
    ContentStreamData contentStream = null;
    List<String> policies = null;
    AccessControlList addACEs = null;
    AccessControlList removeACEs = null;
    ExtensionsData extension = null;

    PropertiesData props = createStringDocumentProperties(name, typeId, propsToSet);

    contentStream = createContent();

    String id = null;
    id = fObjSvc.createDocument(fRepositoryId, props, folderId, contentStream, versioningState,
        policies, addACEs, removeACEs, extension);
    if (null == id)
      junit.framework.Assert.fail("createDocument failed.");

    return id;
  }
  
  public PropertiesData createStringDocumentProperties(String name, String typeId, Map<String, String> propsToSet) {
    List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
    properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_NAME, name));
    properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_OBJECT_TYPE_ID, typeId));
    if (null != propsToSet)
      for (Entry<String, String> propToSet : propsToSet.entrySet()) {
        properties.add(fFactory.createPropertyStringData(propToSet.getKey(), propToSet.getValue()));      
      }
    PropertiesData props = fFactory.createPropertiesData(properties);
    return props;
  }

  public ContentStreamData createContent() {
    ContentStreamDataImpl content = new ContentStreamDataImpl();
    content.setFilename("data.txt");
    content.setMimeType("text/plain");
    int len = 32 * 1024;
    byte[] b = {0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68,
                0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x0c, 0x0a,     
                0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68,
                0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x0c, 0x0a
                }; // 32 Bytes
    ByteArrayOutputStream ba = new ByteArrayOutputStream(len);
    try {
      for (int i=0; i<1024; i++)
        ba.write(b);
    } catch (IOException e) {
        throw new RuntimeException("Failed to fill content stream with data", e) ;
    }
    content.setStream(new ByteArrayInputStream(ba.toByteArray()));
    content.setLength(BigInteger.valueOf(len));
    return content;
  }
  
  public ContentStreamData createAlternateContent() {
    ContentStreamDataImpl content = new ContentStreamDataImpl();
    content.setFilename("data.txt");
    content.setMimeType("text/plain");
    int len = 32 * 1024;
    byte[] b = {0x61, 0x61, 0x61, 0x61, 0x61, 0x61, 0x61, 0x61,
        0x61, 0x61, 0x61, 0x61, 0x61, 0x61, 0x61, 0x61,     
        0x61, 0x61, 0x61, 0x61, 0x61, 0x61, 0x61, 0x61,
        0x61, 0x61, 0x61, 0x61, 0x61, 0x61, 0x61, 0x61
    }; // 32 Bytes
    ByteArrayOutputStream ba = new ByteArrayOutputStream(len);
    try {
      for (int i=0; i<1024; i++)
        ba.write(b);
    } catch (IOException e) {
        throw new RuntimeException("Failed to fill content stream with data", e) ;
    }
    content.setStream(new ByteArrayInputStream(ba.toByteArray()));
    content.setLength(BigInteger.valueOf(len));
    return content;
  }

  /**
   * Compare two streams and return true if they are equal
   * @param csd1
   * @param csd2
   * @return
   */
  public boolean verifyContent(ContentStreamData csd1, ContentStreamData csd2) {
    if (!csd1.getFilename().equals(csd2.getFilename()))
        return false;
    if (!csd1.getLength().equals(csd2.getLength()))
        return false;
    if (!csd1.getMimeType().equals(csd2.getMimeType()))
        return false;
    long len = csd1.getLength().longValue();
    InputStream s1 = csd1.getStream();
    InputStream s2 = csd2.getStream();
    try {
      for (int i=0; i<len; i++) {
        int val1 = s1.read();
        int val2 = s2.read();
        if (val1 != val2)
          return false;
        }
    }
    catch (IOException e) {
      e.printStackTrace();
      return false;
    }    
    return true;
  }
  
  public void updateProperty(String id, String propertyId, String propertyValue) {
    PropertiesData properties = getUpdatePropertyList(propertyId, propertyValue);

    Holder<String> idHolder = new Holder<String>(id);
    Holder<String> changeTokenHolder = new Holder<String>();
    fObjSvc.updateProperties(fRepositoryId, idHolder, changeTokenHolder, properties, null);    
  }

  public PropertiesData getUpdatePropertyList(String propertyId, String propertyValue) {
    List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
    properties.add(fFactory.createPropertyStringData(propertyId, propertyValue));      
    PropertiesData newProps = fFactory.createPropertiesData(properties);
    return newProps;
  }

  public boolean verifyProperty(String id, String propertyId, String propertyValue) {
      PropertiesData props = fObjSvc.getProperties(fRepositoryId, id, "*", null);
      Map<String, PropertyData<?>> propsMap = props.getProperties();
      PropertyStringData pd = (PropertyStringData) propsMap.get(propertyId);
      return propertyValue.equals(pd.getFirstValue());      
  }

}
