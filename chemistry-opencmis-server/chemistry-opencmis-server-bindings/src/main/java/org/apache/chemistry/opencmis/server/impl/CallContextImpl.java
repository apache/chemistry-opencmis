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
package org.apache.chemistry.opencmis.server.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.api.server.CallContext;

/**
 * Implementation of the {@link CallContext} interface.
 */
public class CallContextImpl implements CallContext {

	private String binding;
	private boolean objectInfoRequired;
	private Map<String, String> parameter = new HashMap<String, String>();

	public CallContextImpl(String binding, String repositoryId, boolean objectInfoRequired) {
		this.binding = binding;
		this.objectInfoRequired = objectInfoRequired;
		put(REPOSITORY_ID, repositoryId);
	}

	public String getBinding() {
		return binding;
	}

	public boolean isObjectInfoRequired() {
		return objectInfoRequired;
	}

	public String get(String key) {
		return parameter.get(key);
	}

	public String getRepositoryId() {
		return get(REPOSITORY_ID);
	}

	public String getUsername() {
		return get(USERNAME);
	}

	public String getPassword() {
		return get(PASSWORD);
	}

	public String getLocale() {
		return get(LOCALE);
	}

	/**
	 * Adds a parameter.
	 */
	public void put(String key, String value) {
		parameter.put(key, value);
	}

	/**
	 * Removes a parameter.
	 */
	public String remove(String key) {
		return parameter.remove(key);
	}

}
