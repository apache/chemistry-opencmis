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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.impl.jaxb.CmisAccessControlListType;
import org.apache.opencmis.commons.impl.jaxb.CmisAllowableActionsType;
import org.apache.opencmis.commons.impl.jaxb.CmisContentStreamType;
import org.apache.opencmis.commons.impl.jaxb.CmisException;
import org.apache.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertiesType;
import org.apache.opencmis.commons.impl.jaxb.CmisRenditionType;
import org.apache.opencmis.commons.impl.jaxb.EnumIncludeRelationships;
import org.apache.opencmis.commons.impl.jaxb.EnumUnfileObject;
import org.apache.opencmis.commons.impl.jaxb.EnumVersioningState;
import org.apache.opencmis.commons.impl.jaxb.ObjectServicePort;
import org.apache.opencmis.commons.impl.jaxb.DeleteTreeResponse.FailedToDelete;
import org.apache.opencmis.commons.provider.RenditionData;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CallContext;
import org.apache.opencmis.server.spi.CmisObjectService;

import com.sun.xml.ws.developer.StreamingAttachment;

/**
 * CMIS Object Service.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
@StreamingAttachment(parseEagerly = true, memoryThreshold = 4 * 1024 * 1204)
@WebService(endpointInterface = "org.apache.opencmis.commons.impl.jaxb.ObjectServicePort")
public class ObjectService extends AbstractService implements ObjectServicePort {
  @Resource
  WebServiceContext fContext;

  public void createDocument(String repositoryId, CmisPropertiesType properties, String folderId,
      CmisContentStreamType contentStream, EnumVersioningState versioningState,
      List<String> policies, CmisAccessControlListType addAces,
      CmisAccessControlListType removeAces, Holder<CmisExtensionType> extension,
      Holder<String> objectId) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      ExtensionsData extData = convertExtensionHolder(extension);

      String id = service.createDocument(context, repositoryId, convert(properties), folderId,
          convert(contentStream), convert(VersioningState.class, versioningState), policies,
          convert(addAces, null), convert(removeAces, null), extData);

      if (objectId != null) {
        objectId.value = id;
      }

      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public void createDocumentFromSource(String repositoryId, String sourceId,
      CmisPropertiesType properties, String folderId, EnumVersioningState versioningState,
      List<String> policies, CmisAccessControlListType addAces,
      CmisAccessControlListType removeAces, Holder<CmisExtensionType> extension,
      Holder<String> objectId) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      ExtensionsData extData = convertExtensionHolder(extension);

      String id = service.createDocumentFromSource(context, repositoryId, sourceId,
          convert(properties), folderId, convert(VersioningState.class, versioningState), policies,
          convert(addAces, null), convert(removeAces, null), extData);

      if (objectId != null) {
        objectId.value = id;
      }

      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public void createFolder(String repositoryId, CmisPropertiesType properties, String folderId,
      List<String> policies, CmisAccessControlListType addAces,
      CmisAccessControlListType removeAces, Holder<CmisExtensionType> extension,
      Holder<String> objectId) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      ExtensionsData extData = convertExtensionHolder(extension);

      String id = service.createFolder(context, repositoryId, convert(properties), folderId,
          policies, convert(addAces, null), convert(removeAces, null), extData);

      if (objectId != null) {
        objectId.value = id;
      }

      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public void createPolicy(String repositoryId, CmisPropertiesType properties, String folderId,
      List<String> policies, CmisAccessControlListType addAces,
      CmisAccessControlListType removeAces, Holder<CmisExtensionType> extension,
      Holder<String> objectId) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      ExtensionsData extData = convertExtensionHolder(extension);

      String id = service.createPolicy(context, repositoryId, convert(properties), folderId,
          policies, convert(addAces, null), convert(removeAces, null), extData);

      if (objectId != null) {
        objectId.value = id;
      }

      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public void createRelationship(String repositoryId, CmisPropertiesType properties,
      List<String> policies, CmisAccessControlListType addAces,
      CmisAccessControlListType removeAces, Holder<CmisExtensionType> extension,
      Holder<String> objectId) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      ExtensionsData extData = convertExtensionHolder(extension);

      String id = service.createRelationship(context, repositoryId, convert(properties), policies,
          convert(addAces, null), convert(removeAces, null), extData);

      if (objectId != null) {
        objectId.value = id;
      }

      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public void deleteContentStream(String repositoryId, Holder<String> objectId,
      Holder<String> changeToken, Holder<CmisExtensionType> extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      org.apache.opencmis.commons.provider.Holder<String> objectIdHolder = convertHolder(objectId);
      org.apache.opencmis.commons.provider.Holder<String> changeTokenHolder = convertHolder(changeToken);
      ExtensionsData extData = convertExtensionHolder(extension);

      service
          .deleteContentStream(context, repositoryId, objectIdHolder, changeTokenHolder, extData);

      setHolderValue(objectIdHolder, objectId);
      setHolderValue(changeTokenHolder, changeToken);
      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public void deleteObject(String repositoryId, String objectId, Boolean allVersions,
      Holder<CmisExtensionType> extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      ExtensionsData extData = convertExtensionHolder(extension);

      service.deleteObjectOrCancelCheckOut(context, repositoryId, objectId, allVersions, extData);

      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public FailedToDelete deleteTree(String repositoryId, String folderId, Boolean allVersions,
      EnumUnfileObject unfileObjects, Boolean continueOnFailure, CmisExtensionType extension)
      throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      return convert(service.deleteTree(context, repositoryId, folderId, allVersions, convert(
          UnfileObjects.class, unfileObjects), continueOnFailure, convert(extension)));
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public CmisAllowableActionsType getAllowableActions(String repositoryId, String objectId,
      CmisExtensionType extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      return convert(service.getAllowableActions(context, repositoryId, objectId,
          convert(extension)));
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public CmisContentStreamType getContentStream(String repositoryId, String objectId,
      String streamId, BigInteger offset, BigInteger length, CmisExtensionType extension)
      throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      return convert(service.getContentStream(context, repositoryId, objectId, streamId, offset,
          length, convert(extension)));
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public CmisObjectType getObject(String repositoryId, String objectId, String filter,
      Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl,
      CmisExtensionType extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      return convert(service.getObject(context, repositoryId, objectId, filter,
          includeAllowableActions, convert(IncludeRelationships.class, includeRelationships),
          renditionFilter, includePolicyIds, includeAcl, convert(extension), null));
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public CmisObjectType getObjectByPath(String repositoryId, String path, String filter,
      Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeAcl,
      CmisExtensionType extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      return convert(service.getObjectByPath(context, repositoryId, path, filter,
          includeAllowableActions, convert(IncludeRelationships.class, includeRelationships),
          renditionFilter, includePolicyIds, includeAcl, convert(extension), null));
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public CmisPropertiesType getProperties(String repositoryId, String objectId, String filter,
      CmisExtensionType extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      return convert(service.getProperties(context, repositoryId, objectId, filter,
          convert(extension)));
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public List<CmisRenditionType> getRenditions(String repositoryId, String objectId,
      String renditionFilter, BigInteger maxItems, BigInteger skipCount, CmisExtensionType extension)
      throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      List<CmisRenditionType> result = new ArrayList<CmisRenditionType>();

      List<RenditionData> renditionList = service.getRenditions(context, repositoryId, objectId,
          renditionFilter, maxItems, skipCount, convert(extension));

      if (renditionList != null) {
        for (RenditionData rendition : renditionList) {
          result.add(convert(rendition));
        }
      }

      return result;
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId,
      String sourceFolderId, Holder<CmisExtensionType> extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      org.apache.opencmis.commons.provider.Holder<String> objectIdHolder = convertHolder(objectId);
      ExtensionsData extData = convertExtensionHolder(extension);

      service.moveObject(context, repositoryId, objectIdHolder, targetFolderId, sourceFolderId,
          extData, null);

      setHolderValue(objectIdHolder, objectId);
      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
      Holder<String> changeToken, CmisContentStreamType contentStream,
      Holder<CmisExtensionType> extension) throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      org.apache.opencmis.commons.provider.Holder<String> objectIdHolder = convertHolder(objectId);
      org.apache.opencmis.commons.provider.Holder<String> changeTokenHolder = convertHolder(changeToken);
      ExtensionsData extData = convertExtensionHolder(extension);

      service.setContentStream(context, repositoryId, objectIdHolder, overwriteFlag,
          changeTokenHolder, convert(contentStream), extData);

      setHolderValue(objectIdHolder, objectId);
      setHolderValue(changeTokenHolder, changeToken);
      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }

  public void updateProperties(String repositoryId, Holder<String> objectId,
      Holder<String> changeToken, CmisPropertiesType properties, Holder<CmisExtensionType> extension)
      throws CmisException {
    try {
      AbstractServicesFactory factory = getServicesFactory(fContext);
      CmisObjectService service = factory.getObjectService();
      CallContext context = createContext(fContext);

      org.apache.opencmis.commons.provider.Holder<String> objectIdHolder = convertHolder(objectId);
      org.apache.opencmis.commons.provider.Holder<String> changeTokenHolder = convertHolder(changeToken);
      ExtensionsData extData = convertExtensionHolder(extension);

      service.updateProperties(context, repositoryId, objectIdHolder, changeTokenHolder,
          convert(properties), null, extData, null);

      setHolderValue(objectIdHolder, objectId);
      setHolderValue(changeTokenHolder, changeToken);
      setExtensionValues(extData, extension);
    }
    catch (Exception e) {
      throw convertException(e);
    }
  }
}
