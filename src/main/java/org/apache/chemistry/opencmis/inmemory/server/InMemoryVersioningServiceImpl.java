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
package org.apache.chemistry.opencmis.inmemory.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.inmemory.FilterParser;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Document;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.VersionedDocument;
import org.apache.chemistry.opencmis.inmemory.types.PropertyCreationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryVersioningServiceImpl extends InMemoryAbstractServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryVersioningServiceImpl.class.getName());

    final InMemoryObjectServiceImpl fObjectService; // real implementation of the
    // service
    final AtomLinkInfoProvider fAtomLinkProvider;

    public InMemoryVersioningServiceImpl(StoreManager storeManager, InMemoryObjectServiceImpl objectService) {
        super(storeManager);
        fObjectService = objectService;
        fAtomLinkProvider = new AtomLinkInfoProvider(fStoreManager);
    }

    public void cancelCheckOut(CallContext context, String repositoryId, String objectId, ExtensionsData extension) {

        StoredObject so = validator.cancelCheckOut(context, repositoryId, objectId, extension);

        String user = context.getUsername();
        VersionedDocument verDoc = testHasProperCheckedOutStatus(so, user);

        verDoc.cancelCheckOut(user);
        
        // if this is the last version delete the document itself
        if (verDoc.getAllVersions().size() == 0)
            fStoreManager.getObjectStore(repositoryId).deleteObject(verDoc.getId(), true, user);
    }

    public void checkIn(CallContext context, String repositoryId, Holder<String> objectId, Boolean major,
            Properties properties, ContentStream contentStream, String checkinComment, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension, ObjectInfoHandler objectInfos) {

    	addAces  = org.apache.chemistry.opencmis.inmemory.TypeValidator.expandAclMakros(context.getUsername(), addAces);
    	removeAces  = org.apache.chemistry.opencmis.inmemory.TypeValidator.expandAclMakros(context.getUsername(), removeAces);

    	StoredObject so = validator.checkIn(context, repositoryId, objectId, addAces, removeAces, extension);

        String user = context.getUsername();
        VersionedDocument verDoc = testHasProperCheckedOutStatus(so, user);

        // check if the contentStream is a usable object or ignore it otherwise
        // Note Bworser binding sets an empty object
        if (contentStream != null && contentStream.getStream() == null)
            contentStream = null;
        
        verDoc.checkIn(major, properties, contentStream, checkinComment, user);

        // To be able to provide all Atom links in the response we need
        // additional information:
        if (context.isObjectInfoRequired()) {
            ObjectInfoImpl objectInfo = new ObjectInfoImpl();
            fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfo);
            objectInfos.addObjectInfo(objectInfo);
        }
    }

    public void checkOut(CallContext context, String repositoryId, Holder<String> objectId,
            ExtensionsData extension, Holder<Boolean> contentCopied, ObjectInfoHandler objectInfos) {

        StoredObject so = validator.checkOut(context, repositoryId, objectId, extension, contentCopied);

        TypeDefinition typeDef = getTypeDefinition(repositoryId, so);
        if (!typeDef.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT)) {
            throw new CmisNotSupportedException("Only documents can be checked-out.");
        } else if (!((DocumentTypeDefinition) typeDef).isVersionable()) {
            throw new CmisNotSupportedException("Object can't be checked-out, type is not versionable.");
        }

        checkIsVersionableObject(so);

        VersionedDocument verDoc = getVersionedDocumentOfObjectId(so);

        ContentStream content = null;

        if (so instanceof DocumentVersion) {
            // get document the version is contained in to c
            content = ((DocumentVersion) so).getContent(0, -1);
        } else {
            content = ((VersionedDocument) so).getLatestVersion(false).getContent(0, -1);
        }

        if (verDoc.isCheckedOut()) {
            throw new CmisUpdateConflictException("Document " + objectId.getValue() + " is already checked out.");
        }

        String user = context.getUsername();
        checkHasUser(user);

        DocumentVersion pwc = verDoc.checkOut(content, user);
        objectId.setValue(pwc.getId()); // return the id of the created pwc
        if (null != contentCopied) // Note: always null in AtomPub binding
            contentCopied.setValue(true);
        
        // To be able to provide all Atom links in the response we need
        // additional information:
        if (context.isObjectInfoRequired()) {
            ObjectInfoImpl objectInfo = new ObjectInfoImpl();
            fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, pwc, objectInfo);
            objectInfos.addObjectInfo(objectInfo);
        }
    }

    public List<ObjectData> getAllVersions(CallContext context, String repositoryId, String objectId, String versionSeriesId,
            String filter, Boolean includeAllowableActions, ExtensionsData extension, ObjectInfoHandler objectInfos) {
        
        // Note that in AtomPub object id is null and versionSeriesId is set and in SOAP bindinf versionSeriesId is set
        // and objectId is null
        StoredObject so;
        List<ObjectData> res = new ArrayList<ObjectData>();
        if (null == versionSeriesId)
            versionSeriesId = objectId;
        if (null == versionSeriesId)
            throw new CmisInvalidArgumentException("getAllVersions requires a version series id, but it was null.");
        so = validator.getAllVersions(context, repositoryId, objectId, versionSeriesId, extension);

        if (null == objectId)
            objectId = versionSeriesId;

        if (!(so instanceof VersionedDocument)) {
            if (!(so instanceof DocumentVersion))
                throw new CmisInvalidArgumentException("getAllVersions requires an id of a versioned document.");
            so = ((DocumentVersion)so).getParentDocument();
        }
//        ObjectData objData = getObject(context, repositoryId, so.getId(), filter, includeAllowableActions,
//                IncludeRelationships.NONE,extension, objectInfos);
//        res.add(objData);

        VersionedDocument verDoc = (VersionedDocument) so;
        res = new ArrayList<ObjectData>();
        List<DocumentVersion> versions = verDoc.getAllVersions();
        for (DocumentVersion version : versions) {
            ObjectData objData = getObject(context, repositoryId, version.getId(), filter, includeAllowableActions,
                    IncludeRelationships.NONE,extension, objectInfos);
            res.add(objData);
        }

        // reverse list of versions because spec expects latest version first
        List<ObjectData> temp = new ArrayList<ObjectData>(res.size());
        for (ObjectData ver : res)
            temp.add(0, ver);
        res = temp;

        // provide information for Atom links for version series:
        if (context.isObjectInfoRequired()) {
            ObjectInfoImpl objectInfo = new ObjectInfoImpl();
            fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfo);
            objectInfos.addObjectInfo(objectInfo);
        }

        return res;
    }

    public ObjectData getObjectOfLatestVersion(CallContext context, String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension,
            ObjectInfoHandler objectInfos) {

        StoredObject so = validator.getObjectOfLatestVersion(context, repositoryId, objectId, versionSeriesId, extension);

        ObjectData objData = null;

        // In AtomPu8b you do not get the version series id, only the object id
        if (so instanceof DocumentVersion) {
            so = ((DocumentVersion)so).getParentDocument();
        }
        
        if (so instanceof VersionedDocument) {
            VersionedDocument verDoc = (VersionedDocument) so;
            DocumentVersion latestVersion = verDoc.getLatestVersion(major);
            objData = getObject(context, repositoryId, latestVersion.getId(), filter, includeAllowableActions,
                    includeRelationships, extension, objectInfos);
        } else if (so instanceof Document) {
            objData = getObject(context, repositoryId, so.getId(), filter, includeAllowableActions,
                    includeRelationships, extension, objectInfos);
        } else {
            throw new CmisInvalidArgumentException("Object is not instance of a document (version series)");
        }

        // provide information for Atom links for version series:
        if (context.isObjectInfoRequired()) {
            ObjectInfoImpl objectInfo = new ObjectInfoImpl();
            fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfo);
            objectInfos.addObjectInfo(objectInfo);
        }

        return objData;
    }

    public Properties getPropertiesOfLatestVersion(CallContext context, String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, ExtensionsData extension) {

        StoredObject so = validator.getPropertiesOfLatestVersion(context, repositoryId, objectId, versionSeriesId, extension);

        StoredObject latestVersionObject = null;

        // In AtomPu8b you do not get the version series id, only the object id
        if (so instanceof DocumentVersion) {
            so = ((DocumentVersion)so).getParentDocument();
        }

        if (so instanceof VersionedDocument) {
            VersionedDocument verDoc = (VersionedDocument) so;
            latestVersionObject = verDoc.getLatestVersion(major);
        } else if (so instanceof Document) {
            latestVersionObject = so;
        } else {
            throw new CmisInvalidArgumentException("Object is not instance of a document (version series)");
        }

        List<String> requestedIds = FilterParser.getRequestedIdsFromFilter(filter);

        TypeDefinition td = fStoreManager.getTypeById(repositoryId, latestVersionObject.getTypeId()).getTypeDefinition();
        Properties props = PropertyCreationHelper.getPropertiesFromObject(latestVersionObject, td,
                requestedIds, true);

        return props;
    }

    private ObjectData getObject(CallContext context, String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, ExtensionsData extension,
            ObjectInfoHandler objectInfos) {

        return fObjectService.getObject(context, repositoryId, objectId, filter, includeAllowableActions,
                includeRelationships, null, false, includeAllowableActions, extension, objectInfos);
    }
}
