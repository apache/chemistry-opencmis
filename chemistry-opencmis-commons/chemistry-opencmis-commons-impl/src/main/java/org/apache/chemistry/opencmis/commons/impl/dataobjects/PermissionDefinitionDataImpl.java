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
package org.apache.chemistry.opencmis.commons.impl.dataobjects;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class PermissionDefinitionDataImpl extends AbstractExtensionData implements
        org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition {

    private static final long serialVersionUID = 1L;

    private String fPermission;
    private String fDescription;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.opencmis.client.provider.PermissionDefinitionData#getPermission
     * ()
     */
    public String getId() {
        return fPermission;
    }

    public void setPermission(String permission) {
        fPermission = permission;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.opencmis.client.provider.PermissionDefinitionData#getDescription
     * ()
     */
    public String getDescription() {
        return fDescription;
    }

    public void setDescription(String description) {
        fDescription = description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Permission Definition [permission=" + fPermission + ", description=" + fDescription + "]"
                + super.toString();
    }

}
