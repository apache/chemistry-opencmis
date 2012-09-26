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
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
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
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Children;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryNavigationServiceImpl extends InMemoryAbstractServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryNavigationServiceImpl.class);

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
                    orderBy, context.getUsername(), includeRelationships);
            for (StoredObject checkedOut : checkedOuts) {
                TypeDefinition td = fStoreManager.getTypeById(repositoryId, checkedOut.getTypeId()).getTypeDefinition();
//                DocumentVersion workingCopy = ((VersionedDocument) checkedOut).getPwc();
//                if (null == workingCopy)
//                	throw new CmisConstraintException("document " + checkedOut + " is checked out but has no working copy");       
                ObjectData od = PropertyCreationHelper.getObjectData(td, checkedOut, filter, user,
                        includeAllowableActions, includeRelationships, renditionFilter, false, false, extension);
                if (context.isObjectInfoRequired()) {
                    ObjectInfoImpl objectInfo = new ObjectInfoImpl();
                    fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, /* workingCopy */ checkedOut, objectInfo);
                    objectInfos.addObjectInfo(objectInfo);
                }
                odList.add(od);
            }
        } else {
            LOG.debug("getting checked-out documents for folder: " + folderId);
            ObjectInFolderList children = getChildrenIntern(repositoryId, folderId, filter, orderBy,
                    includeAllowableActions, includeRelationships, renditionFilter, false, -1, -1, false, context
                            .isObjectInfoRequired() ? objectInfos : null, user);
            for (ObjectInFolderData child : children.getObjects()) {
                ObjectData obj = child.getObject();
                StoredObject so = fStoreManager.getObjectStore(repositoryId).getObjectById(obj.getId());
                LOG.debug("Checked out: children:" + obj.getId());
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
        res.setHasMoreItems(false);

        LOG.debug("end getCheckedOutDocs()");
        return res;
    }

    public ObjectInFolderList getChildren(CallContext context, String repositoryId, String folderId, String filter,
            String orderBy, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount,
            ExtensionsData extension, ObjectInfoHandler objectInfos) {

        LOG.debug("start getChildren()");

        validator.getChildren(context,repositoryId, folderId, extension);

        int maxItemsInt = maxItems == null ? -1 : maxItems.intValue();
        int skipCountInt = skipCount == null ? 0 : skipCount.intValue();
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

        ObjectData res = getFolderParentIntern(repositoryId, folder, filter, false, 
        		IncludeRelationships.NONE, context.getUsername(), context.isObjectInfoRequired() ? objectInfos : null);
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

        result = getObjectParentsIntern(repositoryId, spo, filter, 
        		context.isObjectInfoRequired() ? objectInfos : null, includeAllowableActions, includeRelationships,
				renditionFilter, context.getUsername());

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

        Children.ChildrenResult children = folderOnly ? folder.getFolderChildren(maxItems, skipCount, user) : folder
                .getChildren(maxItems, skipCount, user);

        for (StoredObject spo : children.getChildren()) {
            ObjectInFolderDataImpl oifd = new ObjectInFolderDataImpl();
            if (includePathSegments != null && includePathSegments) {
                oifd.setPathSegment(spo.getName());
            }

            TypeDefinition typeDef = fStoreManager.getTypeById(repositoryId, spo.getTypeId()).getTypeDefinition();
            ObjectData objectData = PropertyCreationHelper.getObjectData(typeDef, spo, filter, user, includeAllowableActions, 
                    includeRelationships, renditionFilter, false, false, null);

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
        result.setNumItems(BigInteger.valueOf(children.getNoItems()));
        result.setHasMoreItems(children.getNoItems() > skipCount + folderList.size());

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
            ObjectInfoHandler objectInfos, Boolean includeAllowableActions, 
            IncludeRelationships includeRelationships, String renditionFilter, String user) {

        List<ObjectParentData> result = null;
        if (sop instanceof SingleFiling) {
            ObjectData parent = getFolderParentIntern(repositoryId, (SingleFiling) sop, filter, 
            		includeAllowableActions, includeRelationships, user, objectInfos);
            if (null != parent) {
                ObjectParentDataImpl parentData = new ObjectParentDataImpl();
                parentData.setObject(parent);
                String path = ((SingleFiling) sop).getPath();
                int beginIndex = path.lastIndexOf(Filing.PATH_SEPARATOR) + 1; 
                //   Note: if not found results in 0
                String relPathSeg = path.substring(beginIndex, path.length());
                parentData.setRelativePathSegment(relPathSeg);
                result = Collections.singletonList((ObjectParentData) parentData);
            } else {
                result = Collections.emptyList();
            }
        } else if (sop instanceof MultiFiling) {
            result = new ArrayList<ObjectParentData>();
            MultiFiling multiParentObj = (MultiFiling) sop;
            List<Folder> parents = multiParentObj.getParents(user);
            if (null != parents) {
                for (Folder parent : parents) {
                    ObjectParentDataImpl parentData = new ObjectParentDataImpl();
                    TypeDefinition typeDef = fStoreManager.getTypeById(repositoryId, parent.getTypeId()).getTypeDefinition();
                    ObjectData objData = PropertyCreationHelper.getObjectData(typeDef, parent, filter, user, includeAllowableActions, 
                            includeRelationships, renditionFilter, false, true, null);

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
    		Boolean includeAllowableActions, IncludeRelationships includeRelationships, 
    		String user, ObjectInfoHandler objectInfos) {

        ObjectDataImpl parent = new ObjectDataImpl();

        Folder parentFolder = sop.getParent();

        if (null == parentFolder) {
            return null;
        }

        copyFilteredProperties(repositoryId, parentFolder, filter, parent);
        
        parent.setRelationships(DataObjectCreator.getRelationships(includeRelationships, parentFolder, user));
        
        if (includeAllowableActions != null && includeAllowableActions) {
            //  AllowableActions allowableActions = DataObjectCreator.fillAllowableActions(spo, user);
          	AllowableActions allowableActions = parentFolder.getAllowableActions(user);
          	parent.setAllowableActions(allowableActions);
          }
        
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
