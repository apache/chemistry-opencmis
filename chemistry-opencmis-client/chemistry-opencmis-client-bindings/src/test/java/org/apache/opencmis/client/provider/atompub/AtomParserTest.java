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
package org.apache.opencmis.client.provider.atompub;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import junit.framework.TestCase;

import org.apache.opencmis.client.provider.spi.atompub.AtomEntryWriter;
import org.apache.opencmis.client.provider.spi.atompub.AtomPubParser;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomBase;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomElement;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomEntry;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertiesType;
import org.apache.opencmis.commons.impl.jaxb.CmisProperty;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyDecimal;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyInteger;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyString;

/**
 * Minimal test for AtomEntryWriter and AtomPubParser.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AtomParserTest extends TestCase {

  private static final byte[] CONTENT = "This is my test content!".getBytes();
  private static final String CONTENT_TYPE = "text/plain";

  public void testParser() throws Exception {
    ByteArrayOutputStream bao = new ByteArrayOutputStream();

    // set up an object
    CmisPropertiesType properties = new CmisPropertiesType();

    CmisPropertyString propName = new CmisPropertyString();
    propName.setPropertyDefinitionId(PropertyIds.CMIS_NAME);
    propName.getValue().add("TestName");
    properties.getProperty().add(propName);

    CmisPropertyInteger propInt = new CmisPropertyInteger();
    propInt.setPropertyDefinitionId("IntProp");
    propInt.getValue().add(BigInteger.valueOf(1));
    propInt.getValue().add(BigInteger.valueOf(2));
    propInt.getValue().add(BigInteger.valueOf(3));
    properties.getProperty().add(propInt);

    CmisPropertyDecimal propDec = new CmisPropertyDecimal();
    propDec.setPropertyDefinitionId("DecProp");
    propDec.getValue().add(
        new BigDecimal("3.14159253589793238462643383279502884197"
            + "169399375105820974944592307816406286208998628034825342117067982148086513"));
    properties.getProperty().add(propDec);

    CmisObjectType object1 = new CmisObjectType();
    object1.setProperties(properties);

    // write the entry
    AtomEntryWriter aew = new AtomEntryWriter(object1, CONTENT_TYPE, new ByteArrayInputStream(
        CONTENT));
    aew.write(bao);

    byte[] entryContent = bao.toByteArray();
    assertTrue(entryContent.length > 0);

    String entryContentStr = new String(entryContent, "UTF-8");
    System.out.println(entryContentStr);

    // parse it
    AtomPubParser parser = new AtomPubParser(new ByteArrayInputStream(entryContent));
    parser.parse();
    AtomBase parseResult = parser.getResults();

    assertTrue(parseResult instanceof AtomEntry);
    AtomEntry entry = (AtomEntry) parseResult;

    assertNotNull(entry);
    assertTrue(entry.getElements().size() > 0);

    // find the object
    CmisObjectType object2 = null;
    for (AtomElement element : entry.getElements()) {
      if (element.getObject() instanceof CmisObjectType) {
        assertNull(object2);
        object2 = (CmisObjectType) element.getObject();
      }
    }

    assertNotNull(object2);
    assertNotNull(object2.getProperties());

    // compare properteis
    for (CmisProperty property1 : object1.getProperties().getProperty()) {
      boolean found = false;

      for (CmisProperty property2 : object2.getProperties().getProperty()) {
        if (property1.getPropertyDefinitionId().equals(property2.getPropertyDefinitionId())) {
          found = true;

          assertEquals(property1, property2);
          break;
        }
      }

      assertTrue(found);
    }
  }

  protected void assertEquals(CmisProperty expected, CmisProperty actual) throws Exception {
    if ((expected == null) && (actual == null)) {
      return;
    }

    if ((expected == null) || (actual == null)) {
      fail("Property is null!");
    }

    assertEquals(expected.getPropertyDefinitionId(), actual.getPropertyDefinitionId());
    assertEquals(expected.getClass(), actual.getClass());

    Method m1 = expected.getClass().getMethod("getValue", new Class<?>[0]);
    List<?> values1 = (List<?>) m1.invoke(expected, new Object[0]);
    assertNotNull(values1);
    assertFalse(values1.isEmpty());

    Method m2 = actual.getClass().getMethod("getValue", new Class<?>[0]);
    List<?> values2 = (List<?>) m2.invoke(actual, new Object[0]);
    assertNotNull(values2);
    assertFalse(values2.isEmpty());

    assertEquals(values1.size(), values2.size());

    for (int i = 0; i < values1.size(); i++) {
      assertEquals(values1.get(i), values2.get(i));
    }
  }
}
