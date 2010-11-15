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
package org.apache.chemistry.opencmis.commons.impl.dataobjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;

/**
 * Properties data implementation.
 */
public class PropertiesImpl extends AbstractExtensionData implements Properties {

    private static final long serialVersionUID = 1L;

    List<PropertyData<?>> propertyList = new ArrayList<PropertyData<?>>();
    Map<String, PropertyData<?>> properties = new LinkedHashMap<String, PropertyData<?>>();

    /**
     * Constructor.
     */
    public PropertiesImpl() {
    }

    /**
     * Constructor.
     * 
     * @param properties
     *            initial collection of properties
     */
    public PropertiesImpl(Collection<PropertyData<?>> properties) {
        if (properties != null) {
            for (PropertyData<?> prop : properties) {
                addProperty(prop);
            }
        }
    }

    public Map<String, PropertyData<?>> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public List<PropertyData<?>> getPropertyList() {
        return Collections.unmodifiableList(propertyList);
    }

    /**
     * Adds a property.
     * 
     * @param property
     *            the property
     */
    public void addProperty(PropertyData<?> property) {
        if (property == null) {
            return;
        }

        propertyList.add(property);
        properties.put(property.getId(), property);
    }

    /**
     * Replaces a property.
     * 
     * @param property
     *            the property
     */
    public void replaceProperty(PropertyData<?> property) {
        if ((property == null) || (property.getId() == null)) {
            return;
        }

        removeProperty(property.getId());

        propertyList.add(property);
        properties.put(property.getId(), property);
    }

    /**
     * Removes a property.
     * 
     * @param id
     *            the property id
     */
    public void removeProperty(String id) {
        if (id == null) {
            return;
        }

        Iterator<PropertyData<?>> iterator = propertyList.iterator();
        while (iterator.hasNext()) {
            PropertyData<?> property = iterator.next();
            if (id.equals(property.getId())) {
                iterator.remove();
                break;
            }
        }

        properties.remove(id);
    }

    @Override
    public String toString() {
        return "Properties Data [properties=" + propertyList + "]" + super.toString();
    }

}
