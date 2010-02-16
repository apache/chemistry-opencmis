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
package org.apache.opencmis.server.spi;

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

/**
 * CMIS Object Service interface.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public interface CmisObjectService {

  /**
   * createDocument.
   * 
   * <p>
   * Bindings: Web Services
   * </p>
   */
  String createDocument(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, ContentStreamData contentStream, VersioningState versioningState,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension);

  /**
   * createDocumentFromSource.
   * 
   * <p>
   * Bindings: Web Services
   * </p>
   */
  String createDocumentFromSource(CallContext context, String repositoryId, String sourceId,
      PropertiesData properties, String folderId, VersioningState versioningState,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension);

  /**
   * createFolder.
   * 
   * <p>
   * Bindings: Web Services
   * </p>
   */
  String createFolder(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension);

  /**
   * createRelationship.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  String createRelationship(CallContext context, String repositoryId, PropertiesData properties,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension);

  /**
   * createPolicy.
   * 
   * <p>
   * Bindings: Web Services
   * </p>
   */
  String createPolicy(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension);

  /**
   * create.
   * 
   * <p>
   * Bindings: AtomPub
   * </p>
   */
  ObjectData create(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, ContentStreamData contentStream, VersioningState versioningState,
      List<String> policies, ExtensionsData extension, ObjectInfoHolder objectInfos);

  AllowableActionsData getAllowableActions(CallContext context, String repositoryId,
      String objectId, ExtensionsData extension);

  /**
   * getObject.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  ObjectData getObject(CallContext context, String repositoryId, String objectId, String filter,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl,
      ExtensionsData extension, ObjectInfoHolder objectInfos);

  /**
   * getProperties.
   * 
   * <p>
   * Bindings: Web Services
   * </p>
   */
  PropertiesData getProperties(CallContext context, String repositoryId, String objectId,
      String filter, ExtensionsData extension);

  List<RenditionData> getRenditions(CallContext context, String repositoryId, String objectId,
      String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension);

  ObjectData getObjectByPath(CallContext context, String repositoryId, String path, String filter,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl,
      ExtensionsData extension, ObjectInfoHolder objectInfos);

  ContentStreamData getContentStream(CallContext context, String repositoryId, String objectId,
      String streamId, BigInteger offset, BigInteger length, ExtensionsData extension);

  ObjectData updateProperties(CallContext context, String repositoryId, Holder<String> objectId,
      Holder<String> changeToken, PropertiesData properties, AccessControlList acl,
      ExtensionsData extension, ObjectInfoHolder objectInfos);

  ObjectData moveObject(CallContext context, String repositoryId, Holder<String> objectId,
      String targetFolderId, String sourceFolderId, ExtensionsData extension,
      ObjectInfoHolder objectInfos);

  void deleteObjectOrCancelCheckOut(CallContext context, String repositoryId, String objectId,
      Boolean allVersions, ExtensionsData extension);

  FailedToDeleteData deleteTree(CallContext context, String repositoryId, String folderId,
      Boolean allVersions, UnfileObjects unfileObjects, Boolean continueOnFailure,
      ExtensionsData extension);

  void setContentStream(CallContext context, String repositoryId, Holder<String> objectId,
      Boolean overwriteFlag, Holder<String> changeToken, ContentStreamData contentStream,
      ExtensionsData extension);

  void deleteContentStream(CallContext context, String repositoryId, Holder<String> objectId,
      Holder<String> changeToken, ExtensionsData extension);
}
