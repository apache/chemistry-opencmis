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
package org.apache.opencmis.client.provider.spi;

import java.io.Serializable;

/**
 * CMIS provider session interface.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public interface Session extends Serializable {

  /**
   * Gets a session value.
   */
  Object get(String key);

  /**
   * Returns a session value or the default value if the key doesn't exist.
   */
  Object get(String key, Object defValue);

  /**
   * Returns a session value or the default value if the key doesn't exist.
   */
  int get(String key, int defValue);

  /**
   * Adds a non-transient session value.
   */
  void put(String key, Serializable object);

  /**
   * Adds a session value.
   */
  void put(String key, Object object, boolean isTransient);

  /**
   * Removes a session value.
   */
  void remove(String key);
}
