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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomElement;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomEntry;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomFeed;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomLink;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.UrlBuilder;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.impl.jaxb.CmisProperty;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyId;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PolicyService;

/**
 * Policy Service AtomPub client.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class PolicyServiceImpl extends AbstractAtomPubService implements PolicyService {

  /**
   * Constructor.
   */
  public PolicyServiceImpl(Session session) {
    setSession(session);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PolicyService#applyPolicy(java.lang.String, java.lang.String,
   * java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void applyPolicy(String repositoryId, String policyId, String objectId,
      ExtensionsData extension) {
    // find the link
    String link = loadLink(repositoryId, objectId, Constants.REL_POLICIES, Constants.MEDIATYPE_FEED);

    if (link == null) {
      throw new CmisObjectNotFoundException("Unknown repository or object!");
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

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PolicyService#getAppliedPolicies(java.lang.String,
   * java.lang.String, java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter,
      ExtensionsData extension) {
    List<ObjectData> result = new ArrayList<ObjectData>();

    // find the link
    String link = loadLink(repositoryId, objectId, Constants.REL_POLICIES, Constants.MEDIATYPE_FEED);

    if (link == null) {
      throw new CmisObjectNotFoundException("Unknown repository or object!");
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
          if (element.getObject() instanceof CmisObjectType) {
            policy = convert((CmisObjectType) element.getObject());
          }
        }

        if (policy != null) {
          result.add(policy);
        }
      }
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PolicyService#removePolicy(java.lang.String,
   * java.lang.String, java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void removePolicy(String repositoryId, String policyId, String objectId,
      ExtensionsData extension) {
    // we need a policy id
    if (policyId == null) {
      throw new CmisInvalidArgumentException("Policy id must be set!");
    }

    // find the link
    String link = loadLink(repositoryId, objectId, Constants.REL_POLICIES, Constants.MEDIATYPE_FEED);

    if (link == null) {
      throw new CmisObjectNotFoundException("Unknown repository or object!");
    }

    UrlBuilder url = new UrlBuilder(link);
    url.addParameter(Constants.PARAM_FILTER, PropertyIds.CMIS_OBJECT_ID);

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
          }
          else if (element.getObject() instanceof CmisObjectType) {
            String id = findIdProperty((CmisObjectType) element.getObject());
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
  private String findIdProperty(CmisObjectType object) {
    if ((object == null) || (object.getProperties() == null)) {
      return null;
    }

    for (CmisProperty property : object.getProperties().getProperty()) {
      if (PropertyIds.CMIS_OBJECT_ID.equals(property.getPropertyDefinitionId())
          && (property instanceof CmisPropertyId)) {
        List<String> values = ((CmisPropertyId) property).getValue();
        if (values.size() == 1) {
          return values.get(0);
        }
      }
    }

    return null;
  }
}
