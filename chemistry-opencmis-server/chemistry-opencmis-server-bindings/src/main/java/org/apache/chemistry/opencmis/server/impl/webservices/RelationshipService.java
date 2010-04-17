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
package org.apache.chemistry.opencmis.server.impl.webservices;

import static org.apache.chemistry.opencmis.commons.impl.Converter.convert;

import java.math.BigInteger;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.apache.chemistry.opencmis.commons.api.server.CallContext;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectListType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumRelationshipDirection;
import org.apache.chemistry.opencmis.commons.impl.jaxb.RelationshipServicePort;
import org.apache.chemistry.opencmis.server.spi.AbstractServicesFactory;
import org.apache.chemistry.opencmis.server.spi.CmisRelationshipService;

/**
 * CMIS Relationship Service.
 */
@WebService(endpointInterface = "org.apache.chemistry.opencmis.commons.impl.jaxb.RelationshipServicePort")
public class RelationshipService extends AbstractService implements RelationshipServicePort {
	@Resource
	WebServiceContext wsContext;

	public CmisObjectListType getObjectRelationships(String repositoryId, String objectId,
			Boolean includeSubRelationshipTypes, EnumRelationshipDirection relationshipDirection, String typeId,
			String filter, Boolean includeAllowableActions, BigInteger maxItems, BigInteger skipCount,
			CmisExtensionType extension) throws CmisException {
		try {
			AbstractServicesFactory factory = getServicesFactory(wsContext);
			CmisRelationshipService service = factory.getRelationshipService();
			CallContext context = createContext(wsContext, repositoryId);

			return convert(service.getObjectRelationships(context, repositoryId, objectId, includeSubRelationshipTypes,
					convert(RelationshipDirection.class, relationshipDirection), typeId, filter,
					includeAllowableActions, maxItems, skipCount, convert(extension), null));
		} catch (Exception e) {
			throw convertException(e);
		}
	}

}
