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
package org.apache.chemistry.opencmis.jcr;

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;

/**
 * Default Type Manager that registers the required base types cmis:folder and cmis:document plus an unversionable
 * document type with id cmis:unversioned-document.
 */
public class DefaultJcrTypeManager extends JcrTypeManager {

    public static final String DOCUMENT_UNVERSIONED_TYPE_ID = "cmis:unversioned-document";

    public DefaultJcrTypeManager() {

        // folder type
        FolderTypeDefinitionImpl folderType = new FolderTypeDefinitionImpl();
        folderType.setBaseTypeId(BaseTypeId.CMIS_FOLDER);
        folderType.setIsControllableAcl(false);
        folderType.setIsControllablePolicy(false);
        folderType.setIsCreatable(true);
        folderType.setDescription("Folder");
        folderType.setDisplayName("Folder");
        folderType.setIsFileable(true);
        folderType.setIsFulltextIndexed(false);
        folderType.setIsIncludedInSupertypeQuery(true);
        folderType.setLocalName("Folder");
        folderType.setLocalNamespace(JcrTypeManager.NAMESPACE);
        folderType.setIsQueryable(true);
        folderType.setQueryName(JcrTypeManager.FOLDER_TYPE_ID);
        folderType.setId(JcrTypeManager.FOLDER_TYPE_ID);

        JcrTypeManager.addBasePropertyDefinitions(folderType);
        JcrTypeManager.addFolderPropertyDefinitions(folderType);

        addType(folderType);

        // document type
        DocumentTypeDefinitionImpl documentType = new DocumentTypeDefinitionImpl();
        documentType.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
        documentType.setIsControllableAcl(false);
        documentType.setIsControllablePolicy(false);
        documentType.setIsCreatable(true);
        documentType.setDescription("Document");
        documentType.setDisplayName("Document");
        documentType.setIsFileable(true);
        documentType.setIsFulltextIndexed(false);
        documentType.setIsIncludedInSupertypeQuery(true);
        documentType.setLocalName("Document");
        documentType.setLocalNamespace(JcrTypeManager.NAMESPACE);
        documentType.setIsQueryable(true);
        documentType.setQueryName(JcrTypeManager.DOCUMENT_TYPE_ID);
        documentType.setId(JcrTypeManager.DOCUMENT_TYPE_ID);
        documentType.setIsVersionable(true);
        documentType.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);

        JcrTypeManager.addBasePropertyDefinitions(documentType);
        JcrTypeManager.addDocumentPropertyDefinitions(documentType);

        addType(documentType);

        // non versionable document type
        DocumentTypeDefinitionImpl unversionedDocument = new DocumentTypeDefinitionImpl();
        unversionedDocument.initialize(documentType);

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

        addType(unversionedDocument);
    }

}
