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
package org.apache.opencmis.client.runtime.objecttype;

import java.util.List;
import java.util.Map;

import org.apache.opencmis.client.api.Session;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.util.Container;
import org.apache.opencmis.client.api.util.PagingList;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;

/**
 * Base class for object types.
 */
public abstract class AbstractObjectType implements ObjectType {

  private Session session;
  private TypeDefinition typeDefinition;
  private ObjectType baseType;
  private ObjectType parentType;

  /**
   * Initializes the object.
   */
  protected void initialize(Session session, TypeDefinition typeDefintion) {
    this.session = session;
    this.typeDefinition = typeDefintion;
  }

  /**
   * Returns the session object.
   */
  protected Session getSession() {
    return this.session;
  }

  /**
   * Returns the type definition.
   */
  protected TypeDefinition getTypeDefinition() {
    return this.typeDefinition;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#getBaseType()
   */
  public ObjectType getBaseType() {
    if (isBaseType()) {
      return null;
    }

    if (this.baseType != null) {
      return this.baseType;
    }

    if (getBaseTypeId() == null) {
      return null;
    }

    this.baseType = getSession().getTypeDefinition(getBaseTypeId().value());

    return this.baseType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#getBaseTypeId()
   */
  public BaseObjectTypeIds getBaseTypeId() {
    return getTypeDefinition().getBaseId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#getDescription()
   */
  public String getDescription() {
    return getTypeDefinition().getDescription();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#getDisplayName()
   */
  public String getDisplayName() {
    return getTypeDefinition().getDisplayName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#getId()
   */
  public String getId() {
    return getTypeDefinition().getId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#getLocalName()
   */
  public String getLocalName() {
    return getTypeDefinition().getLocalName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#getLocalNamespace()
   */
  public String getLocalNamespace() {
    return getTypeDefinition().getLocalNamespace();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#getParent()
   */
  public ObjectType getParent() {
    if (this.parentType != null) {
      return this.parentType;
    }

    if (getTypeDefinition().getParentId() == null) {
      return null;
    }

    this.parentType = getSession().getTypeDefinition(getTypeDefinition().getParentId());

    return this.parentType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#getPropertyDefintions()
   */
  public Map<String, PropertyDefinition<?>> getPropertyDefintions() {
    return typeDefinition.getPropertyDefinitions();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#getQueryName()
   */
  public String getQueryName() {
    return getTypeDefinition().getQueryName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#isBaseType()
   */
  public boolean isBaseType() {
    return (getTypeDefinition().getParentId() == null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#isControllableAcl()
   */
  public Boolean isControllableAcl() {
    return getTypeDefinition().isControllableAcl();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#isControllablePolicy()
   */
  public Boolean isControllablePolicy() {
    return getTypeDefinition().isControllablePolicy();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#isCreatable()
   */
  public Boolean isCreatable() {
    return getTypeDefinition().isCreatable();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#isFileable()
   */
  public Boolean isFileable() {
    return getTypeDefinition().isFileable();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#isFulltextIndexed()
   */
  public Boolean isFulltextIndexed() {
    return getTypeDefinition().isFulltextIndexed();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#isIncludedInSupertypeQuery()
   */
  public Boolean isIncludedInSupertypeQuery() {
    return getTypeDefinition().isIncludedInSupertypeQuery();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#isQueryable()
   */
  public Boolean isQueryable() {
    return getTypeDefinition().isQueryable();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#getChildren(int)
   */
  public PagingList<ObjectType> getChildren(int itemsPerPage) {
    return getSession().getTypeChildren(getId(), true, itemsPerPage);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.ObjectType#getDescendants(int)
   */
  public List<Container<ObjectType>> getDescendants(int depth) {
    return getSession().getTypeDescendants(getId(), depth, true);
  }
}
