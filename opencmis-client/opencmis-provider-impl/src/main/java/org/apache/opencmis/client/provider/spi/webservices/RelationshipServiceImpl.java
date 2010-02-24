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

import java.math.BigInteger;

import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.RelationshipDirection;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.jaxb.CmisException;
import org.apache.opencmis.commons.impl.jaxb.EnumRelationshipDirection;
import org.apache.opencmis.commons.impl.jaxb.RelationshipServicePort;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.commons.provider.RelationshipService;

/**
 * Relationship Service Web Services client.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RelationshipServiceImpl extends AbstractWebServicesService implements
    RelationshipService {

  private PortProvider fPortProvider;

  /**
   * Constructor.
   */
  public RelationshipServiceImpl(Session session, PortProvider portProvider) {
    setSession(session);
    fPortProvider = portProvider;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RelationshipService#getObjectRelationships(java.lang.String,
   * java.lang.String, java.lang.Boolean, org.apache.opencmis.commons.enums.RelationshipDirection,
   * java.lang.String, java.lang.String, java.lang.Boolean, java.math.BigInteger,
   * java.math.BigInteger, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public ObjectList getObjectRelationships(String repositoryId, String objectId,
      Boolean includeSubRelationshipTypes, RelationshipDirection relationshipDirection,
      String typeId, String filter, Boolean includeAllowableActions, BigInteger maxItems,
      BigInteger skipCount, ExtensionsData extension) {
    RelationshipServicePort port = fPortProvider.getRelationshipServicePort();

    try {
      return convert(port.getObjectRelationships(repositoryId, objectId,
          includeSubRelationshipTypes, convert(EnumRelationshipDirection.class,
              relationshipDirection), typeId, filter, includeAllowableActions, maxItems, skipCount,
          convert(extension)));
    }
    catch (CmisException e) {
      throw convertException(e);
    }
    catch (Exception e) {
      throw new CmisRuntimeException("Error: " + e.getMessage(), e);
    }
  }

}
