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
package org.apache.opencmis.server.impl.webservices;

import static org.apache.opencmis.commons.impl.Converter.convert;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.impl.jaxb.ACLServicePort;
import org.apache.opencmis.commons.impl.jaxb.CmisACLType;
import org.apache.opencmis.commons.impl.jaxb.CmisAccessControlListType;
import org.apache.opencmis.commons.impl.jaxb.CmisException;
import org.apache.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.opencmis.commons.impl.jaxb.EnumACLPropagation;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisAclService;

/**
 * CMIS ACL Service.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
@WebService(endpointInterface = "org.apache.opencmis.commons.impl.jaxb.ACLServicePort")
public class AclService extends AbstractService implements ACLServicePort {
  @Resource
  WebServiceContext fContext;

  public CmisACLType applyACL(String repositoryId, String objectId,
      CmisAccessControlListType addAces, CmisAccessControlListType removeAces,
      EnumACLPropagation aclPropagation, CmisExtensionType extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisAclService service = factory.getAclService();
      CallContext context = createContext(fContext);

      AccessControlList acl = service.applyAcl(context, repositoryId, objectId, convert(addAces,
          null), convert(removeAces, null), convert(AclPropagation.class, aclPropagation),
          convert(extension));

      if (acl == null) {
        return null;
      }

      CmisACLType result = new CmisACLType();
      result.setACL(convert(acl));
      result.setExact(acl.isExact());

      return result;
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public CmisACLType getACL(String repositoryId, String objectId, Boolean onlyBasicPermissions,
      CmisExtensionType extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisAclService service = factory.getAclService();
      CallContext context = createContext(fContext);

      AccessControlList acl = service.getAcl(context, repositoryId, objectId, onlyBasicPermissions,
          convert(extension));

      if (acl == null) {
        return null;
      }

      CmisACLType result = new CmisACLType();
      result.setACL(convert(acl));
      result.setExact(acl.isExact());

      return result;
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

}
