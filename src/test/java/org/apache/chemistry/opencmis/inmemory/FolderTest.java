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

import java.util.List;

import junit.framework.TestCase;

import org.apache.chemistry.opencmis.inmemory.storedobj.api.Filing;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Folder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.FolderImpl;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.ObjectStoreImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * @author Jens
 */

/**
 * Some test directly against the in-memory folder object.
 */
public class FolderTest extends TestCase {

    private ObjectStore fStore;
    private FolderImpl fRoot;
    private FolderImpl f1;
    private FolderImpl f2;
    private FolderImpl f3;
    private FolderImpl f4;
    private FolderImpl f11;
    private static final String TEST_REPOSITORY_ID = "TestRepositoryId";
    private static final String USER = "user";

    @Before
    protected void setUp() throws Exception {
        fStore = new ObjectStoreImpl(TEST_REPOSITORY_ID);
        createFolders();
    }

    @Test
    public void testCreatAndGetFolders() {
        try {
            Folder childFolder = createFolder("Folder 1");
            fRoot.addChildFolder(childFolder);
            fail("Should throw exception if folder already exists.");
        } catch (Exception e) {
        }
        assertEquals(f1.getName(), "Folder 1");
        assertEquals(f11.getName(), "Folder 1.1");
        assertNull(fRoot.getParent());
        assertEquals(fRoot, f1.getParent());
        assertEquals(f1, f11.getParent());
        assertEquals(Filing.PATH_SEPARATOR, fRoot.getPath());
        assertEquals("/Folder 1", f1.getPath());
        assertEquals("/Folder 1/Folder 1.1", f11.getPath());
        StoredObject fTest = fStore.getObjectByPath("/", USER);
        assertEquals(fRoot, fTest);
        fTest = fStore.getObjectByPath("/Folder 1", USER);
        assertEquals(f1, fTest);
        fTest = fStore.getObjectByPath("/Folder 1/Folder 1.1", USER);
        assertEquals(f11, fTest);
        List<? extends StoredObject> subFolders = fRoot.getChildren(-1, -1, "user").getChildren();
        assertEquals(4, subFolders.size());
        subFolders = f2.getChildren(-1, -1, "user").getChildren();
        assertEquals(0, subFolders.size());
        subFolders = f1.getChildren(-1, -1, "user").getChildren();
        assertEquals(1, subFolders.size());
    }

    @Test
    public void testRenameFolder() {
        // rename top level folder
        String newName = "Folder B";
        String oldPath = f2.getPath();
        f2.rename(newName);
        assertEquals(f2.getName(), newName);
        assertEquals(f2.getPath(), Filing.PATH_SEPARATOR + newName);
        assertNull(fStore.getObjectByPath(oldPath, USER));
        assertEquals(f2, fStore.getObjectByPath(Filing.PATH_SEPARATOR + newName, USER));
        try {
            f2.rename("Folder 3");
            fail("Should not allow to rename a folder to an existing name");
        } catch (Exception e) {
        }

        // rename sub folder
        oldPath = f11.getPath();
        f11.rename(newName);
        assertEquals(f11.getName(), newName);
        assertEquals(f11.getPath(), "/Folder 1/Folder B");
        assertNull(fStore.getObjectByPath(oldPath, USER));
        assertEquals(f11, fStore.getObjectByPath("/Folder 1/Folder B", USER));
        try {
            f2.rename(newName);
            fail("Should not allow to rename a folder to an existing name");
        } catch (Exception e) {
        }
        try {
            f2.rename("illegal/name");
            fail("Should not allow to rename a folder to a name with illegal name");
        } catch (Exception e) {
        }

        // rename root folder
        try {
            fRoot.rename("abc");
            fail("Should not be possible to rename root folder");
        } catch (Exception e) {
        }
    }

    @Test
    public void testMoveFolder() {
        String oldPath = f1.getPath();
        Folder f1Parent = f1.getParent();
        f1.move(f1Parent, f3);
        assertNull(fStore.getObjectByPath(oldPath, USER));
        assertEquals(f1.getPath(), "/Folder 3/Folder 1");
        assertEquals(f1, fStore.getObjectByPath("/Folder 3/Folder 1", USER));

        f2.rename("Folder 1");
        try {
            Folder f2Parent = f2.getParent();
            f2.move(f2Parent, f3);
            fail("Should not be possible to move folder to a folder that has a child with same name");
        } catch (Exception e) {
        }
    }

    @Test
    public void testDeleteFolder() {
        String oldPath = f2.getPath();
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
        f1 = (FolderImpl) createFolder("Folder 1");
        fRoot.addChildFolder(f1);
        f1.persist();

        f2 = (FolderImpl) createFolder("Folder 2");
        fRoot.addChildFolder(f2);
        f2.persist();

        f3 = (FolderImpl) createFolder("Folder 3");
        fRoot.addChildFolder(f3);
        f3.persist();

        f4 = (FolderImpl) createFolder("Folder 4");
        fRoot.addChildFolder(f4);
        f4.persist();

        f11 = (FolderImpl) createFolder("Folder 1.1");
        f1.addChildFolder(f11);
        f11.persist();
    }
    
    private Folder createFolder(String name) {
    	return fStore.createFolder(name, null, "user", null, null, null);    	
    }
}
