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
package org.apache.chemistry.opencmis.commons.definitions;

import java.io.Serializable;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;

/**
 * Base property definition interface.
 * 
 * @cmis 1.0
 */
public interface PropertyDefinition<T> extends Serializable, ExtensionsData {

    /**
     * Returns the property definition id.
     * 
     * @return the property definition id
     * 
     * @cmis 1.0
     */
    String getId();

    /**
     * Returns the local name.
     * 
     * @return the local name
     * 
     * @cmis 1.0
     */
    String getLocalName();

    /**
     * Returns the local namespace.
     * 
     * @return the local namespace
     * 
     * @cmis 1.0
     */
    String getLocalNamespace();

    /**
     * Returns the display name.
     * 
     * @return the display name
     * 
     * @cmis 1.0
     */
    String getDisplayName();

    /**
     * Returns the query name
     * 
     * @return the query name
     * 
     * @cmis 1.0
     */
    String getQueryName();

    /**
     * Returns the property description.
     * 
     * @return returns the description
     * 
     * @cmis 1.0
     */
    String getDescription();

    /**
     * Returns the property type.
     * 
     * @return the property type
     * 
     * @cmis 1.0
     */
    PropertyType getPropertyType();

    /**
     * Returns the cardinality.
     * 
     * @return the cardinality
     * 
     * @cmis 1.0
     */
    Cardinality getCardinality();

    /**
     * Returns the updatability.
     * 
     * @return the updatability
     * 
     * @cmis 1.0
     */
    Updatability getUpdatability();

    /**
     * Returns if the property is inherited by a parent type.
     * 
     * @return <code>true</code> - is inherited;
     *         <code>false</code> - is not inherited; <code>null</code> -
     *         unknown (noncompliant repository)
     * 
     * @cmis 1.0
     */
    Boolean isInherited();

    /**
     * Returns if the property is required.
     * 
     * @return <code>true</code> - is required;
     *         <code>false</code> - is not required; <code>null</code> -
     *         unknown (noncompliant repository)
     * 
     * @cmis 1.0
     */
    Boolean isRequired();

    /**
     * Returns if the property is queryable.
     * 
     * @return <code>true</code> - is queryable;
     *         <code>false</code> - is not queryable; <code>null</code> -
     *         unknown (noncompliant repository)
     * 
     * @cmis 1.0
     */
    Boolean isQueryable();

    /**
     * Returns if the property is Orderable.
     * 
     * @return <code>true</code> - is Orderable;
     *         <code>false</code> - is not Orderable; <code>null</code> -
     *         unknown (noncompliant repository)
     * 
     * @cmis 1.0
     */
    Boolean isOrderable();

    /**
     * Returns if the property supports open choice.
     * 
     * @return <code>true</code> - supports open choice;
     *         <code>false</code> - does not support open choice; <code>null</code>
     *         - unknown or not applicable
     * 
     * @cmis 1.0
     */
    Boolean isOpenChoice();

    /**
     * Returns the default value.
     * 
     * @return the default value (list) or <code>null</code> if no default value
     *         is defined
     * 
     * @cmis 1.0
     */
    List<T> getDefaultValue();

    /**
     * Returns the choices for this property.
     * 
     * @return the choices or <code>null</code> if no choices are defined
     * 
     * @cmis 1.0
     */
    List<Choice<T>> getChoices();
}
