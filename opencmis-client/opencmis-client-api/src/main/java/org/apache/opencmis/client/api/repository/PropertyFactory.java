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

import java.util.List;

import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.commons.enums.CmisProperties;
import org.apache.opencmis.commons.enums.PropertyType;

/**
 * A factory to create properties.
 * 
 * @see org.apache.opencmis.client.api.Session#getPropertyFactory()
 */
public interface PropertyFactory {

  // property factory

  <T> Property<T> createProperty(String id, PropertyType type, T value);

  <T> Property<T> createPropertyMultivalue(String id, PropertyType type, List<T> value);

  <T> Property<T> createCmisProperty(CmisProperties p, T value);

}
