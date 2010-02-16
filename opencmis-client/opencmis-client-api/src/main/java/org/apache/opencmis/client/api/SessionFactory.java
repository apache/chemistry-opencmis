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
package org.apache.opencmis.client.api;

import java.util.Map;

/**
 * Entry point into the OpenCMIS Client API. The <code>SessionFactory</code>
 * class implementation needs to be retrieved by any runtime lookup call. This
 * can for instance be a J2EE JNDI lookup or an OSGi service lookup.
 * <p>
 * The entries of parameter map are defined by <code>SessionParameter</code>
 * class which is part of the commons package. Parameters specify connection
 * settings (user name, authentication, connection url, binding type (soap or
 * atom pub) ...).
 * <p>
 * The <code>Session</code> class which is constructed is either the
 * <code>session</code> base class which is the default implementation or it can
 * be derived from that implementing special behavior for the session. Which
 * session finally is returned can be controlled by the
 * <code>SessionParameter.SESSION_TYPE</code> parameter.
 * <p>
 * Example Coding:
 * <p>
 * <code>
 * SessionFactory factory = ... // use a runtime lookup service
 * <br>Map<String, String> parameters = ...
 * <br>
 * <br>parameters.put(SessionParameter.USER, "username");
 * <br>parameters.put(SessionParameter.URL, "http://...");
 * <br>parameters.put(SessionParameter.SESSION_TYPE, SessionType.TRANSIENT.value());
 * <br> ...
 * <br>
 * <br>TrasnientSession s = factory.createSession(parameters);
 * </code>
 *<p>
 * If the <code>SessionType</code> parameter is not specified then the default
 * session is returned.
 * 
 */
public interface SessionFactory {

	/**
	 * Obtain a new session.
	 * 
	 * @param parameters
	 *            a {@code Map} of name/value pairs with parameters for the
	 *            session.
	 * @return a {@code session} to the CMIS repository specified by the {@code
	 *         parameters}.
	 */
	<T extends Session> T createSession(Map<String, String> parameters);

}
