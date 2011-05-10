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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.inmemory.DataObjectCreator;
import org.apache.chemistry.opencmis.inmemory.FilterParser;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Filing;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Folder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.MultiFiling;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.SingleFiling;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.VersionedDocument;
import org.apache.chemistry.opencmis.inmemory.types.PropertyCreationHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InMemoryNavigationServiceImpl extends InMemoryAbstractServiceImpl {

    private static final Log LOG = LogFactory.getLog(InMemoryNavigationServiceImpl.class);

    final AtomLinkInfoProvider fAtomLinkProvider;

    public InMemoryNavigationServiceImpl(StoreManager storeManager) {
        super(storeManager);
        fAtomLinkProvider = new AtomLinkInfoProvider(fStoreManager);
    }

    public ObjectList getCheckedOutDocs(CallContext context, String repositoryId, String folderId, String filter,
            String orderBy, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension,
            ObjectInfoHandler objectInfos) {

        validator.getCheckedOutDocs(context, repositoryId, folderId, extension);
        ObjectListImpl res = new ObjectListImpl();
        List<ObjectData> odList = new ArrayList<ObjectData>();

        LOG.debug("start getCheckedOutDocs()");

        String user = context.getUsername();
        if (null == folderId) {
            List<StoredObject> checkedOuts = fStoreManager.getObjectStore(repositoryId).getCheckedOutDocuments(
                    orderBy);
            for (StoredObject checkedOut : checkedOuts) {
                TypeDefinition td = fStoreManager.getTypeById(repositoryId, checkedOut.getTypeId()).getTypeDefinition();
                ObjectData od = PropertyCreationHelper.getObjectData(td, checkedOut, filter, user,
                        includeAllowableActions, includeRelationships, renditionFilter, false, false, extension);
                if (context.isObjectInfoRequired()) {
                    ObjectInfoImpl objectInfo = new ObjectInfoImpl();
                    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, checkedOut, objectInfo);
                    objectInfos.addObjectInfo(objectInfo);
                }
                odList.add(od);
            }
        } else {
            ObjectInFolderList children = getChildrenIntern(repositoryId, folderId, filter, orderBy,
                    includeAllowableActions, includeRelationships, renditionFilter, false, -1, -1, false, context
                            .isObjectInfoRequired() ? objectInfos : null, user);
            for (ObjectInFolderData child : children.getObjects()) {
                ObjectData obj = child.getObject();
                StoredObject so = fStoreManager.getObjectStore(repositoryId).getObjectById(obj.getId());
                LOG.info("Checked out: children:" + obj.getId());
                if (so instanceof DocumentVersion && ((DocumentVersion) so).getParentDocument().isCheckedOut()) {
                    odList.add(obj);
                    if (context.isObjectInfoRequired()) {
                        ObjectInfoImpl objectInfo = new ObjectInfoImpl();
                        fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfo);
                        objectInfos.addObjectInfo(objectInfo);
                    }
                }
            }
        }
        res.setObjects(odList);
        res.setNumItems(BigInteger.valueOf(odList.size()));

        LOG.debug("end getCheckedOutDocs()");
        return res;
    }

    public ObjectInFolderList getChildren(CallContext context, String repositoryId, String folderId, String filter,
            String orderBy, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount,
            ExtensionsData extension, ObjectInfoHandler objectInfos) {

        LOG.debug("start getChildren()");

        validator.getChildren(repositoryId, folderId, extension);

        int maxItemsInt = maxItems == null ? -1 : maxItems.intValue();
        int skipCountInt = skipCount == null ? -1 : skipCount.intValue();
        String user = context.getUsername();
        ObjectInFolderList res = getChildrenIntern(repositoryId, folderId, filter, orderBy, includeAllowableActions,
                includeRelationships, renditionFilter, includePathSegment, maxItemsInt, skipCountInt, false,
                context.isObjectInfoRequired() ? objectInfos : null, user);
        LOG.debug("stop getChildren()");
        return res;
    }

    public List<ObjectInFolderContainer> getDescendants(CallContext context, String repositoryId, String folderId,
            BigInteger depth, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegment,
            ExtensionsData extension, ObjectInfoHandler objectInfos) {

        LOG.debug("start getDescendants()");

        validator.getDescendants(context, repositoryId, folderId, extension);

        int levels;
        if (depth == null) {
            levels = 2; // one of the recommended defaults (should it be
        } else if (depth.intValue() == 0) {
            throw new CmisInvalidArgumentException("A zero depth is not allowed for getDescendants().");
        } else {
            levels = depth.intValue();
        }

        int level = 0;
        String user = context.getUsername();
        List<ObjectInFolderContainer> result = getDescendantsIntern(repositoryId, folderId, filter,
                includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, level, levels,
                false, objectInfos, user);

        LOG.debug("stop getDescendants()");
        return result;
    }

    public ObjectData getFolderParent(CallContext context, String repositoryId, String folderId, String filter,
            ExtensionsData extension, ObjectInfoHandler objectInfos) {

        LOG.debug("start getFolderParent()");

        StoredObject so = validator.getFolderParent(context, repositoryId, folderId, extension);

        Folder folder = null;
        if (so instanceof Folder) {
            folder = (Folder) so;
        } else {
            throw new CmisInvalidArgumentException("Can't get folder parent, id does not refer to a folder: "
                    + folderId);
        }

        ObjectData res = getFolderParentIntern(repositoryId, folder, filter, context.isObjectInfoRequired() ? objectInfos : null);
        if (res == null) {
            throw new CmisInvalidArgumentException("Cannot get parent of a root folder");
        }

        // To be able to provide all Atom links in the response we need
        // additional information:
        if (context.isObjectInfoRequired()) {
            ObjectInfoImpl objectInfo = new ObjectInfoImpl();
            fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfo);
            objectInfos.addObjectInfo(objectInfo);
        }

        LOG.debug("stop getFolderParent()");
        return res;
    }

    public List<ObjectInFolderContainer> getFolderTree(CallContext context, String repositoryId, String folderId,
            BigInteger depth, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegment,
            ExtensionsData extension, ObjectInfoHandler objectInfos) {

        LOG.debug("start getFolderTree()");

        validator.getFolderTree(context, repositoryId, folderId, extension);

        if (depth != null && depth.intValue() == 0) {
            throw new CmisInvalidArgumentException("A zero depth is not allowed for getFolderTree().");
        }

        int levels = depth == null ? 2 : depth.intValue();
        int level = 0;
        String user = context.getUsername();
        List<ObjectInFolderContainer> result = getDescendantsIntern(repositoryId, folderId, filter,
                includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, level, levels,
                true, objectInfos, user);

        LOG.debug("stop getFolderTree()");
        return result;
    }

    public List<ObjectParentData> getObjectParents(CallContext context, String repositoryId, String objectId,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includeRelativePathSegment, ExtensionsData extension,
            ObjectInfoHandler objectInfos) {

        LOG.debug("start getObjectParents()");

        StoredObject so = validator.getObjectParents(context, repositoryId, objectId, extension);

        // for now we have only folders that have a parent and the in-memory
        // provider only has one
        // parent for each object (no multi-filing)
        List<ObjectParentData> result = null;

        Filing spo = null;
        if (so instanceof Filing) {
            spo = (Filing) so;
        } else {
            return Collections.emptyList();
        }

        result = getObjectParentsIntern(repositoryId, spo, filter, context.isObjectInfoRequired() ? objectInfos : null);

        // To be able to provide all Atom links in the response we need
        // additional information:
        if (context.isObjectInfoRequired()) {
            ObjectInfoImpl objectInfo = new ObjectInfoImpl();
            fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfo);
            objectInfos.addObjectInfo(objectInfo);
        }

        LOG.debug("stop getObjectParents()");
        return result;
    }

    // private helpers

    private ObjectInFolderList getChildrenIntern(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegments, int maxItems, int skipCount, boolean folderOnly, ObjectInfoHandler objectInfos,
            String user) {

        ObjectInFolderListImpl result = new ObjectInFolderListImpl();
        List<ObjectInFolderData> folderList = new ArrayList<ObjectInFolderData>();
        ObjectStore fs = fStoreManager.getObjectStore(repositoryId);
        StoredObject so = fs.getObjectById(folderId);
        Folder folder = null;

        if (so == null) {
            throw new CmisObjectNotFoundException("Unknown object id: " + folderId);
        }

        if (so instanceof Folder) {
            folder = (Folder) so;
        }
        else {
            return null; // it is a document and has no children
        }

        List<? extends StoredObject> children = folderOnly ? folder.getFolderChildren(maxItems, skipCount) : folder
                .getChildren(maxItems, skipCount);

        List<String> requestedIds = FilterParser.getRequestedIdsFromFilter(filter);

        for (StoredObject spo : children) {
            ObjectInFolderDataImpl oifd = new ObjectInFolderDataImpl();
            ObjectDataImpl objectData = new ObjectDataImpl();
            if (includePathSegments != null && includePathSegments) {
                oifd.setPathSegment(spo.getName());
            }
            if (includeAllowableActions != null && includeAllowableActions) {
                AllowableActions allowableActions = DataObjectCreator.fillAllowableActions(spo, user);
                objectData.setAllowableActions(allowableActions);
            }
            if (includeRelationships != null && includeRelationships != IncludeRelationships.NONE) {
                objectData.setRelationships(null /* f.getRelationships() */);
            }
            if (renditionFilter != null && renditionFilter.length() > 0) {
                objectData.setRelationships(null /*
                                                  * f.getRenditions(renditionFilter
                                                  * )
                                                  */);
            }

            TypeDefinition td = fStoreManager.getTypeById(repositoryId, spo.getTypeId()).getTypeDefinition();
            Properties props = PropertyCreationHelper.getPropertiesFromObject(spo, td, requestedIds, true);
            objectData.setProperties(props);

            oifd.setObject(objectData);
            folderList.add(oifd);
            // add additional information for Atom
            if (objectInfos != null) {
                ObjectInfoImpl objectInfo = new ObjectInfoImpl();
                fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, spo, objectInfo);
                objectInfos.addObjectInfo(objectInfo);
            }

        }
        result.setObjects(folderList);
        result.setNumItems(BigInteger.valueOf(folderList.size()));
        if (objectInfos != null) {
            ObjectInfoImpl objectInfo = new ObjectInfoImpl();
            fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, so, objectInfo);
            objectInfos.addObjectInfo(objectInfo);
        }
        return result;
    }

    private List<ObjectInFolderContainer> getDescendantsIntern(String repositoryId, String folderId, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegments, int level, int maxLevels, boolean folderOnly, ObjectInfoHandler objectInfos,
            String user) {

        // log.info("getDescendantsIntern: " + folderId + ", in level " + level
        // + ", max levels " + maxLevels);

        List<ObjectInFolderContainer> childrenOfFolderId = null;
        if (maxLevels == -1 || level < maxLevels) {
            String orderBy = PropertyIds.NAME;
            ObjectInFolderList children = getChildrenIntern(repositoryId, folderId, filter, orderBy,
                    includeAllowableActions, includeRelationships, renditionFilter, includePathSegments, 1000, 0,
                    folderOnly, objectInfos, user);

            childrenOfFolderId = new ArrayList<ObjectInFolderContainer>();
            if (null != children) {

                for (ObjectInFolderData child : children.getObjects()) {
                    ObjectInFolderContainerImpl oifc = new ObjectInFolderContainerImpl();
                    String childId = child.getObject().getId();
                    List<ObjectInFolderContainer> subChildren = getDescendantsIntern(repositoryId, childId, filter,
                            includeAllowableActions, includeRelationships, renditionFilter, includePathSegments,
                            level + 1, maxLevels, folderOnly, objectInfos, user);

                    oifc.setObject(child);
                    if (null != subChildren) {
                        oifc.setChildren(subChildren);
                    }
                    childrenOfFolderId.add(oifc);
                }
            }
        }
        return childrenOfFolderId;
    }

    private List<ObjectParentData> getObjectParentsIntern(String repositoryId, Filing sop, String filter,
            ObjectInfoHandler objectInfos) {

        List<ObjectParentData> result = null;
        if (sop instanceof SingleFiling) {
            ObjectData parent = getFolderParentIntern(repositoryId, (SingleFiling) sop, filter, objectInfos);
            if (null != parent) {
                ObjectParentDataImpl parentData = new ObjectParentDataImpl();
                parentData.setObject(parent);
                String path = ((SingleFiling) sop).getPath();
                int beginIndex = path.lastIndexOf(Filing.PATH_SEPARATOR) + 1; // Note
                // :
                // if
                // /
                // not
                // found
                // results in 0
                String relPathSeg = path.substring(beginIndex, path.length());
                parentData.setRelativePathSegment(relPathSeg);
                result = Collections.singletonList((ObjectParentData) parentData);
            } else {
                result = Collections.emptyList();
            }
        } else if (sop instanceof MultiFiling) {
            result = new ArrayList<ObjectParentData>();
            MultiFiling multiParentObj = (MultiFiling) sop;
            List<Folder> parents = multiParentObj.getParents();
            if (null != parents) {
                for (Folder parent : parents) {
                    ObjectParentDataImpl parentData = new ObjectParentDataImpl();
                    ObjectDataImpl objData = new ObjectDataImpl();
                    copyFilteredProperties(repositoryId, parent, filter, objData);
                    parentData.setObject(objData);
                    parentData.setRelativePathSegment(multiParentObj.getPathSegment());
                    result.add(parentData);
                    if (objectInfos != null) {
                        ObjectInfoImpl objectInfo = new ObjectInfoImpl();
                        fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, parent, objectInfo);
                        objectInfos.addObjectInfo(objectInfo);
                    }
                }
            }
        }
        return result;
    }

    private ObjectData getFolderParentIntern(String repositoryId, SingleFiling sop, String filter,
            ObjectInfoHandler objectInfos) {

        ObjectDataImpl parent = new ObjectDataImpl();

        Folder parentFolder = sop.getParent();

        if (null == parentFolder) {
            return null;
        }

        copyFilteredProperties(repositoryId, parentFolder, filter, parent);
        if (objectInfos != null) {
            ObjectInfoImpl objectInfo = new ObjectInfoImpl();
            fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, parentFolder, objectInfo);
            objectInfos.addObjectInfo(objectInfo);
        }

        return parent;
    }

    void copyFilteredProperties(String repositoryId, StoredObject so, String filter, ObjectDataImpl objData) {
        List<String> requestedIds = FilterParser.getRequestedIdsFromFilter(filter);
        TypeDefinition td = fStoreManager.getTypeById(repositoryId, so.getTypeId()).getTypeDefinition();
        Properties props = PropertyCreationHelper.getPropertiesFromObject(so, td, requestedIds, true);
        objData.setProperties(props);
    }

}
