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

import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_ACL;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_ALLOWABLEACIONS;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CHANGES;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CHECKEDOUT;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CHILDREN;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CONTENT;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_DESCENDANTS;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_ENTRY;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_FOLDERTREE;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_OBJECTBYID;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_OBJECTBYPATH;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_PARENTS;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_POLICIES;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_QUERY;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_RELATIONSHIPS;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_TYPE;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_TYPES;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_TYPESDESC;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_UNFILED;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_VERSIONS;
import static org.apache.chemistry.opencmis.server.shared.Dispatcher.METHOD_DELETE;
import static org.apache.chemistry.opencmis.server.shared.Dispatcher.METHOD_GET;
import static org.apache.chemistry.opencmis.server.shared.Dispatcher.METHOD_POST;
import static org.apache.chemistry.opencmis.server.shared.Dispatcher.METHOD_PUT;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

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
import org.apache.chemistry.opencmis.server.impl.ServerVersion;
import org.apache.chemistry.opencmis.server.shared.CallContextHandler;
import org.apache.chemistry.opencmis.server.shared.Dispatcher;
import org.apache.chemistry.opencmis.server.shared.ExceptionHelper;
import org.apache.chemistry.opencmis.server.shared.HttpUtils;
import org.apache.chemistry.opencmis.server.shared.QueryStringHttpServletRequestWrapper;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CMIS AtomPub servlet.
 */
public class CmisAtomPubServlet extends HttpServlet {

    public static final String PARAM_CALL_CONTEXT_HANDLER = "callContextHandler";
    public static final String PARAM_TRUSTED_PROXIES = "trustedProxies";

    private static final Logger LOG = LoggerFactory.getLogger(CmisAtomPubServlet.class.getName());

    private static final long serialVersionUID = 1L;

    private File tempDir;
    private int memoryThreshold;
    private long maxContentSize;

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

        // get memory threshold and temp directory
        CmisServiceFactory factory = (CmisServiceFactory) config.getServletContext().getAttribute(
                CmisRepositoryContextListener.SERVICES_FACTORY);

        tempDir = factory.getTempDirectory();
        memoryThreshold = factory.getMemoryThreshold();
        maxContentSize = factory.getMaxContentSize();

        // initialize the dispatcher
        dispatcher = new Dispatcher();

        try {
            dispatcher.addResource(RESOURCE_TYPES, METHOD_GET, RepositoryService.class, "getTypeChildren");
            dispatcher.addResource(RESOURCE_TYPESDESC, METHOD_GET, RepositoryService.class, "getTypeDescendants");
            dispatcher.addResource(RESOURCE_TYPE, METHOD_GET, RepositoryService.class, "getTypeDefinition");
            dispatcher.addResource(RESOURCE_CHILDREN, METHOD_GET, NavigationService.class, "getChildren");
            dispatcher.addResource(RESOURCE_DESCENDANTS, METHOD_GET, NavigationService.class, "getDescendants");
            dispatcher.addResource(RESOURCE_FOLDERTREE, METHOD_GET, NavigationService.class, "getFolderTree");
            dispatcher.addResource(RESOURCE_PARENTS, METHOD_GET, NavigationService.class, "getObjectParents");
            dispatcher.addResource(RESOURCE_CHECKEDOUT, METHOD_GET, NavigationService.class, "getCheckedOutDocs");
            dispatcher.addResource(RESOURCE_ENTRY, METHOD_GET, ObjectService.class, "getObject");
            dispatcher.addResource(RESOURCE_OBJECTBYID, METHOD_GET, ObjectService.class, "getObject");
            dispatcher.addResource(RESOURCE_OBJECTBYPATH, METHOD_GET, ObjectService.class, "getObjectByPath");
            dispatcher.addResource(RESOURCE_ALLOWABLEACIONS, METHOD_GET, ObjectService.class, "getAllowableActions");
            dispatcher.addResource(RESOURCE_CONTENT, METHOD_GET, ObjectService.class, "getContentStream");
            dispatcher.addResource(RESOURCE_CONTENT, METHOD_PUT, ObjectService.class, "setContentStream");
            dispatcher.addResource(RESOURCE_CONTENT, METHOD_DELETE, ObjectService.class, "deleteContentStream");
            dispatcher.addResource(RESOURCE_CHILDREN, METHOD_POST, ObjectService.class, "create");
            dispatcher.addResource(RESOURCE_RELATIONSHIPS, METHOD_POST, ObjectService.class, "createRelationship");
            dispatcher.addResource(RESOURCE_ENTRY, METHOD_PUT, ObjectService.class, "updateProperties");
            dispatcher.addResource(RESOURCE_ENTRY, METHOD_DELETE, ObjectService.class, "deleteObject");
            dispatcher.addResource(RESOURCE_CHILDREN, METHOD_DELETE, ObjectService.class, "deleteTree"); // 1.1
            dispatcher.addResource(RESOURCE_DESCENDANTS, METHOD_DELETE, ObjectService.class, "deleteTree");
            dispatcher.addResource(RESOURCE_FOLDERTREE, METHOD_DELETE, ObjectService.class, "deleteTree");
            dispatcher.addResource(RESOURCE_CHECKEDOUT, METHOD_POST, VersioningService.class, "checkOut");
            dispatcher.addResource(RESOURCE_VERSIONS, METHOD_GET, VersioningService.class, "getAllVersions");
            dispatcher.addResource(RESOURCE_VERSIONS, METHOD_DELETE, VersioningService.class, "deleteAllVersions");
            dispatcher.addResource(RESOURCE_QUERY, METHOD_GET, DiscoveryService.class, "query");
            dispatcher.addResource(RESOURCE_QUERY, METHOD_POST, DiscoveryService.class, "query");
            dispatcher.addResource(RESOURCE_CHANGES, METHOD_GET, DiscoveryService.class, "getContentChanges");
            dispatcher.addResource(RESOURCE_RELATIONSHIPS, METHOD_GET, RelationshipService.class,
                    "getObjectRelationships");
            dispatcher.addResource(RESOURCE_UNFILED, METHOD_POST, MultiFilingService.class, "removeObjectFromFolder");
            dispatcher.addResource(RESOURCE_ACL, METHOD_GET, AclService.class, "getAcl");
            dispatcher.addResource(RESOURCE_ACL, METHOD_PUT, AclService.class, "applyAcl");
            dispatcher.addResource(RESOURCE_POLICIES, METHOD_GET, PolicyService.class, "getAppliedPolicies");
            dispatcher.addResource(RESOURCE_POLICIES, METHOD_POST, PolicyService.class, "applyPolicy");
            dispatcher.addResource(RESOURCE_POLICIES, METHOD_DELETE, PolicyService.class, "removePolicy");
        } catch (NoSuchMethodException e) {
            LOG.error("Cannot initialize dispatcher!", e);
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        QueryStringHttpServletRequestWrapper qsRequest = new QueryStringHttpServletRequestWrapper(request);

        // set default headers
        response.addHeader("Cache-Control", "private, max-age=0");
        response.addHeader("Server", ServerVersion.OPENCMIS_SERVER);

        // create a context object, dispatch and handle exceptions
        CallContext context = null;
        try {
            context = HttpUtils.createContext(qsRequest, response, getServletContext(), CallContext.BINDING_ATOMPUB,
                    callContextHandler, tempDir, memoryThreshold, maxContentSize);
            dispatch(context, qsRequest, response);
        } catch (Exception e) {
            if (e instanceof CmisPermissionDeniedException) {
                if ((context == null) || (context.getUsername() == null)) {
                    response.setHeader("WWW-Authenticate", "Basic realm=\"CMIS\"");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required");
                } else {
                    response.sendError(getErrorCode((CmisPermissionDeniedException) e), e.getMessage());
                }
            } else {
                printError(e, response);
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
    private static int getErrorCode(CmisBaseException ex) {
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

    /**
     * Prints the error HTML page.
     */
    private static void printError(Exception ex, HttpServletResponse response) {
        int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        String exceptionName = "runtime";

        if (ex instanceof CmisRuntimeException) {
            LOG.error(ex.getMessage(), ex);
        } else if (ex instanceof CmisBaseException) {
            statusCode = getErrorCode((CmisBaseException) ex);
            exceptionName = ((CmisBaseException) ex).getExceptionName();
        } else {
            LOG.error(ex.getMessage(), ex);
        }

        try {
            PrintWriter pw = response.getWriter();
            response.setStatus(statusCode);
            response.setContentType("text/html");

            pw.print("<html><head><title>Apache Chemistry OpenCMIS - "
                    + exceptionName
                    + " error</title>"
                    + "<style><!--H1 {font-size:24px;line-height:normal;font-weight:bold;background-color:#f0f0f0;color:#003366;border-bottom:1px solid #3c78b5;padding:2px;} "
                    + "BODY {font-family:Verdana,arial,sans-serif;color:black;font-size:14px;} "
                    + "HR {color:#3c78b5;height:1px;}--></style></head><body>");
            pw.print("<h1>HTTP Status " + statusCode + " - <!--exception-->" + exceptionName + "<!--/exception--></h1>");
            pw.print("<p><!--message-->" + StringEscapeUtils.escapeHtml(ex.getMessage()) + "<!--/message--></p>");

            String st = ExceptionHelper.getStacktraceAsString(ex);
            if (st != null) {
                pw.print("<hr noshade='noshade'/><!--stacktrace--><pre>\n" + st
                        + "\n</pre><!--/stacktrace--><hr noshade='noshade'/>");
            }

            pw.print("</body></html>");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            try {
                response.sendError(statusCode, ex.getMessage());
            } catch (Exception en) {
            }
        }
    }
}
