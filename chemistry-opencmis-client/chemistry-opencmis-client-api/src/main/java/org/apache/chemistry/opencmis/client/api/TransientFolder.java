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

public interface TransientFolder extends TransientFileableCmisObject, FolderProperties {

    Document createDocument(Map<String, ?> properties, ContentStream contentStream, VersioningState versioningState,
            List<Policy> policies, List<Ace> addAces, List<Ace> removeAces, OperationContext context);

    Document createDocument(Map<String, ?> properties, ContentStream contentStream, VersioningState versioningState);

    Document createDocumentFromSource(ObjectId source, Map<String, ?> properties, VersioningState versioningState,
            List<Policy> policies, List<Ace> addAces, List<Ace> removeAces, OperationContext context);

    Document createDocumentFromSource(ObjectId source, Map<String, ?> properties, VersioningState versioningState);

    Folder createFolder(Map<String, ?> properties, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces,
            OperationContext context);

    Folder createFolder(Map<String, ?> properties);

    Policy createPolicy(Map<String, ?> properties, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces,
            OperationContext context);

    Policy createPolicy(Map<String, ?> properties);

    void deleteTree(boolean allversions, UnfileObject unfile, boolean continueOnFailure);

    List<Tree<FileableCmisObject>> getFolderTree(int depth);

    List<Tree<FileableCmisObject>> getFolderTree(int depth, OperationContext context);

    List<Tree<FileableCmisObject>> getDescendants(int depth);

    List<Tree<FileableCmisObject>> getDescendants(int depth, OperationContext context);

    ItemIterable<CmisObject> getChildren();

    ItemIterable<CmisObject> getChildren(OperationContext context);

    boolean isRootFolder();

    Folder getFolderParent();

    String getPath();

    ItemIterable<Document> getCheckedOutDocs();

    ItemIterable<Document> getCheckedOutDocs(OperationContext context);

    void setAllowedChildObjectTypes(List<ObjectType> types);
}
