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
package org.apache.chemistry.opencmis.commons.data;

import java.io.Serializable;

import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityOrderBy;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;

public interface RepositoryCapabilities extends Serializable, ExtensionsData {

    // Object

    /**
     * 
     * @cmis 1.0
     */
    CapabilityContentStreamUpdates getContentStreamUpdatesCapability();

    /**
     * 
     * @cmis 1.0
     */
    CapabilityChanges getChangesCapability();

    /**
     * 
     * @cmis 1.0
     */
    CapabilityRenditions getRenditionsCapability();

    // Navigation

    /**
     * 
     * @cmis 1.0
     */
    Boolean isGetDescendantsSupported();

    /**
     * 
     * @cmis 1.0
     */
    Boolean isGetFolderTreeSupported();

    /**
     * 
     * @cmis 1.1
     */
    CapabilityOrderBy getOrderByCapability();

    // Filing

    /**
     * 
     * @cmis 1.0
     */
    Boolean isMultifilingSupported();

    /**
     * 
     * @cmis 1.0
     */
    Boolean isUnfilingSupported();

    /**
     * 
     * @cmis 1.0
     */
    Boolean isVersionSpecificFilingSupported();

    // Versioning

    /**
     * 
     * @cmis 1.0
     */
    Boolean isPwcSearchableSupported();

    /**
     * 
     * @cmis 1.0
     */
    Boolean isPwcUpdatableSupported();

    /**
     * 
     * @cmis 1.0
     */
    Boolean isAllVersionsSearchableSupported();

    // Query

    /**
     * 
     * @cmis 1.0
     */
    CapabilityQuery getQueryCapability();

    /**
     * 
     * @cmis 1.0
     */
    CapabilityJoin getJoinCapability();

    // ACLs

    /**
     * 
     * @cmis 1.0
     */
    CapabilityAcl getAclCapability();

    // Type mutability

    /**
     * 
     * @cmis 1.1
     */
    CreatablePropertyTypes getCreatablePropertyTypes();

    /**
     * 
     * @cmis 1.1
     */
    NewTypeSettableAttributes getNewTypeSettableAttributes();

}
