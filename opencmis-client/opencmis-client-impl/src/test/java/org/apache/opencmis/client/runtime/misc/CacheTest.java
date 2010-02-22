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
package org.apache.opencmis.client.runtime.misc;

import junit.framework.Assert;

import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.runtime.cache.Cache;
import org.apache.opencmis.client.runtime.cache.CacheImpl;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import org.junit.Before;
import org.junit.Test;

public class CacheTest {

	@Before
	public void setup() {
	}

	@Test
	public void cacheSingleobject() {
		Cache cache = new CacheImpl();

		String id = "1";
		String path = "/1";

		// add object
		CmisObject obj1 = this.createCmisObject(id, path);
		cache.put(obj1);

		// access object
		Assert.assertTrue(cache.containsId(id));
		Assert.assertTrue(cache.containsPath(path));

		// access object
		CmisObject obj2 = cache.get(id);
		Assert.assertEquals(obj1, obj2);
		CmisObject obj3 = cache.getByPath(path);
		Assert.assertEquals(obj1, obj3);

		// clear cache
		cache.clear();

		// access object (not found)
		Assert.assertFalse(cache.containsId(id));
		Assert.assertFalse(cache.containsPath(path));

		// access object (not found)
		CmisObject obj4 = cache.get(id);
		Assert.assertNull(obj4);
		CmisObject obj5 = cache.getByPath(path);
		Assert.assertNull(obj5);
	}

	@Test
	public void lruTest() {
		int cacheSize = 3;
		Cache cache = new CacheImpl(cacheSize);

		for (int i = 0; i < cacheSize + 1; i++) {
			CmisObject obj = this.createCmisObject("id" + i, "path" + i);
			cache.put(obj);
		}
		
		Assert.assertNull(cache.get("id0"));    // thrown out
		Assert.assertNotNull(cache.get("id1"));
		Assert.assertNotNull(cache.get("id2"));
		Assert.assertNotNull(cache.get("id3"));
		
	}

	/**
	 * Create a Mock for testing Cache is sufficient.
	 * 
	 * @param id
	 * @param path
	 * @return a mocked object
	 */
	private CmisObject createCmisObject(String id, String path) {
		CmisObject obj = createNiceMock(CmisObject.class);

		expect(obj.getId()).andReturn(id).anyTimes();
		expect(obj.getPath()).andReturn(path).anyTimes();

		replay(obj);

		return obj;
	}
}
