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
package org.apache.opencmis.client.runtime;

import java.util.Map;

import org.apache.opencmis.client.api.Session;
import org.apache.opencmis.client.api.SessionFactory;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.enums.SessionType;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;

/**
 * Default implementation of a session factory. Used by unit tests or
 * applications that depend directly on runtime implementation.
 * <p>
 * <code>
 * SessionFactory sf = new SessionFactoryImpl();<br>
 * Session s = sf.create(...);
 * </code>
 * <p>
 * Alternative factory lookup methods:
 * <p>
 * <p>
 * <code>
 * Context ctx = new DefaultContext();<br>
 * SessionFactory = ctx.lookup(jndi_key);
 * </code>
 */
public class SessionFactoryImpl implements SessionFactory {

	@SuppressWarnings("unchecked")
	public <T extends Session> T createSession(Map<String, String> parameters) {
		Session s = null;
		SessionType t = null;

		// determine session type
		if (parameters.containsKey(SessionParameter.SESSION_TYPE)) {
			t = SessionType.fromValue(parameters
					.get(SessionParameter.SESSION_TYPE));
		} else {
			// default session type if type is not set
			t = SessionType.PERSISTENT;
		}

		switch (t) {
		case PERSISTENT:
			PersistentSessionImpl ps = new PersistentSessionImpl(parameters);
			ps.connect();  // connect session with provider
			s = ps;
			break;
		case TRANSIENT:
			throw new CmisNotSupportedException("SessionType " + t
					+ "not implemented!");
		default:
			throw new CmisRuntimeException("SessionType " + t + "not known!");
		}

		return (T) s;
	}

}
