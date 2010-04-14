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
package org.apache.opencmis.fit.runtime;

import java.util.ArrayList;
import java.util.List;

import org.apache.opencmis.client.provider.factory.CmisProviderFactory;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.provider.CmisProvider;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.util.repository.ObjectGenerator;
import org.junit.Assert;

public class FixtureSetup {

	private CmisProvider provider = null;
	private String rootFolderId = null; // root
	private String testRootFolderId = null; // test root
	private String repositoryId = null;

	public void teardown() {
		this.provider.getObjectService().deleteTree(this.repositoryId,
				this.testRootFolderId, true, UnfileObjects.DELETE, true, null);
	}

	public void setup() {
		this.repositoryId = Fixture.getParamter().get(
				SessionParameter.REPOSITORY_ID);
		Assert.assertNotNull(this.repositoryId);

		this.provider = CmisProviderFactory.newInstance().createCmisProvider(
				Fixture.getParamter());
		Assert.assertNotNull(this.provider);

		// root folder
		if (Fixture.getParamter().containsKey(
				FixtureSessionParameter.TEST_ROOT_FOLDER_ID)) {
			// test root folder
			this.rootFolderId = Fixture.getParamter().get(
					FixtureSessionParameter.TEST_ROOT_FOLDER_ID);
			Assert.assertNotNull(this.rootFolderId);
		} else {
			RepositoryInfoData rid = this.provider.getRepositoryService()
					.getRepositoryInfo(this.repositoryId, null);
			Assert.assertNotNull(rid);
			this.rootFolderId = rid.getRootFolderId();
			Assert.assertNotNull(this.rootFolderId);
		}

		// object types
		String documentTypeId = FixtureData.DOCUMENT_TYPE_ID.value();
		Assert.assertNotNull(documentTypeId);
		String folderTypeId = FixtureData.FOLDER_TYPE_ID.value();
		Assert.assertNotNull(folderTypeId);

		// create test root folder
		List<PropertyData<?>> propList = new ArrayList<PropertyData<?>>();
		propList.add(this.provider.getObjectFactory().createPropertyStringData(
				PropertyIds.CMIS_NAME, Fixture.TEST_ROOT_FOLDER_NAME));
		propList.add(this.provider.getObjectFactory().createPropertyIdData(
				PropertyIds.CMIS_OBJECT_TYPE_ID, folderTypeId));

		PropertiesData properties = this.provider.getObjectFactory()
				.createPropertiesData(propList);

		this.testRootFolderId = this.provider.getObjectService().createFolder(
				this.repositoryId, properties, this.rootFolderId, null, null,
				null, null);
		Assert.assertNotNull(this.testRootFolderId);

		ObjectGenerator og = new ObjectGenerator(provider.getObjectFactory(),
				provider.getNavigationService(), provider.getObjectService(),
				this.repositoryId);

		og.setContentSizeInKB(10);
		og.setDocumentTypeId(documentTypeId);
		og.setFolderTypeId(folderTypeId);
		og.setNumberOfDocumentsToCreatePerFolder(2);
		og.setDocumentPropertiesToGenerate(new ArrayList<String>());
		og.setFolderPropertiesToGenerate(new ArrayList<String>());

		og.createFolderHierachy(2, 2, this.testRootFolderId);
	}

	public String getTestRootId() {
		if (this.testRootFolderId == null) {
			this.testRootFolderId = Fixture.TEST_ROOT_FOLDER_NAME;
		}
		return this.testRootFolderId;
	}

}
