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
package org.apache.opencmis.commons.impl.dataobjects;

import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.ContentStreamDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertiesDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyBooleanDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyDateTimeDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyDecimalDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyHtmlDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyIdDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyIntegerDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyStringDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyUriDataImpl;
import org.apache.opencmis.commons.provider.AccessControlEntry;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyBooleanData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.PropertyDateTimeData;
import org.apache.opencmis.commons.provider.PropertyDecimalData;
import org.apache.opencmis.commons.provider.PropertyHtmlData;
import org.apache.opencmis.commons.provider.PropertyIdData;
import org.apache.opencmis.commons.provider.PropertyIntegerData;
import org.apache.opencmis.commons.provider.PropertyStringData;
import org.apache.opencmis.commons.provider.PropertyUriData;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;

/**
 * CMIS Provider object factory implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class ProviderObjectFactoryImpl implements ProviderObjectFactory, Serializable {

  private static final long serialVersionUID = 1L;

  public ProviderObjectFactoryImpl() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createAccessControlEntry(java.lang
   * .String, java.util.List)
   */
  public AccessControlEntry createAccessControlEntry(String principal, List<String> permissions) {
    return new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(principal), permissions);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createAccessControlList(java.util
   * .List)
   */
  public AccessControlList createAccessControlList(List<AccessControlEntry> aces) {
    return new AccessControlListImpl(aces);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createContentStream(java.math.BigInteger
   * , java.lang.String, java.lang.String, java.io.InputStream)
   */
  public ContentStreamData createContentStream(BigInteger length, String mimetype, String filename,
      InputStream stream) {
    return new ContentStreamDataImpl(length, mimetype, filename, stream);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertiesData(java.util.List)
   */
  public PropertiesData createPropertiesData(List<PropertyData<?>> properties) {
    return new PropertiesDataImpl(properties);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyBooleanData(java.lang
   * .String, java.util.List)
   */
  public PropertyBooleanData createPropertyBooleanData(String id, List<Boolean> values) {
    return new PropertyBooleanDataImpl(id, values);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyBooleanData(java.lang
   * .String, java.lang.Boolean)
   */
  public PropertyBooleanData createPropertyBooleanData(String id, Boolean value) {
    return new PropertyBooleanDataImpl(id, value);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyDateTimeData(java.lang
   * .String, java.util.List)
   */
  public PropertyDateTimeData createPropertyDateTimeData(String id, List<GregorianCalendar> values) {
    return new PropertyDateTimeDataImpl(id, values);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyDateTimeData(java.lang
   * .String, java.util.GregorianCalendar)
   */
  public PropertyDateTimeData createPropertyDateTimeData(String id, GregorianCalendar value) {
    return new PropertyDateTimeDataImpl(id, value);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyDecimalData(java.lang
   * .String, java.util.List)
   */
  public PropertyDecimalData createPropertyDecimalData(String id, List<BigDecimal> values) {
    return new PropertyDecimalDataImpl(id, values);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyDecimalData(java.lang
   * .String, java.math.BigDecimal)
   */
  public PropertyDecimalData createPropertyDecimalData(String id, BigDecimal value) {
    return new PropertyDecimalDataImpl(id, value);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyHtmlData(java.lang.
   * String, java.util.List)
   */
  public PropertyHtmlData createPropertyHtmlData(String id, List<String> values) {
    return new PropertyHtmlDataImpl(id, values);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyHtmlData(java.lang.
   * String, java.lang.String)
   */
  public PropertyHtmlData createPropertyHtmlData(String id, String value) {
    return new PropertyHtmlDataImpl(id, value);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyIdData(java.lang.String
   * , java.util.List)
   */
  public PropertyIdData createPropertyIdData(String id, List<String> values) {
    return new PropertyIdDataImpl(id, values);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyIdData(java.lang.String
   * , java.lang.String)
   */
  public PropertyIdData createPropertyIdData(String id, String value) {
    return new PropertyIdDataImpl(id, value);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyIntegerData(java.lang
   * .String, java.util.List)
   */
  public PropertyIntegerData createPropertyIntegerData(String id, List<BigInteger> values) {
    return new PropertyIntegerDataImpl(id, values);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyIntegerData(java.lang
   * .String, java.math.BigInteger)
   */
  public PropertyIntegerData createPropertyIntegerData(String id, BigInteger value) {
    return new PropertyIntegerDataImpl(id, value);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyStringData(java.lang
   * .String, java.util.List)
   */
  public PropertyStringData createPropertyStringData(String id, List<String> values) {
    return new PropertyStringDataImpl(id, values);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyStringData(java.lang
   * .String, java.lang.String)
   */
  public PropertyStringData createPropertyStringData(String id, String value) {
    return new PropertyStringDataImpl(id, value);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyUriData(java.lang.String
   * , java.util.List)
   */
  public PropertyUriData createPropertyUriData(String id, List<String> values) {
    return new PropertyUriDataImpl(id, values);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ProviderObjectFactory#createPropertyUriData(java.lang.String
   * , java.lang.String)
   */
  public PropertyUriData createPropertyUriData(String id, String value) {
    return new PropertyUriDataImpl(id, value);
  }
}
