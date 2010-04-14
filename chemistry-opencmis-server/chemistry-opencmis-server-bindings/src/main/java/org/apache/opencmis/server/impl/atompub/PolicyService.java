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

import static org.apache.opencmis.commons.impl.Converter.convert;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_POLICIES;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.compileBaseUrl;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.compileUrl;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.compileUrlBuilder;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getStringParameter;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.UrlBuilder;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.server.impl.ObjectInfoHolderImpl;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisPolicyService;
import org.apache.opencmis.server.spi.ObjectInfo;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

/**
 * Policy Service operations.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class PolicyService {

  /**
   * Get applied policies.
   */
  public static void getAppliedPolicies(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisPolicyService service = factory.getPolicyService();

    // get parameters
    String objectId = getStringParameter(request, Constants.PARAM_ID);
    String filter = getStringParameter(request, Constants.PARAM_FILTER);

    // execute
    ObjectInfoHolder objectInfoHolder = new ObjectInfoHolderImpl();
    List<ObjectData> policies = service.getAppliedPolicies(context, repositoryId, objectId, filter,
        null, objectInfoHolder);

    if (policies == null) {
      throw new CmisRuntimeException("Policies are null!");
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
        objectInfo.getLastModificationDate(), null, null);

    // write links
    UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

    feed.writeServiceLink(baseUrl.toString(), repositoryId);

    feed.writeSelfLink(compileUrl(baseUrl, RESOURCE_POLICIES, objectInfo.getId()), null);

    // write entries
    if (policies != null) {
      AtomEntry entry = new AtomEntry(feed.getWriter());
      for (ObjectData policy : policies) {
        if (policy == null) {
          continue;
        }
        writePolicyEntry(entry, objectInfo.getId(), policy, objectInfoHolder, baseUrl);
      }
    }

    // we are done
    feed.endFeed();
    feed.endDocument();
  }

  /**
   * Apply policy.
   */
  public static void applyPolicy(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisPolicyService service = factory.getPolicyService();

    // get parameters
    String objectId = getStringParameter(request, Constants.PARAM_ID);

    AtomEntryParser parser = new AtomEntryParser(request.getInputStream());

    // execute
    ObjectInfoHolder objectInfoHolder = new ObjectInfoHolderImpl();
    ObjectData policy = service.applyPolicy(context, repositoryId, parser.getId(), objectId, null,
        objectInfoHolder);

    if (policy == null) {
      throw new CmisRuntimeException("Policy is null!");
    }

    // set headers
    UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);
    UrlBuilder location = compileUrlBuilder(baseUrl, RESOURCE_POLICIES, objectId);
    location.addParameter(Constants.PARAM_POLICY_ID, policy.getId());

    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setContentType(Constants.MEDIATYPE_ENTRY);
    response.setHeader("Content-Location", location.toString());
    response.setHeader("Location", location.toString());

    // write XML
    AtomEntry entry = new AtomEntry();
    entry.startDocument(response.getOutputStream());
    writePolicyEntry(entry, objectId, policy, objectInfoHolder, baseUrl);
    entry.endDocument();
  }

  /**
   * Remove policy.
   */
  public static void removePolicy(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisPolicyService service = factory.getPolicyService();

    // get parameters
    String objectId = getStringParameter(request, Constants.PARAM_ID);
    String policyId = getStringParameter(request, Constants.PARAM_POLICY_ID);

    // execute
    service.removePolicy(context, repositoryId, policyId, objectId, null);

    // set headers
    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  /**
   * Writes an entry that is attached to an object.
   */
  private static void writePolicyEntry(AtomEntry entry, String objectId, ObjectData policy,
      ObjectInfoHolder infoHolder, UrlBuilder baseUrl) throws Exception {
    CmisObjectType resultJaxb = convert(policy);
    if (resultJaxb == null) {
      return;
    }

    ObjectInfo info = infoHolder.getObjectInfo(policy.getId());
    if (info == null) {
      throw new CmisRuntimeException("Object Info not found!");
    }

    // start
    entry.startEntry(false);

    // write the object
    entry.writeObject(policy, info, null, null, null, null);

    // write links
    UrlBuilder selfLink = compileUrlBuilder(baseUrl, RESOURCE_POLICIES, objectId);
    selfLink.addParameter(Constants.PARAM_POLICY_ID, info.getId());
    entry.writeSelfLink(selfLink.toString(), null);

    // we are done
    entry.endEntry();
  }
}
