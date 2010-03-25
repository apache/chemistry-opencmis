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

import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_ENTRY;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_VERSIONS;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.compileBaseUrl;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.compileUrl;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getBooleanParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getStringParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.writeObjectEntry;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.UrlBuilder;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.server.impl.ObjectInfoHolderImpl;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisObjectService;
import org.apache.opencmis.server.spi.CmisVersioningService;
import org.apache.opencmis.server.spi.ObjectInfo;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

/**
 * Versioning Service operations.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class VersioningService {

  /**
   * Check Out.
   */
  public static void checkOut(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisVersioningService service = factory.getVersioningService();

    // get parameters
    AtomEntryParser parser = new AtomEntryParser(request.getInputStream());

    // execute
    ObjectInfoHolder objectInfoHolder = new ObjectInfoHolderImpl();
    ObjectData object = service.checkOut(context, repositoryId, new Holder<String>(parser.getId()),
        null, null, objectInfoHolder);

    if (object == null) {
      throw new CmisRuntimeException("Object is null!");
    }

    if (object.getId() == null) {
      throw new CmisRuntimeException("Object Id is null!");
    }

    // set headers
    UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);
    String location = compileUrl(baseUrl, RESOURCE_ENTRY, object.getId());

    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setContentType(Constants.MEDIATYPE_ENTRY);
    response.setHeader("Content-Location", location);
    response.setHeader("Location", location);

    // write XML
    AtomEntry entry = new AtomEntry();
    entry.startDocument(response.getOutputStream());
    writeObjectEntry(entry, object, objectInfoHolder, null, repositoryId, null, null, baseUrl, true);
    entry.endDocument();
  }

  /**
   * Get all versions.
   */
  public static void getAllVersions(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisVersioningService service = factory.getVersioningService();

    // get parameters
    String versionSeriesId = getStringParameter(request, Constants.PARAM_ID);
    String filter = getStringParameter(request, Constants.PARAM_FILTER);
    Boolean includeAllowableActions = getBooleanParameter(request,
        Constants.PARAM_ALLOWABLE_ACTIONS);

    // execute
    ObjectInfoHolder objectInfoHolder = new ObjectInfoHolderImpl();
    List<ObjectData> versions = service.getAllVersions(context, repositoryId, versionSeriesId,
        filter, includeAllowableActions, null, objectInfoHolder);

    if (versions == null) {
      throw new CmisRuntimeException("Versions are null!");
    }

    ObjectInfo objectInfo = objectInfoHolder.getObjectInfo(versionSeriesId);
    if (objectInfo == null) {
      throw new CmisRuntimeException("Version Series Info is missing!");
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

    feed.writeSelfLink(compileUrl(baseUrl, RESOURCE_VERSIONS, objectInfo.getId()), null);

    feed.writeViaLink(compileUrl(baseUrl, RESOURCE_ENTRY, versionSeriesId));

    // write entries
    AtomEntry entry = new AtomEntry(feed.getWriter());
    for (ObjectData object : versions) {
      if (object == null) {
        continue;
      }
      writeObjectEntry(entry, object, objectInfoHolder, null, repositoryId, null, null, baseUrl,
          false);
    }

    // we are done
    feed.endFeed();
    feed.endDocument();
  }

  /**
   * Delete object.
   */
  public static void deleteAllVersions(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisObjectService service = factory.getObjectService();

    // get parameters
    String objectId = getStringParameter(request, Constants.PARAM_ID);

    // execute
    service.deleteObjectOrCancelCheckOut(context, repositoryId, objectId, Boolean.TRUE, null);

    // set headers
    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}
