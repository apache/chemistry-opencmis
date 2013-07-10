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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BulkUpdateImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BulkUpdateObjectIdAndChangeTokenImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.junit.Test;

public class BukUpdateConverterTest extends AbstractXMLConverterTest {

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
}
