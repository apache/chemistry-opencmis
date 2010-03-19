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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.opencmis.client.api.Ace;
import org.apache.opencmis.client.api.ObjectId;
import org.apache.opencmis.client.api.Policy;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.enums.CmisProperties;
import org.junit.Test;

public class WriteObjectTest extends AbstractSessionTest {

	@Test
	public void createFolder() {
		ObjectId parentId = this.session
				.createObjectId(Fixture.getTestRootId());
		String folderName = UUID.randomUUID().toString();
		String typeId = FixtureData.FOLDER_TYPE_ID.value();

		ObjectType ot = this.session.getTypeDefinition(typeId);
		Map<String, PropertyDefinition<?>> pdefs = ot.getPropertyDefintions();
		List<Property<?>> properties = new ArrayList<Property<?>>();
		Property<?> prop = null;

		for (PropertyDefinition<?> pd : pdefs.values()) {
			try {
				CmisProperties cmisp = CmisProperties.fromValue(pd.getId());
				switch (cmisp) {
				case NAME:
					prop = this.session.getObjectFactory().createProperty(
							(PropertyDefinition<String>) pd, folderName);
					properties.add(prop);
					break;
				case OBJECT_TYPE_ID:
					prop = this.session.getObjectFactory().createProperty(
							(PropertyDefinition<String>) pd, typeId);
					properties.add(prop);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				// custom property definition
			}

		}

		List<Ace> addAce = new ArrayList<Ace>();
		List<Ace> removeAce = new ArrayList<Ace>();
		List<Policy> policies = new ArrayList<Policy>();
		ObjectId id = this.session.createFolder(properties, parentId, policies,
				addAce, removeAce);
		Assert.assertNotNull(id);
	}

	/**
	 * Method to create named and typed folder using a given parent id.
	 * 
	 * @param foldeName
	 *            Name of folder to create
	 * @param parentId
	 *            Id of parent folder to create this folder as a child
	 */
	@SuppressWarnings("unchecked")
	public void createFolder(String folderName, ObjectId parentId) {
		// retrieve all property definitions for specific cmis type
		ObjectType ot = this.session.getTypeDefinition("cmis:folder");
		Map<String, PropertyDefinition<?>> pdefs = ot.getPropertyDefintions();

		// get property definitions from object type
		PropertyDefinition<String> pdName = (PropertyDefinition<String>) pdefs
				.get(CmisProperties.NAME.value());
		PropertyDefinition<String> pdType = (PropertyDefinition<String>) pdefs
				.get(CmisProperties.OBJECT_TYPE_ID.value());

		// create mandatory properties of object type
		Property<?> propName = this.session.getObjectFactory().createProperty(
				pdName, folderName);
		Property<?> propType = this.session.getObjectFactory().createProperty(
				pdType, "cmis:folder");

		// fill properties list
		List<Property<?>> properties = new ArrayList<Property<?>>();
		properties.add(propName);
		properties.add(propType);

		// create additional optional parameter
		List<Ace> ace = new ArrayList<Ace>();
		List<Policy> pol = null;

		// ready steady go
		ObjectId id = this.session.createFolder(properties, parentId, pol, ace,
				ace);
		Assert.assertNotNull(id);
	}

	// public void createFolderOptimzed(String folderName, ObjectId parentId) {
	// // retrieve all property definitions for specific cmis type
	// ObjectType ot =
	// this.session.getTypeDefinition(BaseObjectTypeIds.CMIS_FOLDER);
	// Map<String, PropertyDefinition<?>> pdefs = ot.getPropertyDefintions();
	//
	// // get property definitions from object type
	// PropertyDefinition<?> pdName = pdefs.get(CmisProperties.NAME);
	// PropertyDefinition<?> pdType = pdefs.get(CmisProperties.OBJECT_TYPE_ID);
	//
	// // create mandatory properties of object type
	// Property<?> propName = this.session.getObjectFactory().createProperty(
	// pdName, folderName);
	// Property<?> propType = this.session.getObjectFactory().createProperty(
	// pdType, "cmis:folder");
	//
	// // fill properties list
	// List<Property<?>> properties = new ArrayList<Property<?>>();
	// properties.add(propName);
	// properties.add(propType);
	//
	// // ready steady go
	// ObjectId id = this.session.createFolder(properties, parentId);
	// Assert.assertNotNull(id);
	// }

}
