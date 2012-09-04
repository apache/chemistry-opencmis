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

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.jcr.type.JcrTypeHandlerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instances of this class represent a private working copy of a cmis:document backed by an underlying
 * JCR <code>Node</code>.
 */
public class JcrPrivateWorkingCopy extends JcrVersionBase {
    private static final Logger log = LoggerFactory.getLogger(JcrPrivateWorkingCopy.class);

    /**
     * Name of a private working copy
     */
    public static final String PWC_NAME = "pwc";

    public JcrPrivateWorkingCopy(Node node, JcrTypeManager typeManager, PathManager pathManager,
            JcrTypeHandlerManager typeHandlerManager) {
        
        super(node, typeManager, pathManager, typeHandlerManager);
    }

    /**
     * @return <code>true</code> iff <code>versionName</code> is the name of private working copy.
     * @see JcrPrivateWorkingCopy#PWC_NAME
     */
    public static boolean denotesPwc(String versionName) {
        return PWC_NAME.equals(versionName);
    }

    /**
     * @param objectId
     * @return <code>true</code> iff <code>objectId</code> is the id of a private working copy.
     * @see JcrPrivateWorkingCopy#PWC_NAME
     */
    public static boolean isPwc(String objectId) {
        return objectId.equals(PWC_NAME);   
    }

    //------------------------------------------< protected >---

    @Override
    protected Node getContextNode() {
        try {
            return getNode().getNode(Node.JCR_CONTENT);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisObjectNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    protected String getPwcId() throws RepositoryException {
        return getObjectId();
    }

    @Override
    protected String getObjectId() throws RepositoryException {
        return getVersionSeriesId();
    }

    @Override
    protected String getVersionLabel() throws RepositoryException {
        return PWC_NAME;
    }

    @Override
    protected boolean isLatestVersion() throws RepositoryException {
        return false;
    }

    @Override
    protected boolean isMajorVersion() throws RepositoryException {
        return false;
    }

    @Override
    protected boolean isLatestMajorVersion() throws RepositoryException {
        return false; 
    }

    @Override
    protected String getCheckInComment() {
        return "";
    }

}
