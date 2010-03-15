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
package org.apache.opencmis.client.runtime;

import java.util.List;

import junit.framework.Assert;

import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.FileableCmisObject;
import org.apache.opencmis.client.api.Folder;
import org.apache.opencmis.client.api.util.Container;
import org.apache.opencmis.client.api.util.PagingList;
import org.junit.Test;

public class ReadOnlyNavigationTest extends AbstractSessionTest {

	@Test
	public void navigateChildrenMin() {
		String path = "/" + Fixture.TEST_ROOT_FOLDER_NAME;
		Folder folder = (Folder) this.session.getObjectByPath(path);
		Assert.assertNotNull("folder not found: " + path, folder);

		PagingList<CmisObject> pl = folder.getChildren(1);
		Assert.assertNotNull(pl);
		// Assert.assertFalse(pl.isEmpty());

		for (List<CmisObject> cl : pl) {
			for (CmisObject o : cl) {
				Assert.assertNotNull(o);
			}
		}
	}

	@Test
	public void navigateChildrenMax() {
		String path = "/" + Fixture.TEST_ROOT_FOLDER_NAME;
		Folder folder = (Folder) this.session.getObjectByPath(path);
		Assert.assertNotNull("folder not found: " + path, folder);

		PagingList<CmisObject> pl = folder.getChildren(1000);
		Assert.assertNotNull(pl);
		// Assert.assertFalse(pl.isEmpty());

		for (List<CmisObject> cl : pl) {
			for (CmisObject o : cl) {
				Assert.assertNotNull(o);
			}
		}
	}

	@Test
	public void navigateChildrenMed() {
		String path = "/" + Fixture.TEST_ROOT_FOLDER_NAME;
		Folder folder = (Folder) this.session.getObjectByPath(path);
		Assert.assertNotNull("folder not found: " + path, folder);

		PagingList<CmisObject> pl = folder.getChildren(2);
		Assert.assertNotNull(pl);
		// Assert.assertFalse(pl.isEmpty());

		for (List<CmisObject> cl : pl) {
			for (CmisObject o : cl) {
				Assert.assertNotNull(o);
			}
		}
	}

	
	@Test
	public void navigateDescendantsMin() {
		String path = "/" + Fixture.TEST_ROOT_FOLDER_NAME;
		Folder folder = (Folder) this.session.getObjectByPath(path);
		Assert.assertNotNull("folder not found: " + path, folder);

		List<Container<FileableCmisObject>> desc = folder.getDescendants(1);
		Assert.assertNotNull(desc);
		Assert.assertFalse(desc.isEmpty());

		for (Container<FileableCmisObject> o : desc) {
			Assert.assertNotNull(o);
			Assert.assertNotNull(o.getItem());
		}
	}
	@Test
	public void navigateDescendantsMax() {
		String path = "/" + Fixture.TEST_ROOT_FOLDER_NAME;
		Folder folder = (Folder) this.session.getObjectByPath(path);
		Assert.assertNotNull("folder not found: " + path, folder);

		List<Container<FileableCmisObject>> desc = folder.getDescendants(1000);
		Assert.assertNotNull(desc);
		Assert.assertFalse(desc.isEmpty());

		for (Container<FileableCmisObject> o : desc) {
			Assert.assertNotNull(o);
			Assert.assertNotNull(o.getItem());
		}
	}
	@Test
	public void navigateDescendantsMed() {
		String path = "/" + Fixture.TEST_ROOT_FOLDER_NAME;
		Folder folder = (Folder) this.session.getObjectByPath(path);
		Assert.assertNotNull("folder not found: " + path, folder);

		List<Container<FileableCmisObject>> desc = folder.getDescendants(2);
		Assert.assertNotNull(desc);
		Assert.assertFalse(desc.isEmpty());

		for (Container<FileableCmisObject> o : desc) {
			Assert.assertNotNull(o);
			Assert.assertNotNull(o.getItem());
		}
	}

	@Test
	public void navigateTreeMed() {
		String path = "/" + Fixture.TEST_ROOT_FOLDER_NAME;
		Folder folder = (Folder) this.session.getObjectByPath(path);
		Assert.assertNotNull("folder not found: " + path, folder);

		List<Container<FileableCmisObject>> tree = folder.getFolderTree(2);
		Assert.assertNotNull(tree);
		Assert.assertFalse(tree.isEmpty());

		for (Container<FileableCmisObject> o : tree) {
			Assert.assertNotNull(o);
			Assert.assertNotNull(o.getItem());
		}
	}
	@Test
	public void navigateTreeMin() {
		String path = "/" + Fixture.TEST_ROOT_FOLDER_NAME;
		Folder folder = (Folder) this.session.getObjectByPath(path);
		Assert.assertNotNull("folder not found: " + path, folder);

		List<Container<FileableCmisObject>> tree = folder.getFolderTree(1);
		Assert.assertNotNull(tree);
		Assert.assertFalse(tree.isEmpty());

		for (Container<FileableCmisObject> o : tree) {
			Assert.assertNotNull(o);
			Assert.assertNotNull(o.getItem());
		}
	}
	@Test
	public void navigateTreeMax() {
		String path = "/" + Fixture.TEST_ROOT_FOLDER_NAME;
		Folder folder = (Folder) this.session.getObjectByPath(path);
		Assert.assertNotNull("folder not found: " + path, folder);

		List<Container<FileableCmisObject>> tree = folder.getFolderTree(1000);
		Assert.assertNotNull(tree);
		Assert.assertFalse(tree.isEmpty());

		for (Container<FileableCmisObject> o : tree) {
			Assert.assertNotNull(o);
			Assert.assertNotNull(o.getItem());
		}
	}

	@Test
	public void navigatePagingRandom() {
		String path = "/" + Fixture.TEST_ROOT_FOLDER_NAME;
		Folder folder = (Folder) this.session.getObjectByPath(path);
		Assert.assertNotNull("folder not found: " + path, folder);

		PagingList<CmisObject> pl = folder.getChildren(2);
		Assert.assertNotNull(pl);
		// Assert.assertFalse(pl.isEmpty());

		List<CmisObject> firstPage = pl.get(0);
		Assert.assertNotNull(firstPage);
	}
}
