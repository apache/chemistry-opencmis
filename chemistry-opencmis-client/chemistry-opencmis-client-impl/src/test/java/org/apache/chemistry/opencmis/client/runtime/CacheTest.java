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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.runtime.cache.Cache;
import org.apache.chemistry.opencmis.client.runtime.cache.CacheImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.junit.Before;
import org.junit.Test;

public class CacheTest {

    @Before
    public void setup() {
    }

    @Test
    public void cacheSingleObjectTest() {
        Cache cache = createCache(100, 3600 * 1000);

        String id = "1";
        // String path = "/1";
        String cacheKey = "key";

        // add object
        CmisObject obj1 = createCmisObject(id);
        cache.put(obj1, cacheKey);

        // access object
        assertTrue(cache.containsId(id, cacheKey));

        // access object
        CmisObject obj2 = cache.getById(id, cacheKey);
        assertEquals(obj1, obj2);

        // clear cache
        cache.clear();

        // access object (not found)
        assertFalse(cache.containsId(id, cacheKey));

        // access object (not found)
        CmisObject obj4 = cache.getById(id, cacheKey);
        assertNull(obj4);
    }

    @Test
    public void cachePathObjectTest() {
        Cache cache = createCache(100, 3600 * 1000);

        String id = "1";
        String path = "/1";
        String cacheKey = "key";

        // add object
        CmisObject obj1 = createCmisObject(id);
        cache.putPath(path, obj1, cacheKey);

        // access object
        assertTrue(cache.containsPath(path, cacheKey));

        // access object
        CmisObject obj2 = cache.getById(id, cacheKey);
        assertEquals(obj1, obj2);

        // access object
        CmisObject obj3 = cache.getByPath(path, cacheKey);
        assertEquals(obj1, obj3);

        // access ID
        String chachedId = cache.getObjectIdByPath(path);
        assertEquals(obj1.getId(), chachedId);

        // remove
        cache.removePath(path);
        assertNull(cache.getObjectIdByPath(path));
        assertFalse(cache.containsPath(path, cacheKey));

        // clear cache
        cache.clear();

        // access object (not found)
        assertFalse(cache.containsId(id, cacheKey));

        // access object (not found)
        CmisObject obj4 = cache.getById(id, cacheKey);
        assertNull(obj4);
    }

    @Test
    public void cacheSizeTest() {
        int cacheSize = 50000;
        Cache cache = createCache(cacheSize, 3600 * 1000);
        assertEquals(cacheSize, cache.getCacheSize());
    }

    @Test
    public void lruTest() {
        int cacheSize = 3;
        Cache cache = createCache(cacheSize, 3600 * 1000);

        String cacheKey = "key";

        for (int i = 0; i < cacheSize + 1; i++) {
            CmisObject obj = createCmisObject("id" + i);
            cache.put(obj, cacheKey);
        }

        assertNull(cache.getById("id0", cacheKey)); // thrown out
        assertNotNull(cache.getById("id1", cacheKey));
        assertNotNull(cache.getById("id2", cacheKey));
        assertNotNull(cache.getById("id3", cacheKey));
    }

    @SuppressWarnings("static-access")
    @Test
    public void ttlTest() throws InterruptedException {
        Cache cache = createCache(10, 500);

        String cacheKey = "key";
        String id = "id";

        CmisObject obj = this.createCmisObject(id);
        cache.put(obj, cacheKey);

        assertNotNull(cache.getById(id, cacheKey));

        Thread.currentThread().sleep(750);

        assertNull(cache.getById(id, cacheKey));
    }

    @Test
    public void serializationTest() throws Exception {
        int cacheSize = 10;
        Cache cache = createCache(cacheSize, 3600 * 1000);

        String cacheKey = "key";

        for (int i = 0; i < cacheSize; i++) {
            CmisObject obj = createCmisObject("id" + i);
            cache.put(obj, cacheKey);
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(cache);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        Cache cache2 = (Cache) in.readObject();
        in.close();

        for (int k = 0; k < cacheSize; k++) {
            CmisObject o1 = cache.getById("id" + k, cacheKey);
            CmisObject o2 = cache2.getById("id" + k, cacheKey);
            assertEquals(o1.getId(), o2.getId());
        }
    }

    /**
     * Create a Mock for testing Cache is sufficient.
     * 
     * @param id
     * @return a mocked object
     */
    private static CmisObject createCmisObject(final String id) {
        return new CmisObjectMock(id);
    }

    private static Cache createCache(int cacheSize, int ttl) {
        Cache cache = new CacheImpl();

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(SessionParameter.CACHE_SIZE_OBJECTS, "" + cacheSize);
        parameters.put(SessionParameter.CACHE_TTL_OBJECTS, "" + ttl);

        cache.initialize(null, parameters);

        return cache;
    }
}
