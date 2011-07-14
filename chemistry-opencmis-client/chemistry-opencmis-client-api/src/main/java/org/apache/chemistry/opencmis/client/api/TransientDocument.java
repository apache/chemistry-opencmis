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
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

public interface TransientDocument extends TransientFileableCmisObject, DocumentProperties {

    void deleteAllVersions();

    ContentStream getContentStream();

    ContentStream getContentStream(String streamId);

    void setContentStream(ContentStream contentStream, boolean overwrite);

    void deleteContentStream();

    Document getObjectOfLatestVersion(boolean major);

    Document getObjectOfLatestVersion(boolean major, OperationContext context);

    List<Document> getAllVersions();

    List<Document> getAllVersions(OperationContext context);

    Document copy(ObjectId targetFolderId);

    Document copy(ObjectId targetFolderId, Map<String, ?> properties, VersioningState versioningState,
            List<Policy> policies, List<Ace> addACEs, List<Ace> removeACEs, OperationContext context);

    ObjectId checkIn(boolean major, String checkinComment);
}
