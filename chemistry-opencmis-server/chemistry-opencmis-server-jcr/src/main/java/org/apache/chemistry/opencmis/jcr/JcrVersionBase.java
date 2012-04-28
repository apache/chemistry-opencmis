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

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.jcr.type.JcrTypeHandlerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import java.util.Iterator;
import java.util.Set;

/**
 * Instances of this class represent a versionable cmis:document and its versions backed by an underlying
 * JCR <code>Node</code>. 
 */
public abstract class JcrVersionBase extends JcrDocument {
    private static final Logger log = LoggerFactory.getLogger(JcrVersionBase.class);

    protected JcrVersionBase(Node node, JcrTypeManager typeManager, PathManager pathManager, JcrTypeHandlerManager typeHandlerManager) {
        super(node, typeManager, pathManager, typeHandlerManager);
    }

    /**
     * See CMIS 1.0 section 2.2.7.6 getAllVersions
     */
    public Iterator<JcrVersion> getVersions() {
        try {
            VersionHistory versionHistory = getVersionHistory(getNode());
            final VersionIterator versions = versionHistory.getAllLinearVersions();

            return new Iterator<JcrVersion>() {
                public boolean hasNext() {
                    return versions.hasNext();
                }

                public JcrVersion next() {
                    return new JcrVersion(getNode(), versions.nextVersion(), typeManager, pathManager, typeHandlerManager);
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }
    
    @Override
    public void delete(boolean allVersions, boolean isPwc) {
        Node node = getNode();
        try {
            if (node.isCheckedOut()) {
                if (isPwc) {
                    cancelCheckout(node);
                }
                else {
                    throw new CmisStorageException("Cannot delete checked out document: " + getId());
                }
            }
            else if (allVersions) {
                checkout(node);
                node.remove();
                node.getSession().save();
            }
            else {
                throw new CmisRuntimeException("Cannot delete a single version");
            }
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.7.1 checkOut
     *
     * @throws CmisRuntimeException
     */
    public JcrPrivateWorkingCopy checkout() {
        Node node = getNode();
        try {
            if (node.isCheckedOut()) {
                throw new CmisConstraintException("Document is already checked out " + getId());
            }

            checkout(node);
            return getPwc();
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.7.3 checkedIn
     *
     * @throws CmisRuntimeException
     */
    public JcrVersion checkin(Properties properties, ContentStream contentStream, String checkinComment) {
        Node node = getNode();

        try {
            if (!node.isCheckedOut()) {
                throw new CmisStorageException("Not checked out: " + getId());
            }

            if (properties != null && !properties.getPropertyList().isEmpty()) {
                updateProperties(properties);
            }

            if (contentStream != null) {
                setContentStream(contentStream, true);
            }

            // todo handle checkinComment
            checkin(node);
            return (JcrVersion) create(node);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.7.2 cancelCheckout
     *
     * @throws CmisRuntimeException
     */
    public void cancelCheckout() {
        Node node = getNode();
        try {
            cancelCheckout(node);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Get the private working copy of the versions series or throw an exception if not checked out.
     *
     * @return  a {@link JcrPrivateWorkingCopy} instance
     * @throws CmisObjectNotFoundException  if not checked out
     * @throws CmisRuntimeException
     */
    public JcrPrivateWorkingCopy getPwc() {
        try {
            Node node = getNode();
            if (node.isCheckedOut()) {
                return new JcrPrivateWorkingCopy(getNode(), typeManager, pathManager, typeHandlerManager);
            }
            else {
                throw new CmisObjectNotFoundException("Not checked out document has no private working copy");
            }
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Get a specific version by name
     * @param name  name of the version to get
     * @return  a {@link JcrVersion} instance for <code>name</code>
     * @throws CmisObjectNotFoundException  if a version <code>name</code> does not exist
     * @throws CmisRuntimeException
     */
    public JcrVersion getVersion(String name) {
        try {
            Node node = getNode();
            VersionHistory versionHistory = getVersionHistory(node);
            Version version = versionHistory.getVersion(name);
            return new JcrVersion(node, version, typeManager, pathManager, typeHandlerManager);
        }
        catch (UnsupportedRepositoryOperationException e) {
            log.debug(e.getMessage(), e);
            throw new CmisObjectNotFoundException(e.getMessage(), e);
        }
        catch (VersionException e) {
            log.debug(e.getMessage(), e);
            throw new CmisObjectNotFoundException(e.getMessage(), e);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    //------------------------------------------< protected >---

    /**
     * @return  Id of the version representing the base of this versions series
     * @throws RepositoryException
     */
    protected String getBaseNodeId() throws RepositoryException {
        Version baseVersion = getBaseVersion(getNode());
        JcrNode baseNode = new JcrVersion(getNode(), baseVersion, typeManager, pathManager, typeHandlerManager);
        return baseNode.getId();
    }

    /**
     * @return  Id of the private working copy of this version series
     * @throws RepositoryException
     */
    protected String getPwcId() throws RepositoryException {
        return null;
    }
    
    @Override
    protected void compileProperties(PropertiesImpl properties, Set<String> filter, ObjectInfoImpl objectInfo)
            throws RepositoryException {

        super.compileProperties(properties, filter, objectInfo);

        objectInfo.setWorkingCopyOriginalId(getBaseNodeId());
        objectInfo.setWorkingCopyId(getPwcId());
    }

    @Override
    protected Set<Action> compileAllowableActions(Set<Action> aas) {
        Set<Action> result = super.compileAllowableActions(aas);
        setAction(result, Action.CAN_GET_ALL_VERSIONS, true);
        setAction(result, Action.CAN_CHECK_OUT, true);
        setAction(result, Action.CAN_CANCEL_CHECK_OUT, true);
        setAction(result, Action.CAN_CHECK_IN, true);
        return result;
    }
    
    @Override
    protected String getTypeIdInternal() {
        return JcrTypeManager.DOCUMENT_TYPE_ID;
    }

    @Override
    protected boolean isCheckedOut() throws RepositoryException {
        return getNode().isCheckedOut();
    }

    @Override
    protected String getCheckedOutId() throws RepositoryException {
        return isCheckedOut()
                ? getVersionSeriesId() + "/pwc"
                : null;
    }

    @Override
    protected String getCheckedOutBy() throws RepositoryException {
        return isCheckedOut()
                ? getNode().getSession().getUserID()
                : null;
    }
    
    //------------------------------------------< private >---

    private static void checkout(Node node) throws RepositoryException {
        getVersionManager(node).checkout(node.getPath());
    }

    private static void checkin(Node node) throws RepositoryException {
        getVersionManager(node).checkin(node.getPath());
    }

    private static void cancelCheckout(Node node) throws RepositoryException {
        Version base = getBaseVersion(node);
        getVersionManager(node).restore(base, true);
    }
    
}
