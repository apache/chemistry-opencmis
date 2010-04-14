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

import org.apache.opencmis.client.provider.cache.Cache;
import org.apache.opencmis.client.provider.cache.impl.CacheImpl;
import org.apache.opencmis.client.provider.cache.impl.MapCacheLevelImpl;
import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.provider.RepositoryInfoData;

/**
 * A cache for repository info objects.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RepositoryInfoCache implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final int CACHE_SIZE_REPOSITORIES = 10;

  private Cache fCache;

  /**
   * Constructor.
   * 
   * @param session
   *          the session object
   */
  public RepositoryInfoCache(Session session) {
    int repCount = session.get(SessionParameter.CACHE_SIZE_REPOSITORIES, CACHE_SIZE_REPOSITORIES);
    if (repCount < 1) {
      repCount = CACHE_SIZE_REPOSITORIES;
    }

    fCache = new CacheImpl("Repository Info Cache");
    fCache.initialize(new String[] { MapCacheLevelImpl.class.getName() + " "
        + MapCacheLevelImpl.CAPACITY + "=" + repCount });
  }

  /**
   * Adds a repository info object to the cache.
   * 
   * @param repositoryInfo
   *          the repository info object
   */
  public void put(RepositoryInfoData repositoryInfo) {
    if ((repositoryInfo == null) || (repositoryInfo.getRepositoryId() == null)) {
      return;
    }

    fCache.put(repositoryInfo, repositoryInfo.getRepositoryId());
  }

  /**
   * Retrieves a repository info object from the cache.
   * 
   * @param repositoryId
   *          the repository id
   * @return the repository info object or <code>null</code> if the object is not in the cache
   */
  public RepositoryInfoData get(String repositoryId) {
    return (RepositoryInfoData) fCache.get(repositoryId);
  }

  /**
   * Removes a repository info object from the cache.
   * 
   * @param repositoryId
   *          the repository id
   */
  public void remove(String repositoryId) {
    fCache.remove(repositoryId);
  }

  @Override
  public String toString() {
    return fCache.toString();
  }
}
