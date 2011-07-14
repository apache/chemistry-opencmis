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

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.ExtensionLevel;

public interface TransientCmisObject extends ObjectId, CmisObjectProperties {

    // properties

    void setName(String name);

    <T> void setPropertyValue(String id, Object value);

    // read-only

    AllowableActions getAllowableActions();

    List<Relationship> getRelationships();

    List<Rendition> getRenditions();

    // ACL

    void addAce(String principalId, List<String> permissions, AclPropagation aclPropagation);

    void removeAce(String principalId, List<String> permissions, AclPropagation aclPropagation);

    Acl getOriginalAcl();

    // policies

    void applyPolicy(Policy... policyIds);

    void removePolicy(Policy... policyIds);

    List<Policy> getPolicies();

    // delete

    void delete(boolean allVersions);

    // extensions

    List<CmisExtensionElement> getInputExtensions(ExtensionLevel level);

    List<CmisExtensionElement> getOutputExtensions(ExtensionLevel level);

    void setOutputExtensions(ExtensionLevel level, List<CmisExtensionElement> extensions);

    // save

    boolean isMarkedForDelete();

    boolean isModified();

    void reset();

    void refreshAndReset();

    ObjectId save();

    // shared object

    CmisObject getCmisObject();
}
