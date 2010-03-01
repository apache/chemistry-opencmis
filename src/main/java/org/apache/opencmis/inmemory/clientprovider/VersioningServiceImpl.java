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

import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.VersioningService;
import org.apache.opencmis.inmemory.server.InMemoryVersioningServiceImpl;

public class VersioningServiceImpl extends AbstractService implements VersioningService {

  private InMemoryVersioningServiceImpl fVersioningSvc;

  public VersioningServiceImpl(InMemoryVersioningServiceImpl verSvc) {
    fVersioningSvc = verSvc;
  }

  public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {

    fVersioningSvc.cancelCheckOut(fDummyCallContext, repositoryId, objectId, extension);
  }

  public void checkIn(String repositoryId, Holder<String> objectId, Boolean major,
      PropertiesData properties, ContentStreamData contentStream, String checkinComment,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {

    fVersioningSvc.checkIn(fDummyCallContext, repositoryId, objectId, major, properties,
        contentStream, checkinComment, policies, addAces, removeAces, extension, null);
  }

  public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension,
      Holder<Boolean> contentCopied) {

    fVersioningSvc.checkOut(fDummyCallContext, repositoryId, objectId, extension, contentCopied,
        null);
  }

  public List<ObjectData> getAllVersions(String repositoryId, String versionSeriesId,
      String filter, Boolean includeAllowableActions, ExtensionsData extension) {

    return fVersioningSvc.getAllVersions(fDummyCallContext, repositoryId, versionSeriesId, filter,
        includeAllowableActions, extension, null);
  }

  public ObjectData getObjectOfLatestVersion(String repositoryId, String versionSeriesId,
      Boolean major, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
      Boolean includeAcl, ExtensionsData extension) {

    return fVersioningSvc.getObjectOfLatestVersion(fDummyCallContext, repositoryId,
        versionSeriesId, major, filter, includeAllowableActions, includeRelationships,
        renditionFilter, includePolicyIds, includeAcl, extension, null);
  }

  public PropertiesData getPropertiesOfLatestVersion(String repositoryId, String versionSeriesId,
      Boolean major, String filter, ExtensionsData extension) {

    return fVersioningSvc.getPropertiesOfLatestVersion(fDummyCallContext, repositoryId,
        versionSeriesId, major, filter, extension);
  }

}
