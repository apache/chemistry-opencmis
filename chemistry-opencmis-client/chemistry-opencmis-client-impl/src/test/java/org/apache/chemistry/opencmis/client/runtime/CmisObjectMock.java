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
package org.apache.chemistry.opencmis.client.runtime;

import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.TransientCmisObject;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.ExtensionLevel;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;

public class CmisObjectMock implements CmisObject, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String id;

    public CmisObjectMock(String id) {
        this.id = id;
    }

    public Acl addAcl(List<Ace> addAces, AclPropagation aclPropagation) {
        return null;
    }

    public Acl applyAcl(List<Ace> addAces, List<Ace> removeAces, AclPropagation aclPropagation) {
        return null;
    }

    public Acl setAcl(List<Ace> aces) {
        return null;
    }

    public void applyPolicy(ObjectId policyId) {
    }

    public void delete() {
    }

    public void delete(boolean allVersions) {
    }

    public Acl getAcl() {
        return null;
    }

    public Acl getAcl(boolean onlyBasicPermissions) {
        return null;
    }

    public AllowableActions getAllowableActions() {
        return null;
    }

    public ObjectType getBaseType() {
        return null;
    }

    public BaseTypeId getBaseTypeId() {
        return null;
    }

    public String getChangeToken() {
        return null;
    }

    public String getCreatedBy() {
        return null;
    }

    public GregorianCalendar getCreationDate() {
        return null;
    }

    public GregorianCalendar getLastModificationDate() {
        return null;
    }

    public String getLastModifiedBy() {
        return null;
    }

    public String getName() {
        return null;
    }

    public List<Policy> getPolicies() {
        return null;
    }

    public List<Property<?>> getProperties() {
        return null;
    }

    public <T> Property<T> getProperty(String id) {
        return null;
    }

    public <T> T getPropertyValue(String id) {
        return null;
    }

    public long getRefreshTimestamp() {
        return 0;
    }

    public List<Relationship> getRelationships() {
        return null;
    }

    public ItemIterable<Relationship> getRelationships(boolean includeSubRelationshipTypes,
            RelationshipDirection relationshipDirection, ObjectType type, OperationContext context) {
        return null;
    }

    public List<Rendition> getRenditions() {
        return null;
    }

    public ObjectType getType() {
        return null;
    }

    public List<CmisExtensionElement> getExtensions(ExtensionLevel level) {
        return null;
    }

    public void refresh() {

    }

    public void refreshIfOld(long durationInMillis) {

    }

    public Acl removeAcl(List<Ace> removeAces, AclPropagation aclPropagation) {
        return null;
    }

    public ObjectId updateProperties() {
        return null;
    }

    public CmisObject updateProperties(Map<String, ?> properties) {
        return null;
    }

    public ObjectId updateProperties(Map<String, ?> properties, boolean refresh) {
        return null;
    }

    public void applyPolicy(ObjectId... policyIds) {

    }

    public void removePolicy(ObjectId... policyIds) {
    }

    public String getId() {
        return this.id;
    }

    public <T> T getAdapter(Class<T> adapterInterface) {
        return null;
    }

    public TransientCmisObject getTransientObject() {
        return null;
    }
}
