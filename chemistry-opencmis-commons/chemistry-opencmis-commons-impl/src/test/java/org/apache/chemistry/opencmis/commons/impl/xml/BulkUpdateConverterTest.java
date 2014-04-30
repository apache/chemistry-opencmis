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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.WSConverter;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BulkUpdateImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BulkUpdateObjectIdAndChangeTokenImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectIdAndChangeTokenType;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;
import org.junit.Test;

public class BulkUpdateConverterTest extends AbstractXMLConverterTest {

    @Test
    public void testBulkUpdate() throws Exception {
        // run the test a few times with different values
        for (int i = 0; i < 5; i++) {
            BulkUpdateImpl bulkUpdate = new BulkUpdateImpl();

            List<BulkUpdateObjectIdAndChangeToken> list = new ArrayList<BulkUpdateObjectIdAndChangeToken>();
            for (int j = 0; j < randomInt(19) + 1; j++) {
                BulkUpdateObjectIdAndChangeTokenImpl idAndToken = new BulkUpdateObjectIdAndChangeTokenImpl(
                        randomString(), randomString());
                list.add(idAndToken);
            }
            bulkUpdate.setObjectIdAndChangeToken(list);

            PropertiesImpl properties = new PropertiesImpl();
            for (int j = 0; j < randomInt(9) + 1; j++) {
                properties.addProperty(createPropertyData(
                        PropertyType.values()[randomInt(PropertyType.values().length)], randomInt(2)));
            }
            bulkUpdate.setProperties(properties);

            List<String> addSecTypes = new ArrayList<String>();
            for (int j = 0; j < randomInt(4) + 1; j++) {
                addSecTypes.add(randomString());
            }
            bulkUpdate.setAddSecondaryTypeIds(addSecTypes);

            List<String> removeSecTypes = new ArrayList<String>();
            for (int j = 0; j < randomInt(4) + 1; j++) {
                removeSecTypes.add(randomString());
            }
            bulkUpdate.setRemoveSecondaryTypeIds(removeSecTypes);

            assertBulkUpdate11(bulkUpdate, true);
        }
    }

    protected void assertBulkUpdate11(BulkUpdateImpl bulkUpdate, boolean validate) throws Exception {
        assertXmlBulkUpdate11(bulkUpdate, validate);
        assertWsBulkUpdate11(bulkUpdate);
        assertJsonBulkUpdate11(bulkUpdate);
    }

    protected void assertXmlBulkUpdate11(BulkUpdateImpl bulkUpdate, boolean validate) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        XMLStreamWriter writer = createWriter(out);
        XMLConverter.writeBulkUpdate(writer, TEST_NAMESPACE, bulkUpdate);
        closeWriter(writer);

        byte[] xml = out.toByteArray();

        if (validate) {
            validate(xml, CmisVersion.CMIS_1_1);
        }

        XMLStreamReader parser = createParser(xml);
        BulkUpdateImpl result = XMLConverter.convertBulkUpdate(parser);
        closeParser(parser);

        assertNotNull(result);
        assertDataObjectsEquals("BulkUpdate", bulkUpdate, result, null);
        assertNull(result.getExtensions());
    }

    protected void assertWsBulkUpdate11(BulkUpdateImpl bulkUpdate) throws Exception {
        CmisObjectIdAndChangeTokenType ws = WSConverter.convert(bulkUpdate.getObjectIdAndChangeToken().get(0));

        BulkUpdateObjectIdAndChangeToken result = WSConverter.convert(ws);

        assertNotNull(result);
        assertDataObjectsEquals("BulkUpdate", bulkUpdate.getObjectIdAndChangeToken().get(0), result, null);
        assertNull(result.getExtensions());
    }

    protected void assertJsonBulkUpdate11(BulkUpdateImpl bulkUpdate) throws Exception {
        StringWriter sw = new StringWriter();

        JSONConverter.convert(bulkUpdate.getObjectIdAndChangeToken().get(0)).writeJSONString(sw);

        Object json = (new JSONParser()).parse(sw.toString());
        assertTrue(json instanceof Map<?, ?>);
        @SuppressWarnings("unchecked")
        BulkUpdateObjectIdAndChangeToken result = JSONConverter.convertBulkUpdate((Map<String, Object>) json);

        assertNotNull(result);
        assertDataObjectsEquals("BulkUpdate", bulkUpdate.getObjectIdAndChangeToken().get(0), result, null);
        assertNull(result.getExtensions());
    }
}
