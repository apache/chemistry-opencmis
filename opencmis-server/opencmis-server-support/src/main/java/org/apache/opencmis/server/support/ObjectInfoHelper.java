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
package org.apache.opencmis.server.support;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionList;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.enums.CapabilityAcl;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectInFolderContainer;
import org.apache.opencmis.commons.provider.ObjectInFolderData;
import org.apache.opencmis.commons.provider.ObjectInFolderList;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.commons.provider.ObjectParentData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.RenditionData;
import org.apache.opencmis.commons.provider.RepositoryCapabilitiesData;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisObjectService;
import org.apache.opencmis.server.spi.CmisRepositoryService;
import org.apache.opencmis.server.spi.CmisNavigationService;
import org.apache.opencmis.server.spi.ObjectInfoHolder;
import org.apache.opencmis.server.spi.ObjectInfoImpl;
import org.apache.opencmis.server.spi.RenditionInfo;
import org.apache.opencmis.server.spi.RenditionInfosImpl;


/**
 * A helper class used from various methods to fill the ObjectInfoHolder structure with 
 * the required information to generate all the links in the AtomPub binding. Use this 
 * class as a convenience class for starting the implementation. For productive use it
 * is highly recommended that a server implementation replaces this implementation by
 * a more efficient one. This default implementation can only rely on the services and
 * therefore requires a round-trip for each object to the server once again. This can 
 * be avoided in a repository specific implementation.
 * 
 */

public class ObjectInfoHelper
{
    private CmisObjectService _objSvc;
    private CmisRepositoryService _repSvc;
    private CmisNavigationService _navSvc;
    
    private Map<String, RepositoryCapabilitiesData > _repos = new HashMap<String, RepositoryCapabilitiesData >();
    private Map<String, Boolean > _mapPolicies = new HashMap<String, Boolean >();
    private Map<String, Boolean > _mapRelationships = new HashMap<String, Boolean >();
    
    public ObjectInfoHelper(CmisRepositoryService repSvc, CmisObjectService objSvc, CmisNavigationService navSvc) {
        _objSvc = objSvc;
        _repSvc = repSvc;
        _navSvc = navSvc;
    }
    
    /**
     * fill an ObjectInfoHolder object with required information needed for Atom binding 
     * to be able to generate the necessary links in AtomPub
     * 
     * @param context
     *          call context of the current request
     * @param repositoryId
     *          id of repository 
     * @param objectId
     *          object to retrieve information for
     * @param objectInfos
     *          Holder to fill with information
     */
    public ObjectData fillObjectInfoHolder(CallContext context, String repositoryId, String objectId,
        ObjectInfoHolder objectInfos) {
        
      if (null == objectInfos || null == objectId)
        return null;
      
      // call getObject to get the required information to fill ObjectInfoHolder
      ObjectData objData = getObject(context, repositoryId, objectId);
      fillObjectInfoHolder(context, repositoryId, objData, objectInfos);
      
      return objData; // might be useful as return value in some service methods
    }
    
    /**
     * Fill object in
     * @param context
     * @param repositoryId
     * @param objData
     * @param filter
     * @param objectInfos
     */
    public void fillObjectInfoHolder(CallContext context, String repositoryId, ObjectData objData,
        String filter, ObjectInfoHolder objectInfos) {
        // fill objectInfos
        if (filterContainsRequiredProperties(filter))
            fillObjectInfoHolder(context, repositoryId, objData, objectInfos);
        else // get object again as we need almost all system properties
            fillObjectInfoHolder(context, repositoryId, objData.getId(), objectInfos);        
    }
    
    public boolean filterContainsRequiredProperties(String filter) {
        if (filter==null)
            return false;
        if (filter.equals("*"))
            return true;
        if (!filter.contains(PropertyIds.CMIS_NAME))
            return false;
        if (!filter.contains(PropertyIds.CMIS_CREATED_BY))
            return false;
        if (!filter.contains(PropertyIds.CMIS_CREATION_DATE))
            return false;
        if (!filter.contains(PropertyIds.CMIS_LAST_MODIFICATION_DATE))
            return false;
        if (!filter.contains(PropertyIds.CMIS_OBJECT_TYPE_ID))
            return false;
        if (!filter.contains(PropertyIds.CMIS_BASE_TYPE_ID))
            return false;
        if (!filter.contains(PropertyIds.CMIS_CONTENT_STREAM_FILE_NAME))
            return false;
        if (!filter.contains(PropertyIds.CMIS_CONTENT_STREAM_MIME_TYPE))
            return false;
        if (!filter.contains(PropertyIds.CMIS_CONTENT_STREAM_ID))
            return false;
        return true;
        
    }
    /**
     * fill an ObjectInfoHolder object with required information needed for Atom binding 
     * to be able to generate the necessary links in AtomPub
     * 
     * @param context
     *          call context of the current request
     * @param repositoryId
     *          id of repository 
     * @param objData
     *          object data to grab information from
     * @param objectInfos
     *          Holder to fill with information
     */
    public void fillObjectInfoHolder(CallContext context, String repositoryId, ObjectData objData,
        ObjectInfoHolder objectInfos) {
        
        if (null==objData || null==objectInfos)
            return;
                
        // Get required information about the repository and cache it for later use:
        
        Map<String, PropertyData<?>> properties = objData.getProperties().getProperties();
        RepositoryCapabilitiesData repoCaps = _repos.get(repositoryId);
        if (null == repoCaps) {
            RepositoryInfoData repoInfo = _repSvc.getRepositoryInfo(null, repositoryId, null);
            repoCaps = repoInfo.getRepositoryCapabilities();
            _repos.put(repositoryId, repoCaps);          
        }
        
        Boolean supportsRelationships = _mapRelationships.get(repositoryId);
        Boolean supportsPolicies = _mapPolicies.get(repositoryId);
        if (null == supportsRelationships || null == supportsPolicies) {
            supportsPolicies = supportsRelationships = false;
            TypeDefinitionList children = _repSvc.getTypeChildren(context, repositoryId, null, false,
                BigInteger.valueOf(100), BigInteger.ZERO, null);
            for (TypeDefinition typeDefinition : children.getList()) {
                if (typeDefinition.getId().equals(BaseObjectTypeIds.CMIS_RELATIONSHIP))
                    supportsRelationships = true;
                if (typeDefinition.getId().equals(BaseObjectTypeIds.CMIS_POLICY))
                    supportsPolicies = true;
            }
            _mapRelationships.put(repositoryId, supportsRelationships);
            _mapPolicies.put(repositoryId, supportsPolicies);
        }
        
        ObjectInfoImpl objInfo = new ObjectInfoImpl();
       // Fill all setters:
        objInfo.setId(objData.getId());
        objInfo.setName(getStringProperty(properties, PropertyIds.CMIS_NAME));
        objInfo.setCreatedBy(getStringProperty(properties, PropertyIds.CMIS_CREATED_BY)); 
        objInfo.setCreationDate(getDateProperty(properties, PropertyIds.CMIS_CREATION_DATE)); 
        objInfo.setLastModificationDate(getDateProperty(properties, PropertyIds.CMIS_LAST_MODIFICATION_DATE));
        objInfo.setTypeId(getStringProperty(properties, PropertyIds.CMIS_OBJECT_TYPE_ID));
        String baseId = getStringProperty(properties, PropertyIds.CMIS_BASE_TYPE_ID);
        objInfo.setBaseType(BaseObjectTypeIds.fromValue(baseId));
        
        boolean isVersioned = getStringProperty(properties, PropertyIds.CMIS_VERSION_SERIES_ID) != null;
        // versioning information: 
        if (isVersioned) {          
          objInfo.setIsCurrentVersion(getBooleanProperty(properties, PropertyIds.CMIS_IS_LATEST_VERSION)); 
          objInfo.setVersionSeriesId(getStringProperty(properties, PropertyIds.CMIS_VERSION_SERIES_ID));
          objInfo.setWorkingCopyId(getStringProperty(properties, PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_ID));
          objInfo.setWorkingCopyOriginalId(null);
        } else { // unversioned document
          objInfo.setIsCurrentVersion (true); 
          objInfo.setVersionSeriesId(null);
          objInfo.setWorkingCopyId(null);
          objInfo.setWorkingCopyOriginalId(null);
        }
        
        String fileName = getStringProperty(properties, PropertyIds.CMIS_CONTENT_STREAM_FILE_NAME);
        String mimeType = getStringProperty(properties, PropertyIds.CMIS_CONTENT_STREAM_MIME_TYPE);
        String streamId = getStringProperty(properties, PropertyIds.CMIS_CONTENT_STREAM_ID);
        BigInteger length = getIntegerProperty(properties, PropertyIds.CMIS_CONTENT_STREAM_LENGTH);
        boolean hasContent = fileName != null || mimeType != null || streamId != null || length != null;
        if (hasContent) {
          objInfo.setHasContent(hasContent);
          objInfo.setContentType(mimeType);
          objInfo.setFileName(fileName);
        } else {
          objInfo.setHasContent(false);
          objInfo.setContentType(null);
          objInfo.setFileName(null);
        }
        
        if (objInfo.getBaseType() == BaseObjectTypeIds.CMIS_FOLDER)
            objInfo.setHasParent(getStringProperty(properties, PropertyIds.CMIS_PARENT_ID) != null);
        else if (objInfo.getBaseType() == BaseObjectTypeIds.CMIS_DOCUMENT) {
            if (repoCaps.supportsUnfiling())
                objInfo.setHasParent(documentHasParent(context, repositoryId, objData.getId()));
            else
                objInfo.setHasParent(true);
        } else
            objInfo.setHasParent(false);            
        
        // Renditions, currently not supported by in-memory provider
        objInfo.setRenditionInfos(convertRenditions(objData.getRenditions()));
        
        List<String> sourceIds = new ArrayList<String>();
        List<String> targetIds = new ArrayList<String>();
        getRelationshipIds(objData, sourceIds, targetIds);
        
        // Relationships, currently not supported
        objInfo.setSupportsRelationships(supportsRelationships);
        objInfo.setRelationshipSourceIds(sourceIds);
        objInfo.setRelationshipTargetIds(targetIds);
        
        objInfo.setSupportsPolicies(supportsPolicies);
        
        objInfo.setHasAcl(repoCaps.getCapabilityAcl() != CapabilityAcl.NONE);
        
        String baseTypeId = getStringProperty(properties, PropertyIds.CMIS_BASE_TYPE_ID);
        boolean isFolder = baseTypeId != null && baseTypeId.equals(BaseObjectTypeIds.CMIS_FOLDER.value());
        
        objInfo.setSupportsDescendants(isFolder && repoCaps.supportsGetDescendants());;
        objInfo.setSupportsFolderTree(isFolder && repoCaps.supportsGetFolderTree());
        
        objectInfos.addObjectInfo(objInfo);
    }

        
        

    /**
     * fill an ObjectInfoHolder object with required information needed for Atom binding 
     * to be able to generate the necessary links in AtomPub
     * 
     * @param context
     *          call context of the current request
     * @param repositoryId
     *          id of repository 
     * @param objList
     *          object list, fill information for each element
     * @param objectInfos
     *          Holder to fill with information
     */
    public void fillObjectInfoHolder(
        CallContext context,
        String repositoryId,
        ObjectList objList,
        ObjectInfoHolder objectInfos)
    {
          
        if (null != objectInfos && null != objList && null != objList.getObjects()) {
            // Fill object information for all children in result list
            List<ObjectData> listObjects = objList.getObjects();
            if (null != listObjects)
                for (ObjectData object : listObjects) {
                    fillObjectInfoHolder(context, repositoryId, object.getId(), objectInfos);
                }    
        }
    }

    /**
     * fill an ObjectInfoHolder object with required information needed for Atom binding 
     * to be able to generate the necessary links in AtomPub
     * 
     * @param context
     *          call context of the current request
     * @param repositoryId
     *          id of repository 
     * @param objList
     *          object list, fill information for each element
     * @param objectInfos
     *          Holder to fill with information
     */
    public void fillObjectInfoHolder(
        CallContext context,
        String repositoryId,
        List<ObjectData> objList,
        ObjectInfoHolder objectInfos)
    {
        if (null == objectInfos || null == objList)
            return;

        // Fill object information for all children in result list
        for (ObjectData object : objList) {
            fillObjectInfoHolder(context, repositoryId, object.getId(), objectInfos);
        }
    }

    /**
     * fill an ObjectInfoHolder object with required information needed for Atom binding 
     * to be able to generate the necessary links in AtomPub
     * 
     * @param context
     *          call context of the current request
     * @param repositoryId
     *          id of repository 
     * @param objList
     *          object list, fill information for each element
     * @param objectInfos
     *          Holder to fill with information
     */
    public void fillObjectInfoHolder(
        CallContext context,
        String repositoryId,
        ObjectInFolderList objList,
        ObjectInfoHolder objectInfos)
    {
        if (null == objectInfos || null == objList)
            return;

        // Fill object information for all children in result list
        for (ObjectInFolderData object : objList.getObjects()) {
            fillObjectInfoHolder(context, repositoryId, object.getObject().getId(), objectInfos);
        }
    }

    /**
     * fill an ObjectInfoHolder object with required information needed for Atom binding 
     * to be able to generate the necessary links in AtomPub
     * 
     * @param context
     *          call context of the current request
     * @param repositoryId
     *          id of repository 
     * @param objList
     *          object list, fill information for each element
     * @param objectInfos
     *          Holder to fill with information
     */
    public void fillObjectInfoHolderObjectParentData(
        CallContext context,
        String repositoryId,
        List<ObjectParentData> objParents,
        ObjectInfoHolder objectInfos)
    {
        if (null == objectInfos || null == objParents)
            return;

        for (ObjectParentData object : objParents) {
            fillObjectInfoHolder(context, repositoryId, object.getObject().getId(), objectInfos);
        }        
    }
        
    /**
     * fill an ObjectInfoHolder object with required information needed for Atom binding 
     * to be able to generate the necessary links in AtomPub
     * 
     * @param context
     *          call context of the current request
     * @param repositoryId
     *          id of repository 
     * @param objList
     *          object list, fill information for each element recursively
     * @param objectInfos
     *          Holder to fill with information
     */
    public void fillObjectInfoHolderFolderContainer(
        CallContext context,
        String repositoryId,
        List<ObjectInFolderContainer> oifcList,
        ObjectInfoHolder objectInfos)
    {
        if (null == objectInfos || null == oifcList)
            return;

          
        for (ObjectInFolderContainer object : oifcList) {
            fillObjectInfoHolderFolderContainer(context, repositoryId, object, objectInfos);
        }    
    }
    
    private void fillObjectInfoHolderFolderContainer(
        CallContext context,
        String repositoryId,
        ObjectInFolderContainer oifc,
        ObjectInfoHolder objectInfos)
    {
        if (null == objectInfos || null == oifc)
            return;

        fillObjectInfoHolder(context, repositoryId, oifc.getObject().getObject(), objectInfos);

        if (null!=oifc.getChildren())
            for (ObjectInFolderContainer object : oifc.getChildren()) {
                // call recursively
                fillObjectInfoHolderFolderContainer(context, repositoryId, object, objectInfos);
            }    
    }

    private Boolean getBooleanProperty(Map<String, PropertyData<?>> props, String key) {
        PropertyData<?> pdVal = props.get(key);
        Boolean val = null==pdVal ? null : (Boolean) pdVal.getFirstValue();
        return val;
    }

    private String getStringProperty(Map<String, PropertyData<?>> props, String key) {
        PropertyData<?> pdVal = props.get(key);
        String val = null==pdVal ? null : (String) pdVal.getFirstValue();
        return val;
    }
    
    private GregorianCalendar getDateProperty(Map<String, PropertyData<?>> props, String key) {
        PropertyData<?> pdVal = props.get(key);
        GregorianCalendar val = null==pdVal ? null : (GregorianCalendar) pdVal.getFirstValue();
        return val;
    }

    private BigInteger getIntegerProperty(Map<String, PropertyData<?>> props, String key) {
        PropertyData<?> pdVal = props.get(key);
        BigInteger val = null==pdVal ? null : (BigInteger) pdVal.getFirstValue();
        return val;
    }

    private ObjectData getObject(CallContext context, String repositoryId, String objectId) {
        
        ObjectData od = _objSvc.getObject(context, repositoryId, objectId, null, false, 
            IncludeRelationships.BOTH, "*", true, true, null, null);
        return od;
    }

    private List<RenditionInfo> convertRenditions(List<RenditionData> renditions) {
        
        if (null==renditions)
            return null;
        
        List<RenditionInfo> rendInfos = new ArrayList<RenditionInfo>(renditions.size());
        RenditionInfosImpl ri = new RenditionInfosImpl();
        for (RenditionData rd : renditions) {
            ri.setContentType(rd.getMimeType());
            ri.setId(rd.getStreamId());
            ri.setKind(rd.getKind());
            ri.setLength(rd.getLength());
            ri.setTitle(rd.getTitle());
            rendInfos.add(ri);            
        }
        return rendInfos;
    }
    
    private void getRelationshipIds(ObjectData objData, List<String> sourceIds, List<String> targetIds) {
        if (null==objData || null == objData.getRelationships())
            return;
        
        String objectId = objData.getId();
        for (ObjectData rel : objData.getRelationships()) {
            String relId = getStringProperty(rel.getProperties().getProperties(), PropertyIds.CMIS_OBJECT_ID);
            String sourceId = getStringProperty(rel.getProperties().getProperties(), PropertyIds.CMIS_SOURCE_ID);
            String targetId = getStringProperty(rel.getProperties().getProperties(), PropertyIds.CMIS_TARGET_ID);
            if (objectId.equals(sourceId))
                sourceIds.add(relId);
            if (objectId.equals(targetId))
                targetIds.add(relId);
        }
    }

    private boolean documentHasParent(CallContext context, String repositoryId, String objectId)
    {
        List<ObjectParentData> opd = _navSvc.getObjectParents(context, repositoryId, objectId, null, false, IncludeRelationships.NONE, 
            null, false, null, null);

        return opd!= null && opd.size()>0;
    }

}

