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

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.api.AclCapabilities;
import org.apache.chemistry.opencmis.commons.api.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.api.PermissionMapping;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.SupportedPermissions;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AclCapabilitiesDataImpl extends AbstractExtensionData implements AclCapabilities {

    private static final long serialVersionUID = 1L;

    private SupportedPermissions fSupportedPermissions;
    private AclPropagation fACLPropagation;
    private Map<String, PermissionMapping> permissionMapping;
    private List<PermissionDefinition> fPermissionDefinitionList;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.opencmis.client.provider.ACLCapabilitiesData#getACLPropagation
     * ()
     */
    public SupportedPermissions getSupportedPermissions() {
        return fSupportedPermissions;
    }

    public void setSupportedPermissions(SupportedPermissions supportedPermissions) {
        fSupportedPermissions = supportedPermissions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.opencmis.client.provider.ACLCapabilitiesData#getACLPropagation
     * ()
     */
    public AclPropagation getAclPropagation() {
        return fACLPropagation;
    }

    public void setAclPropagation(AclPropagation aclPropagation) {
        fACLPropagation = aclPropagation;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.apache.opencmis.client.provider.ACLCapabilitiesData#
     * getPermissionMappingData()
     */
    public Map<String, PermissionMapping> getPermissionMapping() {
        return permissionMapping;
    }

    public void setPermissionMappingData(Map<String, PermissionMapping> permissionMapping) {
        this.permissionMapping = permissionMapping;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.apache.opencmis.client.provider.ACLCapabilitiesData#
     * getPermissionDefinitionData()
     */
    public List<PermissionDefinition> getPermissions() {
        return fPermissionDefinitionList;
    }

    public void setPermissionDefinitionData(List<PermissionDefinition> permissionDefinitionList) {
        fPermissionDefinitionList = permissionDefinitionList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ACL Capabilities [ACL propagation=" + fACLPropagation + ", permission definition list="
                + fPermissionDefinitionList + ", permission mappings=" + permissionMapping + "]" + super.toString();
    }
}
