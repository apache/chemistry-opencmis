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
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public interface PropertyDefinition<T> extends Serializable, ExtensionsData {

    /**
     * Returns the property definition id.
     * 
     * @return the property definition id
     */
    String getId();

    /**
     * Returns the local name.
     * 
     * @return the local name
     */
    String getLocalName();

    /**
     * Returns the local namespace.
     * 
     * @return the local namespace
     */
    String getLocalNamespace();

    /**
     * Returns the display name.
     * 
     * @return the display name
     */
    String getDisplayName();

    /**
     * Returns the query name
     * 
     * @return the query name
     */
    String getQueryName();

    /**
     * Returns the property description.
     * 
     * @return returns the description
     */
    String getDescription();

    /**
     * Returns the property type.
     * 
     * @return the property type
     */
    PropertyType getPropertyType();

    /**
     * Returns the cardinality.
     * 
     * @return the cardinality
     */
    Cardinality getCardinality();

    /**
     * Returns the updatability.
     * 
     * @return the updatability
     */
    Updatability getUpdatability();

    /**
     * Returns if the property is inherited by a parent type.
     * 
     * @return <code>true</code> - is inherited;
     *         <code>false</false> - is not inherited; <code>null</code> -
     *         unknown (noncompliant repository)
     */
    Boolean isInherited();

    /**
     * Returns if the property is required.
     * 
     * @return <code>true</code> - is required;
     *         <code>false</false> - is not required; <code>null</code> -
     *         unknown (noncompliant repository)
     */
    Boolean isRequired();

    /**
     * Returns if the property is queryable.
     * 
     * @return <code>true</code> - is queryable;
     *         <code>false</false> - is not queryable; <code>null</code> -
     *         unknown (noncompliant repository)
     */
    Boolean isQueryable();

    /**
     * Returns if the property is Orderable.
     * 
     * @return <code>true</code> - is Orderable;
     *         <code>false</false> - is not Orderable; <code>null</code> -
     *         unknown (noncompliant repository)
     */
    Boolean isOrderable();

    /**
     * Returns if the property supports open choice.
     * 
     * @return <code>true</code> - supports open choice;
     *         <code>false</false> - does not support open choice; <code>null</code>
     *         - unknown or not applicable
     */
    Boolean isOpenChoice();

    /**
     * Returns the default value.
     * 
     * @return the default value (list) or <code>null</code> if no default value
     *         is defined
     */
    List<T> getDefaultValue();

    /**
     * Returns the choices for this property.
     * 
     * @return the choices or <code>null</code> if no choices are defined
     */
    List<Choice<T>> getChoices();
}
