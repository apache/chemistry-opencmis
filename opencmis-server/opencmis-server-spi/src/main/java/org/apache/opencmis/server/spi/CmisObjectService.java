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
 * CMIS Object Service interface. Please refer to the CMIS specification and the OpenCMIS
 * documentation for details.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public interface CmisObjectService {

  /**
   * Creates new document.
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
   * Copies a document.
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
   * Creates a new folder.
   * 
   * <p>
   * Bindings: Web Services
   * </p>
   */
  String createFolder(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension);

  /**
   * Create a new relationship.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  String createRelationship(CallContext context, String repositoryId, PropertiesData properties,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension);

  /**
   * Creates a new policy.
   * 
   * <p>
   * Bindings: Web Services
   * </p>
   */
  String createPolicy(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension);

  /**
   * Creates a new document, folder or policy. The property "cmis:objectTypeId" defines the type and
   * implicitly the base type.
   * 
   * <p>
   * Bindings: AtomPub
   * </p>
   */
  ObjectData create(CallContext context, String repositoryId, PropertiesData properties,
      String folderId, ContentStreamData contentStream, VersioningState versioningState,
      List<String> policies, ExtensionsData extension, ObjectInfoHolder objectInfos);

  /**
   * Gets the allowable actions.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  AllowableActionsData getAllowableActions(CallContext context, String repositoryId,
      String objectId, ExtensionsData extension);

  /**
   * Gets an object by id.
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
   * Gets the properties of an object.
   * 
   * <p>
   * Bindings: Web Services
   * </p>
   */
  PropertiesData getProperties(CallContext context, String repositoryId, String objectId,
      String filter, ExtensionsData extension);

  /**
   * Gets the renditions of an object.
   * 
   * <p>
   * Bindings: Web Services
   * </p>
   */
  List<RenditionData> getRenditions(CallContext context, String repositoryId, String objectId,
      String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension);

  /**
   * Gets an object by path.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  ObjectData getObjectByPath(CallContext context, String repositoryId, String path, String filter,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl,
      ExtensionsData extension, ObjectInfoHolder objectInfos);

  /**
   * Gets the content of a document.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  ContentStreamData getContentStream(CallContext context, String repositoryId, String objectId,
      String streamId, BigInteger offset, BigInteger length, ExtensionsData extension);

  /**
   * Updates the properties of an object.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  ObjectData updateProperties(CallContext context, String repositoryId, Holder<String> objectId,
      Holder<String> changeToken, PropertiesData properties, AccessControlList acl,
      ExtensionsData extension, ObjectInfoHolder objectInfos);

  /**
   * Moves an object.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  ObjectData moveObject(CallContext context, String repositoryId, Holder<String> objectId,
      String targetFolderId, String sourceFolderId, ExtensionsData extension,
      ObjectInfoHolder objectInfos);

  /**
   * Deletes an object or cancels a check out. For the Web Services binding this is always an object
   * deletion. For the AtomPub it depends on the referenced object. If it is a checked out document
   * then the check out must be canceled. If the object is not a checked out document then the
   * object must be deleted.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  void deleteObjectOrCancelCheckOut(CallContext context, String repositoryId, String objectId,
      Boolean allVersions, ExtensionsData extension);

  /**
   * Deletes a folder tree.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  FailedToDeleteData deleteTree(CallContext context, String repositoryId, String folderId,
      Boolean allVersions, UnfileObjects unfileObjects, Boolean continueOnFailure,
      ExtensionsData extension);

  /**
   * Sets a new content.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  void setContentStream(CallContext context, String repositoryId, Holder<String> objectId,
      Boolean overwriteFlag, Holder<String> changeToken, ContentStreamData contentStream,
      ExtensionsData extension);

  /**
   * Deletes a content.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  void deleteContentStream(CallContext context, String repositoryId, Holder<String> objectId,
      Holder<String> changeToken, ExtensionsData extension);
}
