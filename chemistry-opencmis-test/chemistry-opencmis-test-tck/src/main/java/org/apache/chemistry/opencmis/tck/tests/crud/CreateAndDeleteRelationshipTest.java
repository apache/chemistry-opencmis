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

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

/**
 * Simple folder test.
 */
public class CreateAndDeleteRelationshipTest extends AbstractSessionTest {

	@Override
	public void init(Map<String, String> parameters) {
		super.init(parameters);
		setName("Create and Delete Relationship Test");
		setDescription("Creates a relationship between two documents, checks the newly created relationship and finally deletes the created relationship.");
	}

	@Override
	public void run(Session session) {
		CmisTestResult f;
		boolean found;

		if (hasRelationships(session)) {
			// create a test folder
			Folder testFolder = createTestFolder(session);

			try {
				// create documents
				Document doc1 = createDocument(session, testFolder, "doc1.txt",
						"doc1");
				Document doc2 = createDocument(session, testFolder, "doc2.txt",
						"doc2");

				// create relationship
				Relationship rel = createRelationship(session, "rel1", doc1,
						doc2);

				f = createResult(FAILURE,
						"Source document id does not match relationship source id!");
				addResult(assertEquals(doc1.getId(), rel.getSourceId().getId(),
						null, f));

				f = createResult(FAILURE,
						"Target document id does not match relationship target id!");
				addResult(assertEquals(doc2.getId(), rel.getTarget().getId(),
						null, f));

				// check the source document
				doc1.refresh();
				List<Relationship> doc1rels = doc1.getRelationships();

				f = createResult(FAILURE,
						"Source document has no relationships but must have at least one!");
				addResult(assertListNotEmpty(doc1rels, null, f));

				if (doc1rels != null) {
					found = false;
					for (Relationship r : doc1rels) {
						if (rel.getId().equals(r.getId())) {
							found = true;
							break;
						}
					}

					f = createResult(
							FAILURE,
							"Newly created relationship not found in the relationships of the source document!");
					addResult(assertIsTrue(found, null, f));
				}

				found = false;
				for (Relationship r : session.getRelationships(doc1, true,
						RelationshipDirection.SOURCE, null,
						SELECT_ALL_NO_CACHE_OC)) {
					if (rel.getId().equals(r.getId())) {
						found = true;
						break;
					}
				}

				f = createResult(
						FAILURE,
						"Newly created relationship not found in the relationships returned by getObjectRelationships() for the source document!");
				addResult(assertIsTrue(found, null, f));

				// check the target document
				doc2.refresh();
				List<Relationship> doc2rels = doc2.getRelationships();

				f = createResult(FAILURE,
						"Target document has no relationships but must have at least one!");
				addResult(assertListNotEmpty(doc2rels, null, f));

				if (doc2rels != null) {
					found = false;
					for (Relationship r : doc2rels) {
						if (rel.getId().equals(r.getId())) {
							found = true;
							break;
						}
					}

					f = createResult(
							FAILURE,
							"Newly created relationship not found in the relationships of the target document!");
					addResult(assertIsTrue(found, null, f));
				}

				found = false;
				for (Relationship r : session.getRelationships(doc2, true,
						RelationshipDirection.TARGET, null,
						SELECT_ALL_NO_CACHE_OC)) {
					if (rel.getId().equals(r.getId())) {
						found = true;
						break;
					}
				}

				f = createResult(
						FAILURE,
						"Newly created relationship not found in the relationships returned by getObjectRelationships() for the target document!");
				addResult(assertIsTrue(found, null, f));

				// remove
				deleteObject(rel);
				deleteObject(doc2);
				deleteObject(doc1);
			} finally {
				// delete the test folder
				deleteTestFolder();
			}
		} else {
			addResult(createResult(SKIPPED,
					"Relationships not supported. Test Skipped!"));
		}
	}
}
