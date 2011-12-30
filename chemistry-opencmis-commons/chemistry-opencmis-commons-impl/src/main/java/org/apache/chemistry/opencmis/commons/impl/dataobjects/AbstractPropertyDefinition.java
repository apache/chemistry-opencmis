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
package org.apache.chemistry.opencmis.commons.impl.dataobjects;

import java.util.List;

import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;

/**
 * Abstract property definition data implementation.
 */
public abstract class AbstractPropertyDefinition<T> extends AbstractExtensionData implements PropertyDefinition<T> {

    private static final long serialVersionUID = 1L;

    private String id;
    private String localName;
    private String localNamespace;
    private String queryName;
    private String displayName;
    private String description;
    private PropertyType propertyType;
    private Cardinality cardinality;
    private List<Choice<T>> choiceList;
    private List<T> defaultValue;
    private Updatability updatability;
    private Boolean isInherited;
    private Boolean isQueryable;
    private Boolean isOrderable;
    private Boolean isRequired;
    private Boolean isOpenChoice;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getLocalNamespace() {
        return localNamespace;
    }

    public void setLocalNamespace(String localNamespace) {
        this.localNamespace = localNamespace;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public void setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
    }

    public List<Choice<T>> getChoices() {
        return choiceList;
    }

    public void setChoices(List<Choice<T>> choiceList) {
        this.choiceList = choiceList;
    }

    public List<T> getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(List<T> defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Updatability getUpdatability() {
        return updatability;
    }

    public void setUpdatability(Updatability updatability) {
        this.updatability = updatability;
    }

    public Boolean isInherited() {
        return isInherited;
    }

    public void setIsInherited(Boolean isInherited) {
        this.isInherited = isInherited;
    }

    public Boolean isQueryable() {
        return isQueryable;
    }

    public void setIsQueryable(Boolean isQueryable) {
        this.isQueryable = isQueryable;
    }

    public Boolean isOrderable() {
        return isOrderable;
    }

    public void setIsOrderable(Boolean isOrderable) {
        this.isOrderable = isOrderable;
    }

    public Boolean isRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public Boolean isOpenChoice() {
        return isOpenChoice;
    }

    public void setIsOpenChoice(Boolean isOpenChoice) {
        this.isOpenChoice = isOpenChoice;
    }

    @Override
    public String toString() {
        return "Property Definition [id=" + id + ", display name=" + displayName + ", description=" + description
                + ", local name=" + localName + ", local namespace=" + localNamespace + ", query name=" + queryName
                + ", property type=" + propertyType + ", cardinality=" + cardinality + ", choice list=" + choiceList
                + ", default value=" + defaultValue + ", is inherited=" + isInherited + ", is open choice="
                + isOpenChoice + ", is queryable=" + isQueryable + ", is required=" + isRequired + ", updatability="
                + updatability + "]" + super.toString();
    }
}
