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
package org.apache.opencmis.client.provider.spi.webservices;

import static org.apache.opencmis.commons.impl.Converter.convert;

import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.jaxb.ACLServicePort;
import org.apache.opencmis.commons.impl.jaxb.CmisException;
import org.apache.opencmis.commons.impl.jaxb.EnumACLPropagation;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.AclService;

/**
 * ACL Service Web Services client.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AclServiceImpl extends AbstractWebServicesService implements AclService {

  private final PortProvider fPortProvider;

  /**
   * Constructor.
   */
  public AclServiceImpl(Session session, PortProvider portProvider) {
    setSession(session);
    fPortProvider = portProvider;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ACLService#applyACL(java.lang.String, java.lang.String,
   * org.apache.opencmis.client.provider.AccessControlList, org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.commons.enums.ACLPropagation, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public AccessControlList applyAcl(String repositoryId, String objectId,
      AccessControlList addACEs, AccessControlList removeACEs, AclPropagation aclPropagation,
      ExtensionsData extension) {
    ACLServicePort port = fPortProvider.getACLServicePort();

    try {
      return convert(port.applyACL(repositoryId, objectId, convert(addACEs), convert(removeACEs),
          convert(EnumACLPropagation.class, aclPropagation), convert(extension)));
    }
    catch (CmisException e) {
      throw convertException(e);
    }
    catch (Exception e) {
      throw new CmisRuntimeException("Error: " + e.getMessage(), e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ACLService#getACL(java.lang.String, java.lang.String,
   * java.lang.Boolean, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public AccessControlList getAcl(String repositoryId, String objectId,
      Boolean onlyBasicPermissions, ExtensionsData extension) {
    ACLServicePort port = fPortProvider.getACLServicePort();

    try {
      return convert(port.getACL(repositoryId, objectId, onlyBasicPermissions, convert(extension)));
    }
    catch (CmisException e) {
      throw convertException(e);
    }
    catch (Exception e) {
      throw new CmisRuntimeException("Error: " + e.getMessage(), e);
    }
  }
}
