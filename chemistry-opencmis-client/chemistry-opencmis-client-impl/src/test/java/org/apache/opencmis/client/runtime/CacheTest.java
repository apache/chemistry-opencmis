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
package org.apache.opencmis.client.runtime;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Assert;

import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.runtime.cache.Cache;
import org.apache.opencmis.client.runtime.cache.CacheImpl;
import org.junit.Before;
import org.junit.Test;

import com.sun.xml.ws.util.ByteArrayBuffer;

public class CacheTest {

  @Before
  public void setup() {
  }

  @Test
  public void cacheSingleObjectTest() {
    Cache cache = CacheImpl.newInstance();

    String id = "1";
    // String path = "/1";
    String cacheKey = "key";

    // add object
    CmisObject obj1 = this.createCmisObject(id);
    cache.put(obj1, cacheKey);

    // access object
    Assert.assertTrue(cache.containsId(id, cacheKey));

    // access object
    CmisObject obj2 = cache.getById(id, cacheKey);
    Assert.assertEquals(obj1, obj2);

    // clear cache
    cache.clear();

    // access object (not found)
    Assert.assertFalse(cache.containsId(id, cacheKey));

    // access object (not found)
    CmisObject obj4 = cache.getById(id, cacheKey);
    Assert.assertNull(obj4);
  }

  @Test
  public void cacheSizeTest() {
    int cacheSize = 50000;
    Cache cache = CacheImpl.newInstance(cacheSize);
    Assert.assertEquals(cacheSize, cache.getCacheSize());
  }

  @Test
  public void lruTest() {
    int cacheSize = 3;
    Cache cache = CacheImpl.newInstance(cacheSize);

    String cacheKey = "key";

    for (int i = 0; i < cacheSize + 1; i++) {
      CmisObject obj = this.createCmisObject("id" + i);
      cache.put(obj, cacheKey);
    }

    Assert.assertNull(cache.getById("id0", cacheKey)); // thrown out
    Assert.assertNotNull(cache.getById("id1", cacheKey));
    Assert.assertNotNull(cache.getById("id2", cacheKey));
    Assert.assertNotNull(cache.getById("id3", cacheKey));
  }

  @Test
  public void serializationTest() throws IOException, ClassNotFoundException {
    int cacheSize = 10;
    Cache cache = CacheImpl.newInstance(cacheSize);

    String cacheKey = "key";

    for (int i = 0; i < cacheSize; i++) {
      CmisObject obj = this.createCmisObject("id" + i);
      cache.put(obj, cacheKey);
    }

    ByteArrayBuffer buffer = new ByteArrayBuffer();
    ObjectOutputStream out = new ObjectOutputStream(buffer);
    out.writeObject(cache);
    out.close();

    ObjectInputStream in = new ObjectInputStream(buffer.newInputStream());
    Cache cache2 = (Cache) in.readObject();
    in.close();

    for (int k = 0; k < cacheSize; k++) {
      CmisObject o1 = cache.getById("id" + k, cacheKey);
      CmisObject o2 = cache2.getById("id" + k, cacheKey);
      Assert.assertEquals(o1.getId(), o2.getId());
    }

  }

  /**
   * Create a Mock for testing Cache is sufficient.
   * 
   * @param id
   * @param path
   * @return a mocked object
   */
  private CmisObject createCmisObject(String id) {
    CmisObject obj = createNiceMock(CmisObject.class);

    expect(obj.getId()).andReturn(id).anyTimes();

    replay(obj);

    return obj;
  }
}
