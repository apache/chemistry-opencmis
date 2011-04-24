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

import java.util.List;

import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.TransientFileableCmisObject;

public abstract class AbstractTransientFileableCmisObject extends AbstractTransientCmisObject implements
        TransientFileableCmisObject {

    public FileableCmisObject move(ObjectId sourceFolderId, ObjectId targetFolderId) {
        return ((FileableCmisObject) getCmisObject()).move(sourceFolderId, targetFolderId);
    }

    public List<Folder> getParents() {
        return ((FileableCmisObject) getCmisObject()).getParents();
    }

    public List<String> getPaths() {
        return ((FileableCmisObject) getCmisObject()).getPaths();
    }

    public void addToFolder(ObjectId folderId, boolean allVersions) {
        ((FileableCmisObject) getCmisObject()).addToFolder(folderId, allVersions);
    }

    public void removeFromFolder(ObjectId folderId) {
        ((FileableCmisObject) getCmisObject()).removeFromFolder(folderId);
    }
}
