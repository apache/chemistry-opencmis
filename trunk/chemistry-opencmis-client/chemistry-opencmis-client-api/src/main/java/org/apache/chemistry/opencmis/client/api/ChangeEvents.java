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

/**
 * Change event list.
 * 
 * @cmis 1.0
 */
public interface ChangeEvents {

    /**
     * Returns the changelog token if available.
     */
    String getLatestChangeLogToken();

    /**
     * Returns the change event list.
     */
    List<ChangeEvent> getChangeEvents();

    /**
     * Indicates whether are more change events or not.
     */
    boolean getHasMoreItems();

    /**
     * Returns the total number of change events if available.
     */
    long getTotalNumItems();
}
