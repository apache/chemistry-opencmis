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

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

/**
 * Object Type.
 * <p>
 * See CMIS Domain Model - section 2.1.3.
 */
public interface ObjectType extends TypeDefinition {

    String DOCUMENT_BASETYPE_ID = BaseTypeId.CMIS_DOCUMENT.value();
    String FOLDER_BASETYPE_ID = BaseTypeId.CMIS_FOLDER.value();
    String RELATIONSHIP_BASETYPE_ID = BaseTypeId.CMIS_RELATIONSHIP.value();
    String POLICY_BASETYPE_ID = BaseTypeId.CMIS_POLICY.value();

    /**
     * Indicates if this is base object type (i.e. if {@code getId()} returns
     * ...{@code _BASETYPE_ID}.
     * 
     * @return {@code true} if this type is a base type, {@code false} if this
     *         type is a derived type.
     */
    boolean isBaseType();

    /**
     * Get the type's base type, if the type is a derived (non-base) type.
     * 
     * @return the base type this type is derived from, or {@code null} if it is
     *         a base type ({@code isBase()==true}).
     * @throws CmisRuntimeException
     */
    ObjectType getBaseType(); // null if isBase == true

    /**
     * Get the type's parent type, if the type is a derived (non-base) type.
     * 
     * @return the parent type from which this type is derived, or {@code null}
     *         if it is a base type ( {@code isBase()==true}).
     * @throws CmisRuntimeException
     */
    ObjectType getParentType();

    /**
     * Get the list of types directly derived from this type (which will return
     * this type on {@code getParent()}).
     * 
     * @return a {@code List} of types which are directly derived from this
     *         type.
     */
    ItemIterable<ObjectType> getChildren();

    /**
     * Get the list of all types somehow derived from this type.
     * 
     * @param depth
     *            the depth to which the derived types should be resolved.
     * @return a {@code Tree} of types which are derived from this type (direct
     *         and via their parents).
     */
    List<Tree<ObjectType>> getDescendants(int depth);

}
