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

/**
 * CMIS Relationship.
 * 
 * Domain Model 2.6
 */
public interface Relationship extends CmisObject {

    // relationship specific properties

    /**
     * Gets the source object.
     */
    CmisObject getSource(); // cmis:sourceId

    /**
     * Gets the source object using the given {@link OperationContext}.
     */
    CmisObject getSource(OperationContext context);

    /**
     * Gets the target object.
     */
    CmisObject getTarget(); // cmis:targetId

    /**
     * Gets the target object using the given {@link OperationContext}.
     */
    CmisObject getTarget(OperationContext context);
}
