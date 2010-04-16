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
import org.apache.chemistry.opencmis.client.api.objecttype.DocumentType;
import org.apache.chemistry.opencmis.client.api.objecttype.ObjectType;
import org.apache.chemistry.opencmis.client.api.util.Container;
import org.apache.chemistry.opencmis.client.api.util.PagingList;
import org.apache.chemistry.opencmis.commons.api.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;

/**
 * Document type.
 */
public class DocumentTypeImpl extends DocumentTypeDefinitionImpl implements
        DocumentType {

    private static final long serialVersionUID = 1L;

    private ObjectTypeHelper helper;

    public DocumentTypeImpl(Session session,
            DocumentTypeDefinition typeDefinition) {
        initialize(typeDefinition);
        helper = new ObjectTypeHelper(session, this);
    }

    public ObjectType getBaseType() {
        return helper.getBaseType();
    }

    public PagingList<ObjectType> getChildren(int itemsPerPage) {
        return helper.getChildren(itemsPerPage);
    }

    public List<Container<ObjectType>> getDescendants(int depth) {
        return helper.getDescendants(depth);
    }

    public ObjectType getParentType() {
        return helper.getParentType();
    }

    public boolean isBaseType() {
        return helper.isBaseType();
    }

}
