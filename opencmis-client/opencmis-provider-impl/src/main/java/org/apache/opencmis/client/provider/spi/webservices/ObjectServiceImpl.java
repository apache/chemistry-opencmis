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

import static org.apache.opencmis.commons.impl.Converter.convert;
import static org.apache.opencmis.commons.impl.Converter.convertExtensionHolder;
import static org.apache.opencmis.commons.impl.Converter.convertHolder;
import static org.apache.opencmis.commons.impl.Converter.setExtensionValues;
import static org.apache.opencmis.commons.impl.Converter.setHolderValue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.jaxb.CmisException;
import org.apache.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.opencmis.commons.impl.jaxb.CmisRenditionType;
import org.apache.opencmis.commons.impl.jaxb.EnumIncludeRelationships;
import org.apache.opencmis.commons.impl.jaxb.EnumUnfileObject;
import org.apache.opencmis.commons.impl.jaxb.EnumVersioningState;
import org.apache.opencmis.commons.impl.jaxb.ObjectServicePort;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.AllowableActionsData;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.FailedToDeleteData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectService;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.RenditionData;

/**
 * Object Service Web Services client.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class ObjectServiceImpl extends AbstractWebServicesService implements ObjectService {

  private final PortProvider fPortProvider;

  /**
   * Constructor.
   */
  public ObjectServiceImpl(Session session, PortProvider portProvider) {
    setSession(session);
    fPortProvider = portProvider;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectService#createDocument(java.lang.String,
   * org.apache.opencmis.client.provider.PropertiesData, java.lang.String,
   * org.apache.opencmis.client.provider.ContentStreamData, org.apache.opencmis.commons.enums.VersioningState,
   * java.util.List, org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.AccessControlList, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public String createDocument(String repositoryId, PropertiesData properties, String folderId,
      ContentStreamData contentStream, VersioningState versioningState, List<String> policies,
      AccessControlList addACEs, AccessControlList removeACEs, ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      javax.xml.ws.Holder<String> objectId = new javax.xml.ws.Holder<String>();
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.createDocument(repositoryId, convert(properties), folderId, convert(contentStream),
          convert(EnumVersioningState.class, versioningState), policies, convert(addACEs),
          convert(removeACEs), portExtension, objectId);

      setExtensionValues(portExtension, extension);

      return objectId.value;
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
   * @see org.apache.opencmis.client.provider.ObjectService#createDocumentFromSource(java.lang.String,
   * java.lang.String, org.apache.opencmis.client.provider.PropertiesData, java.lang.String,
   * org.apache.opencmis.commons.enums.VersioningState, java.util.List,
   * org.apache.opencmis.client.provider.AccessControlList, org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public String createDocumentFromSource(String repositoryId, String sourceId,
      PropertiesData properties, String folderId, VersioningState versioningState,
      List<String> policies, AccessControlList addACEs, AccessControlList removeACEs,
      ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      javax.xml.ws.Holder<String> objectId = new javax.xml.ws.Holder<String>();
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.createDocumentFromSource(repositoryId, sourceId, convert(properties), folderId, convert(
          EnumVersioningState.class, versioningState), policies, convert(addACEs),
          convert(removeACEs), portExtension, objectId);

      setExtensionValues(portExtension, extension);

      return objectId.value;
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
   * @see org.apache.opencmis.client.provider.ObjectService#createFolder(java.lang.String,
   * org.apache.opencmis.client.provider.PropertiesData, java.lang.String, java.util.List,
   * org.apache.opencmis.client.provider.AccessControlList, org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public String createFolder(String repositoryId, PropertiesData properties, String folderId,
      List<String> policies, AccessControlList addACEs, AccessControlList removeACEs,
      ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      javax.xml.ws.Holder<String> objectId = new javax.xml.ws.Holder<String>();
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.createFolder(repositoryId, convert(properties), folderId, policies, convert(addACEs),
          convert(removeACEs), portExtension, objectId);

      setExtensionValues(portExtension, extension);

      return objectId.value;
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
   * @see org.apache.opencmis.client.provider.ObjectService#createPolicy(java.lang.String,
   * org.apache.opencmis.client.provider.PropertiesData, java.lang.String, java.util.List,
   * org.apache.opencmis.client.provider.AccessControlList, org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public String createPolicy(String repositoryId, PropertiesData properties, String folderId,
      List<String> policies, AccessControlList addACEs, AccessControlList removeACEs,
      ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      javax.xml.ws.Holder<String> objectId = new javax.xml.ws.Holder<String>();
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.createPolicy(repositoryId, convert(properties), folderId, policies, convert(addACEs),
          convert(removeACEs), portExtension, objectId);

      setExtensionValues(portExtension, extension);

      return objectId.value;
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
   * @see org.apache.opencmis.client.provider.ObjectService#createRelationship(java.lang.String,
   * org.apache.opencmis.client.provider.PropertiesData, java.util.List,
   * org.apache.opencmis.client.provider.AccessControlList, org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public String createRelationship(String repositoryId, PropertiesData properties,
      List<String> policies, AccessControlList addACEs, AccessControlList removeACEs,
      ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      javax.xml.ws.Holder<String> objectId = new javax.xml.ws.Holder<String>();
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.createRelationship(repositoryId, convert(properties), policies, convert(addACEs),
          convert(removeACEs), portExtension, objectId);

      setExtensionValues(portExtension, extension);

      return objectId.value;
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
   * @see org.apache.opencmis.client.provider.ObjectService#updateProperties(java.lang.String,
   * org.apache.opencmis.client.provider.Holder, org.apache.opencmis.client.provider.Holder,
   * org.apache.opencmis.client.provider.PropertiesData, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void updateProperties(String repositoryId, Holder<String> objectId,
      Holder<String> changeToken, PropertiesData properties, ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      javax.xml.ws.Holder<String> portObjectId = convertHolder(objectId);
      javax.xml.ws.Holder<String> portChangeToken = convertHolder(changeToken);
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.updateProperties(repositoryId, portObjectId, portChangeToken, convert(properties),
          portExtension);

      setHolderValue(portObjectId, objectId);
      setHolderValue(portChangeToken, changeToken);
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
   * @see org.apache.opencmis.client.provider.ObjectService#deleteObject(java.lang.String,
   * java.lang.String, java.lang.Boolean, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void deleteObject(String repositoryId, String objectId, Boolean allVersions,
      ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.deleteObject(repositoryId, objectId, allVersions, portExtension);

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
   * @see org.apache.opencmis.client.provider.ObjectService#deleteTree(java.lang.String, java.lang.String,
   * java.lang.Boolean, org.apache.opencmis.commons.enums.UnfileObject, java.lang.Boolean,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
      UnfileObjects unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      return convert(port.deleteTree(repositoryId, folderId, allVersions, convert(
          EnumUnfileObject.class, unfileObjects), continueOnFailure, convert(extension)));
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
   * @see org.apache.opencmis.client.provider.ObjectService#getAllowableActions(java.lang.String,
   * java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public AllowableActionsData getAllowableActions(String repositoryId, String objectId,
      ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      return convert(port.getAllowableActions(repositoryId, objectId, convert(extension)));
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
   * @see org.apache.opencmis.client.provider.ObjectService#getContentStream(java.lang.String,
   * java.lang.String, java.lang.String, java.math.BigInteger, java.math.BigInteger,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public ContentStreamData getContentStream(String repositoryId, String objectId, String streamId,
      BigInteger offset, BigInteger length, ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      return convert(port.getContentStream(repositoryId, objectId, streamId, offset, length,
          convert(extension)));
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
   * @see org.apache.opencmis.client.provider.ObjectService#getObject(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.Boolean, org.apache.opencmis.commons.enums.IncludeRelationships,
   * java.lang.String, java.lang.Boolean, java.lang.Boolean,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public ObjectData getObject(String repositoryId, String objectId, String filter,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeACL, ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      return convert(port.getObject(repositoryId, objectId, filter, includeAllowableActions,
          convert(EnumIncludeRelationships.class, includeRelationships), renditionFilter,
          includePolicyIds, includeACL, convert(extension)));
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
   * @see org.apache.opencmis.client.provider.ObjectService#getObjectByPath(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean,
   * java.lang.Boolean, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public ObjectData getObjectByPath(String repositoryId, String path, String filter,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeACL, ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      return convert(port.getObjectByPath(repositoryId, path, filter, includeAllowableActions,
          convert(EnumIncludeRelationships.class, includeRelationships), renditionFilter,
          includePolicyIds, includeACL, convert(extension)));
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
   * @see org.apache.opencmis.client.provider.ObjectService#getProperties(java.lang.String,
   * java.lang.String, java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public PropertiesData getProperties(String repositoryId, String objectId, String filter,
      ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      return convert(port.getProperties(repositoryId, objectId, filter, convert(extension)));
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
   * @see org.apache.opencmis.client.provider.ObjectService#getRenditions(java.lang.String,
   * java.lang.String, java.lang.String, java.math.BigInteger, java.math.BigInteger,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public List<RenditionData> getRenditions(String repositoryId, String objectId,
      String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      List<CmisRenditionType> renditionList = port.getRenditions(repositoryId, objectId,
          renditionFilter, maxItems, skipCount, convert(extension));

      // no list?
      if (renditionList == null) {
        return null;
      }

      // convert list
      List<RenditionData> result = new ArrayList<RenditionData>();
      for (CmisRenditionType rendition : renditionList) {
        result.add(convert(rendition));
      }

      return result;
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
   * @see org.apache.opencmis.client.provider.ObjectService#moveObject(java.lang.String,
   * org.apache.opencmis.client.provider.Holder, java.lang.String, java.lang.String,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId,
      String sourceFolderId, ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      javax.xml.ws.Holder<String> portObjectId = convertHolder(objectId);
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.moveObject(repositoryId, portObjectId, targetFolderId, sourceFolderId, portExtension);

      setHolderValue(portObjectId, objectId);
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
   * @see org.apache.opencmis.client.provider.ObjectService#setContentStream(java.lang.String,
   * org.apache.opencmis.client.provider.Holder, java.lang.Boolean, org.apache.opencmis.client.provider.Holder,
   * org.apache.opencmis.client.provider.ContentStreamData, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
      Holder<String> changeToken, ContentStreamData contentStream, ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      javax.xml.ws.Holder<String> portObjectId = convertHolder(objectId);
      javax.xml.ws.Holder<String> portChangeToken = convertHolder(changeToken);
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.setContentStream(repositoryId, portObjectId, overwriteFlag, portChangeToken,
          convert(contentStream), portExtension);

      setHolderValue(portObjectId, objectId);
      setHolderValue(portChangeToken, changeToken);
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
   * @see org.apache.opencmis.client.provider.ObjectService#deleteContentStream(java.lang.String,
   * org.apache.opencmis.client.provider.Holder, org.apache.opencmis.client.provider.Holder,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void deleteContentStream(String repositoryId, Holder<String> objectId,
      Holder<String> changeToken, ExtensionsData extension) {
    ObjectServicePort port = fPortProvider.getObjectServicePort();

    try {
      javax.xml.ws.Holder<String> portObjectId = convertHolder(objectId);
      javax.xml.ws.Holder<String> portChangeToken = convertHolder(changeToken);
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.deleteContentStream(repositoryId, portObjectId, portChangeToken, portExtension);

      setHolderValue(portObjectId, objectId);
      setHolderValue(portChangeToken, changeToken);
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
