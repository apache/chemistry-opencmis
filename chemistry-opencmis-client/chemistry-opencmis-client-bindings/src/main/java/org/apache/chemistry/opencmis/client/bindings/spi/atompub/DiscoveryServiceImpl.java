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
package org.apache.chemistry.opencmis.client.bindings.spi.atompub;

import static org.apache.chemistry.opencmis.commons.impl.Converter.convert;

import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomElement;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomEntry;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomFeed;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomLink;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpUtils;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.JaxBHelper;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisQueryType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumIncludeRelationships;
import org.apache.chemistry.opencmis.commons.spi.DiscoveryService;
import org.apache.chemistry.opencmis.commons.spi.Holder;

/**
 * Discovery Service AtomPub client.
 */
public class DiscoveryServiceImpl extends AbstractAtomPubService implements DiscoveryService {

    /**
     * Constructor.
     */
    public DiscoveryServiceImpl(BindingSession session) {
        setSession(session);
    }

    public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties,
            String filter, Boolean includePolicyIds, Boolean includeACL, BigInteger maxItems, ExtensionsData extension) {
        ObjectListImpl result = new ObjectListImpl();

        // find the link
        String link = loadRepositoryLink(repositoryId, Constants.REP_REL_CHANGES);

        if (link == null) {
            throw new CmisObjectNotFoundException("Unknown repository or content changes not supported!");
        }

        UrlBuilder url = new UrlBuilder(link);
        url.addParameter(Constants.PARAM_CHANGE_LOG_TOKEN, (changeLogToken == null ? null : changeLogToken.getValue()));
        url.addParameter(Constants.PARAM_PROPERTIES, includeProperties);
        url.addParameter(Constants.PARAM_FILTER, filter);
        url.addParameter(Constants.PARAM_POLICY_IDS, includePolicyIds);
        url.addParameter(Constants.PARAM_ACL, includeACL);
        url.addParameter(Constants.PARAM_MAX_ITEMS, maxItems);

        // read and parse
        HttpUtils.Response resp = read(url);
        AtomFeed feed = parse(resp.getStream(), AtomFeed.class);

        // handle top level
        for (AtomElement element : feed.getElements()) {
            if (element.getObject() instanceof AtomLink) {
                if (isNextLink(element)) {
                    result.setHasMoreItems(Boolean.TRUE);
                }
            } else if (isInt(NAME_NUM_ITEMS, element)) {
                result.setNumItems((BigInteger) element.getObject());
            }
        }

        // get the changes
        if (!feed.getEntries().isEmpty()) {
            result.setObjects(new ArrayList<ObjectData>(feed.getEntries().size()));

            for (AtomEntry entry : feed.getEntries()) {
                ObjectData hit = null;

                // walk through the entry
                for (AtomElement element : entry.getElements()) {
                    if (element.getObject() instanceof CmisObjectType) {
                        hit = convert((CmisObjectType) element.getObject());
                    }
                }

                if (hit != null) {
                    result.getObjects().add(hit);
                }
            }
        }

        return result;
    }

    public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        ObjectListImpl result = new ObjectListImpl();

        // find the link
        String link = loadCollection(repositoryId, Constants.COLLECTION_QUERY);

        if (link == null) {
            throw new CmisObjectNotFoundException("Unknown repository or query not supported!");
        }

        UrlBuilder url = new UrlBuilder(link);

        // compile query request
        final CmisQueryType query = new CmisQueryType();
        query.setStatement(statement);
        query.setSearchAllVersions(searchAllVersions);
        query.setIncludeAllowableActions(includeAllowableActions);
        query.setIncludeRelationships(convert(EnumIncludeRelationships.class, includeRelationships));
        query.setRenditionFilter(renditionFilter);
        query.setMaxItems(maxItems);
        query.setSkipCount(skipCount);

        // post the query and parse results
        HttpUtils.Response resp = post(url, Constants.MEDIATYPE_QUERY, new HttpUtils.Output() {
            public void write(OutputStream out) throws Exception {
                JaxBHelper.marshal(JaxBHelper.CMIS_OBJECT_FACTORY.createQuery(query), out, false);
            }
        });
        AtomFeed feed = parse(resp.getStream(), AtomFeed.class);

        // handle top level
        for (AtomElement element : feed.getElements()) {
            if (element.getObject() instanceof AtomLink) {
                if (isNextLink(element)) {
                    result.setHasMoreItems(Boolean.TRUE);
                }
            } else if (isInt(NAME_NUM_ITEMS, element)) {
                result.setNumItems((BigInteger) element.getObject());
            }
        }

        // get the result set
        if (!feed.getEntries().isEmpty()) {
            result.setObjects(new ArrayList<ObjectData>(feed.getEntries().size()));

            for (AtomEntry entry : feed.getEntries()) {
                ObjectData hit = null;

                // walk through the entry
                for (AtomElement element : entry.getElements()) {
                    if (element.getObject() instanceof CmisObjectType) {
                        hit = convert((CmisObjectType) element.getObject());
                    }
                }

                if (hit != null) {
                    result.getObjects().add(hit);
                }
            }
        }

        return result;
    }
}
