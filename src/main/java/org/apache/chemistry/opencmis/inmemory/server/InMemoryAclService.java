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
package org.apache.chemistry.opencmis.inmemory.server;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InMemoryAclService extends InMemoryAbstractServiceImpl {

    private static final Log LOG = LogFactory.getLog(InMemoryAclService.class.getName());
    
    public InMemoryAclService(StoreManager storeManager) {
        super(storeManager);
    }

    public Acl getAcl(CallContext context, String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
        LOG.debug("start getObject()");

        StoredObject so = validator.getAcl(context, repositoryId, objectId, extension);
        if (so instanceof DocumentVersion)
            return ((DocumentVersion) so).getParentDocument().getAcl();
        else
            return so.getAcl();
    }

    public Acl applyAcl(CallContext context, String repositoryId, String objectId, Acl addAces, Acl removeAces, AclPropagation aclPropagation,
            ExtensionsData extension) {

        StoredObject so = validator.applyAcl(context, repositoryId, objectId, aclPropagation, extension);
        return fStoreManager.getObjectStore(repositoryId).applyAcl(so, addAces, removeAces, aclPropagation, context.getUsername());
       
    }
    
    public Acl applyAcl(CallContext context, String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation) {
        
        StoredObject so = validator.applyAcl(context, repositoryId, objectId);
        return fStoreManager.getObjectStore(repositoryId).applyAcl(so, aces, aclPropagation, context.getUsername());        
    }

}
