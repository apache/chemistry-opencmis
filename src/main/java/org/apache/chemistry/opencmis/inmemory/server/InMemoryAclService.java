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
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.inmemory.TypeValidator;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryAclService extends InMemoryAbstractServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryAclService.class.getName());
    final AtomLinkInfoProvider fAtomLinkProvider;

    public InMemoryAclService(StoreManager storeManager) {
        super(storeManager);
        fAtomLinkProvider = new AtomLinkInfoProvider(fStoreManager);
    }

    public Acl getAcl(CallContext context, String repositoryId, String objectId, Boolean onlyBasicPermissions,
            ExtensionsData extension, ObjectInfoHandler objectInfos) {
        LOG.debug("start getAcl()");
        Acl acl = null;
        StoredObject so = validator.getAcl(context, repositoryId, objectId, extension);
        if (so instanceof DocumentVersion)
            acl = ((DocumentVersion) so).getParentDocument().getAcl();
        else
            acl = so.getAcl();

        if (context.isObjectInfoRequired()) {
            ObjectInfoImpl objectInfo = new ObjectInfoImpl();
            fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfo);
            objectInfos.addObjectInfo(objectInfo);
        }
        
        return acl;
    }

    public Acl applyAcl(CallContext context, String repositoryId, String objectId, Acl addAces, Acl removeAces, AclPropagation aclPropagation,
            ExtensionsData extension, ObjectInfoHandler objectInfos) {

    	addAces  = TypeValidator.expandAclMakros(context.getUsername(), addAces);
    	removeAces  = TypeValidator.expandAclMakros(context.getUsername(), removeAces);
        
    	StoredObject so = validator.applyAcl(context, repositoryId, objectId, aclPropagation, extension);
        Acl acl = fStoreManager.getObjectStore(repositoryId).applyAcl(so, addAces, removeAces, aclPropagation, context.getUsername());
        
        if (context.isObjectInfoRequired()) {
            ObjectInfoImpl objectInfo = new ObjectInfoImpl();
            fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfo);
            objectInfos.addObjectInfo(objectInfo);
        }
        return acl;       
    }
    
    public Acl applyAcl(CallContext context, String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation) {
        
    	aces  = TypeValidator.expandAclMakros(context.getUsername(), aces);

    	StoredObject so = validator.applyAcl(context, repositoryId, objectId);
        return fStoreManager.getObjectStore(repositoryId).applyAcl(so, aces, aclPropagation, context.getUsername());        
    }

}
