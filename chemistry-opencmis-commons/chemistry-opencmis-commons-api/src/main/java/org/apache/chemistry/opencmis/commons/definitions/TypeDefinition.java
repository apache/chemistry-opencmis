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
import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

/**
 * Base type definition interface.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public interface TypeDefinition extends Serializable, ExtensionsData {

    /**
     * Returns the type id.
     * 
     * @return the type id
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
     * Returns the base object type id.
     * 
     * @return the base object type id
     */
    BaseTypeId getBaseTypeId();

    /**
     * Returns the parent type id.
     * 
     * @return the parent type id or <code>null</code> if the type is a base
     *         type
     */
    String getParentTypeId();

    /**
     * Returns if an object of this type can be created.
     * 
     * @return <code>true</code> if an object of this type can be created;
     *         <code>false</code> if creation of objects of this type is not
     *         possible; <code>null</code> - unknown (noncompliant repository)
     */
    Boolean isCreatable();

    /**
     * Returns if an object of this type can be filed.
     * 
     * @return <code>true</code> if an object of this type can be filed;
     *         <code>false</code> if an object of this type cannot be filed;
     *         <code>null</code> - unknown (noncompliant repository)
     */
    Boolean isFileable();

    /**
     * Returns if this type is queryable.
     * 
     * @return <code>true</code> if this type is queryable; <code>false</code>
     *         if this type is not queryable; <code>null</code> - unknown
     *         (noncompliant repository)
     */
    Boolean isQueryable();

    /**
     * Returns if this type is full text indexed.
     * 
     * @return <code>true</code> if this type is full text indexed;
     *         <code>false</code> if this type is not full text indexed;
     *         <code>null</code> - unknown (noncompliant repository)
     */
    Boolean isFulltextIndexed();

    /**
     * Returns if this type is included in queries that query the super type.
     * 
     * @return <code>true</code> if this type is included; <code>false</code> if
     *         this type is not included; <code>null</code> - unknown
     *         (noncompliant repository)
     */
    Boolean isIncludedInSupertypeQuery();

    /**
     * Returns if objects of this type are controllable by policies.
     * 
     * @return <code>true</code> if objects are controllable by policies;
     *         <code>false</code> if objects are not controllable by policies;
     *         <code>null</code> - unknown (noncompliant repository)
     */
    Boolean isControllablePolicy();

    /**
     * Returns if objects of this type are controllable by ACLs.
     * 
     * @return <code>true</code> if objects are controllable by ACLs;
     *         <code>false</code> if objects are not controllable by ACLs;
     *         <code>null</code> - unknown (noncompliant repository)
     */
    Boolean isControllableAcl();

    /**
     * Returns the property definitions of this type.
     * 
     * @return the property definitions or <code>null</code> if the property
     *         definitions were not requested
     */
    Map<String, PropertyDefinition<?>> getPropertyDefinitions();
}
