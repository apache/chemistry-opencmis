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

import org.apache.opencmis.client.api.CmisObject;

/**
 * Implements a session cache providing following capabilities:
 * <p>
 * <ul>
 * <li>access CmisObject by object id</li>
 * <li>access CmisObject by object path</li>
 * </ul>
 */
public interface Cache {

  boolean containsId(String objectId, String cacheKey);

  boolean containsPath(String path, String cacheKey);

  void put(CmisObject object, String cacheKey);

  void putPath(String path, CmisObject object, String cacheKey);

  CmisObject getById(String objectId, String cacheKey);

  CmisObject getByPath(String path, String cacheKey);

  void clear();

  void resetPathCache();

  int getCacheSize();
}
