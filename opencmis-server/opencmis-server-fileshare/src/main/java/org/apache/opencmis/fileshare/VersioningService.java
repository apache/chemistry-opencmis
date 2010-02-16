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

import java.util.Collections;
import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisVersioningService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

/**
 * Versioning Service.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class VersioningService implements CmisVersioningService {

  private RepositoryMap fRepositoryMap;

  /**
   * Constructor.
   */
  public VersioningService(RepositoryMap repositoryMap) {
    fRepositoryMap = repositoryMap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisVersioningService#cancelCheckOut(org.apache.opencmis.server.spi.CallContext
   * , java.lang.String, java.lang.String, org.apache.opencmis.commons.api.ExtensionsData)
   */
  public void cancelCheckOut(CallContext context, String repositoryId, String objectId,
      ExtensionsData extension) {
    throw new CmisNotSupportedException("cancelCheckOut not supported!");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.server.spi.CmisVersioningService#checkIn(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, org.apache.opencmis.commons.provider.Holder, java.lang.Boolean,
   * org.apache.opencmis.commons.provider.PropertiesData, org.apache.opencmis.commons.provider.ContentStreamData,
   * java.lang.String, java.util.List, org.apache.opencmis.commons.provider.AccessControlList,
   * org.apache.opencmis.commons.provider.AccessControlList, org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData checkIn(CallContext context, String repositoryId, Holder<String> objectId,
      Boolean major, PropertiesData properties, ContentStreamData contentStream,
      String checkinComment, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension, ObjectInfoHolder objectInfos) {
    throw new CmisNotSupportedException("checkIn not supported!");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisVersioningService#checkOut(org.apache.opencmis.server.spi.CallContext,
   * java.lang.String, org.apache.opencmis.commons.provider.Holder,
   * org.apache.opencmis.commons.api.ExtensionsData, org.apache.opencmis.commons.provider.Holder,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData checkOut(CallContext context, String repositoryId, Holder<String> objectId,
      ExtensionsData extension, Holder<Boolean> contentCopied, ObjectInfoHolder objectInfos) {
    throw new CmisNotSupportedException("checkOut not supported!");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisVersioningService#getAllVersions(org.apache.opencmis.server.spi.CallContext
   * , java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.api.ExtensionsData, org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public List<ObjectData> getAllVersions(CallContext context, String repositoryId,
      String versionSeriesId, String filter, Boolean includeAllowableActions,
      ExtensionsData extension, ObjectInfoHolder objectInfos) {
    ObjectData theVersion = fRepositoryMap.getAuthenticatedRepository(context, repositoryId)
        .getObject(context, versionSeriesId, filter, includeAllowableActions, false, objectInfos);

    return Collections.singletonList(theVersion);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisVersioningService#getObjectOfLatestVersion(org.apache.opencmis.server.
   * spi.CallContext, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String,
   * java.lang.Boolean, org.apache.opencmis.commons.enums.IncludeRelationships, java.lang.String,
   * java.lang.Boolean, java.lang.Boolean, org.apache.opencmis.commons.api.ExtensionsData,
   * org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public ObjectData getObjectOfLatestVersion(CallContext context, String repositoryId,
      String versionSeriesId, Boolean major, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
      Boolean includeAcl, ExtensionsData extension, ObjectInfoHolder objectInfos) {
    return fRepositoryMap.getAuthenticatedRepository(context, repositoryId).getObject(context,
        versionSeriesId, filter, includeAllowableActions, includeAcl, objectInfos);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.server.spi.CmisVersioningService#getPropertiesOfLatestVersion(org.apache.opencmis.server
   * .spi.CallContext, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String,
   * org.apache.opencmis.commons.api.ExtensionsData, org.apache.opencmis.server.spi.ObjectInfoHolder)
   */
  public PropertiesData getPropertiesOfLatestVersion(CallContext context, String repositoryId,
      String versionSeriesId, Boolean major, String filter, ExtensionsData extension) {
    ObjectData object = fRepositoryMap.getAuthenticatedRepository(context, repositoryId).getObject(
        context, versionSeriesId, filter, false, false, null);

    return object.getProperties();
  }
}
