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
package org.apache.chemistry.opencmis.inmemory.storedobj.api;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.server.support.TypeManager;

public interface TypeManagerCreatable extends TypeManager {
    

    /**
     * Add a type to the type system. Add all properties from inherited types,
     * add type to children of parent types.
     * 
     * @param cmisType
     *            new type to add
     */
    void addTypeDefinition(TypeDefinition typeDefinition);
    
    /**
     * Modify an existing type definition
     * 
     * @param typeDefinition
     *          type to be modified
     */    
    void updateTypeDefinition(TypeDefinition typeDefinition);

   /**
     * Delete a type from the type system. Delete will succeed only if type is not in use.
     * Otherwise an exception is thrown
     * 
     * @param cmisType
     *            type to delete
     */
    void deleteTypeDefinition(String typeId);
}
