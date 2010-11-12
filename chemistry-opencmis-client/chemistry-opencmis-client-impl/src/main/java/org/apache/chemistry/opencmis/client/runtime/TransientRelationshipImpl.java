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

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.TransientRelationship;
import org.apache.chemistry.opencmis.commons.PropertyIds;

public class TransientRelationshipImpl extends AbstractTransientCmisObject implements TransientRelationship {

    public CmisObject getSource() {
        return getSource(getSession().getDefaultContext());
    }

    public CmisObject getSource(OperationContext context) {
        return getSession().getObject(getSourceId(), context);
    }

    public ObjectId getSourceId() {
        String sourceId = getPropertyValue(PropertyIds.SOURCE_ID);
        if ((sourceId == null) || (sourceId.length() == 0)) {
            return null;
        }

        return getSession().createObjectId(sourceId);
    }

    public void setSourceId(ObjectId id) {
        if ((id == null) || (id.getId() == null) || (id.getId().length() == 0)) {
            throw new IllegalArgumentException("Id is invalid!");
        }

        setPropertyValue(PropertyIds.SOURCE_ID, id.getId());
    }

    public CmisObject getTarget() {
        return getTarget(getSession().getDefaultContext());
    }

    public CmisObject getTarget(OperationContext context) {
        return getSession().getObject(getTargetId(), context);
    }

    public ObjectId getTargetId() {
        String targetId = getPropertyValue(PropertyIds.TARGET_ID);
        if ((targetId == null) || (targetId.length() == 0)) {
            return null;
        }

        return getSession().createObjectId(targetId);
    }

    public void setTargetId(ObjectId id) {
        if ((id == null) || (id.getId() == null) || (id.getId().length() == 0)) {
            throw new IllegalArgumentException("Id is invalid!");
        }

        setPropertyValue(PropertyIds.TARGET_ID, id.getId());
    }
}
