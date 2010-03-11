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

import java.util.ArrayList;
import java.util.List;

import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.jaxb.CmisException;
import org.apache.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.impl.jaxb.EnumIncludeRelationships;
import org.apache.opencmis.commons.impl.jaxb.VersioningServicePort;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.VersioningService;

/**
 * Versioning Service Web Services client.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class VersioningServiceImpl extends AbstractWebServicesService implements VersioningService {

  private final PortProvider fPortProvider;

  /**
   * Constructor.
   */
  public VersioningServiceImpl(Session session, PortProvider portProvider) {
    setSession(session);
    fPortProvider = portProvider;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.VersioningService#checkOut(java.lang.String,
   * org.apache.opencmis.client.provider.Holder, org.apache.opencmis.client.provider.ExtensionsData,
   * org.apache.opencmis.client.provider.Holder)
   */
  public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension,
      Holder<Boolean> contentCopied) {
    VersioningServicePort port = fPortProvider.getVersioningServicePort();

    try {
      javax.xml.ws.Holder<String> portObjectId = convertHolder(objectId);
      javax.xml.ws.Holder<Boolean> portContentCopied = new javax.xml.ws.Holder<Boolean>();
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.checkOut(repositoryId, portObjectId, portExtension, portContentCopied);

      setHolderValue(portObjectId, objectId);
      setHolderValue(portContentCopied, contentCopied);
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
   * @see org.apache.opencmis.client.provider.VersioningService#cancelCheckOut(java.lang.String,
   * java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
    VersioningServicePort port = fPortProvider.getVersioningServicePort();

    try {
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.cancelCheckOut(repositoryId, objectId, portExtension);

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
   * @see org.apache.opencmis.client.provider.VersioningService#checkIn(java.lang.String,
   * org.apache.opencmis.client.provider.Holder, java.lang.Boolean,
   * org.apache.opencmis.client.provider.PropertiesData, org.apache.opencmis.client.provider.ContentStreamData,
   * java.lang.String, java.util.List, org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.AccessControlList, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void checkIn(String repositoryId, Holder<String> objectId, Boolean major,
      PropertiesData properties, ContentStreamData contentStream, String checkinComment,
      List<String> policies, AccessControlList addACEs, AccessControlList removeACEs,
      ExtensionsData extension) {
    VersioningServicePort port = fPortProvider.getVersioningServicePort();

    try {
      javax.xml.ws.Holder<String> portObjectId = convertHolder(objectId);
      javax.xml.ws.Holder<CmisExtensionType> portExtension = convertExtensionHolder(extension);

      port.checkIn(repositoryId, portObjectId, major, convert(properties), convert(contentStream),
          checkinComment, policies, convert(addACEs), convert(removeACEs), portExtension);

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
   * @see org.apache.opencmis.client.provider.VersioningService#getAllVersions(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public List<ObjectData> getAllVersions(String repositoryId, String versionSeriesId,
      String filter, Boolean includeAllowableActions, ExtensionsData extension) {
    VersioningServicePort port = fPortProvider.getVersioningServicePort();

    try {
      List<CmisObjectType> versionList = port.getAllVersions(repositoryId, versionSeriesId, filter,
          includeAllowableActions, convert(extension));

      // no list?
      if (versionList == null) {
        return null;
      }

      // convert list
      List<ObjectData> result = new ArrayList<ObjectData>();
      for (CmisObjectType version : versionList) {
        result.add(convert(version));
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
   * @see org.apache.opencmis.client.provider.VersioningService#getObjectOfLatestVersion(java.lang.String,
   * java.lang.String, java.lang.Boolean, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean,
   * java.lang.Boolean, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public ObjectData getObjectOfLatestVersion(String repositoryId, String versionSeriesId,
      Boolean major, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
      Boolean includeACL, ExtensionsData extension) {
    VersioningServicePort port = fPortProvider.getVersioningServicePort();

    try {
      return convert(port.getObjectOfLatestVersion(repositoryId, versionSeriesId, major, filter,
          includeAllowableActions, convert(EnumIncludeRelationships.class, includeRelationships),
          renditionFilter, includePolicyIds, includeACL, convert(extension)));
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
   * @see
   * org.apache.opencmis.client.provider.VersioningService#getPropertiesOfLatestVersion(java.lang.String,
   * java.lang.String, java.lang.Boolean, java.lang.String,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public PropertiesData getPropertiesOfLatestVersion(String repositoryId, String VersionSeriesId,
      Boolean major, String filter, ExtensionsData extension) {
    VersioningServicePort port = fPortProvider.getVersioningServicePort();

    try {
      return convert(port.getPropertiesOfLatestVersion(repositoryId, VersionSeriesId, major,
          filter, convert(extension)));
    }
    catch (CmisException e) {
      throw convertException(e);
    }
    catch (Exception e) {
      throw new CmisRuntimeException("Error: " + e.getMessage(), e);
    }
  }
}
