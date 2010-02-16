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
package org.apache.opencmis.server.spi;

import java.util.GregorianCalendar;
import java.util.List;

import org.apache.opencmis.commons.enums.BaseObjectTypeIds;

/**
 * Implementation of the {@link ObjectInfo} interface.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class ObjectInfoImpl implements ObjectInfo {

  private String fId;
  private String fName;
  private String fCreatedBy;
  private GregorianCalendar fCreationDate;
  private GregorianCalendar fLastModificationDate;
  private String fTypeId;
  private BaseObjectTypeIds fBaseObjectTypeId;
  private boolean fIsCurrentVersion = true;
  private boolean fHasVersionHistory = false;
  private String fWorkingCopyId = null;
  private String fWorkingCopyOriginalId = null;
  private boolean fHasContent = true;
  private String fContentType = null;
  private String fFileName = null;
  private List<RenditionInfo> fRenditionInfos = null;
  private boolean fSupportsRelationships = false;
  private boolean fSupportsPolicies = false;
  private boolean fHasAcl = false;
  private boolean fHasParent = true;
  private boolean fSupportsDescendants = false;
  private boolean fSupportsFolderTree = false;
  private List<String> fRelationshipSourceIds = null;
  private List<String> fRelationshipTargetIds = null;

  public ObjectInfoImpl() {
  }

  public ObjectInfoImpl(String id, BaseObjectTypeIds baseObjectTypeId) {
    fId = id;
    fBaseObjectTypeId = baseObjectTypeId;
  }

  public String getId() {
    return fId;
  }

  public void setId(String id) {
    fId = id;
  }

  public String getName() {
    return fName;
  }

  public void setName(String name) {
    fName = name;
  }

  public String getCreatedBy() {
    return fCreatedBy;
  }

  public void setCreatedBy(String createdBy) {
    fCreatedBy = createdBy;
  }

  public GregorianCalendar getCreationDate() {
    return fCreationDate;
  }

  public void setCreationDate(GregorianCalendar creationDate) {
    fCreationDate = creationDate;
  }

  public GregorianCalendar getLastModificationDate() {
    return fLastModificationDate;
  }

  public void setLastModificationDate(GregorianCalendar lastModificationDate) {
    fLastModificationDate = lastModificationDate;
  }

  public String getTypeId() {
    return fTypeId;
  }

  public void setTypeId(String typeId) {
    fTypeId = typeId;
  }

  public BaseObjectTypeIds getBaseType() {
    return fBaseObjectTypeId;
  }

  public void setBaseType(BaseObjectTypeIds baseObjectTypeId) {
    fBaseObjectTypeId = baseObjectTypeId;
  }

  public boolean isCurrentVersion() {
    return fIsCurrentVersion;
  }

  public void setIsCurrentVersion(boolean currentVersion) {
    fIsCurrentVersion = currentVersion;
  }

  public boolean hasVersionHistory() {
    return fHasVersionHistory;
  }

  public void setHasVersionHistory(boolean hasVersionHistory) {
    fHasVersionHistory = hasVersionHistory;
  }

  public String getWorkingCopyId() {
    return fWorkingCopyId;
  }

  public void setWorkingCopyId(String workingCopyId) {
    fWorkingCopyId = workingCopyId;
  }

  public String getWorkingCopyOriginalId() {
    return fWorkingCopyOriginalId;
  }

  public void setWorkingCopyOriginalId(String workingCopyOriginalId) {
    fWorkingCopyOriginalId = workingCopyOriginalId;
  }

  public boolean hasContent() {
    return fHasContent;
  }

  public void setHasContent(boolean hasContent) {
    fHasContent = hasContent;
  }

  public String getContentType() {
    return fContentType;
  }

  public void setContentType(String contentType) {
    fContentType = contentType;
  }

  public String getFileName() {
    return fFileName;
  }

  public void setFileName(String fileName) {
    fFileName = fileName;
  }

  public List<RenditionInfo> getRenditionInfos() {
    return fRenditionInfos;
  }

  public void setRenditionInfos(List<RenditionInfo> renditions) {
    fRenditionInfos = renditions;
  }

  public boolean supportsRelationships() {
    return fSupportsRelationships;
  }

  public void setSupportsRelationships(boolean supportsRelationships) {
    fSupportsRelationships = supportsRelationships;
  }

  public boolean supportsPolicies() {
    return fSupportsPolicies;
  }

  public void setSupportsPolicies(boolean supportsPolicies) {
    fSupportsPolicies = supportsPolicies;
  }

  public boolean hasAcl() {
    return fHasAcl;
  }

  public void setHasAcl(boolean hasAcl) {
    fHasAcl = hasAcl;
  }

  public boolean hasParent() {
    return fHasParent;
  }

  public void setHasParent(boolean hasParent) {
    fHasParent = hasParent;
  }

  public boolean supportsDescendants() {
    return fSupportsDescendants;
  }

  public void setSupportsDescendants(boolean supportsDescendants) {
    fSupportsDescendants = supportsDescendants;
  }

  public boolean supportsFolderTree() {
    return fSupportsFolderTree;
  }

  public void setSupportsFolderTree(boolean supportsFolderTree) {
    fSupportsFolderTree = supportsFolderTree;
  }

  public List<String> getRelationshipSourceIds() {
    return fRelationshipSourceIds;
  }

  public void setRelationshipSourceIds(List<String> relationshipSourceIds) {
    fRelationshipSourceIds = relationshipSourceIds;
  }

  public List<String> getRelationshipTargetIds() {
    return fRelationshipTargetIds;
  }

  public void setRelationshipTargetIds(List<String> relationshipTargetIds) {
    fRelationshipTargetIds = relationshipTargetIds;
  }
}
