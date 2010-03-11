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
package org.apache.opencmis.client.provider.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.opencmis.client.provider.spi.Session;

/**
 * CMIS provider session implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class SessionImpl implements Session {

  private static final long serialVersionUID = 1L;

  private Map<String, Object> fData;

  private final ReentrantReadWriteLock fLock = new ReentrantReadWriteLock();

  /**
   * Constructor.
   */
  public SessionImpl() {
    fData = new HashMap<String, Object>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.Session#get(java.lang.String)
   */
  public Object get(String key) {
    Object value = null;

    fLock.readLock().lock();
    try {
      value = fData.get(key);
    }
    finally {
      fLock.readLock().unlock();
    }

    if (value instanceof TransientWrapper) {
      return ((TransientWrapper) value).getObject();
    }

    return value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.Session#get(java.lang.String, java.lang.Object)
   */
  public Object get(String key, Object defValue) {
    Object value = get(key);
    return (value == null ? defValue : value);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.Session#get(java.lang.String, int)
   */
  public int get(String key, int defValue) {
    Object value = get(key);
    int intValue = defValue;

    if (value instanceof Integer) {
      intValue = ((Integer) value).intValue();
    }
    else if (value instanceof String) {
      try {
        intValue = Integer.valueOf((String) value);
      }
      catch (NumberFormatException e) {
      }
    }

    return intValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.Session#put(java.lang.String,
   * java.io.Serializable)
   */
  public void put(String key, Serializable obj) {
    fLock.writeLock().lock();
    try {
      fData.put(key, obj);
    }
    finally {
      fLock.writeLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.Session#put(java.lang.String, java.lang.Object,
   * boolean)
   */
  public void put(String key, Object obj, boolean isTransient) {
    Object value = (isTransient ? new TransientWrapper(obj) : obj);
    if (!(value instanceof Serializable)) {
      throw new IllegalArgumentException("Object must be serializable!");
    }

    fLock.writeLock().lock();
    try {
      fData.put(key, value);
    }
    finally {
      fLock.writeLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.Session#remove(java.lang.String)
   */
  public void remove(String key) {
    fLock.writeLock().lock();
    try {
      fData.remove(key);
    }
    finally {
      fLock.writeLock().unlock();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.Session#readLock()
   */
  public void readLock() {
    fLock.readLock().lock();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.Session#readUnlock()
   */
  public void readUnlock() {
    fLock.readLock().unlock();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.Session#writeLock()
   */
  public void writeLock() {
    fLock.writeLock().lock();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.spi.Session#writeUnlock()
   */
  public void writeUnlock() {
    fLock.writeLock().unlock();
  }

  @Override
  public String toString() {
    return fData.toString();
  }
}
