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
package org.apache.chemistry.opencmis.commons.impl.xml;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChangeEventInfoDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CmisExtensionElementImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PolicyIdListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RenditionDataImpl;
import org.junit.Test;

public class ObjectConvertTest extends AbstractXMLConverterTest {

    @Test
    public void testObjectData() throws Exception {
        // run the test a few times with different values
        for (int i = 0; i < 10; i++) {
            ObjectDataImpl data = createObjectData(true);

            assertObjectData10(data, true);
            assertObjectData11(data, true);
        }
    }

    protected ObjectDataImpl createObjectData(boolean addRelationships) {
        ObjectDataImpl result = new ObjectDataImpl();

        // properties
        PropertiesImpl properties = new PropertiesImpl();
        properties.addProperty(createIdPropertyData(PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value()));
        properties.addProperty(createIdPropertyData(PropertyIds.OBJECT_ID, randomString()));
        properties.addProperty(createIdPropertyData(PropertyIds.OBJECT_TYPE_ID, randomString()));
        properties.addProperty(createPropertyData(PropertyType.BOOLEAN, 0));
        properties.addProperty(createPropertyData(PropertyType.BOOLEAN, 1));
        properties.addProperty(createPropertyData(PropertyType.BOOLEAN, randomInt(8) + 2));
        properties.addProperty(createPropertyData(PropertyType.DATETIME, 0));
        properties.addProperty(createPropertyData(PropertyType.DATETIME, 1));
        properties.addProperty(createPropertyData(PropertyType.DATETIME, randomInt(8) + 2));
        properties.addProperty(createPropertyData(PropertyType.DECIMAL, 0));
        properties.addProperty(createPropertyData(PropertyType.DECIMAL, 1));
        properties.addProperty(createPropertyData(PropertyType.DECIMAL, randomInt(8) + 2));
        properties.addProperty(createPropertyData(PropertyType.HTML, 0));
        properties.addProperty(createPropertyData(PropertyType.HTML, 1));
        properties.addProperty(createPropertyData(PropertyType.HTML, randomInt(8) + 2));
        properties.addProperty(createPropertyData(PropertyType.ID, 0));
        properties.addProperty(createPropertyData(PropertyType.ID, 1));
        properties.addProperty(createPropertyData(PropertyType.ID, randomInt(8) + 2));
        properties.addProperty(createPropertyData(PropertyType.INTEGER, 0));
        properties.addProperty(createPropertyData(PropertyType.INTEGER, 1));
        properties.addProperty(createPropertyData(PropertyType.INTEGER, randomInt(8) + 2));
        properties.addProperty(createPropertyData(PropertyType.STRING, 0));
        properties.addProperty(createPropertyData(PropertyType.STRING, 1));
        properties.addProperty(createPropertyData(PropertyType.STRING, randomInt(8) + 2));
        properties.addProperty(createPropertyData(PropertyType.URI, 0));
        properties.addProperty(createPropertyData(PropertyType.URI, 1));
        properties.addProperty(createPropertyData(PropertyType.URI, randomInt(8) + 2));
        properties.setExtensions(createExtensions(3));
        result.setProperties(properties);

        // allowable actions
        AllowableActionsImpl allowableActions = new AllowableActionsImpl();
        HashSet<Action> actions = new HashSet<Action>();
        for (Action action : Action.values()) {
            actions.add(action);
        }
        allowableActions.setAllowableActions(actions);
        result.setAllowableActions(allowableActions);

        // relationships
        if (addRelationships) {
            List<ObjectData> relationships = new ArrayList<ObjectData>();
            for (int i = 0; i < randomInt(4) + 1; i++) {
                relationships.add(createObjectData(false));
            }
            result.setRelationships(relationships);
        }

        // change event info
        ChangeEventInfoDataImpl changeEventInfo = new ChangeEventInfoDataImpl();
        changeEventInfo.setChangeTime(randomDateTime());
        changeEventInfo.setChangeType(randomEnum(ChangeType.class));
        result.setChangeEventInfo(changeEventInfo);

        // ACL
        AccessControlListImpl acl = new AccessControlListImpl();
        List<Ace> aces = new ArrayList<Ace>();
        for (int i = 0; i < randomInt(9) + 1; i++) {
            AccessControlEntryImpl ace = new AccessControlEntryImpl();
            List<String> permissions = new ArrayList<String>();
            for (int j = 0; j < randomInt(4) + 1; j++) {
                permissions.add(randomString());
            }
            ace.setPermissions(permissions);
            ace.setDirect(randomBoolean());
            ace.setPrincipal(new AccessControlPrincipalDataImpl(randomString()));
            aces.add(ace);
        }
        acl.setAces(aces);
        result.setAcl(acl);

        result.setIsExactAcl(randomBoolean());

        // policy ids
        List<String> pIds = new ArrayList<String>();
        for (int i = 0; i < randomInt(9) + 1; i++) {
            pIds.add(randomString());
        }
        PolicyIdListImpl policyList = new PolicyIdListImpl();
        policyList.setPolicyIds(pIds);
        result.setPolicyIds(policyList);

        // renditions
        List<RenditionData> renditions = new ArrayList<RenditionData>();
        for (int i = 0; i < randomInt(4) + 1; i++) {
            RenditionDataImpl rendition = new RenditionDataImpl();
            rendition.setKind(randomString());
            rendition.setMimeType(randomString());
            rendition.setRenditionDocumentId(randomString());
            rendition.setStreamId(randomString());
            rendition.setTitle(randomString());
            rendition.setBigLength(randomInteger());
            rendition.setBigHeight(randomInteger());
            rendition.setBigWidth(randomInteger());

            renditions.add(rendition);
        }
        result.setRenditions(renditions);

        // extensions
        result.setExtensions(createExtensions(5));

        return result;
    }

    protected PropertyData<?> createPropertyData(PropertyType propertyType, int numValues) {
        AbstractPropertyData<?> result;

        switch (propertyType) {
        case BOOLEAN:
            result = new PropertyBooleanImpl();
            if (numValues > 0) {
                List<Boolean> values = new ArrayList<Boolean>();
                for (int i = 0; i < numValues; i++) {
                    values.add(randomBoolean());
                }
                ((PropertyBooleanImpl) result).setValues(values);
            }
            break;
        case DATETIME:
            result = new PropertyDateTimeImpl();
            if (numValues > 0) {
                List<GregorianCalendar> values = new ArrayList<GregorianCalendar>();
                for (int i = 0; i < numValues; i++) {
                    values.add(randomDateTime());
                }
                ((PropertyDateTimeImpl) result).setValues(values);
            }
            break;
        case DECIMAL:
            result = new PropertyDecimalImpl();
            if (numValues > 0) {
                List<BigDecimal> values = new ArrayList<BigDecimal>();
                for (int i = 0; i < numValues; i++) {
                    values.add(randomDecimal());
                }
                ((PropertyDecimalImpl) result).setValues(values);
            }
            break;
        case HTML:
            result = new PropertyHtmlImpl();
            if (numValues > 0) {
                List<String> values = new ArrayList<String>();
                for (int i = 0; i < numValues; i++) {
                    values.add(randomString());
                }
                ((PropertyHtmlImpl) result).setValues(values);
            }
            break;
        case ID:
            result = new PropertyIdImpl();
            if (numValues > 0) {
                List<String> values = new ArrayList<String>();
                for (int i = 0; i < numValues; i++) {
                    values.add(randomString());
                }
                ((PropertyIdImpl) result).setValues(values);
            }
            break;
        case INTEGER:
            result = new PropertyIntegerImpl();
            if (numValues > 0) {
                List<BigInteger> values = new ArrayList<BigInteger>();
                for (int i = 0; i < numValues; i++) {
                    values.add(randomInteger());
                }
                ((PropertyIntegerImpl) result).setValues(values);
            }
            break;
        case STRING:
            result = new PropertyStringImpl();
            if (numValues > 0) {
                List<String> values = new ArrayList<String>();
                for (int i = 0; i < numValues; i++) {
                    values.add(randomString());
                }
                ((PropertyStringImpl) result).setValues(values);
            }
            break;
        case URI:
            result = new PropertyUriImpl();
            if (numValues > 0) {
                List<String> values = new ArrayList<String>();
                for (int i = 0; i < numValues; i++) {
                    values.add(randomUri());
                }
                ((PropertyUriImpl) result).setValues(values);
            }
            break;
        default:
            return null;
        }

        result.setId(randomString());
        result.setDisplayName(randomString());
        result.setLocalName(randomString());
        result.setQueryName(randomString());

        return result;
    }

    protected PropertyId createIdPropertyData(String id, String value) {
        PropertyIdImpl result = new PropertyIdImpl();

        result.setId(id);
        result.setDisplayName(id);
        result.setLocalName(id);
        result.setQueryName(id);
        result.setValue(value);

        return result;
    }

    protected List<CmisExtensionElement> createExtensions(int depth) {
        List<CmisExtensionElement> result = new ArrayList<CmisExtensionElement>();

        String[] namespaces = new String[] { "http://ext1.com", "http://ext2.org", "http://ext3.net" };

        for (int i = 0; i < randomInt(4) + 1; i++) {
            String ns = namespaces[randomInt(namespaces.length)];

            CmisExtensionElementImpl element;
            if (randomBoolean() || depth < 1) {
                element = new CmisExtensionElementImpl(ns, randomTag(), null, randomString());
            } else {
                element = new CmisExtensionElementImpl(ns, randomTag(), null, createExtensions(depth - 1));
            }

            result.add(element);
        }

        return result;
    }

    protected void assertObjectData10(ObjectData data, boolean validate) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        XMLStreamWriter writer = createWriter(out);
        XMLConverter.writeObject(writer, CmisVersion.CMIS_1_0, TEST_NAMESPACE, data);
        closeWriter(writer);

        byte[] xml = out.toByteArray();

        if (validate) {
            validate(xml, CmisVersion.CMIS_1_0);
        }

        XMLStreamReader parser = createParser(xml);
        ObjectData result = XMLConverter.convertObject(parser);
        closeParser(parser);

        assertNotNull(result);
        assertDataObjectsEquals("ObjectData", data, result, null);
        assertNotNull(result.getExtensions());
    }

    protected void assertObjectData11(ObjectData data, boolean validate) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        XMLStreamWriter writer = createWriter(out);
        XMLConverter.writeObject(writer, CmisVersion.CMIS_1_1, TEST_NAMESPACE, data);
        closeWriter(writer);

        byte[] xml = out.toByteArray();

        if (validate) {
            validate(xml, CmisVersion.CMIS_1_1);
        }

        XMLStreamReader parser = createParser(xml);
        ObjectData result = XMLConverter.convertObject(parser);
        closeParser(parser);

        assertNotNull(result);
        assertDataObjectsEquals("ObjectData", data, result, null);
        assertNotNull(result.getExtensions());
    }
}
