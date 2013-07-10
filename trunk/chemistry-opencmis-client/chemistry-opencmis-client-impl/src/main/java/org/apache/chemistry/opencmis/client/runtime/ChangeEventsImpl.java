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

import java.util.List;

import org.apache.chemistry.opencmis.client.api.ChangeEvent;
import org.apache.chemistry.opencmis.client.api.ChangeEvents;

public class ChangeEventsImpl implements ChangeEvents {

    private String latestChangeLogToken;
    private List<ChangeEvent> events;
    private boolean hasMoreItems = false;
    private long totalNumItems = -1;

    public ChangeEventsImpl() {
    }

    public ChangeEventsImpl(String latestChangeLogToken, List<ChangeEvent> events, boolean hasMoreItems,
            long totalNumItems) {
        setLatestChangeLogToken(latestChangeLogToken);
        setChangeEvents(events);
        setHasMoreItems(hasMoreItems);
        setTotalNumItems(totalNumItems);
    }

    public String getLatestChangeLogToken() {
        return latestChangeLogToken;
    }

    public void setLatestChangeLogToken(String latestChangeLogToken) {
        this.latestChangeLogToken = latestChangeLogToken;
    }

    public List<ChangeEvent> getChangeEvents() {
        return events;
    }

    public void setChangeEvents(List<ChangeEvent> events) {
        this.events = events;
    }

    public boolean getHasMoreItems() {
        return hasMoreItems;
    }

    public void setHasMoreItems(boolean hasMoreItems) {
        this.hasMoreItems = hasMoreItems;
    }

    public void setTotalNumItems(long totalNumItems) {
        this.totalNumItems = totalNumItems;
    }

    public long getTotalNumItems() {
        return totalNumItems;
    }
}
