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
package org.apache.chemistry.opencmis.fit.bindings;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Base class for binding layer tests. It makes sure that the provider object is
 * only created once and that there is a test folder.
 *
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 *
 */
public abstract class AbstractBindingIT {
    public static String FOLDER_TYPE = "cmis:folder";
    public static String DOCUMENT_TYPE = "cmis:document";

    private static CmisBinding binding;
    private static String fRepositoryId;
    private static String fTestFolderId;

    /**
     * Returns the id of test folder. Tests should only use this folder.
     */
    protected String getTestFolderId() {
        return fTestFolderId;
    }

    /**
     * Returns the current binding object.
     */
    protected static CmisBinding getBinding() {
        return binding;
    }

    /**
     * Returns a new binding object.
     */
    protected abstract CmisBinding createBinding();

    /**
     * Returns the repository id of the test repository.
     */
    protected abstract String getRepositoryId();

    @BeforeClass
    public static void setUpClass() {
        binding = null;
    }

    @Before
    public void setUpTest() {
        // only the first test creates the test environment
        if (binding == null) {
            // System.out.println("Creating provider...");

            binding = createBinding();
            fRepositoryId = getRepositoryId();
            createTestFolder();
        }
    }

    @AfterClass
    public static void tearDownClass() {
        deleteTestFolder();
        binding = null;
    }

    /**
     * Creates a folder that will be used by all read-write tests.
     */
    private void createTestFolder() {
        // System.out.println("Creating test folder...");

        // get root folder id
        RepositoryInfo ri = getBinding().getRepositoryService().getRepositoryInfo(fRepositoryId, null);
        assertNotNull(ri);
        assertNotNull(ri.getRootFolderId());

        String rootFolderId = ri.getRootFolderId();

        // set up properties
        List<PropertyData<?>> propertyList = new ArrayList<PropertyData<?>>();
        propertyList.add(getBinding().getObjectFactory().createPropertyStringData(PropertyIds.NAME,
                "provider-tests-" + System.currentTimeMillis()));
        propertyList.add(getBinding().getObjectFactory().createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, FOLDER_TYPE));

        Properties properties = getBinding().getObjectFactory().createPropertiesData(propertyList);

        // create the folder
        fTestFolderId = getBinding().getObjectService().createFolder(fRepositoryId, properties, rootFolderId, null,
                null, null, null);
    }

    /**
     * Deletes the test folder.
     */
    private static void deleteTestFolder() {
        if (fTestFolderId == null) {
            return;
        }

        // System.out.println("Deleting test folder...");

        // delete the whole tree
        getBinding().getObjectService().deleteTree(fRepositoryId, fTestFolderId, true, UnfileObject.DELETE, true, null);
    }
}
