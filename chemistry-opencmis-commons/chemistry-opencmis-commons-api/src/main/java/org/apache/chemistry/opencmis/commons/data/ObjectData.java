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

import java.util.List;

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

/**
 * Base object for CMIS documents, folders, relationships and policies.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public interface ObjectData extends ExtensionsData {

    /**
     * Returns the object id.
     * 
     * @return the object id or <code>null</code> if the object id is unknown
     */
    String getId();

    /**
     * Returns the base object type.
     * 
     * @return the base object type or <code>null</code> if the base object type
     *         is unknown
     */
    BaseTypeId getBaseTypeId();

    /**
     * Returns the object properties. The properties can be incomplete if a
     * property filter was used.
     * 
     * @return the properties or <code>null</code> if no properties are known
     */
    Properties getProperties();

    /**
     * Returns the allowable actions.
     * 
     * @return the allowable actions or <code>null</code> if the allowable
     *         actions are unknown
     */
    AllowableActions getAllowableActions();

    /**
     * Returns the relationships from and to this object.
     * 
     * @return the list of relationship objects or <code>null</code> if no
     *         relationships exist or the relationships are unknown
     */
    List<ObjectData> getRelationships();

    /**
     * Returns the change event infos.
     * 
     * @return the change event infos or <code>null</code> if the infos are
     *         unknown
     */
    ChangeEventInfo getChangeEventInfo();

    /**
     * Returns the access control list.
     * 
     * @return the access control list or <code>null</code> if the access
     *         control list is unknown
     */
    Acl getAcl();

    /**
     * Returns if the access control list reflects the exact permission set in
     * the repository.
     * 
     * @return <code>true<code> - exact; <code>false</code> - not exact, other
     *         permission constraints exist; <code>null</code> - unknown
     */
    Boolean isExactAcl();

    /**
     * Returns the ids of the applied policies.
     * 
     * @return the policy ids or <code>null</code> if no policies are applied or
     *         the ids are unknown
     */
    PolicyIdList getPolicyIds();

    /**
     * Returns the renditions of this object.
     * 
     * @return the list of renditions (might be empty) or <code>null</code> if
     *         no renditions exist or the renditions are unknown
     */
    List<RenditionData> getRenditions();
}