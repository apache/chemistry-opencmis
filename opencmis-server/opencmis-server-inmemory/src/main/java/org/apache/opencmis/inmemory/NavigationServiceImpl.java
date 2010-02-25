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
package org.apache.opencmis.inmemory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectInFolderContainerImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.opencmis.commons.provider.AllowableActionsData;
import org.apache.opencmis.commons.provider.NavigationService;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectInFolderContainer;
import org.apache.opencmis.commons.provider.ObjectInFolderData;
import org.apache.opencmis.commons.provider.ObjectInFolderList;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.commons.provider.ObjectParentData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.inmemory.storedobj.api.Children;
import org.apache.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.opencmis.inmemory.storedobj.api.Folder;
import org.apache.opencmis.inmemory.storedobj.api.MultiFiling;
import org.apache.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.opencmis.inmemory.storedobj.api.Filing;
import org.apache.opencmis.inmemory.storedobj.api.SingleFiling;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.opencmis.inmemory.storedobj.api.VersionedDocument;
import org.apache.opencmis.inmemory.types.PropertyCreationHelper;

/**
 * Navigation Service interface. See CMIS 1.0 domain model for details.
 * @author Jens
 */

public class NavigationServiceImpl extends AbstractServiceImpl implements NavigationService {
  private static Log log = LogFactory.getLog(NavigationServiceImpl.class);
  
  public NavigationServiceImpl(StoreManager storeManager) {
    super(storeManager);
    fStoreManager = storeManager;
  }

  /* (non-Javadoc)
   * @see org.opencmis.client.provider.NavigationService#getCheckedOutDocs(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.opencmis.client.provider.ExtensionsData)
   */
  public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter,
      String orderBy, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
    
    ObjectListImpl res = new ObjectListImpl();
    List<ObjectData> odList = new ArrayList<ObjectData>();
    
    log.debug("start getCheckedOutDocs()");
    if (null != folderId)
      checkStandardParameters(repositoryId, folderId);
    else
      checkRepositoryId(repositoryId);

    if (null == folderId) {
      List<VersionedDocument> checkedOuts = fStoreManager.getObjectStore(repositoryId).getCheckedOutDocuments(orderBy);
      for (VersionedDocument checkedOut : checkedOuts) {
        ObjectData od = PropertyCreationHelper.getObjectData(fStoreManager, checkedOut, filter,
            includeAllowableActions, includeRelationships, renditionFilter, false,
            false, extension);
        odList.add(od);
      }
    } else {
      ObjectInFolderList children = getChildrenIntern(repositoryId, folderId, filter, orderBy, includeAllowableActions,
          includeRelationships, renditionFilter, false, -1, -1, false); 
      for (ObjectInFolderData child: children.getObjects()) {
        ObjectData obj = child.getObject();
        StoredObject so = fStoreManager.getObjectStore(repositoryId).getObjectById(obj.getId());
        log.info("Checked out: children:" + obj.getId());
        if (so instanceof DocumentVersion && ((DocumentVersion)so).getParentDocument().isCheckedOut())
          odList.add(obj);
      }
    }
    res.setObjects(odList);
    res.setNumItems(BigInteger.valueOf(odList.size()));
    
    log.debug("end getCheckedOutDocs()");
    return res; 
  }

  /* (non-Javadoc)
   * @see org.opencmis.client.provider.NavigationService#getChildren(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger, org.opencmis.client.provider.ExtensionsData)
   */
  public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter,
      String orderBy, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePathSegments, BigInteger maxItems,
      BigInteger skipCount, ExtensionsData extension) {
    
    log.debug("start getChildren()");
    checkStandardParameters(repositoryId, folderId);

    int maxItemsInt = maxItems==null ? -1 : maxItems.intValue();
    int skipCountInt = skipCount==null ? -1 : skipCount.intValue();
    ObjectInFolderList res = getChildrenIntern(repositoryId, folderId, filter, orderBy, includeAllowableActions,
        includeRelationships, renditionFilter, includePathSegments, maxItemsInt, 
        skipCountInt, false);
    log.debug("stop getChildren()");
    return res;
  }
  
  /* (non-Javadoc)
   * @see org.opencmis.client.provider.NavigationService#getDescendants(java.lang.String, java.lang.String, java.math.BigInteger, java.lang.String, java.lang.Boolean, org.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.opencmis.client.provider.ExtensionsData)
   */
  public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId,
      BigInteger depth, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter,
      Boolean includePathSegments, ExtensionsData extension) {
    
    log.debug("start getDescendants()");
    checkStandardParameters(repositoryId, folderId);

    int levels;
    if (depth == null)
      levels = 2; // one of the recommended defaults (should it be -1?)
    else if (depth.intValue() == 0)
      throw new CmisInvalidArgumentException("A zero depth is not allowed for getDescendants().");
    else
      levels = depth.intValue();
    
    int level = 0;
    List<ObjectInFolderContainer> result = getDescendantsIntern(repositoryId, folderId,
            filter, includeAllowableActions, includeRelationships, renditionFilter,
            includePathSegments, level, levels, false);
    log.debug("stop getDescendants()");
    return result;
  }

  /* (non-Javadoc)
   * @see org.opencmis.client.provider.NavigationService#getFolderTree(java.lang.String, java.lang.String, java.math.BigInteger, java.lang.String, java.lang.Boolean, org.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.opencmis.client.provider.ExtensionsData)
   */
  public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId,
      BigInteger depth, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter,
      Boolean includePathSegments, ExtensionsData extension) {

    log.debug("start getFolderTree()");
    checkStandardParameters(repositoryId, folderId);

    if (depth != null && depth.intValue() == 0)
      throw new CmisInvalidArgumentException("A zero depth is not allowed for getFolderTree().");
    
    int levels = depth == null ? 2: depth.intValue();
    int level = 0;
    List<ObjectInFolderContainer> result = getDescendantsIntern(repositoryId, folderId,
            filter, includeAllowableActions, includeRelationships, renditionFilter,
            includePathSegments, level, levels, true);
    log.debug("stop getFolderTree()");
    return result;
  }
  
  /* (non-Javadoc)
   * @see org.opencmis.client.provider.NavigationService#getFolderParent(java.lang.String, java.lang.String, java.lang.String, org.opencmis.client.provider.ExtensionsData)
   */
  public ObjectData getFolderParent(String repositoryId, String folderId, String filter,
      ExtensionsData extension) {

    log.debug("start getFolderParent()");
    StoredObject so = checkStandardParameters(repositoryId, folderId);

    Folder folder = null;   
    if (so instanceof Folder)
      folder = (Folder) so;
    else
      throw new CmisInvalidArgumentException("Can't get folder parent, id does not refer to a folder: "
          + folderId);
    
    ObjectData res = getFolderParentIntern(repositoryId, folder, filter);
    log.debug("stop getFolderParent()");
    return res;
  }

  /* (non-Javadoc)
   * @see org.opencmis.client.provider.NavigationService#getObjectParents(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.opencmis.client.provider.ExtensionsData)
   */
  public List<ObjectParentData> getObjectParents(String repositoryId, String objectId,
      String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includeRelativePathSegment, ExtensionsData extension) {

    log.debug("start getObjectParents()");
    StoredObject so = checkStandardParameters(repositoryId, objectId);

    // for now we have only folders that have a parent and the in-memory provider only has one
    // parent for each object (no multi-filing)
    List<ObjectParentData> result = null;
    
    Filing spo = null;        
    if (so instanceof Filing)
      spo = (Filing) so;
    else
      throw new CmisInvalidArgumentException("Can't get object parent, id does not refer to a folder or document: "
          + objectId);
    
    result = getObjectParentsIntern(repositoryId, spo, filter);
    
    log.debug("stop getObjectParents()");
    return result;
  }

  // private helpers
  
  private ObjectInFolderList getChildrenIntern(String repositoryId, String folderId, String filter,
      String orderBy, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePathSegments, int maxItems,
      int skipCount, boolean folderOnly) {
    
    ObjectInFolderListImpl result = new ObjectInFolderListImpl();
    List<ObjectInFolderData> folderList = new ArrayList<ObjectInFolderData>();
    ObjectStore fs = fStoreManager.getObjectStore(repositoryId);
    StoredObject so = fs.getObjectById(folderId);
    Folder folder = null;

    if (so == null)
        throw new CmisObjectNotFoundException("Unknown object id: " + folderId);

    if (so instanceof Folder)
      folder = (Folder) so;
    else
      return null; // it is a document and has no children

    List<? extends StoredObject> children = folderOnly ? folder.getFolderChildren(maxItems,
        skipCount) : folder.getChildren(maxItems, skipCount);
    
    List<String> requestedIds = FilterParser.getRequestedIdsFromFilter(filter);
    
    for (StoredObject spo : children) {
        ObjectInFolderDataImpl oifd = new ObjectInFolderDataImpl();
        ObjectDataImpl objectData = new ObjectDataImpl();
        if (includePathSegments!=null && includePathSegments)
          oifd.setPathSegment(spo.getName());
        if (includeAllowableActions!=null && includeAllowableActions) {
        	AllowableActionsData allowableActions = DataObjectCreator.fillAllowableActions(fs, spo);        	
          objectData.setAllowableActions(allowableActions);
        }
        if (includeRelationships!=null && includeRelationships != IncludeRelationships.NONE) {
          objectData.setRelationships(null /*f.getRelationships()*/);
        }
        if (renditionFilter != null && renditionFilter.length() > 0) {
          objectData.setRelationships(null /*f.getRenditions(renditionFilter)*/);
        }
        
      PropertiesData props = PropertyCreationHelper.getPropertiesFromObject(repositoryId, spo,
          fStoreManager, requestedIds);
        objectData.setProperties(props);
       
        oifd.setObject(objectData);
        folderList.add(oifd);
      }
    result.setObjects(folderList);
    return result;
  }

  private List<ObjectInFolderContainer> getDescendantsIntern(String repositoryId, String folderId,
      String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter,
      Boolean includePathSegments, int level, int maxLevels, boolean folderOnly) {
  
//    log.info("getDescendantsIntern: " + folderId + ", in level " + level 
//        + ", max levels " + maxLevels);

    List<ObjectInFolderContainer> childrenOfFolderId=null;
    if (maxLevels==-1 || level < maxLevels) {
      String orderBy = PropertyIds.CMIS_NAME;
      ObjectInFolderList children = getChildrenIntern(repositoryId, folderId, filter, orderBy,
          includeAllowableActions, includeRelationships, renditionFilter, includePathSegments, 1000, 0, folderOnly);
      
      childrenOfFolderId = new ArrayList<ObjectInFolderContainer>();
      if (null != children) {        

        for (ObjectInFolderData child : children.getObjects()) {      
          ObjectInFolderContainerImpl oifc = new ObjectInFolderContainerImpl();
          String childId = child.getObject().getId();
          List<ObjectInFolderContainer> subChildren = getDescendantsIntern(repositoryId, childId,
              filter, includeAllowableActions, includeRelationships, renditionFilter,
              includePathSegments, level + 1, maxLevels, folderOnly);

          oifc.setObject(child);
          if (null != subChildren)
            oifc.setChildren(subChildren);   
          childrenOfFolderId.add(oifc);
        }
      }
    }
    return childrenOfFolderId;
  }

  private  List<ObjectParentData> getObjectParentsIntern(String repositoryId, Filing sop,
      String filter) {
    
    List<ObjectParentData> result = null;
    if (sop instanceof SingleFiling) {
      ObjectData parent = getFolderParentIntern(repositoryId, (SingleFiling)sop, filter);
      if (null != parent) {
        ObjectParentDataImpl parentData = new ObjectParentDataImpl();
        parentData.setObject(parent);
        String path = ((SingleFiling)sop).getPath();
        int beginIndex = path.lastIndexOf(Filing.PATH_SEPARATOR)+1; // Note: if / not found results in 0
        String relPathSeg = path.substring(beginIndex, path.length());
        parentData.setRelativePathSegment(relPathSeg);
        result = Collections.singletonList((ObjectParentData)parentData);
      }
      else
        result = Collections.emptyList();
    } else if (sop instanceof MultiFiling) {
      result = new ArrayList<ObjectParentData>();
      MultiFiling multiParentObj = (MultiFiling) sop;
      List<Folder> parents = multiParentObj.getParents();
      if (null != parents)
        for (Folder parent : parents) {
          ObjectParentDataImpl parentData = new ObjectParentDataImpl();
          ObjectDataImpl objData = new ObjectDataImpl();
          copyFilteredProperties(repositoryId, parent, filter, objData);
          parentData.setObject(objData);
          parentData.setRelativePathSegment(multiParentObj.getPathSegment());
          result.add(parentData);
        }      
    }
    return result;
  }
  
  private ObjectData getFolderParentIntern(String repositoryId, SingleFiling sop,
      String filter) {

    ObjectDataImpl parent = new ObjectDataImpl();

    Folder parentFolder = sop.getParent();

    if (null == parentFolder) {
      if (sop instanceof Children) // a folder without a parent
        throw new CmisInvalidArgumentException("Cannot get parent of a root folder");
      else
        return null; // an unfiled document
    }
    
    copyFilteredProperties(repositoryId, parentFolder, filter, parent);
    return parent;
  }
  
  void copyFilteredProperties(String repositoryId, StoredObject so, String filter,
      ObjectDataImpl objData) {
    List<String> requestedIds = FilterParser.getRequestedIdsFromFilter(filter);
    PropertiesData props = PropertyCreationHelper.getPropertiesFromObject(repositoryId, so,
        fStoreManager, requestedIds);
    objData.setProperties(props);
  }

}
