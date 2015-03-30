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
package org.apache.chemistry.opencmis.commons.impl.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyDateTime;
import org.apache.chemistry.opencmis.commons.enums.DateTimeFormat;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.DateTimeHelper;
import org.apache.chemistry.opencmis.commons.impl.JSONConstants;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter.PropertyMode;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.junit.Test;

public class DateTimeTimezoneTest {

    private final static String TEST_PROP = "test:prop";
    private final static String TIMEZONE = "GMT+01:23";

    @SuppressWarnings("unchecked")
    @Test
    public void testDateTimePropertyWrite() {
        GregorianCalendar orgValue = new GregorianCalendar(TimeZone.getTimeZone(TIMEZONE));

        PropertiesImpl props = new PropertiesImpl();
        props.addProperty(new PropertyDateTimeImpl(TEST_PROP, orgValue));

        // milliseconds

        JSONObject json1 = JSONConverter.convert(props, "id", null, PropertyMode.OBJECT, false, DateTimeFormat.SIMPLE);

        assertNotNull(json1);
        assertNotNull(json1.get(TEST_PROP));

        Map<String, Object> prop1 = (Map<String, Object>) json1.get(TEST_PROP);
        assertNotNull(prop1.get(JSONConstants.JSON_PROPERTY_VALUE));
        assertTrue(prop1.get(JSONConstants.JSON_PROPERTY_VALUE) instanceof List<?>);

        List<?> values1 = (List<?>) prop1.get(JSONConstants.JSON_PROPERTY_VALUE);
        assertEquals(1, values1.size());
        assertTrue(values1.get(0) instanceof Number);

        assertEquals(orgValue.getTimeInMillis(), ((Number) values1.get(0)).longValue());

        // DateTime string

        JSONObject json2 = JSONConverter
                .convert(props, "id", null, PropertyMode.OBJECT, false, DateTimeFormat.EXTENDED);

        assertNotNull(json2);
        assertNotNull(json2.get(TEST_PROP));

        Map<String, Object> prop2 = (Map<String, Object>) json2.get(TEST_PROP);
        assertNotNull(prop2.get(JSONConstants.JSON_PROPERTY_VALUE));
        assertTrue(prop2.get(JSONConstants.JSON_PROPERTY_VALUE) instanceof List<?>);

        List<?> values2 = (List<?>) prop2.get(JSONConstants.JSON_PROPERTY_VALUE);
        assertEquals(1, values2.size());
        assertTrue(values2.get(0) instanceof String);

        GregorianCalendar calValue = DateTimeHelper.parseXmlDateTime(values2.get(0).toString());
        assertEquals(orgValue.getTimeInMillis(), calValue.getTimeInMillis());
        assertTrue(orgValue.getTimeZone().hasSameRules(calValue.getTimeZone()));
    }

    @Test
    public void testDateTimePropertyRead() {

        GregorianCalendar orgValue = new GregorianCalendar(TimeZone.getTimeZone(TIMEZONE));

        Map<String, Object> json = new HashMap<String, Object>();
        Map<String, Object> propJson = new HashMap<String, Object>();

        json.put(TEST_PROP, propJson);

        // milliseconds

        propJson.put(JSONConstants.JSON_PROPERTY_ID, TEST_PROP);
        propJson.put(JSONConstants.JSON_PROPERTY_DATATYPE, PropertyType.DATETIME.value());
        propJson.put(JSONConstants.JSON_PROPERTY_VALUE, orgValue.getTimeInMillis());

        Properties props1 = JSONConverter.convertProperties(json, null);

        assertNotNull(props1);
        assertNotNull(props1.getProperties());
        assertNotNull(props1.getProperties().get(TEST_PROP));
        assertTrue(props1.getProperties().get(TEST_PROP) instanceof PropertyDateTime);

        GregorianCalendar value1 = ((PropertyDateTime) props1.getProperties().get(TEST_PROP)).getFirstValue();

        assertEquals(orgValue.getTimeInMillis(), value1.getTimeInMillis());
        assertTrue(value1.getTimeZone().hasSameRules(TimeZone.getTimeZone("GMT")));

        // DateTime string

        propJson.put(JSONConstants.JSON_PROPERTY_ID, TEST_PROP);
        propJson.put(JSONConstants.JSON_PROPERTY_DATATYPE, PropertyType.DATETIME.value());
        propJson.put(JSONConstants.JSON_PROPERTY_VALUE, DateTimeHelper.formatXmlDateTime(orgValue));

        Properties props2 = JSONConverter.convertProperties(json, null);

        assertNotNull(props2);
        assertNotNull(props2.getProperties());
        assertNotNull(props2.getProperties().get(TEST_PROP));
        assertTrue(props2.getProperties().get(TEST_PROP) instanceof PropertyDateTime);

        GregorianCalendar value2 = ((PropertyDateTime) props2.getProperties().get(TEST_PROP)).getFirstValue();

        assertEquals(orgValue.getTimeInMillis(), value2.getTimeInMillis());
        assertTrue(value2.getTimeZone().hasSameRules(TimeZone.getTimeZone(TIMEZONE)));
    }
}
