/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.chemistry.opencmis.jcr;

import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionManager;

/**
 * Factory for creating instances of sub-classes of {@link JcrNode} from JCR <code>Node</code>s.
 */
public class JcrNodeFactory {  
    private static final Log log = LogFactory.getLog(JcrNodeFactory.class);

    private TypeManager typeManager;
    private PathManager pathManager;

    /**
     * Create a new {@link JcrNode} from a JCR <code>Node</code>.
     * This implementation creates a {@link JcrUnversionedDocument} if <code>node</code> is
     * of type nt:file but not of type mix:simpleVersionable, a {@link JcrVersion} if <code>node</code>
     * is of type nt:file and of type mix:simpleVersionable and a {@link JcrFolder} otherwise.
     *
     * @param node
     * @return
     */
    public JcrNode create(Node node){
        try {
            if (node.isNodeType(NodeType.NT_FILE)) {
                if (node.isNodeType(NodeType.MIX_SIMPLE_VERSIONABLE)) {
                    VersionManager versionManager = node.getSession().getWorkspace().getVersionManager();
                    Version version = versionManager.getBaseVersion(node.getPath());
                    return new JcrVersion(node, version, typeManager, pathManager, this);
                }
                else {
                    return new JcrUnversionedDocument(node, typeManager, pathManager, this);
                }
            }
            else {
                return new JcrFolder(node, typeManager, pathManager, this);
            }
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    //------------------------------------------< internal >---

    protected final TypeManager getTypeManager() {
        return typeManager;
    }

    protected final PathManager getPathManager() {
        return pathManager;
    }

    void initialize(TypeManager typeManager, PathManager pathManager) {
        this.typeManager = typeManager;
        this.pathManager = pathManager;
    }
}
