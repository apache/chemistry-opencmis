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
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CHANGES;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_QUERY;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.compileBaseUrl;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.compileUrlBuilder;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getBigIntegerParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getBooleanParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getEnumParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getStringParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.writeObjectEntry;

import java.math.BigInteger;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.JaxBHelper;
import org.apache.opencmis.commons.impl.UrlBuilder;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.impl.jaxb.CmisQueryType;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.server.impl.ObjectInfoHolderImpl;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisDiscoveryService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

/**
 * Discovery Service operations.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class DiscoveryService {

  private static final String METHOD_GET = "GET";
  private static final String METHOD_POST = "POST";

  /**
   * Query.
   */
  public static void query(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisDiscoveryService service = factory.getDiscoveryService();

    // get parameters
    String statement = null;
    Boolean searchAllVersions = null;
    Boolean includeAllowableActions = null;
    IncludeRelationships includeRelationships = null;
    String renditionFilter = null;
    BigInteger maxItems = null;
    BigInteger skipCount = null;

    int statusCode = 0;

    if (METHOD_POST.equals(request.getMethod())) {
      // POST -> read from stream
      Object queryRequest = null;
      try {
        Unmarshaller u = JaxBHelper.createUnmarshaller();
        queryRequest = u.unmarshal(request.getInputStream());
      }
      catch (Exception e) {
        throw new CmisInvalidArgumentException("Invalid query request: " + e, e);
      }

      if (!(queryRequest instanceof JAXBElement<?>)) {
        throw new CmisInvalidArgumentException("Not a query document!");
      }

      if (!(((JAXBElement<?>) queryRequest).getValue() instanceof CmisQueryType)) {
        throw new CmisInvalidArgumentException("Not a query document!");
      }

      CmisQueryType queryType = (CmisQueryType) ((JAXBElement<?>) queryRequest).getValue();

      statement = queryType.getStatement();
      searchAllVersions = queryType.isSearchAllVersions();
      includeAllowableActions = queryType.isIncludeAllowableActions();
      includeRelationships = convert(IncludeRelationships.class, queryType
          .getIncludeRelationships());
      renditionFilter = queryType.getRenditionFilter();
      maxItems = queryType.getMaxItems();
      skipCount = queryType.getSkipCount();

      statusCode = HttpServletResponse.SC_CREATED;
    }
    else if (METHOD_GET.equals(request.getMethod())) {
      // GET -> parameters
      statement = getStringParameter(request, Constants.PARAM_Q);
      searchAllVersions = getBooleanParameter(request, Constants.PARAM_SEARCH_ALL_VERSIONS);
      includeAllowableActions = getBooleanParameter(request, Constants.PARAM_ALLOWABLE_ACTIONS);
      includeRelationships = getEnumParameter(request, Constants.PARAM_RELATIONSHIPS,
          IncludeRelationships.class);
      renditionFilter = null;
      maxItems = getBigIntegerParameter(request, Constants.PARAM_MAX_ITEMS);
      skipCount = getBigIntegerParameter(request, Constants.PARAM_SKIP_COUNT);

      statusCode = HttpServletResponse.SC_OK;
    }
    else {
      throw new CmisRuntimeException("Invalid HTTP method!");
    }

    // execute
    ObjectList results = service.query(context, repositoryId, statement, searchAllVersions,
        includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount, null);

    if (results == null) {
      throw new CmisRuntimeException("Results are null!");
    }

    // set headers
    UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

    UrlBuilder pagingUrl = compileUrlBuilder(baseUrl, RESOURCE_QUERY, null);
    pagingUrl.addParameter(Constants.PARAM_Q, statement);
    pagingUrl.addParameter(Constants.PARAM_SEARCH_ALL_VERSIONS, searchAllVersions);
    pagingUrl.addParameter(Constants.PARAM_ALLOWABLE_ACTIONS, includeAllowableActions);
    pagingUrl.addParameter(Constants.PARAM_RELATIONSHIPS, includeRelationships);

    UrlBuilder location = new UrlBuilder(pagingUrl);
    location.addParameter(Constants.PARAM_MAX_ITEMS, maxItems);
    location.addParameter(Constants.PARAM_SKIP_COUNT, skipCount);

    response.setStatus(statusCode);
    response.setContentType(Constants.MEDIATYPE_FEED);
    response.setHeader("Content-Location", location.toString());
    response.setHeader("Location", location.toString());

    // write XML
    AtomFeed feed = new AtomFeed();
    feed.startDocument(response.getOutputStream());
    feed.startFeed(true);

    // write basic Atom feed elements
    GregorianCalendar now = new GregorianCalendar();
    feed.writeFeedElements("query", "", "Query", now, null, results.getNumItems());

    // write links
    feed.writeServiceLink(baseUrl.toString(), repositoryId);

    feed.writePagingLinks(pagingUrl, maxItems, skipCount, results.getNumItems(), results
        .hasMoreItems(), AtomPubUtils.PAGE_SIZE);

    if (results.getObjects() != null) {
      AtomEntry entry = new AtomEntry(feed.getWriter());
      int idCounter = 0;
      for (ObjectData result : results.getObjects()) {
        if (result == null) {
          continue;
        }
        idCounter++;
        writeQueryResultEntry(entry, result, "id-" + idCounter, now);
      }
    }

    // we are done
    feed.endFeed();
    feed.endDocument();
  }

  private static void writeQueryResultEntry(AtomEntry entry, ObjectData result, String id,
      GregorianCalendar now) throws Exception {
    CmisObjectType resultJaxb = convert(result);
    if (resultJaxb == null) {
      return;
    }

    // start
    entry.startEntry(false);

    // write Atom base tags
    entry.writeAuthor("");
    entry.writeId(entry.generateAtomId(id));
    entry.writePublished(now);
    entry.writeTitle("Query Result " + id);
    entry.writeUpdated(now);

    // write query result object
    JaxBHelper.marshal(JaxBHelper.CMIS_EXTRA_OBJECT_FACTORY.createObject(resultJaxb), entry
        .getWriter(), true);

    // we are done
    entry.endEntry();
  }

  /**
   * Get content changes.
   */
  public static void getContentChanges(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisDiscoveryService service = factory.getDiscoveryService();

    // get parameters
    String changeLogToken = getStringParameter(request, Constants.PARAM_CHANGE_LOG_TOKEN);
    Boolean includeProperties = getBooleanParameter(request, Constants.PARAM_PROPERTIES);
    String filter = getStringParameter(request, Constants.PARAM_FILTER);
    Boolean includePolicyIds = getBooleanParameter(request, Constants.PARAM_POLICY_IDS);
    Boolean includeAcl = getBooleanParameter(request, Constants.PARAM_ACL);
    BigInteger maxItems = getBigIntegerParameter(request, Constants.PARAM_MAX_ITEMS);

    // execute
    ObjectInfoHolder objectInfoHolder = new ObjectInfoHolderImpl();
    Holder<String> changeLogTokenHolder = new Holder<String>(changeLogToken);
    ObjectList changes = service.getContentChanges(context, repositoryId, changeLogTokenHolder,
        includeProperties, filter, includePolicyIds, includeAcl, maxItems, null, objectInfoHolder);

    if (changes == null) {
      throw new CmisRuntimeException("Changes are null!");
    }

    // set headers
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(Constants.MEDIATYPE_FEED);

    // write XML
    AtomFeed feed = new AtomFeed();
    feed.startDocument(response.getOutputStream());
    feed.startFeed(true);

    // write basic Atom feed elements
    GregorianCalendar now = new GregorianCalendar();
    feed
        .writeFeedElements("contentChanges", "", "Content Change", now, null, changes.getNumItems());

    // write links
    UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

    feed.writeServiceLink(baseUrl.toString(), repositoryId);

    if (changeLogTokenHolder.getValue() != null) {
      UrlBuilder nextLink = compileUrlBuilder(baseUrl, RESOURCE_CHANGES, null);
      nextLink.addParameter(Constants.PARAM_CHANGE_LOG_TOKEN, changeLogTokenHolder.getValue());
      nextLink.addParameter(Constants.PARAM_PROPERTIES, includeProperties);
      nextLink.addParameter(Constants.PARAM_FILTER, filter);
      nextLink.addParameter(Constants.PARAM_POLICY_IDS, includePolicyIds);
      nextLink.addParameter(Constants.PARAM_ACL, includeAcl);
      nextLink.addParameter(Constants.PARAM_MAX_ITEMS, maxItems);
      feed.writeNextLink(nextLink.toString());
    }

    // write entries
    if (changes.getObjects() != null) {
      AtomEntry entry = new AtomEntry(feed.getWriter());
      for (ObjectData object : changes.getObjects()) {
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
