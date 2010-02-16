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
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.api.TypeDefinitionList;
import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.apache.opencmis.commons.impl.dataobjects.TypeDefinitionContainerImpl;
import org.apache.opencmis.commons.impl.dataobjects.TypeDefinitionListImpl;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.commons.provider.RepositoryService;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;

/**
 * InMemory Repository Service.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RepositoryServiceImpl extends AbstractServiceImpl implements RepositoryService {

  /**
   * Constructor.
   */
  public RepositoryServiceImpl(StoreManager storeManager) {
    super (storeManager);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getRepositoryInfo(java.lang.String,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public RepositoryInfoData getRepositoryInfo(String repositoryId, ExtensionsData extension) {
    RepositoryInfoData repoInfo = getRepositoryInfoFromStoreManager(repositoryId);

    return repoInfo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getRepositoryInfos(boolean,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public List<RepositoryInfoData> getRepositoryInfos(ExtensionsData extension) {
    List<RepositoryInfoData> res = new ArrayList<RepositoryInfoData>();
    List<String> repIds = fStoreManager.getAllRepositoryIds();
    for (String repId : repIds) {
      res.add(fStoreManager.getRepositoryInfo(repId));
    }
    return res;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getTypeChildren(java.lang.String,
   * java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public TypeDefinitionList getTypeChildren(String repositoryId, String typeId,
      Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension) {

    getRepositoryInfoFromStoreManager(repositoryId); // just to check if repository exists

    int skip = skipCount == null ? 0 : skipCount.intValue();
    int max = maxItems == null ? -1 : maxItems.intValue();
      
    TypeDefinitionListImpl result = new TypeDefinitionListImpl();
    List<TypeDefinitionContainer> children = getTypeDescendants(repositoryId, typeId, 
        BigInteger.valueOf(1), includePropertyDefinitions, null);
    result.setNumItems(BigInteger.valueOf(children.size()));
    result.setHasMoreItems(children.size() > max - skip);
    List<TypeDefinition> childrenTypes = new ArrayList<TypeDefinition>();
    ListIterator<TypeDefinitionContainer> it = children.listIterator(skip);
    if (max<0)
      max = children.size();
    for (int i=0; i<max && it.hasNext(); i++)
      childrenTypes.add(it.next().getTypeDefinition());

    result.setList(childrenTypes);      
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getTypeDefinition(java.lang.String,
   * java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public TypeDefinition getTypeDefinition(String repositoryId, String typeId,
      ExtensionsData extension) {
    
    getRepositoryInfoFromStoreManager(repositoryId); // just to check if repository exists

    TypeDefinitionContainer tc = fStoreManager.getTypeById(repositoryId, typeId);
    if (tc != null) {
      return tc.getTypeDefinition();
    }
    else
      throw new CmisObjectNotFoundException("unknown type id: " + typeId);        
 }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getTypeDescendants(java.lang.String,
   * java.lang.String, java.math.BigInteger, java.lang.Boolean,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId,
      BigInteger depth, Boolean includePropertyDefinitions, ExtensionsData extension) {
    
    getRepositoryInfoFromStoreManager(repositoryId); // just to check if repository exists

    if (depth != null && depth.intValue() == 0)
      throw new CmisInvalidArgumentException("depth == 0 is illegal in getTypeDescendants");
    
    List<TypeDefinitionContainer> result =  null;
    if (typeId == null) {
      // spec says that depth must be ignored in this case
        Collection<TypeDefinitionContainer> typeColl = fStoreManager.getTypeDefinitionList(repositoryId);
        result = new ArrayList<TypeDefinitionContainer>(typeColl);
    }
    else {
      TypeDefinitionContainer tc = fStoreManager.getTypeById(repositoryId, typeId);
      if (tc != null) {
        if (null==depth || depth.intValue() == -1)
          result = tc.getChildren();
        else {
          result = getLimitedChildrenTypeList(depth.intValue(), tc.getChildren());
        }
      }
      else
        throw new CmisInvalidArgumentException("unknown type id: " + typeId);        
    }
    
    if (!includePropertyDefinitions) {
      // copy list and omit properties
      List<TypeDefinitionContainer> newResult = new ArrayList<TypeDefinitionContainer>(result.size());
      for (TypeDefinitionContainer c : result) {
        AbstractTypeDefinition td = ((AbstractTypeDefinition)c.getTypeDefinition()).clone();
        TypeDefinitionContainerImpl tdc = new TypeDefinitionContainerImpl(td);
        tdc.setChildren(c.getChildren());
        td.setPropertyDefinitions(null);
        newResult.add(tdc);
      }
      result = newResult;
    }
    return result;
  }

 
  private List<TypeDefinitionContainer> getLimitedChildrenTypeList(int depth,
      List<TypeDefinitionContainer> types) {
    List<TypeDefinitionContainer> result = new ArrayList<TypeDefinitionContainer>();

    for (TypeDefinitionContainer type : types) {
      result.addAll(getLimitedChildrenTypeList(depth, type));
    }
    return result;
  }

  private List<TypeDefinitionContainer> getLimitedChildrenTypeList(int depth,
      TypeDefinitionContainer root) {
    List<TypeDefinitionContainer> typeDescs = new ArrayList<TypeDefinitionContainer>();
    if (depth > 0 && root != null) {
      typeDescs.add(root);
      for (TypeDefinitionContainer c : root.getChildren()) {
        typeDescs.addAll(getLimitedChildrenTypeList(depth - 1, c));
      }
    }
    return typeDescs;
  }

  private RepositoryInfoData getRepositoryInfoFromStoreManager(String repositoryId ) {
    RepositoryInfoData repoInfo = fStoreManager.getRepositoryInfo(repositoryId);
    if (null == repoInfo || !repoInfo.getRepositoryId().equals(repositoryId)) {
      throw new CmisInvalidArgumentException("Unknown repository: " + repositoryId);
    }
    return repoInfo;
  }

}
