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

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.util.PagingList;
import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.enums.RelationshipDirection;

/**
 * Base CMIS object.
 * 
 * See CMIS Domain Model - section 2.1.2.
 */
public interface CmisObject extends ObjectId {

  // common properties

  /**
   * Get the name of this object. {@code Property<String> 'cmis:name'}
   */
  String getName();

  /**
   * Set the name of this object. {@code Property<String> 'cmis:name'}
   */
  void setName(String name);

  /**
   * Get the id of the user who created the object (maintained by the repository). {@code
   * Property<String> 'cmis:createdBy'}
   */
  String getCreatedBy();

  /**
   * Get the timestamp when the object was created (maintained by the repository). {@code
   * Property<GregorianCalendar> 'cmis:creationDate'}
   */
  GregorianCalendar getCreationDate();

  /**
   * Get the id of the user who changed the object (maintained by the repository). {@code
   * Property<String> 'cmis:lastModifiedBy'}
   */
  String getLastModifiedBy();

  /**
   * Get the timestamp when the object was changed (maintained by the repository). {@code
   * Property<GregorianCalendar> 'cmis:lastModificationDate'}
   */
  GregorianCalendar getLastModificationDate();

  /**
   * Get the object's base type (maintained by the repository). {@code Property<String>
   * 'cmis:baseTypeId'}
   */
  ObjectType getBaseType();

  /**
   * Get the object's type. {@code Property<String> 'cmis:objectTypeId'}
   */
  ObjectType getType();

  /**
   * Get the type's base type id.
   * 
   * @return
   */
  BaseObjectTypeIds getBaseTypeId();

  /**
   * Get the change token for this object (maintained by the repository). {@code Property<String>
   * 'cmis:changeToken'}
   */
  String getChangeToken();

  // object

  List<Property<?>> getProperties();

  <T> Property<T> getProperty(String id);

  <T> T getPropertyValue(String id);

  <T> List<T> getPropertyMultivalue(String id);

  AllowableActions getAllowableActions();

  List<Relationship> getRelationships();

  Acl getAcl();

  // object service

  void delete(boolean allVersions);

  ObjectId updateProperties();

  ObjectId updateProperties(Map<String, Object> properties);

  // relationship service

  PagingList<Relationship> getRelationships(boolean includeSubRelationshipTypes,
      RelationshipDirection relationshipDirection, ObjectType type, OperationContext context,
      int itemsPerPage);

  // renditions

  List<Rendition> getRenditions();

  // policy service

  void applyPolicy(ObjectId policyId);

  void removePolicy(ObjectId policyId);

  List<Policy> getPolicies();

  // ACL service

  Acl getAcl(boolean onlyBasicPermissions);

  Acl applyAcl(List<Ace> addAces, List<Ace> removeAces, AclPropagation aclPropagation);

  void addAcl(List<Ace> addAces, AclPropagation aclPropagation);

  void removeAcl(List<Ace> removeAces, AclPropagation aclPropagation);

  // buffered stuff

  <T> void setProperty(String id, T value);

  <T> void setPropertyMultivalue(String id, List<T> value);

  // void saveProperties(); // flush buffered ...Propert...-calls

  // void saveAcl(); // flush buffered ...Acl...-calls

  // session handling

  /**
   * Returns true, if this object has pending changes which are not synced with the backend.
   */
  boolean isChanged();

  /**
   * Returns the timestamp (in milliseconds) of the last refresh.
   */
  long getRefreshTimestamp();

  /**
   * Reloads the data from the repository.
   */
  void refresh();

  /**
   * Reloads the data from the repository if the last refresh did not occur within
   * <code>durationInMillis</code>.
   */
  void refreshIfOld(long durationInMillis);
}
