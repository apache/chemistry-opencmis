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

import java.util.GregorianCalendar;
import java.util.List;

import org.apache.opencmis.client.api.Ace;
import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.ContentStream;
import org.apache.opencmis.client.api.Document;
import org.apache.opencmis.client.api.Folder;
import org.apache.opencmis.client.api.Policy;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.Relationship;
import org.apache.opencmis.client.api.Rendition;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.repository.AllowableActions;
import org.apache.opencmis.client.api.util.AceList;
import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.enums.RelationshipDirection;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.ObjectData;

public class PersistentDocumentImpl implements Document {

	public PersistentDocumentImpl(PersistentSessionImpl persistentSessionImpl,
			ObjectData od) {
		throw new CmisRuntimeException("not implemented");
	}

	public void cancelCheckOut() {
		throw new CmisRuntimeException("not implemented");

	}

	public void checkIn(boolean major, List<Property<?>> properties,
			ContentStream contentStream, String checkinComment,
			List<Policy> policies, List<Ace> addACEs, List<Ace> removeACEs) {
		throw new CmisRuntimeException("not implemented");

	}

	public boolean checkOut() {
		throw new CmisRuntimeException("not implemented");
	}

	public Document copy(List<Property<?>> properties,
			VersioningState versioningState, List<Policy> policies,
			List<Ace> addACEs, List<Ace> removeACEs) {
		throw new CmisRuntimeException("not implemented");
		
	}

	public void deleteAllVersions() {
		throw new CmisRuntimeException("not implemented");

	}

	public void deleteContentStream() {
		throw new CmisRuntimeException("not implemented");

	}

	public List<Document> getAllVersions() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public String getCheckinComment() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public ContentStream getContentStream() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public Document getObjectOfLatestVersion(boolean major) {
		throw new CmisRuntimeException("not implemented");
		
	}

	public List<String> getPaths() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public List<Property<?>> getPropertiesOfLatestVersion(boolean major) {
		throw new CmisRuntimeException("not implemented");
		
	}

	public List<Property<?>> getPropertiesOfLatestVersion(boolean major,
			String filter) {
		throw new CmisRuntimeException("not implemented");
		
	}

	public List<Rendition> getRenditions() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public String getVersionLabel() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public String getVersionSeries() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public String getVersionSeriesCheckedOut() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public String getVersionSeriesCheckedOutBy() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public boolean isImmutable() {
		throw new CmisRuntimeException("not implemented");
	}

	public boolean isLatestMajorVersion() {
		throw new CmisRuntimeException("not implemented");
	}

	public boolean isLatestVersion() {
		throw new CmisRuntimeException("not implemented");
	}

	public boolean isMajorVersion() {
		throw new CmisRuntimeException("not implemented");
	}

	public boolean isVersionSeriesCheckedOut() {
		throw new CmisRuntimeException("not implemented");
	}

	public void setContentStream(boolean overwrite, ContentStream contentStream) {
		throw new CmisRuntimeException("not implemented");

	}

	public void addAcl(List<Ace> addAces, AclPropagation aclPropagation) {
		throw new CmisRuntimeException("not implemented");

	}

	public void addToFolder(Folder folder, boolean allVersions) {
		throw new CmisRuntimeException("not implemented");

	}

	public AceList applyAcl(List<Ace> addAces, List<Ace> removeAces,
			AclPropagation aclPropagation) {
		throw new CmisRuntimeException("not implemented");
		
	}

	public void applyPolicy(Policy policy) {
		throw new CmisRuntimeException("not implemented");

	}

	public void applyPolicy(String policyId) {
		throw new CmisRuntimeException("not implemented");

	}

	public void delete(boolean allVersions) {
		throw new CmisRuntimeException("not implemented");

	}

	public List<Ace> getAcl() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public AceList getAcl(boolean onlyBasicPermissions) {
		throw new CmisRuntimeException("not implemented");
		
	}

	public AllowableActions getAllowableActions() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public ObjectType getBaseType() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public BaseObjectTypeIds getBaseTypeId() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public String getChangeToken() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public String getCreatedBy() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public GregorianCalendar getCreationDate() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public String getId() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public GregorianCalendar getLastModificationDate() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public String getLastModifiedBy() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public String getName() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public List<Folder> getParents() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public List<Policy> getPolicies() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public List<Property<?>> getProperties() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public List<Property<?>> getProperties(String filter) {
		throw new CmisRuntimeException("not implemented");
		
	}

	public <T> Property<T> getProperty(String id) {
		throw new CmisRuntimeException("not implemented");
		
	}

	public <T> List<T> getPropertyMultivalue(String id) {
		throw new CmisRuntimeException("not implemented");
		
	}

	public <T> T getPropertyValue(String id) {
		throw new CmisRuntimeException("not implemented");
		
	}

	public List<Relationship> getRelationships() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public List<Relationship> getRelationships(
			boolean includeSubRelationshipTypes,
			RelationshipDirection relationshipDirection, ObjectType type) {
		throw new CmisRuntimeException("not implemented");
		
	}

	public ObjectType getType() {
		throw new CmisRuntimeException("not implemented");
		
	}

	public boolean isChanged() {
		throw new CmisRuntimeException("not implemented");
	}

	public boolean isExactAcl() {
		throw new CmisRuntimeException("not implemented");
	}

	public CmisObject move(Folder targetfolder) {
		throw new CmisRuntimeException("not implemented");
		
	}

	public void removeAcl(List<Ace> addAces, AclPropagation aclPropagation) {
		throw new CmisRuntimeException("not implemented");

	}

	public void removeFromFolder(Folder folder) {
		throw new CmisRuntimeException("not implemented");

	}

	public void removePolicy(Policy policy) {
		throw new CmisRuntimeException("not implemented");

	}

	public void removePolicy(String policyId) {
		throw new CmisRuntimeException("not implemented");

	}

	public void setName(String name) {
		throw new CmisRuntimeException("not implemented");

	}

	public <T> void setProperty(String id, T value) {
		throw new CmisRuntimeException("not implemented");

	}

	public <T> void setPropertyMultivalue(String id, List<T> value) {
		throw new CmisRuntimeException("not implemented");

	}

	public void setType(ObjectType type) {
		throw new CmisRuntimeException("not implemented");

	}

	public void setTypeId(String typeId) {
		throw new CmisRuntimeException("not implemented");

	}

	public void updateProperties(List<Property<?>> properties) {
		throw new CmisRuntimeException("not implemented");

	}

}
