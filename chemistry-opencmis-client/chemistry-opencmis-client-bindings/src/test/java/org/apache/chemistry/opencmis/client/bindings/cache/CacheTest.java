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
package org.apache.chemistry.opencmis.client.bindings.cache;

import junit.framework.TestCase;

import org.apache.chemistry.opencmis.client.bindings.cache.impl.CacheImpl;
import org.apache.chemistry.opencmis.client.bindings.cache.impl.ContentTypeCacheLevelImpl;
import org.apache.chemistry.opencmis.client.bindings.cache.impl.LruCacheLevelImpl;
import org.apache.chemistry.opencmis.client.bindings.cache.impl.MapCacheLevelImpl;

/**
 * Tests the cache implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class CacheTest extends TestCase {

    public static final String MAP_CACHE_LEVEL = "org.apache.chemistry.opencmis.client.bindings.cache.impl.MapCacheLevelImpl";
    public static final String LRU_CACHE_LEVEL = "org.apache.chemistry.opencmis.client.bindings.cache.impl.LruCacheLevelImpl";

    public void testCache() {
        Cache cache;

        cache = new CacheImpl();
        cache.initialize(new String[] { MAP_CACHE_LEVEL, LRU_CACHE_LEVEL, MAP_CACHE_LEVEL, MAP_CACHE_LEVEL });

        String value1 = "value1";
        String value2 = "value2";
        String value3 = "value3";
        Object valueObj;

        // put and get
        cache.put(value1, "l1", "l2a", "l3", "l4");
        cache.put(value2, "l1", "l2b", "l3", "l4");
        cache.put(value3, "l1", "l2c", "l3", "l4");

        valueObj = cache.get("l1", "l2a", "l3", "l4");
        assertTrue(valueObj instanceof String);
        assertSame(value1, valueObj);

        valueObj = cache.get("l1", "l2b", "l3", "l4");
        assertTrue(valueObj instanceof String);
        assertSame(value2, valueObj);

        valueObj = cache.get("l1", "l2c", "l3", "l4");
        assertTrue(valueObj instanceof String);
        assertSame(value3, valueObj);

        // remove leaf
        cache.remove("l1", "l2", "l3", "l4");
        valueObj = cache.get("l1", "l2", "l3", "l4");
        assertNull(valueObj);

        // put and get
        cache.put(value1, "l1", "l2", "l3", "l4");
        valueObj = cache.get("l1", "l2", "l3", "l4");
        assertTrue(valueObj instanceof String);
        assertSame(value1, valueObj);

        // remove branch
        cache.remove("l1", "l2");
        valueObj = cache.get("l1", "l2", "l3", "l4");
        assertNull(valueObj);
    }

    public void testCacheBadUsage() {
        Cache cache;

        cache = new CacheImpl();
        cache.initialize(new String[] { MAP_CACHE_LEVEL, LRU_CACHE_LEVEL, MAP_CACHE_LEVEL, MAP_CACHE_LEVEL });

        // insufficient number of keys
        try {
            cache.put("value", "l1", "l2", "l3");
        } catch (IllegalArgumentException e) {
        }

        // too many number of keys
        try {
            cache.put("value", "l1", "l2", "l3", "l4", "l5");
        } catch (IllegalArgumentException e) {
        }

        // no keys
        assertNull(cache.get((String[]) null));
    }

    public void testCacheConfig() {
        Cache cache;

        // empty config
        try {
            cache = new CacheImpl();
            cache.initialize(new String[] {});
        } catch (IllegalArgumentException e) {
        }

        // null config
        try {
            cache = new CacheImpl();
            cache.initialize(null);
        } catch (IllegalArgumentException e) {
        }

        // unknown class
        try {
            cache = new CacheImpl();
            cache.initialize(new String[] { "this.is.not.a.valid.class" });
        } catch (IllegalArgumentException e) {
        }

        // not a CacheLevel class
        try {
            cache = new CacheImpl();
            cache.initialize(new String[] { "org.apache.chemistry.opencmis.client.provider.cache.CacheTest" });
        } catch (IllegalArgumentException e) {
        }
    }

    public void testMapCache() {
        Cache cache;

        cache = new CacheImpl();
        cache.initialize(new String[] { MAP_CACHE_LEVEL + " " + MapCacheLevelImpl.CAPACITY + "=10,"
                + MapCacheLevelImpl.LOAD_FACTOR + "=0.5" });

        for (int i = 0; i < 100; i++) {
            cache.put("value" + i, "key" + i);
        }

        for (int i = 0; i < 100; i++) {
            Object valueObj = cache.get("key" + i);
            assertTrue(valueObj instanceof String);
            assertEquals("value" + i, valueObj);
        }
    }

    public void testURLCache() {
        Cache cache;

        cache = new CacheImpl();
        cache.initialize(new String[] { LRU_CACHE_LEVEL + " " + LruCacheLevelImpl.MAX_ENTRIES + "=10" });

        for (int i = 0; i < 100; i++) {
            cache.put("value" + i, "key" + i);
        }

        for (int i = 0; i < 90; i++) {
            Object valueObj = cache.get("key" + i);
            assertNull(valueObj);
        }

        for (int i = 90; i < 100; i++) {
            Object valueObj = cache.get("key" + i);
            assertTrue(valueObj instanceof String);
            assertEquals("value" + i, valueObj);
        }
    }

    public void XtestFallback() {
        Cache cache;

        cache = new CacheImpl();
        cache.initialize(new String[] { MAP_CACHE_LEVEL + " " + MapCacheLevelImpl.CAPACITY + "=10,"
                + MapCacheLevelImpl.LOAD_FACTOR + "=0.5" });

        cache.put("value1", new String[] { null });
        cache.put("value2", "key2");

        assertEquals("value1", cache.get(new String[] { null }));
        assertEquals("value2", cache.get("key2"));
        assertEquals("value1", cache.get("key3"));
    }

    public void testContentTypeCache() {
        ContentTypeCacheLevelImpl cl = new ContentTypeCacheLevelImpl();
        cl.initialize(null);

        String type1 = "type1";

        cl.put(type1, "text/plain; param1=test; charset=UTF-8");

        assertEquals(type1, cl.get("text/plain; param1=test; charset=UTF-8"));
        assertEquals(type1, cl.get("text/plain; param1=test; charset=utf-8"));
        assertEquals(type1, cl.get("text/plain; charset=utf-8; param1=test"));
        assertEquals(type1, cl.get("text/plain; charset=utf-8; param1=test;"));
        assertEquals(type1, cl.get("text/plain;charset=utf-8;param1=test"));
        assertEquals(type1, cl.get("text/plain;\tcharset=utf-8;     param1=test"));
        assertEquals(type1, cl.get("text/plain; charset=\"utf-8\"; param1=test;"));

        assertNull(cl.get("text/plain; param1=blah; charset=UTF-8"));
        assertNull(cl.get("text/plain; param1=test; charset=us-ascii"));
    }
}
