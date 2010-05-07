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
package org.apache.chemistry.opencmis.fit.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.junit.Ignore;
import org.junit.Test;

// relations not yet supported
@Ignore
public abstract class AbstractWriteObjectRelationIT extends AbstractSessionTest {

    @Test
    public void createAndLoopRelations() {
        String path1 = "/" + Fixture.TEST_ROOT_FOLDER_NAME + "/" + FixtureData.DOCUMENT1_NAME;
        Document document1 = (Document) this.session.getObjectByPath(path1);
        assertNotNull("Document not found: " + path1, document1);

        String path2 = "/" + Fixture.TEST_ROOT_FOLDER_NAME + "/" + FixtureData.DOCUMENT2_NAME;
        Document document2 = (Document) this.session.getObjectByPath(path2);
        assertNotNull("Document not found: " + path2, document2);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, ObjectType.RELATIONSHIP_BASETYPE_ID);
        properties.put(PropertyIds.SOURCE_ID, document1.getId());
        properties.put(PropertyIds.TARGET_ID, document2.getId());

        ObjectId id = this.session.createRelationship(properties, null, null, null);

        ObjectType ot = document1.getType();
        ItemIterable<Relationship> relations = document1.getRelationships(true, RelationshipDirection.EITHER, ot,
                this.session.getDefaultContext());
        for (Relationship r : relations) {
            assertNotNull(r);
            assertEquals(id, r.getId());
            break;
        }
    }

}
