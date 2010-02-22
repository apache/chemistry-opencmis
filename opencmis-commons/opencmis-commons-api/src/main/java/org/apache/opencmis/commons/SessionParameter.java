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
package org.apache.opencmis.commons;

/**
 * Collection of session parameters.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public final class SessionParameter {

	private SessionParameter() {
	}

	// ---- general parameter ----
	public static final String USER = "org.apache.opencmis.user";
	public static final String PASSWORD = "org.apache.opencmis.password";

	// ---- provider parameter ----
	/** Predefined binding types (see {@code BindingType}) */
	public static final String BINDING_TYPE = "org.apache.opencmis.provider.binding.type";
	
	/** Class name of the binding class. */
	public static final String BINDING_SPI_CLASS = "org.apache.opencmis.provider.binding.classname";

	/** URL of the AtomPub service document. */
	public static final String ATOMPUB_URL = "org.apache.opencmis.provider.atompub.url";

	public static final String WEBSERVICES_REPOSITORY_SERVICE = "org.apache.opencmis.provider.webservices.RepositoryService";
	public static final String WEBSERVICES_NAVIGATION_SERVICE = "org.apache.opencmis.provider.webservices.NavigationService";
	public static final String WEBSERVICES_OBJECT_SERVICE = "org.apache.opencmis.provider.webservices.ObjectService";
	public static final String WEBSERVICES_VERSIONING_SERVICE = "org.apache.opencmis.provider.webservices.VersioningService";
	public static final String WEBSERVICES_DISCOVERY_SERVICE = "org.apache.opencmis.provider.webservices.DiscoveryService";
	public static final String WEBSERVICES_RELATIONSHIP_SERVICE = "org.apache.opencmis.provider.webservices.RelationshipService";
	public static final String WEBSERVICES_MULTIFILING_SERVICE = "org.apache.opencmis.provider.webservices.MultiFilingService";
	public static final String WEBSERVICES_POLICY_SERVICE = "org.apache.opencmis.provider.webservices.PolicyService";
	public static final String WEBSERVICES_ACL_SERVICE = "org.apache.opencmis.provider.webservices.ACLService";

	/** Class name of the authentication provider. */
	public static final String AUTHENTICATION_PROVIDER_CLASS = "org.apache.opencmis.provider.auth.classname";

	/**
	 * Toggle for HTTP basic authentication. Evaluated by the standard
	 * authentication provider.
	 */
	public static final String AUTH_HTTP_BASIC = "org.apache.opencmis.provider.auth.http.basic";
	/**
	 * Toggle for WS-Security UsernameToken authentication. Evaluated by the
	 * standard authentication provider.
	 */
	public static final String AUTH_SOAP_USERNAMETOKEN = "org.apache.opencmis.provider.auth.soap.usernametoken";

	public static final String CACHE_SIZE_REPOSITORIES = "org.apache.opencmis.provider.cache.repositories.size";
	public static final String CACHE_SIZE_TYPES = "org.apache.opencmis.provider.cache.types.size";
	public static final String CACHE_SIZE_OBJECTS = "org.apache.opencmis.provider.cache.objects.size";

	// --- session control ---

	public static final String LOCALE_ISO639_LANGUAGE = "org.apache.opencmis.locale.iso639";
	public static final String LOCALE_ISO3166_COUNTRY = "org.apache.opencmis.locale.iso3166";
	public static final String LOCALE_VARIANT = "org.apache.opencmis.locale.variant";

	public static final String SESSION_TYPE = "org.apache.opencmis.session.type";
	public static final String REPOSITORY_ID = "org.apache.opencmis.session.repository.id";
}
