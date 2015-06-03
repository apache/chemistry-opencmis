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
package org.apache.chemistry.opencmis.tck.tests.crud;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Item;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Simple folder test.
 */
public class CreateAndDeleteItemTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Create and Delete Item Test");
        setDescription("Creates a few items, checks the newly created items and their parent and finally deletes the created items.");
    }

    @Override
    public void run(Session session) {
        if (session.getRepositoryInfo().getCmisVersion() == CmisVersion.CMIS_1_0) {
            addResult(createResult(SKIPPED, "Items are not supported by CMIS 1.0. Test skipped!"));
            return;
        }

        if (hasItems(session)) {
            CmisTestResult f;

            int numOfItems = 20;

            // create a test folder
            Folder testFolder = createTestFolder(session);

            try {
                Map<String, Item> items = new HashMap<String, Item>();

                // create items
                for (int i = 0; i < numOfItems; i++) {
                    Item newItem = createItem(session, testFolder, "item" + i);
                    items.put(newItem.getId(), newItem);
                }

                // check if all items are there
                ItemIterable<CmisObject> children = testFolder.getChildren(SELECT_ALL_NO_CACHE_OC);
                List<String> childrenIds = new ArrayList<String>();
                for (CmisObject child : children) {
                    if (child != null) {
                        childrenIds.add(child.getId());
                        Item item = items.get(child.getId());

                        f = createResult(FAILURE, "Item and test folder child don't match! Id: " + child.getId());
                        addResult(assertShallowEquals(item, child, null, f));
                    }
                }

                f = createResult(FAILURE, "Number of created items does not match the number of existing items!");
                addResult(assertEquals(numOfItems, childrenIds.size(), null, f));

                for (Item item : items.values()) {
                    if (!childrenIds.contains(item.getId())) {
                        addResult(createResult(FAILURE,
                                "Created item not found in test folder children! Id: " + item.getId()));
                    }
                }

                // delete all item
                for (Item item : items.values()) {
                    item.delete(true);

                    f = createResult(FAILURE,
                            "Item should not exist anymore but it is still there! Id: " + item.getId());
                    addResult(assertIsFalse(exists(item), null, f));
                }
            } finally {
                // delete the test folder
                deleteTestFolder();
            }

            addResult(createInfoResult("Tested the creation and deletion of " + numOfItems + " items."));
        } else {
            addResult(createResult(SKIPPED, "Items not supported. Test skipped!"));
        }
    }
}
