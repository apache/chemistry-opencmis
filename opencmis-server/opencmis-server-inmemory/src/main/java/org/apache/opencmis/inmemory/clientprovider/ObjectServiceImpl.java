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

package org.apache.opencmis.inmemory.clientprovider;

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
import org.apache.opencmis.commons.provider.ObjectService;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.RenditionData;
import org.apache.opencmis.inmemory.server.InMemoryObjectServiceImpl;

public class ObjectServiceImpl extends AbstractService implements ObjectService {

  private InMemoryObjectServiceImpl fObjSvc;

  public ObjectServiceImpl(InMemoryObjectServiceImpl objSvc) {
    fObjSvc = objSvc;
  }

  public String createDocument(String repositoryId, PropertiesData properties, String folderId,
      ContentStreamData contentStream, VersioningState versioningState, List<String> policies,
      AccessControlList addAces, AccessControlList removeAces, ExtensionsData extension) {
    
    return fObjSvc.createDocument(fDummyCallContext, repositoryId, properties, folderId, contentStream, versioningState, policies, addAces, removeAces, extension);
  }

  public String createDocumentFromSource(String repositoryId, String sourceId,
      PropertiesData properties, String folderId, VersioningState versioningState,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {
    
    return fObjSvc.createDocumentFromSource(fDummyCallContext, repositoryId, sourceId, properties, folderId, versioningState, policies, addAces, removeAces, extension);
  }

  public String createFolder(String repositoryId, PropertiesData properties, String folderId,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {
    
    return fObjSvc.createFolder(fDummyCallContext, repositoryId, properties, folderId, policies, addAces, removeAces, extension);
  }

  public String createPolicy(String repositoryId, PropertiesData properties, String folderId,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {
    
    return fObjSvc.createPolicy(fDummyCallContext, repositoryId, properties, folderId, policies, addAces, removeAces, extension);
  }

  public String createRelationship(String repositoryId, PropertiesData properties,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {
    
    return fObjSvc.createRelationship(fDummyCallContext, repositoryId, properties, policies, addAces, removeAces, extension);
  }

  public void deleteContentStream(String repositoryId, Holder<String> objectId,
      Holder<String> changeToken, ExtensionsData extension) {
    
    fObjSvc.deleteContentStream(fDummyCallContext, repositoryId, objectId, changeToken, extension);
  }

  public void deleteObject(String repositoryId, String objectId, Boolean allVersions,
      ExtensionsData extension) {
    
    fObjSvc.deleteObjectOrCancelCheckOut(fDummyCallContext, repositoryId, objectId, allVersions, extension);
  }

  public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
      UnfileObjects unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
    
    return fObjSvc.deleteTree(fDummyCallContext, repositoryId, folderId, allVersions, unfileObjects, continueOnFailure, extension);
  }

  public AllowableActionsData getAllowableActions(String repositoryId, String objectId,
      ExtensionsData extension) {
    
    return fObjSvc.getAllowableActions(fDummyCallContext, repositoryId, objectId, extension);
  }

  public ContentStreamData getContentStream(String repositoryId, String objectId, String streamId,
      BigInteger offset, BigInteger length, ExtensionsData extension) {
    
    return fObjSvc.getContentStream(fDummyCallContext, repositoryId, objectId, streamId, offset, length, extension);
  }

  public ObjectData getObject(String repositoryId, String objectId, String filter,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
    
    return fObjSvc.getObject(fDummyCallContext, repositoryId, objectId, filter, includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension, null);
  }

  public ObjectData getObjectByPath(String repositoryId, String path, String filter,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
    
    return fObjSvc.getObjectByPath(fDummyCallContext, repositoryId, path, filter, includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension, null);
  }

  public PropertiesData getProperties(String repositoryId, String objectId, String filter,
      ExtensionsData extension) {
    
    return fObjSvc.getProperties(fDummyCallContext, repositoryId, objectId, filter, extension);
  }

  public List<RenditionData> getRenditions(String repositoryId, String objectId,
      String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
    
    return fObjSvc.getRenditions(fDummyCallContext, repositoryId, objectId, renditionFilter, maxItems, skipCount, extension);
  }

  public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId,
      String sourceFolderId, ExtensionsData extension) {

    fObjSvc.moveObject(fDummyCallContext, repositoryId, objectId, targetFolderId, sourceFolderId, extension, null);    
  }

  public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
      Holder<String> changeToken, ContentStreamData contentStream, ExtensionsData extension) {
    
    fObjSvc.setContentStream(fDummyCallContext, repositoryId, objectId, overwriteFlag, changeToken, contentStream, extension);
  }

  public void updateProperties(String repositoryId, Holder<String> objectId,
      Holder<String> changeToken, PropertiesData properties, ExtensionsData extension) {
    
    fObjSvc.updateProperties(fDummyCallContext, repositoryId, objectId, changeToken, properties, null, extension, null);    
  }
  
}
