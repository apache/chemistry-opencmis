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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChangeEventInfoDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PolicyIdListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RenditionDataImpl;
import org.junit.Test;

public class ObjectConvertTest extends AbstractXMLConverterTest {

    @Test
    public void testObjectData() throws Exception {
        // run the test a few times with different values
        for (int i = 0; i < 10; i++) {
            ObjectDataImpl data10 = createObjectData(true, CmisVersion.CMIS_1_0);
            assertObjectData10(data10, true);

            ObjectDataImpl data11 = createObjectData(true, CmisVersion.CMIS_1_1);
            assertObjectData11(data11, true);
        }
    }

    protected ObjectDataImpl createObjectData(boolean addRelationships, CmisVersion cmisVersion) {
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
                relationships.add(createObjectData(false, cmisVersion));
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
