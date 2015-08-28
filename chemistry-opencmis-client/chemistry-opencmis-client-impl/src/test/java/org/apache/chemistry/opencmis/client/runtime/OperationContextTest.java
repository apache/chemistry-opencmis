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
package org.apache.chemistry.opencmis.client.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class OperationContextTest {

    @Test
    public void testFilter() {
        OperationContextImpl oc = new OperationContextImpl();

        oc.setFilterString("p1 ,p2, p3");
        Set<String> filter = oc.getFilter();

        assertEquals(6, filter.size());
        assertTrue(filter.contains("p1"));
        assertTrue(filter.contains("p2"));
        assertTrue(filter.contains("p3"));
        assertTrue(filter.contains("cmis:objectId"));
        assertTrue(filter.contains("cmis:objectTypeId"));
        assertTrue(filter.contains("cmis:baseTypeId"));

        oc.setFilterString("*");
        filter = oc.getFilter();

        assertEquals(1, filter.size());
        assertTrue(filter.contains("*"));
    }

    @Test
    public void testRenditionFilter() {
        OperationContextImpl oc = new OperationContextImpl();

        oc.setRenditionFilterString("a/b , c/d");
        Set<String> filter = oc.getRenditionFilter();

        assertEquals(2, filter.size());
        assertTrue(filter.contains("a/b"));
        assertTrue(filter.contains("c/d"));

        oc.setRenditionFilterString("");
        filter = oc.getRenditionFilter();

        assertEquals(1, filter.size());
        assertTrue(filter.contains("cmis:none"));

        oc.setRenditionFilterString(null);
        filter = oc.getRenditionFilter();

        assertEquals(1, filter.size());
        assertTrue(filter.contains("cmis:none"));
    }
}
