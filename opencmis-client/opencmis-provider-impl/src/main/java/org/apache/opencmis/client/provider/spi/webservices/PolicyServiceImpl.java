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
import static org.apache.opencmis.commons.impl.Converter.convertExtensionHolder;
import static org.apache.opencmis.commons.impl.Converter.setExtensionValues;

import java.util.ArrayList;
import java.util.List;

import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.jaxb.CmisException;
import org.apache.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.impl.jaxb.PolicyServicePort;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PolicyService;

/**
 * Policy Service Web Services client.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class PolicyServiceImpl extends AbstractWebServicesService implements PolicyService {

  private PortProvider fPortProvider;

  /**
   * Constructor.
   */
  public PolicyServiceImpl(Session session, PortProvider portProvider) {
    setSession(session);
    fPortProvider = portProvider;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PolicyService#applyPolicy(java.lang.String,
   * java.lang.String, java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void applyPolicy(String repositoryId, String policyId, String objectId,
      ExtensionsData extension) {
    PolicyServicePort port = fPortProvider.getPolicyServicePort();

    try {
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.applyPolicy(repositoryId, policyId, objectId, portExtension);

      setExtensionValues(portExtension, extension);
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
   * @see org.apache.opencmis.client.provider.PolicyService#removePolicy(java.lang.String,
   * java.lang.String, java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void removePolicy(String repositoryId, String policyId, String objectId,
      ExtensionsData extension) {
    PolicyServicePort port = fPortProvider.getPolicyServicePort();

    try {
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.removePolicy(repositoryId, policyId, objectId, portExtension);

      setExtensionValues(portExtension, extension);
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
   * @see org.apache.opencmis.client.provider.PolicyService#getAppliedPolicies(java.lang.String,
   * java.lang.String, java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter,
      ExtensionsData extension) {
    PolicyServicePort port = fPortProvider.getPolicyServicePort();

    try {
      List<CmisObjectType> policyList = port.getAppliedPolicies(repositoryId, objectId, filter,
          convert(extension));

      // no list?
      if (policyList == null) {
        return null;
      }

      // convert list
      List<ObjectData> result = new ArrayList<ObjectData>();
      for (CmisObjectType policy : policyList) {
        result.add(convert(policy));
      }

      return result;
    }
    catch (CmisException e) {
      throw convertException(e);
    }
    catch (Exception e) {
      throw new CmisRuntimeException("Error: " + e.getMessage(), e);
    }
  }
}
