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
package org.apache.opencmis.commons.impl.dataobjects;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;

/**
 * Abstract type definition data implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public abstract class AbstractTypeDefinition extends AbstractExtensionData implements
    TypeDefinition, Cloneable {

  private static final long serialVersionUID = 1L;

  private String fId;
  private String fLocalName;
  private String fLocalNamespace;
  private String fQueryName;
  private String fDisplayName;
  private String fDescription;
  private BaseObjectTypeIds fBaseId;
  private String fParentId;
  private Boolean fIsCreatable;
  private Boolean fIsFileable;
  private Boolean fIsQueryable;
  private Boolean fIsIncludedInSupertypeQuery;
  private Boolean fIsFulltextIndexed;
  private Boolean fIsControllableACL;
  private Boolean fIsControllablePolicy;
  private Map<String, PropertyDefinition<?>> fPropertyDefinitions;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#getId()
   */
  public String getId() {
    return fId;
  }

  public void setId(String id) {
    fId = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#getLocalName()
   */
  public String getLocalName() {
    return fLocalName;
  }

  public void setLocalName(String localName) {
    fLocalName = localName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#getLocalNamespace()
   */
  public String getLocalNamespace() {
    return fLocalNamespace;
  }

  public void setLocalNamespace(String localNamespace) {
    fLocalNamespace = localNamespace;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#getQueryName()
   */
  public String getQueryName() {
    return fQueryName;
  }

  public void setQueryName(String queryName) {
    fQueryName = queryName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#getDisplayName()
   */
  public String getDisplayName() {
    return fDisplayName;
  }

  public void setDisplayName(String displayName) {
    fDisplayName = displayName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#getDescription()
   */
  public String getDescription() {
    return fDescription;
  }

  public void setDescription(String description) {
    fDescription = description;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#getBaseId()
   */
  public BaseObjectTypeIds getBaseId() {
    return fBaseId;
  }

  public void setBaseId(BaseObjectTypeIds baseId) {
    fBaseId = baseId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#getParentId()
   */
  public String getParentId() {
    return fParentId;
  }

  public void setParentId(String parentId) {
    fParentId = parentId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#isCreatable()
   */
  public Boolean isCreatable() {
    return fIsCreatable;
  }

  public void setIsCreatable(Boolean isCreatable) {
    fIsCreatable = isCreatable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#isFileable()
   */
  public Boolean isFileable() {
    return fIsFileable;
  }

  public void setIsFileable(Boolean isFileable) {
    fIsFileable = isFileable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#isQueryable()
   */
  public Boolean isQueryable() {
    return fIsQueryable;
  }

  public void setIsQueryable(Boolean isQueryable) {
    fIsQueryable = isQueryable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#isIncludedInSupertypeQuery()
   */
  public Boolean isIncludedInSupertypeQuery() {
    return fIsIncludedInSupertypeQuery;
  }

  public void setIsIncludedInSupertypeQuery(Boolean isIncludedInSupertypeQuery) {
    fIsIncludedInSupertypeQuery = isIncludedInSupertypeQuery;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#isFulltextIndexed()
   */
  public Boolean isFulltextIndexed() {
    return fIsFulltextIndexed;
  }

  public void setIsFulltextIndexed(Boolean isFulltextIndexed) {
    fIsFulltextIndexed = isFulltextIndexed;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#isControllableACL()
   */
  public Boolean isControllableAcl() {
    return fIsControllableACL;
  }

  public void setIsControllableAcl(Boolean isControllableACL) {
    fIsControllableACL = isControllableACL;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#isControllablePolicy()
   */
  public Boolean isControllablePolicy() {
    return fIsControllablePolicy;
  }

  public void setIsControllablePolicy(Boolean isControllablePolicy) {
    fIsControllablePolicy = isControllablePolicy;
  }

  /*
   * (non-Javadoc)
   * 
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionData#getPropertyDefintions()
   */
  public Map<String, PropertyDefinition<?>> getPropertyDefinitions() {
    return fPropertyDefinitions;
  }

  public void setPropertyDefinitions(Map<String, PropertyDefinition<?>> propertyDefinitions) {
    fPropertyDefinitions = propertyDefinitions;
  }

  /**
   * Adds a property definition.
   * 
   * @param propertyDefinition
   *          the property definition
   */
  public void addPropertyDefinition(PropertyDefinition<?> propertyDefinition) {
    if (propertyDefinition == null) {
      return;
    }

    if (fPropertyDefinitions == null) {
      fPropertyDefinitions = new LinkedHashMap<String, PropertyDefinition<?>>();
    }

    fPropertyDefinitions.put(propertyDefinition.getId(), propertyDefinition);
  }

  public AbstractTypeDefinition clone() {
    try {
      return (AbstractTypeDefinition) super.clone();
    }
    catch (CloneNotSupportedException e) {
      e.printStackTrace();
      throw new RuntimeException("Clone not supported", e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Type Definition [base id=" + fBaseId + ", id=" + fId + ", display Name=" + fDisplayName
        + ", description=" + fDescription + ", local name=" + fLocalName + ", local namespace="
        + fLocalNamespace + ", query name=" + fQueryName + ", parent id=" + fParentId
        + ", is controllable ACL=" + fIsControllableACL + ", is controllable policy="
        + fIsControllablePolicy + ", is creatable=" + fIsCreatable + ", is fileable=" + fIsFileable
        + ", is fulltext indexed=" + fIsFulltextIndexed + ", is included in supertype query="
        + fIsIncludedInSupertypeQuery + ", is queryable=" + fIsQueryable
        + ", property definitions=" + fPropertyDefinitions + "]" + super.toString();
  }
}
