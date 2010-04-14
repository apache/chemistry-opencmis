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
package org.apache.opencmis.server.impl.webservices;

import static org.apache.opencmis.commons.impl.Converter.convertExtensionHolder;
import static org.apache.opencmis.commons.impl.Converter.setExtensionValues;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.impl.jaxb.CmisException;
import org.apache.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.opencmis.commons.impl.jaxb.MultiFilingServicePort;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisMultiFilingService;

/**
 * CMIS MultiFiling Service.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
@WebService(endpointInterface = "org.apache.opencmis.commons.impl.jaxb.MultiFilingServicePort")
public class MultiFilingService extends AbstractService implements MultiFilingServicePort {
  @Resource
  WebServiceContext fContext;

  public void addObjectToFolder(String repositoryId, String objectId, String folderId,
      Boolean allVersions, Holder<CmisExtensionType> extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisMultiFilingService service = factory.getMultiFilingService();
      CallContext context = createContext(fContext);

      ExtensionsData extData = convertExtensionHolder(extension);

      service.addObjectToFolder(context, repositoryId, objectId, folderId, allVersions, extData,
          null);

      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public void removeObjectFromFolder(String repositoryId, String objectId, String folderId,
      Holder<CmisExtensionType> extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisMultiFilingService service = factory.getMultiFilingService();
      CallContext context = createContext(fContext);

      ExtensionsData extData = convertExtensionHolder(extension);

      service.removeObjectFromFolder(context, repositoryId, objectId, folderId, extData, null);

      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

}
