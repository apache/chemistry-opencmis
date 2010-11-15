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
package org.apache.chemistry.opencmis.client.runtime.cache;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;

/**
 * Non synchronized cache implementation. The cache is limited to a specific
 * size of entries and works in a LRU mode.
 */
public class CacheImpl implements Cache {

    private static final long serialVersionUID = 1L;

    private static final float HASHTABLE_LOAD_FACTOR = 0.75f;

    private int cacheSize;

    private LinkedHashMap<String, Map<String, CmisObject>> objectMap;
    private Map<String, String> pathToIdMap;

    private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock();

    /**
     * Default constructor.
     */
    public CacheImpl() {
    }

    public void initialize(Session session, Map<String, String> parameters) {
        fLock.writeLock().lock();
        try {
            try {
                cacheSize = Integer.valueOf(parameters.get(SessionParameter.CACHE_SIZE_OBJECTS));
            } catch (Exception e) {
                cacheSize = 1000;
            }

            initializeInternals();
        } finally {
            fLock.writeLock().unlock();
        }
    }

    /**
     * Sets up the internal objects.
     */
    private void initializeInternals() {
        fLock.writeLock().lock();
        try {
            int hashTableCapacity = (int) Math.ceil(cacheSize / HASHTABLE_LOAD_FACTOR) + 1;

            final int cs = cacheSize;

            objectMap = new LinkedHashMap<String, Map<String, CmisObject>>(hashTableCapacity, HASHTABLE_LOAD_FACTOR) {

                private static final long serialVersionUID = 1L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Map<String, CmisObject>> eldest) {
                    return size() > cs;
                }
            };

            resetPathCache();
        } finally {
            fLock.writeLock().unlock();
        }
    }

    public void clear() {
        initializeInternals();
    }

    public void resetPathCache() {
        fLock.writeLock().lock();
        try {
            pathToIdMap = new HashMap<String, String>();
        } finally {
            fLock.writeLock().unlock();
        }
    }

    public boolean containsId(String objectId, String cacheKey) {
        fLock.readLock().lock();
        try {
            if (!objectMap.containsKey(objectId)) {
                return false;
            }

            return objectMap.get(objectId).containsKey(cacheKey);
        } finally {
            fLock.readLock().unlock();
        }
    }

    public boolean containsPath(String path, String cacheKey) {
        fLock.readLock().lock();
        try {
            if (!pathToIdMap.containsKey(path)) {
                return false;
            }

            return containsId(pathToIdMap.get(path), cacheKey);
        } finally {
            fLock.readLock().unlock();
        }
    }

    public CmisObject getById(String objectId, String cacheKey) {
        fLock.readLock().lock();
        try {
            Map<String, CmisObject> cacheKeyMap = objectMap.get(objectId);
            if (cacheKeyMap == null) {
                return null; // not found
            }

            return cacheKeyMap.get(cacheKey);
        } finally {
            fLock.readLock().unlock();
        }
    }

    public CmisObject getByPath(String path, String cacheKey) {
        fLock.readLock().lock();
        try {
            String id = pathToIdMap.get(path);
            if (id == null) {
                return null; // not found
            }

            CmisObject object = getById(id, cacheKey);
            if ((object == null) && (!objectMap.containsKey(id))) {
                // clean up
                fLock.readLock().unlock();
                fLock.writeLock().lock();
                try {
                    pathToIdMap.remove(path);
                } finally {
                    fLock.writeLock().unlock();
                    fLock.readLock().lock();
                }
            }

            return object;
        } finally {
            fLock.readLock().unlock();
        }
    }

    public void put(CmisObject object, String cacheKey) {
        // no object, no cache key - no cache
        if ((object == null) || (cacheKey == null)) {
            return;
        }

        // no id - no cache
        if (object.getId() == null) {
            return;
        }

        fLock.writeLock().lock();
        try {
            // get cache key map
            Map<String, CmisObject> cacheKeyMap = objectMap.get(object.getId());
            if (cacheKeyMap == null) {
                cacheKeyMap = new HashMap<String, CmisObject>();
                objectMap.put(object.getId(), cacheKeyMap);
            }

            // put into id cache
            cacheKeyMap.put(cacheKey, object);

            // folders may have a path, use it!
            String path = object.getPropertyValue(PropertyIds.PATH);
            if (path != null) {
                pathToIdMap.put(path, object.getId());
            }
        } finally {
            fLock.writeLock().unlock();
        }
    }

    public void putPath(String path, CmisObject object, String cacheKey) {
        fLock.writeLock().lock();
        try {
            put(object, cacheKey);

            if ((object != null) && (object.getId() != null) && (cacheKey != null)) {
                pathToIdMap.put(path, object.getId());
            }
        } finally {
            fLock.writeLock().unlock();
        }
    }

    public int getCacheSize() {
        return this.cacheSize;
    }
}
