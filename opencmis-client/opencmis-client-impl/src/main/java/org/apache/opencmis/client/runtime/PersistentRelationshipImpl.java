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
package org.apache.opencmis.client.runtime;

import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.OperationContext;
import org.apache.opencmis.client.api.Relationship;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.provider.ObjectData;

public class PersistentRelationshipImpl extends AbstractPersistentCmisObject implements
    Relationship {

  /**
   * Constructor.
   */
  public PersistentRelationshipImpl(PersistentSessionImpl session, ObjectType objectType,
      ObjectData objectData, OperationContext context) {
    initialize(session, objectType, objectData, context);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Relationship#getSource()
   */
  public CmisObject getSource() {
    return getSource(getSession().getDefaultContext());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Relationship#getSource()
   */
  public CmisObject getSource(OperationContext context) {
    String sourceId = getPropertyValue(PropertyIds.CMIS_SOURCE_ID);
    if (sourceId == null) {
      return null;
    }

    return getSession().getObject(getSession().createObjectId(sourceId), context);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Relationship#getTarget()
   */
  public CmisObject getTarget() {
    return getTarget(getSession().getDefaultContext());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Relationship#getTarget()
   */
  public CmisObject getTarget(OperationContext context) {
    String targetId = getPropertyValue(PropertyIds.CMIS_TARGET_ID);
    if (targetId == null) {
      return null;
    }

    return getSession().getObject(getSession().createObjectId(targetId), context);
  }

}
