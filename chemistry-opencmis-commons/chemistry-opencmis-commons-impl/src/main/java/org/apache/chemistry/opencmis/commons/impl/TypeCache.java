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
package org.apache.chemistry.opencmis.commons.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;

/**
 * Temporary type cache used for one call.
 */
public class TypeCache {

    private final String repositoryId;
    private final CmisService service;
    private final Map<String, TypeDefinition> typeDefinitions;

    public TypeCache(String repositoryId, CmisService service) {
        this.repositoryId = repositoryId;
        this.service = service;
        typeDefinitions = new HashMap<String, TypeDefinition>();
    }

    public TypeDefinition getTypeDefinition(String typeId) {
        TypeDefinition type = typeDefinitions.get(typeId);
        if (type == null) {
            type = service.getTypeDefinition(repositoryId, typeId, null);
            if (type != null) {
                typeDefinitions.put(type.getId(), type);
            }
        }

        return type;
    }

    public TypeDefinition getTypeDefinitionForObject(String objectId) {
        ObjectInfo info = service.getObjectInfo(repositoryId, objectId);
        if (info == null) {
            return null;
        }

        return getTypeDefinition(info.getTypeId());
    }
}
