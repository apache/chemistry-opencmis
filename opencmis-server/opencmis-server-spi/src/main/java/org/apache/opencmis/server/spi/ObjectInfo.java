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
 * This class contains information about an object. This data is used to generate the appropriate
 * links in AtomPub entries and feeds.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public interface ObjectInfo {

  String getId();

  String getName();

  String getCreatedBy();

  GregorianCalendar getCreationDate();

  GregorianCalendar getLastModificationDate();

  String getTypeId();

  BaseObjectTypeIds getBaseType();

  boolean isCurrentVersion();

  boolean hasVersionHistory();

  String getWorkingCopyId();

  String getWorkingCopyOriginalId();

  boolean hasContent();

  String getContentType();

  String getFileName();

  List<RenditionInfo> getRenditionInfos();

  boolean supportsRelationships();

  boolean supportsPolicies();

  boolean hasAcl();

  boolean hasParent();

  boolean supportsDescendants();

  boolean supportsFolderTree();

  List<String> getRelationshipSourceIds();

  List<String> getRelationshipTargetIds();
}
