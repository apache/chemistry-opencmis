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
package org.apache.opencmis.client.api.util;

import java.util.Map;

/**
 * Implemented by Session class and used by JUnit or other test applications to generate
 * test data.
 */
public interface Testable {

	static String ROOT_FOLDER_ID_PARAMETER = "org.apache.opencmis.root.id"; 
	static String DOCUMENT_TYPE_ID_PARAMETER = "org.apache.opencmis.document.type.id"; 
	static String FOLDER_TYPE_ID_PARAMETER = "org.apache.opencmis.folder.type.id"; 
	
	/**
	 * Trigger a repository to generate test data
	 * @param parameter reserved parameter map
	 */
	void generateTestData(Map<String, String> parameter);

	/**
	 * Clean up generated data
	 */
	void cleanUpTestData();
}
