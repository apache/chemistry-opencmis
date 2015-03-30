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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.impl.CollectionsHelper;
import org.junit.Test;

public class CollectionsHelperTest {

    @Test
    public void testHelpers() {
        assertTrue(CollectionsHelper.isNullOrEmpty((List<String>) null));
        assertTrue(CollectionsHelper.isNullOrEmpty((Map<String, String>) null));
        assertFalse(CollectionsHelper.isNotEmpty((List<String>) null));
        assertFalse(CollectionsHelper.isNotEmpty((Map<String, String>) null));

        assertTrue(CollectionsHelper.isNullOrEmpty(Collections.emptyList()));
        assertTrue(CollectionsHelper.isNullOrEmpty(Collections.emptyMap()));
        assertFalse(CollectionsHelper.isNotEmpty(Collections.emptyList()));
        assertFalse(CollectionsHelper.isNotEmpty(Collections.emptyMap()));

        assertFalse(CollectionsHelper.isNullOrEmpty(Collections.singletonList("value")));
        assertFalse(CollectionsHelper.isNullOrEmpty(Collections.singletonMap("key", "value")));
        assertTrue(CollectionsHelper.isNotEmpty(Collections.singletonList("value")));
        assertTrue(CollectionsHelper.isNotEmpty(Collections.singletonMap("key", "value")));
    }
}
