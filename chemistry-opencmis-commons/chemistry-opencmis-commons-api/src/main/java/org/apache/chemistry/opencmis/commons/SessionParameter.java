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
package org.apache.chemistry.opencmis.commons;

/**
 * Collection of session parameters.
 */
public final class SessionParameter {

    // utility class
    private SessionParameter() {
    }

    // ---- general parameter ----
    public static final String USER = "org.apache.chemistry.opencmis.user";
    public static final String PASSWORD = "org.apache.chemistry.opencmis.password";

    // --- binding parameter ----
    /** Predefined binding types (see {@code BindingType}) */
    public static final String BINDING_TYPE = "org.apache.chemistry.opencmis.binding.spi.type";

    /** Class name of the binding class. */
    public static final String BINDING_SPI_CLASS = "org.apache.chemistry.opencmis.binding.spi.classname";

    /** URL of the AtomPub service document. */
    public static final String ATOMPUB_URL = "org.apache.chemistry.opencmis.binding.atompub.url";

    /** WSDL URLs for Web Services. */
    public static final String WEBSERVICES_REPOSITORY_SERVICE = "org.apache.chemistry.opencmis.binding.webservices.RepositoryService";
    public static final String WEBSERVICES_NAVIGATION_SERVICE = "org.apache.chemistry.opencmis.binding.webservices.NavigationService";
    public static final String WEBSERVICES_OBJECT_SERVICE = "org.apache.chemistry.opencmis.binding.webservices.ObjectService";
    public static final String WEBSERVICES_VERSIONING_SERVICE = "org.apache.chemistry.opencmis.binding.webservices.VersioningService";
    public static final String WEBSERVICES_DISCOVERY_SERVICE = "org.apache.chemistry.opencmis.binding.webservices.DiscoveryService";
    public static final String WEBSERVICES_RELATIONSHIP_SERVICE = "org.apache.chemistry.opencmis.binding.webservices.RelationshipService";
    public static final String WEBSERVICES_MULTIFILING_SERVICE = "org.apache.chemistry.opencmis.binding.webservices.MultiFilingService";
    public static final String WEBSERVICES_POLICY_SERVICE = "org.apache.chemistry.opencmis.binding.webservices.PolicyService";
    public static final String WEBSERVICES_ACL_SERVICE = "org.apache.chemistry.opencmis.binding.webservices.ACLService";
    public static final String WEBSERVICES_MEMORY_THRESHOLD = "org.apache.chemistry.opencmis.binding.webservices.memoryThreshold";

    public static final String WEBSERVICES_PORT_PROVIDER_CLASS = "org.apache.chemistry.opencmis.binding.webservices.portprovider.classname";

    /** URL of the Browser Binding entry point. */
    public static final String BROWSER_URL = "org.apache.chemistry.opencmis.binding.browser.url";
    public static final String BROWSER_SUCCINCT = "org.apache.chemistry.opencmis.binding.browser.succinct";

    /** Factory class name for the local binding. */
    public static final String LOCAL_FACTORY = "org.apache.chemistry.opencmis.binding.local.classname";

    /** Class name of the authentication provider. */
    public static final String AUTHENTICATION_PROVIDER_CLASS = "org.apache.chemistry.opencmis.binding.auth.classname";

    /**
     * Toggle for HTTP basic authentication. Evaluated by the standard
     * authentication provider.
     */
    public static final String AUTH_HTTP_BASIC = "org.apache.chemistry.opencmis.binding.auth.http.basic";

    /**
     * Toggle for WS-Security UsernameToken authentication. Evaluated by the
     * standard authentication provider.
     */
    public static final String AUTH_SOAP_USERNAMETOKEN = "org.apache.chemistry.opencmis.binding.auth.soap.usernametoken";

    // --- connection ---

    public static final String COMPRESSION = "org.apache.chemistry.opencmis.binding.compression";
    public static final String CLIENT_COMPRESSION = "org.apache.chemistry.opencmis.binding.clientcompression";

    public static final String COOKIES = "org.apache.chemistry.opencmis.binding.cookies";
    
    public static final String HEADER = "org.apache.chemistry.opencmis.binding.header";

    public static final String CONNECT_TIMEOUT = "org.apache.chemistry.opencmis.binding.connecttimeout";
    public static final String READ_TIMEOUT = "org.apache.chemistry.opencmis.binding.readtimeout";

    public static final String PROXY_USER = "org.apache.chemistry.opencmis.binding.proxyuser";
    public static final String PROXY_PASSWORD = "org.apache.chemistry.opencmis.binding.proxypassword";

    // --- cache ---

    public static final String CACHE_SIZE_OBJECTS = "org.apache.chemistry.opencmis.cache.objects.size";
    public static final String CACHE_TTL_OBJECTS = "org.apache.chemistry.opencmis.cache.objects.ttl";
    public static final String CACHE_SIZE_PATHTOID = "org.apache.chemistry.opencmis.cache.pathtoid.size";
    public static final String CACHE_TTL_PATHTOID = "org.apache.chemistry.opencmis.cache.pathtoid.ttl";
    public static final String CACHE_PATH_OMIT = "org.apache.chemistry.opencmis.cache.path.omit";

    public static final String CACHE_SIZE_REPOSITORIES = "org.apache.chemistry.opencmis.binding.cache.repositories.size";
    public static final String CACHE_SIZE_TYPES = "org.apache.chemistry.opencmis.binding.cache.types.size";
    public static final String CACHE_SIZE_LINKS = "org.apache.chemistry.opencmis.binding.cache.links.size";

    // --- session control ---

    public static final String LOCALE_ISO639_LANGUAGE = "org.apache.chemistry.opencmis.locale.iso639";
    public static final String LOCALE_ISO3166_COUNTRY = "org.apache.chemistry.opencmis.locale.iso3166";
    public static final String LOCALE_VARIANT = "org.apache.chemistry.opencmis.locale.variant";

    public static final String OBJECT_FACTORY_CLASS = "org.apache.chemistry.opencmis.objectfactory.classname";
    public static final String CACHE_CLASS = "org.apache.chemistry.opencmis.cache.classname";

    public static final String REPOSITORY_ID = "org.apache.chemistry.opencmis.session.repository.id";
}
