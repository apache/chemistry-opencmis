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
package org.apache.opencmis.server.spi;

/**
 * An object implementing this interface holds context data of the current call.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public interface CallContext {

  String BINDING_ATOMPUB = "atompub";
  String BINDING_WEBSERVICES = "webservices";

  String USERNAME = "useranme";
  String PASSWORD = "password";
  String LOCALE = "locale";
  String OFFSET = "offset";
  String LENGTH = "length";

  /**
   * Returns the binding. Usually it returns {@link BINDING_ATOMPUB} or {@link BINDING_WEBSERVICES}.
   */
  String getBinding();

  /**
   * Returns context data by key.
   * 
   * @param key
   *          the key
   * @return the data if the key is valid, <code>null</code> otherwise
   */
  String get(String key);

  /**
   * Returns the user name.
   */
  String getUsername();

  /**
   * Returns the password.
   */
  String getPassword();

  /**
   * Returns the locale.
   */
  String getLocale();
}
