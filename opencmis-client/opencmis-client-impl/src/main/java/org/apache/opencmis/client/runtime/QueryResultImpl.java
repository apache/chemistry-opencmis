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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.opencmis.client.api.AllowableActions;
import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.QueryProperty;
import org.apache.opencmis.client.api.QueryResult;
import org.apache.opencmis.client.api.Relationship;
import org.apache.opencmis.client.api.Rendition;
import org.apache.opencmis.client.api.Session;
import org.apache.opencmis.client.api.repository.ObjectFactory;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.RenditionData;

/**
 * Implementation of <code>QueryResult</code>.
 */
public class QueryResultImpl implements QueryResult, Serializable {

  private static final long serialVersionUID = 1L;

  private Map<String, QueryProperty<?>> propertiesById;
  private Map<String, QueryProperty<?>> propertiesByQueryName;
  private AllowableActions allowableActions;
  private List<Relationship> relationships;
  private List<Rendition> renditions;

  /**
   * Constructor.
   */
  public QueryResultImpl(Session session, ObjectData objectData) {
    if (objectData != null) {

      // handle properties
      if (objectData.getProperties() != null) {
        propertiesById = new LinkedHashMap<String, QueryProperty<?>>();
        propertiesByQueryName = new LinkedHashMap<String, QueryProperty<?>>();

        List<QueryProperty<?>> queryProperties = SessionUtil.convertQueryProperties(session,
            objectData.getProperties());

        for (QueryProperty<?> property : queryProperties) {
          propertiesById.put(property.getId(), property);
          propertiesByQueryName.put(property.getQueryName(), property);
        }
      }

      // handle allowable actions
      if (objectData.getAllowableActions() != null) {
        this.allowableActions = SessionUtil.convertAllowableActions(session, objectData
            .getAllowableActions());
      }

      // handle relationships
      if (objectData.getRelationships() != null) {
        relationships = new ArrayList<Relationship>();
        ObjectFactory of = session.getObjectFactory();
        for (ObjectData rod : objectData.getRelationships()) {
          CmisObject relationship = of.convertObject(rod, session.getDefaultContext());
          if (relationship instanceof Relationship) {
            relationships.add((Relationship) relationship);
          }
        }
      }

      // handle renditions
      if (objectData.getRenditions() != null) {
        this.renditions = new ArrayList<Rendition>();
        for (RenditionData rd : objectData.getRenditions()) {
          this.renditions.add(SessionUtil.convertRendition(session, null, rd));
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.QueryResult#getProperties()
   */
  public List<QueryProperty<?>> getProperties() {
    return new ArrayList<QueryProperty<?>>(propertiesById.values());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.QueryResult#getPropertyById(java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public <T> QueryProperty<T> getPropertyById(String id) {
    return (QueryProperty<T>) propertiesById.get(id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.QueryResult#getPropertyByQueryName(java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public <T> QueryProperty<T> getPropertyByQueryName(String queryName) {
    return (QueryProperty<T>) propertiesByQueryName.get(queryName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.QueryResult#getPropertyValueById(java.lang.String)
   */
  public <T> T getPropertyValueById(String id) {
    QueryProperty<T> property = getPropertyById(id);
    if (property == null) {
      return null;
    }

    return property.getValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.QueryResult#getPropertyValueByQueryName(java.lang.String)
   */
  public <T> T getPropertyValueByQueryName(String queryName) {
    QueryProperty<T> property = getPropertyByQueryName(queryName);
    if (property == null) {
      return null;
    }

    return property.getValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.QueryResult#getPropertyMultivalueById(java.lang.String)
   */
  public <T> List<T> getPropertyMultivalueById(String id) {
    QueryProperty<T> property = getPropertyById(id);
    if (property == null) {
      return null;
    }

    return property.getValues();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.api.QueryResult#getPropertyMultivalueByQueryName(java.lang.String)
   */
  public <T> List<T> getPropertyMultivalueByQueryName(String queryName) {
    QueryProperty<T> property = getPropertyByQueryName(queryName);
    if (property == null) {
      return null;
    }

    return property.getValues();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.QueryResult#getAllowableActions()
   */
  public AllowableActions getAllowableActions() {
    return allowableActions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.QueryResult#getRelationships()
   */
  public List<Relationship> getRelationships() {
    return relationships;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.QueryResult#getRenditions()
   */
  public List<Rendition> getRenditions() {
    return renditions;
  }
}
