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
import static org.apache.opencmis.commons.impl.Converter.convertExtensionHolder;
import static org.apache.opencmis.commons.impl.Converter.setExtensionValues;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.impl.jaxb.CmisException;
import org.apache.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.impl.jaxb.PolicyServicePort;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisPolicyService;

/**
 * CMIS Policy Service.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
@WebService(endpointInterface = "org.apache.opencmis.commons.impl.jaxb.PolicyServicePort")
public class PolicyService extends AbstractService implements PolicyServicePort {
  @Resource
  WebServiceContext fContext;

  public void applyPolicy(String repositoryId, String policyId, String objectId,
      Holder<CmisExtensionType> extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisPolicyService service = factory.getPolicyService();
      CallContext context = createContext(fContext);

      ExtensionsData extData = convertExtensionHolder(extension);

      service.applyPolicy(context, repositoryId, policyId, objectId, extData, null);

      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public List<CmisObjectType> getAppliedPolicies(String repositoryId, String objectId,
      String filter, CmisExtensionType extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisPolicyService service = factory.getPolicyService();
      CallContext context = createContext(fContext);

      List<ObjectData> policies = service.getAppliedPolicies(context, repositoryId, objectId,
          filter, convert(extension), null);

      if (policies == null) {
        return null;
      }

      List<CmisObjectType> result = new ArrayList<CmisObjectType>();
      for (ObjectData object : policies) {
        result.add(convert(object));
      }

      return result;
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public void removePolicy(String repositoryId, String policyId, String objectId,
      Holder<CmisExtensionType> extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisPolicyService service = factory.getPolicyService();
      CallContext context = createContext(fContext);

      ExtensionsData extData = convertExtensionHolder(extension);

      service.removePolicy(context, repositoryId, policyId, objectId, extData);

      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

}
