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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.exceptions.CmisBaseException;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CallContext;

/**
 * Dispatcher for the AtomPub servlet.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class Dispatcher {

  private static final Log LOG = LogFactory.getLog(Dispatcher.class.getName());

  private Map<String, Method> fMethodMap = new HashMap<String, Method>();

  public Dispatcher() {
  }

  /**
   * Connects a resource and HTTP method with a class and a class method.
   */
  public synchronized void addResource(String resource, String httpMethod, Class<?> clazz,
      String classmethod) throws NoSuchMethodException {

    Method m = clazz.getMethod(classmethod, CallContext.class, AbstractServicesFactory.class,
        String.class, HttpServletRequest.class, HttpServletResponse.class);

    fMethodMap.put(getKey(resource, httpMethod), m);
  }

  /**
   * Find the appropriate method an call it.
   * 
   * @return <code>true</code> if the method was found, <code>false</code> otherwise.
   */
  public boolean dispatch(String resource, String httpMethod, CallContext context,
      AbstractServicesFactory factory, String repositoryId, HttpServletRequest request,
      HttpServletResponse response) {

    Method m = fMethodMap.get(getKey(resource, httpMethod));
    if (m == null) {
      return false;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug(repositoryId + " / " + resource + ", " + httpMethod + " -> " + m.getName());
    }

    try {
      m.invoke(null, context, factory, repositoryId, request, response);
    }
    catch (IllegalArgumentException e) {
      throw e;
    }
    catch (IllegalAccessException e) {
      throw new CmisRuntimeException("Internal error!", e);
    }
    catch (InvocationTargetException e) {
      if (e.getCause() instanceof CmisBaseException) {
        throw (CmisBaseException) e.getCause();
      }
      else {
        throw new CmisRuntimeException(e.getMessage(), e);
      }
    }

    return true;
  }

  /**
   * Generates a map key from a resource and an HTTP method.
   */
  private String getKey(String resource, String httpMethod) {
    return resource + "/" + httpMethod;
  }
}
