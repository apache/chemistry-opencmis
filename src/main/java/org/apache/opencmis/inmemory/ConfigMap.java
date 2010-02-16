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
package org.apache.opencmis.inmemory;

/**
 * @author Jens
 *
 * ConfigMap is an interface that abstracts how runtime and configuration parameters are
 * retrieved. For the in-memory store they can come from a CallContext in a server scenario
 * and from a Session if it is used as provider. This interface is used to hide the details
 * where the parameters are read from. 
 */
public interface ConfigMap {
  
  /**
   * Read a configuration value
   * 
   * @param paramName
   *    named parameter key
   * @return
   *    parameter value
   */
  String get(String paramName);
  
}
