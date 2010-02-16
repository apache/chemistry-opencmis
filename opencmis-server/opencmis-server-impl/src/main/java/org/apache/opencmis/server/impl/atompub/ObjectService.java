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

import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CONTENT;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_ENTRY;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.compileBaseUrl;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.compileUrl;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getBooleanParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getEnumParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.getStringParameter;
import static org.apache.opencmis.server.impl.atompub.AtomPubUtils.writeObjectEntry;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.ReturnVersion;
import org.apache.opencmis.commons.impl.UrlBuilder;
import org.apache.opencmis.commons.impl.dataobjects.ContentStreamDataImpl;
import org.apache.opencmis.commons.provider.AllowableActionsData;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.FailedToDeleteData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.PropertyStringData;
import org.apache.opencmis.server.impl.ObjectInfoHolderImpl;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisMultiFilingService;
import org.apache.opencmis.server.spi.CmisObjectService;
import org.apache.opencmis.server.spi.CmisVersioningService;
import org.apache.opencmis.server.spi.ObjectInfo;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

/**
 * Object Service operations.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public final class ObjectService {

  private static final int BUFFER_SIZE = 64 * 1024;

  /**
   * Create*.
   */
  public static void create(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {

    // get parameters
    String folderId = getStringParameter(request, Constants.PARAM_ID);
    String sourceFolderId = getStringParameter(request, Constants.PARAM_SOURCE_FOLDER_ID);
    VersioningState versioningState = getEnumParameter(request, Constants.PARAM_VERSIONIG_STATE,
        VersioningState.class);

    AtomEntryParser parser = new AtomEntryParser(request.getInputStream());
    String objectId = parser.getId();

    // execute
    ObjectInfoHolder objectInfoHolder = new ObjectInfoHolderImpl();
    ObjectData object = null;

    if (objectId == null) {
      // create
      CmisObjectService service = factory.getObjectService();
      object = service.create(context, repositoryId, parser.getProperties(), folderId, parser
          .getContentStream(), versioningState, parser.getPolicyIds(), null, objectInfoHolder);
    }
    else {
      if ((sourceFolderId == null) || (sourceFolderId.trim().length() == 0)) {
        // addObjectToFolder
        CmisMultiFilingService service = factory.getMultiFilingService();
        object = service.addObjectToFolder(context, repositoryId, objectId, sourceFolderId, null,
            null, objectInfoHolder);
      }
      else {
        // move
        CmisObjectService service = factory.getObjectService();
        object = service.moveObject(context, repositoryId, new Holder<String>(objectId), folderId,
            sourceFolderId, null, objectInfoHolder);
      }
    }

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

  /**
   * Create relationship.
   */
  public static void createRelationship(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisObjectService service = factory.getObjectService();

    // get parameters
    AtomEntryParser parser = new AtomEntryParser(request.getInputStream());

    // execute
    ObjectInfoHolder objectInfoHolder = new ObjectInfoHolderImpl();
    ObjectData object = service.create(context, repositoryId, parser.getProperties(), null, null,
        null, parser.getPolicyIds(), null, objectInfoHolder);

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

  /**
   * Delete object.
   */
  public static void deleteObject(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisObjectService service = factory.getObjectService();

    // get parameters
    String objectId = getStringParameter(request, Constants.PARAM_ID);
    Boolean allVersions = getBooleanParameter(request, Constants.PARAM_ALL_VERSIONS);

    // execute
    service.deleteObjectOrCancelCheckOut(context, repositoryId, objectId, allVersions, null);

    // set headers
    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  /**
   * Delete content stream.
   */
  public static void deleteContentStream(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisObjectService service = factory.getObjectService();

    // get parameters
    String objectId = getStringParameter(request, Constants.PARAM_ID);
    String changeToken = getStringParameter(request, Constants.PARAM_CHANGE_TOKEN);

    // execute
    service.deleteContentStream(context, repositoryId, new Holder<String>(objectId),
        changeToken == null ? null : new Holder<String>(changeToken), null);

    // set headers
    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  /**
   * Set content stream.
   */
  public static void setContentStream(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisObjectService service = factory.getObjectService();

    // get parameters
    String objectId = getStringParameter(request, Constants.PARAM_ID);
    String changeToken = getStringParameter(request, Constants.PARAM_CHANGE_TOKEN);
    Boolean overwriteFlag = getBooleanParameter(request, Constants.PARAM_OVERWRITE_FLAG);

    ContentStreamDataImpl contentStream = new ContentStreamDataImpl();
    contentStream.setStream(request.getInputStream());
    contentStream.setMimeType(request.getHeader("Content-Type"));
    String lengthStr = request.getHeader("Content-Length");
    if (lengthStr != null) {
      try {
        contentStream.setLength(new BigInteger(lengthStr));
      }
      catch (NumberFormatException e) {
      }
    }

    // execute
    Holder<String> objectIdHolder = new Holder<String>(objectId);
    service.setContentStream(context, repositoryId, objectIdHolder, overwriteFlag,
        changeToken == null ? null : new Holder<String>(changeToken), contentStream, null);

    // set headers
    String newObjectId = (objectIdHolder.getValue() == null ? objectId : objectIdHolder.getValue());
    String location = compileUrl(compileBaseUrl(request, repositoryId), RESOURCE_CONTENT,
        newObjectId);

    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setHeader("Content-Location", location);
    response.setHeader("Location", location);
  }

  /**
   * Delete tree.
   */
  public static void deleteTree(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisObjectService service = factory.getObjectService();

    // get parameters
    String folderId = getStringParameter(request, Constants.PARAM_ID);
    Boolean allVersions = getBooleanParameter(request, Constants.PARAM_ALL_VERSIONS);
    UnfileObjects unfileObjects = getEnumParameter(request, Constants.PARAM_UNFILE_OBJECTS,
        UnfileObjects.class);
    Boolean continueOnFailure = getBooleanParameter(request, Constants.PARAM_CONTINUE_ON_FAILURE);

    // execute
    FailedToDeleteData ftd = service.deleteTree(context, repositoryId, folderId, allVersions,
        unfileObjects, continueOnFailure, null);

    if ((ftd != null) && (ftd.getIds() != null) && (ftd.getIds().size() > 0)) {
      // print ids that could not be deleted
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.setContentType("text/plain");

      PrintWriter pw = response.getWriter();

      pw.println("Failed to delete the following objects:");
      for (String id : ftd.getIds()) {
        pw.println(id);
      }

      pw.flush();

      return;
    }

    // set headers
    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  /**
   * getObject.
   */
  public static void getObject(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {

    // get parameters
    String objectId = getStringParameter(request, Constants.PARAM_ID);
    ReturnVersion returnVersion = getEnumParameter(request, Constants.PARAM_RETURN_VERSION,
        ReturnVersion.class);
    String filter = getStringParameter(request, Constants.PARAM_FILTER);
    Boolean includeAllowableActions = getBooleanParameter(request,
        Constants.PARAM_ALLOWABLE_ACTIONS);
    IncludeRelationships includeRelationships = getEnumParameter(request,
        Constants.PARAM_RELATIONSHIPS, IncludeRelationships.class);
    String renditionFilter = getStringParameter(request, Constants.PARAM_RENDITION_FILTER);
    Boolean includePolicyIds = getBooleanParameter(request, Constants.PARAM_POLICY_IDS);
    Boolean includeAcl = getBooleanParameter(request, Constants.PARAM_ACL);

    // execute
    ObjectInfoHolder objectInfoHolder = new ObjectInfoHolderImpl();
    ObjectData object = null;

    if ((returnVersion == ReturnVersion.LATEST) || (returnVersion == ReturnVersion.LASTESTMAJOR)) {
      CmisVersioningService service = factory.getVersioningService();
      object = service.getObjectOfLatestVersion(context, repositoryId, objectId,
          returnVersion == ReturnVersion.LASTESTMAJOR, filter, includeAllowableActions,
          includeRelationships, renditionFilter, includePolicyIds, includeAcl, null,
          objectInfoHolder);
    }
    else {
      CmisObjectService service = factory.getObjectService();
      object = service.getObject(context, repositoryId, objectId, filter, includeAllowableActions,
          includeRelationships, renditionFilter, includePolicyIds, includeAcl, null,
          objectInfoHolder);
    }

    if (object == null) {
      throw new CmisRuntimeException("Object is null!");
    }

    ObjectInfo objectInfo = objectInfoHolder.getObjectInfo(object.getId());
    if (objectInfo == null) {
      throw new CmisRuntimeException("Object Info is missing!");
    }

    // set headers
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(Constants.MEDIATYPE_ENTRY);

    // write XML
    UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

    AtomEntry entry = new AtomEntry();
    entry.startDocument(response.getOutputStream());
    writeObjectEntry(entry, object, objectInfoHolder, null, repositoryId, null, null, baseUrl, true);
    entry.endDocument();
  }

  /**
   * objectByPath URI template.
   */
  public static void getObjectByPath(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisObjectService service = factory.getObjectService();

    // get parameters
    String path = getStringParameter(request, Constants.PARAM_PATH);
    String filter = getStringParameter(request, Constants.PARAM_FILTER);
    Boolean includeAllowableActions = getBooleanParameter(request,
        Constants.PARAM_ALLOWABLE_ACTIONS);
    IncludeRelationships includeRelationships = getEnumParameter(request,
        Constants.PARAM_RELATIONSHIPS, IncludeRelationships.class);
    String renditionFilter = getStringParameter(request, Constants.PARAM_RENDITION_FILTER);
    Boolean includePolicyIds = getBooleanParameter(request, Constants.PARAM_POLICY_IDS);
    Boolean includeAcl = getBooleanParameter(request, Constants.PARAM_ACL);

    // execute
    ObjectInfoHolder objectInfoHolder = new ObjectInfoHolderImpl();
    ObjectData object = service.getObjectByPath(context, repositoryId, path, filter,
        includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds,
        includeAcl, null, objectInfoHolder);

    if (object == null) {
      throw new CmisRuntimeException("Object is null!");
    }

    ObjectInfo objectInfo = objectInfoHolder.getObjectInfo(object.getId());
    if (objectInfo == null) {
      throw new CmisRuntimeException("Object Info is missing!");
    }

    // set headers
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(Constants.MEDIATYPE_ENTRY);

    // write XML
    UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

    AtomEntry entry = new AtomEntry();
    entry.startDocument(response.getOutputStream());
    writeObjectEntry(entry, object, objectInfoHolder, null, repositoryId, null, null, baseUrl, true);
    entry.endDocument();
  }

  /**
   * Allowable Actions.
   */
  public static void getAllowableActions(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisObjectService service = factory.getObjectService();

    // get parameters
    String objectId = getStringParameter(request, Constants.PARAM_ID);

    // execute
    AllowableActionsData allowableActions = service.getAllowableActions(context, repositoryId,
        objectId, null);

    if (allowableActions == null) {
      throw new CmisRuntimeException("Allowable Actions is null!");
    }

    // set headers
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(Constants.MEDIATYPE_ALLOWABLEACTION);

    // write XML
    AllowableActionsDocument allowableActionsDocument = new AllowableActionsDocument();
    allowableActionsDocument.writeAllowableActions(allowableActions, response.getOutputStream());
  }

  /**
   * getContentStream.
   */
  public static void getContentStream(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    CmisObjectService service = factory.getObjectService();

    // get parameters
    String objectId = getStringParameter(request, Constants.PARAM_ID);
    String streamId = getStringParameter(request, Constants.PARAM_STREAM_ID);

    BigInteger offset = null;
    String offsetStr = context.get(CallContext.OFFSET);
    if (offsetStr != null) {
      offset = new BigInteger(offsetStr);
    }

    BigInteger length = null;
    String lengthStr = context.get(CallContext.LENGTH);
    if (lengthStr != null) {
      length = new BigInteger(offsetStr);
    }

    // execute
    ContentStreamData content = service.getContentStream(context, repositoryId, objectId, streamId,
        offset, length, null);

    if ((content == null) || (content.getStream() == null)) {
      throw new CmisRuntimeException("Content stream is null!");
    }

    String contentType = content.getMimeType();
    if (contentType == null) {
      contentType = Constants.MEDIATYPE_OCTETSTREAM;
    }

    // set headers
    if ((offset == null) && (length == null)) {
      response.setStatus(HttpServletResponse.SC_OK);
    }
    else {
      response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
    }
    response.setContentType(contentType);

    // send content
    InputStream in = new BufferedInputStream(content.getStream(), BUFFER_SIZE);
    OutputStream out = new BufferedOutputStream(response.getOutputStream());

    byte[] buffer = new byte[BUFFER_SIZE];
    int b;
    while ((b = in.read(buffer)) > -1) {
      out.write(buffer, 0, b);
    }

    in.close();
    out.flush();
  }

  /**
   * UpdateProperties.
   */
  public static void updateProperties(CallContext context, AbstractServicesFactory factory,
      String repositoryId, HttpServletRequest request, HttpServletResponse response)
      throws Exception {

    // get parameters
    String objectId = getStringParameter(request, Constants.PARAM_ID);
    Boolean checkin = getBooleanParameter(request, Constants.PARAM_CHECK_IN);
    String checkinComment = getStringParameter(request, Constants.PARAM_CHECKIN_COMMENT);
    Boolean major = getBooleanParameter(request, Constants.PARAM_MAJOR);

    AtomEntryParser parser = new AtomEntryParser(request.getInputStream());

    // execute
    ObjectInfoHolder objectInfoHolder = new ObjectInfoHolderImpl();
    ObjectData object = null;

    if ((checkin != null) && (checkin.booleanValue())) {
      CmisVersioningService service = factory.getVersioningService();
      object = service.checkIn(context, repositoryId, new Holder<String>(objectId), major, parser
          .getProperties(), parser.getContentStream(), checkinComment, parser.getPolicyIds(), null,
          null, null, objectInfoHolder);
    }
    else {
      String changeToken = extractChangeToken(parser.getProperties());

      CmisObjectService service = factory.getObjectService();
      object = service.updateProperties(context, repositoryId, new Holder<String>(objectId),
          changeToken == null ? null : new Holder<String>(changeToken), parser.getProperties(),
          parser.getAcl(), null, objectInfoHolder);
    }

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
   * Gets the change token from a property set.
   */
  private static String extractChangeToken(PropertiesData properties) {
    if (properties == null) {
      return null;
    }

    Map<String, PropertyData<?>> propertiesMap = properties.getProperties();
    if (propertiesMap == null) {
      return null;
    }

    PropertyData<?> changeLogProperty = propertiesMap.get(PropertyIds.CMIS_CHANGE_TOKEN);
    if (!(changeLogProperty instanceof PropertyStringData)) {
      return null;
    }

    return ((PropertyStringData) changeLogProperty).getFirstValue();
  }
}
