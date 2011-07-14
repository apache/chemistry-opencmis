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
import org.apache.chemistry.opencmis.commons.data.PropertyBoolean;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyDateTime;
import org.apache.chemistry.opencmis.commons.data.PropertyDecimal;
import org.apache.chemistry.opencmis.commons.data.PropertyHtml;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyInteger;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.data.PropertyUri;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriImpl;
import org.apache.chemistry.opencmis.jcr.util.Util;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NodeType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Utility class providing methods for converting various entities from/to their respective representation
 * in JCR/CMIS.
 */
public final class JcrConverter {
    private JcrConverter() {}

    /**
     * Escapes all illegal JCR name characters of a string.
     * The encoding is loosely modeled after URI encoding, but only encodes
     * the characters it absolutely needs to in order to make the resulting
     * string a valid JCR name.
     * <p/>
     * QName EBNF:<br>
     * <pre>
     * simplename ::= onecharsimplename | twocharsimplename | threeormorecharname
     * onecharsimplename ::= (* Any Unicode character except: '.', '/', ':', '[', ']', '*', '|' or any whitespace character *)
     * twocharsimplename ::= '.' onecharsimplename | onecharsimplename '.' | onecharsimplename onecharsimplename
     * threeormorecharname ::= nonspace string nonspace
     * string ::= char | string char
     * char ::= nonspace | ' '
     * nonspace ::= (* Any Unicode character except: '/', ':', '[', ']', '*', '|' or any whitespace character *)
     * </pre>
     *
     * @param cmisName the name to escape
     * @return the escaped name
     */    
    public static String toJcrName(String cmisName) {
        StringBuffer buffer = new StringBuffer(cmisName.length() * 2);
        for (int i = 0; i < cmisName.length(); i++) {
            char ch = cmisName.charAt(i);
            if (ch == '%' || ch == '/' || ch == ':' || ch == '[' || ch == ']' || ch == '*' || ch == '|'
                    || ch == '\t' || ch == '\r' || ch == '\n'
                    || ch == '.' && cmisName.length() < 3
                    || ch == ' ' && (i == 0 || i == cmisName.length() - 1)) {
                buffer.append('%');
                buffer.append(Character.toUpperCase(Character.forDigit(ch / 16, 16)));
                buffer.append(Character.toUpperCase(Character.forDigit(ch % 16, 16)));
            }
            else {
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }

    /**
     * Checks if the given name is valid a valid JCR name
     *
     * @param name  the name to check
     * @return <code>true</code> if the name is valid, <code>false</code> otherwise.
     */
    public static boolean isValidJcrName(String name) { 
        if (name == null || name.length() == 0) {
            return false;
        }

        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == '%' || ch == '/' || ch == ':' || ch == '[' || ch == ']' || ch == '*' || ch == '|'
                    || ch == '\t' || ch == '\r' || ch == '\n'
                    || ch == '.' && name.length() < 3
                    || ch == ' ' && (i == 0 || i == name.length() - 1)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Convert a JCR <code>Property</code> to a CMIS <code>PropertyData</code>.
     * 
     * @param jcrProperty
     * @return  
     * @throws RepositoryException
     */
    public static PropertyData<?> convert(Property jcrProperty) throws RepositoryException {
        AbstractPropertyData<?> propertyData;

        switch (jcrProperty.getType()) {
            case PropertyType.BINARY:
            case PropertyType.NAME:
            case PropertyType.PATH:
            case PropertyType.REFERENCE:
            case PropertyType.WEAKREFERENCE:
            case PropertyType.STRING:
                propertyData = jcrProperty.isMultiple()
                    ? new PropertyStringImpl(jcrProperty.getName(), toStrings(jcrProperty.getValues()))
                    : new PropertyStringImpl(jcrProperty.getName(), jcrProperty.getString());
                break;

            case PropertyType.LONG:
                propertyData = jcrProperty.isMultiple()
                    ? new PropertyIntegerImpl(jcrProperty.getName(), toInts(jcrProperty.getValues()))
                    : new PropertyIntegerImpl(jcrProperty.getName(), BigInteger.valueOf(jcrProperty.getLong()));
                break;

            case PropertyType.DECIMAL:
                propertyData = jcrProperty.isMultiple()
                    ? new PropertyDecimalImpl(jcrProperty.getName(), toDecs(jcrProperty.getValues()))
                    : new PropertyDecimalImpl(jcrProperty.getName(), jcrProperty.getDecimal());
                break;

            case PropertyType.DOUBLE:
                propertyData = jcrProperty.isMultiple()
                    ? new PropertyDecimalImpl(jcrProperty.getName(), doublesToDecs(jcrProperty.getValues()))
                    : new PropertyDecimalImpl(jcrProperty.getName(), BigDecimal.valueOf(jcrProperty.getDouble()));
                break;

            case PropertyType.DATE:
                propertyData = jcrProperty.isMultiple()
                    ? new PropertyDateTimeImpl(jcrProperty.getName(), toDates(jcrProperty.getValues()))
                    : new PropertyDateTimeImpl(jcrProperty.getName(), Util.toCalendar(jcrProperty.getDate()));
                break;

            case PropertyType.BOOLEAN:
                propertyData = jcrProperty.isMultiple()
                    ? new PropertyBooleanImpl(jcrProperty.getName(), toBools(jcrProperty.getValues()))
                    : new PropertyBooleanImpl(jcrProperty.getName(), jcrProperty.getBoolean());
                break;

            case PropertyType.URI:
                propertyData = jcrProperty.isMultiple()
                    ? new PropertyUriImpl(jcrProperty.getName(), toStrings(jcrProperty.getValues()))
                    : new PropertyUriImpl(jcrProperty.getName(), jcrProperty.getString());
                break;

            default:
                throw new CmisInvalidArgumentException("Invalid property type: " + jcrProperty.getType());
        }

        propertyData.setDisplayName(jcrProperty.getName());
        propertyData.setLocalName(jcrProperty.getName());
        propertyData.setQueryName(jcrProperty.getName());
        
        return propertyData;
    }

    /**
     * Set a property on a JCR node. 
     *
     * @param node  the node to set the property
     * @param propertyData  the property to set
     * @throws RepositoryException
     */
    public static void setProperty(Node node, PropertyData<?> propertyData) throws RepositoryException {
        Value[] values;
        int propertyType;

        if (propertyData instanceof PropertyBoolean) {
            values = toValue((PropertyBoolean) propertyData, node.getSession().getValueFactory());
            propertyType = PropertyType.BOOLEAN;
        }
        else if (propertyData instanceof PropertyDateTime) {
            values = toValue((PropertyDateTime) propertyData, node.getSession().getValueFactory());
            propertyType = PropertyType.DATE;
        }
        else if (propertyData instanceof PropertyDecimal) {
            values = toValue((PropertyDecimal) propertyData, node.getSession().getValueFactory());
            propertyType = PropertyType.DECIMAL;
        }
        else if (propertyData instanceof PropertyHtml) {
            values = toValue((PropertyHtml) propertyData, node.getSession().getValueFactory());
            propertyType = PropertyType.STRING;
        }
        else if (propertyData instanceof PropertyId) {
            values = toValue((PropertyId) propertyData, node.getSession().getValueFactory());
            propertyType = PropertyType.STRING;
        }
        else if (propertyData instanceof PropertyInteger) {
            values = toValue((PropertyInteger) propertyData, node.getSession().getValueFactory());
            propertyType = PropertyType.DECIMAL;
        }
        else if (propertyData instanceof PropertyString) {
            values = toValue((PropertyString) propertyData, node.getSession().getValueFactory());
            propertyType = PropertyType.STRING;
        }
        else if (propertyData instanceof PropertyUri) {
            values = toValue((PropertyUri) propertyData, node.getSession().getValueFactory());
            propertyType = PropertyType.URI;
        }
        else {
            throw new CmisInvalidArgumentException("Invalid property type: " + propertyData);
        }

        String id = propertyData.getId();
        String name;
        if (PropertyIds.NAME.equals(id)) {
            node.addMixin(NodeType.MIX_TITLE);
            name = Property.JCR_TITLE;
        }
        else if (PropertyIds.CONTENT_STREAM_MIME_TYPE.equals(id)) {
            name = Property.JCR_MIMETYPE;
        }
        else {
            name = toJcrName(propertyData.getId());
        }

        if (values.length == 1) {
            node.setProperty(name, values[0]);
        }
        else {
            node.setProperty(name, values, propertyType);
        }
    }

    /**
     * Remove a property from a JCR node
     *
     * @param node  the node from which to remove the property
     * @param propertyData  the property to remove
     * @throws RepositoryException
     */
    public static void removeProperty(Node node, PropertyData<?> propertyData) throws RepositoryException {
        String id = propertyData.getId();
        String name = PropertyIds.NAME.equals(id)
                ? Property.JCR_TITLE :
                toJcrName(propertyData.getId());

        if (node.hasProperty(name)) {
            node.getProperty(name).remove();
        }
    }

    //------------------------------------------< private >---

    /**
     * Convert an array of <code>Value</code>s to a list of <code>String</code>s.
     */
    private static List<String> toStrings(Value[] values) throws RepositoryException {
        ArrayList<String> strings = new ArrayList<String>(values.length);

        for (Value v : values) {
            strings.add(v.getString());
        }

        return strings;
    }

    /**
     * Convert an array of <code>Value</code>s to a list of <code>BigInteger</code>s.
     */
    private static List<BigInteger> toInts(Value[] values) throws RepositoryException {
        ArrayList<BigInteger> ints = new ArrayList<BigInteger>(values.length);

        for (Value v : values) {
            ints.add(BigInteger.valueOf(v.getLong()));
        }

        return ints;
    }

    /**
     * Convert an array of <code>Value</code>s to a list of <code>BigDecimal</code>s.
     */
    private static List<BigDecimal> toDecs(Value[] values) throws RepositoryException {
        ArrayList<BigDecimal> decs = new ArrayList<BigDecimal>(values.length);

        for (Value v : values) {
            decs.add(v.getDecimal());
        }

        return decs;
    }

    /**
     * Convert an array of double <code>Value</code>s to a list of <code>BigInteger</code>s.
     */
    private static List<BigDecimal> doublesToDecs(Value[] values) throws RepositoryException {
        ArrayList<BigDecimal> decs = new ArrayList<BigDecimal>(values.length);

        for (Value v : values) {
            decs.add(BigDecimal.valueOf(v.getDouble()));
        }

        return decs;
    }

    /**
     * Convert an array of <code>Value</code>s to a list of <code>Booleans</code>s.
     */
    private static List<Boolean> toBools(Value[] values) throws RepositoryException {
        ArrayList<Boolean> bools = new ArrayList<Boolean>(values.length);

        for (Value v : values) {
            bools.add(v.getBoolean());
        }

        return bools;
    }

    /**
     * Convert an array of <code>Value</code>s to a list of <code>GregorianCalendar</code>s.
     */
    private static List<GregorianCalendar> toDates(Value[] values) throws RepositoryException {
        ArrayList<GregorianCalendar> dates = new ArrayList<GregorianCalendar>(values.length);

        for (Value v : values) {
            dates.add(Util.toCalendar(v.getDate()));
        }

        return dates;
    }

    /**
     * Convert a <code>PropertyBoolean</code> to an array of JCR <code>Values</code>.
     */
    private static Value[] toValue(PropertyBoolean propertyData, ValueFactory valueFactory) {
        List<Boolean> values = propertyData.getValues();
        if (values == null) {
            return new Value[0];
        }

        Value[] result = new Value[values.size()];
        int k = 0;
        for (Boolean v : values) {
            result[k++] = valueFactory.createValue(v);
        }

        return result;
    }

    /**
     * Convert a <code>PropertyDateTime</code> to an array of JCR <code>Values</code>.
     */
    private static Value[] toValue(PropertyDateTime propertyData, ValueFactory valueFactory) {
        List<GregorianCalendar> values = propertyData.getValues();
        if (values == null) {
            return new Value[0];
        }

        Value[] result = new Value[values.size()];
        int k = 0;
        for (GregorianCalendar v : values) {
            result[k++] = valueFactory.createValue(v);
        }

        return result;
    }

    /**
     * Convert a <code>PropertyDecimal</code> to an array of JCR <code>Values</code>.
     */
    private static Value[] toValue(PropertyDecimal propertyData, ValueFactory valueFactory) {
        List<BigDecimal> values = propertyData.getValues();
        if (values == null) {
            return new Value[0];
        }

        Value[] result = new Value[values.size()];
        int k = 0;
        for (BigDecimal v : values) {
            result[k++] = valueFactory.createValue(v);
        }

        return result;
    }

    /**
     * Convert a <code>PropertyHtml</code> to an array of JCR <code>Values</code>.
     */
    private static Value[] toValue(PropertyHtml propertyData, ValueFactory valueFactory) {
        List<String> values = propertyData.getValues();
        if (values == null) {
            return new Value[0];
        }

        Value[] result = new Value[values.size()];
        int k = 0;
        for (String v : values) {
            result[k++] = valueFactory.createValue(v);
        }

        return result;
    }

    /**
     * Convert a <code>PropertyId</code> to an array of JCR <code>Values</code>.
     */
    private static Value[] toValue(PropertyId propertyData, ValueFactory valueFactory) {
        List<String> values = propertyData.getValues();
        if (values == null) {
            return new Value[0];
        }

        Value[] result = new Value[values.size()];
        int k = 0;
        for (String v : values) {
            result[k++] = valueFactory.createValue(v);
        }

        return result;
    }

    /**
     * Convert a <code>PropertyInteger</code> to an array of JCR <code>Values</code>.
     */
    private static Value[] toValue(PropertyInteger propertyData, ValueFactory valueFactory) {
        List<BigInteger> values = propertyData.getValues();
        if (values == null) {
            return new Value[0];
        }

        Value[] result = new Value[values.size()];
        int k = 0;
        for (BigInteger v : values) {
            result[k++] = valueFactory.createValue(new BigDecimal(v));
        }

        return result;
    }

    /**
     * Convert a <code>PropertyString</code> to an array of JCR <code>Values</code>.
     */
    private static Value[] toValue(PropertyString propertyData, ValueFactory valueFactory) {
        List<String> values = propertyData.getValues();
        if (values == null) {
            return new Value[0];
        }

        Value[] result = new Value[values.size()];
        int k = 0;
        for (String v : values) {
            result[k++] = valueFactory.createValue(v);
        }

        return result;
    }

    /**
     * Convert a <code>PropertyUri</code> to an array of JCR <code>Values</code>.
     */
    private static Value[] toValue(PropertyUri propertyData, ValueFactory valueFactory) throws ValueFormatException {
        List<String> values = propertyData.getValues();
        if (values == null) {
            return new Value[0];
        }

        Value[] result = new Value[values.size()];
        int k = 0;
        for (String v : values) {
            result[k++] = valueFactory.createValue(v, PropertyType.URI);
        }

        return result;
    }

}
