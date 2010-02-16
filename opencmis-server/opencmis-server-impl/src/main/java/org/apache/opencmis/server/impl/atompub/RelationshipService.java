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

import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_RELATIONSHIPS;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.compileBaseUrl;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.compileUrl;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.compileUrlBuilder;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getBigIntegerParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getBooleanParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getEnumParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getStringParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.writeObjectEntry;

import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.opencmis.commons.enums.RelationshipDirection;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.UrlBuilder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.server.impl.ObjectInfoHolderImpl;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisRelationshipService;
import org.apache.opencmis.server.spi.ObjectInfo;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

/**
 * Relationship Service operations.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RelationshipService {

  /**
   * Get object relationships.
   */
  public static void getObjectRelationships(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisRelationshipService service = factory.getRelationshipService();

    // get parameters
    String objectId = getStringParameter(request, Constants.PARAM_ID);
    Boolean includeSubRelationshipTypes = getBooleanParameter(request,
        Constants.PARAM_SUB_RELATIONSHIP_TYPES);
    RelationshipDirection relationshipDirection = getEnumParameter(request,
        Constants.PARAM_RELATIONSHIP_DIRECTION, RelationshipDirection.class);
    String typeId = getStringParameter(request, Constants.PARAM_TYPE_ID);
    String filter = getStringParameter(request, Constants.PARAM_FILTER);
    Boolean includeAllowableActions = getBooleanParameter(request,
        Constants.PARAM_ALLOWABLE_ACTIONS);
    BigInteger maxItems = getBigIntegerParameter(request, Constants.PARAM_MAX_ITEMS);
    BigInteger skipCount = getBigIntegerParameter(request, Constants.PARAM_SKIP_COUNT);

    // execute
    ObjectInfoHolder objectInfoHolder = new ObjectInfoHolderImpl();
    ObjectList relationships = service.getObjectRelationships(context, repositoryId, objectId,
        includeSubRelationshipTypes, relationshipDirection, typeId, filter,
        includeAllowableActions, maxItems, skipCount, null, objectInfoHolder);

    if (relationships == null) {
      throw new CmisRuntimeException("Relationships are null!");
    }

    ObjectInfo objectInfo = objectInfoHolder.getObjectInfo(objectId);
    if (objectInfo == null) {
      throw new CmisRuntimeException("Object Info is missing!");
    }

    // set headers
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(Constants.MEDIATYPE_FEED);

    // write XML
    AtomFeed feed = new AtomFeed();
    feed.startDocument(response.getOutputStream());
    feed.startFeed(true);

    // write basic Atom feed elements
    feed.writeFeedElements(objectInfo.getId(), objectInfo.getCreatedBy(), objectInfo.getName(),
        objectInfo.getLastModificationDate(), null, relationships.getNumItems());

    // write links
    UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

    feed.writeServiceLink(baseUrl.toString(), repositoryId);

    feed.writeSelfLink(compileUrl(baseUrl, RESOURCE_RELATIONSHIPS, objectInfo.getId()), null);

    UrlBuilder pagingUrl = new UrlBuilder(compileUrlBuilder(baseUrl, RESOURCE_RELATIONSHIPS,
        objectInfo.getId()));
    pagingUrl.addParameter(Constants.PARAM_SUB_RELATIONSHIP_TYPES, includeSubRelationshipTypes);
    pagingUrl.addParameter(Constants.PARAM_RELATIONSHIP_DIRECTION, relationshipDirection);
    pagingUrl.addParameter(Constants.PARAM_TYPE_ID, typeId);
    pagingUrl.addParameter(Constants.PARAM_FILTER, filter);
    pagingUrl.addParameter(Constants.PARAM_ALLOWABLE_ACTIONS, includeAllowableActions);
    feed.writePagingLinks(pagingUrl, maxItems, skipCount, relationships.getNumItems(),
        relationships.hasMoreItems(), AtomPubUtils.PAGE_SIZE);

    // write entries
    if (relationships != null) {
      AtomEntry entry = new AtomEntry(feed.getWriter());
      for (ObjectData object : relationships.getObjects()) {
        if (object == null) {
          continue;
        }
        writeObjectEntry(entry, object, objectInfoHolder, null, repositoryId, null, null, baseUrl,
            false);
      }
    }

    // we are done
    feed.endFeed();
    feed.endDocument();
  }
}
