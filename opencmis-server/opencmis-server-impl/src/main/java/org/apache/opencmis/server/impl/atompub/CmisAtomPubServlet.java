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
package org.apache.opencmis.server.impl.atompub;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.exceptions.CmisBaseException;
import org.apache.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.opencmis.commons.exceptions.CmisFilterNotValidException;
import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.exceptions.CmisStorageException;
import org.apache.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.opencmis.server.impl.CallContextImpl;
import org.apache.opencmis.server.impl.CmisRepositoryContextListener;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CallContext;

/**
 * CMIS AtomPub servlet.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class CmisAtomPubServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static final String PARAM_CALL_CONTEXT_HANDLER = "callContextHandler";

  private static final String METHOD_GET = "GET";
  private static final String METHOD_POST = "POST";
  private static final String METHOD_PUT = "PUT";
  private static final String METHOD_DELETE = "DELETE";

  private static final Log LOG = LogFactory.getLog(CmisAtomPubServlet.class.getName());

  private Dispatcher fDispatcher;
  private CallContextHandler fCallContextHandler;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    // initialize the call context handler
    fCallContextHandler = null;
    String callContextHandlerClass = config.getInitParameter(PARAM_CALL_CONTEXT_HANDLER);
    if (callContextHandlerClass != null) {
      try {
        fCallContextHandler = (CallContextHandler) Class.forName(callContextHandlerClass)
            .newInstance();
      }
      catch (Exception e) {
        throw new ServletException("Could not load call context handler: " + e, e);
      }
    }

    // initialize the dispatcher
    fDispatcher = new Dispatcher();

    try {
      fDispatcher.addResource(AtomPubUtils.RESOURCE_TYPES, METHOD_GET, RepositoryService.class,
          "getTypeChildren");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_TYPESDESC, METHOD_GET, RepositoryService.class,
          "getTypeDescendants");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_TYPE, METHOD_GET, RepositoryService.class,
          "getTypeDefinition");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_CHILDREN, METHOD_GET, NavigationService.class,
          "getChildren");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_DESCENDANTS, METHOD_GET,
          NavigationService.class, "getDescendants");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_FOLDERTREE, METHOD_GET,
          NavigationService.class, "getFolderTree");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_PARENTS, METHOD_GET, NavigationService.class,
          "getObjectParents");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_CHECKEDOUT, METHOD_GET,
          NavigationService.class, "getCheckedOutDocs");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_ENTRY, METHOD_GET, ObjectService.class,
          "getObject");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_OBJECTBYID, METHOD_GET, ObjectService.class,
          "getObject");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_OBJECTBYPATH, METHOD_GET, ObjectService.class,
          "getObjectByPath");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_ALLOWABLEACIONS, METHOD_GET,
          ObjectService.class, "getAllowableActions");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_CONTENT, METHOD_GET, ObjectService.class,
          "getContentStream");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_CONTENT, METHOD_PUT, ObjectService.class,
          "setContentStream");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_CONTENT, METHOD_DELETE, ObjectService.class,
          "deleteContentStream");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_CHILDREN, METHOD_POST, ObjectService.class,
          "create");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_RELATIONSHIPS, METHOD_POST,
          ObjectService.class, "createRelationship");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_ENTRY, METHOD_PUT, ObjectService.class,
          "updateProperties");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_ENTRY, METHOD_DELETE, ObjectService.class,
          "deleteObject");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_DESCENDANTS, METHOD_DELETE,
          ObjectService.class, "deleteTree");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_CHECKEDOUT, METHOD_POST,
          VersioningService.class, "checkOut");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_VERSIONS, METHOD_GET, VersioningService.class,
          "getAllVersions");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_VERSIONS, METHOD_DELETE,
          VersioningService.class, "deleteAllVersions");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_QUERY, METHOD_GET, DiscoveryService.class,
          "query");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_QUERY, METHOD_POST, DiscoveryService.class,
          "query");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_CHANGES, METHOD_GET, DiscoveryService.class,
          "getContentChanges");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_RELATIONSHIPS, METHOD_GET,
          RelationshipService.class, "getObjectRelationships");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_UNFILED, METHOD_POST, MultiFilingService.class,
          "removeObjectFromFolder");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_ACL, METHOD_GET, AclService.class, "getAcl");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_ACL, METHOD_PUT, AclService.class, "applyAcl");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_POLICIES, METHOD_GET, PolicyService.class,
          "getAppliedPolicies");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_POLICIES, METHOD_POST, PolicyService.class,
          "applyPolicy");
      fDispatcher.addResource(AtomPubUtils.RESOURCE_POLICIES, METHOD_DELETE, PolicyService.class,
          "removePolicy");
    }
    catch (NoSuchMethodException e) {
      LOG.error("Cannot initialize dispatcher!", e);
    }
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // create a context object
    CallContext context = null;
    try {
      context = createContext(request);
    }
    catch (Exception e) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Context creation failed: "
          + e.getMessage());
      return;
    }

    // dispatch and handle exceptions
    try {
      dispatch(context, request, response);
    }
    catch (Exception e) {
      if (e instanceof CmisPermissionDeniedException) {
        if (context.getUsername() == null) {
          response.setHeader("WWW-Authenticate", "Basic realm=\"CMIS\"");
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required");
        }
        else {
          response.sendError(getErrorCode((CmisPermissionDeniedException) e), e.getMessage());
        }
      }
      else if (e instanceof CmisRuntimeException) {
        LOG.error(e.getMessage(), e);
        response.sendError(getErrorCode((CmisRuntimeException) e), e.getMessage());
      }
      else if (e instanceof CmisBaseException) {
        response.sendError(getErrorCode((CmisBaseException) e), e.getMessage());
      }
      else {
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
  private void dispatch(CallContext context, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    // get services factory
    AbstractServicesFactory servicesFactory = (AbstractServicesFactory) getServletContext()
        .getAttribute(CmisRepositoryContextListener.SERVICES_FACTORY);

    // analyze the path
    String[] pathFragments = splitPath(request);

    if (pathFragments.length < 2) {
      // root -> service document
      RepositoryService.getRepositories(context, servicesFactory, request, response);
      return;
    }

    String method = request.getMethod();
    String repositoryId = pathFragments[0];
    String resource = pathFragments[1];

    // dispatch
    boolean methodFound = fDispatcher.dispatch(resource, method, context, servicesFactory,
        repositoryId, request, response);

    // if the dispatcher couldn't find a matching method, return an error message
    if (!methodFound) {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Unknown operation");
    }
  }

  /**
   * Decodes basic auth and creates a {@link CallContext} object.
   */
  protected CallContext createContext(HttpServletRequest request) {
    CallContextImpl context = new CallContextImpl(CallContext.BINDING_ATOMPUB);

    // call call text handler
    if (fCallContextHandler != null) {
      Map<String, String> callContextMap = fCallContextHandler.getCallContextMap(request);
      if (callContextMap != null) {
        for (Map.Entry<String, String> e : callContextMap.entrySet()) {
          context.put(e.getKey(), e.getValue());
        }
      }
    }

    // decode range
    String rangeHeader = request.getHeader("Range");
    if (rangeHeader != null) {
      rangeHeader = rangeHeader.trim();
      BigInteger offset = null;
      BigInteger length = null;

      int eq = rangeHeader.indexOf('=');
      int ds = rangeHeader.indexOf('-');
      if ((eq > 0) && (ds > eq)) {
        String offsetStr = rangeHeader.substring(eq + 1, ds).trim();
        if (offsetStr.length() > 0) {
          offset = new BigInteger(offsetStr);
        }

        if (ds < rangeHeader.length()) {
          String lengthStr = rangeHeader.substring(ds + 1).trim();
          if (lengthStr.length() > 0) {
            if (offset == null) {
              length = new BigInteger(lengthStr);
            }
            else {
              length = (new BigInteger(lengthStr)).subtract(offset);
            }
          }

          if (offset != null) {
            context.put(CallContext.OFFSET, offset.toString());
          }
          if (length != null) {
            context.put(CallContext.LENGTH, length.toString());
          }
        }
      }
    }

    return context;
  }

  /**
   * Splits the path into its fragments.
   */
  private String[] splitPath(HttpServletRequest request) {
    String p = request.getPathInfo();
    if (p == null) {
      return new String[0];
    }

    return p.substring(1).split("/");
  }

  /**
   * Translates an exception in an appropriate HTTP error code.
   */
  private int getErrorCode(CmisBaseException ex) {
    if (ex instanceof CmisConstraintException) {
      return 409;
    }
    else if (ex instanceof CmisContentAlreadyExistsException) {
      return 409;
    }
    else if (ex instanceof CmisFilterNotValidException) {
      return 400;
    }
    else if (ex instanceof CmisInvalidArgumentException) {
      return 400;
    }
    else if (ex instanceof CmisNameConstraintViolationException) {
      return 409;
    }
    else if (ex instanceof CmisNotSupportedException) {
      return 405;
    }
    else if (ex instanceof CmisObjectNotFoundException) {
      return 404;
    }
    else if (ex instanceof CmisPermissionDeniedException) {
      return 403;
    }
    else if (ex instanceof CmisStorageException) {
      return 500;
    }
    else if (ex instanceof CmisStreamNotSupportedException) {
      return 403;
    }
    else if (ex instanceof CmisUpdateConflictException) {
      return 409;
    }
    else if (ex instanceof CmisVersioningException) {
      return 409;
    }

    return 500;
  }
}
