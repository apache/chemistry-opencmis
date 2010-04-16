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
package org.apache.chemistry.opencmis.client.api.repository;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.QueryProperty;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.objecttype.ObjectType;
import org.apache.chemistry.opencmis.commons.api.Ace;
import org.apache.chemistry.opencmis.commons.api.Acl;
import org.apache.chemistry.opencmis.commons.api.ContentStream;
import org.apache.chemistry.opencmis.commons.api.ObjectData;
import org.apache.chemistry.opencmis.commons.api.PropertiesData;
import org.apache.chemistry.opencmis.commons.api.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.api.RenditionData;
import org.apache.chemistry.opencmis.commons.api.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Updatability;

/**
 * A factory to create CMIS objects.
 *
 * @see org.apache.chemistry.opencmis.client.api.Session#getObjectFactory()
 */
public interface ObjectFactory {

  // ACL and ACE

  Acl convertAces(List<Ace> aces);

  // policies

  List<String> convertPolicies(List<Policy> policies);

  // renditions

  Rendition convertRendition(String objectId, RenditionData rendition);

  // content stream

  ContentStream createContentStream(String filename, long length, String mimetype,
      InputStream stream);

  ContentStream convertContentStream(ContentStream contentStream);

  // types

  ObjectType convertTypeDefinition(TypeDefinition typeDefinition);

  ObjectType getTypeFromObjectData(ObjectData objectData);

  // properties

  <T> Property<T> createProperty(PropertyDefinition<?> type, T value);

  <T> Property<T> createPropertyMultivalue(PropertyDefinition<?> type, List<T> values);

  Map<String, Property<?>> convertProperties(ObjectType objectType, PropertiesData properties);

  PropertiesData convertProperties(Map<String, ?> properties, ObjectType type,
      Set<Updatability> updatabilityFilter);

  List<QueryProperty<?>> convertQueryProperties(PropertiesData properties);

  // objects

  CmisObject convertObject(ObjectData objectData, OperationContext context);

  QueryResult convertQueryResult(ObjectData objectData);
}
