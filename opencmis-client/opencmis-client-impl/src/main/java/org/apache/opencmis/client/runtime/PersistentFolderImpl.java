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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeMap;

import org.apache.opencmis.client.api.Ace;
import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.ContentStream;
import org.apache.opencmis.client.api.Document;
import org.apache.opencmis.client.api.Folder;
import org.apache.opencmis.client.api.Policy;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.Relationship;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.repository.AllowableActions;
import org.apache.opencmis.client.api.util.AceList;
import org.apache.opencmis.client.api.util.PagingList;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.RelationshipDirection;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.opencmis.commons.provider.AccessControlEntry;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.FailedToDeleteData;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;

public class PersistentFolderImpl implements Folder {

	private PersistentSessionImpl session;
	private ObjectData objectData;

	public PersistentFolderImpl(PersistentSessionImpl session, ObjectData od) {

		this.session = session;
		this.objectData = od;
	}

	public PersistentFolderImpl(PersistentSessionImpl session) {
		this.session = session;
	}

	public Document createDocument(String name) {
		throw new CmisRuntimeException("not implemented");
	}

	public Document createDocument(String name, String typeId) {
		throw new CmisRuntimeException("not implemented");
	}

	public Document createDocument(List<Property<?>> properties,
			ContentStream contentstream, VersioningState versioningState,
			List<Policy> policies, List<Ace> addACEs, List<Ace> removeACEs) {
		throw new CmisRuntimeException("not implemented");
	}

	public Document createDocumentFromSource(Document source,
			List<Property<?>> properties, VersioningState versioningState,
			List<Policy> policies, List<Ace> addACEs, List<Ace> removeACEs) {
		throw new CmisRuntimeException("not implemented");
	}

	public Folder createFolder(List<Property<?>> properties,
			List<Policy> policies, List<Ace> addACEs, List<Ace> removeACEs) {

		Folder f = this.session.getObjectFactory().createFolder(this,
				properties, policies, addACEs, removeACEs);
		return f;
	}

	public Policy createPolicy(List<Property<?>> properties,
			List<Policy> policies, List<Ace> addACEs, List<Ace> removeACEs) {
		throw new CmisRuntimeException("not implemented");
	}

	public List<String> deleteTree(boolean allVersions, UnfileObjects unfile,
			boolean continueOnFailure) {
		String repositoryId = this.session.getRepositoryInfo().getId();

		FailedToDeleteData failed = this.session.getProvider()
				.getObjectService().deleteTree(repositoryId,
						this.objectData.getId(), allVersions, unfile,
						continueOnFailure, null);

		return failed.getIds();
	}

	public List<ObjectType> getAllowedChildObjectTypes() {
		throw new CmisRuntimeException("not implemented");
	}

	public PagingList<Document> getCheckedOutDocs(String orderby,
			int itemsPerPage) {
		throw new CmisRuntimeException("not implemented");
	}

	public PagingList<CmisObject> getChildren(String orderby, int itemsPerPage) {
		throw new CmisRuntimeException("not implemented");
	}

	public TreeMap<String, CmisObject> getDescendants(int depth) {
		throw new CmisRuntimeException("not implemented");
	}

	public Folder getFolderParent() {
		throw new CmisRuntimeException("not implemented");
	}

	public TreeMap<String, CmisObject> getFolderTree(int depth) {
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
		return this.objectData.getId();
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

	public String getPath() {
		throw new CmisRuntimeException("not implemented");
	}

	public String getPathSegment() {
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

	/**
	 * Create folder in backend
	 * 
	 * @param parent
	 * @param properties
	 * @param policies
	 * @param addACEs
	 * @param removeACEs
	 */
	public void create(Folder parent, List<Property<?>> properties,
			List<Policy> policies, List<Ace> addAce, List<Ace> removeAce) {

		String repositoryId = this.session.getRepositoryInfo().getId();
		String parentFolderId = parent.getId();
		PropertiesData pd = this.convertToPropertiesData(properties);
		List<String> pol = this.convertToPoliciesData(policies);
		AccessControlList addAcl = this.convertToAcl(addAce);
		AccessControlList removeAcl = this.convertToAcl(removeAce);

		String objectId = this.session.getProvider().getObjectService()
				.createFolder(repositoryId, pd, parentFolderId, pol, addAcl,
						removeAcl, null);
		this.objectData = this.session.getProvider().getObjectService()
				.getObject(repositoryId, objectId, null, false,
						IncludeRelationships.NONE, null, true, true, null);

		this.session.getCache().put(this);
	}

	private AccessControlList convertToAcl(List<Ace> aceList) {
		AccessControlListImpl acli = null;
		AccessControlEntryImpl acei;
		List<AccessControlEntry> aceiList = null;
		AccessControlPrincipalDataImpl acpdi = null;

		if (aceList != null) {
			acli = new AccessControlListImpl();
			aceiList = new ArrayList<AccessControlEntry>();

			for (Ace aceEntry : aceList) {
				acei = new AccessControlEntryImpl();
				acei.setPermissions(aceEntry.getPermissionsNames());
				acpdi = new AccessControlPrincipalDataImpl(aceEntry
						.getPrincipalId());
				acei.setPrincipal(acpdi);

				aceiList.add(acei);
			}

			acli.setAces(aceiList);
		}
		return acli;
	}

	private List<String> convertToPoliciesData(List<Policy> policies) {
		List<String> pList = null;

		if (policies != null) {
			pList = new ArrayList<String>();
			for (Policy pol : policies) {
				pList.add(pol.getPolicyText());
			}
		}
		return pList;
	}

	@SuppressWarnings("unchecked")
	private PropertiesData convertToPropertiesData(
			List<Property<?>> origProperties) {
		ProviderObjectFactory of = this.session.getProvider()
				.getObjectFactory();

		List<PropertyData<?>> convProperties = new ArrayList<PropertyData<?>>();
		PropertyData<?> convProperty = null;

		convProperties.add(of.createPropertyStringData(PropertyIds.CMIS_NAME,
				"testfolder"));
		convProperties.add(of.createPropertyIdData(
				PropertyIds.CMIS_OBJECT_TYPE_ID, "cmis_Folder"));

		for (Property<?> origProperty : origProperties) {

			switch (origProperty.getType()) {
			case BOOLEAN:
				Property<Boolean> pb = (Property<Boolean>) origProperty;
				convProperty = of.createPropertyBooleanData(pb.getId(), pb
						.getValue());
				break;
			case DATETIME:
				Property<GregorianCalendar> pg = (Property<GregorianCalendar>) origProperty;
				convProperty = of.createPropertyDateTimeData(pg.getId(), pg
						.getValue());
				break;
			case DECIMAL:
				Property<BigDecimal> pd = (Property<BigDecimal>) origProperty;
				convProperty = of.createPropertyDecimalData(pd.getId(), pd
						.getValue());
				break;
			case HTML:
				Property<String> ph = (Property<String>) origProperty;
				convProperty = of.createPropertyHtmlData(ph.getId(), ph
						.getValue());
				break;
			case ID:
				Property<String> pi = (Property<String>) origProperty;
				convProperty = of.createPropertyIdData(pi.getId(), pi
						.getValue());
				break;
			case INTEGER:
				Property<BigInteger> pn = (Property<BigInteger>) origProperty;
				convProperty = of.createPropertyIntegerData(pn.getId(), pn
						.getValue());
				break;
			case STRING:
				Property<String> ps = (Property<String>) origProperty;
				convProperty = of.createPropertyStringData(ps.getId(), ps
						.getValue());
				break;
			case URI:
				Property<String> pu = (Property<String>) origProperty;
				convProperty = of.createPropertyUriData(pu.getId(), pu
						.getValue());
				break;
			default:
				throw new CmisRuntimeException("unsupported property type"
						+ origProperty.getType());
			}
			convProperties.add(convProperty);
		}

		PropertiesData pd = of.createPropertiesData(convProperties);

		return pd;
	}

}
