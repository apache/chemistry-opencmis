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

import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.jcr.impl.DefaultUnversionedDocumentTypeHandler;
import org.apache.chemistry.opencmis.jcr.type.JcrTypeHandlerManager;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Set;

/**
 * Instances of this class represent a non versionable cmis:document backed by an underlying JCR <code>Node</code>. 
 */
public class JcrUnversionedDocument extends JcrDocument {
    
    public JcrUnversionedDocument(Node node, JcrTypeManager typeManager, PathManager pathManager, JcrTypeHandlerManager typeHandlerManager) {
        super(node, typeManager, pathManager, typeHandlerManager);
    }

    //------------------------------------------< protected >--- 

    @Override
    protected Node getContextNode() throws RepositoryException {
        return getNode().getNode(Node.JCR_CONTENT);
    }

    @Override
    protected Set<Action> compileAllowableActions(Set<Action> aas) {
        Set<Action> result = super.compileAllowableActions(aas);
        setAction(result, Action.CAN_GET_ALL_VERSIONS, false);
        setAction(result, Action.CAN_CHECK_OUT, false);
        setAction(result, Action.CAN_CANCEL_CHECK_OUT, false);
        setAction(result, Action.CAN_CHECK_IN, false);
        return result;
    }

    @Override
    protected String getTypeIdInternal() {
        return DefaultUnversionedDocumentTypeHandler.DOCUMENT_UNVERSIONED_TYPE_ID;
    }

    @Override
    protected boolean isLatestVersion() {
        return true;
    }

    @Override
    protected boolean isMajorVersion() {
        return true;
    }

    @Override
    protected boolean isLatestMajorVersion() {
        return true;
    }

    @Override
    protected String getVersionLabel() {
        return "0.0";
    }

    @Override
    protected boolean isCheckedOut() {
        return false;
    }

    @Override
    protected String getCheckedOutId() {
        return null;   
    }

    @Override
    protected String getCheckedOutBy() throws RepositoryException {
        return null;
    }

    @Override
    protected String getCheckInComment() {
        return "";   
    }


}
