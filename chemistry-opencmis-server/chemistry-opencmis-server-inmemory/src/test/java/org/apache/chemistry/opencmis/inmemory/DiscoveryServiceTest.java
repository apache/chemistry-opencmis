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
package org.apache.chemistry.opencmis.inmemory;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.inmemory.ObjectServiceTest.ObjectTestTypeSystemCreator;
import org.apache.chemistry.opencmis.inmemory.content.ObjectGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryServiceTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(DiscoveryServiceTest.class);
    private static final String TEST_FOLDER_TYPE_ID = ObjectServiceTest.TEST_FOLDER_TYPE_ID;
    private static final String TEST_DOCUMENT_TYPE_ID = ObjectServiceTest.TEST_DOCUMENT_TYPE_ID;
    private static final String TEST_FOLDER_STRING_PROP_ID = ObjectServiceTest.TEST_FOLDER_STRING_PROP_ID;
    private static final String TEST_DOCUMENT_STRING_PROP_ID = ObjectServiceTest.TEST_DOCUMENT_STRING_PROP_ID;

    @Override
    @Before
    public void setUp() {
        super.setTypeCreatorClass(ObjectTestTypeSystemCreator.class.getName());
        super.setUp();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testQuery() {
        log.info("starting testQuery() ...");

        ObjectGenerator gen = new ObjectGenerator(fFactory, fNavSvc, fObjSvc, fRepSvc, fRepositoryId,
                ObjectGenerator.ContentKind.LOREM_IPSUM_TEXT);
        gen.setNumberOfDocumentsToCreatePerFolder(3);
        gen.setDocumentTypeId(TEST_DOCUMENT_TYPE_ID);
        gen.setFolderTypeId(TEST_FOLDER_TYPE_ID);

        List<String> propsToSet = new ArrayList<String>();
        propsToSet.add(TEST_DOCUMENT_STRING_PROP_ID);
        gen.setDocumentPropertiesToGenerate(propsToSet);

        propsToSet = new ArrayList<String>();
        propsToSet.add(TEST_FOLDER_STRING_PROP_ID);
        gen.setFolderPropertiesToGenerate(propsToSet);

        gen.createFolderHierachy(2, 2, fRootFolderId);

        Boolean searchAllVersions = Boolean.FALSE;
        Boolean includeAllowableActions = Boolean.FALSE;
        IncludeRelationships includeRelationships = IncludeRelationships.NONE;
        String renditionFilter = null;
        BigInteger maxItems = null;
        BigInteger skipCount = null;

        String statement = "SELECT * FROM " + TEST_DOCUMENT_TYPE_ID + " WHERE " + TEST_DOCUMENT_STRING_PROP_ID
                + "='My Doc StringProperty 1'";
        ObjectList res = fDiscSvc.query(fRepositoryId, statement, searchAllVersions, includeAllowableActions,
                includeRelationships, renditionFilter, maxItems, skipCount, null);
        assertEquals(1, res.getObjects().size());

        statement = "SELECT " + TEST_DOCUMENT_STRING_PROP_ID + " FROM " + TEST_DOCUMENT_TYPE_ID + " WHERE "
                + TEST_DOCUMENT_STRING_PROP_ID + "='My Doc StringProperty 1'";
        res = fDiscSvc.query(fRepositoryId, statement, searchAllVersions, includeAllowableActions,
                includeRelationships, renditionFilter, maxItems, skipCount, null);
        assertEquals(1, res.getObjects().size());
        assertEquals(1, res.getObjects().get(0).getProperties().getProperties().size()); // only
                                                                                         // one
                                                                                         // property
                                                                                         // should
                                                                                         // be
                                                                                         // delivered

        statement = "SELECT * FROM cmis:folder";
        res = fDiscSvc.query(fRepositoryId, statement, searchAllVersions, includeAllowableActions,
                includeRelationships, renditionFilter, maxItems, skipCount, null);
        // root + 2 at level 1 + 2*2 at level 2 = 7
        assertEquals(7, res.getObjects().size());

        /*
         * assertEquals(BigInteger.valueOf(9), res.getNumItems());
         * 
         * statement = "SELECT * FROM cmis:folder"; res =
         * fDiscSvc.query(fRepositoryId, statement, searchAllVersions,
         * includeAllowableActions, includeRelationships, renditionFilter,
         * maxItems, skipCount, null); // root + 2 at level 1 + 2*2 at level 2 =
         * 7 assertEquals(BigInteger.valueOf(7), res.getNumItems());
         * 
         * statement = "SELECT * FROM cmis:folder"; res =
         * fDiscSvc.query(fRepositoryId, statement, searchAllVersions,
         * includeAllowableActions, includeRelationships, renditionFilter,
         * maxItems, skipCount, null); // root + 2 at level 1 + 2*2 at level 2 =
         * 7 assertEquals(BigInteger.valueOf(7), res.getNumItems());
         * 
         * statement = "SELECT * FROM cmis:folder WHERE name='Jens'"; res =
         * fDiscSvc.query(fRepositoryId, statement, searchAllVersions,
         * includeAllowableActions, includeRelationships, renditionFilter,
         * maxItems, skipCount, null); assertEquals(BigInteger.valueOf(0),
         * res.getNumItems());
         */
        log.info("... testQuery() finished.");
    }

    @Test
    public void testQueryPaging() {
        log.info("starting testQuery() ...");

        String statement;
        ObjectList res;
        ObjectGenerator gen = new ObjectGenerator(fFactory, fNavSvc, fObjSvc, fRepSvc, fRepositoryId,
                ObjectGenerator.ContentKind.LOREM_IPSUM_TEXT);
        gen.setNumberOfDocumentsToCreatePerFolder(3);
        gen.setDocumentTypeId(TEST_DOCUMENT_TYPE_ID);
        gen.setFolderTypeId(TEST_FOLDER_TYPE_ID);

        List<String> propsToSet = new ArrayList<String>();
        propsToSet.add(TEST_DOCUMENT_STRING_PROP_ID);
        gen.setDocumentPropertiesToGenerate(propsToSet);

        propsToSet = new ArrayList<String>();
        propsToSet.add(TEST_FOLDER_STRING_PROP_ID);
        gen.setFolderPropertiesToGenerate(propsToSet);

        gen.createFolderHierachy(2, 2, fRootFolderId);

        Boolean searchAllVersions = Boolean.FALSE;
        Boolean includeAllowableActions = Boolean.FALSE;
        IncludeRelationships includeRelationships = IncludeRelationships.NONE;
        String renditionFilter = null;
        BigInteger skipCount = BigInteger.valueOf(0);
        BigInteger maxItems = BigInteger.valueOf(3);

        int count = 0;
        boolean hasMoreItems = true;
        statement = "SELECT * FROM cmis:document";
        while (hasMoreItems) {
            res = fDiscSvc.query(fRepositoryId, statement, searchAllVersions, includeAllowableActions,
                    includeRelationships, renditionFilter, maxItems, skipCount, null);
            hasMoreItems = res.hasMoreItems();
            assertEquals(3, res.getObjects().size());
            if (res.getNumItems() != null) {
                assertEquals(9L, res.getNumItems().longValue());
            }
            skipCount = skipCount.add(maxItems);
            ++count;
        }
        assertEquals(3, count);
    }

}
