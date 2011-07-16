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
package org.apache.chemistry.opencmis.client.api;

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;

/**
 * Entry point into the OpenCMIS Client API. The <code>SessionFactory</code>
 * class implementation needs to be retrieved by any runtime lookup call. This
 * can for instance be a J2EE JNDI lookup or an OSGi service lookup.
 * <p>
 * The entries of the parameter map are defined by <code>SessionParameter</code>
 * class which is part of the commons package. Parameters specify connection
 * settings (user name, authentication, connection url, binding type (soap or
 * atom pub) ...).
 * <p>
 * The <code>Session</code> class which is constructed is either the
 * <code>session</code> base class which is the default implementation or it can
 * be derived from that implementing special behavior for the session.
 * <p>
 * Sample code:
 * <p>
 * <code>
 * SessionFactory factory = ... // use a runtime lookup service
 * <br>
 * <br>Map<String, String> parameter = ...
 * <br>parameter.put(SessionParameter.USER, "Otto");
 * <br>parameter.put(SessionParameter.PASSWORD, "****");
 * <br>
 * <br>parameter.put(SessionParameter.ATOMPUB_URL, "http://localhost/cmis/atom");
 * <br>parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
 * <br>parameter.put(SessionParameter.REPOSITORY_ID, "myRepository");
 * <br>...
 * <br>Session session = factory.createSession(parameter);
 * </code>
 */
public interface SessionFactory {

    /**
     * Creates a new session.
     * 
     * @param parameters
     *            a {@code Map} of name/value pairs with parameters for the
     *            session
     * @return a {@link Session} connected to the CMIS repository
     * @throws CmisBaseException
     *             if the connection could not be established
     * 
     * @see SessionParameter
     */
    Session createSession(Map<String, String> parameters);

    /**
     * Returns all repositories that are available at the endpoint. See
     * {@link #createSession(Map)} for parameter details. The parameter
     * {@code SessionParameter.REPOSITORY_ID} should not be set.
     */
    List<Repository> getRepositories(Map<String, String> parameters);

}
