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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.opencmis.client.api.Ace;
import org.apache.opencmis.client.api.Acl;
import org.apache.opencmis.client.api.AllowableActions;
import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.ContentStream;
import org.apache.opencmis.client.api.OperationContext;
import org.apache.opencmis.client.api.Policy;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.QueryProperty;
import org.apache.opencmis.client.api.QueryResult;
import org.apache.opencmis.client.api.Rendition;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.AllowableActionsData;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.RenditionData;

/**
 * A factory to create CMIS objects.
 * 
 * @see org.apache.opencmis.client.api.Session#getObjectFactory()
 */
public interface ObjectFactory {

  // allowable actions

  AllowableActions createAllowableAction(Map<String, Boolean> actions);

  AllowableActions convertAllowableActions(AllowableActionsData allowableActions);

  // ACL and ACE

  Ace createAce(String principal, List<String> permissions, boolean isDirect);

  Acl createAcl(List<Ace> aces, Boolean isExact);

  AccessControlList convertAces(List<Ace> aces);

  Acl convertAcl(AccessControlList acl);

  // policies

  List<String> convertPolicies(List<Policy> policies);

  // renditions

  Rendition convertRendition(String objectId, RenditionData rendition);

  // content stream

  ContentStream createContentStream(String filename, long length, String mimetype,
      InputStream stream);

  ContentStreamData convertContentStream(ContentStream contentStream);

  // types

  ObjectType convertTypeDefinition(TypeDefinition typeDefinition);

  ObjectType getTypeFromObjectData(ObjectData objectData);

  // properties

  <T> Property<T> createProperty(PropertyDefinition<T> type, T value);

  <T> Property<T> createPropertyMultivalue(PropertyDefinition<T> type, List<T> values);

  Map<String, Property<?>> convertProperties(ObjectType objectType, PropertiesData properties);

  PropertiesData convertProperties(Collection<Property<?>> properties);

  List<QueryProperty<?>> convertQueryProperties(PropertiesData properties);

  // objects

  CmisObject convertObject(ObjectData objectData, OperationContext context);

  QueryResult convertQueryResult(ObjectData objectData);
}
