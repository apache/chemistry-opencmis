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

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.junit.Test;

public abstract class AbstractWriteAclT extends AbstractSessionTest {

    /**
     * Bug Scenario:
     * 
     * - create file - apply ACL - read file by 2nd session - read properties <-
     * side effect with caching? - read ACL -> didn't work
     */
    @Test
    public void createFileApplyAclAndGetAclFromNewSession() {
        this.session.getDefaultContext().setIncludeAcls(true);
        this.session2.getDefaultContext().setIncludeAcls(true);      
        
        ObjectId parentId = this.session.createObjectId(this.fixture.getTestRootId());
        String folderName = UUID.randomUUID().toString();
        String typeId = FixtureData.DOCUMENT_TYPE_ID.value();

        // properties
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, folderName);
        properties.put(PropertyIds.OBJECT_TYPE_ID, typeId);

        // permissions
        List<Ace> aces = new ArrayList<Ace>();
        ArrayList<String> permissions = new ArrayList<String>();
        permissions.add("cmis:read");
        aces.add(this.session.getObjectFactory().createAce("everyone", permissions));

        // create document
        ObjectId id = this.session.createDocument(properties, parentId, null, VersioningState.NONE);
        assertNotNull(id);

        // get document for id
        Document doc = (Document) this.session.getObject(id);
        assertNotNull(doc);

        // apply acl (not supported by InMemory?)
        Acl acl = doc.applyAcl(aces, null, AclPropagation.PROPAGATE);
        assertNotNull(acl);

        // read & check acls
         Acl acl2 = doc.getAcl();
         assertNotNull(acl2);

        /*
         * Session 2
         */

        // get document for id
        Document doc2 = (Document) this.session2.getObject(id);
        assertNotNull(doc2);

        // read properties (required to reproduce the bug)
        List<Property<?>> pl2 = doc2.getProperties();
        assertNotNull(pl2);

        // read & check acls
        Acl acl3 = doc2.getAcl();
        assertNotNull(acl3);

    }

}
