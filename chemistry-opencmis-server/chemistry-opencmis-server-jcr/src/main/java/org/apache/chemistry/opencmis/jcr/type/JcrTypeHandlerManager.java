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
package org.apache.chemistry.opencmis.jcr.type;

import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.jcr.JcrNode;
import org.apache.chemistry.opencmis.jcr.JcrTypeManager;
import org.apache.chemistry.opencmis.jcr.PathManager;
import org.apache.chemistry.opencmis.jcr.query.IdentifierMap;
import org.apache.chemistry.opencmis.jcr.util.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages a set of registered type handlers.
 */
public class JcrTypeHandlerManager {

    private static final Logger log = LoggerFactory.getLogger(JcrTypeHandlerManager.class);

    private final PathManager pathManager;
    private final JcrTypeManager typeManager;
    private final Map<String, JcrTypeHandler> typeHandlers = new HashMap<String, JcrTypeHandler>();

    public JcrTypeHandlerManager(PathManager pathManager, JcrTypeManager typeManager) {
        this.pathManager = pathManager;
        this.typeManager = typeManager;
    }

    public void addHandler(JcrTypeHandler typeHandler) {
        if (typeManager.addType(typeHandler.getTypeDefinition())) {
            typeHandlers.put(typeHandler.getTypeId(), typeHandler);
            typeHandler.initialize(pathManager, typeManager, this);
        }
    }

    public JcrTypeHandler getTypeHandler(String typeId) {
        JcrTypeHandler typeHandler = typeHandlers.get(typeId);
        if (typeHandler == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }
        return typeHandler;
    }

    public JcrFolderTypeHandler getFolderTypeHandler(String typeId) {
        JcrTypeHandler typeHandler = getTypeHandler(typeId);
        if (!(typeHandler instanceof JcrFolderTypeHandler)) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is not a folder!");
        }
        return (JcrFolderTypeHandler) typeHandler;
    }

    public JcrDocumentTypeHandler getDocumentTypeHandler(String typeId) {
        JcrTypeHandler typeHandler = getTypeHandler(typeId);
        if (!(typeHandler instanceof JcrDocumentTypeHandler)) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is not a document!");
        }
        return (JcrDocumentTypeHandler) typeHandler;
    }

    public Predicate<Node> getNodePredicate() {
        return new Predicate<Node>() {
            public boolean evaluate(Node node) {
                try {
                    for (JcrTypeHandler typeHandler : typeHandlers.values()) {
                        if (typeHandler.canHandle(node)) {
                            return true;
                        }
                    }
                    return false;
                }
                catch (RepositoryException e) {
                    log.debug(e.getMessage(), e);
                    throw new CmisRuntimeException(e.getMessage(), e);
                }
            }
        };
    }

    public IdentifierMap getIdentifierMap(String typeId) {
        JcrTypeHandler typeHandler = getTypeHandler(typeId);
        IdentifierMap identifierMap = typeHandler.getIdentifierMap();
        if (identifierMap == null) {
            throw new CmisRuntimeException("Not supported: query for type " + typeId);
        }
        return identifierMap;
    }

    public JcrNode create(Node node) {
        try {
            for (JcrTypeHandler typeHandler : typeHandlers.values()) {
                if (typeHandler.canHandle(node)) {
                    return typeHandler.getJcrNode(node);
                }
            }
            throw new CmisObjectNotFoundException("No object type for object '" + node.getIdentifier() + "'");
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisObjectNotFoundException(e.getMessage(), e);
        }
    }
}
