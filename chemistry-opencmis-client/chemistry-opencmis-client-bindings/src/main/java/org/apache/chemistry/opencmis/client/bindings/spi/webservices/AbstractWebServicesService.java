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
package org.apache.chemistry.opencmis.client.bindings.spi.webservices;

import java.io.StringWriter;
import java.math.BigInteger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.chemistry.opencmis.client.bindings.impl.CmisBindingsHelper;
import org.apache.chemistry.opencmis.client.bindings.impl.RepositoryInfoCache;
import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisFilterNotValidException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.XMLUtils;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisException;
import org.w3c.dom.Node;

/**
 * Base class for all Web Services clients.
 */
public abstract class AbstractWebServicesService {

    private BindingSession session;

    /**
     * Sets the current session.
     */
    protected void setSession(BindingSession session) {
        this.session = session;
    }

    /**
     * Gets the current session.
     */
    protected BindingSession getSession() {
        return session;
    }

    /**
     * Converts a Web Services Exception into a CMIS Client exception.
     */
    protected CmisBaseException convertException(CmisException ex) {
        if ((ex == null) || (ex.getFaultInfo() == null)) {
            return new CmisRuntimeException("CmisException has no fault!");
        }

        String msg = ex.getFaultInfo().getMessage();
        BigInteger code = ex.getFaultInfo().getCode();

        String errorContent = null;
        if (!ex.getFaultInfo().getAny().isEmpty()) {
            StringBuilder sb = new StringBuilder(1024);
            for (Object o : ex.getFaultInfo().getAny()) {
                if (o != null) {
                    if (o instanceof Node) {
                        sb.append(getNodeAsString((Node) o));
                    } else {
                        sb.append(o.toString());
                    }
                    sb.append('\n');
                }
            }
            errorContent = sb.toString();
        }

        switch (ex.getFaultInfo().getType()) {
        case CONSTRAINT:
            return new CmisConstraintException(msg, code, errorContent);
        case CONTENT_ALREADY_EXISTS:
            return new CmisContentAlreadyExistsException(msg, code, errorContent);
        case FILTER_NOT_VALID:
            return new CmisFilterNotValidException(msg, code, errorContent);
        case INVALID_ARGUMENT:
            return new CmisInvalidArgumentException(msg, code, errorContent);
        case NAME_CONSTRAINT_VIOLATION:
            return new CmisNameConstraintViolationException(msg, code, errorContent);
        case NOT_SUPPORTED:
            return new CmisNotSupportedException(msg, code, errorContent);
        case OBJECT_NOT_FOUND:
            return new CmisObjectNotFoundException(msg, code, errorContent);
        case PERMISSION_DENIED:
            return new CmisPermissionDeniedException(msg, code, errorContent);
        case RUNTIME:
            return new CmisRuntimeException(msg, code, errorContent);
        case STORAGE:
            return new CmisStorageException(msg, code, errorContent);
        case STREAM_NOT_SUPPORTED:
            return new CmisStreamNotSupportedException(msg, code, errorContent);
        case UPDATE_CONFLICT:
            return new CmisUpdateConflictException(msg, code, errorContent);
        case VERSIONING:
            return new CmisVersioningException(msg, code, errorContent);
        default:
        }

        return new CmisRuntimeException("Unknown exception[" + ex.getFaultInfo().getType().value() + "]: " + msg);
    }

    private static String getNodeAsString(Node node) {
        try {
            Transformer transformer = XMLUtils.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            // transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StringWriter sw = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(sw));
            return sw.toString();
        } catch (TransformerException e) {
            assert false;
        }

        return "";
    }

    /**
     * Return the CMIS version of the given repository.
     */
    protected CmisVersion getCmisVersion(String repositoryId) {
        if (CmisBindingsHelper.getForcedCmisVersion(session) != null) {
            return CmisBindingsHelper.getForcedCmisVersion(session);
        }

        RepositoryInfoCache cache = CmisBindingsHelper.getRepositoryInfoCache(session);
        RepositoryInfo info = cache.get(repositoryId);

        if (info == null) {
            info = CmisBindingsHelper.getSPI(session).getRepositoryService().getRepositoryInfo(repositoryId, null);
            if (info != null) {
                cache.put(info);
            }
        }

        // if the version is unknown try CMIS 1.0
        return (info == null ? CmisVersion.CMIS_1_0 : info.getCmisVersion());
    }
}
