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
package org.apache.chemistry.opencmis.client.runtime.objecttype;

import java.util.List;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.objecttype.ObjectType;
import org.apache.chemistry.opencmis.client.api.util.Container;
import org.apache.chemistry.opencmis.client.api.util.PagingList;

/**
 * Helper for object types, containing session-related info.
 * <p>
 * This is needed because Java doesn't support multiple inheritance.
 */
public class ObjectTypeHelper {

    private Session session;
    private ObjectType objectType;
    private ObjectType baseType;
    private ObjectType parentType;

    public ObjectTypeHelper(Session session, ObjectType objectType) {
        this.session = session;
        this.objectType = objectType;
    }

    public Session getSession() {
        return session;
    }

    public boolean isBaseType() {
        return objectType.getParentTypeId() == null;
    }

    public ObjectType getBaseType() {
        if (isBaseType()) {
            return null;
        }
        if (baseType != null) {
            return baseType;
        }
        if (objectType.getBaseTypeId() == null) {
            return null;
        }
        baseType = session
                .getTypeDefinition(objectType.getBaseTypeId().value());
        return baseType;
    }

    public ObjectType getParentType() {
        if (parentType != null) {
            return parentType;
        }
        if (objectType.getParentTypeId() == null) {
            return null;
        }
        parentType = session.getTypeDefinition(objectType.getParentTypeId());
        return parentType;
    }

    public PagingList<ObjectType> getChildren(int itemsPerPage) {
        return session.getTypeChildren(objectType.getId(), true, itemsPerPage);
    }

    public List<Container<ObjectType>> getDescendants(int depth) {
        return session.getTypeDescendants(objectType.getId(), depth, true);
    }
}
