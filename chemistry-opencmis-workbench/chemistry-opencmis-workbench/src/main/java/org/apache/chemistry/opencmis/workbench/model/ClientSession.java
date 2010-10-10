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
package org.apache.chemistry.opencmis.workbench.model;

import java.net.Authenticator;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;

public class ClientSession {

    public static final String WORKBENCH_PREFIX = "cmis.workbench.";
    public static final String OBJECT_PREFIX = WORKBENCH_PREFIX + "object.";
    public static final String FOLDER_PREFIX = WORKBENCH_PREFIX + "folder.";
    public static final String ACCEPT_SELF_SIGNED_CERTIFICATES = WORKBENCH_PREFIX + "acceptSelfSignedCertificates";

    public enum Authentication {
        NONE, STANDARD, NTLM
    }

    private static final Set<String> PROPERTY_SET = new HashSet<String>();
    static {
        PROPERTY_SET.add(PropertyIds.OBJECT_ID);
        PROPERTY_SET.add(PropertyIds.OBJECT_TYPE_ID);
        PROPERTY_SET.add(PropertyIds.NAME);
        PROPERTY_SET.add(PropertyIds.CONTENT_STREAM_MIME_TYPE);
        PROPERTY_SET.add(PropertyIds.CONTENT_STREAM_LENGTH);
        PROPERTY_SET.add(PropertyIds.CONTENT_STREAM_FILE_NAME);
        PROPERTY_SET.add(PropertyIds.CREATED_BY);
        PROPERTY_SET.add(PropertyIds.CREATION_DATE);
        PROPERTY_SET.add(PropertyIds.LAST_MODIFIED_BY);
        PROPERTY_SET.add(PropertyIds.LAST_MODIFICATION_DATE);
    }

    private Map<String, String> sessionParameters;
    private List<Repository> repositories;
    private Session session;
    private OperationContext objectOperationContext;
    private OperationContext folderOperationContext;

    public ClientSession(Map<String, String> sessionParameters) {
        if (sessionParameters == null) {
            throw new IllegalArgumentException("Parameters must not be null!");
        }

        connect(sessionParameters);
    }

    public static Map<String, String> createSessionParameters(String url, BindingType binding, String username,
            String password, Authentication authentication) {
        Map<String, String> parameters = new LinkedHashMap<String, String>();

        if (binding == BindingType.WEBSERVICES) {
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());
            parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, url);
            parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, url);
            parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, url);
            parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, url);
            parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, url);
            parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, url);
            parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, url);
            parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, url);
            parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, url);
        } else {
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            parameters.put(SessionParameter.ATOMPUB_URL, url);
        }

        switch (authentication) {
        case STANDARD:
            parameters.put(SessionParameter.USER, username);
            parameters.put(SessionParameter.PASSWORD, password);
            break;
        case NTLM:
            parameters.put(SessionParameter.USER, username);
            parameters.put(SessionParameter.PASSWORD, password);
            parameters.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS,
                    CmisBindingFactory.NTLM_AUTHENTICATION_PROVIDER);
            break;
        }

        // get additional workbench properties from system properties
        Properties sysProps = System.getProperties();
        for (String key : sysProps.stringPropertyNames()) {
            if (key.startsWith(WORKBENCH_PREFIX)) {
                parameters.put(key, sysProps.getProperty(key));
            }
        }

        return parameters;
    }

    private void connect(Map<String, String> sessionParameters) {
        this.sessionParameters = sessionParameters;

        // set a new dummy authenticator
        // don't send previous credentials to another server
        Authenticator.setDefault(new Authenticator() {
        });

        if (Boolean.parseBoolean(sessionParameters.get(ACCEPT_SELF_SIGNED_CERTIFICATES))) {
            acceptSelfSignedCertificates();
        }

        repositories = SessionFactoryImpl.newInstance().getRepositories(sessionParameters);
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public Session createSession(int index) {
        session = repositories.get(index).createSession();
        createOperationContexts();
        return getSession();
    }

    public Session getSession() {
        return session;
    }

    public OperationContext getObjectOperationContext() {
        return objectOperationContext;
    }

    public OperationContext getFolderOperationContext() {
        return folderOperationContext;
    }

    private void createOperationContexts() {
        // object operation context
        setDefault(OBJECT_PREFIX, sessionParameters, ClientOperationContext.FILTER, "*");
        setDefault(OBJECT_PREFIX, sessionParameters, ClientOperationContext.INCLUDE_ACLS, "true");
        setDefault(OBJECT_PREFIX, sessionParameters, ClientOperationContext.INCLUDE_ALLOWABLE_ACTIONS, "true");
        setDefault(OBJECT_PREFIX, sessionParameters, ClientOperationContext.INCLUDE_POLICIES, "true");
        setDefault(OBJECT_PREFIX, sessionParameters, ClientOperationContext.INCLUDE_RELATIONSHIPS,
                IncludeRelationships.BOTH.value());
        setDefault(OBJECT_PREFIX, sessionParameters, ClientOperationContext.RENDITION_FILTER, "*");
        setDefault(OBJECT_PREFIX, sessionParameters, ClientOperationContext.ORDER_BY, null);
        setDefault(OBJECT_PREFIX, sessionParameters, ClientOperationContext.MAX_ITEMS_PER_PAGE, "1000");

        objectOperationContext = new ClientOperationContext(OBJECT_PREFIX, sessionParameters);

        // folder operation context
        if (!sessionParameters.containsKey(FOLDER_PREFIX + ClientOperationContext.FILTER)) {
            ObjectType type = session.getTypeDefinition(BaseTypeId.CMIS_DOCUMENT.value());

            StringBuilder filter = new StringBuilder();
            for (String propId : PROPERTY_SET) {
                PropertyDefinition<?> propDef = type.getPropertyDefinitions().get(propId);
                if (propDef != null) {
                    if (filter.length() > 0) {
                        filter.append(",");
                    }
                    filter.append(propDef.getQueryName());
                }
            }

            sessionParameters.put(FOLDER_PREFIX + ClientOperationContext.FILTER, filter.toString());
        }

        setDefault(FOLDER_PREFIX, sessionParameters, ClientOperationContext.INCLUDE_ACLS, "false");
        setDefault(FOLDER_PREFIX, sessionParameters, ClientOperationContext.INCLUDE_ALLOWABLE_ACTIONS, "false");
        setDefault(FOLDER_PREFIX, sessionParameters, ClientOperationContext.INCLUDE_POLICIES, "false");
        setDefault(FOLDER_PREFIX, sessionParameters, ClientOperationContext.INCLUDE_RELATIONSHIPS,
                IncludeRelationships.NONE.value());
        setDefault(FOLDER_PREFIX, sessionParameters, ClientOperationContext.RENDITION_FILTER, "cmis:none");
        setDefault(FOLDER_PREFIX, sessionParameters, ClientOperationContext.ORDER_BY, null);
        setDefault(FOLDER_PREFIX, sessionParameters, ClientOperationContext.MAX_ITEMS_PER_PAGE, "1000");

        folderOperationContext = new ClientOperationContext(FOLDER_PREFIX, sessionParameters);
    }

    private void setDefault(String prefix, Map<String, String> map, String key, String value) {
        if (!map.containsKey(prefix + key)) {
            map.put(prefix + key, value);
        }
    }

    private void acceptSelfSignedCertificates() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }
}
