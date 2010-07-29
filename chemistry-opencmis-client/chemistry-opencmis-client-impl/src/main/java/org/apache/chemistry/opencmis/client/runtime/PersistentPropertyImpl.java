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
package org.apache.chemistry.opencmis.client.runtime;

import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyData;

/**
 * Property Implementation.
 */
public class PersistentPropertyImpl<T> extends AbstractPropertyData<T> implements Property<T>, Serializable {

    private static final long serialVersionUID = 1L;
    private PropertyDefinition<T> propertyDefinition;

    protected void initialize(PropertyDefinition<?> pd) {
        setId(pd.getId());
        setDisplayName(pd.getDisplayName());
        setLocalName(pd.getLocalName());
        setQueryName(pd.getQueryName());
    }

    /**
     * Constructs a single-value property.
     */
    @SuppressWarnings("unchecked")
    public PersistentPropertyImpl(PropertyDefinition<?> pd, T value) {
        if (pd == null) {
            throw new IllegalArgumentException("Type must be set!");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value must be set!");
        }
        propertyDefinition = (PropertyDefinition<T>) pd;
        initialize(pd);
        setValue(value);
    }

    /**
     * Constructs a multi-value property.
     */
    @SuppressWarnings("unchecked")
    public PersistentPropertyImpl(PropertyDefinition<?> pd, List<T> values) {
        if (pd == null) {
            throw new IllegalArgumentException("Type must be set!");
        }
        propertyDefinition = (PropertyDefinition<T>) pd;
        initialize(pd);
        setValues(values);
    }

    public PropertyDefinition<T> getDefinition() {
        return propertyDefinition;
    }

    public PropertyType getType() {
        return propertyDefinition.getPropertyType();
    }

    public String getValueAsString() {
        List<T> values = getValues();
        if (values.size() == 0) {
            return null;
        }

        return formatValue(values.get(0));
    }

    public String getValuesAsString() {
        List<T> values = getValues();

        StringBuilder result = new StringBuilder();
        for (T value : values) {
            if (result.length() > 0) {
                result.append(", ");
            }

            result.append(formatValue(value));
        }

        return "[" + result.toString() + "]";
    }

    private String formatValue(T value) {
        String result;

        if (value == null) {
            return null;
        }

        if (value instanceof GregorianCalendar) {
            result = ((GregorianCalendar) value).getTime().toString();
        } else {
            result = value.toString();
        }

        return result;
    }

    public boolean isMultiValued() {
        return propertyDefinition.getCardinality() == Cardinality.MULTI;
    }
}
