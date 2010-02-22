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
package org.apache.opencmis.server.spi;

import java.util.Map;

import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;

/**
 * This class provides the CMIS service instances.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public abstract class AbstractServicesFactory {

  /**
   * Initializes the factory instance.
   */
  public void init(Map<String, String> parameters) {
  }

  /**
   * Cleans up the the factory instance.
   */
  public void destroy() {
  }

  /**
   * Returns the CMIS Repository Service object.
   */
  public CmisRepositoryService getRepositoryService() {
    throw new CmisNotSupportedException("Repository Service not supported!");
  }

  /**
   * Returns the CMIS Navigation Service object.
   */
  public CmisNavigationService getNavigationService() {
    throw new CmisNotSupportedException("Navigation Service not supported!");
  }

  /**
   * Returns the CMIS Object Service object.
   */
  public CmisObjectService getObjectService() {
    throw new CmisNotSupportedException("Object Service not supported!");
  }

  /**
   * Returns the CMIS Versioning Service object.
   */
  public CmisVersioningService getVersioningService() {
    throw new CmisNotSupportedException("Versioning Service not supported!");
  }

  /**
   * Returns the CMIS Relationship Service object.
   */
  public CmisRelationshipService getRelationshipService() {
    throw new CmisNotSupportedException("Releationship Service not supported!");
  }

  /**
   * Returns the CMIS Discovery Service object.
   */
  public CmisDiscoveryService getDiscoveryService() {
    throw new CmisNotSupportedException("Discovery Service not supported!");
  }

  /**
   * Returns the CMIS MultiFiling Service object.
   */
  public CmisMultiFilingService getMultiFilingService() {
    throw new CmisNotSupportedException("MultiFiling Service not supported!");
  }

  /**
   * Returns the CMIS ACL Service object.
   */
  public CmisAclService getAclService() {
    throw new CmisNotSupportedException("ACL Service not supported!");
  }

  /**
   * Returns the CMIS Policy Service object.
   */
  public CmisPolicyService getPolicyService() {
    throw new CmisNotSupportedException("Policy Service not supported!");
  }
}
