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
package org.apache.opencmis.client.api;

import java.util.List;

import org.apache.opencmis.commons.enums.VersioningState;

/**
 * Domain Model 2.4
 */
public interface Document extends CmisObject {

	List<String> getPaths();
	
	List<Rendition> getRenditions();

  // object service

  ContentStream getContentStream();

  void setContentStream(boolean overwrite, ContentStream contentStream);

  void deleteContentStream();

  // versioning service

  boolean checkOut(); // returns contentCopied

  void cancelCheckOut();

  void checkIn(boolean major, List<Property<?>> properties, ContentStream contentStream,
      String checkinComment, List<Policy> policies, List<Ace> addACEs, List<Ace> removeACEs);

  Document getObjectOfLatestVersion(boolean major);

  List<Property<?>> getPropertiesOfLatestVersion(boolean major);

  List<Property<?>> getPropertiesOfLatestVersion(boolean major, String filter);

  List<Document> getAllVersions();

  // document specific properties

  boolean isImmutable(); // cmis:isImmutable

  boolean isLatestVersion(); // cmis:isLatestVersion

  boolean isMajorVersion(); // cmis:isMajorVersion

  boolean isLatestMajorVersion(); // cmis:isLatestMajorVersion

  String getVersionLabel(); // cmis:versionLabel

  String getVersionSeries(); // cmis:versionSeriesId

  boolean isVersionSeriesCheckedOut(); // cmis:isVersionSeriesCheckedOut

  String getVersionSeriesCheckedOutBy(); // cmis:versionSeriesCheckedOutBy

  String getVersionSeriesCheckedOut(); // cmis:versionSeriesCheckedOutId

  void deleteAllVersions();
  
  String getCheckinComment(); // cmis:checkinComment

  /**
   * Shortcut for ObjectFactory.createDocumentFromSource(this, ...).
   * 
   * @param properties
   * @param versioningState
   * @param policies
   * @param addACEs
   * @param removeACEs
   * @return
   */
  Document copy(List<Property<?>> properties, VersioningState versioningState, List<Policy> policies,
      List<Ace> addACEs, List<Ace> removeACEs);

}
