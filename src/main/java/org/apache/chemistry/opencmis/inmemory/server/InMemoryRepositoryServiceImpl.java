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
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionListImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.inmemory.TypeValidator;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.TypeManagerCreatable;

public class InMemoryRepositoryServiceImpl extends InMemoryAbstractServiceImpl {

    public InMemoryRepositoryServiceImpl(StoreManager storeManager) {
        super(storeManager);
    }

    public RepositoryInfo getRepositoryInfo(CallContext context, String repositoryId, ExtensionsData extension) {

        validator.getRepositoryInfo(context, repositoryId, extension);

        RepositoryInfo repoInfo = getRepositoryInfoFromStoreManager(repositoryId);

        return repoInfo;
    }

    public List<RepositoryInfo> getRepositoryInfos(CallContext context, ExtensionsData extension) {

        validator.getRepositoryInfos(context, extension);
        List<RepositoryInfo> res = new ArrayList<RepositoryInfo>();
        List<String> repIds = fStoreManager.getAllRepositoryIds();
        for (String repId : repIds) {
            res.add(fStoreManager.getRepositoryInfo(repId));
        }
        return res;
    }

    public TypeDefinitionList getTypeChildren(CallContext context, String repositoryId, String typeId,
            Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {

        validator.getTypeChildren(context, repositoryId, typeId, extension);

        boolean inclPropDefs = includePropertyDefinitions == null ? false : includePropertyDefinitions;
        getRepositoryInfoFromStoreManager(repositoryId); // just to check if
        // repository
        // exists

        int skip = skipCount == null ? 0 : skipCount.intValue();
        int max = maxItems == null ? -1 : maxItems.intValue();

        TypeDefinitionListImpl result = new TypeDefinitionListImpl();
        List<TypeDefinitionContainer> children;
        if (typeId == null) {
            // spec says that base types must be returned in this case
            children = fStoreManager.getRootTypes(repositoryId);
        } else {
            children = getTypeDescendants(context, repositoryId, typeId, BigInteger.valueOf(1),
                    inclPropDefs, null);
        }
        result.setNumItems(BigInteger.valueOf(children.size()));
        result.setHasMoreItems(children.size() > max - skip);
        List<TypeDefinition> childrenTypes = new ArrayList<TypeDefinition>();
        ListIterator<TypeDefinitionContainer> it = children.listIterator(skip);
        if (max < 0) {
            max = children.size();
        }
        for (int i = skip; i < max + skip && it.hasNext(); i++) {
            childrenTypes.add(it.next().getTypeDefinition());
        }

        result.setList(childrenTypes);
        return result;
    }

    public TypeDefinition getTypeDefinition(CallContext context, String repositoryId, String typeId,
            ExtensionsData extension) {

        validator.getTypeDefinition(context, repositoryId, typeId, extension);

        TypeDefinitionContainer tc = fStoreManager.getTypeById(repositoryId, typeId);
        if (tc != null) {
            return tc.getTypeDefinition();
        } else {
            throw new CmisObjectNotFoundException("unknown type id: " + typeId);
        }
    }

    public List<TypeDefinitionContainer> getTypeDescendants(CallContext context, String repositoryId, String typeId,
            BigInteger depth, Boolean includePropertyDefinitions, ExtensionsData extension) {

        validator.getTypeDescendants(context, repositoryId, typeId, extension);

        boolean inclPropDefs = includePropertyDefinitions == null ? false : includePropertyDefinitions;

        if (depth != null && depth.intValue() == 0) {
            throw new CmisInvalidArgumentException("depth == 0 is illegal in getTypeDescendants");
        }

        List<TypeDefinitionContainer> result = null;
        if (typeId == null) {
            // spec says that depth must be ignored in this case
            Collection<TypeDefinitionContainer> tmp = fStoreManager.getTypeDefinitionList(repositoryId,
                    inclPropDefs);
            result = new ArrayList<TypeDefinitionContainer>(tmp);
        } else {
            TypeDefinitionContainer tc = fStoreManager.getTypeById(repositoryId, typeId, inclPropDefs,
                    depth == null ? -1 : depth.intValue());
            if (tc == null) {
                throw new CmisInvalidArgumentException("unknown type id: " + typeId);
            } else {
                result = tc.getChildren();
            }
        }

        return result;
    }

    public void createTypeDefinition(String repositoryId, Holder<TypeDefinition> type, ExtensionsData extension) {

        if (null == repositoryId)
            throw new CmisInvalidArgumentException("Repository id may not be null");

        TypeManagerCreatable typeManager = fStoreManager.getTypeManager(repositoryId);
        if (null == typeManager)
            throw new CmisInvalidArgumentException("Unknown repository " + repositoryId);
        
        TypeDefinition td = type.getValue();
        TypeValidator.checkType(typeManager, td);
        TypeValidator.checkProperties(typeManager, td.getPropertyDefinitions().values());
        
        typeManager.addTypeDefinition(type.getValue());
    }

    public void updateTypeDefinition(String repositoryId, Holder<TypeDefinition> type, ExtensionsData extension) {
        String typeId = type.getValue().getId();
        TypeManagerCreatable typeManager = fStoreManager.getTypeManager(repositoryId);
        if (null == typeManager)
            throw new CmisInvalidArgumentException("Unknown repository " + repositoryId);
        
        TypeDefinitionContainer typeDefC = typeManager.getTypeById(typeId);
        if (null == typeDefC)
            throw new CmisInvalidArgumentException("Cannot update type unknown type id: " + typeId);

        typeManager.updateTypeDefinition(type.getValue());
    }

    public void deleteTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
        
        TypeManagerCreatable typeManager = fStoreManager.getTypeManager(repositoryId);
        if (null == typeManager)
            throw new CmisInvalidArgumentException("Unknown repository " + repositoryId);
        
        TypeDefinitionContainer typeDefC = typeManager.getTypeById(typeId);
        if (null == typeDefC)
            throw new CmisInvalidArgumentException("Cannot delete type unknown type id: " + typeId);

        TypeDefinition typeDef =  typeDefC.getTypeDefinition();
        // TODO: re-enable when CMIS 1.1 is supported.
//        if (!typeDef.getTypeMutability().supportsDelete()) {
//            throw new CmisInvalidArgumentException("type definition " + typeId + " cannot be deleted, deletion is not supported for type id " + typeId);            
//        }

        ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);
        if (objectStore.isTypeInUse(typeId)) {
            throw new CmisInvalidArgumentException("type definition " + typeId + " cannot be deleted, type is in use.");            
        }
        
        typeManager.deleteTypeDefinition(typeId);        
    }
    
    private RepositoryInfo getRepositoryInfoFromStoreManager(String repositoryId) {
        RepositoryInfo repoInfo = fStoreManager.getRepositoryInfo(repositoryId);
        if (null == repoInfo || !repoInfo.getId().equals(repositoryId)) {
            throw new CmisInvalidArgumentException("Unknown repository: " + repositoryId);
        }
        return repoInfo;
    }

}
