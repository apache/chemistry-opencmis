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
package org.apache.chemistry.opencmis.tck.tests.query;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;

import java.util.Map;

import org.apache.chemistry.opencmis.client.api.ChangeEvent;
import org.apache.chemistry.opencmis.client.api.ChangeEvents;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Content Changes smoke test.
 */
public class ContentChangesSmokeTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Content Changes Smoke Test");
        setDescription("Calls getContentChanges(). It does not check if the results are correct!");
    }

    @Override
    public void run(Session session) {
        CmisTestResult f;

        if (supportsContentChanges(session)) {
            ChangeEvents events = session.getContentChanges(null, true, 1000, SELECT_ALL_NO_CACHE_OC);

            f = createResult(FAILURE, "Change events are null!");
            addResult(assertNotNull(events, null, f));

            if (events != null && events.getChangeEvents() != null) {

                if (getBinding() != BindingType.ATOMPUB) {
                    // the AtompPub binding does not return a change log token
                    f = createResult(FAILURE, "Change log token is null!");
                    addResult(assertNotNull(events.getLatestChangeLogToken(), null, f));
                }

                for (ChangeEvent event : events.getChangeEvents()) {
                    f = createResult(FAILURE, "Object Id is not set!");
                    addResult(assertStringNotEmpty(event.getObjectId(), null, f));

                    f = createResult(FAILURE, "Change Type is not set! Id: " + event.getObjectId());
                    addResult(assertNotNull(event.getChangeType(), null, f));

                    f = createResult(FAILURE, "Change Time is not set! Id: " + event.getObjectId());
                    addResult(assertNotNull(event.getChangeTime(), null, f));

                    if (event.getObjectId() != null) {
                        if (event.getChangeType() == ChangeType.DELETED) {
                            try {
                                session.getObject(event.getObjectId(), SELECT_ALL_NO_CACHE_OC);
                                addResult(createResult(FAILURE,
                                        "Change event indicates that an object has been deleted but it still exists. Id: "
                                                + event.getObjectId()));
                            } catch (CmisObjectNotFoundException e) {
                            }
                        } else {
                            try {
                                CmisObject object = session.getObject(event.getObjectId(), SELECT_ALL_NO_CACHE_OC);
                                addResult(checkObject(session, object, getAllProperties(object), "Object check. Id: "
                                        + event.getObjectId()));
                            } catch (CmisObjectNotFoundException e) {
                                // object might have been deleted later
                            }
                        }
                    }
                }
            }
        } else {
            addResult(createResult(SKIPPED, "Content Changes not supported. Test Skipped!"));
        }
    }

    protected boolean supportsContentChanges(Session session) {
        RepositoryInfo repository = session.getRepositoryInfo();

        if (repository.getCapabilities().getChangesCapability() == null) {
            return false;
        }

        return repository.getCapabilities().getChangesCapability() != CapabilityChanges.NONE;
    }
}
