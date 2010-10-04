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
package org.apache.chemistry.opencmis.server.impl.atompub;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.server.impl.CmisRepositoryContextListener;
import org.apache.chemistry.opencmis.server.shared.CallContextHandler;
import org.apache.chemistry.opencmis.server.shared.HttpUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CMIS AtomPub servlet.
 */
public class CmisAtomPubServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String PARAM_CALL_CONTEXT_HANDLER = "callContextHandler";

    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_DELETE = "DELETE";

    private static final Log LOG = LogFactory.getLog(CmisAtomPubServlet.class.getName());

    private Dispatcher dispatcher;
    private CallContextHandler callContextHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // initialize the call context handler
        callContextHandler = null;
        String callContextHandlerClass = config.getInitParameter(PARAM_CALL_CONTEXT_HANDLER);
        if (callContextHandlerClass != null) {
            try {
                callContextHandler = (CallContextHandler) Class.forName(callContextHandlerClass).newInstance();
            } catch (Exception e) {
                throw new ServletException("Could not load call context handler: " + e, e);
            }
        }

        // initialize the dispatcher
        dispatcher = new Dispatcher();

        try {
            dispatcher.addResource(AtomPubUtils.RESOURCE_TYPES, METHOD_GET, RepositoryService.class, "getTypeChildren");
            dispatcher.addResource(AtomPubUtils.RESOURCE_TYPESDESC, METHOD_GET, RepositoryService.class,
                    "getTypeDescendants");
            dispatcher
                    .addResource(AtomPubUtils.RESOURCE_TYPE, METHOD_GET, RepositoryService.class, "getTypeDefinition");
            dispatcher.addResource(AtomPubUtils.RESOURCE_CHILDREN, METHOD_GET, NavigationService.class, "getChildren");
            dispatcher.addResource(AtomPubUtils.RESOURCE_DESCENDANTS, METHOD_GET, NavigationService.class,
                    "getDescendants");
            dispatcher.addResource(AtomPubUtils.RESOURCE_FOLDERTREE, METHOD_GET, NavigationService.class,
                    "getFolderTree");
            dispatcher.addResource(AtomPubUtils.RESOURCE_PARENTS, METHOD_GET, NavigationService.class,
                    "getObjectParents");
            dispatcher.addResource(AtomPubUtils.RESOURCE_CHECKEDOUT, METHOD_GET, NavigationService.class,
                    "getCheckedOutDocs");
            dispatcher.addResource(AtomPubUtils.RESOURCE_ENTRY, METHOD_GET, ObjectService.class, "getObject");
            dispatcher.addResource(AtomPubUtils.RESOURCE_OBJECTBYID, METHOD_GET, ObjectService.class, "getObject");
            dispatcher.addResource(AtomPubUtils.RESOURCE_OBJECTBYPATH, METHOD_GET, ObjectService.class,
                    "getObjectByPath");
            dispatcher.addResource(AtomPubUtils.RESOURCE_ALLOWABLEACIONS, METHOD_GET, ObjectService.class,
                    "getAllowableActions");
            dispatcher.addResource(AtomPubUtils.RESOURCE_CONTENT, METHOD_GET, ObjectService.class, "getContentStream");
            dispatcher.addResource(AtomPubUtils.RESOURCE_CONTENT, METHOD_PUT, ObjectService.class, "setContentStream");
            dispatcher.addResource(AtomPubUtils.RESOURCE_CONTENT, METHOD_DELETE, ObjectService.class,
                    "deleteContentStream");
            dispatcher.addResource(AtomPubUtils.RESOURCE_CHILDREN, METHOD_POST, ObjectService.class, "create");
            dispatcher.addResource(AtomPubUtils.RESOURCE_RELATIONSHIPS, METHOD_POST, ObjectService.class,
                    "createRelationship");
            dispatcher.addResource(AtomPubUtils.RESOURCE_ENTRY, METHOD_PUT, ObjectService.class, "updateProperties");
            dispatcher.addResource(AtomPubUtils.RESOURCE_ENTRY, METHOD_DELETE, ObjectService.class, "deleteObject");
            dispatcher.addResource(AtomPubUtils.RESOURCE_DESCENDANTS, METHOD_DELETE, ObjectService.class, "deleteTree");
            dispatcher.addResource(AtomPubUtils.RESOURCE_CHECKEDOUT, METHOD_POST, VersioningService.class, "checkOut");
            dispatcher.addResource(AtomPubUtils.RESOURCE_VERSIONS, METHOD_GET, VersioningService.class,
                    "getAllVersions");
            dispatcher.addResource(AtomPubUtils.RESOURCE_VERSIONS, METHOD_DELETE, VersioningService.class,
                    "deleteAllVersions");
            dispatcher.addResource(AtomPubUtils.RESOURCE_QUERY, METHOD_GET, DiscoveryService.class, "query");
            dispatcher.addResource(AtomPubUtils.RESOURCE_QUERY, METHOD_POST, DiscoveryService.class, "query");
            dispatcher.addResource(AtomPubUtils.RESOURCE_CHANGES, METHOD_GET, DiscoveryService.class,
                    "getContentChanges");
            dispatcher.addResource(AtomPubUtils.RESOURCE_RELATIONSHIPS, METHOD_GET, RelationshipService.class,
                    "getObjectRelationships");
            dispatcher.addResource(AtomPubUtils.RESOURCE_UNFILED, METHOD_POST, MultiFilingService.class,
                    "removeObjectFromFolder");
            dispatcher.addResource(AtomPubUtils.RESOURCE_ACL, METHOD_GET, AclService.class, "getAcl");
            dispatcher.addResource(AtomPubUtils.RESOURCE_ACL, METHOD_PUT, AclService.class, "applyAcl");
            dispatcher.addResource(AtomPubUtils.RESOURCE_POLICIES, METHOD_GET, PolicyService.class,
                    "getAppliedPolicies");
            dispatcher.addResource(AtomPubUtils.RESOURCE_POLICIES, METHOD_POST, PolicyService.class, "applyPolicy");
            dispatcher.addResource(AtomPubUtils.RESOURCE_POLICIES, METHOD_DELETE, PolicyService.class, "removePolicy");
        } catch (NoSuchMethodException e) {
            LOG.error("Cannot initialize dispatcher!", e);
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        // create a context object, dispatch and handle exceptions
        CallContext context = null;
        try {
            context = HttpUtils.createContext(request, CallContext.BINDING_ATOMPUB, callContextHandler);
            dispatch(context, request, response);
        } catch (Exception e) {
            if (e instanceof CmisPermissionDeniedException) {
                if ((context == null) || (context.getUsername() == null)) {
                    response.setHeader("WWW-Authenticate", "Basic realm=\"CMIS\"");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required");
                } else {
                    response.sendError(getErrorCode((CmisPermissionDeniedException) e), e.getMessage());
                }
            } else if (e instanceof CmisRuntimeException) {
                LOG.error(e.getMessage(), e);
                response.sendError(getErrorCode((CmisRuntimeException) e), e.getMessage());
            } else if (e instanceof CmisBaseException) {
                response.sendError(getErrorCode((CmisBaseException) e), e.getMessage());
            } else {
                LOG.error(e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        // we are done.
        response.flushBuffer();
    }

    /**
     * Dispatches to feed, entry or whatever.
     */
    private void dispatch(CallContext context, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        CmisService service = null;
        try {
            // get services factory
            CmisServiceFactory factory = (CmisServiceFactory) getServletContext().getAttribute(
                    CmisRepositoryContextListener.SERVICES_FACTORY);

            if (factory == null) {
                throw new CmisRuntimeException("Service factory not available! Configuration problem?");
            }

            // get the service
            service = factory.getService(context);

            // analyze the path
            String[] pathFragments = HttpUtils.splitPath(request);

            if (pathFragments.length < 2) {
                // root -> service document
                RepositoryService.getRepositories(context, service, request, response);
                return;
            }

            String method = request.getMethod();
            String repositoryId = pathFragments[0];
            String resource = pathFragments[1];

            // dispatch
            boolean methodFound = dispatcher.dispatch(resource, method, context, service, repositoryId, request,
                    response);

            // if the dispatcher couldn't find a matching method, return an
            // error message
            if (!methodFound) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Unknown operation");
            }
        } finally {
            if (service != null) {
                service.close();
            }
        }
    }

    /**
     * Translates an exception in an appropriate HTTP error code.
     */
    private int getErrorCode(CmisBaseException ex) {
        if (ex instanceof CmisConstraintException) {
            return 409;
        } else if (ex instanceof CmisContentAlreadyExistsException) {
            return 409;
        } else if (ex instanceof CmisFilterNotValidException) {
            return 400;
        } else if (ex instanceof CmisInvalidArgumentException) {
            return 400;
        } else if (ex instanceof CmisNameConstraintViolationException) {
            return 409;
        } else if (ex instanceof CmisNotSupportedException) {
            return 405;
        } else if (ex instanceof CmisObjectNotFoundException) {
            return 404;
        } else if (ex instanceof CmisPermissionDeniedException) {
            return 403;
        } else if (ex instanceof CmisStorageException) {
            return 500;
        } else if (ex instanceof CmisStreamNotSupportedException) {
            return 403;
        } else if (ex instanceof CmisUpdateConflictException) {
            return 409;
        } else if (ex instanceof CmisVersioningException) {
            return 409;
        }

        return 500;
    }
}
