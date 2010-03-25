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
package org.apache.opencmis.fileshare;

import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.server.spi.ObjectInfoImpl;

public class FolderInfo extends ObjectInfoImpl {

  public FolderInfo() {
    setBaseType(BaseObjectTypeIds.CMIS_FOLDER);
    setContentType(null);
    setFileName(null);
    setHasAcl(true);
    setHasContent(false);
    setVersionSeriesId(null);
    setIsCurrentVersion(true);
    setRelationshipSourceIds(null);
    setRelationshipTargetIds(null);
    setRenditionInfos(null);
    setSupportsDescendants(true);
    setSupportsFolderTree(true);
    setSupportsPolicies(false);
    setSupportsRelationships(false);
    setWorkingCopyId(null);
    setWorkingCopyOriginalId(null);
  }
}
