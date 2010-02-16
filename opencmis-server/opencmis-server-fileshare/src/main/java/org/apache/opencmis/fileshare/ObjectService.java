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
package org.apache.opencmis.fileshare;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
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
 * Object Service.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class ObjectService implements CmisObjectService {

  private RepositoryMap fRepositoryMap;

  /**
   * Constructor.
   */
  public ObjectService(RepositoryMap repositoryMap) {
    fRepositoryMap = repositoryMap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.server.spi.CmisObjectService#create(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, org.apache.opencmis.commons.provider.PropertiesData, java.lang.String,
   * org.apache.opencmis.commons.provider.ContentStreamData, org.apache.opencmis.commons.enums.VersioningState,
   * java.util.List, org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData create(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, ContentStreamData contentStream, VersioningState versioningState,
      List<String> policies, ExtensionsData extension, ObjectInfoHolder objectInfos) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).create(context,
        properties, folderId, contentStream, versioningState, objectInfos);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#createDocument(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, org.apache.opencmis.commons.provider.PropertiesData, java.lang.String,
   * org.apache.opencmis.commons.provider.ContentStreamData, org.apache.opencmis.commons.enums.VersioningState,
   * java.util.List, org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.provider.AccessControlList, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public String createDocument(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, ContentStreamData contentStream, VersioningState versioningState,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).createDocument(context,
        properties, folderId, contentStream, versioningState);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#createDocumentFromSource(org.apache.opencmis.server.spi.
   * CallContext, java.lang.String, java.lang.String, org.apache.opencmis.commons.provider.PropertiesData,
   * java.lang.String, org.apache.opencmis.commons.enums.VersioningState, java.util.List,
   * org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.provider.AccessControlList, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public String createDocumentFromSource(CallContext context, String repositoryId, String sourceId,
      PropertiesData properties, String folderId, VersioningState versioningState,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId)
        .createDocumentFromSource(context, sourceId, properties, folderId, versioningState);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#createFolder(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, org.apache.opencmis.commons.provider.PropertiesData, java.lang.String,
   * java.util.List, org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.provider.AccessControlList, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public String createFolder(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).createFolder(context,
        properties, folderId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#createPolicy(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, org.apache.opencmis.commons.provider.PropertiesData, java.lang.String,
   * java.util.List, org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.provider.AccessControlList, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public String createPolicy(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension) {
    fRepositoryMap.getAuthenticatedRepository(context, repositoryId);
    throw new CmisNotSupportedException("createPolicy not supported!");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#createRelationship(org.apache.opencmis.server.spi.CallContext
   * , java.lang.String, org.apache.opencmis.commons.provider.PropertiesData, java.util.List,
   * org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.provider.AccessControlList, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public String createRelationship(CallContext context, String repositoryId,
      PropertiesData properties, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension) {
    fRepositoryMap.getAuthenticatedRepository(context, repositoryId);
    throw new CmisNotSupportedException("createRelationship not supported!");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#deleteContentStream(org.apache.opencmis.server.spi.CallContext
   * , java.lang.String, org.apache.opencmis.commons.provider.Holder, org.apache.opencmis.commons.provider.Holder,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public void deleteContentStream(CallContext context, String repositoryId,
      Holder<String> objectId, Holder<String> changeToken, ExtensionsData extension) {
    fRepositoryMap.getAuthenticatedRepository(context, repositoryId).setContentStream(context,
        objectId, true, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#deleteObjectOrCancelCheckOut(org.apache.opencmis.server.
   * spi.CallContext, java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public void deleteObjectOrCancelCheckOut(CallContext context, String repositoryId,
      String objectId, Boolean allVersions, ExtensionsData extension) {
    fRepositoryMap.getAuthenticatedRepository(context, repositoryId)
        .deleteObject(context, objectId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.server.spi.CmisObjectService#deleteTree(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.enums.UnfileObjects, java.lang.Boolean,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public FailedToDeleteData deleteTree(CallContext context, String repositoryId, String folderId,
      Boolean allVersions, UnfileObjects unfileObjects, Boolean continueOnFailure,
      ExtensionsData extension) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).deleteTree(context,
        folderId, continueOnFailure);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#getAllowableActions(org.apache.opencmis.server.spi.CallContext
   * , java.lang.String, java.lang.String, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public AllowableActionsData getAllowableActions(CallContext context, String repositoryId,
      String objectId, ExtensionsData extension) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).getAllowableActions(
        context, objectId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#getContentStream(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, java.lang.String, java.lang.String, java.math.BigInteger,
   * java.math.BigInteger, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public ContentStreamData getContentStream(CallContext context, String repositoryId,
      String objectId, String streamId, BigInteger offset, BigInteger length,
      ExtensionsData extension) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).getContentStream(
        context, objectId, offset, length);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.server.spi.CmisObjectService#getObject(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean,
   * java.lang.Boolean, org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData getObject(CallContext context, String repositoryId, String objectId,
      String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl,
      ExtensionsData extension, ObjectInfoHolder objectInfos) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).getObject(context,
        objectId, filter, includeAllowableActions, includeAcl, objectInfos);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#getObjectByPath(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean,
   * java.lang.Boolean, org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData getObjectByPath(CallContext context, String repositoryId, String path,
      String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl,
      ExtensionsData extension, ObjectInfoHolder objectInfos) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).getObjectByPath(
        context, path, filter, includeAllowableActions, includeAcl, objectInfos);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#getProperties(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, java.lang.String, java.lang.String, org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public PropertiesData getProperties(CallContext context, String repositoryId, String objectId,
      String filter, ExtensionsData extension) {
    ObjectData object = fRepositoryMap.getAuthenticatedRepository(context, repositoryId).getObject(
        context, objectId, filter, false, false, null);
    return object.getProperties();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#getRenditions(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, java.lang.String, java.lang.String, java.math.BigInteger,
   * java.math.BigInteger, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public List<RenditionData> getRenditions(CallContext context, String repositoryId,
      String objectId, String renditionFilter, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension) {
    fRepositoryMap.getAuthenticatedRepository(context, repositoryId);
    return Collections.emptyList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.server.spi.CmisObjectService#moveObject(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, org.apache.opencmis.commons.provider.Holder, java.lang.String, java.lang.String,
   * org.apache.opencmis.commons.api.ExtensionsData, org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData moveObject(CallContext context, String repositoryId, Holder<String> objectId,
      String targetFolderId, String sourceFolderId, ExtensionsData extension,
      ObjectInfoHolder objectInfos) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).moveObject(context,
        objectId, targetFolderId, objectInfos);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#setContentStream(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, org.apache.opencmis.commons.provider.Holder, java.lang.Boolean,
   * org.apache.opencmis.commons.provider.Holder, org.apache.opencmis.commons.provider.ContentStreamData,
   * org.apache.opencmis.commons.api.ExtensionsData)
   */
  public void setContentStream(CallContext context, String repositoryId, Holder<String> objectId,
      Boolean overwriteFlag, Holder<String> changeToken, ContentStreamData contentStream,
      ExtensionsData extension) {
    fRepositoryMap.getAuthenticatedRepository(context, repositoryId).setContentStream(context,
        objectId, overwriteFlag, contentStream);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisObjectService#updateProperties(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, org.apache.opencmis.commons.provider.Holder, org.apache.opencmis.commons.provider.Holder,
   * org.apache.opencmis.commons.provider.PropertiesData, org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.api.ExtensionsData, org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData updateProperties(CallContext context, String repositoryId,
      Holder<String> objectId, Holder<String> changeToken, PropertiesData properties,
      AccessControlList acl, ExtensionsData extension, ObjectInfoHolder objectInfos) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).updateProperties(
        context, objectId, properties, objectInfos);
  }

}
