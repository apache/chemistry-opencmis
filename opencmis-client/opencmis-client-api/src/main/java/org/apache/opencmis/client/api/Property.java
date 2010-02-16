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

import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.enums.PropertyType;

/**
 * {@see org.apache.opencmis.client.api.CMISObject#getProperties()}, {@see
 * org.apache.opencmis.client.api.Session#createProperty(String, Object)} and {@see
 * org.apache.opencmis.client.api.Session#createPropertyMultivalue(String, List)}. Domain Model 2.2.1
 * 
 * @param <T>
 */
public interface Property<T> {

  // property

  public boolean isMultiValued();

  public PropertyType getType();

  public PropertyDefinition<T> getDefinition();

  public String getId();

  public String getLocalName();

  public String getDisplayName();

  public String getQueryName();

  public String getValueAsString();

  public T getValue();

  public List<T> getValues();

}
