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
package org.apache.chemistry.opencmis.client.api;

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

/**
 * CMIS Folder.
 * <p>
 * See Domain Model 2.5
 */
public interface Folder extends FileableCmisObject, FolderProperties {

    TransientFolder getTransientFolder();

    // object service

    /**
     * Creates a new document in this folder.
     * 
     * The stream in <code>contentStream</code> is consumed but not closed by
     * this method.
     * 
     * @return the new document object or <code>null</code> if the parameter
     *         <code>context</code> was set to <code>null</code>
     */
    Document createDocument(Map<String, ?> properties, ContentStream contentStream, VersioningState versioningState,
            List<Policy> policies, List<Ace> addAces, List<Ace> removeAces, OperationContext context);

    /**
     * Creates a new document in this folder.
     * 
     * The stream in <code>contentStream</code> is consumed but not closed by
     * this method.
     * 
     * @return the new document object
     */
    Document createDocument(Map<String, ?> properties, ContentStream contentStream, VersioningState versioningState);

    /**
     * Creates a new document from a source document in this folder.
     * 
     * @return the new document object or <code>null</code> if the parameter
     *         <code>context</code> was set to <code>null</code>
     */
    Document createDocumentFromSource(ObjectId source, Map<String, ?> properties, VersioningState versioningState,
            List<Policy> policies, List<Ace> addAces, List<Ace> removeAces, OperationContext context);

    /**
     * Creates a new document from a source document in this folder.
     * 
     * @return the new document object
     */
    Document createDocumentFromSource(ObjectId source, Map<String, ?> properties, VersioningState versioningState);

    /**
     * Creates a new subfolder in this folder.
     * 
     * @return the new folder object or <code>null</code> if the parameter
     *         <code>context</code> was set to <code>null</code>
     */
    Folder createFolder(Map<String, ?> properties, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces,
            OperationContext context);

    /**
     * Creates a new subfolder in this folder.
     * 
     * @return the new folder object
     */
    Folder createFolder(Map<String, ?> properties);

    /**
     * Creates a new policy in this folder.
     * 
     * @return the new policy object or <code>null</code> if the parameter
     *         <code>context</code> was set to <code>null</code>
     */
    Policy createPolicy(Map<String, ?> properties, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces,
            OperationContext context);

    /**
     * Creates a new policy in this folder.
     * 
     * @return the new policy object
     */
    Policy createPolicy(Map<String, ?> properties);

    /**
     * Deletes this folder and all subfolders.
     * 
     * @return a list of object ids which failed to be deleted
     */
    List<String> deleteTree(boolean allversions, UnfileObject unfile, boolean continueOnFailure);

    // navigation service

    /**
     * Gets the folder tree starting with this folder.
     */
    List<Tree<FileableCmisObject>> getFolderTree(int depth);

    /**
     * Gets the folder tree starting with this folder using the given
     * {@link OperationContext}.
     */
    List<Tree<FileableCmisObject>> getFolderTree(int depth, OperationContext context);

    /**
     * Gets the folder descendants starting with this folder.
     */
    List<Tree<FileableCmisObject>> getDescendants(int depth);

    /**
     * Gets the folder descendants starting with this folder using the given
     * {@link OperationContext}.
     */
    List<Tree<FileableCmisObject>> getDescendants(int depth, OperationContext context);

    /**
     * Returns the children of this folder.
     */
    ItemIterable<CmisObject> getChildren();

    /**
     * Returns the children of this folder using the given
     * {@link OperationContext}.
     */
    ItemIterable<CmisObject> getChildren(OperationContext context);

    /**
     * Returns if the folder is the root folder.
     */
    boolean isRootFolder();

    /**
     * Gets the parent folder object
     * 
     * @return the parent folder object or <code>null</code> if the folder is
     *         the root folder.
     */
    Folder getFolderParent();

    /**
     * Returns the path of the folder.
     */
    String getPath();

    /**
     * Returns all checked out documents in this folder.
     */
    ItemIterable<Document> getCheckedOutDocs();

    /**
     * Returns all checked out documents in this folder using the given
     * {@link OperationContext}.
     */
    ItemIterable<Document> getCheckedOutDocs(OperationContext context);
}
