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
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.jcr.type.JcrTypeHandlerManager;
import org.apache.chemistry.opencmis.jcr.util.FilterIterator;
import org.apache.chemistry.opencmis.jcr.util.Predicate;
import org.apache.chemistry.opencmis.jcr.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Instances of this class represent a cmis:folder backed by an underlying JCR <code>Node</code>. 
 */
public class JcrFolder extends JcrNode {
    private static final Logger log = LoggerFactory.getLogger(JcrFolder.class);

    public JcrFolder(Node node, JcrTypeManager typeManager, PathManager pathManager, JcrTypeHandlerManager typeHandlerManager) {
        super(node, typeManager, pathManager, typeHandlerManager);
    }

    /**
     * See CMIS 1.0 section 2.2.3.1 getChildren
     * 
     * @return  Iterator of <code>JcrNode</code>. Children which are created in the checked out
     *      state are left out from the iterator.
     * @throws CmisRuntimeException
     */
    public Iterator<JcrNode> getNodes() {
        try {
            final FilterIterator<Node> nodes = new FilterIterator<Node>(getNode().getNodes(), typeHandlerManager.getNodePredicate());

            Iterator<JcrNode> jcrNodes = new Iterator<JcrNode>() {
                public boolean hasNext() {
                    return nodes.hasNext();
                }

                public JcrNode next() {
                    return create(nodes.next());
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };

            // Filter out nodes which are checked out and do not have a version history (i.e. only a root version)
            // These are created with VersioningState checkedout and not yet checked in.
            return new FilterIterator<JcrNode>(jcrNodes, new Predicate<JcrNode>() {
                public boolean evaluate(JcrNode node) {
                    try {
                        if (node.isVersionable()) {
                            Version baseVersion = getBaseVersion(node.getNode());
                            return baseVersion.getPredecessors().length > 0;
                        }
                        else {
                            return true;
                        }
                    }
                    catch (RepositoryException e) {
                        log.debug(e.getMessage(), e);
                        throw new CmisRuntimeException(e.getMessage(), e);
                    }
                }
            });

        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.4.2 createDocumentFromSource
     *
     * @throws CmisStorageException
     */
    public JcrNode addNodeFromSource(JcrDocument source, Properties properties) {
        try {
            String destPath = PathManager.createCmisPath(getNode().getPath(), source.getName());
            Session session = getNode().getSession();

            session.getWorkspace().copy(source.getNode().getPath(), destPath);  
            JcrNode jcrNode = create(session.getNode(destPath));

            // overlay new properties
            if (properties != null && properties.getProperties() != null) {
                updateProperties(jcrNode.getNode(), jcrNode.getTypeId(), properties);
            }

            session.save();
            return jcrNode;
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.4.14 deleteObject
     *
     * @throws CmisRuntimeException
     */
    @Override
    public void delete(boolean allVersions, boolean isPwc) {
        try {
            if (getNode().hasNodes()) {
                throw new CmisConstraintException("Folder is not empty!");
            }
            else {
                super.delete(allVersions, isPwc);
            }
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.4.15 deleteTree
     */
    public FailedToDeleteDataImpl deleteTree() {
        FailedToDeleteDataImpl result = new FailedToDeleteDataImpl();

        String id = getId();
        try {
            Node node = getNode();
            if (hasCheckOuts(node)) {
                result.setIds(Collections.<String>singletonList(id));                
            }
            else {
                Session session = node.getSession();
                node.remove();
                session.save();
                result.setIds(Collections.<String>emptyList());
            }
        }
        catch (RepositoryException e) {
            result.setIds(Collections.singletonList(id));
        }

        return result;
    }

    //------------------------------------------< protected >---

    @Override
    protected void compileProperties(PropertiesImpl properties, Set<String> filter, ObjectInfoImpl objectInfo)
            throws RepositoryException {

        super.compileProperties(properties, filter, objectInfo);

        objectInfo.setHasContent(false);
        objectInfo.setSupportsDescendants(true);
        objectInfo.setSupportsFolderTree(true);

        String typeId = getTypeIdInternal();

        addPropertyString(properties, typeId, filter, PropertyIds.PATH, pathManager.getPath(getNode()));

        // folder properties
        if (pathManager.isRoot(getNode())) {
            objectInfo.setHasParent(false);
        }
        else {
            objectInfo.setHasParent(true);
            addPropertyId(properties, typeId, filter, PropertyIds.PARENT_ID, getParent().getObjectId());
        }
    }

    @Override
    protected Set<Action> compileAllowableActions(Set<Action> aas) {
        Set<Action> result = super.compileAllowableActions(aas);
        setAction(result, Action.CAN_GET_DESCENDANTS, true);
        setAction(result, Action.CAN_GET_CHILDREN, true);
        setAction(result, Action.CAN_GET_FOLDER_PARENT, !pathManager.isRoot(getNode()));
        setAction(result, Action.CAN_GET_OBJECT_PARENTS, !pathManager.isRoot(getNode()));
        setAction(result, Action.CAN_GET_FOLDER_TREE, true);
        setAction(result, Action.CAN_CREATE_DOCUMENT, true);
        setAction(result, Action.CAN_CREATE_FOLDER, true);
        setAction(result, Action.CAN_DELETE_TREE, true);
        return result;
    }

    @Override
    protected Node getContextNode() {
        return getNode();
    }

    @Override
    protected String getObjectId() throws RepositoryException {
        return isRoot()
                ? PathManager.CMIS_ROOT_ID
                : super.getObjectId();
    }

    @Override
    protected BaseTypeId getBaseTypeId() {
        return BaseTypeId.CMIS_FOLDER;
    }

    @Override
    protected String getTypeIdInternal() {
        return JcrTypeManager.FOLDER_TYPE_ID;
    }

    public static void setProperties(Node node, TypeDefinition type, Properties properties) {
        if (properties == null || properties.getProperties() == null) {
            throw new CmisConstraintException("No properties!");
        }

        Set<String> addedProps = new HashSet<String>();

        try {
            // check if all required properties are there
            for (PropertyData<?> prop : properties.getProperties().values()) {
                PropertyDefinition<?> propDef = type.getPropertyDefinitions().get(prop.getId());

                // do we know that property?
                if (propDef == null) {
                    throw new CmisConstraintException("Property '" + prop.getId() + "' is unknown!");
                }

                // skip type id
                if (propDef.getId().equals(PropertyIds.OBJECT_TYPE_ID)) {
                    log.warn("Cannot set " + PropertyIds.OBJECT_TYPE_ID + ". Ignoring");
                    addedProps.add(prop.getId());
                    continue;
                }

                // skip content stream file name
                if (propDef.getId().equals(PropertyIds.CONTENT_STREAM_FILE_NAME)) {
                    log.warn("Cannot set " + PropertyIds.CONTENT_STREAM_FILE_NAME + ". Ignoring");
                    addedProps.add(prop.getId());
                    continue;
                }

                // can it be set?
                if (propDef.getUpdatability() == Updatability.READONLY) {
                    throw new CmisConstraintException("Property '" + prop.getId() + "' is readonly!");
                }

                // empty properties are invalid
                if (PropertyHelper.isPropertyEmpty(prop)) {
                    throw new CmisConstraintException("Property '" + prop.getId() + "' must not be empty!");
                }

                // add it
                JcrConverter.setProperty(node, prop);
                addedProps.add(prop.getId());
            }

            // check if required properties are missing and try to add default values if defined
            for (PropertyDefinition<?> propDef : type.getPropertyDefinitions().values()) {
                if (!addedProps.contains(propDef.getId()) && propDef.getUpdatability() != Updatability.READONLY) {
                    PropertyData<?> prop = PropertyHelper.getDefaultValue(propDef);
                    if (prop == null && propDef.isRequired()) {
                        throw new CmisConstraintException("Property '" + propDef.getId() + "' is required!");
                    }
                    else if (prop != null) {
                        JcrConverter.setProperty(node, prop);
                    }
                }
            }
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
    }

    //------------------------------------------< private >---

    private static boolean hasCheckOuts(Node node) throws RepositoryException {
        // Build xpath query of the form
        // '//path/to/node//*[jcr:isCheckedOut='true']'
        String xPath = "/*[jcr:isCheckedOut='true']";
        String path = node.getPath();
        if ("/".equals(path)) {
            path = "";
        }
        xPath = '/' + Util.escape(path) + xPath;

        // Execute query
        QueryManager queryManager = node.getSession().getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(xPath, Query.XPATH);
        QueryResult queryResult = query.execute();
        return queryResult.getNodes().hasNext();
    }
    
}
