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
package org.apache.chemistry.opencmis.tck.tests.crud;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.UNEXPECTED_EXCEPTION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.chemistry.opencmis.client.api.AsyncSession;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.async.AbstractExecutorServiceAsyncSession;
import org.apache.chemistry.opencmis.client.runtime.async.AsyncSessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Simple document test.
 */
public class AsyncCreateAndDeleteFolderTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Asynchronous Create and Delete Folder Test");
        setDescription("Creates folders in parallel and deletes the created folders in parallel.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        int numOfFolders = 100;

        // create an async session
        AsyncSession asyncSession = AsyncSessionFactoryImpl.newInstance().createAsyncSession(session, 10);

        // create a test folder
        Folder testFolder = createTestFolder(session);

        try {
            // create folders
            List<Future<ObjectId>> folderFutures = new ArrayList<Future<ObjectId>>();
            for (int i = 0; i < numOfFolders; i++) {
                String name = "asyncfolder" + i;

                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put(PropertyIds.NAME, name);
                properties.put(PropertyIds.OBJECT_TYPE_ID, getFolderTestTypeId());

                Future<ObjectId> newFolder = asyncSession.createFolder(properties, testFolder);

                folderFutures.add(newFolder);
            }

            // wait for all folders being created
            List<ObjectId> folderIds = new ArrayList<ObjectId>();
            try {
                for (Future<ObjectId> folderFuture : folderFutures) {
                    ObjectId id = folderFuture.get();
                    folderIds.add(id);
                }
            } catch (Exception e) {
                addResult(createResult(UNEXPECTED_EXCEPTION,
                        "Folder could not been created! Exception: " + e.getMessage(), e, true));
            }

            // check children of test folder
            int count = countChildren(testFolder);
            f = createResult(FAILURE, "Test folder should have " + numOfFolders + " children but has " + count + "!");
            addResult(assertEquals(count, numOfFolders, null, f));

            // get folders
            Map<String, Future<CmisObject>> getObjectFutures = new HashMap<String, Future<CmisObject>>();

            for (ObjectId folderId : folderIds) {
                Future<CmisObject> getObjectFuture = asyncSession.getObject(folderId, SELECT_ALL_NO_CACHE_OC);
                getObjectFutures.put(folderId.getId(), getObjectFuture);
            }

            // wait for all folders being fetched
            List<String> paths = new ArrayList<String>();
            try {
                for (Map.Entry<String, Future<CmisObject>> getObjectFuture : getObjectFutures.entrySet()) {
                    CmisObject object = getObjectFuture.getValue().get();

                    f = createResult(FAILURE, "Fetching folder failed!");
                    addResult(assertIsTrue(object instanceof Folder, null, f));

                    if (object != null) {
                        f = createResult(FAILURE, "Fetched wrong folder!");
                        addResult(assertEquals(getObjectFuture.getKey(), object.getId(), null, f));

                        paths.add(((Folder) object).getPath());
                    }
                }
            } catch (Exception e) {
                addResult(createResult(UNEXPECTED_EXCEPTION,
                        "Folders could not been fetched! Exception: " + e.getMessage(), e, true));
            }

            // get folders by path
            Map<String, Future<CmisObject>> getObjectByPathFutures = new HashMap<String, Future<CmisObject>>();

            for (String path : paths) {
                Future<CmisObject> getObjectByPathFuture = asyncSession.getObjectByPath(path, SELECT_ALL_NO_CACHE_OC);
                getObjectByPathFutures.put(path, getObjectByPathFuture);
            }

            // wait for all folders being fetched
            try {
                for (Map.Entry<String, Future<CmisObject>> getObjectByPathFuture : getObjectByPathFutures.entrySet()) {
                    CmisObject object = getObjectByPathFuture.getValue().get();

                    f = createResult(FAILURE, "Fetching folder failed!");
                    addResult(assertIsTrue(object instanceof Folder, null, f));

                    if (object != null) {
                        f = createResult(FAILURE, "Fetched wrong folder!");
                        addResult(assertEquals(getObjectByPathFuture.getKey(), ((Folder) object).getPath(), null, f));
                    }
                }
            } catch (Exception e) {
                addResult(createResult(UNEXPECTED_EXCEPTION,
                        "Folders could not been fetched! Exception: " + e.getMessage(), e, true));
            }

            // delete folders
            List<Future<?>> delFutures = new ArrayList<Future<?>>();
            for (ObjectId folderId : folderIds) {
                Future<?> delFuture = asyncSession.deleteTree(folderId, true, UnfileObject.DELETE, true);
                delFutures.add(delFuture);
            }

            // wait for all folders being deleted
            try {
                for (Future<?> delFuture : delFutures) {
                    delFuture.get();
                }
            } catch (Exception e) {
                addResult(createResult(UNEXPECTED_EXCEPTION,
                        "Folder could not been deleted! Exception: " + e.getMessage(), e, true));
            }

            // check children of test folder
            count = countChildren(testFolder);
            f = createResult(FAILURE, "Test folder should be empty but has " + count + " children!");
            addResult(assertEquals(count, 0, null, f));
        } finally {
            // delete the test folder
            deleteTestFolder();

            if (asyncSession instanceof AbstractExecutorServiceAsyncSession<?>) {
                ((AbstractExecutorServiceAsyncSession<?>) asyncSession).shutdown();
            }
        }

        addResult(createInfoResult("Tested the parallel creation and deletion of " + numOfFolders + " folders."));
    }

    private int countChildren(Folder folder) {
        int count = 0;
        ItemIterable<CmisObject> children = folder.getChildren(SELECT_ALL_NO_CACHE_OC);
        for (CmisObject child : children) {
            if (child instanceof Folder) {
                count++;
            }
        }

        return count;
    }
}
