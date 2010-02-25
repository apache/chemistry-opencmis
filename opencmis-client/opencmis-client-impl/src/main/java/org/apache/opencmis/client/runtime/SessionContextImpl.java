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

import java.io.Serializable;

import org.apache.opencmis.client.api.SessionContext;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.exceptions.CmisFilterNotValidException;

public class SessionContextImpl implements SessionContext, Serializable {

	/**
	 * serialization
	 */
	private static final long serialVersionUID = 5310354977995174876L;
	
	private boolean includeAcls = false;
	private boolean includeAllowableActions = false;
	private boolean includePathSegments = false;
	private boolean includesPolicies = false;
	private String propertiesFilter = null;
	private IncludeRelationships relationshipsFilter = null;
	private String renditionsFilter = null;

	public boolean getIncludeAcls() {
		return this.includeAcls;
	}

	public boolean getIncludeAllowableActions() {
		return this.includeAllowableActions;
	}

	public boolean getIncludePathSegments() {
		return this.includePathSegments;
	}

	public boolean getIncludePolicies() {
		return this.includesPolicies;
	}

	public String getIncludeProperties() {
		return this.propertiesFilter;
	}

	public IncludeRelationships getIncludeRelationships() {
		return this.relationshipsFilter;
	}

	public String getIncludeRenditions() {
		return this.renditionsFilter;
	}

	public void setIncludeAcls(boolean includeAcls) {
		this.includeAcls = includeAcls;
	}

	public void setIncludeAllowableActions(boolean includeAllowableActions) {
		this.includeAllowableActions = includeAllowableActions;
	}

	public void setIncludePathSegments(boolean includePathSegments) {
		this.includePathSegments = includePathSegments; // TODO Auto-generated
		// method stub

	}

	public void setIncludePolicies(boolean includePolicies) {
		this.includesPolicies = includePolicies;
	}

	public void setIncludeProperties(String filter)
			throws CmisFilterNotValidException {
		this.propertiesFilter = filter;

	}

	public void setIncludeRelationships(IncludeRelationships filter)
			throws CmisFilterNotValidException {
		this.relationshipsFilter = filter;
	}

	public void setIncludeRenditions(String filter)
			throws CmisFilterNotValidException {
		this.renditionsFilter = filter;

	}

}
