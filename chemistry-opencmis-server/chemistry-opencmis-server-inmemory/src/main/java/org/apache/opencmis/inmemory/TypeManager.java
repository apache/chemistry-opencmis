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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.impl.Converter;
import org.apache.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.opencmis.commons.impl.dataobjects.TypeDefinitionContainerImpl;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyDefinitionType;
import org.apache.opencmis.inmemory.types.DocumentTypeCreationHelper;
import org.apache.opencmis.inmemory.types.InMemoryDocumentTypeDefinition;
import org.apache.opencmis.inmemory.types.InMemoryFolderTypeDefinition;
import org.apache.opencmis.inmemory.types.InMemoryPolicyTypeDefinition;
import org.apache.opencmis.inmemory.types.InMemoryRelationshipTypeDefinition;

/**
 * Class that manages a type system for a repository types can be added, the inheritance can be
 * managed and type can be retrieved for a given type id.
 * 
 * @author Jens
 * 
 */
public class TypeManager {

  /**
   * map from repository id to a types map
   */
  private Map<String, TypeDefinitionContainer> fTypesMap = new HashMap<String, TypeDefinitionContainer>();

  /**
   * return a type definition from the type definition id
   * 
   * @param typeId
   *    id of the type definition
   * @return
   *    type definition for this id
   */
  public TypeDefinitionContainer getTypeById(String typeId) {
    return fTypesMap.get(typeId);
  }

  /**
   * return a list of all types known in this repository
   * @return
   */
  public synchronized Collection<TypeDefinitionContainer> getTypeDefinitionList() {
    
    List<TypeDefinitionContainer> typeRoots = new ArrayList<TypeDefinitionContainer>();
    // iterate types map and return a list collecting the root types:
    for (TypeDefinitionContainer typeDef : fTypesMap.values()) {
      if (typeDef.getTypeDefinition().getParentId()==null)
        typeRoots.add(typeDef);
    }
    
    return typeRoots;
  }

  /**
   * return a list of the root types as defined in the CMIS spec (for document,
   * folder, policy and relationship
   * 
   * @return
   */
  public List<TypeDefinitionContainer> getRootTypes() {
    // just take first repository
    List<TypeDefinitionContainer> rootTypes = new ArrayList<TypeDefinitionContainer>();

    for (TypeDefinitionContainer type : fTypesMap.values())
      if (isRootType(type))
        rootTypes.add(type);

    return rootTypes;
  }

  /**
   * Initialize the type system with the given types. This list must not contain the CMIS
   * default types. The default type are always contained by default.
   * 
   * @param typesList
   *    list of types to add to the repository
   *    
   */
  public void initTypeSystem(List<TypeDefinition> typesList) {
    
    createCmisDefaultTypes();
    
    // merge all types from the list and build the correct hierachy with children
    // and property lists
    if (null != typesList) {
      for (TypeDefinition typeDef : typesList)
        addTypeDefinition(typeDef);
    }

  }
  

  /**
   * Add a type to the type system. Add all properties from inherited types, add type to children of
   * parent types.
   * 
   * @param repositoryId
   *          repository to which the type is added
   * @param cmisType
   *          new type to add
   */
  public void addTypeDefinition(TypeDefinition cmisType) {
    if (fTypesMap.containsKey(cmisType.getId()))
      throw new RuntimeException("You cannot add type with id " + cmisType.getId()
          + " because it already exists.");

    TypeDefinitionContainerImpl typeContainer = new TypeDefinitionContainerImpl(cmisType);

    if (!fTypesMap.containsKey(cmisType.getParentId()))
      throw new RuntimeException("Cannot add type, because parent with id "
          + cmisType.getParentId() + " does not exist.");

    // add new type to children of parent types
    TypeDefinitionContainer parentTypeContainer = fTypesMap.get(cmisType.getParentId());
    parentTypeContainer.getChildren().add(typeContainer);

    // recursively add inherited properties
    Map<String, PropertyDefinition<?>> propDefs = typeContainer.getTypeDefinition()
        .getPropertyDefinitions();
    addInheritedProperties(propDefs, parentTypeContainer.getTypeDefinition());

    // add type to type map
    fTypesMap.put(cmisType.getId(), typeContainer);
  }

  /**
   * Remove all types from the type system. After this call only the default CMIS types are present
   * in the type system. Use this method with care, its mainly intended for unit tests
   * 
   * @param repositoryId
   */
  public void clearTypeSystem() {
    fTypesMap.clear();
    createCmisDefaultTypes();
  }

  private void addInheritedProperties(Map<String, PropertyDefinition<?>> propDefs,
      TypeDefinition typeDefinition) {

    if (null == typeDefinition)
      return;

    if (null != typeDefinition.getPropertyDefinitions())
      addInheritedPropertyDefinitions(propDefs, typeDefinition.getPropertyDefinitions());
      // propDefs.putAll(typeDefinition.getPropertyDefinitions());

    TypeDefinitionContainer parentTypeContainer = fTypesMap.get(typeDefinition.getParentId());
    TypeDefinition parentType = (null == parentTypeContainer ? null : parentTypeContainer
        .getTypeDefinition());
    addInheritedProperties(propDefs, parentType);
  }

  private void addInheritedPropertyDefinitions(Map<String, PropertyDefinition<?>> propDefs,
      Map<String, PropertyDefinition<?>> superPropDefs) {
    
    for (Entry<String, PropertyDefinition<?>> superProp: superPropDefs.entrySet() ) {
      PropertyDefinition<?> superPropDef = superProp.getValue();
      PropertyDefinition<?> clone = clonePropertyDefinition(superPropDef);
      ((AbstractPropertyDefinition<?>)clone).setIsInherited(true);
      propDefs.put(superProp.getKey(), clone);
    }
  }

  private void createCmisDefaultTypes() {
    List<TypeDefinition> typesList = DocumentTypeCreationHelper.createDefaultTypes();
    for (TypeDefinition typeDef : typesList) {
      TypeDefinitionContainerImpl typeContainer = new TypeDefinitionContainerImpl(typeDef);
      fTypesMap.put(typeDef.getId(), typeContainer);
    }
  }

  private static boolean isRootType(TypeDefinitionContainer c) {
    if (c.getTypeDefinition().equals(InMemoryFolderTypeDefinition.getRootFolderType())
        || c.getTypeDefinition().equals(InMemoryDocumentTypeDefinition.getRootDocumentType())
        || c.getTypeDefinition().equals(InMemoryRelationshipTypeDefinition.getRootRelationshipType())
        || c.getTypeDefinition().equals(InMemoryPolicyTypeDefinition.getRootPolicyType()))
      return true;
    else
      return false;
  }
  
  private static PropertyDefinition<?> clonePropertyDefinition(PropertyDefinition<?> src) {
    // use JAXB converter to easily clone a property definition
    CmisPropertyDefinitionType tmp = Converter.convert(src);
    PropertyDefinition<?> clone = Converter.convert(tmp);
    return clone;
  }
  
//  private static PropertyDefinition<?> clonePropertyDefinition2(PropertyDefinition<?> src)
//      throws IOException, ClassNotFoundException {
//    ByteArrayOutputStream bout = new ByteArrayOutputStream();
//    ObjectOutputStream oout = new ObjectOutputStream(bout);
//    oout.writeObject(src);
//    byte[] bytes = bout.toByteArray();
//
//    ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
//    ObjectInputStream oin = new ObjectInputStream(bin);
//    PropertyDefinition<?> clone = (PropertyDefinition<?>) oin.readObject();
//    return clone;
//  }

}
