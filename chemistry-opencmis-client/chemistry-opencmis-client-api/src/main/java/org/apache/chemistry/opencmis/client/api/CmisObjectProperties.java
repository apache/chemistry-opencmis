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
package org.apache.chemistry.opencmis.client.api;

import java.util.GregorianCalendar;
import java.util.List;

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

/**
 * Accessors to CMIS object properties.
 * <p>
 * A property might not be available because either the repository didn't
 * provide it or a property filter was used to retrieve this object.
 * <p>
 * The property values represent a snapshot of the object when it was loaded.
 * The objects and its properties can be out-of-date if the object has been
 * modified in the repository.
 * <p>
 * Implementations of this interface might alter property values without
 * updating the object in the repository. In this case, the values returned by
 * these accessors don't reflect the state of the object in the repository.
 */
public interface CmisObjectProperties {

    /**
     * Returns a list of all available CMIS properties.
     */
    List<Property<?>> getProperties();

    /**
     * Returns the requested property. If the property is not available,
     * <code>null</code> is returned.
     */
    <T> Property<T> getProperty(String id);

    /**
     * Returns the value of the requested property. If the property is not
     * available, <code>null</code> is returned.
     */
    <T> T getPropertyValue(String id);

    // convenience accessors

    /**
     * Returns the name of this CMIS object (CMIS property
     * <code>cmis:name</code>).
     */
    String getName();

    /**
     * Returns the user who created this CMIS object (CMIS property
     * <code>cmis:createdBy</code>).
     */
    String getCreatedBy();

    /**
     * Returns the timestamp when this CMIS object has been created (CMIS
     * property <code>cmis:creationDate</code>).
     */
    GregorianCalendar getCreationDate();

    /**
     * Returns the user who modified this CMIS object (CMIS property
     * <code>cmis:lastModifiedBy</code>).
     */
    String getLastModifiedBy();

    /**
     * Returns the timestamp when this CMIS object has been modified (CMIS
     * property <code>cmis:lastModificationDate</code>).
     */
    GregorianCalendar getLastModificationDate();

    /**
     * Returns the id of the base type of this CMIS object (CMIS property
     * <code>cmis:baseTypeId</code>).
     */
    BaseTypeId getBaseTypeId();

    /**
     * Returns the base type of this CMIS object (object type identified by
     * <code>cmis:baseTypeId</code>).
     */
    ObjectType getBaseType();

    /**
     * Returns the type of this CMIS object (object type identified by
     * <code>cmis:objectTypeId</code>).
     */
    ObjectType getType();

    /**
     * Returns the change token (CMIS property <code>cmis:changeToken</code>).
     */
    String getChangeToken();
}
