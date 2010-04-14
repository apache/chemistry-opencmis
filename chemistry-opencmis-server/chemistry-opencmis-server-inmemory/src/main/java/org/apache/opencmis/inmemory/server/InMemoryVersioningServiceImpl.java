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
package org.apache.opencmis.inmemory.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.api.DocumentTypeDefinition;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.inmemory.FilterParser;
import org.apache.opencmis.inmemory.storedobj.api.Document;
import org.apache.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.opencmis.inmemory.storedobj.api.VersionedDocument;
import org.apache.opencmis.inmemory.types.PropertyCreationHelper;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisVersioningService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;

public class InMemoryVersioningServiceImpl extends AbstractServiceImpl implements
    CmisVersioningService {

  private static final Log LOG = LogFactory.getLog(InMemoryVersioningServiceImpl.class.getName());

  InMemoryObjectServiceImpl fObjectService; // real implementation of the service
  AtomLinkInfoProvider fAtomLinkProvider;

  public InMemoryVersioningServiceImpl(StoreManager storeManager,
      InMemoryObjectServiceImpl objectService) {
    super(storeManager);
    fObjectService = objectService;
    fAtomLinkProvider = new AtomLinkInfoProvider(fStoreManager);
  }

  public void cancelCheckOut(CallContext context, String repositoryId, String objectId,
      ExtensionsData extension) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      StoredObject so = checkStandardParameters(repositoryId, objectId);
      String user = RuntimeContext.getRuntimeConfigValue(CallContext.USERNAME);
      VersionedDocument verDoc = testHasProperCheckedOutStatus(so, user);

      verDoc.cancelCheckOut(user);
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public ObjectData checkIn(CallContext context, String repositoryId, Holder<String> objectId,
      Boolean major, PropertiesData properties, ContentStreamData contentStream,
      String checkinComment, List<String> policies, AccessControlList addAces,
      AccessControlList removeAces, ExtensionsData extension, ObjectInfoHolder objectInfos) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      StoredObject so = checkStandardParameters(repositoryId, objectId.getValue());
      String user = RuntimeContext.getRuntimeConfigValue(CallContext.USERNAME);
      VersionedDocument verDoc = testHasProperCheckedOutStatus(so, user);

      DocumentVersion pwc = verDoc.getPwc();

      if (null != contentStream)
        pwc.setContent(contentStream, false);

      if (null != properties && null != properties.getProperties())
        pwc.setCustomProperties(properties.getProperties());

      verDoc.checkIn(major, checkinComment, user);

      // To be able to provide all Atom links in the response we need additional information:
      fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfos);

      ObjectData od = PropertyCreationHelper.getObjectData(fStoreManager, so, null, false,
          IncludeRelationships.NONE, null, false, false, extension);

      return od;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public ObjectData checkOut(CallContext context, String repositoryId, Holder<String> objectId,
      ExtensionsData extension, Holder<Boolean> contentCopied, ObjectInfoHolder objectInfos) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      StoredObject so = checkStandardParameters(repositoryId, objectId.getValue());
      TypeDefinition typeDef = getTypeDefinition(repositoryId, so);
      if (!typeDef.getBaseId().equals(BaseObjectTypeIds.CMIS_DOCUMENT))
        throw new CmisNotSupportedException("Only documents can be checked-out.");
      else if (!((DocumentTypeDefinition) typeDef).isVersionable())
        throw new CmisNotSupportedException("Object can't be checked-out, type is not versionable.");

      checkIsVersionableObject(so);

      VersionedDocument verDoc = getVersionedDocumentOfObjectId(so);

      ContentStreamData content = null;

      if (so instanceof DocumentVersion) {
        // get document the version is contained in to c
        content = ((DocumentVersion) so).getContent(0, -1);
      }
      else {
        content = ((VersionedDocument) so).getLatestVersion(false).getContent(0, -1);
      }

      if (verDoc.isCheckedOut())
        throw new CmisUpdateConflictException("Document " + objectId.getValue()
            + " is already checked out.");

      String user = RuntimeContext.getRuntimeConfigValue(CallContext.USERNAME);
      checkHasUser(user);

      DocumentVersion pwc = verDoc.checkOut(content, user);
      objectId.setValue(pwc.getId()); // return the id of the created pwc

      // To be able to provide all Atom links in the response we need additional information:
      fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfos);

      ObjectData od = PropertyCreationHelper.getObjectData(fStoreManager, so, null, false,
          IncludeRelationships.NONE, null, false, false, extension);

      return od;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public List<ObjectData> getAllVersions(CallContext context, String repositoryId,
      String versionSeriesId, String filter, Boolean includeAllowableActions,
      ExtensionsData extension, ObjectInfoHolder objectInfos) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      StoredObject so = checkStandardParameters(repositoryId, versionSeriesId);

      if (!(so instanceof VersionedDocument))
        throw new RuntimeException("Object is not instance of a VersionedDocument (version series)");

      VersionedDocument verDoc = (VersionedDocument) so;
      List<ObjectData> res = new ArrayList<ObjectData>();
      List<DocumentVersion> versions = verDoc.getAllVersions();
      for (DocumentVersion version : versions) {
        ObjectData objData = getObject(context, repositoryId, version.getId(), filter,
            includeAllowableActions, extension, objectInfos);
        res.add(objData);
      }

      // provide information for Atom links for version series:
      fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfos);

      return res;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public ObjectData getObjectOfLatestVersion(CallContext context, String repositoryId,
      String versionSeriesId, Boolean major, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
      Boolean includeAcl, ExtensionsData extension, ObjectInfoHolder objectInfos) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      StoredObject so = checkStandardParameters(repositoryId, versionSeriesId);
      ObjectData objData = null;

      if (so instanceof VersionedDocument) {
        VersionedDocument verDoc = (VersionedDocument) so;
        DocumentVersion latestVersion = verDoc.getLatestVersion(major);
        objData = getObject(context, repositoryId, latestVersion.getId(), filter,
            includeAllowableActions, extension, objectInfos);
      }
      else if (so instanceof Document) {
        objData = getObject(context, repositoryId, so.getId(), filter, includeAllowableActions,
            extension, objectInfos);
      }
      else
        throw new RuntimeException("Object is not instance of a document (version series)");

      // provide information for Atom links for version series:
      fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfos);

      return objData;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  public PropertiesData getPropertiesOfLatestVersion(CallContext context, String repositoryId,
      String versionSeriesId, Boolean major, String filter, ExtensionsData extension) {

    try {
      // Attach the CallContext to a thread local context that can be accessed from everywhere
      RuntimeContext.attachCfg(context);

      StoredObject so = checkStandardParameters(repositoryId, versionSeriesId);
      StoredObject latestVersionObject = null;

      if (so instanceof VersionedDocument) {
        VersionedDocument verDoc = (VersionedDocument) so;
        latestVersionObject = verDoc.getLatestVersion(major);
      }
      else if (so instanceof Document) {
        latestVersionObject = so;
      }
      else
        throw new RuntimeException("Object is not instance of a document (version series)");

      List<String> requestedIds = FilterParser.getRequestedIdsFromFilter(filter);
      PropertiesData props = PropertyCreationHelper.getPropertiesFromObject(repositoryId,
          latestVersionObject, fStoreManager, requestedIds);

      return props;
    }
    finally {
      RuntimeContext.remove();
    }
  }

  private ObjectData getObject(CallContext context, String repositoryId, String objectId,
      String filter, Boolean includeAllowableActions, ExtensionsData extension,
      ObjectInfoHolder objectInfos) {

    return fObjectService.getObject(context, repositoryId, objectId, filter,
        includeAllowableActions, IncludeRelationships.NONE, null, false, includeAllowableActions,
        extension, objectInfos);
  }
}
