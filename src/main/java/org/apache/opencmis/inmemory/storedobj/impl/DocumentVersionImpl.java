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
package org.apache.opencmis.inmemory.storedobj.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;
import org.apache.opencmis.inmemory.FilterParser;
import org.apache.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.opencmis.inmemory.storedobj.api.Folder;
import org.apache.opencmis.inmemory.storedobj.api.VersionedDocument;

/**
 * A class representing a single version of a document
 * 
 * @author Jens
 *
 */
public class DocumentVersionImpl extends StoredObjectImpl implements DocumentVersion {
  
  private ContentStreamDataImpl fContent;  
  private VersionedDocument fContainer; // the document this version belongs to
  private String fComment; // checkin comment
  boolean fIsMajor;
  boolean fIsPwc; // true if this is the PWC
  
  public DocumentVersionImpl(String repositoryId, VersionedDocument container, ContentStreamData content,
      VersioningState verState, ObjectStoreImpl objStore) {
    super(objStore);
    setRepositoryId(repositoryId);
    fContainer = container;
    setContent(content, false);
    fIsMajor = verState == VersioningState.MAJOR;
    fIsPwc = verState == VersioningState.CHECKEDOUT;
    fProperties = new HashMap<String, PropertyData<?>>(); // ensure that we have a map
  }
  
  public void setContent(ContentStreamData content, boolean mustPersist) {
    if (null == content) {
      fContent = null;
    } else {     
      fContent = new ContentStreamDataImpl();
      fContent.setFileName(content.getFilename());
      fContent.setMimeType(content.getMimeType());
      try {
        fContent.setContent(content.getStream());
      }
      catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to get content from InputStream" , e);
      }
    }  
  }
  
  public String getCheckinComment() {
    return fComment;
  }

  public String getVersionLabel() {
    int majorNo = 0;
    int minorNo = 0;
    List<DocumentVersion> allVersions = fContainer.getAllVersions();
    for (DocumentVersion ver : allVersions) {
      if (ver.isMajor()) {
        ++majorNo;
        minorNo = 0;
      } else
        ++minorNo;
    }
    String label = "V " + majorNo + "." + minorNo;
    return label;
  }

  public boolean isMajor() {
    return fIsMajor && !isPwc();
  }

  public boolean isPwc() {
    return fIsPwc;
  }

  public void commit(boolean isMajor) {
    fIsPwc = false; // unset working copy flag
    fIsMajor = isMajor;
  }
  
  public ContentStreamData getContent(long offset, long length) {
    if (offset<=0 && length<0)
      return fContent;
    else
      return fContent.getCloneWithLimits(offset, length);
  }
  
  public VersionedDocument getParentDocument() {
    return fContainer;
  }

  private boolean isLatestVersion() {
    List<DocumentVersion> allVers = fContainer.getAllVersions();
    boolean isLatestVersion;
    if (isPwc())
      isLatestVersion = allVers.size()>1 && allVers.get(allVers.size()-2).equals(this);
    else
      isLatestVersion = allVers.get(allVers.size()-1).equals(this);
    return isLatestVersion;
  }

  private boolean isLatestMajorVersion() {
    if (!fIsMajor)
      return false;
    
    List<DocumentVersion> allVersions = fContainer.getAllVersions();
    DocumentVersion latestMajor=null;
    
    for (DocumentVersion ver : allVersions)
      if (ver.isMajor() && !ver.isPwc()) 
        latestMajor = ver;

    boolean isLatestMajorVersion = latestMajor == this;
    return isLatestMajorVersion;
  }
  
//  public void persist() {
//    if (null==fId)
//      fId = UUID.randomUUID().toString();
//  }
  
  public void fillProperties(Map<String, PropertyData<?>> properties, ProviderObjectFactory objFactory,
      List<String> requestedIds) {
    
    DocumentVersion pwc = fContainer.getPwc();

    // First get the properties of the container (like custom type properties, etc)
    fContainer.fillProperties(properties, objFactory, requestedIds);
    
    // overwrite the version specific properties (like modification date, user, etc.)
    // and set some properties specific to the version
    super.fillProperties(properties, objFactory, requestedIds);
    
    // fill the version related properties 
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_IS_LATEST_VERSION, requestedIds)) {
      properties.put(PropertyIds.CMIS_IS_LATEST_VERSION, objFactory.createPropertyBooleanData(PropertyIds.CMIS_IS_LATEST_VERSION, isLatestVersion()));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_IS_MAJOR_VERSION, requestedIds)) {
      properties.put(PropertyIds.CMIS_IS_MAJOR_VERSION, objFactory.createPropertyBooleanData(PropertyIds.CMIS_IS_MAJOR_VERSION, fIsMajor));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_IS_LATEST_MAJOR_VERSION, requestedIds)) {
      properties.put(PropertyIds.CMIS_IS_LATEST_MAJOR_VERSION, objFactory.createPropertyBooleanData(PropertyIds.CMIS_IS_LATEST_MAJOR_VERSION, isLatestMajorVersion()));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_VERSION_SERIES_ID, requestedIds)) { 
      properties.put(PropertyIds.CMIS_VERSION_SERIES_ID, objFactory.createPropertyIdData(PropertyIds.CMIS_VERSION_SERIES_ID, fContainer.getId()));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_IS_VERSION_SERIES_CHECKED_OUT, requestedIds)) {
      properties.put(PropertyIds.CMIS_IS_VERSION_SERIES_CHECKED_OUT, objFactory.createPropertyBooleanData(PropertyIds.CMIS_IS_VERSION_SERIES_CHECKED_OUT, fContainer.isCheckedOut()));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_BY, requestedIds)) {
      properties.put(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_BY, objFactory.createPropertyStringData(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_BY, fContainer.getCheckedOutBy()));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_ID, requestedIds)) {
      properties.put(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_ID, objFactory.createPropertyIdData(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_ID, pwc == null ? null : pwc.getId()));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_CHECKIN_COMMENT, requestedIds)) {
      properties.put(PropertyIds.CMIS_CHECKIN_COMMENT, objFactory.createPropertyStringData(PropertyIds.CMIS_CHECKIN_COMMENT, fComment));
    }    
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_VERSION_LABEL, requestedIds)) {
      properties.put(PropertyIds.CMIS_VERSION_LABEL, objFactory.createPropertyStringData(PropertyIds.CMIS_VERSION_LABEL, getVersionLabel()));
    }
  }

  public Folder getParent() {
    return fContainer.getParent();
  }

  public String getPath() {
    return fContainer.getPath();
  }

  public void move(Folder newParent) {
    fContainer.move(newParent);
  }

  public void setParent(Folder parent) {
    fContainer.setParent(parent);    
  }
  
}
