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

import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PropertiesData;

/**
 * CMIS Versioning Service interface. Please refer to the CMIS specification and the OpenCMIS
 * documentation for details.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public interface CmisVersioningService {

  /**
   * Checks out a document.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  public ObjectData checkOut(CallContext context, String repositoryId, Holder<String> objectId,
      ExtensionsData extension, Holder<Boolean> contentCopied, ObjectInfoHolder objectInfos);

  /**
   * Cancels a check out.
   * 
   * <p>
   * Bindings: Web Services
   * </p>
   * 
   * @see CmisObjectService#deleteObjectOrCancelCheckOut(CallContext, String, String, Boolean,
   *      ExtensionsData)
   */
  public void cancelCheckOut(CallContext context, String repositoryId, String objectId,
      ExtensionsData extension);

  public ObjectData checkIn(CallContext context, String repositoryId, Holder<String> objectId,
      Boolean major, PropertiesData properties, ContentStreamData contentStream,
      String checkinComment, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension, ObjectInfoHolder objectInfos);

  /**
   * Gets the latest version an object.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  public ObjectData getObjectOfLatestVersion(CallContext context, String repositoryId,
      String versionSeriesId, Boolean major, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
      Boolean includeAcl, ExtensionsData extension, ObjectInfoHolder objectInfos);

  /**
   * Gets the properties of latest version an object.
   * 
   * <p>
   * Bindings: Web Services
   * </p>
   */
  public PropertiesData getPropertiesOfLatestVersion(CallContext context, String repositoryId,
      String versionSeriesId, Boolean major, String filter, ExtensionsData extension);

  /**
   * Gets the list of all versions of a document.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  public List<ObjectData> getAllVersions(CallContext context, String repositoryId,
      String versionSeriesId, String filter, Boolean includeAllowableActions,
      ExtensionsData extension, ObjectInfoHolder objectInfos);
}
