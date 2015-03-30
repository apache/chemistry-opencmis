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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.XMLUtils;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.junit.Test;

public class AclConvertTest extends AbstractXMLConverterTest {

    @Test
    public void testAcl() throws Exception {

        // run the test a few times with different values
        for (int i = 0; i < 10; i++) {
            AccessControlListImpl acl = new AccessControlListImpl();
            List<Ace> aces = new ArrayList<Ace>();
            for (int j = 0; j < randomInt(9) + 1; j++) {
                AccessControlEntryImpl ace = new AccessControlEntryImpl();
                List<String> permissions = new ArrayList<String>();
                for (int k = 0; k < randomInt(4) + 1; k++) {
                    permissions.add(randomString());
                }
                ace.setPermissions(permissions);
                ace.setDirect(randomBoolean());
                ace.setPrincipal(new AccessControlPrincipalDataImpl(randomString()));
                aces.add(ace);
            }
            acl.setAces(aces);

            assertAcl10(acl, true);
            assertAcl11(acl, true);
        }
    }

    protected void assertAcl10(Acl data, boolean validate) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        XMLStreamWriter writer = XMLUtils.createWriter(out);
        XMLUtils.startXmlDocument(writer);
        XMLConverter.writeAcl(writer, CmisVersion.CMIS_1_0, true, data);
        XMLUtils.endXmlDocument(writer);

        byte[] xml = out.toByteArray();

        if (validate) {
            validate(xml, CmisVersion.CMIS_1_0);
        }

        XMLStreamReader parser = XMLUtils.createParser(new ByteArrayInputStream(xml));
        XMLUtils.findNextStartElemenet(parser);
        Acl result = XMLConverter.convertAcl(parser);
        parser.close();

        assertNotNull(result);
        assertDataObjectsEquals("ACL", data, result, null);
        assertNull(result.getExtensions());
    }

    protected void assertAcl11(Acl data, boolean validate) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        XMLStreamWriter writer = XMLUtils.createWriter(out);
        XMLUtils.startXmlDocument(writer);
        XMLConverter.writeAcl(writer, CmisVersion.CMIS_1_1, true, data);
        XMLUtils.endXmlDocument(writer);

        byte[] xml = out.toByteArray();

        if (validate) {
            validate(xml, CmisVersion.CMIS_1_1);
        }

        XMLStreamReader parser = XMLUtils.createParser(new ByteArrayInputStream(xml));
        XMLUtils.findNextStartElemenet(parser);
        Acl result = XMLConverter.convertAcl(parser);
        parser.close();

        assertNotNull(result);
        assertDataObjectsEquals("ACL", data, result, null);
        assertNull(result.getExtensions());
    }
}
