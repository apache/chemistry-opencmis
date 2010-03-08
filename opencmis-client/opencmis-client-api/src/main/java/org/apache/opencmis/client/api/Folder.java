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

import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.util.Container;
import org.apache.opencmis.client.api.util.PagingList;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;

/**
 * Domain Model 2.5
 */
public interface Folder extends FileableCmisObject {

  // object service
  Document createDocument(String name, String typeId);

  Document createDocument(List<Property<?>> properties, ContentStream contentstream,
      VersioningState versioningState, List<Policy> policies, List<Ace> addACEs,
      List<Ace> removeACEs);

  Document createDocumentFromSource(Document source, List<Property<?>> properties,
      VersioningState versioningState, List<Policy> policies, List<Ace> addACEs,
      List<Ace> removeACEs);

  Folder createFolder(List<Property<?>> properties, List<Policy> policies, List<Ace> addACEs,
      List<Ace> removeACEs);

  Policy createPolicy(List<Property<?>> properties, List<Policy> policies, List<Ace> addACEs,
      List<Ace> removeACEs);

  /**
   * @return list of object ids which failed to be deleted
   */
  List<String> deleteTree(boolean allversions, UnfileObjects unfile, boolean continueOnFailure);

  // navigation service

  List<Container<FileableCmisObject>> getFolderTree(int depth);

  List<Container<FileableCmisObject>> getFolderTree(int depth, OperationContext context);

  List<Container<FileableCmisObject>> getDescendants(int depth);

  List<Container<FileableCmisObject>> getDescendants(int depth, OperationContext context);

  PagingList<CmisObject> getChildren(int itemsPerPage);

  PagingList<CmisObject> getChildren(OperationContext context, int itemsPerPage);

  boolean isRootFolder();
  
  Folder getFolderParent();

  String getPath();

  /**
   * Returns all checked out documents of this folder.
   */
  PagingList<Document> getCheckedOutDocs(int itemsPerPage);

  PagingList<Document> getCheckedOutDocs(OperationContext context, int itemsPerPage);

  // folder specific properties

  List<ObjectType> getAllowedChildObjectTypes(); // cmis:allowedChildObjectTypeIds
}
