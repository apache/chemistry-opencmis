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
package org.apache.chemistry.opencmis.commons.impl.dataobjects;

import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.chemistry.opencmis.commons.bindings.Ace;
import org.apache.chemistry.opencmis.commons.bindings.Acl;
import org.apache.chemistry.opencmis.commons.bindings.BindingsObjectFactory;
import org.apache.chemistry.opencmis.commons.bindings.ContentStream;
import org.apache.chemistry.opencmis.commons.bindings.PropertiesData;
import org.apache.chemistry.opencmis.commons.bindings.PropertyBooleanData;
import org.apache.chemistry.opencmis.commons.bindings.PropertyData;
import org.apache.chemistry.opencmis.commons.bindings.PropertyDateTimeData;
import org.apache.chemistry.opencmis.commons.bindings.PropertyDecimalData;
import org.apache.chemistry.opencmis.commons.bindings.PropertyHtmlData;
import org.apache.chemistry.opencmis.commons.bindings.PropertyIdData;
import org.apache.chemistry.opencmis.commons.bindings.PropertyIntegerData;
import org.apache.chemistry.opencmis.commons.bindings.PropertyStringData;
import org.apache.chemistry.opencmis.commons.bindings.PropertyUriData;

/**
 * CMIS binding object factory implementation.
 *
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 *
 */
public class BindingsObjectFactoryImpl implements BindingsObjectFactory, Serializable {

  private static final long serialVersionUID = 1L;

  public BindingsObjectFactoryImpl() {
  }

  public Ace createAccessControlEntry(String principal, List<String> permissions) {
    return new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(principal), permissions);
  }

  public Acl createAccessControlList(List<Ace> aces) {
    return new AccessControlListImpl(aces);
  }

  public ContentStream createContentStream(String filename, BigInteger length, String mimetype,
      InputStream stream) {
    return new ContentStreamImpl(filename, length, mimetype, stream);
  }

  public PropertiesData createPropertiesData(List<PropertyData<?>> properties) {
    return new PropertiesDataImpl(properties);
  }

  public PropertyBooleanData createPropertyBooleanData(String id, List<Boolean> values) {
    return new PropertyBooleanDataImpl(id, values);
  }

  public PropertyBooleanData createPropertyBooleanData(String id, Boolean value) {
    return new PropertyBooleanDataImpl(id, value);
  }

  public PropertyDateTimeData createPropertyDateTimeData(String id, List<GregorianCalendar> values) {
    return new PropertyDateTimeDataImpl(id, values);
  }

  public PropertyDateTimeData createPropertyDateTimeData(String id, GregorianCalendar value) {
    return new PropertyDateTimeDataImpl(id, value);
  }

  public PropertyDecimalData createPropertyDecimalData(String id, List<BigDecimal> values) {
    return new PropertyDecimalDataImpl(id, values);
  }

  public PropertyDecimalData createPropertyDecimalData(String id, BigDecimal value) {
    return new PropertyDecimalDataImpl(id, value);
  }

  public PropertyHtmlData createPropertyHtmlData(String id, List<String> values) {
    return new PropertyHtmlDataImpl(id, values);
  }

  public PropertyHtmlData createPropertyHtmlData(String id, String value) {
    return new PropertyHtmlDataImpl(id, value);
  }

  public PropertyIdData createPropertyIdData(String id, List<String> values) {
    return new PropertyIdDataImpl(id, values);
  }

  public PropertyIdData createPropertyIdData(String id, String value) {
    return new PropertyIdDataImpl(id, value);
  }

  public PropertyIntegerData createPropertyIntegerData(String id, List<BigInteger> values) {
    return new PropertyIntegerDataImpl(id, values);
  }

  public PropertyIntegerData createPropertyIntegerData(String id, BigInteger value) {
    return new PropertyIntegerDataImpl(id, value);
  }

  public PropertyStringData createPropertyStringData(String id, List<String> values) {
    return new PropertyStringDataImpl(id, values);
  }

  public PropertyStringData createPropertyStringData(String id, String value) {
    return new PropertyStringDataImpl(id, value);
  }

  public PropertyUriData createPropertyUriData(String id, List<String> values) {
    return new PropertyUriDataImpl(id, values);
  }

  public PropertyUriData createPropertyUriData(String id, String value) {
    return new PropertyUriDataImpl(id, value);
  }
}
