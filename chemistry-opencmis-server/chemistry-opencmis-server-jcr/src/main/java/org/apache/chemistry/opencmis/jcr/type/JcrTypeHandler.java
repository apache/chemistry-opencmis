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

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.jcr.JcrNode;
import org.apache.chemistry.opencmis.jcr.JcrTypeManager;
import org.apache.chemistry.opencmis.jcr.PathManager;
import org.apache.chemistry.opencmis.jcr.query.IdentifierMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Provides operations for a specific CMIS object type.
 *
 * @see JcrTypeHandlerManager
 */
public interface JcrTypeHandler {

    /**
     * Called by {@link JcrTypeHandlerManager} when the handler is added.
     */
    void initialize(PathManager pathManager, JcrTypeManager typeManager, JcrTypeHandlerManager typeHandlerManager);

    String getTypeId();

    TypeDefinition getTypeDefinition();

    boolean canHandle(Node node) throws RepositoryException;

    JcrNode getJcrNode(Node node) throws RepositoryException;

    /**
     * Used by QueryTranslator to translate CMIS queries to JCR queries.
     *
     * @see org.apache.chemistry.opencmis.jcr.query.QueryTranslator
     * @see org.apache.chemistry.opencmis.jcr.JcrRepository#query(javax.jcr.Session, java.lang.String, java.lang.Boolean, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger)
     */
    IdentifierMap getIdentifierMap();
}
