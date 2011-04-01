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

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.jcr.query.IdentifierMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionManager;
import java.util.HashMap;
import java.util.Map;

// todo refactor to allow for registration of node types and identifier maps
/**
 * Factory for creating instances of sub-classes of {@link JcrNode} from JCR <code>Node</code>s.
 */
public class JcrNodeFactory {  
    private static final Log log = LogFactory.getLog(JcrNodeFactory.class);

    private JcrTypeManager typeManager;
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

    private static final Map<String, IdentifierMap> ID_MAPS = new HashMap<String, IdentifierMap>() {{
        put(JcrTypeManager.DOCUMENT_TYPE_ID, new DocumentIdentifierMap(true));
        put(JcrTypeManager.DOCUMENT_UNVERSIONED_TYPE_ID, new DocumentIdentifierMap(false));
        put(JcrTypeManager.FOLDER_TYPE_ID, new FolderIdentifierMap());
    }};

    /**
     * Return a {@link IdentifierMap} for the given CMIS type.
     * @param fromType  CMIS type
     * @return  <code>IdentifierMap</code>
     */
    public IdentifierMap getIdentifierMap(TypeDefinition fromType) {
        IdentifierMap identifierMap = ID_MAPS.get(fromType.getId());
        if (identifierMap == null) {
            throw new CmisRuntimeException("Not supported: query for type " + fromType.getId());
        }
        else {
            return identifierMap;
        }
    }

    //------------------------------------------< internal >---

    protected final JcrTypeManager getTypeManager() {
        return typeManager;
    }

    protected final PathManager getPathManager() {
        return pathManager;
    }

    void initialize(JcrTypeManager typeManager, PathManager pathManager) {
        this.typeManager = typeManager;
        this.pathManager = pathManager;
    }

    //------------------------------------------< private >---

    private abstract static class IdentifierMapBase implements IdentifierMap {
        private final String jcrTypeName;

        private final Map<String, String> cmis2Jcr = new HashMap<String, String>() {{
            put(PropertyIds.OBJECT_ID, "@jcr:uuid");
            put(PropertyIds.NAME, "fn:name()");
            put(PropertyIds.CREATED_BY, "@jcr:createdBy");
            put(PropertyIds.CREATION_DATE, "@jcr:created");
            put(PropertyIds.LAST_MODIFIED_BY, "@jcr:lastModifiedBy");
            put(PropertyIds.LAST_MODIFICATION_DATE, "@jcr:lastModified");
            // xxx not supported: BASE_TYPE_ID, CHANGE_TOKEN
        }};

        public IdentifierMapBase(String jcrTypeName) {
            this.jcrTypeName = jcrTypeName;
        }

        public IdentifierMapBase(String jcrTypeName, Map<String, String> cmis2Jcr) {
            this(jcrTypeName);
            this.cmis2Jcr.putAll(cmis2Jcr);
        }

        public String jcrPathFromCol(String name) {
            String jcrPath = cmis2Jcr.get(name);
            if (jcrPath == null) {
                throw new CmisRuntimeException("Not supported: query on column " + name);
            }
            else {
                return jcrPath;
            }
        }

        public String jcrTypeName() {
            return jcrTypeName;   
        }

        public String jcrTypeCondition() {
            return null; 
        }
    }

    private static class DocumentIdentifierMap extends IdentifierMapBase {
        private final boolean isVersionable;

        private static final Map<String, String> CMIS2JCR = new HashMap<String, String>() {{
            put(PropertyIds.CREATED_BY, "jcr:content/@jcr:createdBy");
            put(PropertyIds.CREATION_DATE, "jcr:content/@jcr:created");
            put(PropertyIds.LAST_MODIFIED_BY, "jcr:content/@jcr:lastModifiedBy");
            put(PropertyIds.LAST_MODIFICATION_DATE, "jcr:content/@jcr:lastModified");
            put(PropertyIds.CONTENT_STREAM_MIME_TYPE, "jcr:content/@jcr:mimeType");
            put(PropertyIds.CONTENT_STREAM_FILE_NAME, "fn:name()");
            // xxx not supported: IS_IMMUTABLE, IS_LATEST_VERSION, IS_MAJOR_VERSION, IS_LATEST_MAJOR_VERSION,
            // VERSION_LABEL, VERSION_SERIES_ID, IS_VERSION_SERIES_CHECKED_OUT, VERSION_SERIES_CHECKED_OUT_ID
            // VERSION_SERIES_CHECKED_OUT_BY, CHECKIN_COMMENT, CONTENT_STREAM_ID, CONTENT_STREAM_LENGTH
        }};

        public DocumentIdentifierMap(boolean isVersionable) {
            super("nt:file", CMIS2JCR);
            this.isVersionable = isVersionable;
        }

        @Override
        public String jcrTypeCondition() {
            return (isVersionable ? "" : "not") +
                "(@jcr:mixinTypes = 'mix:simpleVersionable')";
        }
    }

    private static class FolderIdentifierMap extends IdentifierMapBase {
        private static final Map<String, String> CMIS2JCR = new HashMap<String, String>() {{
            // xxx not supported: PARENT_ID, ALLOWED_CHILD_OBJECT_TYPE_IDS, PATH
        }};

        public FolderIdentifierMap() {
            super("nt:folder", CMIS2JCR);
        }
    }
}
