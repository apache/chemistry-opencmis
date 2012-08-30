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

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionManager;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.jcr.JcrBinary;
import org.apache.chemistry.opencmis.jcr.JcrDocument;
import org.apache.chemistry.opencmis.jcr.JcrFolder;
import org.apache.chemistry.opencmis.jcr.JcrNode;
import org.apache.chemistry.opencmis.jcr.JcrTypeManager;
import org.apache.chemistry.opencmis.jcr.JcrVersion;
import org.apache.chemistry.opencmis.jcr.JcrVersionBase;
import org.apache.chemistry.opencmis.jcr.query.IdentifierMap;
import org.apache.chemistry.opencmis.jcr.type.JcrDocumentTypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Type handler that provides cmis:document.
 */
public class DefaultDocumentTypeHandler extends AbstractJcrTypeHandler implements JcrDocumentTypeHandler {

    private static final Logger log = LoggerFactory.getLogger(JcrFolder.class);

    public String getTypeId() {
        return BaseTypeId.CMIS_DOCUMENT.value();
    }

    public TypeDefinition getTypeDefinition() {
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

        return documentType;
    }

    public IdentifierMap getIdentifierMap() {
        return new DefaultDocumentIdentifierMap(true);
    }

    public JcrDocument getJcrNode(Node node) throws RepositoryException {
        VersionManager versionManager = node.getSession().getWorkspace().getVersionManager();
        Version version = versionManager.getBaseVersion(node.getPath());
        return new JcrVersion(node, version, typeManager, pathManager, typeHandlerManager);
    }

    public boolean canHandle(Node node) throws RepositoryException {
        return node.isNodeType(NodeType.NT_FILE) && node.isNodeType(NodeType.MIX_SIMPLE_VERSIONABLE);
    }

    public JcrNode createDocument(JcrFolder parentFolder, String name, Properties properties,
                                  ContentStream contentStream, VersioningState versioningState) {
        try {
            Node fileNode = parentFolder.getNode().addNode(name, NodeType.NT_FILE);
            addFileNodeMixins(fileNode,versioningState);
            Node contentNode = fileNode.addNode(Node.JCR_CONTENT, NodeType.NT_RESOURCE);
            addContentNodeMixins(contentNode);
            // compile the properties
            setContentNodeProperties(contentNode, properties);
            // write content, if available
            updateContentNode(contentStream, contentNode);
            //save changes
            fileNode.getSession().save();
            return getJcrNode(fileNode, versioningState);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
        catch (IOException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
    }

    protected JcrNode getJcrNode(Node fileNode, VersioningState versioningState)
            throws RepositoryException {
        JcrNode jcrFileNode = getJcrNode(fileNode);
        if (versioningState == VersioningState.NONE) {
            return jcrFileNode;
        }

        JcrVersionBase jcrVersion = jcrFileNode.asVersion();
        if (versioningState == VersioningState.MINOR || versioningState == VersioningState.MAJOR) {
            return jcrVersion.checkin(null, null, "auto checkin");
        } else {
            return jcrVersion.getPwc();
        }
    }

    protected void updateContentNode(ContentStream contentStream, Node contentNode)
            throws IOException, RepositoryException {
        Binary binary = contentStream == null || contentStream.getStream() == null
                ? JcrBinary.EMPTY
                : new JcrBinary(new BufferedInputStream(contentStream.getStream()));
        try {
            contentNode.setProperty(Property.JCR_DATA, binary);
            if (contentStream != null && contentStream.getMimeType() != null) {
                contentNode.setProperty(Property.JCR_MIMETYPE, contentStream.getMimeType());
            }
        }
        finally {
            binary.dispose();
        }
    }

    protected void setContentNodeProperties(Node contentNode, Properties properties) {
        JcrFolder.setProperties(contentNode, getTypeDefinition(), properties);
    }

    protected void addContentNodeMixins(Node contentNode) throws RepositoryException {
        contentNode.addMixin(NodeType.MIX_CREATED);
    }

    protected void addFileNodeMixins(Node fileNode, VersioningState versioningState)
            throws RepositoryException {
        if (versioningState != VersioningState.NONE) {
            fileNode.addMixin(NodeType.MIX_SIMPLE_VERSIONABLE);
        }
    }

}
