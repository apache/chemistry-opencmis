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

import static org.apache.opencmis.commons.impl.Converter.convertExtensionHolder;
import static org.apache.opencmis.commons.impl.Converter.setExtensionValues;

import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.jaxb.CmisException;
import org.apache.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.opencmis.commons.impl.jaxb.MultiFilingServicePort;
import org.apache.opencmis.commons.provider.MultiFilingService;

/**
 * MultiFiling Service Web Services client.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class MultiFilingServiceImpl extends AbstractWebServicesService implements
    MultiFilingService {

  private PortProvider fPortProvider;

  /**
   * Constructor.
   */
  public MultiFilingServiceImpl(Session session, PortProvider portProvider) {
    setSession(session);
    fPortProvider = portProvider;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.MultiFilingService#addObjectToFolder(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void addObjectToFolder(String repositoryId, String objectId, String folderId,
      Boolean allVersions, ExtensionsData extension) {
    MultiFilingServicePort port = fPortProvider.getMultiFilingServicePort();

    try {
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.addObjectToFolder(repositoryId, objectId, folderId, allVersions, portExtension);

      setExtensionValues(portExtension, extension);
    }
    catch (CmisException e) {
      throw convertException(e);
    }
    catch (Exception e) {
      throw new CmisRuntimeException("Error: " + e.getMessage(), e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.MultiFilingService#removeObjectFromFolder(java.lang.String,
   * java.lang.String, java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void removeObjectFromFolder(String repositoryId, String objectId, String folderId,
      ExtensionsData extension) {
    MultiFilingServicePort port = fPortProvider.getMultiFilingServicePort();

    try {
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.removeObjectFromFolder(repositoryId, objectId, folderId, portExtension);

      setExtensionValues(portExtension, extension);
    }
    catch (CmisException e) {
      throw convertException(e);
    }
    catch (Exception e) {
      throw new CmisRuntimeException("Error: " + e.getMessage(), e);
    }
  }

}
