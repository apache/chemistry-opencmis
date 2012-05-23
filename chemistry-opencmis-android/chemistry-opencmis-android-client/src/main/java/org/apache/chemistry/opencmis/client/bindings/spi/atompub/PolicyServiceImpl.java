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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomElement;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomEntry;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomFeed;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomLink;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.spi.PolicyService;

/**
 * Policy Service AtomPub client.
 */
public class PolicyServiceImpl extends AbstractAtomPubService implements PolicyService {

    /**
     * Constructor.
     */
    public PolicyServiceImpl(BindingSession session) {
        setSession(session);
    }

    public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        // find the link
        String link = loadLink(repositoryId, objectId, Constants.REL_POLICIES, Constants.MEDIATYPE_FEED);

        if (link == null) {
            throwLinkException(repositoryId, objectId, Constants.REL_POLICIES, Constants.MEDIATYPE_FEED);
        }

        UrlBuilder url = new UrlBuilder(link);

        // set up object and writer
        final AtomEntryWriter entryWriter = new AtomEntryWriter(createIdObject(objectId));

        // post applyPolicy request
        post(url, Constants.MEDIATYPE_ENTRY, new HttpUtils.Output() {
            public void write(OutputStream out) throws Exception {
                entryWriter.write(out);
            }
        });
    }

    public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter,
            ExtensionsData extension) {
        List<ObjectData> result = new ArrayList<ObjectData>();

        // find the link
        String link = loadLink(repositoryId, objectId, Constants.REL_POLICIES, Constants.MEDIATYPE_FEED);

        if (link == null) {
            throwLinkException(repositoryId, objectId, Constants.REL_POLICIES, Constants.MEDIATYPE_FEED);
        }

        UrlBuilder url = new UrlBuilder(link);
        url.addParameter(Constants.PARAM_FILTER, filter);

        // read and parse
        HttpUtils.Response resp = read(url);
        AtomFeed feed = parse(resp.getStream(), AtomFeed.class);

        // get the policies
        if (!feed.getEntries().isEmpty()) {
            for (AtomEntry entry : feed.getEntries()) {
                ObjectData policy = null;

                // walk through the entry
                for (AtomElement element : entry.getElements()) {
                    if (element.getObject() instanceof ObjectData) {
                        policy = (ObjectData) element.getObject();
                    }
                }

                if (policy != null) {
                    result.add(policy);
                }
            }
        }

        return result;
    }

    public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        // we need a policy id
        if (policyId == null) {
            throw new CmisInvalidArgumentException("Policy id must be set!");
        }

        // find the link
        String link = loadLink(repositoryId, objectId, Constants.REL_POLICIES, Constants.MEDIATYPE_FEED);

        if (link == null) {
            throwLinkException(repositoryId, objectId, Constants.REL_POLICIES, Constants.MEDIATYPE_FEED);
        }

        UrlBuilder url = new UrlBuilder(link);
        url.addParameter(Constants.PARAM_FILTER, PropertyIds.OBJECT_ID);

        // read and parse
        HttpUtils.Response resp = read(url);
        AtomFeed feed = parse(resp.getStream(), AtomFeed.class);

        // find the policy
        String policyLink = null;
        boolean found = false;

        if (!feed.getEntries().isEmpty()) {
            for (AtomEntry entry : feed.getEntries()) {
                // walk through the entry
                for (AtomElement element : entry.getElements()) {
                    if (element.getObject() instanceof AtomLink) {
                        AtomLink atomLink = (AtomLink) element.getObject();
                        if (Constants.REL_SELF.equals(atomLink.getRel())) {
                            policyLink = atomLink.getHref();
                        }
                    } else if (element.getObject() instanceof ObjectData) {
                        String id = findIdProperty((ObjectData) element.getObject());
                        if (policyId.equals(id)) {
                            found = true;
                        }
                    }
                }

                if (found) {
                    break;
                }
            }
        }

        // if found, delete it
        if (found && (policyLink != null)) {
            delete(new UrlBuilder(policyLink));
        }
    }

    /**
     * Finds the id property within a CMIS object.
     */
    private static String findIdProperty(ObjectData object) {
        if ((object == null) || (object.getProperties() == null)) {
            return null;
        }

        for (PropertyData<?> property : object.getProperties().getPropertyList()) {
            if (PropertyIds.OBJECT_ID.equals(property.getId()) && (property instanceof PropertyId)) {
                List<String> values = ((PropertyId) property).getValues();
                if (values.size() == 1) {
                    return values.get(0);
                }
            }
        }

        return null;
    }
}
