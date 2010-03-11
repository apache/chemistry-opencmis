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
package org.apache.opencmis.client.provider.cache.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.client.provider.cache.Cache;
import org.apache.opencmis.client.provider.cache.CacheLevel;

/**
 * Default cache implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class CacheImpl implements Cache {

  private static Log log = LogFactory.getLog(CacheImpl.class);

  private static final long serialVersionUID = 1L;

  private List<Class<?>> fLevels;
  private List<Map<String, String>> fLevelParameters;

  private String fName;

  private CacheLevel fRoot;

  private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock();

  /**
   * Constructor.
   */
  public CacheImpl() {
    fName = "Cache";
  }

  /**
   * Constructor.
   */
  public CacheImpl(String name) {
    fName = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.cache.Cache#initialize(java.lang.String[])
   */
  public void initialize(String[] cacheLevelConfig) {
    if (fLevels != null) {
      throw new IllegalStateException("Cache already initialize!");
    }

    if ((cacheLevelConfig == null) || (cacheLevelConfig.length == 0)) {
      throw new IllegalArgumentException("Cache config must not be empty!");
    }

    fLock.writeLock().lock();
    try {
      fLevels = new ArrayList<Class<?>>();
      fLevelParameters = new ArrayList<Map<String, String>>();

      // build level lists
      for (String config : cacheLevelConfig) {
        int x = config.indexOf(' ');
        if (x == -1) {
          addLevel(config, null);
        }
        else {
          addLevel(config.substring(0, x), config.substring(x + 1));
        }
      }

      // create root
      fRoot = createCacheLevel(0);
    }
    finally {
      fLock.writeLock().unlock();
    }
  }

  private void addLevel(String className, String parameters) {
    // get the class
    Class<?> clazz;
    try {
      clazz = Class.forName(className);
    }
    catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Class '" + className + "' not found!", e);
    }

    // check the class
    if (!CacheLevel.class.isAssignableFrom(clazz)) {
      throw new IllegalArgumentException("Class '" + className
          + "' does not implement the CacheLevel interface!");
    }

    fLevels.add(clazz);

    // process parameters
    if (parameters == null) {
      fLevelParameters.add(null);
    }
    else {
      Map<String, String> parameterMap = new HashMap<String, String>();
      fLevelParameters.add(parameterMap);

      for (String pair : parameters.split(",")) {
        String[] keyValue = pair.split("=");
        if (keyValue.length == 1) {
          parameterMap.put(keyValue[0], "");
        }
        else {
          parameterMap.put(keyValue[0], keyValue[1]);
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.cache.Cache#get(java.lang.String[])
   */
  public Object get(String... keys) {
    // check keys
    if (keys == null) {
      return null;
    }

    // check level depth
    if (fLevels.size() != keys.length) {
      throw new IllegalArgumentException("Wrong number of keys!");
    }

    Object result = null;

    fLock.readLock().lock();
    try {
      CacheLevel cacheLevel = fRoot;

      // follow the branch
      for (int i = 0; i < keys.length - 1; i++) {
        Object level = cacheLevel.get(keys[i]);

        // does the branch exist?
        if (level == null) {
          return null;
        }

        // next level
        cacheLevel = (CacheLevel) level;
      }

      // get the value
      result = cacheLevel.get(keys[keys.length - 1]);
    }
    finally {
      fLock.readLock().unlock();
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.cache.Cache#put(java.lang.Object, java.lang.String[])
   */
  public void put(Object value, String... keys) {
    // check keys
    if (keys == null) {
      return;
    }

    // check level depth
    if (fLevels.size() != keys.length) {
      throw new IllegalArgumentException("Wrong number of keys!");
    }

    fLock.writeLock().lock();
    try {
      CacheLevel cacheLevel = fRoot;

      // follow the branch
      for (int i = 0; i < keys.length - 1; i++) {
        Object level = cacheLevel.get(keys[i]);

        // does the branch exist?
        if (level == null) {
          level = createCacheLevel(i + 1);
          cacheLevel.put(level, keys[i]);
        }

        // next level
        cacheLevel = (CacheLevel) level;
      }

      cacheLevel.put(value, keys[keys.length - 1]);

      if (log.isDebugEnabled()) {
        log.debug(fName + ": put [" + getFormattedKeys(keys) + "] = " + value);
      }
    }
    finally {
      fLock.writeLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.cache.Cache#remove(java.lang.String[])
   */
  public void remove(String... keys) {
    if (keys == null) {
      return;
    }

    fLock.writeLock().lock();
    try {
      CacheLevel cacheLevel = fRoot;

      // follow the branch
      for (int i = 0; i < keys.length - 1; i++) {
        Object level = cacheLevel.get(keys[i]);

        // does the branch exist?
        if (level == null) {
          return;
        }

        // next level
        cacheLevel = (CacheLevel) level;
      }

      cacheLevel.remove(keys[keys.length - 1]);

      if (log.isDebugEnabled()) {
        log.debug(fName + ": removed [" + getFormattedKeys(keys) + "]");
      }
    }
    finally {
      fLock.writeLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.cache.Cache#check(java.lang.String[])
   */
  public int check(String... keys) {
    if (keys == null) {
      return -1;
    }

    fLock.readLock().lock();
    try {
      CacheLevel cacheLevel = fRoot;

      // follow the branch
      for (int i = 0; i < keys.length - 1; i++) {
        Object level = cacheLevel.get(keys[i]);

        // does the branch exist?
        if (level == null) {
          return i;
        }

        // next level
        cacheLevel = (CacheLevel) level;
      }
    }
    finally {
      fLock.readLock().unlock();
    }

    return keys.length;
  }

  // ---- internal ----

  /**
   * Creates a cache level object.
   */
  private CacheLevel createCacheLevel(int level) {
    if ((level < 0) || (level >= fLevels.size())) {
      throw new IllegalArgumentException("Cache level doesn't fit the configuration!");
    }

    // get the class and create an instance
    Class<?> clazz = fLevels.get(level);
    CacheLevel cacheLevel = null;
    try {
      cacheLevel = (CacheLevel) clazz.newInstance();
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Cache level problem?!", e);
    }

    // initialize it
    cacheLevel.initialize(fLevelParameters.get(level));

    return cacheLevel;
  }

  @Override
  public String toString() {
    return (fRoot == null ? "(no cache root)" : fRoot.toString());
  }

  // ---- internal ----

  private String getFormattedKeys(String[] keys) {
    StringBuilder sb = new StringBuilder();
    for (String k : keys) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(k);
    }

    return sb.toString();
  }
}