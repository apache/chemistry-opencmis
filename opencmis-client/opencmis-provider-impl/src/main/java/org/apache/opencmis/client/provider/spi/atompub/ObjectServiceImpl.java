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
package org.apache.opencmis.client.provider.spi.atompub;

import static org.apache.opencmis.commons.impl.Converter.convert;
import static org.apache.opencmis.commons.impl.Converter.convertPolicyIds;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.client.provider.spi.atompub.objects.AllowableActions;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomElement;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomEntry;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomLink;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.ReturnVersion;
import org.apache.opencmis.commons.impl.UrlBuilder;
import org.apache.opencmis.commons.impl.dataobjects.ContentStreamDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.impl.jaxb.CmisProperty;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertyString;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.AllowableActionsData;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.FailedToDeleteData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectService;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.PropertyIdData;
import org.apache.opencmis.commons.provider.RenditionData;

/**
 * Object Service AtomPub client.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class ObjectServiceImpl extends AbstractAtomPubService implements ObjectService {

  /**
   * Constructor.
   */
  public ObjectServiceImpl(Session session) {
    setSession(session);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectService#createDocument(java.lang.String,
   * org.apache.opencmis.client.provider.PropertiesData, java.lang.String,
   * org.apache.opencmis.client.provider.ContentStreamData,
   * org.apache.opencmis.commons.enums.VersioningState, java.util.List,
   * org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public String createDocument(String repositoryId, PropertiesData properties, String folderId,
      ContentStreamData contentStream, VersioningState versioningState, List<String> policies,
      AccessControlList addAces, AccessControlList removeAces, ExtensionsData extension) {
    checkCreateProperties(properties);

    // find the link
    String link = loadLink(repositoryId, folderId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);

    if (link == null) {
      throwLinkException(repositoryId, folderId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);
    }

    UrlBuilder url = new UrlBuilder(link);
    url.addParameter(Constants.PARAM_VERSIONIG_STATE, versioningState);

    // set up object and writer
    CmisObjectType object = new CmisObjectType();
    object.setProperties(convert(properties));
    object.setPolicyIds(convertPolicyIds(policies));

    String mediaType = null;
    InputStream stream = null;

    if (contentStream != null) {
      mediaType = contentStream.getMimeType();
      stream = contentStream.getStream();
    }

    final AtomEntryWriter entryWriter = new AtomEntryWriter(object, mediaType, stream);

    // post the new folder object
    HttpUtils.Response resp = post(url, Constants.MEDIATYPE_ENTRY, new HttpUtils.Output() {
      public void write(OutputStream out) throws Exception {
        entryWriter.write(out);
      }
    });

    // parse the response
    AtomEntry entry = parse(resp.getStream(), AtomEntry.class);

    // handle ACL modifications
    handleAclModifications(repositoryId, entry, addAces, removeAces);

    return entry.getId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.ObjectService#createDocumentFromSource(java.lang.String,
   * java.lang.String, org.apache.opencmis.client.provider.PropertiesData, java.lang.String,
   * org.apache.opencmis.commons.enums.VersioningState, java.util.List,
   * org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public String createDocumentFromSource(String repositoryId, String sourceId,
      PropertiesData properties, String folderId, VersioningState versioningState,
      List<String> policies, AccessControlList addACEs, AccessControlList removeACEs,
      ExtensionsData extension) {
    throw new CmisNotSupportedException(
        "createDocumentFromSource is not supported by the AtomPub binding!");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectService#createFolder(java.lang.String,
   * org.apache.opencmis.client.provider.PropertiesData, java.lang.String, java.util.List,
   * org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public String createFolder(String repositoryId, PropertiesData properties, String folderId,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {
    checkCreateProperties(properties);

    // find the link
    String link = loadLink(repositoryId, folderId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);

    if (link == null) {
      throwLinkException(repositoryId, folderId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);
    }

    UrlBuilder url = new UrlBuilder(link);

    // set up object and writer
    CmisObjectType object = new CmisObjectType();
    object.setProperties(convert(properties));
    object.setPolicyIds(convertPolicyIds(policies));

    final AtomEntryWriter entryWriter = new AtomEntryWriter(object);

    // post the new folder object
    HttpUtils.Response resp = post(url, Constants.MEDIATYPE_ENTRY, new HttpUtils.Output() {
      public void write(OutputStream out) throws Exception {
        entryWriter.write(out);
      }
    });

    // parse the response
    AtomEntry entry = parse(resp.getStream(), AtomEntry.class);

    // handle ACL modifications
    handleAclModifications(repositoryId, entry, addAces, removeAces);

    return entry.getId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectService#createPolicy(java.lang.String,
   * org.apache.opencmis.client.provider.PropertiesData, java.lang.String, java.util.List,
   * org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public String createPolicy(String repositoryId, PropertiesData properties, String folderId,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {
    checkCreateProperties(properties);

    // find the link
    String link = loadLink(repositoryId, folderId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);

    if (link == null) {
      throwLinkException(repositoryId, folderId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);
    }

    UrlBuilder url = new UrlBuilder(link);

    // set up object and writer
    CmisObjectType object = new CmisObjectType();
    object.setProperties(convert(properties));
    object.setPolicyIds(convertPolicyIds(policies));

    final AtomEntryWriter entryWriter = new AtomEntryWriter(object);

    // post the new folder object
    HttpUtils.Response resp = post(url, Constants.MEDIATYPE_ENTRY, new HttpUtils.Output() {
      public void write(OutputStream out) throws Exception {
        entryWriter.write(out);
      }
    });

    // parse the response
    AtomEntry entry = parse(resp.getStream(), AtomEntry.class);

    // handle ACL modifications
    handleAclModifications(repositoryId, entry, addAces, removeAces);

    return entry.getId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectService#createRelationship(java.lang.String,
   * org.apache.opencmis.client.provider.PropertiesData, java.util.List,
   * org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public String createRelationship(String repositoryId, PropertiesData properties,
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {
    checkCreateProperties(properties);

    // find source id
    PropertyData<?> sourceIdProperty = properties.getProperties().get(PropertyIds.CMIS_SOURCE_ID);
    if (!(sourceIdProperty instanceof PropertyIdData)) {
      throw new CmisInvalidArgumentException("Source Id is not set!");
    }

    String sourceId = ((PropertyIdData) sourceIdProperty).getFirstValue();
    if (sourceId == null) {
      throw new CmisInvalidArgumentException("Source Id is not set!");
    }

    // find the link
    String link = loadLink(repositoryId, sourceId, Constants.REL_RELATIONSHIPS,
        Constants.MEDIATYPE_FEED);

    if (link == null) {
      throwLinkException(repositoryId, sourceId, Constants.REL_RELATIONSHIPS,
          Constants.MEDIATYPE_FEED);
    }

    UrlBuilder url = new UrlBuilder(link);

    // set up object and writer
    CmisObjectType object = new CmisObjectType();
    object.setProperties(convert(properties));
    object.setPolicyIds(convertPolicyIds(policies));

    final AtomEntryWriter entryWriter = new AtomEntryWriter(object);

    // post the new folder object
    HttpUtils.Response resp = post(url, Constants.MEDIATYPE_ENTRY, new HttpUtils.Output() {
      public void write(OutputStream out) throws Exception {
        entryWriter.write(out);
      }
    });

    // parse the response
    AtomEntry entry = parse(resp.getStream(), AtomEntry.class);

    // handle ACL modifications
    handleAclModifications(repositoryId, entry, addAces, removeAces);

    return entry.getId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectService#updateProperties(java.lang.String,
   * org.apache.opencmis.client.provider.Holder, org.apache.opencmis.client.provider.Holder,
   * org.apache.opencmis.client.provider.PropertiesData,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void updateProperties(String repositoryId, Holder<String> objectId,
      Holder<String> changeToken, PropertiesData properties, ExtensionsData extension) {
    // we need an object id
    if ((objectId == null) || (objectId.getValue() == null) || (objectId.getValue().length() == 0)) {
      throw new CmisInvalidArgumentException("Object id must be set!");
    }

    // find the link
    String link = loadLink(repositoryId, objectId.getValue(), Constants.REL_SELF,
        Constants.MEDIATYPE_ENTRY);

    if (link == null) {
      throwLinkException(repositoryId, objectId.getValue(), Constants.REL_SELF,
          Constants.MEDIATYPE_ENTRY);
    }

    UrlBuilder url = new UrlBuilder(link);
    if (changeToken != null) {
      url.addParameter(Constants.PARAM_CHANGE_TOKEN, changeToken.getValue());
    }

    // set up object and writer
    CmisObjectType object = new CmisObjectType();
    object.setProperties(convert(properties));

    final AtomEntryWriter entryWriter = new AtomEntryWriter(object);

    // update
    HttpUtils.Response resp = put(url, Constants.MEDIATYPE_ENTRY, new HttpUtils.Output() {
      public void write(OutputStream out) throws Exception {
        entryWriter.write(out);
      }
    });

    // parse new entry
    AtomEntry entry = parse(resp.getStream(), AtomEntry.class);

    // we expect a CMIS entry
    if (entry.getId() == null) {
      throw new CmisConnectionException("Received Atom entry is not a CMIS entry!");
    }

    // set object id
    objectId.setValue(entry.getId());

    if (changeToken != null) {
      changeToken.setValue(null); // just in case
    }

    lockLinks();
    try {
      // clean up cache
      removeLinks(repositoryId, entry.getId());

      // walk through the entry
      for (AtomElement element : entry.getElements()) {
        if (element.getObject() instanceof AtomLink) {
          addLink(repositoryId, entry.getId(), (AtomLink) element.getObject());
        }
        else if (element.getObject() instanceof CmisObjectType) {
          // extract new change token
          if (changeToken != null) {
            object = (CmisObjectType) element.getObject();

            if (object.getProperties() != null) {
              for (CmisProperty property : object.getProperties().getProperty()) {
                if (PropertyIds.CMIS_CHANGE_TOKEN.equals(property.getPropertyDefinitionId())
                    && (property instanceof CmisPropertyString)) {

                  CmisPropertyString changeTokenProperty = (CmisPropertyString) property;
                  if (!changeTokenProperty.getValue().isEmpty()) {
                    changeToken.setValue(changeTokenProperty.getValue().get(0));
                  }

                  break;
                }
              }
            }
          }
        }
      }
    }
    finally {
      unlockLinks();
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

    // find the link
    String link = loadLink(repositoryId, objectId, Constants.REL_SELF, Constants.MEDIATYPE_ENTRY);

    if (link == null) {
      throwLinkException(repositoryId, objectId, Constants.REL_SELF, Constants.MEDIATYPE_ENTRY);
    }

    UrlBuilder url = new UrlBuilder(link);
    url.addParameter(Constants.PARAM_ALL_VERSIONS, allVersions);

    delete(url);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectService#deleteTree(java.lang.String,
   * java.lang.String, java.lang.Boolean, org.apache.opencmis.commons.enums.UnfileObject,
   * java.lang.Boolean, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
      UnfileObjects unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {

    // find the link
    String link = loadLink(repositoryId, folderId, Constants.REL_DOWN,
        Constants.MEDIATYPE_DESCENDANTS);

    if (link == null) {
      throwLinkException(repositoryId, folderId, Constants.REL_DOWN,
          Constants.MEDIATYPE_DESCENDANTS);
    }

    UrlBuilder url = new UrlBuilder(link);
    url.addParameter(Constants.PARAM_ALL_VERSIONS, allVersions);
    url.addParameter(Constants.PARAM_UNFILE_OBJECTS, unfileObjects);
    url.addParameter(Constants.PARAM_CONTINUE_ON_FAILURE, continueOnFailure);

    // make the call
    HttpUtils.Response resp = HttpUtils.invokeDELETE(url, getSession());

    // check response code
    if ((resp.getResponseCode() == 200) || (resp.getResponseCode() == 202)
        || (resp.getResponseCode() == 204)) {
      return new FailedToDeleteDataImpl();
    }

    throw convertStatusCode(resp.getResponseCode(), resp.getResponseMessage(), resp
        .getErrorContent(), null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectService#getAllowableActions(java.lang.String,
   * java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public AllowableActionsData getAllowableActions(String repositoryId, String objectId,
      ExtensionsData extension) {
    // find the link
    String link = loadLink(repositoryId, objectId, Constants.REL_ALLOWABLEACTIONS,
        Constants.MEDIATYPE_ALLOWABLEACTION);

    if (link == null) {
      throwLinkException(repositoryId, objectId, Constants.REL_ALLOWABLEACTIONS,
          Constants.MEDIATYPE_ALLOWABLEACTION);
    }

    UrlBuilder url = new UrlBuilder(link);

    // read and parse
    HttpUtils.Response resp = read(url);
    AllowableActions allowableActions = parse(resp.getStream(), AllowableActions.class);

    return convert(allowableActions.getAllowableActions());
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
    ContentStreamDataImpl result = new ContentStreamDataImpl();

    // find the link
    String link = loadLink(repositoryId, objectId, AtomPubParser.LINK_REL_CONTENT, null);

    if (link == null) {
      throwLinkException(repositoryId, objectId, AtomPubParser.LINK_REL_CONTENT, null);
    }

    UrlBuilder url = new UrlBuilder(link);
    url.addParameter(Constants.PARAM_STREAM_ID, streamId);

    // get the content
    HttpUtils.Response resp = HttpUtils.invokeGET(url, getSession(), offset, length);

    // check response code
    if ((resp.getResponseCode() != 200) && (resp.getResponseCode() != 206)) {
      throw convertStatusCode(resp.getResponseCode(), resp.getResponseMessage(), resp
          .getErrorContent(), null);
    }

    result.setFilename(null);
    result.setLength(resp.getContentLength());
    result.setMimeType(resp.getContentTypeHeader());
    result.setStream(resp.getStream());

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectService#getObject(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.Boolean,
   * org.apache.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean,
   * java.lang.Boolean, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public ObjectData getObject(String repositoryId, String objectId, String filter,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeACL, ExtensionsData extension) {

    return getObjectInternal(repositoryId, IdentifierType.ID, objectId, ReturnVersion.THIS, filter,
        includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds,
        includeACL, extension);
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

    return getObjectInternal(repositoryId, IdentifierType.PATH, path, ReturnVersion.THIS, filter,
        includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds,
        includeACL, extension);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectService#getProperties(java.lang.String,
   * java.lang.String, java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public PropertiesData getProperties(String repositoryId, String objectId, String filter,
      ExtensionsData extension) {
    ObjectData object = getObjectInternal(repositoryId, IdentifierType.ID, objectId,
        ReturnVersion.THIS, filter, Boolean.FALSE, IncludeRelationships.NONE, "cmis:none",
        Boolean.FALSE, Boolean.FALSE, extension);

    return object.getProperties();
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
    ObjectData object = getObjectInternal(repositoryId, IdentifierType.ID, objectId,
        ReturnVersion.THIS, PropertyIds.CMIS_OBJECT_ID, Boolean.FALSE, IncludeRelationships.NONE,
        renditionFilter, Boolean.FALSE, Boolean.FALSE, extension);

    List<RenditionData> result = object.getRenditions();
    if (result == null) {
      result = Collections.emptyList();
    }

    return result;
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
    if ((objectId == null) || (objectId.getValue() == null) || (objectId.getValue().length() == 0)) {
      throw new CmisInvalidArgumentException("Object id must be set!");
    }

    if ((targetFolderId == null) || (targetFolderId.length() == 0) || (sourceFolderId == null)
        || (sourceFolderId.length() == 0)) {
      throw new CmisInvalidArgumentException("Source and target folder must be set!");
    }

    // find the link
    String link = loadLink(repositoryId, targetFolderId, Constants.REL_DOWN,
        Constants.MEDIATYPE_CHILDREN);

    if (link == null) {
      throwLinkException(repositoryId, targetFolderId, Constants.REL_DOWN,
          Constants.MEDIATYPE_CHILDREN);
    }

    UrlBuilder url = new UrlBuilder(link);
    url.addParameter(Constants.PARAM_SOURCE_FOLDER_ID, sourceFolderId);

    // set up object and writer
    final AtomEntryWriter entryWriter = new AtomEntryWriter(createIdObject(objectId.getValue()));

    // post move request
    HttpUtils.Response resp = post(url, Constants.MEDIATYPE_ENTRY, new HttpUtils.Output() {
      public void write(OutputStream out) throws Exception {
        entryWriter.write(out);
      }
    });

    // parse the response
    AtomEntry entry = parse(resp.getStream(), AtomEntry.class);

    objectId.setValue(entry.getId());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ObjectService#setContentStream(java.lang.String,
   * org.apache.opencmis.client.provider.Holder, java.lang.Boolean,
   * org.apache.opencmis.client.provider.Holder,
   * org.apache.opencmis.client.provider.ContentStreamData,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
      Holder<String> changeToken, ContentStreamData contentStream, ExtensionsData extension) {
    // we need an object id
    if ((objectId == null) || (objectId.getValue() == null)) {
      throw new CmisInvalidArgumentException("Object ID must be set!");
    }

    // we need content
    if ((contentStream == null) || (contentStream.getStream() == null)
        || (contentStream.getMimeType() == null)) {
      throw new CmisInvalidArgumentException("Content must be set!");
    }

    // find the link
    String link = loadLink(repositoryId, objectId.getValue(), Constants.REL_EDITMEDIA, null);

    if (link == null) {
      throwLinkException(repositoryId, objectId.getValue(), Constants.REL_EDITMEDIA, null);
    }

    UrlBuilder url = new UrlBuilder(link);
    if (changeToken != null) {
      url.addParameter(Constants.PARAM_CHANGE_TOKEN, changeToken.getValue());
    }
    url.addParameter(Constants.PARAM_OVERWRITE_FLAG, overwriteFlag);

    final InputStream stream = contentStream.getStream();

    // send content
    HttpUtils.Response resp = HttpUtils.invokePUT(url, contentStream.getMimeType(),
        new HttpUtils.Output() {
          public void write(OutputStream out) throws Exception {
            int b;
            byte[] buffer = new byte[4096];

            while ((b = stream.read(buffer)) > -1) {
              out.write(buffer, 0, b);
            }

            stream.close();
          }
        }, getSession());

    // check response code
    if ((resp.getResponseCode() != 200) && (resp.getResponseCode() != 201)
        && (resp.getResponseCode() != 204)) {
      throw convertStatusCode(resp.getResponseCode(), resp.getResponseMessage(), resp
          .getErrorContent(), null);
    }

    objectId.setValue(null);
    if (changeToken != null) {
      changeToken.setValue(null);
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
    // we need an object id
    if ((objectId == null) || (objectId.getValue() == null)) {
      throw new CmisInvalidArgumentException("Object ID must be set!");
    }

    // find the link
    String link = loadLink(repositoryId, objectId.getValue(), Constants.REL_EDITMEDIA, null);

    if (link == null) {
      throwLinkException(repositoryId, objectId.getValue(), Constants.REL_EDITMEDIA, null);
    }

    UrlBuilder url = new UrlBuilder(link);
    if (changeToken != null) {
      url.addParameter(Constants.PARAM_CHANGE_TOKEN, changeToken.getValue());
    }

    delete(url);

    objectId.setValue(null);
    if (changeToken != null) {
      changeToken.setValue(null);
    }
  }

  // ---- internal ----

  private void checkCreateProperties(PropertiesData properties) {
    if ((properties == null) || (properties.getProperties() == null)) {
      throw new CmisInvalidArgumentException("Properties must be set!");
    }

    if (!properties.getProperties().containsKey(PropertyIds.CMIS_OBJECT_TYPE_ID)) {
      throw new CmisInvalidArgumentException("Property " + PropertyIds.CMIS_OBJECT_TYPE_ID
          + " must be set!");
    }

    if (properties.getProperties().containsKey(PropertyIds.CMIS_OBJECT_ID)) {
      throw new CmisInvalidArgumentException("Property " + PropertyIds.CMIS_OBJECT_ID
          + " must not be set!");
    }
  }

  /**
   * Handles ACL modifications of newly created objects.
   */
  private void handleAclModifications(String repositoryId, AtomEntry entry,
      AccessControlList addAces, AccessControlList removeAces) {
    if (!isAclMergeRequired(addAces, removeAces)) {
      return;
    }

    AccessControlList originalAces = null;

    // walk through the entry and find the current ACL
    for (AtomElement element : entry.getElements()) {
      if (element.getObject() instanceof CmisObjectType) {
        // extract current ACL
        CmisObjectType object = (CmisObjectType) element.getObject();
        originalAces = convert(object.getAcl(), object.isExactACL());

        break;
      }
    }

    if (originalAces != null) {
      // merge and update ACL
      AccessControlList newACL = mergeAcls(originalAces, addAces, removeAces);
      if (newACL != null) {
        updateAcl(repositoryId, entry.getId(), newACL, null);
      }
    }
  }
}
