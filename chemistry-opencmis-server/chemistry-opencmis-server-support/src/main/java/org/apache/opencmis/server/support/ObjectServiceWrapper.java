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
package org.apache.opencmis.server.support;

import java.math.BigInteger;
import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.AllowableActionsData;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.FailedToDeleteData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.RenditionData;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisObjectService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

/**
 * Object service wrapper.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class ObjectServiceWrapper extends AbstractServiceWrapper implements CmisObjectService {

  private CmisObjectService fService;

  /**
   * Constructor.
   * 
   * @param service
   *          the real service object
   * @param defaultMaxItems
   *          default value for <code>maxItems</code> parameters
   */
  public ObjectServiceWrapper(CmisObjectService service, BigInteger defaultMaxItems) {
    if (service == null) {
      throw new IllegalArgumentException("Service must be set!");
    }

    fService = service;
    setDefaultMaxItems(defaultMaxItems);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#create(org.apache.opencmis.server.spi.CallContext
   * , java.lang.String, org.apache.opencmis.commons.provider.PropertiesData, java.lang.String,
   * org.apache.opencmis.commons.provider.ContentStreamData,
   * org.apache.opencmis.commons.enums.VersioningState, java.util.List,
   * org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData create(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, ContentStreamData contentStream, VersioningState versioningState,
      List<String> policies, ExtensionsData extension, ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    checkProperties(properties);
    versioningState = getDefault(versioningState);

    try {
      return fService.create(context, repositoryId, properties, folderId, contentStream,
          versioningState, policies, extension, objectInfos);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#createDocument(org.apache.opencmis.server.
   * spi.CallContext, java.lang.String, org.apache.opencmis.commons.provider.PropertiesData,
   * java.lang.String, org.apache.opencmis.commons.provider.ContentStreamData,
   * org.apache.opencmis.commons.enums.VersioningState, java.util.List,
   * org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public String createDocument(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, ContentStreamData contentStream, VersioningState versioningState,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkProperties(properties);
    versioningState = getDefault(versioningState);

    try {
      return fService.createDocument(context, repositoryId, properties, folderId, contentStream,
          versioningState, policies, addAces, removeAces, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#createDocumentFromSource(org.apache.opencmis
   * .server.spi.CallContext, java.lang.String, java.lang.String,
   * org.apache.opencmis.commons.provider.PropertiesData, java.lang.String,
   * org.apache.opencmis.commons.enums.VersioningState, java.util.List,
   * org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public String createDocumentFromSource(CallContext context, String repositoryId, String sourceId,
      PropertiesData properties, String folderId, VersioningState versioningState,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkId("Source Id", sourceId);
    versioningState = getDefault(versioningState);

    try {
      return fService.createDocumentFromSource(context, repositoryId, sourceId, properties,
          folderId, versioningState, policies, addAces, removeAces, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#createFolder(org.apache.opencmis.server.spi
   * .CallContext, java.lang.String, org.apache.opencmis.commons.provider.PropertiesData,
   * java.lang.String, java.util.List, org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public String createFolder(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkProperties(properties);
    checkId("Folder Id", folderId);

    try {
      return fService.createFolder(context, repositoryId, properties, folderId, policies, addAces,
          removeAces, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#createPolicy(org.apache.opencmis.server.spi
   * .CallContext, java.lang.String, org.apache.opencmis.commons.provider.PropertiesData,
   * java.lang.String, java.util.List, org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public String createPolicy(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkProperties(properties);

    try {
      return fService.createPolicy(context, repositoryId, properties, folderId, policies, addAces,
          removeAces, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#createRelationship(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, org.apache.opencmis.commons.provider.PropertiesData,
   * java.util.List, org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public String createRelationship(CallContext context, String repositoryId,
      PropertiesData properties, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkProperties(properties);

    try {
      return fService.createRelationship(context, repositoryId, properties, policies, addAces,
          removeAces, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#deleteContentStream(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, org.apache.opencmis.commons.provider.Holder,
   * org.apache.opencmis.commons.provider.Holder, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public void deleteContentStream(CallContext context, String repositoryId,
      Holder<String> objectId, Holder<String> changeToken, ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkHolderId("Object Id", objectId);

    try {
      fService.deleteContentStream(context, repositoryId, objectId, changeToken, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#deleteObjectOrCancelCheckOut(org.apache.opencmis
   * .server.spi.CallContext, java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public void deleteObjectOrCancelCheckOut(CallContext context, String repositoryId,
      String objectId, Boolean allVersions, ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkId("Object Id", objectId);
    allVersions = getDefaultTrue(allVersions);

    try {
      fService
          .deleteObjectOrCancelCheckOut(context, repositoryId, objectId, allVersions, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#deleteTree(org.apache.opencmis.server.spi.
   * CallContext, java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.enums.UnfileObjects, java.lang.Boolean,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public FailedToDeleteData deleteTree(CallContext context, String repositoryId, String folderId,
      Boolean allVersions, UnfileObjects unfileObjects, Boolean continueOnFailure,
      ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkId("Folder Id", folderId);
    allVersions = getDefaultTrue(allVersions);
    unfileObjects = getDefault(unfileObjects);
    continueOnFailure = getDefaultFalse(continueOnFailure);

    try {
      return fService.deleteTree(context, repositoryId, folderId, allVersions, unfileObjects,
          continueOnFailure, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#getAllowableActions(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, java.lang.String,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public AllowableActionsData getAllowableActions(CallContext context, String repositoryId,
      String objectId, ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkId("Object Id", objectId);

    try {
      return fService.getAllowableActions(context, repositoryId, objectId, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#getContentStream(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, java.lang.String, java.lang.String, java.math.BigInteger,
   * java.math.BigInteger, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public ContentStreamData getContentStream(CallContext context, String repositoryId,
      String objectId, String streamId, BigInteger offset, BigInteger length,
      ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkId("Object Id", objectId);
    checkNullOrPositive("Offset", offset);
    checkNullOrPositive("Length", length);

    try {
      return fService.getContentStream(context, repositoryId, objectId, streamId, offset, length,
          extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.apache.opencmis.server.spi.CmisObjectService#getObject(org.apache.opencmis.server.spi.
   * CallContext, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean,
   * java.lang.Boolean, org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData getObject(CallContext context, String repositoryId, String objectId,
      String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl,
      ExtensionsData extension, ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    checkId("Object Id", objectId);
    includeAllowableActions = getDefaultFalse(includeAllowableActions);
    includeRelationships = getDefault(includeRelationships);
    renditionFilter = getDefaultRenditionFilter(renditionFilter);
    includePolicyIds = getDefaultFalse(includePolicyIds);
    includeAcl = getDefaultFalse(includeAcl);

    try {
      return fService.getObject(context, repositoryId, objectId, filter, includeAllowableActions,
          includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension,
          objectInfos);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#getObjectByPath(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean,
   * java.lang.Boolean, org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData getObjectByPath(CallContext context, String repositoryId, String path,
      String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl,
      ExtensionsData extension, ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    checkPath("Path", path);
    includeAllowableActions = getDefaultFalse(includeAllowableActions);
    includeRelationships = getDefault(includeRelationships);
    renditionFilter = getDefaultRenditionFilter(renditionFilter);
    includePolicyIds = getDefaultFalse(includePolicyIds);
    includeAcl = getDefaultFalse(includeAcl);

    try {
      return fService.getObjectByPath(context, repositoryId, path, filter, includeAllowableActions,
          includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension,
          objectInfos);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#getProperties(org.apache.opencmis.server.spi
   * .CallContext, java.lang.String, java.lang.String, java.lang.String,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public PropertiesData getProperties(CallContext context, String repositoryId, String objectId,
      String filter, ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkId("Object Id", objectId);

    try {
      return fService.getProperties(context, repositoryId, objectId, filter, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#getRenditions(org.apache.opencmis.server.spi
   * .CallContext, java.lang.String, java.lang.String, java.lang.String, java.math.BigInteger,
   * java.math.BigInteger, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public List<RenditionData> getRenditions(CallContext context, String repositoryId,
      String objectId, String renditionFilter, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkId("Object Id", objectId);
    renditionFilter = getDefaultRenditionFilter(renditionFilter);
    maxItems = getMaxItems(maxItems);
    skipCount = getSkipCount(skipCount);

    try {
      return fService.getRenditions(context, repositoryId, objectId, renditionFilter, maxItems,
          skipCount, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#moveObject(org.apache.opencmis.server.spi.
   * CallContext, java.lang.String, org.apache.opencmis.commons.provider.Holder, java.lang.String,
   * java.lang.String, org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData moveObject(CallContext context, String repositoryId, Holder<String> objectId,
      String targetFolderId, String sourceFolderId, ExtensionsData extension,
      ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    checkHolderId("Object Id", objectId);
    checkId("Target Folder Id", targetFolderId);

    try {
      return fService.moveObject(context, repositoryId, objectId, targetFolderId, sourceFolderId,
          extension, objectInfos);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#setContentStream(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, org.apache.opencmis.commons.provider.Holder,
   * java.lang.Boolean, org.apache.opencmis.commons.provider.Holder,
   * org.apache.opencmis.commons.provider.ContentStreamData,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public void setContentStream(CallContext context, String repositoryId, Holder<String> objectId,
      Boolean overwriteFlag, Holder<String> changeToken, ContentStreamData contentStream,
      ExtensionsData extension) {
    checkRepositoryId(repositoryId);
    checkHolderId("Object Id", objectId);
    overwriteFlag = getDefaultTrue(overwriteFlag);
    checkContentStream(contentStream);

    try {
      fService.setContentStream(context, repositoryId, objectId, overwriteFlag, changeToken,
          contentStream, extension);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#updateProperties(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, org.apache.opencmis.commons.provider.Holder,
   * org.apache.opencmis.commons.provider.Holder,
   * org.apache.opencmis.commons.provider.PropertiesData,
   * org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData updateProperties(CallContext context, String repositoryId,
      Holder<String> objectId, Holder<String> changeToken, PropertiesData properties,
      AccessControlList acl, ExtensionsData extension, ObjectInfoHolder objectInfos) {
    checkRepositoryId(repositoryId);
    checkHolderId("Object Id", objectId);
    checkProperties(properties);

    try {
      return fService.updateProperties(context, repositoryId, objectId, changeToken, properties,
          acl, extension, objectInfos);
    }
    catch (Exception e) {
      throw createCmisException(e);
    }
  }

}
