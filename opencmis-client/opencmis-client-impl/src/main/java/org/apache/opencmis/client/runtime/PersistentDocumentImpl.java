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

import java.util.List;

import org.apache.opencmis.client.api.Ace;
import org.apache.opencmis.client.api.ContentStream;
import org.apache.opencmis.client.api.Document;
import org.apache.opencmis.client.api.Policy;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.Rendition;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.ObjectData;

public class PersistentDocumentImpl extends AbstractPersistentFilableCmisObject implements Document {

  /**
   * Constructor.
   */
  public PersistentDocumentImpl(PersistentSessionImpl session, ObjectType objectType,
      ObjectData objectData) {
    initialize(session, objectType, objectData);
  }

  public void cancelCheckOut() {
    throw new CmisRuntimeException("not implemented");
  }

  public void checkIn(boolean major, List<Property<?>> properties, ContentStream contentStream,
      String checkinComment, List<Policy> policies, List<Ace> addACEs, List<Ace> removeACEs) {
    throw new CmisRuntimeException("not implemented");

  }

  public boolean checkOut() {
    throw new CmisRuntimeException("not implemented");
  }

  public Document copy(List<Property<?>> properties, VersioningState versioningState,
      List<Policy> policies, List<Ace> addACEs, List<Ace> removeACEs) {
    throw new CmisRuntimeException("not implemented");
  }

  public void deleteAllVersions() {
    throw new CmisRuntimeException("not implemented");
  }

  public void deleteContentStream() {
    throw new CmisRuntimeException("not implemented");
  }

  public List<Document> getAllVersions() {
    throw new CmisRuntimeException("not implemented");
  }

  public String getCheckinComment() {
    throw new CmisRuntimeException("not implemented");
  }

  public ContentStream getContentStream() {
    throw new CmisRuntimeException("not implemented");
  }

  public Document getObjectOfLatestVersion(boolean major) {
    throw new CmisRuntimeException("not implemented");
  }

  public List<Property<?>> getPropertiesOfLatestVersion(boolean major) {
    throw new CmisRuntimeException("not implemented");
  }

  public List<Property<?>> getPropertiesOfLatestVersion(boolean major, String filter) {
    throw new CmisRuntimeException("not implemented");
  }

  public List<Rendition> getRenditions() {
    throw new CmisRuntimeException("not implemented");
  }

  public String getVersionLabel() {
    throw new CmisRuntimeException("not implemented");
  }

  public String getVersionSeries() {
    throw new CmisRuntimeException("not implemented");
  }

  public String getVersionSeriesCheckedOut() {
    throw new CmisRuntimeException("not implemented");
  }

  public String getVersionSeriesCheckedOutBy() {
    throw new CmisRuntimeException("not implemented");
  }

  public boolean isImmutable() {
    throw new CmisRuntimeException("not implemented");
  }

  public boolean isLatestMajorVersion() {
    throw new CmisRuntimeException("not implemented");
  }

  public boolean isLatestVersion() {
    throw new CmisRuntimeException("not implemented");
  }

  public boolean isMajorVersion() {
    throw new CmisRuntimeException("not implemented");
  }

  public boolean isVersionSeriesCheckedOut() {
    throw new CmisRuntimeException("not implemented");
  }

  public void setContentStream(boolean overwrite, ContentStream contentStream) {
    throw new CmisRuntimeException("not implemented");
  }
}
