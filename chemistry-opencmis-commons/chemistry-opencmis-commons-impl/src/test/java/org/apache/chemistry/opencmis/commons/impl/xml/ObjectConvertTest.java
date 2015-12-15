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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.PropertyBoolean;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyDateTime;
import org.apache.chemistry.opencmis.commons.data.PropertyDecimal;
import org.apache.chemistry.opencmis.commons.data.PropertyHtml;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyInteger;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.data.PropertyUri;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.definitions.MutablePropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.DateTimeFormat;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.TypeCache;
import org.apache.chemistry.opencmis.commons.impl.WSConverter;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChangeEventInfoDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PolicyIdListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RenditionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectInFolderContainerType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectInFolderListType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectListType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;
import org.junit.Test;

public class ObjectConvertTest extends AbstractXMLConverterTest {

    @Test
    public void testObjectData() throws Exception {
        // run the test a few times with different values
        for (int i = 0; i < 10; i++) {
            ObjectDataImpl data10 = createObjectData(true, CmisVersion.CMIS_1_0, true, true);
            assertObjectData10(data10, true);

            ObjectDataImpl data11 = createObjectData(true, CmisVersion.CMIS_1_1, true, true);
            assertObjectData11(data11, true);

            ObjectDataImpl data11j = createObjectData(true, CmisVersion.CMIS_1_1, true, true);
            assertJsonObjectData11(data11j);
            assertJsonObjectData11Succinct(data11j);
        }
    }

    @Test
    public void testChildren() throws Exception {
        // run the test a few times with different values
        for (int i = 0; i < 5; i++) {
            List<ObjectData> list1 = new ArrayList<ObjectData>();
            List<ObjectInFolderData> list2 = new ArrayList<ObjectInFolderData>();

            for (int j = 0; j < randomInt(10) + 10; j++) {
                ObjectData data = createObjectData(true, CmisVersion.CMIS_1_1, false, false);

                list1.add(data);

                ObjectInFolderDataImpl dataInFolder = new ObjectInFolderDataImpl(data);
                dataInFolder.setPathSegment(randomString());
                list2.add(dataInFolder);
            }

            ObjectListImpl children1 = new ObjectListImpl();
            children1.setObjects(list1);

            ObjectInFolderListImpl children2 = new ObjectInFolderListImpl();
            children2.setObjects(list2);

            if (randomBoolean()) {
                children1.setNumItems(BigInteger.valueOf(list1.size()));
                children1.setHasMoreItems(false);
                children2.setNumItems(BigInteger.valueOf(list2.size()));
                children2.setHasMoreItems(false);
            } else {
                children1.setNumItems(BigInteger.valueOf(list1.size() + 1 + randomInt(99)));
                children1.setHasMoreItems(true);
                children2.setNumItems(BigInteger.valueOf(list2.size() + 1 + randomInt(99)));
                children2.setHasMoreItems(true);
            }

            assertObjectList(children1, children2);
        }
    }

    @Test
    public void testDescendants() throws Exception {
        // run the test a few times with different values
        for (int i = 0; i < 5; i++) {
            ObjectInFolderContainer container = createObjectInFolderContainer(randomInt(7) + 2);
            assertObjectContainer(container);
        }
    }

    private ObjectInFolderContainer createObjectInFolderContainer(int level) {
        ObjectInFolderContainerImpl result = new ObjectInFolderContainerImpl();

        ObjectInFolderDataImpl dataInFolder = new ObjectInFolderDataImpl(createObjectData(true, CmisVersion.CMIS_1_1,
                false, false));
        dataInFolder.setPathSegment(randomString());
        result.setObject(dataInFolder);

        if (level > 0) {
            List<ObjectInFolderContainer> children = new ArrayList<ObjectInFolderContainer>();
            for (int i = 0; i < randomInt(3); i++) {
                children.add(createObjectInFolderContainer(level - 1));
            }

            result.setChildren(children);
        }

        return result;
    }

    protected ObjectDataImpl createObjectData(boolean addRelationships, CmisVersion cmisVersion, boolean withChanges,
            boolean withExtensions) {
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
        if (withExtensions) {
            properties.setExtensions(createExtensions(3));
        }
        result.setProperties(properties);

        // allowable actions
        AllowableActionsImpl allowableActions = new AllowableActionsImpl();
        Set<Action> actions = new HashSet<Action>();
        for (Action action : Action.values()) {
            if (action == Action.CAN_CREATE_ITEM && cmisVersion == CmisVersion.CMIS_1_0) {
                continue;
            }
            actions.add(action);
        }
        allowableActions.setAllowableActions(actions);
        result.setAllowableActions(allowableActions);

        // relationships
        if (addRelationships) {
            List<ObjectData> relationships = new ArrayList<ObjectData>();
            for (int i = 0; i < randomInt(4) + 1; i++) {
                relationships.add(createObjectData(false, cmisVersion, withChanges, withExtensions));
            }
            result.setRelationships(relationships);
        }

        // change event info
        if (withChanges) {
            ChangeEventInfoDataImpl changeEventInfo = new ChangeEventInfoDataImpl();
            changeEventInfo.setChangeTime(randomDateTime());
            changeEventInfo.setChangeType(randomEnum(ChangeType.class));
            result.setChangeEventInfo(changeEventInfo);
        }

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
        if (withExtensions) {
            result.setExtensions(createExtensions(5));
        }

        return result;
    }

    // --- asserts ---

    protected void assertObjectData10(ObjectData data, boolean validate) throws Exception {
        assertXmlObjectData10(data, validate);
        assertWsObjectData10(data);
    }

    protected void assertXmlObjectData10(ObjectData data, boolean validate) throws Exception {
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

    protected void assertWsObjectData10(ObjectData data) throws Exception {
        CmisObjectType ws = WSConverter.convert(data, CmisVersion.CMIS_1_0);

        ObjectData result = WSConverter.convert(ws);

        assertNotNull(result);
        assertDataObjectsEquals("ObjectData", data, result, null);
        assertNotNull(result.getExtensions());
    }

    protected void assertObjectData11(ObjectData data, boolean validate) throws Exception {
        assertXmlObjectData11(data, validate);
        assertWsObjectData11(data);
    }

    protected void assertXmlObjectData11(ObjectData data, boolean validate) throws Exception {
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

    protected void assertWsObjectData11(ObjectData data) throws Exception {
        CmisObjectType ws = WSConverter.convert(data, CmisVersion.CMIS_1_1);

        ObjectData result = WSConverter.convert(ws);

        assertNotNull(result);
        assertDataObjectsEquals("ObjectData", data, result, null);
        assertNotNull(result.getExtensions());
    }

    protected void assertJsonObjectData11(ObjectData data) throws Exception {
        TypeCache typeCache = null;

        StringWriter sw = new StringWriter();

        JSONObject jsonObject = JSONConverter.convert(data, typeCache, JSONConverter.PropertyMode.CHANGE, false,
                DateTimeFormat.SIMPLE);
        jsonObject.writeJSONString(sw);

        // test toJSONString()
        assertEquals(sw.toString(), jsonObject.toJSONString());

        Object json = (new JSONParser()).parse(sw.toString());
        assertTrue(json instanceof Map<?, ?>);
        @SuppressWarnings("unchecked")
        ObjectData result = JSONConverter.convertObject((Map<String, Object>) json, typeCache);

        assertNotNull(result);
        assertDataObjectsEquals("ObjectData", data, result, Collections.singleton("getExtensions"));
        assertExtensionsEquals(data.getExtensions(), result.getExtensions());
    }

    protected void assertJsonObjectData11Succinct(ObjectData data) throws Exception {
        TypeCache typeCache = new TestTypeCache(data);

        StringWriter sw = new StringWriter();

        JSONObject jsonObject = JSONConverter.convert(data, typeCache, JSONConverter.PropertyMode.CHANGE, true,
                DateTimeFormat.SIMPLE);
        jsonObject.writeJSONString(sw);

        // test toJSONString()
        assertEquals(sw.toString(), jsonObject.toJSONString());

        Object json = (new JSONParser()).parse(sw.toString());
        assertTrue(json instanceof Map<?, ?>);
        @SuppressWarnings("unchecked")
        ObjectData result = JSONConverter.convertObject((Map<String, Object>) json, typeCache);

        assertNotNull(result);

        Set<String> ignoreMethods = new HashSet<String>();
        ignoreMethods.add("getProperties");
        ignoreMethods.add("getExtensions");

        assertDataObjectsEquals("ObjectData", data, result, ignoreMethods);
        assertExtensionsEquals(data.getExtensions(), result.getExtensions());

        assertEquals(data.getProperties().getPropertyList().size(), result.getProperties().getPropertyList().size());

        for (PropertyData<?> prop : data.getProperties().getPropertyList()) {
            assertTrue(result.getProperties().getProperties().containsKey(prop.getId()));

            Object value = data.getProperties().getProperties().get(prop.getId()).getFirstValue();
            if (value instanceof String) {
                assertEquals(value, result.getProperties().getProperties().get(prop.getId()).getFirstValue());
            }
        }
    }

    protected void assertObjectList(ObjectList children1, ObjectInFolderList children2) throws Exception {
        assertWsObjectList(children1);
        assertJsonObjectList(children1);
        assertWsObjectInFolderList(children2);
        assertJsonObjectInFolderList(children2);
    }

    protected void assertWsObjectList(ObjectList children) throws Exception {
        CmisObjectListType ws = WSConverter.convert(children, CmisVersion.CMIS_1_1);

        ObjectList result = WSConverter.convert(ws);

        assertNotNull(result);
        assertDataObjectsEquals("ObjectList", children, result, null);
    }

    protected void assertJsonObjectList(ObjectList children) throws Exception {
        TypeCache typeCache = null;

        StringWriter sw = new StringWriter();

        JSONConverter.convert(children, typeCache, JSONConverter.PropertyMode.CHANGE, false, DateTimeFormat.SIMPLE)
                .writeJSONString(sw);

        Object json = (new JSONParser()).parse(sw.toString());
        assertTrue(json instanceof Map<?, ?>);
        @SuppressWarnings("unchecked")
        ObjectList result = JSONConverter.convertObjectList((Map<String, Object>) json, typeCache, false);

        assertNotNull(result);
        assertDataObjectsEquals("ObjectList", children, result, null);
    }

    protected void assertWsObjectInFolderList(ObjectInFolderList children) throws Exception {
        CmisObjectInFolderListType ws = WSConverter.convert(children, CmisVersion.CMIS_1_1);

        ObjectInFolderList result = WSConverter.convert(ws);

        assertNotNull(result);
        assertDataObjectsEquals("ObjectInFolderList", children, result, null);
    }

    protected void assertJsonObjectInFolderList(ObjectInFolderList children) throws Exception {
        TypeCache typeCache = null;

        StringWriter sw = new StringWriter();

        JSONConverter.convert(children, typeCache, false, DateTimeFormat.SIMPLE).writeJSONString(sw);

        Object json = (new JSONParser()).parse(sw.toString());
        assertTrue(json instanceof Map<?, ?>);
        @SuppressWarnings("unchecked")
        ObjectInFolderList result = JSONConverter.convertObjectInFolderList((Map<String, Object>) json, typeCache);

        assertNotNull(result);
        assertDataObjectsEquals("ObjectInFolderList", children, result, null);
    }

    protected void assertObjectContainer(ObjectInFolderContainer container) throws Exception {
        assertWsObjectContainer(container);
        assertJsonObjectContainer(container);
    }

    protected void assertWsObjectContainer(ObjectInFolderContainer container) {
        CmisObjectInFolderContainerType ws = WSConverter.convert(container, CmisVersion.CMIS_1_1);

        ObjectInFolderContainer result = WSConverter.convert(ws);

        assertNotNull(result);
        assertDataObjectsEquals("ObjectContainer", container, result, null);
    }

    protected void assertJsonObjectContainer(ObjectInFolderContainer container) throws Exception {
        TypeCache typeCache = null;

        StringWriter sw = new StringWriter();

        JSONConverter.convert(container, typeCache, false, DateTimeFormat.EXTENDED).writeJSONString(sw);

        Object json = (new JSONParser()).parse(sw.toString());
        assertTrue(json instanceof Map<?, ?>);
        @SuppressWarnings("unchecked")
        ObjectInFolderContainer result = JSONConverter.convertDescendant((Map<String, Object>) json, typeCache);

        assertNotNull(result);
        assertDataObjectsEquals("ObjectContainer", container, result, null);
    }

    private static class TestTypeCache implements TypeCache {

        private DocumentTypeDefinitionImpl objectType;

        public TestTypeCache(ObjectData data) {
            objectType = new DocumentTypeDefinitionImpl();

            for (PropertyData<?> prop : data.getProperties().getPropertyList()) {
                MutablePropertyDefinition<?> propDef;

                if (prop instanceof PropertyString) {
                    propDef = new PropertyStringDefinitionImpl();
                } else if (prop instanceof PropertyId) {
                    propDef = new PropertyIdDefinitionImpl();
                } else if (prop instanceof PropertyBoolean) {
                    propDef = new PropertyBooleanDefinitionImpl();
                } else if (prop instanceof PropertyInteger) {
                    propDef = new PropertyIntegerDefinitionImpl();
                } else if (prop instanceof PropertyDateTime) {
                    propDef = new PropertyDateTimeDefinitionImpl();
                } else if (prop instanceof PropertyDecimal) {
                    propDef = new PropertyDecimalDefinitionImpl();
                } else if (prop instanceof PropertyHtml) {
                    propDef = new PropertyHtmlDefinitionImpl();
                } else if (prop instanceof PropertyUri) {
                    propDef = new PropertyUriDefinitionImpl();
                } else {
                    throw new IllegalArgumentException("Invalid data type!");
                }

                propDef.setId(prop.getId());
                propDef.setDisplayName(prop.getDisplayName());
                propDef.setLocalName(prop.getLocalName());
                propDef.setQueryName(prop.getQueryName());

                objectType.addPropertyDefinition(propDef);
            }
        }

        @Override
        public TypeDefinition getTypeDefinition(String typeId) {
            if ("cmis:folder".equals(typeId)) {
                return new FolderTypeDefinitionImpl();
            } else if ("cmis:document".equals(typeId)) {
                return new DocumentTypeDefinitionImpl();
            } else {
                return objectType;
            }
        }

        @Override
        public TypeDefinition reloadTypeDefinition(String typeId) {
            return null;
        }

        @Override
        public TypeDefinition getTypeDefinitionForObject(String objectId) {
            return null;
        }

        @Override
        public PropertyDefinition<?> getPropertyDefinition(String propId) {
            return null;
        }
    }
}
