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
package org.apache.chemistry.opencmis.jcr.impl;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.jcr.JcrDocument;
import org.apache.chemistry.opencmis.jcr.JcrTypeManager;
import org.apache.chemistry.opencmis.jcr.JcrUnversionedDocument;
import org.apache.chemistry.opencmis.jcr.query.IdentifierMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

/**
 * Type handler that provides cmis:unversioned-document.
 */
public class DefaultUnversionedDocumentTypeHandler extends DefaultDocumentTypeHandler {

    public static final String DOCUMENT_UNVERSIONED_TYPE_ID = "cmis:unversioned-document";

    @Override
    public String getTypeId() {
        return DOCUMENT_UNVERSIONED_TYPE_ID;
    }

    @Override
    public TypeDefinition getTypeDefinition() {

        DocumentTypeDefinitionImpl unversionedDocument = new DocumentTypeDefinitionImpl();
        unversionedDocument.initialize(super.getTypeDefinition());

        unversionedDocument.setDescription("Unversioned document");
        unversionedDocument.setDisplayName("Unversioned document");
        unversionedDocument.setLocalName("Unversioned document");
        unversionedDocument.setIsQueryable(true);
        unversionedDocument.setQueryName(DOCUMENT_UNVERSIONED_TYPE_ID);
        unversionedDocument.setId(DOCUMENT_UNVERSIONED_TYPE_ID);
        unversionedDocument.setParentTypeId(JcrTypeManager.DOCUMENT_TYPE_ID);

        unversionedDocument.setIsVersionable(false);
        unversionedDocument.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);

        JcrTypeManager.addBasePropertyDefinitions(unversionedDocument);
        JcrTypeManager.addDocumentPropertyDefinitions(unversionedDocument);

        return unversionedDocument;
    }

    @Override
    public IdentifierMap getIdentifierMap() {
        return new DefaultDocumentIdentifierMap(false);
    }

    @Override
    public boolean canHandle(Node node) throws RepositoryException {
        return node.isNodeType(NodeType.NT_FILE) && !node.isNodeType(NodeType.MIX_SIMPLE_VERSIONABLE);
    }

    @Override
    public JcrDocument getJcrNode(Node node) {
        return new JcrUnversionedDocument(node, typeManager, pathManager, typeHandlerManager);
    }
}
