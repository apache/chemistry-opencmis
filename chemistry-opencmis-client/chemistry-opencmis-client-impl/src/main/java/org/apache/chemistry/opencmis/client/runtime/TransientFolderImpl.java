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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.TransientFolder;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;

public class TransientFolderImpl extends AbstractTransientFileableCmisObject implements TransientFolder {

    private boolean isMarkedForDeleteTree;
    private boolean deleteTreeAllVersions;
    private UnfileObject deleteTreeUnfile;
    private boolean deleteTreeContinueOnFailure;

    @Override
    protected void initialize(Session session, CmisObject object) {
        super.initialize(session, object);

        isMarkedForDeleteTree = false;
    }

    public Document createDocument(Map<String, ?> properties, ContentStream contentStream,
            VersioningState versioningState, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces,
            OperationContext context) {
        return ((Folder) getCmisObject()).createDocument(properties, contentStream, versioningState, policies, addAces,
                removeAces, context);
    }

    public Document createDocument(Map<String, ?> properties, ContentStream contentStream,
            VersioningState versioningState) {
        return ((Folder) getCmisObject()).createDocument(properties, contentStream, versioningState);
    }

    public Document createDocumentFromSource(ObjectId source, Map<String, ?> properties,
            VersioningState versioningState, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces,
            OperationContext context) {
        return ((Folder) getCmisObject()).createDocumentFromSource(source, properties, versioningState, policies,
                addAces, removeAces, context);
    }

    public Document createDocumentFromSource(ObjectId source, Map<String, ?> properties, VersioningState versioningState) {
        return ((Folder) getCmisObject()).createDocumentFromSource(source, properties, versioningState);
    }

    public Folder createFolder(Map<String, ?> properties, List<Policy> policies, List<Ace> addAces,
            List<Ace> removeAces, OperationContext context) {
        return ((Folder) getCmisObject()).createFolder(properties, policies, addAces, removeAces, context);
    }

    public Folder createFolder(Map<String, ?> properties) {
        return ((Folder) getCmisObject()).createFolder(properties);
    }

    public Policy createPolicy(Map<String, ?> properties, List<Policy> policies, List<Ace> addAces,
            List<Ace> removeAces, OperationContext context) {
        return ((Folder) getCmisObject()).createPolicy(properties, policies, addAces, removeAces, context);
    }

    public Policy createPolicy(Map<String, ?> properties) {
        return ((Folder) getCmisObject()).createPolicy(properties);
    }

    public void deleteTree(boolean allversions, UnfileObject unfile, boolean continueOnFailure) {
        deleteTreeAllVersions = allversions;
        deleteTreeUnfile = unfile;
        deleteTreeContinueOnFailure = continueOnFailure;

        isMarkedForDeleteTree = true;
        isMarkedForDelete = true;
        isModified = true;
    }

    public List<Tree<FileableCmisObject>> getFolderTree(int depth) {
        return ((Folder) getCmisObject()).getFolderTree(depth);
    }

    public List<Tree<FileableCmisObject>> getFolderTree(int depth, OperationContext context) {
        return ((Folder) getCmisObject()).getFolderTree(depth, context);
    }

    public List<Tree<FileableCmisObject>> getDescendants(int depth) {
        return ((Folder) getCmisObject()).getDescendants(depth);
    }

    public List<Tree<FileableCmisObject>> getDescendants(int depth, OperationContext context) {
        return ((Folder) getCmisObject()).getDescendants(depth, context);
    }

    public ItemIterable<CmisObject> getChildren() {
        return ((Folder) getCmisObject()).getChildren();
    }

    public ItemIterable<CmisObject> getChildren(OperationContext context) {
        return ((Folder) getCmisObject()).getChildren(context);
    }

    public boolean isRootFolder() {
        return ((Folder) getCmisObject()).isRootFolder();
    }

    public Folder getFolderParent() {
        return ((Folder) getCmisObject()).getFolderParent();
    }

    public String getPath() {
        return ((Folder) getCmisObject()).getPath();
    }

    public ItemIterable<Document> getCheckedOutDocs() {
        return ((Folder) getCmisObject()).getCheckedOutDocs();
    }

    public ItemIterable<Document> getCheckedOutDocs(OperationContext context) {
        return ((Folder) getCmisObject()).getCheckedOutDocs();
    }

    public String getParentId() {
        return getPropertyValue(PropertyIds.PARENT_ID);
    }

    public List<ObjectType> getAllowedChildObjectTypes() {
        List<ObjectType> result = new ArrayList<ObjectType>();

        List<String> otids = getPropertyValue(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS);
        if (otids == null) {
            return result;
        }

        for (String otid : otids) {
            result.add(getSession().getTypeDefinition(otid));
        }

        return result;
    }

    public void setAllowedChildObjectTypes(List<ObjectType> types) {
        List<String> typeIds = new ArrayList<String>();

        if ((types != null) && (!types.isEmpty())) {
            for (ObjectType type : types) {
                if (type != null) {
                    typeIds.add(type.getId());
                }
            }
        }

        setPropertyValue(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, typeIds);
    }

    // override save()

    @Override
    public ObjectId save() {
        if (!isModified()) {
            // nothing has change, so there is nothing to do
            return getObjectId();
        }

        if (isMarkedForDeleteTree) {
            FailedToDeleteData ftd = getBinding().getObjectService().deleteTree(getRepositoryId(), getId(),
                    deleteTreeAllVersions, deleteTreeUnfile, deleteTreeContinueOnFailure, null);

            if ((ftd != null) && (!ftd.getIds().isEmpty())) {
                throw new CmisConstraintException("deleteTree could not delete all folder children: " + ftd.getIds());
            }

            return null;
        }

        return super.save();
    }
}
