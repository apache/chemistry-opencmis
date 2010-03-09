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
package org.apache.opencmis.client.api.repository;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.opencmis.client.api.Ace;
import org.apache.opencmis.client.api.Acl;
import org.apache.opencmis.client.api.AllowableActions;
import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.ContentStream;
import org.apache.opencmis.client.api.Document;
import org.apache.opencmis.client.api.Folder;
import org.apache.opencmis.client.api.OperationContext;
import org.apache.opencmis.client.api.Policy;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.QueryResult;
import org.apache.opencmis.client.api.Relationship;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.provider.ObjectData;

/**
 * A factory to create CMIS objects.
 * 
 * @see org.apache.opencmis.client.api.Session#getObjectFactory()
 */
public interface ObjectFactory {

  // object factory

  AllowableActions createAllowableAction(Map<String, Boolean> actions);

  Ace createAce(String principal, List<String> permissions, boolean isDirect);

  Acl createAcl(List<Ace> aces, Boolean isExact);

  ContentStream createContentStream(String filename, long length, String mimetype,
      InputStream stream);

  // object service

  // shortcut
  Document createDocument(Folder parentfolder, String name);

  Document createDocument(List<Property<?>> properties, Folder parentfolder,
      ContentStream contentstream, VersioningState versioningState, List<Policy> policies,
      List<Ace> addACEs, List<Ace> removeACEs);

  Document createDocumentFromSource(Document source, List<Property<?>> properties,
      Folder parentfolder, VersioningState versioningState, List<Policy> policies,
      List<Ace> addACEs, List<Ace> removeACEs);

  Relationship createRelationship(List<Property<?>> properties, List<Policy> policies,
      List<Ace> addACEs, List<Ace> removeACEs);

  Policy createPolicy(List<Property<?>> properties, Folder parentfolder, List<Policy> policies,
      List<Ace> addACEs, List<Ace> removeACEs);

  Folder createFolder(Folder parent, List<Property<?>> properties, List<Policy> policies,
      List<Ace> addACEs, List<Ace> removeACEs);

  CmisObject convertObject(ObjectData objectData, OperationContext context);

  QueryResult convertQueryResult(ObjectData objectData);
}
