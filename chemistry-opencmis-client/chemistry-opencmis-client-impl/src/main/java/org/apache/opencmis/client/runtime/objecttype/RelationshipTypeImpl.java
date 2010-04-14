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
package org.apache.opencmis.client.runtime.objecttype;

import java.util.ArrayList;
import java.util.List;

import org.apache.opencmis.client.api.Session;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.objecttype.RelationshipType;
import org.apache.opencmis.commons.api.RelationshipTypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinition;

/**
 * Relationship type.
 */
public class RelationshipTypeImpl extends AbstractObjectType implements RelationshipType {

  private List<ObjectType> allowedSourceTypes;
  private List<ObjectType> allowedTargetTypes;

  /**
   * Constructor.
   */
  public RelationshipTypeImpl(Session session, TypeDefinition typeDefinition) {
    initialize(session, typeDefinition);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.RelationshipType#getAllowedSourceTypes()
   */
  public List<ObjectType> getAllowedSourceTypes() {
    if (allowedSourceTypes == null) {
      List<ObjectType> types = new ArrayList<ObjectType>();

      List<String> ids = ((RelationshipTypeDefinition) getTypeDefinition()).getAllowedSourceTypes();
      if (ids != null) {
        for (String id : ids) {
          types.add(getSession().getTypeDefinition(id));
        }
      }

      allowedSourceTypes = types;
    }

    return allowedSourceTypes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.RelationshipType#getAllowedTargetTypes()
   */
  public List<ObjectType> getAllowedTargetTypes() {
    if (allowedTargetTypes == null) {
      List<ObjectType> types = new ArrayList<ObjectType>();

      List<String> ids = ((RelationshipTypeDefinition) getTypeDefinition()).getAllowedTargetTypes();
      if (ids != null) {
        for (String id : ids) {
          types.add(getSession().getTypeDefinition(id));
        }
      }

      allowedTargetTypes = types;
    }

    return allowedTargetTypes;
  }

}
