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
package org.apache.opencmis.commons.provider;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Factory for CMIS provider objects.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public interface ProviderObjectFactory {

  AccessControlEntry createAccessControlEntry(String principal, List<String> permissions);

  AccessControlList createAccessControlList(List<AccessControlEntry> aces);

  PropertyBooleanData createPropertyBooleanData(String id, List<Boolean> values);

  PropertyBooleanData createPropertyBooleanData(String id, Boolean value);

  PropertyIdData createPropertyIdData(String id, List<String> values);

  PropertyIdData createPropertyIdData(String id, String value);

  PropertyIntegerData createPropertyIntegerData(String id, List<BigInteger> values);

  PropertyIntegerData createPropertyIntegerData(String id, BigInteger value);

  PropertyDateTimeData createPropertyDateTimeData(String id, List<GregorianCalendar> values);

  PropertyDateTimeData createPropertyDateTimeData(String id, GregorianCalendar value);

  PropertyDecimalData createPropertyDecimalData(String id, List<BigDecimal> values);

  PropertyDecimalData createPropertyDecimalData(String id, BigDecimal value);

  PropertyHtmlData createPropertyHtmlData(String id, List<String> values);

  PropertyHtmlData createPropertyHtmlData(String id, String value);

  PropertyStringData createPropertyStringData(String id, List<String> values);

  PropertyStringData createPropertyStringData(String id, String value);

  PropertyUriData createPropertyUriData(String id, List<String> values);

  PropertyUriData createPropertyUriData(String id, String value);

  PropertiesData createPropertiesData(List<PropertyData<?>> properties);

  ContentStreamData createContentStream(BigInteger length, String mimetype, String filename,
      InputStream stream);
}
