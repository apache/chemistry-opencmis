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
package org.apache.opencmis.server.impl.atompub;

import static org.apache.opencmis.commons.impl.Converter.convert;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getBooleanParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getEnumParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getStringParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.JaxBHelper;
import org.apache.opencmis.commons.impl.jaxb.CmisAccessControlListType;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisAclService;

/**
 * ACL Service operations.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AclService {

  /**
   * Get ACL.
   */
  public static void getAcl(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisAclService service = factory.getAclService();

    // get parameters
    String objectId = getStringParameter(request, Constants.PARAM_ID);
    Boolean onlyBasicPermissions = getBooleanParameter(request,
        Constants.PARAM_ONLY_BASIC_PERMISSIONS);

    // execute
    AccessControlList acl = service.getAcl(context, repositoryId, objectId, onlyBasicPermissions,
        null);

    if (acl == null) {
      throw new CmisRuntimeException("ACL is null!");
    }

    // set headers
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(Constants.MEDIATYPE_ACL);

    // write XML
    AclDocument aclDocument = new AclDocument();
    aclDocument.writeAcl(acl, response.getOutputStream());
  }

  /**
   * Apply ACL.
   */
  public static void applyAcl(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisAclService service = factory.getAclService();

    // get parameters
    String objectId = getStringParameter(request, Constants.PARAM_ID);
    AclPropagation aclPropagation = getEnumParameter(request, Constants.PARAM_ACL_PROPAGATION,
        AclPropagation.class);

    Object aclRequest = null;
    try {
      Unmarshaller u = JaxBHelper.createUnmarshaller();
      aclRequest = u.unmarshal(request.getInputStream());
    }
    catch (Exception e) {
      throw new CmisInvalidArgumentException("Invalid ACL request: " + e, e);
    }

    if (!(aclRequest instanceof JAXBElement<?>)) {
      throw new CmisInvalidArgumentException("Not an ACL document!");
    }

    if (!(((JAXBElement<?>) aclRequest).getValue() instanceof CmisAccessControlListType)) {
      throw new CmisInvalidArgumentException("Not an ACL document!");
    }

    AccessControlList aces = convert((CmisAccessControlListType) ((JAXBElement<?>) aclRequest)
        .getValue(), null);

    // execute
    AccessControlList acl = service.applyAcl(context, repositoryId, objectId, aces, aclPropagation);

    // set headers
    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setContentType(Constants.MEDIATYPE_ACL);

    // write XML
    AclDocument aclDocument = new AclDocument();
    aclDocument.writeAcl(acl, response.getOutputStream());
  }
}
