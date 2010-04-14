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

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.provider.AccessControlList;

/**
 * CMIS ACL Service interface. Please refer to the CMIS specification and the OpenCMIS documentation
 * for details.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public interface CmisAclService {

  /**
   * Returns the ACL of an object.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  AccessControlList getAcl(CallContext context, String repositoryId, String objectId,
      Boolean onlyBasicPermissions, ExtensionsData extension);

  /**
   * Adds ACEs to and removes ACEs from the ACL of an object.
   * 
   * <p>
   * Bindings: Web Services
   * </p>
   */
  AccessControlList applyAcl(CallContext context, String repositoryId, String objectId,
      AccessControlList addAces, AccessControlList removeAces, AclPropagation aclPropagation,
      ExtensionsData extension);

  /**
   * Applies a new ACL to an object. Since it is not possible to transmit an "add ACL" and a
   * "remove ACL" via AtomPub, the merging has to be done the client side. The ACEs provided here is
   * supposed to the new complete ACL.
   * 
   * <p>
   * Bindings: AtomPub
   * </p>
   */
  AccessControlList applyAcl(CallContext context, String repositoryId, String objectId,
      AccessControlList aces, AclPropagation aclPropagation);
}
