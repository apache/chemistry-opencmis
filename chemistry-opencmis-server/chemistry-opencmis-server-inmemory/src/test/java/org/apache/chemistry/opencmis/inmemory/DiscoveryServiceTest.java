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

import org.apache.chemistry.opencmis.commons.api.ObjectList;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.inmemory.ObjectServiceTest.ObjectTestTypeSystemCreator;
import org.apache.chemistry.opencmis.util.repository.ObjectGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

public class DiscoveryServiceTest extends AbstractServiceTst {

    private static Log log = LogFactory.getLog(DiscoveryServiceTest.class);
    private static final String TEST_FOLDER_TYPE_ID = ObjectServiceTest.TEST_FOLDER_TYPE_ID;
    private static final String TEST_DOCUMENT_TYPE_ID = ObjectServiceTest.TEST_DOCUMENT_TYPE_ID;
    private static final String TEST_FOLDER_STRING_PROP_ID = ObjectServiceTest.TEST_FOLDER_STRING_PROP_ID;
    private static final String TEST_DOCUMENT_STRING_PROP_ID = ObjectServiceTest.TEST_DOCUMENT_STRING_PROP_ID;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setTypeCreatorClass(ObjectTestTypeSystemCreator.class.getName());
        super.setUp();
    }

    @Test
    public void testQuery() throws Exception {
        log.info("starting testQuery() ...");

        ObjectGenerator gen = new ObjectGenerator(fFactory, fNavSvc, fObjSvc, fRepositoryId);
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

        String statement = "SELECT * FROM cmis:document";
        ObjectList res = fDiscSvc.query(fRepositoryId, statement, searchAllVersions, includeAllowableActions,
                includeRelationships, renditionFilter, maxItems, skipCount, null);
        // 3 at level 1 + 3*2 at level 2 = 9
        assertEquals(BigInteger.valueOf(9), res.getNumItems());

        statement = "SELECT * FROM cmis:folder";
        res = fDiscSvc.query(fRepositoryId, statement, searchAllVersions, includeAllowableActions,
                includeRelationships, renditionFilter, maxItems, skipCount, null);
        // root + 2 at level 1 + 2*2 at level 2 = 7
        assertEquals(BigInteger.valueOf(7), res.getNumItems());

        statement = "SELECT * FROM cmis:folder";
        res = fDiscSvc.query(fRepositoryId, statement, searchAllVersions, includeAllowableActions,
                includeRelationships, renditionFilter, maxItems, skipCount, null);
        // root + 2 at level 1 + 2*2 at level 2 = 7
        assertEquals(BigInteger.valueOf(7), res.getNumItems());

        log.info("... testQuery() finished.");
    }

}
