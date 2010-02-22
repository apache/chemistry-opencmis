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
package org.apache.opencmis.client.runtime.cache;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.opencmis.client.api.CmisObject;

/**
 * Non synchronized cache implementation. The cache is limited to a specific
 * size of entries and works in a LRU mode
 */
public class CacheImpl implements Cache, Serializable {

	private LinkedHashMap<String, CmisObject> idMap = null;
	private LinkedHashMap<String, CmisObject> pathMap = null;

	private static final float hashTableLoadFactor = 0.75f;

	private int cacheSize = 1000; // default

	/**
	 * serialization
	 */
	private static final long serialVersionUID = 1978445442452564094L;

	public CacheImpl() {
		this.idMap = this.createLruCache();
		this.pathMap = this.createLruCache();
	}

	public CacheImpl(int cacheSize) {
		this.cacheSize = cacheSize;

		this.idMap = this.createLruCache();
		this.pathMap = this.createLruCache();
	}

	private LinkedHashMap<String, CmisObject> createLruCache() {
		int hashTableCapacity = (int) Math
				.ceil(cacheSize / hashTableLoadFactor) + 1;

		LinkedHashMap<String, CmisObject> map = new LinkedHashMap<String, CmisObject>(
				hashTableCapacity, hashTableLoadFactor) {

			// (an anonymous inner class)
			private static final long serialVersionUID = -3928413932856712672L;

			@Override
			protected boolean removeEldestEntry(
					Map.Entry<String, CmisObject> eldest) {
				return size() > CacheImpl.this.cacheSize;
			}
		};
		return map;
	}

	public boolean containsId(String objectId) {
		return this.idMap.containsKey(objectId);
	}

	public void clear() {
		this.idMap.clear();
		this.pathMap.clear();
	}

	public boolean containsPath(String path) {
		return this.pathMap.containsKey(path);
	}

	public CmisObject get(String objectId) {
		return this.idMap.get(objectId);
	}

	public CmisObject getByPath(String path) {
		return this.pathMap.get(path);
	}

	public void put(CmisObject object) {
		this.idMap.put(object.getId(), object);
		this.pathMap.put(object.getPath(), object);
	}

}
