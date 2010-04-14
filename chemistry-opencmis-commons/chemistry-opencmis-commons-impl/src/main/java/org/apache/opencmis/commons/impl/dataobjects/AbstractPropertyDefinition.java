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

import java.util.List;

import org.apache.opencmis.commons.api.Choice;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.enums.Cardinality;
import org.apache.opencmis.commons.enums.PropertyType;
import org.apache.opencmis.commons.enums.Updatability;

/**
 * Abstract property definition data implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public abstract class AbstractPropertyDefinition<T> extends AbstractExtensionData implements
    PropertyDefinition<T> {

  private static final long serialVersionUID = 1L;

  private String fId;
  private String fLocalName;
  private String fLocalNamespace;
  private String fQueryName;
  private String fDisplayName;
  private String fDescription;
  private PropertyType fPropertyType;
  private Cardinality fCardinality;
  private List<Choice<T>> fChoiceList;
  private List<T> fDefaultValue;
  private Updatability fUpdatability;
  private Boolean fIsInherited;
  private Boolean fIsQueryable;
  private Boolean fIsOrderable;
  private Boolean fIsRequired;
  private Boolean fIsOpenChoice;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#getId()
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
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#getLocalName()
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
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#getLocalNamespace()
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
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#getQueryName()
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
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#getDisplayName()
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
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#getDescription()
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
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#getPropertyType()
   */
  public PropertyType getPropertyType() {
    return fPropertyType;
  }

  public void setPropertyType(PropertyType propertyType) {
    fPropertyType = propertyType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#getCardinality()
   */
  public Cardinality getCardinality() {
    return fCardinality;
  }

  public void setCardinality(Cardinality cardinality) {
    fCardinality = cardinality;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#getChoices()
   */
  public List<Choice<T>> getChoices() {
    return fChoiceList;
  }

  public void setChoices(List<Choice<T>> choiceList) {
    fChoiceList = choiceList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#getDefaultValue()
   */
  public List<T> getDefaultValue() {
    return fDefaultValue;
  }

  public void setDefaultValue(List<T> defaultValue) {
    fDefaultValue = defaultValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#getUpdatability()
   */
  public Updatability getUpdatability() {
    return fUpdatability;
  }

  public void setUpdatability(Updatability updatability) {
    fUpdatability = updatability;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#isInherited()
   */
  public Boolean isInherited() {
    return fIsInherited;
  }

  public void setIsInherited(Boolean isInherited) {
    fIsInherited = isInherited;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#isQueryable()
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
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#isOrderable()
   */
  public Boolean isOrderable() {
    return fIsOrderable;
  }

  public void setIsOrderable(Boolean isOrderable) {
    fIsOrderable = isOrderable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#isRequired()
   */
  public Boolean isRequired() {
    return fIsRequired;
  }

  public void setIsRequired(Boolean isRequired) {
    fIsRequired = isRequired;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PropertyDefinitionData#isOpenChoice()
   */
  public Boolean isOpenChoice() {
    return fIsOpenChoice;
  }

  public void setIsOpenChoice(Boolean isOpenChoice) {
    fIsOpenChoice = isOpenChoice;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Property Definition [id=" + fId + ", display name=" + fDisplayName + ", description="
        + fDescription + ", local name=" + fLocalName + ", local namespace=" + fLocalNamespace
        + ", query name=" + fQueryName + ", property type=" + fPropertyType + ", cardinality="
        + fCardinality + ", choice list=" + fChoiceList + ", default value=" + fDefaultValue
        + ", is inherited=" + fIsInherited + ", is open choice=" + fIsOpenChoice
        + ", is queryable=" + fIsQueryable + ", is required=" + fIsRequired + ", updatability="
        + fUpdatability + "]" + super.toString();
  }
}
