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
package org.apache.opencmis.commons.provider;

import java.math.BigInteger;
import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;

/**
 * Object Service interface. See CMIS 1.0 domain model for details.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 * @see <a href="http://www.oasis-open.org/committees/tc_home.php?wg_abbrev=cmis">OASIS CMIS
 *      Technical Committee</a>
 */
public interface ObjectService {

  String createDocument(String repositoryId, PropertiesData properties, String folderId,
      ContentStreamData contentStream, VersioningState versioningState, List<String> policies,
      AccessControlList addAces, AccessControlList removeAces, ExtensionsData extension);

  String createDocumentFromSource(String repositoryId, String sourceId, PropertiesData properties,
      String folderId, VersioningState versioningState, List<String> policies,
      AccessControlList addAces, AccessControlList removeAces, ExtensionsData extension);

  String createFolder(String repositoryId, PropertiesData properties, String folderId,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension);

  String createRelationship(String repositoryId, PropertiesData properties, List<String> policies,
      AccessControlList addAces, AccessControlList removeAces, ExtensionsData extension);

  String createPolicy(String repositoryId, PropertiesData properties, String folderId,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension);

  AllowableActionsData getAllowableActions(String repositoryId, String objectId,
      ExtensionsData extension);

  ObjectData getObject(String repositoryId, String objectId, String filter,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension);

  PropertiesData getProperties(String repositoryId, String objectId, String filter,
      ExtensionsData extension);

  List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
      BigInteger maxItems, BigInteger skipCount, ExtensionsData extension);

  ObjectData getObjectByPath(String repositoryId, String path, String filter,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension);

  ContentStreamData getContentStream(String repositoryId, String objectId, String streamId,
      BigInteger offset, BigInteger length, ExtensionsData extension);

  void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
      PropertiesData properties, ExtensionsData extension);

  void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId,
      String sourceFolderId, ExtensionsData extension);

  void deleteObject(String repositoryId, String objectId, Boolean allVersions,
      ExtensionsData extension);

  FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
      UnfileObjects unfileObjects, Boolean continueOnFailure, ExtensionsData extension);

  void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
      Holder<String> changeToken, ContentStreamData contentStream, ExtensionsData extension);

  void deleteContentStream(String repositoryId, Holder<String> objectId,
      Holder<String> changeToken, ExtensionsData extension);
}
