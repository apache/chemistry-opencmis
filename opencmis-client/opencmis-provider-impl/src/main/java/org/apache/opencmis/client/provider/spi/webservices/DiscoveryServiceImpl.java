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
import static org.apache.opencmis.commons.impl.Converter.convertHolder;
import static org.apache.opencmis.commons.impl.Converter.setHolderValue;

import java.math.BigInteger;

import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.jaxb.CmisException;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectListType;
import org.apache.opencmis.commons.impl.jaxb.DiscoveryServicePort;
import org.apache.opencmis.commons.impl.jaxb.EnumIncludeRelationships;
import org.apache.opencmis.commons.provider.DiscoveryService;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectList;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class DiscoveryServiceImpl extends AbstractWebServicesService implements DiscoveryService {

  private PortProvider fPortProvider;

  /**
   * Constructor.
   */
  public DiscoveryServiceImpl(Session session, PortProvider portProvider) {
    setSession(session);
    fPortProvider = portProvider;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.DiscoveryService#getContentChanges(java.lang.String,
   * org.apache.opencmis.client.provider.Holder, java.lang.Boolean, java.lang.String, java.lang.Boolean,
   * java.lang.Boolean, java.math.BigInteger, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken,
      Boolean includeProperties, String filter, Boolean includePolicyIds, Boolean includeACL,
      BigInteger maxItems, ExtensionsData extension) {
    DiscoveryServicePort port = fPortProvider.getDiscoveryServicePort();

    try {
      javax.xml.ws.Holder<String> portChangeLokToken = convertHolder(changeLogToken);
      javax.xml.ws.Holder<CmisObjectListType> portObjects = new javax.xml.ws.Holder<CmisObjectListType>();

      port.getContentChanges(repositoryId, portChangeLokToken, includeProperties, filter,
          includePolicyIds, includeACL, maxItems, convert(extension), portObjects);

      setHolderValue(portChangeLokToken, changeLogToken);

      return convert(portObjects.value);
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
   * @see org.apache.opencmis.client.provider.DiscoveryService#query(java.lang.String, java.lang.String,
   * java.lang.Boolean, java.lang.Boolean, org.apache.opencmis.commons.enums.IncludeRelationships,
   * java.lang.String, java.math.BigInteger, java.math.BigInteger,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
    DiscoveryServicePort port = fPortProvider.getDiscoveryServicePort();

    try {
      return convert(port.query(repositoryId, statement, searchAllVersions,
          includeAllowableActions, convert(EnumIncludeRelationships.class, includeRelationships),
          renditionFilter, maxItems, skipCount, convert(extension)));
    }
    catch (CmisException e) {
      throw convertException(e);
    }
    catch (Exception e) {
      throw new CmisRuntimeException("Error: " + e.getMessage(), e);
    }
  }

}
