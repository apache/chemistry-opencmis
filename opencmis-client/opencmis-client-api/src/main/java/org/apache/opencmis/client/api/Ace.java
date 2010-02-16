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
package org.apache.opencmis.client.api;

import java.util.List;

/**
 * Access Control Entry.
 * 
 * @see Acl
 * @see org.apache.opencmis.client.api.repository.ObjectFactory#createAce(String, List)
 * 
 *      See CMIS Domain Model - section 2.1.8.
 */
public interface Ace {

  /**
   * Get the id of the principal, for which the ACE grants permissions.
   * 
   * @return the id of the principal for this ACE
   */
  String getPrincipalId();

  /**
   * Get the list of permissions, this ACE grants to its principal.
   * 
   * @return the list of {@code AclPermission} objects
   */
  List<AclPermission> getPermissions();

  /**
   * Get the list of ids for the permissions, this ACE grants to its principal.
   * 
   * @return the list of {@code String}s with the ids of the permissions
   */
  List<String> getPermissionsNames();

  /**
   * Gets the {@code direct} flag for this ACE.
   * 
   * @return {@code true} if the ACE is directly assigned to the object is has been retrieved from,
   *         {@code false} if it was inherited somehow
   */
  boolean isDirect();

}
