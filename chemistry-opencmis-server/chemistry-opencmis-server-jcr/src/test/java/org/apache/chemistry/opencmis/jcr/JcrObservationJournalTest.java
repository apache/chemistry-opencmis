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
 *
 */
package org.apache.chemistry.opencmis.jcr;

import java.math.BigInteger;

import junit.framework.Assert;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ChangeEventInfo;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class JcrObservationJournalTest extends AbstractJcrSessionTest {
    private static final BigInteger PAGE_SIZE = new BigInteger("4");

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        Assert.assertEquals(CapabilityChanges.OBJECTIDSONLY, getJcrRepository()
                .getRepositoryInfo(getSession()).getCapabilities()
                .getChangesCapability());

        fillObservationJournal();
    }

    @Test
    public void testObservationJournal() throws Exception {
        // FIXME not such method: JcrRepository.getContentChanges()
//        ObjectList objList = getJcrRepository().getContentChanges(getSession(),
//                null, false, null, false, false, PAGE_SIZE, null);
        ObjectList objList = null;

        //The repository supports journaled observation
        Assert.assertNotNull(objList);

        //event list isn't empty
        Assert.assertEquals(true, !objList.getObjects().isEmpty());

        ChangeEventInfo latestChangeEvent = objList.getObjects()
                .get(objList.getNumItems().intValue() - 1).getChangeEventInfo();
        long previousChangeLogToken = latestChangeEvent.getChangeTime().getTimeInMillis();
        // FIXME not such method: JcrRepository.getContentChanges()
//        objList = getJcrRepository().getContentChanges(getSession(),
//                String.valueOf(previousChangeLogToken), false, null, false, false, PAGE_SIZE, null);

        latestChangeEvent = objList.getObjects()
                .get(objList.getNumItems().intValue() - 1).getChangeEventInfo();

        long latestChangeLogToken = latestChangeEvent.getChangeTime().getTimeInMillis();

        //Ensure the latest change log token is the really latest one.
        Assert.assertEquals(true, latestChangeLogToken > previousChangeLogToken);

        //In journal must remain some events.
        Assert.assertEquals(Boolean.TRUE, objList.hasMoreItems());
    }

    private void fillObservationJournal() {
        PropertiesImpl properties = new PropertiesImpl();
        properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, "OBSERVATION_TEST_FOLDER"));
        properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "cmis:folder"));

        String testFolderId = getJcrRepository().createFolder(getSession(), properties, getRootFolder().getId());
        properties = new PropertiesImpl();
        properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, "NEW_NAME_TEST_FOLDER"));
        getJcrRepository().getJcrNode(getSession(), testFolderId)
            .updateProperties(properties);
        getJcrRepository().deleteObject(getSession(), testFolderId, true);
    }

}
