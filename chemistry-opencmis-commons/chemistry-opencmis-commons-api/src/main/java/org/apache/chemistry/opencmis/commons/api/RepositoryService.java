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
package org.apache.chemistry.opencmis.commons.api;

import java.math.BigInteger;
import java.util.List;

/**
 * Repository Service interface. See CMIS 1.0 domain model for details.
 */
public interface RepositoryService {

    /**
     * Returns a list of CMIS repositories available from this CMIS service
     * endpoint.
     */
    List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension);

    /**
     * Returns information about the CMIS repository, the optional capabilities
     * it supports and its Access Control information if applicable.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension);

    /**
     * Returns the list of Object-Types defined for the Repository that are
     * children of the specified Type.
     * 
     * @param repositoryId
     *            the identifier for the repository
     * @param typeId
     *            optional) the typeId of an Object-Type specified in the
     *            repository
     * @param includePropertyDefinitions
     *            (optional) if <code>true</code>, then the Repository MUST
     *            return the property definitions for each object type returned
     */
    TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension);

    /**
     * Returns the set of descendant object type defined for the repository
     * under the specified type.
     */
    List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension);

    /**
     * Gets the definition of the specified object type.
     */
    TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension);
}
