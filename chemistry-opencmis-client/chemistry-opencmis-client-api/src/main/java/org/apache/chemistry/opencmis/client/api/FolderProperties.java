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

/**
 * Accessors to CMIS folder properties.
 *
 * @see CmisObjectProperties
 */
public interface FolderProperties {

    /**
     * Returns the parent id or <code>null</code> if the folder is ther root
     * folder (CMIS property <code>cmis:parentId</code>).
     */
    String getParentId();

    /**
     * Returns the list of the allowed object types in this folder (CMIS
     * property <code>cmis:allowedChildObjectTypeIds</code>). If the list is
     * empty or <code>null</code> all object types are allowed.
     */
    List<ObjectType> getAllowedChildObjectTypes();
}
