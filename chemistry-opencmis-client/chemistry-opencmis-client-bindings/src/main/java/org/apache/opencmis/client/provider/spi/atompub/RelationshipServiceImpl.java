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
package org.apache.opencmis.client.provider.spi.atompub;

import static org.apache.opencmis.commons.impl.Converter.convert;

import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomElement;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomEntry;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomFeed;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomLink;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.RelationshipDirection;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.UrlBuilder;
import org.apache.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.commons.provider.RelationshipService;

/**
 * Relationship Service AtomPub client.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RelationshipServiceImpl extends AbstractAtomPubService implements RelationshipService {

  /**
   * Constructor.
   */
  public RelationshipServiceImpl(Session session) {
    setSession(session);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.RelationshipService#getObjectRelationships(java.lang.String
   * , java.lang.String, java.lang.Boolean, org.apache.opencmis.commons.enums.RelationshipDirection,
   * java.lang.String, java.lang.String, java.lang.Boolean, java.math.BigInteger,
   * java.math.BigInteger, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public ObjectList getObjectRelationships(String repositoryId, String objectId,
      Boolean includeSubRelationshipTypes, RelationshipDirection relationshipDirection,
      String typeId, String filter, Boolean includeAllowableActions, BigInteger maxItems,
      BigInteger skipCount, ExtensionsData extension) {
    ObjectListImpl result = new ObjectListImpl();

    // find the link
    String link = loadLink(repositoryId, objectId, Constants.REL_RELATIONSHIPS,
        Constants.MEDIATYPE_FEED);

    if (link == null) {
      throwLinkException(repositoryId, objectId, Constants.REL_RELATIONSHIPS,
          Constants.MEDIATYPE_FEED);
    }

    UrlBuilder url = new UrlBuilder(link);
    url.addParameter(Constants.PARAM_SUB_RELATIONSHIP_TYPES, includeSubRelationshipTypes);
    url.addParameter(Constants.PARAM_RELATIONSHIP_DIRECTION, relationshipDirection);
    url.addParameter(Constants.PARAM_TYPE_ID, typeId);
    url.addParameter(Constants.PARAM_FILTER, filter);
    url.addParameter(Constants.PARAM_ALLOWABLE_ACTIONS, includeAllowableActions);
    url.addParameter(Constants.PARAM_MAX_ITEMS, maxItems);
    url.addParameter(Constants.PARAM_SKIP_COUNT, skipCount);

    // read and parse
    HttpUtils.Response resp = read(url);
    AtomFeed feed = parse(resp.getStream(), AtomFeed.class);

    // handle top level
    for (AtomElement element : feed.getElements()) {
      if (element.getObject() instanceof AtomLink) {
        if (isNextLink(element)) {
          result.setHasMoreItems(Boolean.TRUE);
        }
      }
      else if (isInt(NAME_NUM_ITEMS, element)) {
        result.setNumItems((BigInteger) element.getObject());
      }
    }

    // get the children
    if (!feed.getEntries().isEmpty()) {
      result.setObjects(new ArrayList<ObjectData>(feed.getEntries().size()));

      for (AtomEntry entry : feed.getEntries()) {
        ObjectData relationship = null;

        lockLinks();
        try {
          // clean up cache
          removeLinks(repositoryId, entry.getId());

          // walk through the entry
          for (AtomElement element : entry.getElements()) {
            if (element.getObject() instanceof AtomLink) {
              addLink(repositoryId, entry.getId(), (AtomLink) element.getObject());
            }
            else if (element.getObject() instanceof CmisObjectType) {
              relationship = convert((CmisObjectType) element.getObject());
            }
          }
        }
        finally {
          unlockLinks();
        }

        if (relationship != null) {
          result.getObjects().add(relationship);
        }
      }

    }

    return result;
  }
}
