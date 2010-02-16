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
package org.apache.opencmis.commons.impl.dataobjects;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;

/**
 * Properties data implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class PropertiesDataImpl extends AbstractExtensionData implements PropertiesData {

  Map<String, PropertyData<?>> fProperties = new LinkedHashMap<String, PropertyData<?>>();

  /**
   * Constructor.
   */
  public PropertiesDataImpl() {
  }

  /**
   * Constructor.
   * 
   * @param properties
   *          initial list of properties
   */
  public PropertiesDataImpl(List<PropertyData<?>> properties) {
    if (properties != null) {
      for (PropertyData<?> prop : properties) {
        addProperty(prop);
      }
    }
  }

  public Map<String, PropertyData<?>> getProperties() {
    return Collections.unmodifiableMap(fProperties);
  }

  /**
   * Adds a property.
   * 
   * @param property
   *          the property
   */
  public void addProperty(PropertyData<?> property) {
    if (property == null) {
      return;
    }

    fProperties.put(property.getId(), property);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Properties Data [properties=" + fProperties + "]" + super.toString();
  }

}
