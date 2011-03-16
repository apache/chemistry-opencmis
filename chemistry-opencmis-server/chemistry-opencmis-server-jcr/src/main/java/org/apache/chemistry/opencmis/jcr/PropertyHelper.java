/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.chemistry.opencmis.jcr;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Helper class with utility functions for handling {@link Properties}.
 */
public final class PropertyHelper {
    private PropertyHelper() {
    }

    /**
     * Retrieve a string value.
     *
     * @param properties
     * @param name  the name of the value to retrieve
     * @return  the first value of the given <code>name</code> or <code>null</code> if either
     *      these are no string properties or no property of <code>name</code> exists. 
     */
    public static String getStringProperty(Properties properties, String name) {
        PropertyData<?> property = properties.getProperties().get(name);
        if (!(property instanceof PropertyString)) {
            return null;
        }

        return ((PropertyString) property).getFirstValue();
    }

    /**
     * Gets the type id from a set of properties.
     */
    public static String getTypeId(Properties properties) {
        PropertyData<?> typeProperty = properties.getProperties().get(PropertyIds.OBJECT_TYPE_ID);
        if (!(typeProperty instanceof PropertyId)) {
            throw new CmisInvalidArgumentException("Type id must be set!");
        }

        String typeId = ((PropertyId) typeProperty).getFirstValue();
        if (typeId == null) {
            throw new CmisInvalidArgumentException("Type id must be set!");
        }

        return typeId;
    }

    /**
     * @param prop
     * @return  <code>true</code> iff <code>prop</code> denotes an empty property data value
     */
    public static boolean isPropertyEmpty(PropertyData<?> prop) {
        return prop == null || prop.getValues() == null || prop.getValues().isEmpty();
    }

    /**
     * Determine the default property data value for a given property definition.
     * @param propDef
     * @return
     * @throws CmisRuntimeException  if <code>propDef</code> is invalid or unknown.
     */
    @SuppressWarnings("unchecked")
    public static PropertyData<?> getDefaultValue(PropertyDefinition<?> propDef) {
        if (propDef == null) {
            return null;
        }

        List<?> defaultValue = propDef.getDefaultValue();
        if (defaultValue != null && !defaultValue.isEmpty()) {
            switch (propDef.getPropertyType()) {
                case BOOLEAN:
                    return new PropertyBooleanImpl(propDef.getId(), (List<Boolean>) defaultValue);
                case DATETIME:
                    return new PropertyDateTimeImpl(propDef.getId(), (List<GregorianCalendar>) defaultValue);
                case DECIMAL:
                    return new PropertyDecimalImpl(propDef.getId(), (List<BigDecimal>) defaultValue);
                case HTML:
                    return new PropertyHtmlImpl(propDef.getId(), (List<String>) defaultValue);
                case ID:
                    return new PropertyIdImpl(propDef.getId(), (List<String>) defaultValue);
                case INTEGER:
                    return new PropertyIntegerImpl(propDef.getId(), (List<BigInteger>) defaultValue);
                case STRING:
                    return new PropertyStringImpl(propDef.getId(), (List<String>) defaultValue);
                case URI:
                    return new PropertyUriImpl(propDef.getId(), (List<String>) defaultValue);
                default:
                    throw new CmisRuntimeException("Unknown datatype: " + propDef.getPropertyType());
            }
        }
        return null;
    }

}
