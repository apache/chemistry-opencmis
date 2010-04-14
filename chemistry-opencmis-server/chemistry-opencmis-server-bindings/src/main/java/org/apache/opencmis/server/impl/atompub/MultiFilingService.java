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
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.compileBaseUrl;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.compileUrl;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getStringParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.writeObjectEntry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.UrlBuilder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.server.impl.ObjectInfoHolderImpl;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisMultiFilingService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

/**
 * MultiFiling Service operations.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class MultiFilingService {

  /**
   * Remove object from folder.
   */
  public static void removeObjectFromFolder(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisMultiFilingService service = factory.getMultiFilingService();

    // get parameters
    String removeFrom = getStringParameter(request, Constants.PARAM_REMOVE_FROM);

    AtomEntryParser parser = new AtomEntryParser(request.getInputStream());
    String objectId = parser.getId();

    // execute
    ObjectInfoHolder objectInfoHolder = new ObjectInfoHolderImpl();
    ObjectData object = service.removeObjectFromFolder(context, repositoryId, objectId, removeFrom,
        null, objectInfoHolder);

    if (object == null) {
      throw new CmisRuntimeException("Object is null!");
    }

    if (object.getId() == null) {
      throw new CmisRuntimeException("Object Id is null!");
    }

    // set headers
    UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setContentType(Constants.MEDIATYPE_ENTRY);
    response.setHeader("Location", compileUrl(baseUrl, RESOURCE_ENTRY, object.getId()));

    // write XML
    AtomEntry entry = new AtomEntry();
    entry.startDocument(response.getOutputStream());
    writeObjectEntry(entry, object, objectInfoHolder, null, repositoryId, null, null, baseUrl, true);
    entry.endDocument();
  }
}
