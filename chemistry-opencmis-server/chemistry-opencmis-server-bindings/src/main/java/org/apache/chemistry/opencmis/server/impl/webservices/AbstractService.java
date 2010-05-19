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
package org.apache.chemistry.opencmis.server.impl.webservices;

import java.math.BigInteger;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisFilterNotValidException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisFaultType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumServiceException;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.impl.CallContextImpl;
import org.apache.chemistry.opencmis.server.impl.CmisRepositoryContextListener;

/**
 * This class contains operations used by all services.
 */
public abstract class AbstractService {

    public static final String CALL_CONTEXT_MAP = "org.apache.chemistry.opencmis.callcontext";

    /**
     * Returns the services factory.
     */
    protected AbstractServiceFactory getServiceFactory(WebServiceContext wsContext) {
        ServletContext servletContext = (ServletContext) wsContext.getMessageContext().get(
                MessageContext.SERVLET_CONTEXT);

        return (AbstractServiceFactory) servletContext.getAttribute(CmisRepositoryContextListener.SERVICES_FACTORY);
    }

    /**
     * Creates a CallContext object for the Web Service context.
     */
    @SuppressWarnings("unchecked")
    protected CallContext createContext(WebServiceContext wsContext, String repositoryId) {
        CallContextImpl context = new CallContextImpl(CallContext.BINDING_WEBSERVICES, repositoryId, false);

        MessageContext mc = wsContext.getMessageContext();
        Map<String, String> callContextMap = (Map<String, String>) mc.get(CALL_CONTEXT_MAP);
        if (callContextMap != null) {
            for (Map.Entry<String, String> e : callContextMap.entrySet()) {
                context.put(e.getKey(), e.getValue());
            }
        }

        return context;
    }

    /**
     * Returns the {@link CmisService} object.
     */
    protected CmisService getService(WebServiceContext wsContext, String repositoryId) {
        AbstractServiceFactory factory = getServiceFactory(wsContext);
        CallContext context = createContext(wsContext, repositoryId);
        return factory.getService(context);
    }

    /**
     * Closes the service instance.
     */
    protected void closeService(CmisService service) {
        if (service != null) {
            service.close();
        }
    }

    /**
     * Converts a CMIS exception to the appropriate Web Service exception.
     */
    protected CmisException convertException(Exception ex) {
        CmisFaultType fault = new CmisFaultType();
        fault.setMessage("Unknown exception");
        fault.setCode(BigInteger.ZERO);
        fault.setType(EnumServiceException.RUNTIME);

        if (ex != null) {
            fault.setMessage(ex.getMessage());

            if (ex instanceof CmisBaseException) {
                fault.setCode(((CmisBaseException) ex).getCode());
            }

            if (ex instanceof CmisConstraintException) {
                fault.setType(EnumServiceException.CONSTRAINT);
            } else if (ex instanceof CmisContentAlreadyExistsException) {
                fault.setType(EnumServiceException.CONTENT_ALREADY_EXISTS);
            } else if (ex instanceof CmisFilterNotValidException) {
                fault.setType(EnumServiceException.FILTER_NOT_VALID);
            } else if (ex instanceof CmisInvalidArgumentException) {
                fault.setType(EnumServiceException.INVALID_ARGUMENT);
            } else if (ex instanceof CmisNameConstraintViolationException) {
                fault.setType(EnumServiceException.NAME_CONSTRAINT_VIOLATION);
            } else if (ex instanceof CmisNotSupportedException) {
                fault.setType(EnumServiceException.NOT_SUPPORTED);
            } else if (ex instanceof CmisObjectNotFoundException) {
                fault.setType(EnumServiceException.OBJECT_NOT_FOUND);
            } else if (ex instanceof CmisPermissionDeniedException) {
                fault.setType(EnumServiceException.PERMISSION_DENIED);
            } else if (ex instanceof CmisStorageException) {
                fault.setType(EnumServiceException.STORAGE);
            } else if (ex instanceof CmisStreamNotSupportedException) {
                fault.setType(EnumServiceException.STREAM_NOT_SUPPORTED);
            } else if (ex instanceof CmisUpdateConflictException) {
                fault.setType(EnumServiceException.UPDATE_CONFLICT);
            } else if (ex instanceof CmisVersioningException) {
                fault.setType(EnumServiceException.VERSIONING);
            }
        }

        return new CmisException(fault.getMessage(), fault, ex);
    }
}
