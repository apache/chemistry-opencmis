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
import java.util.Collections;
import java.util.List;

import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.enums.Cardinality;
import org.apache.opencmis.commons.enums.PropertyType;

/**
 * Property Implementation.
 */
public class PersistentPropertyImpl<T> implements Property<T>, Serializable {

  /**
   * serialization
   */
  private static final long serialVersionUID = -6586532350183649719L;
  private PropertyDefinition<T> type;
  private List<T> values;

  /**
   * Constructs a single-value property.
   */
  public PersistentPropertyImpl(PropertyDefinition<T> type, T value) {
    if (type == null) {
      throw new IllegalArgumentException("Type must be set!");
    }

    if (value == null) {
      throw new IllegalArgumentException("Value must be set!");
    }

    this.type = type;
    this.values = Collections.singletonList(value);
  }

  /**
   * Constructs a multi-value property.
   */
  public PersistentPropertyImpl(PropertyDefinition<T> type, List<T> values) {
    if (type == null) {
      throw new IllegalArgumentException("Type must be set!");
    }

    this.type = type;
    this.values = values;
  }

  public PropertyDefinition<T> getDefinition() {
    return this.type;
  }

  public String getDisplayName() {
    return this.type.getDisplayName();
  }

  public String getId() {
    return this.type.getId();
  }

  public String getLocalName() {
    return this.type.getLocalName();
  }

  public String getQueryName() {
    return this.type.getQueryName();
  }

  public PropertyType getType() {
    return this.type.getPropertyType();
  }

  public T getValue() {
    if (this.values.size() == 0) {
      return null;
    }
    return this.values.get(0);
  }

  public String getValueAsString() {
    if (this.values.size() == 0) {
      return null;
    }
    switch (this.type.getPropertyType()) {
    default:
      return this.values.get(0).toString();
    }
  }

  public List<T> getValues() {
    if (this.values.size() == 0) {
      return null;
    }
    return this.values;
  }

  public boolean isMultiValued() {
    return this.type.getCardinality() == Cardinality.MULTI;
  }
}
