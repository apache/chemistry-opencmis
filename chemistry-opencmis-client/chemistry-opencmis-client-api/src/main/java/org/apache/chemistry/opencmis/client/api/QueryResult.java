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

import java.util.List;

import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.PropertyData;

/**
 * Query result.
 */
public interface QueryResult {

    /**
     * Returns a list of all properties in this query result.
     */
    List<PropertyData<?>> getProperties();

    /**
     * Returns a property by id.
     * <p>
     * Since repositories are not obligated to add property ids to their query
     * result properties, this method might not always work as expected with
     * some repositories. Use {@link #getPropertyByQueryName(String)} instead.
     */
    <T> PropertyData<T> getPropertyById(String id);

    /**
     * Returns a property by query name or alias.
     */
    <T> PropertyData<T> getPropertyByQueryName(String queryName);

    /**
     * Returns a property (single) value by id.
     * 
     * @see #getPropertyById(String)
     */
    <T> T getPropertyValueById(String id);

    /**
     * Returns a property (single) value by query name or alias.
     * 
     * @see #getPropertyByQueryName(String)
     */
    <T> T getPropertyValueByQueryName(String queryName);

    /**
     * Returns a property multi-value by id.
     * 
     * @see #getPropertyById(String)
     */
    <T> List<T> getPropertyMultivalueById(String id);

    /**
     * Returns a property multi-value by query name or alias.
     * 
     * @see #getPropertyByQueryName(String)
     */
    <T> List<T> getPropertyMultivalueByQueryName(String queryName);

    /**
     * Returns the allowable actions if they were requested.
     */
    AllowableActions getAllowableActions();

    /**
     * Returns the relationships if they were requested.
     */
    List<Relationship> getRelationships();

    /**
     * Returns the renditions if they were requested.
     */
    List<Rendition> getRenditions();
}
