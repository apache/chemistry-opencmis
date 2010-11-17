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

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ChangeEventInfo;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.PolicyIdList;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

/**
 * ObjectData implementation.
 */
public class ObjectDataImpl extends AbstractExtensionData implements ObjectData {

    private static final long serialVersionUID = 1L;

    private Properties properties;
    private ChangeEventInfo changeEventInfo;
    private List<ObjectData> relationships;
    private List<RenditionData> renditions;
    private PolicyIdList policyIds;
    private AllowableActions allowableActions;
    private Acl acl;
    private Boolean isExactAcl;

    public String getId() {
        Object value = getFirstValue(PropertyIds.OBJECT_ID);
        if (value instanceof String) {
            return (String) value;
        }

        return null;
    }

    public BaseTypeId getBaseTypeId() {
        Object value = getFirstValue(PropertyIds.BASE_TYPE_ID);
        if (value instanceof String) {
            try {
                return BaseTypeId.fromValue((String) value);
            } catch (Exception e) {
            }
        }

        return null;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public ChangeEventInfo getChangeEventInfo() {
        return changeEventInfo;
    }

    public void setChangeEventInfo(ChangeEventInfo changeEventInfo) {
        this.changeEventInfo = changeEventInfo;
    }

    public List<ObjectData> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<ObjectData> relationships) {
        this.relationships = relationships;
    }

    public List<RenditionData> getRenditions() {
        return renditions;
    }

    public void setRenditions(List<RenditionData> renditions) {
        this.renditions = renditions;
    }

    public PolicyIdList getPolicyIds() {
        return policyIds;
    }

    public void setPolicyIds(PolicyIdList policyIds) {
        this.policyIds = policyIds;
    }

    public AllowableActions getAllowableActions() {
        return allowableActions;
    }

    public void setAllowableActions(AllowableActions allowableActions) {
        this.allowableActions = allowableActions;
    }

    public Acl getAcl() {
        return acl;
    }

    public void setAcl(Acl acl) {
        this.acl = acl;
    }

    public Boolean isExactAcl() {
        return isExactAcl;
    }

    public void setIsExactAcl(Boolean isExactACL) {
        this.isExactAcl = isExactACL;
    }

    // ---- internal ----

    /**
     * Returns the first value of a property or <code>null</code> if the
     * property is not set.
     */
    private Object getFirstValue(String id) {
        if ((properties == null) || (properties.getProperties() == null)) {
            return null;
        }

        PropertyData<?> property = properties.getProperties().get(id);
        if (property == null) {
            return null;
        }

        return property.getFirstValue();
    }

    @Override
    public String toString() {
        return "Object Data [properties=" + properties + ", allowable actions=" + allowableActions
                + ", change event info=" + changeEventInfo + ", ACL=" + acl + ", is exact ACL=" + isExactAcl
                + ", policy ids=" + policyIds + ", relationships=" + relationships + ", renditions=" + renditions
                + "]" + super.toString();
    }
}
