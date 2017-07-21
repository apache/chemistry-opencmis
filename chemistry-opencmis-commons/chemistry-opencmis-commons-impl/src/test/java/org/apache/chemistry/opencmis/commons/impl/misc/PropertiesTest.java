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
package org.apache.chemistry.opencmis.commons.impl.misc;

import org.apache.chemistry.opencmis.commons.data.MutableProperties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import static org.junit.Assert.*;
import org.junit.Test;

public class PropertiesTest {

    @Test
    /* see CMIS-1041 */
    public void testProperties() throws Exception {
        MutableProperties properties = new PropertiesImpl();
        PropertyData<String> propertyOne = new PropertyStringImpl("my:test", "testOne");
        PropertyData<String> propertyTwo = new PropertyStringImpl("my:test", "testTwo");

        properties.addProperty(propertyOne);
        assertEquals(1, properties.getProperties().size());
        assertEquals(1, properties.getPropertyList().size());

        properties.replaceProperty(propertyTwo);
        assertEquals(1, properties.getProperties().size());
        assertEquals(1, properties.getPropertyList().size());

        try {
            properties.addProperty(propertyOne);
            fail("adding an existing property must fail");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
