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

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.XMLUtils;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.QueryTypeImpl;
import org.junit.Test;

public class QueryConvertTest extends AbstractXMLConverterTest {

    @Test
    public void testQueryType() throws Exception {

        // run the test a few times with different values
        for (int i = 0; i < 10; i++) {
            QueryTypeImpl query = new QueryTypeImpl();

            query.setStatement(randomString());
            query.setSearchAllVersions(randomBoolean());
            query.setIncludeAllowableActions(randomBoolean());
            query.setIncludeRelationships(randomEnum(IncludeRelationships.class));
            query.setRenditionFilter(randomString());
            query.setMaxItems(randomInteger());
            query.setSkipCount(randomInteger());

            assertQueryType10(query, true);
            assertQueryType11(query, true);
        }

    }

    protected void assertQueryType10(QueryTypeImpl data, boolean validate) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        XMLStreamWriter writer = XMLUtils.createWriter(out);
        XMLUtils.startXmlDocument(writer);
        XMLConverter.writeQuery(writer, CmisVersion.CMIS_1_0, data);
        XMLUtils.endXmlDocument(writer);

        byte[] xml = out.toByteArray();

        if (validate) {
            validate(xml, CmisVersion.CMIS_1_0);
        }

        XMLStreamReader parser = XMLUtils.createParser(new ByteArrayInputStream(xml));
        XMLUtils.findNextStartElemenet(parser);
        QueryTypeImpl result = XMLConverter.convertQuery(parser);
        parser.close();

        assertNotNull(result);
        assertDataObjectsEquals("QueryType", data, result, null);
        assertNull(result.getExtensions());
    }

    protected void assertQueryType11(QueryTypeImpl data, boolean validate) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        XMLStreamWriter writer = XMLUtils.createWriter(out);
        XMLUtils.startXmlDocument(writer);
        XMLConverter.writeQuery(writer, CmisVersion.CMIS_1_1, data);
        XMLUtils.endXmlDocument(writer);

        byte[] xml = out.toByteArray();

        if (validate) {
            validate(xml, CmisVersion.CMIS_1_1);
        }

        XMLStreamReader parser = XMLUtils.createParser(new ByteArrayInputStream(xml));
        XMLUtils.findNextStartElemenet(parser);
        QueryTypeImpl result = XMLConverter.convertQuery(parser);
        parser.close();

        assertNotNull(result);
        assertDataObjectsEquals("QueryType", data, result, null);
        assertNull(result.getExtensions());
    }
}
