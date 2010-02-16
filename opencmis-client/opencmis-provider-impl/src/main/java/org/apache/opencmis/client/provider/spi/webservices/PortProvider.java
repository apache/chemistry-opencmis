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
package org.apache.opencmis.client.provider.spi.webservices;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.client.provider.impl.CmisProviderHelper;
import org.apache.opencmis.client.provider.spi.AbstractAuthenticationProvider;
import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.exceptions.CmisBaseException;
import org.apache.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.jaxb.ACLService;
import org.apache.opencmis.commons.impl.jaxb.ACLServicePort;
import org.apache.opencmis.commons.impl.jaxb.DiscoveryService;
import org.apache.opencmis.commons.impl.jaxb.DiscoveryServicePort;
import org.apache.opencmis.commons.impl.jaxb.MultiFilingService;
import org.apache.opencmis.commons.impl.jaxb.MultiFilingServicePort;
import org.apache.opencmis.commons.impl.jaxb.NavigationService;
import org.apache.opencmis.commons.impl.jaxb.NavigationServicePort;
import org.apache.opencmis.commons.impl.jaxb.ObjectService;
import org.apache.opencmis.commons.impl.jaxb.ObjectServicePort;
import org.apache.opencmis.commons.impl.jaxb.PolicyService;
import org.apache.opencmis.commons.impl.jaxb.PolicyServicePort;
import org.apache.opencmis.commons.impl.jaxb.RelationshipService;
import org.apache.opencmis.commons.impl.jaxb.RelationshipServicePort;
import org.apache.opencmis.commons.impl.jaxb.RepositoryService;
import org.apache.opencmis.commons.impl.jaxb.RepositoryServicePort;
import org.apache.opencmis.commons.impl.jaxb.VersioningService;
import org.apache.opencmis.commons.impl.jaxb.VersioningServicePort;
import org.w3c.dom.Element;

import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.developer.StreamingAttachmentFeature;
import com.sun.xml.ws.developer.WSBindingProvider;

/**
 * Provides CMIS Web Services port objects. Handles authentication headers.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class PortProvider {

  private static Log log = LogFactory.getLog(PortProvider.class);

  public static final String CMIS_NAMESPACE = "http://docs.oasis-open.org/ns/cmis/ws/200908/";

  public static final String REPOSITORY_SERVICE = "RepositoryService";
  public static final String OBJECT_SERVICE = "ObjectService";
  public static final String DISCOVERY_SERVICE = "DiscoveryService";
  public static final String NAVIGATION_SERVICE = "NavigationService";
  public static final String MULTIFILING_SERVICE = "MulifilingService";
  public static final String VERSIONING_SERVICE = "VersioningService";
  public static final String RELATIONSHIP_SERVICE = "RelationshipService";
  public static final String POLICY_SERVICE = "PolicyService";
  public static final String ACL_SERVICE = "ACLService";

  private static final int CHUNK_SIZE = 64 * 1024;

  private Session fSession;

  /**
   * Constructor.
   */
  public PortProvider(Session session) {
    fSession = session;
  }

  /**
   * Return the Repository Service port object.
   */
  public RepositoryServicePort getRepositoryServicePort() {
    return (RepositoryServicePort) getPortObject(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE);
  }

  /**
   * Return the Navigation Service port object.
   */
  public NavigationServicePort getNavigationServicePort() {
    return (NavigationServicePort) getPortObject(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE);
  }

  /**
   * Return the Object Service port object.
   */
  public ObjectServicePort getObjectServicePort() {
    return (ObjectServicePort) getPortObject(SessionParameter.WEBSERVICES_OBJECT_SERVICE);
  }

  /**
   * Return the Versioning Service port object.
   */
  public VersioningServicePort getVersioningServicePort() {
    return (VersioningServicePort) getPortObject(SessionParameter.WEBSERVICES_VERSIONING_SERVICE);
  }

  /**
   * Return the Discovery Service port object.
   */
  public DiscoveryServicePort getDiscoveryServicePort() {
    return (DiscoveryServicePort) getPortObject(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE);
  }

  /**
   * Return the MultiFiling Service port object.
   */
  public MultiFilingServicePort getMultiFilingServicePort() {
    return (MultiFilingServicePort) getPortObject(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE);
  }

  /**
   * Return the Relationship Service port object.
   */
  public RelationshipServicePort getRelationshipServicePort() {
    return (RelationshipServicePort) getPortObject(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE);
  }

  /**
   * Return the Policy Service port object.
   */
  public PolicyServicePort getPolicyServicePort() {
    return (PolicyServicePort) getPortObject(SessionParameter.WEBSERVICES_POLICY_SERVICE);
  }

  /**
   * Return the ACL Service port object.
   */
  public ACLServicePort getACLServicePort() {
    return (ACLServicePort) getPortObject(SessionParameter.WEBSERVICES_ACL_SERVICE);
  }

  // ---- internal ----

  /**
   * Gets a port object from the session or (re-)initializes the port objects.
   */
  @SuppressWarnings("unchecked")
  private Object getPortObject(String portKey) {
    Map<String, Object> portMap = (Map<String, Object>) fSession.get(SpiSessionParameter.PORTS);

    // does the port map exist?
    if (portMap == null) {
      // create and store map
      portMap = new HashMap<String, Object>();
      fSession.put(SpiSessionParameter.PORTS, portMap, true);
    }

    // is the port in the port map?
    if (!portMap.containsKey(portKey)) {
      // create and add port object
      portMap.put(portKey, initPortObject(portKey));
    }

    return portMap.get(portKey);
  }

  /**
   * Creates a port object.
   */
  private Object initPortObject(String portKey) {
    Object portObject = null;

    if (log.isDebugEnabled()) {
      log.debug("Initializing Web Service " + portKey + "...");
    }

    try {
      // get WSDL URL
      URL wsdlUrl = new URL((String) fSession.get(portKey));

      // build the requested port object
      if (SessionParameter.WEBSERVICES_REPOSITORY_SERVICE.equals(portKey)) {
        RepositoryService service = new RepositoryService(wsdlUrl, new QName(CMIS_NAMESPACE,
            REPOSITORY_SERVICE));
        portObject = service.getRepositoryServicePort(new MTOMFeature());
      }
      else if (SessionParameter.WEBSERVICES_NAVIGATION_SERVICE.equals(portKey)) {
        NavigationService service = new NavigationService(wsdlUrl, new QName(CMIS_NAMESPACE,
            NAVIGATION_SERVICE));
        portObject = service.getNavigationServicePort(new MTOMFeature());
      }
      else if (SessionParameter.WEBSERVICES_OBJECT_SERVICE.equals(portKey)) {
        ObjectService service = new ObjectService(wsdlUrl,
            new QName(CMIS_NAMESPACE, OBJECT_SERVICE));
        portObject = service.getObjectServicePort(new MTOMFeature(),
            new StreamingAttachmentFeature(null, true, 4 * 1024 * 1024));
        ((BindingProvider) portObject).getRequestContext().put(
            JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, CHUNK_SIZE);
      }
      else if (SessionParameter.WEBSERVICES_VERSIONING_SERVICE.equals(portKey)) {
        VersioningService service = new VersioningService(wsdlUrl, new QName(CMIS_NAMESPACE,
            VERSIONING_SERVICE));
        portObject = service.getVersioningServicePort(new MTOMFeature());
      }
      else if (SessionParameter.WEBSERVICES_DISCOVERY_SERVICE.equals(portKey)) {
        DiscoveryService service = new DiscoveryService(wsdlUrl, new QName(CMIS_NAMESPACE,
            DISCOVERY_SERVICE));
        portObject = service.getDiscoveryServicePort(new MTOMFeature());
      }
      else if (SessionParameter.WEBSERVICES_MULTIFILING_SERVICE.equals(portKey)) {
        MultiFilingService service = new MultiFilingService(wsdlUrl, new QName(CMIS_NAMESPACE,
            MULTIFILING_SERVICE));
        portObject = service.getMultiFilingServicePort(new MTOMFeature());
      }
      else if (SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE.equals(portKey)) {
        RelationshipService service = new RelationshipService(wsdlUrl, new QName(CMIS_NAMESPACE,
            RELATIONSHIP_SERVICE));
        portObject = service.getRelationshipServicePort(new MTOMFeature());
      }
      else if (SessionParameter.WEBSERVICES_POLICY_SERVICE.equals(portKey)) {
        PolicyService service = new PolicyService(wsdlUrl,
            new QName(CMIS_NAMESPACE, POLICY_SERVICE));
        portObject = service.getPolicyServicePort(new MTOMFeature());
      }
      else if (SessionParameter.WEBSERVICES_ACL_SERVICE.equals(portKey)) {
        ACLService service = new ACLService(wsdlUrl, new QName(CMIS_NAMESPACE, ACL_SERVICE));
        portObject = service.getACLServicePort(new MTOMFeature());
      }

      // add SOAP and HTTP authentication headers
      AbstractAuthenticationProvider authProvider = CmisProviderHelper
          .getAuthenticationProvider(fSession);
      if (authProvider != null) {
        // SOAP header
        Element soapHeader = authProvider.getSOAPHeaders(portObject);
        if (soapHeader != null) {
          ((WSBindingProvider) portObject).setOutboundHeaders(Headers.create(soapHeader));
        }

        // HTTP header
        Map<String, List<String>> httpHeaders = authProvider.getHTTPHeaders(wsdlUrl.toString());
        if (httpHeaders != null) {
          ((BindingProvider) portObject).getRequestContext().put(
              MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);
        }
      }
    }
    catch (CmisBaseException ce) {
      throw ce;
    }
    catch (Exception e) {
      throw new CmisConnectionException("Cannot initalize Web Services port object [" + portKey
          + "]: " + e.getMessage(), e);
    }

    // we have no object ... strange ...
    if (portObject == null) {
      throw new CmisRuntimeException("Cannot find Web Services port object [" + portKey + "]!");
    }

    return portObject;
  }
}
