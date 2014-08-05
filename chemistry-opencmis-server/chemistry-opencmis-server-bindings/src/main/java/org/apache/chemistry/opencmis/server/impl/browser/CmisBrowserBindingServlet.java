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
package org.apache.chemistry.opencmis.server.impl.browser;

import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_ADD_OBJECT_TO_FOLDER;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_APPEND_CONTENT;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_APPLY_ACL;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_APPLY_POLICY;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_BULK_UPDATE;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_CANCEL_CHECK_OUT;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_CHECK_IN;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_CHECK_OUT;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_CREATE_DOCUMENT;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_CREATE_DOCUMENT_FROM_SOURCE;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_CREATE_FOLDER;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_CREATE_ITEM;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_CREATE_POLICY;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_CREATE_RELATIONSHIP;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_CREATE_TYPE;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_DELETE;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_DELETE_CONTENT;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_DELETE_TREE;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_DELETE_TYPE;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_MOVE;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_QUERY;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_REMOVE_OBJECT_FROM_FOLDER;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_REMOVE_POLICY;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_SET_CONTENT;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_UPDATE_PROPERTIES;
import static org.apache.chemistry.opencmis.commons.impl.Constants.CMISACTION_UPDATE_TYPE;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_OBJECT_ID;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_ACL;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_ALLOWABLEACTIONS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_CHECKEDOUT;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_CHILDREN;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_CONTENT;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_CONTENT_CHANGES;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_DESCENDANTS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_FOLDER_TREE;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_LAST_RESULT;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_OBJECT;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_PARENT;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_PARENTS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_POLICIES;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_PROPERTIES;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_QUERY;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_RELATIONSHIPS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_RENDITIONS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_REPOSITORY_INFO;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_TYPE_CHILDREN;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_TYPE_DEFINITION;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_TYPE_DESCENDANTS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.SELECTOR_VERSIONS;
import static org.apache.chemistry.opencmis.commons.impl.JSONConstants.ERROR_EXCEPTION;
import static org.apache.chemistry.opencmis.commons.impl.JSONConstants.ERROR_MESSAGE;
import static org.apache.chemistry.opencmis.commons.impl.JSONConstants.ERROR_STACKTRACE;
import static org.apache.chemistry.opencmis.server.shared.Dispatcher.METHOD_GET;
import static org.apache.chemistry.opencmis.server.shared.Dispatcher.METHOD_HEAD;
import static org.apache.chemistry.opencmis.server.shared.Dispatcher.METHOD_POST;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
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
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.impl.ServerVersion;
import org.apache.chemistry.opencmis.server.impl.browser.token.TokenHandler;
import org.apache.chemistry.opencmis.server.shared.AbstractCmisHttpServlet;
import org.apache.chemistry.opencmis.server.shared.Dispatcher;
import org.apache.chemistry.opencmis.server.shared.ExceptionHelper;
import org.apache.chemistry.opencmis.server.shared.HEADHttpServletRequestWrapper;
import org.apache.chemistry.opencmis.server.shared.HttpUtils;
import org.apache.chemistry.opencmis.server.shared.NoBodyHttpServletResponseWrapper;
import org.apache.chemistry.opencmis.server.shared.QueryStringHttpServletRequestWrapper;
import org.apache.chemistry.opencmis.server.shared.ServiceCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmisBrowserBindingServlet extends AbstractCmisHttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(CmisBrowserBindingServlet.class);

    private final Dispatcher repositoryDispatcher = new Dispatcher(false);
    private final Dispatcher rootDispatcher = new Dispatcher(false);
    private static final ErrorServiceCall ERROR_SERTVICE_CALL = new ErrorServiceCall();

    public enum CallUrl {
        SERVICE, REPOSITORY, ROOT
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // set the binding
        setBinding(CallContext.BINDING_BROWSER);

        // set CMIS version -> can only be 1.1
        setCmisVersion(CmisVersion.CMIS_1_1);

        // initialize repository resources
        addRepositoryResource("", METHOD_GET, new RepositoryService.GetRepositories());
        addRepositoryResource(SELECTOR_REPOSITORY_INFO, METHOD_GET, new RepositoryService.GetRepositoryInfo());
        addRepositoryResource(SELECTOR_LAST_RESULT, METHOD_GET, new RepositoryService.GetLastResult());
        addRepositoryResource(SELECTOR_TYPE_CHILDREN, METHOD_GET, new RepositoryService.GetTypeChildren());
        addRepositoryResource(SELECTOR_TYPE_DESCENDANTS, METHOD_GET, new RepositoryService.GetTypeDescendants());
        addRepositoryResource(SELECTOR_TYPE_DEFINITION, METHOD_GET, new RepositoryService.GetTypeDefinition());
        addRepositoryResource(CMISACTION_CREATE_TYPE, METHOD_POST, new RepositoryService.CreateType());
        addRepositoryResource(CMISACTION_UPDATE_TYPE, METHOD_POST, new RepositoryService.UpdateType());
        addRepositoryResource(CMISACTION_DELETE_TYPE, METHOD_POST, new RepositoryService.DeleteType());
        addRepositoryResource(SELECTOR_QUERY, METHOD_GET, new DiscoveryService.Query());
        addRepositoryResource(SELECTOR_CHECKEDOUT, METHOD_GET, new NavigationService.GetCheckedOutDocs());
        addRepositoryResource(SELECTOR_CONTENT_CHANGES, METHOD_GET, new DiscoveryService.GetContentChanges());

        addRepositoryResource(CMISACTION_QUERY, METHOD_POST, new DiscoveryService.Query());
        addRepositoryResource(CMISACTION_CREATE_DOCUMENT, METHOD_POST, new ObjectService.CreateDocument());
        addRepositoryResource(CMISACTION_CREATE_DOCUMENT_FROM_SOURCE, METHOD_POST,
                new ObjectService.CreateDocumentFromSource());
        addRepositoryResource(CMISACTION_CREATE_POLICY, METHOD_POST, new ObjectService.CreatePolicy());
        addRepositoryResource(CMISACTION_CREATE_ITEM, METHOD_POST, new ObjectService.CreateItem());
        addRepositoryResource(CMISACTION_CREATE_RELATIONSHIP, METHOD_POST, new ObjectService.CreateRelationship());
        addRepositoryResource(CMISACTION_BULK_UPDATE, METHOD_POST, new ObjectService.BulkUpdateProperties());

        // initialize root resources
        addRootResource(SELECTOR_OBJECT, METHOD_GET, new ObjectService.GetObject());
        addRootResource(SELECTOR_PROPERTIES, METHOD_GET, new ObjectService.GetProperties());
        addRootResource(SELECTOR_ALLOWABLEACTIONS, METHOD_GET, new ObjectService.GetAllowableActions());
        addRootResource(SELECTOR_RENDITIONS, METHOD_GET, new ObjectService.GetRenditions());
        addRootResource(SELECTOR_CONTENT, METHOD_GET, new ObjectService.GetContentStream());
        addRootResource(SELECTOR_CHILDREN, METHOD_GET, new NavigationService.GetChildren());
        addRootResource(SELECTOR_DESCENDANTS, METHOD_GET, new NavigationService.GetDescendants());
        addRootResource(SELECTOR_FOLDER_TREE, METHOD_GET, new NavigationService.GetFolderTree());
        addRootResource(SELECTOR_PARENT, METHOD_GET, new NavigationService.GetFolderParent());
        addRootResource(SELECTOR_PARENTS, METHOD_GET, new NavigationService.GetObjectParents());
        addRootResource(SELECTOR_VERSIONS, METHOD_GET, new VersioningService.GetAllVersions());
        addRootResource(SELECTOR_RELATIONSHIPS, METHOD_GET, new RelationshipService.GetObjectRelationships());
        addRootResource(SELECTOR_CHECKEDOUT, METHOD_GET, new NavigationService.GetCheckedOutDocs());
        addRootResource(SELECTOR_POLICIES, METHOD_GET, new PolicyService.GetAppliedPolicies());
        addRootResource(SELECTOR_ACL, METHOD_GET, new AclService.GetACL());

        addRootResource(CMISACTION_CREATE_DOCUMENT, METHOD_POST, new ObjectService.CreateDocument());
        addRootResource(CMISACTION_CREATE_DOCUMENT_FROM_SOURCE, METHOD_POST,
                new ObjectService.CreateDocumentFromSource());
        addRootResource(CMISACTION_CREATE_FOLDER, METHOD_POST, new ObjectService.CreateFolder());
        addRootResource(CMISACTION_CREATE_POLICY, METHOD_POST, new ObjectService.CreatePolicy());
        addRootResource(CMISACTION_CREATE_ITEM, METHOD_POST, new ObjectService.CreateItem());
        addRootResource(CMISACTION_UPDATE_PROPERTIES, METHOD_POST, new ObjectService.UpdateProperties());
        addRootResource(CMISACTION_SET_CONTENT, METHOD_POST, new ObjectService.SetContentStream());
        addRootResource(CMISACTION_APPEND_CONTENT, METHOD_POST, new ObjectService.AppendContentStream());
        addRootResource(CMISACTION_DELETE_CONTENT, METHOD_POST, new ObjectService.DeleteContentStream());
        addRootResource(CMISACTION_DELETE, METHOD_POST, new ObjectService.DeleteObject());
        addRootResource(CMISACTION_DELETE_TREE, METHOD_POST, new ObjectService.DeleteTree());
        addRootResource(CMISACTION_MOVE, METHOD_POST, new ObjectService.MoveObject());
        addRootResource(CMISACTION_ADD_OBJECT_TO_FOLDER, METHOD_POST, new MultiFilingService.AddObjectToFolder());
        addRootResource(CMISACTION_REMOVE_OBJECT_FROM_FOLDER, METHOD_POST,
                new MultiFilingService.RemoveObjectFromFolder());
        addRootResource(CMISACTION_CHECK_OUT, METHOD_POST, new VersioningService.CheckOut());
        addRootResource(CMISACTION_CANCEL_CHECK_OUT, METHOD_POST, new VersioningService.CancelCheckOut());
        addRootResource(CMISACTION_CHECK_IN, METHOD_POST, new VersioningService.CheckIn());
        addRootResource(CMISACTION_APPLY_POLICY, METHOD_POST, new PolicyService.ApplyPolicy());
        addRootResource(CMISACTION_REMOVE_POLICY, METHOD_POST, new PolicyService.RemovePolicy());
        addRootResource(CMISACTION_APPLY_ACL, METHOD_POST, new AclService.ApplyACL());

        // old OpenCMIS client send invalid selector, support them anyway
        addRootResource("folder", METHOD_GET, new NavigationService.GetFolderTree());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        CallContext context = null;

        try {
            // set default headers
            response.addHeader("Cache-Control", "private, max-age=0");
            response.addHeader("Server", ServerVersion.OPENCMIS_SERVER);

            String method = request.getMethod();

            if (METHOD_GET.equals(method)) {
                request = new QueryStringHttpServletRequestWrapper(request);
            } else if (METHOD_POST.equals(method)) {
                request = new POSTHttpServletRequestWrapper(request, getThresholdOutputStreamFactory());
            } else if (METHOD_HEAD.equals(method)) {
                request = new HEADHttpServletRequestWrapper(request);
                response = new NoBodyHttpServletResponseWrapper(response);
            } else {
                throw new CmisNotSupportedException("Unsupported method");
            }

            // invoke token handler, if necessary
            if (request.getParameter("login") != null && getCallContextHandler() instanceof TokenHandler) {
                ((TokenHandler) getCallContextHandler()).service(getServletContext(), request, response);
                return;
            }

            context = createContext(getServletContext(), request, response);
            dispatch(context, request, response);
        } catch (Exception e) {
            if (e instanceof CmisUnauthorizedException) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"CMIS\"");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required");
            } else if (e instanceof CmisPermissionDeniedException) {
                if (context == null || context.getUsername() == null) {
                    response.setHeader("WWW-Authenticate", "Basic realm=\"CMIS\"");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required");
                } else {
                    printError(context, e, request, response);
                }
            } else {
                printError(context, e, request, response);
            }
        } finally {
            // we are done.
            response.flushBuffer();
        }
    }

    // --------------------------------------------------------

    /**
     * Registers a new repository resource.
     */
    protected void addRepositoryResource(String resource, String httpMethod, ServiceCall serviceCall) {
        repositoryDispatcher.addResource(resource, httpMethod, serviceCall);
    }

    /**
     * Registers a new root resource.
     */
    protected void addRootResource(String resource, String httpMethod, ServiceCall serviceCall) {
        rootDispatcher.addResource(resource, httpMethod, serviceCall);
    }

    private void dispatch(CallContext context, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        BrowserCallContextImpl browserContext = (BrowserCallContextImpl) context;
        CmisService service = null;
        try {
            // get the service
            service = getServiceFactory().getService(context);

            // analyze the path
            String[] pathFragments = HttpUtils.splitPath(request);

            if (pathFragments.length < 1) {
                // root -> repository infos
                repositoryDispatcher.dispatch("", METHOD_GET, context, service, null, request, response);
                return;
            }

            // select dispatcher
            CallUrl callUrl = null;
            if (pathFragments.length == 1) {
                callUrl = CallUrl.REPOSITORY;
            } else if (AbstractBrowserServiceCall.ROOT_PATH_FRAGMENT.equals(pathFragments[1])) {
                callUrl = CallUrl.ROOT;
            }

            if (callUrl == null) {
                throw new CmisNotSupportedException("Unknown operation");
            }

            String method = request.getMethod();
            String repositoryId = pathFragments[0];
            boolean callServiceFound = false;

            if (METHOD_GET.equals(method)) {
                String selector = HttpUtils.getStringParameter(request, Constants.PARAM_SELECTOR);
                String objectId = HttpUtils.getStringParameter(request, PARAM_OBJECT_ID);

                // dispatch
                if (callUrl == CallUrl.REPOSITORY) {
                    if (selector == null) {
                        selector = "";
                    }

                    browserContext.setCallDetails(service, objectId, null, null);
                    callServiceFound = repositoryDispatcher.dispatch(selector, method, browserContext, service,
                            repositoryId, request, response);
                } else if (callUrl == CallUrl.ROOT) {
                    browserContext.setCallDetails(service, objectId, pathFragments, null);

                    // set default method if necessary
                    if (selector == null) {
                        try {
                            BaseTypeId basetype = browserContext.getBaseTypeId();
                            switch (basetype) {
                            case CMIS_DOCUMENT:
                                selector = SELECTOR_CONTENT;
                                break;
                            case CMIS_FOLDER:
                                selector = SELECTOR_CHILDREN;
                                break;
                            default:
                                selector = SELECTOR_OBJECT;
                                break;
                            }
                        } catch (Exception e) {
                            selector = SELECTOR_OBJECT;
                        }
                    }

                    callServiceFound = rootDispatcher.dispatch(selector, method, browserContext, service, repositoryId,
                            request, response);
                }
            } else if (METHOD_POST.equals(method)) {
                String cmisaction = HttpUtils.getStringParameter(request, Constants.CONTROL_CMISACTION);
                String objectId = HttpUtils.getStringParameter(request, Constants.CONTROL_OBJECT_ID);
                String token = HttpUtils.getStringParameter(request, Constants.CONTROL_TOKEN);

                if (cmisaction == null || cmisaction.length() == 0) {
                    throw new CmisNotSupportedException("Unknown action");
                }

                // dispatch
                if (callUrl == CallUrl.REPOSITORY) {
                    browserContext.setCallDetails(service, objectId, null, token);
                    callServiceFound = repositoryDispatcher.dispatch(cmisaction, method, browserContext, service,
                            repositoryId, request, response);
                } else if (callUrl == CallUrl.ROOT) {
                    browserContext.setCallDetails(service, objectId, pathFragments, token);
                    callServiceFound = rootDispatcher.dispatch(cmisaction, method, browserContext, service,
                            repositoryId, request, response);
                }
            }

            // if the dispatcher couldn't find a matching service call
            // -> return an error message
            if (!callServiceFound) {
                throw new CmisNotSupportedException("Unknown operation");
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
    protected int getErrorCode(CmisBaseException ex) {
        return ERROR_SERTVICE_CALL.getErrorCode(ex);
    }

    /**
     * Prints an error as JSON.
     */
    protected void printError(CallContext context, Exception ex, HttpServletRequest request,
            HttpServletResponse response) {
        ERROR_SERTVICE_CALL.printError(context, ex, request, response);
    }

    static class ErrorServiceCall extends AbstractBrowserServiceCall {

        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            // no implementation
        }

        public int getErrorCode(CmisBaseException ex) {
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

        public void printError(CallContext context, Exception ex, HttpServletRequest request,
                HttpServletResponse response) {
            int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            String exceptionName = CmisRuntimeException.EXCEPTION_NAME;

            if (ex instanceof CmisRuntimeException) {
                LOG.error(ex.getMessage(), ex);
            } else if (ex instanceof CmisStorageException) {
                LOG.error(ex.getMessage(), ex);
                statusCode = getErrorCode((CmisStorageException) ex);
                exceptionName = ((CmisStorageException) ex).getExceptionName();
            } else if (ex instanceof CmisBaseException) {
                statusCode = getErrorCode((CmisBaseException) ex);
                exceptionName = ((CmisBaseException) ex).getExceptionName();
            } else if (ex instanceof IOException) {
                LOG.warn(ex.getMessage(), ex);
            } else {
                LOG.error(ex.getMessage(), ex);
            }

            if (response.isCommitted()) {
                LOG.warn("Failed to send error message to client. Response is already committed.", ex);
                return;
            }

            String token = (context instanceof BrowserCallContextImpl ? ((BrowserCallContextImpl) context).getToken()
                    : null);

            if (token == null) {
                response.resetBuffer();
                setStatus(request, response, statusCode);

                String message = ex.getMessage();
                if (!(ex instanceof CmisBaseException)) {
                    message = "An error occurred!";
                }

                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put(ERROR_EXCEPTION, exceptionName);
                jsonResponse.put(ERROR_MESSAGE, message);

                String st = ExceptionHelper.getStacktraceAsString(ex);
                if (st != null) {
                    jsonResponse.put(ERROR_STACKTRACE, st);
                }

                try {
                    writeJSON(jsonResponse, request, response);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    try {
                        response.sendError(statusCode, message);
                    } catch (Exception en) {
                        // there is nothing else we can do
                    }
                }
            } else {
                setStatus(request, response, HttpServletResponse.SC_OK);
                response.setContentType(HTML_MIME_TYPE);
                response.setContentLength(0);

                if (context != null) {
                    setCookie(request, response, context.getRepositoryId(), token,
                            createCookieValue(statusCode, null, exceptionName, ex.getMessage()));
                }
            }
        }
    }
}
