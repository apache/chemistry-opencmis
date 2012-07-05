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
package org.apache.chemistry.opencmis.server.shared;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatcher for the AtomPub and Browser binding servlet.
 */
public class Dispatcher implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String BASE_URL_ATTRIBUTE= "org.apache.chemistry.opencmis.baseurl";
    
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";

    private static final Logger LOG = LoggerFactory.getLogger(Dispatcher.class.getName());

    private final boolean caseSensitive;
    private Map<String, Method> methodMap = new HashMap<String, Method>();

    public Dispatcher() {
        this(true);
    }

    public Dispatcher(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * Connects a resource and HTTP method with a class and a class method.
     */
    public synchronized void addResource(String resource, String httpMethod, Class<?> clazz, String classmethod)
            throws NoSuchMethodException {

        Method m = clazz.getMethod(classmethod, CallContext.class, CmisService.class, String.class,
                HttpServletRequest.class, HttpServletResponse.class);

        methodMap.put(getKey(resource, httpMethod), m);
    }

    /**
     * Find the appropriate method an call it.
     * 
     * @return <code>true</code> if the method was found, <code>false</code>
     *         otherwise.
     */
    public boolean dispatch(String resource, String httpMethod, CallContext context, CmisService service,
            String repositoryId, HttpServletRequest request, HttpServletResponse response) {
        Method m = methodMap.get(getKey(resource, httpMethod));
        if (m == null) {
            return false;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(repositoryId + " / " + resource + ", " + httpMethod + " -> " + m.getName());
        }

        try {
            m.invoke(null, context, service, repositoryId, request, response);
        } catch (IllegalAccessException e) {
            throw new CmisRuntimeException("Internal error!", e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof CmisBaseException) {
                throw (CmisBaseException) e.getCause();
            } else {
                throw new CmisRuntimeException(e.getMessage(), e);
            }
        }

        return true;
    }

    /**
     * Generates a map key from a resource and an HTTP method.
     */
    private String getKey(String resource, String httpMethod) {
        String s = resource + "/" + httpMethod;
        return (caseSensitive ? s : s.toUpperCase());
    }
}
