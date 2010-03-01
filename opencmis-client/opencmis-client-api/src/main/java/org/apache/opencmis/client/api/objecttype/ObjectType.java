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
package org.apache.opencmis.client.api.objecttype;

import java.util.List;
import java.util.Map;

import org.apache.opencmis.client.api.util.Container;
import org.apache.opencmis.client.api.util.PagingList;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;

/**
 * Object Type.
 * 
 * See CMIS Domain Model - section 2.1.3.
 */
public interface ObjectType {

  String DOCUMENT_BASETYPE_ID = BaseObjectTypeIds.CMIS_DOCUMENT.value();
  String FOLDER_BASETYPE_ID = BaseObjectTypeIds.CMIS_FOLDER.value();
  String RELATIONSHIP_BASETYPE_ID = BaseObjectTypeIds.CMIS_RELATIONSHIP.value();
  String POLICY_BASETYPE_ID = BaseObjectTypeIds.CMIS_POLICY.value();

  /**
   * Get this type's id.
   * 
   * @return the id of this type.
   */
  String getId();

  /**
   * Get the type's local name as used in the repository.
   * 
   * @return the local name of the type.
   */
  String getLocalName();

  /**
   * Get the type's namespace as used in the repository.
   * 
   * @return the namespace for this type.
   */
  String getLocalNamespace();

  /**
   * Get the type's query name, used for query and filter operations.
   * 
   * @return the type's query name.
   */
  String getQueryName();

  /**
   * Get the type's display name, used to be presented to the user.
   * 
   * @return the type's display name.
   */
  String getDisplayName();

  /**
   * Indicates if this is base object type (i.e. if {@code getId()} returns ...{@code _BASETYPE_ID}.
   * 
   * @return {@code true} if this type is a base type, {@code false} if this type is a derived type.
   */
  boolean isBase();

  /**
   * Get the type's base type, if the type is a derived (non-base) type.
   * 
   * @return the base type this type is derived from, or {@code null} if it is a base type ({@code
   *         isBase()==true}).
   * @throws CmisRuntimeException
   */
  ObjectType getBaseType(); // null if isBase == true

  /**
   * Get the type's base type id.
   * 
   * @return
   */
  BaseObjectTypeIds getBaseTypeId();

  /**
   * Get the type's parent type, if the type is a derived (non-base) type.
   * 
   * @return the parent type from which this type is derived, or {@code null} if it is a base type (
   *         {@code isBase()==true}).
   * @throws CmisRuntimeException
   */
  ObjectType getParent();

  /**
   * Additional description for this type.
   * 
   * @return the description, intended for the user. Might be {@code null}.
   */
  String getDescription();

  /**
   * Indicates that objects of this type can be created.
   * 
   * @return {@code true} if objects for this type can be created.
   */
  Boolean isCreatable();

  /**
   * Indicates that objects of this type can be filed/unfiled.
   * 
   * @return {@code true} if objects for this type can be used for filing operations.
   */
  Boolean isFileable();

  /**
   * Indicates that objects of this type can be queried.
   * 
   * @return {@code true} if objects for this type can be used for query operations.
   */
  Boolean isQueryable();

  /**
   * Indicates that objects of this type can be controlled by policies.
   * 
   * @return {@code true} if objects for this type can be used for policy operations.
   */
  Boolean isControllablePolicy();

  /**
   * Indicates that objects of this type can be controlled by ACLs.
   * 
   * @return {@code true} if objects for this type can be used for ACL operations.
   */
  Boolean isControllableAcl();

  /**
   * Indicates that objects of this type are indexed in the full text index.
   * 
   * @return {@code true} if objects for this type can be searched via full text searches.
   */
  Boolean isFulltextIndexed();

  /**
   * Indicates that objects of this type can be queried via its super type.
   * 
   * @return {@code true} if objects for this type are included in queries for their super type.
   */
  Boolean isIncludedInSupertypeQuery();

  /**
   * Get the {@code Map} of {@code PropertyDefinition}s, indexed by the property definition's ids.
   * 
   * @return the {@code Map} of property definitions. @
   */
  Map<String, PropertyDefinition<?>> getPropertyDefintions();

  /**
   * Get the list of types directly derived from this type (which will return this type on {@code
   * getParent()}).
   * 
   * @param itemsPerPage
   *          types per page
   * @return a {@code List} of types which are directly derived from this type. @
   */
  PagingList<ObjectType> getChildren(int itemsPerPage);

  /**
   * Get the list of all types somehow derived from this type.
   * 
   * @param depth
   *          the depth to which the derived types should be resolved.
   * @return a {@code Tree} of types which are derived from this type (direct and via their
   *         parents). @
   */
  List<Container<ObjectType>> getDescendants(int depth);

}
