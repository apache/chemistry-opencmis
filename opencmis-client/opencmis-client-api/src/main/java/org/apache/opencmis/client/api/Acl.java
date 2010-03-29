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
 * Access Control List.
 * 
 * @see CmisObject#getAcl()
 * @see org.apache.opencmis.client.api.repository.ObjectFactory#createAcl(List)
 * 
 *      See CMIS Domain Model - section 2.1.8.
 */
public interface Acl {

  /**
   * Get the list of Access Control Entries for this ACL.
   * 
   * @return the list of ACEs for this ACL
   */
  List<Ace> getAces();

  /**
   * Get an indicator, if this ACL fully describes the permission of this object. See CMIS Domain
   * Model - section 2.2.10.1.2.
   * 
   * @return {@code true} if the ACL describes the permissions for the object it has been retrieved
   *         for completely, {@code false} if other security constraints might be effective as well
   */
  Boolean isExact();

}
