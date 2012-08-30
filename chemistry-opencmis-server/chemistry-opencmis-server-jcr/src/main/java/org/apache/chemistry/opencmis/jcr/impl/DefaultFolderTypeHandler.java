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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;
import org.apache.chemistry.opencmis.jcr.JcrFolder;
import org.apache.chemistry.opencmis.jcr.JcrTypeManager;
import org.apache.chemistry.opencmis.jcr.query.IdentifierMap;
import org.apache.chemistry.opencmis.jcr.type.JcrFolderTypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Type handler that provides cmis:folder.
 */
public class DefaultFolderTypeHandler extends AbstractJcrTypeHandler implements JcrFolderTypeHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultFolderTypeHandler.class);

    private static class FolderIdentifierMap extends DefaultIdentifierMapBase {

        public FolderIdentifierMap() {
            super("nt:folder");
            // xxx not supported: PARENT_ID, ALLOWED_CHILD_OBJECT_TYPE_IDS, PATH
        }
    }

    public String getTypeId() {
        return BaseTypeId.CMIS_FOLDER.value();
    }

    public TypeDefinition getTypeDefinition() {
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

        return folderType;
    }

    public IdentifierMap getIdentifierMap() {
        return new FolderIdentifierMap();
    }

    public JcrFolder getJcrNode(Node node) {
        return new JcrFolder(node, typeManager, pathManager, typeHandlerManager);
    }

    public boolean canHandle(Node node) throws RepositoryException {
        return node.isNodeType(NodeType.NT_FOLDER) || node.getDepth() == 0;
    }

    public JcrFolder createFolder(JcrFolder parentFolder, String name, Properties properties) {
        try {
            Node node = parentFolder.getNode().addNode(name, NodeType.NT_FOLDER);
            addMixins(node);
            // compile the properties
            updateProperties(node, properties);
            //save changes
            node.getSession().save();
            return getJcrNode(node);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
    }

    protected void updateProperties(Node node, Properties properties) {
        JcrFolder.setProperties(node, getTypeDefinition(), properties);
    }

    protected void addMixins(Node node) throws RepositoryException {
        node.addMixin(NodeType.MIX_CREATED);
        node.addMixin(NodeType.MIX_LAST_MODIFIED);
    }
}
