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

import static org.apache.opencmis.commons.impl.Converter.convert;
import static org.apache.opencmis.commons.impl.Converter.convertExtensionHolder;
import static org.apache.opencmis.commons.impl.Converter.convertHolder;
import static org.apache.opencmis.commons.impl.Converter.setExtensionValues;
import static org.apache.opencmis.commons.impl.Converter.setHolderValue;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.impl.jaxb.CmisAccessControlListType;
import org.apache.opencmis.commons.impl.jaxb.CmisContentStreamType;
import org.apache.opencmis.commons.impl.jaxb.CmisException;
import org.apache.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertiesType;
import org.apache.opencmis.commons.impl.jaxb.EnumIncludeRelationships;
import org.apache.opencmis.commons.impl.jaxb.VersioningServicePort;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisVersioningService;

/**
 * CMIS Versioning Service.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
@WebService(endpointInterface = "org.apache.opencmis.commons.impl.jaxb.VersioningServicePort")
public class VersioningService extends AbstractService implements VersioningServicePort {
  @Resource
  WebServiceContext fContext;

  public void cancelCheckOut(String repositoryId, String objectId,
      Holder<CmisExtensionType> extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisVersioningService service = factory.getVersioningService();
      CallContext context = createContext(fContext);

      ExtensionsData extData = convertExtensionHolder(extension);

      service.cancelCheckOut(context, repositoryId, objectId, extData);

      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public void checkIn(String repositoryId, Holder<String> objectId, Boolean major,
      CmisPropertiesType properties, CmisContentStreamType contentStream, String checkinComment,
      List<String> policies, CmisAccessControlListType addAces,
      CmisAccessControlListType removeAces, Holder<CmisExtensionType> extension)
      throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisVersioningService service = factory.getVersioningService();
      CallContext context = createContext(fContext);

      org.apache.opencmis.commons.provider.Holder<String> objectIdHolder = convertHolder(objectId);
      ExtensionsData extData = convertExtensionHolder(extension);

      service.checkIn(context, repositoryId, objectIdHolder, major, convert(properties),
          convert(contentStream), checkinComment, policies, convert(addAces, null), convert(
              removeAces, null), extData, null);

      setHolderValue(objectIdHolder, objectId);
      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public void checkOut(String repositoryId, Holder<String> objectId,
      Holder<CmisExtensionType> extension, Holder<Boolean> contentCopied) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisVersioningService service = factory.getVersioningService();
      CallContext context = createContext(fContext);

      org.apache.opencmis.commons.provider.Holder<String> objectIdHolder = convertHolder(objectId);
      org.apache.opencmis.commons.provider.Holder<Boolean> contentCopiedHolder = new org.apache.opencmis.commons.provider.Holder<Boolean>();
      ExtensionsData extData = convertExtensionHolder(extension);

      service.checkOut(context, repositoryId, objectIdHolder, extData, contentCopiedHolder, null);

      if (contentCopied != null) {
        contentCopied.value = contentCopiedHolder.getValue();
      }

      setHolderValue(objectIdHolder, objectId);
      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public List<CmisObjectType> getAllVersions(String repositoryId, String versionSeriesId,
      String filter, Boolean includeAllowableActions, CmisExtensionType extension)
      throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisVersioningService service = factory.getVersioningService();
      CallContext context = createContext(fContext);

      List<ObjectData> versions = service.getAllVersions(context, repositoryId, versionSeriesId,
          filter, includeAllowableActions, convert(extension), null);

      if (versions == null) {
        return null;
      }

      List<CmisObjectType> result = new ArrayList<CmisObjectType>();
      for (ObjectData object : versions) {
        result.add(convert(object));
      }

      return result;
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public CmisObjectType getObjectOfLatestVersion(String repositoryId, String versionSeriesId,
      Boolean major, String filter, Boolean includeAllowableActions,
      EnumIncludeRelationships includeRelationships, String renditionFilter,
      Boolean includePolicyIds, Boolean includeAcl, CmisExtensionType extension)
      throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisVersioningService service = factory.getVersioningService();
      CallContext context = createContext(fContext);

      return convert(service.getObjectOfLatestVersion(context, repositoryId, versionSeriesId,
          major, filter, includeAllowableActions, convert(IncludeRelationships.class,
              includeRelationships), renditionFilter, includePolicyIds, includeAcl,
          convert(extension), null));
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public CmisPropertiesType getPropertiesOfLatestVersion(String repositoryId,
      String versionSeriesId, Boolean major, String filter, CmisExtensionType extension)
      throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisVersioningService service = factory.getVersioningService();
      CallContext context = createContext(fContext);

      return convert(service.getPropertiesOfLatestVersion(context, repositoryId, versionSeriesId,
          major, filter, convert(extension)));
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

}
