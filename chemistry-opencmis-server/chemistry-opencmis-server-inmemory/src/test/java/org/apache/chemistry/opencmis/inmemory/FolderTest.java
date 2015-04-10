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

import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.apache.chemistry.opencmis.inmemory.storedobj.api.Fileable;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Filing;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Folder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.FolderImpl;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.ObjectStoreImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * Some test directly against the in-memory folder object.
 */
public class FolderTest extends TestCase {

    private ObjectStoreImpl fStore;
    private FolderImpl fRoot;
    private FolderImpl f1;
    private FolderImpl f2;
    private FolderImpl f3;
    private FolderImpl f4;
    private FolderImpl f11;
    private static final String TEST_REPOSITORY_ID = "TestRepositoryId";
    private static final String USER = "user";

    @Override
    @Before
    protected void setUp() throws Exception {
        ConfigurationSettings.init(new HashMap<String, String>());
        fStore = new ObjectStoreImpl(TEST_REPOSITORY_ID);
        createFolders();
    }

    @Test
    public void testCreateAndGetFolders() {
        try {
            createFolder("Folder 1", fRoot);
            fail("Should throw exception if folder already exists.");
        } catch (Exception e) {
        }
        assertEquals(f1.getName(), "Folder 1");
        assertEquals(f11.getName(), "Folder 1.1");
        assertNull(fRoot.getParentId());
        assertEquals(fRoot.getId(), f1.getParentId());
        assertEquals(f1.getId(), f11.getParentId());
        assertEquals(Filing.PATH_SEPARATOR, getPath(fRoot));
        assertEquals("/Folder 1", getPath(f1));
        assertEquals("/Folder 1/Folder 1.1", getPath(f11));
        StoredObject fTest = fStore.getObjectByPath("/", USER);
        assertEquals(fRoot, fTest);
        fTest = fStore.getObjectByPath("/Folder 1", USER);
        assertEquals(f1, fTest);
        fTest = fStore.getObjectByPath("/Folder 1/Folder 1.1", USER);
        assertEquals(f11, fTest);
        List<Fileable> subFolders = fStore.getChildren(fRoot, -1, -1, "user", false).getChildren();
        assertEquals(4, subFolders.size());
        subFolders = fStore.getChildren(f2, -1, -1, "user", false).getChildren();
        assertEquals(0, subFolders.size());
        subFolders = fStore.getChildren(f1, -1, -1, "user", false).getChildren();
        assertEquals(1, subFolders.size());
    }

    @Test
    public void testRenameFolder() {
        // rename top level folder
        String newName = "Folder B";
        String oldPath = getPath(f2);
        fStore.rename(f2, newName, USER);
        assertEquals(f2.getName(), newName);
        assertEquals(getPath(f2), Filing.PATH_SEPARATOR + newName);
        assertNull(fStore.getObjectByPath(oldPath, USER));
        assertEquals(f2, fStore.getObjectByPath(Filing.PATH_SEPARATOR + newName, USER));
        try {
            fStore.rename(f2, "Folder 3", USER);
            fail("Should not allow to rename a folder to an existing name");
        } catch (Exception e) {
        }

        // rename sub folder
        oldPath = getPath(f11);
        fStore.rename(f11, newName, USER);
        assertEquals(f11.getName(), newName);
        assertEquals(getPath(f11), "/Folder 1/Folder B");
        assertNull(fStore.getObjectByPath(oldPath, USER));
        assertEquals(f11, fStore.getObjectByPath("/Folder 1/Folder B", USER));

        // rename to existing name
        try {
        	newName = f3.getName();
            fStore.rename(f2, newName, USER);
            fail("Should not allow to rename a folder to an existing name");
        } catch (Exception e) {
        }

        // rename to same name
        try {
            newName = f2.getName();
        	fStore.rename(f2, f2.getName(), USER);
            assertEquals(f2.getName(), newName);
        } catch (Exception e) {
            fail("Rename with same name as before should succeed.");
        }

        // rename root folder
        try {
            fStore.rename(fRoot, "abc", USER);
            fail("Should not be possible to rename root folder");
        } catch (Exception e) {
        }
    }

    @Test
    public void testMoveFolder() {
        String oldPath = getPath(f1);
        Folder f1Parent = fRoot;
        fStore.move(f1, f1Parent, f3, USER);
        assertNull(fStore.getObjectByPath(oldPath, USER));
        assertEquals(getPath(f1), "/Folder 3/Folder 1");
        assertEquals(f1, fStore.getObjectByPath("/Folder 3/Folder 1", USER));

        fStore.rename(f2, "Folder 1", USER);
        try {
            Folder f2Parent = fRoot;
            fStore.move(f2, f2Parent, f3, USER);
            fail("Should not be possible to move folder to a folder that has a child with same name");
        } catch (Exception e) {
        }
    }

    @Test
    public void testDeleteFolder() {
        String oldPath = getPath(f2);
        fStore.deleteObject(f2.getId(), true, "TestUser");
        assertNull(fStore.getObjectByPath(oldPath, USER));

        try {
            fStore.deleteObject(f1.getId(), true, "TestUser");
            fail("Should not be possible to move folder that has children");
        } catch (Exception e) {
        }
    }

    private void createFolders() {
        fRoot = (FolderImpl) fStore.getRootFolder();
        f1 = (FolderImpl) createFolder("Folder 1", fRoot);

        f2 = (FolderImpl) createFolder("Folder 2", fRoot);

        f3 = (FolderImpl) createFolder("Folder 3", fRoot);

        f4 = (FolderImpl) createFolder("Folder 4", fRoot);

        f11 = (FolderImpl) createFolder("Folder 1.1", f1);
    }

    private Folder createFolder(String name, Folder parent) {
        return fStore.createFolder(name, null, "user", parent, null, null, null);
    }

    private String getPath(Folder folder) {
        return fStore.getFolderPath(folder.getId());
    }
}
