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
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.jcr.type.JcrTypeHandlerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Set;

/**
 * Instances of this class represent a cmis:document backed by an underlying JCR <code>Node</code>.
 */
public abstract class JcrDocument extends JcrNode {
    private static final Logger log = LoggerFactory.getLogger(JcrDocument.class);

    public static final String MIME_UNKNOWN = "application/octet-stream";

    protected JcrDocument(Node node, JcrTypeManager typeManager, PathManager pathManager, JcrTypeHandlerManager typeHandlerManager) {
        super(node, typeManager, pathManager, typeHandlerManager);
    }

    /**
     * @return  <code>true</code> iff the document is checked out
     */
    public boolean isDocumentCheckedOut() {
        try {
            return getNode().isCheckedOut();
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.4.10 getContentStream
     *
     * @throws CmisObjectNotFoundException
     * @throws CmisRuntimeException
     */
    public ContentStream getContentStream() {
        try {
            Node contentNode = getContextNode();
            Property data = contentNode.getProperty(Property.JCR_DATA);

            // compile data
            ContentStreamImpl result = new ContentStreamImpl();
            result.setFileName(getNodeName());
            result.setLength(BigInteger.valueOf(data.getLength()));
            result.setMimeType(getPropertyOrElse(contentNode, Property.JCR_MIMETYPE, MIME_UNKNOWN));
            result.setStream(new BufferedInputStream(data.getBinary().getStream()));  // stream closed by consumer

            return result;
        }
        catch (PathNotFoundException e) {
            log.debug(e.getMessage(), e);
            throw new CmisObjectNotFoundException(e.getMessage(), e);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.4.16 setContentStream
     *
     * @throws CmisStorageException
     */
    public JcrNode setContentStream(ContentStream contentStream, boolean overwriteFlag) {
        try {
            // get content node. For version series this is *not* the same as the
            // context node. See CMIS-438.
            Node contentNode = getNode().getNode(Node.JCR_CONTENT);
            Property data = contentNode.getProperty(Property.JCR_DATA);

            // check overwrite
            if (!overwriteFlag && data.getLength() != 0) {
                throw new CmisContentAlreadyExistsException("Content already exists!");
            }

            JcrVersionBase jcrVersion = isVersionable()
                    ? asVersion()
                    : null;

            boolean autoCheckout = jcrVersion != null && !jcrVersion.isCheckedOut();
            if (autoCheckout) {
                jcrVersion.checkout();
            }

            // write content, if available
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

            contentNode.getSession().save();

            if (autoCheckout) {
                // auto versioning -> return new version created by checkin
                return jcrVersion.checkin(null, null, "auto checkout");
            }
            else if (jcrVersion != null) {
                // the node is checked out -> return pwc.
                return jcrVersion.getPwc();
            }
            else {
                // non versionable -> return this
                return this;
            }
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

    //------------------------------------------< protected >---

    /**
     * @return  the value of the <code>cmis:isLatestVersion</code> property
     * @throws RepositoryException
     */
    protected abstract boolean isLatestVersion() throws RepositoryException;

    /**
     * @return  the value of the <code>cmis:isMajorVersion</code> property
     * @throws RepositoryException
     */
    protected abstract boolean isMajorVersion() throws RepositoryException;

    /**
     * @return  the value of the <code>cmis:isLatestMajorVersion</code> property
     * @throws RepositoryException
     */
    protected abstract boolean isLatestMajorVersion() throws RepositoryException;

    /**
     * @return  the value of the <code>cmis:versionLabel</code> property
     * @throws RepositoryException
     */
    protected abstract String getVersionLabel() throws RepositoryException;

    /**
     * @return  the value of the <code>cmis:isVersionSeriesCheckedOut</code> property
     * @throws RepositoryException
     */
    protected abstract boolean isCheckedOut() throws RepositoryException;

    /**
     * @return  the value of the <code>cmis:versionSeriesCheckedOutId</code> property
     * @throws RepositoryException
     */
    protected abstract String getCheckedOutId() throws RepositoryException;

    /**
     * @return  the value of the <code>cmis:versionSeriesCheckedOutBy</code> property
     * @throws RepositoryException
     */
    protected abstract String getCheckedOutBy() throws RepositoryException;


    /**
     * @return  the value of the <code>cmis:checkinComment</code> property
     * @throws RepositoryException
     */
    protected abstract String getCheckInComment() throws RepositoryException;

    protected boolean getIsImmutable() {
        return false;
    }

    @Override
    protected void compileProperties(PropertiesImpl properties, Set<String> filter, ObjectInfoImpl objectInfo)
            throws RepositoryException {

        super.compileProperties(properties, filter, objectInfo);

        objectInfo.setHasContent(true);
        objectInfo.setHasParent(true);
        objectInfo.setSupportsDescendants(false);
        objectInfo.setSupportsFolderTree(false);

        String typeId = getTypeIdInternal();
        Node contextNode = getContextNode();

        // mutability
        addPropertyBoolean(properties, typeId, filter, PropertyIds.IS_IMMUTABLE, getIsImmutable());

        // content stream
        long length = getPropertyLength(contextNode, Property.JCR_DATA);
        addPropertyInteger(properties, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, length);

        // mime type
        String mimeType = getPropertyOrElse(contextNode, Property.JCR_MIMETYPE, MIME_UNKNOWN);
        addPropertyString(properties, typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE, mimeType);
        objectInfo.setContentType(mimeType);

        // file name
        String fileName = getNodeName();
        addPropertyString(properties, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, fileName);
        objectInfo.setFileName(fileName);

        addPropertyId(properties, typeId, filter, PropertyIds.CONTENT_STREAM_ID, getObjectId() + "/stream");

        // versioning
        addPropertyBoolean(properties, typeId, filter, PropertyIds.IS_LATEST_VERSION, isLatestVersion());
        addPropertyBoolean(properties, typeId, filter, PropertyIds.IS_MAJOR_VERSION, isMajorVersion());
        addPropertyBoolean(properties, typeId, filter, PropertyIds.IS_LATEST_MAJOR_VERSION, isLatestMajorVersion());
        addPropertyString(properties, typeId, filter, PropertyIds.VERSION_LABEL, getVersionLabel());
        addPropertyId(properties, typeId, filter, PropertyIds.VERSION_SERIES_ID, getVersionSeriesId());
        addPropertyString(properties, typeId, filter, PropertyIds.CHECKIN_COMMENT, getCheckInComment());

        boolean isCheckedOut = isCheckedOut();
        addPropertyBoolean(properties, typeId, filter, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, isCheckedOut);

        if (isCheckedOut) {
            addPropertyId(properties, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, getCheckedOutId());
            addPropertyString(properties, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, getCheckedOutBy());
        }
    }

    @Override
    protected Set<Action> compileAllowableActions(Set<Action> aas) {
        Set<Action> result = super.compileAllowableActions(aas);
        setAction(result, Action.CAN_GET_CONTENT_STREAM, true);
        setAction(result, Action.CAN_SET_CONTENT_STREAM, true);
        setAction(result, Action.CAN_DELETE_CONTENT_STREAM, true);
        setAction(result, Action.CAN_GET_RENDITIONS, false);
        return result;
    }

    @Override
    protected BaseTypeId getBaseTypeId() {
        return BaseTypeId.CMIS_DOCUMENT;
    }

}
