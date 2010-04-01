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
package org.apache.opencmis.client.runtime;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.opencmis.client.api.Ace;
import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.ContentStream;
import org.apache.opencmis.client.api.Document;
import org.apache.opencmis.client.api.ObjectId;
import org.apache.opencmis.client.api.OperationContext;
import org.apache.opencmis.client.api.Policy;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.repository.ObjectFactory;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.enums.Updatability;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.ObjectData;

public class PersistentDocumentImpl extends AbstractPersistentFilableCmisObject implements Document {

  /**
   * Constructor.
   */
  public PersistentDocumentImpl(PersistentSessionImpl session, ObjectType objectType,
      ObjectData objectData, OperationContext context) {
    initialize(session, objectType, objectData, context);
  }

  // properties

  public String getCheckinComment() {
    return getPropertyValue(PropertyIds.CMIS_CHECKIN_COMMENT);
  }

  public String getVersionLabel() {
    return getPropertyValue(PropertyIds.CMIS_VERSION_LABEL);
  }

  public String getVersionSeriesId() {
    return getPropertyValue(PropertyIds.CMIS_VERSION_SERIES_ID);
  }

  public String getVersionSeriesCheckedOutId() {
    return getPropertyValue(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_ID);
  }

  public String getVersionSeriesCheckedOutBy() {
    return getPropertyValue(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_BY);
  }

  public Boolean isImmutable() {
    return getPropertyValue(PropertyIds.CMIS_IS_IMMUTABLE);
  }

  public Boolean isLatestMajorVersion() {
    return getPropertyValue(PropertyIds.CMIS_IS_LATEST_MAJOR_VERSION);
  }

  public Boolean isLatestVersion() {
    return getPropertyValue(PropertyIds.CMIS_IS_LATEST_VERSION);
  }

  public Boolean isMajorVersion() {
    return getPropertyValue(PropertyIds.CMIS_IS_MAJOR_VERSION);
  }

  public Boolean isVersionSeriesCheckedOut() {
    return getPropertyValue(PropertyIds.CMIS_IS_VERSION_SERIES_CHECKED_OUT);
  }

  public long getContentStreamLength() {
    BigInteger bigInt = getPropertyValue(PropertyIds.CMIS_CONTENT_STREAM_LENGTH);
    return (bigInt == null) ? (long) -1 : bigInt.longValue();
  }

  public String getContentStreamMimeType() {
    return getPropertyValue(PropertyIds.CMIS_CONTENT_STREAM_MIME_TYPE);
  }

  public String getContentStreamFileName() {
    return getPropertyValue(PropertyIds.CMIS_CONTENT_STREAM_FILE_NAME);
  }

  public String getContentStreamId() {
    return getPropertyValue(PropertyIds.CMIS_CONTENT_STREAM_ID);
  }

  // operations

  public Document copy(List<Property<?>> properties, VersioningState versioningState,
      List<Policy> policies, List<Ace> addACEs, List<Ace> removeACEs) {
    throw new CmisRuntimeException("not implemented");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Document#deleteAllVersions()
   */
  public void deleteAllVersions() {
    delete(true);
  }

  // versioning

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Document#checkOut()
   */
  public ObjectId checkOut() {
    String objectId = getObjectId();
    Holder<String> objectIdHolder = new Holder<String>(objectId);

    getProvider().getVersioningService().checkOut(getRepositoryId(), objectIdHolder, null, null);

    if (objectIdHolder.getValue() == null) {
      return null;
    }

    return getSession().createObjectId(objectIdHolder.getValue());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Document#cancelCheckOut()
   */
  public void cancelCheckOut() {
    String objectId = getObjectId();

    getProvider().getVersioningService().cancelCheckOut(getRepositoryId(), objectId, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Document#checkIn(boolean, java.util.Map,
   * org.apache.opencmis.client.api.ContentStream, java.lang.String, java.util.List, java.util.List,
   * java.util.List)
   */
  public ObjectId checkIn(boolean major, Map<String, ?> properties, ContentStream contentStream,
      String checkinComment, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces) {
    String objectId;
    ObjectType type;
    readLock();
    try {
      objectId = getObjectId();
      type = getType();
    }
    finally {
      readUnlock();
    }

    Holder<String> objectIdHolder = new Holder<String>(objectId);

    ObjectFactory of = getObjectFactory();

    Set<Updatability> updatebility = new HashSet<Updatability>();
    updatebility.add(Updatability.READWRITE);
    updatebility.add(Updatability.WHENCHECKEDOUT);

    getProvider().getVersioningService().checkIn(getRepositoryId(), objectIdHolder, major,
        of.convertProperties(properties, type, updatebility),
        of.convertContentStream(contentStream), checkinComment, of.convertPolicies(policies),
        of.convertAces(addAces), of.convertAces(removeAces), null);

    if (objectIdHolder.getValue() == null) {
      return null;
    }

    return getSession().createObjectId(objectIdHolder.getValue());

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Document#getAllVersions()
   */
  public List<Document> getAllVersions() {
    return getAllVersions(getSession().getDefaultContext());
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.apache.opencmis.client.api.Document#getAllVersions(org.apache.opencmis.client.api.
   * OperationContext)
   */
  public List<Document> getAllVersions(OperationContext context) {
    String objectId;
    String versionSeriesId;

    readLock();
    try {
      objectId = getObjectId();
      versionSeriesId = getVersionSeriesId();
    }
    finally {
      readUnlock();
    }

    List<ObjectData> versions = getProvider().getVersioningService().getAllVersions(
        getRepositoryId(), objectId, versionSeriesId, context.getFilterString(),
        context.isIncludeAllowableActions(), null);

    ObjectFactory objectFactory = getSession().getObjectFactory();

    List<Document> result = new ArrayList<Document>();
    if (versions != null) {
      for (ObjectData objectData : versions) {
        CmisObject doc = objectFactory.convertObject(objectData, context);
        if (!(doc instanceof Document)) {
          // should not happen...
          continue;
        }

        result.add((Document) doc);
      }
    }

    return result;

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Document#getObjectOfLatestVersion(boolean)
   */
  public Document getObjectOfLatestVersion(boolean major) {
    return getObjectOfLatestVersion(major, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Document#getObjectOfLatestVersion(boolean,
   * org.apache.opencmis.client.api.OperationContext)
   */
  public Document getObjectOfLatestVersion(boolean major, OperationContext context) {
    String versionSeriesId = getVersionSeriesId();
    if (versionSeriesId == null) {
      throw new CmisRuntimeException("Version series id is unknown!");
    }

    ObjectData objectData = getProvider().getVersioningService().getObjectOfLatestVersion(
        getRepositoryId(), versionSeriesId, major, context.getFilterString(),
        context.isIncludeAllowableActions(), context.getIncludeRelationships(),
        context.getRenditionFilterString(), context.isIncludePolicies(), context.isIncludeAcls(),
        null);

    ObjectFactory objectFactory = getSession().getObjectFactory();

    CmisObject result = objectFactory.convertObject(objectData, context);
    if (!(result instanceof Document)) {
      throw new CmisRuntimeException("Latest version is not a document!");
    }

    return (Document) result;
  }

  // content operations

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Document#getContentStream()
   */
  public ContentStream getContentStream() {
    String objectId = getObjectId();

    // get the stream
    ContentStreamData contentStream = getProvider().getObjectService().getContentStream(
        getRepositoryId(), objectId, null, null, null, null);

    // TODO: what should happen if the length is not set?
    long length = (contentStream.getLength() == null ? -1 : contentStream.getLength().longValue());

    // convert and return stream object
    return getSession().getObjectFactory().createContentStream(contentStream.getFilename(), length,
        contentStream.getMimeType(), contentStream.getStream());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Document#setContentStream(boolean,
   * org.apache.opencmis.client.api.ContentStream)
   */
  public ObjectId setContentStream(boolean overwrite, ContentStream contentStream) {
    String objectId;
    String changeToken;

    readLock();
    try {
      objectId = getObjectId();
      changeToken = getPropertyValue(PropertyIds.CMIS_CHANGE_TOKEN);
    }
    finally {
      readUnlock();
    }

    Holder<String> objectIdHolder = new Holder<String>(objectId);
    Holder<String> changeTokenHolder = new Holder<String>(changeToken);

    getProvider().getObjectService().setContentStream(getRepositoryId(), objectIdHolder, overwrite,
        changeTokenHolder, getObjectFactory().convertContentStream(contentStream), null);

    if (objectIdHolder.getValue() == null) {
      return null;
    }

    return getSession().createObjectId(objectIdHolder.getValue());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Document#deleteContentStream()
   */
  public ObjectId deleteContentStream() {
    String objectId;
    String changeToken;

    readLock();
    try {
      objectId = getObjectId();
      changeToken = getPropertyValue(PropertyIds.CMIS_CHANGE_TOKEN);
    }
    finally {
      readUnlock();
    }

    Holder<String> objectIdHolder = new Holder<String>(objectId);
    Holder<String> changeTokenHolder = new Holder<String>(changeToken);

    getProvider().getObjectService().deleteContentStream(getRepositoryId(), objectIdHolder,
        changeTokenHolder, null);

    if (objectIdHolder.getValue() == null) {
      return null;
    }

    return getSession().createObjectId(objectIdHolder.getValue());
  }
}
