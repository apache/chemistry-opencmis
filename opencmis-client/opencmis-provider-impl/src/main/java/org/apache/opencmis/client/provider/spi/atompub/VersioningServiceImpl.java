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
import java.util.ArrayList;
import java.util.List;

import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomElement;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomEntry;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomFeed;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomLink;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.ReturnVersion;
import org.apache.opencmis.commons.impl.UrlBuilder;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.impl.jaxb.CmisPropertiesType;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.VersioningService;

/**
 * Versioning Service AtomPub client.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class VersioningServiceImpl extends AbstractAtomPubService implements VersioningService {

  /**
   * Constructor.
   */
  public VersioningServiceImpl(Session session) {
    setSession(session);
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
    if ((objectId == null) || (objectId.getValue() == null) || (objectId.getValue().length() == 0)) {
      throw new CmisInvalidArgumentException("Object id must be set!");
    }

    // find the link
    String link = loadCollection(repositoryId, Constants.COLLECTION_CHECKEDOUT);

    if (link == null) {
      throw new CmisObjectNotFoundException(
          "Unknown repository or checkedout collection not supported!");
    }

    UrlBuilder url = new UrlBuilder(link);

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

    // clean up cache
    removeLinks(repositoryId, entry.getId());

    // walk through the entry
    for (AtomElement element : entry.getElements()) {
      if (element.getObject() instanceof AtomLink) {
        addLink(repositoryId, entry.getId(), (AtomLink) element.getObject());
      }
    }

    if (contentCopied != null) {
      contentCopied.setValue(null);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.VersioningService#cancelCheckOut(java.lang.String,
   * java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
    // find the link
    String link = loadLink(repositoryId, objectId, Constants.REL_SELF, Constants.MEDIATYPE_ENTRY);

    if (link == null) {
      throw new CmisObjectNotFoundException("Unknown repository or object!");
    }

    delete(new UrlBuilder(link));
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
      List<String> policies, AccessControlList addAces, AccessControlList removeAces,
      ExtensionsData extension) {
    // we need an object id
    if ((objectId == null) || (objectId.getValue() == null) || (objectId.getValue().length() == 0)) {
      throw new CmisInvalidArgumentException("Object id must be set!");
    }

    // find the link
    String link = loadLink(repositoryId, objectId.getValue(), Constants.REL_SELF,
        Constants.MEDIATYPE_ENTRY);

    if (link == null) {
      throw new CmisObjectNotFoundException("Unknown repository or object!");
    }

    UrlBuilder url = new UrlBuilder(link);
    url.addParameter(Constants.PARAM_CHECKIN_COMMENT, checkinComment);
    url.addParameter(Constants.PARAM_MAJOR, major);
    url.addParameter(Constants.PARAM_CHECK_IN, "true");

    // set up object and writer
    CmisObjectType object = new CmisObjectType();
    object.setProperties(convert(properties));
    object.setPolicyIds(convertPolicyIds(policies));

    if(object.getProperties() == null) {
      object.setProperties(new CmisPropertiesType());
    }
    
    String mediaType = null;
    InputStream stream = null;

    if (contentStream != null) {
      mediaType = contentStream.getMimeType();
      stream = contentStream.getStream();
    }

    final AtomEntryWriter entryWriter = new AtomEntryWriter(object, mediaType, stream);

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

    // clean up cache
    removeLinks(repositoryId, entry.getId());

    // walk through the entry
    AccessControlList originalAces = null;
    for (AtomElement element : entry.getElements()) {
      if (element.getObject() instanceof AtomLink) {
        addLink(repositoryId, entry.getId(), (AtomLink) element.getObject());
      }
      else if (element.getObject() instanceof CmisObjectType) {
        // extract current ACL
        object = (CmisObjectType) element.getObject();
        originalAces = convert(object.getAcl(), object.isExactACL());
      }
    }

    // handle ACL modifications
    if ((originalAces != null) && (isAclMergeRequired(addAces, removeAces))) {
      // merge and update ACL
      AccessControlList newACL = mergeAcls(originalAces, addAces, removeAces);
      if (newACL != null) {
        updateAcl(repositoryId, entry.getId(), newACL, null);
      }
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
    List<ObjectData> result = new ArrayList<ObjectData>();

    // find the link
    String link = loadLink(repositoryId, versionSeriesId, Constants.REL_VERSIONHISTORY,
        Constants.MEDIATYPE_FEED);

    if (link == null) {
      throw new CmisObjectNotFoundException("Unknown repository or folder!");
    }

    UrlBuilder url = new UrlBuilder(link);
    url.addParameter(Constants.PARAM_FILTER, filter);
    url.addParameter(Constants.PARAM_ALLOWABLE_ACTIONS, includeAllowableActions);

    // read and parse
    HttpUtils.Response resp = read(url);
    AtomFeed feed = parse(resp.getStream(), AtomFeed.class);

    // get the versions
    if (!feed.getEntries().isEmpty()) {
      for (AtomEntry entry : feed.getEntries()) {
        ObjectData version = null;

        // clean up cache
        removeLinks(repositoryId, entry.getId());

        // walk through the entry
        for (AtomElement element : entry.getElements()) {
          if (element.getObject() instanceof AtomLink) {
            addLink(repositoryId, entry.getId(), (AtomLink) element.getObject());
          }
          else if (element.getObject() instanceof CmisObjectType) {
            version = convert((CmisObjectType) element.getObject());
          }
        }

        if (version != null) {
          result.add(version);
        }
      }
    }

    return result;
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

    ReturnVersion returnVersion = ReturnVersion.LATEST;
    if ((major != null) && (major.booleanValue())) {
      returnVersion = ReturnVersion.LASTESTMAJOR;
    }

    return getObjectInternal(repositoryId, IdentifierType.ID, versionSeriesId, returnVersion,
        filter, includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds,
        includeACL, extension);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.VersioningService#getPropertiesOfLatestVersion(java.lang.String,
   * java.lang.String, java.lang.Boolean, java.lang.String,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public PropertiesData getPropertiesOfLatestVersion(String repositoryId, String versionSeriesId,
      Boolean major, String filter, ExtensionsData extension) {

    ReturnVersion returnVersion = ReturnVersion.LATEST;
    if ((major != null) && (major.booleanValue())) {
      returnVersion = ReturnVersion.LASTESTMAJOR;
    }

    ObjectData object = getObjectInternal(repositoryId, IdentifierType.ID, versionSeriesId,
        returnVersion, filter, Boolean.FALSE, IncludeRelationships.NONE, "cmis:none",
        Boolean.FALSE, Boolean.FALSE, extension);

    return object.getProperties();
  }

}
