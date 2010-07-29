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

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyBoolean;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyDateTime;
import org.apache.chemistry.opencmis.commons.data.PropertyDecimal;
import org.apache.chemistry.opencmis.commons.data.PropertyHtml;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyInteger;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.data.PropertyUri;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;

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

    public ContentStream createContentStream(String filename, BigInteger length, String mimetype, InputStream stream) {
        return new ContentStreamImpl(filename, length, mimetype, stream);
    }

    public Properties createPropertiesData(List<PropertyData<?>> properties) {
        return new PropertiesImpl(properties);
    }

    public PropertyBoolean createPropertyBooleanData(String id, List<Boolean> values) {
        return new PropertyBooleanImpl(id, values);
    }

    public PropertyBoolean createPropertyBooleanData(String id, Boolean value) {
        return new PropertyBooleanImpl(id, value);
    }

    public PropertyDateTime createPropertyDateTimeData(String id, List<GregorianCalendar> values) {
        return new PropertyDateTimeImpl(id, values);
    }

    public PropertyDateTime createPropertyDateTimeData(String id, GregorianCalendar value) {
        return new PropertyDateTimeImpl(id, value);
    }

    public PropertyDecimal createPropertyDecimalData(String id, List<BigDecimal> values) {
        return new PropertyDecimalImpl(id, values);
    }

    public PropertyDecimal createPropertyDecimalData(String id, BigDecimal value) {
        return new PropertyDecimalImpl(id, value);
    }

    public PropertyHtml createPropertyHtmlData(String id, List<String> values) {
        return new PropertyHtmlImpl(id, values);
    }

    public PropertyHtml createPropertyHtmlData(String id, String value) {
        return new PropertyHtmlImpl(id, value);
    }

    public PropertyId createPropertyIdData(String id, List<String> values) {
        return new PropertyIdImpl(id, values);
    }

    public PropertyId createPropertyIdData(String id, String value) {
        return new PropertyIdImpl(id, value);
    }

    public PropertyInteger createPropertyIntegerData(String id, List<BigInteger> values) {
        return new PropertyIntegerImpl(id, values);
    }

    public PropertyInteger createPropertyIntegerData(String id, BigInteger value) {
        return new PropertyIntegerImpl(id, value);
    }

    public PropertyString createPropertyStringData(String id, List<String> values) {
        return new PropertyStringImpl(id, values);
    }

    public PropertyString createPropertyStringData(String id, String value) {
        return new PropertyStringImpl(id, value);
    }

    public PropertyUri createPropertyUriData(String id, List<String> values) {
        return new PropertyUriImpl(id, values);
    }

    public PropertyUri createPropertyUriData(String id, String value) {
        return new PropertyUriImpl(id, value);
    }
}
